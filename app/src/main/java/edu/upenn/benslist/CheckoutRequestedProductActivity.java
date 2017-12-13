package edu.upenn.benslist;

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
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by johnquinn on 3/14/17.
 */

public class CheckoutRequestedProductActivity extends AppCompatActivity implements View.OnClickListener {

    private Product product;
    private String productID;
    private DatabaseReference mDatabase;
    private DatabaseReference mInboxReference;
    private String senderID;
    private String requestID;
    private String currentUserID;
    private String mUsername;
    Button acceptButton;
    Button declineButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.inbox_request_more);
        this.product = (Product) getIntent().getExtras().getSerializable("Product");
        this.senderID = getIntent().getStringExtra("senderID");
        this.requestID = getIntent().getStringExtra("requestID");

        this.productID = product.getProductID();

        FirebaseUser fbUser = FirebaseAuth.getInstance().getCurrentUser();
        currentUserID = fbUser.getUid();

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mInboxReference = mDatabase.child("users").child(currentUserID).child("inbox");


        DatabaseReference mUserReference = FirebaseDatabase.getInstance().getReference().child("users").child(currentUserID);
        mUserReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mUsername = dataSnapshot.child("name").getValue(String.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        Button doneButton = (Button) findViewById(R.id.doneViewingProductsButton);
        acceptButton = (Button) findViewById(R.id.acceptButton);
        declineButton = (Button) findViewById(R.id.declineButton);

        doneButton.setOnClickListener(this);
        acceptButton.setOnClickListener(this);
        declineButton.setOnClickListener(this);

        setProductTextValues();

    }

    protected void setProductTextValues() {
        TextView productName = (TextView) findViewById(R.id.detailedListingProductName);
        productName.setText("Name: " + product.getName());

        TextView productDescription = (TextView) findViewById(R.id.detailedListingProductDescription);
        productDescription.setText("Description: " + product.getDescription());

        TextView productPrice = (TextView) findViewById(R.id.detailedListingProductPrice);
        productPrice.setText("Price: " + product.getPrice());

        TextView productLocation = (TextView) findViewById(R.id.detailedListingProductLocation);
        productLocation.setText("Location: " + product.getLocation());

        TextView uploaderPhoneNumber = (TextView) findViewById(R.id.detailedListingUploaderPhoneNumber);
        uploaderPhoneNumber.setText("Phone Number: " + product.getPhoneNumber());

        TextView uploaderName = (TextView) findViewById(R.id.detailedListingUploaderName);
        uploaderName.setText("Uploader Name: " + product.getUploaderName());
    }




    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case (R.id.doneViewingProductsButton) :
                Intent returnIntent = new Intent(this, InboxActivity.class);
                setResult(RESULT_OK, returnIntent);
                finish();
                break;

            case (R.id.acceptButton) :

                mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        Product product = snapshot.child("products").child(productID).getValue(Product.class);

                        //push product to database/user/productsIveBought
                        DatabaseReference ref = mDatabase.child("users").child(senderID).child("productsIveBought").push();
                        ref.setValue(product);

                        //remove product from database/products
                        ref = mDatabase.child("products").child(productID);
                        ref.removeValue();

                        //push product to database/purchasedProducts
                        mDatabase.child("purchasedProducts").child(productID).setValue(product);

                        //remove request from inbox
                        ref = mInboxReference.child(requestID);
                        ref.removeValue();

                        //add to transactions
                        ref = mDatabase.child("transactions").push();
                        Transaction t = new Transaction(senderID, FirebaseAuth.getInstance().getCurrentUser().getUid(), productID, ServerValue.TIMESTAMP);
                        ref.setValue(t);

                        //grey out buttons
                        acceptButton.setAlpha(.5f);
                        acceptButton.setClickable(false);

                        declineButton.setAlpha(.5f);
                        declineButton.setClickable(false);
                    }


                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }

                });

                String acceptMessageString = "accept your purchase request for "
                        + product.getName();
                sendMeassage(senderID,acceptMessageString);

                break;



            case (R.id.declineButton):

                //delete the request from user's inbox
                mInboxReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        DatabaseReference ref = mInboxReference.child(requestID);
                        ref.removeValue();

                        //grey out buttons
                        acceptButton.setAlpha(.5f);
                        acceptButton.setClickable(false);

                        declineButton.setAlpha(.5f);
                        declineButton.setClickable(false);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });//mInboxReference.addListenerForSingleValueEvent

                //TODO send a message informing the buyer
                String declineMessageString = "declines your purchase request for "
                        + product.getName();
                sendMeassage(senderID,declineMessageString);
                break;

            default :
                break;

        }
    }

    private void sendMeassage(final String receiverID, final String messageString){

        final String channelID = currentUserID.compareTo(receiverID) > 0 ?
                currentUserID + receiverID :
                receiverID + currentUserID;

        Message message = new Message(messageString, mUsername);

        mDatabase.child("inbox").child(channelID).push().setValue(message);

        final DatabaseReference mMessageDatabase = mDatabase.child("users").child(receiverID).child("inbox");

        mMessageDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean alreadyExist = false;

                for(DataSnapshot inboxSnapshot : dataSnapshot.getChildren()){
                    String senderIDString = inboxSnapshot.child("senderID").getValue(String.class);
                    String typeString = inboxSnapshot.child("type").getValue(String.class);

                    if(typeString.equals("message") && senderIDString.equals(currentUserID)){
                        alreadyExist = true;

                        String key = inboxSnapshot.getKey();
                        DatabaseReference ref = mMessageDatabase.child(key);
                        ref.removeValue();

                        MessageInbox message = new MessageInbox(messageString, currentUserID, receiverID,
                                ServerValue.TIMESTAMP);

                        ref = mMessageDatabase.push();
                        ref.setValue(message);

                        break;
                    }


                }

                if (!alreadyExist){
                    MessageInbox message = new MessageInbox(messageString, currentUserID, receiverID,
                            ServerValue.TIMESTAMP);
                    DatabaseReference ref = mMessageDatabase.push();
                    ref.setValue(message);
                }



            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

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
