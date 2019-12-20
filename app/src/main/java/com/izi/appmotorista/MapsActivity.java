package com.izi.appmotorista;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.izi.appmotorista.helper.ConfiguracaoFirebase;
import com.izi.appmotorista.helper.Permissoes;
import com.izi.appmotorista.model.Localizacao;
import com.izi.appmotorista.model.Motoca;

import java.util.ArrayList;
import java.util.List;



public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, RoutingListener {

    private Toolbar toolbar;
    private ImageView btnConfimarPedido;
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private Toolbar supportActionBar;
    private DatabaseReference mDatabase;
    private ProgressDialog progressDialog;
    private Button btnRecebido;
    private LatLng latLng;

    private Localizacao local = new Localizacao();

    private Motoca motoca = new Motoca();
    private String [] permissoes = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION
    };

  //  private DatabaseReference mDatabase;

    private String idLoja;

    private Bundle bConfirma;
    private Intent iConfirma;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inicializarComponentes();
        recuperarLocalizacao();
        setContentView(R.layout.activity_maps);
        Permissoes.validarPermissoes(permissoes,this, 1);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks((GoogleApiClient.ConnectionCallbacks) this)
                    .addOnConnectionFailedListener((GoogleApiClient.OnConnectionFailedListener) this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }


    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }

    private void recuperarLocalizacao(){

        DatabaseReference lojaRef = mDatabase
                .child("motoca");

        lojaRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    motoca =  ds.getValue(Motoca.class); // try to pass values to my Localizacao.java
                }

                //  Double latitude = local.getLatitude();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    @Override
    public void onConnected(Bundle bundle) {


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)
        { return; }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null)
        {
            if(mMap != null)
            {
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                Marker marker1 =  mMap.addMarker(new MarkerOptions().position(latLng).title("Minha Posição"));
                final LatLng latLng2 = new LatLng(-22.839677, -47.052799);
                Marker marker2 =  mMap.addMarker(new MarkerOptions().position(latLng2).title("Minha Posição"));
                builder.include(marker1.getPosition());
                builder.include(marker2.getPosition());


                LatLngBounds bounds = builder.build();

                int width = getResources().getDisplayMetrics().widthPixels;
                int height = getResources().getDisplayMetrics().heightPixels;
                int padding = (int) (width * 0.10); // offset from edges of the map 10% of screen

                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);

                mMap.animateCamera(cu);

                getRouteToMarker(latLng);
            }


        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    private void inicializarComponentes(){
        mDatabase = ConfiguracaoFirebase.getFirebase();
        polylines = new ArrayList<>();
    }

    private void getRouteToMarker(LatLng pickupLatLng) {

        if (pickupLatLng != null && mLastLocation != null) {
            Routing routing = new Routing.Builder()
                    .travelMode(AbstractRouting.TravelMode.DRIVING)
                    .withListener(this)
                    .alternativeRoutes(false)
                    .key("AIzaSyCVA5Z6ZpycRP-NPtyKjvBbUSKXJ6aiD70")
                    .waypoints(new LatLng(-22.839677, -47.052799), pickupLatLng)
                    .build();
            routing.execute();
        }
    }


    private List<Polyline> polylines;
    private static final int[] COLORS = new int[]{R.color.primary_dark_material_light};

    @Override
    public void onRoutingFailure(RouteException e) {
//        progressDialog.dismiss();
        if (e != null) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Something went wrong, Try again", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {
        // progressDialog.dismiss();
        if (polylines.size() > 0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }
        polylines = new ArrayList<>();
        //add route(s) to the map.
        for (int i = 0; i < route.size(); i++) {

            //In case of more than 5 alternative routes
            int colorIndex = i % COLORS.length;

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(COLORS[colorIndex]));
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = mMap.addPolyline(polyOptions);
            polylines.add(polyline);

            // Toast.makeText(getApplicationContext(), "Route " + (i + 1) + ": distance - " + route.get(i).getDistanceValue() + ": duration - " + route.get(i).getDurationValue(), Toast.LENGTH_SHORT).show();
        }


    }


    @Override
    public void onRoutingCancelled() {
    }

    private void erasePolylines(){
        for(Polyline line : polylines){
            line.remove();
        }
        polylines.clear();
    }
}
