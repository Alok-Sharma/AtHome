package aloksharma.ufl.edu.athome;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

/**
 * Created by Alok on 3/10/2015.
 */
public class WifiChangeReceiver extends BroadcastReceiver {
    String homeWifi = "d8:eb:97:1a:1f:2b";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("guitar", "broadcast received");
        ConnectivityManager connectionManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectionManager.getActiveNetworkInfo(); //TODO activeNetwork can be null
        ServerAccess server = new ServerAccess(context.getApplicationContext());
        if(activeNetwork != null){
            boolean isWiFi = activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
            boolean isConnected = activeNetwork.isConnected();
            Log.d("guitar", "active network not null, type: " + activeNetwork.getType() + " connected: " + isConnected);
            String currentWifi;
            if(isWiFi && isConnected){
                currentWifi = getWifiName(context);
                Log.d("guitar", "broadcast bssid " + currentWifi);
                if(currentWifi.equals(homeWifi)){
                    //at home
                    server.setAtHomeStatus(true);
                }else{
                    //not at home
                    server.setAtHomeStatus(false);
                }
            }else{
                // not connected to wifi.
                server.setAtHomeStatus(false);
            }
        }else{
            //not connected to internet
            Log.d("guitar", "active network null");
            server.setAtHomeStatus(false);
        }
    }

    public String getWifiName(Context context) {
        String bssid = "none";
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
//        bssid = wifiInfo.getSSID(); //gets wifi name. Is not unique.
        bssid = wifiInfo.getBSSID(); //gets MAC address of router. Should be unique.
        return bssid;
    }
}

