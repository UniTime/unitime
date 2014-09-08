<#-- 
  UniTime 3.5 (University Timetabling Application)
  Copyright (C) 2014, UniTime LLC
  
  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 3 of the License, or
  (at your option) any later version.
  
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
  
  You should have received a copy of the GNU General Public License along
  with this program.  If not, see <http://www.gnu.org/licenses/>.
  
  ----
  This template is used to generate Documentation/License/dependencies.txt file:
     mvn license:add-third-party
-->
<#function artifactFormat p>
    <#if p.name?index_of('Unnamed') &gt; -1>
        <#return p.artifactId + " (" + p.groupId + ":" + p.artifactId + ":" + p.version + ")">
    <#else>
        <#return p.name + " (" + p.groupId + ":" + p.artifactId + ":" + p.version + ")">
    </#if>
</#function>
UniTime 3.5 third-party dependencies:
<#list dependencyMap as e><#assign project = e.getKey()/><#assign licenses = e.getValue()/>

${project.artifactId}-${project.version}.jar<#if project.name??>
	${project.name}</#if>
	Maven: ${project.groupId}:${project.artifactId}:${project.version}<#if project.url??>
	URL: ${project.url}</#if><#list licenses as license>
	License: ${license}</#list>
</#list>

The famfamfam Silk Icons
	URL: http://www.famfamfam.com/lab/icons/silk
	License: Creative Commons Attribution 3.0 License

LED Icon Set
	URL: http://led24.de/iconset/
	License: Creative Commons Attribution 3.0 License

Free web development icons
	URL: http://www.icojam.com/blog/?p=119
	License: Public Domain

Onebit icon sets
	URL: http://www.icojam.com/blog/?p=177
	License: Public Domain

----
List of third-party dependencies grouped by their license type:
<#list licenseMap as e><#assign license = e.getKey()/><#assign projects = e.getValue()/>

${license}<#list projects as project>
	${artifactFormat(project)}</#list>
</#list>
	Free web development icons
	Onebit icon sets

Creative Commons Attribution 3.0 License
	The famfamfam Silk Icons
	LED Icon Set

----
For the appropriate license, see

Apache Software License (ASL), Version 2.0 
	http://www.apache.org/licenses/LICENSE-2.0
	http://www.gwtproject.org/terms.html (GWT)
BSD or BSD-style Licenses
	http://asm.ow2.org/license.html (ASM Core)
	http://www.antlr.org/license.html (AntLR Parser Generator)
	http://freemarker.org/docs/app_license.html (FreeMaker)
	http://opensource.org/licenses/bsd-license.php (Protocol Buffer Java API, biweekly)
	http://dom4j.sourceforge.net/dom4j-1.6.1/license.html (dom4j)
Common Development and Distribution License (CDDL), Version 1.0
	http://opensource.org/licenses/CDDL-1.0
Creative Commons Attribution 3.0 License
	http://creativecommons.org/licenses/by/3.0
Eclipse Public License (EPL), Version 1.0
	http://www.eclipse.org/legal/epl-v10.html
GNU General Public License (GPL), Version 2
	http://www.gnu.org/licenses/gpl-2.0.html
GNU General Public License, Version 2 with the Classpath Exception
	http://openjdk.java.net/legal/gplv2+ce.html
GNU Lesser General Public License (LGPL), Version 2.1
	https://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
GNU Lesser General Public License (LGPL), Version 3
	https://www.gnu.org/licenses/lgpl.html
JA-SIG License for Use
	http://www.jasig.org/cas/license
Public Domain
	http://www.json.org/license.html (JSON)
	http://creativecommons.org/publicdomain/mark/1.0 (Free web development icons, Onebit icon sets)
MIT License
	http://www.slf4j.org/license.html (SLF4J)
Mozilla Public License (MPL), Version 1.1
	http://www.mozilla.org/MPL/1.1