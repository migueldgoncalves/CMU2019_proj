package pt.ulisboa.tecnico.cmov.proj;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.PopupMenu;

public class AlbumView extends Activity implements PopupMenu.OnMenuItemClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);
        getWindow().getDecorView().setBackgroundColor(Color.WHITE);
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
    }
}
