package org.kineticsfoundation.widget;

/**
 * Available widget types
 * Created by akaverin on 6/7/13.
 */
public enum WidgetType {

    TEXT, NUMBER, NOTE, CHECKBOX, LIST, SEPARATOR;
    private static final int count = WidgetType.values().length;

    public static int size() {
        return count;
    }

}
