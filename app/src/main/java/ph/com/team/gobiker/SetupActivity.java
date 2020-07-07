package ph.com.team.gobiker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {
    private EditText FullName, weight, height, age, province_city;
    private Button SaveInformationbutton;
    private CircleImageView ProfileImage;
    private CheckBox checkBike, checkMotor;

    private FirebaseAuth mAuth;
    private Uri ImageUri;
    private DatabaseReference UsersRef;
    private ProgressDialog loadingBar;
    private StorageReference UserProfileImageRef;
    private Spinner Gender, WUnit, HUnit;

    private TextView wt, ht, at, bn;

    String currentUserID;
    final static int Gallery_Pick = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        checkBike = findViewById(R.id.setup_checkBoxBike);
        checkMotor = findViewById(R.id.setup_checkBoxMotor);
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);
        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("profileimage");

        wt = findViewById(R.id.setup_weight_text);
        ht = findViewById(R.id.setup_height_text);
        at = findViewById(R.id.setup_age_text);
        bn = findViewById(R.id.setup_bike_note);
        weight = findViewById(R.id.setup_weight);
        height = findViewById(R.id.setup_height);
        age = findViewById(R.id.setup_age);
        province_city = findViewById(R.id.setup_address);

        WUnit = findViewById(R.id.setup_weight_unit);
        String[] itemsW = new String[]{"kgs", "lbs"};
        ArrayAdapter<String> adapterW = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, itemsW);
        WUnit.setAdapter(adapterW);

        HUnit = findViewById(R.id.setup_height_unit);
        String[] itemsH = new String[]{"cm", "in"};
        ArrayAdapter<String> adapterH = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, itemsH);
        HUnit.setAdapter(adapterH);

        wt.setVisibility(View.GONE);
        ht.setVisibility(View.GONE);
        at.setVisibility(View.GONE);
        bn.setVisibility(View.GONE);
        weight.setVisibility(View.GONE);
        height.setVisibility(View.GONE);
        age.setVisibility(View.GONE);
        WUnit.setVisibility(View.GONE);
        HUnit.setVisibility(View.GONE);

        checkBike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkBike.isChecked()){
                    wt.setVisibility(View.VISIBLE);
                    ht.setVisibility(View.VISIBLE);
                    at.setVisibility(View.VISIBLE);
                    bn.setVisibility(View.VISIBLE);
                    weight.setVisibility(View.VISIBLE);
                    height.setVisibility(View.VISIBLE);
                    age.setVisibility(View.VISIBLE);
                    WUnit.setVisibility(View.VISIBLE);
                    HUnit.setVisibility(View.VISIBLE);
                }
                else{
                    wt.setVisibility(View.GONE);
                    ht.setVisibility(View.GONE);
                    at.setVisibility(View.GONE);
                    bn.setVisibility(View.GONE);
                    weight.setVisibility(View.GONE);
                    height.setVisibility(View.GONE);
                    age.setVisibility(View.GONE);
                    WUnit.setVisibility(View.GONE);
                    HUnit.setVisibility(View.GONE);
                }
            }
        });

        Gender = findViewById(R.id.setup_gender);
        String[] items = new String[]{"Male", "Female"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        Gender.setAdapter(adapter);

        FullName = findViewById(R.id.setup_full_name);
        SaveInformationbutton = findViewById(R.id.setup_information_button);
        ProfileImage = findViewById(R.id.setup_profile_image);
        loadingBar = new ProgressDialog(this);

        SaveInformationbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SaveAccountSetupInformation();
            }
        });

        ProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,Gallery_Pick);
            }
        });

        UsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    if(dataSnapshot.hasChild("profileimage")) {
                        String image = dataSnapshot.child("profileimage").getValue().toString();
                        Picasso.with(SetupActivity.this).load(image).placeholder(R.drawable.profile).into(ProfileImage);
                    }
                    else{
                        Toast.makeText(SetupActivity.this,"Please select profile image first...",Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==Gallery_Pick && resultCode==RESULT_OK && data!=null){
            ImageUri = data.getData();
            loadingBar.setTitle("Profile Image");
            loadingBar.setMessage("Please wait, while we are updating your Profile Image...");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();

            ProfileImage.setImageURI(ImageUri);

            final StorageReference filePath = UserProfileImageRef.child(currentUserID+".jpg");
            filePath.putFile(ImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String downloadUrl = uri.toString();
                            Toast.makeText(SetupActivity.this, "Image uploaded successfully to storage", Toast.LENGTH_SHORT).show();
                            UsersRef.child("profileimage").setValue(downloadUrl)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                Intent selfIntent = new Intent(SetupActivity.this, SetupActivity.class);
                                                startActivity(selfIntent);
                                                Toast.makeText(SetupActivity.this,"Profile Image stored to firebase storage successfully .",Toast.LENGTH_SHORT).show();
                                            }
                                            else{
                                                Toast.makeText(SetupActivity.this,"Error occurred:"+task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                                            }
                                            loadingBar.dismiss();
                                        }
                                    });
                        }
                    });
                }
            });
        }

        /*if (requestCode==Gallery_Pick && resultCode==RESULT_OK && data!=null){
            Uri ImageUri = data.getData();
            CropImage.activity().setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }

        if (requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK){
                loadingBar.setTitle("Profile Image");
                loadingBar.setMessage("Please wait, while we are updating your Profile Image...");
                loadingBar.setCanceledOnTouchOutside(true);
                loadingBar.show();

                Uri resultUri = result.getUri();
                final StorageReference filePath = UserProfileImageRef.child(currentUserID+".jpg");
                filePath.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String downloadUrl = uri.toString();
                                Toast.makeText(SetupActivity.this, "Image uploaded successfully to storage", Toast.LENGTH_SHORT).show();
                                UsersRef.child("profileimage").setValue(downloadUrl)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    Intent selfIntent = new Intent(SetupActivity.this, SetupActivity.class);
                                                    startActivity(selfIntent);
                                                    Toast.makeText(SetupActivity.this,"Profile Image stored to firebase storage successfully .",Toast.LENGTH_SHORT).show();
                                                }
                                                else{
                                                    Toast.makeText(SetupActivity.this,"Error occurred:"+task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                                                }
                                                loadingBar.dismiss();
                                            }
                                        });
                            }
                        });
                    }
                });
            }
            else{
                Toast.makeText(SetupActivity.this,"Error occurred: Image cannot be cropped. Try again.",Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();
            }
        }*/
    }

    private void SaveAccountSetupInformation() {
        String gender = Gender.getSelectedItem().toString();
        String fullname = FullName.getText().toString();
        Boolean checkm = checkMotor.isChecked();
        Boolean checkb = checkBike.isChecked();
        String prov_city = province_city.getText().toString();

        if (TextUtils.isEmpty(fullname)) {
            Toast.makeText(this, "Please write your fullname...", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(prov_city)) {
            Toast.makeText(this, "Please write your province or city...", Toast.LENGTH_SHORT).show();
        }
        else if (!checkm && !checkb){
            Toast.makeText(this, "Please select bicycle or motorcycle.",Toast.LENGTH_SHORT).show();
        }
        else{
            String hei="", wei="", yo="", hUnit="", wUnit="", sh="", sw="";
            if (checkb){
                sh = height.getText().toString();
                sw = weight.getText().toString();
                yo = age.getText().toString();

                if (yo.equals(""))
                    yo = "0";

                if (!sh.equals("")){
                    hUnit = HUnit.getSelectedItem().toString();
                    if (hUnit.equals("cm"))
                        hei = sh;
                    else
                        hei = Double.toString((Double.parseDouble(sh)*2.54));
                }

                if (!sw.equals("")){
                    wUnit = WUnit.getSelectedItem().toString();

                    if (wUnit.equals("kgs"))
                        wei = sw;
                    else
                        wei = Double.toString((Double.parseDouble(sw)/2.205));
                }
            }
            loadingBar.setTitle("Saving Information");
            loadingBar.setMessage("Please wait, while we are creating your new account...");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);
            HashMap userMap = new HashMap();
            userMap.put("fullname", fullname);
            userMap.put("gender", gender);
            userMap.put("bike",checkb);
            userMap.put("motor",checkm);
            userMap.put("height",hei);
            userMap.put("savedheight",sh);
            userMap.put("savedhunit",hUnit);
            userMap.put("weight",wei);
            userMap.put("savedweight",sw);
            userMap.put("savedwunit",wUnit);
            userMap.put("age",yo);
            userMap.put("level","1");
            userMap.put("overall_distance","0");
            userMap.put("address",prov_city);

            UsersRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()){
                        SendUserToMainActivity();
                        Toast.makeText(SetupActivity.this,"Your Account is created successfully",Toast.LENGTH_LONG).show();
                    }
                    else{
                        String message = task.getException().getMessage();
                        Toast.makeText(SetupActivity.this,"Error Occurred:"+message,Toast.LENGTH_SHORT).show();
                    }
                    loadingBar.dismiss();
                }
            });
        }
    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(SetupActivity.this,NavActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}
