import com.google.gson.Gson;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

import static java.nio.file.StandardCopyOption.ATOMIC_MOVE;

public class Operations {

    protected static final String TEMPORARY_BACKUP_NAME = "backups/ServerState.new";
    protected static final String STATE_BACKUP_NAME = "ServerState.old";
    protected static final String STATE_BACKUP_PATH = "backups/" + STATE_BACKUP_NAME;

    private static Operations operations;

    private HashMap<Integer, Album> albums = new HashMap<>();
    private HashMap<String, User> users = new HashMap<>();
    private HashMap<Integer, Session> sessions = new HashMap<>();

    private Operations() {

    }

    public static Operations getServer() {
        if (Operations.operations != null)
            return Operations.operations;
        Operations.operations = new Operations();
        Operations.readServerState();
        return Operations.operations;
    }

    private static void readServerState() {
        if (isBackupFileCreated()) {
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
            System.out.println("No backup file found - Creating a new one");
            Operations.writeServerState();
        }
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

    private static boolean isBackupFileCreated() {
        File f = new File(STATE_BACKUP_PATH);
        return (f.exists() && !f.isDirectory());
    }

    public static void cleanServer() {
        Operations.operations = null;
    }

    public HashMap<Integer, Album> getAlbums() {
        return albums;
    }

    public Album getAlbumById(int albumId) {
        if (albums.containsKey(albumId))
            return albums.get(albumId);
        return null;
    }

    public HashMap<String, User> getUsers() {
        return users;
    }

    public User getUserByUsername(String username) {
        if (users.containsKey(username))
            return users.get(username);
        return null;
    }

    public HashMap<Integer, Session> getSessions() {
        return sessions;
    }

    public Session getSessionById(int sessionId) {
        if (sessions.containsKey(sessionId))
            return sessions.get(sessionId);
        return null;
    }
}
