package org.kineticsfoundation.widget;

import android.os.Bundle;

/**
 * Handy builder pattern
 * Created by akaverin on 6/17/13.
 */
public class WidgetBuilder {

    private final Bundle properties;

    private WidgetBuilder() {
        this.properties = new Bundle();
        properties.putBoolean(WidgetProperty.EDIT_MODE.name(), false);
    }

    public WidgetBuilder setType(WidgetType type) {
        properties.putSerializable(WidgetProperty.TYPE.name(), type);
        return this;
    }

    public WidgetBuilder setName(String name) {
        properties.putString(WidgetProperty.NAME.name(), name);
        return this;
    }

    public WidgetBuilder setValue(String value) {
        properties.putString(WidgetProperty.VALUE.name(), value);
        return this;
    }

    public WidgetBuilder setValue(Boolean value) {
        properties.putBoolean(WidgetProperty.VALUE.name(), value);
        return this;
    }

    public WidgetBuilder setRequired(boolean isRequired) {
        properties.putBoolean(WidgetProperty.REQUIRED.name(), isRequired);
        return this;
    }

    public WidgetBuilder setExtra(Bundle extra) {
        properties.putBundle(WidgetProperty.EXTRA.name(), extra);
        return this;
    }

    public WidgetBuilder setMode(boolean isEdit) {
        properties.putBoolean(WidgetProperty.EDIT_MODE.name(), isEdit);
        return this;
    }

    public WidgetBuilder setExtension() {
        properties.putBoolean(WidgetProperty.EXTENSION.name(), true);
        return this;
    }

    public Bundle build() {
        return properties;
    }

    public static WidgetBuilder builder() {
        return new WidgetBuilder();
    }

}
