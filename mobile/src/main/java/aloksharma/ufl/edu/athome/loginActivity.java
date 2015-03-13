package aloksharma.ufl.edu.athome;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.facebook.widget.LoginButton;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

/**
 * Created by Soham Talukdar on 3/13/2015.
 * Login Page of the app.
 */
public class LoginActivity extends Activity {
    protected EditText username;
    protected EditText password;
    protected Button signIn;
    protected Button signUp;
    Intent toMainActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        toMainActivity = new Intent(this, MainActivity.class);

        View someView = findViewById(R.id.login);
        View root = someView.getRootView();
        root.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_dark));
        username=(EditText)findViewById(R.id.username);
        password=(EditText)findViewById(R.id.password);
        signIn=(Button)findViewById(R.id.signIn);
        signUp=(Button)findViewById(R.id.signUp);
        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String userName=username.getText().toString().trim();
                String passWord=password.getText().toString().trim();
                ParseUser.logInInBackground(userName,passWord, new LogInCallback() {
                    public void done(ParseUser user, ParseException e) {
                        if (user != null) {
                            // Hooray! The user is logged in.
                            Toast.makeText(LoginActivity.this, "Successful sign-in", Toast.LENGTH_LONG).show();
                            startActivity(toMainActivity);
                        } else {
                            // Signup failed. Look at the ParseException to see what happened.
                            Toast.makeText(LoginActivity.this, "Sign-in Failed :"+e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent takeRegPage= new Intent(LoginActivity.this,Registration.class);
                startActivity(takeRegPage);
            }
        });



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
