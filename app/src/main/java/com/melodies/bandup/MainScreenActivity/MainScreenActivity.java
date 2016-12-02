package com.melodies.bandup.MainScreenActivity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.google.android.gms.common.api.GoogleApiClient;
import com.melodies.bandup.DatabaseSingleton;
import com.melodies.bandup.DatePickable;
import com.melodies.bandup.DatePickerFragment;
import com.melodies.bandup.Login;
import com.melodies.bandup.R;
import com.melodies.bandup.SoundCloudFragments.SoundCloudLoginFragment;
import com.melodies.bandup.SoundCloudFragments.SoundCloudPlayerFragment;
import com.melodies.bandup.SoundCloudFragments.SoundCloudSelectorFragment;
import com.melodies.bandup.VolleySingleton;
import com.melodies.bandup.gcm_tools.RegistrationIntentService;
import com.melodies.bandup.helper_classes.User;
import com.melodies.bandup.listeners.BandUpErrorListener;
import com.melodies.bandup.listeners.BandUpResponseListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;

public class MainScreenActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        UserListFragment.OnFragmentInteractionListener,
        MatchesFragment.OnListFragmentInteractionListener,
        SettingsFragment.OnFragmentInteractionListener,
        ProfileFragment.OnFragmentInteractionListener,
        UserDetailsFragment.OnFragmentInteractionListener,
        SoundCloudSelectorFragment.OnFragmentInteractionListener,
        SoundCloudLoginFragment.OnFragmentInteractionListener,
        SoundCloudPlayerFragment.OnFragmentInteractionListener,
        UserSearchFragment.OnFragmentInteractionListener,
        UpcomingFeaturesFragment.OnFragmentInteractionListener,
        LocationListener, DatePickable {

    int EDIT_INSTRUMENTS_REQUEST_CODE = 4939;
    int EDIT_GENRES_REQUEST_CODE = 4989;


    UserListFragment userListFragment;
    UserListFragment mUserSearchResultsFragment;
    UserDetailsFragment userDetailsFragment;
    MatchesFragment matchesFragment;
    SettingsFragment settingsFragment;
    ProfileFragment profileFragment;
    UserSearchFragment mUserSearchFragment;
    UpcomingFeaturesFragment mUpcomingFeaturesFragment;

    ProgressDialog logoutDialog;
    LocationManager locationManager;
    Criteria criteria;
    String bestProvider;
    SharedPreferences sharedPrefs;
    GoogleApiClient mGoogleApiClient;

    private boolean mIsSearch = false;

    public void setIsSearch(boolean isSearch){
        mIsSearch = isSearch;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        profileFragment.onImageSelectResult(requestCode, resultCode, data);
        profileFragment.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);
        sharedPrefs = getSharedPreferences("permissions", Context.MODE_PRIVATE);
        if (!sharedPrefs.contains("display_rationale")) {
            SharedPreferences.Editor editor = sharedPrefs.edit();
            editor.putBoolean("display_rationale", true);
            editor.apply();
        }
        // Create all fragments
        userListFragment            = UserListFragment.newInstance(null);
        userDetailsFragment         = UserDetailsFragment.newInstance();
        matchesFragment             = new MatchesFragment();
        settingsFragment            = SettingsFragment.newInstance();
        profileFragment             = ProfileFragment.newInstance();
        mUserSearchFragment         = UserSearchFragment.newInstance();
        mUpcomingFeaturesFragment = UpcomingFeaturesFragment.newInstance();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        // Set the first item in the drawer to selected.
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setItemIconTintList(null);

        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().getItem(0).setChecked(true);

        // Open the UserListFragment
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.mainFrame, userListFragment);
        ft.commit();

        // We know the user is logged in time to start services
        startService(new Intent(getApplicationContext(), RegistrationIntentService.class));
        //startService(new Intent(getApplicationContext(), BandUpGCMListenerService.class));
        Boolean shouldDisplayRationale = sharedPrefs.getBoolean("display_rationale", false);

        int apiVersion = android.os.Build.VERSION.SDK_INT;
        if (apiVersion >= android.os.Build.VERSION_CODES.M){

            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // If location permissions have NOT been granted.
                // Tell the user what we are going to do with the location.
                if (shouldDisplayRationale) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainScreenActivity.this);
                    builder.setTitle("Location Access")
                            .setMessage("To find musicians near you we need access to your location.\nWe will only use it to measure distance between you and other musicians.")
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    ActivityCompat.requestPermissions(MainScreenActivity.this, new String[]{
                                            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION
                                    }, LOCATION_REQUEST_CODE);
                                }
                            })
                            .show();
                }
            } else {
                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.putBoolean("display_rationale", true);
                editor.apply();
                createLocationRequest();
            }

        } else {
            createLocationRequest();
        }
    }
    boolean isExiting = false;

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        int count = getSupportFragmentManager().getBackStackEntryCount();

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (count != 0){
            getSupportFragmentManager().popBackStack();
        } else if (isTaskRoot()) {
            if (isExiting) {
                super.onBackPressed();
                return;
            }

            this.isExiting = true;
            Toast.makeText(this, R.string.exit_bandup_toast, Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    isExiting=false;
                }
            }, 5000);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        switch (id){
            case R.id.nav_near_me:
                mIsSearch = false;
                ft.replace(R.id.mainFrame, userListFragment);
                ft.commit();
                setTitle(getString(R.string.main_title_user_list));
                break;
            case R.id.nav_matches:
                ft.replace(R.id.mainFrame, matchesFragment);
                ft.commit();
                setTitle(getString(R.string.main_title_matches));
                break;
            case R.id.nav_edit_profile:
                ft.replace(R.id.mainFrame, profileFragment);
                ft.commit();
                setTitle(getString(R.string.main_title_edit_profile));
                break;
            case R.id.nav_settings:
                ft.replace(R.id.mainFrame, settingsFragment);
                ft.commit();
                setTitle(getString(R.string.main_title_settings));
                break;
            case R.id.nav_search:
                ft.replace(R.id.mainFrame, mUserSearchFragment);
                ft.commit();
                setTitle(getString(R.string.search));
                break;
            case R.id.nav_logout:
                logout();
                logoutDialog = new ProgressDialog(MainScreenActivity.this);
                logoutDialog.setMessage("Logging out");
                logoutDialog.setTitle("Please wait...");
                logoutDialog.show();
                break;
            case R.id.nav_upcomming:
                ft.replace(R.id.mainFrame, mUpcomingFeaturesFragment);
                ft.commit();
                setTitle("Upcoming features");
                break;
            default:
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void logout() {
        DatabaseSingleton.getInstance(MainScreenActivity.this).getBandUpDatabase().logout(
                new BandUpResponseListener() {
            @Override
            public void onBandUpResponse(Object response) {
                logoutDialog.dismiss();
                Intent intent = new Intent(MainScreenActivity.this, Login.class);
                startActivity(intent);
                finish();
            }
        }, new BandUpErrorListener() {
            @Override
            public void onBandUpErrorResponse(VolleyError error) {
                logoutDialog.dismiss();
                VolleySingleton.getInstance(MainScreenActivity.this).checkCauseOfError(error);
            }
        });
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    /**
     * Used for the MatchesFragment. When the user taps on another user.
     * @param user The the user that the current user wants to chat with.
     */
    @Override
    public void onListFragmentInteraction(User user) {
        matchesFragment.onClickChat(user);
    }

    public void onClickDisplayModal(View view) {
        profileFragment.onClickDisplayModal(view);
    }

    public void onClickAboutMe(View view) {
        profileFragment.onClickAboutMe(view);
    }

    public void onClickAge(View view) { profileFragment.onClickAge(view); }

    public void onCLickFavorite(View view) { profileFragment.onClickFavorite(view); }

    public void onClickContact(View view) { settingsFragment.onClickContact(view); }

    public void onClickPrivacyPolicy(View view) { settingsFragment.onClickPrivacyPolicy(view); }

    public UserListFragment startSearchResults(User[] users){
        mIsSearch = true;
        mUserSearchResultsFragment = UserListFragment.newInstance(users);

        setTitle("Search results");
        return mUserSearchResultsFragment;
    }

    public void onClickDetails(View view, int position) {
        System.out.println(position);
        switch (view.getId()) {
            case R.id.btnDetails:
                Bundle bundle = new Bundle();
                if (mIsSearch){
                    if (mUserSearchResultsFragment.mAdapter.getUser(position) == null) {
                        return;
                    }
                    bundle.putString("user_id", mUserSearchResultsFragment.mAdapter.getUser(position).id);
                }else {
                    if (userListFragment.mAdapter.getUser(position) == null) {
                        return;
                    }
                    bundle.putString("user_id", userListFragment.mAdapter.getUser(position).id);
                }

                if (userDetailsFragment.getArguments() != null) {
                    userDetailsFragment.getArguments().clear();
                    userDetailsFragment.getArguments().putAll(bundle);
                } else {
                    userDetailsFragment.setArguments(bundle);
                }
                FragmentManager fm = getSupportFragmentManager();
                FragmentTransaction ft = fm.beginTransaction().replace(R.id.mainFrame, userDetailsFragment).addToBackStack(null);
                ft.commit();
                break;
        }
    }

    public void onClickSearch(View view) {
        mUserSearchFragment.onClickSearch(view);
    }

    public void onShowGenres(View v) {
        mUserSearchFragment.onShowGenres(v);
    }

    public void onShowInstruments(View v) {
        mUserSearchFragment.onShowInstruments(v);
    }

    // ======= Location setup ========
    private final int LOCATION_REQUEST_CODE = 333;


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        if (requestCode == LOCATION_REQUEST_CODE){
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted, yay! Do the contacts-related task you need to do.

                // We will display the rationale next time we are denied access to the location.
                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.putBoolean("display_rationale", true);
                editor.apply();
                createLocationRequest();
            } else {
                // Permission denied, boo!
                // Since the user has denied, we will not display it again.
                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.putBoolean("display_rationale", false);
                editor.apply();
                //createLocationRequest();
            }
            return;
        }
        profileFragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    protected void createLocationRequest() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // request permissions
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION
            }, LOCATION_REQUEST_CODE);
            return;
        }
        LocationManager locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setPowerRequirement(Criteria.POWER_HIGH);
        bestProvider = String.valueOf(locationManager.getBestProvider(criteria, true));
        try {
            Location location = locationManager.getLastKnownLocation(locationManager.GPS_PROVIDER);
            if (location == null) {
                locationManager.requestLocationUpdates(bestProvider, 1000, 0, this);
            }
            sendLocation(location);
        }catch (IllegalArgumentException ex){
            ex.printStackTrace();
        }catch (SecurityException ex){
            ex.printStackTrace();
        }
    }

    private void sendLocation(Location location){
        if (location == null){
            System.err.println("Location is null when sending location.");
            System.err.println(Arrays.toString(Thread.currentThread().getStackTrace()));
            return;
        }

        JSONObject locObject = new JSONObject();
        JSONObject jsonObject = new JSONObject();
        try {
            locObject.put("lon", location.getLongitude());
            locObject.put("lat", location.getLatitude());

            jsonObject.put("location", locObject);
            System.out.println(locObject);
            DatabaseSingleton.getInstance(this).getBandUpDatabase().postLocation(jsonObject,
                    new BandUpResponseListener() {
                        @Override
                        public void onBandUpResponse(Object response) {
                            // we were successful nothing to report
                        }
                    }, new BandUpErrorListener() {
                        @Override
                        public void onBandUpErrorResponse(VolleyError error) {
                            error.printStackTrace();
                            Toast.makeText(getApplicationContext(), "Something went wrong sending location", Toast.LENGTH_LONG).show();
                        }
                    });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    public void onClickEditInstruments(View view) {
        profileFragment.onClickEditInstruments();
    }

    public void onClickEditGenres(View view) {
        profileFragment.onClickEditGenres();
    }


    public void onClickLike(String userID) {
        JSONObject user = new JSONObject();

        try {
            user.put("userID", userID);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        DatabaseSingleton.getInstance(MainScreenActivity.this.getApplicationContext()).getBandUpDatabase().postLike(user, new BandUpResponseListener() {
            @Override
            public void onBandUpResponse(Object response) {
                JSONObject responseObj = null;

                if (response instanceof JSONObject) {
                    responseObj = (JSONObject) response;
                } else {
                    return;
                }

                try {
                    Boolean isMatch;
                    if (!responseObj.isNull("isMatch")) {
                        isMatch = responseObj.getBoolean("isMatch");
                    } else {
                        Toast.makeText(MainScreenActivity.this, "Error loading match.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (isMatch) {
                        Toast.makeText(MainScreenActivity.this, "You Matched!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainScreenActivity.this, "You liked this person", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new BandUpErrorListener() {
            @Override
            public void onBandUpErrorResponse(VolleyError error) {
                VolleySingleton.getInstance(MainScreenActivity.this).checkCauseOfError(error);

            }
        });
    }

    @Override
    public void onDateSet(int year, int month, int day) {
        DatePickerFragment datePickerFragment = new DatePickerFragment();

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, day);

        // Calendar to Date object.
        Date dateOfBirth = cal.getTime();

        // Get the locale date format.
        java.text.DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(this);

        // Formatted date.
        String date = dateFormat.format(dateOfBirth);

        String age = datePickerFragment.ageCalculator(year, month, day);

        String dateString = String.format("%s (%s)", date, age);

        // send date and age to updateAge in profileFragment and do whatever you want with it
        profileFragment.updateAge(dateOfBirth, age);
    }
}