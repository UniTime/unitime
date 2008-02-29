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

import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.ExamPeriod;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.LocationDAO;


/** 
 * MyEclipse Struts
 * Creation date: 05-12-2006
 * 
 * XDoclet definition:
 * @struts.form name="editRoomPrefForm"
 */
public class EditRoomPerPrefForm extends ActionForm {

	/**
	 * 
	 */
	private static final long serialVersionUID = 0L;
	// --------------------------------------------------------- Instance Variables
	private String op;
    private Long id;
    private String name;
	private String[] periods;
	private String[] prefs;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getPref(int idx) {
		return prefs[idx];
	}

    public void setPref(int idx, String pref) {
        prefs[idx] = pref;
    }

    public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

    public String getOp() {
        return op;
    }

    public void setOp(String op) {
        this.op = op;
    }

    public String[] getPeriods() {
        return periods;
    }

    public void setPeriods(String[] periods) {
        this.periods = periods;
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
		return errors;
	}
	
	/** 
	 * Method reset
	 * @param mapping
	 * @param request
	 */
	public void reset(ActionMapping mapping, HttpServletRequest request) {
		id = null; name = null; op = "Save"; 
		try {
	        User user = Web.getUser(request.getSession());
	        Session session = Session.getCurrentAcadSession(user);
	        TreeSet eps = ExamPeriod.findAll(session.getUniqueId());
	        periods = new String[eps.size()];
	        prefs = new String[eps.size()];
	        int idx = 0;
	        for (Iterator i=eps.iterator();i.hasNext();idx++) {
	            ExamPeriod ep = (ExamPeriod)i.next();
	            periods[idx] = ep.getName();
	            prefs[idx] = PreferenceLevel.sNeutral;
	        }
		} catch (Exception e) {}
	}

    public List getPreferenceLevels() {
        return PreferenceLevel.getPreferenceLevelList(false);
    }
    
    public void load(Location location) {
        TreeSet eps = ExamPeriod.findAll(location.getSession().getUniqueId());
        periods = new String[eps.size()];
        prefs = new String[eps.size()];
        int idx = 0;
        for (Iterator i=eps.iterator();i.hasNext();idx++) {
            ExamPeriod ep = (ExamPeriod)i.next();
            PreferenceLevel pref = location.getExamPreference(ep);
            periods[idx] = ep.getName();
            prefs[idx] = pref.getPrefProlog();
        }
        id = location.getUniqueId();
        name = location.getLabel();
    }
    
    public void save(HttpServletRequest request) {
        Location location = new LocationDAO().get(getId());
        location.clearExamPreferences();
        TreeSet eps = ExamPeriod.findAll(location.getSession().getUniqueId());
        int idx = 0;
        for (Iterator i=eps.iterator();i.hasNext();idx++) {
            ExamPeriod ep = (ExamPeriod)i.next();
            PreferenceLevel pref = PreferenceLevel.getPreferenceLevel(prefs[idx]);
            location.addExamPreference(ep, pref);
        }
        location.saveOrUpdate();
        ChangeLog.addChange(
                null, 
                request, 
                location, 
                ChangeLog.Source.ROOM_EXAM_PERIOD_REF_EDIT, 
                ChangeLog.Operation.UPDATE, 
                null, 
                null);

    }

}

