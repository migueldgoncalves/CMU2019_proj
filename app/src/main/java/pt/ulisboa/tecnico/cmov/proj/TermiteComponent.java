package pt.ulisboa.tecnico.cmov.proj;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.Looper;
import android.os.Messenger;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pt.inesc.termite.wifidirect.SimWifiP2pBroadcast;
import pt.inesc.termite.wifidirect.SimWifiP2pDevice;
import pt.inesc.termite.wifidirect.SimWifiP2pDeviceList;
import pt.inesc.termite.wifidirect.SimWifiP2pInfo;
import pt.inesc.termite.wifidirect.SimWifiP2pManager;
import pt.inesc.termite.wifidirect.service.SimWifiP2pService;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocket;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocketManager;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocketServer;
import pt.ulisboa.tecnico.cmov.proj.Data.Peer2PhotoApp;

public class TermiteComponent implements SimWifiP2pManager.PeerListListener, SimWifiP2pManager.GroupInfoListener {

    public static final String TAG = "msgsender";
    public static final String SEND_USERNAME = "SEND_USERNAME";
    public static final String CATALOG = "CATALOG";
    public static final String PHOTO = "PHOTO";
    public static final String MESSAGE_SPLITTER = ",";
    public static final String PATH_SPLITTER = ";";
    public static final String ALBUM_USER_MAP_SPLITTER = "º";

    public String virtualIP = "";
    private SimWifiP2pManager mManager = null;
    private SimWifiP2pManager.Channel mChannel = null;
    private Context context = null;
    private Messenger mService = null;
    private SimWifiP2pSocketServer mSrvSocket = null;
    private SimWifiP2pBroadcastReceiver mReceiver = null;
    private ServiceConnection mConnection = null;
    private AppCompatActivity activity = null;
    private HashMap<String, String> ip_Username_Map = new HashMap<>();
    private HashMap<String, SimWifiP2pSocket> ip_Socket_Map = new HashMap<>();
    private HashMap<String, String[]> user_Photo_Map = new HashMap<String, String[]>();
    public HashMap<String, ArrayList<String>> albumName_User_Map = new HashMap<>();
    public HashMap<String, String> albumId_albumName_Map = new HashMap<>();

    public TermiteComponent(AppCompatActivity activity, Context context, Looper looper) {
        this.activity = activity;
        this.context = context;
        createConnection(context, looper);
        initTermite(context);
        beginService(context);
    }

    private void createConnection(Context context, Looper looper) {
        mConnection = new ServiceConnection() {

            @Override
            public void onServiceConnected(ComponentName className, IBinder service) {
                mService = new Messenger(service);
                mManager = new SimWifiP2pManager(mService);
                mChannel = mManager.initialize(context, looper, null);
            }

            @Override
            public void onServiceDisconnected(ComponentName arg0) {
                mService = null;
                mManager = null;
                mChannel = null;
            }
        };
    }

