/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2008 - 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.solver.ui;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.dom4j.Element;

/**
 * @author Tomas Muller
 */
public class PropertiesInfo extends Properties implements TimetableInfo, Serializable {
	private static final long serialVersionUID = 1L;
	public static int sVersion = 1; // to be able to do some changes in the future
	
	public PropertiesInfo() {
		super();
	}
	public PropertiesInfo(Map info) {
		super();
		for (Iterator i1=info.entrySet().iterator();i1.hasNext();) {
			Map.Entry entry = (Map.Entry)i1.next();
			String key = (String)entry.getKey();
			String value = entry.getValue().toString();
			setProperty(key, value);
		}
	}
	
	public void load(Element root) throws Exception {
		clear();
		int version = Integer.parseInt(root.attributeValue("version"));
		if (version==1) {
			for (Iterator i=root.elementIterator("entry");i.hasNext();) {
				Element el = (Element)i.next();
				setProperty(el.attributeValue("key"),el.getText());
			}
		}
	}
	
	public void save(Element root) throws Exception {
		root.addAttribute("version", String.valueOf(sVersion));
		for (Iterator i=entrySet().iterator();i.hasNext();) {
			Map.Entry entry = (Map.Entry)i.next();
			String key = (String)entry.getKey();
			String value = (String)entry.getValue();
			root.addElement("entry").addAttribute("key",key).setText(value);
		}
	}

	public boolean saveToFile() {
		return false;
	}
}
