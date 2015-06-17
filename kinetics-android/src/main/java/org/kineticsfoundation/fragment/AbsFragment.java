package org.kineticsfoundation.fragment;

import android.app.Activity;
import android.app.Fragment;
import org.kineticsfoundation.KineticsApplication;

/**
 * Base class for all {@link Fragment} implementations
 * Created by akaverin on 5/30/13.
 */
class AbsFragment extends Fragment {

    KineticsApplication application;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        application = (KineticsApplication) activity.getApplication();
    }

}
