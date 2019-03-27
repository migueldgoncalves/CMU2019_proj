import com.google.gson.Gson;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import static java.nio.file.StandardCopyOption.ATOMIC_MOVE;

public class Operations {

    protected static final String TEMPORARY_BACKUP_NAME = "backups/ServerState.new";
    protected static final String STATE_BACKUP_NAME = "ServerState.old";
    protected static final String STATE_BACKUP_PATH = "backups/" + STATE_BACKUP_NAME;

    private static Operations operations;

    private HashMap<Integer, Album> albums = new HashMap<>();
    //Username as KEY and USER value
    private HashMap<String, User> users = new HashMap<>();
    //Session ID as Key, SESSION object as value
    private HashMap<Integer, Session> sessions = new HashMap<>();

    private Operations() {

    }

    // Singleton

    public static Operations getServer() {
        if (Operations.operations != null)
            return Operations.operations;
        Operations.operations = new Operations();
        Operations.readServerState();
        return Operations.operations;
    }

    // Server state setters

    protected String addAlbum(Album album) {
        if (!isAlbumCreated(album.getId())) {
            albums.put(album.getId(), album);
            Operations.writeServerState();
            return "Album successfully added";
        }
        return "Album already exists";
    }

    protected String addUserToAlbum(String username, int albumId, String SliceURL){
        Album temp = albums.get(albumId);
        if(!temp.isUserInAlbum(username)){
            temp.addUserToAlbum(username, SliceURL);
            albums.replace(albumId, temp);
            return "User Successfully Added To Album";
        }else{
            return "The user was already in The Album";
        }
    }

    protected boolean createAlbum(String albumName){
        try{
            int newAlbumId = new Random().nextInt();
            while(albums.containsKey(newAlbumId)){
                newAlbumId = new Random().nextInt();
            }
            albums.put(newAlbumId, new Album(albumName, newAlbumId));
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    protected ArrayList<String> getSystemUsers(){
        return new ArrayList<>(users.keySet());
    }

    protected String logIn(String username, String password){
        if(!users.containsKey(username)){
            return "The Inserted Credentials are incorrect!";
        }
        if(users.get(username).getPassword().equals(password)){
            return "Username and Password Valid: Loged In";
        }
        else{
            return "Invalid Password! Please Try Again";
        }
    }

    //Users must update the current member of an album and get their photographs as new ones are added to these albums;
    //TODO: Implement timed updates issued by the client in order to keep updating the albums they are in
    //TODO: In client create this method

    protected ArrayList<Album> listAlbum(String username){
        ArrayList<Album> result = new ArrayList<Album>();
        for(Album i : albums.values()){
            if(i.isUserInAlbum(username)){
                result.add(i);
            }
        }
        //Verifacar no cliente se o tamanho do array é maior que zero, caso seja 0 nao apresentar nenhum album
        return result;
    }

    protected String addUsers(User user) {
        if (!isUserCreated(user.getUsername())) {
            users.put(user.getUsername(), user);
            Operations.writeServerState();
            return "User successfully added";
        }
        return "User already exists";
    }

    protected String addSession(Session session) {
        if (!isSessionCreated(session.getSessionId())) {
            sessions.put(session.getSessionId(), session);
            Operations.writeServerState();
            return "Session successfully added";
        }
        return "Session already exists";
    }

    // Server state getters

    protected HashMap<Integer, Album> getAlbums() {
        return albums;
    }

    protected Album getAlbumById(int albumId) {
        if (isAlbumCreated(albumId))
            return albums.get(albumId);
        return null;
    }

    private boolean isAlbumCreated(int albumId) {
        return albums.containsKey(albumId);
    }

    protected int getAlbumsLength() {
        return albums.size();
    }

    protected HashMap<Integer, Session> getSessions() {
        return sessions;
    }

    protected Session getSessionById(int sessionId) {
        if (isSessionCreated(sessionId))
            return sessions.get(sessionId);
        return null;
    }

    private boolean isSessionCreated(int sessionId) {
        return sessions.containsKey(sessionId);
    }

    protected int getSessionsLength() {
        return sessions.size();
    }

    protected HashMap<String, User> getUsers() {
        return users;
    }

    protected User getUserByUsername(String username) {
        if (isUserCreated(username))
            return users.get(username);
        return null;
    }

    private boolean isUserCreated(String username) {
        return users.containsKey(username);
    }

    protected int getUsersLength() {
        return users.size();
    }

    // Server state backup methods

    private static void readServerState() {
        if (isBackupFileCreatedAndNotEmpty()) {
            try {
                Gson gson = new Gson();
                String jsonString = FileUtils.readFileToString(new File(STATE_BACKUP_PATH), "UTF-8");
                jsonString = jsonString.replace("\n", "").replace("\r", "");
                Operations.operations = gson.fromJson(jsonString, Operations.class);
                System.out.println("Recovered Server State");
            } catch (Exception e) {
                System.out.println("Backup file found is unreadable - Creating a new one");
                Operations.writeServerState();
            }
        } else {
            System.out.println("No written backup file found - Creating a new one");
            Operations.writeServerState();
        }
    }

    private static boolean isBackupFileCreatedAndNotEmpty() {
        File f = new File(STATE_BACKUP_PATH);
        if (f.exists() && !f.isDirectory()) {
            try {
                String jsonString = FileUtils.readFileToString(new File(STATE_BACKUP_PATH), "UTF-8");
                jsonString = jsonString.replace("\n", "").replace("\r", "");
                jsonString = jsonString.trim();
                if (jsonString.length() > 0)
                    return true;
                System.out.println("Backup file is empty");
                return false;
            } catch (Exception e) {
                System.out.println("Failed to assert if backup file is not empty");
                return false;
            }
        }
        System.out.println("Backup file not found");
        return false;
    }

    private static void writeServerState() {
        try {
            new File(Operations.TEMPORARY_BACKUP_NAME);
            PrintWriter writer = new PrintWriter(Operations.TEMPORARY_BACKUP_NAME);
            writer.println(new Gson().toJson(Operations.getServer()));
            writer.close();
            System.out.println(new Gson().toJson(Operations.getServer()));
            Files.move(Paths.get(TEMPORARY_BACKUP_NAME), Paths.get(STATE_BACKUP_PATH), ATOMIC_MOVE);
            System.out.println("Backup file created");
        } catch (Exception e) {
            System.out.println("Could not backup server state");
        }
    }

    // Server cleaner

    protected static void cleanServer() {
        Operations.operations = null;
        File backup = new File(Operations.STATE_BACKUP_PATH);
        // Deletes backup file
        if (backup.exists() && !backup.isDirectory()) {
            try {
                backup.delete();
                System.out.println("Server backup file deleted");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
