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
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.util.HashMap;

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
    public static final String REQUEST_USERNAME = "REQUEST_USERNAME";
    public static final String RETURN_USERNAME = "RETURN_USERNAME";
    public static final String CATALOG = "CATALOG";

    public String virtualIP = "";
    private SimWifiP2pManager mManager = null;
    private SimWifiP2pManager.Channel mChannel = null;
    private Messenger mService = null;
    private SimWifiP2pSocketServer mSrvSocket = null;
    private SimWifiP2pBroadcastReceiver mReceiver = null;
    private ServiceConnection mConnection = null;
    private AppCompatActivity activity = null;
    private HashMap<String, String> usernameMap = new HashMap<>();
    private HashMap<String, SimWifiP2pSocket> cliSocketMap = new HashMap<>();

    public TermiteComponent(AppCompatActivity activity, Context context, Looper looper) {
        this.activity = activity;
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

    private String getCatalog() {
        //TODO: return Catalog content
        return "Catalog";
    }

    private String getUsername() {
        Peer2PhotoApp app = (Peer2PhotoApp)activity.getApplication();
        return (app != null) ? app.getUsername() : "User";
    }

    private void requestUsername(String destinationIpAddress) {
        String message = REQUEST_USERNAME + " " + virtualIP +  "\n";
        new SendTask().executeOnExecutor(
                AsyncTask.THREAD_POOL_EXECUTOR,
                message, destinationIpAddress);
    }

    private void sendMessage(String message, String ipAddress) throws IOException {
        if (!cliSocketMap.containsKey(ipAddress)) cliSocketMap.put(ipAddress, new SimWifiP2pSocket(ipAddress, 10001));

        SimWifiP2pSocket mCliSocket = cliSocketMap.get(ipAddress);
        mCliSocket.getOutputStream().write(message.getBytes());
        BufferedReader sockIn = new BufferedReader(
                new InputStreamReader(mCliSocket.getInputStream()));
        //TODO: Necessario??
        //sockIn.readLine();
        //TODO: Get virtual IP directly??
        mCliSocket.close();
    }

    private void processRequest(String[] request) throws IOException {
        if (request[0].equals(REQUEST_USERNAME)) {
            sendMessage(RETURN_USERNAME + " " + getUsername() + " " + virtualIP, request[1]);
        }
        else if (request[0].equals(RETURN_USERNAME)) {
            usernameMap.put(request[2], request[1]);
            boolean requestCompleted = true;
            for (String username : usernameMap.values()) {
                if (username.equals("")) {
                    requestCompleted = false;
                    break;
                }
            }
            if (requestCompleted) {
                sendCatalogs();
            }
        }
    }

    private void sendCatalogs() {
        for (String ipAddress : usernameMap.keySet()) {
            sendCatalog(ipAddress);
        }
    }

    private void sendCatalog(String destinationIpAddress) {
        String message = CATALOG + " " + virtualIP + " " + destinationIpAddress +  "\n";
        new SendTask().executeOnExecutor(
                AsyncTask.THREAD_POOL_EXECUTOR,
                message, destinationIpAddress);
    }

    /*
     * Asynctasks implementing message exchange
     */

    public class IncommingCommTask extends AsyncTask<Void, String, Void> {

        @Override
        protected Void doInBackground(Void... params) {

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
                        String[] result = st.split(" ");
                        if (result.length > 0) {
                            processRequest(result);
                        }
                        publishProgress(st);
                        sock.getOutputStream().write(("\n").getBytes());
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

        if (peers.getDeviceList().size() > 0) {
            /*
            for (SimWifiP2pDevice device : peers.getDeviceList()) {

            }
            */
        }
    }

    @Override
    public void onGroupInfoAvailable(SimWifiP2pDeviceList devices,
                                     SimWifiP2pInfo groupInfo) {

        if (groupInfo.getDevicesInNetwork().size() > 0) {
            for (SimWifiP2pDevice device : devices.getDeviceList()) {
                if (device.deviceName.equals(groupInfo.getDeviceName())) {
                    virtualIP = device.getVirtIp();
                    break;
                }
            }
            for (SimWifiP2pDevice device : devices.getDeviceList()) {
                if (!device.deviceName.equals(groupInfo.getDeviceName())) {
                    usernameMap.put(device.getVirtIp(), "");
                    requestUsername(device.getVirtIp());
                }
            }
        }
    }
}
