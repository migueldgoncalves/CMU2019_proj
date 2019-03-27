import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

public class SessionTest {

    Session session = null;

    @Before
    public void setUp() {
        session = new Session(1, 5);
    }

    @Test
    public void sessionConstructorTest() {
        Assert.assertEquals(1, session.getUserId());
        Assert.assertTrue(session.getSessionId() > 0 && session.getSessionId() < Session.MAX_SESSION_ID);
        Assert.assertTrue((int) new Date().getTime() - (int) session.getLoginTime().getTime() < 1000);
        Assert.assertEquals(5, session.getSessionDuration());
    }

    @After
    public void tearDown() {
        session = null;
    }
}