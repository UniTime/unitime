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
package org.unitime.timetable.webutil.timegrid;

import java.text.DecimalFormat;
import java.util.BitSet;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.Vector;

import org.cpsolver.coursett.model.TimeLocation;
import org.cpsolver.coursett.preference.PreferenceCombination;
import org.hibernate.Query;
import org.unitime.commons.Debug;
import org.unitime.timetable.interfaces.RoomAvailabilityInterface;
import org.unitime.timetable.interfaces.RoomAvailabilityInterface.TimeBlock;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.ExactTimeMins;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.Room;
import org.unitime.timetable.model.RoomSharingModel;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Solution;
import org.unitime.timetable.model.TimePattern;
import org.unitime.timetable.model.dao.SolutionDAO;
import org.unitime.timetable.solver.ui.AssignmentPreferenceInfo;
import org.unitime.timetable.solver.ui.GroupConstraintInfo;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.DateUtils;
import org.unitime.timetable.util.Formats;
import org.unitime.timetable.util.RoomAvailability;


/**
 * @author Tomas Muller
 */
public class SolutionGridModel extends TimetableGridModel {
	private static final long serialVersionUID = -3207641071203870684L;
	private transient Long iRoomId = null;
	private static DecimalFormat sDF = new DecimalFormat("0.0");
    
