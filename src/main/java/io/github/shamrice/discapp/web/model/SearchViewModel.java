package io.github.shamrice.discapp.web.model;

public class SearchViewModel {

    private long appId;
    private String searchText;
    private String searchAgain;
    private String returnToApp;

    public long getAppId() {
        return appId;
    }

    public void setAppId(long appId) {
        this.appId = appId;
    }

    public String getSearchText() {
        return searchText;
    }

    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }

    public String getSearchAgain() {
        return searchAgain;
    }

    public void setSearchAgain(String searchAgain) {
        this.searchAgain = searchAgain;
    }

    public String getReturnToApp() {
        return returnToApp;
    }

    public void setReturnToApp(String returnToApp) {
        this.returnToApp = returnToApp;
    }
}
