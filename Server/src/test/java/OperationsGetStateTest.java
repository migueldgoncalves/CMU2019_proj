import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.PrintWriter;

public class OperationsGetStateTest {

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
    public void validBackupFileTest() {

    }

    @Test
    public void noBackupFileTest() {
        try {
            Assert.assertFalse(original.exists());
            operations = Operations.getServer();
            Assert.assertEquals(0, operations.getAlbums().size());
            Assert.assertEquals(0, operations.getSessions().size());
            Assert.assertEquals(0, operations.getUsers().size());
            Assert.assertEquals("{\"albums\":{},\"users\":{},\"sessions\":{}}",
                    FileUtils.readFileToString(new File(Operations.STATE_BACKUP_PATH), "UTF-8").
                            replace("\n", "").replace("\r", ""));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void invalidBackupFileTest() {
        try {
            new File(Operations.STATE_BACKUP_PATH);
            PrintWriter writer = new PrintWriter(Operations.STATE_BACKUP_PATH);
            writer.println("Invalid backup file");
            writer.close();
            Assert.assertEquals("Invalid backup file", FileUtils.readFileToString(new File(Operations.STATE_BACKUP_PATH), "UTF-8").
                    replace("\n", "").replace("\r", ""));

            operations = Operations.getServer();
            Assert.assertEquals(0, operations.getAlbums().size());
            Assert.assertEquals(0, operations.getSessions().size());
            Assert.assertEquals(0, operations.getUsers().size());
            Assert.assertEquals("{\"albums\":{},\"users\":{},\"sessions\":{}}",
                    FileUtils.readFileToString(new File(Operations.STATE_BACKUP_PATH), "UTF-8").
                            replace("\n", "").replace("\r", ""));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @After
    public void tearDown() {
        //If the test created a test backup file, it is deleted
        if (original.exists() && !original.isDirectory())
            try {
                original.delete();
            } catch (Exception e) {
                e.printStackTrace();
            }
        //If there was already a backup file, it is moved back to the backup directory
        if (temporary.exists() && !temporary.isDirectory()) {
            try {
                FileUtils.moveFile(temporary, original);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Operations.cleanServer();
        operations = null;
    }
}
