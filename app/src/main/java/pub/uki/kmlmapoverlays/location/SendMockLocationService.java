package pub.uki.kmlmapoverlays.location;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;

import kmlmapoverlays.offline.maps.R;

/**
 * A Service that injects mock Location objects into the Location Services back-end. All other
 * apps that are connected to Location Services will see the test location values instead of
 * real values, until the test is over.
 */
public class SendMockLocationService extends Service implements ConnectionCallbacks, OnConnectionFailedListener {

    private static String TAG = SendMockLocationService.class.getSimpleName();

    private static final int NOTIFICATION_ID = -101;

    // Indicates if the spoofing location has started
    public static boolean spoofingStarted = false;

    // The time in seconds to wait between each mock location injection
    private static final int INJECTION_INTERVAL = 5;

    // Object that connects the app to Location Services
    private LocationClient locationClient;

    // Stores an instance of the local broadcast manager.
    private LocalBroadcastManager localBroadcastManager;

    private NotificationManager notificationManager;

    // Mock location to send to other apps
    private Location mockLocation;

    private AlarmManager alarmManager;
    private PendingIntent alarmIntent;

    @Override
    public void onCreate() {
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmIntent = PendingIntent.getBroadcast(this, 0, new Intent(this, SendLocationAlarmReceiver.class), 0);
    }

    @Override
    public IBinder onBind(Intent inputIntent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent startIntent, int flags, int startId) {
        if (startIntent != null && startIntent.getAction() != null) {

            // If the incoming Intent was a request to set mock location
            if (startIntent.getAction().equals(LocationConst.ACTION_START_SPOOFING)) {

                mockLocation = startIntent.getParcelableExtra(LocationConst.EXTRA_LOCATION);
                // Create a location client
                locationClient = new LocationClient(this, this, this);
                // Start connecting to Location Services
                locationClient.connect();
                spoofingStarted = true;

                // If the incoming Intent was a request to disable mock location
            } else if (startIntent.getAction().equals(LocationConst.ACTION_STOP_SPOOFING)) {

                // Clear the testing notification
                removeNotification();
                // Send a message back to the main activity
                sendBroadcastMessage(LocationConst.CODE_MOCK_STOPPED, 0);

                alarmManager.cancel(alarmIntent);

                if (locationClient != null && locationClient.isConnected()) {
                    locationClient.setMockMode(false);
                    locationClient.disconnect();
                }
                // If a test run is already in progress
                spoofingStarted = false;
                // Stop the service
                stopSelf();

                // If the incoming Intent was a request to send new mock location
            } else if (startIntent.getAction().equals(LocationConst.ACTION_NEW_LOCATION)) {

                mockLocation = startIntent.getParcelableExtra(LocationConst.EXTRA_LOCATION);
                alarmManager.cancel(alarmIntent);
                alarmManager.setRepeating(AlarmManager.RTC, System.currentTimeMillis(), INJECTION_INTERVAL * 1000, alarmIntent);

            } else if (startIntent.getAction().equals(LocationConst.ACTION_SEND_LOCATION)) {

                // Time values to put into the mock Location
                if (android.os.Build.VERSION.SDK_INT >= 17) {
                    mockLocation.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
                }
                mockLocation.setTime(System.currentTimeMillis());
                // Inject the mock location into Location Services
                try {
                    locationClient.setMockLocation(mockLocation);
                } catch (IllegalStateException e) {
                    Log.e(TAG, e.getMessage(), e);
                }
            }
        }
        // Tell the system to keep the Service alive, but to discard the Intent that started the Service
        return Service.START_STICKY;
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Send connection failure broadcast to main activity
        sendBroadcastMessage(LocationConst.CODE_CONNECTION_FAILED, result.getErrorCode());
        // Shut down
        stopSelf();
    }

    @Override
    public void onConnected(Bundle arg0) {
        // Add a notification that testing is in progress
        postNotification(getString(R.string.notification_content_mock_running));

        // Send message to main activity
        sendBroadcastMessage(LocationConst.CODE_CONNECTED, 0);

        // Start injecting mock locations into Location Services
        locationClient.setMockMode(true);
        alarmManager.setRepeating(AlarmManager.RTC, System.currentTimeMillis(), INJECTION_INTERVAL * 1000, alarmIntent);
    }

    @Override
    public void onDisconnected() {
        // If testing didn't finish, send an error message
        if (spoofingStarted) {
            sendBroadcastMessage(LocationConst.CODE_DISCONNECTED, LocationConst.CODE_MOCK_STOPPED);
        }
    }

    /**
     * Send a broadcast message back to the main Activity, indicating a change in status.
     *
     * @param code1 The main status code to return
     * @param code2 A subcode for the status code, or 0.
     */
    private void sendBroadcastMessage(int code1, int code2) {
        // Create a new Intent to send back to the main Activity
        Intent sendIntent = new Intent(LocationConst.ACTION_SERVICE_MESSAGE);
        // Put the status codes into the Intent
        sendIntent.putExtra(LocationConst.KEY_EXTRA_CODE1, code1);
        sendIntent.putExtra(LocationConst.KEY_EXTRA_CODE2, code2);
        // Send the Intent
        localBroadcastManager.sendBroadcast(sendIntent);
    }

    /**
     * Post a notification to the notification bar.
     *
     * @param contentText Text to use for the notification content (main line of expanded notification).
     */
    private void postNotification(String contentText) {
        String contentTitle = this.getString(R.string.notification_title_mock_running);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setAutoCancel(false)
                .setSmallIcon(R.drawable.ic_notify)
                .setContentTitle(contentTitle)
                .setContentText(contentText);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    /**
     * Remove notification from the notification bar.
     */
    private void removeNotification() {
        notificationManager.cancel(NOTIFICATION_ID);
    }

}
