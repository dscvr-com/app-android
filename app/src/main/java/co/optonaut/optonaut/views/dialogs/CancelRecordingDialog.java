package co.optonaut.optonaut.views.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import co.optonaut.optonaut.R;
import co.optonaut.optonaut.record.RecordFragment;
import co.optonaut.optonaut.views.redesign.OverlayNavigationFragment;

/**
 * @author Nilan Marktanner
 * @date 2016-02-12
 */
public class CancelRecordingDialog extends DialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.dialog_cancel_recording)
                .setPositiveButton(getResources().getString(R.string.dialog_cancel), (dialog, which) -> {
                    if (getTargetFragment() instanceof OverlayNavigationFragment) {
                        ((OverlayNavigationFragment) getTargetFragment()).cancel();
                    }
                }).setNegativeButton(getResources().getString(R.string.dialog_dont_cancel), (dialog, which) -> {
                    dismiss();
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
