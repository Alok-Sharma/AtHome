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
import java.util.List;

/**
 * Created by Alok on 2/25/2015.
 * Use this to talk to the backend server.
 */
public class ServerAccess {

    String userEmail = "aloksharma@ufl.edu";
    ParseObject userObject;
    MainActivity mainActivity;

    public ServerAccess(Context context) {

        mainActivity = (MainActivity)context;
        // Enable Local Datastore.
        Parse.enableLocalDatastore(context);
        Parse.initialize(context, "KwiPstN87loO5vCbMsaixTkQLu6KWIYjAV9eUYrF", "EHKdb0bCDnlGuecbUfkMnxizZBMRdrZ6ntrdzLrV");

        //Get the user object
        ParseQuery<ParseObject> userQueryOffline = ParseQuery.getQuery("AtHome");
        userQueryOffline.fromLocalDatastore();  //offline
        userQueryOffline.whereEqualTo("Email", userEmail);
        userQueryOffline.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if (e == null) {
                    if (parseObjects.size() == 0) {
                        //user doesnt exist in data store. This shouldn't happen since putUser wouldve added it in the datastore.
                        Log.d("guitar", "user doesnt exist in local datastore.");
                        ParseQuery<ParseObject> userQueryOnline = ParseQuery.getQuery("AtHome");
                        userQueryOnline.whereEqualTo("Email", userEmail);
                        List<ParseObject> parseObjectsOnline = new ArrayList<ParseObject>();
                        try{
                            parseObjectsOnline = userQueryOnline.find();
                        }catch (ParseException eOnline){
                            Log.d("guitar", "error, couldnt get user from cloud: " + eOnline.getMessage());
                        }
                        userObject = parseObjectsOnline.get(0);
                        userObject.pinInBackground(); //save this object
                    } else {
                        userObject = parseObjects.get(0);
                    }
                    getFriendStatus(userObject.getList("FriendList"));
                } else {
                    Log.d("guitar", "error in constructor: " + e.getMessage());
                }
            }
        });
    }

    /*
        Called by getFriends.
        Values are fetched from server.
     */
    public void getFriendStatus(List friends){
        if(friends != null) {
            List<ParseQuery<ParseObject>> queries = new ArrayList<ParseQuery<ParseObject>>();
            for (int i = 0; i < friends.size(); i++) {
                queries.add(ParseQuery.getQuery("AtHome").whereEqualTo("Email", friends.get(i)));
            }
            ParseQuery<ParseObject> friendsQuery = ParseQuery.or(queries);
            friendsQuery.findInBackground(new FindCallback<ParseObject>() {
                ArrayList<String> friendsHome = new ArrayList<String>();
                ArrayList<String> friendsNotHome = new ArrayList<String>();

                @Override
                public void done(List<ParseObject> parseObjects, ParseException e) {
                    if (e == null) {
                        String text = "";
                        for (int j = 0; j < parseObjects.size(); j++) {
                            Log.d("guitar", parseObjects.get(j).getString("Email") + " is " + parseObjects.get(j).getBoolean("Status"));
                            if(parseObjects.get(j).getBoolean("Status")){
                                friendsHome.add(parseObjects.get(j).getString("Email"));
                            }else{
                                friendsNotHome.add(parseObjects.get(j).getString("Email"));
                            }
                        }
                        mainActivity.changeText(friendsHome, friendsNotHome);
                    }
                }
            });
        }else{
            Log.d("guitar", "getFriendStatus got a null list of friends, meaning friends dont exist yet.");
        }
    }

    /*
    Done Offline. Fetches friend list. userObject wouldve already been populated.
     */
    public List getFriends(){
        List friends = new ArrayList();
        if(userObject != null) {
            friends = userObject.getList("FriendList");
        }else{
            Log.d("guitar" , "error: userobject was null in getFriends");
        }
        return friends;
    }

    /*
    Add new user.
     */
    public void putUser(){
        final ParseObject userObject = new ParseObject("AtHome");
        //First we need to check if the object already exists on the server before we put.
        ParseQuery<ParseObject> atHomeVerify = ParseQuery.getQuery("AtHome");
        atHomeVerify.whereEqualTo("Email", userEmail);
        atHomeVerify.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if(e == null){
                    if(parseObjects.size() == 0){
                        //User doesn't exist. Put new user on server
                        Log.d("guitar", "new user!");
                        userObject.put("Email", userEmail);
                        userObject.put("Status", false);

                        userObject.pinInBackground(); //save this offline in the datastore, and then save in cloud.
                        userObject.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e != null) {
                                    Log.d("guitar", e.getMessage());
                                } else {
                                    Log.d("guitar", "new user pushed " + userObject.getObjectId());
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
    Add a new friend. Checks if friend exists on server. If exists, add to your friend list, and get added to their list.
    If friend doesnt exists, send invite. Invite system not yet implemented.
     */
    public void addFriend(final String friendEmail){
        //TODO: send invite.
        final ParseQuery<ParseObject> friendExists = ParseQuery.getQuery("AtHome");
        friendExists.whereEqualTo("Email", friendEmail);
        friendExists.findInBackground(new FindCallback<ParseObject>() {

            @Override
            public void done(List<ParseObject> friendObject, ParseException e) {
                if(friendObject.size() == 0){
                    //friend doesn't exists, send invite here.
                }else{
                    //friend exists, add to friend list.

                    userObject.add("FriendList", friendEmail);
                    userObject.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if(e != null){
                                Log.d("guitar", "unable to add friend: " + e.getMessage());
                            }
                        }
                    });

                    //Add yourself to the friends list as well.
                    //There should be only one object returned for that friend email, since it is unique. Which is why accessing the 0th index.
                    Log.d("guitar", "friend: " + friendObject.get(0).getString("Email"));
                    friendObject.get(0).add("FriendList", userEmail);
                    friendObject.get(0).saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if(e != null){
                                Log.d("guitar", "unable to add user to friends list " + e.getMessage());
                            }
                        }
                    });

                }
            }
        });
    }
}

//TODO: Functions can block calling thread. So call them from a findinbackground thread. Or make callers get stuck in a while loop until they havent finished.

