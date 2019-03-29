package pt.ulisboa.tecnico.cmov.proj;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.PopupMenu;

import java.util.ArrayList;
import java.util.Arrays;

import pt.ulisboa.tecnico.cmov.proj.Data.Photo;
import pt.ulisboa.tecnico.cmov.proj.Data.PhotoArrayAdapter;

public class AlbumView extends Activity implements PopupMenu.OnMenuItemClickListener {

    private static ArrayList<Photo> photos = new ArrayList<Photo>();
    private static ArrayAdapter<Photo> photoAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);
        getWindow().getDecorView().setBackgroundColor(Color.WHITE);

        photos = new ArrayList<>(Arrays.asList(
                new Photo(null),
                new Photo(null),
                new Photo(null),
                new Photo(null),
                new Photo(null)
        ));

        photoAdapter = new PhotoArrayAdapter(this, 0, photos);
        GridView photoTable = findViewById(R.id.photo_grid);

        photoTable.setAdapter(photoAdapter);
    }

    public void showPopUp(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.setOnMenuItemClickListener(this);
        popup.inflate(R.menu.activity_album_options);
        popup.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_photo:
                //startActivity(new Intent(AlbumView.this, AddPhoto.class));
                break;
            case R.id.add_user:
                //startActivity(new Intent(AlbumView.this, AddUser.class));
                break;
        }

        return true;

    }
}
