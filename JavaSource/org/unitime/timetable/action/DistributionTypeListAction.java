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
package org.unitime.timetable.action;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.tiles.annotation.TilesDefinition;
import org.apache.struts2.tiles.annotation.TilesPutAttribute;
import org.hibernate.HibernateException;
import org.springframework.stereotype.Service;
import org.unitime.commons.web.WebTable;
import org.unitime.commons.web.WebTable.WebTableLine;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.form.BlankForm;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DistributionType;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.security.rights.Right;


/** 
 * @author Tomas Muller
 */
@Service("/distributionTypeList")
@Action(value = "distributionTypeList", results = {
		@Result(name = "showDistributionTypeList", type = "tiles", location = "distributionTypeList.tiles")
	})
@TilesDefinition(name = "distributionTypeList.tiles", extend = "baseLayout", putAttributes =  {
		@TilesPutAttribute(name = "title", value = "Distribution Types"),
		@TilesPutAttribute(name = "body", value = "/admin/distributionTypeList.jsp")
	})
public class DistributionTypeListAction extends UniTimeAction<BlankForm> {
	private static final long serialVersionUID = -5869733478310566508L;
	protected static CourseMessages MSG = Localization.create(CourseMessages.class);

	@Override
	public String execute() throws HibernateException {
		sessionContext.checkPermission(Right.DistributionTypes);
		return "showDistributionTypeList";
	}
	
	public String getTable() {
		List<DistributionType> distTypes = new ArrayList<DistributionType>();
		distTypes.addAll(DistributionType.findAll(false, false, null));
		distTypes.addAll(DistributionType.findAll(false, true, null));
		
	    WebTable webTable = new WebTable( 11,
	    	    MSG.sectDistributionTypes(),
	    	    "distributionTypeList.action?ord=%%",
	    	    new String[] {
	    	    		MSG.fieldId(),
	    	    		MSG.fieldReference(),
	    	    		MSG.fieldAbbreviation(),
	    	    		MSG.fieldName(),
	    	    		MSG.fieldType(),
	    	    		MSG.fieldVisible(),
	    	    		MSG.fieldAllowInstructorPreference(),
	    	    		MSG.fieldSequencingRequired(),
	    	    		MSG.fieldAllowPreferences(),
	    	    		MSG.fieldDepartments(),
	    	    		MSG.fieldDescription()
	    	    		},
	    	    new String[] {"left", "left", "left", "left", "left", "center", "center", "center", "center", "left", "left"}, 
	    	    new boolean[] {true, true, true, true, true, true, true, true, true, true, true} );
	    
	    WebTable.setOrder(sessionContext,"DistributionTypeList.ord",request.getParameter("ord"),1);
	    boolean edit = sessionContext.hasPermission(Right.DistributionTypeEdit);
	    
	    for (DistributionType d: distTypes) {
		    String allowPref = null;
		    if ("".equals(d.getAllowedPref())) {
		    	allowPref = "<i>" + MSG.itemNone() + "</i>";
		    } else if ("P43210R".equals(d.getAllowedPref())) {
		    	allowPref = "<i>" + MSG.itemAll() + "</i>";
		    } else {
		    	for (PreferenceLevel p: PreferenceLevel.getPreferenceLevelList()) {
		    		if (d.getAllowedPref().indexOf(PreferenceLevel.prolog2char(p.getPrefProlog()))<0) continue;
		    		if (allowPref==null)
		    			allowPref="";
		    		else
		    			allowPref+="<br>";
		    		if (PreferenceLevel.sNeutral.equals(p.getPrefProlog()))
		    			allowPref += p.getPrefName();
		    		else
		    			allowPref += "<span style='color:"+p.prefcolor()+";'>"+p.getPrefName().replaceAll(" ","&nbsp;")+"</span>";
		    	}
		    }
		    String deptStr = "";
		    String deptCmp = "";
		    for (Iterator i = d.getDepartments(sessionContext.getUser().getCurrentAcademicSessionId()).iterator();i.hasNext();) {
		    	Department x = (Department)i.next();
		    	deptStr += x.getManagingDeptAbbv().trim();
		    	deptCmp += x.getDeptCode();
		    	if (i.hasNext()) { deptStr += ", "; deptCmp += ","; }
		    }
		    WebTableLine line = webTable.addLine(
		    	edit ? "onClick=\"document.location='distributionTypeEdit.do?id="+d.getUniqueId()+"';\"" : null,
		    	new String[] {
		    		d.getRequirementId().toString(),
		    		d.getReference(),
		    		d.getAbbreviation(),
		    		d.getLabel(),
		    		d.isExamPref().booleanValue() ? MSG.itemDistTypeExams() : MSG.itemDistTypeCourses(),
		    		d.isVisible() ? MSG.yes() : MSG.no(),
		    		d.isExamPref().booleanValue()? MSG.notApplicable() : d.isInstructorPref().booleanValue() ? MSG.yes() : MSG.no(),
		    		d.isSequencingRequired() ? MSG.yes() : MSG.no(),
		    		allowPref,
		    		(deptStr.length()==0?"<i>" + MSG.itemAll() + "</i>":deptStr),
		    		d.getDescr()
		    	}, 
		    	new Comparable[] {
		    		d.getRequirementId(),
		    		d.getReference(),
		    		d.getAbbreviation(),
		    		d.getLabel(),
		    		Integer.valueOf(d.isExamPref().booleanValue()?1:0),
		    		d.isVisible(),
		    		Integer.valueOf(d.isInstructorPref().booleanValue()?1:0),
		    		Integer.valueOf(d.isSequencingRequired()?1:0),
		    		null,
		    		deptCmp,
		    		d.getDescr()
		    	},null);
		    if (!d.isVisible())
		    	line.setStyle("color:gray;");
	    }
	    return webTable.printTable(WebTable.getOrder(sessionContext,"DistributionTypeList.ord"));
	}

}
