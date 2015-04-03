package aloksharma.ufl.edu.athome;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.List;

/**
 * Created by Alok on 3/10/2015.
 */
public class WifiChangeReceiver extends BroadcastReceiver {
    Intent serverIntent;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("guitar", "broadcast received");
        checkWifiHome(context);
    }

    public void setAtHomeStatus(Context context, String status){
        serverIntent = new Intent(context, ServerAccess.class);
        serverIntent.putExtra("server_action", ServerAccess.ServerAction.SET_HOME_STATUS.toString());
        serverIntent.putExtra("server_action_arg", status);
        context.startService(serverIntent);
    }

    public void checkWifiHome(Context context){
        String homeWifi = PreferenceManager.getDefaultSharedPreferences(context).getString("home_wifi_id", ""); //ALOKIMP
        ConnectivityManager connectionManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectionManager.getActiveNetworkInfo();
        Log.d("guitarWifi", "check wifi home: " + homeWifi);

        if(activeNetwork != null){
            boolean isWiFi = activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
            boolean isConnected = activeNetwork.isConnected();
            Log.d("guitarWifi", "active network not null, type: " + activeNetwork.getType() + " connected: " + isConnected);
            String currentWifi;
            if(isWiFi && isConnected){
//                currentWifi = getWifiName(context);
                currentWifi = getWifiID(context); //ALOKIMP
                Log.d("guitarWifi", "current wifi: " + currentWifi);
                if(currentWifi.equals(homeWifi)){
                    //at home
                    setAtHomeStatus(context, ServerAccess.AtHomeStatus.TRUE.toString());
                }else{
                    //not at home
                    setAtHomeStatus(context, ServerAccess.AtHomeStatus.FALSE.toString());
                }
            }else{
                // not connected to wifi.
                Log.d("guitarWifi", "not connected to a wifi");
                setAtHomeStatus(context, ServerAccess.AtHomeStatus.FALSE.toString());
            }
        }else{
            //not connected to internet
            Log.d("guitarWifi", "active network null");
            setAtHomeStatus(context, ServerAccess.AtHomeStatus.FALSE.toString());
        }
    }

    public String getWifiName(Context context) {
        String ssid = "none";
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        ssid = wifiInfo.getSSID(); //gets wifi name. Is not unique.
        return ssid;
    }

    public String getWifiID(Context context){
        String bssid = "none";
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        bssid = wifiInfo.getBSSID();    //gets wifi ID. Is unique.
        return bssid;
    }

    public List<WifiConfiguration> getSavedWifiList(Context context){
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        return wifiManager.getConfiguredNetworks();
    }
}

