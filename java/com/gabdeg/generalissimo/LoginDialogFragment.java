package com.gabdeg.generalissimo;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class LoginDialogFragment extends DialogFragment {

    boolean invalid = false;

    public void showInvalidBanner() {
        invalid = true;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.login_dialog_fragment, null);
        if (invalid) {
            view.findViewById(R.id.header_text).setBackgroundColor(
                    getResources().getColor(android.R.color.holo_red_dark));
        }
        builder.setView(view)
                .setPositiveButton("SIGN IN", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ((MainActivity) getActivity()).onCredentialsGotten(
                                ((TextView) getDialog().findViewById(R.id.username)).getText().toString(),
                                ((TextView) getDialog().findViewById(R.id.password)).getText().toString()
                        );
                    }
                });


        return builder.create();
    }

}
