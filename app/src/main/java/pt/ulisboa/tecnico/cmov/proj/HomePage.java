package pt.ulisboa.tecnico.cmov.proj;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.dropbox.core.android.Auth;
import com.dropbox.core.v2.files.FileMetadata;

import java.util.ArrayList;

import pt.ulisboa.tecnico.cmov.proj.Data.Album;
import pt.ulisboa.tecnico.cmov.proj.Data.AlbumAdapter;
import pt.ulisboa.tecnico.cmov.proj.Dropbox.DropboxActivity;
import pt.ulisboa.tecnico.cmov.proj.Dropbox.DropboxClientFactory;
import pt.ulisboa.tecnico.cmov.proj.Dropbox.UploadFileTask;

public class HomePage extends DropboxActivity implements NavigationView.OnNavigationItemSelectedListener {

    private String m_Text = "";
    private final static String ACCESS_KEY = "ktxcdvzt610l2ao";
    private final static String ACCESS_SECRET = "wurqteptiyuh9s2";

    private static ArrayList<Album> albums = new ArrayList<Album>();
    private static ArrayAdapter<Album> albumAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createAlbum();
            }
        });

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        populateAlbumArray();

        albumAdapter = new AlbumAdapter(this, 0, albums);
        GridView albumTable = findViewById(R.id.album_grid);
        albumTable.setAdapter(albumAdapter);

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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home_page, menu);
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

        if (id == R.id.nav_home) {
            startActivity(new Intent(HomePage.this, HomePage.class));
        } else if (id == R.id.nav_createAlbum) {
            createAlbum();
        } else if (id == R.id.nav_findUsers) {
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
            Toast.makeText(HomePage.this, "You Are Now Logged In To Your Dropbox",
                    Toast.LENGTH_LONG).show();
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

    private void createAlbum(){
        AlertDialog.Builder builder = new AlertDialog.Builder(HomePage.this);
        builder.setTitle("Title");

        // Set up the input
        final EditText input = new EditText(HomePage.this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL);
        builder.setView(input);

        //TODO: Verify if album name already exists

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                m_Text = input.getText().toString();
                ViewGroup linearLayout = findViewById(R.id.album_grid);

                //TODO: Ask server to create album in its local storage

                //#####################################################

                //TODO: Create Cloud File corresponding to album file (TXT file) after server acknowledgment of album creation
                new UploadFileTask(HomePage.this, DropboxClientFactory.getClient(), new UploadFileTask.Callback(){

                    @Override
                    public void onUploadComplete(FileMetadata result) {

                    }

                    @Override
                    public void onError(Exception e) {

                    }
                }).execute(m_Text, "/Peer2Photo", "NEW_ALBUM");
                //#####################################################


                Button bt = new Button(HomePage.this);
                bt.setText(m_Text);
                bt.setBackgroundColor(Color.RED);
                bt.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
                        LayoutParams.WRAP_CONTENT));
                linearLayout.addView(bt);
                bt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(HomePage.this, AlbumView.class);
                        Bundle b = new Bundle();
                        b.putString("AlbumName", m_Text); //Your id
                        intent.putExtras(b); //Put your id to your next Intent
                        startActivity(intent);
                        finish();
                    }
                });
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

}
