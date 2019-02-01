package api.auth;

public class ApplicationToken {

    private String applicationToken;

    public ApplicationToken() {
        this.applicationToken = null;
    }

    public ApplicationToken(String applicationToken) {
        this.applicationToken = applicationToken;
    }

    public String getApplicationToken() {
        return applicationToken;
    }

    public void setApplicationToken(String applicationToken) {
        this.applicationToken = applicationToken;
    }
}
