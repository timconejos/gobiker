package ph.com.team.gobiker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPassword extends AppCompatActivity {

    private Toolbar mToolbar;
    private Button ResetPasswordSendEmailButton,ResetPasswordRememberButton;
    private EditText ResetEmailInput;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        mAuth = FirebaseAuth.getInstance();

        ResetPasswordSendEmailButton = findViewById(R.id.reset_password_email_button);
        ResetEmailInput = findViewById(R.id.reset_password_EMAIL);
        ResetPasswordRememberButton = findViewById(R.id.reset_password_remember_button);

        ResetPasswordSendEmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userEmail = ResetEmailInput.getText().toString();
                if (TextUtils.isEmpty(userEmail)){
                    Toast.makeText(ForgotPassword.this,"Please write your valid email address first.",Toast.LENGTH_SHORT).show();
                }
                else{
                    mAuth.sendPasswordResetEmail(userEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                Toast.makeText(ForgotPassword.this,"Please check your Email Account, If you want to reset your password.",Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(ForgotPassword.this, login.class));
                            }
                            else{
                                String message  = task.getException().getMessage();
                                Toast.makeText(ForgotPassword.this,"Error occurred: "+message,Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });

        ResetPasswordRememberButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ForgotPassword.this, login.class));
            }
        });
    }
}
