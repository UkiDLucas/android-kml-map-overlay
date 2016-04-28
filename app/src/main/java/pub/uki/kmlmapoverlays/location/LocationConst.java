package pub.uki.kmlmapoverlays.location;

/**
 * Constants used in other classes in the app
 */
public final class LocationConst {
    /*
     * Action values sent by Intent from the main activity to the service
     */
    // Request start spoofing location
    public static final String ACTION_START_SPOOFING = "technology.uki.kmlmapoverlays.offline.maps.ACTION_START_SPOOFING";
    // Request stop spoofing location
    public static final String ACTION_STOP_SPOOFING = "technology.uki.kmlmapoverlays.offline.maps.ACTION_STOP_SPOOFING";
    // Request spoof new location
    public static final String ACTION_SEND_LOCATION = "technology.uki.kmlmapoverlays.offline.maps.ACTION_SEND_LOCATION";
    // Request spoof new location
    public static final String ACTION_NEW_LOCATION = "technology.uki.kmlmapoverlays.offline.maps.ACTION_NEW_LOCATION";

    /*
     * Extended data keys for the broadcast Intent sent from the service to the main activity.
     * Key1 is the base connection message.
     * Key2 is extra data or error codes.
     */
    public static final String KEY_EXTRA_CODE1 = "technology.uki.kmlmapoverlays.offline.maps.KEY_EXTRA_CODE1";
    public static final String KEY_EXTRA_CODE2 = "technology.uki.kmlmapoverlays.offline.maps.KEY_EXTRA_CODE2";

    /*
     * Codes for communicating status back to the main activity
     */
    // The location client is disconnected
    public static final int CODE_DISCONNECTED = 0;
    // The location client is connected
    public static final int CODE_CONNECTED = 1;
    // The client failed to connect to Location Services
    public static final int CODE_CONNECTION_FAILED = 2;
    // Location client started sending mock location to other apps
    public static final int CODE_MOCK_STARTED = 3;
    // Location client stopped sending mock location to other apps
    public static final int CODE_MOCK_STOPPED = 4;

    // The name used for all mock locations
    public static final String LOCATION_PROVIDER = "flp";
    // Mark the broadcast Intent with an action
    public static final String ACTION_SERVICE_MESSAGE = "technology.uki.kmlmapoverlays.offline.maps.ACTION_SERVICE_MESSAGE";

    /*
     * Key for extended data in the Activity's outgoing Intent that records the requested mock location
     * for mock locations sent to Location Services.
     */
    public static final String EXTRA_LOCATION = "technology.uki.kmlmapoverlays.offline.maps.EXTRA_LOCATION";
}
