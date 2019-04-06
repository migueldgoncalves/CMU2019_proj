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

            Assert.assertTrue(operations.getLogs().contains("Operation ID: 1"));
            Assert.assertTrue(operations.getLogs().contains("Operation name: SIGNUP"));
            Assert.assertTrue(operations.getLogs().contains("Operation time:"));
            Assert.assertTrue(operations.getLogs().contains("Operation input: {\"username\":\"username\",\"password\":\"password\",\"publicKey\":[0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0],\"sessionId\":0}"));
            Assert.assertTrue(operations.getLogs().contains("Operation output: {\"success\":\"User created successfully\",\"sessionId\":0"));
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

            Assert.assertTrue(operations.getLogs().contains("Operation ID: 1"));
            Assert.assertTrue(operations.getLogs().contains("Operation ID: 2"));
            Assert.assertTrue(operations.getLogs().contains("Operation ID: 3"));
            Assert.assertTrue(operations.getLogs().contains("Operation ID: 4"));
            Assert.assertTrue(operations.getLogs().contains("Operation ID: 5"));
            Assert.assertTrue(operations.getLogs().contains("Operation ID: 6"));
            Assert.assertTrue(operations.getLogs().contains("Operation ID: 7"));
            Assert.assertTrue(operations.getLogs().contains("Operation ID: 8"));
            Assert.assertFalse(operations.getLogs().contains("Operation ID: 9"));
            Assert.assertTrue(operations.getLogs().contains("Operation name: SIGNUP"));
            Assert.assertTrue(operations.getLogs().contains("Operation time:"));
            Assert.assertTrue(operations.getLogs().contains("Operation input: {\"password\":\"password\",\"publicKey\":[0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0],\"sessionId\":0}"));
            Assert.assertTrue(operations.getLogs().contains("Operation input: {\"username\":\"username\",\"password\":\"password\",\"publicKey\":[0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0],\"sessionId\":0}"));
            Assert.assertTrue(operations.getLogs().contains("Operation output: {\"error\":\"Username cannot be null\",\"sessionId\":0"));
            Assert.assertTrue(operations.getLogs().contains("Operation output: {\"error\":\"Username cannot be empty\",\"sessionId\":0"));
            Assert.assertTrue(operations.getLogs().contains("Operation output: {\"error\":\"Username must only contain digits and letters\",\"sessionId\":0"));
            Assert.assertTrue(operations.getLogs().contains("Operation output: {\"error\":\"Username must have at least " + Operations.MIN_USERNAME_LENGTH + " characters\",\"sessionId\":0"));
            Assert.assertTrue(operations.getLogs().contains("Operation output: {\"error\":\"Username must have at most " + Operations.MAX_USERNAME_LENGTH + " characters\",\"sessionId\":0"));
            Assert.assertTrue(operations.getLogs().contains("Operation output: {\"success\":\"User created successfully\",\"sessionId\":0"));
            Assert.assertTrue(operations.getLogs().contains("Operation output: {\"error\":\"Username already exists\",\"sessionId\":0"));
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

            Assert.assertTrue(operations.getLogs().contains("Operation ID: 1"));
            Assert.assertTrue(operations.getLogs().contains("Operation ID: 2"));
            Assert.assertTrue(operations.getLogs().contains("Operation ID: 3"));
            Assert.assertTrue(operations.getLogs().contains("Operation ID: 4"));
            Assert.assertTrue(operations.getLogs().contains("Operation ID: 5"));
            Assert.assertFalse(operations.getLogs().contains("Operation ID: 6"));
            Assert.assertTrue(operations.getLogs().contains("Operation name: SIGNUP"));
            Assert.assertTrue(operations.getLogs().contains("Operation time:"));
            Assert.assertTrue(operations.getLogs().contains("Operation input: {\"username\":\"username\",\"publicKey\":[0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0],\"sessionId\":0}"));
            Assert.assertTrue(operations.getLogs().contains("Operation output: {\"error\":\"Password cannot be null\",\"sessionId\":0"));
            Assert.assertTrue(operations.getLogs().contains("Operation output: {\"error\":\"Password cannot be empty\",\"sessionId\":0"));
            Assert.assertTrue(operations.getLogs().contains("Operation output: {\"error\":\"Password must have at least " + Operations.MIN_PASSWORD_LENGTH + " characters\",\"sessionId\":0"));
            Assert.assertTrue(operations.getLogs().contains("Operation output: {\"error\":\"Password must have at most " + Operations.MAX_PASSWORD_LENGTH + " characters\",\"sessionId\":0"));
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

            Assert.assertTrue(operations.getLogs().contains("Operation ID: 1"));
            Assert.assertTrue(operations.getLogs().contains("Operation ID: 2"));
            Assert.assertTrue(operations.getLogs().contains("Operation ID: 3"));
            Assert.assertTrue(operations.getLogs().contains("Operation ID: 4"));
            Assert.assertFalse(operations.getLogs().contains("Operation ID: 5"));
            Assert.assertTrue(operations.getLogs().contains("Operation name: SIGNUP"));
            Assert.assertTrue(operations.getLogs().contains("Operation time:"));
            Assert.assertTrue(operations.getLogs().contains("Operation input: {\"username\":\"username\",\"password\":\"password\",\"publicKey\":[0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0],\"sessionId\":0}"));
            Assert.assertTrue(operations.getLogs().contains("Operation output: {\"error\":\"Public key cannot be null\",\"sessionId\":0"));
            Assert.assertTrue(operations.getLogs().contains("Operation output: {\"error\":\"Public key must have " + Operations.RSA_KEY_BYTE_LENGTH * 8 + " bits\",\"sessionId\":0"));
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
            Assert.assertTrue(operations.getLogs().contains("Operation ID: 1"));
            Assert.assertFalse(operations.getLogs().contains("Operation ID: 2"));
            Assert.assertTrue(operations.getLogs().contains("Operation name: SIGNUP"));
            Assert.assertTrue(operations.getLogs().contains("Operation time:"));
            Assert.assertTrue(operations.getLogs().contains("Operation input: {\"password\":\"x\",\"publicKey\":[0],\"sessionId\":0}"));
            Assert.assertTrue(operations.getLogs().contains("Operation output: {\"error\":\"Username cannot be null\",\"sessionId\":0"));
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
