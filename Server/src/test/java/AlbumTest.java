import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class AlbumTest {

    private Album album = null;

    @Before
    public void setUp() {
        album = new Album("album", 1);
    }

    @Test
    public void albumConstructorTest() {
        Assert.assertEquals(0, album.getAlbumUserNumber());
        Assert.assertEquals("album", album.getName());
        Assert.assertEquals(1, album.getId());
    }

    @Test
    public void userInAlbumTest() {
        album.addUserToAlbum("user", "URL");
        Assert.assertTrue(album.isUserInAlbum("user"));
        Assert.assertEquals("URL", album.getSliceURL("user"));
        Assert.assertEquals(1, album.getAlbumUserNumber());
    }

    @Test
    public void userNotInAlbum() {
        Assert.assertFalse(album.isUserInAlbum("anotherUser"));
        Assert.assertEquals("User is not in album", album.getSliceURL("anotherUser"));
        Assert.assertEquals(0, album.getAlbumUserNumber());
    }

    @After
    public void tearDown() {
        album = null;
    }
}
