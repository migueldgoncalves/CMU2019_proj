import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

public class OperationsLogsAddsGetsTest {

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
    public void addValidLogTest() {
        try {
            AppRequest appRequest = new AppRequest();
            appRequest.setUsername("username");
            appRequest.setPassword("password");
            AppResponse appResponse = new AppResponse();
            appResponse.setSuccess("Success");

            String response = operations.addLog("operation", appRequest, appResponse);
            Assert.assertEquals("Operation successfully logged", response);
            Assert.assertTrue(operations.getLogs().contains("Operation ID: 1"));
            Assert.assertTrue(operations.getLogs().contains("Operation name: operation"));
            Assert.assertTrue(operations.getLogs().contains("Operation time:"));
            Assert.assertTrue(operations.getLogs().contains("Operation input: {\"username\":\"username\",\"password\":\"password\",\"sessionId\":0}"));
            Assert.assertTrue(operations.getLogs().contains("Operation output: {\"success\":\"Success\",\"sessionId\":0,\"albumId\":0}"));

            Assert.assertEquals(0, operations.getAlbumsLength());
            Assert.assertEquals(0, operations.getSessionsLength());
            Assert.assertEquals(0, operations.getUsersLength());
            Assert.assertEquals(1, operations.getLogsLength());

            String jsonString = FileUtils.readFileToString(new File(Operations.STATE_BACKUP_PATH), "UTF-8");
            jsonString = jsonString.replace("\n", "").replace("\r", "");
            Assert.assertTrue(jsonString.contains("{\"albums\":{},\"users\":{},\"sessions\":{},\"logs\":\"Operation ID: 1\\nOperation name: operation\\nOperation time:"));
            Assert.assertTrue(jsonString.contains("\\nOperation input: {\\\"username\\\":\\\"username\\\",\\\"password\\\":\\\"password\\\",\\\"sessionId\\\":0}\\nOperation output: {\\\"success\\\":\\\"Success\\\",\\\"sessionId\\\":0,\\\"albumId\\\":0}\\n---------------------------------------------------------------------------------------------------------------\\n\",\"counterAlbum\":0,\"counterLog\":1}"));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void addNullOperationSessionTest() {
        try {
            AppRequest appRequest = new AppRequest();
            appRequest.setUsername("username");
            appRequest.setPassword("password");
            AppResponse appResponse = new AppResponse();
            appResponse.setSuccess("Success");

            String response = operations.addLog(null, appRequest, appResponse);
            Assert.assertEquals("Operation name cannot be null", response);

            Assert.assertEquals(0, operations.getAlbumsLength());
            Assert.assertEquals(0, operations.getSessionsLength());
            Assert.assertEquals(0, operations.getUsersLength());
            Assert.assertEquals(0, operations.getLogsLength());
            Assert.assertEquals(0, operations.getLogs().length());

            String jsonString = FileUtils.readFileToString(new File(Operations.STATE_BACKUP_PATH), "UTF-8");
            jsonString = jsonString.replace("\n", "").replace("\r", "");
            Assert.assertEquals("{\"albums\":{},\"users\":{},\"sessions\":{},\"logs\":\"\",\"counterAlbum\":0,\"counterLog\":0}", jsonString);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void addNullRequestLogTest() {
        try {
            AppResponse appResponse = new AppResponse();
            appResponse.setSuccess("Success");

            String response = operations.addLog("operation", null, appResponse);
            Assert.assertEquals("Operation request cannot be null", response);

            Assert.assertEquals(0, operations.getAlbumsLength());
            Assert.assertEquals(0, operations.getSessionsLength());
            Assert.assertEquals(0, operations.getUsersLength());
            Assert.assertEquals(0, operations.getLogsLength());
            Assert.assertEquals(0, operations.getLogs().length());

            String jsonString = FileUtils.readFileToString(new File(Operations.STATE_BACKUP_PATH), "UTF-8");
            jsonString = jsonString.replace("\n", "").replace("\r", "");
            Assert.assertEquals("{\"albums\":{},\"users\":{},\"sessions\":{},\"logs\":\"\",\"counterAlbum\":0,\"counterLog\":0}", jsonString);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void addNullResponseLogTest() {
        try {
            AppRequest appRequest = new AppRequest();
            appRequest.setUsername("username");
            appRequest.setPassword("password");

            String response = operations.addLog("operation", appRequest, null);
            Assert.assertEquals("Operation response cannot be null", response);

            Assert.assertEquals(0, operations.getAlbumsLength());
            Assert.assertEquals(0, operations.getSessionsLength());
            Assert.assertEquals(0, operations.getUsersLength());
            Assert.assertEquals(0, operations.getLogsLength());
            Assert.assertEquals(0, operations.getLogs().length());

            String jsonString = FileUtils.readFileToString(new File(Operations.STATE_BACKUP_PATH), "UTF-8");
            jsonString = jsonString.replace("\n", "").replace("\r", "");
            Assert.assertEquals("{\"albums\":{},\"users\":{},\"sessions\":{},\"logs\":\"\",\"counterAlbum\":0,\"counterLog\":0}", jsonString);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void addAllParametersNullResponseLogTest() {
        try {
            String response = operations.addLog(null, null, null);
            Assert.assertEquals("Operation name cannot be null", response);

            Assert.assertEquals(0, operations.getAlbumsLength());
            Assert.assertEquals(0, operations.getSessionsLength());
            Assert.assertEquals(0, operations.getUsersLength());
            Assert.assertEquals(0, operations.getLogsLength());
            Assert.assertEquals(0, operations.getLogs().length());

            String jsonString = FileUtils.readFileToString(new File(Operations.STATE_BACKUP_PATH), "UTF-8");
            jsonString = jsonString.replace("\n", "").replace("\r", "");
            Assert.assertEquals("{\"albums\":{},\"users\":{},\"sessions\":{},\"logs\":\"\",\"counterAlbum\":0,\"counterLog\":0}", jsonString);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void getEmptyLogsBaseMethodTest() {
        try {
            Assert.assertEquals(0, operations.getLogsLength());
            Assert.assertEquals(0, operations.getLogs().length());
            String jsonString = FileUtils.readFileToString(new File(Operations.STATE_BACKUP_PATH), "UTF-8");
            jsonString = jsonString.replace("\n", "").replace("\r", "");
            Assert.assertTrue(jsonString.contains("{\"albums\":{},\"users\":{},\"sessions\":{},\"logs\":\"\",\"counterAlbum\":0,\"counterLog\":0}"));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void getEmptyLogsServiceMethodTest() {
        try {
            AppResponse response = operations.serviceGetLogs();
            Assert.assertEquals(309, response.getLogs().length());
            Assert.assertTrue(operations.getLogs().contains("Operation ID: 1"));
            Assert.assertTrue(operations.getLogs().contains("Operation name: LOGS"));
            Assert.assertTrue(operations.getLogs().contains("Operation time:"));
            Assert.assertTrue(operations.getLogs().contains("Operation input: {\"sessionId\":0}"));
            Assert.assertTrue(operations.getLogs().contains("Operation output: {\"success\":\"Logs correctly obtained\",\"sessionId\":0,\"albumId\":0}"));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void getNotEmptyLogsServiceMethodTest() {
        try {
            operations.signUp("username", "password", new byte[256]);
            int sessionId = operations.logIn("username", "password").getSessionId();
            operations.logOut(sessionId);
            Assert.assertEquals(3, operations.getLogsLength());

            Assert.assertTrue(operations.getLogs().contains("Operation ID: 1"));
            Assert.assertTrue(operations.getLogs().contains("Operation name: SIGNUP"));
            Assert.assertTrue(operations.getLogs().contains("Operation time:"));
            Assert.assertTrue(operations.getLogs().contains("Operation input: {\"username\":\"username\",\"password\":\"password\",\"publicKey\":[0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0],\"sessionId\":0}"));
            Assert.assertTrue(operations.getLogs().contains("Operation output: {\"success\":\"User created successfully\",\"sessionId\":0,\"albumId\":0}"));
            Assert.assertTrue(operations.getLogs().contains("---------------------------------------------------------------------------------------------------------------"));
            Assert.assertTrue(operations.getLogs().contains("Operation ID: 2"));
            Assert.assertTrue(operations.getLogs().contains("Operation name: LOGIN"));
            Assert.assertTrue(operations.getLogs().contains("Operation time:"));
            Assert.assertTrue(operations.getLogs().contains("Operation input: {\"username\":\"username\",\"password\":\"password\",\"sessionId\":0}"));
            Assert.assertTrue(operations.getLogs().contains("Operation output: {\"success\":\"Login successful\",\"sessionId\":"));
            Assert.assertTrue(operations.getLogs().contains("Operation ID: 3"));
            Assert.assertTrue(operations.getLogs().contains("Operation name: LOGOUT"));
            Assert.assertTrue(operations.getLogs().contains("Operation time:"));
            Assert.assertTrue(operations.getLogs().contains("Operation input: {\"sessionId\":"));
            Assert.assertTrue(operations.getLogs().contains("Operation output: {\"success\":\"Session successfully deleted\",\"sessionId\":0,\"albumId\":0}"));
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
