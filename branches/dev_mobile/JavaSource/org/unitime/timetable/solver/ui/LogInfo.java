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

	public void load(Element root) throws Exception {
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
	public void save(Element root) throws Exception {
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
