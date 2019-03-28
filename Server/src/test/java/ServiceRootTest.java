import com.google.gson.Gson;
import com.squareup.okhttp.*;
import io.javalin.Javalin;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.net.URL;

public class ServiceRootTest {

    public static final int OK = 200;
    public static final int CREATED = 201;
    public static final int NO_CONTENT = 204;
    public static final int BAD_REQUEST = 400;
    public static final int NOT_FOUND = 404;

    public static final int PORT = 7000;

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private Javalin app = null;
    private OkHttpClient client = null;

    private Operations operations = null;
    private File original = null;
    private File temporary = null;

    public static final String URL_BASE = "http://localhost:" + PORT;
    public static final String URL_SIGNUP = URL_BASE + "/signup";

    @Before
    public void setUp() {
        JavalinApp javalinApp = new JavalinApp();
        this.app = javalinApp.init();
        this.client = new OkHttpClient();

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
    public void invalidPathTest() {
        try {
            String url = URL_BASE +"/pagenotexisting";
            Request request = new Request.Builder().url(url).build();
            Response response = client.newCall(request).execute();
            Assert.assertEquals(NOT_FOUND, response.code());
            Assert.assertEquals("This link does not exist", response.body().string());
        }
        catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void rootPathTest() {
        try {
            Request request = new Request.Builder().url(URL_BASE).build();
            Response response = client.newCall(request).execute();
            Assert.assertEquals(OK, response.code());
            Assert.assertEquals("{\"status\":\"OK\"}", response.body().string());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void signUpPathTest() {
        try {
            AppRequest appRequest = new AppRequest();
            appRequest.setUsername("username");
            appRequest.setPassword("password");
            appRequest.setPublicKey(new byte[256]);
            RequestBody body = RequestBody.create(JSON, new Gson().toJson(appRequest));
            Request request = new Request.Builder().url(URL_SIGNUP).post(body).build();
            Response response = client.newCall(request).execute();

            Assert.assertEquals(CREATED, response.code());
            Assert.assertEquals(1, operations.getUsersLength());
            Assert.assertNotNull(operations.getUserByUsername("username"));
            Assert.assertEquals("password", operations.getUserByUsername("username").getPassword());
            Assert.assertEquals(256, operations.getUserByUsername("username").getPublicKey().length);
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
