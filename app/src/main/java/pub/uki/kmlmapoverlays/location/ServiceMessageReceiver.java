package pub.uki.kmlmapoverlays.location;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public abstract class ServiceMessageReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Get the message code from the incoming Intent
        int code1 = intent.getIntExtra(LocationConst.KEY_EXTRA_CODE1, 0);
        int code2 = intent.getIntExtra(LocationConst.KEY_EXTRA_CODE2, 0);
        // Choose the action, based on the message code
        switch (code1) {
                /*
                 * SendMockLocationService reported that the location client is connected. Update
                 * the app status reporting field in the UI.
                 */
            case LocationConst.CODE_CONNECTED:
                onConnected();
                break;

                /*
                 * SendMockLocationService reported that the location client disconnected. This
                 * happens if Location Services drops the connection. Update the app status and the
                 * connection status reporting fields in the UI.
                 */
            case LocationConst.CODE_DISCONNECTED:
                onDisconnected();
                break;

                /*
                 * SendMockLocationService reported that an attempt to connect to Location
                 * Services failed. The Service has already stopped itself.
                 * Update the connection status reporting field and include the error code.
                 * Also update the app status field
                 */
            case LocationConst.CODE_CONNECTION_FAILED:
                onConnectionFailed();
                break;

                /*
                 * SendMockLocationService reported that the user requested a mock location, but the same location
                 * is already set. Update the app status reporting field.
                 */
            case LocationConst.CODE_MOCK_STARTED:
                onMockLocationSpoofingStarted();
                break;

                /*
                 * SendMockLocationService reported that was interrupted by user.
                 * Update the app status reporting field.
                 */
            case LocationConst.CODE_MOCK_STOPPED:
                onMockLocationSpoofingStopped();
                break;
            default:
                break;
        }
    }

    protected void onConnected() {
    }

    ;

    protected void onDisconnected() {
    }

    ;

    protected void onConnectionFailed() {
    }

    ;

    protected void onMockLocationSpoofingStarted() {
    }

    ;

    protected void onMockLocationSpoofingStopped() {
    }

    ;
}
