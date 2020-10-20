#!/usr/bin/python
#
# DiscApp Thread Activity Refresh Utility
#
# Command line usage: python thread_activity_refresh.py
#                     python thread_activity_refresh.py -n 150
#
# Script will crawl through the whole thread table and update or insert new parent ids into the thread
# activity table so that they are up to date with the latest sub thread that exists below them.
# You can also use the '-n' command line parameter to just update the latest x number of threads.
#
# Setup:
# ------
# sudo apt-get install  libpq-dev
# pip install psycopg2

from configparser import ConfigParser
import psycopg2
import argparse


def config(filename='database.conf', section='postgresql'):
    config_parser = ConfigParser()
    config_parser.read(filename)

    db = {}
    if config_parser.has_section(section):
        params = config_parser.items(section)
        for param in params:
            db[param[0]] = param[1]
    else:
        raise Exception('Section {0} not found in the {1} file'.format(section, filename))

    return db


def start_update_thread_activity(last_num_threads):
    conn = None
    try:
        params = config()
        print('Connecting to database...')
        conn = psycopg2.connect(**params)

        # create cursor
        cur = conn.cursor()

        # execute statement
        print('PostgreSQL database version:')
        cur.execute('SELECT version()')
        db_version = cur.fetchone()
        print(db_version)

        # close communication
        cur.close()

        if last_num_threads == 0:
            print('Removing existing temp table and recreating fresh...')
            cur = conn.cursor()
            sql = """DROP TABLE IF EXISTS thread_activity_temp ;"""
            print(sql)
            cur.execute(sql)
            cur.close()
            conn.commit()

            cur = conn.cursor()
            sql = """CREATE TABLE thread_activity_temp (
                        thread_id int NULL
                    ); """
            print(sql)
            cur.execute(sql)
            cur.close()
            conn.commit()
            print('Done.')
        else:
            print('Removing newest ' + str(last_num_threads) + ' threads from temp table and thread activity '
                                                               'to be re-processed.')
            cur = conn.cursor()
            sql = 'SELECT id FROM thread ' \
                  'WHERE deleted = FALSE AND is_approved = TRUE ' \
                  'ORDER BY thread.create_dt DESC ' \
                  'LIMIT ' + str(last_num_threads) + ' ;'
            print(sql)
            cur.execute(sql, last_num_threads)
            rows = cur.fetchall()

            for row in rows:
                print(row[0])
                cur = conn.cursor()
                sql = 'DELETE FROM thread_activity WHERE thread_id = ' + str(row[0])
                print(sql)
                cur.execute(sql)

                cur = conn.cursor()
                sql = 'DELETE FROM thread_activity_temp WHERE thread_id = ' + str(row[0])
                print(sql)
                cur.execute(sql)

            cur.close()
            conn.commit()

        print('Starting update of thread activity table...')
        run_thread_activity_update(conn)
        run_thread_activity_update_on_remaining_root_threads(conn)

    except (Exception, psycopg2.DatabaseError) as error:
        print(error)

    finally:
        if conn is not None:
            conn.close()
            print('Database connection closed.')


def run_thread_activity_update_on_remaining_root_threads(conn):

    print('Inserting any missing root level threads into thread activity...')
    is_done = False

    while not is_done:
        cur = conn.cursor()
        sql = """SELECT id, application_id, create_dt 
                 FROM thread 
                 WHERE 
                    parent_id = 0 
                    AND deleted = false 
                    AND is_approved = true 
                    AND id NOT IN (
                        SELECT thread_id FROM thread_activity
                    ); """
        # print(sql)

        cur.execute(sql)
        row = cur.fetchone()
        cur.close()

        if row is None:
            is_done = True
        elif row is not None:
            thread_id = row[0]
            application_id = row[1]
            create_dt = row[2]

            thread_activity_sql = """INSERT INTO thread_activity (application_id, thread_id, create_dt, mod_dt) 
                                      VALUES(%s, %s, %s, %s) RETURNING id; """
            cur = conn.cursor()
            cur.execute(thread_activity_sql, (application_id, thread_id, create_dt, create_dt))
            inserted_row_id = cur.fetchone()[0]
            conn.commit()
            cur.close()
            print('inserted new thread activity for thread_id ' + str(thread_id)
                  + ' inserted_row_id: ' + str(inserted_row_id))

            insert_into_thread_activity_temp(conn, thread_id)
    print('done.')


