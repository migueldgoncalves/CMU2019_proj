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
        String result = operations.signUp("username", "password", new byte[256]);
        Assert.assertEquals("User created successfully", result);
        Assert.assertEquals(1, operations.getUsersLength());
        Assert.assertEquals("username", operations.getUserByUsername("username").getUsername());
        Assert.assertEquals("password", operations.getUserByUsername("username").getPassword());
        Assert.assertEquals(256, operations.getUserByUsername("username").getPublicKey().length);
        Assert.assertEquals(0, operations.getUserByUsername("username").getUserAlbumNumber());
    }

    @Test
    public void invalidUsernameTest() {
        Assert.assertEquals("Username cannot be null", operations.signUp(null, "password", new byte[256]));
        Assert.assertEquals("Username cannot be empty", operations.signUp("", "password", new byte[256]));
        Assert.assertEquals("Username cannot be empty", operations.signUp("    ", "password", new byte[256]));
        Assert.assertEquals("Username must only contain digits and letters", operations.signUp("username%", "password", new byte[256]));
        Assert.assertEquals("Username must have at least " + Operations.MIN_USERNAME_LENGTH + " characters", operations.signUp("x", "password", new byte[256]));
        Assert.assertEquals("Username must have at most " + Operations.MAX_USERNAME_LENGTH + " characters", operations.signUp("123456789012345678901234567890123456789012345678901234567890", "password", new byte[256]));
        Assert.assertEquals(0, operations.getUsersLength());
        operations.signUp("username", "password", new byte[256]);
        Assert.assertEquals("Username already exists", operations.signUp("username", "password", new byte[256]));
        Assert.assertEquals(1, operations.getUsersLength());
    }

    @Test
    public void invalidPasswordTest() {
        Assert.assertEquals("Password cannot be null", operations.signUp("username", null, new byte[256]));
        Assert.assertEquals("Password cannot be empty", operations.signUp("username", "", new byte[256]));
        Assert.assertEquals("Password cannot be empty", operations.signUp("username", "    ", new byte[256]));
        Assert.assertEquals("Password must have at least " + Operations.MIN_PASSWORD_LENGTH + " characters", operations.signUp("username", "x", new byte[256]));
        Assert.assertEquals("Password must have at most " + Operations.MAX_PASSWORD_LENGTH + " characters", operations.signUp("username", "123456789012345678901234567890123456789012345678901234567890", new byte[256]));
        Assert.assertEquals(0, operations.getUsersLength());
    }

    @Test
    public void invalidPublicKeyTest() {
        Assert.assertEquals("Public key cannot be null", operations.signUp("username", "password", null));
        Assert.assertEquals("Public key must have " + Operations.RSA_KEY_BYTE_LENGTH * 8 + " bits", operations.signUp("username", "password", new byte[1]));
        Assert.assertEquals("Public key must have " + Operations.RSA_KEY_BYTE_LENGTH * 8 + " bits", operations.signUp("username", "password", new byte[255]));
        Assert.assertEquals("Public key must have " + Operations.RSA_KEY_BYTE_LENGTH * 8 + " bits", operations.signUp("username", "password", new byte[257]));
        Assert.assertEquals(0, operations.getUsersLength());
    }

    @Test
    public void allInvalidParameters() {
        Assert.assertEquals("Username cannot be null", operations.signUp(null, "x", new byte[1]));
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
