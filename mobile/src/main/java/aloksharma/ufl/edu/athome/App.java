package aloksharma.ufl.edu.athome;

import android.app.Application;
import android.content.Context;

import com.parse.Parse;

/**
 * Created by Alok on 3/10/2015.
 */
public class App extends Application {
    private static Context mContext;
    @Override
    public void onCreate() {
        super.onCreate();
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, "KwiPstN87loO5vCbMsaixTkQLu6KWIYjAV9eUYrF", "EHKdb0bCDnlGuecbUfkMnxizZBMRdrZ6ntrdzLrV");
        mContext = getApplicationContext();
    }

    public static Context getContext() {
        return mContext;
    }
}