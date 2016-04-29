package pub.uki.kmlmapoverlays;


import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

import org.osmdroid.bonuspack.overlays.MapEventsOverlay;
import org.osmdroid.bonuspack.overlays.MapEventsReceiver;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.ScaleBarOverlay;

import java.io.File;
import java.util.ArrayList;

import kmlmapoverlays.offline.maps.R;
import pub.uki.kmlmapoverlays.dialog.LocationNotSelectedDialogFragment;
import pub.uki.kmlmapoverlays.dialog.MockLocationDisabledDialogFragment;
import pub.uki.kmlmapoverlays.dialog.TilesNotFoundDialogFragment;
import pub.uki.kmlmapoverlays.location.LocationConst;
import pub.uki.kmlmapoverlays.location.SendMockLocationService;
import pub.uki.kmlmapoverlays.location.ServiceMessageReceiver;
import pub.uki.kmlmapoverlays.tile.CheckTilesExistenceTask;
import pub.uki.kmlmapoverlays.tile.ExtendedTilesOverlay;
import pub.uki.kmlmapoverlays.tile.ExtendedTilesOverlayFactory;

public class MapFragment extends Fragment
        implements
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener,
        MapListener,
        MapEventsReceiver {

    private static final String TAG = MapFragment.class.getSimpleName();

    private static final String KEY_LATITUDE = "lat";
    private static final String KEY_LONGITUDE = "lng";
    private static final String KEY_ZOOM = "zoom";

    private static final float LOCATION_ACCURACY = 3.0f;

    private MapView mapView;
    private ItemizedIconOverlay<OverlayItem> selectedLocationOverlay;

    private CheckBox btnSpoofLocation;
    private TextView selectedLocation;
    private TextView zoomLvlText;

    private SharedPreferences prefs;

    private GeoPoint currentSelectedLocation;

    // Broadcast receiver for local broadcasts from SendMockLocationService
    private ServiceMessageReceiver messageReceiver = new ServiceMessageReceiver() {
        @Override
        protected void onMockLocationSpoofingStarted() {
            super.onMockLocationSpoofingStarted();
            btnSpoofLocation.setChecked(true);
            btnSpoofLocation.setText(getString(R.string.sending_location_to_other_apps));
        }

        @Override
        protected void onMockLocationSpoofingStopped() {
            super.onMockLocationSpoofingStopped();
            btnSpoofLocation.setChecked(false);
            btnSpoofLocation.setText(getString(R.string.press_to_send_location));
        }
    };

    private LocationClient locationClient;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.map_fragment, null);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        findViews();

        setupMap();
        initSendLocationBtn();
        setupLocationClientIfNeeded();
    }

    private void findViews() {
        selectedLocation = (TextView) getView().findViewById(R.id.selected_location);
        zoomLvlText = (TextView) getView().findViewById(R.id.zoom_lvl);
        mapView = (MapView) getView().findViewById(R.id.mapview);
        btnSpoofLocation = (CheckBox) getView().findViewById(R.id.spoof_location_button);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (locationClient != null && (locationClient.isConnected() || locationClient.isConnecting())) {
            locationClient.disconnect();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.map, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_mylocation:
                requestCurrentLocation();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void requestCurrentLocation() {
        if (locationClient == null) {
            setupLocationClientIfNeeded();
        } else {
            Location location = locationClient.getLastLocation();
            if (location != null) {
                currentSelectedLocation = new GeoPoint(location.getLatitude(), location.getLongitude());
                mapView.getController().setCenter(currentSelectedLocation);
            } else {
                locationClient.requestLocationUpdates(LocationRequest.create().setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY), new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        currentSelectedLocation = new GeoPoint(location.getLatitude(), location.getLongitude());
                        locationClient.removeLocationUpdates(this);
                        mapView.getController().setCenter(currentSelectedLocation);
                        mapView.getController().setZoom(Math.max(mapView.getZoomLevel(), 12));
                    }
                });
            }
        }
    }

    private void setupLocationClientIfNeeded() {
        if (locationClient == null) {
            locationClient = new LocationClient(getActivity().getApplicationContext(), this, this);
        }
        if (!locationClient.isConnected() && !locationClient.isConnecting()) {
            locationClient.connect();
        }
    }

    private void initSendLocationBtn() {
        btnSpoofLocation.setText(SendMockLocationService.spoofingStarted ? getString(R.string.sending_location_to_other_apps) : getString(R.string.press_to_send_location));
        btnSpoofLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentSelectedLocation != null) {
                    if (mockLocationsEnabled()) {
                        // Instantiate the Intent that starts SendMockLocationService
                        Intent startServiceIntent = new Intent(getActivity(), SendMockLocationService.class);
                        if (!btnSpoofLocation.isChecked()) {
                            btnSpoofLocation.setText(getString(R.string.press_to_send_location));
                            startServiceIntent.setAction(LocationConst.ACTION_STOP_SPOOFING);
                        } else {
                            btnSpoofLocation.setText(getString(R.string.sending_location_to_other_apps));
                            startServiceIntent.setAction(LocationConst.ACTION_START_SPOOFING);
                            startServiceIntent.putExtra(LocationConst.EXTRA_LOCATION, createMockLocation());
                        }
                        // Start SendMockLocationService
                        getActivity().startService(startServiceIntent);
                    } else if (btnSpoofLocation.isChecked()) {
                        btnSpoofLocation.setChecked(false);
                        MockLocationDisabledDialogFragment dialogFragment = MockLocationDisabledDialogFragment.newInstance();
                        dialogFragment.show(getActivity().getSupportFragmentManager(), null);
                    }
                } else {
                    btnSpoofLocation.setChecked(false);
                    LocationNotSelectedDialogFragment dialogFragment = LocationNotSelectedDialogFragment.newInstance();
                    dialogFragment.show(getActivity().getSupportFragmentManager(), null);
                }
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(messageReceiver, new IntentFilter(LocationConst.ACTION_SERVICE_MESSAGE));
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(messageReceiver);
    }

    private boolean mockLocationsEnabled() {
        // returns true if mock location enabled, false if not enabled.
        return !"0".equals(Settings.Secure.getString(getActivity().getContentResolver(), Settings.Secure.ALLOW_MOCK_LOCATION));
    }

    private void setupMap() {
        final File tilesDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + getString(R.string.app_name));
        new CheckTilesExistenceTask() {
            @Override
            protected void onPostExecute(Boolean tilesExist) {
                super.onPostExecute(tilesExist);

                final ExtendedTilesOverlayFactory tilesOverlayFactory = new ExtendedTilesOverlayFactory(getActivity().getApplicationContext(), mapView.getTileRequestCompleteHandler());
                if (tilesExist) {
                    new AsyncTask<Void, Void, ExtendedTilesOverlay>() {
                        @Override
                        protected ExtendedTilesOverlay doInBackground(Void... params) {
                            return tilesOverlayFactory.create(tilesDir);
                        }

                        @Override
                        protected void onPostExecute(ExtendedTilesOverlay tilesOverlay) {
                            mapView.setUseDataConnection(false);
                            mapView.setMultiTouchControls(true);
                            mapView.setBuiltInZoomControls(true);
                            mapView.setMinZoomLevel(tilesOverlay.getMinZoomLevel());
                            mapView.setMaxZoomLevel(tilesOverlay.getMaxZoomLevel());

                            mapView.getOverlays().add(tilesOverlay);

                            ScaleBarOverlay mScaleBarOverlay = new ScaleBarOverlay(getActivity());
                            mScaleBarOverlay.enableScaleBar();
                            mapView.getOverlayManager().add(mScaleBarOverlay);

                            mapView.setMapListener(MapFragment.this);
                            mapView.getOverlayManager().add(new MapEventsOverlay(getActivity(), MapFragment.this));

                            //if there is previously saved zoom level and current data set contains this zoom level
                            //set it as default
                            if (prefs.contains(KEY_ZOOM) && tilesOverlay.containsZoomLevel(prefs.getInt(KEY_ZOOM, 0))) {
                                mapView.getController().setZoom(prefs.getInt(KEY_ZOOM, 0));
                            } else {
                                //set avg zoom level from data set otherwise
                                mapView.getController().setZoom(tilesOverlay.getAvgZoomLevel());
                            }

                            // if there is previously saved user selection show pin on the map
                            if (prefs.contains(KEY_LATITUDE) && prefs.contains(KEY_LONGITUDE)) {
                                currentSelectedLocation = new GeoPoint(prefs.getInt(KEY_LATITUDE, 0), prefs.getInt(KEY_LONGITUDE, 0));
                                displaySelectedLocation();
                                mapView.getController().setCenter(currentSelectedLocation);
                            } else {
                                mapView.getController().setCenter(tilesOverlay.getMiddleGeoPointForZoomLevel(mapView.getZoomLevel()));
                            }
                            zoomLvlText.setText("zoom: " + mapView.getZoomLevel());
                            btnSpoofLocation.setEnabled(true);
                        }
                    }.execute();
                } else {
                    TilesNotFoundDialogFragment dialogFragment = TilesNotFoundDialogFragment.newInstance(tilesDir.toString());
                    dialogFragment.show(getActivity().getSupportFragmentManager(), null);
                }
            }
        }.execute(tilesDir);
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "Location client connected");
        requestCurrentLocation();
    }

    @Override
    public void onDisconnected() {
        Log.i(TAG, "Location client disconnected");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.w(TAG, "Location client failed to connect");
    }

    @Override
    public boolean onScroll(ScrollEvent scrollEvent) {
        return false;
    }

    @Override
    public boolean onZoom(ZoomEvent zoomEvent) {
        //save current zoom level
        prefs.edit().putInt(KEY_ZOOM, zoomEvent.getZoomLevel()).commit();
        zoomLvlText.setText("zoom: " + zoomEvent.getZoomLevel());
        return false;
    }

    @Override
    public boolean singleTapConfirmedHelper(GeoPoint geoPoint) {
        return false;
    }

    @Override
    public boolean longPressHelper(GeoPoint geoPoint) {
        currentSelectedLocation = geoPoint;
        // save current selected location
        prefs.edit().putInt(KEY_LATITUDE, currentSelectedLocation.getLatitudeE6()).putInt(KEY_LONGITUDE, currentSelectedLocation.getLongitudeE6()).commit();
        displaySelectedLocation();
        //send new location to other apps if checked
        if (btnSpoofLocation.isChecked()) {
            Intent startServiceIntent = new Intent(getActivity(), SendMockLocationService.class);
            startServiceIntent.setAction(LocationConst.ACTION_NEW_LOCATION);
            startServiceIntent.putExtra(LocationConst.EXTRA_LOCATION, createMockLocation());
            getActivity().startService(startServiceIntent);
        }
        return true;
    }

    private void displaySelectedLocation() {
        selectedLocation.setText(getString(R.string.selected_location, currentSelectedLocation.getLatitude(), currentSelectedLocation.getLongitude()));
        if (selectedLocationOverlay != null && mapView.getOverlayManager().contains(selectedLocationOverlay)) {
            mapView.getOverlayManager().remove(selectedLocationOverlay);
        }
        OverlayItem item = new OverlayItem("", "", currentSelectedLocation);
        item.setMarker(getActivity().getResources().getDrawable(R.drawable.ic_pin));
        ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();
        items.add(item);
        selectedLocationOverlay = new ItemizedIconOverlay<OverlayItem>(getActivity(), items, null);
        mapView.getOverlayManager().add(selectedLocationOverlay);
        mapView.invalidate();
    }

    private Location createMockLocation() {
        // Create a new Location to inject into Location Services
        Location mockLocation = new Location(LocationConst.LOCATION_PROVIDER);
        // Set the location accuracy, latitude, and longitude
        mockLocation.setAccuracy(LOCATION_ACCURACY);
        mockLocation.setLatitude(currentSelectedLocation.getLatitude());
        mockLocation.setLongitude(currentSelectedLocation.getLongitude());
        return mockLocation;
    }
}
