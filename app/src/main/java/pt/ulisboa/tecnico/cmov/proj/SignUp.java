package pt.ulisboa.tecnico.cmov.proj;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.EditText;

import pt.ulisboa.tecnico.cmov.proj.HTMLHandlers.HttpRequestPost;

public class SignUp extends AppCompatActivity {

    //public static final String URL_BASE = "http://localhost:8080";
    public String URL_BASE;
    public String URL_SIGNUP;

    private EditText UsernameView;
    private EditText PasswordView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        URL_BASE = getString(R.string.serverIP);
        URL_SIGNUP = "/signup";

        UsernameView = findViewById(R.id.username_signup);
        PasswordView = findViewById(R.id.password_signup);

        Button SignUpButton = findViewById(R.id.sign_up_button);
        SignUpButton.setOnClickListener(v -> {
            UsernameView.setError(null);
            PasswordView.setError(null);

            new HttpRequestPost(getApplicationContext()).httpRequest(URL_BASE, URL_SIGNUP, UsernameView.getText().toString(), PasswordView.getText().toString());            });
    }

}
