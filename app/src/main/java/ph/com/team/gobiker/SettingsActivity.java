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
import ph.com.team.gobiker.ui.dashboard.DashboardFragment;

public class SettingsActivity extends AppCompatActivity {
    private EditText userProfName, userPhone, weight, height, age, province_city;
    private CheckBox checkBike, checkMotor;
    private Button UpdateAccountSettingsButton, CancelUpdateButton;
    private CircleImageView userProfImage;
    private DatabaseReference SettingsUserRef;
    private FirebaseAuth mAuth;
    private String currentUserId;
    private Uri ImageUri;
    private ProgressDialog loadingBar;
    private StorageReference UserProfileImageRef;
    private Spinner Gender, WUnit, HUnit;
    final static int Gallery_Pick = 1;
    private TextView wt, ht, at, bn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        SettingsUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("profileimage");

        userProfName = findViewById(R.id.settings_profile_full_name);
        userPhone = findViewById(R.id.settings_phone);
        Gender = findViewById(R.id.settings_gender);
        String[] items = new String[]{"Male", "Female"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        Gender.setAdapter(adapter);

        checkBike = findViewById(R.id.settings_checkBoxBike);
        checkMotor = findViewById(R.id.settings_checkBoxMotor);

        UpdateAccountSettingsButton = findViewById(R.id.update_account_settings_button);
        CancelUpdateButton = findViewById(R.id.cancel_action);
        userProfImage = findViewById(R.id.settings_profile_image);
        loadingBar = new ProgressDialog(this);

        wt = findViewById(R.id.settings_weight_text);
        ht = findViewById(R.id.settings_height_text);
        at = findViewById(R.id.settings_age_text);
        bn = findViewById(R.id.settings_bike_note);
        weight = findViewById(R.id.settings_weight);
        height = findViewById(R.id.settings_height);
        age = findViewById(R.id.settings_age);
        province_city = findViewById(R.id.settings_profile_address);

        WUnit = findViewById(R.id.settings_weight_unit);
        String[] itemsW = new String[]{"kgs", "lbs"};
        ArrayAdapter<String> adapterW = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, itemsW);
        WUnit.setAdapter(adapterW);

        HUnit = findViewById(R.id.settings_height_unit);
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

        SettingsUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    String myProfileName = dataSnapshot.child("fullname").getValue().toString();
                    String myGender = dataSnapshot.child("gender").getValue().toString();
                    String myPhone = dataSnapshot.child("phone").getValue().toString();
                    String myBike = dataSnapshot.child("bike").getValue().toString();
                    String myMotor = dataSnapshot.child("motor").getValue().toString();

                    if (dataSnapshot.hasChild("profileimage")){
                        String myProfileImage = dataSnapshot.child("profileimage").getValue().toString();
                        Picasso.with(SettingsActivity.this).load(myProfileImage).placeholder(R.drawable.profile).into(userProfImage);
                    }
                    else{
                        Picasso.with(SettingsActivity.this).load(R.drawable.profile).into(userProfImage);
                    }

                    if (dataSnapshot.hasChild("address")){
                        province_city.setText(dataSnapshot.child("address").getValue().toString());
                    }

                    userProfName.setText(myProfileName);
                    if (myGender.equals("Male"))
                        Gender.setSelection(0);
                    else
                        Gender.setSelection(1);

                    userPhone.setText(myPhone);

                    if (myBike.equals("true"))
                        checkBike.setChecked(true);
                    if (myMotor.equals("true"))
                        checkMotor.setChecked(true);

