
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
Sitemap: https://nediscapp.herokuapp.com/sitemap.xml

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
VALUES (0, 'mailing.list.email.admin.subject.template', 'Your NeDiscApp Report');

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
        APPLICATION_REPORT_DATA_START
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
        APPLICATION_REPORT_DATA_END
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
            <input type="hidden" name="appIds" value="APPLICATION_ID" />
        </form>
    </div>
</html>
');


INSERT INTO configuration (application_id, name, value)
VALUES (0, 'email.notification.newapp.subject.template', 'New DiscApp Message Board Created: APPLICATION_NAME');

INSERT INTO configuration (application_id, name, value)
VALUES (0, 'email.notification.newapp.body.template', '<HTML>
<head>
<style type="text/css">
pre.code {      background-color:#fff;
                border:1px dashed #000;
                padding:1em; }
</style>
</head>
<BODY>
<h2>New message board "APPLICATION_NAME" has been created and added to your account.</h2>
<P>
    Thank you for creating a new message board at <a href="BASE_SITE_URL">BASE_SITE_URL</a>. In this email, you will
    find some useful information and links to get you started.
</P>
<DIV>
	<h3>Useful Links:</h3>
	<UL>
		<LI>Your New Message Board: <a href="MESSAGE_BOARD_URL">MESSAGE_BOARD_URL</a></LI>
    	<LI>Message Board Admin Page: <a href="MESSAGE_BOARD_ADMIN_URL">MESSAGE_BOARD_ADMIN_URL</a></LI>
    	<LI>Modify Account Page: <a href="MODIFY_ACCOUNT_URL">MODIFY_ACCOUNT_URL</a></LI>
    	<LI>Help Forum: <a href="HELP_FORUM_URL">HELP_FORUM_URL</a></LI>
	</UL>
</DIV>
<hr />
<div>
	<h3>Information On Areas Of The Message Board Admin Page</h3>
	<ul>
		<li><b>Info/Landing Page:</b> Contains links to all areas of the admin section.</li>
		<li><b>Appearance:</b> Here you can customize the look and feel of your message board. You can choose how threads are
			displayed and sorted. You can also change the style sheet as well as customize buttons, labels and more!</li>
		<li><b>Messages:</b> The messages section allows you to perform admin actions on messages posted to your message board.
			Messages here can be modified, reported or deleted. You can also approve messages here if your application is
			configured to require that messages are approved before they are publicly displayed. Users who are granted "Editor" permissions on the
			Security section will have access to this page as well to help with administration.</li>
		<li><b>Security:</b> This section allows you to set user and application permission settings. There are also settings for where
			HTML can be allowed when posting messages, a section to block IP address prefixes and some additional miscellaneous
			security settings.
			<br />
			<br />
			<u>User Permission levels descriptions:</u>
			<ul>
				<li><b>None:</b> Users won''t have any access to your DiscussionApp.</li>
				<li><b>Read:</b> Users can''t post any messages.</li>
				<li><b>Reply:</b> Users can reply to messages but can''t create new threads.</li>
				<li><b>Post:</b> Users can create new threads as well as response to messages. (This is the default user permission value.)</li>
				<li><b>Edit:</b> Users with Edit privileges are moderators who can edit <em>any</em> message. Be very selective about whom
					you assign this privilege to. (Only available on user specific permissions)</li>
				<li><b>Hold:</b> Messages must be approved by a moderator with Edit privileges or the message board admin.</li>
				<li><b>Delete:</b> On specific user permissions, set the permissions to "Delete" to remove any specific permission settings for
					that user.</li>
			</ul>
			<br />
		</li>
		<li><b>Mailing List:</b> Here you can configure what the messages sent to users who are subscribed to your message board contain. You can also
			configure the appearance of the mailing list related pages. Instant email reply notifications on messages can also be turned
			on and off in this section.</li>
		<li><b>RSS:</b> Allows you to configuration the behavior of how messages are presented in the RSS feed for the message board.</li>
		<li><b>Widget:</b> The widget section helps you configure a small message board widget that you can place on your site. The code needed
			to display the widge is also available on this page.</li>
		<li><b>Locale Settings:</b> Sets the message board''s Time Zone as well as the date and time format to be displayed on messages.</li>
		<li><b>Statistics:</b> Shows statistics for page views, unique IP counts and average page views per IP address broken down by day.</li>
		<li><b>Import/Export:</b> If you would like a copy of the contents of your message board, you can create an export of it here. The export
			is in the form of a SQL script. You can also upload a previous export here as well if you would like to restore a previous message
			board to your current message board. Please note that importing data is subject to review and may take up to 14 business days before
			data is imported. If you would like to "rollback" your messages to a previous date and are not importing data from a different message
			board, please create a request on the <a href="HELP_FORUM_URL">Help Forum</a> for assistance instead.</li>
		<li><b>Documentation:</b> Provides information on some of the more advanced features that are possible with configuring your message board.
			A copy of this information can be found below under "Advanced DiscApp Feature Documentation" </li>
		<li><b>Help:</b> A link to the <a href="HELP_FORUM_URL">Help Forum</a></li>

	</ul>
</div>
<hr />
<DIV>
    <h3>Advanced DiscApp Feature Documentation</h3>
	The following information can also be found on the documentation page off of the message board admin page: <a href="DOC_ADMIN_URL">DOC_ADMIN_URL</a>.
	<BR />
	<br />
    <div id="navigation">
		The topics discussed below give information on how to implement the following features on your message board:
        <ul>
            <li>
                Highlight New Messages
            </li>
            <li>
                Read Thread History
            </li>
            <li>
                Search Feature
            </li>
            <li>
                Administration Link
            </li>
            <li>
				Admin Post Distinction on Messages
            </li>
        </ul>
    </div>
   <div id="highlight_new_messages">
        <h4>Highlight New Messages</h4>
        <p>
            With this feature enabled on the Appearance settings page under the Threads configuration, any message or
            reply that has been created in the past 24 hours will be marked with an additional CSS class "new_message".
            Once a message is no longer less than 24 hours old, it will no longer be marked as a new message.
        </p>
        <p>
            <b><u>Using this feature:</u></b>
        </p>

        <ul>
            <li>
                <b>Built-in CSS options:</b> If you are using one of the built in CSS options provided in the Appearance settings
                page, no additional work will be needed as this feature has already been added to those choices.
            </li>
            <li>
                <b>Custom CSS option:</b> If you are using your own CSS page on your DiscApp, you will need to add the
                following additional rule to your file (using the color of  your choosing):
                <br />
                <br />
                <u>Expand on index:</u>
                <br />
                <pre class="code">
    span.new_message {
         background-color:gray;
    }
                </pre>
                <u>Non-expand on index:</u>
                <br />
                <pre class="code">
    a.new_message {
        background-color:gray;
    }
                </pre>
            </li>
        </ul>
    </div>
    <div id="read_thread_history">
        <h4>Read Thread History</h4>
        <p>
            This feature allows users who have a registered account and are logged in to keep track of which threads
            they have read. With this feature an additional CSS class attribute will be added called "read" to thread
            anchor tags. By default, on the built-in CSS options, it is the same as the visited link color. This can be
            changed on custom CSS implementations if desired.
        </p>
        <p>
            <b><u>Using this feature:</u></b>
        </p>
        <ul>
            <li>
                <b>Built in CSS options:</b> If you are using one of the built in CSS options provided in the Appearance settings
                page, no additional work will be needed as this feature has already been added to those choices.
            </li>
            <li>
                <b>Custom CSS option:</b> If you are using your own CSS page on your DiscApp, you will need to add the
                following additional rule to your file (using the color of  your choosing):
                <br />
                <pre class="code">
    a.read {
        color: #551A8B;
    }
                </pre>
            </li>
        </ul>
    </div>
    <div id="search_feature">
        <h4>Adding a Search Feature</h4>
        <p>
            By default, when your DiscApp is created, the HTML required for the search feature is added to the epilogue of
            your DiscApp. The search feature allows users to search subjects and message bodies of articles of your DiscApp.
            Please use the below HTML code if you would like add the search feature elsewhere or need to reset or replace
            the original entry.
        </p>
        <p>
            <b><u>Using this feature:</u></b>
        </p>
        <pre class="code">    &lt;b&gt;Search the message board:&lt;/b&gt;
    &lt;FORM METHOD=&quot;POST&quot; ACTION=&quot;BASE_SITE_URL/indices/search?disc=APPLICATION_ID&quot;&gt;
        &lt;INPUT TYPE=&quot;hidden&quot; NAME=&quot;id&quot; VALUE=&quot;46108&quot;&gt;
        &lt;INPUT TYPE=&quot;text&quot; NAME=&quot;searchTerm&quot;&gt;
        &lt;INPUT TYPE=&quot;submit&quot; NAME=&quot;submit&quot; VALUE=&quot;Search&quot;&gt;
    &lt;/FORM&gt;</pre>
    </div>
    <div id="administration_link">
        <h4>Adding an Administration Link</h4>
        <p>
            By default, when your DiscApp is created, the administration link added to the epilogue of
            your DiscApp.
            Please use the below HTML code if you would like add the link elsewhere or need to reset or replace
            the original entry.
        </p>
        <p>
            <b><u>Using this feature:</u></b>
        </p>
        <pre class="code">    &lt;a href=&quot;MESSAGE_BOARD_ADMIN_URL&quot;&gt;Admin&lt;/a&gt;</pre>
    </div>
    <div id="admin_post_distinction">
        <h4>Admin Post Distinction on Messages</h4>
        <p>
            Admin owners of a DiscApp can select to mark their post as an "Admin post". This will cause a
            special SPAN tag to be added to the author field with the css class of "admin_post". The built in
            CSS offerings have the correct CSS required to correctly view this feature. If you are using a
            custom CSS, please include the following in your CSS code. (Feel free to change the colors as needed).
        </p>
        <p>
            <b><u>Using this feature:</u></b>
        </p>
        <pre class="code">
    .admin_post {
        color: #fff;
        background: #f00;
        padding: 2px;
        margin: 5px;
        font-weight: bold;
        border-radius: 5px;
    }</pre>
</DIV>
<br />
<PRE>
-------------------------------------------------------------------

Message from NE Disc App:

This is an automated message. Please do not respond to this message.

</PRE>
</BODY>
</HTML>
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

CREATE INDEX idx_thread_activity_thread_id
ON thread_activity(thread_id);

CREATE INDEX idx_thread_activity_mod_dt_application_id_thread_id
ON thread_activity(mod_dt, application_id, thread_id);

CREATE INDEX idx_user_read_thread_application_id_discapp_user_id
ON user_read_thread(application_id, discapp_user_id);


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

