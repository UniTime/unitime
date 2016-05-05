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
	public PropertiesInfo(Map<String, String> info) {
		super();
		for (Iterator i1=info.entrySet().iterator();i1.hasNext();) {
			Map.Entry entry = (Map.Entry)i1.next();
			String key = (String)entry.getKey();
			String value = entry.getValue().toString();
			setProperty(key, value);
		}
	}
	
	public void load(Element root) {
		clear();
		int version = Integer.parseInt(root.attributeValue("version"));
		if (version==1) {
			for (Iterator i=root.elementIterator("entry");i.hasNext();) {
				Element el = (Element)i.next();
				setProperty(el.attributeValue("key"),el.getText());
			}
		}
	}
	
	public void save(Element root) {
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
