package pub.uki.kmlmapoverlays.tile;

import android.content.Context;
import android.util.SparseArray;
import android.util.SparseIntArray;

import org.osmdroid.tileprovider.MapTileProviderBase;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.TilesOverlay;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ExtendedTilesOverlay extends TilesOverlay {
    // TODO: we need a better name for this (this container doesn't hold tiles)
    private SparseArray<SparseArray<List<Integer>>> tilesHierarchy = new SparseArray<SparseArray<List<Integer>>>();

    private SparseArray<SparseIntArray> middleTileForZoomCache;
    private int avgZoomLevel;

    public ExtendedTilesOverlay(MapTileProviderBase aTileProvider, Context aContext) {
        super(aTileProvider, aContext);
        avgZoomLevel = -1;
        middleTileForZoomCache = new SparseArray<SparseIntArray>();
    }

    void setTilesHierarchy(SparseArray<SparseArray<List<Integer>>> tilesHierarchy) {
        this.tilesHierarchy = tilesHierarchy;
    }

    public int getMinZoomLevel() {
        return tilesHierarchy.keyAt(0);
    }

    public int getMaxZoomLevel() {
        return tilesHierarchy.keyAt(tilesHierarchy.size() - 1);
    }

    public int getAvgZoomLevel() {
        if (avgZoomLevel == -1) {
            int[] zoomLevels = new int[tilesHierarchy.size()];
            for (int i = 0; i < tilesHierarchy.size(); i++) {
                zoomLevels[i] = (tilesHierarchy.keyAt(i));
            }
            Arrays.sort(zoomLevels);
            avgZoomLevel = zoomLevels[zoomLevels.length / 2];
        }
        return avgZoomLevel;
    }

    public boolean containsZoomLevel(int zoomLevel) {
        return tilesHierarchy.indexOfKey(zoomLevel) >= 0;
    }

    public SparseIntArray getMiddleTileXYForZoomLevel(int zoomLevel) {
        if (middleTileForZoomCache.get(zoomLevel) == null) {

            SparseArray<List<Integer>> tilesForZoom = tilesHierarchy.get(zoomLevel);

            int tileX[] = new int[tilesForZoom.size()];
            for (int i = 0; i < tilesForZoom.size(); i++) {
                tileX[i] = tilesForZoom.keyAt(i);
            }
            Arrays.sort(tileX);
            int middleX = tileX[tileX.length / 2];

            List<Integer> yTiles = tilesForZoom.get(middleX);
            Collections.sort(yTiles);
            int middleY = yTiles.get(yTiles.size() / 2);

            SparseIntArray tileXY = new SparseIntArray();
            tileXY.put(middleX, middleY);

            middleTileForZoomCache.put(zoomLevel, tileXY);
        }
        return middleTileForZoomCache.get(zoomLevel);
    }

    public GeoPoint getMiddleGeoPointForZoomLevel(int zoomLevel) {
        SparseIntArray tileXY = getMiddleTileXYForZoomLevel(zoomLevel);
        BoundingBox bb = BoundingBox.tile2boundingBox(tileXY.keyAt(0), tileXY.get(tileXY.keyAt(0)), zoomLevel);

        double lng = (bb.getWest() + bb.getEast()) / 2;
        double lat = (bb.getNorth() + bb.getSouth()) / 2;

        return new GeoPoint(lat, lng);
    }

}
