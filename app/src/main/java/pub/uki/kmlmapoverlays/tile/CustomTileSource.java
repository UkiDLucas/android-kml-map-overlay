package pub.uki.kmlmapoverlays.tile;

import org.osmdroid.ResourceProxy;
import org.osmdroid.tileprovider.tilesource.BitmapTileSourceBase;

/**
 * Simple implementation that extends BitmapTileSourceBase and nothing else
 */
public class CustomTileSource extends BitmapTileSourceBase {

    public CustomTileSource(String aName, ResourceProxy.string aResourceId, int aZoomMinLevel, int aZoomMaxLevel, int aTileSizePixels, String aImageFilenameEnding) {
        super(aName, aResourceId, aZoomMinLevel, aZoomMaxLevel, aTileSizePixels, aImageFilenameEnding);
    }
}
