package aloksharma.ufl.edu.athome;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
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
    private ServerBroadcastReceiver serverBroadcastReceiver;
    private Intent serverIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //TODO: Register local broadcast listener for updates from serveraccess.
        button1 = (Button)findViewById(R.id.addButton1);
        mainText = (TextView)findViewById(R.id.mainText);
        button1.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                x = v.getLeft() + event.getX();
                y = v.getTop() + event.getY();
//                mServiceIntent = new Intent(getApplicationContext(), ServerAccess.class);
//                mServiceIntent.putExtra("server_action", ServerAccess.ServerAction.ADD_FRIEND.toString());
//                getApplicationContext().startService(mServiceIntent);
                return false;
            }
        });

        IntentFilter serverBroadcastFilter = new IntentFilter("server_response");
        serverBroadcastFilter.addCategory(Intent.CATEGORY_DEFAULT);
        serverBroadcastReceiver = new ServerBroadcastReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(serverBroadcastReceiver, serverBroadcastFilter);
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
        fragUp = true;
        int randomColor = Color.GREEN;
        mfragment = CircularRevealingFragment.newInstance((int) x, (int) y, randomColor);
        getFragmentManager().beginTransaction().add(android.R.id.content, mfragment).addToBackStack(null).commit();

        serverIntent = new Intent(getApplicationContext(), ServerAccess.class);
        serverIntent.putExtra("server_action", ServerAccess.ServerAction.GET_FRIENDS.toString());
        getApplicationContext().startService(serverIntent);
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

    private class ServerBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getStringExtra("server_action");
            if(action.equals(ServerAccess.ServerAction.GET_FRIENDS.toString())){
                Log.d("guitar", "activity received friends: " + intent.getStringArrayListExtra("data"));
            }else if(action.equals(ServerAccess.ServerAction.GET_FRIENDS_HOME.toString())){

            }else if(action.equals(ServerAccess.ServerAction.GET_USER.toString())){

            }
        }
    }
}
