/*
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2008, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package org.unitime.timetable.action;

import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.unitime.commons.Debug;
import org.unitime.commons.web.Web;
import org.unitime.commons.web.WebTable;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.dao.PreferenceLevelDAO;


/** 
 * @author Tomas Muller
 */
public class PreferenceLevelListAction extends Action {

	// --------------------------------------------------------- Instance Variables

	// --------------------------------------------------------- Methods

	/** 
	 * Method execute
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return ActionForward
	 */
	public ActionForward execute(
		ActionMapping mapping,
		ActionForm form,
		HttpServletRequest request,
		HttpServletResponse response) throws Exception {
		
	       String errM = "";
	        
	        // Check if user is logged in
		    HttpSession webSession = request.getSession();
	        if(!Web.isLoggedIn( webSession )) {
	            throw new Exception ("Access Denied.");
	        }

	        org.hibernate.Session hibSession = null;

			// Create new table
		    WebTable webTable = new WebTable( 3,
		    	    "Preferences Level",
		    	    new String[] {"ID", "Prolog", "Name"},
		    	    new String[] {"left", "left","left"},
		    	    null );

	        // Loop through PreferenceLevel class
			try {
				PreferenceLevelDAO prefLevelDao = new PreferenceLevelDAO();
				hibSession = prefLevelDao.getSession();
				
				List prefsLevelList = hibSession.createCriteria(PreferenceLevel.class)
										.list();
				Iterator iterPrefsLevel = prefsLevelList.iterator();
				
				while(iterPrefsLevel.hasNext()) {
					PreferenceLevel prefsLevel = (PreferenceLevel) iterPrefsLevel.next();
					
				    // Add to web table
				    webTable.addLine(
			        	null,
			        	new String[] {prefsLevel.getPrefId().toString(),
			        					prefsLevel.getPrefProlog(), 
			        					prefsLevel.getPrefName()},
			        	null, null);
				}

	        }
	        catch (Exception e) {
				Debug.error(e);
	            return mapping.findForward("fail");
	        }
	        finally {
	        	//if (hibSession!=null && hibSession.isOpen()) hibSession.close();	        	
	        }

	        String tblData = webTable.printTable();
	        request.setAttribute("prefsLevelList", errM + tblData);
	        return mapping.findForward("success");
	    }
	}


