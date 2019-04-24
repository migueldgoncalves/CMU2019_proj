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
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;

public class SignUp extends AppCompatActivity {

    //public static final String URL_BASE = "http://localhost:8080";
    public String URL_BASE;
    public String URL_SIGNUP;

    Context ctx = this;
    private RequestQueue queue = null;
    private JSONObject httpResponse = null;

    private EditText UsernameView;
    private EditText PasswordView;

    String success;
    String error;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        URL_BASE = getString(R.string.serverIP);
        URL_SIGNUP = URL_BASE + "/signup";

        UsernameView = findViewById(R.id.username_signup);
        PasswordView = findViewById(R.id.password_signup);

        Button SignUpButton = findViewById(R.id.sign_up_button);
        SignUpButton.setOnClickListener(this::signUp);
    }

    private void httpRequest(String username, String password) {
        android.util.Log.d("debug", "Starting POST request to URL " + URL_SIGNUP);
        createHTTPQueue();
        HashMap<String, String> mapRequest = new HashMap<>();
        mapRequest.put("username", username);
        mapRequest.put("password", password);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, URL_SIGNUP, new JSONObject(mapRequest),
                httpResponse -> {
                    try {
                        setHTTPResponse(httpResponse);
                        android.util.Log.d("debug", httpResponse.toString());
                        if(httpResponse.has("error")) {
                            error = httpResponse.getString("error");
                            android.util.Log.d("debug", "Error");
                            android.util.Log.d("debug", error);
                            Toast.makeText(ctx, error, Toast.LENGTH_SHORT).show();
                        }
                        else if(httpResponse.has("success")) {
                            success = httpResponse.getString("success");
                            android.util.Log.d("debug", "Success");
                            android.util.Log.d("debug", success);
                            Toast.makeText(ctx, success, Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(ctx, SignIn.class);
                            startActivity(intent);
                        }
                        else {
                            Toast.makeText(ctx, "No adequate response received", Toast.LENGTH_SHORT).show();
                            throw new Exception("No adequate response received", new Exception());
                        }
                    } catch (Exception e) {
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

    private void signUp(View view) {
        // Reset errors.
        UsernameView.setError(null);
        PasswordView.setError(null);

        httpRequest(UsernameView.getText().toString(), PasswordView.getText().toString());
    }

    private void setHTTPResponse(JSONObject json) {
        this.httpResponse = json;
    }

    private void cleanHTTPResponse() {
        success = null;
        error = null;
        this.httpResponse = null;
        android.util.Log.d("debug", "Cleaned " + new Date().getTime());
    }

    private void createHTTPQueue() {
        if(this.queue == null) {
            this.queue = Volley.newRequestQueue(ctx);
        }
    }
}
