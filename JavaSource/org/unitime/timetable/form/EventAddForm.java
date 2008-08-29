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
package org.unitime.timetable.form;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
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
import org.hibernate.Query;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.model.Building;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.Event;
import org.unitime.timetable.model.CourseEvent;
import org.unitime.timetable.model.ExamOwner;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.RelatedCourseInfo;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.Preference;
import org.unitime.timetable.model.Roles;
import org.unitime.timetable.model.RoomFeature;
import org.unitime.timetable.model.RoomGroup;
import org.unitime.timetable.model.RoomType;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.comparators.ClassComparator;
import org.unitime.timetable.model.comparators.InstrOfferingConfigComparator;
import org.unitime.timetable.model.comparators.SchedulingSubpartComparator;
import org.unitime.timetable.model.dao.Class_DAO;
import org.unitime.timetable.model.dao.CourseOfferingDAO;
import org.unitime.timetable.model.dao.EventDAO;
import org.unitime.timetable.model.dao.InstrOfferingConfigDAO;
import org.unitime.timetable.model.dao.LocationDAO;
import org.unitime.timetable.model.dao.SchedulingSubpartDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.util.ComboBoxLookup;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.DateUtils;
import org.unitime.timetable.util.DynamicList;
import org.unitime.timetable.util.DynamicListObjectFactory;
import org.unitime.timetable.util.IdValue;

/**
 * @author Zuzana Mullerova
 */

public class EventAddForm extends ActionForm {

	private String iOp;
	private String iEventType;
	private Long iSessionId;
	private boolean iAttendanceRequired; 
	private int iStartTime;
	private int iStopTime;
//	private String iLocationType;
//	private Vector<String> iLocationTypes = new Vector<String>();
	private Long iBuildingId;
	private String iRoomNumber;
	private TreeSet<Date> iMeetingDates = new TreeSet();
	private String iMinCapacity;
	private String iMaxCapacity;
	private boolean iLookAtNearLocations;
	private Long[] iRoomFeatures = null;
	private Long[] iRoomTypes = null;
	private Long[] iRoomGroups = null;
	private boolean iHasRole = false;
	
	//if adding meetings to an existing event
	private Long iEventId; 
	private Event iEvent;
	private String iEventName;
	private boolean iIsAddMeetings;
	
	// courses/classes for course related events
    private List iSubjectArea;
    private List iCourseNbr;
    private List iItype;
    private List iClassNumber;
    private Collection iSubjectAreas;
    private int iSelected;
    private boolean iAdmin;
    private Set iManagingDepts = null;
    
    private String iCapacity = null;
    private String iMaxRooms = null;

	public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
		
		ActionErrors errors = new ActionErrors();

		if (iStartTime>=iStopTime)
			errors.add("stopDate", new ActionMessage("errors.generic", "Start Time must be earlier than Stop Time. It is not possible to enter overnight events."));
		
		if (iSessionId==null) {
			errors.add("session", new ActionMessage("errors.generic", "No academic session is selected."));
		}
		
		if (iMeetingDates.isEmpty()) {
			errors.add("dates", new ActionMessage("errors.generic", "No event dates are selected."));
		}

        int min = 0;
		if (iMinCapacity!=null && iMinCapacity.length()>0) {
			try {
				min = Integer.parseInt(iMinCapacity);
			} catch (NumberFormatException nfe) {
				errors.add("minCapacity", new ActionMessage("errors.generic", "Minimum room capacity should be a number or blank (no lower limit)."));
			}
		}
		
		int max = Integer.MAX_VALUE;
		if (iMaxCapacity!=null && iMaxCapacity.length()>0) {
			try {
				min = Integer.parseInt(iMaxCapacity);
			} catch (NumberFormatException nfe) {
				errors.add("maxCapacity", new ActionMessage("errors.generic", "Maximum room capacity should be a number or blank (no upper limit)."));
			}
		}
		
		if (min>max) {
			errors.add("minMaxCapacity", new ActionMessage("errors.generic", "Maximum room capacity should not be smaller than minimum room capacity."));
		}
		
		if ("Course Event".equals(iEventType) && !(iIsAddMeetings)) {
			boolean hasRci = false;
	        for (int idx=0;idx<getSubjectArea().size();idx++) {
	            RelatedCourseInfo rci = getRelatedCourseInfo(idx);
	            if (rci!=null) { hasRci = true; break; }
	        }
	        if (!hasRci) {
	            errors.add("relatedCourseInfo", new ActionMessage("errors.generic", "At least one class/course has to be specified.") );
	        }
		}
		
		if (iLookAtNearLocations) {
            if (iBuildingId==null || iBuildingId == -1) {
                errors.add("building", new ActionMessage("errors.generic", "Building is not selected (it is needed for close by locations)."));
            }
		}

        if (getPossibleLocations().isEmpty()) {
                errors.add("noLocation", new ActionMessage("errors.generic", "There is no location matching your criteria."));
            }

