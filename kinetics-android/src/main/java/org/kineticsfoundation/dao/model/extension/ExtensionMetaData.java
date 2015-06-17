package org.kineticsfoundation.dao.model.extension;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.kineticsfoundation.dao.model.UniqueEntity;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;


@JsonSerialize(include = Inclusion.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExtensionMetaData implements UniqueEntity {

    private Integer id;
    private String name;
    private ExtensionType type;
    private List<String> list;
    /**
     * Bitset like mask of properties based on Enums. It should provide
     * flexibility as properties will extend
     */
    private Set<ExtensionProperty> properties;

    public ExtensionMetaData() {
    }

    public ExtensionMetaData(Integer id, String name, ExtensionType type, List<String> list) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.list = list;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ExtensionType getType() {
        return type;
    }

    public void setType(ExtensionType type) {
        this.type = type;
    }

    public Set<ExtensionProperty> getProperties() {
        return Collections.unmodifiableSet(properties);
    }

    public void setProperties(EnumSet<ExtensionProperty> properties) {
        this.properties = properties;
    }

    public List<String> getList() {
        return list;
    }

    public void setList(List<String> list) {
        this.list = list;
    }

    @Override
    public String toString() {
        return "ExtensionMetaData{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", list=" + list +
                ", properties=" + properties +
                '}';
    }
}
