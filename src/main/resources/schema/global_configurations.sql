
-- Global site configurations

INSERT INTO owner(id, first_name, last_name, phone, email, enabled)
VALUES (0, 'Owner', 'Owner', '555-555-5555', 'owner.example.com', true);

INSERT INTO application(id, name, owner_id, enabled, deleted, searchable)
VALUES(0, 'DiscApp', 0, true, false, false);

INSERT INTO configuration (application_id, name, value)
VALUES (0, 'email.passwordreset.subject', 'DiscApp: Reset Password Request');

INSERT INTO configuration (application_id, name, value)
VALUES (0, 'email.passwordreset.message',
'Please use the following URL to reset the password for your account: PASSWORD_RESET_URL \r\n \r\n Your password reset code is: PASSWORD_RESET_CODE');

INSERT INTO configuration (application_id, name, value)
VALUES (0, 'email.newaccount.subject', 'DiscApp: New Account Creation Request');

INSERT INTO configuration (application_id, name, value)
VALUES (0, 'email.newaccount.message',
'A new account creation request has been created for the email address: NEW_ACCOUNT_EMAIL \r\n \r\n If this account is valid, please approve and send approval message.');

INSERT INTO configuration (application_id, name, value)
VALUES (0, 'email.admin.address', 'ADMIN_EMAIL@EXAMPLE.COM');


INSERT INTO configuration (application_id, name, value)
VALUES (0, 'recaptcha.secret', 'RE_CAPTCHA_SECRET');

INSERT INTO configuration (application_id, name, value)
VALUES (0, 'recaptcha.verify.url', 'https://www.google.com/recaptcha/api/siteverify');

INSERT INTO configuration (application_id, name, value)
VALUES (0, 'recaptcha.verify.enabled', 'true');

INSERT INTO configuration (application_id, name, value)
VALUES (0, 'import.upload.location', 'imports');

INSERT INTO configuration (application_id, name, value)
VALUES (0, 'export.download.location', 'exports');

INSERT INTO configuration (application_id, name, value)
VALUES (0, 'thread.post.interval.minimum', '1');

INSERT INTO configuration (application_id, name, value)
VALUES (0, 'account.max.apps', '3');

INSERT INTO configuration (application_id, name, value)
VALUES (0, 'filter.badwords.list', '');

INSERT INTO configuration (application_id, name, value)
VALUES (0, 'robots.txt.contents', 'Sitemap: https://nediscapp.herokuapp.com/Sitemaps/forums.xml
Sitemap: https://nediscapp.herokuapp.com/Sitemaps/articles.xml

User-agent: *
Disallow: /test
Disallow: /error
Disallow: /auth
Disallow: /application
');

INSERT INTO configuration (application_id, name, value)
VALUES (0, 'whois.url', 'https://www.whois.com/whois/');