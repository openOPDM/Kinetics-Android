package org.kineticsfoundation.dao.model.extension;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * Values added to specific {@link ExtendedEntity} instance. Description of this
 * extension can be found in {@link ExtensionMetaData}.
 *
 * @author akaverin
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExtensionData {

    private String value;
    private String name;
    private Integer metaId;

    public ExtensionData() {
    }

    public ExtensionData(String name, String value, Integer metaId) {
        this.name = name;
        this.value = value;
        this.metaId = metaId;
    }

    public Integer getMetaId() {
        return metaId;
    }

    public void setMetaId(Integer metaId) {
        this.metaId = metaId;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "ExtensionData{" +
                "value='" + value + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
