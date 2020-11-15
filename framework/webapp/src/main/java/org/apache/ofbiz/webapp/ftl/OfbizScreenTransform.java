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
package org.apache.ofbiz.webapp.ftl;

import freemarker.core.Environment;
import freemarker.ext.beans.BeanModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateTransformModel;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.ofbiz.base.util.GeneralException;
import org.apache.ofbiz.base.util.UtilGenerics;
import org.apache.ofbiz.base.util.UtilHttp;
import org.apache.ofbiz.base.util.UtilValidate;
import org.apache.ofbiz.base.util.collections.MapStack;
import org.apache.ofbiz.base.util.template.FreeMarkerWorker;
import org.apache.ofbiz.widget.model.ModelTheme;
import org.apache.ofbiz.widget.model.ThemeFactory;
import org.apache.ofbiz.widget.renderer.ScreenRenderer;
import org.apache.ofbiz.widget.renderer.ScreenStringRenderer;
import org.apache.ofbiz.widget.renderer.VisualTheme;
import org.apache.ofbiz.widget.renderer.macro.MacroScreenRenderer;
import org.xml.sax.SAXException;

/**
 * OfbizContentTransform - Freemarker Transform for content links
 */
public class OfbizScreenTransform implements TemplateTransformModel {

    private static final String MODULE = OfbizScreenTransform.class.getName();
    @Override
    public Writer getWriter(Writer out, @SuppressWarnings("rawtypes") Map args) {
        final StringBuilder buf = new StringBuilder();
        final Map<String, Object> context = UtilGenerics.cast(FreeMarkerWorker.createEnvironmentMap(Environment.getCurrentEnvironment()));
        final String location = (String) args.get("location");
        final String name = (String) args.get("name");
        final String screenType = UtilValidate.isNotEmpty(args.get("location")) ? (String) args.get("location") : "screen";
        return new Writer(out) {
            @Override
            public void write(char cbuf[], int off, int len) {
                buf.append(cbuf, off, len);
            }

            @Override
            public void flush() throws IOException {
                out.flush();
            }

            @Override
            public void close() throws IOException {
                try {
                    Environment env = Environment.getCurrentEnvironment();
                    BeanModel req = (BeanModel) env.getVariable("request");
                    HttpServletRequest request = req == null ? null : (HttpServletRequest) req.getWrappedObject();
                    VisualTheme visualTheme = (VisualTheme) context.get("visualTheme");
                    if (visualTheme == null) visualTheme = ThemeFactory.resolveVisualTheme(request);
                    ModelTheme modelTheme = visualTheme.getModelTheme();

                    //String screenMacroLibraryPath = modelTheme.getScreenRendererLocation();
                    String screenName = name != null ? name : buf.toString();

                    String screenMacroLibraryPath = modelTheme.getScreenRendererLocation(screenType);
                    ScreenStringRenderer screenStringRenderer = new MacroScreenRenderer(modelTheme.getType(screenType), screenMacroLibraryPath);

                    Writer writer = new StringWriter();
                    ScreenRenderer screens = new ScreenRenderer(writer, MapStack.create(context), screenStringRenderer);

                    //check if the name is combined
                    if (screenName.contains("#")) {
                        screens.render(screenName);
                    } else {
                        String forwardLocation = location != null ? location :
                                FreeMarkerWorker.unwrap(env.getVariable("defaultTemplateLocation"));
                        if (forwardLocation == null) {
                            throw new IllegalStateException("No location resolved to rendering the screen");
                        }
                        screens.render(screenName, forwardLocation);
                    }

                    out.write(writer.toString());
                } catch (GeneralException | SAXException | ParserConfigurationException | TemplateException e) {
                    throw new IOException(e.getMessage());
                }
            }
        };
    }
}
