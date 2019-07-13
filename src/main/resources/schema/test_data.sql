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




insert into configuration
(application_id, name, value)
values (46108, 'thread.entry.break.text', ' &#9787; ');

insert into configuration
(application_id, name, value)
values (46108, 'label.submitter.text', 'Name:');

insert into configuration
(application_id, name, value)
values (46108, 'label.subject.text', 'Clicky Text:');

insert into configuration
(application_id, name, value)
values (46108, 'label.email.text', 'DO NOT ENTER WILL BE SPAMMED:');

insert into configuration
(application_id, name, value)
values (46108, 'label.thread.body.text', 'Post Asterisk Text:');

insert into configuration
(application_id, name, value)
values (46108, 'button.preview.text', 'Preview');

insert into configuration
(application_id, name, value)
values (46108, 'button.post.message.text', 'Post Message');

insert into configuration
(application_id, name, value)
values (46108, 'button.return.to.messages.text', 'Return to Messages');

insert into configuration
(application_id, name, value)
values (46108, 'button.next.page.text', 'Next Page');

insert into configuration
(application_id, name, value)
values (46108, 'button.reply.post.text', 'Post Reply');

insert into configuration
(application_id, name, value)
values (46108, 'button.share.text', 'Spam Button (DON''T PRESS)');

insert into configuration
(application_id, name, value)
values (46108, 'stylesheet.url', '\styles\disc_46108.css');

insert into configuration
(application_id, name, value)
values (46108, 'thread.sort.order', 'new');

insert into configuration
(application_id, name, value)
values (46108, 'page.index.expand.threads', 'false');

insert into configuration
(application_id, name, value)
values (46108, 'page.index.preview.first', 'false');

insert into configuration
(application_id, name, value)
values (46108, 'thread.highlight.new', 'true');

insert into configuration
(application_id, name, value)
values (46108, 'thread.break.text', '<center>&#9829; &#9830; &#9827; &#9824; &#9829; &#9830; &#9827; &#9824; &#9829; &#9830; &#9827; &#9824; &#9829;</center>');

insert into configuration
(application_id, name, value)
values (46108, 'page.index.thread.depth', '30');

insert into configuration
(application_id, name, value)
values (46108, 'page.header.text', '<style type="text/css"> body{ font: small terminal; color:#9C9C9C; } a:link {color:#2eff00}; a:visited {color:#1b9600}; </style>');

insert into configuration
(application_id, name, value)
values (46108, 'page.footer.text', '<i>"Don''t quote me." - Erik </i><STYLE TYPE="text/css">');

insert into configuration
(application_id, name, value)
values (46108, 'favicon.url', 'http://nukementerprises.puckdroppersplace.us/favicon.ico');




