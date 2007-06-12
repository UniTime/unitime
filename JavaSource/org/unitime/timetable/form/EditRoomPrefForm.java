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
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.util.LabelValueBean;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.NonUniversityLocation;
import org.unitime.timetable.model.Preference;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.Roles;
import org.unitime.timetable.model.Room;
import org.unitime.timetable.model.RoomDept;
import org.unitime.timetable.model.RoomPref;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.dao.LocationDAO;
import org.unitime.timetable.model.dao.TimetableManagerDAO;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.DynamicList;
import org.unitime.timetable.util.DynamicListObjectFactory;


/** 
 * MyEclipse Struts
 * Creation date: 05-12-2006
 * 
 * XDoclet definition:
 * @struts.form name="editRoomPrefForm"
 */
public class EditRoomPrefForm extends ActionForm {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3038073479295582435L;
	// --------------------------------------------------------- Instance Variables
	private String doit;
	private String pref;
	private String name;
	private String[] selectedPref = {};
	private String id;
	private List roomPrefLevels;
	private String deptCode;
	private List depts;
	
    // --------------------------------------------------------- Classes
    /** Factory to create dynamic list element for Preference Level */
    protected DynamicListObjectFactory factoryPrefLevel = new DynamicListObjectFactory() {
        public Object create() {
            return new String(PreferenceLevel.PREF_LEVEL_NEUTRAL);
        }
    };
	// --------------------------------------------------------- Methods

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String[] getSelectedPref() {
		return selectedPref;
	}

