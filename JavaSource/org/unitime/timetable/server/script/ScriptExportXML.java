/*
 * UniTime 3.5 (University Timetabling Application)
 * Copyright (C) 2015, UniTime LLC, and individual contributors
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
package org.unitime.timetable.server.script;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.dom.DOMCDATA;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.springframework.stereotype.Service;
import org.unitime.timetable.export.ExportHelper;
import org.unitime.timetable.export.Exporter;
import org.unitime.timetable.model.Script;
import org.unitime.timetable.model.ScriptParameter;
import org.unitime.timetable.model.dao.ScriptDAO;
import org.unitime.timetable.security.rights.Right;

@Service("org.unitime.timetable.export.Exporter:script.xml")
public class ScriptExportXML implements Exporter {

	@Override
	public String reference() {
		return "script.xml";
	}

	@Override
	public void export(ExportHelper helper) throws IOException {
		String s = helper.getParameter("script");
		if (s == null) throw new IllegalArgumentException("No script provided, please set the script parameter.");
		Script script = ScriptDAO.getInstance().get(Long.valueOf(s));
		if (script == null) throw new IllegalArgumentException("Stript " + s + " does not exist.");
		
		helper.getSessionContext().checkPermission(Right.ScriptEdit);
		
		helper.setup("text/xml", script.getName().replace('/', '-').replace('\\', '-').replace(':', '-') + ".xml", false);
		
		Document document = DocumentHelper.createDocument();
		Element scriptEl = document.addElement("script");
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
		scriptEl.addAttribute("created", new Date().toString());
        
        OutputStream out = helper.getOutputStream();
        new XMLWriter(out, OutputFormat.createPrettyPrint()).write(document);
	}

}