                    if (myBike.equals("true")){
                        wt.setVisibility(View.VISIBLE);
                        ht.setVisibility(View.VISIBLE);
                        at.setVisibility(View.VISIBLE);
                        bn.setVisibility(View.VISIBLE);
                        weight.setVisibility(View.VISIBLE);
                        height.setVisibility(View.VISIBLE);
                        age.setVisibility(View.VISIBLE);
                        WUnit.setVisibility(View.VISIBLE);
                        HUnit.setVisibility(View.VISIBLE);
                        String myWeight="", myHeight="",myAge="";
                        if (dataSnapshot.hasChild("savedweight")) {
                            if (dataSnapshot.child("savedweight").getValue().toString().equals("")) {
                                WUnit.setSelection(0);
                            }
                            else{
                                myWeight = dataSnapshot.child("savedweight").getValue().toString();
                                if (dataSnapshot.child("savedwunit").getValue().toString().equals("lbs"))
                                    WUnit.setSelection(1);
                                else
                                    WUnit.setSelection(0);
                            }
                        }

                        if (dataSnapshot.hasChild("savedheight")) {
                            if (dataSnapshot.child("savedheight").getValue().toString().equals("")) {
                                HUnit.setSelection(0);
                            }
                            else{
                                myHeight = dataSnapshot.child("savedheight").getValue().toString();
                                if (dataSnapshot.child("savedhunit").getValue().toString().equals("in"))
                                    HUnit.setSelection(1);
                                else
                                    HUnit.setSelection(0);
                            }
                        }

                        if (dataSnapshot.hasChild("age")) {
                            if (dataSnapshot.child("age").getValue().toString().equals("0")) {
                                myAge="";
                            }
                            else{
                                myAge = dataSnapshot.child("age").getValue().toString();
                            }
                        }

                        weight.setText(myWeight);
                        height.setText(myHeight);
                        age.setText(myAge);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        UpdateAccountSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ValidateAccountInfo();
            }
        });

        CancelUpdateButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        userProfImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,Gallery_Pick);
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

            userProfImage.setImageURI(ImageUri);

            final StorageReference filePath = UserProfileImageRef.child(currentUserId+".jpg");
            filePath.putFile(ImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String downloadUrl = uri.toString();
                            Toast.makeText(SettingsActivity.this, "Image uploaded successfully to storage", Toast.LENGTH_SHORT).show();
                            SettingsUserRef.child("profileimage").setValue(downloadUrl)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                Intent selfIntent = new Intent(SettingsActivity.this, SettingsActivity.class);
                                                startActivity(selfIntent);
                                                Toast.makeText(SettingsActivity.this,"Profile Image stored to firebase storage successfully .",Toast.LENGTH_SHORT).show();
                                            }
                                            else{
                                                Toast.makeText(SettingsActivity.this,"Error occurred:"+task.getException().getMessage(),Toast.LENGTH_SHORT).show();
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

    private void ValidateAccountInfo() {
        String gender = Gender.getSelectedItem().toString();
        String fullname = userProfName.getText().toString();
        String phone = userPhone.getText().toString();
        Boolean checkm = checkMotor.isChecked();
        Boolean checkb = checkBike.isChecked();
        String prov_city = province_city.getText().toString();

        if (TextUtils.isEmpty(fullname)) {
            Toast.makeText(this, "Please write your fullname...", Toast.LENGTH_SHORT).show();
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
            loadingBar.setMessage("Please wait, while we are updating your account...");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);
            HashMap userMap = new HashMap();
            userMap.put("fullname", fullname);
            userMap.put("phone", phone);
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
            userMap.put("address",prov_city);

            SettingsUserRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()){
                        Intent selfIntent = new Intent(SettingsActivity.this,SettingsActivity.class);
                        startActivity(selfIntent);
                        SendUserToMainActivity();
                        Toast.makeText(SettingsActivity.this,"Account Settings Updated Successfully", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        String message = task.getException().getMessage();
                        Toast.makeText(SettingsActivity.this,"Error Occurred:"+message,Toast.LENGTH_SHORT).show();
                    }
                    loadingBar.dismiss();
                }
            });
        }
    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(SettingsActivity.this, NavActivity.class);
        //mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        //finish();
    }
}
