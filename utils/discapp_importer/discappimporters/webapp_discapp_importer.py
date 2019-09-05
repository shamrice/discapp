import psycopg2


class WebAppDiscAppImporter:

    def __init__(self, conn, app_id):
        self.conn = conn
        self.app_id = app_id

    def import_disc_app(self):

        is_done = False
        while not is_done:
            print('*********looking for next root level thread***************')
            next_root = self.__get_next_base_thread()

            if next_root is not None:
                print('NEXT ROOT LEVEL THREAD = ' + str(next_root))

                original_parent_id = next_root[0]

                self.__import_sub_thread(next_root, 0, original_parent_id)

                print('finished importing sub threads.')

            else:
                is_done = True
                print('Import complete!')

    def __get_next_base_thread(self):

        print('getting next oldest thread with parent = 0')

        cur = self.conn.cursor()
        sql = 'SELECT min(id) from disc_' + str(self.app_id) + ' WHERE parent = 0 AND is_imported = false'
        print('sql=' + sql)
        cur.execute(sql)
        row = cur.fetchone()
        cur.close()

        if row is not None:
            thread_id = row[0]
            print('next original id = ' + str(thread_id))
            if thread_id is not None:
                cur = self.conn.cursor()
                cur.execute('SELECT id, author, email, ip, user_agent, subject, '
                            + 'show_email, parent, personal_account, date_entered, threadactivity, message FROM disc_'
                            + str(self.app_id) + ' WHERE id = ' + str(thread_id))
                result = cur.fetchone()
                cur.close()

                print('found next root level row to insert')
                return result

        print('did not find next root level row to insert')
        return None

    def __get_next_threads(self, original_parent_id):

        print('getting next oldest threads with parent_id = ' + str(original_parent_id))

        if original_parent_id is not None:

            cur = self.conn.cursor()
            sql = 'SELECT id, author, email, ip, user_agent, subject, '\
                  + 'show_email, parent, personal_account, date_entered, threadactivity, message FROM disc_'\
                + str(self.app_id) + ' WHERE parent = %s AND is_imported = false ORDER by date_entered desc'

            print('sql=' + sql)
            original_parent_id = str(original_parent_id)
            cur.execute(sql, [original_parent_id])
            rows = cur.fetchall()
            cur.close()

            print('found next row(s) to insert' + str(rows))
            return rows

        print('did not find next row to insert')
        return None

    def __import_sub_thread(self, current_row, current_parent_id, original_parent_id):

        print('Importing sub-thread= ' + str(current_row))
        new_parent = self.__insert_into_thread_table(current_row, current_parent_id)
        next_rows = self.__get_next_threads(str(original_parent_id))

        if next_rows is not None:

            for next_row in next_rows:

                print('Next rows are not None. Importing row...' + str(next_row))
                original_parent_id = next_row[0]
                print('..................going down.....................')
                self.__import_sub_thread(next_row, new_parent, original_parent_id)
                print('..................back up........................')

    def __insert_into_thread_table(self, row, new_parent_id):
        print('insert=' + str(row))
        sql = """INSERT INTO thread (application_id, submitter, email, ip_address, user_agent, 
                subject, deleted, show_email, parent_id, discapp_user_id, create_dt, mod_dt) 
                VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s) RETURNING id;"""

        source_id = row[0]
        submitter = row[1]
        email = row[2]
        ip_address = row[3]
        user_agent = row[4]
        subject = row[5]
        deleted = False
        show_email = row[6]
        disc_app_user_id = None
        create_dt = row[9]
        mod_dt = row[10]
        body = row[11]

        print('sql=' + sql)
        cur = self.conn.cursor()
        cur.execute(sql, (self.app_id, submitter, email, ip_address, user_agent, subject, deleted, show_email,
                    new_parent_id, disc_app_user_id, create_dt, mod_dt))

        inserted_row_id = cur.fetchone()[0]
        self.conn.commit()
        cur.close()

        if body is not None:
            print('inserting body text=' + body)
            sql = """INSERT INTO thread_body (application_id, thread_id, body, create_dt, mod_dt) 
                    VALUES (%s, %s, %s, %s, %s) RETURNING id;"""
            print('sql=' + sql)
            cur = self.conn.cursor()
            cur.execute(sql, (self.app_id, inserted_row_id, body, create_dt, mod_dt))
            self.conn.commit()
            cur.close()

        self.__update_staging_table_thread_as_imported(source_id)

        return inserted_row_id

    def __update_staging_table_thread_as_imported(self, thread_id):
        print('updating record as imported=' + str(thread_id))
        sql = 'UPDATE disc_' + str(self.app_id) + ' SET is_imported = true where id = %s;'

        print('sql=' + sql)
        cur = self.conn.cursor()
        thread_id = str(thread_id)
        cur.execute(sql, [thread_id])
        self.conn.commit()
        cur.close()
