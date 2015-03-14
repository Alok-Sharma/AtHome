package aloksharma.ufl.edu.athome;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

//import com.facebook.Session;

/**
 * Created by Soham Talukdar on 3/13/2015.
 * Register a new user to the app.
 */

public class Registration extends ActionBarActivity {
protected EditText username;
protected EditText password;
protected EditText email;
protected Button  submitButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        View someView = findViewById(R.id.reg);
        View root = someView.getRootView();
        root.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_dark));
        username=(EditText)findViewById(R.id.username);
        //password.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        password=(EditText)findViewById(R.id.password);
        email=(EditText)findViewById(R.id.email);
        submitButton=(Button)findViewById(R.id.signUp);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userName = username.getText().toString().trim();
                String pwd = password.getText().toString().trim();
                String mail = email.getText().toString().trim();
                ParseUser user = new ParseUser();
                user.setUsername(userName);
                user.setPassword(pwd);
                user.setEmail(mail);
                Log.d("guitar", "Entered onclick" + mail + pwd + userName);
                Toast.makeText(Registration.this, "Connecting Server", Toast.LENGTH_LONG).show();
                user.signUpInBackground(new SignUpCallback() {
                    public void done(ParseException e) {
                        Log.d("guitar","Entered signUpInBackGround");
                        if (e == null) {
                            Toast.makeText(Registration.this, "WELCOME TO AT HOME", Toast.LENGTH_LONG).show();
                        }
                        else {
                            // Sign up didn't succeed. Look at the ParseException
                            // to figure out what went wrong
                            Log.d("guitar","ERROR: "+e.getMessage());
                            Toast.makeText(Registration.this,e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
            });
        }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_registration, menu);
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
