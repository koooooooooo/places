package com.ko.nearbuildings;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;
import com.ko.nearbuildings.adapters.PlacesAdapter;
import com.ko.nearbuildings.net.ApiService;
import com.ko.nearbuildings.net.PlaceResponse;
import com.ko.nearbuildings.utils.CommonUtils;
import com.ko.nearbuildings.utils.PermissionUtil;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.converter.GsonConverter;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final int REQUEST_CODE = 12;
    private PermissionUtil.PermissionRequestObject permissionRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    public static final int DISTANCE = 1000;
    private RestAdapter adapter;
    private ListView placesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(MainActivity.this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        adapter = new RestAdapter.Builder()
                .setEndpoint("https://maps.googleapis.com/maps/api/place/nearbysearch")
                .setConverter(new GsonConverter(new Gson())).build();

        placesList = (ListView) findViewById(R.id.placesList);

        findViewById(R.id.placesBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getPlaces();
            }
        });
    }

    private void getPlaces() {
        if (CommonUtils.isHasInternetConnection(this) && mLastLocation != null) {
            adapter.create(ApiService.class).getPlaces(mLastLocation.getLatitude() + "," + mLastLocation.getLongitude(),
                    DISTANCE, getString(R.string.google_places_key),
                    new Callback<PlaceResponse>() {
                        @Override
                        public void success(PlaceResponse placeResponse, Response response) {
                            placesList.setAdapter(new PlacesAdapter(MainActivity.this, placeResponse.getResults()));
                        }

                        @Override
                        public void failure(RetrofitError error) {
                            Toast.makeText(MainActivity.this, getString(R.string.network_error), Toast.LENGTH_LONG).show();
                        }
                    });
        } else {
            Toast.makeText(MainActivity.this, getString(R.string.no_internet_connection), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (permissionRequest != null) {
            permissionRequest.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionRequest = PermissionUtil.with(this)
                    .request(Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION)
                    .onAnyDenied(new PermissionUtil.OnPermission() {
                        @Override
                        public void call() {
                            Intent intent = new Intent()
                                    .setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                    .setData(Uri.fromParts("package", getApplicationContext().getPackageName(), null));
                            startActivity(intent);
                        }
                    }).ask(REQUEST_CODE);
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            getPlaces();
        } else {
            Toast.makeText(MainActivity.this, getString(R.string.error_while_location), Toast.LENGTH_LONG).show();
        }
    }

}
