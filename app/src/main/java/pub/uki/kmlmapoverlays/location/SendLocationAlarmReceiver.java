package pub.uki.kmlmapoverlays.location;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class SendLocationAlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent serviceIntent = new Intent(context, SendMockLocationService.class);
        serviceIntent.setAction(LocationConst.ACTION_SEND_LOCATION);
        context.startService(serviceIntent);
    }
}
