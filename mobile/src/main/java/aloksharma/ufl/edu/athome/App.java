package aloksharma.ufl.edu.athome;

import android.app.Application;

import com.parse.Parse;

/**
 * Created by Alok on 3/10/2015.
 */
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, "KwiPstN87loO5vCbMsaixTkQLu6KWIYjAV9eUYrF", "EHKdb0bCDnlGuecbUfkMnxizZBMRdrZ6ntrdzLrV");

    }
}