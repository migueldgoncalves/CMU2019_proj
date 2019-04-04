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
            /*Assert.assertEquals("operation", operations.getLogs().get(0).getOperation());
            Assert.assertTrue(new Date().getTime() - operations.getLogs().get(0).getTimestamp().getTime() < 1000);
            Assert.assertEquals("username", operations.getLogs().get(0).getRequest().getUsername());
            Assert.assertEquals("password", operations.getLogs().get(0).getRequest().getPassword());
            Assert.assertEquals("Success", operations.getLogs().get(0).getResponse().getSuccess());*/

            Assert.assertEquals(0, operations.getAlbumsLength());
            Assert.assertEquals(0, operations.getSessionsLength());
            Assert.assertEquals(0, operations.getUsersLength());
            Assert.assertEquals(1, operations.getLogsLength());

            String jsonString = FileUtils.readFileToString(new File(Operations.STATE_BACKUP_PATH), "UTF-8");
            jsonString = jsonString.replace("\n", "").replace("\r", "");
            Assert.assertTrue(jsonString.contains("{\"albums\":{},\"users\":{},\"sessions\":{},\"logs\":[{\"operation\":\"operation\",\"timestamp\":"));
            Assert.assertTrue(jsonString.contains(",\"request\":{\"username\":\"username\",\"password\":\"password\",\"sessionId\":0},\"response\":{\"success\":\"Success\",\"sessionId\":0}}]}"));
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

            String jsonString = FileUtils.readFileToString(new File(Operations.STATE_BACKUP_PATH), "UTF-8");
            jsonString = jsonString.replace("\n", "").replace("\r", "");
            Assert.assertEquals("{\"albums\":{},\"users\":{},\"sessions\":{},\"logs\":[]}", jsonString);
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
            Assert.assertEquals("App Request cannot be null", response);

            Assert.assertEquals(0, operations.getAlbumsLength());
            Assert.assertEquals(0, operations.getSessionsLength());
            Assert.assertEquals(0, operations.getUsersLength());
            Assert.assertEquals(0, operations.getLogsLength());

            String jsonString = FileUtils.readFileToString(new File(Operations.STATE_BACKUP_PATH), "UTF-8");
            jsonString = jsonString.replace("\n", "").replace("\r", "");
            Assert.assertEquals("{\"albums\":{},\"users\":{},\"sessions\":{},\"logs\":[]}", jsonString);
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
            Assert.assertEquals("App Response cannot be null", response);

            Assert.assertEquals(0, operations.getAlbumsLength());
            Assert.assertEquals(0, operations.getSessionsLength());
            Assert.assertEquals(0, operations.getUsersLength());
            Assert.assertEquals(0, operations.getLogsLength());

            String jsonString = FileUtils.readFileToString(new File(Operations.STATE_BACKUP_PATH), "UTF-8");
            jsonString = jsonString.replace("\n", "").replace("\r", "");
            Assert.assertEquals("{\"albums\":{},\"users\":{},\"sessions\":{},\"logs\":[]}", jsonString);
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

            String jsonString = FileUtils.readFileToString(new File(Operations.STATE_BACKUP_PATH), "UTF-8");
            jsonString = jsonString.replace("\n", "").replace("\r", "");
            Assert.assertEquals("{\"albums\":{},\"users\":{},\"sessions\":{},\"logs\":[]}", jsonString);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void getEmptyLogsBaseMethodTest() {
        try {
            Assert.assertEquals(0, operations.getLogsLength());
            String jsonString = FileUtils.readFileToString(new File(Operations.STATE_BACKUP_PATH), "UTF-8");
            jsonString = jsonString.replace("\n", "").replace("\r", "");
            Assert.assertTrue(jsonString.contains("{\"albums\":{},\"users\":{},\"sessions\":{},\"logs\":[]}"));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void getEmptyLogsServiceMethodTest() {
        try {
            AppResponse response = operations.serviceGetLogs();
            //Assert.assertEquals(1, response.getLogs().size());
            String jsonString = FileUtils.readFileToString(new File(Operations.STATE_BACKUP_PATH), "UTF-8");
            jsonString = jsonString.replace("\n", "").replace("\r", "");
            Assert.assertTrue(jsonString.contains("{\"albums\":{},\"users\":{},\"sessions\":{},\"logs\":[{\"operation\":\"LOGS\",\"timestamp\":\""));
            Assert.assertTrue(jsonString.contains("\",\"request\":{\"sessionId\":0},\"response\":{\"success\":\"Logs correctly obtained\",\"sessionId\":0}}]}"));
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
            //Assert.assertEquals(4, operations.serviceGetLogs().getLogs().size());

            String jsonString = FileUtils.readFileToString(new File(Operations.STATE_BACKUP_PATH), "UTF-8");
            jsonString = jsonString.replace("\n", "").replace("\r", "");
            Assert.assertTrue(jsonString.contains("{\"albums\":{},\"users\":{\"username\":{\"username\":\"username\",\"password\":\"password\",\"publicKey\":[0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0],\"albums\":[],\"sessionId\":0}},\"sessions\":{},\"logs\":[{\"operation\":\"SIGNUP\",\"timestamp\":"));
            Assert.assertTrue(jsonString.contains(",\"request\":{\"username\":\"username\",\"password\":\"password\",\"publicKey\":[0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0],\"sessionId\":0},\"response\":{\"success\":\"User created successfully\",\"sessionId\":0}},{\"operation\":\"LOGIN\",\"timestamp\":\""));
            Assert.assertTrue(jsonString.contains(",\"request\":{\"username\":\"username\",\"password\":\"password\",\"sessionId\":0},\"response\":{\"success\":\"Login successful\",\"sessionId\":"));
            Assert.assertTrue(jsonString.contains("}},{\"operation\":\"LOGOUT\",\"timestamp\":\""));
            Assert.assertTrue(jsonString.contains("\",\"request\":{\"sessionId\":"));
            Assert.assertTrue(jsonString.contains("},\"response\":{\"success\":\"Session successfully deleted\",\"sessionId\":0}},{\"operation\":\"LOGS\",\"timestamp\":\""));
            Assert.assertTrue(jsonString.contains("\",\"request\":{\"sessionId\":0},\"response\":{\"sessionId\":0}}]}"));
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
