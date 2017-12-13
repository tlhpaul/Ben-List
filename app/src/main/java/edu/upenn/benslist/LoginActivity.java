package edu.upenn.benslist;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private String currentUserID;

    private GoogleApiClient mGoogleApiClient;
    private static final int RC_SIGN_IN = 9001;
    private static final String TAG = "Login";

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;


    private static CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptSignin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptSignin();
            }
        });

        Button mRegisterButton = (Button) findViewById(R.id.register_button);
        mRegisterButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                createAccount();
            }
        });

        Button mResetPasswordButton = (Button) findViewById(R.id.resetPassword_button);
        mResetPasswordButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                resetPassword();
            }
        });


        mRegisterButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                createAccount();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        //Initiate Firebase
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };



    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptSignin() {

        // Store values at the time of the login attempt.
        final String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        if (email.equals("") || password.equals("")) {
            Toast.makeText(LoginActivity.this, "Please fill in both email and password before signing in",
                    Toast.LENGTH_SHORT).show();
        } else {

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());

                            // If sign in fails, display a message to the user. If sign in succeeds
                            // the auth state listener will be notified and logic to handle the
                            // signed in user can be handled in the listener.
                            if (!task.isSuccessful()) {
                                Log.w(TAG, "signInWithEmail", task.getException());
                                Toast.makeText(LoginActivity.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                FirebaseUser fbuser = FirebaseAuth.getInstance().getCurrentUser();
                                currentUserID = fbuser.getUid();
                                FirebaseDatabase.getInstance().getReference().child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        String name = dataSnapshot.child(currentUserID).child("name").getValue(String.class);
                                        if (name == null) {
                                            Intent intent = new Intent(LoginActivity.this, UserProfileActivity.class);
                                            intent.putExtra("SignUp", true);
                                            startActivity(intent);
                                        }
//                                        String userAddress = dataSnapshot.child(currentUserID).child("address").getValue(String.class);
//                                        String interests = dataSnapshot.child(currentUserID).child("interests").getValue(String.class);
                                        else {
                                            //Intent intent = new Intent(LoginActivity.this, HomePageActivity.class);
                                            Intent intent = new Intent(LoginActivity.this, MainActivityNewsFeed.class);
                                            startActivity(intent);
                                        }
                                    }


                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });

                            }

                        }
                    });
        }
    }

    private void createAccount() {

        // Store values at the time of the login attempt.
        final String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        if (email.equals("") || password.equals("")) {
            Toast.makeText(LoginActivity.this, "Please fill in both email and password before registering",
                    Toast.LENGTH_SHORT).show();
        } else {
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());

                                Intent intent = new Intent(LoginActivity.this, UserProfileActivity.class);
                                intent.putExtra("SignUp", true);
                                startActivity(intent);
                            } else {
                                StringBuilder error = new StringBuilder("Authentication Failed: ");
                                try {
                                    throw task.getException();
                                } catch(FirebaseAuthWeakPasswordException e) {
                                    error.append("Weak Password");
                                } catch(FirebaseAuthInvalidCredentialsException e) {
                                    error.append("Invalid Credentials");
                                } catch(FirebaseAuthUserCollisionException e) {
                                    error.append("User already exists");
                                } catch(FirebaseException e) {
                                    error.append("Weak Password");
                                }catch(Exception e) {
                                    error.append("Failed");
                                }
                                Toast.makeText(LoginActivity.this, error.toString(),
                                        Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
        }
    }

    private void resetPassword(){
        FirebaseAuth auth = FirebaseAuth.getInstance();
        final String email = mEmailView.getText().toString();
        String emailAddress = email;

        auth.sendPasswordResetEmail(emailAddress)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Email sent.");
                            Toast.makeText(LoginActivity.this, "Email sent",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}

