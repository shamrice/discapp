import psycopg2


class DiscAppImporter:

    def __init__(self, conn, app_id, import_type):
        self.conn = conn
        self.app_id = app_id
        self.import_type = import_type

        if import_type == 'webapp':
            self.next_base_thread_search_sql = 'SELECT min(id) from disc_' + str(self.app_id) \
                                                + ' WHERE parent = 0 AND is_imported = false'
            self.next_base_thread_sql = 'SELECT id, author, email, ip, user_agent, subject, '\
                                        + 'show_email, parent, personal_account, date_entered, threadactivity, ' \
                                        + ' message FROM disc_'\
                                        + str(self.app_id) + ' WHERE id = %s'
            self.next_threads_sql = 'SELECT id, author, email, ip, user_agent, subject, '\
                                    + 'show_email, parent, personal_account, date_entered, threadactivity, ' \
                                    + 'message FROM disc_' + str(self.app_id) + ' WHERE parent = %s ' \
                                    + 'AND is_imported = false ORDER by date_entered desc'
            self.next_id_index = 0
            self.source_id_index = 0
            self.submitter_index = 1
            self.email_index = 2
            self.ip_address_index = 3
            self.user_agent_index = 4
            self.subject_index = 5
            self.deleted_value = False
            self.show_email_index = 6
            self.disc_app_user_id_value = None
            self.create_dt_index = 9
            self.mod_dt_index = 10
            self.body_index = 11

        else:
            self.next_base_thread_search_sql = 'SELECT min(thread_id) from disc_' + str(self.app_id) \
                                               + ' WHERE parent_id = 0 AND is_imported = false'
            self.next_base_thread_sql = 'SELECT id, thread_id, application_id, submitter, email, ip_address, ' \
                                        + ' user_agent, subject, deleted, show_email, parent_id, ' \
                                        + ' discapp_user_id, create_dt, mod_dt, body FROM disc_' + str(self.app_id) \
                                        + ' WHERE thread_id = %s'
            self.next_threads_sql = 'SELECT id, thread_id, application_id, submitter, email, ip_address, ' \
                                    + ' user_agent, subject, deleted, show_email, parent_id, discapp_user_id, ' \
                                    + ' create_dt, mod_dt, body FROM disc_' + str(self.app_id) \
                                    + ' WHERE parent_id = %s AND is_imported = false ORDER by create_dt desc'
            self.next_id_index = 1
            self.source_id_index = 0
            self.submitter_index = 3
            self.email_index = 4
            self.ip_address_index = 5
            self.user_agent_index = 6
            self.subject_index = 7
            self.deleted_index = 8
            self.show_email_index = 9
            self.disc_app_user_id_index = 11
            self.create_dt_index = 12
            self.mod_dt_index = 13
            self.body_index = 14

    def import_disc_app(self):

        is_done = False
        while not is_done:
            print('*********looking for next root level thread***************')
            next_root = self.__get_next_base_thread()

            if next_root is not None:
                print('NEXT ROOT LEVEL THREAD = ' + str(next_root))

                original_parent_id = next_root[self.next_id_index]

                self.__import_sub_thread(next_root, 0, original_parent_id)

                print('finished importing sub threads.')

            else:
                is_done = True
                print('Import complete!')

    def __get_next_base_thread(self):

        print('getting next oldest thread with parent = 0')

        cur = self.conn.cursor()
        sql = self.next_base_thread_search_sql
        print('sql=' + sql)
        cur.execute(sql)
        row = cur.fetchone()
        cur.close()

        if row is not None:
            thread_id = row[0]
            print('next original id = ' + str(thread_id))
            if thread_id is not None:
                cur = self.conn.cursor()
                cur.execute(self.next_base_thread_sql, [thread_id])
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
            sql = self.next_threads_sql

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
                original_parent_id = next_row[self.next_id_index]
                print('..................going down.....................')
                self.__import_sub_thread(next_row, new_parent, original_parent_id)
                print('..................back up........................')

    def __insert_into_thread_table(self, row, new_parent_id):

        source_id = row[self.source_id_index]
        submitter = row[self.submitter_index]
        email = row[self.email_index]
        ip_address = row[self.ip_address_index]
        user_agent = row[self.user_agent_index]
        subject = row[self.subject_index]
        show_email = row[self.show_email_index]
        create_dt = row[self.create_dt_index]
        mod_dt = row[self.mod_dt_index]
        body = row[self.body_index]

        if self.import_type == 'webapp':
            deleted = self.deleted_value
            disc_app_user_id = self.disc_app_user_id_value
        else:
            deleted = row[self.deleted_index]
            disc_app_user_id = row[self.disc_app_user_id_index]

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

            if body is not None and body != '':
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