		return errors;
	}
	
	public void reset(ActionMapping mapping, HttpServletRequest request) {
		iOp = null;
		iEventType = Event.sEventTypes[Event.sEventTypeSpecial];
		iSessionId = null;
		iEventId = null;
		try {
			iSessionId = Session.getCurrentAcadSession(Web.getUser(request.getSession())).getUniqueId();
		} catch (Exception e) {}
		if (iSessionId==null) {
		    TreeSet<Session> sessions = Session.getAllSessions();
		    if (!sessions.isEmpty()) iSessionId = sessions.last().getUniqueId();
		}
		iStartTime = 90;
		iStopTime = 210;
//		iLocationType = null;
		iMeetingDates.clear();
		iMinCapacity = null;
		iMaxCapacity = null;
		iCapacity = null;
		iLookAtNearLocations = false;
		iAttendanceRequired = false;
        iSubjectArea = DynamicList.getInstance(new ArrayList(), idfactory);
        iCourseNbr = DynamicList.getInstance(new ArrayList(), idfactory);
        iItype = DynamicList.getInstance(new ArrayList(), idfactory);
        iClassNumber = DynamicList.getInstance(new ArrayList(), idfactory);
        for (int i=0;i<Constants.PREF_ROWS_ADDED;i++) {
            addRelatedCourseInfo(null);
        }
        User user = Web.getUser(request.getSession());
        iAdmin = (user!=null && user.isAdmin());
        iHasRole = (user.getRole()!=null);
        if (Roles.EVENT_MGR_ROLE.equals(user.getRole())) {
            TimetableManager mgr = (user==null?null:TimetableManager.getManager(user));
            if (mgr!=null) iManagingDepts = mgr.getDepartments();
        }
        iMaxRooms = "10";
        iBuildingId = null;
        iRoomTypes = null;
        iRoomFeatures = null;
        iRoomGroups = null;
        iRoomNumber = null;
	}
	
	// load event info from session attribute Event
	public void load (HttpSession session) throws Exception {
		iEventType = (String) session.getAttribute("Event.EventType");
		iSessionId = (Long) session.getAttribute("Event.SessionId");		
		iStartTime = (Integer) session.getAttribute("Event.StartTime");
		iStopTime = (Integer) session.getAttribute("Event.StopTime");
//		iLocationType = (String) session.getAttribute("Event.LocationType");
//		iLocationTypes = (Vector<String>) session.getAttribute("Event.LocationTypes");
		iMeetingDates = (TreeSet<Date>) session.getAttribute("Event.MeetingDates");
		iMinCapacity = (String) session.getAttribute("Event.MinCapacity");
		iMaxCapacity = (String) session.getAttribute("Event.MaxCapacity");
		iBuildingId = (Long) session.getAttribute("Event.BuildingId");
		iRoomNumber = (String) session.getAttribute("Event.RoomNumber");
		iLookAtNearLocations = (Boolean) session.getAttribute("Event.LookAtNearLocations");
		iAttendanceRequired = (Boolean) session.getAttribute("Event.AttendanceRequired");
		if (session.getAttribute("Event.SubjectArea")!=null) {
			iSubjectArea = (List) session.getAttribute("Event.SubjectArea");
			iCourseNbr = (List) session.getAttribute("Event.CourseNbr");
			iItype = (List) session.getAttribute("Event.SubjectItype");
			iClassNumber = (List) session.getAttribute("Event.ClassNumber");
		}
		iIsAddMeetings = (Boolean) (session.getAttribute("Event.IsAddMeetings"));
		iEventId = (Long) (session.getAttribute("Event.EventId"));
		if (iIsAddMeetings) {
			iEvent = EventDAO.getInstance().get(iEventId);
			iEventName = iEvent.getEventName();
			iEventType = iEvent.getEventTypeLabel();
		}
        iRoomTypes = (Long[]) session.getAttribute("Event.RoomTypes");
        iRoomGroups = (Long[]) session.getAttribute("Event.RoomGroups");
		iRoomFeatures = (Long[]) session.getAttribute("Event.RoomFeatures");
		iCapacity = (String)session.getAttribute("Event.Capacity");
		iMaxRooms = (String)session.getAttribute("Event.MaxRooms");
		if (iMaxRooms==null) { iMaxRooms = "10"; }
	}
	
	// save event parameters to session attribute Event
	public void save (HttpSession session) {
		session.setAttribute("Event.EventType", iEventType);
		session.setAttribute("Event.SessionId", iSessionId);		
		session.setAttribute("Event.StartTime", iStartTime);
		session.setAttribute("Event.StopTime", iStopTime);
//		session.setAttribute("Event.LocationType", iLocationType);
//		session.setAttribute("Event.LocationTypes", iLocationTypes);
		session.setAttribute("Event.MeetingDates", iMeetingDates);
		session.setAttribute("Event.MinCapacity", iMinCapacity);
		session.setAttribute("Event.MaxCapacity", iMaxCapacity);
		session.setAttribute("Event.BuildingId", iBuildingId);
		session.setAttribute("Event.RoomNumber", iRoomNumber);
		session.setAttribute("Event.LookAtNearLocations", (Boolean) iLookAtNearLocations);
		session.setAttribute("Event.SubjectArea", iSubjectArea);
		session.setAttribute("Event.CourseNbr", iCourseNbr);
		session.setAttribute("Event.SubjectItype", iItype);
		session.setAttribute("Event.ClassNumber", iClassNumber);
		session.setAttribute("Event.StudentIds", getStudentIds());
		session.setAttribute("Event.AttendanceRequired", iAttendanceRequired);
		session.setAttribute("Event.EventId", iEventId);
		session.setAttribute("Event.IsAddMeetings", iIsAddMeetings);
		session.setAttribute("Event.RoomTypes", iRoomTypes);
        session.setAttribute("Event.RoomGroups", iRoomGroups);
        session.setAttribute("Event.RoomFeatures", iRoomFeatures);
        session.setAttribute("Event.Capacity", iCapacity);
        session.setAttribute("Event.MaxRooms", iMaxRooms);
	}
	
	
	// load event dates selected by user
	public void loadDates(HttpServletRequest request) {
        iMeetingDates.clear();
        if (iSessionId==null) return;
		Session s = Session.getSessionById(iSessionId);
        Calendar today = Calendar.getInstance();
        today.setTime(s.getEventBeginDate());
        while (!today.getTime().after(s.getEventEndDate())) {
            if ("1".equals(request.getParameter("cal_val_"+today.get(Calendar.MONTH)+"_"+today.get(Calendar.DAY_OF_MONTH))))
                iMeetingDates.add(today.getTime());
            today.add(Calendar.DAY_OF_YEAR,1);
        }
	}
	
	public TreeSet<Date> getMeetingDates() { return iMeetingDates; }
	public void setMeetingDates(TreeSet<Date> dates) { iMeetingDates = dates; }
	
	public String getDatesTable() {
	    return getDatesTable(!"true".equals(ApplicationProperties.getProperty("tmtbl.event.allowEditPast","false")));
	}
	
	// display calendar for event dates
	public String getDatesTable(boolean disblePast) {
        if (iSessionId==null) return null;
		Session s = Session.getSessionById(iSessionId);
        int year = s.getYear();
        Calendar today = Calendar.getInstance();
        today.setTime(s.getEventEndDate());
        int endMonth = today.get(Calendar.MONTH);
        if (today.get(Calendar.YEAR)>year) endMonth+=12;
        today.setTime(s.getEventBeginDate());
        int startMonth = today.get(Calendar.MONTH);
        if (today.get(Calendar.YEAR)<year) startMonth-=12;
        today.set(Calendar.DAY_OF_MONTH,1);
        String pattern = "[", border = "[";
        Calendar now = Calendar.getInstance();
        now.setTime(new Date());
        now.set(Calendar.HOUR_OF_DAY,0);
        now.set(Calendar.MINUTE,0);
        now.set(Calendar.SECOND,0);
        now.set(Calendar.MILLISECOND,0);
        for (int m=startMonth;m<=endMonth;m++) {
            if (m!=startMonth) {pattern+=","; border+=","; }
             pattern+="["; border+="[";
             int daysOfMonth = DateUtils.getNrDaysOfMonth(m, year);
             for (int d=1;d<=daysOfMonth;d++) {
            	 if (d>1) {pattern+=","; border+=","; }
            	 boolean dis = today.getTime().before(s.getEventBeginDate()) || today.getTime().after(s.getEventEndDate());
            	 pattern+=(dis || (disblePast && today.before(now))?"'@'":iMeetingDates.contains(today.getTime())?"'1'":"'0'");
            	 today.add(Calendar.DAY_OF_YEAR,1);
            	 border += s.getBorder(d, m);
             }
             pattern+="]"; border+="]";
        }
        pattern+="]"; border+="]";
        return "<script language='JavaScript' type='text/javascript' src='scripts/datepatt.js'></script>"+
        	"<script language='JavaScript'>"+
        	"calGenerate("+year+","+startMonth+","+endMonth+","+
            pattern+","+"['1','0','@'],"+
            "['Selected','Not Selected','"+(disblePast?"Past / Outside Session":"Outside Session")+"'],"+
            "['rgb(240,240,50)','rgb(240,240,240)','rgb(150,150,150)'],"+
            "'1',"+border+",true,true);"+
            "</script>";
	}
	
	public String getOp (){return iOp;}
	public void setOp (String op) {iOp = op;}
	
	public String[] getEventTypes() {
		String[] types = (iHasRole ?
		        new String[] {Event.sEventTypes[Event.sEventTypeCourse], Event.sEventTypes[Event.sEventTypeSpecial]} :
		        new String[] {Event.sEventTypes[Event.sEventTypeSpecial]});; 
		return types;
	}
	
	public String getEventType() {return iEventType;}
	public void setEventType(String eventType) {iEventType = eventType;}
	
	public Vector<ComboBoxLookup> getAcademicSessions() {
		Vector<ComboBoxLookup> aSessions = new Vector();
		Date today = new Date();
		for (Iterator i=Session.getAllSessions().iterator();i.hasNext();) {
			Session session = (Session)i.next();
			//if (!session.getStatusType().canOwnerView()) continue;
			//if (session.getClassesEndDateTime().compareTo(today)<0) continue;
			aSessions.add(new ComboBoxLookup(session.getLabel(),session.getUniqueId().toString()));
		}
		return aSessions;
	}
		
	public Long getSessionId() {return iSessionId;}
	public void setSessionId(Long sessionId) {iSessionId = sessionId;}

	public boolean getAttendanceRequired() {
		return iAttendanceRequired;
	}
	
	public void setAttendanceRequired(boolean attReq) {iAttendanceRequired = attReq;}

    public int getStartTime() {return iStartTime; }
    public void setStartTime(int startTime) {iStartTime = startTime;}

    public int getStopTime() {return iStopTime; }
    public void setStopTime(int stopTime) {iStopTime = stopTime;}
    
    public Long getEventId() {return iEventId;}
    public void setEventId(Long id) {iEventId = id;}
    
    public Event getEvent() {return iEvent;}
    public void setEvent(Event event) {iEvent = event;}
    
    public boolean getIsAddMeetings() {return iIsAddMeetings;}
    public void setIsAddMeetings (boolean isAdd) {iIsAddMeetings = isAdd;}
    
    public String getEventName() {return iEventName;}
    public void setEventName(String name) {iEventName = name;}

	public Vector<ComboBoxLookup> getLocationTypes() {
		Vector<ComboBoxLookup> ltypes = new Vector();
		ltypes.add(new ComboBoxLookup("N/A yet", "1"));
		return ltypes;
	}

