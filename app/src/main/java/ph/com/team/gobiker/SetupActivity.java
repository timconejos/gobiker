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

import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {
    private EditText FullName, weight, height, age;
    private Button SaveInformationbutton;
    private CircleImageView ProfileImage;
    private CheckBox checkBike, checkMotor, checkPhone, checkAddress;

    private FirebaseAuth mAuth;
    private Uri ImageUri;
    private DatabaseReference UsersRef, ProvinceRef, CityRef;
    private ProgressDialog loadingBar;
    private StorageReference UserProfileImageRef;
    private Spinner Gender, WUnit, HUnit, province, city, active_ride;

    private TextView wt, ht, at, bn, bioinfo, aclabel;
    private View divider;

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
        ProvinceRef = FirebaseDatabase.getInstance().getReference().child("Province");
        CityRef = FirebaseDatabase.getInstance().getReference().child("City");

        wt = findViewById(R.id.setup_weight_text);
        ht = findViewById(R.id.setup_height_text);
        at = findViewById(R.id.setup_age_text);
        bn = findViewById(R.id.setup_bike_note);
        weight = findViewById(R.id.setup_weight);
        height = findViewById(R.id.setup_height);
        age = findViewById(R.id.setup_age);

        checkAddress = findViewById(R.id.setup_checkAddress);
        //checkPhone = findViewById(R.id.setup_checkPhone);

        divider = findViewById(R.id.divider2);
        bioinfo = findViewById(R.id.biometrics_info2);

        active_ride = findViewById(R.id.setup_active_ride);
        String[] itemsActiveRide = new String[]{"Bicycle", "Motorcycle"};
        ArrayAdapter<String> adapterActiveRide = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, itemsActiveRide);
        active_ride.setAdapter(adapterActiveRide);

        aclabel = findViewById(R.id.active_ride_label2);

        //province_city = findViewById(R.id.setup_address);

        province = findViewById(R.id.setup_province);
        city = findViewById(R.id.setup_city);

        ArrayList<String> itemsP = new ArrayList<String>();
        itemsP.add("");

        ProvinceRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (long i=0; i<dataSnapshot.getChildrenCount(); i++) {
                    itemsP.add(dataSnapshot.child(String.valueOf(i)).getValue().toString());
                }
                itemsP.remove(0);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        ArrayAdapter<String> adapterP = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, itemsP);
        province.setAdapter(adapterP);

        ArrayList<String> itemsC = new ArrayList<String>();

        province.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (!province.getSelectedItem().toString().equals("")) {
                    CityRef.child(province.getSelectedItem().toString()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            itemsC.clear();
                            for (long i = 0; i < dataSnapshot.getChildrenCount(); i++) {
                                itemsC.add(dataSnapshot.child(String.valueOf(i)).getValue().toString());
                            }
                            ArrayAdapter<String> adapterC = new ArrayAdapter<String>(SetupActivity.this, android.R.layout.simple_spinner_dropdown_item, itemsC);
                            city.setAdapter(adapterC);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
                else{
                    itemsC.clear();
                    itemsC.add("");
                    ArrayAdapter<String> adapterC = new ArrayAdapter<String>(SetupActivity.this, android.R.layout.simple_spinner_dropdown_item, itemsC);
                    city.setAdapter(adapterC);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

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
        divider.setVisibility(View.GONE);
        bioinfo.setVisibility(View.GONE);
        active_ride.setVisibility(View.GONE);
        aclabel.setVisibility(View.GONE);

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
                    divider.setVisibility(View.VISIBLE);
                    bioinfo.setVisibility(View.VISIBLE);
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
                    divider.setVisibility(View.GONE);
                    bioinfo.setVisibility(View.GONE);
                }

                if (checkBike.isChecked() && checkMotor.isChecked()){
                    active_ride.setVisibility(View.VISIBLE);
                    aclabel.setVisibility(View.VISIBLE);
                }
                else{
                    active_ride.setVisibility(View.GONE);
                    aclabel.setVisibility(View.GONE);
                }
            }
        });

        checkMotor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkBike.isChecked() && checkMotor.isChecked()){
                    active_ride.setVisibility(View.VISIBLE);
                    aclabel.setVisibility(View.VISIBLE);
                }
                else{
                    active_ride.setVisibility(View.GONE);
                    aclabel.setVisibility(View.GONE);
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
        String Province = province.getSelectedItem().toString();
        String City = city.getSelectedItem().toString();
        //String prov_city = province_city.getText().toString();
        //Boolean ckPhone = checkPhone.isChecked();
        Boolean ckAdd = checkAddress.isChecked();

        if (TextUtils.isEmpty(fullname)) {
            Toast.makeText(this, "Please write your fullname.", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(Province)) {
            Toast.makeText(this, "Please write your province.", Toast.LENGTH_SHORT).show();
        }
        else if (!checkm && !checkb){
            Toast.makeText(this, "Please select bicycle or motorcycle.",Toast.LENGTH_SHORT).show();
        }
        else{
            String hei="0", wei="0", yo="0", hUnit="0", wUnit="0", sh="0", sw="0";
            String acRide = "Bicycle";

            if (checkm && checkb){
                acRide = active_ride.getSelectedItem().toString();
            }
            else if (checkm){
                acRide = "Motorcycle";
            }
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
                else{
                    hei = "0";
                    sh = "0";
                    hUnit = "0";
                }

                if (!sw.equals("")){
                    wUnit = WUnit.getSelectedItem().toString();

                    if (wUnit.equals("kgs"))
                        wei = sw;
                    else
                        wei = Double.toString((Double.parseDouble(sw)/2.205));
                }
                else{
                    wei = "0";
                    sw = "0";
                    wUnit = "0";
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
            userMap.put("bike_level","1");
            userMap.put("bike_overall_distance","0");
            userMap.put("motor_level","1");
            userMap.put("motor_overall_distance","0");
            userMap.put("province",Province);
            userMap.put("city",City);
            userMap.put("active_ride",acRide);
            userMap.put("check_address",ckAdd);
            userMap.put("check_phone","true");

            UsersRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()){
                        SendUserToMainActivity();
                        Toast.makeText(SetupActivity.this,"Your account is created successfully.",Toast.LENGTH_LONG).show();
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
