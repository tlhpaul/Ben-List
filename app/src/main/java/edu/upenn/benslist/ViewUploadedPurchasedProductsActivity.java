package edu.upenn.benslist;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by johnquinn on 4/5/17.
 */


/*
This DOESN'T just display the products a user has uploaded. It can also display the products
that a user has bought in the past.
 */

public class ViewUploadedPurchasedProductsActivity extends AppCompatActivity implements View.OnClickListener {

    private User user;
    private String userId;
    private String type;
    private ViewGroup mLinearLayout;
    private DatabaseReference mUserReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_uploaded_products_layout);
        this.userId = getIntent().getStringExtra("UserId");
        this.type = (String) getIntent().getStringExtra("Type");
        mUserReference = FirebaseDatabase.getInstance().getReference()
                .child("users").child(userId);

        Button doneButton = (Button) findViewById(R.id.doneViewingProductsButton);

        doneButton.setOnClickListener(this);

        mLinearLayout = (ViewGroup) findViewById(R.id.uploadedProductsLinearLayout);


    }

    protected void onStart() {
        super.onStart();
        mLinearLayout.removeAllViewsInLayout();

        mUserReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue(String.class);
                if (type.equals("uploads")) {
                    List<Product> productsIveUploaded = new LinkedList<>();
                    for (DataSnapshot productSnapshot : dataSnapshot.child(
                            "productsIveUploaded").getChildren()) {
                        Product product = productSnapshot.getValue(Product.class);
                        productsIveUploaded.add(product);
                    }
                    addProductsToView(productsIveUploaded, name);
                }
                else if (type.equals("previousPurchases")) {
                    List<Product> productsIveBought = new LinkedList<>();
                    for (DataSnapshot productSnapshot : dataSnapshot.child(
                            "productsIveBought").getChildren()) {
                        Product product = productSnapshot.getValue(Product.class);
                        productsIveBought.add(product);
                    }
                    addProductsToView(productsIveBought, name);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



    }

    private void addProductsToView(List<Product> products, String name) {
        //add each product to the activity
        final Context thisContext = this;

        for (final Product product : products) {

            View view = LayoutInflater.from(this).inflate(R.layout.product_listing_layout, mLinearLayout, false);

            TextView productName = (TextView) view.findViewById(R.id.productListingProductName);
            productName.setText("Name: " + product.getName());

            TextView productDescription = (TextView) view.findViewById(R.id.productListingProductDescription);
            productDescription.setText("Description: " + product.getDescription());

            TextView productPrice = (TextView) view.findViewById(R.id.productListingProductPrice);
            productPrice.setText("Price: " + product.getPrice());

            TextView productLocation = (TextView) view.findViewById(R.id.productListingProductLocation);
            productLocation.setText("Location: " + product.getLocation());

            TextView uploaderPhoneNumber = (TextView) view.findViewById(R.id.productListingUploaderPhoneNumber);
            uploaderPhoneNumber.setText("Phone Number: " + product.getPhoneNumber());

            TextView uploaderName = (TextView) view.findViewById(R.id.productListingUploaderName);
            uploaderName.setText("Uploader Name: " + name);

            Button checkOutButton = (Button) view.findViewById(R.id.productListingCheckOutListingButton);
            checkOutButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i;
                    if (type.equals("uploads")){
                        i = new Intent(thisContext, CheckoutProductActivity.class);
                    }else{
                        i = new Intent(thisContext,CheckoutPurchasedProductActivity.class);
                    }
                    i.putExtra("Product", (Serializable) product);
                    startActivity(i);
                }
            });

            if (product.getPicUrl() != null){
                Log.d("url", product.getPicUrl());
                ImageView productPic = (ImageView) view.findViewById(R.id.productPicture);
                try{
                    Bitmap image = decodeFromFirebaseBase64(product.getPicUrl());
                    productPic.setImageBitmap(image);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            mLinearLayout.addView(view);
        }
    }

    private static Bitmap decodeFromFirebaseBase64(String image) throws IOException {
        byte[] decodedString = Base64.decode(image, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedString , 0, decodedString .length);
    }



    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case (R.id.doneViewingProductsButton) :
                Intent returnIntent = new Intent(this, ViewUsersProfileActivity.class);
                setResult(RESULT_OK, returnIntent);
                finish();
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
