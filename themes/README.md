<!--
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
-->
Apache OFBiz® Themes
====================

# What's a theme
A Theme is a ofbiz component that define all element necessary to rendering all information generate by the screen engine through a embed technology
Currently themes present in Apache OFBiz use html5/jquery/css to do that.

To realize this, a theme can be define some properties that can be necessary, define is own ftl macro the rendering the different modelScreen element and define is own screen decorator to prepare the final user screen structure for the technology used by the theme
# How define it
A theme is a standard component, present in the directory themes or plugins with a file definition present on widget/Theme.xml and support the [widget-theme.xsd](http://ofbiz.apache.org/dtds/widget-theme.xsd)

To offer the possibility for the end users to select the theme throw the select theme screen, the theme need to load on the entity VisualTheme one or more entries related to this theme

# Structure of Theme.xml
The theme definition file help OFBiz to know what specific rendering the Theme want to use.
It compose by two definition element and four optional blocks

## Main definition
The First element mandatory is the or these visualThemeId related to the theme
Define like that
```xml
   <visual-themes>
        <visual-theme id="MY_THEME"/>
    </visual-themes>
```
You need to define on the database en entry on entity VisualTheme for each visual-theme id define here.
```xml
<entity-engine-xml>
    <VisualTheme visualThemeId="MY_THEME" visualThemeSetId="BACKOFFICE" description="My theme - Example (based on flatgrey)"/>
</entity-engine-xml>
```
>_Note_: a component theme can be load one or more than one visual theme id. Generally only one is present.

The second important (but not mandatory) element is **implements**
```xml
   <extends location="component://common-theme/widget/Theme.xml"/>
```
This element indicate that your theme copy form the extends theme all information not present in this file definition.
If this element isn't present on your theme, you will need to define all information present in common-theme to be sure that OFBiz missing nothing for a correct run.
Otherwise some functionnality can be break.
>_Note_: It's highly recommend to extends the common-theme to be sure that you theme work correctly and surcharge only what you need.

The four following blocks are optionnal if you define an extends theme
## General properties
This block contains all properties that the screen engine can be use to prepare the rendering and that the theme can be implement
```xml
    <widget-properties><!--Transversal properties relative to ofbiz widget component-->
        <default-view-size value="20"/>
        <autocompleter
                default-view-size="10"
                default-min-lenght="2"
                default-delay="300"
                display-return-field="true"/>
        <lookup
                position="topleft"
                width="640"
                height="500"/>
        <layered-modal
                width="800"
                height="600"/>
    </widget-properties>
```

## Theme's specific properties
This block contains all properties specific to this theme, in general some propreties present on ftl template, that are initialized by the theme and that can be surchage by an other theme through the extends element
```xml
        <property name="jgrowl.position" value="center" type="String"/><!--possible value: top-left, top-right, bottom-left, bottom-right, center-->
        <property name="jgrowl.width" value="800" type="Integer"/>
        <property name="jgrowl.height" value="" type="Integer"/>
        <property name="jgrowl.speed" value="100" type="Integer"/>
```
## Ftl macro library
This block define for each technology implemented by the screen engine where it can find the macro library for each model (Screen, Form, Tree, Menu)
```xml
   <templates><!-- Freemarker template use by this theme to render widget model-->
        <template name="screen" type="html" content-type="UTF-8" encoding="none" encoder="html" compress="false">
            <template-file widget="screen" location="component://common-theme/template/macro/HtmlScreenMacroLibrary.ftl"/>
            <template-file widget="form" location="component://common-theme/template/macro/HtmlFormMacroLibrary.ftl"/>
            <template-file widget="tree" location="component://common-theme/template/macro/HtmlTreeMacroLibrary.ftl"/>
            <template-file widget="menu" location="component://common-theme/template/macro/HtmlMenuMacroLibrary.ftl"/>
        </template>
        ...
   </templates>
```
>_Note_: If you want surcharge some few macros, you can just create wanted macros and import the other from common-theme (at the file beginning) like that : ```<#include "component://common-theme/template/macro/HtmlFormMacroLibrary.ftl"/>```

## Screen library
This block define where OFBiz can found all official screen define in framework/common
```xml
     <common-screens><!--list all common screen and decorator global to each application that each theme can be surcharge or not and use the screen present on common theme-->
        <structural-decorator default-location="component://common-theme/widget/CommonScreens.xml">
            <screen name="GlobalDecorator"/>
            ...
        </structural-decorator>
        <embed-decorator default-location="component://common-theme/widget/CommonScreens.xml">
            <screen name="FindScreenDecorator"/>
        </embed-decorator>
        <general-screen default-location="component://common-theme/widget/CommonScreens.xml">
            <screen name="geoChart"/>
            ...
        </general-screen>
    </common-screens>
```
Screens are separate on three type :
* structural-decorator : contains all decorator that organise all the screen structure
* embed-decorator : decorator use only on sub screen
* general-screen : list all generic inter application screen
# The common-theme
This is the root theme that contains all information to ensure a good functional for OFBiz.
Currently it keep all old theme system for backward compatibility with ftl template managed by the entity **VisualThemeResource**
# Create your own theme
As a theme is a component, you can create a new theme like a plugin. You can see more information on this subject [on the ofbiz README](https://github.com/apache/ofbiz-framework/blame/trunk/README.md#L584)
After create run component, you can add the two minimal information :
* Theme.xml file in **plugins/my-theme/widget/** with mininal trame :
```xml
<theme name="my-theme"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:noNamespaceSchemaLocation="http://ofbiz.apache.org/dtds/widget-theme.xsd">
    <visual-themes>
        <visual-theme id="MY_THEME"/>
    </visual-themes>
</theme>
```
* your data file to add your visual theme in **plugins/my-theme/data/**
```xml
<entity-engine-xml>
    <VisualTheme visualThemeId="MY_THEME" visualThemeSetId="BACKOFFICE" description="My theme"/>
</entity-engine-xml>
```
## extends common-theme
This is a first step to understand how the theme system work. With your new theme, you can try to surchage different elements.
To start, extends the common-theme :
```xml
<theme name="my-theme"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:noNamespaceSchemaLocation="http://ofbiz.apache.org/dtds/widget-theme.xsd">
    <visual-themes>
        <visual-theme id="MY_THEME"/>
    </visual-themes>
    <extends location="component://common-theme/widget/Theme.xml"/>
</theme>
```
Now your theme would be operational but without particularity.

You can surcharge a ftl macro, to do this create your own ftl macro file in **plugins/my-theme/templates/macro/HtmlFormMacroLibrary.ftl** with

```ftl
<#include "component://common-theme/template/macro/HtmlFormMacroLibrary.ftl"/>

<#macro renderDisplayField type imageLocation idName description title class alert inPlaceEditorUrl="" inPlaceEditorParams="">
    <#if description?has_content>
    *###*${description?replace("\n", "<br />")}**<#t/>
    <#else>
        *#&nbsp;#*<#t/>
    </#if>
</#macro>
```
now indicate to your theme that you want use this library
```xml
<theme name="my-theme"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:noNamespaceSchemaLocation="http://ofbiz.apache.org/dtds/widget-theme.xsd">
    <visual-themes>
        <visual-theme id="MY_THEME"/>
    </visual-themes>
    <extends location="component://common-theme/widget/Theme.xml"/>
    <templates>
        <template name="screen" type="html" content-type="UTF-8" encoding="none" encoder="html" compress="false">
            <template-file widget="form" location="component://my-theme/template/macro/HtmlFormMacroLibrary.ftl"/>
        </template>
    </templates>
</theme>
```
and check the result when you select your theme. Ok ok the result isn't really interesting but it's to understand how it works.

## create from scratch