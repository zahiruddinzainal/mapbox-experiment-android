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


public class Cumulative extends AppCompatActivity {

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

        xText = findViewById(R.id.text);

        xTemporaryTextDistance = findViewById(R.id.x);
        xTemporaryTextTime = findViewById(R.id.y);

        pass = findViewById(R.id.pass);
        //Hardcoded Latitude and Longitude origin point
        double[] originLongitude = {101.586720, 101.586320, 101.686320, 101.486320};
        double[] originLatitude = {3.332390, 3.322390, 3.345390, 3.422390 };

        //Hardcoded Latitude and Longitude destination point
        double[] destinationLongitude = {101.586720, 101.556720, 101.556720, 101.676720};
        double[] destinationLatitude = {3.032390, 3.03890, 3.08890, 4.03890 };


        //reminder: array start from zero 0
        for (int i=0; i<4; i++){

            final Point destination = Point.fromLngLat(originLongitude [i] , originLatitude [i]);
            final Point originPoint = Point.fromLngLat(destinationLongitude [i], destinationLatitude [i]);

            NavigationRoute.builder(Cumulative.this)
                    .accessToken(Mapbox.getAccessToken())
                    .origin(originPoint)
                    .destination(destination)
                    .build()
                    .getRoute(new Callback<DirectionsResponse>() {
                        @Override
                        public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                            
                            Timber.d("Response code: %s", response.code());
                            if (response.body() == null) {
                                Timber.e("No routes found, make sure you set the right user and access token.");
                                return;
                            } else if (response.body().routes().size() < 1) {
                                Timber.e("No routes found");
                                return;
                            }

                            currentRoute = response.body().routes().get(0);

                            double distance = (currentRoute.distance())/1000; //in meters, convert meter to km
                            String y =  String.format("%.2f", distance);
                            double time = (currentRoute.duration())/60; // in seconds, convert meter to km
                            String z =  String.format("%.2f", time);

                            xText.append("Distance: " + y + " Kilometers" + "\nTime: " + z + " minutes\n\n");
                            xTemporaryTextDistance.setText(y);
                            xTemporaryTextTime.setText(z);

                        }

                        @Override
                        public void onFailure(Call<DirectionsResponse> call, Throwable t) {

                        }
                    });
        }


        String jarak = xTemporaryTextDistance.getText().toString();
        String masa = xTemporaryTextTime.getText().toString();

        pass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] array={"asd","fgh","dcf","dg","ere","dsf"};
                Intent i=new Intent(Cumulative.this, GetArray.class);
                i.putExtra("PASS_DATA",array);
                startActivity(i);
            }
        });




    }
}