package ph.com.team.gobiker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {
    private EditText userProfName, userPhone, weight, height, age;
    private CheckBox checkBike, checkMotor, checkPhone, checkAddress;
    private Button UpdateAccountSettingsButton, CancelUpdateButton;
    private CircleImageView userProfImage;
    private DatabaseReference SettingsUserRef, ProvinceRef, CityRef;
    private FirebaseAuth mAuth;
    private String currentUserId;
    private Uri ImageUri;
    private ProgressDialog loadingBar;
    private StorageReference UserProfileImageRef;
    private Spinner Gender, WUnit, HUnit, province, city, active_ride;
    final static int Gallery_Pick = 1;
    private TextView wt, ht, at, bn, aclabel, bioinfo;
    private View divider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        SettingsUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("profileimage");
        ProvinceRef = FirebaseDatabase.getInstance().getReference().child("Province");
        CityRef = FirebaseDatabase.getInstance().getReference().child("City");

        divider = findViewById(R.id.divider);
        bioinfo = findViewById(R.id.biometrics_info);

        userProfName = findViewById(R.id.settings_profile_full_name);
        userPhone = findViewById(R.id.settings_phone);
        Gender = findViewById(R.id.settings_gender);
        String[] items = new String[]{"Male", "Female","Rather Not Say"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        Gender.setAdapter(adapter);

        active_ride = findViewById(R.id.settings_active_ride);
        String[] itemsActiveRide = new String[]{"Bicycle", "Motorcycle"};
        ArrayAdapter<String> adapterActiveRide = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, itemsActiveRide);
        active_ride.setAdapter(adapterActiveRide);

        aclabel = findViewById(R.id.active_ride_label);
        checkBike = findViewById(R.id.settings_checkBoxBike);
        checkMotor = findViewById(R.id.settings_checkBoxMotor);
        checkAddress = findViewById(R.id.settings_checkAddress);
        checkPhone = findViewById(R.id.settings_checkPhone);

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
        province = findViewById(R.id.settings_province);
        city = findViewById(R.id.settings_city);

        ArrayList<String> itemsP = new ArrayList<String>();
        itemsP.add("Abra");
        itemsP.add("Agusan del Norte");
        itemsP.add("Agusan del Sur");
        itemsP.add("Aklan");
        itemsP.add("Albay");
        itemsP.add("Antique");
        itemsP.add("Apayao");
        itemsP.add("Aurora");
        itemsP.add("Basilan");
        itemsP.add("Bataan");
        itemsP.add("Batanes");
        itemsP.add("Batangas");
        itemsP.add("Benguet");
        itemsP.add("Biliran");
        itemsP.add("Bohol");
        itemsP.add("Bukidnon");
        itemsP.add("Bulacan");
        itemsP.add("Cagayan");
        itemsP.add("Camarines Norte");
        itemsP.add("Camarines Sur");
        itemsP.add("Camiguin");
        itemsP.add("Capiz");
        itemsP.add("Catanduanes");
        itemsP.add("Cavite");
        itemsP.add("Cebu");
        itemsP.add("Cotabato");
        itemsP.add("Davao de Oro");
        itemsP.add("Davao del Norte");
        itemsP.add("Davao del Sur");
        itemsP.add("Davao Occidental");
        itemsP.add("Davao Oriental");
        itemsP.add("Dinagat Islands");
        itemsP.add("Eastern Samar");
        itemsP.add("Guimaras");
        itemsP.add("Ifugao");
        itemsP.add("Ilocos Norte");
        itemsP.add("Ilocos Sur");
        itemsP.add("Iloilo");
        itemsP.add("Isabela");
        itemsP.add("Kalinga");
        itemsP.add("La Union");
        itemsP.add("Laguna");
        itemsP.add("Lanao del Norte");
        itemsP.add("Lanao del Sur");
        itemsP.add("Leyte");
        itemsP.add("Maguindanao");
        itemsP.add("Marinduque");
        itemsP.add("Masbate");
        itemsP.add("Metro Manila");
        itemsP.add("Misamis Occidental");
        itemsP.add("Misamis Oriental");
        itemsP.add("Mountain Province");
        itemsP.add("Negros Occidental");
        itemsP.add("Negros Oriental");
        itemsP.add("Northern Samar");
        itemsP.add("Nueva Ecija");
        itemsP.add("Nueva Vizcaya");
        itemsP.add("Occidental Mindoro");
        itemsP.add("Oriental Mindoro");
        itemsP.add("Palawan");
        itemsP.add("Pampanga");
        itemsP.add("Pangasinan");
        itemsP.add("Quezon");
        itemsP.add("Quirino");
        itemsP.add("Rizal");
        itemsP.add("Romblon");
        itemsP.add("Samar");
        itemsP.add("Sarangani");
        itemsP.add("Siquijor");
        itemsP.add("Sorsogon");
        itemsP.add("South Cotabato");
        itemsP.add("Southern Leyte");
        itemsP.add("Sultan Kudarat");
        itemsP.add("Sulu");
        itemsP.add("Surigao del Norte");
        itemsP.add("Surigao del Sur");
        itemsP.add("Tarlac");
        itemsP.add("Tawi-Tawi");
        itemsP.add("Zambales");
        itemsP.add("Zamboanga del Norte");
        itemsP.add("Zamboanga del Sur");
        itemsP.add("Zamboanga Sibugay");

        ArrayAdapter<String> adapterP = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, itemsP);
        province.setAdapter(adapterP);

        ArrayList<String> itemsC_Abra = new ArrayList<String>();
        ArrayList<String> itemsC_Agusan_del_Norte = new ArrayList<String>();
        ArrayList<String> itemsC_Agusan_del_Sur = new ArrayList<String>();
        ArrayList<String> itemsC_Aklan = new ArrayList<String>();
        ArrayList<String> itemsC_Albay = new ArrayList<String>();
        ArrayList<String> itemsC_Antique = new ArrayList<String>();
        ArrayList<String> itemsC_Apayao = new ArrayList<String>();
        ArrayList<String> itemsC_Aurora = new ArrayList<String>();
        ArrayList<String> itemsC_Basilan = new ArrayList<String>();
        ArrayList<String> itemsC_Bataan = new ArrayList<String>();
        ArrayList<String> itemsC_Batanes = new ArrayList<String>();
        ArrayList<String> itemsC_Batangas = new ArrayList<String>();
        ArrayList<String> itemsC_Benguet = new ArrayList<String>();
        ArrayList<String> itemsC_Biliran = new ArrayList<String>();
        ArrayList<String> itemsC_Bohol = new ArrayList<String>();
        ArrayList<String> itemsC_Bukidnon = new ArrayList<String>();
        ArrayList<String> itemsC_Bulacan = new ArrayList<String>();
        ArrayList<String> itemsC_Cagayan = new ArrayList<String>();
        ArrayList<String> itemsC_Camarines_Norte = new ArrayList<String>();
        ArrayList<String> itemsC_Camarines_Sur = new ArrayList<String>();
        ArrayList<String> itemsC_Camiguin = new ArrayList<String>();
        ArrayList<String> itemsC_Capiz = new ArrayList<String>();
        ArrayList<String> itemsC_Catanduanes = new ArrayList<String>();
        ArrayList<String> itemsC_Cavite = new ArrayList<String>();
        ArrayList<String> itemsC_Cebu = new ArrayList<String>();
        ArrayList<String> itemsC_Cotabato = new ArrayList<String>();
        ArrayList<String> itemsC_Davao_de_Oro = new ArrayList<String>();
        ArrayList<String> itemsC_Davao_del_Norte = new ArrayList<String>();
        ArrayList<String> itemsC_Davao_del_Sur = new ArrayList<String>();
        ArrayList<String> itemsC_Davao_Occidental = new ArrayList<String>();
        ArrayList<String> itemsC_Davao_Oriental = new ArrayList<String>();
        ArrayList<String> itemsC_Dinagat_Islands = new ArrayList<String>();
        ArrayList<String> itemsC_Eastern_Samar = new ArrayList<String>();
        ArrayList<String> itemsC_Guimaras = new ArrayList<String>();
        ArrayList<String> itemsC_Ifugao = new ArrayList<String>();
        ArrayList<String> itemsC_Ilocos_Norte = new ArrayList<String>();
        ArrayList<String> itemsC_Ilocos_Sur = new ArrayList<String>();
        ArrayList<String> itemsC_Iloilo = new ArrayList<String>();
        ArrayList<String> itemsC_Isabela = new ArrayList<String>();
        ArrayList<String> itemsC_Kalinga = new ArrayList<String>();
        ArrayList<String> itemsC_La_Union = new ArrayList<String>();
        ArrayList<String> itemsC_Laguna = new ArrayList<String>();
        ArrayList<String> itemsC_Lanao_del_Norte = new ArrayList<String>();
        ArrayList<String> itemsC_Lanao_del_Sur = new ArrayList<String>();
        ArrayList<String> itemsC_Leyte = new ArrayList<String>();
        ArrayList<String> itemsC_Maguindanao = new ArrayList<String>();
        ArrayList<String> itemsC_Marinduque = new ArrayList<String>();
        ArrayList<String> itemsC_Masbate = new ArrayList<String>();
        ArrayList<String> itemsC_Metro_Manila = new ArrayList<String>();
        ArrayList<String> itemsC_Misamis_Occidental = new ArrayList<String>();
        ArrayList<String> itemsC_Misamis_Oriental = new ArrayList<String>();
        ArrayList<String> itemsC_Mountain_Province = new ArrayList<String>();
        ArrayList<String> itemsC_Negros_Occidental = new ArrayList<String>();
        ArrayList<String> itemsC_Negros_Oriental = new ArrayList<String>();
        ArrayList<String> itemsC_Northern_Samar = new ArrayList<String>();
        ArrayList<String> itemsC_Nueva_Ecija = new ArrayList<String>();
        ArrayList<String> itemsC_Nueva_Vizcaya = new ArrayList<String>();
        ArrayList<String> itemsC_Occidental_Mindoro = new ArrayList<String>();
        ArrayList<String> itemsC_Oriental_Mindoro = new ArrayList<String>();
        ArrayList<String> itemsC_Palawan = new ArrayList<String>();
        ArrayList<String> itemsC_Pampanga = new ArrayList<String>();
        ArrayList<String> itemsC_Pangasinan = new ArrayList<String>();
        ArrayList<String> itemsC_Quezon = new ArrayList<String>();
        ArrayList<String> itemsC_Quirino = new ArrayList<String>();
        ArrayList<String> itemsC_Rizal = new ArrayList<String>();
        ArrayList<String> itemsC_Romblon = new ArrayList<String>();
        ArrayList<String> itemsC_Samar = new ArrayList<String>();
        ArrayList<String> itemsC_Sarangani = new ArrayList<String>();
        ArrayList<String> itemsC_Siquijor = new ArrayList<String>();
        ArrayList<String> itemsC_Sorsogon = new ArrayList<String>();
        ArrayList<String> itemsC_South_Cotabato = new ArrayList<String>();
        ArrayList<String> itemsC_Southern_Leyte = new ArrayList<String>();
        ArrayList<String> itemsC_Sultan_Kudarat = new ArrayList<String>();
        ArrayList<String> itemsC_Sulu = new ArrayList<String>();
        ArrayList<String> itemsC_Surigao_del_Norte = new ArrayList<String>();
        ArrayList<String> itemsC_Surigao_del_Sur = new ArrayList<String>();
        ArrayList<String> itemsC_Tarlac = new ArrayList<String>();
        ArrayList<String> itemsC_Tawi_Tawi = new ArrayList<String>();
        ArrayList<String> itemsC_Zambales = new ArrayList<String>();
        ArrayList<String> itemsC_Zamboanga_del_Norte = new ArrayList<String>();
        ArrayList<String> itemsC_Zamboanga_del_Sur = new ArrayList<String>();
        ArrayList<String> itemsC_Zamboanga_Sibugay = new ArrayList<String>();

        itemsC_Abra.add("Bangued");
        itemsC_Abra.add("Boliney");
        itemsC_Abra.add("Bucay");
        itemsC_Abra.add("Bucloc");
        itemsC_Abra.add("Daguioman");
        itemsC_Abra.add("Danglas");
        itemsC_Abra.add("Dolores");
        itemsC_Abra.add("La Paz");
        itemsC_Abra.add("Lacub");
        itemsC_Abra.add("Lagangilang");
        itemsC_Abra.add("Lagayan");
        itemsC_Abra.add("Langiden");
        itemsC_Abra.add("Licuan-Baay (Licuan)");
        itemsC_Abra.add("Luba");
        itemsC_Abra.add("Malibcong");
        itemsC_Abra.add("Manabo");
        itemsC_Abra.add("Penarrubia");
        itemsC_Abra.add("Pidigan");
        itemsC_Abra.add("Pilar");
        itemsC_Abra.add("Sallapadan");
        itemsC_Abra.add("San Isidro");
        itemsC_Abra.add("San Juan");
        itemsC_Abra.add("San Quintin");
        itemsC_Abra.add("Tayum");
        itemsC_Abra.add("Tineg");
        itemsC_Abra.add("Tubo");
        itemsC_Abra.add("Villaviciosa");
        itemsC_Agusan_del_Norte.add("Buenavista");
        itemsC_Agusan_del_Norte.add("Butuan");
        itemsC_Agusan_del_Norte.add("Cabadbaran");
        itemsC_Agusan_del_Norte.add("Carmen");
        itemsC_Agusan_del_Norte.add("Jabonga");
        itemsC_Agusan_del_Norte.add("Kitcharao");
        itemsC_Agusan_del_Norte.add("Las Nieves");
        itemsC_Agusan_del_Norte.add("Magallanes");
        itemsC_Agusan_del_Norte.add("Nasipit");
        itemsC_Agusan_del_Norte.add("Remedios T. Romualdez");
        itemsC_Agusan_del_Norte.add("Santiago");
        itemsC_Agusan_del_Norte.add("Tubay");
        itemsC_Agusan_del_Sur.add("Bayugan");
        itemsC_Agusan_del_Sur.add("Bunawan");
        itemsC_Agusan_del_Sur.add("Esperanza");
        itemsC_Agusan_del_Sur.add("La Paz");
        itemsC_Agusan_del_Sur.add("Loreto");
        itemsC_Agusan_del_Sur.add("Prosperidad");
        itemsC_Agusan_del_Sur.add("Rosario");
        itemsC_Agusan_del_Sur.add("San Francisco");
        itemsC_Agusan_del_Sur.add("San Luis");
        itemsC_Agusan_del_Sur.add("Santa Josefa");
        itemsC_Agusan_del_Sur.add("Sibagat");
        itemsC_Agusan_del_Sur.add("Talacogon");
        itemsC_Agusan_del_Sur.add("Trento");
        itemsC_Agusan_del_Sur.add("Veruela");
        itemsC_Aklan.add("Altavas");
        itemsC_Aklan.add("Balete");
        itemsC_Aklan.add("Banga");
        itemsC_Aklan.add("Batan");
        itemsC_Aklan.add("Buruanga");
        itemsC_Aklan.add("Ibajay");
        itemsC_Aklan.add("Kalibo");
        itemsC_Aklan.add("Lezo");
        itemsC_Aklan.add("Libacao");
        itemsC_Aklan.add("Madalag");
        itemsC_Aklan.add("Makato");
        itemsC_Aklan.add("Malay");
        itemsC_Aklan.add("Malinao");
        itemsC_Aklan.add("Nabas");
        itemsC_Aklan.add("New Washington");
        itemsC_Aklan.add("Numancia");
        itemsC_Aklan.add("Tangalan");
        itemsC_Albay.add("Bacacay");
        itemsC_Albay.add("Camalig");
        itemsC_Albay.add("Daraga (Locsin)");
        itemsC_Albay.add("Guinobatan");
        itemsC_Albay.add("Jovellar");
        itemsC_Albay.add("Legazpi");
        itemsC_Albay.add("Libon");
        itemsC_Albay.add("Ligao");
        itemsC_Albay.add("Malilipot");
        itemsC_Albay.add("Malinao");
        itemsC_Albay.add("Manito");
        itemsC_Albay.add("Oas");
        itemsC_Albay.add("Pio Duran");
        itemsC_Albay.add("Polangui");
        itemsC_Albay.add("Rapu-Rapu");
        itemsC_Albay.add("Santo Domingo");
        itemsC_Albay.add("Tabaco");
        itemsC_Albay.add("Tiwi");
        itemsC_Antique.add("Anini-y");
        itemsC_Antique.add("Barbaza");
        itemsC_Antique.add("Belison");
        itemsC_Antique.add("Bugasong");
        itemsC_Antique.add("Caluya");
        itemsC_Antique.add("Culasi");
        itemsC_Antique.add("Hamtic");
        itemsC_Antique.add("Laua-an");
        itemsC_Antique.add("Libertad");
        itemsC_Antique.add("Pandan");
        itemsC_Antique.add("Patnongon");
        itemsC_Antique.add("San Jose de Buenavista");
        itemsC_Antique.add("San Remigio");
        itemsC_Antique.add("Sebaste");
        itemsC_Antique.add("Sibalom");
        itemsC_Antique.add("Tibiao");
        itemsC_Antique.add("Tobias Fornier (Dao)");
        itemsC_Antique.add("Valderrama");
        itemsC_Apayao.add("Calanasan");
        itemsC_Apayao.add("Conner");
        itemsC_Apayao.add("Flora");
        itemsC_Apayao.add("Kabugao");
        itemsC_Apayao.add("Luna");
        itemsC_Apayao.add("Pudtol");
        itemsC_Apayao.add("Santa Marcela");
        itemsC_Aurora.add("Baler");
        itemsC_Aurora.add("Casiguran");
        itemsC_Aurora.add("Dilasag");
        itemsC_Aurora.add("Dinalungan");
        itemsC_Aurora.add("Dingalan");
        itemsC_Aurora.add("Dipaculao");
        itemsC_Aurora.add("Maria Aurora");
        itemsC_Aurora.add("San Luis");
        itemsC_Basilan.add("Akbar");
        itemsC_Basilan.add("Al-Barka");
        itemsC_Basilan.add("Hadji Mohammad Ajul");
        itemsC_Basilan.add("Hadji Muhtamad");
        itemsC_Basilan.add("Isabela City");
        itemsC_Basilan.add("Lamitan");
        itemsC_Basilan.add("Lantawan");
        itemsC_Basilan.add("Maluso");
        itemsC_Basilan.add("Sumisip");
        itemsC_Basilan.add("Tabuan-Lasa");
        itemsC_Basilan.add("Tipo-Tipo");
        itemsC_Basilan.add("Tuburan");
        itemsC_Basilan.add("Ungkaya Pukan");
        itemsC_Bataan.add("Abucay");
        itemsC_Bataan.add("Bagac");
        itemsC_Bataan.add("Balanga");
        itemsC_Bataan.add("Dinalupihan");
        itemsC_Bataan.add("Hermosa");
        itemsC_Bataan.add("Limay");
        itemsC_Bataan.add("Mariveles");
        itemsC_Bataan.add("Morong");
        itemsC_Bataan.add("Orani");
        itemsC_Bataan.add("Orion");
        itemsC_Bataan.add("Pilar");
        itemsC_Bataan.add("Samal");
        itemsC_Batanes.add("Basco");
        itemsC_Batanes.add("Itbayat");
        itemsC_Batanes.add("Ivana");
        itemsC_Batanes.add("Mahatao");
        itemsC_Batanes.add("Sabtang");
        itemsC_Batanes.add("Uyugan");
        itemsC_Batangas.add("Agoncillo");
        itemsC_Batangas.add("Alitagtag");
        itemsC_Batangas.add("Balayan");
        itemsC_Batangas.add("Balete");
        itemsC_Batangas.add("Batangas City");
        itemsC_Batangas.add("Bauan");
        itemsC_Batangas.add("Calaca");
        itemsC_Batangas.add("Calatagan");
        itemsC_Batangas.add("Cuenca");
        itemsC_Batangas.add("Ibaan");
        itemsC_Batangas.add("Laurel");
        itemsC_Batangas.add("Lemery");
        itemsC_Batangas.add("Lian");
        itemsC_Batangas.add("Lipa");
        itemsC_Batangas.add("Lobo");
        itemsC_Batangas.add("Mabini");
        itemsC_Batangas.add("Malvar");
        itemsC_Batangas.add("Mataasnakahoy");
        itemsC_Batangas.add("Nasugbu");
        itemsC_Batangas.add("Padre Garcia");
        itemsC_Batangas.add("Rosario");
        itemsC_Batangas.add("San Jose");
        itemsC_Batangas.add("San Juan");
        itemsC_Batangas.add("San Luis");
        itemsC_Batangas.add("San Nicolas");
        itemsC_Batangas.add("San Pascual");
        itemsC_Batangas.add("Santa Teresita");
        itemsC_Batangas.add("Santo Tomas");
        itemsC_Batangas.add("Taal");
        itemsC_Batangas.add("Talisay");
        itemsC_Batangas.add("Tanauan");
        itemsC_Batangas.add("Taysan");
        itemsC_Batangas.add("Tingloy");
        itemsC_Batangas.add("Tuy");
        itemsC_Benguet.add("Atok");
        itemsC_Benguet.add("Baguio");
        itemsC_Benguet.add("Bakun");
        itemsC_Benguet.add("Bokod");
        itemsC_Benguet.add("Buguias");
        itemsC_Benguet.add("Itogon");
        itemsC_Benguet.add("Kabayan");
        itemsC_Benguet.add("Kapangan");
        itemsC_Benguet.add("Kibungan");
        itemsC_Benguet.add("La Trinidad");
        itemsC_Benguet.add("Mankayan");
        itemsC_Benguet.add("Sablan");
        itemsC_Benguet.add("Tuba");
        itemsC_Benguet.add("Tublay");
        itemsC_Biliran.add("Almeria");
        itemsC_Biliran.add("Biliran");
        itemsC_Biliran.add("Cabucgayan");
        itemsC_Biliran.add("Caibiran");
        itemsC_Biliran.add("Culaba");
        itemsC_Biliran.add("Kawayan");
        itemsC_Biliran.add("Maripipi");
        itemsC_Biliran.add("Naval");
        itemsC_Bohol.add("Alburquerque");
        itemsC_Bohol.add("Alicia");
        itemsC_Bohol.add("Anda");
        itemsC_Bohol.add("Antequera");
        itemsC_Bohol.add("Baclayon");
        itemsC_Bohol.add("Balilihan");
        itemsC_Bohol.add("Batuan");
        itemsC_Bohol.add("Bien Unido");
        itemsC_Bohol.add("Bilar");
        itemsC_Bohol.add("Buenavista");
        itemsC_Bohol.add("Calape");
        itemsC_Bohol.add("Candijay");
        itemsC_Bohol.add("Carmen");
        itemsC_Bohol.add("Catigbian");
        itemsC_Bohol.add("Clarin");
        itemsC_Bohol.add("Corella");
        itemsC_Bohol.add("Cortes");
        itemsC_Bohol.add("Dagohoy");
        itemsC_Bohol.add("Danao");
        itemsC_Bohol.add("Dauis");
        itemsC_Bohol.add("Dimiao");
        itemsC_Bohol.add("Duero");
        itemsC_Bohol.add("Garcia Hernandez");
        itemsC_Bohol.add("Getafe");
        itemsC_Bohol.add("Guindulman");
        itemsC_Bohol.add("Inabanga");
        itemsC_Bohol.add("Jagna");
        itemsC_Bohol.add("Lila");
        itemsC_Bohol.add("Loay");
        itemsC_Bohol.add("Loboc");
        itemsC_Bohol.add("Loon");
        itemsC_Bohol.add("Mabini");
        itemsC_Bohol.add("Maribojoc");
        itemsC_Bohol.add("Panglao");
        itemsC_Bohol.add("Pilar");
        itemsC_Bohol.add("President Carlos P. Garcia (Pitogo)");
        itemsC_Bohol.add("Sagbayan (Borja)");
        itemsC_Bohol.add("San Isidro");
        itemsC_Bohol.add("San Miguel");
        itemsC_Bohol.add("Sevilla");
        itemsC_Bohol.add("Sierra Bullones");
        itemsC_Bohol.add("Sikatuna");
        itemsC_Bohol.add("Tagbilaran");
        itemsC_Bohol.add("Talibon");
        itemsC_Bohol.add("Trinidad");
        itemsC_Bohol.add("Tubigon");
        itemsC_Bohol.add("Ubay");
        itemsC_Bohol.add("Valencia");
        itemsC_Bukidnon.add("Baungon");
        itemsC_Bukidnon.add("Cabanglasan");
        itemsC_Bukidnon.add("Damulog");
        itemsC_Bukidnon.add("Dangcagan");
        itemsC_Bukidnon.add("Don Carlos");
        itemsC_Bukidnon.add("Impasugong");
        itemsC_Bukidnon.add("Kadingilan");
        itemsC_Bukidnon.add("Kalilangan");
        itemsC_Bukidnon.add("Kibawe");
        itemsC_Bukidnon.add("Kitaotao");
        itemsC_Bukidnon.add("Lantapan");
        itemsC_Bukidnon.add("Libona");
        itemsC_Bukidnon.add("Malaybalay");
        itemsC_Bukidnon.add("Malitbog");
        itemsC_Bukidnon.add("Manolo Fortich");
        itemsC_Bukidnon.add("Maramag");
        itemsC_Bukidnon.add("Pangantucan");
        itemsC_Bukidnon.add("Quezon");
        itemsC_Bukidnon.add("San Fernando");
        itemsC_Bukidnon.add("Sumilao");
        itemsC_Bukidnon.add("Talakag");
        itemsC_Bukidnon.add("Valencia");
        itemsC_Bulacan.add("Angat");
        itemsC_Bulacan.add("Balagtas (Bigaa)");
        itemsC_Bulacan.add("Baliuag");
        itemsC_Bulacan.add("Bocaue");
        itemsC_Bulacan.add("Bulakan");
        itemsC_Bulacan.add("Bustos");
        itemsC_Bulacan.add("Calumpit");
        itemsC_Bulacan.add("Dona Remedios Trinidad");
        itemsC_Bulacan.add("Guiguinto");
        itemsC_Bulacan.add("Hagonoy");
        itemsC_Bulacan.add("Malolos");
        itemsC_Bulacan.add("Marilao");
        itemsC_Bulacan.add("Meycauayan");
        itemsC_Bulacan.add("Norzagaray");
        itemsC_Bulacan.add("Obando");
        itemsC_Bulacan.add("Pandi");
        itemsC_Bulacan.add("Paombong");
        itemsC_Bulacan.add("Plaridel");
        itemsC_Bulacan.add("Pulilan");
        itemsC_Bulacan.add("San Ildefonso");
        itemsC_Bulacan.add("San Jose del Monte");
        itemsC_Bulacan.add("San Miguel");
        itemsC_Bulacan.add("San Rafael");
        itemsC_Bulacan.add("Santa Maria");
        itemsC_Cagayan.add("Abulug");
        itemsC_Cagayan.add("Alcala");
        itemsC_Cagayan.add("Allacapan");
        itemsC_Cagayan.add("Amulung");
        itemsC_Cagayan.add("Aparri");
        itemsC_Cagayan.add("Baggao");
        itemsC_Cagayan.add("Ballesteros");
        itemsC_Cagayan.add("Buguey");
        itemsC_Cagayan.add("Calayan");
        itemsC_Cagayan.add("Camalaniugan");
        itemsC_Cagayan.add("Claveria");
        itemsC_Cagayan.add("Enrile");
        itemsC_Cagayan.add("Gattaran");
        itemsC_Cagayan.add("Gonzaga");
        itemsC_Cagayan.add("Iguig");
        itemsC_Cagayan.add("Lal-lo");
        itemsC_Cagayan.add("Lasam");
        itemsC_Cagayan.add("Pamplona");
        itemsC_Cagayan.add("Penablanca");
        itemsC_Cagayan.add("Piat");
        itemsC_Cagayan.add("Rizal");
        itemsC_Cagayan.add("Sanchez-Mira");
        itemsC_Cagayan.add("Santa Ana");
        itemsC_Cagayan.add("Santa Praxedes");
        itemsC_Cagayan.add("Santa Teresita");
        itemsC_Cagayan.add("Santo Nino (Faire)");
        itemsC_Cagayan.add("Solana");
        itemsC_Cagayan.add("Tuao");
        itemsC_Cagayan.add("Tuguegarao");
        itemsC_Camarines_Norte.add("Basud");
        itemsC_Camarines_Norte.add("Capalonga");
        itemsC_Camarines_Norte.add("Daet");
        itemsC_Camarines_Norte.add("Jose Panganiban");
        itemsC_Camarines_Norte.add("Labo");
        itemsC_Camarines_Norte.add("Mercedes");
        itemsC_Camarines_Norte.add("Paracale");
        itemsC_Camarines_Norte.add("San Lorenzo Ruiz (Imelda)");
        itemsC_Camarines_Norte.add("San Vicente");
        itemsC_Camarines_Norte.add("Santa Elena");
        itemsC_Camarines_Norte.add("Talisay");
        itemsC_Camarines_Norte.add("Vinzons");
        itemsC_Camarines_Sur.add("Baao");
        itemsC_Camarines_Sur.add("Balatan");
        itemsC_Camarines_Sur.add("Bato");
        itemsC_Camarines_Sur.add("Bombon");
        itemsC_Camarines_Sur.add("Buhi");
        itemsC_Camarines_Sur.add("Bula");
        itemsC_Camarines_Sur.add("Cabusao");
        itemsC_Camarines_Sur.add("Calabanga");
        itemsC_Camarines_Sur.add("Camaligan");
        itemsC_Camarines_Sur.add("Canaman");
        itemsC_Camarines_Sur.add("Caramoan");
        itemsC_Camarines_Sur.add("Del Gallego");
        itemsC_Camarines_Sur.add("Gainza");
        itemsC_Camarines_Sur.add("Garchitorena");
        itemsC_Camarines_Sur.add("Goa");
        itemsC_Camarines_Sur.add("Iriga");
        itemsC_Camarines_Sur.add("Lagonoy");
        itemsC_Camarines_Sur.add("Libmanan");
        itemsC_Camarines_Sur.add("Lupi");
        itemsC_Camarines_Sur.add("Magarao");
        itemsC_Camarines_Sur.add("Milaor");
        itemsC_Camarines_Sur.add("Minalabac");
        itemsC_Camarines_Sur.add("Nabua");
        itemsC_Camarines_Sur.add("Naga");
        itemsC_Camarines_Sur.add("Ocampo");
        itemsC_Camarines_Sur.add("Pamplona");
        itemsC_Camarines_Sur.add("Pasacao");
        itemsC_Camarines_Sur.add("Pili");
        itemsC_Camarines_Sur.add("Presentacion (Parubcan)");
        itemsC_Camarines_Sur.add("Ragay");
        itemsC_Camarines_Sur.add("Sagnay");
        itemsC_Camarines_Sur.add("San Fernando");
        itemsC_Camarines_Sur.add("San Jose");
        itemsC_Camarines_Sur.add("Sipocot");
        itemsC_Camarines_Sur.add("Siruma");
        itemsC_Camarines_Sur.add("Tigaon");
        itemsC_Camarines_Sur.add("Tinambac");
        itemsC_Camiguin.add("Catarman");
        itemsC_Camiguin.add("Guinsiliban");
        itemsC_Camiguin.add("Mahinog");
        itemsC_Camiguin.add("Mambajao");
        itemsC_Camiguin.add("Sagay");
        itemsC_Capiz.add("Cuartero");
        itemsC_Capiz.add("Dao");
        itemsC_Capiz.add("Dumalag");
        itemsC_Capiz.add("Dumarao");
        itemsC_Capiz.add("Ivisan");
        itemsC_Capiz.add("Jamindan");
        itemsC_Capiz.add("Maayon");
        itemsC_Capiz.add("Mambusao");
        itemsC_Capiz.add("Panay");
        itemsC_Capiz.add("Panitan");
        itemsC_Capiz.add("Pilar");
        itemsC_Capiz.add("Pontevedra");
        itemsC_Capiz.add("President Roxas");
        itemsC_Capiz.add("Roxas City");
        itemsC_Capiz.add("Sapian");
        itemsC_Capiz.add("Sigma");
        itemsC_Capiz.add("Tapaz");
        itemsC_Catanduanes.add("Bagamanoc");
        itemsC_Catanduanes.add("Baras");
        itemsC_Catanduanes.add("Bato");
        itemsC_Catanduanes.add("Caramoran");
        itemsC_Catanduanes.add("Gigmoto");
        itemsC_Catanduanes.add("Pandan");
        itemsC_Catanduanes.add("Panganiban (Payo)");
        itemsC_Catanduanes.add("San Andres (Calolbon)");
        itemsC_Catanduanes.add("San Miguel");
        itemsC_Catanduanes.add("Viga");
        itemsC_Catanduanes.add("Virac");
        itemsC_Cavite.add("Alfonso");
        itemsC_Cavite.add("Amadeo");
        itemsC_Cavite.add("Bacoor");
        itemsC_Cavite.add("Carmona");
        itemsC_Cavite.add("Cavite City");
        itemsC_Cavite.add("Dasmarinas");
        itemsC_Cavite.add("General Emilio Aguinaldo");
        itemsC_Cavite.add("General Mariano Alvarez");
        itemsC_Cavite.add("General Trias");
        itemsC_Cavite.add("Imus");
        itemsC_Cavite.add("Indang");
        itemsC_Cavite.add("Kawit");
        itemsC_Cavite.add("Magallanes");
        itemsC_Cavite.add("Maragondon");
        itemsC_Cavite.add("Mendez (Mendez-Nunez)");
        itemsC_Cavite.add("Naic");
        itemsC_Cavite.add("Noveleta");
        itemsC_Cavite.add("Rosario");
        itemsC_Cavite.add("Silang");
        itemsC_Cavite.add("Tagaytay");
        itemsC_Cavite.add("Tanza");
        itemsC_Cavite.add("Ternate");
        itemsC_Cavite.add("Trece Martires");
        itemsC_Cebu.add("Alcantara");
        itemsC_Cebu.add("Alcoy");
        itemsC_Cebu.add("Alegria");
        itemsC_Cebu.add("Aloguinsan");
        itemsC_Cebu.add("Argao");
        itemsC_Cebu.add("Asturias");
        itemsC_Cebu.add("Badian");
        itemsC_Cebu.add("Balamban");
        itemsC_Cebu.add("Bantayan");
        itemsC_Cebu.add("Barili");
        itemsC_Cebu.add("Bogo");
        itemsC_Cebu.add("Boljoon");
        itemsC_Cebu.add("Borbon");
        itemsC_Cebu.add("Carcar");
        itemsC_Cebu.add("Carmen");
        itemsC_Cebu.add("Catmon");
        itemsC_Cebu.add("Cebu City");
        itemsC_Cebu.add("Compostela");
        itemsC_Cebu.add("Consolacion");
        itemsC_Cebu.add("Cordova");
        itemsC_Cebu.add("Daanbantayan");
        itemsC_Cebu.add("Dalaguete");
        itemsC_Cebu.add("Danao");
        itemsC_Cebu.add("Dumanjug");
        itemsC_Cebu.add("Ginatilan");
        itemsC_Cebu.add("Lapu-Lapu (Opon)");
        itemsC_Cebu.add("Liloan");
        itemsC_Cebu.add("Madridejos");
        itemsC_Cebu.add("Malabuyoc");
        itemsC_Cebu.add("Mandaue");
        itemsC_Cebu.add("Medellin");
        itemsC_Cebu.add("Minglanilla");
        itemsC_Cebu.add("Moalboal");
        itemsC_Cebu.add("Naga");
        itemsC_Cebu.add("Oslob");
        itemsC_Cebu.add("Pilar");
        itemsC_Cebu.add("Pinamungajan");
        itemsC_Cebu.add("Poro");
        itemsC_Cebu.add("Ronda");
        itemsC_Cebu.add("Samboan");
        itemsC_Cebu.add("San Fernando");
        itemsC_Cebu.add("San Francisco");
        itemsC_Cebu.add("San Remigio");
        itemsC_Cebu.add("Santa Fe");
        itemsC_Cebu.add("Santander");
        itemsC_Cebu.add("Sibonga");
        itemsC_Cebu.add("Sogod");
        itemsC_Cebu.add("Tabogon");
        itemsC_Cebu.add("Tabuelan");
        itemsC_Cebu.add("Talisay");
        itemsC_Cebu.add("Toledo");
        itemsC_Cebu.add("Tuburan");
        itemsC_Cebu.add("Tudela");
        itemsC_Cotabato.add("Alamada");
        itemsC_Cotabato.add("Aleosan");
        itemsC_Cotabato.add("Antipas");
        itemsC_Cotabato.add("Arakan");
        itemsC_Cotabato.add("Banisilan");
        itemsC_Cotabato.add("Carmen");
        itemsC_Cotabato.add("Kabacan");
        itemsC_Cotabato.add("Kidapawan");
        itemsC_Cotabato.add("Libungan");
        itemsC_Cotabato.add("M'lang");
        itemsC_Cotabato.add("Magpet");
        itemsC_Cotabato.add("Makilala");
        itemsC_Cotabato.add("Matalam");
        itemsC_Cotabato.add("Midsayap");
        itemsC_Cotabato.add("Pigcawayan");
        itemsC_Cotabato.add("Pikit");
        itemsC_Cotabato.add("President Roxas");
        itemsC_Cotabato.add("Tulunan");
        itemsC_Davao_de_Oro.add("Compostela");
        itemsC_Davao_de_Oro.add("Laak (San Vicente)");
        itemsC_Davao_de_Oro.add("Mabini (Dona Alicia)");
        itemsC_Davao_de_Oro.add("Maco");
        itemsC_Davao_de_Oro.add("Maragusan (San Mariano)");
        itemsC_Davao_de_Oro.add("Mawab");
        itemsC_Davao_de_Oro.add("Monkayo");
        itemsC_Davao_de_Oro.add("Montevista");
        itemsC_Davao_de_Oro.add("Nabunturan");
        itemsC_Davao_de_Oro.add("New Bataan");
        itemsC_Davao_de_Oro.add("Pantukan");
        itemsC_Davao_del_Norte.add("Asuncion (Saug)");
        itemsC_Davao_del_Norte.add("Braulio E. Dujali");
        itemsC_Davao_del_Norte.add("Carmen");
        itemsC_Davao_del_Norte.add("Kapalong");
        itemsC_Davao_del_Norte.add("New Corella");
        itemsC_Davao_del_Norte.add("Panabo");
        itemsC_Davao_del_Norte.add("Samal");
        itemsC_Davao_del_Norte.add("San Isidro");
        itemsC_Davao_del_Norte.add("Santo Tomas");
        itemsC_Davao_del_Norte.add("Tagum");
        itemsC_Davao_del_Norte.add("Talaingod");
        itemsC_Davao_del_Sur.add("Bansalan");
        itemsC_Davao_del_Sur.add("Davao City");
        itemsC_Davao_del_Sur.add("Digos");
        itemsC_Davao_del_Sur.add("Hagonoy");
        itemsC_Davao_del_Sur.add("Kiblawan");
        itemsC_Davao_del_Sur.add("Magsaysay");
        itemsC_Davao_del_Sur.add("Malalag");
        itemsC_Davao_del_Sur.add("Matanao");
        itemsC_Davao_del_Sur.add("Padada");
        itemsC_Davao_del_Sur.add("Santa Cruz");
        itemsC_Davao_del_Sur.add("Sulop");
        itemsC_Davao_Occidental.add("Don Marcelino");
        itemsC_Davao_Occidental.add("Jose Abad Santos (Trinidad)");
        itemsC_Davao_Occidental.add("Malita");
        itemsC_Davao_Occidental.add("Santa Maria");
        itemsC_Davao_Occidental.add("Sarangani");
        itemsC_Davao_Oriental.add("Baganga");
        itemsC_Davao_Oriental.add("Banaybanay");
        itemsC_Davao_Oriental.add("Boston");
        itemsC_Davao_Oriental.add("Caraga");
        itemsC_Davao_Oriental.add("Cateel");
        itemsC_Davao_Oriental.add("Governor Generoso");
        itemsC_Davao_Oriental.add("Lupon");
        itemsC_Davao_Oriental.add("Manay");
        itemsC_Davao_Oriental.add("Mati");
        itemsC_Davao_Oriental.add("San Isidro");
        itemsC_Davao_Oriental.add("Tarragona");
        itemsC_Dinagat_Islands.add("Basilisa (Rizal)");
        itemsC_Dinagat_Islands.add("Cagdianao");
        itemsC_Dinagat_Islands.add("Dinagat");
        itemsC_Dinagat_Islands.add("Libjo (Albor)");
        itemsC_Dinagat_Islands.add("Loreto");
        itemsC_Dinagat_Islands.add("San Jose");
        itemsC_Dinagat_Islands.add("Tubajon");
        itemsC_Eastern_Samar.add("Arteche");
        itemsC_Eastern_Samar.add("Balangiga");
        itemsC_Eastern_Samar.add("Balangkayan");
        itemsC_Eastern_Samar.add("Borongan");
        itemsC_Eastern_Samar.add("Can-avid");
        itemsC_Eastern_Samar.add("Dolores");
        itemsC_Eastern_Samar.add("General MacArthur");
        itemsC_Eastern_Samar.add("Giporlos");
        itemsC_Eastern_Samar.add("Guiuan");
        itemsC_Eastern_Samar.add("Hernani");
        itemsC_Eastern_Samar.add("Jipapad");
        itemsC_Eastern_Samar.add("Lawaan");
        itemsC_Eastern_Samar.add("Llorente");
        itemsC_Eastern_Samar.add("Maslog");
        itemsC_Eastern_Samar.add("Maydolong");
        itemsC_Eastern_Samar.add("Mercedes");
        itemsC_Eastern_Samar.add("Oras");
        itemsC_Eastern_Samar.add("Quinapondan");
        itemsC_Eastern_Samar.add("Salcedo");
        itemsC_Eastern_Samar.add("San Julian");
        itemsC_Eastern_Samar.add("San Policarpo");
        itemsC_Eastern_Samar.add("Sulat");
        itemsC_Eastern_Samar.add("Taft");
        itemsC_Guimaras.add("Buenavista");
        itemsC_Guimaras.add("Jordan");
        itemsC_Guimaras.add("Nueva Valencia");
        itemsC_Guimaras.add("San Lorenzo");
        itemsC_Guimaras.add("Sibunag");
        itemsC_Ifugao.add("Aguinaldo");
        itemsC_Ifugao.add("Alfonso Lista (Potia)");
        itemsC_Ifugao.add("Asipulo");
        itemsC_Ifugao.add("Banaue");
        itemsC_Ifugao.add("Hingyon");
        itemsC_Ifugao.add("Hungduan");
        itemsC_Ifugao.add("Kiangan");
        itemsC_Ifugao.add("Lagawe");
        itemsC_Ifugao.add("Lamut");
        itemsC_Ifugao.add("Mayoyao");
        itemsC_Ifugao.add("Tinoc");
        itemsC_Ilocos_Norte.add("Adams");
        itemsC_Ilocos_Norte.add("Bacarra");
        itemsC_Ilocos_Norte.add("Badoc");
        itemsC_Ilocos_Norte.add("Bangui");
        itemsC_Ilocos_Norte.add("Banna (Espiritu)");
        itemsC_Ilocos_Norte.add("Batac");
        itemsC_Ilocos_Norte.add("Burgos");
        itemsC_Ilocos_Norte.add("Carasi");
        itemsC_Ilocos_Norte.add("Currimao");
        itemsC_Ilocos_Norte.add("Dingras");
        itemsC_Ilocos_Norte.add("Dumalneg");
        itemsC_Ilocos_Norte.add("Laoag");
        itemsC_Ilocos_Norte.add("Marcos");
        itemsC_Ilocos_Norte.add("Nueva Era");
        itemsC_Ilocos_Norte.add("Pagudpud");
        itemsC_Ilocos_Norte.add("Paoay");
        itemsC_Ilocos_Norte.add("Pasuquin");
        itemsC_Ilocos_Norte.add("Piddig");
        itemsC_Ilocos_Norte.add("Pinili");
        itemsC_Ilocos_Norte.add("San Nicolas");
        itemsC_Ilocos_Norte.add("Sarrat");
        itemsC_Ilocos_Norte.add("Solsona");
        itemsC_Ilocos_Norte.add("Vintar");
        itemsC_Ilocos_Sur.add("Alilem");
        itemsC_Ilocos_Sur.add("Banayoyo");
        itemsC_Ilocos_Sur.add("Bantay");
        itemsC_Ilocos_Sur.add("Burgos");
        itemsC_Ilocos_Sur.add("Cabugao");
        itemsC_Ilocos_Sur.add("Candon");
        itemsC_Ilocos_Sur.add("Caoayan");
        itemsC_Ilocos_Sur.add("Cervantes");
        itemsC_Ilocos_Sur.add("Galimuyod");
        itemsC_Ilocos_Sur.add("Gregorio del Pilar (Concepcion)");
        itemsC_Ilocos_Sur.add("Lidlidda");
        itemsC_Ilocos_Sur.add("Magsingal");
        itemsC_Ilocos_Sur.add("Nagbukel");
        itemsC_Ilocos_Sur.add("Narvacan");
        itemsC_Ilocos_Sur.add("Quirino (Angkaki)");
        itemsC_Ilocos_Sur.add("Salcedo (Baugen)");
        itemsC_Ilocos_Sur.add("San Emilio");
        itemsC_Ilocos_Sur.add("San Esteban");
        itemsC_Ilocos_Sur.add("San Ildefonso");
        itemsC_Ilocos_Sur.add("San Juan (Lapog)");
        itemsC_Ilocos_Sur.add("San Vicente");
        itemsC_Ilocos_Sur.add("Santa");
        itemsC_Ilocos_Sur.add("Santa Catalina");
        itemsC_Ilocos_Sur.add("Santa Cruz");
        itemsC_Ilocos_Sur.add("Santa Lucia");
        itemsC_Ilocos_Sur.add("Santa Maria");
        itemsC_Ilocos_Sur.add("Santiago");
        itemsC_Ilocos_Sur.add("Santo Domingo");
        itemsC_Ilocos_Sur.add("Sigay");
        itemsC_Ilocos_Sur.add("Sinait");
        itemsC_Ilocos_Sur.add("Sugpon");
        itemsC_Ilocos_Sur.add("Suyo");
        itemsC_Ilocos_Sur.add("Tagudin");
        itemsC_Ilocos_Sur.add("Vigan");
        itemsC_Iloilo.add("Ajuy");
        itemsC_Iloilo.add("Alimodian");
        itemsC_Iloilo.add("Anilao");
        itemsC_Iloilo.add("Badiangan");
        itemsC_Iloilo.add("Balasan");
        itemsC_Iloilo.add("Banate");
        itemsC_Iloilo.add("Barotac Nuevo");
        itemsC_Iloilo.add("Barotac Viejo");
        itemsC_Iloilo.add("Batad");
        itemsC_Iloilo.add("Bingawan");
        itemsC_Iloilo.add("Cabatuan");
        itemsC_Iloilo.add("Calinog");
        itemsC_Iloilo.add("Carles");
        itemsC_Iloilo.add("Concepcion");
        itemsC_Iloilo.add("Dingle");
        itemsC_Iloilo.add("Duenas");
        itemsC_Iloilo.add("Dumangas");
        itemsC_Iloilo.add("Estancia");
        itemsC_Iloilo.add("Guimbal");
        itemsC_Iloilo.add("Igbaras");
        itemsC_Iloilo.add("Iloilo City");
        itemsC_Iloilo.add("Janiuay");
        itemsC_Iloilo.add("Lambunao");
        itemsC_Iloilo.add("Leganes");
        itemsC_Iloilo.add("Lemery");
        itemsC_Iloilo.add("Leon");
        itemsC_Iloilo.add("Maasin");
        itemsC_Iloilo.add("Miagao");
        itemsC_Iloilo.add("Mina");
        itemsC_Iloilo.add("New Lucena");
        itemsC_Iloilo.add("Oton");
        itemsC_Iloilo.add("Passi");
        itemsC_Iloilo.add("Pavia");
        itemsC_Iloilo.add("Pototan");
        itemsC_Iloilo.add("San Dionisio");
        itemsC_Iloilo.add("San Enrique");
        itemsC_Iloilo.add("San Joaquin");
        itemsC_Iloilo.add("San Miguel");
        itemsC_Iloilo.add("San Rafael");
        itemsC_Iloilo.add("Santa Barbara");
        itemsC_Iloilo.add("Sara");
        itemsC_Iloilo.add("Tigbauan");
        itemsC_Iloilo.add("Tubungan");
        itemsC_Iloilo.add("Zarraga");
        itemsC_Isabela.add("Alicia");
        itemsC_Isabela.add("Angadanan");
        itemsC_Isabela.add("Aurora");
        itemsC_Isabela.add("Benito Soliven");
        itemsC_Isabela.add("Burgos");
        itemsC_Isabela.add("Cabagan");
        itemsC_Isabela.add("Cabatuan");
        itemsC_Isabela.add("Cauayan");
        itemsC_Isabela.add("Cordon");
        itemsC_Isabela.add("Delfin Albano (Magsaysay)");
        itemsC_Isabela.add("Dinapigue");
        itemsC_Isabela.add("Divilacan");
        itemsC_Isabela.add("Echague");
        itemsC_Isabela.add("Gamu");
        itemsC_Isabela.add("Ilagan");
        itemsC_Isabela.add("Jones");
        itemsC_Isabela.add("Luna");
        itemsC_Isabela.add("Maconacon");
        itemsC_Isabela.add("Mallig");
        itemsC_Isabela.add("Naguilian");
        itemsC_Isabela.add("Palanan");
        itemsC_Isabela.add("Quezon");
        itemsC_Isabela.add("Quirino");
        itemsC_Isabela.add("Ramon");
        itemsC_Isabela.add("Reina Mercedes");
        itemsC_Isabela.add("Roxas");
        itemsC_Isabela.add("San Agustin");
        itemsC_Isabela.add("San Guillermo");
        itemsC_Isabela.add("San Isidro");
        itemsC_Isabela.add("San Manuel (Callang)");
        itemsC_Isabela.add("San Mariano");
        itemsC_Isabela.add("San Mateo");
        itemsC_Isabela.add("San Pablo");
        itemsC_Isabela.add("Santa Maria");
        itemsC_Isabela.add("Santiago");
        itemsC_Isabela.add("Santo Tomas");
        itemsC_Isabela.add("Tumauini");
        itemsC_Kalinga.add("Balbalan");
        itemsC_Kalinga.add("Lubuagan");
        itemsC_Kalinga.add("Pasil");
        itemsC_Kalinga.add("Pinukpuk");
        itemsC_Kalinga.add("Rizal (Liwan)");
        itemsC_Kalinga.add("Tabuk");
        itemsC_Kalinga.add("Tanudan");
        itemsC_Kalinga.add("Tinglayan");
        itemsC_La_Union.add("Agoo");
        itemsC_La_Union.add("Aringay");
        itemsC_La_Union.add("Bacnotan");
        itemsC_La_Union.add("Bagulin");
        itemsC_La_Union.add("Balaoan");
        itemsC_La_Union.add("Bangar");
        itemsC_La_Union.add("Bauang");
        itemsC_La_Union.add("Burgos");
        itemsC_La_Union.add("Caba");
        itemsC_La_Union.add("Luna");
        itemsC_La_Union.add("Naguilian");
        itemsC_La_Union.add("Pugo");
        itemsC_La_Union.add("Rosario");
        itemsC_La_Union.add("San Fernando");
        itemsC_La_Union.add("San Gabriel");
        itemsC_La_Union.add("San Juan");
        itemsC_La_Union.add("Santo Tomas");
        itemsC_La_Union.add("Santol");
        itemsC_La_Union.add("Sudipen");
        itemsC_La_Union.add("Tubao");
        itemsC_Laguna.add("Alaminos");
        itemsC_Laguna.add("Bay");
        itemsC_Laguna.add("Binan");
        itemsC_Laguna.add("Cabuyao");
        itemsC_Laguna.add("Calamba");
        itemsC_Laguna.add("Calauan");
        itemsC_Laguna.add("Cavinti");
        itemsC_Laguna.add("Famy");
        itemsC_Laguna.add("Kalayaan");
        itemsC_Laguna.add("Liliw");
        itemsC_Laguna.add("Los Banos");
        itemsC_Laguna.add("Luisiana");
        itemsC_Laguna.add("Lumban");
        itemsC_Laguna.add("Mabitac");
        itemsC_Laguna.add("Magdalena");
        itemsC_Laguna.add("Majayjay");
        itemsC_Laguna.add("Nagcarlan");
        itemsC_Laguna.add("Paete");
        itemsC_Laguna.add("Pagsanjan");
        itemsC_Laguna.add("Pakil");
        itemsC_Laguna.add("Pangil");
        itemsC_Laguna.add("Pila");
        itemsC_Laguna.add("Rizal");
        itemsC_Laguna.add("San Pablo");
        itemsC_Laguna.add("San Pedro");
        itemsC_Laguna.add("Santa Cruz");
        itemsC_Laguna.add("Santa Maria");
        itemsC_Laguna.add("Santa Rosa");
        itemsC_Laguna.add("Siniloan");
        itemsC_Laguna.add("Victoria");
        itemsC_Lanao_del_Norte.add("Bacolod");
        itemsC_Lanao_del_Norte.add("Baloi");
        itemsC_Lanao_del_Norte.add("Baroy");
        itemsC_Lanao_del_Norte.add("Iligan");
        itemsC_Lanao_del_Norte.add("Kapatagan");
        itemsC_Lanao_del_Norte.add("Kauswagan");
        itemsC_Lanao_del_Norte.add("Kolambugan");
        itemsC_Lanao_del_Norte.add("Lala");
        itemsC_Lanao_del_Norte.add("Linamon");
        itemsC_Lanao_del_Norte.add("Magsaysay");
        itemsC_Lanao_del_Norte.add("Maigo");
        itemsC_Lanao_del_Norte.add("Matungao");
        itemsC_Lanao_del_Norte.add("Munai");
        itemsC_Lanao_del_Norte.add("Nunungan");
        itemsC_Lanao_del_Norte.add("Pantao Ragat");
        itemsC_Lanao_del_Norte.add("Pantar");
        itemsC_Lanao_del_Norte.add("Poona Piagapo");
        itemsC_Lanao_del_Norte.add("Salvador");
        itemsC_Lanao_del_Norte.add("Sapad");
        itemsC_Lanao_del_Norte.add("Sultan Naga Dimaporo (Karomatan)");
        itemsC_Lanao_del_Norte.add("Tagoloan");
        itemsC_Lanao_del_Norte.add("Tangcal");
        itemsC_Lanao_del_Norte.add("Tubod");
        itemsC_Lanao_del_Sur.add("Amai Manabilang (Bumbaran)");
        itemsC_Lanao_del_Sur.add("Bacolod-Kalawi (Bacolod-Grande)");
        itemsC_Lanao_del_Sur.add("Balabagan");
        itemsC_Lanao_del_Sur.add("Balindong (Watu)");
        itemsC_Lanao_del_Sur.add("Bayang");
        itemsC_Lanao_del_Sur.add("Binidayan");
        itemsC_Lanao_del_Sur.add("Buadiposo-Buntong");
        itemsC_Lanao_del_Sur.add("Bubong");
        itemsC_Lanao_del_Sur.add("Butig");
        itemsC_Lanao_del_Sur.add("Calanogas");
        itemsC_Lanao_del_Sur.add("Ditsaan-Ramain");
        itemsC_Lanao_del_Sur.add("Ganassi");
        itemsC_Lanao_del_Sur.add("Kapai");
        itemsC_Lanao_del_Sur.add("Kapatagan");
        itemsC_Lanao_del_Sur.add("Lumba-Bayabao (Maguing)");
        itemsC_Lanao_del_Sur.add("Lumbaca-Unayan");
        itemsC_Lanao_del_Sur.add("Lumbatan");
        itemsC_Lanao_del_Sur.add("Lumbayanague");
        itemsC_Lanao_del_Sur.add("Madalum");
        itemsC_Lanao_del_Sur.add("Madamba");
        itemsC_Lanao_del_Sur.add("Maguing");
        itemsC_Lanao_del_Sur.add("Malabang");
        itemsC_Lanao_del_Sur.add("Marantao");
        itemsC_Lanao_del_Sur.add("Marawi");
        itemsC_Lanao_del_Sur.add("Marogong");
        itemsC_Lanao_del_Sur.add("Masiu");
        itemsC_Lanao_del_Sur.add("Mulondo");
        itemsC_Lanao_del_Sur.add("Pagayawan (Tatarikan)");
        itemsC_Lanao_del_Sur.add("Piagapo");
        itemsC_Lanao_del_Sur.add("Picong (Sultan Gumander)");
        itemsC_Lanao_del_Sur.add("Poona Bayabao (Gata)");
        itemsC_Lanao_del_Sur.add("Pualas");
        itemsC_Lanao_del_Sur.add("Saguiaran");
        itemsC_Lanao_del_Sur.add("Sultan Dumalondong");
        itemsC_Lanao_del_Sur.add("Tagoloan II");
        itemsC_Lanao_del_Sur.add("Tamparan");
        itemsC_Lanao_del_Sur.add("Taraka");
        itemsC_Lanao_del_Sur.add("Tubaran");
        itemsC_Lanao_del_Sur.add("Tugaya");
        itemsC_Lanao_del_Sur.add("Wao");
        itemsC_Leyte.add("Abuyog");
        itemsC_Leyte.add("Alangalang");
        itemsC_Leyte.add("Albuera");
        itemsC_Leyte.add("Babatngon");
        itemsC_Leyte.add("Barugo");
        itemsC_Leyte.add("Bato");
        itemsC_Leyte.add("Baybay");
        itemsC_Leyte.add("Burauen");
        itemsC_Leyte.add("Calubian");
        itemsC_Leyte.add("Capoocan");
        itemsC_Leyte.add("Carigara");
        itemsC_Leyte.add("Dagami");
        itemsC_Leyte.add("Dulag");
        itemsC_Leyte.add("Hilongos");
        itemsC_Leyte.add("Hindang");
        itemsC_Leyte.add("Inopacan");
        itemsC_Leyte.add("Isabel");
        itemsC_Leyte.add("Jaro");
        itemsC_Leyte.add("Javier (Bugho)");
        itemsC_Leyte.add("Julita");
        itemsC_Leyte.add("Kananga");
        itemsC_Leyte.add("La Paz");
        itemsC_Leyte.add("Leyte");
        itemsC_Leyte.add("MacArthur");
        itemsC_Leyte.add("Mahaplag");
        itemsC_Leyte.add("Matag-ob");
        itemsC_Leyte.add("Matalom");
        itemsC_Leyte.add("Mayorga");
        itemsC_Leyte.add("Merida");
        itemsC_Leyte.add("Ormoc");
        itemsC_Leyte.add("Palo");
        itemsC_Leyte.add("Palompon");
        itemsC_Leyte.add("Pastrana");
        itemsC_Leyte.add("San Isidro");
        itemsC_Leyte.add("San Miguel");
        itemsC_Leyte.add("Santa Fe");
        itemsC_Leyte.add("Tabango");
        itemsC_Leyte.add("Tabontabon");
        itemsC_Leyte.add("Tacloban");
        itemsC_Leyte.add("Tanauan");
        itemsC_Leyte.add("Tolosa");
        itemsC_Leyte.add("Tunga");
        itemsC_Leyte.add("Villaba");
        itemsC_Maguindanao.add("Ampatuan");
        itemsC_Maguindanao.add("Barira");
        itemsC_Maguindanao.add("Buldon");
        itemsC_Maguindanao.add("Buluan");
        itemsC_Maguindanao.add("Cotabato City");
        itemsC_Maguindanao.add("Datu Abdullah Sangki");
        itemsC_Maguindanao.add("Datu Anggal Midtimbang");
        itemsC_Maguindanao.add("Datu Blah T. Sinsuat");
        itemsC_Maguindanao.add("Datu Hoffer Ampatuan");
        itemsC_Maguindanao.add("Datu Montawal (Pagagawan)");
        itemsC_Maguindanao.add("Datu Odin Sinsuat (Dinaig)");
        itemsC_Maguindanao.add("Datu Paglas");
        itemsC_Maguindanao.add("Datu Piang (Dulawan)");
        itemsC_Maguindanao.add("Datu Salibo");
        itemsC_Maguindanao.add("Datu Saudi-Ampatuan");
        itemsC_Maguindanao.add("Datu Unsay");
        itemsC_Maguindanao.add("General Salipada K. Pendatun");
        itemsC_Maguindanao.add("Guindulungan");
        itemsC_Maguindanao.add("Kabuntalan (Tumbao)");
        itemsC_Maguindanao.add("Mamasapano");
        itemsC_Maguindanao.add("Mangudadatu");
        itemsC_Maguindanao.add("Matanog");
        itemsC_Maguindanao.add("Northern Kabuntalan");
        itemsC_Maguindanao.add("Pagalungan");
        itemsC_Maguindanao.add("Paglat");
        itemsC_Maguindanao.add("Pandag");
        itemsC_Maguindanao.add("Parang");
        itemsC_Maguindanao.add("Rajah Buayan");
        itemsC_Maguindanao.add("Shariff Aguak (Maganoy)");
        itemsC_Maguindanao.add("Shariff Saydona Mustapha");
        itemsC_Maguindanao.add("South Upi");
        itemsC_Maguindanao.add("Sultan Kudarat (Nuling)");
        itemsC_Maguindanao.add("Sultan Mastura");
        itemsC_Maguindanao.add("Sultan sa Barongis (Lambayong)");
        itemsC_Maguindanao.add("Sultan Sumagka (Talitay)");
        itemsC_Maguindanao.add("Talayan");
        itemsC_Maguindanao.add("Upi");
        itemsC_Marinduque.add("Boac");
        itemsC_Marinduque.add("Buenavista");
        itemsC_Marinduque.add("Gasan");
        itemsC_Marinduque.add("Mogpog");
        itemsC_Marinduque.add("Santa Cruz");
        itemsC_Marinduque.add("Torrijos");
        itemsC_Masbate.add("Aroroy");
        itemsC_Masbate.add("Baleno");
        itemsC_Masbate.add("Balud");
        itemsC_Masbate.add("Batuan");
        itemsC_Masbate.add("Cataingan");
        itemsC_Masbate.add("Cawayan");
        itemsC_Masbate.add("Claveria");
        itemsC_Masbate.add("Dimasalang");
        itemsC_Masbate.add("Esperanza");
        itemsC_Masbate.add("Mandaon");
        itemsC_Masbate.add("Masbate City");
        itemsC_Masbate.add("Milagros");
        itemsC_Masbate.add("Mobo");
        itemsC_Masbate.add("Monreal");
        itemsC_Masbate.add("Palanas");
        itemsC_Masbate.add("Pio V. Corpuz (Limbuhan)");
        itemsC_Masbate.add("Placer");
        itemsC_Masbate.add("San Fernando");
        itemsC_Masbate.add("San Jacinto");
        itemsC_Masbate.add("San Pascual");
        itemsC_Masbate.add("Uson");
        itemsC_Misamis_Occidental.add("Aloran");
        itemsC_Misamis_Occidental.add("Baliangao");
        itemsC_Misamis_Occidental.add("Bonifacio");
        itemsC_Misamis_Occidental.add("Calamba");
        itemsC_Misamis_Occidental.add("Clarin");
        itemsC_Misamis_Occidental.add("Concepcion");
        itemsC_Misamis_Occidental.add("Don Victoriano Chiongbian (Don Mariano Marcos)");
        itemsC_Misamis_Occidental.add("Jimenez");
        itemsC_Misamis_Occidental.add("Lopez Jaena");
        itemsC_Misamis_Occidental.add("Oroquieta");
        itemsC_Misamis_Occidental.add("Ozamiz");
        itemsC_Misamis_Occidental.add("Panaon");
        itemsC_Misamis_Occidental.add("Plaridel");
        itemsC_Misamis_Occidental.add("Sapang Dalaga");
        itemsC_Misamis_Occidental.add("Sinacaban");
        itemsC_Misamis_Occidental.add("Tangub");
        itemsC_Misamis_Occidental.add("Tudela");
        itemsC_Misamis_Oriental.add("Alubijid");
        itemsC_Misamis_Oriental.add("Balingasag");
        itemsC_Misamis_Oriental.add("Balingoan");
        itemsC_Misamis_Oriental.add("Binuangan");
        itemsC_Misamis_Oriental.add("Cagayan de Oro");
        itemsC_Misamis_Oriental.add("Claveria");
        itemsC_Misamis_Oriental.add("El Salvador");
        itemsC_Misamis_Oriental.add("Gingoog");
        itemsC_Misamis_Oriental.add("Gitagum");
        itemsC_Misamis_Oriental.add("Initao");
        itemsC_Misamis_Oriental.add("Jasaan");
        itemsC_Misamis_Oriental.add("Kinoguitan");
        itemsC_Misamis_Oriental.add("Lagonglong");
        itemsC_Misamis_Oriental.add("Laguindingan");
        itemsC_Misamis_Oriental.add("Libertad");
        itemsC_Misamis_Oriental.add("Lugait");
        itemsC_Misamis_Oriental.add("Magsaysay (Linugos)");
        itemsC_Misamis_Oriental.add("Manticao");
        itemsC_Misamis_Oriental.add("Medina");
        itemsC_Misamis_Oriental.add("Naawan");
        itemsC_Misamis_Oriental.add("Opol");
        itemsC_Misamis_Oriental.add("Salay");
        itemsC_Misamis_Oriental.add("Sugbongcogon");
        itemsC_Misamis_Oriental.add("Tagoloan");
        itemsC_Misamis_Oriental.add("Talisayan");
        itemsC_Misamis_Oriental.add("Villanueva");
        itemsC_Mountain_Province.add("Barlig");
        itemsC_Mountain_Province.add("Bauko");
        itemsC_Mountain_Province.add("Besao");
        itemsC_Mountain_Province.add("Bontoc");
        itemsC_Mountain_Province.add("Natonin");
        itemsC_Mountain_Province.add("Paracelis");
        itemsC_Mountain_Province.add("Sabangan");
        itemsC_Mountain_Province.add("Sadanga");
        itemsC_Mountain_Province.add("Sagada");
        itemsC_Mountain_Province.add("Tadian");
        itemsC_Metro_Manila.add("Caloocan");
        itemsC_Metro_Manila.add("Las Pinas");
        itemsC_Metro_Manila.add("Makati");
        itemsC_Metro_Manila.add("Malabon");
        itemsC_Metro_Manila.add("Mandaluyong");
        itemsC_Metro_Manila.add("Manila");
        itemsC_Metro_Manila.add("Marikina");
        itemsC_Metro_Manila.add("Muntinlupa");
        itemsC_Metro_Manila.add("Navotas");
        itemsC_Metro_Manila.add("Paranaque");
        itemsC_Metro_Manila.add("Pasay");
        itemsC_Metro_Manila.add("Pasig");
        itemsC_Metro_Manila.add("Pateros");
        itemsC_Metro_Manila.add("Quezon City");
        itemsC_Metro_Manila.add("San Juan");
        itemsC_Metro_Manila.add("Taguig");
        itemsC_Metro_Manila.add("Valenzuela");
        itemsC_Negros_Occidental.add("Bacolod");
        itemsC_Negros_Occidental.add("Bago");
        itemsC_Negros_Occidental.add("Binalbagan");
        itemsC_Negros_Occidental.add("Cadiz");
        itemsC_Negros_Occidental.add("Calatrava");
        itemsC_Negros_Occidental.add("Candoni");
        itemsC_Negros_Occidental.add("Cauayan");
        itemsC_Negros_Occidental.add("Enrique B. Magalona (Saravia)");
        itemsC_Negros_Occidental.add("Escalante");
        itemsC_Negros_Occidental.add("Himamaylan");
        itemsC_Negros_Occidental.add("Hinigaran");
        itemsC_Negros_Occidental.add("Hinoba-an (Asia)");
        itemsC_Negros_Occidental.add("Ilog");
        itemsC_Negros_Occidental.add("Isabela");
        itemsC_Negros_Occidental.add("Kabankalan");
        itemsC_Negros_Occidental.add("La Carlota");
        itemsC_Negros_Occidental.add("La Castellana");
        itemsC_Negros_Occidental.add("Manapla");
        itemsC_Negros_Occidental.add("Moises Padilla (Magallon)");
        itemsC_Negros_Occidental.add("Murcia");
        itemsC_Negros_Occidental.add("Pontevedra");
        itemsC_Negros_Occidental.add("Pulupandan");
        itemsC_Negros_Occidental.add("Sagay");
        itemsC_Negros_Occidental.add("Salvador Benedicto");
        itemsC_Negros_Occidental.add("San Carlos");
        itemsC_Negros_Occidental.add("San Enrique");
        itemsC_Negros_Occidental.add("Silay");
        itemsC_Negros_Occidental.add("Sipalay");
        itemsC_Negros_Occidental.add("Talisay");
        itemsC_Negros_Occidental.add("Toboso");
        itemsC_Negros_Occidental.add("Valladolid");
        itemsC_Negros_Occidental.add("Victorias");
        itemsC_Negros_Oriental.add("Amlan (Ayuquitan)");
        itemsC_Negros_Oriental.add("Ayungon");
        itemsC_Negros_Oriental.add("Bacong");
        itemsC_Negros_Oriental.add("Bais");
        itemsC_Negros_Oriental.add("Basay");
        itemsC_Negros_Oriental.add("Bayawan (Tulong)");
        itemsC_Negros_Oriental.add("Bindoy (Payabon)");
        itemsC_Negros_Oriental.add("Canlaon");
        itemsC_Negros_Oriental.add("Dauin");
        itemsC_Negros_Oriental.add("Dumaguete");
        itemsC_Negros_Oriental.add("Guihulngan");
        itemsC_Negros_Oriental.add("Jimalalud");
        itemsC_Negros_Oriental.add("La Libertad");
        itemsC_Negros_Oriental.add("Mabinay");
        itemsC_Negros_Oriental.add("Manjuyod");
        itemsC_Negros_Oriental.add("Pamplona");
        itemsC_Negros_Oriental.add("San Jose");
        itemsC_Negros_Oriental.add("Santa Catalina");
        itemsC_Negros_Oriental.add("Siaton");
        itemsC_Negros_Oriental.add("Sibulan");
        itemsC_Negros_Oriental.add("Tanjay");
        itemsC_Negros_Oriental.add("Tayasan");
        itemsC_Negros_Oriental.add("Valencia (Luzurriaga)");
        itemsC_Negros_Oriental.add("Vallehermoso");
        itemsC_Negros_Oriental.add("Zamboanguita");
        itemsC_Northern_Samar.add("Allen");
        itemsC_Northern_Samar.add("Biri");
        itemsC_Northern_Samar.add("Bobon");
        itemsC_Northern_Samar.add("Capul");
        itemsC_Northern_Samar.add("Catarman");
        itemsC_Northern_Samar.add("Catubig");
        itemsC_Northern_Samar.add("Gamay");
        itemsC_Northern_Samar.add("Laoang");
        itemsC_Northern_Samar.add("Lapinig");
        itemsC_Northern_Samar.add("Las Navas");
        itemsC_Northern_Samar.add("Lavezares");
        itemsC_Northern_Samar.add("Lope de Vega");
        itemsC_Northern_Samar.add("Mapanas");
        itemsC_Northern_Samar.add("Mondragon");
        itemsC_Northern_Samar.add("Palapag");
        itemsC_Northern_Samar.add("Pambujan");
        itemsC_Northern_Samar.add("Rosario");
        itemsC_Northern_Samar.add("San Antonio");
        itemsC_Northern_Samar.add("San Isidro");
        itemsC_Northern_Samar.add("San Jose");
        itemsC_Northern_Samar.add("San Roque");
        itemsC_Northern_Samar.add("San Vicente");
        itemsC_Northern_Samar.add("Silvino Lobos");
        itemsC_Northern_Samar.add("Victoria");
        itemsC_Nueva_Ecija.add("Aliaga");
        itemsC_Nueva_Ecija.add("Bongabon");
        itemsC_Nueva_Ecija.add("Cabanatuan");
        itemsC_Nueva_Ecija.add("Cabiao");
        itemsC_Nueva_Ecija.add("Carranglan");
        itemsC_Nueva_Ecija.add("Cuyapo");
        itemsC_Nueva_Ecija.add("Gabaldon (Bitulok & Sabani)");
        itemsC_Nueva_Ecija.add("Gapan");
        itemsC_Nueva_Ecija.add("General Mamerto Natividad");
        itemsC_Nueva_Ecija.add("General Tinio (Papaya)");
        itemsC_Nueva_Ecija.add("Guimba");
        itemsC_Nueva_Ecija.add("Jaen");
        itemsC_Nueva_Ecija.add("Laur");
        itemsC_Nueva_Ecija.add("Licab");
        itemsC_Nueva_Ecija.add("Llanera");
        itemsC_Nueva_Ecija.add("Lupao");
        itemsC_Nueva_Ecija.add("Munoz");
        itemsC_Nueva_Ecija.add("Nampicuan");
        itemsC_Nueva_Ecija.add("Palayan");
        itemsC_Nueva_Ecija.add("Pantabangan");
        itemsC_Nueva_Ecija.add("Penaranda");
        itemsC_Nueva_Ecija.add("Quezon");
        itemsC_Nueva_Ecija.add("Rizal");
        itemsC_Nueva_Ecija.add("San Antonio");
        itemsC_Nueva_Ecija.add("San Isidro");
        itemsC_Nueva_Ecija.add("San Jose");
        itemsC_Nueva_Ecija.add("San Leonardo");
        itemsC_Nueva_Ecija.add("Santa Rosa");
        itemsC_Nueva_Ecija.add("Santo Domingo");
        itemsC_Nueva_Ecija.add("Talavera");
        itemsC_Nueva_Ecija.add("Talugtug");
        itemsC_Nueva_Ecija.add("Zaragoza");
        itemsC_Nueva_Vizcaya.add("Alfonso Castaneda");
        itemsC_Nueva_Vizcaya.add("Ambaguio");
        itemsC_Nueva_Vizcaya.add("Aritao");
        itemsC_Nueva_Vizcaya.add("Bagabag");
        itemsC_Nueva_Vizcaya.add("Bambang");
        itemsC_Nueva_Vizcaya.add("Bayombong");
        itemsC_Nueva_Vizcaya.add("Diadi");
        itemsC_Nueva_Vizcaya.add("Dupax del Norte");
        itemsC_Nueva_Vizcaya.add("Dupax del Sur");
        itemsC_Nueva_Vizcaya.add("Kasibu");
        itemsC_Nueva_Vizcaya.add("Kayapa");
        itemsC_Nueva_Vizcaya.add("Quezon");
        itemsC_Nueva_Vizcaya.add("Santa Fe (Imugan)");
        itemsC_Nueva_Vizcaya.add("Solano");
        itemsC_Nueva_Vizcaya.add("Villaverde (Ibung)");
        itemsC_Occidental_Mindoro.add("Abra de Ilog");
        itemsC_Occidental_Mindoro.add("Calintaan");
        itemsC_Occidental_Mindoro.add("Looc");
        itemsC_Occidental_Mindoro.add("Lubang");
        itemsC_Occidental_Mindoro.add("Magsaysay");
        itemsC_Occidental_Mindoro.add("Mamburao");
        itemsC_Occidental_Mindoro.add("Paluan");
        itemsC_Occidental_Mindoro.add("Rizal");
        itemsC_Occidental_Mindoro.add("Sablayan");
        itemsC_Occidental_Mindoro.add("San Jose");
        itemsC_Occidental_Mindoro.add("Santa Cruz");
        itemsC_Oriental_Mindoro.add("Baco");
        itemsC_Oriental_Mindoro.add("Bansud");
        itemsC_Oriental_Mindoro.add("Bongabong");
        itemsC_Oriental_Mindoro.add("Bulalacao (San Pedro)");
        itemsC_Oriental_Mindoro.add("Calapan");
        itemsC_Oriental_Mindoro.add("Gloria");
        itemsC_Oriental_Mindoro.add("Mansalay");
        itemsC_Oriental_Mindoro.add("Naujan");
        itemsC_Oriental_Mindoro.add("Pinamalayan");
        itemsC_Oriental_Mindoro.add("Pola");
        itemsC_Oriental_Mindoro.add("Puerto Galera");
        itemsC_Oriental_Mindoro.add("Roxas");
        itemsC_Oriental_Mindoro.add("San Teodoro");
        itemsC_Oriental_Mindoro.add("Socorro");
        itemsC_Oriental_Mindoro.add("Victoria");
        itemsC_Palawan.add("Aborlan");
        itemsC_Palawan.add("Agutaya");
        itemsC_Palawan.add("Araceli");
        itemsC_Palawan.add("Balabac");
        itemsC_Palawan.add("Bataraza");
        itemsC_Palawan.add("Brooke's Point");
        itemsC_Palawan.add("Busuanga");
        itemsC_Palawan.add("Cagayancillo");
        itemsC_Palawan.add("Coron");
        itemsC_Palawan.add("Culion");
        itemsC_Palawan.add("Cuyo");
        itemsC_Palawan.add("Dumaran");
        itemsC_Palawan.add("El Nido (Bacuit)");
        itemsC_Palawan.add("Kalayaan");
        itemsC_Palawan.add("Linapacan");
        itemsC_Palawan.add("Magsaysay");
        itemsC_Palawan.add("Narra");
        itemsC_Palawan.add("Puerto Princesa");
        itemsC_Palawan.add("Quezon");
        itemsC_Palawan.add("Rizal (Marcos)");
        itemsC_Palawan.add("Roxas");
        itemsC_Palawan.add("San Vicente");
        itemsC_Palawan.add("Sofronio Espanola");
        itemsC_Palawan.add("Taytay");
        itemsC_Pampanga.add("Angeles");
        itemsC_Pampanga.add("Apalit");
        itemsC_Pampanga.add("Arayat");
        itemsC_Pampanga.add("Bacolor");
        itemsC_Pampanga.add("Candaba");
        itemsC_Pampanga.add("Floridablanca");
        itemsC_Pampanga.add("Guagua");
        itemsC_Pampanga.add("Lubao");
        itemsC_Pampanga.add("Mabalacat");
        itemsC_Pampanga.add("Macabebe");
        itemsC_Pampanga.add("Magalang");
        itemsC_Pampanga.add("Masantol");
        itemsC_Pampanga.add("Mexico");
        itemsC_Pampanga.add("Minalin");
        itemsC_Pampanga.add("Porac");
        itemsC_Pampanga.add("San Fernando");
        itemsC_Pampanga.add("San Luis");
        itemsC_Pampanga.add("San Simon");
        itemsC_Pampanga.add("Santa Ana");
        itemsC_Pampanga.add("Santa Rita");
        itemsC_Pampanga.add("Santo Tomas");
        itemsC_Pampanga.add("Sasmuan");
        itemsC_Pangasinan.add("Agno");
        itemsC_Pangasinan.add("Aguilar");
        itemsC_Pangasinan.add("Alaminos");
        itemsC_Pangasinan.add("Alcala");
        itemsC_Pangasinan.add("Anda");
        itemsC_Pangasinan.add("Asingan");
        itemsC_Pangasinan.add("Balungao");
        itemsC_Pangasinan.add("Bani");
        itemsC_Pangasinan.add("Basista");
        itemsC_Pangasinan.add("Bautista");
        itemsC_Pangasinan.add("Bayambang");
        itemsC_Pangasinan.add("Binalonan");
        itemsC_Pangasinan.add("Binmaley");
        itemsC_Pangasinan.add("Bolinao");
        itemsC_Pangasinan.add("Bugallon");
        itemsC_Pangasinan.add("Burgos");
        itemsC_Pangasinan.add("Calasiao");
        itemsC_Pangasinan.add("Dagupan");
        itemsC_Pangasinan.add("Dasol");
        itemsC_Pangasinan.add("Infanta");
        itemsC_Pangasinan.add("Labrador");
        itemsC_Pangasinan.add("Laoac");
        itemsC_Pangasinan.add("Lingayen");
        itemsC_Pangasinan.add("Mabini");
        itemsC_Pangasinan.add("Malasiqui");
        itemsC_Pangasinan.add("Manaoag");
        itemsC_Pangasinan.add("Mangaldan");
        itemsC_Pangasinan.add("Mangatarem");
        itemsC_Pangasinan.add("Mapandan");
        itemsC_Pangasinan.add("Natividad");
        itemsC_Pangasinan.add("Pozorrubio");
        itemsC_Pangasinan.add("Rosales");
        itemsC_Pangasinan.add("San Carlos");
        itemsC_Pangasinan.add("San Fabian");
        itemsC_Pangasinan.add("San Jacinto");
        itemsC_Pangasinan.add("San Manuel");
        itemsC_Pangasinan.add("San Nicolas");
        itemsC_Pangasinan.add("San Quintin");
        itemsC_Pangasinan.add("Santa Barbara");
        itemsC_Pangasinan.add("Santa Maria");
        itemsC_Pangasinan.add("Santo Tomas");
        itemsC_Pangasinan.add("Sison");
        itemsC_Pangasinan.add("Sual");
        itemsC_Pangasinan.add("Tayug");
        itemsC_Pangasinan.add("Umingan");
        itemsC_Pangasinan.add("Urbiztondo");
        itemsC_Pangasinan.add("Urdaneta");
        itemsC_Pangasinan.add("Villasis");
        itemsC_Quezon.add("Agdangan");
        itemsC_Quezon.add("Alabat");
        itemsC_Quezon.add("Atimonan");
        itemsC_Quezon.add("Buenavista");
        itemsC_Quezon.add("Burdeos");
        itemsC_Quezon.add("Calauag");
        itemsC_Quezon.add("Candelaria");
        itemsC_Quezon.add("Catanauan");
        itemsC_Quezon.add("Dolores");
        itemsC_Quezon.add("General Luna");
        itemsC_Quezon.add("General Nakar");
        itemsC_Quezon.add("Guinayangan");
        itemsC_Quezon.add("Gumaca");
        itemsC_Quezon.add("Infanta");
        itemsC_Quezon.add("Jomalig");
        itemsC_Quezon.add("Lopez");
        itemsC_Quezon.add("Lucban");
        itemsC_Quezon.add("Lucena");
        itemsC_Quezon.add("Macalelon");
        itemsC_Quezon.add("Mauban");
        itemsC_Quezon.add("Mulanay");
        itemsC_Quezon.add("Padre Burgos");
        itemsC_Quezon.add("Pagbilao");
        itemsC_Quezon.add("Panukulan");
        itemsC_Quezon.add("Patnanungan");
        itemsC_Quezon.add("Perez");
        itemsC_Quezon.add("Pitogo");
        itemsC_Quezon.add("Plaridel");
        itemsC_Quezon.add("Polillo");
        itemsC_Quezon.add("Quezon");
        itemsC_Quezon.add("Real");
        itemsC_Quezon.add("Sampaloc");
        itemsC_Quezon.add("San Andres");
        itemsC_Quezon.add("San Antonio");
        itemsC_Quezon.add("San Francisco (Aurora)");
        itemsC_Quezon.add("San Narciso");
        itemsC_Quezon.add("Sariaya");
        itemsC_Quezon.add("Tagkawayan");
        itemsC_Quezon.add("Tayabas");
        itemsC_Quezon.add("Tiaong");
        itemsC_Quezon.add("Unisan");
        itemsC_Quirino.add("Aglipay");
        itemsC_Quirino.add("Cabarroguis");
        itemsC_Quirino.add("Diffun");
        itemsC_Quirino.add("Maddela");
        itemsC_Quirino.add("Nagtipunan");
        itemsC_Quirino.add("Saguday");
        itemsC_Rizal.add("Angono");
        itemsC_Rizal.add("Antipolo");
        itemsC_Rizal.add("Baras");
        itemsC_Rizal.add("Binangonan");
        itemsC_Rizal.add("Cainta");
        itemsC_Rizal.add("Cardona");
        itemsC_Rizal.add("Jalajala");
        itemsC_Rizal.add("Morong");
        itemsC_Rizal.add("Pililla");
        itemsC_Rizal.add("Rodriguez (Montalban)");
        itemsC_Rizal.add("San Mateo");
        itemsC_Rizal.add("Tanay");
        itemsC_Rizal.add("Taytay");
        itemsC_Rizal.add("Teresa");
        itemsC_Romblon.add("Alcantara");
        itemsC_Romblon.add("Banton (Jones)");
        itemsC_Romblon.add("Cajidiocan");
        itemsC_Romblon.add("Calatrava");
        itemsC_Romblon.add("Concepcion");
        itemsC_Romblon.add("Corcuera");
        itemsC_Romblon.add("Ferrol");
        itemsC_Romblon.add("Looc");
        itemsC_Romblon.add("Magdiwang");
        itemsC_Romblon.add("Odiongan");
        itemsC_Romblon.add("Romblon");
        itemsC_Romblon.add("San Agustin");
        itemsC_Romblon.add("San Andres");
        itemsC_Romblon.add("San Fernando");
        itemsC_Romblon.add("San Jose");
        itemsC_Romblon.add("Santa Fe");
        itemsC_Romblon.add("Santa Maria (Imelda)");
        itemsC_Samar.add("Almagro");
        itemsC_Samar.add("Basey");
        itemsC_Samar.add("Calbayog");
        itemsC_Samar.add("Calbiga");
        itemsC_Samar.add("Catbalogan");
        itemsC_Samar.add("Daram");
        itemsC_Samar.add("Gandara");
        itemsC_Samar.add("Hinabangan");
        itemsC_Samar.add("Jiabong");
        itemsC_Samar.add("Marabut");
        itemsC_Samar.add("Matuguinao");
        itemsC_Samar.add("Motiong");
        itemsC_Samar.add("Pagsanghan");
        itemsC_Samar.add("Paranas (Wright)");
        itemsC_Samar.add("Pinabacdao");
        itemsC_Samar.add("San Jorge");
        itemsC_Samar.add("San Jose de Buan");
        itemsC_Samar.add("San Sebastian");
        itemsC_Samar.add("Santa Margarita");
        itemsC_Samar.add("Santa Rita");
        itemsC_Samar.add("Santo Nino");
        itemsC_Samar.add("Tagapul-an");
        itemsC_Samar.add("Talalora");
        itemsC_Samar.add("Tarangnan");
        itemsC_Samar.add("Villareal");
        itemsC_Samar.add("Zumarraga");
        itemsC_Sarangani.add("Alabel");
        itemsC_Sarangani.add("Glan");
        itemsC_Sarangani.add("Kiamba");
        itemsC_Sarangani.add("Maasim");
        itemsC_Sarangani.add("Maitum");
        itemsC_Sarangani.add("Malapatan");
        itemsC_Sarangani.add("Malungon");
        itemsC_Siquijor.add("Enrique Villanueva");
        itemsC_Siquijor.add("Larena");
        itemsC_Siquijor.add("Lazi");
        itemsC_Siquijor.add("Maria");
        itemsC_Siquijor.add("San Juan");
        itemsC_Siquijor.add("Siquijor");
        itemsC_Sorsogon.add("Barcelona");
        itemsC_Sorsogon.add("Bulan");
        itemsC_Sorsogon.add("Bulusan");
        itemsC_Sorsogon.add("Casiguran");
        itemsC_Sorsogon.add("Castilla");
        itemsC_Sorsogon.add("Donsol");
        itemsC_Sorsogon.add("Gubat");
        itemsC_Sorsogon.add("Irosin");
        itemsC_Sorsogon.add("Juban");
        itemsC_Sorsogon.add("Magallanes");
        itemsC_Sorsogon.add("Matnog");
        itemsC_Sorsogon.add("Pilar");
        itemsC_Sorsogon.add("Prieto Diaz");
        itemsC_Sorsogon.add("Santa Magdalena");
        itemsC_Sorsogon.add("Sorsogon City");
        itemsC_South_Cotabato.add("Banga");
        itemsC_South_Cotabato.add("General Santos (Dadiangas)");
        itemsC_South_Cotabato.add("Koronadal");
        itemsC_South_Cotabato.add("Lake Sebu");
        itemsC_South_Cotabato.add("Norala");
        itemsC_South_Cotabato.add("Polomolok");
        itemsC_South_Cotabato.add("Santo Nino");
        itemsC_South_Cotabato.add("Surallah");
        itemsC_South_Cotabato.add("T'Boli");
        itemsC_South_Cotabato.add("Tampakan");
        itemsC_South_Cotabato.add("Tantangan");
        itemsC_South_Cotabato.add("Tupi");
        itemsC_Southern_Leyte.add("Anahawan");
        itemsC_Southern_Leyte.add("Bontoc");
        itemsC_Southern_Leyte.add("Hinunangan");
        itemsC_Southern_Leyte.add("Hinundayan");
        itemsC_Southern_Leyte.add("Libagon");
        itemsC_Southern_Leyte.add("Liloan");
        itemsC_Southern_Leyte.add("Limasawa");
        itemsC_Southern_Leyte.add("Maasin");
        itemsC_Southern_Leyte.add("Macrohon");
        itemsC_Southern_Leyte.add("Malitbog");
        itemsC_Southern_Leyte.add("Padre Burgos");
        itemsC_Southern_Leyte.add("Pintuyan");
        itemsC_Southern_Leyte.add("Saint Bernard");
        itemsC_Southern_Leyte.add("San Francisco");
        itemsC_Southern_Leyte.add("San Juan (Cabalian)");
        itemsC_Southern_Leyte.add("San Ricardo");
        itemsC_Southern_Leyte.add("Silago");
        itemsC_Southern_Leyte.add("Sogod");
        itemsC_Southern_Leyte.add("Tomas Oppus");
        itemsC_Sultan_Kudarat.add("Bagumbayan");
        itemsC_Sultan_Kudarat.add("Columbio");
        itemsC_Sultan_Kudarat.add("Esperanza");
        itemsC_Sultan_Kudarat.add("Isulan");
        itemsC_Sultan_Kudarat.add("Kalamansig");
        itemsC_Sultan_Kudarat.add("Lambayong (Mariano Marcos)");
        itemsC_Sultan_Kudarat.add("Lebak");
        itemsC_Sultan_Kudarat.add("Lutayan");
        itemsC_Sultan_Kudarat.add("Palimbang");
        itemsC_Sultan_Kudarat.add("President Quirino");
        itemsC_Sultan_Kudarat.add("Senator Ninoy Aquino");
        itemsC_Sultan_Kudarat.add("Tacurong");
        itemsC_Sulu.add("Banguingui (Tongkil)");
        itemsC_Sulu.add("Hadji Panglima Tahil (Marunggas)");
        itemsC_Sulu.add("Indanan");
        itemsC_Sulu.add("Jolo");
        itemsC_Sulu.add("Kalingalan Caluang");
        itemsC_Sulu.add("Lugus");
        itemsC_Sulu.add("Luuk");
        itemsC_Sulu.add("Maimbung");
        itemsC_Sulu.add("Old Panamao");
        itemsC_Sulu.add("Omar");
        itemsC_Sulu.add("Pandami");
        itemsC_Sulu.add("Panglima Estino (New Panamao)");
        itemsC_Sulu.add("Pangutaran");
        itemsC_Sulu.add("Parang");
        itemsC_Sulu.add("Pata");
        itemsC_Sulu.add("Patikul");
        itemsC_Sulu.add("Siasi");
        itemsC_Sulu.add("Talipao");
        itemsC_Sulu.add("Tapul");
        itemsC_Surigao_del_Norte.add("Alegria");
        itemsC_Surigao_del_Norte.add("Bacuag");
        itemsC_Surigao_del_Norte.add("Burgos");
        itemsC_Surigao_del_Norte.add("Claver");
        itemsC_Surigao_del_Norte.add("Dapa");
        itemsC_Surigao_del_Norte.add("Del Carmen");
        itemsC_Surigao_del_Norte.add("General Luna");
        itemsC_Surigao_del_Norte.add("Gigaquit");
        itemsC_Surigao_del_Norte.add("Mainit");
        itemsC_Surigao_del_Norte.add("Malimono");
        itemsC_Surigao_del_Norte.add("Pilar");
        itemsC_Surigao_del_Norte.add("Placer");
        itemsC_Surigao_del_Norte.add("San Benito");
        itemsC_Surigao_del_Norte.add("San Francisco (Anao-Aon)");
        itemsC_Surigao_del_Norte.add("San Isidro");
        itemsC_Surigao_del_Norte.add("Santa Monica (Sapao)");
        itemsC_Surigao_del_Norte.add("Sison");
        itemsC_Surigao_del_Norte.add("Socorro");
        itemsC_Surigao_del_Norte.add("Surigao City");
        itemsC_Surigao_del_Norte.add("Tagana-an");
        itemsC_Surigao_del_Norte.add("Tubod");
        itemsC_Surigao_del_Sur.add("Barobo");
        itemsC_Surigao_del_Sur.add("Bayabas");
        itemsC_Surigao_del_Sur.add("Bislig");
        itemsC_Surigao_del_Sur.add("Cagwait");
        itemsC_Surigao_del_Sur.add("Cantilan");
        itemsC_Surigao_del_Sur.add("Carmen");
        itemsC_Surigao_del_Sur.add("Carrascal");
        itemsC_Surigao_del_Sur.add("Cortes");
        itemsC_Surigao_del_Sur.add("Hinatuan");
        itemsC_Surigao_del_Sur.add("Lanuza");
        itemsC_Surigao_del_Sur.add("Lianga");
        itemsC_Surigao_del_Sur.add("Lingig");
        itemsC_Surigao_del_Sur.add("Madrid");
        itemsC_Surigao_del_Sur.add("Marihatag");
        itemsC_Surigao_del_Sur.add("San Agustin");
        itemsC_Surigao_del_Sur.add("San Miguel");
        itemsC_Surigao_del_Sur.add("Tagbina");
        itemsC_Surigao_del_Sur.add("Tago");
        itemsC_Surigao_del_Sur.add("Tandag");
        itemsC_Tarlac.add("Anao");
        itemsC_Tarlac.add("Bamban");
        itemsC_Tarlac.add("Camiling");
        itemsC_Tarlac.add("Capas");
        itemsC_Tarlac.add("Concepcion");
        itemsC_Tarlac.add("Gerona");
        itemsC_Tarlac.add("La Paz");
        itemsC_Tarlac.add("Mayantoc");
        itemsC_Tarlac.add("Moncada");
        itemsC_Tarlac.add("Paniqui");
        itemsC_Tarlac.add("Pura");
        itemsC_Tarlac.add("Ramos");
        itemsC_Tarlac.add("San Clemente");
        itemsC_Tarlac.add("San Jose");
        itemsC_Tarlac.add("San Manuel");
        itemsC_Tarlac.add("Santa Ignacia");
        itemsC_Tarlac.add("Tarlac City");
        itemsC_Tarlac.add("Victoria");
        itemsC_Tawi_Tawi.add("Bongao");
        itemsC_Tawi_Tawi.add("Languyan");
        itemsC_Tawi_Tawi.add("Mapun (Cagayan de Tawi-Tawi)");
        itemsC_Tawi_Tawi.add("Panglima Sugala (Balimbing)");
        itemsC_Tawi_Tawi.add("Sapa-Sapa");
        itemsC_Tawi_Tawi.add("Sibutu");
        itemsC_Tawi_Tawi.add("Simunul");
        itemsC_Tawi_Tawi.add("Sitangkai");
        itemsC_Tawi_Tawi.add("South Ubian");
        itemsC_Tawi_Tawi.add("Tandubas");
        itemsC_Tawi_Tawi.add("Turtle Islands (Taganak)");
        itemsC_Zambales.add("Botolan");
        itemsC_Zambales.add("Cabangan");
        itemsC_Zambales.add("Candelaria");
        itemsC_Zambales.add("Castillejos");
        itemsC_Zambales.add("Iba");
        itemsC_Zambales.add("Masinloc");
        itemsC_Zambales.add("Olongapo");
        itemsC_Zambales.add("Palauig");
        itemsC_Zambales.add("San Antonio");
        itemsC_Zambales.add("San Felipe");
        itemsC_Zambales.add("San Marcelino");
        itemsC_Zambales.add("San Narciso");
        itemsC_Zambales.add("Santa Cruz");
        itemsC_Zambales.add("Subic");
        itemsC_Zamboanga_del_Norte.add("Baliguian");
        itemsC_Zamboanga_del_Norte.add("Dapitan");
        itemsC_Zamboanga_del_Norte.add("Dipolog");
        itemsC_Zamboanga_del_Norte.add("Godod");
        itemsC_Zamboanga_del_Norte.add("Gutalac");
        itemsC_Zamboanga_del_Norte.add("Jose Dalman (Ponot)");
        itemsC_Zamboanga_del_Norte.add("Kalawit");
        itemsC_Zamboanga_del_Norte.add("Katipunan");
        itemsC_Zamboanga_del_Norte.add("La Libertad");
        itemsC_Zamboanga_del_Norte.add("Labason");
        itemsC_Zamboanga_del_Norte.add("Leon B. Postigo (Bacungan)");
        itemsC_Zamboanga_del_Norte.add("Liloy");
        itemsC_Zamboanga_del_Norte.add("Manukan");
        itemsC_Zamboanga_del_Norte.add("Mutia");
        itemsC_Zamboanga_del_Norte.add("Pinan (New Pinan)");
        itemsC_Zamboanga_del_Norte.add("Polanco");
        itemsC_Zamboanga_del_Norte.add("President Manuel A. Roxas");
        itemsC_Zamboanga_del_Norte.add("Rizal");
        itemsC_Zamboanga_del_Norte.add("Salug");
        itemsC_Zamboanga_del_Norte.add("Sergio Osmena Sr.");
        itemsC_Zamboanga_del_Norte.add("Siayan");
        itemsC_Zamboanga_del_Norte.add("Sibuco");
        itemsC_Zamboanga_del_Norte.add("Sibutad");
        itemsC_Zamboanga_del_Norte.add("Sindangan");
        itemsC_Zamboanga_del_Norte.add("Siocon");
        itemsC_Zamboanga_del_Norte.add("Sirawai");
        itemsC_Zamboanga_del_Norte.add("Tampilisan");
        itemsC_Zamboanga_del_Sur.add("Aurora");
        itemsC_Zamboanga_del_Sur.add("Bayog");
        itemsC_Zamboanga_del_Sur.add("Dimataling");
        itemsC_Zamboanga_del_Sur.add("Dinas");
        itemsC_Zamboanga_del_Sur.add("Dumalinao");
        itemsC_Zamboanga_del_Sur.add("Dumingag");
        itemsC_Zamboanga_del_Sur.add("Guipos");
        itemsC_Zamboanga_del_Sur.add("Josefina");
        itemsC_Zamboanga_del_Sur.add("Kumalarang");
        itemsC_Zamboanga_del_Sur.add("Labangan");
        itemsC_Zamboanga_del_Sur.add("Lakewood");
        itemsC_Zamboanga_del_Sur.add("Lapuyan");
        itemsC_Zamboanga_del_Sur.add("Mahayag");
        itemsC_Zamboanga_del_Sur.add("Margosatubig");
        itemsC_Zamboanga_del_Sur.add("Midsalip");
        itemsC_Zamboanga_del_Sur.add("Molave");
        itemsC_Zamboanga_del_Sur.add("Pagadian");
        itemsC_Zamboanga_del_Sur.add("Pitogo");
        itemsC_Zamboanga_del_Sur.add("Ramon Magsaysay (Liargo)");
        itemsC_Zamboanga_del_Sur.add("San Miguel");
        itemsC_Zamboanga_del_Sur.add("San Pablo");
        itemsC_Zamboanga_del_Sur.add("Sominot (Don Mariano Marcos)");
        itemsC_Zamboanga_del_Sur.add("Tabina");
        itemsC_Zamboanga_del_Sur.add("Tambulig");
        itemsC_Zamboanga_del_Sur.add("Tigbao");
        itemsC_Zamboanga_del_Sur.add("Tukuran");
        itemsC_Zamboanga_del_Sur.add("Vincenzo A. Sagun");
        itemsC_Zamboanga_del_Sur.add("Zamboanga City");
        itemsC_Zamboanga_Sibugay.add("Alicia");
        itemsC_Zamboanga_Sibugay.add("Buug");
        itemsC_Zamboanga_Sibugay.add("Diplahan");
        itemsC_Zamboanga_Sibugay.add("Imelda");
        itemsC_Zamboanga_Sibugay.add("Ipil");
        itemsC_Zamboanga_Sibugay.add("Kabasalan");
        itemsC_Zamboanga_Sibugay.add("Mabuhay");
        itemsC_Zamboanga_Sibugay.add("Malangas");
        itemsC_Zamboanga_Sibugay.add("Naga");
        itemsC_Zamboanga_Sibugay.add("Olutanga");
        itemsC_Zamboanga_Sibugay.add("Payao");
        itemsC_Zamboanga_Sibugay.add("Roseller Lim");
        itemsC_Zamboanga_Sibugay.add("Siay");
        itemsC_Zamboanga_Sibugay.add("Talusan");
        itemsC_Zamboanga_Sibugay.add("Titay");
        itemsC_Zamboanga_Sibugay.add("Tungawan");


        //ArrayAdapter<String> adapterC = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, itemsC_Metro_Manila);
        //city.setAdapter(adapterC);

        province.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String pr = province.getSelectedItem().toString();
                if (!pr.equals("")) {
                    if (pr.equals("Abra")) setCity(itemsC_Abra);
                    if (pr.equals("Agusan del Norte")) setCity(itemsC_Agusan_del_Norte);
                    if (pr.equals("Agusan del Sur")) setCity(itemsC_Agusan_del_Sur);
                    if (pr.equals("Aklan")) setCity(itemsC_Aklan);
                    if (pr.equals("Albay")) setCity(itemsC_Albay);
                    if (pr.equals("Antique")) setCity(itemsC_Antique);
                    if (pr.equals("Apayao")) setCity(itemsC_Apayao);
                    if (pr.equals("Aurora")) setCity(itemsC_Aurora);
                    if (pr.equals("Basilan")) setCity(itemsC_Basilan);
                    if (pr.equals("Bataan")) setCity(itemsC_Bataan);
                    if (pr.equals("Batanes")) setCity(itemsC_Batanes);
                    if (pr.equals("Batangas")) setCity(itemsC_Batangas);
                    if (pr.equals("Benguet")) setCity(itemsC_Benguet);
                    if (pr.equals("Biliran")) setCity(itemsC_Biliran);
                    if (pr.equals("Bohol")) setCity(itemsC_Bohol);
                    if (pr.equals("Bukidnon")) setCity(itemsC_Bukidnon);
                    if (pr.equals("Bulacan")) setCity(itemsC_Bulacan);
                    if (pr.equals("Cagayan")) setCity(itemsC_Cagayan);
                    if (pr.equals("Camarines Norte")) setCity(itemsC_Camarines_Norte);
                    if (pr.equals("Camarines Sur")) setCity(itemsC_Camarines_Sur);
                    if (pr.equals("Camiguin")) setCity(itemsC_Camiguin);
                    if (pr.equals("Capiz")) setCity(itemsC_Capiz);
                    if (pr.equals("Catanduanes")) setCity(itemsC_Catanduanes);
                    if (pr.equals("Cavite")) setCity(itemsC_Cavite);
                    if (pr.equals("Cebu")) setCity(itemsC_Cebu);
                    if (pr.equals("Cotabato")) setCity(itemsC_Cotabato);
                    if (pr.equals("Davao de Oro")) setCity(itemsC_Davao_de_Oro);
                    if (pr.equals("Davao del Norte")) setCity(itemsC_Davao_del_Norte);
                    if (pr.equals("Davao del Sur")) setCity(itemsC_Davao_del_Sur);
                    if (pr.equals("Davao Occidental")) setCity(itemsC_Davao_Occidental);
                    if (pr.equals("Davao Oriental")) setCity(itemsC_Davao_Oriental);
                    if (pr.equals("Dinagat Islands")) setCity(itemsC_Dinagat_Islands);
                    if (pr.equals("Eastern Samar")) setCity(itemsC_Eastern_Samar);
                    if (pr.equals("Guimaras")) setCity(itemsC_Guimaras);
                    if (pr.equals("Ifugao")) setCity(itemsC_Ifugao);
                    if (pr.equals("Ilocos Norte")) setCity(itemsC_Ilocos_Norte);
                    if (pr.equals("Ilocos Sur")) setCity(itemsC_Ilocos_Sur);
                    if (pr.equals("Iloilo")) setCity(itemsC_Iloilo);
                    if (pr.equals("Isabela")) setCity(itemsC_Isabela);
                    if (pr.equals("Kalinga")) setCity(itemsC_Kalinga);
                    if (pr.equals("La Union")) setCity(itemsC_La_Union);
                    if (pr.equals("Laguna")) setCity(itemsC_Laguna);
                    if (pr.equals("Lanao del Norte")) setCity(itemsC_Lanao_del_Norte);
                    if (pr.equals("Lanao del Sur")) setCity(itemsC_Lanao_del_Sur);
                    if (pr.equals("Leyte")) setCity(itemsC_Leyte);
                    if (pr.equals("Maguindanao")) setCity(itemsC_Maguindanao);
                    if (pr.equals("Marinduque")) setCity(itemsC_Marinduque);
                    if (pr.equals("Masbate")) setCity(itemsC_Masbate);
                    if (pr.equals("Metro Manila")) setCity(itemsC_Metro_Manila);
                    if (pr.equals("Misamis Occidental")) setCity(itemsC_Misamis_Occidental);
                    if (pr.equals("Misamis Oriental")) setCity(itemsC_Misamis_Oriental);
                    if (pr.equals("Mountain Province")) setCity(itemsC_Mountain_Province);
                    if (pr.equals("Negros Occidental")) setCity(itemsC_Negros_Occidental);
                    if (pr.equals("Negros Oriental")) setCity(itemsC_Negros_Oriental);
                    if (pr.equals("Northern Samar")) setCity(itemsC_Northern_Samar);
                    if (pr.equals("Nueva Ecija")) setCity(itemsC_Nueva_Ecija);
                    if (pr.equals("Nueva Vizcaya")) setCity(itemsC_Nueva_Vizcaya);
                    if (pr.equals("Occidental Mindoro")) setCity(itemsC_Occidental_Mindoro);
                    if (pr.equals("Oriental Mindoro")) setCity(itemsC_Oriental_Mindoro);
                    if (pr.equals("Palawan")) setCity(itemsC_Palawan);
                    if (pr.equals("Pampanga")) setCity(itemsC_Pampanga);
                    if (pr.equals("Pangasinan")) setCity(itemsC_Pangasinan);
                    if (pr.equals("Quezon")) setCity(itemsC_Quezon);
                    if (pr.equals("Quirino")) setCity(itemsC_Quirino);
                    if (pr.equals("Rizal")) setCity(itemsC_Rizal);
                    if (pr.equals("Romblon")) setCity(itemsC_Romblon);
                    if (pr.equals("Samar")) setCity(itemsC_Samar);
                    if (pr.equals("Sarangani")) setCity(itemsC_Sarangani);
                    if (pr.equals("Siquijor")) setCity(itemsC_Siquijor);
                    if (pr.equals("Sorsogon")) setCity(itemsC_Sorsogon);
                    if (pr.equals("South Cotabato")) setCity(itemsC_South_Cotabato);
                    if (pr.equals("Southern Leyte")) setCity(itemsC_Southern_Leyte);
                    if (pr.equals("Sultan Kudarat")) setCity(itemsC_Sultan_Kudarat);
                    if (pr.equals("Sulu")) setCity(itemsC_Sulu);
                    if (pr.equals("Surigao del Norte")) setCity(itemsC_Surigao_del_Norte);
                    if (pr.equals("Surigao del Sur")) setCity(itemsC_Surigao_del_Sur);
                    if (pr.equals("Tarlac")) setCity(itemsC_Tarlac);
                    if (pr.equals("Tawi-Tawi")) setCity(itemsC_Tawi_Tawi);
                    if (pr.equals("Zambales")) setCity(itemsC_Zambales);
                    if (pr.equals("Zamboanga del Norte")) setCity(itemsC_Zamboanga_del_Norte);
                    if (pr.equals("Zamboanga del Sur")) setCity(itemsC_Zamboanga_del_Sur);
                    if (pr.equals("Zamboanga Sibugay")) setCity(itemsC_Zamboanga_Sibugay);

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        /*ProvinceRef.addValueEventListener(new ValueEventListener() {
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

        /*ArrayList<String> itemsC = new ArrayList<String>();

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
                            ArrayAdapter<String> adapterC = new ArrayAdapter<String>(SettingsActivity.this, android.R.layout.simple_spinner_dropdown_item, itemsC);
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
                    ArrayAdapter<String> adapterC = new ArrayAdapter<String>(SettingsActivity.this, android.R.layout.simple_spinner_dropdown_item, itemsC);
                    city.setAdapter(adapterC);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });*/

        WUnit = findViewById(R.id.settings_weight_unit);
        String[] itemsW = new String[]{"kgs", "lbs"};
        ArrayAdapter<String> adapterW = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, itemsW);
        WUnit.setAdapter(adapterW);

        HUnit = findViewById(R.id.settings_height_unit);
        String[] itemsH = new String[]{"cm", "in"};
        ArrayAdapter<String> adapterH = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, itemsH);
        HUnit.setAdapter(adapterH);

        /*wt.setVisibility(View.GONE);
        ht.setVisibility(View.GONE);
        at.setVisibility(View.GONE);
        bn.setVisibility(View.GONE);
        weight.setVisibility(View.GONE);
        height.setVisibility(View.GONE);
        age.setVisibility(View.GONE);
        WUnit.setVisibility(View.GONE);
        HUnit.setVisibility(View.GONE);
        active_ride.setVisibility(View.GONE);
        aclabel.setVisibility(View.GONE);
        divider.setVisibility(View.GONE);
        bioinfo.setVisibility(View.GONE);*/

        checkBike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*if (checkBike.isChecked()){
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
                }*/

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

        SettingsUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    String myProfileName = dataSnapshot.child("fullname").getValue().toString();
                    String myGender = dataSnapshot.child("gender").getValue().toString();
                    String myPhone = dataSnapshot.child("phone").getValue().toString();
                    String myBike = dataSnapshot.child("bike").getValue().toString();
                    String myMotor = dataSnapshot.child("motor").getValue().toString();
                    String myProvince = dataSnapshot.child("province").getValue().toString();
                    String myCity = dataSnapshot.child("city").getValue().toString();

                    if (dataSnapshot.hasChild("profileimage")){
                        String myProfileImage = dataSnapshot.child("profileimage").getValue().toString();
                        Picasso.with(SettingsActivity.this).load(myProfileImage).placeholder(R.drawable.profile).into(userProfImage);
                    }
                    else{
                        Picasso.with(SettingsActivity.this).load(R.drawable.profile).into(userProfImage);
                    }

                    if (dataSnapshot.hasChild("check_address")){
                        if (dataSnapshot.child("check_address").getValue().toString().equals("true")){
                            checkAddress.setChecked(true);
                        }
                        else{
                            checkAddress.setChecked(false);
                        }
                    }
                    else{
                        checkAddress.setChecked(true);
                    }

                    if (dataSnapshot.hasChild("check_phone")){
                        if (dataSnapshot.child("check_phone").getValue().toString().equals("true")){
                            checkPhone.setChecked(true);
                        }
                        else{
                            checkPhone.setChecked(false);
                        }
                    }
                    else{
                        checkPhone.setChecked(true);
                    }


                    userProfName.setText(myProfileName);
                    if (myGender.equals("Male"))
                        Gender.setSelection(0);
                    if (myGender.equals("Female"))
                        Gender.setSelection(1);
                    if (myGender.equals("Rather Not Say"))
                        Gender.setSelection(2);

                    province.setSelection(adapterP.getPosition(myProvince));

                    if (myProvince.equals("Abra")) setCity(itemsC_Abra,myCity);
                    if (myProvince.equals("Agusan del Norte")) setCity(itemsC_Agusan_del_Norte,myCity);
                    if (myProvince.equals("Agusan del Sur")) setCity(itemsC_Agusan_del_Sur,myCity);
                    if (myProvince.equals("Aklan")) setCity(itemsC_Aklan,myCity);
                    if (myProvince.equals("Albay")) setCity(itemsC_Albay,myCity);
                    if (myProvince.equals("Antique")) setCity(itemsC_Antique,myCity);
                    if (myProvince.equals("Apayao")) setCity(itemsC_Apayao,myCity);
                    if (myProvince.equals("Aurora")) setCity(itemsC_Aurora,myCity);
                    if (myProvince.equals("Basilan")) setCity(itemsC_Basilan,myCity);
                    if (myProvince.equals("Bataan")) setCity(itemsC_Bataan,myCity);
                    if (myProvince.equals("Batanes")) setCity(itemsC_Batanes,myCity);
                    if (myProvince.equals("Batangas")) setCity(itemsC_Batangas,myCity);
                    if (myProvince.equals("Benguet")) setCity(itemsC_Benguet,myCity);
                    if (myProvince.equals("Biliran")) setCity(itemsC_Biliran,myCity);
                    if (myProvince.equals("Bohol")) setCity(itemsC_Bohol,myCity);
                    if (myProvince.equals("Bukidnon")) setCity(itemsC_Bukidnon,myCity);
                    if (myProvince.equals("Bulacan")) setCity(itemsC_Bulacan,myCity);
                    if (myProvince.equals("Cagayan")) setCity(itemsC_Cagayan,myCity);
                    if (myProvince.equals("Camarines Norte")) setCity(itemsC_Camarines_Norte,myCity);
                    if (myProvince.equals("Camarines Sur")) setCity(itemsC_Camarines_Sur,myCity);
                    if (myProvince.equals("Camiguin")) setCity(itemsC_Camiguin,myCity);
                    if (myProvince.equals("Capiz")) setCity(itemsC_Capiz,myCity);
                    if (myProvince.equals("Catanduanes")) setCity(itemsC_Catanduanes,myCity);
                    if (myProvince.equals("Cavite")) setCity(itemsC_Cavite,myCity);
                    if (myProvince.equals("Cebu")) setCity(itemsC_Cebu,myCity);
                    if (myProvince.equals("Cotabato")) setCity(itemsC_Cotabato,myCity);
                    if (myProvince.equals("Davao de Oro")) setCity(itemsC_Davao_de_Oro,myCity);
                    if (myProvince.equals("Davao del Norte")) setCity(itemsC_Davao_del_Norte,myCity);
                    if (myProvince.equals("Davao del Sur")) setCity(itemsC_Davao_del_Sur,myCity);
                    if (myProvince.equals("Davao Occidental")) setCity(itemsC_Davao_Occidental,myCity);
                    if (myProvince.equals("Davao Oriental")) setCity(itemsC_Davao_Oriental,myCity);
                    if (myProvince.equals("Dinagat Islands")) setCity(itemsC_Dinagat_Islands,myCity);
                    if (myProvince.equals("Eastern Samar")) setCity(itemsC_Eastern_Samar,myCity);
                    if (myProvince.equals("Guimaras")) setCity(itemsC_Guimaras,myCity);
                    if (myProvince.equals("Ifugao")) setCity(itemsC_Ifugao,myCity);
                    if (myProvince.equals("Ilocos Norte")) setCity(itemsC_Ilocos_Norte,myCity);
                    if (myProvince.equals("Ilocos Sur")) setCity(itemsC_Ilocos_Sur,myCity);
                    if (myProvince.equals("Iloilo")) setCity(itemsC_Iloilo,myCity);
                    if (myProvince.equals("Isabela")) setCity(itemsC_Isabela,myCity);
                    if (myProvince.equals("Kalinga")) setCity(itemsC_Kalinga,myCity);
                    if (myProvince.equals("La Union")) setCity(itemsC_La_Union,myCity);
                    if (myProvince.equals("Laguna")) setCity(itemsC_Laguna,myCity);
                    if (myProvince.equals("Lanao del Norte")) setCity(itemsC_Lanao_del_Norte,myCity);
                    if (myProvince.equals("Lanao del Sur")) setCity(itemsC_Lanao_del_Sur,myCity);
                    if (myProvince.equals("Leyte")) setCity(itemsC_Leyte,myCity);
                    if (myProvince.equals("Maguindanao")) setCity(itemsC_Maguindanao,myCity);
                    if (myProvince.equals("Marinduque")) setCity(itemsC_Marinduque,myCity);
                    if (myProvince.equals("Masbate")) setCity(itemsC_Masbate,myCity);
                    if (myProvince.equals("Metro Manila")) setCity(itemsC_Metro_Manila,myCity);
                    if (myProvince.equals("Misamis Occidental")) setCity(itemsC_Misamis_Occidental,myCity);
                    if (myProvince.equals("Misamis Oriental")) setCity(itemsC_Misamis_Oriental,myCity);
                    if (myProvince.equals("Mountain Province")) setCity(itemsC_Mountain_Province,myCity);
                    if (myProvince.equals("Negros Occidental")) setCity(itemsC_Negros_Occidental,myCity);
                    if (myProvince.equals("Negros Oriental")) setCity(itemsC_Negros_Oriental,myCity);
                    if (myProvince.equals("Northern Samar")) setCity(itemsC_Northern_Samar,myCity);
                    if (myProvince.equals("Nueva Ecija")) setCity(itemsC_Nueva_Ecija,myCity);
                    if (myProvince.equals("Nueva Vizcaya")) setCity(itemsC_Nueva_Vizcaya,myCity);
                    if (myProvince.equals("Occidental Mindoro")) setCity(itemsC_Occidental_Mindoro,myCity);
                    if (myProvince.equals("Oriental Mindoro")) setCity(itemsC_Oriental_Mindoro,myCity);
                    if (myProvince.equals("Palawan")) setCity(itemsC_Palawan,myCity);
                    if (myProvince.equals("Pampanga")) setCity(itemsC_Pampanga,myCity);
                    if (myProvince.equals("Pangasinan")) setCity(itemsC_Pangasinan,myCity);
                    if (myProvince.equals("Quezon")) setCity(itemsC_Quezon,myCity);
                    if (myProvince.equals("Quirino")) setCity(itemsC_Quirino,myCity);
                    if (myProvince.equals("Rizal")) setCity(itemsC_Rizal,myCity);
                    if (myProvince.equals("Romblon")) setCity(itemsC_Romblon,myCity);
                    if (myProvince.equals("Samar")) setCity(itemsC_Samar,myCity);
                    if (myProvince.equals("Sarangani")) setCity(itemsC_Sarangani,myCity);
                    if (myProvince.equals("Siquijor")) setCity(itemsC_Siquijor,myCity);
                    if (myProvince.equals("Sorsogon")) setCity(itemsC_Sorsogon,myCity);
                    if (myProvince.equals("South Cotabato")) setCity(itemsC_South_Cotabato,myCity);
                    if (myProvince.equals("Southern Leyte")) setCity(itemsC_Southern_Leyte,myCity);
                    if (myProvince.equals("Sultan Kudarat")) setCity(itemsC_Sultan_Kudarat,myCity);
                    if (myProvince.equals("Sulu")) setCity(itemsC_Sulu,myCity);
                    if (myProvince.equals("Surigao del Norte")) setCity(itemsC_Surigao_del_Norte,myCity);
                    if (myProvince.equals("Surigao del Sur")) setCity(itemsC_Surigao_del_Sur,myCity);
                    if (myProvince.equals("Tarlac")) setCity(itemsC_Tarlac,myCity);
                    if (myProvince.equals("Tawi-Tawi")) setCity(itemsC_Tawi_Tawi,myCity);
                    if (myProvince.equals("Zambales")) setCity(itemsC_Zambales,myCity);
                    if (myProvince.equals("Zamboanga del Norte")) setCity(itemsC_Zamboanga_del_Norte,myCity);
                    if (myProvince.equals("Zamboanga del Sur")) setCity(itemsC_Zamboanga_del_Sur,myCity);
                    if (myProvince.equals("Zamboanga Sibugay")) setCity(itemsC_Zamboanga_Sibugay,myCity);

                    userPhone.setText(myPhone);

                    if (myBike.equals("true"))
                        checkBike.setChecked(true);
                    if (myMotor.equals("true"))
                        checkMotor.setChecked(true);

                    //if (myBike.equals("true")){
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
                        String myWeight="", myHeight="",myAge="";
                        if (dataSnapshot.hasChild("savedweight")) {
                            if (dataSnapshot.child("savedweight").getValue().toString().equals("0")) {
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
                            if (dataSnapshot.child("savedheight").getValue().toString().equals("0")) {
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
                    //}

                    if (myBike.equals("true") && myMotor.equals("true")){
                        aclabel.setVisibility(View.VISIBLE);
                        active_ride.setVisibility(View.VISIBLE);
                        if (dataSnapshot.hasChild("active_ride")){
                            String aride = dataSnapshot.child("active_ride").getValue().toString();
                            if (aride.equals("Bicycle"))
                                active_ride.setSelection(0);
                            else
                                active_ride.setSelection(1);
                        }
                    }
                    else{
                        aclabel.setVisibility(View.GONE);
                        active_ride.setVisibility(View.GONE);
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
        String Province = province.getSelectedItem().toString();
        String City = city.getSelectedItem().toString();
        Boolean ckPhone = checkPhone.isChecked();
        Boolean ckAdd = checkAddress.isChecked();

        if (TextUtils.isEmpty(fullname)) {
            Toast.makeText(this, "Please write your fullname...", Toast.LENGTH_SHORT).show();
        }
        else if (!checkm && !checkb){
            Toast.makeText(this, "Please select bicycle or motorcycle.",Toast.LENGTH_SHORT).show();
        }
        else{
            String hei="0", wei="0", yo="0", hUnit="0", wUnit="0", sh="0", sw="0";
            //if (checkb){
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
           //}

            String acRide = "Bicycle";

            if (checkm && checkb){
                acRide = active_ride.getSelectedItem().toString();
            }
            else if (checkm){
                acRide = "Motorcycle";
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
            userMap.put("province",Province);
            userMap.put("city",City);
            userMap.put("active_ride",acRide);
            userMap.put("check_address",ckAdd);
            userMap.put("check_phone",ckPhone);

            SettingsUserRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()){
                        Intent selfIntent = new Intent(SettingsActivity.this,SettingsActivity.class);
                        startActivity(selfIntent);
//                        SendUserToMainActivity();
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

    private void setCity(ArrayList<String> itemsC){
        ArrayAdapter<String> adapterC = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, itemsC);
        city.setAdapter(adapterC);
    }

    private void setCity(ArrayList<String> itemsC, String sel){
        ArrayAdapter<String> adapterC = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, itemsC);
        city.setAdapter(adapterC);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                city.setSelection(adapterC.getPosition(sel));
            }
        }, 1000);

    }
}
