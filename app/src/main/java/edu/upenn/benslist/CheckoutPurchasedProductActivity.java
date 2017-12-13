package edu.upenn.benslist;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

/**
 * Created by johnquinn on 3/14/17.
 */

public class CheckoutPurchasedProductActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    private Product product;
    private DatabaseReference mDatabase;
    private String rating;
    private String transactionID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_purchased_product);
        this.product = (Product) getIntent().getExtras().getSerializable("Product");
        mDatabase = FirebaseDatabase.getInstance().getReference().child("transactions");

//        Button checkOutUploadersProfileButton =
//                (Button) findViewById(R.id.detailedListingCheckUploadersPage);
//        checkOutUploadersProfileButton.setOnClickListener(this);
//        checkOutUploadersProfileButton.setText("Check Out " + product.getUploaderName() + "'s Profile");
        Spinner spinner = (Spinner) findViewById(R.id.userRatingSpinner);
        spinner.setOnItemSelectedListener(this);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.user_rating_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        this.rating = "1"; //default rating
        setView();


        Button submit = (Button) findViewById(R.id.submitRating);
        submit.setOnClickListener(this);
        Button doneButton = (Button) findViewById(R.id.doneViewingProductsButton);
        doneButton.setOnClickListener(this);

        setProductTextValues();

    }

    private void setView() {
        mDatabase.orderByChild("productID").equalTo(product.getProductID()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot s : dataSnapshot.getChildren()) {
                    transactionID = s.getKey();
                    if (s.child("rating").getValue() != null) {
                        TextView v = (TextView) findViewById(R.id.rateProductText);
                        v.setVisibility(View.INVISIBLE);
                        Button submit =
                                (Button) findViewById(R.id.submitRating);
                        submit.setVisibility(View.INVISIBLE);
                        Spinner spinner = (Spinner) findViewById(R.id.userRatingSpinner);
                        spinner.setVisibility(View.INVISIBLE);
                    }
                    break;
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        rating = (String) parent.getItemAtPosition(position);
    }

    public void onNothingSelected(AdapterView<?> arg0) {

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
            case (R.id.submitRating) :
                mDatabase.child(transactionID).child("rating").setValue(rating);
                TextView view = (TextView) findViewById(R.id.rateProductText);
                view.setVisibility(View.INVISIBLE);
                Button submit = (Button) findViewById(R.id.submitRating);
                submit.setVisibility(View.INVISIBLE);
                Spinner spinner = (Spinner) findViewById(R.id.userRatingSpinner);
                spinner.setVisibility(View.INVISIBLE);
                break;

            case (R.id.doneViewingProductsButton) :
                Intent returnIntent = new Intent(this, ViewUploadedPurchasedProductsActivity.class);
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
