package aloksharma.ufl.edu.athome;

import android.app.Application;
import android.content.Intent;

import com.parse.Parse;
import com.parse.ParseObject;

/**
 * Created by Alok on 3/10/2015.
 */
public class App extends Application {
    ParseObject userObject;
    Intent mServiceIntent;
    @Override
    public void onCreate() {
        super.onCreate();
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, "KwiPstN87loO5vCbMsaixTkQLu6KWIYjAV9eUYrF", "EHKdb0bCDnlGuecbUfkMnxizZBMRdrZ6ntrdzLrV");

        mServiceIntent = new Intent(this, ServerAccess.class);
        mServiceIntent.putExtra("server_action", ServerAccess.ServerAction.GET_FRIENDS.toString());
        this.startService(mServiceIntent);
    }

    public ParseObject getUserObject() {
        return userObject;
    }

    public void setUserObject(ParseObject userObject) {
        this.userObject = userObject;
    }
}