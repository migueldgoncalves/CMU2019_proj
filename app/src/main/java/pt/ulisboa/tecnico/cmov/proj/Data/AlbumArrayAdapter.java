package pt.ulisboa.tecnico.cmov.proj.Data;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import pt.ulisboa.tecnico.cmov.proj.R;

public class AlbumArrayAdapter extends ArrayAdapter<Album> {

    private Context context;
    private List<Album> albums;
    private String albumName = "Album Name";
    //private Image albumThumbnail = null; ??

    public AlbumArrayAdapter(Context context, int resource, ArrayList<Album> objects) {
        super(context, resource, objects);

        this.context = context;
        this.albums = objects;
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        //get the property we are displaying
        Album album = albums.get(position);
        albumName = album.getAlbumName();

        //get the inflater and inflate the XML layout for each item
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.activity_album_thumb, null);

        TextView albumNameText = view.findViewById(R.id.album_name);
        albumNameText.setText(albumName);

        return view;
    }
}