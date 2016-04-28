package pub.uki.kmlmapoverlays.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;


public class LocationNotSelectedDialogFragment extends DialogFragment {

    public static LocationNotSelectedDialogFragment newInstance() {
        return new LocationNotSelectedDialogFragment();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity()).setTitle(R.string.warning)
                .setMessage(getString(R.string.location_not_selected_message))
                .setCancelable(true).setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create();
    }
}
