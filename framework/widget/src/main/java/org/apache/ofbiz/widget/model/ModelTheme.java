/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * dedicate language governing permissions and limitations
 * under the License.
 *******************************************************************************/
package org.apache.ofbiz.widget.model;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.ofbiz.base.util.Debug;
import org.apache.ofbiz.base.util.GeneralException;
import org.apache.ofbiz.base.util.ObjectType;
import org.apache.ofbiz.base.util.UtilMisc;
import org.apache.ofbiz.base.util.UtilValidate;
import org.apache.ofbiz.base.util.UtilXml;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * Widget Theme Library - Widget model class.
 */
@SuppressWarnings("serial")
public class ModelTheme {

    public static final String module = ModelTheme.class.getName();
    /**
     * The parameter name used to control widget boundary comments. Currently
     * set to "widgetVerbose".
     */
    public static final String enableBoundaryCommentsParam = "widgetVerbose";

    private ModelTheme originTheme = null;
    //generic properties
    private String name;
    private List<String> visualThemeIds = null;
    private Integer defaultViewSize = null;

    // Autocomplete configuration
    // Default number of items to be displayed in lookup ajax autocompleter
    private Integer autocompleterDefaultViewSize = null;
    // Default minimum number of characters an user has to type before the ajax autocompleter activates (jQuery default is 1)
    private Integer autocompleterDefaultMinLength = null;
    // Default delay in milliseconds the Autocomplete waits after a keystroke to activate itself. A zero-delay makes sense for local data (more responsive), but can produce a lot of load for remote data, while being less responsive.
    private Integer autocompleterDefaultDelay = null;
    // Show/hide the ID field that is returned from ajax autocompleter
    private Boolean autocompleterDisplayReturnField = null;

    //layer modal
    // Default position and size for lookup layered windows
    private String lookupPosition = null;
    private Integer lookupWidth = null;
    private Integer lookupHeight = null;
    //Default size for layered modal windows
    private Integer linkDefaultLayeredModalWidth = null;
    private Integer linkDefaultLayeredModalHeight = null;

    //dedicate theme properties
    private Map<String, Object> dedicateProperties;

    //template rendering
    private Map<String, ModelTemplate> modelTemplateMap;

    //** XML Constructor */
    public ModelTheme(Element themeElement) {
        this.name = themeElement.getAttribute("name");
        for (Element childElement : UtilXml.childElementList(themeElement)) {
            switch (childElement.getNodeName()) {
                case "default-view-size":
                    this.defaultViewSize = Integer.valueOf(childElement.getAttribute("value"));
                    break;
                case "autocompleter":
                    this.autocompleterDefaultDelay = Integer.valueOf(childElement.getAttribute("default-delay"));
                    this.autocompleterDefaultMinLength = Integer.valueOf(childElement.getAttribute("default-min-lenght"));
                    this.autocompleterDefaultViewSize = Integer.valueOf(childElement.getAttribute("default-view-size"));
                    this.autocompleterDisplayReturnField = "true".equalsIgnoreCase(childElement.getAttribute("display-return-field"));
                    break;
                case "lookup":
                    this.lookupPosition = childElement.getAttribute("position");
                    this.lookupHeight = Integer.valueOf(childElement.getAttribute("height"));
                    this.lookupWidth = Integer.valueOf(childElement.getAttribute("width"));
                    break;
                case "layered-modal":
                    this.linkDefaultLayeredModalHeight = Integer.valueOf(childElement.getAttribute("height"));
                    this.linkDefaultLayeredModalWidth = Integer.valueOf(childElement.getAttribute("width"));
                    break;
                case "visual-themes":
                    List<String> visualThemeIds = new LinkedList<>();
                    for (Element visualTheme : UtilXml.childElementList(childElement)) {
                        visualThemeIds.add(visualTheme.getAttribute("id"));
                    }
                    this.visualThemeIds = Collections.unmodifiableList(visualThemeIds);
                case "properties":
                    for (Element property : UtilXml.childElementList(childElement)) {
                        addDedicateProperty(property);
                    }
                    break;
                case "templates":
                    for (Element template : UtilXml.childElementList(childElement)) {
                        addTemplate(template);
                    }
                    break;
                case "extend":
                    try {
                        this.originTheme = ThemeFactory.getModelThemeFromLocation(childElement.getAttribute("location"));
                    } catch (IOException | ParserConfigurationException | SAXException e) {
                        Debug.logError("Impossible to read the origin theme at location " + childElement.getAttribute("location"), module);
                    }
                    break;
            }
        }
        if (this.originTheme != null) {
            extendFromOrigin();
        }
        if (dedicateProperties != null) dedicateProperties = Collections.unmodifiableMap(dedicateProperties);
        if (modelTemplateMap != null) modelTemplateMap = Collections.unmodifiableMap(modelTemplateMap);
    }

