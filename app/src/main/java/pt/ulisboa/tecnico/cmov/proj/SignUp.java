package pt.ulisboa.tecnico.cmov.proj;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;

public class SignUp extends AppCompatActivity {

    public static final String URL_BASE = "http://192.168.42.51:8080";
    public static final String URL_SIGNUP = URL_BASE + "/signup";

    Context ctx = this;
    private RequestQueue queue = null;
    private JSONObject httpResponse = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        //try {
        android.util.Log.d("debug", "Real beginning " + new Date().getTime());
            httpPost("username", "password");
        //while(httpResponse==null) {if(httpResponse!=null) break;}
            android.util.Log.d("debug", "All began at " + new Date().getTime());
            //android.util.Log.d("debug", httpResponse.getString("success"));
        //JSONArray array = httpResponse.getJSONArray("");
            //AppResponse response = (AppResponse) array.get(0);
        //} catch (JSONException e) {
        //    android.util.Log.d("debug", e.getStackTrace().toString());
        //}
        android.util.Log.d("debug", "Start cleaning " + new Date().getTime());
        cleanHTTPResponse();
    }

    private void httpGet() {
// ...

// Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        String url ="http://www.google.com";

// Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, URL_BASE,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        android.util.Log.d("debug","Response is: " + response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                android.util.Log.d("debug","You failed");
            }
        });

// Add the request to the RequestQueue.
        queue.add(stringRequest);
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

    private void httpPost(String username, String password) {
        android.util.Log.d("debug", "Iniciando pedido POST al URL " + URL_SIGNUP);
        createHTTPQueue();
        HashMap<String, String> mapRequest = new HashMap();
        mapRequest.put("username", "username5");
        mapRequest.put("password", "password");
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, URL_SIGNUP, new JSONObject(mapRequest),
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject httpResponse) {
                        setHTTPResponse(httpResponse);
                        android.util.Log.d("debug", httpResponse.toString());
                        android.util.Log.d("debug", "This should not be last: " + new Date().getTime());
                        try {
                            android.util.Log.d("debug", httpResponse.getString("success"));
                            android.util.Log.d("debug", httpResponse.getString("error"));
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
        android.util.Log.d("debug", "Pedido POST ejecutado, su respuesta es " + httpResponse);
        android.util.Log.d("debug", "This is the real end: " + new Date().getTime());
    }
}
