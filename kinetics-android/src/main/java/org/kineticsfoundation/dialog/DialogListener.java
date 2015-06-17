package org.kineticsfoundation.dialog;

import android.os.Bundle;

/**
 * interface for Dialog listeners
 */
public interface DialogListener {
    /**
     * @param bundle - data specific to dialog call
     */
    void onDialogResult(Bundle bundle);

}
