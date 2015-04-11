package aloksharma.ufl.edu.athome;

import android.app.Application;
import android.content.Context;

import com.facebook.FacebookSdk;
import com.parse.Parse;
import com.parse.ParseCrashReporting;
import com.parse.ParseFacebookUtils;

//import com.parse.Parse;

/**
 * Created by Alok on 3/10/2015.
 */
public class App extends Application {

    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
//    private static final String TWITTER_KEY = "I3Im2n1UEEUu4x7LwigTq3TbY";
//    private static final String TWITTER_SECRET = "gkFkyQbbGgN9gkLWp9kU88xAYNE4dw7lP0dJPz6oP3gnoYS07N";

    private static Context mContext;
    @Override
    public void onCreate() {
        super.onCreate();

        //Setup twitter digits
//        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
//        Fabric.with(this, new Twitter(authConfig), new Digits());

        //setup facebook
        FacebookSdk.sdkInitialize(this);

        // Setup Parse
        ParseCrashReporting.enable(this);
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, "KwiPstN87loO5vCbMsaixTkQLu6KWIYjAV9eUYrF", "EHKdb0bCDnlGuecbUfkMnxizZBMRdrZ6ntrdzLrV");
        ParseFacebookUtils.initialize(this);
        mContext = getApplicationContext();
//        throw new RuntimeException("Test Exception!");
    }

    public static Context getContext() {
        return mContext;
    }
}