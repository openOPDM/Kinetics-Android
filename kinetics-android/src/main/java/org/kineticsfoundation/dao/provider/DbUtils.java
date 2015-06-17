package org.kineticsfoundation.dao.provider;

import org.springframework.util.StringUtils;

import java.util.Arrays;

/**
 * Created with IntelliJ IDEA.
 * User: akaverin
 * Date: 5/7/13
 * Time: 9:40 AM
 */
class DbUtils {

    private static final char DOT = '.';
    private static final char EQUAL = '=';

    private DbUtils() {
    }

    static String joinFrom(String... tableAndJoins) {
        return StringUtils.collectionToDelimitedString(Arrays.asList(tableAndJoins), " ");
    }

    static String leftJoin(String joinTable, String parentField, String joinField) {
        return " LEFT OUTER JOIN " + joinTable + " ON " + parentField + EQUAL + joinField;
    }

    static String select(String table, String column) {
        return table + DOT + column;
    }

}
