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
import android.view.Gravity;
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
                requestToServer(getApplicationContext(), ServerAccess.ServerAction.GET_FRIENDS_HOME);
            }
        });

        IntentFilter serverBroadcastFilter = new IntentFilter("server_response");
        serverBroadcastFilter.addCategory(Intent.CATEGORY_DEFAULT);
        serverBroadcastReceiver = new ServerBroadcastReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(serverBroadcastReceiver, serverBroadcastFilter);

    }

    @Override
    protected void onResume() {
        super.onResume();
        ParseAnalytics.trackAppOpenedInBackground(getIntent()); //Parse analytics for app opens
        requestToServer(getApplicationContext(), ServerAccess.ServerAction.GET_FRIENDS_HOME); //get list of friends who are home
        wifiChecker.checkWifiHome(this); //report to server if I am home.
    }

    public void changeText(ArrayList<AtHomeUser> friendsHome){
        atHomeUsersLayout.removeAllViews();
        RelativeLayout mainContainer = (RelativeLayout)findViewById(R.id.mainContainer);

        if(sharedPreferences.getBoolean("invisible", false)){
            mainText.setText("Yay!\nyou're invisible!");
            mainContainer.setBackgroundColor(getResources().getColor(R.color.bg_main_invisible));
            TextView tv = new TextView(this);
            tv.setGravity(Gravity.CENTER);
            tv.setText("They can't see you, you can't see them.");
            atHomeUsersLayout.addView(tv);

        }else{
            mainContainer.setBackgroundColor(getResources().getColor(R.color.bg_main_normal));
            if(sharedPreferences.getString("home_wifi_id", null) == null){
                mainText.setText("Pssst,\nwhat's your\nhome WiFi?");
                TextView tv = new TextView(this);
                tv.setGravity(Gravity.CENTER);
                tv.setText("Set your home WiFi from the settings option above.");
                atHomeUsersLayout.addView(tv);
            }else if(friendsHome == null){
                //Current user is the only one on this wifi.
                mainText.setText("You are\nthe 1st one\non this WiFi");
                mainContainer.setBackgroundColor(getResources().getColor(R.color.bg_main_invisible));
                TextView tv = new TextView(this);
                tv.setGravity(Gravity.CENTER);
                tv.setText("Share this app with others in your home. No one else found on this WiFi for now.");
                atHomeUsersLayout.addView(tv);
            }else if(friendsHome.size() == 0){
                mainText.setText("Nope,\nno one\nis home");
            }else if (friendsHome.size() == 1){
                mainText.setText("Yep,\n1 person\nis home");
            }else{
                mainText.setText("Yep,\n" + friendsHome.size() + " people\nare home");
            }

            if(friendsHome != null) {
                for (int i = 0; i < friendsHome.size(); i++) {
                    View homeUserView = getLayoutInflater().inflate(R.layout.component_users, atHomeUsersLayout, false);
                    atHomeUsersLayout.addView(homeUserView);
                    TextView homeUserName = (TextView) homeUserView.findViewById(R.id.atHomeUserText);
                    homeUserName.setText(friendsHome.get(i).getFirstName());
                }
            }
        }
    }

    /*
    Called by the button onClicks in activity_main.xml
     */
    public void addFragment(final View v)
    {
        fragUp = true;
        circularFragment = CircularRevealingFragment.newInstance((int) x, (int) y);
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

    private static void requestToServer(Context context, ServerAccess.ServerAction serverAction){
        Intent serverIntent = new Intent(context, ServerAccess.class);
        serverIntent.putExtra("server_action", serverAction.toString());
        context.startService(serverIntent);
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

    public static class ChangeWifiDialogFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            final WifiChangeReceiver wifiChecker = new WifiChangeReceiver();

            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            String old_wifi = sharedPreferences.getString("home_wifi_name", null);
            final String new_wifi = wifiChecker.getWifiName(getActivity());
            final String new_wifi_id = wifiChecker.getWifiID(getActivity());

            String dialog_message;
            String dialog_cancel;

            if(old_wifi == null){
                dialog_message = "Set " + new_wifi + " as your home Wifi?";
                dialog_cancel = "Cancel";
                //TODO: Important- Using same code here and in the else method. Need to restructure the if clauses.
                builder.setPositiveButton("Change", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        TextView currentWifiText = (TextView)getActivity().findViewById(R.id.currentWifi);
                        currentWifiText.setText(new_wifi);
                        sharedPreferences.edit().putString("home_wifi_name", new_wifi).commit();
                        sharedPreferences.edit().putString("home_wifi_id", new_wifi_id).commit();

                        Toast.makeText(getActivity(), "Changed your home wifi to " + new_wifi, Toast.LENGTH_SHORT).show();
                        wifiChecker.checkWifiHome(getActivity());
                        requestToServer(getActivity(), ServerAccess.ServerAction.SET_WIFI);
                        requestToServer(getActivity(), ServerAccess.ServerAction.GET_FRIENDS_HOME);
                    }
                });
            }else if(old_wifi.equals(new_wifi)){
                dialog_message = "Your home wifi is already set to " + old_wifi;
                dialog_cancel = "Ok";
            }else{
                dialog_message = "Change your home wifi from " + old_wifi + " to " + new_wifi + "?";
                dialog_cancel = "Cancel";

                builder.setPositiveButton("Change", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        TextView currentWifiText = (TextView)getActivity().findViewById(R.id.currentWifi);
                        currentWifiText.setText(new_wifi);
                        sharedPreferences.edit().putString("home_wifi_name", new_wifi).commit();
                        sharedPreferences.edit().putString("home_wifi_id", new_wifi_id).commit();

                        Toast.makeText(getActivity(), "Changed your home wifi to " + new_wifi, Toast.LENGTH_SHORT).show();
                        wifiChecker.checkWifiHome(getActivity());
                        requestToServer(getActivity(), ServerAccess.ServerAction.SET_WIFI);
                        requestToServer(getActivity(), ServerAccess.ServerAction.GET_FRIENDS_HOME);
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
