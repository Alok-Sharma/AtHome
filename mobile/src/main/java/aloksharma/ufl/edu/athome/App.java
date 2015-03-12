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
    Intent serverIntent;
    @Override
    public void onCreate() {
        super.onCreate();
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, "KwiPstN87loO5vCbMsaixTkQLu6KWIYjAV9eUYrF", "EHKdb0bCDnlGuecbUfkMnxizZBMRdrZ6ntrdzLrV");

        serverIntent = new Intent(this, ServerAccess.class);
        serverIntent.putExtra("server_action", ServerAccess.ServerAction.GET_FRIENDS_HOME.toString());
        this.startService(serverIntent);
    }

    public ParseObject getUserObject() {
        return userObject;
    }

    public void setUserObject(ParseObject userObject) {
        this.userObject = userObject;
    }
}