package aloksharma.ufl.edu.athome;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;


public class MainActivity extends Activity {

    ServerAccess server;
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

        Button button1;
        final SwipeRefreshLayout swipeLayout;

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

        swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        swipeLayout.setColorSchemeResources(R.color.orange, R.color.blue, R.color.green);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override public void run() {
                        requestToServer(ServerAccess.ServerAction.GET_FRIENDS_HOME);
                        swipeLayout.setRefreshing(false);
                    }
                }, 1000);
            }
        });

        serverIntent = new Intent(getApplicationContext(), ServerAccess.class);
        IntentFilter serverBroadcastFilter = new IntentFilter("server_response");
        serverBroadcastFilter.addCategory(Intent.CATEGORY_DEFAULT);
        serverBroadcastReceiver = new ServerBroadcastReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(serverBroadcastReceiver, serverBroadcastFilter);

        requestToServer(ServerAccess.ServerAction.GET_FRIENDS_HOME);
    }

    public void changeText(ArrayList<AtHomeUser> friendsHome){
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
            }else if(action.equals(ServerAccess.ServerAction.GET_USER.toString())){

            }
        }
    }
}
