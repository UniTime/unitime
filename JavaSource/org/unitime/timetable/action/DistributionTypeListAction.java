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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.hibernate.HibernateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unitime.commons.web.WebTable;
import org.unitime.commons.web.WebTable.WebTableLine;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DistributionType;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;


/** 
 * MyEclipse Struts
 * Creation date: 02-18-2005
 * 
 * XDoclet definition:
 * @struts:action path="/distributionTypeList" name="distributionTypeListForm" input="/admin/distributionTypeList.jsp" scope="request" validate="true"
 *
 * @author Tomas Muller
 */
@Service("/distributionTypeList")
public class DistributionTypeListAction extends Action {

	@Autowired SessionContext sessionContext;
	// --------------------------------------------------------- Instance Variables

	// --------------------------------------------------------- Methods

	/** 
	 * Method execute
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return ActionForward
	 * @throws HibernateException
	 */
	public ActionForward execute(
		ActionMapping mapping,
		ActionForm form,
		HttpServletRequest request,
		HttpServletResponse response) throws HibernateException {
		sessionContext.checkPermission(Right.DistributionTypes);
		
		List<DistributionType> distTypes = new ArrayList<DistributionType>();
		distTypes.addAll(DistributionType.findAll(false, false, null));
		distTypes.addAll(DistributionType.findAll(false, true, null));
		
	    WebTable webTable = new WebTable( 11,
	    	    "Distribution Types",
	    	    "distributionTypeList.do?ord=%%",
	    	    new String[] {"Id", "Reference", "Abbreviation", "Name", "Type", "Visible", "Allow Instructor Preference", "Sequencing Required", "Allow Preferences", "Departments", "Description"},
	    	    new String[] {"left", "left", "left", "left", "left", "center", "center", "center", "center", "left", "left"}, 
	    	    new boolean[] {true, true, true, true, true, true, true, true, true, true, true} );
	    
	    WebTable.setOrder(sessionContext,"DistributionTypeList.ord",request.getParameter("ord"),1);
	    boolean edit = sessionContext.hasPermission(Right.DistributionTypeEdit);
	    
	    for (DistributionType d: distTypes) {
		    String allowPref = null;
		    if ("".equals(d.getAllowedPref())) {
		    	allowPref = "<i>None</i>";
		    } else if ("P43210R".equals(d.getAllowedPref())) {
		    	allowPref = "<i>All</i>";
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
		    		d.isExamPref().booleanValue()?"Examination":"Course",
		    		d.isVisible() ? "Yes" : "No",
		    		d.isExamPref().booleanValue()?"N/A":d.isInstructorPref().booleanValue()?"Yes":"No",
		    		d.isSequencingRequired()?"Yes":"No",
		    		allowPref,
		    		(deptStr.length()==0?"<i>All</i>":deptStr),
		    		d.getDescr()
		    	}, 
		    	new Comparable[] {
		    		d.getRequirementId(),
		    		d.getReference(),
		    		d.getAbbreviation(),
		    		d.getLabel(),
		    		new Integer(d.isExamPref().booleanValue()?1:0),
		    		d.isVisible(),
		    		new Integer(d.isInstructorPref().booleanValue()?1:0),
		    		new Integer(d.isSequencingRequired()?1:0),
		    		null,
		    		deptCmp,
		    		d.getDescr()
		    	},null);
		    if (!d.isVisible())
		    	line.setStyle("color:gray;");
	    }
	    
	    request.setAttribute("table",webTable.printTable(WebTable.getOrder(sessionContext,"DistributionTypeList.ord")));
		
		return mapping.findForward("showDistributionTypeList");
		
	}

}
