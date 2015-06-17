package org.kineticsfoundation.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import com.lohika.sync.account.AccountManager;
import com.lohika.sync.core.exception.SynAuthErrorException;

/**
 * Helper utils for network
 * Created by akaverin on 5/30/13.
 */
public final class NetworkUtils {

    private NetworkUtils() {
    }

    public static boolean isNetworkAvailable(Context ctx) {
        ConnectivityManager cm = (ConnectivityManager) ctx
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo currentNetworkInfo = cm.getActiveNetworkInfo();
        return currentNetworkInfo != null && currentNetworkInfo.isConnected();
    }

    /**
     * @param accountManager of our application
     * @return new or cached session token
     * @throws SynAuthErrorException if failed to obtain token
     */
    public static String getSessionToken(AccountManager accountManager) {
        String token = accountManager.getBlockingAuthToken(true);
        if (TextUtils.isEmpty(token)) {
            throw new SynAuthErrorException("Failed to obtain new token");
        }
        return token;
    }
}
