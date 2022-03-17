package com.nav.monkeyseemonkeydo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.MenuItemCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    /** SUMMARY
     * Variable declarations */
    FloatingActionButton fab, currentLocation;
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle actionBarDrawerToggle;
    Toolbar toolbar;
    NavigationView navigationView;
    FusedLocationProviderClient client;
    SupportMapFragment supportMapFragment;
    FirebaseFirestore fstore;
    FirebaseAuth mAuth;
    String userID;
    String Ysystem;
    String Xsystem, System;
    SwitchCompat switchcompat;
    Menu menu;
    CircleImageView ProfilePic;
    String Distance;
    String Duration;
    Button btn_fav, btn_route;
    LatLng latLng;
    TextView distance, duration, type_measure, title, address, other, number;

    //////////////////////////////////

    private GoogleMap mMap;
    private MarkerOptions mMarkerOptions;
    private LatLng mOrigin;
    private LatLng mDestination;
    private Polyline mPolyline;
    private static final int REQUEST_CODE = 44;
    private static final int AUTOCOMPLETE_REQUEST_CODE = 22;
    private static final String TAG = MainActivity.class.getSimpleName();

    /** SUMMARY
     *  onCreate method, from here variables will be set to there element id if needed and othre methods will be called to preform specific functions
     * The users selected unit of measurement will be fetched from the DB
     * The users username will be collected from the DB
     * The users profile picture will be fetched from the DB
     * The users current location will be fetched and displayed on the map*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Firebase variables
        mAuth = FirebaseAuth.getInstance();
        fstore = FirebaseFirestore.getInstance();
        userID = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        distance = findViewById(R.id.distance);
        duration = findViewById(R.id.duration);
        type_measure = findViewById(R.id.type_measure);

        //Fetches username and unit of measurement
        getSystem();
        getUsername();

        //Tool and nav bars
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawerLayout = findViewById(R.id.drawer);
        navigationView = findViewById(R.id.navigationView);
        navigationView.bringToFront();
        navigationView.setNavigationItemSelectedListener(this);
        View view = navigationView.getHeaderView(0);
        ProfilePic = (CircleImageView) view.findViewById(R.id.ImgProfile);

        //fetches the users profile pic
        LoadProfilePic();

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        //control the status bar
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);
        menu = navigationView.getMenu();

        //Controls the drawer menu and the switch found in the menu
        switchcompat = (SwitchCompat) MenuItemCompat.getActionView(menu.findItem(R.id.nav_itemimp)).findViewById(R.id.switchImperial);

        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.setDrawerIndicatorEnabled(true);
        actionBarDrawerToggle.syncState();

        //controls the floating button clicks
        currentLocation = findViewById(R.id.current_location);
        currentLocation.setOnClickListener(view1 -> getCurrentLocation());

        fab = findViewById(R.id.floatingActionButton);
        fab.setOnClickListener(view12 -> onSearchCalled());

        //Refers to the map fragment in the main activity layout
        supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapid);

        //fetches current location
        client = LocationServices.getFusedLocationProviderClient(this);

        //Checks if user has given need permissions
        if (ActivityCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
        } else {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
        }

    }





    /** SUMMARY
     * methods for user interface and navigation
     * methods to fetch user data from the FirebaseFirestore
     * method for fetching the users favourites list*/

    /** SUMMARY
     * Fetches the username */
    public void getUsername() {

        final DocumentReference documentReference = fstore.collection("users").document(userID);
        documentReference.addSnapshotListener(this, (documentSnapshot, error) -> {

           // assert documentSnapshot != null;
            String Username = documentSnapshot.getString("Username");

            menu = navigationView.getMenu();
            menu.findItem(R.id.user_name_display).setTitle(Username);


        });
    }

    /** SUMMARY
     * Logs the user out
     * Returns them to the login page*/
    public void logout() {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
        finish();
    }

    /** SUMMARY
     *Top nav bar
     * method that listens for which item has been selected
     * the options are to logout, go to the settings for the app*/
    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
                logout();

                return true;
            case R.id.home:
                //startActivity(new Intent(this,MainActivity.class));
                return true;
            case R.id.settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;

        }
        return false;

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
                        getFavDetails();


                        return true;
                    case R.id.action_add:
                        startActivity(new Intent(MainActivity.this, SavedPicsActivity.class));
                        return true;
                    case R.id.action_filter:
                        Place_Filter();


                        return true;
                }
                return false;
            };


    /** SUMMARY
     * Is the filter for the map
     * Is a work in progress*/
    public void Place_Filter() {
        AlertDialog.Builder filter = new AlertDialog.Builder(MainActivity.this);
        View viewInflatedfilter = LayoutInflater.from(MainActivity.this).inflate(R.layout.filter_list, (ViewGroup) findViewById(android.R.id.content), false);
        filter.setView(viewInflatedfilter);
        final AlertDialog alertDialogfilter = filter.create();
        if (alertDialogfilter.getWindow() != null)
            alertDialogfilter.getWindow().getAttributes();
        alertDialogfilter.show();

    }

    /** SUMMARY
     * Fetches the list of favourite places
     * the lsit is displayed in an alertdialog
     * the custom layout is found in res/layout
     * the user can click on any of the places stored in their list and then the camera view will move to that location on the map*/
    public void getFavDetails() {
        AlertDialog.Builder fav_list = new AlertDialog.Builder(MainActivity.this);
        View viewInflated = LayoutInflater.from(MainActivity.this).inflate(R.layout.fav_list, (ViewGroup) findViewById(android.R.id.content), false);
        fav_list.setView(viewInflated);
        final AlertDialog alertDialog = fav_list.create();
        if (alertDialog.getWindow() != null)
            alertDialog.getWindow().getAttributes();

        //Listens for any changes made to the database, so that any new entries can be fetched
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference();
        myRef.child("users").child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {


                    for (DataSnapshot fav_place : dataSnapshot.getChildren()) {
                        String placeName = (String) fav_place.child(" Place_Name").getValue();
                        String date = (String) fav_place.child("Date").getValue();
                        String place_ID = (String) fav_place.child("Place_ID").getValue();
                        double latitude = (double) fav_place.child("Latitude").getValue();
                        double longitude = (double) fav_place.child("Longitude").getValue();

                        LatLng latLngFav = new LatLng(latitude, longitude);

                        LinearLayout ll = (LinearLayout) viewInflated.findViewById(R.id.ll_view);
                        Button button = new Button(MainActivity.this);
                        button.setText(placeName);

                        button.setOnClickListener(view -> {
                            alertDialog.cancel();
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLngFav, 15));


                        });
                        //Toast.makeText(MainActivity.this, ""+ latLngFav, Toast.LENGTH_SHORT).show();
                        ll.addView(button);


                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


        alertDialog.show();

    }


    /** SUMMARY
     * Saves the users selected unit of measurement to the DB  */
    public void SetMeasurement() {
        final DocumentReference documentReference = fstore.collection("MeasurementSystem").document(userID);
        documentReference.addSnapshotListener(this, (documentSnapshot, error) -> {

            Map<String, Object> MeasurementSystem = new HashMap<>();
            MeasurementSystem.put("System", System);
            documentReference.set(MeasurementSystem).addOnSuccessListener(aVoid -> Log.d(TAG, "onSuccess: system saved" + userID)).addOnFailureListener(e -> Log.d(TAG, "onFailure: system Not Saved" + e.getMessage()));
        });
    }


    /** SUMMARY
     * Fetches the users selected unit of measurement */
    public void getSystem() {

        final DocumentReference documentReference = fstore.collection("MeasurementSystem").document(userID);
        documentReference.addSnapshotListener(this, (documentSnapshot, error) -> {

           // assert documentSnapshot != null;
            Xsystem = documentSnapshot.getString("System");
            type_measure.setText(Xsystem);

            menu = navigationView.getMenu();

            if (Xsystem == null){
                Xsystem = "Imperial";
            }

            if (Xsystem.equals("Imperial")) {
                menu.findItem(R.id.nav_itemimp).setTitle("Imperial");
                switchcompat.setChecked(false);
            }
            if (Xsystem.equals("Metric")) {
                menu.findItem(R.id.nav_itemimp).setTitle("Metric");
                switchcompat.setChecked(true);
            }

            switcher();

        });

    }

    /** SUMMARY
     * This method controls the switcher for unit of measurement found in the menu drawer */
    public void switcher() {
        switchcompat.setOnCheckedChangeListener((buttonView, isChecked) -> {

            getCurrentLocation();
            mMap.clear();
            duration.setVisibility(View.GONE);
            distance.setVisibility(View.GONE);

            if (switchcompat.isChecked()) {
                menu.findItem(R.id.nav_itemimp).setTitle("Metric");
                System = "Metric";
            } else if (!switchcompat.isChecked()) {
                menu.findItem(R.id.nav_itemimp).setTitle("Imperial");
                System = "Imperial";
            }
            SetMeasurement();

        });
    }

    /** SUMMARY
     * Fetches the users profile picture and loads it into the circle image view found in the drawer menu
     * If their is no profile picture, a default img is used*/
    public void LoadProfilePic() {


        String userID;
        mAuth = FirebaseAuth.getInstance();
        userID = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        final DocumentReference documentReference2 = fstore.collection("ProfilePics").document(userID);
        documentReference2.addSnapshotListener(this, (documentSnapshot, error) -> {
            //assert documentSnapshot != null;
            String Bit = documentSnapshot.getString("Image");
            if (Bit != null) {
                byte[] bytes = Base64.decode(Bit, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                ProfilePic.setImageBitmap(bitmap);
                ProfilePic.setMinimumWidth(350);
                ProfilePic.setMinimumHeight(350);
            }

        });
    }


    /** SUMMARY
     *When the user presses the search button, it calls this method
     * A search bar will be displayed to the user
     * The user can enter a location they wish to find
     * The search bar will provide suggestions based on the user input*/

    public void onSearchCalled() {
        // Set the fields to specify which types of place data to return.
        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG, Place.Field.OPENING_HOURS, Place.Field.PHONE_NUMBER);
        // Start the autocomplete intent.
        Intent intent = new Autocomplete.IntentBuilder(
                AutocompleteActivityMode.OVERLAY, fields).build(this);
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
    }

    /** SUMMARY
     * Once the user has searched for a location
     * The camera will move to the location and drop a marker
     * the user can click on the marker to get any info about the location
     * If it is a point of interest they can also just click on the map marker for details*/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(Objects.requireNonNull(data));
                Log.i(TAG, "Place: " + place.getName() + ", " + place.getId() + ", " + place.getAddress());
                // Toast.makeText(MainActivity.this, "ID: " + place.getId() + "address:" + place.getAddress() + "Name:" + place.getLatLng() + " latlong: " + place.getLatLng(), Toast.LENGTH_LONG).show();

                supportMapFragment.getMapAsync(googleMap -> {
                    googleMap.clear();
                    duration.setVisibility(View.GONE);
                    distance.setVisibility(View.GONE);

                    MarkerOptions locationSearched = new MarkerOptions();
                    if (place.getOpeningHours() != null || place.getPhoneNumber() != null) {
                        locationSearched.position(Objects.requireNonNull(place.getLatLng())).title(place.getName()).snippet("Address: " + place.getAddress() + "\n" +
                                "\n" + "Phone Number: " + place.getPhoneNumber() + "\n" +
                                "\n" + "Opening Hours: " + place.getOpeningHours().getWeekdayText());
                    } else {
                        locationSearched.position(Objects.requireNonNull(place.getLatLng())).title(place.getName()).snippet("Address: " + place.getAddress() + "\n" +
                                "Coordinates: " + place.getLatLng());
                    }


                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 15));
                    googleMap.addMarker(locationSearched);

                    //custom view for the marker details
                    mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

                        @Override
                        public View getInfoWindow(@NonNull Marker arg0) {
                            return null;
                        }

                        @Override
                        public View getInfoContents(@NonNull Marker marker) {


                            LinearLayout info = new LinearLayout(MainActivity.this);
                            info.setOrientation(LinearLayout.VERTICAL);

                            TextView title = new TextView(MainActivity.this);
                            title.setTextColor(Color.BLACK);
                            title.setGravity(Gravity.CENTER);
                            title.setTypeface(null, Typeface.BOLD);
                            title.setText(marker.getTitle());

                            TextView snippet = new TextView(MainActivity.this);
                            snippet.setTextColor(Color.GRAY);
                            snippet.setText(marker.getSnippet());

                            LinearLayout infoBtn = new LinearLayout(MainActivity.this);
                            infoBtn.setOrientation(LinearLayout.VERTICAL);
                            Button AddToFav = new Button(MainActivity.this);
                            AddToFav.isClickable();
                            AddToFav.setText("Add To Favorites");
                            AddToFav.bringToFront();
                            AddToFav.setOnClickListener(v -> Toast.makeText(MainActivity.this, "Added", Toast.LENGTH_LONG).show());

                            info.addView(title);
                            info.addView(snippet);
                            //info.addView(infoBtn);
                            infoBtn.addView(AddToFav);

                            return info;
                        }
                    });

                });


            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                // TODO: Handle the error.
                Status status = Autocomplete.getStatusFromIntent(Objects.requireNonNull(data));
                Toast.makeText(MainActivity.this, "Error: " + status.getStatusMessage(), Toast.LENGTH_LONG).show();
                Log.i(TAG, status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }




    /** SUMMARY
     * Gets the users current location and displays it to them on the map
     * The user can also click on the map to drop a marker and get the location
     * The user can hold down on the map to drop a marker and get a route to it from their current location
     * The user can click on any point of interest to get a popup with the location details such as:
     * address
     * opening times
     * places phone number
     * places name
     * From there the user can either close the popup or preform a function such as adding it to their favourites list or getting the route to the location*/
    @SuppressLint("MissingPermission")
    private void getCurrentLocation() {
        String apiKey = getString(R.string.api_key);

        //initializes places api
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), apiKey);
        }

        // Create a new Places client instance.
        PlacesClient placesClient = Places.createClient(this);

        @SuppressLint("MissingPermission") Task<Location> task = client.getLastLocation();
        task.addOnSuccessListener(location -> {
            if (location != null) {
                supportMapFragment.getMapAsync(googleMap -> {
                    mMap = googleMap;
                    duration.setVisibility(View.GONE);
                    distance.setVisibility(View.GONE);

                    //start position
                    mOrigin = new LatLng(location.getLatitude(), location.getLongitude());

                    //current location
                    latLng = new LatLng(location.getLatitude(), location.getLongitude());

                    MarkerOptions options = new MarkerOptions().position(latLng).title("I am here");
                    options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                    mMap.setMyLocationEnabled(true);
                    mMap.getUiSettings().setMyLocationButtonEnabled(false);
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                    googleMap.clear();
                    // googleMap.addMarker(options);
                    googleMap.setOnPoiClickListener(pointOfInterest -> {

                        String x = "Latitude:" + pointOfInterest.latLng.latitude + " Longitude:" + pointOfInterest.latLng.longitude;
                        mDestination = new LatLng(pointOfInterest.latLng.latitude, pointOfInterest.latLng.longitude);

                        AlertDialog.Builder Menu = new AlertDialog.Builder(MainActivity.this);
                        View viewInflated = LayoutInflater.from(MainActivity.this).inflate(R.layout.marker_details, (ViewGroup) findViewById(android.R.id.content), false);
                        Menu.setView(viewInflated);
                        final AlertDialog alertDialog = Menu.create();
                        if (alertDialog.getWindow() != null)
                            alertDialog.getWindow().getAttributes();

                        title = viewInflated.findViewById(R.id.name);
                        address = viewInflated.findViewById(R.id.address);
                        number = viewInflated.findViewById(R.id.number);
                        other = viewInflated.findViewById(R.id.other);
                        btn_fav = viewInflated.findViewById(R.id.btn_add_fav);
                        btn_route = viewInflated.findViewById(R.id.btn_route);

                        List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.PHONE_NUMBER, Place.Field.ADDRESS, Place.Field.OPENING_HOURS);

                        // Construct a request object, passing the place ID and fields array.
                        FetchPlaceRequest request = FetchPlaceRequest.newInstance(pointOfInterest.placeId, placeFields);

                        placesClient.fetchPlace(request).addOnSuccessListener((response) -> {
                            Place place = response.getPlace();
                            if (place.getOpeningHours() != null || place.getAddress() != null || place.getPhoneNumber() != null) {
                                title.setText(place.getName());
                                address.setText(place.getAddress());
                                number.setText(place.getPhoneNumber());
                                if (place.getOpeningHours() == null) {

                                } else {
                                    other.setText("" + place.getOpeningHours().getWeekdayText());
                                }

                            } else {
                                title.setText(place.getName());
                                address.setText(x);
                                other.setText("");
                            }

                            Log.i(TAG, "Place found: " + place.getName());
                        }).addOnFailureListener((exception) -> {
                            if (exception instanceof ApiException) {
                                ApiException apiException = (ApiException) exception;
                                int statusCode = apiException.getStatusCode();
                                // Handle error with given status code.
                                Log.e(TAG, "Place not found: " + exception.getMessage());
                            }
                        });

                        alertDialog.show();
                        btn_route.setOnClickListener(v -> {
                            Toast.makeText(MainActivity.this, "Added", Toast.LENGTH_LONG).show();

                            if (mOrigin != null && mDestination != null)
                                mMap.clear();
                            mMarkerOptions = new MarkerOptions().position(mDestination).title("Destination");
                            mMap.addMarker(mMarkerOptions);
                            drawRoute();
                            alertDialog.cancel();
                        });

                        btn_fav.setOnClickListener(v -> {

                            Date c = Calendar.getInstance().getTime();
                            SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault());
                            String formattedDate = df.format(c);
                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
                            Map<String, Object> user = new HashMap<>();
                            user.put("Place_ID", pointOfInterest.placeId);
                            user.put(" Place_Name", pointOfInterest.name);
                            user.put("Latitude", pointOfInterest.latLng.latitude);
                            user.put("Longitude", pointOfInterest.latLng.longitude);
                            user.put("Date", formattedDate);
                            ref.child("users").child(userID).push().setValue(user);
                            Toast.makeText(MainActivity.this, "Added", Toast.LENGTH_LONG).show();
                            alertDialog.cancel();
                        });


                    });
                    googleMap.setOnMapClickListener(latLng -> {
                        duration.setVisibility(View.GONE);
                        distance.setVisibility(View.GONE);
                        //when clicked on map, initialize marker
                        MarkerOptions markerOptions = new MarkerOptions();
                        //set position
                        markerOptions.position(latLng);
                        //set title
                        markerOptions.title(latLng.latitude + ":" + latLng.longitude);
                        //remove markers
                        googleMap.clear();
                        //animating marker zoom
                        googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                                latLng, 15
                        ));
                        //add marker on the map
                        googleMap.addMarker(markerOptions);

                    });
                    mMap.setOnMapLongClickListener(latLng1 -> {
                        mDestination = latLng1;
                        mMap.clear();
                        // mMarkerOptions1 = new MarkerOptions().position(mOrigin).title("Start");
                        mMarkerOptions = new MarkerOptions().position(mDestination).title("Destination");
                        //mMap.addMarker(mMarkerOptions1);
                        mMap.addMarker(mMarkerOptions);
                        if (mOrigin != null && mDestination != null)
                            drawRoute();
                    });


                });

            }
        });
    }


