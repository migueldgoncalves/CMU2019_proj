import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

public class LogTest {

    Log log = null;

    @Before
    public void setUp() {
        AppRequest request = new AppRequest();
        request.setUsername("username");
        request.setPassword("password");

        AppResponse response = new AppResponse();
        response.setSuccess("Success");

        log = new Log("operation", request, response);
    }

    @Test
    public void logConstructorTest() {
        try {
            Assert.assertEquals("operation", log.getOperation());
            Assert.assertTrue(new Date().getTime() - log.getTimestamp().getTime() < 1000);
            Assert.assertEquals("username", log.getRequest().getUsername());
            Assert.assertEquals("password", log.getRequest().getPassword());
            Assert.assertEquals("Success", log.getResponse().getSuccess());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @After
    public void tearDown() {
        log = null;
    }
}
