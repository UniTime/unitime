/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2008 - 2010, UniTime LLC, and individual contributors
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
package org.unitime.timetable.webutil;

import org.unitime.commons.web.htmlgen.TableCell;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.PreferenceGroup;


/**
 * @author Stephanie Schluttenhofer
 *
 */
public class WebSubpartClassListTableBuilder extends WebClassListTableBuilder {

	/**
	 * 
	 */
	public WebSubpartClassListTableBuilder() {
		super();
	}
	
	private String addDeleteAnchor(Class_ aClass){
		StringBuffer sb = new StringBuffer();
		sb.append("<A onclick=\" if(confirm('Are you sure you want to delete this class section? Continue?')) {"); 
		sb.append("document.location.href='schedulingSubpartEdit.do?op=DeleteClass&ssuid=");
	    sb.append(aClass.getSchedulingSubpart().getUniqueId().toString());
        sb.append("&classId=");
        sb.append(aClass.getUniqueId());
        sb.append("'; return true; } else { return false; }");
        sb.append("\" >");
		sb.append("<IMG src=\"images/Delete16.gif\" border=\"0\" alt=\"Delete Class\" title=\"Delete Class\" align=\"top\">");
		sb.append("</A> ");
		return(sb.toString());
	}

    protected TableCell buildPrefGroupLabel(CourseOffering co, PreferenceGroup prefGroup, String indentSpaces, boolean isEditable){
    	if (prefGroup instanceof Class_) {
    		TableCell cell = initNormalCell(indentSpaces, isEditable);
        	if ("PreferenceGroup".equals(getBackType()) && prefGroup.getUniqueId().toString().equals(getBackId()))
        		cell.addContent("<A name=\"back\"></A>");
    		Class_ aClass = (Class_) prefGroup;
	    	if(!isEditable){
	    		cell.addContent("<font color=gray>");
	    	}
	        cell.addContent("<A name=\"A" + prefGroup.getUniqueId().toString() + "\"></A>");
	        if (isEditable && aClass.canBeDeleted()){
	            cell.addContent(this.addDeleteAnchor(aClass));	
	        }
	        if (isEditable){
	        	cell.addContent("<A onclick=\"document.location='classEdit.do?cid=" + aClass.getUniqueId().toString() + "&sec=" + aClass.getSectionNumberString() + "'\" >");
	        }
	    	cell.addContent("<b>");
	        cell.addContent(aClass.getClassLabel(co));
	        cell.addContent("</b>");
	        if (isEditable){
	        	cell.addContent("</A>");
	        }
	        cell.setNoWrap(true);
	        if(!isEditable){
	        	cell.addContent("</font>");
	        }
	        return(cell);
        } else {
        	return(super.buildPrefGroupLabel(co, prefGroup,indentSpaces, isEditable, null));
        }
    	
        
    }
}
