/*
 * UniTime 3.4 - 3.5 (University Timetabling Application)
 * Copyright (C) 2013, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
*/
package org.unitime.timetable.dataexchange;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.dom4j.Element;
import org.unitime.timetable.model.Script;
import org.unitime.timetable.model.ScriptParameter;

public class ScriptImport extends BaseImport {

	@Override
	public void loadXml(Element root) throws Exception {
        try {
            beginTransaction();
            if (root.getName().equalsIgnoreCase("script")) {
            	importScript(root);
            } else if (root.getName().equalsIgnoreCase("scripts")) {
            	for (Iterator i = root.elementIterator("script"); i.hasNext(); )
                    importScript((Element) i.next());
            } else {
            	throw new Exception("Given XML file is not a script file.");
            }
            commitTransaction();
        } catch (Exception e) {
            fatal("Exception: " + e.getMessage(), e);
            rollbackTransaction();
            throw e;
        }
	}
	
	protected void importScript(Element scriptEl) {
		String name = scriptEl.attributeValue("name");
		if (name == null) {
			error("String name is not provided.");
			return;
		}
		Script script = (Script)getHibSession().createQuery("from Script where name = :name").setString("name", name).uniqueResult();
		if (script == null) {
			script = new Script();
			script.setParameters(new HashSet<ScriptParameter>());
			script.setName(name);
		}
		script.setEngine(scriptEl.attributeValue("engine", "ECMAScript"));
		script.setPermission(scriptEl.attributeValue("permission"));
		
		Map<String, ScriptParameter> params = new HashMap<String, ScriptParameter>();
		for (ScriptParameter parameter: script.getParameters())
			params.put(parameter.getName(), parameter);
		
		for (Iterator i = scriptEl.elementIterator("parameter"); i.hasNext(); ) {
			Element paramEl = (Element) i.next();
			String pName = paramEl.attributeValue("name");
			if (pName == null) continue;
			ScriptParameter parameter = params.remove(pName);
			if (parameter == null) {
				parameter = new ScriptParameter();
				parameter.setName(pName);
				parameter.setScript(script);
				script.getParameters().add(parameter);
			}
			parameter.setLabel(paramEl.attributeValue("label"));
			parameter.setType(paramEl.attributeValue("type"));
			parameter.setDefaultValue(paramEl.attributeValue("default"));
		}
		
		for (ScriptParameter parameter: params.values()) {
			getHibSession().delete(parameter);
			script.getParameters().remove(parameter);
		}
		
		Element bodyEl = scriptEl.element("body");
		if (bodyEl != null)
			script.setScript(bodyEl.getText());
		else
			script.setScript(null);
		
		Element descriptionEl = scriptEl.element("description");
		if (descriptionEl != null)
			script.setDescription(descriptionEl.getText());
		else
			script.setDescription(null);
		
		getHibSession().saveOrUpdate(script);
	}

}