	public SolutionGridModel(String solutionIdsStr, Location room, org.hibernate.Session hibSession, int firstDay, int bgMode, boolean showEvents) {
		super(sResourceTypeRoom, room.getUniqueId().intValue());
		setName(room.getLabel());
		setSize(room.getCapacity().intValue());
		setFirstDay(firstDay);
		iRoomId = room.getUniqueId();
		Solution firstSolution = null;
		String ownerIds = "";
		HashSet deptIds = new HashSet();
		for (StringTokenizer s=new StringTokenizer(solutionIdsStr,",");s.hasMoreTokens();) {
			Long solutionId = Long.valueOf(s.nextToken());
			Solution solution = (new SolutionDAO()).get(solutionId, hibSession);
			if (solution==null) continue;
			if (firstSolution==null) firstSolution = solution;
			if (ownerIds.length()>0) ownerIds += ",";
			ownerIds += solution.getOwner().getUniqueId();
			for (Iterator i=solution.getOwner().getDepartments().iterator();i.hasNext();) {
				Department d = (Department)i.next();
				deptIds.add(d.getUniqueId());
			}
		}
		Query q = hibSession.createQuery("select distinct a from Assignment as a inner join a.rooms as r where a.solution.uniqueId in ("+solutionIdsStr+") and r.uniqueId=:resourceId");
		q.setLong("resourceId", room.getUniqueId());
		q.setCacheable(true);
		init(q.list(),hibSession,firstDay,bgMode);
		
		q = hibSession.createQuery("select distinct a from Room r inner join r.assignments as a "+
		"where r.uniqueId=:roomId and a.solution.commited=true and a.solution.owner.session.uniqueId=:sessionId and a.solution.owner.uniqueId not in ("+ownerIds+")");
		q.setLong("roomId",room.getUniqueId());
        q.setLong("sessionId", room.getSession().getUniqueId().longValue());
		q.setCacheable(true);
		List commitedAssignments = q.list();
		for (Iterator x=commitedAssignments.iterator();x.hasNext();) {
			Assignment a = (Assignment)x.next();
			init(a,hibSession,firstDay,sBgModeNotAvailable);
			/*
			int days = a.getDays().intValue();
			int startSlot = a.getStartSlot().intValue();
			int length = a.getTimePattern().getSlotsPerMtg().intValue();
			if (a.getTimePattern().getType().intValue()==TimePattern.sTypeExactTime) {
				length = TimePatternModel.getExactSlotsPerMtg(days, a.getClazz().getSchedulingSubpart().getMinutesPerWk().intValue());
			}
			for (int i=0;i<Constants.DAY_CODES.length;i++) {
				if ((Constants.DAY_CODES[i]&days)==0) continue;
				for (int j=startSlot;j<startSlot+length;j++)
					setAvailable(i,j,false);
			}
			*/
		}
		RoomSharingModel sharing = room.getRoomSharingModel();
		if (sharing!=null) {
			for (int i=0;i<Constants.DAY_CODES.length;i++)
				for (int j=0;j<Constants.SLOTS_PER_DAY;j++) {
					if (sharing.isFreeForAll(i,j)) continue;
					if (sharing.isNotAvailable(i,j)) { setAvailable(i,j,false); continue; }
					Long dept = sharing.getDepartmentId(i,j);
					if (dept!=null && !deptIds.contains(dept))
						setAvailable(i,j,false);
				}
		}
		if (showEvents && RoomAvailability.getInstance() != null) {
	        Calendar startDateCal = Calendar.getInstance(Locale.US);
	        // Range can be limited to classes time using
	        // startDateCal.setTime(room.getSession().getSessionBeginDateTime());
	        // endDateCal.setTime(room.getSession().getClassesEndDateTime());
	        startDateCal.setTime(DateUtils.getDate(1, room.getSession().getStartMonth(), room.getSession().getSessionStartYear()));
	        startDateCal.set(Calendar.HOUR_OF_DAY, 0);
	        startDateCal.set(Calendar.MINUTE, 0);
	        startDateCal.set(Calendar.SECOND, 0);
	        Calendar endDateCal = Calendar.getInstance(Locale.US);
	        endDateCal.setTime(DateUtils.getDate(0, room.getSession().getEndMonth() + 1, room.getSession().getSessionStartYear()));
	        endDateCal.set(Calendar.HOUR_OF_DAY, 23);
	        endDateCal.set(Calendar.MINUTE, 59);
	        endDateCal.set(Calendar.SECOND, 59);
			Collection<TimeBlock> times = RoomAvailability.getInstance().getRoomAvailability(room.getUniqueId(), startDateCal.getTime(), endDateCal.getTime(), RoomAvailabilityInterface.sClassType);
			if (times != null) {
				int sessionYear = room.getSession().getSessionStartYear();
		        int firstDOY = room.getSession().getDayOfYear(1, room.getSession().getPatternStartMonth());
		        int lastDOY = room.getSession().getDayOfYear(0, room.getSession().getPatternEndMonth()+1);
		        Calendar c = Calendar.getInstance(Locale.US);
		        Formats.Format<Date> df = Formats.getDateFormat(Formats.Pattern.DATE_EVENT_SHORT);
				for (TimeBlock time: times) {
					if (time.getEndTime().before(startDateCal.getTime()) || time.getStartTime().after(endDateCal.getTime())) continue;
	                int dayCode = 0;
	                c.setTime(time.getStartTime());
	                int m = c.get(Calendar.MONTH);
	                int d = c.get(Calendar.DAY_OF_MONTH);
	                if (c.get(Calendar.YEAR)<sessionYear) m-=(12 * (sessionYear - c.get(Calendar.YEAR)));
	                if (c.get(Calendar.YEAR)>sessionYear) m+=(12 * (c.get(Calendar.YEAR) - sessionYear));
	                BitSet weekCode = new BitSet(lastDOY - firstDOY);
	                int offset = room.getSession().getDayOfYear(d,m) - firstDOY;
	                weekCode.set(offset);
	                switch (c.get(Calendar.DAY_OF_WEEK)) {
	                    case Calendar.MONDAY    : dayCode = Constants.DAY_CODES[Constants.DAY_MON]; break;
	                    case Calendar.TUESDAY   : dayCode = Constants.DAY_CODES[Constants.DAY_TUE]; break;
	                    case Calendar.WEDNESDAY : dayCode = Constants.DAY_CODES[Constants.DAY_WED]; break;
	                    case Calendar.THURSDAY  : dayCode = Constants.DAY_CODES[Constants.DAY_THU]; break;
	                    case Calendar.FRIDAY    : dayCode = Constants.DAY_CODES[Constants.DAY_FRI]; break;
	                    case Calendar.SATURDAY  : dayCode = Constants.DAY_CODES[Constants.DAY_SAT]; break;
	                    case Calendar.SUNDAY    : dayCode = Constants.DAY_CODES[Constants.DAY_SUN]; break;
	                }
	                int startSlot = (c.get(Calendar.HOUR_OF_DAY)*60 + c.get(Calendar.MINUTE) - Constants.FIRST_SLOT_TIME_MIN) / Constants.SLOT_LENGTH_MIN;
	                c.setTime(time.getEndTime());
	                int endSlot = (c.get(Calendar.HOUR_OF_DAY)*60 + c.get(Calendar.MINUTE) - Constants.FIRST_SLOT_TIME_MIN) / Constants.SLOT_LENGTH_MIN;
	                int length = endSlot - startSlot;
	                if (length<=0) continue;
	                TimeLocation timeLocation = new TimeLocation(dayCode, startSlot, length, 0, 0, null, df.format(time.getStartTime()), weekCode, 0);
	        		TimetableGridCell cell = null;
	        		for (Enumeration<Integer> f=timeLocation.getStartSlots();f.hasMoreElements();) {
	        			int slot = f.nextElement();
	        			if (firstDay>=0 && !timeLocation.getWeekCode().get(firstDay+(slot/Constants.SLOTS_PER_DAY))) continue;
	        			if (cell==null) {
	        				cell =  new TimetableGridCell(
	        						slot/Constants.SLOTS_PER_DAY,
	        						slot%Constants.SLOTS_PER_DAY,
	        						-1, 
	        						room.getUniqueId(),
	        						room.getLabel(),
	        						time.getEventName(), 
	        						null,
	        						null,
	        						null, 
	        						time.getEventName() + " (" + time.getEventType() + ")", 
	        						TimetableGridCell.sBgColorNotAvailable, 
	        						length,
	        						0, 
	        						1,
	        						df.format(time.getStartTime()),
	        						weekCode,
	        						null,
	        						Constants.toTime(Constants.SLOT_LENGTH_MIN * startSlot + Constants.FIRST_SLOT_TIME_MIN) + " - " + Constants.toTime(Constants.SLOT_LENGTH_MIN  * endSlot + Constants.FIRST_SLOT_TIME_MIN));
	        			} else {
	        				cell = cell.copyCell(slot/Constants.SLOTS_PER_DAY,cell.getMeetingNumber()+1);
	        			}
	        			addCell(slot,cell);
	        		}
				}
			}
		}
        setType(room instanceof Room ? ((Room)room).getRoomType().getUniqueId(): null);
	}
	
