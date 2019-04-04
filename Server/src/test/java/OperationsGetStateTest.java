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
    public void validNotEmptyStateBackupFileTest() {
        try {
            new File(Operations.STATE_BACKUP_PATH);
            PrintWriter writer = new PrintWriter(Operations.STATE_BACKUP_PATH);
            writer.println("{\"albums\":{\"1\":{\"id\":1,\"slices\":{},\"name\":\"album\"}},\"users\":{\"user1\":{\"username\":\"user1\",\"password\":\"password1\",\"publicKey\":[0,0,0,0,0],\"albums\":[]},\"user2\":{\"username\":\"user2\",\"password\":\"password2\",\"publicKey\":[0,0,0,0,0,0,0,0,0,0],\"albums\":[]},\"user3\":{\"username\":\"user3\",\"password\":\"password3\",\"publicKey\":[0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0],\"albums\":[]}},\"sessions\":{\"1713868246\":{\"userId\":1,\"sessionId\":1713868246,\"loginTime\":\"Mar 27, 2019 5:53:33 PM\",\"sessionDuration\":5},\"182718366\":{\"userId\":2,\"sessionId\":182718366,\"loginTime\":\"Mar 27, 2019 5:53:33 PM\",\"sessionDuration\":10}},\"logs\":[{\"operation\":\"SIGNUP\",\"timestamp\":\"Mar 30, 2019 4:11:34 AM\",\"request\":{\"username\":\"username\",\"password\":\"wrongPassword\",\"sessionId\":0},\"response\":{\"error\":\"Invalid Password! Please Try Again\",\"sessionId\":0}}]}");
            writer.close();
            Assert.assertEquals("{\"albums\":{\"1\":{\"id\":1,\"slices\":{},\"name\":\"album\"}},\"users\":{\"user1\":{\"username\":\"user1\",\"password\":\"password1\",\"publicKey\":[0,0,0,0,0],\"albums\":[]},\"user2\":{\"username\":\"user2\",\"password\":\"password2\",\"publicKey\":[0,0,0,0,0,0,0,0,0,0],\"albums\":[]},\"user3\":{\"username\":\"user3\",\"password\":\"password3\",\"publicKey\":[0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0],\"albums\":[]}},\"sessions\":{\"1713868246\":{\"userId\":1,\"sessionId\":1713868246,\"loginTime\":\"Mar 27, 2019 5:53:33 PM\",\"sessionDuration\":5},\"182718366\":{\"userId\":2,\"sessionId\":182718366,\"loginTime\":\"Mar 27, 2019 5:53:33 PM\",\"sessionDuration\":10}},\"logs\":[{\"operation\":\"SIGNUP\",\"timestamp\":\"Mar 30, 2019 4:11:34 AM\",\"request\":{\"username\":\"username\",\"password\":\"wrongPassword\",\"sessionId\":0},\"response\":{\"error\":\"Invalid Password! Please Try Again\",\"sessionId\":0}}]}", FileUtils.readFileToString(new File(Operations.STATE_BACKUP_PATH), "UTF-8").
                    replace("\n", "").replace("\r", ""));
            operations = Operations.getServer();
            Assert.assertEquals(1, operations.getAlbums().size());
            Assert.assertEquals(2, operations.getSessions().size());
            Assert.assertEquals(3, operations.getUsers().size());
            Assert.assertEquals(1, operations.getLogsLength());
            Assert.assertEquals("{\"albums\":{\"1\":{\"id\":1,\"slices\":{},\"name\":\"album\"}},\"users\":{\"user1\":{\"username\":\"user1\",\"password\":\"password1\",\"publicKey\":[0,0,0,0,0],\"albums\":[]},\"user2\":{\"username\":\"user2\",\"password\":\"password2\",\"publicKey\":[0,0,0,0,0,0,0,0,0,0],\"albums\":[]},\"user3\":{\"username\":\"user3\",\"password\":\"password3\",\"publicKey\":[0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0],\"albums\":[]}},\"sessions\":{\"1713868246\":{\"userId\":1,\"sessionId\":1713868246,\"loginTime\":\"Mar 27, 2019 5:53:33 PM\",\"sessionDuration\":5},\"182718366\":{\"userId\":2,\"sessionId\":182718366,\"loginTime\":\"Mar 27, 2019 5:53:33 PM\",\"sessionDuration\":10}},\"logs\":[{\"operation\":\"SIGNUP\",\"timestamp\":\"Mar 30, 2019 4:11:34 AM\",\"request\":{\"username\":\"username\",\"password\":\"wrongPassword\",\"sessionId\":0},\"response\":{\"error\":\"Invalid Password! Please Try Again\",\"sessionId\":0}}]}",
                    FileUtils.readFileToString(new File(Operations.STATE_BACKUP_PATH), "UTF-8").
                            replace("\n", "").replace("\r", ""));
            Assert.assertEquals("album", operations.getAlbumById(1).getName());
            Assert.assertEquals(10, operations.getSessionById(182718366).getSessionDuration());
            Assert.assertEquals(20, operations.getUserByUsername("user3").getPublicKey().length);
            //Assert.assertEquals("username", operations.getLogs().get(0).getRequest().getUsername());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void validEmptyStateBackupFileTest() {
        try {
            new File(Operations.STATE_BACKUP_PATH);
            PrintWriter writer = new PrintWriter(Operations.STATE_BACKUP_PATH);
            writer.println("{\"albums\":{},\"users\":{},\"sessions\":{},\"logs\":[]}");
            writer.close();
            Assert.assertEquals("{\"albums\":{},\"users\":{},\"sessions\":{},\"logs\":[]}", FileUtils.readFileToString(new File(Operations.STATE_BACKUP_PATH), "UTF-8").
                    replace("\n", "").replace("\r", ""));

            operations = Operations.getServer();
            Assert.assertEquals(0, operations.getAlbums().size());
            Assert.assertEquals(0, operations.getSessions().size());
            Assert.assertEquals(0, operations.getUsers().size());
            Assert.assertEquals(0, operations.getLogsLength());
            Assert.assertEquals("{\"albums\":{},\"users\":{},\"sessions\":{},\"logs\":[]}",
                    FileUtils.readFileToString(new File(Operations.STATE_BACKUP_PATH), "UTF-8").
                            replace("\n", "").replace("\r", ""));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void emptyBackupFileTest() {
        try {
            new File(Operations.STATE_BACKUP_PATH);
            PrintWriter writer = new PrintWriter(Operations.STATE_BACKUP_PATH);
            writer.println("");
            writer.close();
            Assert.assertEquals("", FileUtils.readFileToString(new File(Operations.STATE_BACKUP_PATH), "UTF-8").
                    replace("\n", "").replace("\r", ""));

            operations = Operations.getServer();
            Assert.assertEquals(0, operations.getAlbums().size());
            Assert.assertEquals(0, operations.getSessions().size());
            Assert.assertEquals(0, operations.getUsers().size());
            Assert.assertEquals(0, operations.getLogsLength());
            Assert.assertEquals("{\"albums\":{},\"users\":{},\"sessions\":{},\"logs\":[]}",
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
            Assert.assertEquals(0, operations.getLogsLength());
            Assert.assertEquals("{\"albums\":{},\"users\":{},\"sessions\":{},\"logs\":[]}",
                    FileUtils.readFileToString(new File(Operations.STATE_BACKUP_PATH), "UTF-8").
                            replace("\n", "").replace("\r", ""));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void noBackupFileTest() {
        try {
            Assert.assertFalse(original.exists());
            operations = Operations.getServer();
            Assert.assertEquals(0, operations.getAlbums().size());
            Assert.assertEquals(0, operations.getSessions().size());
            Assert.assertEquals(0, operations.getUsers().size());
            Assert.assertEquals(0, operations.getLogsLength());
            Assert.assertEquals("{\"albums\":{},\"users\":{},\"sessions\":{},\"logs\":[]}",
                    FileUtils.readFileToString(new File(Operations.STATE_BACKUP_PATH), "UTF-8").
                            replace("\n", "").replace("\r", ""));
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
