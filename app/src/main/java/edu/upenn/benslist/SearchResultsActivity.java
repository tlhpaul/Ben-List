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
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by johnquinn on 2/16/17.
 */

public class SearchResultsActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int RESULT_GO_TO_FILTER_SEARCH_RESULTS = 4;
    private String searchCategory;
    private String searchQuery;
    private ViewGroup mLinearLayout;
    private DatabaseReference mProductReference;
    private DatabaseReference mUserReference;
    private ValueEventListener mProductListener;

    //order of listings shown
    private boolean sortByPrice;

    /*
    TODO - added in on April 13th
     */

    //search filters
    private boolean lowPriceFilter;
    private boolean medPriceFilter;
    private boolean highPriceFilter;
    private boolean closeLocationFilter;
    private boolean mediumLocationFilter;
    private boolean farLocationFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_results_layout);

        // Get search specifications from intent
        this.searchCategory = getIntent().getStringExtra("Search Category");
        this.searchQuery = getIntent().getStringExtra("Search Query");
        this.sortByPrice = getIntent().getBooleanExtra("Sort By Price", false);

        //get search filters from intent
        this.lowPriceFilter = getIntent().getBooleanExtra("Low Price", false);
        this.medPriceFilter = getIntent().getBooleanExtra("Medium Price", false);
        this.highPriceFilter = getIntent().getBooleanExtra("High Price", false);
        this.closeLocationFilter = getIntent().getBooleanExtra("Close Location", false);
        this.mediumLocationFilter = getIntent().getBooleanExtra("Medium Location", false);
        this.farLocationFilter = getIntent().getBooleanExtra("Far Location", false);


        // Initialize Database
        mProductReference = FirebaseDatabase.getInstance().getReference()
                .child("products");
        mUserReference = FirebaseDatabase.getInstance().getReference()
                .child("users");

        //Initialize views
        Button filterResultsButton = (Button) findViewById(R.id.filterSearchResultsButton);
        filterResultsButton.setOnClickListener(this);
        Button backToHomePageButton = (Button) findViewById(R.id.goBackToHomePageFromSearchResultsButton);
        backToHomePageButton.setOnClickListener(this);
        Button sortByPriceButton = (Button) findViewById(R.id.sortResultsByPriceButton);
        sortByPriceButton.setOnClickListener(this);

        mLinearLayout = (ViewGroup) findViewById(R.id.searchResultsLinearLayout);
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseUser fbUser = FirebaseAuth.getInstance().getCurrentUser();

        ValueEventListener productListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Product> products = new LinkedList<>();
                for (DataSnapshot productSnapshot : dataSnapshot.getChildren()) {
                    Product product = productSnapshot.getValue(Product.class);

                    if (fulfillsSearchRequirements(product)) {
                        products.add(product);
                    }
                }
                addProductsFromSearch(products);
            }

            public boolean fulfillsSearchRequirements(Product product) {
              // @JOSH add condition to check blocked users to product uploader id
                //check if it fulfills the search category and search query first
                /*
                if (!product.getCategory().equals(searchCategory) || !product.getName().contains(searchQuery)) {
                    return false;
                }
                */
                if ( ! searchCategory.equalsIgnoreCase("all categories") &&( ! product.getCategory().equals(searchCategory) || !product.getName().contains(searchQuery)) ){
                    return false;
                }
                //then checks to see if it fulfills the search filter criteria
                boolean fulfillsPriceFilters = false;
                boolean fulFillsLocationFilters = false;

                if (!lowPriceFilter && !medPriceFilter && !highPriceFilter) {
                    fulfillsPriceFilters = true;
                }
                else {
                    if (lowPriceFilter) {
                        fulfillsPriceFilters = fulfillsPriceFilters || (product.getPriceCategory() == 1);
                    }

                    if (medPriceFilter) {
                        fulfillsPriceFilters = fulfillsPriceFilters || (product.getPriceCategory() == 2);
                    }

                    if (highPriceFilter) {
                        fulfillsPriceFilters = fulfillsPriceFilters || (product.getPriceCategory() == 3);
                    }
                }

                if (!closeLocationFilter && !mediumLocationFilter && !farLocationFilter) {
                    fulFillsLocationFilters = true;
                }
                else {
                    if (closeLocationFilter) {
                        fulFillsLocationFilters = fulFillsLocationFilters || (product.getLocationCategory() == 1);
                    }

                    if (mediumLocationFilter) {
                        fulFillsLocationFilters = fulFillsLocationFilters || (product.getLocationCategory() == 2);
                    }

                    if (farLocationFilter) {
                        fulFillsLocationFilters = fulFillsLocationFilters || (product.getLocationCategory() == 3);
                    }
                }

                return (fulfillsPriceFilters && fulFillsLocationFilters);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

                Toast.makeText(SearchResultsActivity.this, "Failed to load products.",
                        Toast.LENGTH_SHORT).show();
                // [END_EXCLUDE]
            }
        };

        mProductReference.addValueEventListener(productListener);

    }

    protected void addProductsFromSearch(List<Product> products) {

        if (sortByPrice) {
            Collections.sort(products);
        }

        final Context thisContext = this;

        //add each product to the activity
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
            uploaderName.setText("Uploader Name: " + product.getUploaderName());


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

            Button checkOutButton = (Button) view.findViewById(R.id.productListingCheckOutListingButton);
            checkOutButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(thisContext, CheckoutProductActivity.class);
                    i.putExtra("Product", (Serializable) product);
                    startActivity(i);
                }
            });



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
            case (R.id.filterSearchResultsButton) :
                Intent i = new Intent(this, FilterSearchResultsActivity.class);
                i.putExtra("Search Category", searchCategory);
                i.putExtra("Search Query", searchQuery);
                startActivity(i);
                break;

            case (R.id.goBackToHomePageFromSearchResultsButton) :
                Intent intent = new Intent(this, MainActivityNewsFeed.class);
                startActivity(intent);
                break;

            case (R.id.sortResultsByPriceButton) :
                Intent newIntent = new Intent(this, SearchResultsActivity.class);

                newIntent.putExtra("Search Category", searchCategory);
                newIntent.putExtra("Search Query", searchQuery);
                newIntent.putExtra("Sort By Price", true);

                newIntent.putExtra("Low Price", lowPriceFilter);
                newIntent.putExtra("Medium Price", medPriceFilter);
                newIntent.putExtra("High Price", highPriceFilter);
                newIntent.putExtra("Close Location", closeLocationFilter);
                newIntent.putExtra("Medium Location", mediumLocationFilter);
                newIntent.putExtra("Far Location", farLocationFilter);

                startActivity(newIntent);
                break;

            default :
                break;
        }
    }


    //This isn't used as of April 13th
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {


        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_GO_TO_FILTER_SEARCH_RESULTS && resultCode == RESULT_OK) {

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
