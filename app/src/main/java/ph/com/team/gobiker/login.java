package ph.com.team.gobiker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import ph.com.team.gobiker.ui.login.MainLoginActivity;

public class login extends AppCompatActivity {

    private Button mLogin,signUp;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();
        mLogin = findViewById(R.id.login);
        signUp = findViewById(R.id.signup);
        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(login.this, MainLoginActivity.class));
                finish();
            }
        });
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(login.this, CreateAccount.class));
                finish();
            }
        });

    }

    @Override
    protected void onStart(){
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null){
            SendUserToMainActivity();
        }
    }

    private void SendUserToMainActivity() {
        Intent setupIntent = new Intent(login.this, NavActivity.class);
        startActivity(setupIntent);
        finish();
    }
}