	public SolutionGridModel(String solutionIdsStr, DepartmentalInstructor instructor, org.hibernate.Session hibSession, int firstDay, int bgMode, boolean showEvents) {
		super(sResourceTypeInstructor, instructor.getUniqueId().intValue());
		setName(instructor.getLastName()+", "+instructor.getFirstName()+(instructor.getMiddleName()==null?"":" "+instructor.getMiddleName()));
		setFirstDay(firstDay);
		Solution firstSolution = null;
		String ownerIds = "";
		for (StringTokenizer s=new StringTokenizer(solutionIdsStr,",");s.hasMoreTokens();) {
			Long solutionId = Long.valueOf(s.nextToken());
			Solution solution = (new SolutionDAO()).get(solutionId, hibSession);
			if (solution==null) continue;
			if (firstSolution==null) firstSolution = solution;
			if (ownerIds.length()>0) ownerIds += ",";
			ownerIds += solution.getOwner().getUniqueId();
		}
		List commitedAssignments = null;
		
		if (instructor.getExternalUniqueId()!=null && instructor.getExternalUniqueId().length()>0) {
			Query q = hibSession.createQuery("select distinct a from Assignment as a inner join a.instructors as i where a.solution.uniqueId in ("+solutionIdsStr+") and i.externalUniqueId=:puid");
			q.setString("puid", instructor.getExternalUniqueId());
			q.setCacheable(true);
			init(q.list(),hibSession,firstDay,bgMode);
			q = hibSession.createQuery("select distinct a from DepartmentalInstructor i inner join i.assignments as a "+
					"where i.externalUniqueId=:puid and a.solution.commited=true and a.solution.owner.session.uniqueId=:sessionId and a.solution.owner.uniqueId not in ("+ownerIds+")");
			q.setString("puid",instructor.getExternalUniqueId());
            q.setLong("sessionId", instructor.getDepartment().getSession().getUniqueId().longValue());
			q.setCacheable(true);
			commitedAssignments = q.list();
		} else {
			Query q = hibSession.createQuery("select distinct a from Assignment as a inner join a.instructors as i where a.solution.uniqueId in ("+solutionIdsStr+") and i.uniqueId=:resourceId");
			q.setLong("resourceId", instructor.getUniqueId());
			q.setCacheable(true);
			init(q.list(),hibSession,firstDay,bgMode);
			q = hibSession.createQuery("select distinct a from DepartmentalInstructor i inner join i.assignments as a "+
					"where i.uniqueId=:instructorId and a.solution.commited=true and a.solution.owner.session.uniqueId=:sessionId and a.solution.owner.uniqueId not in ("+ownerIds+")");
			q.setLong("instructorId",instructor.getUniqueId());
            q.setLong("sessionId", instructor.getDepartment().getSession().getUniqueId().longValue());
			q.setCacheable(true);
			commitedAssignments = q.list();
		}
		
		for (Iterator x=commitedAssignments.iterator();x.hasNext();) {
			Assignment a = (Assignment)x.next();
			init(a,hibSession,firstDay,sBgModeNotAvailable);
			/*
			int days = a.getDays().intValue();
			int startSlot = a.getStartSlot().intValue();
			int length = a.getTimePattern().getSlotsPerMtg().intValue(); 
			if (a.getTimePattern().getType().intValue()==TimePattern.sTypeExactTime) {
				length = TimePatternModel.getExactSlotsPerMtg(days, a.getClazz().getSchedulingSubpart().getMinutesPerWk().intValue());
			}
			for (int i=0;i<Constants.DAY_CODES.length;i++) {
				if ((Constants.DAY_CODES[i]&days)==0) continue;
				for (int j=startSlot;j<startSlot+length;j++)
					setAvailable(i,j,false);
			}
			*/
		}
		if (showEvents && RoomAvailability.getInstance() != null) {
	        Calendar startDateCal = Calendar.getInstance(Locale.US);
	        // Range can be limited to classes time using
	        // startDateCal.setTime(room.getSession().getSessionBeginDateTime());
	        // endDateCal.setTime(room.getSession().getClassesEndDateTime());
	        Session session = instructor.getDepartment().getSession();
	        startDateCal.setTime(DateUtils.getDate(1, session.getStartMonth(), session.getSessionStartYear()));
	        startDateCal.set(Calendar.HOUR_OF_DAY, 0);
	        startDateCal.set(Calendar.MINUTE, 0);
	        startDateCal.set(Calendar.SECOND, 0);
	        Calendar endDateCal = Calendar.getInstance(Locale.US);
	        endDateCal.setTime(DateUtils.getDate(0, session.getEndMonth() + 1, session.getSessionStartYear()));
	        endDateCal.set(Calendar.HOUR_OF_DAY, 23);
	        endDateCal.set(Calendar.MINUTE, 59);
	        endDateCal.set(Calendar.SECOND, 59);
			Collection<TimeBlock> times = RoomAvailability.getInstance().getInstructorAvailability(instructor.getUniqueId(), startDateCal.getTime(), endDateCal.getTime(), RoomAvailabilityInterface.sClassType);
			if (times != null) {
				int sessionYear = session.getSessionStartYear();
		        int firstDOY = session.getDayOfYear(1, session.getPatternStartMonth());
		        int lastDOY = session.getDayOfYear(0, session.getPatternEndMonth()+1);
		        Calendar c = Calendar.getInstance(Locale.US);
		        Formats.Format<Date> df = Formats.getDateFormat(Formats.Pattern.DATE_EVENT_SHORT);
				for (TimeBlock time: times) {
					if (time.getEndTime().before(startDateCal.getTime()) || time.getStartTime().after(endDateCal.getTime())) continue;
	                int dayCode = 0;
	                c.setTime(time.getStartTime());
	                int m = c.get(Calendar.MONTH);
	                int d = c.get(Calendar.DAY_OF_MONTH);
	                if (c.get(Calendar.YEAR)<sessionYear) m-=(12 * (sessionYear - c.get(Calendar.YEAR)));
	                if (c.get(Calendar.YEAR)>sessionYear) m+=(12 * (c.get(Calendar.YEAR) - sessionYear));
	                BitSet weekCode = new BitSet(lastDOY - firstDOY);
	                int offset = session.getDayOfYear(d,m) - firstDOY;
	                weekCode.set(offset);
	                switch (c.get(Calendar.DAY_OF_WEEK)) {
	                    case Calendar.MONDAY    : dayCode = Constants.DAY_CODES[Constants.DAY_MON]; break;
	                    case Calendar.TUESDAY   : dayCode = Constants.DAY_CODES[Constants.DAY_TUE]; break;
	                    case Calendar.WEDNESDAY : dayCode = Constants.DAY_CODES[Constants.DAY_WED]; break;
	                    case Calendar.THURSDAY  : dayCode = Constants.DAY_CODES[Constants.DAY_THU]; break;
	                    case Calendar.FRIDAY    : dayCode = Constants.DAY_CODES[Constants.DAY_FRI]; break;
	                    case Calendar.SATURDAY  : dayCode = Constants.DAY_CODES[Constants.DAY_SAT]; break;
	                    case Calendar.SUNDAY    : dayCode = Constants.DAY_CODES[Constants.DAY_SUN]; break;
	                }
	                int startSlot = (c.get(Calendar.HOUR_OF_DAY)*60 + c.get(Calendar.MINUTE) - Constants.FIRST_SLOT_TIME_MIN) / Constants.SLOT_LENGTH_MIN;
	                c.setTime(time.getEndTime());
	                int endSlot = (c.get(Calendar.HOUR_OF_DAY)*60 + c.get(Calendar.MINUTE) - Constants.FIRST_SLOT_TIME_MIN) / Constants.SLOT_LENGTH_MIN;
	                int length = endSlot - startSlot;
	                if (length<=0) continue;
	                TimeLocation timeLocation = new TimeLocation(dayCode, startSlot, length, 0, 0, null, df.format(time.getStartTime()), weekCode, 0);
	        		TimetableGridCell cell = null;
	        		for (Enumeration<Integer> f=timeLocation.getStartSlots();f.hasMoreElements();) {
	        			int slot = f.nextElement();
	        			if (firstDay>=0 && !timeLocation.getWeekCode().get(firstDay+(slot/Constants.SLOTS_PER_DAY))) continue;
	        			if (cell==null) {
	        				cell =  new TimetableGridCell(
	        						slot/Constants.SLOTS_PER_DAY,
	        						slot%Constants.SLOTS_PER_DAY,
	        						-1, 
	        						-1,
	        						null,
	        						time.getEventName(), 
	        						null,
	        						null,
	        						null, 
	        						time.getEventName() + " (" + time.getEventType() + ")", 
	        						TimetableGridCell.sBgColorNotAvailable, 
	        						length,
	        						0, 
	        						1,
	        						df.format(time.getStartTime()),
	        						weekCode,
	        						null,
	        						Constants.toTime(Constants.SLOT_LENGTH_MIN * startSlot + Constants.FIRST_SLOT_TIME_MIN) + " - " + Constants.toTime(Constants.SLOT_LENGTH_MIN  * endSlot + Constants.FIRST_SLOT_TIME_MIN));
	        			} else {
	        				cell = cell.copyCell(slot/Constants.SLOTS_PER_DAY,cell.getMeetingNumber()+1);
	        			}
	        			addCell(slot,cell);
	        		}
				}
			}
		}
        if (instructor.getPositionType()!=null)
            setType(new Long(instructor.getPositionType().getSortOrder()));
	}
	
