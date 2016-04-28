package pub.uki.kmlmapoverlays.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import kmlmapoverlays.offline.maps.R;

public class MockLocationDisabledDialogFragment extends DialogFragment {

    public static MockLocationDisabledDialogFragment newInstance() {
        return new MockLocationDisabledDialogFragment();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity()).setTitle(R.string.warning)
                .setMessage(getString(R.string.mock_location_disabled_notice))
                .setCancelable(true).setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS);
                        startActivity(intent);
                    }
                }).setNegativeButton(getString(android.R.string.cancel), null).create();
    }

}
