/*
 * UniTime 3.0 (University Course Timetabling & Student Sectioning Application)
 * Copyright (C) 2007, UniTime.org, and individual contributors
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
package org.unitime.timetable.form;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.util.LabelValueBean;
import org.unitime.commons.Debug;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.Roles;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.dao.TimetableManagerDAO;
import org.unitime.timetable.util.Constants;


/** 
* MyEclipse Struts
* Creation date: 02-18-2005
* 
* XDoclet definition:
* @struts:form name="roomFeatureListForm"
*/
public class RoomFeatureListForm extends ActionForm {

	private static final long serialVersionUID = 3256728385592768053L;
	/**
	 * 
	 */
	// --------------------------------------------------------- Instance Variables
	private Collection globalRoomFeatures;
	private Collection departmentRoomFeatures;
	private String deptCode;
	private boolean deptSize;
	private boolean canAdd;
	
	// --------------------------------------------------------- Methods

	public String getDeptCodeX() {
		return deptCode;
	}


	public void setDeptCodeX(String deptCode) {
		this.deptCode = deptCode;
	}


	/** 
	 * Method reset
	 * @param mapping
	 * @param request
	 */
	public void reset(ActionMapping mapping, HttpServletRequest request) {

		globalRoomFeatures = new ArrayList();
		departmentRoomFeatures = new ArrayList();
		deptSize=displayDeptList(request);
		canAdd = false;
		try {
			setDeptAttr(request);
		} catch (Exception e) {
			Debug.error(e);
		}
	}
	
	/**
	 * 
	 * @param request
	 * @return
	 */
    private boolean displayDeptList(HttpServletRequest request) {
    	deptSize = true;
    	User user = Web.getUser(request.getSession());
    	Long sessionId = (Long) user.getAttribute(Constants.SESSION_ID_ATTR_NAME);
    	if (!user.getRole().equals(Roles.ADMIN_ROLE) && !user.getRole().equals(Roles.EXAM_MGR_ROLE)) {
	    	TimetableManager mgr = TimetableManager.getManager(user);
	    	Set mgrDepts = Department.findAllOwned(sessionId, mgr, true);
	    	if (mgrDepts.size() == 1) {
	    		deptSize = false;
	    	}
    	} 
    	return deptSize;
	}

	public Collection getGlobalRoomFeatures() {
		return globalRoomFeatures;
	}
	public void setGlobalRoomFeatures(Collection globalRoomFeatures) {
		this.globalRoomFeatures = globalRoomFeatures;
	}
	public Collection getDepartmentRoomFeatures() {
		return departmentRoomFeatures;
	}
	public void setDepartmentRoomFeatures(Collection departmentRoomFeatures) {
		this.departmentRoomFeatures = departmentRoomFeatures;
	}


	public boolean isDeptSize() {
		return deptSize;
	}

	public void setDeptSize(boolean deptSize) {
		this.deptSize = deptSize;
	}
	
	/* (non-Javadoc)
     * @see org.apache.struts.action.ActionForm#validate(org.apache.struts.action.ActionMapping, javax.servlet.http.HttpServletRequest)
     */
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();
      
        /*
        if(deptCode==null || deptCode.equalsIgnoreCase("")) {
        	errors.add("deptCode", 
                    new ActionMessage("errors.required", "Department") );
        }
        */
       
        return errors;
    }
    
	/**
	 * 
	 * @param request
	 * @throws Exception
	 */
	private void setDeptAttr(HttpServletRequest request) throws Exception {
		HttpSession webSession = request.getSession();
		User user = Web.getUser(webSession);
		Long sessionId = Session.getCurrentAcadSession(user).getSessionId();
		ArrayList departments = new ArrayList();
		
		String mgrId = (String)user.getAttribute(Constants.TMTBL_MGR_ID_ATTR_NAME);
		TimetableManagerDAO tdao = new TimetableManagerDAO();
	    TimetableManager owner = tdao.get(new Long(mgrId));
	        
	    
	    TreeSet depts = null;
	    if (user.getRole().equals(Roles.ADMIN_ROLE)) {
        	depts = Department.findAllBeingUsed(sessionId); 
        } else {
        	depts = Department.findAllOwned(sessionId, owner, true);
        }
	
		for (Iterator i=depts.iterator();i.hasNext();) {
			Department d = (Department)i.next();
			String code = d.getDeptCode().trim();
			String abbv = d.getName().trim();
			if (d.isExternalManager().booleanValue()) {
				departments.add(new LabelValueBean(code + " - " + abbv + " ("+d.getExternalMgrLabel()+")", code));
			} else {
				departments.add(new LabelValueBean(code + " - " + abbv, code));
			}
		}
		
		request.setAttribute(Department.DEPT_ATTR_NAME, departments);
		if (webSession.getAttribute(Constants.DEPT_CODE_ATTR_ROOM_NAME) != null) {
			deptCode = (String) webSession.getAttribute(Constants.DEPT_CODE_ATTR_ROOM_NAME);
		}
	}

	public void setCanAdd(boolean canAdd) {
		this.canAdd = canAdd;
	}
	public boolean getCanAdd() {
		return canAdd;
	}
}