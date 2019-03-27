import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class UserTest {

    User user;

    @Before
    public void setUp() {
        user = new User("username", "password", new byte[10]);
    }

    @Test
    public void userConstructorTest() {
        Assert.assertEquals("username", user.getUsername());
        Assert.assertEquals("password", user.getPassword());
        Assert.assertEquals(10, user.getPublicKey().length);
        Assert.assertEquals(0, user.getUserAlbumNumber());
    }

    @Test
    public void userInAlbumTest() {
        user.addAlbumUserIsIn(1);
        Assert.assertEquals(1, user.getUserAlbumNumber());
        Assert.assertTrue(user.isUserInAlbum(1));
    }

    @Test
    public void userNotInAlbumTest() {
        Assert.assertEquals(0, user.getUserAlbumNumber());
        Assert.assertFalse(user.isUserInAlbum(1));
    }

    @After
    public void tearDown() {
        user = null;
    }
}