/** SUMMARY
 * method called when a user set a marker by long hold down
 * morigin is start location
 * mdestination is the end location*/

    private void drawRoute() {

        // Getting URL from the Google Directions API
        String url = getDirectionsUrl(mOrigin, mDestination);

        DownloadTask downloadTask = new DownloadTask();

        // Start downloading json data from Google Directions API
        downloadTask.execute(url);
    }

    /** SUMMARY
     * The url constructor
     * url is sent to the web api*/
    private String getDirectionsUrl(LatLng origin, LatLng dest) {

        // start location
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // end location
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;


        // parameters passed to the web service
        String parameters = str_origin + "&" + str_dest;

        // Building the url to the web service

        return "https://maps.googleapis.com/maps/api/directions/json?" + parameters + "&key=AIzaSyChrBR11v6iA0rP623GQkfGlLZxSkrH72c";
    }

    /** SUMMARY
     * method used to download the json data from the url*/
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            StringBuilder sb = new StringBuilder();

            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();
            br.close();

        } catch (Exception e) {
            Log.d("Exception on download", e.toString());
        } finally {
          //  assert iStream != null;
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }


    /** SUMMARY
     * downloads data from the google directions api*/
    private class DownloadTask extends AsyncTask<String, Void, String> {


        @Override
        protected String doInBackground(String... url) {

            // stores data from the web service
            String data = "";

            try {
               //fetches the data
                data = downloadUrl(url[0]);
                Log.d("DownloadTask", "DownloadTask : " + data);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();
            parserTask.execute(result);
        }
    }

    /** SUMMARY
     * passes the google directions in json
     * provides the route duration
     * provides the route distance
     * maps the route on the map
     * the distance and duration of the route is displayed on the map*/
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {


        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                // Starts parsing data
                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        // Executes after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points;
            PolylineOptions lineOptions = null;

            // goes through all the available routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);


                    double lat = Double.parseDouble(Objects.requireNonNull(point.get("lat")));
                    double lng = Double.parseDouble(Objects.requireNonNull(point.get("lng")));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(8);
                lineOptions.color(Color.RED);
            }

            // Drawing polyline on the Google Map for the i-th route
            //this is the visible red line that the user will be able to use to see their route
            if (lineOptions != null) {
                if (mPolyline != null) {
                    mPolyline.remove();
                }
                mPolyline = mMap.addPolyline(lineOptions);

                //the distance and duration of the route
                //distance in in meters
                Distance = DirectionsJSONParser.parsedDistance;
                Duration = DirectionsJSONParser.parsedDuration;

                //the unit of measurement selected by the user
                Ysystem = type_measure.getText().toString();

                //converts the distance to imperial and rounds it
                if (Ysystem.equals("Imperial")) {
                    DecimalFormat df = new DecimalFormat("0.00");
                    double x = Double.parseDouble(Distance);
                    double y = 0.000621371;
                    double z = x * y;
                    String d, t;
                    d = "Distance: " + df.format(z) + " mi";
                    t = "ETA: " + Duration;
                    duration.setText(t);
                    distance.setText(d);
                    duration.setVisibility(View.VISIBLE);
                    distance.setVisibility(View.VISIBLE);

                }

                //converts the distance to metric and rounds it
                if (Ysystem.equals("Metric")) {
                    DecimalFormat df = new DecimalFormat("0.00");
                    double x = Double.parseDouble(Distance);
                    double y = 0.001;
                    double z = x * y;

                    String d, t;
                    d = "Distance: " + df.format(z) + " km";
                    t = "ETA: " + Duration;
                    duration.setText(t);
                    distance.setText(d);
                    duration.setVisibility(View.VISIBLE);
                    distance.setVisibility(View.VISIBLE);
                }


            } else
                Toast.makeText(getApplicationContext(), "No route is found", Toast.LENGTH_LONG).show();
        }
    }


///////////////////////////////


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 44) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            }

        }
    }


}






