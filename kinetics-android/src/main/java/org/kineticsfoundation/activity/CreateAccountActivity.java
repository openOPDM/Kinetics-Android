package org.kineticsfoundation.activity;

import android.os.Bundle;
import org.kineticsfoundation.R;

/**
 * Create Account logic container
 * Created by akaverin on 5/31/13.
 */
public class CreateAccountActivity extends AbsActivity {

    public static final int CREATE_CODE = 1;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_account_activity);
    }

}