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

import java.util.LinkedList;
import java.util.List;

//seems to be unused
/**
 * Created by johnquinn on 4/5/17.
 */


public class ViewUsersProfileActivity extends AppCompatActivity implements View.OnClickListener {

    private User user;
    private List<String> blockedUsers;
    private String userId;
    private String mUserId;
    private String name;
    private static final int RESULT_VIEW_UPLOADED_PRODUCTS = 10;
    private static final int RESULT_VIEW_FAVORITE_USERS = 11;
    private static final int RESULT_VIEW_PREVIOUS_PURCHASES = 12;
    TextView usersNameText;
    TextView usersAgeText;
    TextView usersRatingText;

    DatabaseReference mUserReference;
    //DatabaseReference mCurrentReference;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_users_profile_layout);
        this.userId = getIntent().getStringExtra("UserId");
        name = "";
        blockedUsers = new LinkedList<>();

        Button viewProductsTheyveBoughtButton = (Button) findViewById(R.id.viewUploadedProductsButton);
        Button viewFavoriteUsersButton = (Button) findViewById(R.id.viewFavoriteUsersButton);
        Button previousPurchasesButton = (Button) findViewById(R.id.viewPreviousPurchasesButton);
        Button reportUserButton = (Button) findViewById(R.id.reportUserButton);

        Button messageUserButton = (Button) findViewById(R.id.messageUserButton);

        usersNameText = (TextView) findViewById(R.id.usersNameTextField);
        usersAgeText = (TextView) findViewById(R.id.usersAgeTextField);
        usersRatingText = (TextView) findViewById(R.id.usersRatingTextField);

        mUserReference = FirebaseDatabase.getInstance().getReference()
                .child("users").child(userId);

        viewProductsTheyveBoughtButton.setOnClickListener(this);
        viewFavoriteUsersButton.setOnClickListener(this);
        previousPurchasesButton.setOnClickListener(this);
        reportUserButton.setOnClickListener(this);

        messageUserButton.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        ValueEventListener productListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                name = dataSnapshot.child("name").getValue(String.class);

                usersNameText.setText("User's Name: " + name);
                usersAgeText.setText("User's Age: " + dataSnapshot.child("age").getValue(String.class));

                //TODO
                String rating = "";
                if (dataSnapshot.child("rating").getValue() != null)  {
                   rating = dataSnapshot.child("rating").getValue().toString();
                }

                usersRatingText.setText("User's Rating: " + rating);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

                Toast.makeText(ViewUsersProfileActivity.this, "Failed to load products.",
                        Toast.LENGTH_SHORT).show();
                // [END_EXCLUDE]
            }
        };

        mUserReference.addValueEventListener(productListener);


    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case (R.id.viewUploadedProductsButton) :
                Intent newIntent = new Intent(this, ViewUploadedPurchasedProductsActivity.class);
                newIntent.putExtra("UserId", userId);
                newIntent.putExtra("Type", "uploads");
                startActivityForResult(newIntent, RESULT_VIEW_UPLOADED_PRODUCTS);
                break;

            case (R.id.viewPreviousPurchasesButton) :
                Intent i = new Intent(this, ViewUploadedPurchasedProductsActivity.class);
                i.putExtra("UserId", userId);
                i.putExtra("Type", "previousPurchases");
                startActivityForResult(i, RESULT_VIEW_PREVIOUS_PURCHASES);
                break;

            case (R.id.viewFavoriteUsersButton) :
                Intent intent = new Intent(this, FavoriteUsersActivity.class);
                intent.putExtra("UserId", userId);
                startActivityForResult(intent, RESULT_VIEW_FAVORITE_USERS);
                break;

            case (R.id.reportUserButton) :

                FirebaseUser mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                //Get the current users information
                mUserId = mFirebaseUser.getUid();
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference().
                        child("users").child(mUserId).child("blockedUsers").push();
                ref.setValue(userId);
                Intent iHome = new Intent(this, HomePageActivity.class);
                startActivity(iHome);
                break;

            case (R.id.messageUserButton) :
                Intent mIntent = new Intent(this, InboxMessageActivity.class);
                mIntent.putExtra("UserId", userId);
                mIntent.putExtra("Name", name);
                startActivity(mIntent);
                break;

            default :
                break;
        }
    }

//    protected void blockUser() {
//        // Initialize Firebase User
//        FirebaseUser mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
//
//
//        //Get the current users information
//        mUserId = mFirebaseUser.getUid();
//        mCurrentReference = FirebaseDatabase.getInstance().getReference().
//                child("users").child(mUserId);
//        ValueEventListener blockedUserListener = new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                for(DataSnapshot userSnapshot : dataSnapshot.child("blockedUsers").getChildren()) {
//                    String curr = userSnapshot.getValue(String.class);
//                    //Used for testing purposes
//                    System.out.println("CURRENT USER IN THE LINKED LIST: " + curr);
//                    blockedUsers.add(curr);
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//                Toast.makeText(ViewUsersProfileActivity.this, "Failed to load user's name.",
//                        Toast.LENGTH_SHORT).show();
//            }
//        };
//
//        mCurrentReference.addValueEventListener(blockedUserListener);
//    }


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
