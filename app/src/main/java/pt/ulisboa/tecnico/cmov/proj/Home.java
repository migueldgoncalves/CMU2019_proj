package pt.ulisboa.tecnico.cmov.proj;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

import pt.ulisboa.tecnico.cmov.proj.Data.Album;
import pt.ulisboa.tecnico.cmov.proj.Data.AlbumArrayAdapter;

public class Home extends AppCompatActivity {

    private static ArrayList<Album> albums = new ArrayList<Album>();
    private static ArrayAdapter<Album> albumAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        getWindow().getDecorView().setBackgroundColor(Color.WHITE);

        albums = new ArrayList<>(Arrays.asList(
                new Album("Fotos 2019", R.drawable.empty_thumbnail),
                new Album("Fotos 2018", R.drawable.empty_thumbnail)
        ));

        albumAdapter = new AlbumArrayAdapter(this, 0, albums);
        GridView albumTable = findViewById(R.id.album_grid);

        albumTable.setAdapter(albumAdapter);

        //albumAdapter.notifyDataSetChanged();

        final Button addAlbumButton = findViewById(R.id.add_album_button);
        addAlbumButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(Home.this, NewAlbum.class));
            }
        });
    }
}
