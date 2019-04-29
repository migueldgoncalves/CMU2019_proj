package pt.ulisboa.tecnico.cmov.proj;

import android.Manifest;
import android.content.Context;
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
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
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

public class HomePage extends DropboxActivity implements NavigationView.OnNavigationItemSelectedListener {

    private final static String ACCESS_KEY = "ktxcdvzt610l2ao";
    private final static String ACCESS_SECRET = "wurqteptiyuh9s2";

    //public static final String URL_BASE = "http://localhost:8080";
    public String URL_BASE;
    public String URL_CREATE_ALBUM;
    public String URL_LOAD_ALBUMS;

    Context ctx = this;
    private RequestQueue queue = null;
    private JSONObject httpResponse = null;

    private static ArrayList<Album> albums = new ArrayList<>();
    private static ArrayAdapter<Album> albumAdapter = null;

    String success;
    String error;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        URL_BASE = getString(R.string.serverIP);
        URL_CREATE_ALBUM = URL_BASE + "/createalbum";
        URL_LOAD_ALBUMS = URL_BASE + "/useralbums";

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

        populateAlbumArray();

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

    void populateAlbumArray() {
        //TODO: Replace with fetching dropbox folder for all albums

        /*
        albums = new ArrayList<>(Arrays.asList(
                new Album("Fotos 2019", R.drawable.empty_thumbnail),
                new Album("Fotos 2018", R.drawable.empty_thumbnail),
                new Album("Fotos 2017", R.drawable.empty_thumbnail),
                new Album("Fotos 2016", R.drawable.empty_thumbnail),
                new Album("Fotos 2015", R.drawable.empty_thumbnail),
                new Album("Fotos 2014", R.drawable.empty_thumbnail),
                new Album("Fotos 2013", R.drawable.empty_thumbnail)
        ));
        */
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
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            startActivity(new Intent(HomePage.this, HomePage.class));
        } else if (id == R.id.nav_createAlbum) {
            createAlbumByUser();
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
            startActivity(new Intent(HomePage.this, MainActivity.class));
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

            }
        }
    }

    @Override
    protected void loadData() {

    }

    private void addNewAlbum(String albumName) {
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
                    Toast.makeText(ctx, "An Album With The Name " + input.getText().toString() + " Already Exists!", Toast.LENGTH_SHORT).show();
                    builder.setView(input);
                }
            }

            httpRequestCreateAlbum(albumName, username, sessionId);

        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void createAlbumInCloud(String albumName, String albumId){
        new UploadFileTask(HomePage.this, DropboxClientFactory.getClient(), new UploadFileTask.Callback(){

            @Override
            public void onUploadComplete(FileMetadata result) {
                Toast.makeText(ctx, "Upload Complete!", Toast.LENGTH_SHORT).show();
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

        httpRequestForAlbumLoading(((Peer2PhotoApp)getApplication()).getUsername(), ((Peer2PhotoApp)getApplication()).getSessionId());
    }

    private void httpRequestForAlbumLoading(String username, String sessionId) {
        android.util.Log.d("debug", "Starting GET request to URL " + URL_LOAD_ALBUMS + "/" + sessionId + "/" + username);
        createHTTPQueue();
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, URL_LOAD_ALBUMS + "/" + sessionId + "/" + username, null,
                httpResponse -> {
                    try {
                        setHTTPResponse(httpResponse);
                        android.util.Log.d("debug", httpResponse.toString());
                        if(httpResponse.has("error")) {
                            error = httpResponse.getString("error");
                            android.util.Log.d("debug", "Error");
                            android.util.Log.d("debug", error);
                            Toast.makeText(ctx, error, Toast.LENGTH_SHORT).show();
                        }
                        else if(httpResponse.has("success")) {
                            success = httpResponse.getString("success");
                            android.util.Log.d("debug", "Success");
                            android.util.Log.d("debug", success);
                            Toast.makeText(ctx, success, Toast.LENGTH_SHORT).show();

                            if(!httpResponse.getString("size").equals("0")){
                                String[] albumIds = httpResponse.getString("albums").split(",");
                                parseAlbumNames(albumIds, httpResponse);
                            }
                        }
                        else {
                            Toast.makeText(ctx, "No adequate response received", Toast.LENGTH_SHORT).show();
                            throw new Exception("No adequate response received", new Exception());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    cleanHTTPResponse();
                }, error -> {
            cleanHTTPResponse();
            android.util.Log.d("debug", "GET error");
        }
        );
        queue.add(request);
    }

    private void parseAlbumNames(String[] albumIds, JSONObject httpResponse) {
        // 3 cases - User was not added to third party albums;
        // User was added to album with same name as another album user already has;
        // User was added to album with a name different of all user's albums
        try{
            for(int i = 0; i < albumIds.length; i++){
                String albumName = httpResponse.getString(albumIds[i]);
                if(((Peer2PhotoApp)getApplication()).getAlbumId(albumName) == null) {
                    createAlbumInCloud(albumName, albumIds[i]);
                    addNewAlbum(albumName);
                    android.util.Log.d("debug", "User has been added to album of other user and its name does not exist in user's albums");
                }
                else if(!((Peer2PhotoApp)getApplication()).getAlbumId(albumName).equals(albumIds[i])){
                    String newName = albumName + "_" + albumIds[i];
                    createAlbumInCloud(newName, albumIds[i]);
                    addNewAlbum(newName);
                    android.util.Log.d("debug", "User has been added to album of other user with name equal to one of user's albums");
                }else{
                    android.util.Log.d("debug", "User either created this album or has already set slice in it");
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private void httpRequestCreateAlbum(String albumName, String username, String sessionId) {
        android.util.Log.d("debug", "Starting POST request to URL " + URL_CREATE_ALBUM);
        createHTTPQueue();
        HashMap<String, String> mapRequest = new HashMap<>();
        mapRequest.put("albumName", albumName);
        mapRequest.put("username", username);
        mapRequest.put("sessionId", sessionId);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, URL_CREATE_ALBUM, new JSONObject(mapRequest),
                httpResponse -> {
                    try {
                        setHTTPResponse(httpResponse);
                        android.util.Log.d("debug", httpResponse.toString());
                        if(httpResponse.has("error")) {
                            error = httpResponse.getString("error");
                            android.util.Log.d("debug", "Error");
                            android.util.Log.d("debug", error);
                            Toast.makeText(ctx, error, Toast.LENGTH_SHORT).show();
                        }
                        else if(httpResponse.has("success")) {
                            success = httpResponse.getString("success");
                            String albumId = httpResponse.getString("albumId");
                            android.util.Log.d("debug", "Success");
                            android.util.Log.d("debug", success);
                            Toast.makeText(ctx, success, Toast.LENGTH_SHORT).show();

                            createAlbumInCloud(albumName, albumId);
                            addNewAlbum(albumName);
                        }
                        else {
                            Toast.makeText(ctx, "No adequate response received", Toast.LENGTH_SHORT).show();
                            throw new Exception("No adequate response received", new Exception());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    cleanHTTPResponse();
                }, error -> {
            cleanHTTPResponse();
            android.util.Log.d("debug", "POST error");
        }
        );
        queue.add(request);
    }

    private void setHTTPResponse(JSONObject json) {
        this.httpResponse = json;
    }

    private void cleanHTTPResponse() {
        success = null;
        error = null;
        this.httpResponse = null;
        android.util.Log.d("debug", "Cleaned " + new Date().getTime());
    }

    private void createHTTPQueue() {
        if(this.queue == null) {
            this.queue = Volley.newRequestQueue(ctx);
        }
    }

}