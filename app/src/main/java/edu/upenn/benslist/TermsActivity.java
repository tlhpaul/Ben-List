package edu.upenn.benslist;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.sendbird.android.SendBird;

import java.util.HashMap;

/**
 * Created by joshross
 */

public class TermsActivity extends AppCompatActivity {
    private HashMap<Integer, Class> actionMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms);
        actionMap = new HashMap<>();
        buildActivityMap();
    }

    /**
     * Code Snippet for adding the menu bar 3 points to select Logout, About, Home, Terms
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.tools, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (launchOptionSelectedActivity(item)) return true;
        Toast.makeText(this, "Could not recognize a button press", Toast.LENGTH_SHORT).show();
        return false;
//        Intent intent;
//
//        switch (item.getItemId()) {
//            case R.id.action_about:
//                //Go to About page
//                intent = new Intent(this, AboutActivity.class);
//                startActivity(intent);
//                return true;
//
//            case R.id.action_home:
//                //Go to Home page
//                intent = new Intent(this, HomePageActivity.class);
//                startActivity(intent);
//                return true;
//
//            case R.id.action_logout:
//                //Logs out the current user and brings user to the logout page
//                //Need to add code for actually logging out a user
//                intent = new Intent(this, LoginActivity.class);
//                startActivity(intent);
//                return true;
//
//            case R.id.action_terms:
//                //Go to terms page
//                intent = new Intent(this, TermsActivity.class);
//                startActivity(intent);
//                return true;
//
//            case R.id.action_forum:
//                //Go to forum page
//                intent = new Intent(this, PublicForumActivity.class);
//                startActivity(intent);
//                return true;
//
//            default:
//                //Could not recognize a button press
//                Toast.makeText(this, "Could not recognize a button press", Toast.LENGTH_SHORT).show();
//                return false;
//        }
    }

    /**
     * Launches the option activity selected by user
     * @param item
     * @return
     */
    private boolean launchOptionSelectedActivity(MenuItem item) {
        Class classActivity = actionMap.get(item.getItemId());
        if (classActivity == null) return false;
        Intent intent = new Intent(this, classActivity);
        startActivity(intent);
        return true;
    }

    /**
     * Builds the action map, stores action id and corresponding class
     */
    private void buildActivityMap() {
        actionMap.put(R.id.action_about, AboutActivity.class);
        actionMap.put(R.id.action_home, MainActivityNewsFeed.class);
        actionMap.put(R.id.action_logout, LoginActivity.class);
        actionMap.put(R.id.action_terms, TermsActivity.class);
        actionMap.put(R.id.action_forum, PublicForumActivity.class);
    }

}
