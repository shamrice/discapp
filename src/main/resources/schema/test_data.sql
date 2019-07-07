select * from configuration;
select * from owner;
select * from application;
select * from thread;
select * from thread_body;


insert into owner
(first_name, last_name, phone, email, enabled)
values ('Erik', 'Test', '555-555-5555', 'test@example.com', true);
commit;

insert into application
(name, owner_id)
values ('test disc app', 1);
commit;

insert into configuration
(application_id, name, value, create_dt, mod_dt)
values (1, 'test.prop', 'true', current_timestamp, current_timestamp);
commit;

insert into application
(id, name, owner_id)
values (46108, 'NEMB', 1);
commit;

insert into thread
(application_id, submitter, email, ip_address, subject)
values (46108, 'Tester', 'test@example.com', '127.0.0.1', 'Test subject');

insert into thread_body
(application_id, thread_id, body)
values (46108, 1, 'This is a test of the thread body');

update application
set mod_dt = current_timestamp where id = 1;