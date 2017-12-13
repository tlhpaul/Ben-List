package edu.upenn.benslist;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by maxdoppelt on 4/1/17.
 */

public class SearchUserResultsActivity extends AppCompatActivity implements View.OnClickListener, Serializable {

    private static final int RESULT_GO_TO_FILTER_SEARCH_RESULTS = 4;
    private String searchQuery;
    private ViewGroup mLinearLayout;

    private DatabaseReference mUserReference;
    //private ValueEventListener mUserListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_results_layout);

        // Get search specifications from intent
        this.searchQuery = getIntent().getStringExtra("Search Query");

        // Initialize Database
        mUserReference = FirebaseDatabase.getInstance().getReference()
                .child("users");

        //Initialize views
        Button filterResultsButton = (Button) findViewById(R.id.filterSearchResultsButton);
        filterResultsButton.setOnClickListener(this);
        Button backToHomePageButton = (Button) findViewById(R.id.goBackToHomePageFromSearchResultsButton);
        backToHomePageButton.setOnClickListener(this);

        mLinearLayout = (ViewGroup) findViewById(R.id.searchResultsLinearLayout);
    }

    @Override
    public void onStart() {
        super.onStart();

        ValueEventListener userListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<User> users = new LinkedList<>();
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    User user = userSnapshot.getValue(User.class);
                    if (user.getName().contains(searchQuery)) {
                        users.add(user);
                    }
                }
                addUsersFromSearch(users);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

                Toast.makeText(SearchUserResultsActivity.this, "Failed to load users.",
                        Toast.LENGTH_SHORT).show();
                // [END_EXCLUDE]
            }
        };

        mUserReference.addValueEventListener(userListener);
    }

    protected void addUsersFromSearch(List<User> users) {

        final Context thisContext = this;

        //add each product to the activity
        for (final User user : users) {

            View view = LayoutInflater.from(this).inflate(R.layout.product_listing_layout, mLinearLayout, false);

            TextView productName = (TextView) view.findViewById(R.id.productListingProductName);
            productName.setText("Name: " + user.getName());

            TextView productLocation = (TextView) view.findViewById(R.id.productListingProductLocation);
            productLocation.setText("Location: " + user.getAge());

            Button checkOutButton = (Button) view.findViewById(R.id.productListingCheckOutListingButton);
            checkOutButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(thisContext, UserProfileActivity.class);
                    i.putExtra("User", (Serializable) user);
                    startActivity(i);
                }
            });

            mLinearLayout.addView(view);
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case (R.id.filterSearchResultsButton) :
                Intent i = new Intent(this, FilterSearchResultsActivity.class);
                startActivityForResult(i, RESULT_GO_TO_FILTER_SEARCH_RESULTS);
                break;
            case (R.id.goBackToHomePageFromSearchResultsButton) :
                Intent intent = new Intent(this, HomePageActivity.class);
                startActivity(intent);
                break;
            default :
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
                //Need to add code for actually logging out a user
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
