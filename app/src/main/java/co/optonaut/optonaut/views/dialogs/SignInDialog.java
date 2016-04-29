package co.optonaut.optonaut.views.dialogs;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import co.optonaut.optonaut.R;
import co.optonaut.optonaut.views.profile.SignInActivity;

/**
 * Created by Mariel on 3/29/2016.
 */
public class SignInDialog extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.dialog_sign_in)
                .setPositiveButton(getResources().getString(R.string.dialog_sign_in_button), (dialog, which) -> {
                    //how can i call an Activity here???
                    Intent intent= new Intent(getActivity(), SignInActivity.class);
//                    intent.putextra("your_extra","your_class_value");
                    getActivity().startActivity(intent);
                }).setNegativeButton(getResources().getString(R.string.dialog_later), (dialog, which) -> {
            dismiss();
        });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
