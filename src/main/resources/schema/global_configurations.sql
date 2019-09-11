
-- Global site configurations

INSERT INTO owner(id, first_name, last_name, phone, email, enabled)
VALUES (0, 'Owner', 'Owner', '555-555-5555', 'owner.example.com', true);

INSERT INTO application(id, name, owner_id)
VALUES(0, 'DiscApp', 0);

INSERT INTO configuration (application_id, name, value)
VALUES (0, 'email.passwordreset.subject', 'DiscApp: Reset Password Request');

INSERT INTO configuration (application_id, name, value)
VALUES (0, 'email.passwordreset.message',
'Please user the following URL to reset the password for your account: PASSWORD_RESET_URL \r\n \r\n Your password reset code is: PASSWORD_RESET_CODE');



