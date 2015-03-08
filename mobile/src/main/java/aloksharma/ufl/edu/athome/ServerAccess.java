package aloksharma.ufl.edu.athome;

import android.content.Context;
import android.util.Log;

import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Alok on 2/25/2015.
 * Use this to talk to the backend server.
 */
public class ServerAccess {

    String userEmail = "test@ufl.edu";
    String userObjectId;
    ParseObject userObject;

    public ServerAccess(Context context) {
        // Enable Local Datastore.
        Parse.enableLocalDatastore(context);
        Parse.initialize(context, "KwiPstN87loO5vCbMsaixTkQLu6KWIYjAV9eUYrF", "EHKdb0bCDnlGuecbUfkMnxizZBMRdrZ6ntrdzLrV");
        userObject = new ParseObject("AtHome");
    }

    /*
        Called by getFriends.
        Values are fetched from server.
     */
    public void getFriendStatus(List friends){

        List<ParseQuery<ParseObject>> queries = new ArrayList<ParseQuery<ParseObject>>();
        for(int i = 0; i < friends.size(); i++){
            queries.add(ParseQuery.getQuery("AtHome").whereEqualTo("Email", friends.get(i)));
        }

        ParseQuery<ParseObject> finalAtHome = ParseQuery.or(queries);
        finalAtHome.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if (e == null) {
                    for (int j = 0; j < parseObjects.size(); j++) {
                        Log.d("guitar", parseObjects.get(j).getString("Email") + " is " + parseObjects.get(j).getBoolean("Status"));
                    }
                }
            }
        });
    }

    /*
    Done Offline. Fetches friend status after friend list is fetched.
     */
    public void getFriends(){
        ParseQuery<ParseObject> atHomeOffline = ParseQuery.getQuery("AtHome");
        atHomeOffline.fromLocalDatastore();  //offline
        atHomeOffline.whereEqualTo("Email", userEmail);
        atHomeOffline.findInBackground(new FindCallback<ParseObject>() {
            List friends;
            ParseObject user;

            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if (e == null) {
                    user = parseObjects.get(0);
                    friends = user.getList("FriendList");
                    Log.d("guitar", "friends: " + friends);
                    getFriendStatus(friends);
                }
            }
        });
    }

    /*
    Checks if the email exists on the server
     */
    public void putUser(){

        //First we need to check if the object already exists on the server before we put.
        ParseQuery<ParseObject> atHomeVerify = ParseQuery.getQuery("AtHome");
        atHomeVerify.whereEqualTo("Email", userEmail);
        atHomeVerify.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if(e == null){
                    if(parseObjects.size() == 0){
                        //put on server
                        Log.d("guitar", "new user!");
//                        final ParseObject atHome = new ParseObject("AtHome");
                        userObject.put("Email", userEmail);
                        userObject.put("FriendList", Arrays.asList("aloksharma@ufl.edu", "stalukdar@ufl.edu"));
                        userObject.put("Status", false);

                        userObject.pinInBackground(); //save this offline in the datastore, and then save in cloud.
                        userObject.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e != null) {
                                    Log.d("guitar", e.getMessage());
                                } else {
                                    Log.d("guitar", "new user pushed " + userObject.getObjectId());
                                    userObjectId = userObject.getObjectId();
                                    addFriend("mrig@ufl.edu");
                                }
                            }
                        });
                    }else{
                        //Email already exists on server. Do not push.
                        Log.d("guitar", "user already exists.");
                    }
                }
            }
        });
    }

    /*
    Currently being called after put user is successful.
     */
    public void addFriend(String friendEmail){
        userObject.add("FriendList", friendEmail);
        userObject.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e != null){
                    Log.d("guitar", "add friend failed: " + e.getMessage());
                }
            }
        });
    }
}
