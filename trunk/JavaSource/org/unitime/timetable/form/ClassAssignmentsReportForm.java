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
package org.unitime.timetable.form;

import java.util.ArrayList;
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.unitime.timetable.model.comparators.ClassCourseComparator;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.LookupTables;


/**
 * @author Stephanie Schluttenhofer
 */
public class ClassAssignmentsReportForm extends ActionForm implements ClassListFormInterface {
	/**
	 * Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = 3257854294022959921L;	
	private Collection classes;
	private Collection subjectAreas;
	private String[] subjectAreaIds; 
	private String buttonAction;
	private String subjectAreaAbbv;
	private String ctrlInstrOfferingId;
	
	private String sortBy;
	private String filterAssignedRoom;
	private String filterManager;
	private String filterIType;
	private boolean filterAssignedTimeMon;
	private boolean filterAssignedTimeTue;
	private boolean filterAssignedTimeWed;
	private boolean filterAssignedTimeThu;
	private boolean filterAssignedTimeFri;
	private boolean filterAssignedTimeSat;
	private boolean filterAssignedTimeSun;
	private String filterAssignedTimeHour;
	private String filterAssignedTimeMin;
	private String filterAssignedTimeAmPm;
	private String filterAssignedTimeLength;
	private boolean sortByKeepSubparts;
	private boolean showCrossListedClasses;
    
	private boolean returnAllControlClassesForSubjects;
	private String[] userDeptIds;
	
	/**
     * @return Returns the ctrlInstrOfferingId.
     */
    public String getCtrlInstrOfferingId() {
        return ctrlInstrOfferingId;
    }
    /**
     * @param ctrlInstrOfferingId The ctrlInstrOfferingId to set.
     */
    public void setCtrlInstrOfferingId(String ctrlInstrOfferingId) {
        this.ctrlInstrOfferingId = ctrlInstrOfferingId;
    }
    /**
     * @return Returns the subjectAreaAbbv.
     */
    public String getSubjectAreaAbbv() {
        return subjectAreaAbbv;
    }
    /**
     * @param subjectAreaAbbv The subjectAreaAbbv to set.
     */
    public void setSubjectAreaAbbv(String subjectAreaAbbv) {
        this.subjectAreaAbbv = subjectAreaAbbv;
    }
    /**
     * @return Returns the buttonAction.
     */
    public String getButtonAction() {
        return buttonAction;
    }
    /**
     * @param buttonAction The buttonAction to set.
     */
    public void setButtonAction(String buttonAction) {
        this.buttonAction = buttonAction;
    }
    /**
	 * @return Returns the subjectAreaIds.
	 */
	public String[] getSubjectAreaIds() {
		return subjectAreaIds;
	}
	/**
	 * @param subjectAreaIds The subjectAreaIds to set.
	 */
	public void setSubjectAreaIds(String[] subjectAreaIds) {
		this.subjectAreaIds = subjectAreaIds;
	}

    // --------------------------------------------------------- Methods
	/** 
	 * Method reset
	 * @param mapping
	 * @param request
	 */
	public void reset(ActionMapping mapping, HttpServletRequest request) {
	    init();
		LookupTables.setupItypes(request,true);
	}
	public void init(){
		classes = new ArrayList();
		subjectAreas = new ArrayList();
		subjectAreaIds = new String[0];

		sortBy = ClassCourseComparator.getName(ClassCourseComparator.SortBy.NAME);
		filterManager = "";
		filterAssignedRoom = "";
		filterIType = "";
		sortByKeepSubparts = false;
		
		filterAssignedTimeMon = false;
		filterAssignedTimeTue = false;
		filterAssignedTimeWed = false;
		filterAssignedTimeThu = false;
		filterAssignedTimeFri = false;
		filterAssignedTimeSat = false;
		filterAssignedTimeSun = false;
		filterAssignedTimeHour = "";
		filterAssignedTimeMin = "";
		filterAssignedTimeAmPm = "";
		filterAssignedTimeLength = "";
		
		returnAllControlClassesForSubjects = true;
		userDeptIds = new String[0];
		showCrossListedClasses = false;
	}

