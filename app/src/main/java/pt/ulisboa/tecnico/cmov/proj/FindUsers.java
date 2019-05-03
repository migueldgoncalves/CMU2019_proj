package pt.ulisboa.tecnico.cmov.proj;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import pt.ulisboa.tecnico.cmov.proj.Adapters.UserAdapter;
import pt.ulisboa.tecnico.cmov.proj.Data.Peer2PhotoApp;
import pt.ulisboa.tecnico.cmov.proj.Data.User;
import pt.ulisboa.tecnico.cmov.proj.HTMLHandlers.HttpRequestGetAllUsers;

public class FindUsers extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static ArrayList<User> users = new ArrayList<>();
    private static ArrayAdapter<User> userAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_users);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        users.clear();

        String URL_BASE = getString(R.string.serverIP);
        String URL_GET_ALL_USERS = URL_BASE + "/users";


        String sessionId = ((Peer2PhotoApp)getApplication()).getSessionId();
        String username = ((Peer2PhotoApp)getApplication()).getUsername();
        String URL = URL_GET_ALL_USERS + "/" + sessionId + "/" + username;

        new HttpRequestGetAllUsers(this);
        HttpRequestGetAllUsers.httpRequest(URL);

        users = new ArrayList<>(Arrays.asList(
                new User(0, "Joao"),
                new User(1, "Jacinto")
        ));

        userAdapter = new UserAdapter(this, 0, users);
        ListView userTable = findViewById(R.id.userList);
        userTable.setAdapter(userAdapter);

        userTable.setOnItemClickListener((parent, view, position, id) -> addUser(position));

        //Send server requests as user types out user's name
        EditText searchText = findViewById(R.id.searchText);
        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {}
            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                //TODO: Comecamos a procurar depois do utilizador introduzir 2 caracteres?
                if(s.length() > 1)
                    sendServerRequest(s.toString());
            }
        });
    }

    protected void sendServerRequest(String userName) {
        //TODO: Gonçalo este metodo ainda e necessario ?
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
        getMenuInflater().inflate(R.menu.find_users, menu);
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
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {

        } else if (id == R.id.nav_createAlbum) {

        } else if (id == R.id.nav_logs) {
            
        } else if (id == R.id.nav_dropbox) {

        } else if (id == R.id.nav_signOut) {

        } else if (id == R.id.nav_settings){

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void addUser(int position){
        AlertDialog.Builder builder = new AlertDialog.Builder(FindUsers.this);
        builder.setTitle("Add " + users.get(position).getUserName() + " to album?");

        // Set up the buttons
        builder.setPositiveButton("OK", (dialog, which) -> {
            Intent intent = new Intent();
            intent.putExtra("userName", users.get(position).getUserName());
            setResult(Activity.RESULT_OK, intent);
            finish();
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    public void updateApplicationLogs(@NonNull String operationResult){
        String Operation = "OPERATION: List All Users" + "\n";
        String timeStamp = "TIMESTAMP: " + new Date().toString() + "\n";
        String result = "RESULT: " + operationResult + "\n";

        ((Peer2PhotoApp)getApplication()).updateLog(Operation + timeStamp + result);

    }

    public void parseUsers(@NonNull String allUsers){
        String[] parsedUsers = allUsers.split(",");

        //TODO: Gonçalo o vetor parsedUsers contem os usernames de todos os utilizadores do sistema, este metodo vai ser executado onCreate e aquilo que preciso que facas e apresentar todos os usernames que estao no vetor de strings da forma que tens o joao e o jacinto apresentados

    }

}
