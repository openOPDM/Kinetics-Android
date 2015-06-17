package org.kineticsfoundation.dao;

import android.provider.BaseColumns;

/**
 * All our DB related fields and constants will be here
 * Created with IntelliJ IDEA.
 * User: akaverin
 * Date: 5/24/13
 * Time: 11:52 AM
 */
public interface CacheContract {

    /**
     * Sync status of synchronized entities. Do no change order.
     */
    public enum Sync {
        NO_SYNC, CREATED, MODIFIED, DELETED
    }

    /**
     * All tables in our DAO layer
     */
    public interface Tables {
        String USER = "user";
        String TEST_SESSION = "test_session";
        String EXT_DATA = "ext_data";
        String EXT_METADATA = "ext_metadata";
        String EXT_LIST_NODE = "ext_list_node";
    }

    /**
     * Entities columns
     */
    public interface Columns extends BaseColumns {
        String ID = "id";
        String EXT_METADATA_ID = "ext_metadata_id";
        String TEST_SESSION_ID = "test_session_id";
        String EMAIL = "email";
        String FIRST_NAME = "first_name";
        String SECOND_NAME = "second_name";
        String UID = "uid";
        String BIRTHDAY = "birthday";
        String GENDER = "gender";
        String TYPE = "type";
        String SCORE = "score";
        String RAW_DATA = "raw_data";
        String NOTES = "notes";
        String VALID = "valid";
        String CREATION = "creation";
        String NAME = "name";
        String VALUE = "value";
        String META_ID = "metaId";
        String PROPS = "props";
        String LABEL = "label";
        String SYNC = "sync";
    }

    /**
     * Projections and Joined tables
     */
    public interface VirtualTables {
        String EXT_METADATA_COMBINED = "ext_data_combined";
        String TEST_SESSION_DETAILS = "test_session_details";
    }


}
