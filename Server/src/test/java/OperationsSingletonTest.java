import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

public class OperationsSingletonTest {

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
    }

    @Test
    public void singletonGetEmptyServerTest() {
        operations = Operations.getServer();
        Assert.assertEquals(0, operations.getAlbumsLength());
        Assert.assertEquals(0, operations.getSessionsLength());
        Assert.assertEquals(0, operations.getUsersLength());
        operations = null;
        operations = Operations.getServer();
        Assert.assertEquals(0, operations.getAlbumsLength());
        Assert.assertEquals(0, operations.getSessionsLength());
        Assert.assertEquals(0, operations.getUsersLength());
        operations = null;
        Operations.cleanServer();
        operations = Operations.getServer();
        Assert.assertEquals(0, operations.getAlbumsLength());
        Assert.assertEquals(0, operations.getSessionsLength());
        Assert.assertEquals(0, operations.getUsersLength());
    }

    @Test
    public void singletonGetNonEmptyServerTest() {
        Album album = new Album("album", 1);
        Session session1 = new Session("user1", 5);
        Session session2 = new Session("user2", 10);
        User user1 = new User("user1", "password1", new byte[5]);
        User user2 = new User("user2", "password2", new byte[10]);
        User user3 = new User("user3", "password3", new byte[20]);

        operations = Operations.getServer();
        operations.addAlbum(album);
        operations.addUser(user1);
        operations.addUser(user2);
        operations.addUser(user3);
        operations.addSession(session1);
        operations.addSession(session2);

        operations = null;
        operations = Operations.getServer();
        Assert.assertEquals(1, operations.getAlbumsLength());
        Assert.assertEquals(2, operations.getSessionsLength());
        Assert.assertEquals(3, operations.getUsersLength());
        Assert.assertEquals("album", operations.getAlbumById(1).getName());
        Assert.assertEquals(10, operations.getSessionById(session2.getSessionId()).getSessionDuration());
        Assert.assertEquals(20, operations.getUserByUsername("user3").getPublicKey().length);

        operations = null;
        Operations.cleanServer();
        operations = Operations.getServer();
        Assert.assertEquals(0, operations.getAlbumsLength());
        Assert.assertEquals(0, operations.getSessionsLength());
        Assert.assertEquals(0, operations.getUsersLength());
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
