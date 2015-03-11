package aloksharma.ufl.edu.athome;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.parse.FindCallback;
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

//TODO have this whole class be in its own thread. Then instead of calling findInBackground, call find. Sequential execution of functions will be possible then.
public class ServerAccess {

    String userEmail = "aloksharma@ufl.edu";
    ParseObject userObject;
    boolean userFetched = false;
    MainActivity mainActivity;
    Context mcontext;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor sharedPrefEditor;

    public ServerAccess(Context context) {
        this.mcontext = context;
        sharedPreferences = mcontext.getSharedPreferences(mcontext.getString(R.string.shared_pref_name), mcontext.MODE_PRIVATE);
        sharedPrefEditor = sharedPreferences.edit();
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
                        userFetched = true;
                        userObject.pinInBackground(); //save this object
                    } else {
                        //user exists offline.
                        Log.d("guitar", "user in local datastore.");
                        userObject = parseObjects.get(0);
                        userFetched = true;
                    }

                    //Got the userObject. If mainActivity spawned the server, populate the fields.
                    if(mcontext instanceof MainActivity){
                        mainActivity = (MainActivity)mcontext;
                        getFriendStatus();
                    }
                } else {
                    Log.d("guitar", "error in constructor: " + e.getMessage());
                }
            }
        });
    }

    /*
        Needs to be called only after userObject fetched from constructor.
        Values are fetched from server.
        Also changes UI by calling changeText() of mainActivity.
     */
    public void getFriendStatus(){

        List friends = userObject.getList("FriendList");
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
    Should be called only after userObject initialized.
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
    Should be called only after userObject initialized.
    Add new user.
     */
    public void putUser(){
        final List friendList = new ArrayList();
        final ParseObject userObjectNew = new ParseObject("AtHome");
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
                        userObjectNew.put("Email", userEmail);
                        userObjectNew.put("Status", false);
                        userObjectNew.put("FriendList", friendList);
                        userObjectNew.pinInBackground(); //save this offline in the datastore, and then save in cloud.
                        userObjectNew.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e != null) {
                                    Log.d("guitar", e.getMessage());
                                } else {
                                    Log.d("guitar", "new user pushed " + userObjectNew.getObjectId());
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
    Should be called only after userObject initialized.
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
                    userObject.pinInBackground();
                    userObject.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if(e != null){
                                Log.d("guitar", "unable to add friend: " + e.getMessage());
                            }else{
                                getFriendStatus();
                            }
                        }
                    });

                    //Add yourself to the friends list as well.
                    //There should be only one object returned for that friend email, since it is unique. Which is why accessing the 0th index.
                    Log.d("guitar", "new friend: " + friendObject.get(0).getString("Email"));
                    friendObject.get(0).add("FriendList", userEmail);
                    friendObject.get(0).pinInBackground(); //save friend in local store.
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

    public void removeFriend(final String friendEmail){
        Log.d("guitar", "attempt to remove " + friendEmail);
        List friendList = new ArrayList();
        friendList.add(friendEmail);
        final List userList = new ArrayList();
        userList.add(userEmail);

        ParseQuery<ParseObject> findFriendQuery = new ParseQuery<ParseObject>("AtHome");
        findFriendQuery.whereEqualTo("Email", friendEmail);
        findFriendQuery.fromLocalDatastore();
        findFriendQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(final List<ParseObject> friendObjects, ParseException e) {
                if(e == null && friendObjects.size() != 0){
                    //found the friend to remove.
                    friendObjects.get(0).removeAll("FriendList", userList); //remove user from friend
                    friendObjects.get(0).saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if(e != null){
                                Log.d("guitar", "unable to remove user from friends friend list: " + e.getMessage());
                            }else{
                                Log.d("guitar", "removed user from friends friend list " + friendObjects.get(0).getString("Email"));
                            }
                        }
                    });
                    friendObjects.get(0).unpinInBackground(); //remove the friend from local store.
                }else if(e != null){
                    Log.d("guitar", "couldnt find friend " + e.getMessage());
                }
            }
        });

        userObject.removeAll("FriendList", friendList);
        userObject.pinInBackground(); //update local store since friend list is modified.
        userObject.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e != null){
                    Log.d("guitar", "error removing friend: " + e.getMessage());
                }else{
                    Log.d("guitar", "removed " + friendEmail);
                    getFriendStatus();
                }
            }
        });
    }

    //Should be called only after userObject initialized.
    public void setAtHomeStatus(final Boolean status){
        //first check what we have already told the server from shared pref. If different, then tell server.
        Boolean statusOnServer = sharedPreferences.getBoolean("AtHome", true);
        Log.d("guitar", "changing status to " + status + ". Shared pref says: " + statusOnServer);
        if(statusOnServer != status){
            //send to server now.
            //
            if(userObject == null){
                ParseQuery<ParseObject> userQuery = new ParseQuery<ParseObject>("AtHome");
                userQuery.whereEqualTo("Email", userEmail);
                userQuery.fromLocalDatastore();
                try{
                    userObject = userQuery.find().get(0);
                }catch (ParseException e){
                    Log.d("guitar", "error in set home status, couldnt get user: " + e.getMessage());
                }
            }
            userObject.put("Status", status);
            userObject.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if(e != null){
                        Log.d("guitar", "unable to change status of user");
                    }else{
                        Log.d("guitar", "changed status to " + status);
                        sharedPrefEditor.putBoolean("AtHome", status); //save what we have told the server.
                        sharedPrefEditor.commit();
                    }
                }
            });
        }

    }
}


