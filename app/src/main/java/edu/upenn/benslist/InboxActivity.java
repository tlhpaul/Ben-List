package edu.upenn.benslist;

import android.support.v7.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;

import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.StringSignature;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.Serializable;
import java.util.HashMap;


public class InboxActivity extends AppCompatActivity
        implements View.OnClickListener {

    private String currentUserID;
    private DatabaseReference mInboxReference;
    private ViewGroup mLinearLayout;
    private DatabaseReference mDatabase;
    private String mUsername;
    private HashMap<String, View> childView = new HashMap<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
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

        mLinearLayout = (ViewGroup) findViewById(R.id.inboxLinearLayout);
    }

    private void init(){
        setContentView(R.layout.inbox_activity);
        Button doneButton = (Button) findViewById(R.id.doneViewingInboxButton);
        doneButton.setOnClickListener(this);
    }


    protected void onResume(){
        super.onResume();
        mLinearLayout.removeAllViewsInLayout();
        mLinearLayout.invalidate();
    }


    protected void onStart() {
        super.onStart();
//        mLinearLayout.removeAllViewsInLayout();

        mInboxReference.orderByChild("timestamp").limitToLast(10).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String type = dataSnapshot.child("type").getValue(String.class);
                String senderIDString = dataSnapshot.child("senderID").getValue(String.class);
                String requestIDString = dataSnapshot.getKey();

                if (type.equals("request")) {
                    String productIDString = dataSnapshot.child("productID").getValue(String.class);
                    String productNameString = dataSnapshot.child("productName").getValue(String.class);
                    addRequestInboxToView(productIDString, senderIDString, requestIDString,productNameString);
                }else if (type.equals("message")){
                    String messageString = dataSnapshot.child("message").getValue(String.class);
                    addMessaageInboxToView(senderIDString,messageString, requestIDString);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                String requestIDString = dataSnapshot.getKey();
                mLinearLayout.removeView(childView.get(requestIDString));

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



    }

    private void addRequestInboxToView(final String productID, final String senderID,
                                       final String requestID, final String productName) {
        //add each product to the activity
        final Context thisContext = this;

        System.out.println("add to view");

        View view = LayoutInflater.from(this).inflate(R.layout.inbox_request, mLinearLayout, false);
        setRequestInboxInfo(view,productID, senderID, productName);

        final Button acceptButton = (Button) view.findViewById(R.id.acceptButton);
        final Button declineButton = (Button) view.findViewById(R.id.declineButton);
        final Button moreButton = (Button) view.findViewById(R.id.moreRequestButton);

        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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

                        moreButton.setAlpha(.5f);
                        moreButton.setClickable(false);
                    }


                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }

                });

                String acceptMessageString = "accept your purchase request for "
                        + productName;
                sendMeassage(senderID,acceptMessageString);



            }
        });

        declineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

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

                        moreButton.setAlpha(.5f);
                        moreButton.setClickable(false);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });//mInboxReference.addListenerForSingleValueEvent

                //TODO send a message informing the buyer
                String declineMessageString = "declines your purchase request for "
                        + productName;
                sendMeassage(senderID,declineMessageString);

            }
        });

        moreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                DatabaseReference mProductReference = mDatabase.child("users").child(currentUserID);
                mProductReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot productSnapshot : dataSnapshot.child(
                                "productsIveUploaded").getChildren()) {
                            if (productSnapshot.child("productID").getValue(String.class).equals(productID)) {
                                Product product = productSnapshot.getValue(Product.class);
                                Intent i = new Intent(thisContext, CheckoutRequestedProductActivity.class);
                                i.putExtra("Product", (Serializable) product);
                                i.putExtra("senderID",senderID);
                                i.putExtra("requestID",requestID);


                                startActivity(i);
                            }

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


            }
        });

        mLinearLayout.addView(view,0);

    }

    private void setRequestInboxInfo(final View view,final String productID,
                                     final String senderID,final String productName) {
        //set sender's name
        DatabaseReference mUserReference = mDatabase.child("users");
        mUserReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //set sender's name
                String buyerNameString = dataSnapshot.child(senderID)
                        .child("name").getValue(String.class);
                TextView buyerNameTextView = (TextView) view.findViewById(R.id.BuyerName);
                buyerNameTextView.setText(buyerNameString);

                //set profile picture
                if (dataSnapshot.child(senderID).hasChild("photo")) {
                    ImageView profilePicture = (ImageView) view.findViewById(R.id.requestSenderPhoto);
                    String photoUrl = dataSnapshot.child(senderID).child("photo").getValue(String.class);

                    if (photoUrl != "") {
                        StorageReference photoRef = FirebaseStorage.getInstance().getReferenceFromUrl(photoUrl);
                        Glide.with(InboxActivity.this)
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

        //set product's name
        TextView productNameTextView = (TextView) view.findViewById(R.id.productName);
        productNameTextView.setText(productName);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case (R.id.doneViewingInboxButton):
                Intent returnIntent = new Intent(this, HomePageActivity.class);
                setResult(RESULT_OK, returnIntent);
                finish();
        }
    }



    private void addMessaageInboxToView(final String senderID, String message,String requestID) {
        //add message to Inbox
        final Context thisContext = this;

        System.out.println("add to view");

        View view = LayoutInflater.from(this).inflate(R.layout.inbox_message, mLinearLayout, false);
        setMessageInboxInfo(view, senderID, message);
        Integer viewID = view.getId();
        childView.put(requestID,view);




        final Button moreButton = (Button) view.findViewById(R.id.moreMessageButton);
        final Button replyButton = (Button) view.findViewById(R.id.replyButton);

        moreButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                //get sender's name and start InboxMessageActivity
                DatabaseReference mUserReference = mDatabase.child("users");
                mUserReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String senderNameString = dataSnapshot.child(senderID)
                                .child("name").getValue(String.class);
                        Intent mIntent = new Intent(thisContext, InboxMessageActivity.class);
                        mIntent.putExtra("UserId", senderID);
                        mIntent.putExtra("Name",senderNameString);
                        startActivity(mIntent);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        });

        final EditText replyMessage = (EditText) view.findViewById(R.id.replyText) ;

        replyMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    replyButton.setEnabled(true);
                } else {
                    replyButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });


        replyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                sendMeassage(senderID,replyMessage.getText().toString());
                replyButton.setAlpha(.5f);
                replyButton.setClickable(false);

                replyMessage.setText("");

            }
        });
        mLinearLayout.addView(view, 0);
    }

    private void setMessageInboxInfo(final View view, final String senderID, final String message) {
        //set sender's name
        DatabaseReference mUserReference = mDatabase.child("users");
        mUserReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //set sender's name
                String senderNameString = dataSnapshot.child(senderID)
                        .child("name").getValue(String.class);
                TextView senderName = (TextView) view.findViewById(R.id.SenderName);
                senderName.setText(senderNameString);

                //set profile picture
                if (dataSnapshot.child(senderID).hasChild("photo")) {
                    ImageView profilePicture = (ImageView) view.findViewById(R.id.messageProfilePhoto);
                    String photoUrl = dataSnapshot.child(senderID).child("photo").getValue(String.class);

                    if (photoUrl != "") {
                        StorageReference photoRef = FirebaseStorage.getInstance().getReferenceFromUrl(photoUrl);
                        Glide.with(InboxActivity.this)
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

        //set message content
        TextView messageContent = (TextView) view.findViewById(R.id.messageContent);
        messageContent.setText(message);
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


}