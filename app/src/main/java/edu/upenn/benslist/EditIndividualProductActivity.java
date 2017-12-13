package edu.upenn.benslist;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by tylerdouglas on 4/19/17.
 */

public class EditIndividualProductActivity extends AppCompatActivity implements View.OnClickListener{

    private static final int RESULT_LOAD_IMAGE = 1;
    private ImageView imageToUpload;
    private String itemCategory;
    private String currentUserName;
    private Product product;

    private String description;
    private String location;
    private String name;
    private String phoneNumber;
    private int priceCategory;
    private double distance;
    private String price;
    private double priceAsDouble;
    private int locationCategory;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_product);
        this.currentUserName = getIntent().getStringExtra("Username");
        this.product = (Product) getIntent().getExtras().getSerializable("Product");

        populateProductFields();

    }

    private void populateProductFields() {
        imageToUpload = (ImageView) findViewById(R.id.imageToUpload);
        Button uploadImageButton = (Button) findViewById(R.id.uploadPictureButton);
        Button doneButton = (Button) findViewById(R.id.doneButton);

        uploadImageButton.setOnClickListener(this);
        doneButton.setOnClickListener(this);


        Spinner spinner = (Spinner) findViewById(R.id.productCategorySpinner);
        itemCategory = product.getCategory();
        ArrayList<String> categoryOptions = new ArrayList<String>
                (Arrays.asList(getResources().getStringArray(R.array.product_categories_array)));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                itemCategory = parentView.getItemAtPosition(position).toString();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }

        });

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.product_categories_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(categoryOptions.indexOf(itemCategory));


        EditText editProductName = (EditText) findViewById(R.id.editProductName);
        editProductName.setText(product.getName());

        EditText editProductDescription = (EditText) findViewById(R.id.editProductDescription);
        editProductDescription.setText(product.getDescription());

        EditText editPrice = (EditText) findViewById(R.id.editPrice);
        editPrice.setText(Double.toString(product.getPriceAsDouble()));

        EditText editLocation = (EditText) findViewById(R.id.editLocation);
        editLocation.setText(product.getLocation());

        EditText editDistance = (EditText) findViewById(R.id.editDistance);
        editDistance.setText(Double.toString(product.getDistance()));

        EditText editPhoneNumber = (EditText) findViewById(R.id.editPhoneNumber);
        editPhoneNumber.setText(product.getPhoneNumber());
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case (R.id.uploadPictureButton) :
                Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, RESULT_LOAD_IMAGE);
                break;

            case (R.id.doneButton) :