	public void setSelectedPref(String[] selectedPref) {
		this.selectedPref = selectedPref;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/** 
	 * Method validate
	 * @param mapping
	 * @param request
	 * @return ActionErrors
	 */
	public ActionErrors validate(
		ActionMapping mapping,
		HttpServletRequest request) {
		
		ActionErrors errors = new ActionErrors();
		for (int i = 0; i< selectedPref.length; i++) {
			if (selectedPref[i].trim().equals(Preference.BLANK_PREF_VALUE)){
	            errors.add("roomPrefs", 
	                    new ActionMessage(
	                            "errors.generic", 
	                    		"Invalid room preference level.") );
	        }
		}
		return errors;
	}
	
    /**
     * Checks that pref levels are selected
     * @param lst List of pref levels
     * @return true if checks ok, false otherwise
     */
    public boolean checkPrefLevels(List lst) {
        
        for(int i=0; i<lst.size(); i++) {
            String value = ((String) lst.get(i));
            // No selection made
            if(value.trim().equals(Preference.BLANK_PREF_VALUE)) {
                return false;
            }
        }
        return true;
    }

	/** 
	 * Method reset
	 * @param mapping
	 * @param request
	 */
	public void reset(ActionMapping mapping, HttpServletRequest request) {
		roomPrefLevels = DynamicList.getInstance(new ArrayList(), factoryPrefLevel);
		depts = DynamicList.getInstance(new ArrayList(), factoryPrefLevel); 
		setDefaults(request);
	}

	/**
	 * 
	 * @param request
	 */
	private void setDefaults(HttpServletRequest request) {
		//get location information
		Long id = Long.valueOf(request.getParameter("id"));
		LocationDAO ldao = new LocationDAO();
		Location location = ldao.get(id);
		if (location instanceof Room) {
			Room r = (Room) location;
			name = (r.getLabel());
		} else if (location instanceof NonUniversityLocation) {
				NonUniversityLocation nonUnivLocation = (NonUniversityLocation) location;
				name = nonUnivLocation.getName();
		} 
		
		//get user preference information
		HttpSession webSession = request.getSession();
		User user = Web.getUser(webSession);
		boolean isAdmin = user.getRole().equals(Roles.ADMIN_ROLE);
		String mgrId = (String)user.getAttribute(Constants.TMTBL_MGR_ID_ATTR_NAME);
		TimetableManagerDAO tdao = new TimetableManagerDAO();
        TimetableManager owner = tdao.get(new Long(mgrId));
		Long sessionId;
		try {
			sessionId = Session.getCurrentAcadSession(user).getUniqueId();
		
	        ArrayList availableRoomPrefs = new ArrayList();
	        Set departments = new TreeSet();
			if (user.getRole().equals(Roles.ADMIN_ROLE)) {
				departments = Department.findAllBeingUsed(sessionId);
			} else {
				departments = owner.departmentsForSession(sessionId);
			}
	        //Set departments = new TreeSet(owner.departmentsForSession(sessionId));
			
	        Set availableDepts = new TreeSet();
	        for (Iterator iter = location.getRoomDepts().iterator(); iter.hasNext();) {
	        	RoomDept rd = (RoomDept) iter.next();
	        	Department d = rd.getDepartment();
	        	if (departments.contains(d)) {
	        		availableDepts.add(d);
	        	}
	        }
	        ArrayList list = new ArrayList();
	        list.addAll(availableDepts);
	        depts = list;
	        
	        Set rps = new HashSet();
	        ArrayList depts = new ArrayList();
	        String[] selectedPrefs = new String[availableDepts.size()];
	        int i = 0;
	        for (Iterator iter = availableDepts.iterator(); iter.hasNext();) {
	        	Department dept = (Department) iter.next();
	        	RoomPref roomPref = location.getRoomPreference(dept);
	        	if (roomPref != null) {
	        		selectedPrefs[i] = roomPref.getPrefLevel().getUniqueId().toString();
	        	} else{
	        		selectedPrefs[i] = PreferenceLevel.PREF_LEVEL_NEUTRAL;
	        	}
	        	i++;
		        depts.add(new LabelValueBean( dept.getDeptCode()+"-"+dept.getAbbreviation(), dept.getDeptCode())); 
	        }
	        selectedPref = selectedPrefs;
	        
	        request.setAttribute(Department.DEPT_ATTR_NAME, depts);
	        
	        //set default department
	        if (!isAdmin && (departments.size() == 1)) {
	        	Department d = (Department) departments.iterator().next();
	        	deptCode = d.getDeptCode();
	        } else if (webSession.getAttribute(Constants.DEPT_CODE_ATTR_ROOM_NAME) != null) {
	        	deptCode = webSession.getAttribute(
						Constants.DEPT_CODE_ATTR_ROOM_NAME).toString();
			}
			
	        //set availabe room preferences
	    	Vector prefs = new Vector();
	    	boolean containsPref = false; 
	    	for (Enumeration e=PreferenceLevel.getPreferenceLevelList(false).elements();e.hasMoreElements();) {
	    		PreferenceLevel pref = (PreferenceLevel)e.nextElement();
	    		if (!pref.getPrefProlog().equalsIgnoreCase(PreferenceLevel.sRequired))
	    			prefs.addElement(pref);
	    	}
	    	request.setAttribute(PreferenceLevel.PREF_LEVEL_ATTR_NAME, prefs);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
	}

	public String getDoit() {
		return doit;
	}

	public void setDoit(String doit) {
		this.doit = doit;
	}

	public String getPref() {
		return pref;
	}

	public void setPref(String pref) {
		this.pref = pref;
	}
	
    /**
     * @return Returns the roomPrefLevels.
     */
    public List getRoomPrefLevels() {
        return roomPrefLevels;
    }
    /**
     * @return Returns the roomPrefLevels.
     */
    public String getRoomPrefLevels(int key) {
        return roomPrefLevels.get(key).toString();
    }
    /**
     * @param roomPrefs The roomPrefLevels to set.
     */
    public void setRoomPrefLevels(int key, Object value) {
        this.roomPrefLevels.set(key, value);
    }
    /**
     * @param roomPrefs The roomPrefLevels to set.
     */
    public void setRoomPrefLevels(List roomPrefLevels) {
        this.roomPrefLevels = roomPrefLevels;
    }

	public String getDeptCode() {
		return deptCode;
	}

	public void setDeptCode(String deptCode) {
		this.deptCode = deptCode;
	}

	public List getDepts() {
		return depts;
	}

	public void setDepts(List depts) {
		this.depts = depts;
	}
	
    public String getDepts(int key) {
        return depts.get(key).toString();
    }
    
    public void setDepts(int key, Object value) {
        this.depts.set(key, value);
    }

}

