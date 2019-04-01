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
            Album album = new Album("album", 1);
            String returnString = operations.addAlbum(album);
            Assert.assertEquals("Album successfully added", returnString);
            Assert.assertEquals(1, operations.getAlbumsLength());

            String jsonString = FileUtils.readFileToString(new File(Operations.STATE_BACKUP_PATH), "UTF-8");
            jsonString = jsonString.replace("\n", "").replace("\r", "");
            Assert.assertEquals("{\"albums\":{\"1\":{\"id\":1,\"slices\":{},\"name\":\"album\"}},\"users\":{},\"sessions\":{},\"logs\":[]}", jsonString);

            operations = null;
            Operations.cleanServer();

            PrintWriter writer = new PrintWriter(Operations.STATE_BACKUP_PATH);
            writer.println(jsonString);
            writer.close();

            operations = Operations.getServer();
            Assert.assertEquals(1, operations.getAlbumsLength());
            Assert.assertEquals(0, operations.getSessionsLength());
            Assert.assertEquals(0, operations.getUsersLength());
            Assert.assertEquals(0, operations.getLogsLength());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void albumAddExistingTest() {
        try {
            Assert.assertEquals(0, operations.getAlbumsLength());
            Album album = new Album("album", 1);
            operations.addAlbum(album);
            String returnValue = operations.addAlbum(album);

            Assert.assertEquals("Album already exists", returnValue);
            Assert.assertEquals(1, operations.getAlbumsLength());
            Assert.assertEquals(0, operations.getSessionsLength());
            Assert.assertEquals(0, operations.getUsersLength());
            Assert.assertEquals(0, operations.getLogsLength());

            String jsonString = FileUtils.readFileToString(new File(Operations.STATE_BACKUP_PATH), "UTF-8");
            jsonString = jsonString.replace("\n", "").replace("\r", "");
            Assert.assertEquals("{\"albums\":{\"1\":{\"id\":1,\"slices\":{},\"name\":\"album\"}},\"users\":{},\"sessions\":{},\"logs\":[]}", jsonString);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void albumAddNullTest() {
        try {
            String returnString = operations.addAlbum(null);
            Assert.assertEquals("Album cannot be null", returnString);
            Assert.assertEquals(0, operations.getAlbumsLength());
            Assert.assertEquals(0, operations.getSessionsLength());
            Assert.assertEquals(0, operations.getUsersLength());
            Assert.assertEquals(0, operations.getLogsLength());

            String jsonString = FileUtils.readFileToString(new File(Operations.STATE_BACKUP_PATH), "UTF-8");
            jsonString = jsonString.replace("\n", "").replace("\r", "");
            Assert.assertEquals("{\"albums\":{},\"users\":{},\"sessions\":{},\"logs\":[]}", jsonString);

            returnString = operations.addAlbum(new Album("album", 1));
            Assert.assertEquals("Album successfully added", returnString);
            Assert.assertEquals(1, operations.getAlbumsLength());
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
            operations.addAlbum(new Album("album", 1));
            Assert.assertEquals(1, operations.getAlbums().size());
            Assert.assertEquals(1, operations.getAlbumsLength());
            operations.addAlbum(new Album("album", 1));
            Assert.assertEquals(1, operations.getAlbums().size());
            Assert.assertEquals(1, operations.getAlbumsLength());
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
            operations.addAlbum(new Album("album", 1));
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
