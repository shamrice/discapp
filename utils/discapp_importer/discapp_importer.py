#!/usr/bin/python
#
# DiscApp Importer Script
#
# Command line usage: python discapp_importer.py -a {staged_appId_to_import} -t {source-type:webapp|ne}
#
# Imports records that were previously manually staged into a table with the name 'disc_{appId}' into
# the thread table. If no source table type is specified, 'ne' format is assumed.
#
#
# Setup:
# ------
# sudo apt-get install  libpq-dev
# pip install psycopg2


from configparser import ConfigParser
from discappimporters import discapp_importer
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


def start_import(import_type, app_id):
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

        print('Importing from staged ' + import_type + ' source.')
        importer = discapp_importer.DiscAppImporter(conn, app_id, import_type)
        importer.import_disc_app()

    except (Exception, psycopg2.DatabaseError) as error:
        print(error)

    finally:
        if conn is not None:
            conn.close()
            print('Database connection closed.')


if __name__ == '__main__':

    parser = argparse.ArgumentParser(
        description='Import a previously staged Disc App import loaded into a \'disc_{APP_ID}\' '
                    + ' table into production thread/thread_body tables.',
        epilog='Please run the associated import \'disc_{APP_ID}.sql\' script before running this script. If source '
               + 'is from \'webapp\', run script through the \'discapp_export_converter\' utility script first '
               + 'to generate an importable SQL script that can stage the threads to be imported. ')
    parser.add_argument('-t',
                        '--type',
                        required=True,
                        choices=['ne', 'webapp'],
                        default='ne',
                        help='Type of import. \'ne\' or \'webapp\'')

    parser.add_argument('-a',
                        '--app-id',
                        required=True,
                        type=int,
                        help='Application Id of Disc App to import.')

    args = parser.parse_args()
    print(args)
    print('WARNING: You are about to import all records from source table disc_' + str(args.app_id)
          + ' with import type: ' + args.type)
    accept = input('This action can only be reverse manually. Are you sure you want to continue? [y/N]: ')

    if accept.lower() == 'y' or accept.lower() == 'yes':
        start_import(args.type, args.app_id)
    else:
        print("Aborting import.")
