package pub.uki.kmlmapoverlays.tile;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.util.SparseArray;

import org.osmdroid.tileprovider.modules.ArchiveFileFactory;
import org.osmdroid.tileprovider.modules.IArchiveFile;
import org.osmdroid.tileprovider.modules.MapTileFileArchiveProvider;
import org.osmdroid.tileprovider.modules.MapTileModuleProviderBase;
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ExtendedTilesOverlayFactory {
    private static final String TAG = ExtendedTilesOverlayFactory.class.getSimpleName();

    private static final String DEFAULT_TILES_FOLDER = "MapQuest";
    private static final String JPG = ".jpg";
    public static final int ZOOM_MIN = 0;
    public static final int ZOOM_MAX = 18;
    public static final int TILE_SIZE = 256;

    private Context context;
    private Handler requestCompleteHandler;

    public ExtendedTilesOverlayFactory(Context context, Handler requestCompleteHandler) {
        this.context = context;
        this.requestCompleteHandler = requestCompleteHandler;
    }

    public ExtendedTilesOverlay create(File tilesDir) {
        File zipFiles[] = tilesDir.listFiles();
        SparseArray<SparseArray<List<Integer>>> tilesHierarchy = new SparseArray<SparseArray<List<Integer>>>();
        SparseArray<List<Integer>> xyMap;
        List<Integer> yList;

        ExtendedTilesOverlay tilesOverlay = null;
        if (zipFiles != null && zipFiles.length > 0) {

            IArchiveFile[] archives = new IArchiveFile[zipFiles.length];

            for (int i = 0; i < zipFiles.length; i++) {
                archives[i] = ArchiveFileFactory.getArchiveFile(zipFiles[i]);

                try {
                    ZipFile zipFile = new ZipFile(zipFiles[i]);
                    Enumeration zipEntries = zipFile.entries();

                    while (zipEntries.hasMoreElements()) {

                        ZipEntry zipEntry = ((ZipEntry) zipEntries.nextElement());
                        String[] tokens = zipEntry.getName().split("[/|.]");

                        int zoom = Integer.parseInt(tokens[1]);
                        int x = Integer.parseInt(tokens[2]);
                        int y = Integer.parseInt(tokens[3]);

                        if (tilesHierarchy.get(zoom) != null) {
                            xyMap = tilesHierarchy.get(zoom);
                            if (xyMap.get(x) != null) {
                                yList = xyMap.get(x);
                                yList.add(y);
                            } else {
                                yList = new ArrayList<Integer>();
                                yList.add(y);
                                xyMap.put(x, yList);
                            }
                        } else {
                            xyMap = new SparseArray<List<Integer>>();
                            yList = new ArrayList<Integer>();
                            yList.add(y);
                            xyMap.put(x, yList);
                            tilesHierarchy.put(zoom, xyMap);
                        }
                    }
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage(), e);
                }
            }
            // MapQuest is the name of the folder inside the zip (so zip is chicago.zip and inside it is a folder called MapQuest)
            CustomTileSource customTiles = new CustomTileSource(DEFAULT_TILES_FOLDER, null, ZOOM_MIN, ZOOM_MAX, TILE_SIZE, JPG);

            MapTileModuleProviderBase[] providers = new MapTileModuleProviderBase[1];
            providers[0] = new MapTileFileArchiveProvider(new SimpleRegisterReceiver(context), customTiles, archives);    // this one is for local tiles (zip etc.)

            CustomTilesProvider tileProvider = new CustomTilesProvider(context, customTiles, new SimpleRegisterReceiver(context), providers);
            tileProvider.setTileRequestCompleteHandler(requestCompleteHandler);
            tileProvider.setUseDataConnection(false);
            tilesOverlay = new ExtendedTilesOverlay(tileProvider, context.getApplicationContext());
            tilesOverlay.setTilesHierarchy(tilesHierarchy);
        }

        return tilesOverlay;
    }
}
