package pub.uki.kmlmapoverlays;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import kmlmapoverlays.offline.maps.R;

public class MapScreen extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_screen);
    }

}
