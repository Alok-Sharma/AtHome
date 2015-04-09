package aloksharma.ufl.edu.athome;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import io.codetail.animation.SupportAnimator;

/**
 * Created by Alok on 2/26/2015.
 */

public class CircularRevealingFragment extends Fragment{
    int cx, cy;
    WifiChangeReceiver wifiChecker;
    SharedPreferences sharedPreferences;
    ImageView profilePic;
    Intent serverIntent;

    public CircularRevealingFragment(){

    }

    public static CircularRevealingFragment newInstance(int centerX, int centerY)
    {
        Bundle args = new Bundle();
        args.putInt("cx", centerX);
        args.putInt("cy", centerY);
        CircularRevealingFragment fragment = new CircularRevealingFragment();
        fragment.setArguments(args);
        return fragment;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
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
                Log.d("guitarview", "id: " + v.getId() + " " + R.id.rel);
                View myview = getView().findViewById(R.id.rel);
                SupportAnimator reveal = io.codetail.animation.ViewAnimationUtils.createCircularReveal(myview, cx, cy, 0, radius);
                reveal.setInterpolator(new DecelerateInterpolator(1.5f));
                reveal.setDuration(700);
                reveal.start();
            }
        });

        wifiChecker = new WifiChangeReceiver();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(App.getContext());
        TextView currentWifiText = (TextView)rootView.findViewById(R.id.currentWifi);
        currentWifiText.setText(sharedPreferences.getString("home_wifi_name", "No Wifi set"));

        TextView nameText = (TextView)rootView.findViewById(R.id.settingsName);
        nameText.setText(sharedPreferences.getString("user_fname", "") + " " +sharedPreferences.getString("user_lname", ""));

        profilePic = (ImageView)rootView.findViewById(R.id.profilePic);
//        getFacebookProfilePicture();

        serverIntent = new Intent(getActivity(), ServerAccess.class);
        SwitchCompat invisibleSwitch = (SwitchCompat)rootView.findViewById(R.id.invisibleSwitch);
        invisibleSwitch.setChecked(sharedPreferences.getBoolean("invisible", false));
        //check for previously set value of invisibility.
        invisibleSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    Log.d("guitar", "ischecked true");
                    sharedPreferences.edit().putBoolean("invisible", true).commit();
                    requestToServer(ServerAccess.ServerAction.SET_INVISIBLE);
//                    requestToServer(ServerAccess.ServerAction.GET_FRIENDS_HOME);//Why?
                }else{
                    Log.d("guitar", "ischecked false");
                    sharedPreferences.edit().putBoolean("invisible", false).commit();
                    wifiChecker.checkWifiHome(getActivity());
                    requestToServer(ServerAccess.ServerAction.GET_FRIENDS_HOME);
                }
            }
        });

        return rootView;
    }

    private void requestToServer(ServerAccess.ServerAction serverAction){
        serverIntent.putExtra("server_action", serverAction.toString());
        getActivity().startService(serverIntent);
    }


    public Bitmap getFacebookProfilePicture(){
        final String fb_id = sharedPreferences.getString("fb_id", null);
        Log.d("guitar", "fb_id" + sharedPreferences.getString("fb_id", null));

        AsyncTask<Void, Void, Bitmap> taskGetPicture = new AsyncTask<Void, Void, Bitmap>(){
            protected Bitmap doInBackground(Void... p) {
                try {
                    URL imageURL = new URL("https://graph.facebook.com/" + fb_id + "/picture?type=large");
                    Bitmap bitmap = BitmapFactory.decodeStream(imageURL.openConnection().getInputStream());
                    Log.d("guitarfb", "got the image");
                    return bitmap;
                }catch (MalformedURLException e){
                    Log.d("guitar", "malformed fb profile pic get error: " + e.getMessage());
                }catch (IOException e){
                    Log.d("guitar", "IOEx on fb profile pic: " + e.getMessage());
                }
                return null;
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                super.onPostExecute(bitmap);
                profilePic.setImageBitmap(bitmap);
            }
        };
        taskGetPicture.execute();
        return null;
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
        SupportAnimator unreveal = null;
        unreveal = mfragment.prepareUnrevealAnimator(cx, cy);

        if(unreveal != null) {
            unreveal.addListener(new SupportAnimator.AnimatorListener() {
                @Override
                public void onAnimationStart() {
                }

                @Override
                public void onAnimationEnd() {
                    // removeFragment the fragment only when the animation finishes
                    FragmentManager fragmentManager = getFragmentManager();
                    if(fragmentManager != null){
                        getFragmentManager().popBackStack();
                        getFragmentManager().beginTransaction().remove(mfragment).commit();
                        getFragmentManager().executePendingTransactions(); //Prevents the flashing.
                    }
                }

                @Override
                public void onAnimationCancel() {
                }

                @Override
                public void onAnimationRepeat() {
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
    public SupportAnimator prepareUnrevealAnimator(float cx, float cy)
    {

        int radius = getEnclosingCircleRadius(getView(), (int)cx, (int)cy);
        if(radius == -1){
            return null;
        }
        SupportAnimator anim = io.codetail.animation.ViewAnimationUtils.createCircularReveal(getView().findViewById(R.id.rel), (int) cx, (int) cy, radius, 0);
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
}
