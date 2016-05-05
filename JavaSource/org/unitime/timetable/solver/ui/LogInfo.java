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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


import org.cpsolver.ifs.util.Progress;
import org.dom4j.Element;

/**
 * @author Tomas Muller
 */
public class LogInfo implements TimetableInfo, Serializable {
	private static final long serialVersionUID = 1L;
	public static int sVersion = 1; // to be able to do some changes in the future
	public static int sNoSaveThreshold = Progress.MSGLEVEL_DEBUG;
	private List<Progress.Message> iLog = new ArrayList<Progress.Message>();
	
	public void setLog(List<Progress.Message> log) { iLog = log; }
	public List<Progress.Message> getLog() { return iLog; }
	
    public String getLog(int level) {
    	StringBuffer sb = new StringBuffer(); 
    	for (Progress.Message m: iLog) {
    		String s = m.toString(level);
    		if (s!=null) sb.append(s+"\n");
    	}
    	return sb.toString();
    }
    
    public String getHtmlLog(int level, boolean includeDate) {
    	StringBuffer sb = new StringBuffer(); 
    	for (Progress.Message m: iLog) {
    		String s = m.toHtmlString(level, includeDate);
    		if (s!=null) sb.append(s+"<br>");
    	}
    	return sb.toString();
    }
	
    public String getHtmlLog(int level, boolean includeDate, String fromStage) {
    	StringBuffer sb = new StringBuffer(); 
    	for (Progress.Message m: iLog) {
    		if (m.getLevel()==Progress.MSGLEVEL_STAGE && m.getMessage().equals(fromStage))
    			sb = new StringBuffer();
    		String s = m.toHtmlString(level, includeDate);
    		if (s!=null) sb.append(s+"<br>");
    	}
    	return sb.toString();
    }

	public void load(Element root) {
		/*
		XMLWriter writer = new XMLWriter(System.out, OutputFormat.createPrettyPrint());
		writer.write(root.getDocument());
		writer.flush();
		*/
		iLog.clear();
		int version = Integer.parseInt(root.attributeValue("version"));
		if (version==1) {
			for (Iterator i=root.elementIterator("msg");i.hasNext();)
				iLog.add(new Progress.Message((Element)i.next()));
		}		
	}
	public void save(Element root) {
		root.addAttribute("version", String.valueOf(sVersion));
    	for (Progress.Message msg: iLog) {
			if (msg.getLevel()<=sNoSaveThreshold) continue;
			msg.save(root.addElement("msg"));
		}
	}

	public boolean saveToFile() {
		return false;
	}
}
