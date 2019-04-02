package pt.ulisboa.tecnico.cmov.proj;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import pt.ulisboa.tecnico.cmov.proj.Data.Peer2PhotoApp;


public class SignIn extends AppCompatActivity {

    private EditText username;
    private EditText password;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        Button signInButton = findViewById(R.id.sign_in);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //TODO:Check If server acknowledges the introduced credentials and if so store the credentials and proceed to next activity

                if(serverValidatesCredentials()){
                    InitialVariableSetup();
                    startActivity(new Intent(SignIn.this, HomePage.class));
                }

            }
        });

    }

    private boolean serverValidatesCredentials() {

        String introducedUsername = ((EditText)findViewById(R.id.username)).getText().toString();
        String introducedPassword = ((EditText)findViewById(R.id.password)).getText().toString();

        return true;
    }

    private void InitialVariableSetup(){
        ((Peer2PhotoApp) this.getApplication()).setUsername(((EditText)findViewById(R.id.username)).getText().toString());
        ((Peer2PhotoApp) this.getApplication()).setPassword(((EditText)findViewById(R.id.password)).getText().toString());
    }

}