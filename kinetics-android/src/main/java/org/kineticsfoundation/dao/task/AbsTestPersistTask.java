package org.kineticsfoundation.dao.task;

import android.app.Application;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import org.kineticsfoundation.KineticsApplication;
import org.kineticsfoundation.dao.CacheContract;
import org.kineticsfoundation.dao.provider.ContentProviderHelper;
import org.kineticsfoundation.sync.synchronizers.SyncGroup;
import org.kineticsfoundation.test.TestModel;
import org.kineticsfoundation.widget.WidgetProperty;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static org.kineticsfoundation.test.TestModel.Attr.IS_CHECKED;
import static org.kineticsfoundation.test.TestModel.Attr.NOTE;
import static org.kineticsfoundation.widget.WidgetProperty.EXTENSION;
import static org.kineticsfoundation.widget.WidgetProperty.VALUE;

/**
 * Task for Test background persisting
 * Created by akaverin on 7/12/13.
 */
public abstract class AbsTestPersistTask extends AsyncTask<Void, Void, Void> {

    protected final TestModel testModel;
    private final KineticsApplication application;

    public AbsTestPersistTask(TestModel testModel, Application application) {
        this.testModel = testModel;
        this.application = (KineticsApplication) application;
    }

    @Override
    protected Void doInBackground(Void... params) {
        Map<String, Bundle> attrs = testModel.getWidgetAttrs();

        ContentValues testValues = new ContentValues();

        testValues.put(CacheContract.Columns.TYPE, getTestType());
        testValues.put(CacheContract.Columns.VALID, attrs.get(IS_CHECKED.name()).getBoolean(VALUE.name()));
        testValues.put(CacheContract.Columns.SCORE, buildScore());
        testValues.put(CacheContract.Columns.CREATION, new Date().getTime());
        testValues.put(CacheContract.Columns.NOTES, attrs.get(NOTE.name()).getString(VALUE.name()));
        testValues.put(CacheContract.Columns.RAW_DATA, buildRawData());
        testValues.put(CacheContract.Columns.SYNC, CacheContract.Sync.CREATED.ordinal());

        ContentProviderHelper providerHelper = new ContentProviderHelper(application.getContentResolver());
        final int testSessionId = providerHelper.insert(CacheContract.Tables.TEST_SESSION, testValues, true);

        //iterate over model to get extensions -> and build ContentValues for each and do bulk insert
        ArrayList<ContentValues> extensionValues = newArrayList();
        for (Bundle modelBundle : attrs.values()) {
            if (!modelBundle.getBoolean(EXTENSION.name())) {
                continue;
            }
            String extValue = modelBundle.getString(VALUE.name());
            if (TextUtils.isEmpty(extValue)) {
                continue;
            }
            ContentValues values = new ContentValues();
            values.put(CacheContract.Columns.TEST_SESSION_ID, testSessionId);
            values.put(CacheContract.Columns.NAME, modelBundle.getString(WidgetProperty.NAME.name()));
            values.put(CacheContract.Columns.VALUE, extValue);
            values.put(CacheContract.Columns.META_ID, modelBundle.getBundle(WidgetProperty.EXTRA.name()).getInt
                    (CacheContract.Columns.ID));

            extensionValues.add(values);
        }
        if (extensionValues.isEmpty()) {
            return null;
        }
        providerHelper.insert(CacheContract.Tables.EXT_DATA, extensionValues.toArray(new
                ContentValues[extensionValues.size()])
                , false);

        //performing FORCED UPLOAD only sync request
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_UPLOAD, true);

        application.getSyncUtils().requestSyncForData(SyncGroup.TEST.name(), bundle, true);

        return null;
    }

    protected abstract String getTestType();

    protected abstract String buildRawData();

    protected abstract Double buildScore();

    protected static String getDeviceInfo() {
        return Build.MANUFACTURER + " " + Build.MODEL + " Android OS: " + Build.VERSION.RELEASE + " (API: " + Build
                .VERSION.SDK_INT + ")";
    }
}
