import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.PrintWriter;

public class OperationsAlbumsAddsGetsTest {

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
    public void albumAddNewTest() {
        try {
            Assert.assertEquals(0, operations.getAlbumsLength());
            operations.addUser(new User("username", "password", new byte[256]));
            operations.addUser(new User("username2", "password", new byte[256]));

            String[] returnValues = operations.addAlbum(new Album("album", 10), "username");
            Assert.assertEquals("Album successfully added", returnValues[0]);
            Assert.assertEquals("10", returnValues[1]);
            Assert.assertEquals(1, operations.getAlbumsLength());
            Assert.assertEquals(1, operations.getUserByUsername("username").getUserAlbumNumber());
            Assert.assertEquals(10, (int) operations.getUserByUsername("username").getAlbums().get(0));
            Assert.assertEquals(1, operations.getAlbumById(10).getAlbumUserNumber());
            Assert.assertTrue(operations.getAlbumById(10).isUserInAlbum("username"));
            Assert.assertNull(operations.getAlbumById(10).getSliceURL("username"));

            returnValues = operations.addAlbum(new Album("album", 11), "username");
            Assert.assertEquals("Album successfully added", returnValues[0]);
            Assert.assertEquals("11", returnValues[1]);
            Assert.assertEquals(2, operations.getAlbumsLength());
            Assert.assertEquals(2, operations.getUserByUsername("username").getUserAlbumNumber());
            Assert.assertEquals(11, (int) operations.getUserByUsername("username").getAlbums().get(1));
            Assert.assertNull(operations.getAlbumById(11).getSliceURL("username"));

            returnValues = operations.addAlbum(new Album("album", 12), "username2");
            Assert.assertEquals("Album successfully added", returnValues[0]);
            Assert.assertEquals("12", returnValues[1]);
            Assert.assertEquals(3, operations.getAlbumsLength());
            Assert.assertEquals(1, operations.getUserByUsername("username2").getUserAlbumNumber());
            Assert.assertEquals(11, (int) operations.getUserByUsername("username").getAlbums().get(1));
            Assert.assertEquals(1, operations.getAlbumById(12).getAlbumUserNumber());
            Assert.assertTrue(operations.getAlbumById(12).isUserInAlbum("username2"));
            Assert.assertNull(operations.getAlbumById(12).getSliceURL("username2"));

            String jsonString = FileUtils.readFileToString(new File(Operations.STATE_BACKUP_PATH), "UTF-8");
            jsonString = jsonString.replace("\n", "").replace("\r", "");
            Assert.assertEquals("{\"albums\":{\"10\":{\"id\":10,\"slices\":{},\"name\":\"album\"},\"11\":{\"id\":11,\"slices\":{},\"name\":\"album\"},\"12\":{\"id\":12,\"slices\":{},\"name\":\"album\"}},\"users\":{\"username2\":{\"username\":\"username2\",\"password\":\"password\",\"publicKey\":[0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0],\"albums\":[12],\"sessionId\":0},\"username\":{\"username\":\"username\",\"password\":\"password\",\"publicKey\":[0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0],\"albums\":[10,11],\"sessionId\":0}},\"sessions\":{},\"logs\":\"\",\"counterAlbum\":0,\"counterLog\":0}", jsonString);

            operations = null;
            Operations.cleanServer();

            PrintWriter writer = new PrintWriter(Operations.STATE_BACKUP_PATH);
            writer.println(jsonString);
            writer.close();

            operations = Operations.getServer();
            Assert.assertEquals(3, operations.getAlbumsLength());
            Assert.assertEquals(0, operations.getSessionsLength());
            Assert.assertEquals(2, operations.getUsersLength());
            Assert.assertEquals(0, operations.getLogsLength());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void albumAddNullAlbumTest() {
        try {
            operations.addUser(new User("username", "password", new byte[256]));
            String[] returnValues = operations.addAlbum(null, "username");
            Assert.assertEquals("Album cannot be null", returnValues[0]);
            Assert.assertNull(returnValues[1]);

            String jsonString = FileUtils.readFileToString(new File(Operations.STATE_BACKUP_PATH), "UTF-8");
            jsonString = jsonString.replace("\n", "").replace("\r", "");
            Assert.assertEquals("{\"albums\":{},\"users\":{\"username\":{\"username\":\"username\",\"password\":\"password\",\"publicKey\":[0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0],\"albums\":[],\"sessionId\":0}},\"sessions\":{},\"logs\":\"\",\"counterAlbum\":0,\"counterLog\":0}", jsonString);

            Assert.assertEquals(0, operations.getAlbumsLength());
            Assert.assertEquals(0, operations.getSessionsLength());
            Assert.assertEquals(1, operations.getUsersLength());
            Assert.assertEquals(0, operations.getLogsLength());

            returnValues = operations.addAlbum(new Album("album", 10), "username");
            Assert.assertEquals("Album successfully added", returnValues[0]);
            Assert.assertEquals("10", returnValues[1]);
            Assert.assertEquals(1, operations.getAlbumsLength());
            Assert.assertEquals(10, (int) operations.getUserByUsername("username").getAlbums().get(0));
            Assert.assertNull(operations.getAlbumById(10).getSliceURL("username"));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void albumAddInvalidUsernameTest() {
        try {
            operations.addUser(new User("username", "password", new byte[256]));
            String[] returnValues = operations.addAlbum(new Album("album", 10), "username2");
            Assert.assertEquals("Username does not exist or is invalid", returnValues[0]);
            Assert.assertNull(returnValues[1]);

            String jsonString = FileUtils.readFileToString(new File(Operations.STATE_BACKUP_PATH), "UTF-8");
            jsonString = jsonString.replace("\n", "").replace("\r", "");
            Assert.assertEquals("{\"albums\":{},\"users\":{\"username\":{\"username\":\"username\",\"password\":\"password\",\"publicKey\":[0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0],\"albums\":[],\"sessionId\":0}},\"sessions\":{},\"logs\":\"\",\"counterAlbum\":0,\"counterLog\":0}", jsonString);

            Assert.assertEquals(0, operations.getAlbumsLength());
            Assert.assertEquals(0, operations.getSessionsLength());
            Assert.assertEquals(1, operations.getUsersLength());
            Assert.assertEquals(0, operations.getLogsLength());

            returnValues = operations.addAlbum(new Album("album", 10), "username");
            Assert.assertEquals("Album successfully added", returnValues[0]);
            Assert.assertEquals("10", returnValues[1]);
            Assert.assertEquals(1, operations.getAlbumsLength());
            Assert.assertEquals(10, (int) operations.getUserByUsername("username").getAlbums().get(0));
            Assert.assertNull(operations.getAlbumById(10).getSliceURL("username"));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void getAlbumsTest() {
        try {
            Assert.assertEquals(0, operations.getAlbums().size());
            Assert.assertEquals(0, operations.getAlbumsLength());
            operations.addUser(new User("username", "password", new byte[256]));
            operations.addAlbum(new Album("album", 10), "username");
            Assert.assertEquals(1, operations.getAlbums().size());
            Assert.assertEquals(1, operations.getAlbumsLength());
            operations.addAlbum(new Album("album", 11), "username");
            Assert.assertEquals(2, operations.getAlbums().size());
            Assert.assertEquals(2, operations.getAlbumsLength());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void getAlbumByIdTest() {
        try {
            Assert.assertNull(operations.getAlbumById(0));
            Assert.assertNull(operations.getAlbumById(1));
            operations.addUser(new User("username", "password", new byte[256]));
            operations.addAlbum(new Album("album", 1), "username");
            Assert.assertNull(operations.getAlbumById(0));
            Assert.assertEquals("album", operations.getAlbumById(1).getName());
            Assert.assertNull(operations.getAlbumById(2));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
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
