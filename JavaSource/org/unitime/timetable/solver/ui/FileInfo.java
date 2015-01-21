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

import org.dom4j.Element;

/**
 * @author Tomas Muller
 */
public class FileInfo implements TimetableInfo, Serializable {
	private static final long serialVersionUID = 1L;
	public static int sVersion = 1; // to be able to do some changes in the future
	
	private String iName = null;
	
	public FileInfo() {}
	
	public String getName() { return iName; }
	public void setName(String name) { iName = name; }
	public TimetableInfo loadInfo(TimetableInfoFileProxy proxy) throws Exception {
		return proxy.loadFromFile(iName);
	}
	public void saveInfo(TimetableInfo info, TimetableInfoFileProxy proxy) throws Exception {
		proxy.saveToFile(iName, info);
	}
	public void deleteFile(TimetableInfoFileProxy proxy) throws Exception {
		proxy.deleteFile(iName);
	}

	public void load(Element root) throws Exception {
		int version = Integer.parseInt(root.attributeValue("version"));
		if (version==1) {
			iName = root.attributeValue("name");
		}
	}
	
	public void save(Element root) throws Exception {
		root.addAttribute("version", String.valueOf(sVersion));
		root.addAttribute("name", iName);
	}

	public boolean saveToFile() {
		return false;
	}
}
