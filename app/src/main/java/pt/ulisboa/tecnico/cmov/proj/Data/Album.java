package pt.ulisboa.tecnico.cmov.proj.Data;

import android.media.Image;

import java.util.ArrayList;
import java.util.Collection;

public class Album {

    private String albumName;
    private Image thumbnail = null;
    private ArrayList<User> users = new ArrayList<User>();

    public Album(String albumName) {
        this.albumName = albumName;
    }

    public Album(String albumName, ArrayList<User> users) {
        this.albumName = albumName;
        this.users = users;
    }

    public String getAlbumName() { return this.albumName; }

    public Collection<User> getAllUsers() { return users; }

    public User getUserWithID() {
        //TODO:
        return null;
    }
}
