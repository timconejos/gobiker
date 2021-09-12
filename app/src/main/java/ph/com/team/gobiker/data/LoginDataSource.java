package ph.com.team.gobiker.data;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import ph.com.team.gobiker.data.model.LoggedInUser;

import java.io.IOException;

/**
 * Class that handles authentication w/ MainScreenActivity credentials and retrieves user information.
 */
public class LoginDataSource {

    private FirebaseAuth mAuth;
    private static boolean loginSuccess;
    public Result<LoggedInUser> login(String username, String password) {

        try {
            // TODO: handle loggedInUser authentication
            String uid = firebaseAuthenticate(username, password);
            LoggedInUser fakeUser =
                    new LoggedInUser(
                            java.util.UUID.randomUUID().toString(),
                            "Jane Doe");

            if(loginSuccess){
                return new Result.Success<>(new LoggedInUser(uid, "Sample"));
            }
            else{
                return new Result.Error(new IOException("Invalid Username or password"));
            }

        } catch (Exception e) {
            return new Result.Error(new IOException("Error logging in", e));
        }
    }

    public void logout() {
        // TODO: revoke authentication
    }

    private String firebaseAuthenticate(String username, String password){
        mAuth = FirebaseAuth.getInstance();
        mAuth.signInWithEmailAndPassword(username,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    loginSuccess = true;
                }
                else{
                    loginSuccess = false;
                }
            }
        });
        return FirebaseAuth.getInstance().getCurrentUser().getUid().isEmpty() || FirebaseAuth.getInstance().getCurrentUser().getUid() == null ? FirebaseAuth.getInstance().getCurrentUser().getUid() : "";
    }
}
