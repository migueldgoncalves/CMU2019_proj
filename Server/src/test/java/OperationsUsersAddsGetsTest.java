import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.PrintWriter;

public class OperationsUsersAddsGetsTest {

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
    public void userAddNewTest() {
        try {
            Assert.assertEquals(0, operations.getUsersLength());
            User user = new User("username", "password", new byte[5]);
            String returnString = operations.addUser(user);
            Assert.assertEquals("User successfully added", returnString);
            Assert.assertEquals(1, operations.getUsersLength());

            String jsonString = FileUtils.readFileToString(new File(Operations.STATE_BACKUP_PATH), "UTF-8");
            jsonString = jsonString.replace("\n", "").replace("\r", "");
            Assert.assertEquals("{\"albums\":{},\"users\":{\"username\":{\"username\":\"username\",\"password\":\"password\",\"publicKey\":[0,0,0,0,0],\"albums\":[]}},\"sessions\":{}}", jsonString);

            operations = null;
            Operations.cleanServer();

            PrintWriter writer = new PrintWriter(Operations.STATE_BACKUP_PATH);
            writer.println(jsonString);
            writer.close();

            operations = Operations.getServer();
            Assert.assertEquals(0, operations.getAlbumsLength());
            Assert.assertEquals(0, operations.getSessionsLength());
            Assert.assertEquals(1, operations.getUsersLength());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void userAddExistingTest() {
        try {
            Assert.assertEquals(0, operations.getUsersLength());
            User user = new User("username", "password", new byte[5]);
            operations.addUser(user);
            String returnValue = operations.addUser(user);

            Assert.assertEquals("User already exists", returnValue);
            Assert.assertEquals(0, operations.getAlbumsLength());
            Assert.assertEquals(0, operations.getSessionsLength());
            Assert.assertEquals(1, operations.getUsersLength());

            String jsonString = FileUtils.readFileToString(new File(Operations.STATE_BACKUP_PATH), "UTF-8");
            jsonString = jsonString.replace("\n", "").replace("\r", "");
            Assert.assertEquals("{\"albums\":{},\"users\":{\"username\":{\"username\":\"username\",\"password\":\"password\",\"publicKey\":[0,0,0,0,0],\"albums\":[]}},\"sessions\":{}}", jsonString);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void userAddNullTest() {
        try {
            String returnString = operations.addUser(null);
            Assert.assertEquals("User cannot be null", returnString);
            Assert.assertEquals(0, operations.getAlbumsLength());
            Assert.assertEquals(0, operations.getSessionsLength());
            Assert.assertEquals(0, operations.getUsersLength());

            String jsonString = FileUtils.readFileToString(new File(Operations.STATE_BACKUP_PATH), "UTF-8");
            jsonString = jsonString.replace("\n", "").replace("\r", "");
            Assert.assertEquals("{\"albums\":{},\"users\":{},\"sessions\":{}}", jsonString);

            returnString = operations.addUser(new User("username", "password", new byte[5]));
            Assert.assertEquals("User successfully added", returnString);
            Assert.assertEquals(1, operations.getUsersLength());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void getUsersTest() {
        Assert.assertEquals(0, operations.getUsers().size());
        Assert.assertEquals(0, operations.getUsersLength());
        operations.addUser(new User("username", "password", new byte[5]));
        Assert.assertEquals(1, operations.getUsers().size());
        Assert.assertEquals(1, operations.getUsersLength());
    }

    @Test
    public void getUsersByIdTest() {
        Assert.assertNull(operations.getUserByUsername(null));
        Assert.assertNull(operations.getUserByUsername("username"));
        Assert.assertNull(operations.getUserByUsername("anotherUsername"));
        User user = new User("username", "password", new byte[5]);
        operations.addUser(user);
        Assert.assertNull(operations.getUserByUsername(null));
        Assert.assertEquals("password", operations.getUserByUsername("username").getPassword());
        Assert.assertNull(operations.getUserByUsername("anotherUsername"));
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
