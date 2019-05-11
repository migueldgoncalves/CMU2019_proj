package pt.ulisboa.tecnico.cmov.proj;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridView;

import com.dropbox.core.v2.files.FileMetadata;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

import pt.ulisboa.tecnico.cmov.proj.Adapters.PhotoAdapter;
import pt.ulisboa.tecnico.cmov.proj.Data.Peer2PhotoApp;
import pt.ulisboa.tecnico.cmov.proj.Data.Photo;
import pt.ulisboa.tecnico.cmov.proj.Dropbox.DownloadFileTask;
import pt.ulisboa.tecnico.cmov.proj.Dropbox.DropboxClientFactory;
import pt.ulisboa.tecnico.cmov.proj.Dropbox.UploadFileTask;
import pt.ulisboa.tecnico.cmov.proj.HTMLHandlers.HttpRequestGetAlbumPhotos;
import pt.ulisboa.tecnico.cmov.proj.HTMLHandlers.HttpRequestPutAddUserToAlbum;

public class AlbumView extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static ArrayList<Photo> photos = new ArrayList<>();
    private static ArrayAdapter<Photo> photoAdapter = null;

    public String URL_BASE;
    public String URL_ALBUM;
    public String URL_ADD_USER_TO_ALBUM;

    private boolean usingWifiDirect = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_view);

        usingWifiDirect = savedInstanceState.getBoolean("isWifi");

        URL_BASE = getString(R.string.serverIP);
        URL_ALBUM = URL_BASE + "/album";
        URL_ADD_USER_TO_ALBUM = URL_BASE + "/adduser";

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

        photoAdapter = new PhotoAdapter(this, 0, photos);
        GridView photoTable = findViewById(R.id.photo_grid);
        photoTable.setAdapter(photoAdapter);

        photoTable.setOnItemClickListener((parent, view, position, id) -> { });

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        loadLocalPhotos();
        if (!usingWifiDirect) getRemotePhotos();
    }

    private void addNewPhoto(Bitmap photoBitmap) {
        photos.add(new Photo(photoBitmap));
        photoAdapter.notifyDataSetChanged();
        updateApplicationLogs("Photo Successfully Added to Album", "Add Photo To Album");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK){
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            assert selectedImage != null;
            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            assert cursor != null;
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

    public void imageScalingAndPosting(String filePath){
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
        String option = (String) item.getTitleCondensed();

        //noinspection SimplifiableIfStatement
        if (option.equals("Add User")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(AlbumView.this);
            builder.setTitle("Username To Add");

            final EditText input = new EditText(AlbumView.this);
            input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL);
            builder.setView(input);

            // Set up the buttons
            builder.setPositiveButton("OK", (dialog, which) -> {
                String usernameToAdd = input.getText().toString();
                String sessionId = ((Peer2PhotoApp) getApplication()).getSessionId();
                String username = ((Peer2PhotoApp) getApplication()).getUsername();

                Bundle b = getIntent().getExtras();
                String value = "ERROR"; // or other values
                if(b != null)
                    value = b.getString("AlbumName");

                String albumId = ((Peer2PhotoApp) getApplication()).getAlbumId(value);

                new HttpRequestPutAddUserToAlbum(this);
                HttpRequestPutAddUserToAlbum.httpRequest(albumId, username, sessionId, usernameToAdd, URL_ADD_USER_TO_ALBUM);

            });

            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

            builder.show();
        }
        else if (id == R.id.add_photo) {
            //TODO: Adicionar
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
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

    private void loadLocalPhotos(){

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
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        else {
            android.util.Log.d("debug", "IT IS NOT A FILE!!");
        }
    }

    private void getRemotePhotos(){
        Bundle b = getIntent().getExtras();
        String value = "ERROR"; // or other values
        if(b != null)
            value = b.getString("AlbumName");

        String AlbumId = ((Peer2PhotoApp) (getApplication())).getAlbumId(value);
        String SessionId = ((Peer2PhotoApp) (getApplication())).getSessionId();
        String Username = ((Peer2PhotoApp) (getApplication())).getUsername();

        File imageRoot = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), getString(R.string.app_directory_name) + "/" + value);

        if(imageRoot.exists() && imageRoot.isDirectory()){
            try{
                File[] files = imageRoot.listFiles();
                for (File file : files) {
                    imageScalingAndPosting(file.getPath());
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        new HttpRequestGetAlbumPhotos(this);
        HttpRequestGetAlbumPhotos.httpRequest(AlbumId, Username, SessionId, URL_ALBUM);

    }

    public void urlParser(String usersString, JSONObject mapResponse){

        new DownloadFileTask(AlbumView.this, DropboxClientFactory.getClient(), new DownloadFileTask.Callback(){
            @Override
            public void onDownloadComplete(File file) {

            }

            @Override
            public void onError(Exception e) {

            }
        }).execute(usersString, ((Peer2PhotoApp)getApplication()).getUsername(), mapResponse.toString(), getApplicationContext().getFilesDir().getPath(), getString(R.string.app_directory_name));

    }

    public void updateApplicationLogs(@NonNull String operationResult, @NonNull String operation){
        String Operation = "OPERATION: " + operation + "\n";
        String timeStamp = "TIMESTAMP: " + new Date().toString() + "\n";
        String result = "RESULT: " + operationResult + "\n";

        ((Peer2PhotoApp)getApplication()).updateLog(Operation + timeStamp + result);

    }

}
