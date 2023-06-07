/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
*/
package org.unitime.timetable.dataexchange;

import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.dom.DOMCDATA;
import org.unitime.timetable.model.Script;
import org.unitime.timetable.model.ScriptParameter;
import org.unitime.timetable.model.Session;

/**
 * @author Tomas Muller
 */
public class ScriptExport extends BaseExport{

	@Override
	public void saveXml(Document document, Session session, Properties parameters) throws Exception {
		try {
			beginTransaction();
			
			document.addDocType("scripts", "-//UniTime//UniTime Scripts DTD/EN", "http://www.unitime.org/interface/Script.dtd");
			
			Element root = document.addElement("scripts");
			root.addAttribute("created", new Date().toString());
			
			for (Script script: (List<Script>)getHibSession().createQuery("from Script order by name").list()) {
				exportScript(root, script);
			}
			
			commitTransaction();
        } catch (Exception e) {
            fatal("Exception: "+e.getMessage(),e);
            rollbackTransaction();
        }
	}
	
	public static Element exportScript(Element parent, Script script) {
		Element scriptEl = parent.addElement("script");
        scriptEl.addAttribute("name", script.getName());
        if (script.getPermission() != null)
        	scriptEl.addAttribute("permission", script.getPermission());
        scriptEl.addAttribute("engine", script.getEngine());
        if (script.getDescription() != null)
        	scriptEl.addElement("description").add(new DOMCDATA(script.getDescription()));
        for (ScriptParameter parameter: script.getParameters()) {
        	Element paramEl = scriptEl.addElement("parameter");
        	paramEl.addAttribute("name", parameter.getName());
        	if (parameter.getLabel() != null)
        		paramEl.addAttribute("label", parameter.getLabel());
        	paramEl.addAttribute("type", parameter.getType());
        	if (parameter.getDefaultValue() != null)
        		paramEl.addAttribute("default", parameter.getDefaultValue());
        }
        if (script.getScript() != null)
        	scriptEl.addElement("body").add(new DOMCDATA(script.getScript()));
		return scriptEl;
	}
}
