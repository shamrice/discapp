
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
VALUES (0, 'email.newaccount.subject', 'DiscApp: New Account Registration');

INSERT INTO configuration (application_id, name, value)
VALUES (0, 'email.newaccount.message',
'
This email has been sent from SITE_URL

You have received this email because this email address
was used during registration for our forums.
If you did not register at our forums, please disregard this
email. You do not need to unsubscribe or take any further action.

------------------------------------------------
Activation Instructions
------------------------------------------------

Thank you for registering.
We require that you "validate" your registration to ensure that
the email address you entered was correct. This protects against
unwanted spam and malicious abuse.

To activate your account, simply click on the following link:

USER_REGISTRATION_URL

(Some email client users may need to copy and paste the link into your web
browser).
');

INSERT INTO configuration (application_id, name, value)
VALUES (0, 'email.accountlocked.subject', 'DiscApp: User Account Locked Due To Invalid Password Attempts');

INSERT INTO configuration (application_id, name, value)
VALUES (0, 'email.accountlocked.message',
'Your account ACCOUNT_EMAIL has been locked due to multiple failed password attempts.\r\n \r\n Please wait at least ACCOUNT_LOCK_DURATION before trying to log in again.\r\n\r\nIf you do not remember your password. Please reset the password of your account here: PASSWORD_RESET_URL');

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
VALUES (0, 'epilogue.default.value', '<p align="center">
<a href="/indices/APP_ID">Return to the Top</a>
</p>
<P><FORM METHOD="POST" ACTION="SEARCH_URL?disc=APP_ID">
<INPUT TYPE="hidden" NAME="id" VALUE="APP_ID">
<INPUT TYPE="text" NAME="searchTerm">
<INPUT TYPE="submit" NAME="submit" VALUE="Search">
</FORM>
</P>
<BR><BR>
<font color="#FFFFFF" size="-2"><a href="MAINTENANCE_URL?id=APP_ID">Admin</a></font>
');

INSERT INTO configuration (application_id, name, value)
VALUES (0, 'whois.url', 'https://www.whois.com/whois/');

INSERT INTO configuration (application_id, name, value)
VALUES (0, 'home.search.length.min', '2');

INSERT INTO configuration (application_id, name, value)
VALUES (0, 'login.failed.attempts.max', '5');

INSERT INTO configuration (application_id, name, value)
VALUES (0, 'login.failed.lock.duration', '300000');

INSERT INTO configuration (application_id, name, value)
VALUES (0, 'mailing.list.email.confirmation.subject.template', 'Please confirm your subscription to APPLICATION_NAME');

INSERT INTO configuration (application_id, name, value)
VALUES (0, 'mailing.list.email.confirmation.body.template', '<HTML><BODY>
<P>CONFIRMATION_MESSAGE</P>
<p><a href="CONFIRMATION_URL">CONFIRMATION_URL</a></p>
<PRE>
-------------------------------------------------------------------

Message from NE Disc App:

This is an automated message. Please do not respond to this message.

We received a request to add you to the APPLICATION_NAME  mailing list.

You will not receive any messages if you do not confirm your subscription.

</PRE>
</BODY>
</HTML>
');

INSERT INTO configuration (application_id, name, value)
VALUES (0, 'email.notification.reply.subject.template', 'response to your article on APPLICATION_NAME');

INSERT INTO configuration (application_id, name, value)
VALUES (0, 'email.notification.reply.body.template', '<html><body>
<p>This is an automated message. Please do not respond to this message.</p>
<p>A response has been posted to your article on "APPLICATION_NAME".</p>
<p>The URL for the response is <a href="APP_DISCUSSION_URL?disc=APP_ID&article=THREAD_ID">APP_DISCUSSION_URL?disc=APP_ID&article=THREAD_ID</a></p>
</body></html>');


INSERT INTO configuration (application_id, name, value)
VALUES (0, 'stylesheet.url.graph', '/styles/graph2.css');

INSERT INTO configuration (application_id, name, value)
VALUES (0, 'stylesheet.url.ocean', '/styles/ocean.css');

INSERT INTO configuration (application_id, name, value)
VALUES (0, 'stylesheet.url.cute', '/styles/cute.css');

INSERT INTO configuration (application_id, name, value)
VALUES (0, 'stylesheet.url.cyan', '/styles/cyan2.css');

INSERT INTO configuration (application_id, name, value)
VALUES (0, 'stylesheet.url.forest', '/styles/forest.css');

INSERT INTO configuration (application_id, name, value)
VALUES (0, 'stylesheet.url.midnight', '/styles/midnight.css');

INSERT INTO configuration (application_id, name, value)
VALUES (0, 'stylesheet.url.steely', '/styles/steely.css');

INSERT INTO configuration (application_id, name, value)
VALUES (0, 'stylesheet.url.traditional', '/styles/traditional.css');

INSERT INTO configuration (application_id, name, value)
VALUES (0, 'stylesheet.url.default', '/styles/default.css');

INSERT INTO configuration (application_id, name, value)
VALUES (0, 'stylesheet.url.maintenance', '/styles/maint.css');

INSERT INTO configuration (application_id, name, value)
VALUES (0, 'mailing.list.email.admin.subject.template', 'Your NeDiscApp Report - APPLICATION_NAME');

INSERT INTO configuration (application_id, name, value)
VALUES (0, 'mailing.list.email.admin.body.template', '<html>
    <h3 style="margin: 2em; margin-left: 0;">
        <a href="BASE_SITE_URL" style="text-decoration: none; font-weight: bold;">Ne<span style="color: #d44; margin-left: 2px;">Disc</span>App</a>
        report for OWNER_EMAIL_ADDRESS
        <a style="font-size: smaller; margin-left: 1em;" href="MODIFY_ACCOUNT_URL">Settings</a>
        <a style="font-size: smaller; margin-left: 1em;" href="HELP_FORUM_URL">Support</a>
        <!--/span-->
    </h3>

    <table style="border: 1px solid black; background-color: #cce; padding: 1em; border-collapse: collapse; margin-left: 2px;">
        <tr>
            <td colspan="8" style="font-size: larger; font-weight: bold; text-align: center; padding: 1ex;"><a href="BASE_SITE_URL">DiscussionApps</a></td>
        </tr>
        <tr>
            <th style="text-align: center; vertical-align: bottom; border-bottom: 1px solid black;">Name</th>
            <th style="text-align: center; vertical-align: bottom; border-bottom: 1px solid black;">Entries</th>
            <th style="text-align: center; vertical-align: bottom; border-bottom: 1px solid black;">Last entry</th>
            <th style="text-align: center; vertical-align: bottom; border-bottom: 1px solid black;">Subscribers</th>
            <th style="text-align: center; vertical-align: bottom; border-bottom: 1px solid black;">
                Last<br />
                subscription
            </th>
            <th style="text-align: center; vertical-align: bottom; border-bottom: 1px solid black;">
                Average Daily Visitors<br />
                (last month)
            </th>
            <th style="text-align: center; vertical-align: bottom; border-bottom: 1px solid black;">
                Unapproved<br />
                messages
            </th>
        </tr>
        <tr style="margin-bottom: 3px;">
            <td style="background-color: #f4f4ff; text-align: right; padding: 1ex; border: 1px solid #444;">
            <a href="MAINTENANCE_URL">APPLICATION_NAME </a></td>
            <td style="background-color: #f4f4ff; text-align: right; padding: 1ex; border: 1px solid #444;">
                <a href="MAINTENANCE_THREADS_URL">TOTAL_THREADS</a>
            </td>
            <td style="background-color: #f4f4ff; text-align: right; padding: 1ex; border: 1px solid #444;">
                <a href="MAINTENANCE_THREADS_URL">LAST_THREAD_CREATION</a>
            </td>
            <td style="background-color: #f4f4ff; text-align: right; padding: 1ex; border: 1px solid #444;">
                <a href="MAINTENANCE_SUBSCRIBERS_URL">TOTAL_SUBSCRIBERS</a>
            </td>
            <td style="background-color: #f4f4ff; text-align: right; padding: 1ex; border: 1px solid #444;">
                <a href="MAINTENANCE_SUBSCRIBERS_URL">LAST_SUBSCRIPTION_DATE</a>
            </td>
            <td style="background-color: #f4f4ff; text-align: right; padding: 1ex; border: 1px solid #444;">
                <a href="MAINTENANCE_STATS_URL">TOTAL_LAST_MONTH_VISITORS</a>
            </td>
            <td style="background-color: #f4f4ff; text-align: right; padding: 1ex; border: 1px solid #444;">
                <a href="MAINTENANCE_THREADS_UNAPPROVED_URL">TOTAL_UNAPPROVED_MESSAGES</a>
            </td>
        </tr>
    </table>
    <div style="height: 2em;"></div>
    <div
        style="border:1px solid #666; background-color:#cce;=20
                                width:30em; padding:1ex; margin:2em; margin-left:2px; ">
        <form method="post" action="REPORT_FREQUENCY_URL" enctype="multipart/form-data">
            Send email reports
            <select name="changeReportFrequency">
                <option value="Daily">Daily</option>
                <option value="Weekly">Weekly</option>
                <option selected="selected" value="Monthly">Monthly</option>
                <option value="Never">Never</option>
            </select>
            <input type="submit" name="updateReportFrequency" value="Update Report Frequency" />
            <input type="hidden" name="emailAddress" value="OWNER_EMAIL_ADDRESS" />
            <input type="hidden" name="authCode" value="GENERATED_AUTH_CODE" />
            <input type="hidden" name="appId" value="APPLICATION_ID" />
        </form>
    </div>
</html>
');


-- INDEXES
CREATE INDEX idx_thread_application_id_parent_id_deleted_approved
ON thread(application_id, parent_id, deleted, is_approved)
WHERE deleted = false and approved = true;

CREATE INDEX idx_thread_body_thread_id
ON thread_body(thread_id);

CREATE INDEX idx_thread_body_id
ON thread_body (id);

CREATE INDEX idx_configuration_application_id_name
ON configuration(application_id, name);

CREATE INDEX idx_stats_unique_ips_stats_id
ON stats_unique_ips(stats_id);

CREATE INDEX idx_stats_application_id_stat_date
ON stats(application_id, stat_date);

CREATE INDEX idx_prologue_application_id
ON prologue(application_id);

CREATE INDEX idx_epilogue_application_id
ON epilogue(application_id);

CREATE INDEX idx_discapp_user_id
ON discapp_user(id);

CREATE INDEX idx_discapp_user_email
ON discapp_user(email);


-- STORED PROC

--stored proc to remove old unique ip records from db.
CREATE FUNCTION delete_old_unique_ips() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    DELETE FROM stats_unique_ips WHERE create_dt < NOW() - INTERVAL '90 days';
    RETURN NULL;
END;
$$;

--stored proc to remove old post code records from db.
CREATE FUNCTION delete_old_thread_post_codes() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    DELETE FROM thread_post_code WHERE create_dt < NOW() - INTERVAL '1 days';
    RETURN NULL;
END;
$$;


-- TRIGGERS

--trigger to delete old unique ip records on stat insert
CREATE TRIGGER trigger_delete_old_unique_ips
    AFTER INSERT ON stats_unique_ips
    EXECUTE PROCEDURE delete_old_unique_ips();

--trigger to delete old post code records on post code insert
CREATE TRIGGER trigger_delete_old_thread_post_codes
    AFTER INSERT ON thread_post_code
    EXECUTE PROCEDURE delete_old_thread_post_codes();