    public String getName() {
        return name;
    }

    public List<String> getVisualThemeIds() {
        if (visualThemeIds == null) return new ArrayList<>();
        return visualThemeIds;
    }
    public boolean hasVisualThemeId(String visualThemeId) {
        return visualThemeIds.contains(visualThemeId);
    }

    public Integer getDefaultViewSize() {
        return defaultViewSize;
    }

    public Integer getAutocompleterDefaultViewSize() {
        return autocompleterDefaultViewSize;
    }

    public Integer getAutocompleterDefaultMinLength() {
        return autocompleterDefaultMinLength;
    }

    public Integer getLinkDefaultLayeredModalHeight() {
        return linkDefaultLayeredModalHeight;
    }

    public Integer getLinkDefaultLayeredModalWidth() {
        return linkDefaultLayeredModalWidth;
    }

    public Integer getLookupHeight() {
        return lookupHeight;
    }

    public Integer getLookupWidth() {
        return lookupWidth;
    }

    public String getLookupPosition() {
        return lookupPosition;
    }

    public Boolean getAutocompleterDisplayReturnField() {
        return autocompleterDisplayReturnField;
    }

    public Integer getAutocompleterDefaultDelay() {
        return autocompleterDefaultDelay;
    }

    /**
     * */
    private void extendFromOrigin() {
        if (this.defaultViewSize == null) this.defaultViewSize = originTheme.defaultViewSize;
        if (this.autocompleterDefaultViewSize == null) this.autocompleterDefaultViewSize = originTheme.autocompleterDefaultViewSize;
        if (this.autocompleterDefaultMinLength == null) this.autocompleterDefaultMinLength = originTheme.autocompleterDefaultMinLength;
        if (this.autocompleterDefaultDelay == null) this.autocompleterDefaultDelay = originTheme.autocompleterDefaultDelay;
        if (this.autocompleterDisplayReturnField == null) this.autocompleterDisplayReturnField = originTheme.autocompleterDisplayReturnField;
        if (this.lookupPosition == null) this.lookupPosition = originTheme.lookupPosition;
        if (this.lookupWidth == null) this.lookupWidth = originTheme.lookupWidth;
        if (this.lookupHeight == null) this.lookupHeight = originTheme.lookupHeight;
        if (this.linkDefaultLayeredModalWidth == null) this.linkDefaultLayeredModalWidth = originTheme.linkDefaultLayeredModalWidth;
        if (this.linkDefaultLayeredModalHeight == null) this.linkDefaultLayeredModalHeight = originTheme.linkDefaultLayeredModalHeight;

        // resolve all decicate properties from origin and sucharge by the present dedicate properties
        if (UtilValidate.isNotEmpty(originTheme.dedicateProperties)) {
            Map<String, Object> dedicatePropertiesTmp = new HashMap<>(originTheme.dedicateProperties);
            if (this.dedicateProperties != null) dedicatePropertiesTmp.putAll(this.dedicateProperties);
            this.dedicateProperties = dedicatePropertiesTmp;
        }

        // Add modelTemplate present on origin and not on this
        if (UtilValidate.isNotEmpty(originTheme.modelTemplateMap)) {
            if (UtilValidate.isNotEmpty(this.modelTemplateMap)) {
                for (String modelTemplateName : originTheme.modelTemplateMap.keySet()) {
                    if (! this.modelTemplateMap.containsKey(modelTemplateName)) {
                        this.modelTemplateMap.put(modelTemplateName, originTheme.modelTemplateMap.get(modelTemplateName));
                    }
                }
            } else {
                this.modelTemplateMap = originTheme.modelTemplateMap;
            }
        }
    }

