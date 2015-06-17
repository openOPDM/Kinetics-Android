package org.kineticsfoundation.dao.model;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.kineticsfoundation.dao.model.extension.ExtensionData;

import java.util.Date;
import java.util.List;

/**
 * Entity representing Test data
 *
 * @author akaverin
 */
@JsonSerialize(include = Inclusion.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TestSession implements UniqueEntity {

    private Integer cacheId;
    private Integer id;
    private Date creationDate;
    private String type;
    private String rawData;
    private Double score;
    private Boolean isValid = false;
    private String notes;
    private List<ExtensionData> extension;

    public TestSession() {
    }

    public TestSession(int id, double score, String rawData, String type) {
        this.id = id;
        this.score = score;
        this.rawData = rawData;
        this.type = type;
        this.creationDate = new Date();
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRawData() {
        return rawData;
    }

    public void setRawData(String rawData) {
        this.rawData = rawData;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public Boolean getIsValid() {
        return isValid;
    }

    public void setIsValid(Boolean isValid) {
        this.isValid = isValid;
    }

    @Override
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<ExtensionData> getExtension() {
        return extension;
    }

    public void setExtension(List<ExtensionData> extensionData) {
        this.extension = extensionData;
    }

    @JsonIgnore
    public Integer getCacheId() {
        return cacheId;
    }

    @JsonIgnore
    public void setCacheId(Integer cacheId) {
        this.cacheId = cacheId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        TestSession other = (TestSession) obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "TestSession{" +
                "extension=" + extension +
                ", id=" + id +
                ", creationDate=" + creationDate +
                ", type='" + type + '\'' +
                ", rawData='" + rawData + '\'' +
                ", score=" + score +
                ", isValid=" + isValid +
                ", notes='" + notes + '\'' +
                '}';
    }
}
