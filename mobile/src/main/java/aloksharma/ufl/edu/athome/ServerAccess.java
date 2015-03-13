package aloksharma.ufl.edu.athome;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Alok on 2/25/2015.
 * Use this to talk to the backend server.
 */

public class ServerAccess extends IntentService {

    public enum ServerAction {
        GET_USER, ADD_FRIEND, REMOVE_FRIEND, ADD_USER, GET_FRIENDS, GET_FRIENDS_HOME, SET_HOME_STATUS
    }

    String userEmail = "aloksharma@ufl.edu";
    String first_name = "Alok"; String last_name = "Sharma";
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor sharedPrefEditor;
//    App app = (App)getApplicationContext();

    public ServerAccess() {
        super("ServerAccess");
    }

    /*
        Returns list of friends who are home.
     */
    public List<AtHomeUser> getFriendsHome(ParseObject userObject){

        List<ParseObject> friends;
        List<AtHomeUser> friendsHome = new ArrayList<>();

        List<String> friendList = userObject.getList("FriendList");
        friendList.add(userObject.getString("Email")); //tell server to fetch my details, in addition to fetching details of friends.

        if(friendList.size() != 0) {

            List<ParseQuery<ParseObject>> queries = new ArrayList<ParseQuery<ParseObject>>();
            for (int i = 0; i < friendList.size(); i++) {
                queries.add(ParseQuery.getQuery("AtHome").whereEqualTo("Email", friendList.get(i)));
            }
            ParseQuery<ParseObject> friendsQuery = ParseQuery.or(queries);

            try{
                friends = friendsQuery.find();
                for (int j = 0; j < friends.size(); j++) {
                    Log.d("guitar", friends.get(j).getString("Email") + " is " + friends.get(j).getBoolean("Status"));
                    friends.get(j).pin(); //update their pin whenever you fetch them.

                    if(friends.get(j).getString("Email").equals(userObject.getString("Email"))){
                        //if found myself in list, update myself in local datastore.
                        Log.d("guitar", "fetched my own details while fetching friends status, and updating pinned object");
                        friends.get(j).pin();
                    }else if(friends.get(j).getBoolean("Status")){
                        AtHomeUser atHomeUser = new AtHomeUser();
                        atHomeUser.setEmail(friends.get(j).getString("Email"));
                        atHomeUser.setFirstName(friends.get(j).getString("First_Name"));
                        atHomeUser.setLastName(friends.get(j).getString("Last_Name"));

                        friendsHome.add(atHomeUser);
                    }else{
                        //If friend not home, do nothing.
                    }
                }
            }catch (ParseException e){
                Log.d("guitar", "error finding friends: " + e.getMessage());
            }
        }else{
            Log.d("guitar", "getFriendsHome got an empty list of friends, meaning friends dont exist yet.");
        }
        return friendsHome;
    }

    /*
    Done Offline. Fetches friend list. userObject wouldve already been populated.
    Returns list of email of friends.
     */
    public List<String> getFriends(ParseObject userObject){
        List<String> friends = new ArrayList<>();
        if(userObject != null) {
            friends = userObject.getList("FriendList");
        }else{
            Log.d("guitar" , "error: userobject was null in getFriends");
        }
        return friends;
    }

    public void getFriendsDetailed(ParseObject userObject){
        List<String> friends = getFriends(userObject);

    }

    public ParseObject getUser(){
        List<ParseObject> userObjects = new ArrayList<>();
        ParseObject userObject;

        ParseQuery<ParseObject> userQueryOffline = ParseQuery.getQuery("AtHome");
        userQueryOffline.fromLocalDatastore();  //offline
        userQueryOffline.whereEqualTo("Email", userEmail);
        try{
            userObjects = userQueryOffline.find();
        }catch (ParseException e){
            Log.d("guitar", "get user error: " + e.getMessage());
        }

        if (userObjects.size() == 0) {
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
            //user exists offline.
            Log.d("guitar", "user in local datastore.");
            userObject = userObjects.get(0);
        }
        return userObject;
    }

    /*
    Add new user. Returns user object
     */
    public ParseObject putUser(){
        final List friendList = new ArrayList();
        final ParseObject newUser = new ParseObject("AtHome");
        //First we need to check if the object already exists on the server before we put.
        ParseQuery<ParseObject> userQuery = ParseQuery.getQuery("AtHome");
        userQuery.whereEqualTo("Email", userEmail);
        try{
            if(userQuery.find().size() == 0){
                //User doesn't exist. Put new user on server
                Log.d("guitar", "new user!");
                newUser.put("Email", userEmail);
                newUser.put("Status", false);
                newUser.put("FriendList", friendList);
                newUser.put("First_Name", first_name);
                newUser.put("Last_Name", last_name);
                newUser.pinInBackground(); //save this offline in the datastore, and then save in cloud.
                newUser.save();
            }else{
                //Email already exists on server. Do not push.
                Log.d("guitar", "user already exists.");
            }
        }catch (ParseException e){
            Log.d("guitar", "unable to search for user on server/save new user: " + e.getMessage());
        }
        return newUser;
    }

