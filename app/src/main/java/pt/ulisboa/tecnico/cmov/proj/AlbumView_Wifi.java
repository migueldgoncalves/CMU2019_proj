package pt.ulisboa.tecnico.cmov.proj;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class AlbumView_Wifi extends AlbumView {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Bundle bundle = savedInstanceState != null ? savedInstanceState : new Bundle();
        bundle.putBoolean("isWifi", true);
        super.onCreate(bundle);
        getOtherUsersPhotos();
    }

    @Override
    protected void addUserToAlbum(String username) {
        super.addUserToAlbum(username);
        addedUsers.add(username);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            String userResult = "";
            for (String user : addedUsers) userResult += (user + ",");
            Intent intent = new Intent();
            intent.putExtra("Users", userResult);
            intent.putExtra("AlbumName", albumName);
            setResult(Activity.RESULT_OK, intent);
            handleWifiBackPressed();
        }
    }

    protected void getOtherUsersPhotos() {
        File albumDirectory = new File(getApplicationContext().getFilesDir().getPath() + "/" + albumName);
        if (!albumDirectory.isFile()) return;

        File[] files = albumDirectory.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].getName().length() >= 6 && files[i].getName().substring(0, 6).equals("SLICE_")) {
                try {
                    InputStream inputStream = new FileInputStream(files[i]);
                    InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    ArrayList<String> userPhotos = new ArrayList<>();
                    String fileLine = "";

                    while ((fileLine = bufferedReader.readLine()) != null) {
                        userPhotos.add(fileLine);
                    }

                    inputStream.close();
                    //String username = files[i].getName().substring(6, files[i].getName().length()-3);
                    //username_photos_Map.put(username, userPhotos);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        //TODO: Como mostrar as fotos dos outros aos utilizadores antes de as sacar???
    }
}
