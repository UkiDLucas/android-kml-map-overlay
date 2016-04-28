package pub.uki.kmlmapoverlays.tile;

import android.os.AsyncTask;

import java.io.File;

public abstract class CheckTilesExistenceTask extends AsyncTask<File, Void, Boolean> {
    @Override
    protected Boolean doInBackground(File... params) {
        final File tilesDir = params[0];

        if (tilesDir != null && tilesDir.exists()) {
            File files[] = tilesDir.listFiles();

            if (files != null) {
                for (File file : files) {
                    // if at least single archive with tiles exists we consider that we have tiles
                    if (file.exists()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
