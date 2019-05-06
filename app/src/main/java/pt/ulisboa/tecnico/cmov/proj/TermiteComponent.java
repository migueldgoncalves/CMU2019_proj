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

public class TermiteComponent implements SimWifiP2pManager.PeerListListener, SimWifiP2pManager.GroupInfoListener {

    public static final String TAG = "msgsender";
    public static final String REQUEST_USERNAME = "REQUEST_USERNAME";
    public String virtualIP = "";
    private SimWifiP2pManager mManager = null;
    private SimWifiP2pManager.Channel mChannel = null;
    private Messenger mService = null;
    private SimWifiP2pSocketServer mSrvSocket = null;
    private SimWifiP2pSocket mCliSocket = null;
    private SimWifiP2pBroadcastReceiver mReceiver = null;
    private ServiceConnection mConnection = null;
    private AppCompatActivity activity = null;
    private HashMap<String, String> userMap = new HashMap<>();

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
        //TODO: return Username!
        return "User";
    }

    private void sendText() {
        String catalog = getCatalog();
        new SendCommTask().executeOnExecutor(
                AsyncTask.THREAD_POOL_EXECUTOR,
                catalog);
    }

    private void sendUsername() {
        String username = getUsername();
        new SendCommTask().executeOnExecutor(
                AsyncTask.THREAD_POOL_EXECUTOR,
                username);
    }

    private void requestUsername(String ipAddress) {
        new RequestUsernameTask().executeOnExecutor(
                AsyncTask.THREAD_POOL_EXECUTOR,
                ipAddress);
    }

    private void sendMessage(String message, String ipAddress) throws UnknownHostException, IOException {
        if (mCliSocket == null) mCliSocket = new SimWifiP2pSocket(ipAddress, 10001);

        mCliSocket.getOutputStream().write(message.getBytes());
        BufferedReader sockIn = new BufferedReader(
                new InputStreamReader(mCliSocket.getInputStream()));
        sockIn.readLine();
        mCliSocket.close();
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
                            if (result[0].equals(REQUEST_USERNAME)) {
                                sendMessage(getUsername(), result[1]);
                            }
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

        @Override
        protected void onProgressUpdate(String... values) {
        }
    }

    public class RequestUsernameTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                String message = REQUEST_USERNAME + " " + virtualIP + "\n";
                sendMessage(message, params[0]);
            } catch (UnknownHostException e) {
                return "Unknown Host:" + e.getMessage();
            } catch (IOException e) {
                return "IO error:" + e.getMessage();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {

        }
    }

    public class SendCommTask extends AsyncTask<String, String, Void> {

        @Override
        protected Void doInBackground(String... msg) {
            try {
                mCliSocket.getOutputStream().write((msg[0] + "\n").getBytes());
                BufferedReader sockIn = new BufferedReader(
                        new InputStreamReader(mCliSocket.getInputStream()));
                sockIn.readLine();
                mCliSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //mCliSocket = null;
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
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
                    requestUsername(device.getVirtIp());
                }
            }
        }
    }
}