def run_thread_activity_update(conn):
    print('Digging through non-root level threads to set their thread activity...')
    is_done = False

    while not is_done:
        cur = conn.cursor()
        sql = """SELECT id, parent_id, create_dt, application_id FROM thread 
                 LEFT JOIN thread_activity_temp 
                    ON thread_activity_temp.thread_id = thread.id
                 WHERE 
                    thread_activity_temp.thread_id is null
                    AND PARENT_ID != 0  
                    AND deleted = FALSE  
                    AND is_approved = TRUE 
                    ORDER BY create_dt ASC ; """
        # print(sql)

        cur.execute(sql)
        row = cur.fetchone()
        cur.close()

        if row is None:
            is_done = True
        elif row is not None:
            thread_id = row[0]
            parent_id = row[1]
            create_dt = row[2]
            application_id = row[3]
            print('checking thread_id: ' + str(thread_id))
            root_thread_id = get_root_thread(conn, thread_id, parent_id)
            print('found root parent thread: ' + str(root_thread_id))

            if is_thread_activity_exists(conn, root_thread_id, application_id):
                thread_activity_sql = """UPDATE thread_activity SET mod_dt = %s  
                                      WHERE application_id = %s and thread_id = %s ;"""
                cur = conn.cursor()
                cur.execute(thread_activity_sql, (create_dt, application_id, root_thread_id))
                conn.commit()
                cur.close()
                print('Updated thread activity for thread_id ' + str(root_thread_id))

            else:
                thread_activity_sql = """INSERT INTO thread_activity (application_id, thread_id, create_dt, mod_dt) 
                                      VALUES(%s, %s, %s, %s) RETURNING id; """
                cur = conn.cursor()
                root_create_dt = get_thread_create_dt(conn, root_thread_id)
                cur.execute(thread_activity_sql, (application_id, root_thread_id, root_create_dt, create_dt))
                inserted_row_id = cur.fetchone()[0]
                conn.commit()
                cur.close()
                print('inserted new thread activity for thread_id ' + str(root_thread_id)
                      + ' inserted_row_id: ' + str(inserted_row_id))

            insert_into_thread_activity_temp(conn, thread_id)
    print('done.')


def insert_into_thread_activity_temp(conn, thread_id):
    thread_activity_sql = 'INSERT INTO thread_activity_temp (thread_id) \
                           VALUES(' + str(thread_id) + ') ; '
    cur = conn.cursor()
    cur.execute(thread_activity_sql)
    conn.commit()
    cur.close()
    print('inserted new thread activity temp for thread_id ' + str(thread_id))


def get_root_thread(conn, thread_id, parent_id):
    if parent_id != 0:
        cur = conn.cursor()
        sql = 'SELECT id, parent_id FROM thread WHERE id = ' + str(parent_id)
        print(sql)
        cur.execute(sql)
        row = cur.fetchone()
        thread_id = int(row[0])
        parent_id = int(row[1])
        print('thread_id = ' + str(thread_id) + ' parent_id = ' + str(parent_id))
    else:
        return thread_id

    return get_root_thread(conn, thread_id, parent_id)


def get_thread_create_dt(conn, thread_id):
    cur = conn.cursor()
    sql = 'SELECT create_dt FROM thread WHERE id = ' + str(thread_id)
    print(sql)
    cur.execute(sql)
    row = cur.fetchone()
    create_dt = row[0]
    print('Found create_dt ' + str(create_dt) + ' for thread_id ' + str(thread_id))
    return create_dt


def is_thread_activity_exists(conn, thread_id, application_id):
    cur = conn.cursor()
    sql = 'SELECT thread_id, application_id FROM thread_activity WHERE thread_id = %s AND application_id = %s ; '
    print(sql)
    cur.execute(sql, (thread_id, application_id))
    row = cur.fetchone()
    cur.close()

    if row is not None:
        print('Found existing thread activity: ' + str(row))
        return True
    else:
        return False


if __name__ == '__main__':

    parser = argparse.ArgumentParser(
        description='Update parent thread ids in thread_activity table with latest modified date based on'
                    'sub threads.')

    parser.add_argument('-n', '--last-num-threads', required=False, default=0, type=int,
                        help='Only update top x number of threads')
    args = parser.parse_args()
    print(args)
    if args.last_num_threads > 0:
        print('WARNING: You are about to update the latest ' + str(args.last_num_threads)
              + ' records in the thread_activity table!')
    else:
        print('WARNING: You are about to update ALL records in the thread_activity table!')
    accept = input('This action can only be reverse manually. Are you sure you want to continue? [y/N]: ')

    if accept.lower() == 'y' or accept.lower() == 'yes':
        start_update_thread_activity(args.last_num_threads)
    else:
        print("Aborting thread_activity update.")
