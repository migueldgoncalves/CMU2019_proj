package pt.ulisboa.tecnico.cmov.proj.Data;

import android.app.Application;

import com.google.gson.Gson;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;

public class Peer2PhotoApp extends Application {

    public static enum MODE {
        CLOUD, DIRECT
    }

    private String username;
    private String password;
    private String sessionId;
    private String mode;
    private HashMap<String, String> albums = new HashMap<>();

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

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getAlbumId(String albumName){
        if(albums.containsKey(albumName)){
            return albums.get(albumName);
        }else {
            return null;
        }
    }

    public void getAlbums(String filePath) {
        if (isAlbumsFileCreatedAndNotEmpty(filePath)) {
            try {
                Gson gson = new Gson();
                String jsonString = FileUtils.readFileToString(new File(filePath), "UTF-8");
                jsonString = jsonString.replace("\n", "").replace("\r", "");
                albums = gson.fromJson(jsonString, HashMap.class);
                android.util.Log.d("debug", jsonString);
            } catch (Exception e) {
                android.util.Log.d("debug", "Albums file found is unreadable - Creating a new one");
                setAlbums(filePath);
            }
        } else {
            android.util.Log.d("debug", "No written albums file found - Creating a new one");
            setAlbums(filePath);
        }
    }

    private boolean isAlbumsFileCreatedAndNotEmpty(String filePath) {
        File f = new File(filePath);
        if (f.exists() && !f.isDirectory()) {
            try {
                String jsonString = FileUtils.readFileToString(new File(filePath), "UTF-8");
                jsonString = jsonString.replace("\n", "").replace("\r", "");
                jsonString = jsonString.trim();
                if (jsonString.length() > 0)
                    return true;
                android.util.Log.d("debug", "Albums file is empty");
                return false;
            } catch (Exception e) {
                android.util.Log.d("debug", "Failed to assert if albums file is not empty");
                return false;
            }
        }
        android.util.Log.d("debug", "Albums file not found");
        return false;
    }

    public void addAlbum(String albumId, String albumName, String filePath) {
        albums.put(albumName, albumId);
        setAlbums(filePath);
    }

    private void setAlbums(String filePath) {
        try {
            new File(filePath);
            PrintWriter writer = new PrintWriter(filePath);
            writer.println(new Gson().toJson(albums));
            writer.close();
            android.util.Log.d("debug", new Gson().toJson(albums));
            android.util.Log.d("debug", "Albums file set");
        } catch (Exception e) {
            e.printStackTrace();
            android.util.Log.d("debug", "Could not set albums file");
        }
    }

}
