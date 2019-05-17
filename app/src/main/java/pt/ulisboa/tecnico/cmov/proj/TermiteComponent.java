package pt.ulisboa.tecnico.cmov.proj;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.Looper;
import android.os.Messenger;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
import pt.ulisboa.tecnico.cmov.proj.Data.BitmapDataObject;
import pt.ulisboa.tecnico.cmov.proj.Data.Peer2PhotoApp;

public class TermiteComponent implements SimWifiP2pManager.PeerListListener, SimWifiP2pManager.GroupInfoListener {

    public static final String TAG = "msgsender";
    public static final String SEND_USERNAME = "SEND_USERNAME";
    public static final String CATALOG = "CATALOG";
    public static final String PHOTO = "PHOTO";
    public static final String PATH_SPLITTER = ";";

    ObjectOutputStream imageOutput;

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
        return (app != null) ? app.getUsername() : "null";
    }

    private void sendCatalogs() {
        try {
            for (Map.Entry<String, ArrayList<String>> albumEntry : albumName_User_Map.entrySet()) {
                String albumName = albumEntry.getKey();
                File localPhotosFile = new File(context.getFilesDir().getPath() + "/" + albumName + "/" + albumName + "_LOCAL.txt");
                if (!localPhotosFile.isFile()) continue;
                List<String> contents = FileUtils.readLines(localPhotosFile);
                for (String user : albumEntry.getValue()) {
                    for (Map.Entry<String, String> userEntry : ip_Username_Map.entrySet()) {
                        if (userEntry.getValue().equals(user)) {
                            sendPhoto(contents.get(0), albumName, userEntry.getKey());
                            //sendCatalog(albumEntry.getKey(), contents, userEntry.getKey());
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
        new SendUsername().executeOnExecutor(
                AsyncTask.THREAD_POOL_EXECUTOR,
                getUsername(), virtualIP, destinationIpAddress);
    }

    private void sendCatalog(String albumName, List<String> catalogLines, String destinationIpAddress) {
        String catalogContent = "";
        for (String catalogLine : catalogLines) catalogContent += (catalogLine + ";");
        //String message = CATALOG + MESSAGE_SPLITTER + albumName + MESSAGE_SPLITTER + virtualIP + MESSAGE_SPLITTER + catalogContent + "\n";

        /*
        new SendTask().executeOnExecutor(
                AsyncTask.THREAD_POOL_EXECUTOR,
                message, destinationIpAddress);
        */
    }

    private void sendPhoto(String filePath, String albumName, String destinationIpAddress) {
        try {
            new SendPhoto().executeOnExecutor(
                    AsyncTask.THREAD_POOL_EXECUTOR,
                    filePath, albumName, destinationIpAddress);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processUsername(String username, String ipAddress) {
        ip_Username_Map.put(ipAddress, username);
        boolean requestCompleted = true;
        for (String user : ip_Username_Map.values()) {
            if (user.equals("")) {
                requestCompleted = false;
                break;
            }
        }
        if (requestCompleted) {
            sendCatalogs();
        }
    }

    private void processCatalog(String albumName, String ownerIp, String catalog) {
        String[] paths = catalog.split(PATH_SPLITTER);
        String username = ip_Username_Map.get(ownerIp);
        //user_Photo_Map.put(albumName+ALBUM_USER_MAP_SPLITTER+username, paths);

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

    private void processPhoto(byte[] photoBytes, String albumName) {
        try {
            Bitmap photo = BitmapFactory.decodeByteArray(photoBytes, 0, photoBytes.length);
            File photoDir = new File(context.getFilesDir().getPath() + "/" + albumName);
            if (!photoDir.mkdir()) {
                Log.d("debug", "Failed to create photo directory!");
            }
            File newPhoto = new File(context.getFilesDir().getPath() + "/" + albumName + "/Photo.png");
            if (!newPhoto.createNewFile()) {
                Log.d("debug", "Failed to create new photo file!");
            }
            FileOutputStream out = new FileOutputStream(context.getFilesDir().getPath() + "/" + albumName + "/Photo.png");
            photo.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
        }
        catch (IOException e) {
            e.printStackTrace();
        }
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
                    while (true) {
                        SimWifiP2pSocket sock = mSrvSocket.accept();
                        BufferedReader sockIn = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                        String messageType = sockIn.readLine();
                        sock.getOutputStream().write(("\n").getBytes());
                        android.util.Log.d("debug", "Received: " + messageType);

                        if (messageType.equals(SEND_USERNAME)) {
                            processUsername(sockIn.readLine(), sockIn.readLine());
                        }
                        else if (messageType.equals(CATALOG)) {

                        }
                        else if (messageType.equals(PHOTO)) {
                            String albumName = sockIn.readLine();
                            String photoString = sockIn.readLine();
                            sock.getOutputStream().write(("\n").getBytes());
                            byte[] bytes = new Gson().fromJson(photoString, byte[].class);
                            processPhoto(bytes, albumName);
                        }
                        sock.close();
                    }
                    //publishProgress();
                } catch (IOException e) {
                    Log.d("Error socket:", e.getMessage());
                    break;
                }
            }
            return null;
        }
    }

    public class SendUsername extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                String username = params[0];
                String localIp = params[1];
                String ipAddress = params[2];
                SimWifiP2pSocket mCliSocket = new SimWifiP2pSocket(ipAddress, 10001);

                android.util.Log.d("debug", "Sending username");
                mCliSocket.getOutputStream().write((SEND_USERNAME + "\n").getBytes());
                mCliSocket.getOutputStream().write((username + "\n").getBytes());
                mCliSocket.getOutputStream().write((localIp + "\n").getBytes());
                BufferedReader sockIn = new BufferedReader(
                        new InputStreamReader(mCliSocket.getInputStream()));
                sockIn.readLine();
                android.util.Log.d("debug", "Username Sent");
                mCliSocket.close();
            } catch (UnknownHostException e) {
                return "Unknown Host:" + e.getMessage();
            } catch (IOException e) {
                return "IO error:" + e.getMessage();
            }
            return null;
        }
    }

    public class SendPhoto extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                String photoPath = params[0];
                String albumName = params[1];
                String ipAddress = params[2];
                SimWifiP2pSocket mCliSocket = new SimWifiP2pSocket(ipAddress, 10001);

                Bitmap photo = BitmapFactory.decodeFile(photoPath);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                photo.compress(Bitmap.CompressFormat.PNG, 100, stream);
                BitmapDataObject bitmapDataObject = new BitmapDataObject();
                bitmapDataObject.imageByteArray = stream.toByteArray();
                String serialized = new Gson().toJson(stream.toByteArray());

                android.util.Log.d("debug", "Sending photo");
                mCliSocket.getOutputStream().write((PHOTO + "\n").getBytes());
                mCliSocket.getOutputStream().write((albumName + "\n").getBytes());
                mCliSocket.getOutputStream().write((serialized + "\n").getBytes());
                BufferedReader sockIn = new BufferedReader(
                        new InputStreamReader(mCliSocket.getInputStream()));
                sockIn.readLine();
                android.util.Log.d("debug", "Photo Sent");
                mCliSocket.close();
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
            //clearData();
            for (SimWifiP2pDevice device : devices.getDeviceList()) {
                if (!device.deviceName.equals(groupInfo.getDeviceName())) {
                    if (!ip_Username_Map.containsKey(device.getVirtIp())) ip_Username_Map.put(device.getVirtIp(), "");
                    sendUsername(device.getVirtIp());
                }
            }
        }
    }
}
