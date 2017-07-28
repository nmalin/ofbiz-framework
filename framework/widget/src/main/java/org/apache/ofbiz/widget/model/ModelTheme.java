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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.ofbiz.base.util.Debug;
import org.apache.ofbiz.base.util.GeneralException;
import org.apache.ofbiz.base.util.ObjectType;
import org.apache.ofbiz.base.util.UtilXml;
import org.w3c.dom.Element;

/**
 * Widget Theme Library - Widget model class.
 */
@SuppressWarnings("serial")
public class ModelTheme implements Serializable {

    public static final String module = ModelTheme.class.getName();
    /**
     * The parameter name used to control widget boundary comments. Currently
     * set to "widgetVerbose".
     */
    public static final String enableBoundaryCommentsParam = "widgetVerbose";

    private final ModelTheme originTheme;
    //generic properties
    private final String name;
    private final List<String> visualThemeIds;
    private final Integer defaultViewSize;

    // Autocomplete configuration
    // Default number of items to be displayed in lookup ajax autocompleter
    private final Integer autocompleterDefaultViewSize;
    // Default minimum number of characters an user has to type before the ajax autocompleter activates (jQuery default is 1)
    private final Integer autocompleterDefaultMinLength;
    // Default delay in milliseconds the Autocomplete waits after a keystroke to activate itself. A zero-delay makes sense for local data (more responsive), but can produce a lot of load for remote data, while being less responsive.
    private final Integer autocompleterDefaultDelay;
    // Show/hide the ID field that is returned from ajax autocompleter
    private final Boolean autocompleterDisplayReturnField;

    //layer modal
    // Default position and size for lookup layered windows
    private final String lookupPosition;
    private final Integer lookupWidth;
    private final Integer lookupHeight;
    //Default size for layered modal windows
    private final Integer linkDefaultLayeredModalWidth;
    private final Integer linkDefaultLayeredModalHeight;

    //dedicate theme properties
    private final Map<String, Object> themePropertiesMap;

    //template rendering
    private final Map<String, ModelTemplate> modelTemplateMap;
    private final Map<String, String> modelCommonScreensMap;

