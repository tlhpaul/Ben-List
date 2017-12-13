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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by tylerdouglas on 4/19/17.
 */

public class EditListingActivity extends AppCompatActivity implements View.OnClickListener{

    private User user;
    private String userId;
    private String type;
    private ViewGroup mLinearLayout;
    private DatabaseReference mUserReference;
    private Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_listing_layout);
        this.userId = getIntent().getStringExtra("UserId");
        this.type = "uploads";
        mUserReference = FirebaseDatabase.getInstance().getReference()
                .child("users").child(userId);

        Button doneButton = (Button) findViewById(R.id.doneViewingProductsButton);

        doneButton.setOnClickListener(this);

        mLinearLayout = (ViewGroup) findViewById(R.id.uploadedProductsLinearLayout);
    }

    protected void onStart() {
        super.onStart();

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

            View view = LayoutInflater.from(this).inflate(R.layout.edit_products_listing_layout, mLinearLayout, false);
            setProductName(view, product);
            setProductDescription(view, product);
            setPrice(view, product);
            setProductLocation(view, product);
            setUploaderNameAndPhoneNumber(view, product, name);

//            TextView productName = (TextView) view.findViewById(R.id.productListingProductName);
//            System.out.println("product name is: " + product.getName());
//            productName.setText("Name: " + product.getName());
//
//            TextView productDescription = (TextView) view.findViewById(R.id.productListingProductDescription);
//            System.out.println("product description is: " + product.getDescription());
//
//            productDescription.setText("Description: " + product.getDescription());
//
//            TextView productPrice = (TextView) view.findViewById(R.id.productListingProductPrice);
//            productPrice.setText("Price: " + product.getPrice());
//
//            TextView productLocation = (TextView) view.findViewById(R.id.productListingProductLocation);
//            productLocation.setText("Location: " + product.getLocation());
//
//            TextView uploaderPhoneNumber = (TextView) view.findViewById(R.id.productListingUploaderPhoneNumber);
//            uploaderPhoneNumber.setText("Phone Number: " + product.getPhoneNumber());

//            TextView uploaderName = (TextView) view.findViewById(R.id.productListingUploaderName);
//            uploaderName.setText("Uploader Name: " + name);

            Button checkOutButton = (Button) view.findViewById(R.id.editProductListing);
            checkOutButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent editProduct = new Intent(thisContext, EditIndividualProductActivity.class);
                    editProduct.putExtra("Username", userId);
                    editProduct.putExtra("Product", (Serializable) product);
                    startActivityForResult(editProduct, 15);
                }
            });

            mLinearLayout.addView(view);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 15) {
            System.out.println("refreshing activity");
            Intent refresh = new Intent(this, EditListingActivity.class);
            FirebaseUser fbUser = FirebaseAuth.getInstance().getCurrentUser();
            String currentUserID = fbUser.getUid();
            refresh.putExtra("UserId", currentUserID);
            startActivity(refresh);
            this.finish();
        }

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
        this.menu = menu;
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

    private void setProductName(View view, Product product){
        TextView productName = (TextView) view.findViewById(R.id.productListingProductName);
        System.out.println("product name is: " + product.getName());
        productName.setText("Name: " + product.getName());
    }

    private void setProductDescription(View view, Product product){
        TextView productDescription = (TextView) view.findViewById(R.id.productListingProductDescription);
        System.out.println("product description is: " + product.getDescription());

        productDescription.setText("Description: " + product.getDescription());
    }

    private void setPrice(View view, Product product){
        TextView productPrice = (TextView) view.findViewById(R.id.productListingProductPrice);
        productPrice.setText("Price: " + product.getPrice());
    }

    private void setProductLocation(View view, Product product){
        TextView productLocation = (TextView) view.findViewById(R.id.productListingProductLocation);
        productLocation.setText("Location: " + product.getLocation());
    }

    private void setUploaderNameAndPhoneNumber(View view, Product product, String name){
        TextView uploaderPhoneNumber = (TextView) view.findViewById(R.id.productListingUploaderPhoneNumber);
        uploaderPhoneNumber.setText("Phone Number: " + product.getPhoneNumber());
        TextView uploaderName = (TextView) view.findViewById(R.id.productListingUploaderName);
        uploaderName.setText("Uploader Name: " + name);
    }

}
