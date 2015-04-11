package aloksharma.ufl.edu.athome;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.widget.LoginButton;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;

import org.json.JSONObject;

import java.util.Arrays;

/**
 * Created by Soham Talukdar on 3/13/2015.
 * Login Page of the app.
 */
public class SignInActivity extends Activity {
    protected EditText username;
    protected EditText password;
    protected Button signIn;
    protected Button signUp;
    Intent toMainActivity;
    SharedPreferences sharedPref;
    SharedPreferences.Editor sharedPrefEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        toMainActivity = new Intent(this, MainActivity.class);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        if(ParseUser.getCurrentUser() != null){
            sharedPrefEditor = sharedPref.edit();
            sharedPrefEditor.putString("user_email", ParseUser.getCurrentUser().getEmail());
            sharedPrefEditor.commit();
            fetchExistingUser();
            startActivity(toMainActivity);
            finish();
        }

        LoginButton fbLogin = (LoginButton)findViewById(R.id.login_button);
        fbLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fbLogin();
            }
        });

//        DigitsAuthButton digitsButton = (DigitsAuthButton) findViewById(R.id.auth_button);
//        digitsButton.setCallback(new AuthCallback() {
//            @Override
//            public void success(DigitsSession session, String phoneNumber) {
//                // Do something with the session and phone number
//                Log.d("guitarDig", "success" + phoneNumber);
//            }
//
//            @Override
//            public void failure(DigitsException exception) {
//                // Do something on failure
//                Log.d("guitarDig", "fail: " + exception.getMessage());
//            }
//        });

        username=(EditText)findViewById(R.id.username);
        password=(EditText)findViewById(R.id.password);
        signIn=(Button)findViewById(R.id.signIn);
        signUp=(Button)findViewById(R.id.signUp);
        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String userName = username.getText().toString().trim();
                String passWord = password.getText().toString().trim();
                ParseUser.logInInBackground(userName, passWord, new LogInCallback() {
                    public void done(ParseUser user, ParseException e) {
                        if (user != null) {
                            // Hooray! The user is logged in.
                            sharedPrefEditor = sharedPref.edit();
                            sharedPrefEditor.putString("user_email", ParseUser.getCurrentUser().getEmail());
                            sharedPrefEditor.commit();
                            fetchExistingUser();
                            startActivity(toMainActivity);
                            finish();
                        } else {
                            // Signup failed. Look at the ParseException to see what happened.
                            Toast.makeText(SignInActivity.this, "Sign-in Failed :" + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent takeRegPage = new Intent(SignInActivity.this, Registration.class);
                startActivity(takeRegPage);
            }
        });
    }

    public void fbLogin(){
        ParseFacebookUtils.logInWithReadPermissionsInBackground(this, Arrays.asList("email", "public_profile"), new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException err) {
                if (user == null) {
                    Log.d("MyApp", "Uh oh. The user cancelled the Facebook login.");
                    return;
                } else {
                    Log.d("MyApp", "login worked " + user.getEmail());
                    if(user.isNew()){
                        fetchFBUserDetails();
                    }else{
                        user.saveInBackground();
                        user.pinInBackground();
                        sharedPrefEditor = sharedPref.edit();
                        sharedPrefEditor.putString("user_email", user.getEmail());
                        sharedPrefEditor.commit();
                        fetchExistingUser();
                        startActivity(toMainActivity);
                        finish();
                    }

                }
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("guitarfb", "req: " + requestCode);
        super.onActivityResult(requestCode, resultCode, data);
        ParseFacebookUtils.onActivityResult(requestCode, resultCode, data);
    }

    public void fetchFBUserDetails(){
        if (AccessToken.getCurrentAccessToken() != null) {
            GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(), new GraphRequest.GraphJSONObjectCallback(){

                @Override
                public void onCompleted(JSONObject user, GraphResponse graphResponse) {
                    Log.d("MyApp", "User signed up and logged in through Facebook! ");
                    createNewUser(user);
                    startActivity(toMainActivity);
                    finish();
                }
            }).executeAsync();
        }
    }

    public void fetchExistingUser(){
        Intent serverIntent = new Intent(getApplicationContext(), ServerAccess.class);
        serverIntent.putExtra("server_action", ServerAccess.ServerAction.GET_USER.toString());
        getApplicationContext().startService(serverIntent);
    }

    public void createNewUser(JSONObject graphUser){
        ParseUser.getCurrentUser().put("email", graphUser.optString("email"));
        ParseUser.getCurrentUser().saveInBackground();
        ParseUser.getCurrentUser().pinInBackground();
        sharedPrefEditor = sharedPref.edit();
        sharedPrefEditor.putString("user_email", graphUser.optString("email"));
        sharedPrefEditor.commit();
        Intent serverIntent = new Intent(getApplicationContext(), ServerAccess.class);
        serverIntent.putExtra("server_action", ServerAccess.ServerAction.ADD_USER.toString());
        serverIntent.putExtra("email", graphUser.optString("email"));
        serverIntent.putExtra("fname", graphUser.optString("first_name"));
        serverIntent.putExtra("lname", graphUser.optString("last_name"));
        getApplicationContext().startService(serverIntent);
    }
}
