package com.nav.monkeyseemonkeydo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.app.ActionBar;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    /** SUMMARY
     * Variable declarations */
    public static final int REQUEST_CODE = 101;
    public static final int CR_CODE = 102;
    private static final String TAG = "SettingsTag";
    EditText UserName, Name, Surname;
    String userID,   System, Xsystem;
    Button PwRst, sUname, sName, sSname;
    SwitchCompat MSys;
    FirebaseAuth mAuth;
    FirebaseFirestore fstore;
    CircleImageView ProfilePic,ProfilePic2nd;
    TextView MsysText;
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle actionBarDrawerToggle;
    Toolbar toolbar;
    NavigationView navigationView;
    ActionBar actionBar;
    SwitchCompat switchcompat;
    Menu menu;


    /** SUMMARY
     *  onCreate method, from here variables will be set to there element id if needed and othre methods will be called to preform specific functions
     * The users selected unit of measurement will be fetched from the DB
     * The users username will be collected from the DB
     * The users profile picture will be fetched from the DB*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        UserName = findViewById(R.id.edtChangeUserName);
        sUname = findViewById(R.id.btnChangeUserName);
        Name = findViewById(R.id.edtChangeName);
        sName = findViewById(R.id.btnChangeName);
        Surname = findViewById(R.id.edtChangeSurname);
        sSname = findViewById(R.id.btnChangeSurname);
        PwRst = findViewById(R.id.btnRstPws);
        mAuth = FirebaseAuth.getInstance();
        fstore = FirebaseFirestore.getInstance();
        userID = mAuth.getCurrentUser().getUid();
        ProfilePic2nd = findViewById(R.id.ProfilePic2nd);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSystem();
        drawerLayout = findViewById(R.id.drawer);
        navigationView = findViewById(R.id.navigationView);
        navigationView.bringToFront();
        navigationView.setNavigationItemSelectedListener(this);
        View view =  navigationView.getHeaderView(0);
        ProfilePic = (CircleImageView)view.findViewById(R.id.ImgProfile);

        //loads the user profile pic and their username
        LoadProfilePic();
        getUsernameDisplay();

        menu=navigationView.getMenu();


        switchcompat=(SwitchCompat) MenuItemCompat.getActionView(menu.findItem(R.id.nav_itemimp)).findViewById(R.id.switchImperial);

        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.setDrawerIndicatorEnabled(true);
        actionBarDrawerToggle.syncState();

        LoadProfilePic();


        sUname.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isEmpty(UserName)) {
                    Toast t = Toast.makeText(SettingsActivity.this, "You must enter a username!", Toast.LENGTH_SHORT);
                    t.show();
                }else
                    getUsername();
            }


        });

        sName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isEmpty(Name)) {
                    Toast t = Toast.makeText(SettingsActivity.this, "You must enter a First name!", Toast.LENGTH_SHORT);
                    t.show();
                }else
                    getFirstName();


            }


        });

        sSname.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isEmpty(Surname)) {
                    Toast t = Toast.makeText(SettingsActivity.this, "You must enter a Surname!", Toast.LENGTH_SHORT);
                    t.show();
                }else
                    getSurName();

            }


        });

    }


    /** SUMMARY
     * returns true or false, determines if their is a blank input */
    boolean isEmpty(EditText text) {
        CharSequence str = text.getText().toString();
        return TextUtils.isEmpty(str);
    }

    /** SUMMARY
     * Allows the user to reset their password
     * a link will be sent that they will need to follow*/
    public void ResetPw(View view) {
        final EditText resetMail = new EditText(view.getContext());
        AlertDialog.Builder passwordResetDialog = new AlertDialog.Builder(view.getContext());
        passwordResetDialog.setTitle("Reset Password?");
        passwordResetDialog.setMessage("Enter Your Email To Receive Reset Link");
        passwordResetDialog.setView(resetMail);

        passwordResetDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String mail = resetMail.getText().toString();
                mAuth.sendPasswordResetEmail(mail).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(SettingsActivity.this, "Reset Link Has Been Sent To Your Email", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(SettingsActivity.this, "Error! Reset Link Not Sent To Your Email" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });

        passwordResetDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        passwordResetDialog.create().show();
    }

    /** SUMMARY
     * fetches the username and allows them to edit it*/
    public void getUsername(){
        final DocumentReference documentReference = fstore.collection("users").document(userID);
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException error) {
                String Firstname,SurName,Username;

                Firstname = documentSnapshot.getString("Firstname");
                SurName = documentSnapshot.getString("Surname");
                Username = UserName.getText().toString();
                Map<String, Object> user =  new HashMap<>();
                user.put("Username",Username);
                user.put("Firstname",Firstname);
                user.put("Surname",SurName);
                documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "onSuccess: User profile and data saved" + userID);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: User Not Saved" + e.getMessage());
                    }
                });
            }
        });
    }

    /** SUMMARY
     * fetches the user firstname and allows them to edit it*/
    public void getFirstName(){
        final DocumentReference documentReference = fstore.collection("users").document(userID);
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException error) {
                String Firstname,SurName,Username;

                Firstname = Name.getText().toString();
                SurName = documentSnapshot.getString("Surname");
                Username = documentSnapshot.getString("Username");
                Map<String, Object> user =  new HashMap<>();
                user.put("Username",Username);
                user.put("Firstname",Firstname);
                user.put("Surname",SurName);
                documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "onSuccess: User profile and data saved" + userID);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: User Not Saved" + e.getMessage());
                    }
                });
            }
        });
    }

    /** SUMMARY
     * fetches the user Surname and allows them to edit it*/
    public void getSurName(){
        final DocumentReference documentReference = fstore.collection("users").document(userID);
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException error) {
                String Firstname,SurName,Username;

                Firstname = documentSnapshot.getString("Firstname");
                SurName = Surname.getText().toString();
                Username = documentSnapshot.getString("Username");
                Map<String, Object> user =  new HashMap<>();
                user.put("Username",Username);
                user.put("Firstname",Firstname);
                user.put("Surname",SurName);
                documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "onSuccess: User profile and data saved" + userID);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: User Not Saved" + e.getMessage());
                    }
                });
            }
        });
    }


    /** SUMMARY
     * calls the permission method*/
    public void SetProfileImg(View view) {
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

            DocumentReference documentReference = fstore.collection("ProfilePics").document(userID);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.PNG, 100, baos);
            String imageEncoded = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
            Map<String, Object> ProfilePics =  new HashMap<>();
            ProfilePics.put("Image",imageEncoded);
            documentReference.set(ProfilePics).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d(TAG, "onSuccess: User img saved" + userID);
                    Toast.makeText(SettingsActivity.this,  "Image saved", Toast.LENGTH_SHORT).show();

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, "onFailure: User img not Saved" + e.getMessage());
                    Toast.makeText(SettingsActivity.this,  "Image Not Saved", Toast.LENGTH_SHORT).show();
                }
            });
        }

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
                    ProfilePic.setImageBitmap(bitmap);
                    ProfilePic2nd.setImageBitmap(bitmap);
                }

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
                //startActivity(new Intent(this,SettingsActivity.class));
                return true;

        }
        return false;
    }

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
}