/*******************************************************************************
 * Copyright (c) 2012-2014 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package com.codenvy.ide.maven.tools;

import com.codenvy.commons.xml.Element;
import com.codenvy.commons.xml.NewElement;

import java.util.HashMap;
import java.util.Map;

import static com.codenvy.commons.xml.NewElement.createElement;
import static com.codenvy.commons.xml.XMLTreeLocation.after;
import static com.codenvy.commons.xml.XMLTreeLocation.inTheBegin;
import static java.util.Collections.emptyMap;
import static java.util.Objects.requireNonNull;

//TODO
public class Plugin {

    private String              artifactId;
    private String              groupId;
    private Map<String, String> configuration;

    Element pluginElement;

    Plugin(Element element) {
        pluginElement = element;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getGroupId() {
        return groupId;
    }

    public Map<String, String> getConfiguration() {
        if (configuration == null) {
            return emptyMap();
        }
        return new HashMap<>(configuration);
    }

    public Plugin setArtifactId(String artifactId) {
        this.artifactId = artifactId;
        if (!isNew()) {
            if (artifactId == null) {
                pluginElement.removeChild("artifactId");
            } else if (pluginElement.hasSingleChild("artifactId")) {
                pluginElement.getSingleChild("artifactId").setText(artifactId);
            } else {
                pluginElement.insertChild(createElement("artifactId", artifactId), after("groupId").or(inTheBegin()));
            }
        }
        return this;
    }

    public Plugin setGroupId(String groupId) {
        this.groupId = groupId;
        if (!isNew()) {
            if (groupId == null) {
                pluginElement.removeChild("groupId");
            } else if (pluginElement.hasSingleChild("groupId")) {
                pluginElement.getSingleChild("groupId").setText(groupId);
            } else {
                pluginElement.insertChild(createElement("groupId", groupId), inTheBegin());
            }
        }
        return this;
    }

    public Plugin setConfiguration(Map<String, String> configuration) {
        if (configuration == null || configuration.isEmpty()) {
            removeConfiguration();
        } else {
            setConfiguration0(configuration);
        }
        return this;
    }

    private void setConfiguration0(Map<String, String> configuration) {
        this.configuration = new HashMap<>(configuration);

        if (isNew()) return;

        if (pluginElement.hasSingleChild("configuration")) {
            final Element confElement = pluginElement.getSingleChild("configuration");
            //remove all configuration properties from element
            for (Element property : confElement.getChildren()) {
                property.remove();
            }
            //append each new property to "configuration" element
            for (Map.Entry<String, String> property : configuration.entrySet()) {
                confElement.appendChild(createElement(property.getKey(), property.getValue()));
            }
        } else {
            final NewElement newConfiguration = createElement("configuration");
            for (Map.Entry<String, String> entry : configuration.entrySet()) {
                newConfiguration.appendChild(createElement(entry.getKey(), entry.getValue()));
            }
            //insert new configuration to xml
            pluginElement.appendChild(newConfiguration);
        }
    }

    private void removeConfiguration() {
        if (!isNew()) {
            pluginElement.removeChild("properties");
        }
        configuration = null;
    }

    public Plugin setConfigProperty(String name, String value) {
        requireNonNull(name, "Configuration property name should not be null");
        requireNonNull(value, "Configuration property value should not be null");
        if (!isNew()) {
            addConfigPropertyToXML(name, value);
        }
        configuration().put(name, value);
        return this;
    }

    public Plugin removeConfigProperty(String name) {
        requireNonNull(name, "Configuration property name should ne null");
        if (configuration().remove(name) != null && !isNew()) {
            removeConfigPropertyFromXML(name);
        }
        return this;
    }

    private void removeConfigPropertyFromXML(String name) {
        if (configuration.isEmpty()) {
            pluginElement.removeChild("configuration");
        } else {
            pluginElement.getSingleChild("configuration").removeChild(name);
        }
    }

    private void addConfigPropertyToXML(String name, String value) {
        if (configuration().containsKey(name)) {
            pluginElement.getSingleChild("configuration")
                         .getSingleChild(name)
                         .setText(value);
        } else if (configuration.isEmpty()) {
            pluginElement.appendChild(createElement("configuration", createElement(name, value)));
        } else {
            pluginElement.getSingleChild("configuration").appendChild(createElement(name, value));
        }
    }

    public String getId() {
        return groupId + ':' + artifactId;
    }

    @Override
    public String toString() {
        return getId();
    }

    private Map<String, String> configuration() {
        return configuration == null ? configuration = new HashMap<>() : configuration;
    }

    private boolean isNew() {
        return pluginElement == null;
    }
}
