package ph.com.team.gobiker;

import android.Manifest;

import androidx.annotation.NonNull;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import ph.com.team.gobiker.ui.login.LoginViewModel;
import ph.com.team.gobiker.ui.map.PermissionUtils;

public class MainLoginActivity extends AppCompatActivity {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private LoginViewModel loginViewModel;
    private Button noAccBtn, forgotPassBtn, loginButton;
    private SignInButton googleButton;
    private LoginButton FBButton;
    private EditText UserEmail, UserPassword;
    private ProgressDialog loadingBar, confirmationLoading;
    private FirebaseAuth mAuth;
    private Boolean emailAddressChecker;
    private DatabaseReference UsersRef;
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_login);
        /*loginViewModel = ViewModelProviders.of(this, new LoginViewModelFactory())
                .get(LoginViewModel.class);*/

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.server_client_id))
                .requestEmail()
                .build();
        // [END config_signin]

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        UserEmail = findViewById(R.id.username);
        UserPassword = findViewById(R.id.login_password);
        loginButton = findViewById(R.id.continueLogin);
        googleButton = findViewById(R.id.g_login);
        FBButton = findViewById(R.id.fb_login);
        noAccBtn = findViewById(R.id.login_no_account);
        forgotPassBtn = findViewById(R.id.login_forgot_pass);
        loadingBar = new ProgressDialog(this);
        confirmationLoading = new ProgressDialog(this);
        mAuth = FirebaseAuth.getInstance();

        noAccBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainLoginActivity.this, CreateAccountActivity.class));
                finish();
            }
        });

        forgotPassBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainLoginActivity.this, ForgotPasswordActivity.class));
                finish();
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AllowingUserToLogin();
            }
        });

        googleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gLogin();
            }
        });

        FBButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initiateFBLogin();
            }
        });

        askPermission();
    }

    private void gLogin() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void askPermission() {
        PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                Manifest.permission.ACCESS_FINE_LOCATION, true);
    }

    private void initiateFBLogin(){

    }


    private void AllowingUserToLogin() {
        String email = UserEmail.getText().toString();
        String password = UserPassword.getText().toString();

        if (TextUtils.isEmpty(email)){
            Toast.makeText(this, "Please write your email...", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(password)){
            Toast.makeText(this, "Please write your password...", Toast.LENGTH_SHORT).show();
        }
        else{
            loadingBar.setTitle("Login");
            loadingBar.setMessage("Please wait, while we are allowing you to MainScreenActivity into your Account...");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();

            mAuth.signInWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){
                                //SendUserToMainActivity();
                                //Toast.makeText(MainLoginActivity.this,"You are Logged In successfully.",Toast.LENGTH_SHORT).show();
                                VerifyEmailAddress();
                            }
                            else{
                                String message = task.getException().getMessage();
                                Toast.makeText(MainLoginActivity.this, "Error Occured: " + message, Toast.LENGTH_SHORT).show();
                            }
                            loadingBar.dismiss();
                        }
                    });
        }
    }

    private void SendUserToMainActivity() {
        Intent setupIntent = new Intent(MainLoginActivity.this, NavActivity.class);
        startActivity(setupIntent);
        finish();
    }

    private void VerifyEmailAddress(){
        FirebaseUser user = mAuth.getCurrentUser();
        emailAddressChecker = user.isEmailVerified();

        if (emailAddressChecker){
            if(user != null){
                CheckUserExistence();
            }
        }
        else{
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    confirmationLoading.setTitle("Loading");
                    confirmationLoading.setMessage("Please wait, while we are sending you a new VerificationActivity email...");
                    confirmationLoading.setCanceledOnTouchOutside(true);
                    confirmationLoading.show();
                    switch (which){
                        case DialogInterface.BUTTON_NEGATIVE:
                            user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Toast.makeText(MainLoginActivity.this, "We have sent you a new VerificationActivity email. If you have not received a new email in a few minutes please try again.", Toast.LENGTH_SHORT).show();
                                    confirmationLoading.dismiss();
                                    mAuth.signOut();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(MainLoginActivity.this, "Something went wrong. Please please try again.", Toast.LENGTH_SHORT).show();
                                    confirmationLoading.dismiss();
                                    mAuth.signOut();
                                }
                            });
                            break;
                    }
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(MainLoginActivity.this, R.style.AlertDialogTheme);
            builder.setMessage("Please verify your account first")
                    .setPositiveButton("Ok", dialogClickListener)
                    .setNegativeButton("Re-send Email Verification", dialogClickListener);

            AlertDialog alert = builder.create();
            alert.setOnShowListener(arg0 -> {
                alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colorPrimary));
                alert.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorPrimaryDark));
            });
            alert.show();
        }
    }



    private void CheckUserExistence() {
        final String current_user_id = mAuth.getCurrentUser().getUid();
        UsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(current_user_id)){
                    if (dataSnapshot.child(current_user_id).hasChild("fullname")) {
                        updateUserStatus("online");
                        SendUserToMainActivity();
                    }
                    else {
                        SendUserToSetupActivity();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void SendUserToSetupActivity() {
        Intent setupIntent = new Intent(MainLoginActivity.this, SetupActivity.class);
        startActivity(setupIntent);
        finish();
    }

    private void updateUserStatus(String state){
        String saveCurrentDate, saveCurrentTime;
        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(calForDate.getTime());

        Calendar calForTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
        saveCurrentTime = currentTime.format(calForTime.getTime());

        Map currentStateMap = new HashMap<>();
        currentStateMap.put("time",saveCurrentTime);
        currentStateMap.put("date",saveCurrentDate);
        currentStateMap.put("type",state);

        if(mAuth.getCurrentUser() != null){
            UsersRef.child(mAuth.getCurrentUser().getUid()).child("userState")
                    .updateChildren(currentStateMap);
        }

    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("gsignin", "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            startActivity(new Intent(MainLoginActivity.this, SetupWithExistingActivity.class));
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("gsignin", "signInWithCredential:failure", task.getException());
                        }
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d("gsignin", "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w("gsignin", "Google sign in failed", e);
            }
        }
    }
}
