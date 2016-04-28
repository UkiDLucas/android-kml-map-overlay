package pub.uki.kmlmapoverlays.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import kmlmapoverlays.offline.maps.R;

public class TilesNotFoundDialogFragment extends DialogFragment {
    private static final String PATH_TO_TILES_DIR = "PATH_TO_TILES_DIR";

    public static TilesNotFoundDialogFragment newInstance(String pathToTilesDir) {
        Bundle bundle = new Bundle();
        bundle.putString(PATH_TO_TILES_DIR, pathToTilesDir);
        TilesNotFoundDialogFragment fragment = new TilesNotFoundDialogFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        String pathToTilesDir = args.getString(PATH_TO_TILES_DIR);

        return new AlertDialog.Builder(getActivity()).setTitle(R.string.warning)
                .setMessage(getString(R.string.tiles_not_found_message, pathToTilesDir))
                .setCancelable(true).setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create();
    }
}
