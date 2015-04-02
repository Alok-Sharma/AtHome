package aloksharma.ufl.edu.athome;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
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
        GET_USER, ADD_USER, GET_FRIENDS, GET_FRIENDS_HOME, SET_HOME_STATUS, SET_INVISIBLE,
        SET_WIFI
    }

    public enum AtHomeStatus {
        TRUE, FALSE, INVISIBLE
    }

    String userEmail;
    SharedPreferences sharedPreferences;

    public ServerAccess() {
        super("ServerAccess");
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(App.getContext());
        userEmail = sharedPreferences.getString("user_email", "");
    }

    /*
        Returns list of friends who are home.
     */
    public List<AtHomeUser> getFriendsHome(ParseObject userObject){

        List<ParseObject> friends;
        List<AtHomeUser> friendsHome = new ArrayList<>();

        ParseQuery<ParseObject> friendQuery = new ParseQuery<>("AtHome");
        String wifi_id = sharedPreferences.getString("home_wifi_id", null);
        Log.d("guitar", "looking for wifi: " + wifi_id);
        friendQuery.whereEqualTo("wifi", wifi_id);

        try{
            friends = friendQuery.find();
            Log.d("guitar", "fetched " + friends.size() + " friends");
            for (int j = 0; j < friends.size(); j++) {
                Log.d("guitar", friends.get(j).getString("Email") + " is " + friends.get(j).get("Status"));
                friends.get(j).pin(); //update their pin whenever you fetch them.

                if(friends.size() == 1){
                    //Current user is the only one on this Wifi.
                    return null;
                }
                if(friends.get(j).getString("Email").equals(userObject.getString("Email"))){
                    //if found myself in list, update myself in local datastore.
                    friends.get(j).pin();
                }else if(friends.get(j).get("Status").equals(AtHomeStatus.TRUE.toString())){
                    AtHomeUser atHomeUser = new AtHomeUser();
                    atHomeUser.setEmail(friends.get(j).getString("Email"));
                    atHomeUser.setFirstName(friends.get(j).getString("First_Name"));
                    atHomeUser.setLastName(friends.get(j).getString("Last_Name"));
                    atHomeUser.setWifi(friends.get(j).getString("wifi"));
                    friendsHome.add(atHomeUser);
                }else{
                    //If friend not home, do nothing.
                }
            }
        }catch (ParseException e){
            Log.d("guitar", "error finding friends: " + e.getMessage());
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

    public ParseObject getUser(String userEmail){
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
            List<ParseObject> parseObjectsOnline = new ArrayList<>();
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
        Log.d("guitarUser", "I am: " + userObject.getString("First_Name"));
        SharedPreferences.Editor sharedPrefEditor = sharedPreferences.edit();
        sharedPrefEditor.putString("user_fname", userObject.getString("First_Name"));
        sharedPrefEditor.putString("user_lname", userObject.getString("Last_Name"));
        sharedPrefEditor.putString("home_wifi_id", userObject.getString("wifi"));
        sharedPrefEditor.putString("home_wifi_name", userObject.getString("wifi_name"));
        sharedPrefEditor.commit();

        return userObject;
    }

    /*
    Add new user. Returns user object
     */
    public ParseObject putUser(String userEmail, String first_name, String last_name){
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
                newUser.put("First_Name", first_name);
                newUser.put("Last_Name", last_name);
                newUser.pin(); //save this offline in the datastore, and then save in cloud.
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


    public void setAtHomeStatus(ParseObject userObject, String status){
        //first check what we have already told the server from the existing user object. If different, then tell server.
        String statusOnServer = userObject.getString("Status");
        if (statusOnServer == null){
            statusOnServer = "";
        }

        Log.d("guitar", "changing status to " + status + ". User object says: " + statusOnServer);
        if(!statusOnServer.equals(status)){
            //send to server now.
            if(status.equals(AtHomeStatus.INVISIBLE.toString())){
                //status = null means invisibility has been set.
                userObject.put("Status", AtHomeStatus.INVISIBLE.toString());
                Log.d("guitar", "changed to invisible on server.");
            }else if(!sharedPreferences.getBoolean("invisible", false)){
                userObject.put("Status", status);
                Log.d("guitar", "changed to " + status + " on server.");
            }else{
                Log.d("guitar", "status was not null, but invisible was true, so doing nothing.");
            }

            try{
                userObject.save();
                userObject.pin();
            }catch (ParseException e){
                Log.d("guitar", "unable to change status: " + e.getMessage());
            }
        }else{
//            nothing new to tell server. Dont do anything.
            Log.d("guitar", "did not change status, since status hasnt changed");
        }
    }

    public void setHomeWifi(ParseObject userObject){
        WifiChangeReceiver wifiChangeReceiver = new WifiChangeReceiver();
        String wifiID = wifiChangeReceiver.getWifiID(this);
        String wifiName = wifiChangeReceiver.getWifiName(this);
        Log.d("guitar", "set home wifi: " + wifiID + " for the user: " + userObject.get("Email"));
        if(wifiID != null){
            userObject.put("wifi", wifiID);
            userObject.put("wifi_name", wifiName);
            SharedPreferences.Editor prefEdit = sharedPreferences.edit();
            prefEdit.putString("home_wifi_id", wifiID);
            prefEdit.commit();

            Log.d("guitar", "saving wifi: " + wifiID);
            try{
                userObject.save();
                userObject.pin();
            }catch (ParseException e){
                Log.d("guitar", "error pushing wifi: " +e.getMessage());
            }
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
            List<String> friendList = getFriends(getUser(userEmail));
            Log.d("guitarintent", "get friends intent: " + friendList);
            responseIntent.putStringArrayListExtra("data", new ArrayList<>(friendList));
        }else if(action.equals(ServerAction.ADD_USER.toString())){
            Log.d("guitarintent", "add user intent");
            putUser(intent.getStringExtra("email"), intent.getStringExtra("fname"), intent.getStringExtra("lname"));
        }else if(action.equals(ServerAction.GET_FRIENDS_HOME.toString())){
            List<AtHomeUser> friendsHome = getFriendsHome(getUser(userEmail));
            Log.d("guitarintent", "get friends home: " + friendsHome);
            if(friendsHome == null){
                //Current user is the only one on this wifi
                responseIntent.putParcelableArrayListExtra("data", null);
            }else{
                responseIntent.putParcelableArrayListExtra("data", new ArrayList<>(friendsHome));
            }
        }else if(action.equals(ServerAction.SET_HOME_STATUS.toString())){
            Log.d("guitarintent", "set home status intent: " + intent.getStringExtra("server_action_arg"));
            setAtHomeStatus(getUser(userEmail), intent.getStringExtra("server_action_arg"));
        }else if(action.equals(ServerAction.SET_INVISIBLE.toString())){
            Log.d("guitarintent", "set invisible");
            setAtHomeStatus(getUser(userEmail), AtHomeStatus.INVISIBLE.toString());
        }else if(action.equals(ServerAction.GET_USER.toString())){
            Log.d("guitarintent", "get user intent");
            ParseObject userObject = getUser(userEmail);
            //Take data out of userobject and send broadcast.
        }else if(action.equals(ServerAction.SET_WIFI.toString())) {
            Log.d("guitarintent", "set wifi intent");
            setHomeWifi(getUser(userEmail));
        }

        LocalBroadcastManager.getInstance(this).sendBroadcast(responseIntent);
    }
}


