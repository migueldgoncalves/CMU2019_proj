import com.google.gson.Gson;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static java.nio.file.StandardCopyOption.ATOMIC_MOVE;

public class Operations {

    protected static final int MIN_USERNAME_LENGTH = 3;
    protected static final int MAX_USERNAME_LENGTH = 25;
    protected static final int MIN_PASSWORD_LENGTH = 8;
    protected static final int MAX_PASSWORD_LENGTH = 30;

    protected static final int RSA_KEY_BYTE_LENGTH = 256;

    protected static final int SESSION_DURATION = 5; //Seconds

    protected static final String TEMPORARY_BACKUP_NAME = "backups/ServerState.new";
    protected static final String STATE_BACKUP_NAME = "ServerState.old";
    protected static final String STATE_BACKUP_PATH = "backups/" + STATE_BACKUP_NAME;

    protected static final String SIGNUP_OPERATION = "SIGNUP";
    protected static final String LOGIN_OPERATION = "LOGIN";
    protected static final String LOGOUT_OPERATION = "LOGOUT";
    protected static final String LOGS_OPERATION = "LOGS";
    protected static final String CREATE_ALBUM_OPERATION = "CREATE_ALBUM";

    private static Operations operations;

    private HashMap<Integer, Album> albums = new HashMap<>();
    //Username as KEY and USER value
    private HashMap<String, User> users = new HashMap<>();
    //Session ID as Key, SESSION object as value
    private HashMap<Integer, Session> sessions = new HashMap<>();
    private String logs = "";

    protected AtomicInteger counterAlbum = new AtomicInteger(0);
    protected AtomicInteger counterLog = new AtomicInteger(0);

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

    // Business logic main methods

    public AppResponse signUp(String username, String password, byte[] publicKey) {
        AppRequest request = new AppRequest();
        request.setUsername(username);
        request.setPassword(password);
        request.setPublicKey(publicKey);
        AppResponse response = new AppResponse();

        String usernameEvaluation = isUsernameValid(username);
        if(!usernameEvaluation.equals("Username valid")) {
            response.setError(usernameEvaluation);
            addLog(SIGNUP_OPERATION, request, response);
            return response;
        }
        String passwordEvaluation = isPasswordValid(password);
        if(!passwordEvaluation.equals("Password valid")) {
            response.setError(passwordEvaluation);
            addLog(SIGNUP_OPERATION, request, response);
            return response;
        }
        String publicKeyEvaluation = isPublicKeyValid(publicKey);
        if(!publicKeyEvaluation.equals("Public key valid")) {
            response.setError(publicKeyEvaluation);
            addLog(SIGNUP_OPERATION, request, response);
            return response;
        }
        addUser(new User(username, password, publicKey));
        response.setSuccess("User created successfully");
        addLog(SIGNUP_OPERATION, request, response);
        return response;
    }

    public AppResponse logIn(String username, String password){
        AppRequest request = new AppRequest();
        request.setUsername(username);
        request.setPassword(password);
        AppResponse response = new AppResponse();

        if(!isUserCreated(username)){
            response.setError("The Inserted Username is Incorrect!");
            addLog(LOGIN_OPERATION, request, response);
            return response;
        }
        if(!getUserByUsername(username).isPasswordCorrect(password)){
            response.setError("Invalid Password! Please Try Again");
            addLog(LOGIN_OPERATION, request, response);
            return response;
        }
        if(getUserByUsername(username).getSessionId() > 0) {
            response.setSuccess("Login successful");
            response.setSessionId(getUserByUsername(username).getSessionId());
            addLog(LOGIN_OPERATION, request, response);
            return response;
        }
        Session session = new Session(username, SESSION_DURATION);
        String sessionAddResult = addSession(session);
        if(!sessionAddResult.equals("Session successfully added")) {
            response.setError("Could not create session - Please try to log in again");
            addLog(LOGIN_OPERATION, request, response);
            return response;
        }
        response.setSuccess("Login successful");
        response.setSessionId(session.getSessionId());
        addLog(LOGIN_OPERATION, request, response);
        return response;
    }

    public AppResponse logOut(int sessionId) {
        AppRequest request = new AppRequest();
        request.setSessionId(sessionId);
        AppResponse response = new AppResponse();
        String result = deleteSession(sessionId);
        if(!result.equals("Session successfully deleted")) {
            response.setError(result);
            addLog(LOGOUT_OPERATION, request, response);
            return response;
        }
        response.setSuccess(result);
        addLog(LOGOUT_OPERATION, request, response);
        return response;
    }

    public AppResponse serviceGetLogs() {
        AppRequest request = new AppRequest();
        AppResponse response = new AppResponse();
        response.setSuccess("Logs correctly obtained");
        addLog(LOGS_OPERATION, request, response);
        response.setLogs(getLogs());
        return response;
    }

