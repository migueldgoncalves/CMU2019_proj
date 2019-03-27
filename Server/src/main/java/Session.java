import java.security.SecureRandom;
import java.util.Date;

public class Session {

    protected static final int MAX_SESSION_ID = (int) Math.pow(2, 32);

    private int userId;
    private int sessionId;
    private Date loginTime;
    private int sessionDuration;

    public Session(int userId, int sessionDuration) {
        this.userId = userId;
        while (this.sessionId == 0)
            this.sessionId = new SecureRandom().nextInt(MAX_SESSION_ID);
        this.loginTime = new Date();
        this.sessionDuration = sessionDuration;
    }

    public int getUserId() {
        return userId;
    }

    public int getSessionId() {
        return sessionId;
    }

    public Date getLoginTime() {
        return loginTime;
    }

    public int getSessionDuration() {
        return sessionDuration;
    }
}
