package pt.ulisboa.tecnico.cmov.proj;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;

import pt.ulisboa.tecnico.cmov.proj.Data.Peer2PhotoApp;


public class SignIn extends AppCompatActivity {

    public static final String URL_BASE = "http://192.168.1.10:8080";
    public static final String URL_SIGNUP = URL_BASE + "/signin";

    Context ctx = this;
    private RequestQueue queue = null;
    private JSONObject httpResponse = null;


    private EditText usernameView;
    private EditText passwordView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        Button signInButton = findViewById(R.id.sign_in);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //TODO:Check If server acknowledges the introduced credentials and if so store the credentials and proceed to next activity
                InitialVariableSetup();
                startActivity(new Intent(SignIn.this, HomePage.class));
            }
        });

    }

    private void httpRequest(String username, String password){
        android.util.Log.d("debug", "Starting POST request to URL " + URL_SIGNUP);
        createHTTPQueue();
        HashMap<String, String> mapRequest = new HashMap();
        mapRequest.put("username", username);
        mapRequest.put("password", password);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, URL_SIGNUP, new JSONObject(mapRequest),
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject httpResponse) {
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
                                Intent intent = new Intent(ctx, SignIn.class);
                                startActivity(intent);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        cleanHTTPResponse();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                cleanHTTPResponse();
                android.util.Log.d("debug", "POST error");
            }
        }
        );
        queue.add(request);
    }

    private void signIn(View view) {
        // Reset errors.
        usernameView.setError(null);
        passwordView.setError(null);

        // Store values at the time of the login attempt.
        String username = usernameView.getText().toString();
        String password = passwordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        httpRequest(username, password);
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