    //** XML Constructor */
    public ModelTheme(Element themeElement) {
        this.name = themeElement.getAttribute("name");
        ModelTheme initOriginTheme = null;
        List<String> initVisualThemeIds = new ArrayList<>();
        Map<String, Object> initWidgetPropertiesMap = new HashMap<>();
        Map<String, Object> initThemePropertiesMap = null;
        Map<String, ModelTemplate> initModelTemplateMap = null;
        Map<String, String> initModelCommonScreensMap = null;

        //first collect value from XML
        for (Element childElement : UtilXml.childElementList(themeElement)) {
            switch (childElement.getNodeName()) {
                case "widget-properties":
                    addWidgetProperties(initWidgetPropertiesMap, childElement);
                    break;
                case "visual-themes":
                    for (Element visualTheme : UtilXml.childElementList(childElement)) {
                        initVisualThemeIds.add(visualTheme.getAttribute("id"));
                    }
                case "theme-properties":
                    initThemePropertiesMap = new HashMap<>();
                    for (Element property : UtilXml.childElementList(childElement)) {
                        addThemeProperty(initThemePropertiesMap, property);
                    }
                    break;
                case "templates":
                    initModelTemplateMap = new HashMap<>();
                    for (Element template : UtilXml.childElementList(childElement)) {
                        initModelTemplateMap.put(template.getAttribute("name"), new ModelTemplate(template));
                    }
                    break;
                case "extends":
                    initOriginTheme = ThemeFactory.getModelThemeFromLocation(childElement.getAttribute("location"));
                    break;
                case "common-screens":
                    initModelCommonScreensMap = new HashMap<>();
                    for (Element screenPurpose : UtilXml.childElementList(childElement)) {
                        String defaultLocation = screenPurpose.getAttribute("default-location");
                        for (Element screen : UtilXml.childElementList(screenPurpose)) {
                            String name = screen.getAttribute("name");
                            String location = screen.getAttribute("location");
                            if (location == null) location = defaultLocation;
                            if (location == null) {
                                Debug.logWarning("We can resolve the screen location " + name + " in the theme " + this.name + " so no added it", module);
                                continue;
                            }
                            if (initModelCommonScreensMap.containsKey(name)) {
                                Debug.logWarning("We detect a second screen " + name + " in the theme " + this.name + " so no added it a second time", module);
                                continue;
                            }
                            initModelCommonScreensMap.put(name, location);
                        }
                    }
                    break;
            }
        }

        // resolve value from the origin theme
        this.originTheme = (initOriginTheme != null) ? initOriginTheme : null; 
        if (initOriginTheme != null) {
            if (initModelTemplateMap == null) initModelTemplateMap = new HashMap<>();
            if (initModelCommonScreensMap == null) initModelCommonScreensMap = new HashMap<>();
            extendFromOrigin(initWidgetPropertiesMap, initThemePropertiesMap, initModelTemplateMap, initModelCommonScreensMap);
        }

        // now store all values on final variable
        this.defaultViewSize = (Integer) initWidgetPropertiesMap.get("defaultViewSize");
        this.autocompleterDefaultViewSize = (Integer) initWidgetPropertiesMap.get("autocompleterDefaultViewSize");
        this.autocompleterDefaultMinLength = (Integer) initWidgetPropertiesMap.get("autocompleterDefaultMinLength");
        this.autocompleterDefaultDelay = (Integer) initWidgetPropertiesMap.get("autocompleterDefaultDelay");
        this.autocompleterDisplayReturnField = (Boolean) initWidgetPropertiesMap.get("autocompleterDisplayReturnField");
        this.lookupPosition = (String) initWidgetPropertiesMap.get("lookupPosition");
        this.lookupWidth = (Integer) initWidgetPropertiesMap.get("lookupWidth");
        this.lookupHeight = (Integer) initWidgetPropertiesMap.get("lookupHeight");
        this.linkDefaultLayeredModalWidth = (Integer) initWidgetPropertiesMap.get("linkDefaultLayeredModalWidth");
        this.linkDefaultLayeredModalHeight = (Integer) initWidgetPropertiesMap.get("linkDefaultLayeredModalHeight");
        this.visualThemeIds = Collections.unmodifiableList(initVisualThemeIds);
        this.themePropertiesMap = (initThemePropertiesMap != null ? Collections.unmodifiableMap(initThemePropertiesMap) : null);
        this.modelTemplateMap = (initModelTemplateMap != null ? Collections.unmodifiableMap(initModelTemplateMap) : null);
        this.modelCommonScreensMap = (initModelCommonScreensMap != null ? Collections.unmodifiableMap(initModelCommonScreensMap) : null);
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
    public Boolean getAutocompleterDisplayReturnField() {
        return autocompleterDisplayReturnField;
    }
    public Integer getAutocompleterDefaultDelay() {
        return autocompleterDefaultDelay;
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


    /**
     * */
    private void extendFromOrigin(Map<String, Object> initWidgetPropertiesMap, Map<String, Object> initThemePropertiesMap,
                                  Map<String, ModelTemplate> initModelTemplateMap, Map<String, String> initModelCommonScreensMap) {
        if (initWidgetPropertiesMap.get("defaultViewSize") == null) initWidgetPropertiesMap.put("defaultViewSize", originTheme.defaultViewSize);
        if (initWidgetPropertiesMap.get("autocompleterDefaultViewSize")  == null) initWidgetPropertiesMap.put("autocompleterDefaultViewSize", originTheme.autocompleterDefaultViewSize);
        if (initWidgetPropertiesMap.get("autocompleterDefaultMinLength")  == null) initWidgetPropertiesMap.put("autocompleterDefaultMinLength", originTheme.autocompleterDefaultMinLength);
        if (initWidgetPropertiesMap.get("autocompleterDefaultDelay")  == null) initWidgetPropertiesMap.put("autocompleterDefaultDelay", originTheme.autocompleterDefaultDelay);
        if (initWidgetPropertiesMap.get("autocompleterDisplayReturnField") == null) initWidgetPropertiesMap.put("autocompleterDisplayReturnField", originTheme.autocompleterDisplayReturnField);
        if (initWidgetPropertiesMap.get("lookupPosition") == null) initWidgetPropertiesMap.put("lookupPosition", originTheme.lookupPosition);
        if (initWidgetPropertiesMap.get("lookupWidth") == null) initWidgetPropertiesMap.put("lookupWidth", originTheme.lookupWidth);
        if (initWidgetPropertiesMap.get("lookupHeight") == null) initWidgetPropertiesMap.put("lookupHeight", originTheme.lookupHeight);
        if (initWidgetPropertiesMap.get("linkDefaultLayeredModalWidth") == null) initWidgetPropertiesMap.put("linkDefaultLayeredModalWidth", originTheme.linkDefaultLayeredModalWidth);
        if (initWidgetPropertiesMap.get("linkDefaultLayeredModalHeight") == null) initWidgetPropertiesMap.put("linkDefaultLayeredModalHeight", originTheme.linkDefaultLayeredModalHeight);

        // resolve all decicate properties from origin and sucharge by the present dedicate properties
        if (originTheme.themePropertiesMap != null) {
            Map<String, Object> themePropertiesTmp = new HashMap<>(originTheme.themePropertiesMap);
            if (initThemePropertiesMap != null) themePropertiesTmp.putAll(initThemePropertiesMap);
            initThemePropertiesMap.clear();
            initThemePropertiesMap.putAll(themePropertiesTmp);
        }

        // Add modelTemplate present on origin and not on this
        if (originTheme.modelTemplateMap !=  null) {
            if (initModelTemplateMap != null) {
                for (String modelTemplateName : originTheme.modelTemplateMap.keySet()) {
                    ModelTemplate originModelTemplate = originTheme.modelTemplateMap.get(modelTemplateName);
                    ModelTemplate modelTemplate = initModelTemplateMap.get(modelTemplateName);
                    modelTemplate = new ModelTemplate(modelTemplate, originModelTemplate);
                    initModelTemplateMap.put(modelTemplateName, modelTemplate);
                }
            } else {
                initModelTemplateMap = originTheme.modelTemplateMap;
            }
        }
        if (originTheme.modelCommonScreensMap != null) {
            Map<String, String> modelCommonScreensMapTmp = new HashMap<>(originTheme.modelCommonScreensMap);
            modelCommonScreensMapTmp.putAll(initModelCommonScreensMap);
            initModelCommonScreensMap.clear();
            initModelCommonScreensMap.putAll(modelCommonScreensMapTmp);
        }
    }

    private void addWidgetProperties(Map<String, Object> initWidgetPropertiesMap, Element widgetProperties) {
        for (Element childElement : UtilXml.childElementList(widgetProperties)) {
            switch (childElement.getNodeName()) {
                case "default-view-size":
                    initWidgetPropertiesMap.put("defaultViewSize", Integer.valueOf(childElement.getAttribute("value")));
                    break;
                case "autocompleter":
                    initWidgetPropertiesMap.put("autocompleterDefaultDelay", Integer.valueOf(childElement.getAttribute("default-delay")));
                    initWidgetPropertiesMap.put("autocompleterDefaultMinLength", Integer.valueOf(childElement.getAttribute("default-min-lenght")));
                    initWidgetPropertiesMap.put("autocompleterDefaultViewSize", Integer.valueOf(childElement.getAttribute("default-view-size")));
                    initWidgetPropertiesMap.put("autocompleterDisplayReturnField", "true".equalsIgnoreCase(childElement.getAttribute("display-return-field")));
                    break;
                case "lookup":
                    initWidgetPropertiesMap.put("lookupPosition", childElement.getAttribute("position"));
                    initWidgetPropertiesMap.put("lookupHeight", Integer.valueOf(childElement.getAttribute("height")));
                    initWidgetPropertiesMap.put("lookupWidth", Integer.valueOf(childElement.getAttribute("width")));
                    break;
                case "layered-modal":
                    initWidgetPropertiesMap.put("linkDefaultLayeredModalHeight", Integer.valueOf(childElement.getAttribute("height")));
                    initWidgetPropertiesMap.put("linkDefaultLayeredModalWidth", Integer.valueOf(childElement.getAttribute("width")));
                    break;
            }
        }
    }

    private void addThemeProperty(Map<String, Object> initThemePropertiesMap, Element property) {
        String name = property.getAttribute("name");
        String value = property.getAttribute(("value"));
        String type = property.getAttribute("type");
        if (type == null || "String".equals(type) || "java.lang.String".equals(type)) {
            initThemePropertiesMap.put(name, value);
        } else {
            try {
                initThemePropertiesMap.put(name, ObjectType.simpleTypeConvert(value, type, null, null));
            } catch (GeneralException e) {
                Debug.logError("Impossible to parse the value " + value + " to type " + type + " for the property " + name + " on theme " + this.name, module);
            }
        }
    }
    public Object getProperty(String propertyName) {
        if (! themePropertiesMap.containsKey(propertyName)
                || themePropertiesMap.get(propertyName) == null) return "";
        return themePropertiesMap.get(propertyName);
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
        private final String name;
        private final String type;
        private final String compress;
        private final String encoder;
        private final String contentType;
        private final String encoding;
        private final String screenRendererLocation;
        private final String formRendererLocation;
        private final String menuRendererLocation;
        private final String treeRendererLocation;

        public ModelTemplate(Element template) {
            this.name = template.getAttribute("name");
            this.type = template.getAttribute("type");
            this.compress = template.getAttribute("compress");
            this.encoder = template.getAttribute("encoder");
            this.contentType = template.getAttribute("contentType");
            this.encoding = template.getAttribute("encoding");

            String screenRendererLocation = null;
            String formRendererLocation = null;
            String menuRendererLocation = null;
            String treeRendererLocation = null;
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
            this. screenRendererLocation = screenRendererLocation;
            this. formRendererLocation = formRendererLocation;
            this. menuRendererLocation = menuRendererLocation;
            this. treeRendererLocation = treeRendererLocation;
        }
        public ModelTemplate (ModelTemplate currentModelTemplate, ModelTemplate originModelTemplate) {
            boolean exist = currentModelTemplate != null;
            this.name = exist ? currentModelTemplate.name : originModelTemplate.name;
            this.type = exist ? currentModelTemplate.type : originModelTemplate.type;
            this.compress = exist && currentModelTemplate.compress != null ? currentModelTemplate.compress : originModelTemplate.compress;
            this.encoder = exist && currentModelTemplate.encoder != null ? currentModelTemplate.encoder : originModelTemplate.encoder;
            this.contentType = exist && currentModelTemplate.contentType != null ? currentModelTemplate.contentType : originModelTemplate.contentType;
            this.encoding = exist && currentModelTemplate.encoding != null ? currentModelTemplate.encoding : originModelTemplate.encoding;
            this.screenRendererLocation = exist && currentModelTemplate.screenRendererLocation != null ? currentModelTemplate.screenRendererLocation : originModelTemplate.screenRendererLocation;
            this.formRendererLocation = exist && currentModelTemplate.formRendererLocation != null ? currentModelTemplate.formRendererLocation : originModelTemplate.formRendererLocation;
            this.treeRendererLocation = exist && currentModelTemplate.treeRendererLocation != null ? currentModelTemplate.treeRendererLocation : originModelTemplate.treeRendererLocation;
            this.menuRendererLocation = exist && currentModelTemplate.menuRendererLocation != null ? currentModelTemplate.menuRendererLocation : originModelTemplate.menuRendererLocation;
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
