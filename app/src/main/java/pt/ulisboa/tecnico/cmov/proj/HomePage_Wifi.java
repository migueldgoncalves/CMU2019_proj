package pt.ulisboa.tecnico.cmov.proj;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import pt.ulisboa.tecnico.cmov.proj.Data.Peer2PhotoApp;

public class HomePage_Wifi extends HomePage {

    private TermiteComponent termite = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Bundle bundle = savedInstanceState != null ? savedInstanceState : new Bundle();
        bundle.putBoolean("isWifi", true);
        termite = new TermiteComponent(this, getApplication(), getMainLooper());

        super.onCreate(bundle);
    }

    @Override
    protected void enterAlbum(int position) {
        Intent intent = new Intent(this, usingWifiDirect ? AlbumView_Wifi.class :  AlbumView.class);
        Bundle b = new Bundle();
        String albumId = albums.get(position).getAlbumId();
        String albumName = albums.get(position).getAlbumName();
        b.putString("AlbumId", albumId);
        b.putString("AlbumName", albumName);
        b.putStringArrayList("AlbumUsers", termite.albumName_User_Map.get(albumName));
        intent.putExtras(b);
        startActivityForResult(intent, ALBUM_VIEW_REQUEST);
    }

    @Override
    public void addNewAlbum(String albumId, String albumName) {
        super.addNewAlbum(albumId, albumName);
        termite.albumId_albumName_Map.put(albumId, albumName);
        ArrayList<String> Users = new ArrayList<String>();
        Users.add(((Peer2PhotoApp)getApplication()).getUsername());
        termite.albumName_User_Map.put(albumName, Users);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ALBUM_VIEW_REQUEST) {
            if (resultCode == RESULT_OK) {
                String usernames = data.getExtras().getString("Users");
                if (usernames.equals("")) return;

                String albumName = data.getExtras().getString("AlbumName");
                String[] users = usernames.split(",");
                for (String user : users) {
                    termite.albumName_User_Map.get(albumName).add(user);
                }
            }
        }
    }

    @Override
    public void parseAlbumNames(String[] albumIds, JSONObject httpResponse) {
        // 3\\ cases - User was not added to third party albums;
        // User was added to album with same name as another album user already has;
        // User was added to album with a name different of all user's albums
        try{
            HashMap<String, String> albumId_albumName_Map = new HashMap<String, String>();
            HashMap<String, ArrayList<String>> albumName_User_Map = new HashMap<>();
            for (String albumId : albumIds) {
                String albumName = httpResponse.getString(albumId);
                albumId_albumName_Map.put(albumId, albumName);
                String users = httpResponse.getString("Users_" + albumId);
                String[] usersArray = users.split(",");
                albumName_User_Map.put(albumName, new ArrayList<>(Arrays.asList(usersArray)));
                if (((Peer2PhotoApp) getApplication()).getAlbumId(albumName) == null) {
                    if (!(new File(getApplicationContext().getFilesDir().getPath() + "/" + albumName).exists())) {
                        addNewAlbum(albumId, albumName);
                        Log.d("debug", "User has been added to album of other user and its name does not exist in user's albums");
                    } else {
                        File fileToDelete = new File(getApplicationContext().getFilesDir().getPath() + "/" + albumName);
                        if (fileToDelete.delete()) {
                            addNewAlbum(albumId, albumName);
                            Log.d("debug", "User has been added to album of other user and its name does not exist in user's albums");
                        }
                    }
                } else {
                    if (!((Peer2PhotoApp) getApplication()).getAlbumId(albumName).equals(albumId)) {
                        String newName = albumName + "_" + albumId;
                        addNewAlbum(albumId, newName);
                        Log.d("debug", "User has been added to album of other user with name equal to one of user's albums");
                    } else {
                        if(!new File(getApplicationContext().getFilesDir().getPath() + "/" + albumName).exists()){
                            addNewAlbum(albumId, albumName);
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