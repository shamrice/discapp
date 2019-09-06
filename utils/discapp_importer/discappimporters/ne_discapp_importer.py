import psycopg2


class NeDiscAppImporter:

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

                original_parent_id = next_root[1]

                self.__import_sub_thread(next_root, 0, original_parent_id)

                print('finished importing sub threads.')

            else:
                is_done = True
                print('Import complete!')

    def __get_next_base_thread(self):

        print('getting next oldest thread with parent_id = 0')

        cur = self.conn.cursor()
        sql = 'SELECT min(thread_id) from disc_' + str(self.app_id) + ' WHERE parent_id = 0 AND is_imported = false'
        print('sql=' + sql)
        cur.execute(sql)
        row = cur.fetchone()
        cur.close()

        if row is not None:
            thread_id = row[0]
            print('next original thread_id = ' + str(thread_id))
            if thread_id is not None:
                cur = self.conn.cursor()
                cur.execute('SELECT id, thread_id, application_id, submitter, email, ip_address, user_agent, subject, '
                            + 'deleted, show_email, parent_id, discapp_user_id, create_dt, mod_dt, body FROM disc_'
                            + str(self.app_id) + ' WHERE thread_id = ' + str(thread_id))
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
            sql = 'SELECT id, thread_id, application_id, submitter, email, ip_address, user_agent, subject, '\
                  + 'deleted, show_email, parent_id, discapp_user_id, create_dt, mod_dt, body FROM disc_'\
                + str(self.app_id) + ' WHERE parent_id = %s AND is_imported = false ORDER by create_dt desc'

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
                original_parent_id = next_row[1]
                print('..................going down.....................')
                self.__import_sub_thread(next_row, new_parent, original_parent_id)
                print('..................back up........................')

    def __insert_into_thread_table(self, row, new_parent_id):

        source_id = row[0]
        submitter = row[3]
        email = row[4]
        ip_address = row[5]
        user_agent = row[6]
        subject = row[7]
        deleted = row[8]
        show_email = row[9]
        disc_app_user_id = row[11]
        create_dt = row[12]
        mod_dt = row[13]
        body = row[14]

        existing_record = self.__check_if_record_is_imported(submitter, subject, create_dt)

        if existing_record is not None:
            print('Record already imported. Skipping...')
            inserted_row_id = existing_record[0]
        else:
            print('insert=' + str(row))
            sql = """INSERT INTO thread (application_id, submitter, email, ip_address, user_agent,
                    subject, deleted, show_email, parent_id, discapp_user_id, create_dt, mod_dt)
                    VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s) RETURNING id;"""

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

    def __check_if_record_is_imported(self, submitter, subject, create_date):

        print('Checking for existing record with submitter=' + submitter + ' subject='
              + subject + ' create_date=' + str(create_date))

        cur = self.conn.cursor()
        sql = 'SELECT id FROM thread WHERE application_id = %s and submitter = %s and subject = %s and create_dt = %s'
        cur.execute(sql, (self.app_id, submitter, subject, create_date))
        existing_record = cur.fetchone()
        cur.close()
        print('Existing record = ' + str(existing_record))

        return existing_record
