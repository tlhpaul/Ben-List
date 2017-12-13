package edu.upenn.benslist;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import static android.R.attr.key;
import static android.webkit.ConsoleMessage.MessageLevel.LOG;

/**
 * Created by johnquinn on 3/31/17.
 */

public class SearchUsers extends AppCompatActivity implements View.OnClickListener {

    DatabaseReference mUserReference;
    String searchQuery;
    private HashMap<Integer, Class> actionMap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_search_page_layout);

        Button searchButton = (Button) findViewById(R.id.searchUserButton);
        searchButton.setOnClickListener(this);

        Button doneButton = (Button) findViewById(R.id.goBackToHomePageButton);
        doneButton.setOnClickListener(this);
        mUserReference = FirebaseDatabase.getInstance().getReference()
                .child("users");
        actionMap = new HashMap<>();
        buildActivityMap();
    }

    @Override
    public void onClick(View v) {
        SearchView searchView = (SearchView) findViewById(R.id.editUserSearch);
        searchQuery = searchView.getQuery().toString();
        final Context thisContext = this;
        FirebaseUser fbUser = FirebaseAuth.getInstance().getCurrentUser();
        final String mUserID = fbUser.getUid();

        switch (v.getId()) {
            case (R.id.searchUserButton):

                ValueEventListener userListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Set<String> mBlockedUsers = new HashSet<String>();
                        for (DataSnapshot userSnapshot : dataSnapshot.child(mUserID).child(
                                "blockedUsers").getChildren()) {
                            mBlockedUsers.add(userSnapshot.getValue(String.class));
                        }
                        boolean isBlocked = false;
                        for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                            //Get the blocked users of the current user you are looking at

                            if (userSnapshot.child("name").getValue(String.class) == null) {
//                                Toast.makeText(thisContext,"Cannot find any user.", Toast.LENGTH_SHORT).show();
//                                break;
                                continue;
                            }
                            String key = userSnapshot.getKey().toString();

                            //User user = userSnapshot.getValue(User.class);
                            if (userSnapshot.child("name").getValue(String.class).equals(searchQuery)) {
                                if (mBlockedUsers.contains(key)) {
                                    isBlocked = true;
                                    Toast.makeText(SearchUsers.this, "You have blocked this user.",
                                            Toast.LENGTH_SHORT).show();
                                    break;
                                }
                                //Get the blocked users of the current user you are looking at
                                for (DataSnapshot blockedUserSnapshot : userSnapshot.child(
                                        "blockedUsers").getChildren()) {
                                    if (blockedUserSnapshot.getValue(String.class).equals(mUserID)) {
                                        isBlocked = true;
                                        Toast.makeText(SearchUsers.this, "This user has blocked you.",
                                                Toast.LENGTH_SHORT).show();
                                        break;
                                    }
                                }
                                if (!isBlocked) {
                                    isBlocked = true;
                                    Intent i = new Intent(thisContext, ViewUsersProfileActivity.class);
                                    i.putExtra("UserId", userSnapshot.getKey());
                                    startActivity(i);
                                }
                            }
                        }
                        if (!isBlocked) {
                            Toast.makeText(SearchUsers.this, "Cannot find user.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                        Toast.makeText(SearchUsers.this, "Failed to find user.",
                                Toast.LENGTH_SHORT).show();
                        // [END_EXCLUDE]
                    }
                };

                mUserReference.addValueEventListener(userListener);


                break;

            case (R.id.goBackToHomePageButton):
                Intent intent = new Intent(this, MainActivityNewsFeed.class);
                startActivity(intent);
                break;
        }

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
