package pub.uki.kmlmapoverlays.tile;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import org.osmdroid.tileprovider.IRegisterReceiver;
import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.MapTileProviderArray;
import org.osmdroid.tileprovider.modules.MapTileModuleProviderBase;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kmlmapoverlays.offline.maps.R;

public class CustomTilesProvider extends MapTileProviderArray {

    private static final Logger logger = LoggerFactory.getLogger(MapTileProviderArray.class);

    private Context context;
    private Drawable defaultTile;

    public CustomTilesProvider(Context ctx, ITileSource pTileSource, IRegisterReceiver aRegisterReceiver, MapTileModuleProviderBase[] pTileProviderArray) {
        super(pTileSource, aRegisterReceiver, pTileProviderArray);
        context = ctx;
    }

    @Override
    public Drawable getMapTile(MapTile pTile) {
        Drawable mapTile = super.getMapTile(pTile);
        if (mapTile == null) {
            mapTile = getDefaultTile();
        }
        return mapTile;
    }

    private Drawable getDefaultTile() {
        if (defaultTile == null) {
            try {
                final int tileSize = getTileSource() != null ? getTileSource().getTileSizePixels() : 256;
                final Bitmap bitmap = Bitmap.createBitmap(tileSize, tileSize, Bitmap.Config.ARGB_8888);
                final Canvas canvas = new Canvas(bitmap);
                canvas.drawColor(context.getResources().getColor(android.R.color.white));

                Paint paint = new Paint();
                paint.setColor(context.getResources().getColor(android.R.color.darker_gray));
                paint.setStrokeWidth(0);

                canvas.drawLine(0, 0, 0, tileSize, paint);
                canvas.drawLine(0, 0, tileSize, 0, paint);
                canvas.drawLine(tileSize, 0, tileSize, tileSize, paint);
                canvas.drawLine(0, tileSize, tileSize, tileSize, paint);

                paint.setColor(context.getResources().getColor(android.R.color.primary_text_light));
                paint.setTextSize(context.getResources().getDimensionPixelSize(R.dimen.default_map_tile_text_size));
                paint.setAntiAlias(true);

                String tileText = context.getString(R.string.default_map_tile_title);
                Rect areaRect = new Rect(0, 0, tileSize, tileSize);
                RectF bounds = new RectF(areaRect);
                // measure text width
                bounds.right = paint.measureText(tileText, 0, tileText.length());
                // measure text height
                bounds.bottom = paint.descent() - paint.ascent();

                bounds.left += (areaRect.width() - bounds.right) / 2.0f;
                bounds.top += (areaRect.height() - bounds.bottom) / 2.0f;

                canvas.drawText(tileText, bounds.left, bounds.top - paint.ascent(), paint);
                defaultTile = new BitmapDrawable(context.getResources(), bitmap);
            } catch (final OutOfMemoryError e) {
                logger.error("OutOfMemoryError getting loading tile");
                System.gc();
            }
        }
        return defaultTile;
    }
}
