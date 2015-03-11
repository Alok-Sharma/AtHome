package aloksharma.ufl.edu.athome;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;


public class MainActivity extends Activity {

    ServerAccess server;
    private Button button1;
    private TextView mainText;
    private CircularRevealingFragment mfragment;
    private float x,y;
    private Boolean fragUp = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button1 = (Button)findViewById(R.id.addButton1);
        mainText = (TextView)findViewById(R.id.mainText);

        button1.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                x = v.getLeft() + event.getX();
                y = v.getTop() + event.getY();
                return false;
            }
        });

        server = new ServerAccess(this);

        Log.d("guitar", "wifi: " + getWifiName(this));
    }

    public String getWifiName(Context context) {
        String bssid = "none";
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        bssid = wifiInfo.getBSSID();
//        if (WifiInfo.getDetailedStateOf(wifiInfo.getSupplicantState()) == NetworkInfo.DetailedState.CONNECTED) {
//            bssid = wifiInfo.getBSSID();
//        }
        return bssid;
    }

    public void changeText(ArrayList<String> friendsHome, ArrayList<String> friendsNotHome){
        int numAtHome = friendsHome.size();
        if(numAtHome == 0){
            mainText.setText("Nope,\nno one\nis home.");
        }else if (numAtHome == 1){
            mainText.setText("Yep,\n1 person\nis home.");
        }else{
            mainText.setText("Yep,\n" + numAtHome + " people\n are home.");
        }
    }

    /*
    Called by the button onClicks in activity_main.xml
     */
    public void addFragment(final View v)
    {
//		int randomColor =
//				Color.argb(255, (int)(Math.random() * 255), (int)(Math.random() * 255), (int)(Math.random() * 255));
        fragUp = true;
        int randomColor = Color.GREEN;
        mfragment = CircularRevealingFragment.newInstance((int) x, (int) y, randomColor);
        getFragmentManager().beginTransaction().add(android.R.id.content, mfragment).addToBackStack(null).commit();
    }

    @Override
    public void onBackPressed(){
        if(fragUp){
            removeFragment(mfragment.getView());
        }else{
            super.onBackPressed();
        }
    }

    /*
    Called by the back button in fragment_main.xml
     */
    public void removeFragment(View v){
        fragUp = false;
        mfragment.removeYourself();
    }
}
