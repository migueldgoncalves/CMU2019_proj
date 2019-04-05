package pt.ulisboa.tecnico.cmov.proj;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
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

import com.dropbox.core.v2.files.FileMetadata;

import java.util.ArrayList;
import java.util.Random;

import pt.ulisboa.tecnico.cmov.proj.Adapters.PhotoAdapter;
import pt.ulisboa.tecnico.cmov.proj.Data.Photo;
import pt.ulisboa.tecnico.cmov.proj.Dropbox.DropboxClientFactory;
import pt.ulisboa.tecnico.cmov.proj.Dropbox.UploadFileTask;

public class AlbumView extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static ArrayList<Photo> photos = new ArrayList<Photo>();
    private static ArrayAdapter<Photo> photoAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_view);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                final int ACTIVITY_SELECT_IMAGE = 1234;
                startActivityForResult(i, ACTIVITY_SELECT_IMAGE);
            }
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
            }
        });

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
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
            }).execute(PhotoName, "/Peer2Photo", "NEW_PHOTO", filePath, value);

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
}
