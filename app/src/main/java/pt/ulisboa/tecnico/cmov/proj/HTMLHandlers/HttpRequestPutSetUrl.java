package pt.ulisboa.tecnico.cmov.proj.HTMLHandlers;

import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.util.HashMap;

public class HttpRequestPutSetUrl extends HttpRequest {

    public HttpRequestPutSetUrl(Context context) {
        super(context);
    }

    public static void httpRequest(@NonNull String sessionId,@NonNull String username,@NonNull String Sliceurl,@NonNull String albumId, @NonNull Context mContext, @NonNull String url){
        android.util.Log.d("debug", "Starting PUT request to URL " + url);
        createHTTPQueue();
        HashMap<String, String> mapRequest = new HashMap<>();
        mapRequest.put("sessionId", sessionId);
        mapRequest.put("username", username);
        mapRequest.put("URL", Sliceurl);
        mapRequest.put("albumId", albumId);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, url, new JSONObject(mapRequest),
                httpResponse -> {
                    try {
                        setHTTPResponse(httpResponse);
                        android.util.Log.d("debug", httpResponse.toString());
                        if(httpResponse.has("error")) {
                            error = httpResponse.getString("error");
                            android.util.Log.d("debug", "Error");
                            android.util.Log.d("debug", error);
                            Toast.makeText(mContext, error, Toast.LENGTH_SHORT).show();
                        }
                        else if(httpResponse.has("success")) {
                            success = httpResponse.getString("success");
                            android.util.Log.d("debug", "Success");
                            android.util.Log.d("debug", success);
                            Toast.makeText(mContext, success, Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Toast.makeText(mContext, "No adequate response received", Toast.LENGTH_SHORT).show();
                            throw new Exception("No adequate response received", new Exception());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    cleanHTTPResponse();
                }, error -> {
            cleanHTTPResponse();
            android.util.Log.d("debug", "PUT error");
        });
        queue.add(request);
    }
}
