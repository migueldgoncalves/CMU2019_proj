package pt.ulisboa.tecnico.cmov.proj;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;

import pt.ulisboa.tecnico.cmov.proj.Data.Peer2PhotoApp;


public class SignIn extends AppCompatActivity {

    //public static final String URL_BASE = "http://localhost:8080";
    public static final String URL_BASE = "http://192.168.43.165:8080";
    public static final String URL_SIGNIN = URL_BASE + "/login";

    Context ctx = this;
    private RequestQueue queue = null;
    private JSONObject httpResponse = null;

    private EditText usernameView;
    private EditText passwordView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        usernameView = findViewById(R.id.username_login);
        passwordView = findViewById(R.id.password_login);

        Button signInButton = findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(v -> signIn());
    }

    private void httpRequest(String username, String password){
        android.util.Log.d("debug", "Starting POST request to URL " + URL_SIGNIN);
        createHTTPQueue();
        HashMap<String, String> mapRequest = new HashMap<>();
        mapRequest.put("username", username);
        mapRequest.put("password", password);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, URL_SIGNIN, new JSONObject(mapRequest),
                httpResponse -> {
                    try {
                        setHTTPResponse(httpResponse);
                        String success = httpResponse.getString("success");
                        String error = httpResponse.getString("error");
                        android.util.Log.d("debug", httpResponse.toString());
                        android.util.Log.d("debug", success);
                        android.util.Log.d("debug", error);
                        if(!error.equals("null")) {
                            Toast.makeText(ctx, error, Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Toast.makeText(ctx, success, Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(ctx, HomePage.class);
                            InitialVariableSetup(httpResponse.getString("sessionId"));
                            startActivity(intent);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    cleanHTTPResponse();
                }, error -> {
                    cleanHTTPResponse();
                    android.util.Log.d("debug", "POST error");
                }
        );
        queue.add(request);
    }

    private void signIn() {
        // Reset errors.
        usernameView.setError(null);
        passwordView.setError(null);

        httpRequest(usernameView.getText().toString(), passwordView.getText().toString());
    }

    private void setHTTPResponse(JSONObject json) {
        this.httpResponse = json;
    }

    private void cleanHTTPResponse() {
        this.httpResponse = null;
        android.util.Log.d("debug", "Cleaned " + new Date().getTime());
    }

    private void createHTTPQueue() {
        if(this.queue == null) {
            this.queue = Volley.newRequestQueue(ctx);
        }
    }

    private void InitialVariableSetup(String sessionId){
        ((Peer2PhotoApp) this.getApplication()).setUsername(((EditText)findViewById(R.id.username_login)).getText().toString());
        ((Peer2PhotoApp) this.getApplication()).setPassword(((EditText)findViewById(R.id.password_login)).getText().toString());
        ((Peer2PhotoApp) this.getApplication()).setSessionId(sessionId);
    }

}