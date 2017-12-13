package edu.upenn.benslist;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by johnquinn on 3/14/17.
 */

/*
This page simply displays a list of a user's favorite users that they've bought from.
 */

public class FavoriteUsersActivity extends AppCompatActivity implements View.OnClickListener {

    private String userId;
    ListView listView;
    DatabaseReference mUserReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.favorite_users);

        //User user = (User) getIntent().getSerializableExtra("User");
        this.userId = getIntent().getStringExtra("UserId");
        mUserReference = FirebaseDatabase.getInstance().getReference()
                .child("users").child(userId);



        listView = (ListView) findViewById(R.id.favoriteUsersList);


        Button doneButton = (Button) findViewById(R.id.doneViewingFavoriteUsersButton);
        doneButton.setOnClickListener(this);
    }

    protected void onStart() {
        super.onStart();

        final Context thisContext = this;

        mUserReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<String> favoriteUsers = new ArrayList<String>();
                for (DataSnapshot favoriteUser : dataSnapshot.child(
                        "favoriteUsersIveBoughtFrom").getChildren()) {
                    favoriteUsers.add(favoriteUser.getValue(String.class));
                }
                ArrayAdapter<String> itemsAdapter =
                        new ArrayAdapter<String>(thisContext, android.R.layout.simple_list_item_1,
                                favoriteUsers);
                listView.setAdapter(itemsAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case (R.id.doneViewingFavoriteUsersButton):
                Intent returnIntent = new Intent(this, ViewUsersProfileActivity.class);
                setResult(RESULT_OK, returnIntent);
                finish();
                break;

            default:
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
        Intent intent;
        switch (item.getItemId()) {
            case R.id.action_about:
                //Go to About page
                intent = new Intent(this, AboutActivity.class);
                startActivity(intent);
                return true;

            case R.id.action_home:
                //Go to Home page
                intent = new Intent(this, MainActivityNewsFeed.class);
                startActivity(intent);
                return true;

            case R.id.action_logout:
                //Logs out the current user and brings user to the logout page
                intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                return true;

            case R.id.action_terms:
                //Go to terms page
                intent = new Intent(this, TermsActivity.class);
                startActivity(intent);
                return true;

            case R.id.action_forum:
                //Go to forum page
                intent = new Intent(this, PublicForumActivity.class);
                startActivity(intent);
                return true;

            default:
                //Could not recognize a button press
                Toast.makeText(this, "Could not recognize a button press", Toast.LENGTH_SHORT).show();
                return false;
        }
    }



}
