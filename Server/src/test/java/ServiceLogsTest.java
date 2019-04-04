import com.google.gson.Gson;
import com.squareup.okhttp.*;
import io.javalin.Javalin;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;

public class ServiceLogsTest {

    private static final int OK = 200;

    private static final int PORT = JavalinApp.PORT;

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private Javalin app = null;
    private OkHttpClient client = null;

    private Operations operations = null;
    private File original = null;
    private File temporary = null;

    private static final String URL_BASE = "http://localhost:" + PORT;
    private static final String URL_LOGS = URL_BASE + "/logs";
    private static final String URL_SIGNUP = URL_BASE + "/signup";
    private static final String URL_LOGIN = URL_BASE + "/login";
    private static final String URL_LOGOUT = URL_BASE + "/logout";

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
    public void serviceGetNoLogsTest() {
        try {
            Request request = new Request.Builder().url(URL_LOGS).build();
            Response response = client.newCall(request).execute();
            AppResponse appResponse = new Gson().fromJson(response.body().string(), AppResponse.class);
            /*Assert.assertEquals(1, appResponse.getLogs().size());
            Assert.assertEquals(Operations.LOGIN_OPERATION, appResponse.getLogs().get(0).getOperation());*/
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void serviceGetLogsTest() {
        try {
            AppRequest appRequest = new AppRequest();
            appRequest.setUsername("username");
            appRequest.setPassword("password");
            appRequest.setPublicKey(new byte[256]);
            RequestBody body = RequestBody.create(JSON, new Gson().toJson(appRequest));
            Request request = new Request.Builder().url(URL_SIGNUP).post(body).build();
            Response response = client.newCall(request).execute();

            body = RequestBody.create(JSON, new Gson().toJson(appRequest));
            request = new Request.Builder().url(URL_LOGIN).put(body).build();
            response = client.newCall(request).execute();

            request = new Request.Builder().url(URL_LOGS).build();
            response = client.newCall(request).execute();
            ArrayList logs = new Gson().fromJson(response.body().string(), ArrayList.class);
            Assert.assertEquals(3, logs.size());
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
