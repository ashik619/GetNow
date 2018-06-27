package com.getnowsolutions.getnow.fragments;


import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.getnowsolutions.getnow.PlaceSearchActivity;
import com.getnowsolutions.getnow.R;
import com.getnowsolutions.getnow.handlers.networkhandlers.APIClient;
import com.getnowsolutions.getnow.handlers.networkhandlers.APIInterface;
import com.getnowsolutions.getnow.models.NearbyBus;
import com.getnowsolutions.getnow.utils.Constants;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Dash;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.maps.android.SphericalUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class MapFragment extends Fragment implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {


    @BindView(R.id.yourLocationText)
    TextView yourLocationText;
    @BindView(R.id.destinationText)
    TextView destinationText;
    Unbinder unbinder;
    @BindView(R.id.finBusButton)
    RelativeLayout finBusButton;
    @BindView(R.id.srcButton)
    CardView srcButton;
    @BindView(R.id.dstButton)
    CardView dstButton;
    private Location currentLocation;
    private LatLng srcLatLng;
    private LatLng dstLatLng;

    public MapFragment() {
        // Required empty public constructor
    }

    private GoogleMap mMap;
    private MapView mapView;
    private GoogleApiClient mGoogleApiClient;
    private Marker originMarker;
    private Marker dstMarker;
    private Polyline polyline;
    private int locUpdateCount = 0;
    private static final int MAP_ZOOM = 18;
    private Handler findNearbyBusHandler;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        mapView = (MapView) view.findViewById(R.id.mapView);
        mapView.getMapAsync(this);
        mapView.onCreate(savedInstanceState);
        mapView.onResume(); // needed to get the map to display immediately
        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        listners();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        MapStyleOptions style = MapStyleOptions.loadRawResourceStyle(
                getActivity(), R.raw.map_style);
        mMap.setMapStyle(style);
        buildGoogleApiClient();
        findNearbyBusHandler.postDelayed(findNearbyBusRunnable,3000);
    }

    @Override
    public void onDetach() {
        findNearbyBusHandler.removeCallbacks(findNearbyBusRunnable);
        super.onDetach();
    }

    void listners() {
        dstButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, MapFragment.this);
                Intent intent = new Intent(getActivity(), PlaceSearchActivity.class);
                if (currentLocation != null) {
                    intent.putExtra("loc", String.valueOf(currentLocation.getLatitude()) + "," +
                            String.valueOf(currentLocation.getLongitude()));
                } else {
                    intent.putExtra("loc", "9.9312328" + "," + "76.26730");
                }
                startActivityForResult(intent, 34);
            }
        });
        srcButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, MapFragment.this);
                Intent intent = new Intent(getActivity(), PlaceSearchActivity.class);
                if (currentLocation != null) {
                    intent.putExtra("loc", String.valueOf(currentLocation.getLatitude()) + "," +
                            String.valueOf(currentLocation.getLongitude()));
                } else {
                    intent.putExtra("loc", "9.9312328" + "," + "76.26730");
                }
                startActivityForResult(intent, 35);
            }
        });
    }

    private synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (location != null) {
            currentLocation = location;
            srcLatLng = new LatLng(location.getLatitude(), location.getLongitude());
            if(originMarker == null) {
                originMarker = mMap.addMarker(new MarkerOptions()
                        .position(srcLatLng)
                        .title("Your Location")
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_3d_marker)));
            }else originMarker.setPosition(srcLatLng);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(srcLatLng, MAP_ZOOM));
        } else {
            srcLatLng = Constants.kochiLatLng;
            if(originMarker == null) {
                originMarker = mMap.addMarker(new MarkerOptions()
                        .position(Constants.kochiLatLng)
                        .title("Your Location")
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_3d_marker)));
            }else originMarker.setPosition(Constants.kochiLatLng);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(Constants.kochiLatLng, MAP_ZOOM));
        }
        LocationRequest mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(3);
        mLocationRequest.setFastestInterval(2);
        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        } catch (SecurityException e) {

        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location == null)
            return;
        currentLocation = location;
        srcLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        if(locUpdateCount==1){
            findNearByBuses(String.valueOf(location.getLatitude()),String.valueOf(location.getLongitude()));
        }
        ++locUpdateCount;
        originMarker.setPosition(srcLatLng);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(srcLatLng, MAP_ZOOM));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (polyline != null) {
                polyline.remove();
            }
            switch (requestCode) {
                case 34: {
                    dstLatLng = new LatLng(data.getDoubleExtra("lat", 9.9312328d), data.getDoubleExtra("lng", 76.26730d));
                    dstMarker = mMap.addMarker(new MarkerOptions()
                            .position(dstLatLng)
                            .title("Your Destination")
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_3d_marker)));
                    if(srcLatLng!=null) {
                        LatLngBounds.Builder builder = new LatLngBounds.Builder();
                        builder.include(srcLatLng);
                        builder.include(dstLatLng);
                        LatLngBounds latLngBounds = builder.build();
                        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 400));
                        showCurvedPolyline(srcLatLng, dstLatLng, 0.5);
                    }else {
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(dstLatLng,MAP_ZOOM));
                    }
                    destinationText.setText(data.getStringExtra("place"));
                    break;
                }
                case 35: {
                    srcLatLng = new LatLng(data.getDoubleExtra("lat", 9.9312328d), data.getDoubleExtra("lng", 76.26730d));
                    originMarker = mMap.addMarker(new MarkerOptions()
                            .position(srcLatLng)
                            .title("Your Destination")
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_3d_marker)));
                    if(dstLatLng!=null) {
                        LatLngBounds.Builder builder = new LatLngBounds.Builder();
                        builder.include(srcLatLng);
                        builder.include(dstLatLng);
                        LatLngBounds latLngBounds = builder.build();
                        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 400));
                        showCurvedPolyline(srcLatLng, dstLatLng, 0.5);
                    }else {
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(srcLatLng,MAP_ZOOM));
                    }
                    yourLocationText.setText(data.getStringExtra("place"));
                    break;
                }
            }
        }
    }
    Runnable findNearbyBusRunnable = new Runnable() {
        @Override
        public void run() {
            if(currentLocation!=null) {
                findNearByBuses(String.valueOf(currentLocation.getLatitude()), String.valueOf(currentLocation.getLongitude()));
                findNearbyBusHandler.postDelayed(findNearbyBusRunnable,3000);
            }

        }
    };

    private void findNearByBuses(String lat,String lng){
        APIInterface apiInterface = APIClient.getClient().create(APIInterface.class);
        Call<JsonObject> call = apiInterface.findNearBuses(lat,lng);
        Log.e("URL",call.request().url().toString());
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                Log.e("RESP",response.code()+"");
                if(response.isSuccessful()){
                    Log.e("RESP",response.body().toString());
                    if(response.body().get("success").getAsBoolean()){
                        JsonArray busArray = response.body().getAsJsonArray("data");
                        if(busArray.size()>0) {
                            NearbyBus[] nearbyBuses = new NearbyBus[busArray.size()];
                            for (int i = 0; i < busArray.size(); i++) {
                                JsonObject busObj = busArray.get(i).getAsJsonObject();
                                if(busObj.get("status").getAsInt()>0) {
                                    NearbyBus bus = new NearbyBus();
                                    bus.setName(busObj.get("name").getAsString());
                                    double lat = busObj.getAsJsonObject("cloc")
                                            .getAsJsonArray("coordinates")
                                            .get(1).getAsDouble();
                                    double lng = busObj.getAsJsonObject("cloc")
                                            .getAsJsonArray("coordinates")
                                            .get(0).getAsDouble();
                                    Log.e("LOC", lat + "");
                                    bus.setLatLng(new LatLng(lat, lng));
                                    nearbyBuses[i] = bus;
                                }
                            }
                            showNearByBuses(nearbyBuses);
                        }
                    }

                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {

            }
        });

    }
    private void showNearByBuses(NearbyBus[] nearbyBuses){
        for (NearbyBus bus : nearbyBuses){
             Marker busMarker = mMap.addMarker(new MarkerOptions()
                    .position(bus.getLatLng())
                    .title(bus.getName())
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_bus)));
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    void drawArcFromSrcToDst(LatLng start, LatLng end) {
        ArrayList<LatLng> alLatLng = new ArrayList<>();
        double cLat = ((start.latitude + end.latitude) / 2);
        double cLon = ((start.longitude + end.longitude) / 2);

        //add skew and arcHeight to move the midPoint
        if (Math.abs(start.longitude - end.longitude) < 0.0001) {
            cLon -= 0.0195;
        } else {
            cLat += 0.0195;
        }

        double tDelta = 1.0 / 50;
        for (double t = 0; t <= 1.0; t += tDelta) {
            double oneMinusT = (1.0 - t);
            double t2 = Math.pow(t, 2);
            double lon = oneMinusT * oneMinusT * start.longitude
                    + 2 * oneMinusT * t * cLon
                    + t2 * end.longitude;
            double lat = oneMinusT * oneMinusT * start.latitude
                    + 2 * oneMinusT * t * cLat
                    + t2 * end.latitude;
            alLatLng.add(new LatLng(lat, lon));
        }

        // draw polyline
        PolylineOptions line = new PolylineOptions();
        line.width(7);
        line.color(ContextCompat.getColor(getContext(), R.color.colorPrimary));
        line.add(start);
        line.addAll(alLatLng);
        //line.add(end);
        polyline = mMap.addPolyline(line);
    }

    private void showCurvedPolyline(LatLng p1, LatLng p2, double k) {
        //Calculate distance and heading between two points
        double d = SphericalUtil.computeDistanceBetween(p1, p2);
        double h = SphericalUtil.computeHeading(p1, p2);

        //Midpoint position
        LatLng p = SphericalUtil.computeOffset(p1, d * 0.5, h);

        //Apply some mathematics to calculate position of the circle center
        double x = (1 - k * k) * d * 0.5 / (2 * k);
        double r = (1 + k * k) * d * 0.5 / (2 * k);

        LatLng c = SphericalUtil.computeOffset(p, x, h + 90.0);

        //Polyline options
        PolylineOptions options = new PolylineOptions();
        List<PatternItem> pattern = Arrays.<PatternItem>asList(new Dash(30), new Gap(20));

        //Calculate heading between circle center and two points
        double h1 = SphericalUtil.computeHeading(c, p1);
        double h2 = SphericalUtil.computeHeading(c, p2);

        //Calculate positions of points on circle border and add them to polyline options
        int numpoints = 100;
        double step = (h2 - h1) / numpoints;

        for (int i = 0; i < numpoints; i++) {
            LatLng pi = SphericalUtil.computeOffset(c, r, h1 + i * step);
            options.add(pi);
        }

        //Draw polyline
        polyline = mMap.addPolyline(options.width(10).color(ContextCompat.getColor(getContext(), R.color.polyLine)).geodesic(false).pattern(pattern));
    }

}
