package pt.ulisboa.tecnico.cmov.proj;

import android.os.Bundle;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

public class AlbumView_Wifi extends AlbumView {

    private HashMap<String, ArrayList<String>> username_photos_Map = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Bundle bundle = savedInstanceState != null ? savedInstanceState : new Bundle();
        bundle.putBoolean("isWifi", true);
        super.onCreate(bundle);
        getOtherUsersPhotos();
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
                    String username = files[i].getName().substring(6, files[i].getName().length()-3);
                    username_photos_Map.put(username, userPhotos);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        //TODO: Como mostrar as fotos dos outros aos utilizadores antes de as sacar???
    }
}
