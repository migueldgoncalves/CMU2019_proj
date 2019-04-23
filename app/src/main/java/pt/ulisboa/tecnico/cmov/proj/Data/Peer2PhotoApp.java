package pt.ulisboa.tecnico.cmov.proj.Data;

import android.app.Application;

public class Peer2PhotoApp extends Application {

    public static enum MODE {
        CLOUD, DIRECT
    }

    private String username;
    private String password;
    private String sessionId;
    private String mode;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }


}
