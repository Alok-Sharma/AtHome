package aloksharma.ufl.edu.athome;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.Session;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;

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
        RelativeLayout mainContainer = (RelativeLayout)findViewById(R.id.mainContainer);

        if(sharedPreferences.getBoolean("invisible", false)){
            mainText.setText("Yay!\nyou're invisible!");
            mainContainer.setBackgroundColor(getResources().getColor(R.color.bg_main_invisible));
            TextView tv = new TextView(this);
            tv.setText("They can't see you, you can't see them.");
            atHomeUsersLayout.addView(tv);
        }else{
            mainContainer.setBackgroundColor(getResources().getColor(R.color.bg_main_normal));
            int numAtHome = friendsHome.size();

            if(sharedPreferences.getString("home_wifi_id", null) == null){
                mainText.setText("Pssst,\ntell me your\nhome wifi\nin the settings.");
            }else if(numAtHome == 0){
                mainText.setText("Nope,\nno one\nis home.");
            }else if (numAtHome == 1){
                mainText.setText("Yep,\n1 person\nis home.");
            }else{
                mainText.setText("Yep,\n" + numAtHome + " people\n are home.");
            }

            for(int i = 0; i < numAtHome; i++){
                View homeUserView = getLayoutInflater().inflate(R.layout.component_users, atHomeUsersLayout, false);
                atHomeUsersLayout.addView(homeUserView);
                TextView homeUserName = (TextView)homeUserView.findViewById(R.id.atHomeUserText);
                homeUserName.setText(friendsHome.get(i).getFirstName());
            }
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

        ChangeWifiDialogFragment changeWifiDialogFragment = new ChangeWifiDialogFragment();
        if(wifiChecker.getWifiID(this) != null) {
            changeWifiDialogFragment.show(getFragmentManager(), "guitar");
        }else{
            Toast.makeText(this, "Not connected to a WiFi.", Toast.LENGTH_SHORT).show();
        }
    }

    /*
    Called by the logout button in fragment_main.xml
     */
    public void logout(View v){
        ParseUser.logOut();
        try{
            ParseObject.unpinAll();
        }catch (ParseException e){
            Log.d("guitarError", "unable to unpin all in logout method: " + e.getMessage());
        }
        com.facebook.Session fbs = com.facebook.Session.getActiveSession();
        if (fbs == null) {
            fbs = new Session(this);
            com.facebook.Session.setActiveSession(fbs);
        }
        fbs.closeAndClearTokenInformation();
        sharedPreferences.edit().clear().commit();
        Intent toLoginActivity = new Intent(this, LoginActivity.class);
        startActivity(toLoginActivity);
        finish();
    }

    private void requestToServer(ServerAccess.ServerAction serverAction){
        serverIntent.putExtra("server_action", serverAction.toString());
        getApplicationContext().startService(serverIntent);
    }

    private class ServerBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getStringExtra("server_action");
            if(action.equals(ServerAccess.ServerAction.GET_FRIENDS_HOME.toString())){
                ArrayList<AtHomeUser> friendsHome = intent.getParcelableArrayListExtra("data");
                changeText(friendsHome);
                indeterminate.showProgress(false);
            }
        }
    }

    private class ChangeWifiDialogFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            String old_wifi = sharedPreferences.getString("home_wifi_name", null);
            final String new_wifi = wifiChecker.getWifiName(getActivity());
            String dialog_message;
            String dialog_cancel;

            if(old_wifi.equals(new_wifi)){
                dialog_message = "Your home wifi is already set to " + old_wifi;
                dialog_cancel = "Ok";
            }else{
                dialog_message = "Change your home wifi from " + old_wifi + " to " + new_wifi + "?";
                dialog_cancel = "Cancel";

                builder.setPositiveButton("Change", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        requestToServer(ServerAccess.ServerAction.SET_WIFI); //ALOKIMP
                        wifiChecker.checkWifiHome(getActivity());
                        requestToServer(ServerAccess.ServerAction.GET_FRIENDS_HOME);

                        TextView currentWifiText = (TextView)findViewById(R.id.currentWifi);
                        currentWifiText.setText(new_wifi);
                        sharedPreferences.edit().putString("home_wifi_name", new_wifi).commit();
                        Toast.makeText(getActivity(), "Changed your home wifi to " + new_wifi, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            builder.setMessage(dialog_message);
            builder.setNegativeButton(dialog_cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User cancelled the dialog
                }
            });
            // Create the AlertDialog object and return it
            return builder.create();
        }
    }
}
