package edu.upenn.benslist;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.Serializable;

/**
 * Created by johnquinn on 3/14/17.
 */

public class CheckoutProductActivity extends AppCompatActivity implements View.OnClickListener {

    private Product product;

    private static final int PERMISSION_REQUEST_CODE = 1;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.purchase_product);
        this.product = (Product) getIntent().getExtras().getSerializable("Product");

        Button purchaseProductButton = (Button) findViewById(R.id.detailedListingConfirmPurchase);
        purchaseProductButton.setOnClickListener(this);

        Button leaveReviewButton = (Button) findViewById(R.id.submitReviewButton);
        leaveReviewButton.setOnClickListener(this);

        Button callUserButton = (Button) findViewById(R.id.detailedListingCall);
        callUserButton.setOnClickListener(this);

        Button checkOutUploadersProfileButton =
                (Button) findViewById(R.id.detailedListingCheckUploadersPage);
        checkOutUploadersProfileButton.setOnClickListener(this);
        checkOutUploadersProfileButton.setText("Check Out " + product.getUploaderName() + "'s Profile");

        setProductTextValues();

        addCommentsSection();
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

    protected void addCommentsSection() {
        int i = 1;
        LinearLayout rl = (LinearLayout) findViewById(R.id.purchaseProductLL);
        for (String comment : product.getReviews()) {
            TextView textView = new TextView(this);
            textView.setText("Comment " + i + ": " + comment + "\n\n");
            rl.addView(textView, i + 8);
            i++;
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case (R.id.detailedListingConfirmPurchase):
                String currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                ;
                //TODO prevent users from buying products uploaded by themselves

                if (product.getUploaderID().equals(currentUserID)) {
                    Toast.makeText(CheckoutProductActivity.this,
                            "This product is uploaded by yourself",
                            Toast.LENGTH_SHORT).show();
                    break;
                }
                Intent i = new Intent(this, ProductPurchaseConfirmationActivity.class);
                i.putExtra("Product", (Serializable) product);
                i.putExtra("UploaderID", product.getUploaderID());
                i.putExtra("ProductID", product.getProductID());
                startActivity(i);
                break;

            case (R.id.submitReviewButton):
                EditText editText = (EditText) findViewById(R.id.detailedListingEditReviewText);
                String review = editText.getText().toString();
                product.addReview(review);
                Intent intent = getIntent();
                intent.putExtra("ProductID", product.getProductID());
                finish();
                startActivity(intent);
                break;

            case (R.id.detailedListingCheckUploadersPage):
                Intent newIntent = new Intent(this, ViewUsersProfileActivity.class);
                newIntent.putExtra("UserId", product.getUploaderID());
                startActivity(newIntent);
                break;

            case (R.id.detailedListingCall):
                makeCall(product.getUploaderName());
                break;

            default :
                break;
        }
    }

    public void makeCall(String s)
    {
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + s));
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED){

            requestForCallPermission();

        } else {
            startActivity(intent);

        }
    }
    public void requestForCallPermission()
    {

        if (ActivityCompat.shouldShowRequestPermissionRationale(this,android.Manifest.permission.CALL_PHONE))
        {
        }
        else {

            ActivityCompat.requestPermissions(this,new String[]{android.Manifest.permission.CALL_PHONE},PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    makeCall(product.getUploaderName());
                }
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