    public AppResponse createAlbum(int sessionId, String username, String albumName) {
        AppRequest request = new AppRequest();
        AppResponse response = new AppResponse();
        String[] result = addAlbum(new Album(albumName, counterAlbum.incrementAndGet()), username);
        if(!result[0].equals("Album successfully added")) {
            response.setError(result[0]);
            addLog(CREATE_ALBUM_OPERATION, request, response);
            return response;
        }
        response.setSuccess(result[0]);
        response.setAlbumId(Integer.valueOf(result[1]));
        addLog(CREATE_ALBUM_OPERATION, request, response);
        return response;
    }

    // Business logic auxiliary methods

    protected String isUsernameValid(String username) {
        if (username == null)
            return "Username cannot be null";
        if (username.length() == 0 || username.trim().length() == 0)
            return "Username cannot be empty";
        for (int i=0; i<username.length(); i++)
            if(!Character.isDigit(username.charAt(i)) && !Character.isLetter(username.charAt(i)))
                return "Username must only contain digits and letters";
        if (username.length() < MIN_USERNAME_LENGTH)
            return "Username must have at least " + MIN_USERNAME_LENGTH + " characters";
        if (username.length() > MAX_USERNAME_LENGTH)
            return "Username must have at most " + MAX_USERNAME_LENGTH + " characters";
        username = username.toLowerCase();
        if (isUserCreated(username))
            return "Username already exists";
        return "Username valid";
    }

    protected String isPasswordValid(String password) {
        if (password == null)
            return "Password cannot be null";
        if (password.length() == 0 || password.trim().length() == 0)
            return "Password cannot be empty";
        if (password.length() < MIN_PASSWORD_LENGTH)
            return "Password must have at least " + MIN_PASSWORD_LENGTH + " characters";
        if (password.length() > MAX_PASSWORD_LENGTH)
            return "Password must have at most " + MAX_PASSWORD_LENGTH + " characters";
        return "Password valid";
    }

    protected String isPublicKeyValid(byte[] publicKey) {
        if (publicKey == null)
            return "Public key cannot be null";
        if (publicKey.length != RSA_KEY_BYTE_LENGTH)
            return "Public key must have " + RSA_KEY_BYTE_LENGTH * 8 + " bits";
        return "Public key valid";
    }

    // Server state setters

    protected String[] addAlbum(Album album, String username) {
        String[] response = new String[2];
        if (album != null) {
            if(isUserCreated(username)) {
                album.addUserToAlbum(username, null);
                getUserByUsername(username).addAlbumUserIsIn(album.getId());
                albums.put(album.getId(), album);
                Operations.writeServerState();
                response[0] = "Album successfully added";
                response[1] = String.valueOf(album.getId());
                return response;
            }
            response[0] = "Username does not exist or is invalid";
            return response;
        }
        response[0] = "Album cannot be null";
        return response;
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

    protected ArrayList<String> getSystemUsers(){
        return new ArrayList<>(users.keySet());
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

    protected String addUser(User user) {
        if (user != null) {
            if (!isUserCreated(user.getUsername())) {
                users.put(user.getUsername(), user);
                Operations.writeServerState();
                return "User successfully added";
            }
            return "User already exists";
        }
        return "User cannot be null";
    }

    protected String addSession(Session session) {
        if (session != null) {
            if (!isSessionCreated(session.getSessionId())) {
                if(isUserCreated(session.getUsername())) {
                    if (getUserByUsername(session.getUsername()).getSessionId() == 0) {
                        sessions.put(session.getSessionId(), session);
                        getUserByUsername(session.getUsername()).setSessionId(session.getSessionId());
                        Operations.writeServerState();
                        return "Session successfully added";
                    }
                    return "User already has a session";
                }
                return "User does not exist";
            }
            return "Session already exists";
        }
        return "Session cannot be null";
    }

    protected String addLog(String operation, AppRequest request, AppResponse response) {
        if(operation!=null) {
            if(request!=null) {
                if(response!=null) {
                    String log = "";
                    log+="Operation ID: " + counterLog.incrementAndGet() + "\n";
                    log+="Operation name: " + operation + "\n";
                    log+="Operation time: " + new Date().toString() + "\n";
                    log+="Operation input: " + new Gson().toJson(request) + "\n";
                    log+="Operation output: " + new Gson().toJson(response) + "\n";
                    log+="---------------------------------------------------------------------------------------------------------------\n";
                    logs+=log;
                    Operations.writeServerState();
                    System.out.println(logs);
                    return "Operation successfully logged";
                }
                return "Operation response cannot be null";
            }
            return "Operation request cannot be null";
        }
        return "Operation name cannot be null";
    }

    protected String deleteSession(int sessionId) {
        if(isSessionCreated(sessionId)) {
            getUserByUsername(getSessionById(sessionId).getUsername()).setSessionId(0);
            sessions.remove(sessionId);
            Operations.writeServerState();
            return "Session successfully deleted";
        }
        return "Session does not exist";
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

    protected String getLogs() {
        return logs;
    }

    protected int getLogsLength() {
        return counterLog.get();
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
