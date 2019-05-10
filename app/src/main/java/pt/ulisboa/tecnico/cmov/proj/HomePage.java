package pt.ulisboa.tecnico.cmov.proj;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Messenger;
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

import pt.ulisboa.tecnico.cmov.proj.Adapters.AlbumAdapter;
import pt.ulisboa.tecnico.cmov.proj.Data.Album;
import pt.ulisboa.tecnico.cmov.proj.Data.Peer2PhotoApp;
import pt.ulisboa.tecnico.cmov.proj.Dropbox.DropboxActivity;
import pt.ulisboa.tecnico.cmov.proj.Dropbox.DropboxClientFactory;
import pt.ulisboa.tecnico.cmov.proj.Dropbox.UploadFileTask;
import pt.ulisboa.tecnico.cmov.proj.HTMLHandlers.HttpRequestDeleteSession;
import pt.ulisboa.tecnico.cmov.proj.HTMLHandlers.HttpRequestGetUserAlbums;
import pt.ulisboa.tecnico.cmov.proj.HTMLHandlers.HttpRequestPostCreateAlbum;

public class HomePage extends DropboxActivity implements
        NavigationView.OnNavigationItemSelectedListener {

    private final static String ACCESS_KEY = "ktxcdvzt610l2ao";
    private final static String ACCESS_SECRET = "wurqteptiyuh9s2";

    //public static final String URL_BASE = "http://localhost:8080";
    public String URL_BASE;
    public String URL_CREATE_ALBUM;
    public String URL_LOAD_ALBUMS;
    public String URL_SIGNOUT;

    private static ArrayList<Album> albums = new ArrayList<>();
    private static ArrayAdapter<Album> albumAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        URL_BASE = getString(R.string.serverIP);
        URL_CREATE_ALBUM = URL_BASE + "/createalbum";
        URL_LOAD_ALBUMS = URL_BASE + "/useralbums";
        URL_SIGNOUT = URL_BASE + "/logout";

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Home");

        albums.clear();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> createAlbumByUser());

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        albumAdapter = new AlbumAdapter(this, 0, albums);
        GridView albumTable = findViewById(R.id.album_grid);
        albumTable.setAdapter(albumAdapter);

        loadAlbums();

        albumTable.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(HomePage.this, AlbumView.class);
            Bundle b = new Bundle();
            b.putString("AlbumName", albums.get(position).getAlbumName()); //Your id
            intent.putExtras(b); //Put your id to your next Intent
            startActivity(intent);
            finish();
        });

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                    PackageManager.PERMISSION_GRANTED) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    Toast.makeText(getApplicationContext(), "Application required to write to storage", Toast.LENGTH_SHORT).show();
                }
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
            }
        }
    }

    void updateAlbumAdapter() {
        albumAdapter.notifyDataSetChanged();

        GridView albumTable = findViewById(R.id.album_grid);
        albumTable.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(HomePage.this, AlbumView.class);
            Bundle b = new Bundle();
            b.putString("AlbumName", albums.get(position).getAlbumName()); //Your id
            intent.putExtras(b); //Put your id to your next Intent
            startActivity(intent);
            finish();
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 0) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(), "Application will not run without write storage permission", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            startActivity(new Intent(HomePage.this, HomePage.class));
        } else if (id == R.id.nav_createAlbum) {
            createAlbumByUser();
        }else if (id == R.id.nav_findUsers){
            startActivity(new Intent(HomePage.this, FindUsers.class));
        } else if (id == R.id.nav_logs) {
            startActivity(new Intent(HomePage.this, LogView.class));
        } else if (id == R.id.nav_dropbox) {

            if(!hasToken()){
                Auth.startOAuth2Authentication(HomePage.this, ACCESS_KEY);
            }else {
                Toast.makeText(HomePage.this, "You Are Already Logged In To Your Dropbox",
                        Toast.LENGTH_LONG).show();
            }

        } else if (id == R.id.nav_signOut) {
            String sessionId = ((Peer2PhotoApp) this.getApplication()).getSessionId();
            new HttpRequestDeleteSession(this);
            HttpRequestDeleteSession.httpRequest(sessionId, URL_SIGNOUT);
        } else if (id == R.id.nav_settings){

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onResume(){
        super.onResume();

        if(hasToken()){
            Toast.makeText(HomePage.this, "You Are Now Logged In To Your Dropbox", Toast.LENGTH_LONG).show();
            TextView username = findViewById(R.id.UsernameDisplay);
            TextView mail = findViewById(R.id.MailDisplay);
            try{
                username.setText(DropboxClientFactory.getClient().users().getCurrentAccount().getAccountId());
                mail.setText(DropboxClientFactory.getClient().users().getCurrentAccount().getEmail());
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void loadData() {

    }

    public void addNewAlbum(String albumName) {
        albums.add(new Album(albumName, R.drawable.empty_thumbnail));
        albumAdapter.notifyDataSetChanged();
    }

    private void createAlbumByUser(){
        AlertDialog.Builder builder = new AlertDialog.Builder(HomePage.this);
        builder.setTitle("Album Title");

        final EditText input = new EditText(HomePage.this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", (dialog, which) -> {
            String albumName = input.getText().toString();
            String sessionId = ((Peer2PhotoApp) getApplication()).getSessionId();
            String username = ((Peer2PhotoApp) getApplication()).getUsername();

            if(new File(getApplicationContext().getFilesDir().getPath() + "/" + input.getText().toString()).exists()){
                while (new File(getApplicationContext().getFilesDir().getPath() + "/" + input.getText().toString()).exists()){
                    Toast.makeText(this, "An Album With The Name " + input.getText().toString() + " Already Exists!", Toast.LENGTH_SHORT).show();
                    builder.setView(input);
                    builder.show();
                }
            }

            new HttpRequestPostCreateAlbum(this);
            HttpRequestPostCreateAlbum.httpRequest(username, sessionId, albumName, URL_CREATE_ALBUM);

        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    public void createAlbumInCloud(String albumName, String albumId){
        new UploadFileTask(HomePage.this, DropboxClientFactory.getClient(), new UploadFileTask.Callback(){

            @Override
            public void onUploadComplete(FileMetadata result) {
                Toast.makeText(HomePage.this, "Upload Complete!", Toast.LENGTH_SHORT).show();
                ((Peer2PhotoApp) getApplication()).addAlbum(albumId, albumName, getApplicationContext().getFilesDir().getPath() + "/albums.txt");
            }

            @Override
            public void onError(Exception e) {
                //TODO: Remove Album From Local Storage and From Server Storage
            }
        }).execute(albumName, "/Peer2Photo", "NEW_ALBUM", ((Peer2PhotoApp) getApplication()).getSessionId(), ((Peer2PhotoApp) getApplication()).getUsername(), albumId);
    }

    private void loadAlbums() {
        File[] directories = new File(getApplicationContext().getFilesDir().getPath()).listFiles(File::isDirectory);

        if(!(directories.length == 0)){
            for (File i : directories){
                addNewAlbum(i.getName());
            }
        }

        updateApplicationLogs("List User Albums", "Local Albums Loaded Successfully");

        new HttpRequestGetUserAlbums(this);
        HttpRequestGetUserAlbums.httpRequest(((Peer2PhotoApp)getApplication()).getUsername(), ((Peer2PhotoApp)getApplication()).getSessionId(), URL_LOAD_ALBUMS);
    }

    public void parseAlbumNames(String[] albumIds, JSONObject httpResponse) {
        // 3\\ cases - User was not added to third party albums;
        // User was added to album with same name as another album user already has;
        // User was added to album with a name different of all user's albums
        try{
            for (String albumId : albumIds) {
                String albumName = httpResponse.getString(albumId);
                if (((Peer2PhotoApp) getApplication()).getAlbumId(albumName) == null) {
                    if (!(new File(getApplicationContext().getFilesDir().getPath() + "/" + albumName).exists())) {
                        createAlbumInCloud(albumName, albumId);
                        addNewAlbum(albumName);
                        Log.d("debug", "User has been added to album of other user and its name does not exist in user's albums");
                    } else {
                        File fileToDelete = new File(getApplicationContext().getFilesDir().getPath() + "/" + albumName);
                        if (fileToDelete.delete()) {
                            createAlbumInCloud(albumName, albumId);
                            addNewAlbum(albumName);
                            Log.d("debug", "User has been added to album of other user and its name does not exist in user's albums");
                        }
                    }
                } else {
                    if (!((Peer2PhotoApp) getApplication()).getAlbumId(albumName).equals(albumId)) {
                        String newName = albumName + "_" + albumId;
                        createAlbumInCloud(newName, albumId);
                        addNewAlbum(newName);
                        Log.d("debug", "User has been added to album of other user with name equal to one of user's albums");
                    } else {
                        if(!new File(getApplicationContext().getFilesDir().getPath() + "/" + albumName).exists()){
                            createAlbumInCloud(albumName, albumId);
                            addNewAlbum(albumName);
                        }
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public void updateApplicationLogs(@NonNull String operation, @NonNull String operationResult){
        String Operation = "OPERATION: " + operation + "\n";
        String timeStamp = "TIMESTAMP: " + new Date().toString() + "\n";
        String result = "RESULT: " + operationResult + "\n";

        ((Peer2PhotoApp)getApplication()).updateLog(Operation + timeStamp + result);

    }

}