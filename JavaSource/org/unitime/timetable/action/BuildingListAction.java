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

import java.text.DecimalFormat;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import org.hibernate.HibernateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unitime.commons.web.WebTable;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.model.Building;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.cpsolver.ifs.util.DistanceMetric;


/** 
* MyEclipse Struts
* Creation date: 02-18-2005
* 
* XDoclet definition:
* @struts:action path="/BuildingList" name="buildingListForm" input="/admin/buildingList.jsp" scope="request" validate="true"
*/
/**
 * @author Tomas Muller
 */
@Service("/buildingList")
public class BuildingListAction extends Action {
	
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
		HttpServletResponse response) throws Exception {
		
		sessionContext.checkPermission(Right.BuildingList);
		
		DistanceMetric.Ellipsoid ellipsoid = DistanceMetric.Ellipsoid.valueOf(ApplicationProperty.DistanceEllipsoid.value());
	    WebTable webTable = new WebTable( 5,
	    		null, "buildingList.do?ord=%%",
	    		new String[] {"Abbreviation", "Name", "External ID", ellipsoid.getFirstCoordinateName(), ellipsoid.getSecondCoordinateName()},
	    		new String[] {"left", "left","left","right","right"},
	    		new boolean[] {true,true,true,true,true} );
	    WebTable.setOrder(sessionContext, "BuildingList.ord", request.getParameter("ord"), 1);
	    
	    DecimalFormat df5 = new DecimalFormat("####0.######");
	    for (Building b: Building.findAll(sessionContext.getUser().getCurrentAcademicSessionId())) {
	    	webTable.addLine(
	    			"onClick=\"document.location='buildingEdit.do?op=Edit&id="+b.getUniqueId()+"';\"",
	    			new String[] {
	    				b.getAbbreviation(),
	    				b.getName(),
	    				b.getExternalUniqueId()==null?"<i>N/A</i>":b.getExternalUniqueId().toString(),
	    				(b.getCoordinateX()==null ? "" : df5.format(b.getCoordinateX())),
	    				(b.getCoordinateY()==null ? "" : df5.format(b.getCoordinateY()))
	    				}, 
	    			new Comparable[] {
	    				b.getAbbreviation(),
	    				b.getName(),
	    				b.getExternalUniqueId()==null?"":b.getExternalUniqueId().toString(),
	    				b.getCoordinateX(),
	    				b.getCoordinateY()
	    				});
	    }
	    
	    request.setAttribute("table", webTable.printTable(WebTable.getOrder(sessionContext, "BuildingList.ord")));

	    return mapping.findForward("showBuildingList");
	}

}
