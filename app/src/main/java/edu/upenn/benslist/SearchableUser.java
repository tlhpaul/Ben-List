package edu.upenn.benslist;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

//seems to be unused



public class SearchableUser extends AppCompatActivity implements View.OnClickListener {

    public static final String JARGON = "com.example.searchinterface.jargon";

    DatabaseReference mUserReference;

    private String queryresult;

    private HashMap<Integer, Class> actionMap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_users_profile_layout);

        mUserReference = FirebaseDatabase.getInstance().getReference()
                .child("users");

        handleIntent(getIntent());

        actionMap = new HashMap<>();
        buildActivityMap();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }


    private void handleIntent(Intent intent) {

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            queryresult = intent.getStringExtra(SearchManager.QUERY);
            // use the query to search the data somehow
            Toast.makeText(SearchableUser.this, "Searching User: " + queryresult,
                            Toast.LENGTH_LONG).show();

            searchUser(queryresult);


            Bundle appData = intent.getBundleExtra(SearchManager.APP_DATA);
            if (appData != null) {
                boolean jargon = appData.getBoolean(SearchableUser.JARGON);
                // use the context data to refine our search
            }
        }
    }

    public void searchUser (final String searchQuery) {

        FirebaseUser fbUser = FirebaseAuth.getInstance().getCurrentUser();
        final String mUserID = fbUser.getUid();

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
                            Toast.makeText(SearchableUser.this, "You have blocked this user.",
                                    Toast.LENGTH_SHORT).show();
                            break;
                        }
                        //Get the blocked users of the current user you are looking at
                        for (DataSnapshot blockedUserSnapshot : userSnapshot.child(
                                "blockedUsers").getChildren()) {
                            if (blockedUserSnapshot.getValue(String.class).equals(mUserID)) {
                                isBlocked = true;
                                Toast.makeText(SearchableUser.this, "This user has blocked you.",
                                        Toast.LENGTH_SHORT).show();
                                break;
                            }
                        }
                        if (!isBlocked) {
                            isBlocked = true;
                            Intent i = new Intent(SearchableUser.this, ViewUsersProfileActivity.class);
                            i.putExtra("UserId", userSnapshot.getKey());
                            startActivity(i);
                        }
                    }
                }
                if (!isBlocked) {
                    Toast.makeText(SearchableUser.this, "Cannot find user.",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

                Toast.makeText(SearchableUser.this, "Failed to find user.",
                        Toast.LENGTH_SHORT).show();
                // [END_EXCLUDE]
            }
        };

        mUserReference.addValueEventListener(userListener);

    }

    @Override
    protected void onStart() {
        super.onStart();
    }


    @Override
    public void onClick(View v) {
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.tools, menu);
        return true;
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

    public boolean onOptionsItemSelected(MenuItem item) {
        if (launchOptionSelectedActivity(item)) return true;
        Toast.makeText(this, "Could not recognize a button press", Toast.LENGTH_SHORT).show();
        return false;

    }


}
