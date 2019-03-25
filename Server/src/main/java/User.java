import java.util.ArrayList;

public class User {

    private String username; //Will serve as ID
    private String password;
    private byte[] publicKey;
    private ArrayList<Integer> albums = new ArrayList<>();

    public User(String username, String password, byte[] publicKey) {
        this.username = username;
        this.password = password;
        this.publicKey = publicKey;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public byte[] getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(byte[] publicKey) {
        this.publicKey = publicKey;
    }

    public ArrayList<Integer> getAlbums() {
        return albums;
    }

    public void setAlbums(ArrayList<Integer> albums) {
        this.albums = albums;
    }

    public boolean isUserInAlbum(int albumId) {
        return albums.contains(albumId);
    }

    public int getUserAlbumNumber() {
        return albums.size();
    }

    public void addAlbumUserIsIn(int albumId) {
        albums.add(albumId);
    }
}
