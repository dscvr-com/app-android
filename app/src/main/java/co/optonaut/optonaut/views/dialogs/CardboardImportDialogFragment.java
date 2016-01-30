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
import co.optonaut.optonaut.util.ResultCodes;

/**
 * @author Nilan Marktanner
 * @date 2016-01-30
 */
public class CardboardImportDialogFragment extends DialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.dialog_import_cardboard_image)
                .setPositiveButton(R.string.fire, (dialog, id) -> {
                    dismiss();
                    Intent galleryIntent = new Intent(
                            Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    getActivity().startActivityForResult(galleryIntent , ResultCodes.RESULT_GALLERY);
                })
                .setNegativeButton(R.string.cancel, (dialog, id) -> {

                });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
