import com.google.gson.Gson;
import com.squareup.okhttp.*;
import io.javalin.Javalin;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

public class ServiceSignUpTest {

    private static final int CREATED = 201;

    private static final int PORT = 7000;

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private Javalin app = null;
    private OkHttpClient client = null;

    private Operations operations = null;
    private File original = null;
    private File temporary = null;

    private static final String URL_BASE = "http://localhost:" + PORT;
    private static final String URL_SIGNUP = URL_BASE + "/signup";

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
    public void validSignUpRequestTest() {
        try {
            AppRequest appRequest = new AppRequest();
            appRequest.setUsername("username");
            appRequest.setPassword("password");
            appRequest.setPublicKey(new byte[256]);
            RequestBody body = RequestBody.create(JSON, new Gson().toJson(appRequest));
            Request request = new Request.Builder().url(URL_SIGNUP).post(body).build();
            Response response = client.newCall(request).execute();

            Assert.assertEquals(CREATED, response.code());
            Assert.assertEquals("User created successfully", new Gson().fromJson(response.body().string(), String.class));
            Assert.assertEquals(1, operations.getUsersLength());
            Assert.assertEquals("username", operations.getUserByUsername("username").getUsername());
            Assert.assertEquals("password", operations.getUserByUsername("username").getPassword());
            Assert.assertEquals(256, operations.getUserByUsername("username").getPublicKey().length);
            for(int i=0; i<256; i++)
                Assert.assertEquals(0, operations.getUserByUsername("username").getPublicKey()[i]);
            Assert.assertEquals(0, operations.getUserByUsername("username").getUserAlbumNumber());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void invalidUsernameSignUpRequestTest() {
        try {
            AppRequest appRequest = new AppRequest();
            appRequest.setUsername("");
            appRequest.setPassword("password");
            appRequest.setPublicKey(new byte[256]);
            RequestBody body = RequestBody.create(JSON, new Gson().toJson(appRequest));
            Request request = new Request.Builder().url(URL_SIGNUP).post(body).build();
            Response response = client.newCall(request).execute();

            Assert.assertEquals(CREATED, response.code());
            Assert.assertEquals("Username cannot be empty", new Gson().fromJson(response.body().string(), String.class));
            Assert.assertEquals(0, operations.getUsersLength());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void invalidPasswordSignUpRequestTest() {
        try {
            AppRequest appRequest = new AppRequest();
            appRequest.setUsername("username");
            appRequest.setPassword("x");
            appRequest.setPublicKey(new byte[256]);
            RequestBody body = RequestBody.create(JSON, new Gson().toJson(appRequest));
            Request request = new Request.Builder().url(URL_SIGNUP).post(body).build();
            Response response = client.newCall(request).execute();

            Assert.assertEquals(CREATED, response.code());
            Assert.assertEquals("Password must have at least " + Operations.MIN_PASSWORD_LENGTH + " characters", new Gson().fromJson(response.body().string(), String.class));
            Assert.assertEquals(0, operations.getUsersLength());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void invalidPublicKeySignUpRequestTest() {
        try {
            AppRequest appRequest = new AppRequest();
            appRequest.setUsername("username");
            appRequest.setPassword("password");
            appRequest.setPublicKey(new byte[1]);
            RequestBody body = RequestBody.create(JSON, new Gson().toJson(appRequest));
            Request request = new Request.Builder().url(URL_SIGNUP).post(body).build();
            Response response = client.newCall(request).execute();

            Assert.assertEquals(CREATED, response.code());
            Assert.assertEquals("Public key must have " + Operations.RSA_KEY_BYTE_LENGTH * 8 + " bits", new Gson().fromJson(response.body().string(), String.class));
            Assert.assertEquals(0, operations.getUsersLength());
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