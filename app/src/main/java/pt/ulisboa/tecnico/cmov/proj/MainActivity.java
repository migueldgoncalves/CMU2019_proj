package pt.ulisboa.tecnico.cmov.proj;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().getDecorView().setBackgroundColor(Color.WHITE);

        final Button signIn_button = findViewById(R.id.sign_in);
        signIn_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Code for New Activity of Signing Up
            }
        });

        final Button signUp_button = findViewById(R.id.sign_up);
        signUp_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Code for New Activity of Signing Up
            }
        });
    }
}