    /**
     * @return Returns the classes.
     */
    public Collection getClasses() {
        return classes;
    }
    /**
     * @param classes The classes to set.
     */
    public void setClasses(Collection classes) {
        this.classes = classes;
    }
    /**
     * @return Returns the subjectAreas.
     */
    public Collection getSubjectAreas() {
        return subjectAreas;
    }
    /**
     * @param subjectAreas The subjectAreas to set.
     */
    public void setSubjectAreas(Collection subjectAreas) {
        this.subjectAreas = subjectAreas;
    }
    /* (non-Javadoc)
     * @see org.apache.struts.action.ActionForm#validate(org.apache.struts.action.ActionMapping, javax.servlet.http.HttpServletRequest)
     */
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();
        return errors;
    }
    
	public String getSortBy() { return sortBy; }
	public void setSortBy(String sortBy) { this.sortBy = sortBy; }
	public String[] getSortByOptions() { return ClassCourseComparator.getNames(); }
	public String getFilterManager() { return filterManager; }
	public void setFilterManager(String filterManager) { this.filterManager = filterManager; }
	public String getFilterAssignedRoom() { return filterAssignedRoom; }
	public void setFilterAssignedRoom(String filterAssignedRoom) { this.filterAssignedRoom = filterAssignedRoom; }
	/*
	public String getFilterInstructor() { return filterInstructor; }
	public void setFilterInstructor(String filterInstructor) { this.filterInstructor = filterInstructor; }
	*/
	//NO instructor is shown on class assignment page -> display no filter
	public String getFilterInstructor() { return ""; }
	public String getFilterIType() { return filterIType; }
	public void setFilterIType(String filterIType) { this.filterIType = filterIType; }

	public boolean getFilterAssignedTimeMon() { return filterAssignedTimeMon; }
	public void setFilterAssignedTimeMon(boolean filterAssignedTimeMon) { this.filterAssignedTimeMon = filterAssignedTimeMon; }
	public boolean getFilterAssignedTimeTue() { return filterAssignedTimeTue; }
	public void setFilterAssignedTimeTue(boolean filterAssignedTimeTue) { this.filterAssignedTimeTue = filterAssignedTimeTue; }
	public boolean getFilterAssignedTimeWed() { return filterAssignedTimeWed; }
	public void setFilterAssignedTimeWed(boolean filterAssignedTimeWed) { this.filterAssignedTimeWed = filterAssignedTimeWed; }
	public boolean getFilterAssignedTimeThu() { return filterAssignedTimeThu; }
	public void setFilterAssignedTimeThu(boolean filterAssignedTimeThu) { this.filterAssignedTimeThu = filterAssignedTimeThu; }
	public boolean getFilterAssignedTimeFri() { return filterAssignedTimeFri; }
	public void setFilterAssignedTimeFri(boolean filterAssignedTimeFri) { this.filterAssignedTimeFri = filterAssignedTimeFri; }
	public boolean getFilterAssignedTimeSat() { return filterAssignedTimeSat; }
	public void setFilterAssignedTimeSat(boolean filterAssignedTimeSat) { this.filterAssignedTimeSat = filterAssignedTimeSat; }
	public boolean getFilterAssignedTimeSun() { return filterAssignedTimeSun; }
	public void setFilterAssignedTimeSun(boolean filterAssignedTimeSun) { this.filterAssignedTimeSun = filterAssignedTimeSun; }
	public String getFilterAssignedTimeHour() { return filterAssignedTimeHour; }
	public void setFilterAssignedTimeHour(String filterAssignedTimeHour) { this.filterAssignedTimeHour = filterAssignedTimeHour; }
	public String getFilterAssignedTimeMin() { return filterAssignedTimeMin; }
	public void setFilterAssignedTimeMin(String filterAssignedTimeMin) { this.filterAssignedTimeMin = filterAssignedTimeMin; }
	public String getFilterAssignedTimeAmPm() { return filterAssignedTimeAmPm; }
	public void setFilterAssignedTimeAmPm(String filterAssignedTimeAmPm) { this.filterAssignedTimeAmPm = filterAssignedTimeAmPm; }
	public String getFilterAssignedTimeLength() { return ("0".equals(filterAssignedTimeLength)?"":filterAssignedTimeLength); }
	public void setFilterAssignedTimeLength(String filterAssignedTimeLength) { this.filterAssignedTimeLength = filterAssignedTimeLength; }
	
	public String[] getFilterAssignedTimeHours() {
		return new String[] { "", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12" };
	}
	public String[] getFilterAssignedTimeMins() {
		return new String[] { "", "00", "05", "10", "15", "20", "25", "30", "35", "40", "45", "50", "55" };
	}
	public String[] getFilterAssignedTimeAmPms() {
		return new String[] { "", "am", "pm"};
	}
	public String[] getFilterAssignedTimeLengths() {
		String[] ret = new String[41];
		ret[0]="";
		for (int i=1;i<ret.length;i++)
			ret[i] = String.valueOf(5*i);
		return ret;
	}
	
	public int getFilterDayCode() {
		int dayCode = 0;
		if (filterAssignedTimeMon)
			dayCode += Constants.DAY_CODES[Constants.DAY_MON];
		if (filterAssignedTimeTue)
			dayCode += Constants.DAY_CODES[Constants.DAY_TUE];
		if (filterAssignedTimeWed)
			dayCode += Constants.DAY_CODES[Constants.DAY_WED];
		if (filterAssignedTimeThu)
			dayCode += Constants.DAY_CODES[Constants.DAY_THU];
		if (filterAssignedTimeFri)
			dayCode += Constants.DAY_CODES[Constants.DAY_FRI];
		if (filterAssignedTimeSat)
			dayCode += Constants.DAY_CODES[Constants.DAY_SAT];
		if (filterAssignedTimeSun)
			dayCode += Constants.DAY_CODES[Constants.DAY_SUN];
		return dayCode;
	}
	public void setFilterDayCode(int dayCode) {
		if (dayCode>=0) {
			filterAssignedTimeMon = (dayCode & Constants.DAY_CODES[Constants.DAY_MON]) != 0;
			filterAssignedTimeTue = (dayCode & Constants.DAY_CODES[Constants.DAY_TUE]) != 0;
			filterAssignedTimeWed = (dayCode & Constants.DAY_CODES[Constants.DAY_WED]) != 0;
			filterAssignedTimeThu = (dayCode & Constants.DAY_CODES[Constants.DAY_THU]) != 0;
			filterAssignedTimeFri = (dayCode & Constants.DAY_CODES[Constants.DAY_FRI]) != 0;
			filterAssignedTimeSat = (dayCode & Constants.DAY_CODES[Constants.DAY_SAT]) != 0;
			filterAssignedTimeSun = (dayCode & Constants.DAY_CODES[Constants.DAY_SUN]) != 0;
		} else {
			filterAssignedTimeMon = false;
			filterAssignedTimeTue = false;
			filterAssignedTimeWed = false;
			filterAssignedTimeThu = false;
			filterAssignedTimeFri = false;
			filterAssignedTimeSat = false;
			filterAssignedTimeSun = false;
		}
	}
	public int getFilterStartSlot() {
		try {
			int hour = Integer.parseInt(filterAssignedTimeHour);
			int min = Integer.parseInt(filterAssignedTimeMin);
			boolean morn = !("pm".equals(filterAssignedTimeAmPm));
			if (hour==12) hour=0;
			int startTime = ((hour+(morn?0:12))%24)*60 + min;
			return (startTime - Constants.FIRST_SLOT_TIME_MIN) / Constants.SLOT_LENGTH_MIN;			
		} catch (NumberFormatException e) {
		} catch (NullPointerException e) {
		}
		return -1;
	}
	public void setFilterStartSlot(int startSlot) {
		if (startSlot>=0) {
			int startMin = startSlot * Constants.SLOT_LENGTH_MIN + Constants.FIRST_SLOT_TIME_MIN;
			int min = startMin % 60;
			int startHour = startMin / 60;
			boolean morn = (startHour<12);
			int hour = startHour % 12;
			if (hour==0) hour = 12;
			filterAssignedTimeHour = String.valueOf(hour);
			filterAssignedTimeMin = (min<10?"0":"")+min;
			filterAssignedTimeAmPm = (morn?"am":"pm");
			
		} else {
			filterAssignedTimeHour = "";
			filterAssignedTimeMin = "";
			filterAssignedTimeAmPm = "";
		}
	}
	public int getFilterLength() {
		try {
			return Integer.parseInt(filterAssignedTimeLength);
		} catch (NumberFormatException e) {
		} catch (NullPointerException e) {
		}
		return 0;
	}
	public void setFilterLength(int length) {
		if (length>=0) {
			filterAssignedTimeLength = String.valueOf(length);
		} else {
			filterAssignedTimeLength = "";
		}
	}
	public String getCourseNbr() { return null; }
	public boolean getSortByKeepSubparts() {
		return sortByKeepSubparts;
	}
	public void setSortByKeepSubparts(boolean sortByKeepSubparts) {
		this.sortByKeepSubparts = sortByKeepSubparts;
	}
	public boolean isReturnAllControlClassesForSubjects() {
		return returnAllControlClassesForSubjects;
	}
	public void setReturnAllControlClassesForSubjects(
			boolean returnAllControlClassesForSubjects) {
		this.returnAllControlClassesForSubjects = returnAllControlClassesForSubjects;
	}
	public String[] getUserDeptIds() {
		return userDeptIds;
	}
	public void setUserDeptIds(String[] userDeptIds) {
		this.userDeptIds = userDeptIds;
	}
	public Boolean getDivSec() {
		return (new Boolean(true));
	}
	public Boolean getDemand() {
		return (new Boolean(false));
	}
	public Boolean getProjectedDemand() {
		return (new Boolean(false));
	}
	public Boolean getMinPerWk() {
		return (new Boolean(false));
	}
	public Boolean getLimit() {
		return (new Boolean(true));
	}
	public Boolean getRoomLimit() {
		return (new Boolean(false));
	}
	public Boolean getManager() {
		return (new Boolean(false));
	}
	public Boolean getDatePattern() {
		return (new Boolean(true));
	}
	public Boolean getTimePattern() {
		return (new Boolean(false));
	}
	public Boolean getPreferences() {
		return (new Boolean(false));
	}
	public Boolean getInstructor() {
		return (new Boolean(false));
	}
	public Boolean getTimetable() {
		return (new Boolean(true));
	}
	public Boolean getCredit() {
		return (new Boolean(false));
	}
	public Boolean getSubpartCredit() {
		return (new Boolean(false));
	}
	public Boolean getSchedulePrintNote() {
		return (new Boolean(true));
	}
	public Boolean getNote() {
		return (new Boolean(false));
	}
    public Boolean getConsent() {
		return (new Boolean(false));
    }
    public Boolean getTitle() {
		return (new Boolean(false));
    }
    
    public Boolean getExams() {
        return Boolean.FALSE;
    }
    public Boolean getCanSeeExams() {
        return Boolean.FALSE;
    }
	public boolean getShowCrossListedClasses() {
		return showCrossListedClasses;
	}
	public void setShowCrossListedClasses(boolean showCrossListedClasses) {
		this.showCrossListedClasses = showCrossListedClasses;
	}
}
