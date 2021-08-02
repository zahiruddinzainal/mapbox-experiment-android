package com.example.mapbox;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;


public class MapBoxPlanner extends AppCompatActivity {

    private DirectionsRoute currentRoute;
    private static final String TAG = "DirectionsActivity";

    TextView xText;
    TextView xTemporaryTextDistance;
    TextView xTemporaryTextTime;
    Button pass;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getString(R.string.access_token));
        setContentView(R.layout.activity_cumulative);

        double[][] LATEST_COORDINATE ={{102.2526,102.2501,102.249781,102.2627,102.2464},{2.1898,2.1884,2.202918,2.1826,2.2149}};
        
        double[] originLongitude = new double[5];
        double[] originLatitude = new double[5];
        double[] destinationLongitude = new double[5];
        double[] destinationLatitude = new double[5];

        for(int k=0; k < 5; k++){

            originLongitude[k] = LATEST_COORDINATE[0][k];
            originLatitude[k] = LATEST_COORDINATE[1][k];

            destinationLongitude[k]= LATEST_COORDINATE[0][k+1];
            destinationLatitude[k]= LATEST_COORDINATE[1][k+1];
        }
        //reminder: array start from zero 0
        for (int i = 0; i < 5; i++) {


            final Point originPoint = Point.fromLngLat(originLongitude[i], originLatitude[i]);
            final Point destination = Point.fromLngLat(destinationLongitude[i], destinationLatitude[i]);

            GeoJsonSource source = mapboxMap.getStyle().getSourceAs("destination-source-id");
            if (source != null) {
                source.setGeoJson(Feature.fromGeometry(destination));
            }

            NavigationRoute.builder(MapBoxPlanner.this)
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

                            currentRoute = response.body().routes().get(0);

                            double distance = (currentRoute.distance()) / 1000; //in meters, convert meter to km
                            String y = String.format("%.2f", distance);

                            double test1 = Double.parseDouble(y);
                            totalDistance += test1;

                            double time = (currentRoute.duration()) / 60; // in seconds, convert meter to km
                            String z = String.format("%.2f", time);
                            double test2 = Double.parseDouble(z);
                            totalTime += test2;

                            if(a == 4 ){
                                String aa = String.valueOf(totalDistance);
                                xText.setText(aa);

                            }

                            a++;

                            // xText.append("Distance: " + y + " Kilometers" + "\nTime: " + z + " minutes\n\n");

                        }

                        @Override
                        public void onFailure(Call<DirectionsResponse> call, Throwable t) {

                        }
                    });

        }


    }
});


        String jarak = xTemporaryTextDistance.getText().toString();
        String masa = xTemporaryTextTime.getText().toString();

        pass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] array={"asd","fgh","dcf","dg","ere","dsf"};
                Intent i=new Intent(MapBoxPlanner.this, GetArray.class);
                i.putExtra("PASS_DATA",array);
                startActivity(i);
            }
        });




    }
}