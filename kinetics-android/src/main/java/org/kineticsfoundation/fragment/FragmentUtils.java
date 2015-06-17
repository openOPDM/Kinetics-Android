package org.kineticsfoundation.fragment;

import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.widget.ArrayAdapter;
import org.kineticsfoundation.dao.model.Project;

import java.util.List;

/**
 * Shared logic
 * Created by akaverin on 5/31/13.
 */
public final class FragmentUtils {

    private FragmentUtils() {
    }

    static ArrayAdapter<Project> getCustomersAdapter(Context context, List<Project> projects) {
        return new ArrayAdapter<Project>(context, android.R.layout.simple_spinner_item,
                projects.toArray(new Project[projects.size()]));
    }

    public static void showDialog(Fragment hostFragment, DialogFragment dialog) {
        showDialog(hostFragment, dialog, false);
    }

    public static void showDialog(Fragment hostFragment, DialogFragment dialog, boolean skipIfPresent) {
        FragmentTransaction ft = hostFragment.getFragmentManager().beginTransaction();
        String tag = dialog.getClass().getSimpleName();
        Fragment prev = hostFragment.getFragmentManager().findFragmentByTag(tag);
        if (prev != null) {
            if (skipIfPresent) {
                return;
            }
            ft.remove(prev);
        }
        dialog.show(ft, tag);
    }
}
