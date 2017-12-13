package edu.upenn.benslist;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

/**
 * Created by johnquinn on 2/15/17.
 */

public class FilterSearchResultsActivity extends AppCompatActivity implements View.OnClickListener {

    /*
    TODO - completely changed this April 13th (JP)
     */

    private String searchQuery;
    private String searchCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter_results);

        this.searchCategory = getIntent().getStringExtra("Search Category");
        this.searchQuery = getIntent().getStringExtra("Search Query");

        Button button = (Button)  findViewById(R.id.doneFilteringResultsButton);
        button.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case (R.id.doneFilteringResultsButton) :
                Intent intent = new Intent(this, SearchResultsActivity.class);

                CheckBox lowPrice = (CheckBox) findViewById(R.id.lowPriceCheckBox);
                CheckBox medPrice = (CheckBox) findViewById(R.id.mediumPriceCheckBox);
                CheckBox highPrice = (CheckBox) findViewById(R.id.highPriceCheckBox);
                CheckBox closeLocation = (CheckBox) findViewById(R.id.closeLocationCheckBox);
                CheckBox mediumLocation = (CheckBox) findViewById(R.id.mediumLocationCheckBox);
                CheckBox farLocation = (CheckBox) findViewById(R.id.farLocationCheckBox);

                intent.putExtra("Low Price", lowPrice.isChecked());
                intent.putExtra("Medium Price", medPrice.isChecked());
                intent.putExtra("High Price", highPrice.isChecked());
                intent.putExtra("Close Location", closeLocation.isChecked());
                intent.putExtra("Medium Location", mediumLocation.isChecked());
                intent.putExtra("Far Location", farLocation.isChecked());
                intent.putExtra("Search Category", searchCategory);
                intent.putExtra("Search Query", searchQuery);

                startActivity(intent);
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
