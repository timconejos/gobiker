package ph.com.team.gobiker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

import ph.com.team.gobiker.ui.login.MainLoginActivity;

public class CreateAccount extends AppCompatActivity {

    private EditText UserEmail, UserPhone, UserPassword, UserConfirmPassword;
    private Button CreateAccountButton, mLogin;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;
    private DatabaseReference UsersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        mAuth = FirebaseAuth.getInstance();
        UserEmail = findViewById(R.id.register_email);
        UserPhone = findViewById(R.id.register_phone);
        UserPassword = findViewById(R.id.register_password);
        UserConfirmPassword = findViewById(R.id.register_confirmpassword);
        CreateAccountButton = findViewById(R.id.register_next_button);
        mLogin = findViewById(R.id.register_login_button);
        loadingBar = new ProgressDialog(this);

        CreateAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CreateNewAccount();
            }
        });

        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(CreateAccount.this, MainLoginActivity.class));
                finish();
            }
        });
    }

    private void CreateNewAccount() {
        final String email = UserEmail.getText().toString();
        final String phone = UserPhone.getText().toString();
        final String password = UserPassword.getText().toString();
        final String confirmPassword = UserConfirmPassword.getText().toString();

        if (TextUtils.isEmpty(email)){
            Toast.makeText(this,"Please write your email...",Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(phone)){
            Toast.makeText(this,"Please write your phone number...",Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(password)){
            Toast.makeText(this,"Please write your password...",Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(confirmPassword)){
            Toast.makeText(this,"Please confirm your password...",Toast.LENGTH_SHORT).show();
        }
        else if (!password.equals(confirmPassword)){
            Toast.makeText(this,"Your password do not match with your confirm password...",Toast.LENGTH_SHORT).show();
        }
        else{
            loadingBar.setTitle("Creating New Account");
            loadingBar.setMessage("Please wait, while we are creating your new account...");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);
            mAuth.createUserWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                String currentUserID = mAuth.getCurrentUser().getUid();
                                UsersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);
                                HashMap userMap = new HashMap();
                                userMap.put("email", email);
                                userMap.put("phone", phone);
                                UsersRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
                                    @Override
                                    public void onComplete(@NonNull Task task) {
                                        if (task.isSuccessful()){
                                            SendEmailVerificationMessage();
                                            //SendUserToLoginActivity();
                                            //Toast.makeText(CreateAccount.this,"Your Account is created successfully",Toast.LENGTH_LONG).show();
                                        }
                                        else{
                                            String message = task.getException().getMessage();
                                            Toast.makeText(CreateAccount.this,"Error Occurred:"+message,Toast.LENGTH_SHORT).show();
                                        }

                                    }
                                });


                            }
                            else{
                                String message = task.getException().getMessage();
                                Toast.makeText(CreateAccount.this, "Error Occured: " + message, Toast.LENGTH_SHORT).show();
                            }
                            loadingBar.dismiss();
                        }
                    });
        }
    }

    private void SendUserToLoginActivity() {
        Intent setupIntent = new Intent(CreateAccount.this, MainLoginActivity.class);
        startActivity(setupIntent);
        finish();
    }

    private void SendEmailVerificationMessage(){
        FirebaseUser user = mAuth.getCurrentUser();

        if (user!=null){
            user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        Toast.makeText(CreateAccount.this,"Registration Successful. Please verify your account.",Toast.LENGTH_SHORT).show();
                        SendUserToLoginActivity();
                        mAuth.signOut();
                    }
                    else{
                        Toast.makeText(CreateAccount.this,"Error: "+task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                        mAuth.signOut();
                    }
                }
            });
        }
    }
}
