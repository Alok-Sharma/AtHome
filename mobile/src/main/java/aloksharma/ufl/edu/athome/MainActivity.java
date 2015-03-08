package aloksharma.ufl.edu.athome;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;


public class MainActivity extends Activity {

    ServerAccess server;
    private Button button1;
    private CircularRevealingFragment mfragment;
    private float x,y;
    private Boolean fragUp = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button1 = (Button)findViewById(R.id.addButton1);

        button1.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                x = v.getLeft() + event.getX();
                y = v.getTop() + event.getY();
                return false;
            }
        });


        server = new ServerAccess(this);
//        server.getFriends();
        server.putUser();
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
