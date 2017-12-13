package edu.upenn.benslist;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.StringSignature;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.like.LikeButton;
import com.like.OnLikeListener;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Stack;

public class MainActivityNewsFeed extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    // transaction activity

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

    // HomePage activity
    private static final int RESULT_UPLOAD_PRODUCT = 2;
    FirebaseUser fbUser;
    private String currentUserName;
    DatabaseReference mUserReference;

    // nav profile
    DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_news_feed);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // transaction activity
        reference = FirebaseDatabase.getInstance().getReference().child("transactions");
        userReference = FirebaseDatabase.getInstance().getReference().child("users");
        purchasedProductReference = FirebaseDatabase.getInstance().getReference().child("purchasedProducts");
        currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        likeReference = userReference.child(currentUserID).child("like");

        mLinearLayout = (ViewGroup) findViewById(R.id.newsFeedLinearLayout);
        currentTime = System.currentTimeMillis();

        Button moreButton = (Button) findViewById(R.id.doneViewingProductsButton);
        moreButton.setOnClickListener(this);

        // homepage
        mUserReference = FirebaseDatabase.getInstance().getReference()
                .child("users").child(currentUserID);

        //show nav photo
        setNavProfile();

        //// Get the intent, verify the action and get the query
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            //doMySearch(query);
            Toast.makeText(MainActivityNewsFeed.this, "search:"+query,
                    Toast.LENGTH_SHORT).show();
        }

        //transaction
        queryTransactions(reference.limitToLast(5));

    }



    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_activity_news_feed, menu);

        final SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search_view)
                .getActionView();
        if (null != searchView) {
            searchView.setSearchableInfo(searchManager
                    .getSearchableInfo(getComponentName()));
            searchView.setIconifiedByDefault(false);
        }

        SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
            public boolean onQueryTextChange(String newText) {
                // this is your adapter that will be filtered
                return true;
            }

            public boolean onQueryTextSubmit(final String query) {
                //Here u can get the value "query" which is entered in the search box.
                //Toast.makeText(MainActivityNewsFeed.this, "Searching User: " + query,
                //        Toast.LENGTH_SHORT).show();

                Intent searchIntent = new Intent(MainActivityNewsFeed.this, SearchableUser.class);
                searchIntent.putExtra(SearchManager.QUERY, query);

                Bundle appData = new Bundle();
                appData.putBoolean(SearchableUser.JARGON, true); // put extra data to Bundle
                searchIntent.putExtra(SearchManager.APP_DATA, appData); // pass the search context data
                searchIntent.setAction(Intent.ACTION_SEARCH);

                startActivity(searchIntent);
                //searchUser(query);

                return false;
            }
        };
        searchView.setOnQueryTextListener(queryTextListener);

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_upload_product) {
            // Handle the camera action
            Intent i = new Intent(this, UploadProductActivity.class);
            i.putExtra("Logged In User Name", currentUserName);
            startActivityForResult(i, RESULT_UPLOAD_PRODUCT);

        } else if (id == R.id.nav_edit_listing) {

            Intent editUploadedProduct = new Intent(this, EditListingActivity.class);
            editUploadedProduct.putExtra("UserId", currentUserID);
            startActivity(editUploadedProduct);

        } else if (id == R.id.nav_inbox) {

            Intent inboxIntent = new Intent(this, InboxActivity.class);
            startActivity(inboxIntent);

        } else if (id == R.id.nav_user_profile) {

            Intent profileIntent = new Intent(this, UserProfileActivity.class);
            startActivity(profileIntent);

        } else if (id == R.id.nav_search_product) {

            Intent intent = new Intent(this, SearchPageActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_search_user) {

            Intent searchIntent = new Intent(this, SearchUsers.class);
            startActivity(searchIntent);

        }
         else if (id == R.id.nav_sign_out) {
        Intent searchIntent = new Intent(this, LoginActivity.class);
        startActivity(searchIntent);

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }



    protected void onStart() {
        super.onStart();

        // homepage
        mUserReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                currentUserName = dataSnapshot.child("name").getValue(String.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



    }

    //transaction

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
                        Glide.with(MainActivityNewsFeed.this)
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

    protected void setNavProfile() {
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(currentUserID).hasChild("photo")) {

                    String photoUrl = dataSnapshot.child(currentUserID).child("photo").getValue(String.class);

                    if (photoUrl != "") {
                        StorageReference photoRef = FirebaseStorage.getInstance().getReferenceFromUrl(photoUrl);
                        ImageView profilePicture = (ImageView) findViewById(R.id.nav_imageView);
                        Glide.with(MainActivityNewsFeed.this)
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


}
