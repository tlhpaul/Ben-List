package edu.upenn.benslist;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.StringSignature;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.like.LikeButton;
import com.like.OnLikeListener;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;


public class TransactionActivity extends AppCompatActivity implements View.OnClickListener{

    private TextView mTextMessage;
    private DatabaseReference reference;
    private DatabaseReference userReference;
    private DatabaseReference purchasedProductReference;
    private DatabaseReference likeReference;
    private ViewGroup mLinearLayout;
    private String buyerName;
    private String sellerName;
    private String productName;

    private String currentUserID;
    private long currentTime;
    private long nextFiveStartFrom;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    mTextMessage.setText(R.string.title_home);
                    return true;
                case R.id.navigation_dashboard:
                    mTextMessage.setText(R.string.title_dashboard);
                    return true;
                case R.id.navigation_notifications:
                    mTextMessage.setText(R.string.title_notifications);
                    return true;
            }
            return false;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.newsfeed_layout);

        mTextMessage = (TextView) findViewById(R.id.message);

        reference = FirebaseDatabase.getInstance().getReference().child("transactions");
        userReference = FirebaseDatabase.getInstance().getReference().child("users");
        purchasedProductReference = FirebaseDatabase.getInstance().getReference().child("purchasedProducts");
        currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        likeReference = userReference.child(currentUserID).child("like");

        mLinearLayout = (ViewGroup) findViewById(R.id.newsFeedLinearLayout);
        currentTime = System.currentTimeMillis();

        Button moreButton = (Button) findViewById(R.id.doneViewingProductsButton);
        moreButton.setOnClickListener(this);
    }


    protected void onStart() {
        super.onStart();

        queryTransactions(reference.limitToLast(5));

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case (R.id.doneViewingProductsButton):
                queryTransactions(reference.orderByChild("timeStamp").endAt(nextFiveStartFrom).limitToLast(6));

        }
    }

    public void queryTransactions(Query query) {
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //Get a DataSnapshot for the location at the specified relative path

                Stack<Transaction> stack = new Stack<>();
                boolean removeLast = false;
                if (nextFiveStartFrom != 0) removeLast = true;
                for (DataSnapshot transactionSnapshot : dataSnapshot.getChildren()) {
                    String buyerId = transactionSnapshot.child("buyerID").getValue(String.class);
                    String sellerId = transactionSnapshot.child("sellerID").getValue(String.class);
                    String productId = transactionSnapshot.child("productID").getValue(String.class);
                    String rating = transactionSnapshot.child("rating").getValue(String.class);
                    long time = transactionSnapshot.child("timeStamp").getValue(Long.class);
                    if (stack.isEmpty()) nextFiveStartFrom = time;
                    Transaction transaction = new Transaction(buyerId, sellerId, productId, time, rating);
                    transaction.setTransactionID(transactionSnapshot.getKey());
                    stack.push(transaction);
                }
                if (removeLast) stack.pop();
                List<Transaction> transactions = new LinkedList<>();
                while (!stack.isEmpty()) transactions.add(stack.pop());
                addTransactionsToView(transactions);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }



    private void addTransactionsToView(List<Transaction> transactions){
        //final Context thisContext = this;
        for (final Transaction transaction : transactions) {
            
            View view = LayoutInflater.from(this).inflate(R.layout.single_newsfeed_layout, mLinearLayout, false);
            setBuyerName(view, transaction);
            setSellerName(view, transaction);
            setProductName(view, transaction);
            setPicture(view, transaction);
            setTime(view, transaction);
            setRating(view, transaction);
            setProfilePicture(view, transaction);
            setLikeNum(view, transaction);
            setLikeButton(view, transaction);
            view.setTag(transaction.getTransactionID());
            String test = (String)view.getTag();
            mLinearLayout.addView(view);
        }

    }

    private void setProfilePicture(View view, Transaction transaction) {
        String buyerID = transaction.getBuyerID();
        DatabaseReference buyer = userReference.child(buyerID);
        final View v = view;
        buyer.addListenerForSingleValueEvent(new ValueEventListener(){
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("photo")) {
                    ImageView profilePicture = (ImageView) v.findViewById(R.id.transactionProfilePhoto);
                    String photoUrl = dataSnapshot.child("photo").getValue(String.class);

                    if (photoUrl != "") {
                        StorageReference photoRef = FirebaseStorage.getInstance().getReferenceFromUrl(photoUrl);
                        Glide.with(TransactionActivity.this)
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

    private void setPicture(View view, Transaction transaction){
        String productID = transaction.getProductID();
        DatabaseReference product = purchasedProductReference.child(productID);
        final View v = view;
        product.addListenerForSingleValueEvent(new ValueEventListener(){
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String productPictureUrl = dataSnapshot.child("picUrl").getValue(String.class);
                //Log.d("urlprev", productPictureUrl);
                if (productPictureUrl != null) {
                    Log.d("url", productPictureUrl);
                    ImageView productPic = (ImageView) v.findViewById(R.id.productPicture);

                    try{
                        Bitmap image = decodeFromFirebaseBase64(productPictureUrl);
                        productPic.setImageBitmap(image);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private static Bitmap decodeFromFirebaseBase64(String image) throws IOException {
        byte[] decodedString = Base64.decode(image, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedString , 0, decodedString .length);
    }



    private void setBuyerName(View view, Transaction transaction) {
        String buyerID = transaction.getBuyerID();
        DatabaseReference buyer = userReference.child(buyerID);
        final View v = view;
        buyer.addListenerForSingleValueEvent(new ValueEventListener(){
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                buyerName = dataSnapshot.child("name").getValue(String.class);
                TextView newsFeedBuyer = (TextView) v.findViewById(R.id.newsFeedBuyerName);
                newsFeedBuyer.setText(buyerName + " buys ");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    private void setSellerName(View view, Transaction transaction) {
        String sellerID = transaction.getSellerID();
        DatabaseReference seller = userReference.child(sellerID);
        final View v = view;
        seller.addListenerForSingleValueEvent(new ValueEventListener(){
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                sellerName = dataSnapshot.child("name").getValue(String.class);
                TextView newsFeedSeller = (TextView) v.findViewById(R.id.newsFeedSellerName);
                newsFeedSeller.setText(sellerName + "'s ");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    private void setProductName(View view, Transaction transaction) {
        String productID = transaction.getProductID();
        DatabaseReference product = purchasedProductReference.child(productID);
        final View v = view;
        product.addListenerForSingleValueEvent(new ValueEventListener(){
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                productName = dataSnapshot.child("name").getValue(String.class);
                TextView newsFeedProduct = (TextView) v.findViewById(R.id.newsFeedProductName);
                newsFeedProduct.setText(productName);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void setLikeButton(View view, final Transaction transaction) {
        final LikeButton likeButton = (LikeButton) view.findViewById(R.id.like_button);
        final View v = view;
        likeReference.orderByKey().equalTo("like" + transaction.getTransactionID()).addListenerForSingleValueEvent(new ValueEventListener(){
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    likeButton.setLiked(true);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        likeButton.setOnLikeListener(new OnLikeListener() {
            @Override
            public void liked(LikeButton likeButton) {
                likeReference.child("like" + transaction.getTransactionID()).setValue(transaction.getTransactionID());
                reference.child(transaction.getTransactionID()).child("like").child("user"+currentUserID).setValue(currentUserID);
                setLikeNum(v, transaction);

            }

            @Override
            public void unLiked(LikeButton likeButton) {
                likeReference.child("like" + transaction.getTransactionID()).removeValue();
                reference.child(transaction.getTransactionID()).child("like").child("user"+currentUserID).removeValue();
                setLikeNum(v, transaction);
            }
        });

    }

    private void setLikeNum(View view, Transaction transaction) {
        String transactionID = transaction.getTransactionID();
        DatabaseReference transactionRef = reference.child(transactionID);
        final View v = view;
        transactionRef.addListenerForSingleValueEvent(new ValueEventListener(){
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long likeNum = dataSnapshot.child("like").getChildrenCount();
                TextView newsFeedProduct = (TextView) v.findViewById(R.id.likeNum);
                newsFeedProduct.setText("" + likeNum);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void setTime(View view, Transaction transaction) {
        TextView time = (TextView) view.findViewById(R.id.newsFeedTime);
        time.setText(convertTime(transaction.getTime()));
//        String a = convertTime(transaction.getTime());
//        int b = 1;
    }

    private void setRating(View view, Transaction transaction) {
        TextView ratingView = (TextView) view.findViewById(R.id.newsFeedRating);
        String rating = transaction.getRating();
        if(rating == null) ratingView.setText("Not yet");
        else ratingView.setText("Rating:" + rating);
    }

    private String convertTime(long transactionTime) {
        long timeAgo = (currentTime - transactionTime) / 1000;
        int second = (int)timeAgo ;
        if(second < 60) return "just now";

        int minute = Math.round(timeAgo / 60 );
        if (minute == 1) return "1 minute ago";
        else if (minute < 60) return minute + " minutes ago";

        int hour = Math.round(timeAgo / 3600 );
        if (hour == 1) return "1 hour ago";
        else if (hour < 24) return hour + " hours ago";

        int day = Math.round(timeAgo / 86400);
        if (day == 1) return "1 day ago";
        else if (day < 7) return day + " days ago";

        int week = Math.round(timeAgo / 604800);
        if (week == 1) return "1 week ago";
        else if (week < 4.3) return week + " weeks ago";

        int month = Math.round(timeAgo / 2600640);
        if (month == 1) return "1 month ago";
        else if (month < 12) return month + " months ago";

        int year = Math.round (timeAgo / 31207680);
        if (year == 1) return "1 year age";
        else return year + " years ago";
    }

}



