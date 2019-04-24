package pt.ulisboa.tecnico.cmov.proj;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.dropbox.core.v2.files.FileMetadata;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import pt.ulisboa.tecnico.cmov.proj.Adapters.PhotoAdapter;
import pt.ulisboa.tecnico.cmov.proj.Data.Peer2PhotoApp;
import pt.ulisboa.tecnico.cmov.proj.Data.Photo;
import pt.ulisboa.tecnico.cmov.proj.Dropbox.DropboxClientFactory;
import pt.ulisboa.tecnico.cmov.proj.Dropbox.UploadFileTask;

public class AlbumView extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static ArrayList<Photo> photos = new ArrayList<Photo>();
    private static ArrayAdapter<Photo> photoAdapter = null;

    public String URL_BASE;
    public String URL_ALBUM;

    Context ctx = this;
    private RequestQueue queue = null;
    private JSONObject httpResponse = null;

    String success;
    String error;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_view);

        URL_BASE = getString(R.string.serverIP);
        URL_ALBUM = URL_BASE + "/album";

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getIntent().getStringExtra("AlbumName"));

        photos.clear(); //Não eliminar esta linha

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
            final int ACTIVITY_SELECT_IMAGE = 1234;
            startActivityForResult(i, ACTIVITY_SELECT_IMAGE);
        });

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        populatePhotoArray();

        photoAdapter = new PhotoAdapter(this, 0, photos);
        GridView photoTable = findViewById(R.id.photo_grid);
        photoTable.setAdapter(photoAdapter);

        photoTable.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
                //TODO: O que fazer quando clico numa foto??
                //SUGESTAO: Nada!
                //Só Sei Que Nada Fará
            }
        });

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        loadLocalPhotos();
        getRemotePhotos();
    }

    protected void populatePhotoArray() {
        //TODO: Replace with fetching all photos from album

        /*
        photos   = new ArrayList<>(Arrays.asList(
                new Photo(BitmapFactory.decodeResource(getApplicationContext().getResources(),
                        R.drawable.empty_thumbnail)),
                new Photo(BitmapFactory.decodeResource(getApplicationContext().getResources(),
                        R.drawable.empty_thumbnail)),
                new Photo(BitmapFactory.decodeResource(getApplicationContext().getResources(),
                        R.drawable.empty_thumbnail)),
                new Photo(BitmapFactory.decodeResource(getApplicationContext().getResources(),
                        R.drawable.empty_thumbnail)),
                new Photo(BitmapFactory.decodeResource(getApplicationContext().getResources(),
                        R.drawable.empty_thumbnail)),
                new Photo(BitmapFactory.decodeResource(getApplicationContext().getResources(),
                        R.drawable.empty_thumbnail)),
                new Photo(BitmapFactory.decodeResource(getApplicationContext().getResources(),
                        R.drawable.empty_thumbnail))
        ));
        */
    }

    private void addNewPhoto(Bitmap photoBitmap) {
        photos.add(new Photo(photoBitmap));
        photoAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK){
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String filePath = cursor.getString(columnIndex);
            cursor.close();

            Bundle b = getIntent().getExtras();
            String value = "ERROR"; // or other values
            if(b != null)
                value = b.getString("AlbumName");

            String PhotoName = value + "_Photo_" + new Random().nextInt();

            new UploadFileTask(AlbumView.this, DropboxClientFactory.getClient(), new UploadFileTask.Callback(){
                 @Override
                 public void onUploadComplete(FileMetadata result) {

                  }

                  @Override
                  public void onError(Exception e) {

                 }
            }).execute(PhotoName, "/Peer2Photo", "NEW_PHOTO", filePath, value, ((Peer2PhotoApp) getApplication()).getSessionId(), ((Peer2PhotoApp) getApplication()).getUsername(), ((Peer2PhotoApp) getApplication()).getAlbumId(value));

            imageScalingAndPosting(filePath);

        }
    }

    private void imageScalingAndPosting(String filePath){
        Bitmap yourSelectedImage = BitmapFactory.decodeFile(filePath);

        final int maxSize = 628;
        int outWidth;
        int outHeight;
        int inWidth = yourSelectedImage.getWidth();
        int inHeight = yourSelectedImage.getHeight();
        if(inWidth > inHeight){
            outWidth = maxSize;
            outHeight = (inHeight * maxSize) / inWidth;
        } else {
            outHeight = maxSize;
            outWidth = (inWidth * maxSize) / inHeight;
        }

        Bitmap scaled = Bitmap.createScaledBitmap(yourSelectedImage, outWidth, outHeight, false);

        addNewPhoto(scaled);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.album_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.add_user) {
            Intent intent = new Intent(AlbumView.this, FindUsers.class);
            startActivityForResult(intent, 0);
            finish();
            //TODO: Receive user response
            return true;
        }
        else if (id == R.id.add_photo) {
            //TODO: Adicionar
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private boolean loadLocalPhotos(){

        Bundle b = getIntent().getExtras();
        String value = "ERROR"; // or other values
        if(b != null)
            value = b.getString("AlbumName");

        File localPhotoPaths = new File(getApplicationContext().getFilesDir().getPath() + "/" + value + "/" + value + "_LOCAL.txt");

        if(localPhotoPaths.isFile()){
            try{
                BufferedReader br = new BufferedReader(new FileReader(localPhotoPaths));
                String line;
                while ((line = br.readLine()) != null) {
                    imageScalingAndPosting(line);
                }
                return true;
            }catch (Exception e){
                e.printStackTrace();
                return false;
            }
        }

        System.out.println("IT IS NOT A FILE!!");
        return false;

    }

    private void getRemotePhotos(){
        Bundle b = getIntent().getExtras();
        String value = "ERROR"; // or other values
        if(b != null)
            value = b.getString("AlbumName");

        String AlbumId = ((Peer2PhotoApp) (getApplication())).getAlbumId(value);
        String SessionId = ((Peer2PhotoApp) (getApplication())).getSessionId();
        String Username = ((Peer2PhotoApp) (getApplication())).getUsername();

        httpRequest(AlbumId, Username, SessionId);

    }

    private void httpRequest(String albumId, String username, String sessionId){
        android.util.Log.d("debug", "Starting POST request to URL " + URL_ALBUM);
        createHTTPQueue();
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, URL_ALBUM + "/" + sessionId + "/" + username + "/" + albumId, null,
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

                            urlParser(httpResponse.getString("users").split(","), httpResponse);

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

    private void urlParser(String[] users, JSONObject mapResponse){
        try{
            //The Array List that will have all the URLs for the slices
            ArrayList<String> URLs = new ArrayList<>();

            for(int i = 0; i < users.length; i++){
                if(!users[i].equals(((Peer2PhotoApp)getApplication()).getUsername())){
                    if (mapResponse.getString(users[i]) != null) {
                        URLs.add(mapResponse.getString(users[i]));
                    } else {
                        System.out.println("Null Slice");
                    }
                }
            }

            Bundle b = getIntent().getExtras();
            String value = "ERROR"; // or other values
            if(b != null)
                value = b.getString("AlbumName");


            for (int i = 0; i < URLs.size(); i++){
                //Saving the obtained slice from the cloud in a file to later remove the file
                FileUtils.copyURLToFile(new URL(URLs.get(i).replaceAll("\u003d", "=")), new File(getApplicationContext().getFilesDir().getPath() + "/" + value + "/" + users[i] + "_SLICE.txt"));
                //The Recently Saved Slice
                File Slice = new File(getApplicationContext().getFilesDir().getPath() + "/" + value + "/" + users[i] + "_SLICE.txt");

                if(Slice.exists()){
                    //The Local File That Contains the Paths of The Photos Downloaded in Local Storage
                    File RemotePhotosPath = new File(getApplicationContext().getFilesDir().getPath() + "/" + value + "/" + users[i] + "_REMOTE.txt");

                    if(!RemotePhotosPath.exists()){
                        RemotePhotosPath.createNewFile();
                    }
                    //The URLs for The Photos of the current slice being processed
                    List<String> contents = FileUtils.readLines(Slice);

                    //The Path For The Galery Directory of The App
                    File imageRoot = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), getString(R.string.app_directory_name));

                    for(int x = 0; x < contents.size(); x++){
                        //Downloading a photo and saving it to the galery inside the album created for the app with the Nomeclature USERNAME + _PHOTO + NUMBER_OF_PHOTO + EXTENSION
                        FileUtils.copyURLToFile(new URL(contents.get(x)), new File(imageRoot, users[i] + "_PHOTO" + x + ".jpg"));
                        //Saving the path of the downloaded photo to a file to load the images and later remove them
                        FileUtils.writeStringToFile(RemotePhotosPath, new File(imageRoot, users[i] + "_PHOTO" + x).getPath());

                        imageScalingAndPosting(new File(imageRoot, users[i] + "_PHOTO" + x).getPath());
                    }

                    Slice.delete();

                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }
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
