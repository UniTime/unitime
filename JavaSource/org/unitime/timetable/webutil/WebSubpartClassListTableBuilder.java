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
package org.unitime.timetable.webutil;

import org.unitime.commons.web.htmlgen.TableCell;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.PreferenceGroup;


/**
 * @author Stephanie Schluttenhofer, Tomas Muller
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
		sb.append("<IMG src=\"images/action_delete.png\" border=\"0\" alt=\"Delete Class\" title=\"Delete Class\" align=\"top\">");
		sb.append("</A> ");
		return(sb.toString());
	}

	@Override
    protected TableCell buildPrefGroupLabel(CourseOffering co, PreferenceGroup prefGroup, int indentSpaces, boolean isEditable, String prevLabel, String icon){
    	if (prefGroup instanceof Class_) {
    		TableCell cell = initNormalCell("", isEditable);
        	if (indentSpaces > 0) {
        		int pad = indentSpaces * indent;
        		if (icon != null) pad -= indent;
        		cell.setStyle("padding-left: " + pad + "px;");
        	}
            if (icon != null) cell.addContent(icon);
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
        	return(super.buildPrefGroupLabel(co, prefGroup,indentSpaces, isEditable, prevLabel, icon));
        }
    	
        
    }
}
