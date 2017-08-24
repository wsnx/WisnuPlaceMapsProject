package id.my.wisnumurti.wisnuplacemapsproject;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.app.ProgressDialog;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Marker;
import java.util.ArrayList;

public class MapsActivity extends FragmentActivity {
    private final String TAG = getClass().getSimpleName();
    // available.
    Marker curlocmarker;
    private GoogleMap mMap; // Might be null if Google Play services APK is not
    private String[] places = {"ATM", "Bank", "Cafe", "Fire-Station", "Hospital",
            "Pharmacy", "Police", "Restaurant", "Shopping Mall"};
    private String[] placesname = {"ATM", "Bank", "Cafe", "Pos Pemadam Kebakaran",
            "Rumah Sakit", "Apotik", "Kantor Polisi", "Rumah Makan", "Tempat Belanja"};
    private LocationManager locationManager;
    private Location loc;
    private LocationListener listener = new LocationListener() {
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
        @Override
        public void onProviderEnabled(String provider) {
        }
        @Override
        public void onProviderDisabled(String provider) {
        }
        @Override
        public void onLocationChanged(Location location) {
            Log.e(TAG, "location update : " + location);
            loc = location;
            locationManager.removeUpdates(listener);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();
        Spinner spinnerPlace = (Spinner) findViewById(R.id.spinnerPlace);
        ArrayAdapter<String> placeAdapter = new ArrayAdapter<String>(MapsActivity.this,
                android.R.layout.simple_spinner_item, placesname);
        placeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPlace.setAdapter(placeAdapter);
        spinnerPlace.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.e(TAG, places[position].toLowerCase().replace("-", "_"));
                if (loc != null) {
                    mMap.clear();
                    new GetPlaces(MapsActivity.this,places[position].toLowerCase().replace("-", "_").replace("-", "_")).execute();
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }
    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK
     is correctly
     * installed) and the map has not already been instantiated.. This will ensure
     that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for
     the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and
     correctly
     * installing/updating/enabling the Google Play services. Since the
     FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would
     only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we
     should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }
    private void setUpMapIfNeeded() {
// Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
// Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment)
                    getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
// Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
                currentLocation();
            }
        }
    }
    /**
     * This is where we can add markers or lines, add listeners or move the camera.
     In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not
     null.
     */
    private void setUpMap() {
        mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
    }
    private void initCompo() {
        mMap = ((SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
    }
    private void currentLocation() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        String provider = locationManager.getBestProvider(new Criteria(), false);
        Location location = locationManager.getLastKnownLocation(provider);
        if (location == null) {
            locationManager.requestLocationUpdates(provider, 0, 0, listener);
        } else {
            loc = location;
            new GetPlaces(MapsActivity.this, places[0].toLowerCase().replace("-","_")).execute();
            Log.e(TAG, "location : " + location);
        }
    }

    private class GetPlaces extends AsyncTask<Void, Void, ArrayList<Place>> {
        private ProgressDialog dialog;
        private Context context;
        private String places;
        public GetPlaces(Context context, String places) {
            this.context = context;
            this.places = places;
        }
        @Override
        protected void onPostExecute(ArrayList<Place> result) {
            super.onPostExecute(result);
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            LatLng currloc = new LatLng(loc.getLatitude(), loc.getLongitude());
            curlocmarker = mMap.addMarker(new MarkerOptions()
                    .position(currloc)
                    .title("My Location")
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.user_marker)));
            for (int i = 0; i < result.size(); i++) {
                mMap.addMarker(new MarkerOptions()
                        .title(result.get(i).getName())
                        .position(new LatLng(result.get(i).getLatitude(),
                                result.get(i).getLongitude()))
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_pink))
                        .snippet(result.get(i).getVicinity()));
            }
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(result.get(0).getLatitude(),
                            result.get(0).getLongitude())) // Sets the center of the map to
// Mountain View
                    .zoom(14) // Sets the zoom
                    .tilt(30) // Sets the tilt of the camera to 30 degrees
                    .build(); // Creates a CameraPosition from the builder
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(context);
            dialog.setCancelable(false);
            dialog.setMessage("Loading..");
            dialog.isIndeterminate();
            dialog.show();
        }
        @Override
        protected ArrayList<Place> doInBackground(Void... arg0) {
//use apikey browser AIzaSyCXUUywhyNKhftGkki8g7sX90qjhJ7_6LM
            PlacesService service = new
                    PlacesService("AIzaSyDDfPTAeQB2gDcwU6nIUkjIaKrbBkHR4ug");
            ArrayList<Place> findPlaces = service.findPlaces(loc.getLatitude(),
                    loc.getLongitude(), places);
            for (int i = 0; i < findPlaces.size(); i++) {
                Place placeDetail = findPlaces.get(i);
                Log.e(TAG, "places : " + placeDetail.getName());
            }
            return findPlaces;
        }
    }
}






