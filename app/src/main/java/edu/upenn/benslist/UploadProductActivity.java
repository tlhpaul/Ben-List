package edu.upenn.benslist;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;

public class UploadProductActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener,
View.OnClickListener {

    private Uri mSaveUri;
    private String picUrl;
    private static final int RESULT_LOAD_IMAGE = 1;
    private ImageView imageToUpload;
    private String itemCategory;
    private String currentUserName;
    StorageReference storageRef = FirebaseStorage.getInstance().getReference();
    DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("products");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_product);
        this.currentUserName = getIntent().getStringExtra("Logged In User Name");

        imageToUpload = (ImageView) findViewById(R.id.imageToUpload);
        Button uploadImageButton = (Button) findViewById(R.id.uploadPictureButton);
        Button doneButton = (Button) findViewById(R.id.doneButton);

        uploadImageButton.setOnClickListener(this);
        doneButton.setOnClickListener(this);


        Spinner spinner = (Spinner) findViewById(R.id.productCategorySpinner);
        spinner.setOnItemSelectedListener(this);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.product_categories_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        itemCategory = "Furniture";
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        itemCategory = parent.getItemAtPosition(position).toString();
    }

    public void onNothingSelected(AdapterView<?> arg0) {

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
                /*
                TODO - changes made april 13th by JP here
                 */
                if (checkEmpty()) break;

                Intent returnIntent = new Intent(this, HomePageActivity.class);

                EditText productName = (EditText) findViewById(R.id.editProductName);
                EditText productDescription = (EditText) findViewById(R.id.editProductDescription);
                EditText productLocation = (EditText) findViewById(R.id.editLocation);
                EditText productPhoneNumber = (EditText) findViewById(R.id.editPhoneNumber);

                EditText priceText = (EditText) findViewById(R.id.editPrice);
                EditText distanceText = (EditText) findViewById(R.id.editDistance);

                ImageView productPicture = (ImageView) findViewById(R.id.imageToUpload);


                String price = priceText.getText().toString();
                double distance = 0.0;
                if (!distanceText.getText().toString().trim().equals(""))
                    distance = Double.parseDouble(distanceText.getText().toString());
                try {
                    int decimalPoint = price.indexOf('.');
                    double priceAsDouble = 0.0;
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

                    final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
                    FirebaseUser fbUser = FirebaseAuth.getInstance().getCurrentUser();
                    final String currentUserID = fbUser.getUid();


                    DatabaseReference ref = mDatabase.child("products").push();
                    Product product = Product.writeNewProductToDatabase(productName.getText().toString(),
                            productDescription.getText().toString(), priceAsDouble,
                            productLocation.getText().toString(), productPhoneNumber.getText().toString(),
                            itemCategory, currentUserName, ref.getKey(), distance, picUrl);

                    ref.setValue(product);

                    ref = mDatabase.child("users").child(currentUserID).child("productsIveUploaded").push();
                    ref.setValue(product);

                    setResult(RESULT_OK, returnIntent);
                    finish();
                }
                catch (NumberFormatException e) {
                    Toast.makeText(this, "Input valid price", Toast.LENGTH_LONG);
                }
                break;

            default :
                break;
        }
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
            Bitmap bitmap;
            try{
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageSelected);
                picUrl = encodingBitMapAndReturnURL(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String encodingBitMapAndReturnURL(Bitmap imageBitmap){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        return Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP);
    }


    private static Bitmap decodeFromFirebaseBase64(String image) throws IOException {
        byte[] decodedString = Base64.decode(image, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedString , 0, decodedString .length);
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

    private boolean checkEmpty() {
        EditText productName = (EditText) findViewById(R.id.editProductName);
        EditText productDescription = (EditText) findViewById(R.id.editProductDescription);
        EditText productLocation = (EditText) findViewById(R.id.editLocation);
        EditText productPhoneNumber = (EditText) findViewById(R.id.editPhoneNumber);
        EditText priceText = (EditText) findViewById(R.id.editPrice);
        EditText distanceText = (EditText) findViewById(R.id.editDistance);
        if (productName.getText().toString().trim().equals("") || productDescription.getText().toString().trim().equals("")
                || productPhoneNumber.getText().toString().trim().equals("") || priceText.getText().toString().trim().equals("")) {
            Toast.makeText(UploadProductActivity.this, "Please fill in required Fields", Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }

}
