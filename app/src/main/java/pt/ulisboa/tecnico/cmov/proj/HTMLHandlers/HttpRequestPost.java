package pt.ulisboa.tecnico.cmov.proj.HTMLHandlers;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.util.HashMap;

import pt.ulisboa.tecnico.cmov.proj.HomePage;
import pt.ulisboa.tecnico.cmov.proj.SignIn;

import static android.support.v4.content.ContextCompat.startActivity;

public class HttpRequestPost extends HttpRequest {

    public HttpRequestPost(Context context) {
        super(context);
    }

    /*
    In this Method we use the variable number of arguments to our advantage to use this class as the handler for POST requests to the server
    PARAMS[0] --> The URL of the Operation (SignUp or Create Album)
    PARAMS[2 .. 3] --> The Username and Password for the SignUp operation
    PARAMS[2 .. 4] --> The Username, SessionId and AlbumName for the create Album Operation
     */
    public static void httpRequest(String... params) {
        android.util.Log.d("debug", "Starting POST request to URL " + params[0]);
        createHTTPQueue();
        HashMap<String, String> mapRequest = new HashMap<>();
        JsonObjectRequest request;

        switch (params[1]){
            case "/signup":

                mapRequest.put("username", params[2]);
                mapRequest.put("password", params[3]);

                request = new JsonObjectRequest(Request.Method.POST, params[0] + params[1], new JSONObject(mapRequest),
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
                                    startActivity(ctx, intent,null);
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
                break;

            case "/createalbum":
                mapRequest.put("albumName", params[2]);
                mapRequest.put("username", params[3]);
                mapRequest.put("sessionId", params[4]);
                request = new JsonObjectRequest(Request.Method.POST, params[0] + params[1], new JSONObject(mapRequest),
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
                                    String albumId = httpResponse.getString("albumId");
                                    android.util.Log.d("debug", "Success");
                                    android.util.Log.d("debug", success);
                                    Toast.makeText(ctx, success, Toast.LENGTH_SHORT).show();

                                    ((HomePage) ctx).createAlbumInCloud(params[2], albumId);
                                    ((HomePage) ctx).addNewAlbum(params[2]);
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
                break;
                default:
                    System.out.println("NANI??");
                    break;
        }
    }
}
