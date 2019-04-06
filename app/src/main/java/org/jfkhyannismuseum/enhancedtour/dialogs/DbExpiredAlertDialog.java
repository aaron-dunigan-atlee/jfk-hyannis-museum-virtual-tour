package org.jfkhyannismuseum.enhancedtour.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

/* An alert dialog to inform the user that they can no longer view past exhibits.
   based on https://developer.android.com/guide/topics/ui/dialogs#AlertDialog
   and https://developer.android.com/guide/topics/ui/dialogs#PassingEvents
 */
public class DbExpiredAlertDialog extends DialogFragment {
    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    public interface DbExpiredAlertListener {
    }

    // Use this instance of the interface to deliver action events
    private DbExpiredAlertListener mListener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(org.jfkhyannismuseum.enhancedtour.R.string.db_expired_alert_message)
                .setTitle(org.jfkhyannismuseum.enhancedtour.R.string.db_expired_alert_title);
        builder.setPositiveButton(org.jfkhyannismuseum.enhancedtour.R.string.ok_db_expired_alert, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do nothing on click OK.
            }
        });
        return builder.create();

    }

    // Override the Fragment.onAttach() method to instantiate the LocationAlertListener
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the LocationAlertListener so we can send events to the host
            mListener = (DbExpiredAlertListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(context.toString()
                    + " must implement DbExpiredAlertListener");
        }
    }
}
