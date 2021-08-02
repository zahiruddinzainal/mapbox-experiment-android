  package com.example.mapbox;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncher;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncherOptions;
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;


public class MapBox extends AppCompatActivity implements OnMapReadyCallback, PermissionsListener {

    private MapView mapView;
    private MapboxMap mapboxMap;
    private PermissionsManager permissionsManager;
    private LocationComponent locationComponent;
    private DirectionsRoute currentRoute;
    private static final String TAG = "DirectionsActivity";
    private NavigationMapRoute navigationMapRoute;
    private Button startNavigationButton;
    private Button findDistance;
    private Button findTime;
    EditText xChooseOrigin;
    EditText xChooseDestination;
    TextView xTextAlert;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getString(R.string.access_token));
        setContentView(R.layout.activity_mapbox);

        xTextAlert = findViewById(R.id.textalert);
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

    }

    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        mapboxMap.setStyle(getString(R.string.navigation_guidance_day), new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                enableLocationComponent(style);
                addDestinationIconSymbolLayer(style);

                String prevLatitude = getIntent().getStringExtra("prevLatitude");

                setRoute();

                findDistance = findViewById(R.id.findDistance);
                findDistance.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (xChooseOrigin.getText().toString().trim().length() == 0 || xChooseDestination.getText().toString().trim().length() == 0){
                            Toast.makeText(getApplicationContext(),"Choose origin and destination point first",Toast.LENGTH_SHORT).show();
                        }
                        else {
                            mapView.getMapAsync(MapBox.this);
                            xTextAlert.setVisibility(View.GONE);
                            displayDistance();
                        }

                    }
                });

                findTime = findViewById(R.id.findTime);
                findTime.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        displayTime();
                    }
                });

                startNavigationButton = findViewById(R.id.startNavigation);
                startNavigationButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                       displayNavigation();
                    }
                });
            }
        });
    }

    private void setRoute() {

        //Hardcoded Latitude and Longitude origin point
        double originLongitude = 101.586720;
        double originLatitude = 3.332390;

        //Hardcoded Latitude and Longitude destination point
        double destinationLongitude = 101.586720;
        double destinationLatitude = 3.032390;

//        // GPS current location's Latitude and Longitude (origin)
//        double originLongitude = locationComponent.getLastKnownLocation().getLongitude();
//        double originLatitude = locationComponent.getLastKnownLocation().getLatitude();
//
//        // GPS current location's Latitude and Longitude (destination)
//        double destinationLongitude = locationComponent.getLastKnownLocation().getLongitude();
//        double destinationLatitude = locationComponent.getLastKnownLocation().getLatitude();

        String x = String.valueOf(originLongitude);

        //setup path by passing lat & longitude
        final Point destination = Point.fromLngLat(originLongitude , originLatitude);
        final Point originPoint = Point.fromLngLat(destinationLongitude, destinationLatitude);

        GeoJsonSource source = mapboxMap.getStyle().getSourceAs("destination-source-id");
        if (source != null) {
            source.setGeoJson(Feature.fromGeometry(destination));
        }

        // setup route to map view before display in line 152-159
        mapView.setVisibility(View.VISIBLE);
        NavigationRoute.builder(MapBox.this)
            .accessToken(Mapbox.getAccessToken())
            .origin(originPoint)
            .destination(destination)
            .build()
            .getRoute(new Callback<DirectionsResponse>() {
                @Override
                public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {

                    // You can get the generic HTTP info about the response
                    Timber.d("Response code: %s", response.code());
                    if (response.body() == null) {
                        Timber.e("No routes found, make sure you set the right user and access token.");
                        return;
                    } else if (response.body().routes().size() < 1) {
                        Timber.e("No routes found");
                        return;
                    }

                    // line 164 (currentRoute) is important. It is the main object that store:
                    //  1.Origin latitude
                    //  2.Origin longitude
                    //  3.Destination latitude
                    //  4.Destination longitude
                    //  5.Route path details (it will be used in future to calculate distance and time (based on realtime situation) eg: roadblock, traffic jam, closed road etc

                    currentRoute = response.body().routes().get(0);

                    // Draw the route on the map
                    // can delete if don't want to show route line (line 168 to 173)
                    if (navigationMapRoute != null) {
                        navigationMapRoute.removeRoute();
                    } else {
                        navigationMapRoute = new NavigationMapRoute(null, mapView, mapboxMap, R.style.NavigationMapRoute);
                    }
                    navigationMapRoute.addRoute(currentRoute);
                }

                @Override
                public void onFailure(Call<DirectionsResponse> call, Throwable t) {

                }
            });
    }

    private void displayDistance() {
        double distance = (currentRoute.distance())/1000; //in meters, convert meter to km
        String xDistance =  String.format("%.2f", distance);

        final Dialog dialog = new Dialog(MapBox.this); // Context, this, etc.
        dialog.setContentView(R.layout.dialog_distance);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent); // set adapter background jadi transparent

        TextView text_distance = (TextView) dialog.findViewById(R.id.mDistance);
        text_distance.setText("Distance: " + xDistance + " Kilometres");

        Button backButton = (Button) dialog.findViewById(R.id.mBackButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();

    }

    private void displayTime() {
        double time = (currentRoute.duration())/60; // in seconds, convert meter to km
        String xTime =  String.format("%.2f", time);

        final Dialog dialog = new Dialog(MapBox.this); // Context, this, etc.
        dialog.setContentView(R.layout.dialog_time);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent); // set adapter background jadi transparent

        TextView text_time = (TextView) dialog.findViewById(R.id.mTime);
        text_time.setText("Time: " + xTime + " minutes");

        Button backButton = (Button) dialog.findViewById(R.id.mBackButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();

    }

    private void displayNavigation() {
        boolean simulateRoute = true;
        NavigationLauncherOptions options = NavigationLauncherOptions.builder()
                .directionsRoute(currentRoute)
                .shouldSimulateRoute(simulateRoute)
                .build();
        NavigationLauncher.startNavigation(MapBox.this, options);
    }

    private void addDestinationIconSymbolLayer(@NonNull Style loadedMapStyle) {
        loadedMapStyle.addImage("destination-icon-id",
                BitmapFactory.decodeResource(this.getResources(), R.drawable.mapbox_marker_icon_default));
        GeoJsonSource geoJsonSource = new GeoJsonSource("destination-source-id");
        loadedMapStyle.addSource(geoJsonSource);
        SymbolLayer destinationSymbolLayer = new SymbolLayer("destination-symbol-layer-id", "destination-source-id");
        destinationSymbolLayer.withProperties(
                iconImage("destination-icon-id"),
                iconAllowOverlap(true),
                iconIgnorePlacement(true)
        );
        loadedMapStyle.addLayer(destinationSymbolLayer);
    }


    @SuppressWarnings( {"MissingPermission"})
    private void enableLocationComponent(@NonNull Style loadedMapStyle) {

        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            locationComponent = mapboxMap.getLocationComponent();
            locationComponent.activateLocationComponent(this, loadedMapStyle);
            locationComponent.setLocationComponentEnabled(true);
            locationComponent.setCameraMode(CameraMode.TRACKING);
        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(this, R.string.user_location_permission_explanation, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            enableLocationComponent(mapboxMap.getStyle());
        } else {
            Toast.makeText(this, R.string.user_location_permission_not_granted, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    // Call Back method  to get the Message form other Activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        // check if the request code is same as what is passed  here it is 2
        if(requestCode==2)
        {
            String org = data.getStringExtra("ORIGIN_ADDRESS");
            xChooseOrigin.setText(org);
        }

        // check if the request code is same as what is passed  here it is 3
        if(requestCode==3)
        {
            String des = data.getStringExtra("DESTINATION_ADDRESS");
            xChooseDestination.setText(des);
        }
    }


}