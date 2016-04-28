package co.optonaut.optonaut.views.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import co.optonaut.optonaut.R;
import co.optonaut.optonaut.util.Constants;
import co.optonaut.optonaut.views.OverlayNavigationFragment;

/**
 * @author Nilan Marktanner
 * @date 2016-02-12
 */
public class ChooseRecordingModeDialog extends DialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.dialog_choose_recording_mode)
                .setPositiveButton(getResources().getString(R.string.dialog_choose_recording_mode_1), (dialog, which) -> {
                    if (getTargetFragment() instanceof OverlayNavigationFragment) {
                        ((OverlayNavigationFragment) getTargetFragment()).switchToPreviewRecordMode(Constants.MODE_CENTER);
                    }
                }).setNegativeButton(getResources().getString(R.string.dialog_choose_recording_mode_3), (dialog, which) -> {
                        ((OverlayNavigationFragment) getTargetFragment()).switchToPreviewRecordMode(Constants.MODE_TRUNCATED);
                });
        builder.setCancelable(false);
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
