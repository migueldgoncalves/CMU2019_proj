package pt.ulisboa.tecnico.cmov.proj;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class NewAlbum extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_album);
        getWindow().getDecorView().setBackgroundColor(Color.WHITE);

        final Button createAlbumButton = findViewById(R.id.create_album_button);
        createAlbumButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //TODO:
            }
        });
    }
}
