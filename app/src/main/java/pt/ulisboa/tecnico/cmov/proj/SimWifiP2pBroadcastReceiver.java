package pt.ulisboa.tecnico.cmov.proj;

import pt.inesc.termite.wifidirect.SimWifiP2pBroadcast;
import pt.inesc.termite.wifidirect.SimWifiP2pInfo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

public class SimWifiP2pBroadcastReceiver extends BroadcastReceiver {

    private AppCompatActivity mActivity;
    private TermiteComponent termiteComponent;

    public SimWifiP2pBroadcastReceiver(AppCompatActivity activity, TermiteComponent termiteComponent) {
        super();
        this.mActivity = activity;
        this.termiteComponent = termiteComponent;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (SimWifiP2pBroadcast.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {

            // This action is triggered when the Termite service changes state:
            // - creating the service generates the WIFI_P2P_STATE_ENABLED event
            // - destroying the service generates the WIFI_P2P_STATE_DISABLED event

            int state = intent.getIntExtra(SimWifiP2pBroadcast.EXTRA_WIFI_STATE, -1);
            if (state == SimWifiP2pBroadcast.WIFI_P2P_STATE_ENABLED) {
                //Toast.makeText(mActivity, "WiFi Direct enabled", Toast.LENGTH_SHORT).show();
            } else {
                //Toast.makeText(mActivity, "WiFi Direct disabled", Toast.LENGTH_SHORT).show();
            }

        } else if (SimWifiP2pBroadcast.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {

            // Request available peers from the wifi p2p manager. This is an
            // asynchronous call and the calling activity is notified with a
            // callback on PeerListListener.onPeersAvailable()

            //Toast.makeText(mActivity, "Peer list changed", Toast.LENGTH_SHORT).show();

            termiteComponent.updateApplicationLogs("Updated wifi peers.", "success");

        } else if (SimWifiP2pBroadcast.WIFI_P2P_NETWORK_MEMBERSHIP_CHANGED_ACTION.equals(action)) {

            SimWifiP2pInfo ginfo = (SimWifiP2pInfo) intent.getSerializableExtra(
                    SimWifiP2pBroadcast.EXTRA_GROUP_INFO);
            ginfo.print();
            //Toast.makeText(mActivity, "Network membership changed", Toast.LENGTH_SHORT).show();

            termiteComponent.requestPeers();
            termiteComponent.updateApplicationLogs("Updated network membership.", "success");


        } else if (SimWifiP2pBroadcast.WIFI_P2P_GROUP_OWNERSHIP_CHANGED_ACTION.equals(action)) {

            SimWifiP2pInfo ginfo = (SimWifiP2pInfo) intent.getSerializableExtra(
                    SimWifiP2pBroadcast.EXTRA_GROUP_INFO);
            ginfo.print();
            //Toast.makeText(mActivity, "Group ownership changed", Toast.LENGTH_SHORT).show();
            termiteComponent.updateApplicationLogs("Updated network ownership.", "success");
        }
    }
}