//	public String getLocationType() {return iLocationType; }
//    public void setLocationType(String locationType) {iLocationType = locationType;}
    
    //the index i goes in five minute increments, but displayed are 15 minute increments 
    public Vector<ComboBoxLookup> getTimes() {
    	Vector<ComboBoxLookup> times = new Vector();
    	int hour;
    	int minute;
    	String ampm;
    	for (int i=0; i<288; i=i+3) {
    		hour = (i/12)%12;
    		if (hour==0) hour=12; 
    		minute = i%12*5;
    		if (i/144==0) ampm="am"; 
    			else ampm = "pm";
    		times.add(new ComboBoxLookup(hour+":"+(minute<10?"0":"")+minute+" "+ampm, String.valueOf(i)));
    	}
    	return times;
    }
    
    public Vector<ComboBoxLookup> getStopTimes() {
        Vector<ComboBoxLookup> times = new Vector();
        int hour;
        int minute;
        String ampm;
        for (int i=3; i<=288; i=i+3) {
            hour = (i/12)%12;
            if (hour==0) hour=12; 
            minute = i%12*5;
            if ((i/144)%2==0) ampm="am"; 
                else ampm = "pm";
            times.add(new ComboBoxLookup(hour+":"+(minute<10?"0":"")+minute+" "+ampm, String.valueOf(i)));
        }
        return times;
    }

    public List getBuildings() {
        if (iSessionId==null) return null;
        List ret = Building.findAll(iSessionId);
        buildings: for (Iterator i=ret.iterator();i.hasNext();) {
            Building b = (Building)i.next();
            for (RoomType roomType : RoomType.findAll(true)) {
                if ((iAdmin || roomType.getOption(b.getSession()).canScheduleEvents()) && roomType.countManagableRoomsOfBuilding(b.getUniqueId())>0) continue buildings;
            }
            i.remove();
        }
        return ret;
    }
    
    public Long getBuildingId() { return iBuildingId;}
    public void setBuildingId(Long id) {iBuildingId = id;}
    
    public String getRoomNumber() {return iRoomNumber;}
    public void setRoomNumber(String roomNr) {iRoomNumber = roomNr;}
   
    public String getMinCapacity() {return iMinCapacity;}
    public void setMinCapacity (String minCapacity) {iMinCapacity = minCapacity;}
    
    public String getMaxCapacity() {return iMaxCapacity;}
    public void setMaxCapacity (String minCapacity) {iMaxCapacity = minCapacity;}
    
    public Boolean getLookAtNearLocations() {return iLookAtNearLocations;}
    public void setLookAtNearLocations (Boolean look) {iLookAtNearLocations = look;}
    
    public Collection<RoomFeature> getAllRoomFeatures() {
    	return RoomFeature.getAllGlobalRoomFeatures();
    }
    
    public Collection<RoomGroup> getAllRoomGroups() {
        return RoomGroup.getAllGlobalRoomGroups();
    }

    public Collection<RoomType> getAllRoomTypes() {
        Collection<RoomType> ret = RoomType.findAll();
        if (getSessionId()!=null) {
            Session session = new SessionDAO().get(getSessionId());
            for (Iterator<RoomType> i=ret.iterator(); i.hasNext();) {
                RoomType t = (RoomType)i.next();
                if (t.countManagableRooms(getSessionId())<=0) {i.remove(); continue; }
                if (!iAdmin && !t.getOption(session).canScheduleEvents()) {i.remove(); continue; }
            }
        }
        return ret;
    }
    
    public Long[] getRoomTypes() { return iRoomTypes; }
	public void setRoomTypes(Long[] rts) { iRoomTypes = rts; }

    public Long[] getRoomGroups() { return iRoomGroups; }
    public void setRoomGroups(Long[] rgs) { iRoomGroups= rgs; }

    public Long[] getRoomFeatures() { return iRoomFeatures; }
    public void setRoomFeatures(Long[] rfs) { iRoomFeatures = rfs; }

    public void cleanSessionAttributes(HttpSession session) {
		session.removeAttribute("Event.DateLocations");
		session.removeAttribute("Event.StartTime");
		session.removeAttribute("Event.StopTime");
		session.removeAttribute("Event.MeetingDates");
		session.removeAttribute("Event.MinCapacity");
		session.removeAttribute("Event.MaxCapacity");
		session.removeAttribute("Event.BuildingId");
		session.removeAttribute("Event.RoomNumber");
		session.removeAttribute("Event.SessionId");
		session.removeAttribute("back");
		session.removeAttribute("Event.LookAtNearLocations");
		session.removeAttribute("Event.SubjectArea");
		session.removeAttribute("Event.CourseNbr");
		session.removeAttribute("Event.SubjectItype");
		session.removeAttribute("Event.ClassNumber");
		session.removeAttribute("Event.EventId");
		session.removeAttribute("Event.IsAddMeetings");
		session.removeAttribute("Event.Name");
		session.removeAttribute("Event.mcEmail");
		session.removeAttribute("Event.mcFName");
		session.removeAttribute("Event.mcLName");
		session.removeAttribute("Event.mcPhone");
		session.removeAttribute("Event.AdditionalInfo");
		session.removeAttribute("Event.RoomFeatures");
		session.removeAttribute("Event.AdditionalEmails");
		session.removeAttribute("Event.RoomGroups");
		session.removeAttribute("Event.RoomTypes");
		session.removeAttribute("Event.Capacity");
	}
    
    
