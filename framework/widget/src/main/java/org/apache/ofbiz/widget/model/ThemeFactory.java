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
 * specific language governing permissions and limitations
 * under the License.
 *******************************************************************************/
package org.apache.ofbiz.widget.model;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.ofbiz.base.location.FlexibleLocation;
import org.apache.ofbiz.base.util.Debug;
import org.apache.ofbiz.base.util.FileUtil;
import org.apache.ofbiz.base.util.UtilMisc;
import org.apache.ofbiz.base.util.UtilProperties;
import org.apache.ofbiz.base.util.UtilXml;
import org.apache.ofbiz.base.util.cache.UtilCache;
import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.service.GenericServiceException;
import org.apache.ofbiz.service.LocalDispatcher;
import org.apache.ofbiz.webapp.WebAppUtil;
import org.apache.ofbiz.webapp.website.WebSiteWorker;
import org.apache.ofbiz.widget.renderer.Theme;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;


/**
 * Widget Library - Screen factory class
 */
public class ThemeFactory {

    public static final String module = ThemeFactory.class.getName();

    public static final UtilCache<String, ModelTheme> themeLocationCache = UtilCache.createUtilCache("widget.theme.locationResource", 0, 0, false);
    public static final UtilCache<String, ModelTheme> themeVisualThemeIdCache = UtilCache.createUtilCache("widget.theme.idAndlocationResource", 0, 0, false);

    public static ModelTheme getModelThemeFromLocation(String resourceName) {
        ModelTheme modelTheme = themeLocationCache.get(resourceName);
        if (modelTheme == null) {
            synchronized (ThemeFactory.class) {
                try {
                    modelTheme = themeLocationCache.get(resourceName);
                    if (modelTheme == null) {
                        URL themeFileUrl = null;
                        themeFileUrl = FlexibleLocation.resolveLocation(resourceName);
                        if (themeFileUrl == null) {
                            throw new IllegalArgumentException("Could not resolve location to URL: " + resourceName);
                        }
                        Document themeFileDoc = UtilXml.readXmlDocument(themeFileUrl, true, true);
                        modelTheme = readThemeDocument(themeFileDoc, resourceName);
                        themeLocationCache.put(resourceName, modelTheme);
                    }
                } catch (IOException | ParserConfigurationException | SAXException e) {
                    Debug.logError("Impossible to resolve the theme from the resourceName " + resourceName, module);
                }
            }
        }
        return modelTheme;
    }

    public static ModelTheme readThemeDocument(Document themeFileDoc, String sourceLocation) {
        if (themeFileDoc != null) {
            // read document and construct ModelScreen for each screen element
            ModelTheme modelTheme = new ModelTheme(themeFileDoc.getDocumentElement());
            return modelTheme;
        }
        return null;
    }

    public static Theme getThemeFromId(String visualThemeId) {
        if (visualThemeId == null) return null;
        ModelTheme modelTheme = themeVisualThemeIdCache.get(visualThemeId);
        if (modelTheme == null) {
            synchronized (ThemeFactory.class) {
                modelTheme = themeVisualThemeIdCache.get(visualThemeId);
                if (modelTheme == null) {
                    String ofbizHome = System.getProperty("ofbiz.home");
                    try {
                        List<File> xmlThemes = FileUtil.findXmlFiles(ofbizHome, "themes", "theme", "widget-theme.xsd");
                        for (File xmlTheme : xmlThemes) {
                            modelTheme = getModelThemeFromLocation(xmlTheme.toURI().toURL().toString());
                            if (modelTheme != null) {
                                for (String containsVisualThemeId : modelTheme.getVisualThemeIds()) {
                                    themeVisualThemeIdCache.put(containsVisualThemeId, modelTheme);
                                }
                            }
                        }
                    } catch (IOException e) {
                        Debug.logError("Impossible to resolve the theme from the visualThemeId " + visualThemeId + " throw: " + e, module);
                    }
                }
            }
            return new Theme(themeVisualThemeIdCache.get(visualThemeId), visualThemeId);
        }
        return new Theme(modelTheme, visualThemeId);
    }

    public static Theme resolveTheme(HttpServletRequest request) {
        //search on request
        HttpSession session = request.getSession();
        Theme theme = (Theme) session.getAttribute("theme");
        if (theme == null) {
            theme = (Theme) request.getAttribute("theme");
        }

        String visualThemeId = null;
        //resolve on user pref
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        GenericValue userLogin = (GenericValue) session.getAttribute("userLogin");
        if (userLogin != null && dispatcher != null) {
            try {
                Map<String, Object> userPreferencesResult = dispatcher.runSync("getUserPreferenceGroup",
                        UtilMisc.toMap("userPrefGroupTypeId", "GLOBAL_PREFERENCES", "userLogin", userLogin));
                Map<String, Object> userPreferences = (Map<String, Object>) userPreferencesResult.get("userPrefMap");
                if (userPreferences.containsKey("VISUAL_THEME")) {
                    visualThemeId = (String) userPreferences.get("VISUAL_THEME");
                }
            } catch (GenericServiceException e) {
                Debug.logError("Impossible to resolve the theme from user prefrence for " + userLogin.get("userLoginId"), module);
            }
        }

        //resolve from webapp
        if (visualThemeId == null) {
            ServletContext servletContext = request.getServletContext();
            visualThemeId = servletContext.getInitParameter("visualThemeId");
        }

        //resolve from general properties
        if (visualThemeId == null) {
            visualThemeId = UtilProperties.getPropertyValue("general", "VISUAL_THEME", "COMMON");
        }
        return getThemeFromId(visualThemeId);
    }
}