	public SolutionGridModel(String solutionIdsStr, Department dept, org.hibernate.Session hibSession, int firstDay, int bgMode) {
		super(sResourceTypeInstructor, dept.getUniqueId().longValue());
		setName(dept.getShortLabel());
		setFirstDay(firstDay);
		Solution firstSolution = null;
		String ownerIds = "";
		for (StringTokenizer s=new StringTokenizer(solutionIdsStr,",");s.hasMoreTokens();) {
			Long solutionId = Long.valueOf(s.nextToken());
			Solution solution = (new SolutionDAO()).get(solutionId, hibSession);
			if (solution==null) continue;
			if (firstSolution==null) firstSolution = solution;
			if (ownerIds.length()>0) ownerIds += ",";
			ownerIds += solution.getOwner().getUniqueId();
		}
		Query q = hibSession.createQuery("select distinct a from Assignment as a inner join a.clazz.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings as o inner join o.subjectArea.department as d where " +
				"a.solution.uniqueId in ("+solutionIdsStr+") and d.uniqueId=:resourceId and " +
				"o.isControl=true");
		q.setCacheable(true);
		q.setLong("resourceId", dept.getUniqueId().longValue());
		List a = q.list();
		setSize(a.size());
		init(a,hibSession,firstDay,bgMode);
	}	
	
