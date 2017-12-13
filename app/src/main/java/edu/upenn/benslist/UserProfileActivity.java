package edu.upenn.benslist;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.StringSignature;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;

/**
 * Created by tylerdouglas on 3/26/17.
 */

public class UserProfileActivity extends AppCompatActivity {

    private FirebaseUser fbuser = FirebaseAuth.getInstance().getCurrentUser();
    private Boolean submitMode;

    DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("users");
    String currentUserID;

    private Menu menu;
    private boolean signUp;

    StorageReference storageRef = FirebaseStorage.getInstance().getReference();

    private static final int UPLOAD_PROFILE_PIC = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_layout);
        signUp = getIntent().getBooleanExtra("SignUp", false);
        submitMode = signUp;

        currentUserID = fbuser.getUid();
        EditText emailField = (EditText) findViewById(R.id.emailAddress);
        emailField.setText(fbuser.getEmail());



        if (! signUp) {
            setUserValues();
        }
        else {
            submitMode();
        }
        createButtons();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Detects request codes
        if(requestCode==UPLOAD_PROFILE_PIC && resultCode == Activity.RESULT_OK && data != null) {
            Uri file = data.getData();
            final StorageReference photoRef = storageRef.child("images/profilePic/" + currentUserID);
            UploadTask uploadTask = photoRef.putFile(file);


            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    String downloadUrl = photoRef.toString();
                    mDatabase.child(currentUserID).child("photo").setValue(downloadUrl);
                    Toast.makeText(UserProfileActivity.this, "Photo uploaded", Toast.LENGTH_SHORT).show();

                }
            });

        }

        setUserValues();

    }




    protected void setUserValues() {
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //User user = dataSnapshot.child(currentUserID).getValue(User.class);
                String name = dataSnapshot.child(currentUserID).child("name").getValue(String.class);
                String userAddress = dataSnapshot.child(currentUserID).child("address").getValue(String.class);
                String interests = dataSnapshot.child(currentUserID).child("interests").getValue(String.class);
                String userRating = "0";
                if (dataSnapshot.child(currentUserID).child("rating").getValue() != null) {
                    userRating = dataSnapshot.child(currentUserID).child("rating").getValue().toString();
                }

                String age = dataSnapshot.child(currentUserID).child("age").getValue(String.class);

                setTextViews(name, userAddress, interests, userRating, age);
                if (dataSnapshot.child(currentUserID).hasChild("photo")) {

                    String photoUrl = dataSnapshot.child(currentUserID).child("photo").getValue(String.class);

                    if (photoUrl != "") {
                        StorageReference photoRef = FirebaseStorage.getInstance().getReferenceFromUrl(photoUrl);
                        ImageView profilePicture = (ImageView) findViewById(R.id.profilePicture);
                        Glide.with(UserProfileActivity.this)
                                .using(new FirebaseImageLoader())
                                .load(photoRef)
                                .signature(new StringSignature(photoRef.getMetadata().toString()))
                                .into(profilePicture);
                    }
                }

            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });




    }


    private void setTextViews(String name, String userAddress, String interests, String userRating, String age) {
        EditText nameField = (EditText) findViewById(R.id.name);
        nameField.setText(name);

        EditText emailField = (EditText) findViewById(R.id.emailAddress);
        emailField.setText(fbuser.getEmail());

        EditText address = (EditText) findViewById(R.id.address);

        String homeAddress = (userAddress != null && userAddress.equals("")) ? "Enter Home Address" : userAddress ;
        address.setText(homeAddress);

        EditText interestsEditText = (EditText) findViewById(R.id.interests);
        String userInterests = (interests != null && interests.equals("")) ? "Enter Interests" : interests;
        interestsEditText.setText(userInterests);

        EditText ageText = (EditText) findViewById(R.id.age);
        ageText.setText(age);

        RatingBar ratingBar = (RatingBar) findViewById(R.id.ratingBar);
        int rating = Double.valueOf(userRating).intValue();
        ratingBar.setNumStars(5);
        ratingBar.setRating(rating);
    }

    protected void createButtons() {

        EditText nameField = (EditText) findViewById(R.id.name);
        nameField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    EditText nameFieldInner = (EditText) findViewById(R.id.name);
                    nameFieldInner.setText(event.getCharacters());
                    handled = true;
                }
                return handled;
            }
        });

        Button confirm = (Button) findViewById(R.id.confirm);
        Button viewMyProducts = (Button) findViewById(R.id.viewProductsButton);
        Button previousPurchases = (Button) findViewById(R.id.previousProducts);
        Button favoriteUsers = (Button) findViewById(R.id.favoriteUsers);
        Button profilePicUploadButton = (Button) findViewById(R.id.uploadPicButton);
        if (signUp) {
            viewMyProducts.setVisibility(View.GONE);
            previousPurchases.setVisibility(View.GONE);
            favoriteUsers.setVisibility(View.GONE);
            profilePicUploadButton.setVisibility(View.GONE);
            confirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!checkEmpty()) {
                        EditText nameField = (EditText) findViewById(R.id.name);
                        EditText address = (EditText) findViewById(R.id.address);
                        EditText interests = (EditText) findViewById(R.id.interests);
//                        EditText emailAddress = (EditText) findViewById(R.id.emailAddress);
                        EditText ageText = (EditText) findViewById(R.id.age);
//                        mDatabase.child(currentUserID).child("email").setValue(String.valueOf(emailAddress.getText()));
                        mDatabase.child(currentUserID).child("name").setValue(String.valueOf(nameField.getText()));
                        mDatabase.child(currentUserID).child("address").setValue(String.valueOf(address.getText()));
                        mDatabase.child(currentUserID).child("interests").setValue(String.valueOf(interests.getText()));
                        mDatabase.child(currentUserID).child("age").setValue(String.valueOf(ageText.getText()));
                        Intent i = new Intent(v.getContext(), MainActivityNewsFeed.class);
                        startActivity(i);
                    }
                }
            });
        }
        else {
            confirm.setVisibility(View.GONE);

            viewMyProducts.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(v.getContext(), ViewUploadedPurchasedProductsActivity.class);
                    i.putExtra("UserId", currentUserID);
                    i.putExtra("Type", "uploads");
                    startActivity(i);

                }
            });


            previousPurchases.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(v.getContext(), ViewUploadedPurchasedProductsActivity.class);
                    i.putExtra("Type", "previousPurchases");
                    i.putExtra("UserId", currentUserID);
                    startActivity(i);
                }
            });


            favoriteUsers.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(v.getContext(), FavoriteUsersActivity.class);
                    i.putExtra("UserId", currentUserID);
                    startActivity(i);
                }
            });

            profilePicUploadButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(galleryIntent, UPLOAD_PROFILE_PIC);

                }
            });
        }
    }




    /**
     * Checks if UserProfileActivity is in submitMode.
     * If SubmitMode = true then it changes all EditText Fields to editable
     * If SubmitMode = false then it changes all EditText fields to non-editable
     */
    private void submitMode() {
        EditText nameField = (EditText) findViewById(R.id.name);
        EditText address = (EditText) findViewById(R.id.address);
        EditText interests = (EditText) findViewById(R.id.interests);
        EditText ageText = (EditText) findViewById(R.id.age);

        if (submitMode) {
            nameField.setEnabled(true);
            address.setEnabled(true);
            interests.setEnabled(true);
            ageText.setEnabled(true);
        } else {
            nameField.setEnabled(false);
            address.setEnabled(false);
            interests.setEnabled(false);
            ageText.setEnabled(false);
        }
    }



    /**
     * Code Snippet for adding the menu bar 3 points to select Logout, About, Home, Terms
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        this.menu = menu;
        inflater.inflate(R.menu.tools, menu);
        inflater.inflate(R.menu.submit, menu);
        if (signUp) this.menu.setGroupVisible(0, false);
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

            case R.id.Edit:
                //Enable Editting fields
                MenuItem editButton = menu.findItem(R.id.Edit);
                if (submitMode) {
                    if (!checkEmpty()) {
                        submitMode = false;
                        EditText nameField = (EditText) findViewById(R.id.name);
                        EditText address = (EditText) findViewById(R.id.address);
                        EditText interests = (EditText) findViewById(R.id.interests);
                        EditText emailAddress = (EditText) findViewById(R.id.emailAddress);
                        EditText ageText = (EditText) findViewById(R.id.age);
                        mDatabase.child(currentUserID).child("email").setValue(String.valueOf(emailAddress.getText()));
                        mDatabase.child(currentUserID).child("name").setValue(String.valueOf(nameField.getText()));
                        mDatabase.child(currentUserID).child("address").setValue(String.valueOf(address.getText()));
                        mDatabase.child(currentUserID).child("interests").setValue(String.valueOf(interests.getText()));
                        mDatabase.child(currentUserID).child("age").setValue(String.valueOf(ageText.getText()));
                        //mDatabase.child(currentUserID).child("blockedUsers").setValue(new HashSet<String>());
                        editButton.setTitle("Edit");
                    }


                } else {
                    submitMode = true;
                    editButton.setTitle("Submit");
                }
                submitMode();
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

    private boolean checkEmpty() {
        EditText nameField = (EditText) findViewById(R.id.name);
        EditText address = (EditText) findViewById(R.id.address);
        EditText interests = (EditText) findViewById(R.id.interests);
        EditText emailAddress = (EditText) findViewById(R.id.emailAddress);
        EditText ageText = (EditText) findViewById(R.id.age);
        if (String.valueOf(nameField.getText()).equals("") || String.valueOf(emailAddress.getText()).equals("") || String.valueOf(address.getText()).equals("") || String.valueOf(interests.getText()).equals("") || String.valueOf(ageText.getText()).equals("")) {
            Toast.makeText(UserProfileActivity.this, "Please fill in all Fields", Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }
}
