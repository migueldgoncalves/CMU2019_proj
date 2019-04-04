import com.google.gson.Gson;
import com.squareup.okhttp.*;
import io.javalin.Javalin;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

public class ServiceLogInTest {

    private static final int CREATED = 201;

    private static final int PORT = JavalinApp.PORT;

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private Javalin app = null;
    private OkHttpClient client = null;

    private Operations operations = null;
    private File original = null;
    private File temporary = null;

    private static final String URL_BASE = "http://localhost:" + PORT;
    private static final String URL_LOGIN = URL_BASE + "/login";

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
        JavalinApp javalinApp = new JavalinApp();
        this.app = javalinApp.init();
        this.client = new OkHttpClient();
        operations = Operations.getServer();
    }

    @Test
    public void validLogInRequestWithPreviousSession() {
        try {
            operations.addUser(new User("username", "password", new byte[256]));
            Session session = new Session("username", 5);
            operations.addSession(session);

            AppRequest appRequest = new AppRequest();
            appRequest.setUsername("username");
            appRequest.setPassword("password");
            RequestBody body = RequestBody.create(JSON, new Gson().toJson(appRequest));
            Request request = new Request.Builder().url(URL_LOGIN).put(body).build();
            Response response = client.newCall(request).execute();

            Assert.assertEquals(CREATED, response.code());
            AppResponse appResponse = new Gson().fromJson(response.body().string(), AppResponse.class);
            Assert.assertEquals("Login successful", appResponse.getSuccess());
            Assert.assertNull(appResponse.getError());
            Assert.assertEquals(session.getSessionId(), appResponse.getSessionId());
            Assert.assertEquals(1, operations.getLogsLength());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void validLogInRequestWithoutPreviousSession() {
        try {
            operations.addUser(new User("username", "password", new byte[256]));
            AppRequest appRequest = new AppRequest();
            appRequest.setUsername("username");
            appRequest.setPassword("password");

            RequestBody body = RequestBody.create(JSON, new Gson().toJson(appRequest));
            Request request = new Request.Builder().url(URL_LOGIN).put(body).build();
            Response response = client.newCall(request).execute();
            AppResponse appResponse = new Gson().fromJson(response.body().string(), AppResponse.class);

            Assert.assertEquals(CREATED, response.code());
            Assert.assertEquals("Login successful", appResponse.getSuccess());
            Assert.assertNull(appResponse.getError());
            Assert.assertTrue(appResponse.getSessionId() > 0);
            Assert.assertEquals(operations.getUserByUsername("username").getSessionId(), appResponse.getSessionId());
            Assert.assertEquals(1, operations.getLogsLength());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void nullUsernameLogInRequestTest() {
        try {
            operations.addUser(new User("username", "password", new byte[256]));
            AppRequest appRequest = new AppRequest();
            appRequest.setUsername(null);
            appRequest.setPassword("password");

            RequestBody body = RequestBody.create(JSON, new Gson().toJson(appRequest));
            Request request = new Request.Builder().url(URL_LOGIN).put(body).build();
            Response response = client.newCall(request).execute();
            AppResponse appResponse = new Gson().fromJson(response.body().string(), AppResponse.class);

            Assert.assertEquals(CREATED, response.code());
            Assert.assertEquals("The Inserted Username is Incorrect!", appResponse.getError());
            Assert.assertEquals(0, operations.getUserByUsername("username").getSessionId());
            Assert.assertEquals(1, operations.getLogsLength());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void invalidUsernameLogInRequestTest() {
        try {
            operations.addUser(new User("username", "password", new byte[256]));
            AppRequest appRequest = new AppRequest();
            appRequest.setUsername("anotherUsername");
            appRequest.setPassword("password");

            RequestBody body = RequestBody.create(JSON, new Gson().toJson(appRequest));
            Request request = new Request.Builder().url(URL_LOGIN).put(body).build();
            Response response = client.newCall(request).execute();
            AppResponse appResponse = new Gson().fromJson(response.body().string(), AppResponse.class);

            Assert.assertEquals(CREATED, response.code());
            Assert.assertEquals("The Inserted Username is Incorrect!", appResponse.getError());
            Assert.assertEquals(0, operations.getUserByUsername("username").getSessionId());
            Assert.assertEquals(1, operations.getLogsLength());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void nullPasswordLogInRequestTest() {
        try {
            operations.addUser(new User("username", "password", new byte[256]));
            AppRequest appRequest = new AppRequest();
            appRequest.setUsername("username");
            appRequest.setPassword(null);

            RequestBody body = RequestBody.create(JSON, new Gson().toJson(appRequest));
            Request request = new Request.Builder().url(URL_LOGIN).put(body).build();
            Response response = client.newCall(request).execute();
            AppResponse appResponse = new Gson().fromJson(response.body().string(), AppResponse.class);

            Assert.assertEquals(CREATED, response.code());
            Assert.assertEquals("Invalid Password! Please Try Again", appResponse.getError());
            Assert.assertEquals(0, operations.getUserByUsername("username").getSessionId());
            Assert.assertEquals(1, operations.getLogsLength());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void invalidPasswordLogInRequestTest() {
        try {
            operations.addUser(new User("username", "password", new byte[256]));
            AppRequest appRequest = new AppRequest();
            appRequest.setUsername("username");
            appRequest.setPassword("wrongPassword");

            RequestBody body = RequestBody.create(JSON, new Gson().toJson(appRequest));
            Request request = new Request.Builder().url(URL_LOGIN).put(body).build();
            Response response = client.newCall(request).execute();
            AppResponse appResponse = new Gson().fromJson(response.body().string(), AppResponse.class);

            Assert.assertEquals(CREATED, response.code());
            Assert.assertEquals("Invalid Password! Please Try Again", appResponse.getError());
            Assert.assertEquals(0, operations.getUserByUsername("username").getSessionId());
            Assert.assertEquals(1, operations.getLogsLength());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @After
    public void tearDown() {
        this.app.stop();
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