    private void addDedicateProperty(Element property) {
        if (dedicateProperties == null) dedicateProperties = new LinkedHashMap<>();
        String name = property.getAttribute("name");
        String value = property.getAttribute(("value"));
        String type = property.getAttribute("type");
        if (type == null || "String".equals(type) || "java.lang.String".equals(type)) {
            dedicateProperties.put(name, value);
        } else {
            try {
                dedicateProperties.put(name, ObjectType.simpleTypeConvert(value, type, null, null));
            } catch (GeneralException e) {
                Debug.logError("Impossible to parse the value " + value + " to type " + type + " for the property " + name + " on theme " + this.name, module);
            }
        }
    }
    public Object getProperty(String propertyName) {
        if (! dedicateProperties.containsKey(propertyName)
                || dedicateProperties.get(propertyName) == null) return "";
        return dedicateProperties.get(propertyName);
    }

    private void addTemplate(Element template) {
        if (modelTemplateMap == null) modelTemplateMap = new LinkedHashMap<>();
        modelTemplateMap.put(template.getAttribute("name"), new ModelTemplate(template));
    }

    public String getType(String name) {
        ModelTemplate modelTemplate = modelTemplateMap.get(name);
        if (modelTemplate != null) return modelTemplate.getEncoder();
        return null;
    }
    public String getEncoder(String name) {
        ModelTemplate modelTemplate = modelTemplateMap.get(name);
        if (modelTemplate != null) return modelTemplate.getEncoder();
        return null;
    }
    public String getCompress(String name) {
        ModelTemplate modelTemplate = modelTemplateMap.get(name);
        if (modelTemplate != null) return modelTemplate.getCompress();
        return null;
    }
    public String getContentType(String name) {
        ModelTemplate modelTemplate = modelTemplateMap.get(name);
        if (modelTemplate != null) return modelTemplate.getContentType();
        return null;
    }
    public String getEncoding(String name) {
        ModelTemplate modelTemplate = modelTemplateMap.get(name);
        if (modelTemplate != null) return modelTemplate.getEncoding();
        return null;
    }

    public String getScreenRendererLocation(String name) {
        ModelTemplate modelTemplate = modelTemplateMap.get(name);
        if (modelTemplate != null) return modelTemplate.getScreenRendererLocation();
        return null;
    }
    public String getFormRendererLocation(String name) {
        ModelTemplate modelTemplate = modelTemplateMap.get(name);
        if (modelTemplate != null) return modelTemplate.getFormRendererLocation();
        return null;
    }
    public String getTreeRendererLocation(String name) {
        ModelTemplate modelTemplate = modelTemplateMap.get(name);
        if (modelTemplate != null) return modelTemplate.getTreeRendererLocation();
        return null;
    }
    public String getMenuRendererLocation(String name) {
        ModelTemplate modelTemplate = modelTemplateMap.get(name);
        if (modelTemplate != null) return modelTemplate.getMenuRendererLocation();
        return null;
    }

    /**
     *
     */
    private class ModelTemplate {
        private String name = null;
        private String type = null;
        private String compress = null;
        private String encoder = null;
        private String contentType = null;
        private String encoding = null;
        private String screenRendererLocation = null;
        private String formRendererLocation = null;
        private String menuRendererLocation = null;
        private String treeRendererLocation = null;

        public ModelTemplate(Element template) {
            this.name = template.getAttribute("name");
            this.type = template.getAttribute("type");
            this.compress = template.getAttribute("compress");
            this.encoder = template.getAttribute("encoder");
            this.contentType = template.getAttribute("contentType");
            this.encoding = template.getAttribute("encoding");
            for (Element templateFile : UtilXml.childElementList(template)) {
                switch (templateFile.getAttribute("widget")) {
                    case "screen":
                        screenRendererLocation = templateFile.getAttribute("location");
                        break;
                    case "form":
                        formRendererLocation = templateFile.getAttribute("location");
                        break;
                    case "tree":
                        treeRendererLocation = templateFile.getAttribute("location");
                        break;
                    case "menu":
                        menuRendererLocation = templateFile.getAttribute("location");
                        break;
                }
            }
        }
        public String getName() {
            return name;
        }
        public String getEncoder() {
            return encoder;
        }
        public String getCompress() {
            return compress;
        }
        public String getContentType() {
            return contentType;
        }
        public String getEncoding() {
            return encoding;
        }

        public String getScreenRendererLocation() {
            return screenRendererLocation;
        }
        public String getFormRendererLocation() {
            return formRendererLocation;
        }
        public String getTreeRendererLocation() {
            return treeRendererLocation;
        }
        public String getMenuRendererLocation() {
            return menuRendererLocation;
        }
    }
}
