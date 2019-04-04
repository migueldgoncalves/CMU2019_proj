import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

public class OperationsSignUpTest {

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
    public void validSignUp() {
        try {
            AppResponse response = operations.signUp("username", "password", new byte[256]);
            Assert.assertEquals("User created successfully", response.getSuccess());
            Assert.assertNull(response.getError());
            Assert.assertEquals(1, operations.getUsersLength());
            Assert.assertEquals(1, operations.getLogsLength());
            Assert.assertEquals("username", operations.getUserByUsername("username").getUsername());
            Assert.assertEquals("password", operations.getUserByUsername("username").getPassword());
            Assert.assertEquals(256, operations.getUserByUsername("username").getPublicKey().length);
            for (int i = 0; i < 256; i++)
                Assert.assertEquals(0, operations.getUserByUsername("username").getPublicKey()[i]);
            Assert.assertEquals(0, operations.getUserByUsername("username").getUserAlbumNumber());

            /*Assert.assertEquals(Operations.SIGNUP_OPERATION, operations.getLogs().get(0).getOperation());
            Assert.assertTrue(new Date().getTime() - operations.getLogs().get(0).getTimestamp().getTime() < 1000);
            Assert.assertEquals("username", operations.getLogs().get(0).getRequest().getUsername());
            Assert.assertEquals("password", operations.getLogs().get(0).getRequest().getPassword());
            Assert.assertEquals("User created successfully", operations.getLogs().get(0).getResponse().getSuccess());
            Assert.assertNull(operations.getLogs().get(0).getResponse().getError());*/
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void invalidUsernameTest() {
        try {
            Assert.assertEquals("Username cannot be null", operations.signUp(null, "password", new byte[256]).getError());
            Assert.assertEquals("Username cannot be empty", operations.signUp("", "password", new byte[256]).getError());
            Assert.assertEquals("Username cannot be empty", operations.signUp("    ", "password", new byte[256]).getError());
            Assert.assertEquals("Username must only contain digits and letters", operations.signUp("username%", "password", new byte[256]).getError());
            Assert.assertEquals("Username must have at least " + Operations.MIN_USERNAME_LENGTH + " characters", operations.signUp("x", "password", new byte[256]).getError());
            Assert.assertEquals("Username must have at most " + Operations.MAX_USERNAME_LENGTH + " characters", operations.signUp("123456789012345678901234567890123456789012345678901234567890", "password", new byte[256]).getError());
            Assert.assertEquals(0, operations.getUsersLength());
            operations.signUp("username", "password", new byte[256]);
            Assert.assertEquals("Username already exists", operations.signUp("username", "password", new byte[256]).getError());
            Assert.assertEquals(1, operations.getUsersLength());
            Assert.assertEquals(8, operations.getLogsLength());

            /*Assert.assertEquals("Username cannot be null", operations.getLogs().get(0).getResponse().getError());
            Assert.assertEquals("Username cannot be empty", operations.getLogs().get(1).getResponse().getError());
            Assert.assertEquals("Username cannot be empty", operations.getLogs().get(2).getResponse().getError());
            Assert.assertEquals("Username must only contain digits and letters", operations.getLogs().get(3).getResponse().getError());
            Assert.assertEquals("Username must have at least " + Operations.MIN_USERNAME_LENGTH + " characters", operations.getLogs().get(4).getResponse().getError());
            Assert.assertEquals("Username must have at most " + Operations.MAX_USERNAME_LENGTH + " characters", operations.getLogs().get(5).getResponse().getError());
            Assert.assertEquals("User created successfully", operations.getLogs().get(6).getResponse().getSuccess());
            Assert.assertEquals("Username already exists", operations.getLogs().get(7).getResponse().getError());*/
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void invalidPasswordTest() {
        try {
            Assert.assertEquals("Password cannot be null", operations.signUp("username", null, new byte[256]).getError());
            Assert.assertEquals("Password cannot be empty", operations.signUp("username", "", new byte[256]).getError());
            Assert.assertEquals("Password cannot be empty", operations.signUp("username", "    ", new byte[256]).getError());
            Assert.assertEquals("Password must have at least " + Operations.MIN_PASSWORD_LENGTH + " characters", operations.signUp("username", "x", new byte[256]).getError());
            Assert.assertEquals("Password must have at most " + Operations.MAX_PASSWORD_LENGTH + " characters", operations.signUp("username", "123456789012345678901234567890123456789012345678901234567890", new byte[256]).getError());
            Assert.assertEquals(0, operations.getUsersLength());
            Assert.assertEquals(5, operations.getLogsLength());

            /*Assert.assertEquals("Password cannot be null", operations.getLogs().get(0).getResponse().getError());
            Assert.assertEquals("Password cannot be empty", operations.getLogs().get(1).getResponse().getError());
            Assert.assertEquals("Password cannot be empty", operations.getLogs().get(2).getResponse().getError());
            Assert.assertEquals("Password must have at least " + Operations.MIN_PASSWORD_LENGTH + " characters", operations.getLogs().get(3).getResponse().getError());
            Assert.assertEquals("Password must have at most " + Operations.MAX_PASSWORD_LENGTH + " characters", operations.getLogs().get(4).getResponse().getError());*/
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void invalidPublicKeyTest() {
        try {
            Assert.assertEquals("Public key cannot be null", operations.signUp("username", "password", null).getError());
            Assert.assertEquals("Public key must have " + Operations.RSA_KEY_BYTE_LENGTH * 8 + " bits", operations.signUp("username", "password", new byte[1]).getError());
            Assert.assertEquals("Public key must have " + Operations.RSA_KEY_BYTE_LENGTH * 8 + " bits", operations.signUp("username", "password", new byte[255]).getError());
            Assert.assertEquals("Public key must have " + Operations.RSA_KEY_BYTE_LENGTH * 8 + " bits", operations.signUp("username", "password", new byte[257]).getError());
            Assert.assertEquals(0, operations.getUsersLength());
            Assert.assertEquals(4, operations.getLogsLength());

            /*Assert.assertEquals("Public key cannot be null", operations.getLogs().get(0).getResponse().getError());
            Assert.assertEquals("Public key must have " + Operations.RSA_KEY_BYTE_LENGTH * 8 + " bits", operations.getLogs().get(1).getResponse().getError());
            Assert.assertEquals("Public key must have " + Operations.RSA_KEY_BYTE_LENGTH * 8 + " bits", operations.getLogs().get(2).getResponse().getError());
            Assert.assertEquals("Public key must have " + Operations.RSA_KEY_BYTE_LENGTH * 8 + " bits", operations.getLogs().get(3).getResponse().getError());*/
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void allInvalidParameters() {
        try {
            Assert.assertEquals("Username cannot be null", operations.signUp(null, "x", new byte[1]).getError());
            Assert.assertEquals(0, operations.getUsersLength());
            Assert.assertEquals(1, operations.getLogsLength());
            //Assert.assertEquals("Username cannot be null", operations.getLogs().get(0).getResponse().getError());
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