    private void initTermite(Context context) {
        SimWifiP2pSocketManager.Init(context);

        // register broadcast receiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_STATE_CHANGED_ACTION);
        filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_PEERS_CHANGED_ACTION);
        filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_NETWORK_MEMBERSHIP_CHANGED_ACTION);
        filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_GROUP_OWNERSHIP_CHANGED_ACTION);
        mReceiver = new SimWifiP2pBroadcastReceiver(activity, this);
        activity.registerReceiver(mReceiver, filter);
    }

    private void beginService(Context context) {

        Intent intent = new Intent(context, SimWifiP2pService.class);
        activity.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        // spawn the chat server background task
        new IncommingCommTask().executeOnExecutor(
                AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void requestPeers() {
        mManager.requestGroupInfo(mChannel, this);
    }

    private String getUsername() {
        Peer2PhotoApp app = (Peer2PhotoApp)activity.getApplication();
        return (app != null) ? app.getUsername() : "User";
    }

    public static String convertStreamToString(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        reader.close();
        return sb.toString();
    }

    private void sendCatalogs() {
        try {
            for (Map.Entry<String, ArrayList<String>> albumEntry : albumName_User_Map.entrySet()) {
                File localPhotosFile = new File(context.getFilesDir().getPath() + "/" + albumEntry.getKey() + "/" + albumEntry.getKey() + "_LOCAL.txt");
                if (!localPhotosFile.isFile()) continue;
                List<String> contents = FileUtils.readLines(localPhotosFile);
                for (String user : albumEntry.getValue()) {
                    for (Map.Entry<String, String> userEntry : ip_Username_Map.entrySet()) {
                        if (userEntry.getValue().equals(user)) {
                            android.util.Log.d("debug", "SENT CATALOG TO " + user);
                            sendCatalog(albumEntry.getKey(), contents, userEntry.getValue());
                        }
                    }
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendUsername(String destinationIpAddress) {
        String message = SEND_USERNAME + MESSAGE_SPLITTER + getUsername() + MESSAGE_SPLITTER + virtualIP +  "\n";
        new SendTask().executeOnExecutor(
                AsyncTask.THREAD_POOL_EXECUTOR,
                message, destinationIpAddress);
    }

    private void sendCatalog(String albumName, List<String> catalogLines, String destinationIpAddress) {
        String catalogContent = "";
        for (String catalogLine : catalogLines) catalogContent += (catalogLine + ";");
        String message = CATALOG + MESSAGE_SPLITTER + albumName + MESSAGE_SPLITTER + virtualIP + MESSAGE_SPLITTER + catalogContent + "\n";

        new SendTask().executeOnExecutor(
                AsyncTask.THREAD_POOL_EXECUTOR,
                message, destinationIpAddress);
    }

    private void sendPhoto(File photo, String destinationIpAddress) {
        try {
            FileInputStream fin = new FileInputStream(photo);
            String message = SEND_USERNAME + MESSAGE_SPLITTER + photo.getName() + MESSAGE_SPLITTER + convertStreamToString(fin) + "\n";
            new SendTask().executeOnExecutor(
                    AsyncTask.THREAD_POOL_EXECUTOR,
                    message, destinationIpAddress);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendMessage(String message, String ipAddress) throws IOException {
        if (!ip_Socket_Map.containsKey(ipAddress)) ip_Socket_Map.put(ipAddress, new SimWifiP2pSocket(ipAddress, 10001));

        SimWifiP2pSocket mCliSocket = ip_Socket_Map.get(ipAddress);
        android.util.Log.d("debug", "Sent: " + message);
        mCliSocket.getOutputStream().write(message.getBytes());
        BufferedReader sockIn = new BufferedReader(
                new InputStreamReader(mCliSocket.getInputStream()));
        sockIn.readLine();
        mCliSocket.close();
    }

    private void processRequest(String[] request) {
        android.util.Log.d("debug", "PROCESSING REQUEST " + request[0]);
        if (request[0].equals(SEND_USERNAME)) {
            ip_Username_Map.put(request[2], request[1]);
            boolean requestCompleted = true;
            for (String username : ip_Username_Map.values()) {
                if (username.equals("")) {
                    requestCompleted = false;
                    break;
                }
            }
            if (requestCompleted) {
                sendCatalogs();
            }
        }
        else if (request[0].equals(CATALOG)) {
            processCatalog(request[1], request[2], request[3]);
        }
        else if (request[0].equals(PHOTO)) {
            processPhoto(request[1], request[2]);
        }
    }

    private void processCatalog(String albumName, String ownerIp, String catalog) {
        String[] paths = catalog.split(PATH_SPLITTER);
        String username = ip_Username_Map.get(ownerIp);
        user_Photo_Map.put(albumName+ALBUM_USER_MAP_SPLITTER+username, paths);
        Toast.makeText(context, "Received catalog from " + username, Toast.LENGTH_SHORT).show();

        File newUserCatalog = new File(context.getFilesDir().getPath() + "/" + albumName + "/" + "SLICE_" +  username + ".txt");
        if (!newUserCatalog.isFile()) return;

        try {
            //TODO: Fazer log das cenas do Termite???
            Log.d("debug", "Saved " + albumName + "-" + username + " catalog locally.");
            BufferedWriter out = new BufferedWriter(new FileWriter(newUserCatalog, false));
            out.write(catalog + "\n");
            out.flush();
            out.close();
        }
        catch (IOException e) {
            Log.d("debug", "Failed to write catalog:");
            e.printStackTrace();
        }
    }

    private void processPhoto(String fileName, String content) {
        try {
            //TODO: Create new file where??
            File newPhoto = new File(context.getFilesDir().getPath(), fileName);
            FileOutputStream outputStreamWriter = new FileOutputStream(newPhoto);
            outputStreamWriter.write(content.getBytes());
            outputStreamWriter.close();
            //AlbumView.imageScalingAndPosting(filePath);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void clearData() {
        ip_Username_Map.clear();
        ip_Socket_Map.clear();
        user_Photo_Map.clear();
    }

    public void updateApplicationLogs(@NonNull String operation, @NonNull String operationResult){
        String Operation = "OPERATION: " + operation + "\n";
        String timeStamp = "TIMESTAMP: " + new Date().toString() + "\n";
        String result = "RESULT: " + operationResult + "\n";

        ((Peer2PhotoApp)activity.getApplication()).updateLog(Operation + timeStamp + result);

    }

    /*
     * Asynctasks implementing message exchange
     */

    public class IncommingCommTask extends AsyncTask<Void, String, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            android.util.Log.d("debug", "INCOMING!");
            Log.d(TAG, "IncommingCommTask started (" + this.hashCode() + ").");

            try {
                mSrvSocket = new SimWifiP2pSocketServer(
                        10001);
            } catch (IOException e) {
                e.printStackTrace();
            }
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    SimWifiP2pSocket sock = mSrvSocket.accept();
                    try {
                        BufferedReader sockIn = new BufferedReader(
                                new InputStreamReader(sock.getInputStream()));
                        String st = sockIn.readLine();
                        sock.getOutputStream().write(("\n").getBytes());
                        android.util.Log.d("debug", "Received: " + st);
                        String[] result = st.split(MESSAGE_SPLITTER);
                        if (result.length > 0) {
                            processRequest(result);
                        }
                        publishProgress(st);
                    } catch (IOException e) {
                        Log.d("Error reading socket:", e.getMessage());
                    } finally {
                        sock.close();
                    }
                } catch (IOException e) {
                    Log.d("Error socket:", e.getMessage());
                    break;
                    //e.printStackTrace();
                }
            }
            return null;
        }
    }

    public class SendTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                sendMessage(params[0], params[1]);
            } catch (UnknownHostException e) {
                return "Unknown Host:" + e.getMessage();
            } catch (IOException e) {
                return "IO error:" + e.getMessage();
            }
            return null;
        }
    }

    @Override
    public void onPeersAvailable(SimWifiP2pDeviceList peers) {

    }

    @Override
    public void onGroupInfoAvailable(SimWifiP2pDeviceList devices,
                                     SimWifiP2pInfo groupInfo) {

        if (groupInfo.getDevicesInNetwork().size() > 0) {
            if (virtualIP == "") {
                for (SimWifiP2pDevice device : devices.getDeviceList()) {
                    if (device.deviceName.equals(groupInfo.getDeviceName())) {
                        virtualIP = device.getVirtIp();
                        break;
                    }
                }
            }
            clearData();
            for (SimWifiP2pDevice device : devices.getDeviceList()) {
                if (!device.deviceName.equals(groupInfo.getDeviceName())) {
                    if (!ip_Username_Map.containsKey(device.getVirtIp())) ip_Username_Map.put(device.getVirtIp(), "");
                    sendUsername(device.getVirtIp());
                }
            }
        }
    }
}
