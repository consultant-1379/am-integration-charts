/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 *
 *
 * The copyright to the computer program(s) herein is the property of
 *
 * Ericsson Inc. The programs may be used and/or copied only with written
 *
 * permission from Ericsson Inc. or in accordance with the terms and
 *
 * conditions stipulated in the agreement/contract under which the
 *
 * program(s) have been supplied.
 ******************************************************************************/
package com.ericsson.evnfm.acceptance.models;

import static org.apache.commons.io.FilenameUtils.removeExtension;

import static com.ericsson.evnfm.acceptance.utils.FileUtilities.getKubeConfigFromResources;

import java.util.Objects;

import org.apache.commons.io.FilenameUtils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ClusterConfig {

    private String id;

    private String name;

    private String status;

    private String description;

    private String crdNamespace;

    private boolean isDefault;

    private String localPath;

    private String namespace;

    private boolean createNamespace;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        if (name != null) {
            this.localPath = getKubeConfigFromResources(FilenameUtils.getBaseName(name)).orElse(null);
            this.name = name;
        }
    }

    public String getUIName() {
        return removeExtension(name);
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(final String status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public String getCrdNamespace() {
        return crdNamespace;
    }

    public void setCrdNamespace(String crdNamespace) {
        this.crdNamespace = crdNamespace;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        if (localPath != null) {
            this.localPath = getKubeConfigFromResources(localPath).orElse(localPath);
            this.name = FilenameUtils.getName(localPath);
        }
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public boolean isCreateNamespace(){
        return createNamespace;
    }

    public void setCreateNamespace(boolean createNamespace){
        this.createNamespace = createNamespace;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setIsDefault(final boolean aDefault) {
        isDefault = aDefault;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClusterConfig that = (ClusterConfig) o;
        return getName().equals(that.getName()) &&
                getDescription().equals(that.getDescription());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getStatus(), getDescription());
    }

    @Override
    public String toString() {
        return "ClusterConfig{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", status='" + status + '\'' +
                ", description='" + description + '\'' +
                ", crdNamespace='" + crdNamespace + '\'' +
                ", localPath='" + localPath + '\'' +
                ", isDefault='" + isDefault + '\'' +
                '}';
    }
}
