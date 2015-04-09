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
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.facebook.Session;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.ArrayList;

import me.alexrs.wavedrawable.WaveDrawable;


public class MainActivity extends Activity {

    private final long WAVE_ANIMATION_DURATION = 700;
    private TextView mainTextLine1, mainTextLine2, mainTextLine3, auxText;
    private CircularRevealingFragment circularFragment;
    private float x,y;
    private Boolean fragUp = false;
    private ServerBroadcastReceiver serverBroadcastReceiver;
    private LinearLayout atHomeUsersLayout;
    private WifiChangeReceiver wifiChecker;
    private SharedPreferences sharedPreferences;
    private WaveDrawable wave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        wifiChecker = new WifiChangeReceiver();
        ImageButton addFragmentButton, refreshButton;
        addFragmentButton = (ImageButton)findViewById(R.id.addButton1);
        refreshButton = (ImageButton) findViewById(R.id.refresh);
        mainTextLine1 = (TextView)findViewById(R.id.mainTextLine1);
        mainTextLine2 = (TextView)findViewById(R.id.mainTextLine2);
        mainTextLine3 = (TextView)findViewById(R.id.mainTextLine3);
        auxText = new TextView(this);
        auxText.setGravity(Gravity.CENTER);
        auxText.setTextColor(getResources().getColor(R.color.text));
        atHomeUsersLayout = (LinearLayout)findViewById(R.id.atHomeUsers);
        addFragmentButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                x = v.getLeft() + event.getX();
                y = v.getTop() + event.getY();
                return false;
            }
        });

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int screenHeight = size.y;
        wave = new WaveDrawable(getResources().getColor(R.color.wave), screenHeight, WAVE_ANIMATION_DURATION); //(color,radius,time)
        wave.setWaveInterpolator(new DecelerateInterpolator());
        refreshButton.setBackgroundDrawable(wave);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!sharedPreferences.getBoolean("invisible", false)){
                    //if not invisible, then get friends home, and do the wave animation.
                    requestToServer(getApplicationContext(), ServerAccess.ServerAction.GET_FRIENDS_HOME);
                    toggleWaveAnimation(true);
                }else{
                    //else do nothing, except for shake text animation.
                    toggleTextAnimation(false);
                }
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
        mainTextLine1.setVisibility(View.GONE);mainTextLine2.setVisibility(View.GONE);mainTextLine3.setVisibility(View.GONE);

        if(sharedPreferences.getBoolean("invisible", false)){
            mainTextLine1.setVisibility(View.VISIBLE);mainTextLine2.setVisibility(View.VISIBLE);mainTextLine3.setVisibility(View.VISIBLE);
            mainTextLine1.setText("Yay!");
            mainTextLine2.setText("you're");
            mainTextLine3.setText("invisible!");
            mainContainer.setBackgroundColor(getResources().getColor(R.color.bg_main_invisible));
            auxText.setText("They can't see you, you can't see them.");
            atHomeUsersLayout.addView(auxText);
            toggleTextAnimation(false);
        }else{
            mainContainer.setBackgroundColor(getResources().getColor(R.color.bg_main_normal));
            if(sharedPreferences.getString("home_wifi_id", null) == null){
                mainTextLine1.setVisibility(View.VISIBLE);mainTextLine2.setVisibility(View.VISIBLE);mainTextLine3.setVisibility(View.VISIBLE);
                mainTextLine1.setText("Pssst,");
                mainTextLine2.setText("what's your");
                mainTextLine3.setText("home WiFi?");
                auxText.setText("Set your home WiFi in the profile menu above.");
                atHomeUsersLayout.addView(auxText);
                toggleTextAnimation(false);
            }else if(friendsHome == null){
                //Current user is the only one on this wifi.
                mainTextLine1.setVisibility(View.VISIBLE);mainTextLine2.setVisibility(View.VISIBLE);mainTextLine3.setVisibility(View.VISIBLE);
                mainTextLine1.setText("You are");
                mainTextLine2.setText("the 1st one");
                mainTextLine3.setText("on this WiFi");
                mainContainer.setBackgroundColor(getResources().getColor(R.color.bg_main_invisible));
                auxText.setText("Share this app with others in your home. No one else found on this WiFi for now.");
                atHomeUsersLayout.addView(auxText);
                toggleTextAnimation(false);
            }else if(friendsHome.size() == 0){
                mainTextLine1.setVisibility(View.VISIBLE);mainTextLine2.setVisibility(View.VISIBLE);mainTextLine3.setVisibility(View.VISIBLE);
                mainTextLine1.setText("Nope,");
                mainTextLine2.setText("no one");
                mainTextLine3.setText("is home");
                toggleTextAnimation(true);
            }else if (friendsHome.size() == 1){
                mainTextLine1.setVisibility(View.VISIBLE);mainTextLine2.setVisibility(View.VISIBLE);mainTextLine3.setVisibility(View.VISIBLE);
                mainTextLine1.setText("Yep,");
                mainTextLine2.setText("1 person");
                mainTextLine3.setText("is home");
                toggleTextAnimation(true);
            }else{
                mainTextLine1.setVisibility(View.VISIBLE);mainTextLine2.setVisibility(View.VISIBLE);mainTextLine3.setVisibility(View.VISIBLE);
                mainTextLine1.setText("Yep,");
                mainTextLine2.setText(friendsHome.size() + " people");
                mainTextLine3.setText("are home");
                toggleTextAnimation(true);
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
        Intent toLoginActivity = new Intent(this, SignInActivity.class);
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
                toggleWaveAnimation(false);
            }else if(action.equals(ServerAccess.ServerAction.SET_INVISIBLE.toString())){
                changeText(null);
            }
        }
    }

    long timeOfStart = 0;
    private void toggleWaveAnimation(Boolean toggle){
        if(!toggle) {
            if (wave != null && wave.isAnimationRunning()) {
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        wave.stopAnimation();
                    }
                }, calculateTimeToStopWave(System.currentTimeMillis(), timeOfStart));

            }
        }else{
            wave.startAnimation();
            timeOfStart = System.currentTimeMillis();
            Log.d("guitarTime", "time of start: " + timeOfStart);
        }
    }

    private void toggleTextAnimation(Boolean type){

        if(!type){
            YoYo.with(Techniques.Shake).duration(400).playOn(mainTextLine1);
            YoYo.with(Techniques.Shake).duration(500).playOn(mainTextLine2);
            YoYo.with(Techniques.Shake).duration(600).playOn(mainTextLine3);
        }else{

            YoYo.with(Techniques.FadeOutUp).duration(400).playOn(mainTextLine1);
            YoYo.with(Techniques.FadeOutUp).duration(500).playOn(mainTextLine2);
            YoYo.with(Techniques.FadeOutUp).duration(600).playOn(mainTextLine3);
            YoYo.with(Techniques.FadeOutUp).duration(700).playOn(atHomeUsersLayout);

            final Handler handler = new Handler();

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    YoYo.with(Techniques.FadeInUp).duration(300).playOn(mainTextLine1);
                }
            }, 500);

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    YoYo.with(Techniques.FadeInUp).duration(300).playOn(mainTextLine2);
                }
            }, 600);

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    YoYo.with(Techniques.FadeInUp).duration(300).playOn(mainTextLine3);
                }
            }, 700);

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    YoYo.with(Techniques.FadeInUp).duration(300).playOn(atHomeUsersLayout);
                }
            }, 700);
        }
    }

    private long calculateTimeToStopWave(long stopTime, long startTime){
        Log.d("guitarTime", "stoptime: " + stopTime + " start time: " + startTime);
        long temp = stopTime - startTime;
        temp = temp % WAVE_ANIMATION_DURATION;
        long result = WAVE_ANIMATION_DURATION - temp;
        Log.d("guitarTime", "result: " + result);
        return result;
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
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
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

                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
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
