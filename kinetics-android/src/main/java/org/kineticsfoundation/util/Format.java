package org.kineticsfoundation.util;

import android.database.Cursor;
import org.kineticsfoundation.dao.CacheContract;

/**
 * Output formatting helpers
 * Created by akaverin on 7/12/13.
 */
public class Format {

    private static final String SCORE_FORMAT = "%.2f sec";
    private static final String PST_RMS_FORMAT = "%.6f m/c<sup><small>2</small></sup>";
    private static final String PST_FORMAT = "%.6f m<sup><small>2</small></sup>/c<sup><small>5</small></sup>";

    public static String formatScoreOutput(Cursor cursor) {
        return String.format(SCORE_FORMAT, cursor.getDouble(cursor.getColumnIndex(CacheContract.Columns.SCORE)));
    }

    public static String formatScoreOutput(Double score) {
        return String.format(SCORE_FORMAT, score);
    }

    public static String formatPstMeasurement(double measure) {
        return String.format(PST_FORMAT, measure);
    }

    public static String formatPstRmsMeasurement(double measure) {
        return String.format(PST_RMS_FORMAT, measure);
    }


}
