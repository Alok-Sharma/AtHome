package aloksharma.ufl.edu.athome;

import android.content.Context;
import android.util.Log;

import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.List;

/**
 * Created by Alok on 2/25/2015.
 * Use this to talk to the backend server.
 */
public class ServerAccess {

    public ServerAccess(Context context) {
        // Enable Local Datastore.
        Parse.enableLocalDatastore(context);
        Parse.initialize(context, "KwiPstN87loO5vCbMsaixTkQLu6KWIYjAV9eUYrF", "EHKdb0bCDnlGuecbUfkMnxizZBMRdrZ6ntrdzLrV");
    }

    public void getTest(){
        ParseQuery<ParseObject> query = ParseQuery.getQuery("AtHome");
        query.whereEqualTo("GroupID", 1);
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> group, ParseException e) {
                ParseObject r1;
                if (e == null) {
                    Log.d("guitar", "Retrieved " + group.size() + " roommates of group 1 " + group);
                    r1 = group.get(0);
                    Log.d("guitar", "Roommte 1:" + r1.getBoolean("Status"));
                } else {
                    Log.d("guitar", "Error: " + e.getMessage());
                }
            }
        });
    }

    public void putTest(String email){
        ParseObject appCloud = new ParseObject("AtHome");
        appCloud.put("Email", email);
        appCloud.put("GroupID", 1);
        appCloud.put("Status", true);

        appCloud.saveInBackground();
    }
}
