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

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.model.GraphUser;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;

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
//        toMainActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        if(ParseUser.getCurrentUser() != null){
            sharedPrefEditor = sharedPref.edit();
            sharedPrefEditor.putString("user_email", ParseUser.getCurrentUser().getEmail());
            sharedPrefEditor.commit();
            fetchExistingUser();
            startActivity(toMainActivity);
            finish();
        }

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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("guitarfb", "req: " + requestCode);
        if (requestCode == 32665) //weird fix for a weird parse problem. Read more about it at: https://www.parse.com/questions/nullpointerexceptions-from-the-facebook-authentication
            ParseFacebookUtils.finishAuthentication(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
        ParseFacebookUtils.logIn(Arrays.asList("email", ParseFacebookUtils.Permissions.User.ABOUT_ME, ParseFacebookUtils.Permissions.User.EMAIL), this, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException err) {
                if (user == null) {
                    Log.d("MyApp", "Uh oh. The user cancelled the Facebook login.");
                    return;
                } else {
                    fetchUserDetails(user.isNew());
                }
            }
        });
    }

    public void fetchUserDetails(final Boolean newUser){
        if (ParseFacebookUtils.getSession().isOpened()) {
            Request.newMeRequest(ParseFacebookUtils.getSession(), new Request.GraphUserCallback() {

                @Override
                public void onCompleted(GraphUser graphUser, Response response) {

                    String email = graphUser.getProperty("email").toString();
                    ParseUser.getCurrentUser().put("email", email); //populate the User table of our Parse database.
                    ParseUser.getCurrentUser().saveInBackground();
                    ParseUser.getCurrentUser().pinInBackground();

                    sharedPrefEditor = sharedPref.edit();
                    sharedPrefEditor.putString("user_email", email);
                    sharedPrefEditor.commit();

                    if(newUser){
                        Log.d("MyApp", "User signed up and logged in through Facebook! ");
                        createNewUser(graphUser);
                    }else{
                        Log.d("MyApp", "User logged in through Facebook! ");
                        fetchExistingUser();
                    }
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

    public void createNewUser(GraphUser graphUser){
        Intent serverIntent = new Intent(getApplicationContext(), ServerAccess.class);
        serverIntent.putExtra("server_action", ServerAccess.ServerAction.ADD_USER.toString());
        serverIntent.putExtra("email", graphUser.getProperty("email").toString());
        serverIntent.putExtra("fname", graphUser.getFirstName());
        serverIntent.putExtra("lname", graphUser.getLastName());
        getApplicationContext().startService(serverIntent);
    }
}
