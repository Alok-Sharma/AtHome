package aloksharma.ufl.edu.athome;

import android.content.Context;
import android.util.Log;

import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.Arrays;
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
        ParseQuery<ParseObject> atHome = ParseQuery.getQuery("AtHome");
        ParseQuery<ParseObject> friendList = ParseQuery.getQuery("FriendList");
        friendList.whereEqualTo("Email", "aloksharma@ufl.edu");
        friendList.selectKeys(Arrays.asList("Friends"));

        atHome.whereMatchesQuery("Email", friendList);
        atHome.whereEqualTo("Status", true);

        atHome.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> group, ParseException e) {
                ParseObject r1;
                if (e == null) {
                    Log.d("guitar", "line1: " + group.size());
                    r1 = group.get(0);
                    Log.d("guitar", "line2: " + r1.getString("Email"));
                } else {
                    Log.d("guitar", "Error: " + e.getMessage());
                }
            }
        });


//        query2.whereEqualTo("Email", "aloksharma@ufl.edu");
//        query.whereEqualTo("", query2.find);
//        query.findInBackground(new FindCallback<ParseObject>() {
//            public void done(List<ParseObject> group, ParseException e) {
//                ParseObject r1;
//                if (e == null) {
//                    Log.d("guitar", "Retrieved " + group.size() + " roommates of group 1 " + group);
//                    r1 = group.get(0);
//                    Log.d("guitar", "Roommte 1:" + r1.getBoolean("Status"));
//                } else {
//                    Log.d("guitar", "Error: " + e.getMessage());
//                }
//            }
//        });
    }

    public void putTest(){
        ParseObject appCloud = new ParseObject("AtHome");
//        appCloud.put("Email", "aloksharma@ufl.edu");
//        appCloud.put("Status", true);
//        appCloud.put("Email", "stalukdar@ufl.edu");
//        appCloud.put("Status", true);
        appCloud.put("Email", "mrig@ufl.edu");
        appCloud.put("Status", true);

        ParseObject appCloud2 = new ParseObject("FriendList");
        appCloud2.put("Email", "aloksharma@ufl.edu");
        appCloud2.addAllUnique("Friends", Arrays.asList("stalukdar@ufl.edu", "mrig@ufl.edu"));

        appCloud.saveInBackground();
        appCloud2.saveInBackground();
    }
}