    /*
    Add a new friend. Checks if friend exists on server. If exists, add to your friend list, and get added to their list.
    If friend doesnt exists, send invite. Invite system not yet implemented.
     */
    public void addFriend(ParseObject userObject, final String friendEmail){
        //TODO: send invite.
        List<ParseObject> friendObjects = new ArrayList<>();
        final ParseQuery<ParseObject> friendExists = ParseQuery.getQuery("AtHome");
        friendExists.whereEqualTo("Email", friendEmail);
        try{
            friendObjects = friendExists.find();
            if(friendObjects.size() == 0){
                //friend doesn't exists, send invite here.
            }else{
                userObject.add("FriendList", friendEmail);
                userObject.pin();
                userObject.save();

                friendObjects.get(0).add("FriendList", userEmail);
                //TODO: Save friend object in local datastore? Yes for now.
                //TODO: friend needs to know they have been added to a friend list, and that their own friend list has changed.
                friendObjects.get(0).pin();
                friendObjects.get(0).save();
            }
        }catch (ParseException e){
            Log.d("guitar" , "couldnt add friend: " + e.getMessage());
        }
    }

    public void removeFriend(ParseObject userObject, String friendEmail){
        Log.d("guitar", "attempt to remove " + friendEmail);

        List<ParseObject> friendObjects;
        List<String> friendList = new ArrayList<>();
        friendList.add(friendEmail);
        List<String> userList = new ArrayList<>();
        userList.add(userEmail);
        ParseQuery<ParseObject> findFriendQuery = new ParseQuery<>("AtHome");
        findFriendQuery.whereEqualTo("Email", friendEmail);
        findFriendQuery.fromLocalDatastore();
        try{
            friendObjects = findFriendQuery.find();
            if(friendObjects.size() != 0){
                //found the friend to remove.
                //TODO: friend needs to know their friend list has changed (user has been removed from their friend list).
                friendObjects.get(0).removeAll("FriendList", userList); //remove user from friend
                friendObjects.get(0).save();
                friendObjects.get(0).unpin(); //remove the friend from local store.

                userObject.removeAll("FriendList", friendList); //remove friend from user
                userObject.save();
                userObject.pin(); //update local store since friend list is modified.
            }
        }catch (ParseException e){
            Log.d("guitar", "couldnt remove friend: " + e.getMessage());
        }
    }

    //Should be called only after userObject initialized.
    public void setAtHomeStatus(final ParseObject userObject, final Boolean status){
        //first check what we have already told the server from shared pref. If different, then tell server.
        sharedPreferences = getSharedPreferences(getString(R.string.shared_pref_name), this.MODE_PRIVATE);
        sharedPrefEditor = sharedPreferences.edit();
        Boolean statusOnServer = sharedPreferences.getBoolean("AtHome", false);
        Log.d("guitar", "changing status to " + status + ". Shared pref says: " + statusOnServer);
        if(statusOnServer != status){
            //send to server now.
            userObject.put("Status", status);
            try{
                userObject.save();
                userObject.pin();
                sharedPrefEditor.putBoolean("AtHome", status); //save what we have told the server.
                sharedPrefEditor.commit();
            }catch (ParseException e){
                Log.d("guitar", "unable to change status: " + e.getMessage());
            }
        }else{
//            nothing new to tell server. Dont do anything.
        }
    }

    //Receive incoming requests from activities, and send appropriate server request.
    //On receiving response from server, send broadcast back to activity.
    @Override
    protected void onHandleIntent(Intent intent) {
        //GET_USER, ADD_FRIEND, REMOVE_FRIEND, ADD_USER, GET_FRIENDS, GET_FRIENDS_STATUS, SET_HOME_STATUS
        String action;
        action = intent.getStringExtra("server_action");

        Intent responseIntent = new Intent("server_response");
        responseIntent.putExtra("server_action", action);

        if(action.equals(ServerAction.GET_FRIENDS.toString())){
            List<String> friendList = getFriends(getUser());
            Log.d("guitarintent", "get friends intent: " + friendList);
            responseIntent.putStringArrayListExtra("data", new ArrayList<>(friendList));
        }else if(action.equals(ServerAction.ADD_FRIEND.toString())){
            Log.d("guitarintent", "add friend intent");
        }else if(action.equals(ServerAction.ADD_USER.toString())){
            Log.d("guitarintent", "add user intent");
        }else if(action.equals(ServerAction.GET_FRIENDS_HOME.toString())){
            List<AtHomeUser> friendsHome = getFriendsHome(getUser());
            Log.d("guitarintent", "get friends friends home: " + friendsHome);
//            responseIntent.putStringArrayListExtra("data", new ArrayList<AtHomeUser>(friendsHome));
            responseIntent.putParcelableArrayListExtra("data", new ArrayList<AtHomeUser>(friendsHome));
        }else if(action.equals(ServerAction.REMOVE_FRIEND.toString())){
            Log.d("guitarintent", "remove friend intent");
        }else if(action.equals(ServerAction.SET_HOME_STATUS.toString())){
            Log.d("guitarintent", "set home status intent: " + intent.getBooleanExtra("server_action_arg", false));
            setAtHomeStatus(getUser(), intent.getBooleanExtra("server_action_arg", false));
        }else if(action.equals(ServerAction.GET_USER.toString())){
            Log.d("guitarintent", "get user intent");
            ParseObject userObject = getUser();

            //Take data out of userobject and send broadcast.
        }

        LocalBroadcastManager.getInstance(this).sendBroadcast(responseIntent);
    }
}


