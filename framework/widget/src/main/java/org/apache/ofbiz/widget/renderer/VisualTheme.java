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
package org.apache.ofbiz.widget.renderer;

import java.util.ArrayList;
import java.util.List;
import org.apache.ofbiz.base.util.UtilXml;
import org.apache.ofbiz.widget.model.ModelTheme;
import org.w3c.dom.Element;

public final class VisualTheme {

    public static final String module = VisualTheme.class.getName();
    private ModelTheme modelTheme;

    public String getVisualThemeId() {
        return visualThemeId;
    }

    public List<String> getScreenshots() {
        return screenshots;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public String visualThemeId;
    public List<String> screenshots = new ArrayList<>();
    public String displayName = "";
    public String description = "";

    public VisualTheme(ModelTheme modelTheme, Element visualThemeElement) {
        this.modelTheme = modelTheme;
        this.visualThemeId = visualThemeElement.getAttribute("id");
        this.displayName = visualThemeElement.getAttribute("display-name");
        this.description = UtilXml.elementValue(UtilXml.firstChildElement(visualThemeElement, "description"));
        for (Element screenshotElement : UtilXml.childElementList(visualThemeElement, "screenshot")) {
            screenshots.add(screenshotElement.getAttribute("location"));
        }
    }

    public ModelTheme getModelTheme() {
        return modelTheme;
    }
/*
    public String toString() {
        StringBuilder toString = new StringBuilder("visual-theme-id:").append(visualThemeId)
                .append(", display-name: ").append(this.displayName)
                .append(", description: ").append(description)
                .append(", screenshots: ").append(screenshots);
        return toString.toString();
    }
    */
}
