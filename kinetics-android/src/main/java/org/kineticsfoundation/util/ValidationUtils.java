package org.kineticsfoundation.util;

import android.content.Context;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.TextView;
import com.google.common.base.Preconditions;
import org.kineticsfoundation.R;

/**
 * Helper logic for Input fields validation
 * Created by akaverin on 5/31/13.
 */
public final class ValidationUtils {

    private ValidationUtils() {
    }

    private static boolean validate(ValidationType type,
                                    String inputString) {
        Preconditions.checkNotNull(type);
        if (TextUtils.isEmpty(inputString)) {
            return false;
        }
        switch (type) {
//            case PASSWORD:
//                return Constants.PASS_LENGTH_MIN <= inputString.length();
            case EMAIL:
                return Patterns.EMAIL_ADDRESS.matcher(inputString).matches();

            default:
                return true;
        }
    }

    public static <T extends TextView> boolean isValid(Context ctx, ValidationType type, T view) {
        if (validate(type, view.getText().toString())) {
            return true;
        }
        switch (type) {
            case EMAIL:
                view.setError(ctx.getString(R.string.wrong_email_error_label_text));
                break;
            case TEXT:
            case PASSWORD:
                view.setError(ctx.getString(R.string.empty_field_error_label_text));
                break;
            case CONFIRMATION_CODE:
                view.setError(ctx.getString(R.string.empty_field_error_label_text));
                break;
        }
        view.requestFocus();
        return false;
    }


    public enum ValidationType {
        PASSWORD, EMAIL, TEXT, CONFIRMATION_CODE
    }
}