	private void init(List assignments, org.hibernate.Session hibSession, int firstDay, int bgMode) {
		for (Iterator i=assignments.iterator();i.hasNext();) {
			Assignment assignment = (Assignment)i.next();
			init(assignment, hibSession, firstDay, bgMode);
		}		
	}
	
	public void init(Assignment assignment, org.hibernate.Session hibSession, int firstDay, int bgMode) {
		TimetableGridCell cell = null;

		int days = assignment.getDays().intValue();
		int start = assignment.getStartSlot().intValue();

        BitSet weekCode = (assignment.getDatePattern()==null?null:assignment.getDatePattern().getPatternBitSet());
		
		for (int j=0;j<Constants.DAY_CODES.length;j++) {
			if ((Constants.DAY_CODES[j]&days)==0) continue;
			if (firstDay>=0) {
				int day = firstDay + j;
				if (!weekCode.get(day)) continue;
			}
			
			if (cell==null)
				cell = createCell(j,start,hibSession, assignment, bgMode);
			else {
				cell = cell.copyCell(j,cell.getMeetingNumber()+1);
				cell.setDays(TimetableGridCell.formatDatePattern(assignment.getDatePattern(), Constants.DAY_CODES[j]));
			}
			addCell(j,start,cell);
		}
	}
	
