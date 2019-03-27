package pt.ulisboa.tecnico.cmov.proj.Data;

import android.media.Image;

import java.util.ArrayList;
import java.util.Collection;

public class Album {

    private String albumName;
    private int thumbnail = -1;
    private ArrayList<User> users = new ArrayList<User>();

    public Album(String albumName, int thumbnail) {
        this.albumName = albumName;
        this.thumbnail = thumbnail;
    }

    public Album(String albumName, ArrayList<User> users) {
        this.albumName = albumName;
        this.users = users;
    }

    public String getAlbumName() { return this.albumName; }

    public int getAlbumThumbnail() { return this.thumbnail; }

    public Collection<User> getAllUsers() { return users; }

    public User getUserWithID() {
        //TODO:
        return null;
    }
}
