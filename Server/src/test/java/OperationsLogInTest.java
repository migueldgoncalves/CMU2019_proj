import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

public class OperationsLogInTest {

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
    public void validLogInWithPreviousSessionTest() {
        User user = new User("username", "password", new byte[256]);
        operations.addUser(user);
        Session session = new Session("username", Operations.SESSION_DURATION);
        operations.addSession(session);
        user.setSessionId(session.getSessionId());
        Assert.assertEquals(String.valueOf(session.getSessionId()), operations.logIn("username", "password"));
        Assert.assertEquals(1, operations.getUsersLength());
        Assert.assertEquals(1, operations.getSessionsLength());
        Assert.assertEquals(0, operations.getAlbumsLength());
    }

    @Test
    public void validLogInWithoutPreviousSessionTest() {
        User user = new User("username", "password", new byte[256]);
        operations.addUser(user);
        Assert.assertTrue(Integer.valueOf(operations.logIn("username", "password")) > 0);
        Assert.assertTrue(operations.getUserByUsername("username").getSessionId() > 0);
        Assert.assertEquals(1, operations.getUsersLength());
        Assert.assertEquals(1, operations.getSessionsLength());
        Assert.assertEquals(0, operations.getAlbumsLength());
    }

    @Test
    public void nullUsernameLogInTest() {
        Assert.assertEquals("The Inserted Username is Incorrect!", operations.logIn(null, "password"));
        Assert.assertEquals(0, operations.getUsersLength());
        Assert.assertEquals(0, operations.getSessionsLength());
        Assert.assertEquals(0, operations.getAlbumsLength());
    }

    @Test
    public void nonExistingUsernameLogInTest() {
        Assert.assertEquals("The Inserted Username is Incorrect!", operations.logIn("username", "password"));
        Assert.assertEquals(0, operations.getUsersLength());
        Assert.assertEquals(0, operations.getSessionsLength());
        Assert.assertEquals(0, operations.getAlbumsLength());
    }

    @Test
    public void nullPasswordLogInTest() {
        User user = new User("username", "password", new byte[256]);
        operations.addUser(user);
        Assert.assertEquals("Invalid Password! Please Try Again", operations.logIn("username", null));
        Assert.assertEquals(1, operations.getUsersLength());
        Assert.assertEquals(0, operations.getSessionsLength());
        Assert.assertEquals(0, operations.getAlbumsLength());
    }

    @Test
    public void incorrectPasswordLogInTest() {
        User user = new User("username", "password", new byte[256]);
        operations.addUser(user);
        Assert.assertEquals("Invalid Password! Please Try Again", operations.logIn("username", "incorrectPassword"));
        Assert.assertEquals(1, operations.getUsersLength());
        Assert.assertEquals(0, operations.getSessionsLength());
        Assert.assertEquals(0, operations.getAlbumsLength());
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
