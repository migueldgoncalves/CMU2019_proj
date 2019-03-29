import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.PrintWriter;

public class OperationsSessionsAddsGetsTest {

    private Operations operations = null;
    private File original = null;
    private File temporary = null;

    @Before
    public void setUp() {
        original = new File(Operations.STATE_BACKUP_PATH);
        temporary = new File(Operations.STATE_BACKUP_NAME);
        //If there is already a backup file, it will be moved to other directory
        if (original.exists() && !original.isDirectory()) {
            try {
                FileUtils.moveFile(original, temporary);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        operations = Operations.getServer();
    }

    @Test
    public void sessionAddNewTest() {
        try {
            Assert.assertEquals(0, operations.getSessionsLength());
            User user = new User("username", "password", new byte[256]);
            operations.addUser(user);
            Session session = new Session("username", 5);
            String returnString = operations.addSession(session);
            Assert.assertEquals("Session successfully added", returnString);
            Assert.assertEquals(1, operations.getSessionsLength());

            String jsonString = FileUtils.readFileToString(new File(Operations.STATE_BACKUP_PATH), "UTF-8");
            jsonString = jsonString.replace("\n", "").replace("\r", "");
            System.out.println(jsonString);
            Assert.assertTrue(jsonString.contains("{\"albums\":{},\"users\":{\"username\":{\"username\":\"username\",\"password\":\"password\",\"publicKey\":[0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0],\"albums\":[],\"sessionId\":"));
            Assert.assertTrue(jsonString.contains("\":{\"username\":\"username\",\"sessionId\":"));
            Assert.assertTrue(jsonString.contains(",\"loginTime\":\""));
            Assert.assertTrue(jsonString.contains("\",\"sessionDuration\":5}}}"));

            operations = null;
            Operations.cleanServer();

            PrintWriter writer = new PrintWriter(Operations.STATE_BACKUP_PATH);
            writer.println(jsonString);
            writer.close();

            operations = Operations.getServer();
            Assert.assertEquals(0, operations.getAlbumsLength());
            Assert.assertEquals(1, operations.getSessionsLength());
            Assert.assertEquals(1, operations.getUsersLength());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void sessionAddNullTest() {
        try {
            String returnString = operations.addSession(null);
            Assert.assertEquals("Session cannot be null", returnString);
            Assert.assertEquals(0, operations.getAlbumsLength());
            Assert.assertEquals(0, operations.getSessionsLength());
            Assert.assertEquals(0, operations.getUsersLength());

            String jsonString = FileUtils.readFileToString(new File(Operations.STATE_BACKUP_PATH), "UTF-8");
            jsonString = jsonString.replace("\n", "").replace("\r", "");
            Assert.assertEquals("{\"albums\":{},\"users\":{},\"sessions\":{}}", jsonString);

            operations.addUser(new User("username", "password", new byte[256]));
            returnString = operations.addSession(new Session("username", 5));
            Assert.assertEquals("Session successfully added", returnString);
            Assert.assertEquals(1, operations.getSessionsLength());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void sessionAddExistingTest() {
        try {
            operations.addUser(new User("username", "password", new byte[256]));
            Assert.assertEquals(0, operations.getSessionsLength());
            Session session = new Session("username", 5);
            operations.addSession(session);
            String returnValue = operations.addSession(session);

            Assert.assertEquals("Session already exists", returnValue);
            Assert.assertEquals(0, operations.getAlbumsLength());
            Assert.assertEquals(1, operations.getSessionsLength());
            Assert.assertEquals(1, operations.getUsersLength());

            String jsonString = FileUtils.readFileToString(new File(Operations.STATE_BACKUP_PATH), "UTF-8");
            jsonString = jsonString.replace("\n", "").replace("\r", "");
            Assert.assertTrue(jsonString.contains("{\"albums\":{},\"users\":{\"username\":{\"username\":\"username\",\"password\":\"password\",\"publicKey\":[0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0],\"albums\":[],\"sessionId\":"));
            Assert.assertTrue(jsonString.contains("\":{\"username\":\"username\",\"sessionId\":"));
            Assert.assertTrue(jsonString.contains(",\"loginTime\":\""));
            Assert.assertTrue(jsonString.contains("\",\"sessionDuration\":5}}}"));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void sessionAddNonExistingUser() {
        try {
            Assert.assertEquals(0, operations.getSessionsLength());
            Session session = new Session("username", 5);
            String returnValue = operations.addSession(session);
            Assert.assertEquals("User does not exist", returnValue);
            Assert.assertEquals(0, operations.getAlbumsLength());
            Assert.assertEquals(0, operations.getSessionsLength());
            Assert.assertEquals(0, operations.getUsersLength());

            operations.addUser(new User("username", "password", new byte[256]));
            returnValue = operations.addSession(session);
            Assert.assertEquals("Session successfully added", returnValue);
            Assert.assertEquals(0, operations.getAlbumsLength());
            Assert.assertEquals(1, operations.getSessionsLength());
            Assert.assertEquals(1, operations.getUsersLength());

            String jsonString = FileUtils.readFileToString(new File(Operations.STATE_BACKUP_PATH), "UTF-8");
            jsonString = jsonString.replace("\n", "").replace("\r", "");
            Assert.assertTrue(jsonString.contains("{\"albums\":{},\"users\":{\"username\":{\"username\":\"username\",\"password\":\"password\",\"publicKey\":[0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0],\"albums\":[],\"sessionId\":"));
            Assert.assertTrue(jsonString.contains("\":{\"username\":\"username\",\"sessionId\":"));
            Assert.assertTrue(jsonString.contains(",\"loginTime\":\""));
            Assert.assertTrue(jsonString.contains("\",\"sessionDuration\":5}}}"));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void sessionAddUserAlreadyHasSessionTest() {
        try {
            User user = new User("username", "password", new byte[256]);
            operations.addUser(user);
            Session session = new Session("username", 5);
            Session session2 = new Session("username", 5);
            operations.addSession(session);

            String returnValue = operations.addSession(new Session("username", 5));
            Assert.assertEquals(1, operations.getSessionsLength());
            Assert.assertEquals("User already has a session", returnValue);
            Assert.assertEquals(0, operations.getAlbumsLength());
            Assert.assertEquals(1, operations.getSessionsLength());
            Assert.assertEquals(1, operations.getUsersLength());

            String jsonString = FileUtils.readFileToString(new File(Operations.STATE_BACKUP_PATH), "UTF-8");
            jsonString = jsonString.replace("\n", "").replace("\r", "");
            Assert.assertTrue(jsonString.contains("{\"albums\":{},\"users\":{\"username\":{\"username\":\"username\",\"password\":\"password\",\"publicKey\":[0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0],\"albums\":[],\"sessionId\":"));
            Assert.assertTrue(jsonString.contains("\":{\"username\":\"username\",\"sessionId\":"));
            Assert.assertTrue(jsonString.contains(",\"loginTime\":\""));
            Assert.assertTrue(jsonString.contains("\",\"sessionDuration\":5}}}"));

            user.setSessionId(0);
            operations.addSession(session2);
            Assert.assertEquals(session2.getSessionId(), user.getSessionId());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void getSessionsTest() {
        Assert.assertEquals(0, operations.getSessions().size());
        Assert.assertEquals(0, operations.getSessionsLength());
        operations.addUser(new User("username", "password", new byte[256]));
        operations.addSession(new Session("username", 5));
        Assert.assertEquals(1, operations.getSessions().size());
        Assert.assertEquals(1, operations.getSessionsLength());
    }

    @Test
    public void getSessionByIdTest() {
        Assert.assertNull(operations.getSessionById(0));
        Assert.assertNull(operations.getSessionById(1));
        Assert.assertNull(operations.getSessionById(Session.MAX_SESSION_ID - 2));
        Assert.assertNull((operations.getSessionById(Session.MAX_SESSION_ID - 1)));
        operations.addUser(new User("username", "password", new byte[256]));
        Session session = new Session("username", 5);
        operations.addSession(session);
        Assert.assertNull(operations.getSessionById(session.getSessionId() - 1));
        Assert.assertEquals("username", operations.getSessionById(session.getSessionId()).getUsername());
        Assert.assertNull(operations.getSessionById(session.getSessionId() + 1));
    }

    @Test
    public void deleteExistingSessionTest() {
        User user = new User("username", "password", new byte[256]);
        operations.addUser(user);
        Session session = new Session("username", 5);
        operations.addSession(session);
        Assert.assertEquals(1, operations.getSessionsLength());
        Assert.assertEquals(session.getSessionId(), user.getSessionId());

        String result = operations.deleteSession(session.getSessionId());
        Assert.assertEquals("Session successfully deleted", result);
        Assert.assertEquals(0, user.getSessionId());
        Assert.assertNull(operations.getSessionById(session.getSessionId()));
        Assert.assertEquals(0, operations.getSessionsLength());
    }

    @Test
    public void deleteNonExistingSessionTest() {
        String result = operations.deleteSession(1);
        Assert.assertEquals("Session does not exist", result);
        Assert.assertEquals(0, operations.getSessionsLength());
    }

    @After
    public void tearDown() {
        Operations.cleanServer(); //Also deletes server backup file
        operations = null;
        //If there was already a backup file, it is moved back to the backup directory
        if (temporary.exists() && !temporary.isDirectory()) {
            try {
                FileUtils.moveFile(temporary, original);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
