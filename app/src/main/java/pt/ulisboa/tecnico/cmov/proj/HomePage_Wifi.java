package pt.ulisboa.tecnico.cmov.proj;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.dropbox.core.android.Auth;
import com.dropbox.core.v2.files.FileMetadata;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import pt.ulisboa.tecnico.cmov.proj.Adapters.AlbumAdapter;
import pt.ulisboa.tecnico.cmov.proj.Data.Album;
import pt.ulisboa.tecnico.cmov.proj.Data.Peer2PhotoApp;
import pt.ulisboa.tecnico.cmov.proj.Dropbox.DropboxActivity;
import pt.ulisboa.tecnico.cmov.proj.Dropbox.DropboxClientFactory;
import pt.ulisboa.tecnico.cmov.proj.Dropbox.UploadFileTask;
import pt.ulisboa.tecnico.cmov.proj.HTMLHandlers.HttpRequestDeleteSession;
import pt.ulisboa.tecnico.cmov.proj.HTMLHandlers.HttpRequestGetUserAlbums;
import pt.ulisboa.tecnico.cmov.proj.HTMLHandlers.HttpRequestPostCreateAlbum;

public class HomePage_Wifi extends HomePage {

    private TermiteComponent termite = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        savedInstanceState.putBoolean("isWifi", true);
        super.onCreate(savedInstanceState);

        termite = new TermiteComponent(this, getApplication(), getMainLooper());
    }

    @Override
    public void parseAlbumNames(String[] albumIds, JSONObject httpResponse) {
        // 3\\ cases - User was not added to third party albums;
        // User was added to album with same name as another album user already has;
        // User was added to album with a name different of all user's albums
        try{
            HashMap<String, String> albumId_albumName_Map = new HashMap<String, String>();
            HashMap<String, String[]> albumName_User_Map = new HashMap<String, String[]>();
            for (String albumId : albumIds) {
                String albumName = httpResponse.getString(albumId);
                albumId_albumName_Map.put(albumId, albumName);
                String users = httpResponse.getString("Users_" + albumId);
                albumName_User_Map.put(albumName, users.split(","));
                if (((Peer2PhotoApp) getApplication()).getAlbumId(albumName) == null) {
                    if (!(new File(getApplicationContext().getFilesDir().getPath() + "/" + albumName).exists())) {
                        addNewAlbum(albumName);
                        Log.d("debug", "User has been added to album of other user and its name does not exist in user's albums");
                    } else {
                        File fileToDelete = new File(getApplicationContext().getFilesDir().getPath() + "/" + albumName);
                        if (fileToDelete.delete()) {
                            addNewAlbum(albumName);
                            Log.d("debug", "User has been added to album of other user and its name does not exist in user's albums");
                        }
                    }
                } else {
                    if (!((Peer2PhotoApp) getApplication()).getAlbumId(albumName).equals(albumId)) {
                        String newName = albumName + "_" + albumId;
                        addNewAlbum(newName);
                        Log.d("debug", "User has been added to album of other user with name equal to one of user's albums");
                    } else {
                        if(!new File(getApplicationContext().getFilesDir().getPath() + "/" + albumName).exists()){
                            addNewAlbum(albumName);
                        }
                    }
                }
            }
            termite.albumName_User_Map = albumName_User_Map;
            termite.albumId_albumName_Map = albumId_albumName_Map;

            Toast.makeText(HomePage_Wifi.this, "Updated albums",
                    Toast.LENGTH_LONG).show();
        }catch (Exception e){
            e.printStackTrace();
        }

    }

}