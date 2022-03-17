package com.nav.monkeyseemonkeydo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class SavedPicsActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    /** SUMMARY
     * Variable declarations */
    public static final int REQUEST_CODE = 101;
    public static final int CR_CODE = 102;
    private static final String TAG = "SettingsTag";
    SwitchCompat MSys;
    FirebaseAuth mAuth;
    FirebaseFirestore fstore;
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle actionBarDrawerToggle;
    Toolbar toolbar;
    NavigationView navigationView;
    ActionBar actionBar;
    SwitchCompat switchcompat;
    Menu menu;
    String userID,   System, Xsystem;
    CircleImageView ProfilePic2;

    /** SUMMARY
     *  onCreate method, from here variables will be set to there element id if needed and other methods will be called to preform specific functions
     * The users selected unit of measurement will be fetched from the DB
     * The users username will be collected from the DB
     * The users profile picture will be fetched from the DB
     * The users saved pictures will be fetched and displayed*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_pics);

        mAuth = FirebaseAuth.getInstance();
        fstore = FirebaseFirestore.getInstance();
        userID = mAuth.getCurrentUser().getUid();

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSystem();
        drawerLayout = findViewById(R.id.drawer);
        navigationView = findViewById(R.id.navigationView);
        navigationView.bringToFront();
        navigationView.setNavigationItemSelectedListener(this);
        View view =  navigationView.getHeaderView(0);
        ProfilePic2 = (CircleImageView)view.findViewById(R.id.ImgProfile);

        //loads the user profile pic and their username
        LoadProfilePic();
        getUsernameDisplay();
        getFavDetails();

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        //control the status bar
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);
        menu = navigationView.getMenu();



        switchcompat=(SwitchCompat) MenuItemCompat.getActionView(menu.findItem(R.id.nav_itemimp)).findViewById(R.id.switchImperial);

        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.setDrawerIndicatorEnabled(true);
        actionBarDrawerToggle.syncState();

        LoadProfilePic();

    }

    /** SUMMARY
     * fetches the unit of measurement*/
    public  void getSystem(){

        final DocumentReference documentReference = fstore.collection("MeasurementSystem").document(userID);
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException error) {

                Xsystem = documentSnapshot.getString("System");

                menu=navigationView.getMenu();

                if (Xsystem == null){
                    Xsystem = "Imperial";
                }

                if (Xsystem.equals("Imperial"))
                {
                    menu.findItem(R.id.nav_itemimp).setTitle("Imperial");
                    switchcompat.setChecked(false);
                }
                if (Xsystem.equals("Metric")){
                    menu.findItem(R.id.nav_itemimp).setTitle("Metric");
                    switchcompat.setChecked(true);
                }

                switcher();
            }
        });
    }


    /** SUMMARY
     * controls the switch for unit of measurement*/
    public void switcher(){
        switchcompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (switchcompat.isChecked()) {
                    menu.findItem(R.id.nav_itemimp).setTitle("Metric");
                    System = "Metric";
                }else if (!switchcompat.isChecked()){
                    menu.findItem(R.id.nav_itemimp).setTitle("Imperial");
                    System = "Imperial";
                }
                SetMeasurement();

            }
        });
    }

    /** SUMMARY
     * sets the unit of measurement*/
    public void SetMeasurement(){
        final DocumentReference documentReference = fstore.collection("MeasurementSystem").document(userID);
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException error) {

                Map<String, Object> MeasurementSystem =  new HashMap<>();
                MeasurementSystem.put("System",System);
                documentReference.set(MeasurementSystem).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "onSuccess: system saved" + userID);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: system Not Saved" + e.getMessage());
                    }
                });
            }
        });
    }

    /** SUMMARY
     * logs the user out*/
    public void logout() {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
        finish();
    }

    /** SUMMARY
     * listener for the items in the drawer menu */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
                logout();

                return true;
            case R.id.home:
                startActivity(new Intent(this,MainActivity.class));
                return true;
            case R.id.settings:
                startActivity(new Intent(this,SettingsActivity.class));
                return true;

        }
        return false;
    }
    /** SUMMARY
     * Gets the users name and displays it in the drawer menu */
    private void getUsernameDisplay() {
        final DocumentReference documentReference = fstore.collection("users").document(userID);
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException error) {

                String Username = documentSnapshot.getString("Username");

                menu=navigationView.getMenu();
                menu.findItem(R.id.user_name_display).setTitle(Username);



            }
        });
    }
    /** SUMMARY
     * fetches the users profile picture and loads it in to the img views*/
    public void LoadProfilePic(){


        String userID;
        mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getCurrentUser().getUid();

        final DocumentReference documentReference2 = fstore.collection("ProfilePics").document(userID);
        documentReference2.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException error) {
                String Bit = documentSnapshot.getString("Image");
                if(Bit != null)
                {
                    byte[] bytes = Base64.decode(Bit,Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                    ProfilePic2.setImageBitmap(bitmap);
                }

            }
        });


    }

    /** SUMMARY
     * Bottom nav bar
     * listens for which item has been selected
     * user can open up a list of their favourite places
     * user can view the map filter
     * user can click add to add a new favourite location*/
    @SuppressLint("NonConstantResourceId")
    private final BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = item -> {
        switch (item.getItemId()) {
            case R.id.action_favorites:
                startActivity(new Intent(SavedPicsActivity.this, MainActivity.class));

                return true;
            case R.id.action_add:
                    SetProfileImg();
                return true;
            case R.id.action_filter:
                getFavDetails();
                return true;
        }
        return false;
    };


    /** SUMMARY
     * calls the permission method*/
    public void SetProfileImg() {
        askCameraPermission();
    }

    /** SUMMARY
     * asks for permissions needed for using the camera*/
    private void askCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CODE);
        } else {
            openCamera();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();

            } else {
                Toast.makeText(this, "give cam", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /** SUMMARY
     * opens the camera on the users phone*/
    private void openCamera() {
        Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(camera, CR_CODE);
    }

    /** SUMMARY
     * if the user takes a picture and wants to keep it
     * the img is saved to the Db
     * then loaded in whenever it is needed*/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @NonNull Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CR_CODE && resultCode == RESULT_OK) {
            final Bitmap image = (Bitmap) data.getExtras().get("data");

            mAuth = FirebaseAuth.getInstance();
            userID = mAuth.getCurrentUser().getUid();
            Date c = Calendar.getInstance().getTime();
            SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault());
            String formattedDate = df.format(c);
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
            Map<String, Object> userpics = new HashMap<>();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.PNG, 100, baos);
            String imageEncoded = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
            userpics.put("Image",imageEncoded);
            userpics.put("Date", formattedDate);
            ref.child("userspics").child(userID).push().setValue(userpics);
            Toast.makeText(SavedPicsActivity.this, "Added", Toast.LENGTH_LONG).show();
            getFavDetails();

        }

    }
    /** SUMMARY
     * Loads all the images that the user has taken
     * */
    public void getFavDetails() {
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.linearlayout);
        linearLayout.removeAllViews();
        //Listens for any changes made to the database, so that any new entries can be fetched
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference();
        myRef.child("userspics").child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {


                    for (DataSnapshot user_pics : dataSnapshot.getChildren()) {
                        String image = (String) user_pics.child("Image").getValue();

                        if (image != null){
                            byte[] bytes = Base64.decode(image,Base64.DEFAULT);
                            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                            ImageView images = new ImageView(SavedPicsActivity.this);
                            images.setImageBitmap(bitmap);
                            images.setMinimumHeight(800);
                            images.setPadding(0,10,0,0);
                            linearLayout.addView(images);
                        }






                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }
}