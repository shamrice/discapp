package io.github.shamrice.discapp.service.configuration;

public enum ConfigurationProperty {

    SUBMITTER_LABEL_TEXT ("label.submitter.text"),
    SUBJECT_LABEL_TEXT ("label.subject.text"),
    EMAIL_LABEL_TEXT ("label.email.text"),
    DATE_LABEL_TEXT ("label.data.text"),
    THREAD_BODY_LABEL_TEXT ("label.thread.body.text"),
    PREVIEW_BUTTON_TEXT ("button.preview.text"),
    EDIT_BUTTON_TEXT ("button.edit.text"),
    POST_MESSAGE_BUTTON_TEXT ("button.post.message.text"),
    RETURN_TO_MESSAGES_BUTTON_TEXT ("button.return.to.messages.text"),
    NEXT_PAGE_BUTTON_TEXT ("button.page.next.text"),
    PREVIOUS_PAGE_BUTTON_TEXT ("button.page.previous.text"),
    POST_REPLY_MESSAGE_BUTTON_TEXT ("button.reply.post.text"),
    SHARE_BUTTON_TEXT ("button.share.text"),
    STYLE_SHEET_URL ("stylesheet.url"),
    THREAD_SORT_ORDER ("thread.sort.order"),
    EXPAND_THREADS_ON_INDEX_PAGE ("page.index.expand.threads"),
    PREVIEW_FIRST_MESSAGE_OF_THREAD_ON_INDEX_PAGE ("page.index.preview.first"),
    HIGHLIGHT_NEW_MESSAGES ("thread.highlight.new"),
    THREAD_BREAK_TEXT ("thread.break.text"),
    ENTRY_BREAK_TEXT ("thread.entry.break.text"),
    THREAD_DEPTH_ON_INDEX_PAGE ("page.index.thread.depth"),
    MAX_THREADS_ON_INDEX_PAGE ("page.index.thread.count.max"),
    HEADER_TEXT ("page.header.text"),
    FOOTER_TEXT ("page.footer.text"),
    FAVICON_URL ("favicon.url"),
    TIMEZONE_LOCATION ("timezone.location"),
    DATE_FORMAT_PATTERN ("date.format.pattern"),
    WIDGET_SHOW_AUTHOR ("widget.show.author"),
    WIDGET_SHOW_DATE("widget.show.date"),
    WIDGET_USE_STYLE_SHEET("widget.use.stylesheet"),
    EMAIL_PASSWORD_RESET_SUBJECT("email.passwordreset.subject"),
    EMAIL_PASSWORD_RESET_MESSAGE("email.passwordreset.message"),
    EMAIL_NEW_ACCOUNT_CREATED_SUBJECT("email.newaccount.subject"),
    EMAIL_NEW_ACCOUNT_CREATED_MESSAGE("email.newaccount.message"),
    EMAIL_ADMIN_ADDRESS("email.admin.address"),
    RE_CAPTCHA_VERIFY_URL("recaptcha.verify.url"),
    RE_CAPTCHA_SECRET("recaptcha.secret"),
    RE_CAPTCHA_VERIFY_ENABLED("recaptcha.verify.enabled");

    private final String propName;

    ConfigurationProperty(String propName) {
        this.propName = propName;
    }

    public String getPropName() {
        return propName;
    }
}
