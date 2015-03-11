package aloksharma.ufl.edu.athome;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseObject;

/**
 * Created by Alok on 3/10/2015.
 */
public class App extends Application {
    ParseObject userObject;
    @Override
    public void onCreate() {
        super.onCreate();

        Parse.enableLocalDatastore(this);
        Parse.initialize(this, "KwiPstN87loO5vCbMsaixTkQLu6KWIYjAV9eUYrF", "EHKdb0bCDnlGuecbUfkMnxizZBMRdrZ6ntrdzLrV");
    }

    public void setUserObject(ParseObject userObject){
        this.userObject = userObject;
    }

    public ParseObject getUserObject(){
        return userObject;
    }
}