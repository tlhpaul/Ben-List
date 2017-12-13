package edu.upenn.benslist;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.ValueEventListener;
import com.sendbird.android.SendBird;

/**
 * Created by joshross
 */

public class PublicForumActivity extends AppCompatActivity
implements GoogleApiClient.OnConnectionFailedListener {

        public static class MessageViewHolder extends RecyclerView.ViewHolder {
            public TextView messageTextView;
            public ImageView messageImageView;
            public TextView messengerTextView;

            public MessageViewHolder(View v) {
                super(v);
                messageTextView = (TextView) itemView.findViewById(R.id.messageTextView);
                messageImageView = (ImageView) itemView.findViewById(R.id.messageImageView);
                messengerTextView = (TextView) itemView.findViewById(R.id.messengerTextView);
            }
        }

        private static final String TAG = "PublicForumActivity";
        public static final String MESSAGES_CHILD = "messages";
        private static final int REQUEST_INVITE = 1;
        private static final int REQUEST_IMAGE = 2;
        private static final String LOADING_IMAGE_URL = "https://www.google.com/images/spin-32.gif";
        public static final int DEFAULT_MSG_LENGTH_LIMIT = 100;
        public static final String ANONYMOUS = "anonymous";
        private static final String MESSAGE_SENT_EVENT = "message_sent";
        private String mUsername;
        private String mUserId;
        private String mPhotoUrl;
        private SharedPreferences mSharedPreferences;
        private GoogleApiClient mGoogleApiClient;
        private static final String MESSAGE_URL = "https://s17-37-benslist.firebaseio.com/message/";

        private Button mSendButton;
        private RecyclerView mMessageRecyclerView;
        private LinearLayoutManager mLinearLayoutManager;
        private EditText mMessageEditText;
        private ImageView mAddMessageImageView;


    // Firebase instance variables
        private FirebaseAuth mFirebaseAuth;
        private FirebaseUser mFirebaseUser;
        private DatabaseReference mFirebaseDatabaseReference;
        private DatabaseReference mUserReference;
        private FirebaseRecyclerAdapter<Message, MessageViewHolder> mFirebaseAdapter;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_public_forum);

            mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            // Initialize Firebase Auth
            mFirebaseAuth = FirebaseAuth.getInstance();
            mFirebaseUser = mFirebaseAuth.getCurrentUser();
            //Get the current users information
            mUserId = mFirebaseUser.getUid();
            mUserReference = FirebaseDatabase.getInstance().getReference().child("users").child(mUserId);
            ValueEventListener productListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    mUsername = dataSnapshot.child("name").getValue(String.class);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(PublicForumActivity.this, "Failed to load user's name.",
                            Toast.LENGTH_SHORT).show();
                }
            };

            mUserReference.addValueEventListener(productListener);

            //Populate the database with fake data
            mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
            mFirebaseDatabaseReference.child("messages");

            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                    .addApi(Auth.GOOGLE_SIGN_IN_API)
                    .build();

            // Initialize ProgressBar and RecyclerView.
            mMessageRecyclerView = (RecyclerView) findViewById(R.id.messageRecyclerView);
            mLinearLayoutManager = new LinearLayoutManager(this);
            mLinearLayoutManager.setStackFromEnd(true);
            mMessageRecyclerView.setLayoutManager(mLinearLayoutManager);

            // New child entries
            mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
            mFirebaseAdapter = new FirebaseRecyclerAdapter<Message,
                    MessageViewHolder>(Message.class, R.layout.item_message, MessageViewHolder.class,
                    mFirebaseDatabaseReference.child(MESSAGES_CHILD)) {

                @Override
                protected void populateViewHolder(final MessageViewHolder viewHolder,
                                                  Message friendlyMessage, int position) {
                    if (friendlyMessage.getText() != null) {
                        viewHolder.messageTextView.setText(friendlyMessage.getText());
                        viewHolder.messageTextView.setVisibility(TextView.VISIBLE);
                    } else {
                        viewHolder.messageTextView.setVisibility(TextView.GONE);
                    }

                    viewHolder.messengerTextView.setText(friendlyMessage.getName());
                }
            };

            mFirebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
                @Override
                public void onItemRangeInserted(int positionStart, int itemCount) {
                    super.onItemRangeInserted(positionStart, itemCount);
                    int friendlyMessageCount = mFirebaseAdapter.getItemCount();
                    int lastVisiblePosition =
                            mLinearLayoutManager.findLastCompletelyVisibleItemPosition();
                    // If the recycler view is initially being loaded or the
                    // user is at the bottom of the list, scroll to the bottom
                    // of the list to show the newly added message.
                    if (lastVisiblePosition == -1 ||
                            (positionStart >= (friendlyMessageCount - 1) &&
                                    lastVisiblePosition == (positionStart - 1))) {
                        mMessageRecyclerView.scrollToPosition(positionStart);
                    }
                }
            });

            mMessageRecyclerView.setLayoutManager(mLinearLayoutManager);
            mMessageRecyclerView.setAdapter(mFirebaseAdapter);

            mMessageEditText = (EditText) findViewById(R.id.messageEditText);
            mMessageEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(mSharedPreferences
                    .getInt("friendly_msg_length", DEFAULT_MSG_LENGTH_LIMIT))});
            mMessageEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    if (charSequence.toString().trim().length() > 0) {
                        mSendButton.setEnabled(true);
                    } else {
                        mSendButton.setEnabled(false);
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {
                }
            });

            mSendButton = (Button) findViewById(R.id.sendButton);
            mSendButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Won't Display the username because there is an issue with mUsername
                    Message message = new Message(mMessageEditText.getText().toString(), mUsername);
                    mFirebaseDatabaseReference.child(MESSAGES_CHILD).push().setValue(message);
                    mMessageEditText.setText("");
                }
            });

//            mAddMessageImageView = (ImageView) findViewById(R.id.addMessageImageView);
//            mAddMessageImageView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    //Todo include capability to send images if we want to
//                }
//            });

        }

        @Override
        public void onStart() {
            super.onStart();
        }

        @Override
        public void onPause() {
            super.onPause();
        }

        @Override
        public void onResume() {
            super.onResume();
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
        }

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

        @Override
        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
            // An unresolvable error has occurred and Google APIs (including Sign-In) will not
            // be available.
            Log.d(TAG, "onConnectionFailed:" + connectionResult);
            Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
        }
}
