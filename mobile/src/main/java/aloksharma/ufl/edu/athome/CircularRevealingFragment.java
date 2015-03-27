package aloksharma.ufl.edu.athome;

import android.animation.Animator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.wifi.WifiConfiguration;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by Alok on 2/26/2015.
 * The settings fragment. Things to put in:
 *  - Current wifi.
 *  - Invisibility.
 *  - Logout
 *
 */

public class CircularRevealingFragment extends Fragment{
    int cx, cy;
    WifiChangeReceiver wifiChecker;

    public CircularRevealingFragment(){

    }

    public static CircularRevealingFragment newInstance(int centerX, int centerY, int color)
    {
        Bundle args = new Bundle();
        args.putInt("cx", centerX);
        args.putInt("cy", centerY);
        args.putInt("color", color);
        CircularRevealingFragment fragment = new CircularRevealingFragment();
        fragment.setArguments(args);
        return fragment;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        rootView.setBackgroundColor(getArguments().getInt("color"));
        // To run the animation as soon as the view is layout in the view hierarchy we add this
        // listener and removeFragment it
        // as soon as it runs to prevent multiple animations if the view changes bounds
        rootView.addOnLayoutChangeListener(new View.OnLayoutChangeListener()
        {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop,
                                       int oldRight, int oldBottom)
            {
                v.removeOnLayoutChangeListener(this);
                cx = getArguments().getInt("cx");
                cy = getArguments().getInt("cy");
                // get the hypothenuse so the radius is from one corner to the other
                int radius = (int)Math.hypot(right, bottom);

                // TODO: Handle animation for pre lollipop devices.
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Animator reveal = ViewAnimationUtils.createCircularReveal(v, cx, cy, 0, radius);
                    reveal.setInterpolator(new DecelerateInterpolator(1.5f));
                    reveal.setDuration(700);
                    reveal.start();
                }
            }
        });

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(App.getContext());

        Button setWifiButton = (Button)rootView.findViewById(R.id.wifiButton);
        setWifiButton.setText(sharedPreferences.getString("home_wifi", "No Wifi set"));

        TextView nameText = (TextView)rootView.findViewById(R.id.settingsName);
        nameText.setText(sharedPreferences.getString("user_fname", "") + " " +sharedPreferences.getString("user_lname", ""));

        final Switch invisibleSwitch = (Switch)rootView.findViewById(R.id.invisibleSwitch);
        //check for previously set value of invisibility.
        invisibleSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    Log.d("guitar", "ischecked true");
                }else{
                    Log.d("guitar", "ischecked false");
                }
            }
        });

        return rootView;
    }


    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
    }

    /*
    Executes fragment removal animation and removes the fragment from view.
     */
    public void removeYourself(){
        final CircularRevealingFragment mfragment = this;
        Animator unreveal = null;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            unreveal = mfragment.prepareUnrevealAnimator(cx, cy);
        }
        if(unreveal != null) {
            unreveal.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    // removeFragment the fragment only when the animation finishes
                    FragmentManager fragmentManager = getFragmentManager();
                    if(fragmentManager != null){
                        getFragmentManager().popBackStack();
                        getFragmentManager().beginTransaction().remove(mfragment).commit();
                        getFragmentManager().executePendingTransactions(); //Prevents the flashing.
                    }
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                }
            });
            unreveal.start();
        }else{
            FragmentManager fragmentManager = getFragmentManager();
            if(fragmentManager != null){
                getFragmentManager().popBackStack();
                getFragmentManager().beginTransaction().remove(mfragment).commit();
                getFragmentManager().executePendingTransactions(); //Prevents the flashing.
            }
        }
    }


    /**
     * Get the animator to unreveal the circle
     *
     * @param cx center x of the circle (or where the view was touched)
     * @param cy center y of the circle (or where the view was touched)
     * @return Animator object that will be used for the animation
     */
    public Animator prepareUnrevealAnimator(float cx, float cy)
    {

        int radius = getEnclosingCircleRadius(getView(), (int)cx, (int)cy);
        if(radius == -1){
            return null;
        }
        Animator anim = ViewAnimationUtils.createCircularReveal(getView(), (int) cx, (int) cy, radius, 0);
        anim.setInterpolator(new AccelerateInterpolator(1.5f));
        anim.setDuration(700);
        return anim;
    }

    /**
     * To be really accurate we have to start the circle on the furthest corner of the view
     *
     * @param v the view to unreveal
     * @param cx center x of the circle
     * @param cy center y of the circle
     * @return the maximum radius
     */
    private int getEnclosingCircleRadius(View v, int cx, int cy)
    {
        if(v == null){
            return -1;
        }
        int realCenterX = cx + v.getLeft();
        int realCenterY = cy + v.getTop();
        int distanceTopLeft = (int)Math.hypot(realCenterX - v.getLeft(), realCenterY - v.getTop());
        int distanceTopRight = (int)Math.hypot(v.getRight() - realCenterX, realCenterY - v.getTop());
        int distanceBottomLeft = (int)Math.hypot(realCenterX - v.getLeft(), v.getBottom() - realCenterY);
        int distanceBotomRight = (int)Math.hypot(v.getRight() - realCenterX, v.getBottom() - realCenterY);

        int[] distances = new int[] {distanceTopLeft, distanceTopRight, distanceBottomLeft, distanceBotomRight};
        int radius = distances[0];
        for (int i = 1; i < distances.length; i++)
        {
            if (distances[i] > radius)
                radius = distances[i];
        }
        return radius;
    }

    public void setWifi(final Context context, final Button view){
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        wifiChecker = new WifiChangeReceiver();
        HashMap<String, Integer> wifiPriority = new HashMap<>();
        TreeMap<String, Integer> sortedWifi;
        final List<WifiConfiguration> savedWifiList = wifiChecker.getSavedWifiList(context);
        final Iterator<WifiConfiguration> iterator =  savedWifiList.iterator();
        WifiConfiguration tempWifi;
        while(iterator.hasNext()){
            tempWifi = iterator.next();
            wifiPriority.put(tempWifi.SSID, tempWifi.priority);
        }

        sortedWifi = SortByValue(wifiPriority);
        Log.d("guitar", "" + sortedWifi);
        final CharSequence[] sortedWifiKeys = sortedWifi.keySet().toArray(new CharSequence[sortedWifi.size()]);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Select your Home WiFi");
        builder.setItems(sortedWifiKeys, new DialogInterface.OnClickListener() {
            SharedPreferences.Editor prefEditor;
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d("guitar", "Selected wifi: " + sortedWifiKeys[which]);
                prefEditor = sharedPreferences.edit();
                prefEditor.putString("home_wifi", sortedWifiKeys[which].toString());
                prefEditor.commit();
                view.setText("Home WiFi: " + sortedWifiKeys[which].toString());
                wifiChecker.checkWifiHome(context);
            }
        });
        builder.show();
    }

    public static TreeMap<String, Integer> SortByValue(HashMap<String, Integer> wifiPriority) {
        ValueComparator vc =  new ValueComparator(wifiPriority);
        TreeMap<String,Integer> sortedMap = new TreeMap<>(vc);
        sortedMap.putAll(wifiPriority);
        return sortedMap;
    }
}

class ValueComparator implements Comparator<String> {

    Map<String, Integer> map;

    public ValueComparator(Map<String, Integer> base) {
        this.map = base;
    }

    public int compare(String a, String b) {
        if (map.get(a) >= map.get(b)) {
            return -1;
        } else {
            return 1;
        } // returning 0 would merge keys
    }
}