//                Intent returnIntent = new Intent(this, EditListingActivity.class);
                final Intent returnIntent = new Intent();

                FirebaseUser fbuser = FirebaseAuth.getInstance().getCurrentUser();
                DatabaseReference mUserReference = FirebaseDatabase.getInstance().getReference()
                        .child("users").child(fbuser.getUid());

                EditText productName = (EditText) findViewById(R.id.editProductName);
                EditText productDescription = (EditText) findViewById(R.id.editProductDescription);
                EditText productLocation = (EditText) findViewById(R.id.editLocation);
                EditText productPhoneNumber = (EditText) findViewById(R.id.editPhoneNumber);
                EditText priceText = (EditText) findViewById(R.id.editPrice);
                EditText distanceText = (EditText) findViewById(R.id.editDistance);

                description = String.valueOf(productDescription.getText());
                location = String.valueOf(productLocation.getText());
                name = String.valueOf(productName.getText());
                phoneNumber = String.valueOf(productPhoneNumber.getText());
                priceCategory = getPriceLevel(priceAsDouble);
                locationCategory = getLocationLevel(distance);


                price = priceText.getText().toString();
                distance = Double.parseDouble(String.valueOf(String.valueOf(distanceText.getText())));
                int decimalPoint = price.indexOf('.');

                if (checkEmpty(description, name, phoneNumber, price)) break;

                priceAsDouble = 0.0;
                if (decimalPoint == -1) {
                    priceAsDouble = Double.parseDouble(price);
                }
                else if (price.length() - decimalPoint - 1 == 2) {
                    priceAsDouble = Double.parseDouble(price);
                }
                else if (price.length() - decimalPoint - 1 == 1) {
                    priceAsDouble = Double.parseDouble(price);
                }
                else if (price.length() - decimalPoint == 1) {
                    //45.
                    priceAsDouble = Double.parseDouble(price.substring(0, price.length() - 1));
                }
                else {
                    priceAsDouble = Double.parseDouble(price.substring(0, decimalPoint + 3));
                }


                price = "$" + priceAsDouble;
                int decimalIndex = price.indexOf('.');
                if (price.length() - decimalIndex == 2) {
                    price += "0";
                }



                mUserReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String productRefKey = "";
                        for (DataSnapshot productSnapshot : dataSnapshot.child(
                                "productsIveUploaded").getChildren()) {
                            Product snapshotProduct = productSnapshot.getValue(Product.class);
                            if (snapshotProduct.getProductID().equals(product.getProductID())) {
                                productRefKey = productSnapshot.getKey();
                            }
                            System.out.println("This is the current product " + productRefKey);
                        }

                        DatabaseReference productRef = FirebaseDatabase.getInstance().getReference()
                                .child("users").child(product.getUploaderID()).child("productsIveUploaded")
                                .child(productRefKey);

                        productRef.child("category").setValue(itemCategory);
                        productRef.child("description").setValue(description);
                        productRef.child("distance").setValue(distance);
                        productRef.child("location").setValue(location);
                        productRef.child("name").setValue(name);
                        productRef.child("phoneNumber").setValue(phoneNumber);
                        productRef.child("price").setValue(price);
                        productRef.child("priceAsDouble").setValue(priceAsDouble);
                        productRef.child("priceCategory").setValue(priceCategory);
                        productRef.child("locationCategory").setValue(locationCategory);

                        setGeneralProduct(product.getProductID(), description, distance, location, name,
                                phoneNumber, price, priceAsDouble, priceCategory, locationCategory);
                        setResult(RESULT_OK, returnIntent);
                        finish();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

//                setResult(RESULT_OK, returnIntent);
//                finish();
                break;
            default :
                break;
        }
    }

    private void setGeneralProduct(String productID, String description, double distance,
                                   String location, String name, String phoneNumber, String price,
                                   double priceAsDouble, int priceCategory, int locationCategory) {
        System.out.println("Product ID is: " + productID);
        DatabaseReference productRef = FirebaseDatabase.getInstance().getReference()
                .child("products").child(productID);


        productRef.child("category").setValue(itemCategory);
        productRef.child("description").setValue(description);
        productRef.child("distance").setValue(distance);
        productRef.child("location").setValue(location);
        productRef.child("name").setValue(name);
        productRef.child("phoneNumber").setValue(phoneNumber);
        productRef.child("price").setValue(price);
        productRef.child("priceAsDouble").setValue(priceAsDouble);
        productRef.child("priceCategory").setValue(priceCategory);
        productRef.child("locationCategory").setValue(locationCategory);

    }

    private int getPriceLevel(double price) {
        if (price < 0) {
            return -1;
        }
        if (price <= 99.99) {
            return 1;
        }
        else if (price <= 199.99) {
            return 2;
        }
        else {
            return 3;
        }
    }

    private int getLocationLevel(double distance) {
        if (distance < 0) {
            return -1;
        }
        if (distance <= 9.99) {
            return 1;
        }
        else if (distance <= 19.99) {
            return 2;
        }
        else {
            return 3;
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri imageSelected = data.getData();
            imageToUpload.setImageURI(imageSelected);
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

    private boolean checkEmpty(String description, String name, String phoneNumber, String price){
        if (description.trim().isEmpty() || name.trim().isEmpty() || phoneNumber.trim().isEmpty() ||
                price.trim().isEmpty()){
            Toast.makeText(EditIndividualProductActivity.this, "Please fill in required Fields", Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }


}
