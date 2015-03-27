package aloksharma.ufl.edu.athome;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.parse.ParseAnalytics;

import java.util.ArrayList;

import mbanje.kurt.fabbutton.FabButton;


public class MainActivity extends Activity {

    private TextView mainText;
    private CircularRevealingFragment circularFragment;
    private float x,y;
    private Boolean fragUp = false;
    private ServerBroadcastReceiver serverBroadcastReceiver;
    private Intent serverIntent;
    private LinearLayout atHomeUsersLayout;
    private WifiChangeReceiver wifiChecker;
    FabButton indeterminate;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor prefEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        wifiChecker = new WifiChangeReceiver();
        ImageButton button1;

        button1 = (ImageButton)findViewById(R.id.addButton1);
        indeterminate = (FabButton) findViewById(R.id.indeterminate);
        mainText = (TextView)findViewById(R.id.mainText);
        atHomeUsersLayout = (LinearLayout)findViewById(R.id.atHomeUsers);
        button1.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                x = v.getLeft() + event.getX();
                y = v.getTop() + event.getY();
                return false;
            }
        });

        indeterminate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                indeterminate.showProgress(true);
                requestToServer(ServerAccess.ServerAction.GET_FRIENDS_HOME);
            }
        });

        serverIntent = new Intent(getApplicationContext(), ServerAccess.class);
        IntentFilter serverBroadcastFilter = new IntentFilter("server_response");
        serverBroadcastFilter.addCategory(Intent.CATEGORY_DEFAULT);
        serverBroadcastReceiver = new ServerBroadcastReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(serverBroadcastReceiver, serverBroadcastFilter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ParseAnalytics.trackAppOpenedInBackground(getIntent()); //Parse analytics for app opens
        requestToServer(ServerAccess.ServerAction.GET_FRIENDS_HOME); //get list of friends who are home
        wifiChecker.checkWifiHome(this); //report to server if I am home.
    }

    public void changeText(ArrayList<AtHomeUser> friendsHome){
        atHomeUsersLayout.removeAllViews();
        int numAtHome = friendsHome.size();
        if(numAtHome == 0){
            mainText.setText("Nope,\nno one\nis home.");
        }else if (numAtHome == 1){
            mainText.setText("Yep,\n1 person\nis home.");
        }else{
            mainText.setText("Yep,\n" + numAtHome + " people\n are home.");
        }

        for(int i = 0; i < numAtHome; i++){
//            TextView tv = new TextView(this);
            View homeUserView = getLayoutInflater().inflate(R.layout.component_users, atHomeUsersLayout, false);
//            tv.setGravity(Gravity.CENTER);
//            tv.setPadding(5,0,5,0);
//            tv.setText(friendsHome.get(i).getFirstName());
//            atHomeUsersLayout.addView(tv);
            atHomeUsersLayout.addView(homeUserView);
            TextView homeUserName = (TextView)homeUserView.findViewById(R.id.atHomeUserText);
            homeUserName.setText(friendsHome.get(i).getFirstName());
        }
    }

    /*
    Called by the button onClicks in activity_main.xml
     */
    public void addFragment(final View v)
    {
        fragUp = true;
        int fragmentColor = 0xFFC33C54;
        circularFragment = CircularRevealingFragment.newInstance((int) x, (int) y, fragmentColor);
        getFragmentManager().beginTransaction().add(android.R.id.content, circularFragment).addToBackStack(null).commit();
    }

    @Override
    public void onBackPressed(){
        if(fragUp){
            removeFragment(circularFragment.getView());
        }else{
            super.onBackPressed();
        }
    }

    /*
    Called by the back button in fragment_main.xml
     */
    public void removeFragment(View v){
        fragUp = false;
        circularFragment.removeYourself();
    }

    /*
    Called by the Wifi button in the fragment_main.xml
     */
    public void setWifi(View v){
        circularFragment.setWifi(this, (Button)v);
    }

    private void requestToServer(ServerAccess.ServerAction serverAction){
        serverIntent.putExtra("server_action", serverAction.toString());
        getApplicationContext().startService(serverIntent);
    }

    private class ServerBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getStringExtra("server_action");
            if(action.equals(ServerAccess.ServerAction.GET_FRIENDS.toString())){
                Log.d("guitar", "activity received friends: " + intent.getStringArrayListExtra("data"));
            }else if(action.equals(ServerAccess.ServerAction.GET_FRIENDS_HOME.toString())){
                ArrayList<AtHomeUser> friendsHome = intent.getParcelableArrayListExtra("data");
                changeText(friendsHome);
                indeterminate.showProgress(false);
            }else if(action.equals(ServerAccess.ServerAction.GET_USER.toString())){

            }
        }
    }
}