// Methods related to course events with conflict checking    
    public String[] getObjectTypes() { return ExamOwner.sOwnerTypes; }
    
    protected DynamicListObjectFactory factory = new DynamicListObjectFactory() {
        public Object create() {
            return new String(Preference.BLANK_PREF_VALUE);
        }
    };

    protected DynamicListObjectFactory idfactory = new DynamicListObjectFactory() {
        public Object create() {
            return new Long(-1);
        }
    };
    
    
    public List getSubjectAreaList() { return iSubjectArea; }
    public List getSubjectArea() { return iSubjectArea; }
    public Long getSubjectArea(int key) { return (Long)iSubjectArea.get(key); }
    public void setSubjectArea(int key, Long value) { this.iSubjectArea.set(key, value); }
    public void setSubjectArea(List subjectArea) { this.iSubjectArea = subjectArea; }
    public List getCourseNbr() { return iCourseNbr; }
    public Long getCourseNbr(int key) { return (Long)iCourseNbr.get(key); }
    public void setCourseNbr(int key, Long value) { this.iCourseNbr.set(key, value); }
    public void setCourseNbr(List courseNbr) { this.iCourseNbr = courseNbr; }
    public List getItype() { return iItype; }
    public Long getItype(int key) { return (Long)iItype.get(key); }
    public void setItype(int key, Long value) { this.iItype.set(key, value); }
    public void setItype(List itype) { this.iItype = itype; }
    public List getClassNumber() { return iClassNumber; }
    public Long getClassNumber(int key) { return (Long)iClassNumber.get(key); }
    public void setClassNumber(int key, Long value) { this.iClassNumber.set(key, value); }
    public void setClassNumber(List classNumber) { this.iClassNumber = classNumber; }
    public int getSelected() {return iSelected;}
    public void setSelected (int selected) {iSelected = selected;}
    
    public void deleteRelatedCourseInfo(int idx) {
        getSubjectArea().remove(idx);
        getCourseNbr().remove(idx);
        getItype().remove(idx);
        getClassNumber().remove(idx);
    }
    
    public void addRelatedCourseInfo(RelatedCourseInfo rci) {
        if (rci==null) {
            getSubjectArea().add(new Long(-1));
            getCourseNbr().add(new Long(-1));
            getItype().add(new Long(-1));
            getClassNumber().add(new Long(-1));
        } else {
            switch (rci.getOwnerType()) {
            case ExamOwner.sOwnerTypeClass :
                Class_ clazz = (Class_)rci.getOwnerObject();
                getSubjectArea().add(clazz.getSchedulingSubpart().getControllingCourseOffering().getSubjectArea().getUniqueId());
                getCourseNbr().add(clazz.getSchedulingSubpart().getControllingCourseOffering().getUniqueId());
                getItype().add(clazz.getSchedulingSubpart().getUniqueId());
                getClassNumber().add(clazz.getUniqueId());
                break;
            case ExamOwner.sOwnerTypeConfig :
                InstrOfferingConfig config = (InstrOfferingConfig)rci.getOwnerObject();
                getSubjectArea().add(config.getControllingCourseOffering().getSubjectArea().getUniqueId());
                getCourseNbr().add(config.getControllingCourseOffering().getUniqueId());
                getItype().add(-config.getUniqueId());
                getClassNumber().add(new Long(-1));
                break;
            case ExamOwner.sOwnerTypeCourse :
                CourseOffering course = (CourseOffering) rci.getOwnerObject();
                getSubjectArea().add(course.getSubjectArea().getUniqueId());
                getCourseNbr().add(course.getUniqueId());
                getItype().add(Long.MIN_VALUE);
                getClassNumber().add(new Long(-1));
                break;
            case ExamOwner.sOwnerTypeOffering :
                InstructionalOffering offering = (InstructionalOffering)rci.getOwnerObject();
                getSubjectArea().add(offering.getControllingCourseOffering().getSubjectArea().getUniqueId());
                getCourseNbr().add(offering.getControllingCourseOffering().getUniqueId());
                getItype().add(Long.MIN_VALUE+1);
                getClassNumber().add(new Long(-1));
                break;
            }
        }
    }
    
    public Collection getSubjectAreas() { return iSubjectAreas; }
    public void setSubjectAreas(Collection subjectAreas) { this.iSubjectAreas = subjectAreas; }
    
    public Collection getCourseNbrs(int idx) { 
        Vector ret = new Vector();
        boolean contains = false;
        if (getSubjectArea(idx)>=0) {
            for (Iterator i= new CourseOfferingDAO().
                    getSession().
                    createQuery("select co.uniqueId, co.courseNbr from CourseOffering co "+
                            "where co.uniqueCourseNbr.subjectArea.uniqueId = :subjectAreaId "+
                            "and co.instructionalOffering.notOffered = false "+
                            "order by co.courseNbr ").
                    setFetchSize(200).
                    setCacheable(true).
                    setLong("subjectAreaId", getSubjectArea(idx)).iterate();i.hasNext();) {
                Object[] o = (Object[])i.next();
                ret.add(new IdValue((Long)o[0],(String)o[1]));
                if (o[0].equals(getCourseNbr(idx))) contains = true;
            }
        }
        if (!contains) setCourseNbr(idx, -1L);
        if (ret.size()==1) setCourseNbr(idx, ((IdValue)ret.firstElement()).getId());
        else ret.insertElementAt(new IdValue(-1L,"-"), 0);
        return ret;
    }
    
    public Collection getItypes(int idx) { 
        Vector ret = new Vector();
        boolean contains = false;
        if (getCourseNbr(idx)>=0) {
            CourseOffering course = new CourseOfferingDAO().get(getCourseNbr(idx));
            if (course.isIsControl())
                ret.add(new IdValue(Long.MIN_VALUE+1,"Offering"));
            ret.add(new IdValue(Long.MIN_VALUE,"Course"));
            if (!course.isIsControl()) {
                setItype(idx, Long.MIN_VALUE);
                return ret;
            }
            TreeSet configs = new TreeSet(new InstrOfferingConfigComparator(null));
            configs.addAll(new InstrOfferingConfigDAO().
                getSession().
                createQuery("select distinct c from " +
                        "InstrOfferingConfig c inner join c.instructionalOffering.courseOfferings co "+
                        "where co.uniqueId = :courseOfferingId").
                setFetchSize(200).
                setCacheable(true).
                setLong("courseOfferingId", course.getUniqueId()).
                list());
            if (!configs.isEmpty()) {
                ret.add(new IdValue(Long.MIN_VALUE+2,"-- Configurations --"));
                for (Iterator i=configs.iterator();i.hasNext();) {
                    InstrOfferingConfig c = (InstrOfferingConfig)i.next();
                    if (c.getUniqueId().equals(getItype(idx))) contains = true;
                    ret.add(new IdValue(-c.getUniqueId(), c.getName()));
                }
            }
            TreeSet subparts = new TreeSet(new SchedulingSubpartComparator(null));
            subparts.addAll(new SchedulingSubpartDAO().
                getSession().
                createQuery("select distinct s from " +
                        "SchedulingSubpart s inner join s.instrOfferingConfig.instructionalOffering.courseOfferings co "+
                        "where co.uniqueId = :courseOfferingId").
                setFetchSize(200).
                setCacheable(true).
                setLong("courseOfferingId", course.getUniqueId()).
                list());
            if (!configs.isEmpty() && !subparts.isEmpty())
                ret.add(new IdValue(Long.MIN_VALUE+2,"-- Subparts --"));
            for (Iterator i=subparts.iterator();i.hasNext();) {
                SchedulingSubpart s = (SchedulingSubpart)i.next();
                Long sid = s.getUniqueId();
                String name = s.getItype().getAbbv();
                String sufix = s.getSchedulingSubpartSuffix();
                while (s.getParentSubpart()!=null) {
                    name = "&nbsp;&nbsp;&nbsp;&nbsp;"+name;
                    s = s.getParentSubpart();
                }
                if (s.getInstrOfferingConfig().getInstructionalOffering().getInstrOfferingConfigs().size()>1)
                    name += " ["+s.getInstrOfferingConfig().getName()+"]";
                if (sid.equals(getItype(idx))) contains = true;
                ret.add(new IdValue(sid, name+(sufix==null || sufix.length()==0?"":" ("+sufix+")")));
            }
        } else {
            ret.addElement(new IdValue(0L,"N/A"));
        }
        if (!contains) setItype(idx, ((IdValue)ret.firstElement()).getId());
        return ret;
    }
    
    public RelatedCourseInfo getRelatedCourseInfo(int idx) {
        if (getSubjectArea(idx)<0 || getCourseNbr(idx)<0) return null;
        CourseOffering course = new CourseOfferingDAO().get(getCourseNbr(idx));
        if (course==null) return null;
        if (getItype(idx)==Long.MIN_VALUE) { //course
            RelatedCourseInfo owner = new RelatedCourseInfo();
            owner.setOwner(course);
            return owner;
        } else if (getItype(idx)==Long.MIN_VALUE+1 || getItype(idx)==Long.MIN_VALUE+2) { //offering
            RelatedCourseInfo owner = new RelatedCourseInfo();
            owner.setOwner(course.getInstructionalOffering());
            return owner;
        } else if (getItype(idx)<0) { //config
            InstrOfferingConfig config = new InstrOfferingConfigDAO().get(-getItype(idx));
            if (config==null) return null;
            RelatedCourseInfo owner = new RelatedCourseInfo();
            owner.setOwner(config);
            return owner;
        } else if (getClassNumber(idx)>=0) { //class
            Class_ clazz = new Class_DAO().get(getClassNumber(idx));
            if (clazz==null) return null;
            RelatedCourseInfo owner = new RelatedCourseInfo();
            owner.setOwner(clazz);
            return owner;
        }
        return null;
    }
    
    public void setRelatedCourseInfos(CourseEvent event) {
        if (event.getRelatedCourses()==null) event.setRelatedCourses(new HashSet());
        event.getRelatedCourses().clear();
        for (int idx=0;idx<getSubjectArea().size();idx++) {
            RelatedCourseInfo course = getRelatedCourseInfo(idx);
            if (course!=null) {
                event.getRelatedCourses().add(course);
                course.setEvent(event);
            }
        }
    }
    
    public Set<Long> getStudentIds() {
        HashSet<Long> ret = new HashSet();
        for (int idx=0;idx<getSubjectArea().size();idx++) {
            RelatedCourseInfo course = getRelatedCourseInfo(idx);
            if (course!=null) {
                ret.addAll(course.getStudentIds());
            }
        }
        return ret;
    }
    
    public Collection getClassNumbers(int idx) {
        Vector ret = new Vector();
        boolean contains = false;
        SchedulingSubpart subpart = (getItype(idx)>0?new SchedulingSubpartDAO().get(getItype(idx)):null);
        if (subpart!=null) {
            TreeSet classes = new TreeSet(new ClassComparator(ClassComparator.COMPARE_BY_HIERARCHY));
            classes.addAll(new Class_DAO().
                getSession().
                createQuery("select distinct c from Class_ c "+
                        "where c.schedulingSubpart.uniqueId=:schedulingSubpartId").
                setFetchSize(200).
                setCacheable(true).
                setLong("schedulingSubpartId", getItype(idx)).
                list());
            for (Iterator i=classes.iterator();i.hasNext();) {
                Class_ c = (Class_)i.next();
                if (c.getUniqueId().equals(getClassNumber(idx))) contains = true;
                ret.add(new IdValue(c.getUniqueId(), c.getSectionNumberString())); 
            }
        }
        if (ret.isEmpty()) ret.add(new IdValue(-1L,"N/A"));
        if (!contains) setClassNumber(idx, -1L);
        if (ret.size()==1) setClassNumber(idx, ((IdValue)ret.firstElement()).getId());
        else ret.insertElementAt(new IdValue(-1L,"-"), 0);
        return ret;
    }
    
 // get possible locations for the event based on entered criteria
	public Hashtable<Long, Location> getPossibleLocations() {
		Hashtable<Long, Location> locations = new Hashtable();
		String query;
		String a = "", b = "";
		if (iRoomFeatures!=null && iRoomFeatures.length>0) {
			for (int i=0;i<iRoomFeatures.length;i++) {
				a+= ", GlobalRoomFeature f"+i;
				b+= " and f"+i+".uniqueId="+iRoomFeatures[i]+" and f"+i+" in elements(r.features)";
			}
		}
        if (iRoomGroups!=null && iRoomGroups.length>0) {
            b+= " and (";
            for (int i=0;i<iRoomGroups.length;i++) {
                if (i>0) b+=" or";
                a+= ", RoomGroup g"+i;
                b+= " (g"+i+".uniqueId="+iRoomGroups[i]+" and g"+i+" in elements(r.roomGroups))";
            }
            b+=")";
        }
        if (iRoomTypes!=null && iRoomTypes.length>0) {
            b+= " and r.roomType.uniqueId in (";
            for (int i=0;i<iRoomTypes.length;i++) {
                if (i>0) b+=",";
                b+= iRoomTypes[i];
            }
            b+=")";
        }
        if (iLookAtNearLocations && iBuildingId!=null && iBuildingId>=0) {
            query = "select r from Room r " +
                    "inner join r.roomDepts rd inner join rd.department.timetableManagers m inner join m.managerRoles mr, "+
                    "Building b"+a+" where b.uniqueId = :buildingId and " +
                    "rd.control=true and mr.role.reference=:eventMgr and "+
                    "(r.building=b or ((((r.coordinateX - b.coordinateX)*(r.coordinateX - b.coordinateX)) +" +
                    "((r.coordinateY - b.coordinateY)*(r.coordinateY - b.coordinateY)))" +
                    "< 67*67))";
        } else {
            query = "select r from Room r " +
                    "inner join r.roomDepts rd inner join rd.department.timetableManagers m inner join m.managerRoles mr"+a+
                    " where rd.control=true and mr.role.reference=:eventMgr";
            if (iBuildingId!=null && iBuildingId>=0) { query += " and r.building.uniqueId=:buildingId"; }   
        }
			
		if (iMinCapacity!=null && iMinCapacity.length()>0) { query+= " and r.capacity>= :minCapacity";	}
		if (iMaxCapacity!=null && iMaxCapacity.length()>0) { query+= " and r.capacity<= :maxCapacity";	}
		if (iRoomNumber!=null && iRoomNumber.length()>0) { query+=" and r.roomNumber like (:roomNumber)"; }
 		query += b;

		Query hibQuery = new LocationDAO().getSession().createQuery(query);

		if (iBuildingId!=null && iBuildingId>=0) hibQuery.setLong("buildingId", iBuildingId);
		hibQuery.setString("eventMgr", Roles.EVENT_MGR_ROLE);
		if (iMinCapacity!=null && iMinCapacity.length()>0) { hibQuery.setInteger("minCapacity", Integer.valueOf(iMinCapacity)); }
		if (iMaxCapacity!=null && iMaxCapacity.length()>0) { hibQuery.setInteger("maxCapacity", Integer.valueOf(iMaxCapacity)); }
		if (iRoomNumber!=null && iRoomNumber.length()>0) { 
			hibQuery.setString("roomNumber", iRoomNumber.replaceAll("\\*", "%")); 
		}
		
		for (Iterator i=hibQuery.setCacheable(true).iterate();i.hasNext();) {
			Location location = (Location)i.next();
			if (location.getPermanentId()!=null)
				locations.put(location.getPermanentId(), location);
		}
		
        if (iBuildingId==null || iBuildingId<0) {
            query = "select r from NonUniversityLocation r " +
                "inner join r.roomDepts rd inner join rd.department.timetableManagers m inner join m.managerRoles mr"+
                a+" where rd.control=true and mr.role.reference=:eventMgr";
            
            if (iMinCapacity!=null && iMinCapacity.length()>0) { query+= " and r.capacity>= :minCapacity";  }
            if (iMaxCapacity!=null && iMaxCapacity.length()>0) { query+= " and r.capacity<= :maxCapacity";  }
            if (iRoomNumber!=null && iRoomNumber.length()>0) {
                query+=" and r.name like (:roomNumber)"; 
            }
            query += b;
            hibQuery = new LocationDAO().getSession().createQuery(query);
            hibQuery.setString("eventMgr", Roles.EVENT_MGR_ROLE);
            if (iMinCapacity!=null && iMinCapacity.length()>0) { hibQuery.setInteger("minCapacity", Integer.valueOf(iMinCapacity)); }
            if (iMaxCapacity!=null && iMaxCapacity.length()>0) { hibQuery.setInteger("maxCapacity", Integer.valueOf(iMaxCapacity)); }
            if (iRoomNumber!=null && iRoomNumber.length()>0) { 
                hibQuery.setString("roomNumber", iRoomNumber.replaceAll("\\*", "%")); 
            }
            
            for (Iterator i=hibQuery.setCacheable(true).iterate();i.hasNext();) {
                Location location = (Location)i.next();
                if (location.getPermanentId()!=null)
                    locations.put(location.getPermanentId(), location);
            }
        }
		
		return locations;
	}
	
	public String getCapacity() { return iCapacity; }
	public void setCapacity(String capacity) { iCapacity = capacity; }
    public String getMaxRooms() { return iMaxRooms; }
    public void setMaxRooms(String maxRooms) { iMaxRooms = maxRooms; }

	public boolean isHasOutsideLocations() {
	    if (getSessionId()==null) return false;
	    Session s = Session.getSessionById(iSessionId);
	    boolean hasRoomType = false;
        for (RoomType roomType : RoomType.findAll(false)) {
            if ((iAdmin || roomType.getOption(s).canScheduleEvents()) && roomType.countManagableRooms(iSessionId)>0) return true;
        }
        return false;
	}
	
	public boolean isAdmin() {
	    return iAdmin;
	}
	public Set<Department> getManagingDepartments() {
	    return iManagingDepts;
	}
}
