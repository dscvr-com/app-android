package co.optonaut.optonaut.views.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import co.optonaut.optonaut.R;

/**
 * @author Nilan Marktanner
 * @date 2016-02-02
 */
public class VRModeExplanationDialog extends DialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.dialog_vrmode_explanation)
                .setCancelable(false);
        // Create the AlertDialog object and return it
        return builder.create();
    }
}