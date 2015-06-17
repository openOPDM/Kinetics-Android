package org.kineticsfoundation.dao.model.extension;

/**
 * Values of this enum will be persisted in {@link ExtensionMetaData}. So, do
 * not change order or drop any values
 *
 * @author akaverin
 */
public enum ExtensionProperty {

    //2nd not used, but required for UT
    REQUIRED(0x1), SHOW_IN_GRID(0x2);
    private final int mask;

    ExtensionProperty(int mask) {
        this.mask = mask;
    }

    public int getMask() {
        return mask;
    }

    public static boolean isRequired(int props) {
        return (props & REQUIRED.getMask()) != 0;
    }
}
