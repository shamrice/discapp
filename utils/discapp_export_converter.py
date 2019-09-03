#
# DiscApp Export Converter Script
#
# Command line usage: python discapp_export_converter.py {source_export_sql_file}
#
# Creates a version of the export script that is compatible with staging the exported data
# into the current Postgres database.
#
# WARNING: Scripts MUST to be checked before being ran for any potential dangerous commands.
#
# Execute script in database: \i {path to converted_disc_export.sql}
#

import sys

if len(sys.argv) != 2:
    print('Invalid number of arguments supplied. Please provide source SQL file.')
    exit(-1)

file_contents = 'set standard_conforming_strings = off;\n'

with open(sys.argv[1], 'r') as input_file:
    print('\nOpened ' + sys.argv[1] + ' as input file.')
    line = input_file.readline()
    while line:
        if not line.__contains__('/*!4'):
            file_contents += line
        line = input_file.readline()

print('Replacing MySQL table creation script with Postgres.')
file_contents = file_contents.replace('`', '')
file_contents = file_contents.replace('ENGINE=MyISAM AUTO_INCREMENT=21020 DEFAULT CHARSET=latin1', '')
file_contents = file_contents.replace('id mediumint(8) unsigned NOT NULL AUTO_INCREMENT', 'id serial NOT NULL')
file_contents = file_contents.replace('mediumint(8) unsigned', 'int')
file_contents = file_contents.replace('enum(\'0\',\'1\')', 'bool')
file_contents = file_contents.replace('enum(\'1\',\'0\')', 'bool')
file_contents = file_contents.replace('NOT NULL DEFAULT \'1\'', 'NULL')
file_contents = file_contents.replace('NOT NULL DEFAULT \'0\'', 'NULL')
file_contents = file_contents.replace('date_entered datetime', 'date_entered TIMESTAMP')
file_contents = file_contents.replace('threadactivity datetime', 'threadactivity TIMESTAMP')
file_contents = file_contents.replace('KEY parent (parent),', '')
file_contents = file_contents.replace('KEY grandparent (grandparent)', '')
file_contents = file_contents.replace('PRIMARY KEY (id),', 'PRIMARY KEY (id)')
file_contents = file_contents.replace('LOCK TABLES disc_46108 WRITE;', '')
file_contents = file_contents.replace('UNLOCK TABLES;', '')

file_contents += 'set standard_conforming_strings = on;\n'
file_contents += 'show standard_conforming_strings;\n\n'

print('\n\tScript Preview:\n-----------------------------\n')
print(file_contents[:1000])

with open('converted_disc_export.sql', 'w') as output_file:
    output_file.write(file_contents)

print('\n------------------------------\n\tComplete\n------------------------------\n')
