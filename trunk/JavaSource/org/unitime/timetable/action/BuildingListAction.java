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
package org.unitime.timetable.action;

import java.text.DecimalFormat;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.cpsolver.ifs.util.DistanceMetric;

import org.hibernate.HibernateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unitime.commons.web.WebTable;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.model.Building;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;


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
		
		DistanceMetric.Ellipsoid ellipsoid = DistanceMetric.Ellipsoid.valueOf(ApplicationProperties.getProperty("unitime.distance.ellipsoid", DistanceMetric.Ellipsoid.LEGACY.name()));
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