	public static String hardConflicts2pref(AssignmentPreferenceInfo assignmentInfo) {
		if (assignmentInfo==null) return PreferenceLevel.sNeutral;
		String pref = PreferenceLevel.sNeutral;
		if (assignmentInfo.getNrRoomLocations()==1 && assignmentInfo.getNrTimeLocations()==1) pref = PreferenceLevel.sRequired;
		else if (assignmentInfo.getNrSameTimePlacementsNoConf()>0) pref=PreferenceLevel.sStronglyPreferred;
		else if (assignmentInfo.getNrTimeLocations()>1 && assignmentInfo.getNrSameRoomPlacementsNoConf()>0) pref=PreferenceLevel.sProhibited;
		else if (assignmentInfo.getNrTimeLocations()>1) pref=PreferenceLevel.sNeutral;
		else if (assignmentInfo.getNrSameRoomPlacementsNoConf()>0) pref=PreferenceLevel.sDiscouraged;
		else if (assignmentInfo.getNrRoomLocations()>1) pref=PreferenceLevel.sStronglyDiscouraged;
		else pref=PreferenceLevel.sRequired;
		return pref;
	}
	
	private TimetableGridCell createCell(int day, int slot, org.hibernate.Session hibSession, Assignment assignment, int bgMode) {
		String name = assignment.getClassName();
		
		String title = "";
		int length = assignment.getTimePattern().getSlotsPerMtg().intValue();
		int nrMeetings = assignment.getTimePattern().getNrMeetings().intValue();
		if (assignment.getTimePattern().getType().intValue()==TimePattern.sTypeExactTime) {
			length = ExactTimeMins.getNrSlotsPerMtg(assignment.getDays().intValue(), assignment.getClazz().getSchedulingSubpart().getMinutesPerWk().intValue());
			nrMeetings = 0;
			for (int i=0;i<Constants.NR_DAYS;i++)
				if ((assignment.getDays().intValue() & Constants.DAY_CODES[i])!=0)
					nrMeetings ++;
		}
		String shortComment = null;
		String shortCommentNoColor = null;
		String onClick = "showGwtDialog('Suggestions', 'suggestions.do?id="+assignment.getClassId()+"&op=Reset','900','90%');";
		String background = null;
		StringBuffer roomName = new StringBuffer();
		for (Iterator i=assignment.getRooms().iterator();i.hasNext();) {
			Location r = (Location)i.next();
			if (roomName.length()>0) roomName.append(",");
			roomName.append(r.getLabel());
		}
		
		if (bgMode==sBgModeNone) background = TimetableGridCell.sBgColorNeutral;
		
		if (bgMode==sBgModeNotAvailable) background = TimetableGridCell.sBgColorNotAvailable;
		
		AssignmentPreferenceInfo assignmentInfo = null; 
		if (bgMode!=sBgModeNotAvailable) {
			try {
				assignmentInfo = (AssignmentPreferenceInfo)assignment.getAssignmentInfo("AssignmentInfo"); 
			} catch (Exception e) {
				Debug.error(e);
			}
		}
		
		if (assignmentInfo!=null) {
			int roomPref = (iRoomId==null?assignmentInfo.combineRoomPreference():assignmentInfo.getRoomPreference(iRoomId));
			
			if (bgMode==sBgModeTimePref) {
				background = TimetableGridCell.pref2color(assignmentInfo.getTimePreference());
			} else if (bgMode==sBgModeRoomPref) {
				background = TimetableGridCell.pref2color(roomPref);
			} else if (bgMode==sBgModeStudentConf) {
				background = TimetableGridCell.conflicts2color(assignmentInfo.getNrStudentConflicts());
			} else if (bgMode==sBgModeInstructorBtbPref) {
				background = TimetableGridCell.pref2color(assignmentInfo.getBtbInstructorPreference());
			} else if (bgMode==sBgModePerturbations) {
				String pref = PreferenceLevel.sNeutral;
				if (assignmentInfo.getInitialAssignment()!=null) {
					if (assignmentInfo.getIsInitial()) pref = PreferenceLevel.sStronglyPreferred;
					else if (assignmentInfo.getHasInitialSameTime()) pref = PreferenceLevel.sDiscouraged;
					else if (assignmentInfo.getHasInitialSameRoom()) pref = PreferenceLevel.sStronglyDiscouraged;
					else pref=PreferenceLevel.sProhibited;
				}
				background = TimetableGridCell.pref2color(pref);
			} else if (bgMode==sBgModePerturbationPenalty) {
				background = TimetableGridCell.conflicts2color((int)Math.ceil(assignmentInfo.getPerturbationPenalty()));
			} else if (bgMode==sBgModeHardConflicts) {
				background = TimetableGridCell.pref2color(hardConflicts2pref(assignmentInfo));
			} else if (bgMode==sBgModeDepartmentalBalancing) {
				background = TimetableGridCell.conflicts2colorFast((int)assignmentInfo.getMaxDeptBalancPenalty());
			} else if (bgMode==sBgModeTooBigRooms) {
		        //FIXME: this needs to be changed to reflect the new maxLimit/room ratio model
				int roomCap = assignment.getClazz().getMinRoomLimit().intValue();
				long minRoomSize = assignmentInfo.getMinRoomSize();
				int roomSize = 0;
				for (Iterator i=assignment.getRooms().iterator();i.hasNext();) {
					Location r = (Location)i.next();
					roomSize += r.getCapacity().intValue();
				}
				if (roomSize<roomCap)
					background = TimetableGridCell.pref2color(PreferenceLevel.sRequired);
				else
					background = TimetableGridCell.pref2color(assignmentInfo.getTooBigRoomPreference());
				if (!assignment.getRooms().isEmpty()) {
					shortComment = "<span style='color:rgb(200,200,200)'>"+(assignmentInfo.getNrRoomLocations()==1?"<u>":"")+roomCap+" / "+
							(minRoomSize == Integer.MAX_VALUE ? "-" : String.valueOf(minRoomSize))+" / "+
							roomSize+(assignmentInfo.getNrRoomLocations()==1?"</u>":"")+"</span>";
					shortCommentNoColor = roomCap+" / "+(minRoomSize == Integer.MAX_VALUE ? "-" : String.valueOf(minRoomSize))+" / "+roomSize;
				}
			}
			
			if (shortComment==null)
				shortComment = "<span style='color:rgb(200,200,200)'>"+
					(assignmentInfo.getBestNormalizedTimePreference()<assignmentInfo.getNormalizedTimePreference()?"<span style='color:red'>"+(int)(assignmentInfo.getNormalizedTimePreference()-assignmentInfo.getBestNormalizedTimePreference())+"</span>":""+(int)(assignmentInfo.getNormalizedTimePreference()-assignmentInfo.getBestNormalizedTimePreference())) + ", " +
					(assignmentInfo.getNrStudentConflicts()>0?"<span style='color:rgb(20,130,10)'>"+assignmentInfo.getNrStudentConflicts()+"</span>":""+assignmentInfo.getNrStudentConflicts()) + ", " +
					(assignmentInfo.getBestRoomPreference()<roomPref?"<span style='color:blue'>"+(roomPref-assignmentInfo.getBestRoomPreference())+"</span>":""+(roomPref-assignmentInfo.getBestRoomPreference()))+
					"</span>";
			if (shortCommentNoColor==null)
				shortCommentNoColor = 
					(int)(assignmentInfo.getNormalizedTimePreference()-assignmentInfo.getBestNormalizedTimePreference())+", " +
					assignmentInfo.getNrStudentConflicts()+", " +
					(roomPref-assignmentInfo.getBestRoomPreference());
			
			title = "Time preference: " + (int)assignmentInfo.getNormalizedTimePreference()+"<br>"+
					"Student conflicts: "+assignmentInfo.getNrStudentConflicts()+
						" [committed:" + assignmentInfo.getNrCommitedStudentConflicts() +
						", distance:" + assignmentInfo.getNrDistanceStudentConflicts() +
						", hard:" + assignmentInfo.getNrHardStudentConflicts() + "]<br>"+
					"Room preference: "+roomPref+"<br>"+
					"Back-to-back instructor pref.: "+assignmentInfo.getBtbInstructorPreference()+"<br>"+
					(assignmentInfo.getInitialAssignment()!=null?"Initial assignment: "+(assignmentInfo.getIsInitial()?"<i>current assignment</i>":assignmentInfo.getInitialAssignment())+"<br>":"")+
					(assignmentInfo.getInitialAssignment()!=null?"Perturbation penalty: "+sDF.format(assignmentInfo.getPerturbationPenalty())+"<br>":"")+
					"Non-conflicting placements: "+assignmentInfo.getNrPlacementsNoConf()+"<br>"+
					"Department balance: "+assignmentInfo.getDeptBalancPenalty();
		} else {
			title = assignment.getClassName() + " (" + assignment.getSolution().getOwner().getName() + ")";
		}
		
		if (bgMode==sBgModeDistributionConstPref) {
			Vector constraintInfos = new Vector();
			try {
				constraintInfos = assignment.getConstraintInfos("DistributionInfo"); 
			} catch (Exception e) {
				Debug.error(e);
			}
			if (constraintInfos!=null) {
				PreferenceCombination pref = PreferenceCombination.getDefault();
				for (Enumeration e=constraintInfos.elements();e.hasMoreElements();) {
					GroupConstraintInfo gcInfo = (GroupConstraintInfo)e.nextElement();
					if (gcInfo.isSatisfied()) continue;
					if (PreferenceLevel.sRequired.equals(gcInfo.getPreference()) || PreferenceLevel.sProhibited.equals(gcInfo.getPreference()))
						pref.addPreferenceProlog(PreferenceLevel.sProhibited);
					pref.addPreferenceInt(Math.abs(PreferenceLevel.prolog2int(gcInfo.getPreference())));
				}
				title = title+"<br>Distribution preference: "+pref.getPreferenceProlog();
				background=TimetableGridCell.pref2color(pref.getPreferenceProlog());
			}
		}
		
		String instructors = null;
		for (Iterator i=assignment.getInstructors().iterator(); i.hasNext();) {
			DepartmentalInstructor instructor = (DepartmentalInstructor)i.next();
			if (instructors==null) {
				instructors = "";
			} else {
				instructors += ", ";
			}
			instructors += instructor.getName(DepartmentalInstructor.sNameFormatShort);
		}
		
		String time = Constants.toTime(assignment.getStartSlot() * Constants.SLOT_LENGTH_MIN + Constants.FIRST_SLOT_TIME_MIN) +
				" - " + Constants.toTime((assignment.getStartSlot() + assignment.getSlotPerMtg()) * Constants.SLOT_LENGTH_MIN + Constants.FIRST_SLOT_TIME_MIN - assignment.getBreakTime());
		
		return new TimetableGridCell(
				day,
				slot,
				assignment.getUniqueId().intValue(), 
				(iRoomId==null?0:iRoomId.intValue()),
				roomName.toString(),
				name, 
				shortComment,
				shortCommentNoColor,
				(bgMode==sBgModeNotAvailable?null:onClick), 
				title, 
				background, 
				length, 
				0, 
				nrMeetings,
				TimetableGridCell.formatDatePattern(assignment.getDatePattern(), Constants.DAY_CODES[day]),
				assignment.getDatePattern().getPatternBitSet(),
				instructors,
				time);
	}
}
