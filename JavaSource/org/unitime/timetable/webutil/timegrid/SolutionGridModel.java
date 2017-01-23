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
package org.unitime.timetable.webutil.timegrid;

import java.text.DecimalFormat;
import java.util.BitSet;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import org.cpsolver.coursett.model.TimeLocation;
import org.cpsolver.coursett.preference.PreferenceCombination;
import org.hibernate.Query;
import org.unitime.commons.Debug;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.events.EventLookupBackend;
import org.unitime.timetable.gwt.server.DayCode;
import org.unitime.timetable.interfaces.RoomAvailabilityInterface;
import org.unitime.timetable.interfaces.RoomAvailabilityInterface.TimeBlock;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.CurriculumClassification;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.ExactTimeMins;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.Room;
import org.unitime.timetable.model.RoomSharingModel;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Solution;
import org.unitime.timetable.model.StudentGroup;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.TimePattern;
import org.unitime.timetable.model.dao.SolutionDAO;
import org.unitime.timetable.solver.ui.AssignmentPreferenceInfo;
import org.unitime.timetable.solver.ui.GroupConstraintInfo;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.DateUtils;
import org.unitime.timetable.util.Formats;
import org.unitime.timetable.util.RoomAvailability;
import org.unitime.timetable.util.duration.DurationModel;


/**
 * @author Tomas Muller
 */
public class SolutionGridModel extends TimetableGridModel {
	private static final long serialVersionUID = -3207641071203870684L;
	private transient Long iRoomId = null;
	private static DecimalFormat sDF = new DecimalFormat("0.0");
    
	public SolutionGridModel(String solutionIdsStr, Location room, org.hibernate.Session hibSession, TimetableGridContext context) {
		super(sResourceTypeRoom, room.getUniqueId().intValue());
		setName(room.getLabel());
		setSize(room.getCapacity().intValue());
		setFirstDay(context.getFirstDay());
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
		init(q.list(),hibSession,context);
		
		q = hibSession.createQuery("select distinct a from Room r inner join r.assignments as a "+
		"where r.uniqueId=:roomId and a.solution.commited=true and a.solution.owner.session.uniqueId=:sessionId and a.solution.owner.uniqueId not in ("+ownerIds+")");
		q.setLong("roomId",room.getUniqueId());
        q.setLong("sessionId", room.getSession().getUniqueId().longValue());
		q.setCacheable(true);
		List commitedAssignments = q.list();
		for (Iterator x=commitedAssignments.iterator();x.hasNext();) {
			Assignment a = (Assignment)x.next();
			init(a,hibSession,context.getFirstDay(),sBgModeNotAvailable);
		}
		setUtilization(getUtilization() + countUtilization(context, commitedAssignments));
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
		if (context.isShowEvents() && RoomAvailability.getInstance() != null) {
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
	        			if (context.getFirstDay()>=0 && !timeLocation.getWeekCode().get(context.getFirstDay()+(slot/Constants.SLOTS_PER_DAY))) continue;
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
	
	public SolutionGridModel(String solutionIdsStr, DepartmentalInstructor instructor, org.hibernate.Session hibSession, TimetableGridContext context) {
		super(sResourceTypeInstructor, instructor.getUniqueId().intValue());
		setName(instructor.getLastName()+", "+instructor.getFirstName()+(instructor.getMiddleName()==null?"":" "+instructor.getMiddleName()));
		setFirstDay(context.getFirstDay());
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
		
		if (ApplicationProperty.TimetableGridUseClassInstructors.isTrue()) {
			String check = "";
			if (ApplicationProperty.TimetableGridUseClassInstructorsCheckLead.isTrue())
				check += " and i.lead = true";
			if (ApplicationProperty.TimetableGridUseClassInstructorsCheckClassDisplayInstructors.isTrue())
				check += " and i.classInstructing.displayInstructor = true";
			if (instructor.getExternalUniqueId() != null && !instructor.getExternalUniqueId().isEmpty()) {
				Query q = hibSession.createQuery("select distinct a from Assignment as a inner join a.clazz.classInstructors as i where a.solution.uniqueId in (" + solutionIdsStr + ") and i.instructor.externalUniqueId = :extId" + check);
				q.setString("extId", instructor.getExternalUniqueId());
				q.setCacheable(true);
				init(q.list(), hibSession, context);
				q = hibSession.createQuery("select distinct a from ClassInstructor i inner join i.classInstructing.assignments as a "+
						"where i.instructor.externalUniqueId = :extId and a.solution.commited = true and a.solution.owner.session.uniqueId = :sessionId and a.solution.owner.uniqueId not in (" + ownerIds + ")" + check);
				q.setString("extId",instructor.getExternalUniqueId());
	            q.setLong("sessionId", instructor.getDepartment().getSession().getUniqueId().longValue());
				q.setCacheable(true);
				commitedAssignments = q.list();
			} else {
				Query q = hibSession.createQuery("select distinct a from Assignment as a inner join a.clazz.classInstructors as i where a.solution.uniqueId in (" + solutionIdsStr + ") and i.instructor.uniqueId = :instructorId" + check);
				q.setLong("instructorId", instructor.getUniqueId());
				q.setCacheable(true);
				init(q.list(),hibSession,context);
				q = hibSession.createQuery("select distinct a from ClassInstructor i inner join i.classInstructing.assignments as a "+
						"where i.instructor.uniqueId = :instructorId and a.solution.commited = true and a.solution.owner.session.uniqueId = :sessionId and a.solution.owner.uniqueId not in (" + ownerIds + ")" + check);
				q.setLong("instructorId",instructor.getUniqueId());
	            q.setLong("sessionId", instructor.getDepartment().getSession().getUniqueId().longValue());
				q.setCacheable(true);
				commitedAssignments = q.list();
			}
		} else {
			if (instructor.getExternalUniqueId()!=null && instructor.getExternalUniqueId().length()>0) {
				Query q = hibSession.createQuery("select distinct a from Assignment as a inner join a.instructors as i where a.solution.uniqueId in ("+solutionIdsStr+") and i.externalUniqueId=:puid");
				q.setString("puid", instructor.getExternalUniqueId());
				q.setCacheable(true);
				init(q.list(),hibSession,context);
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
				init(q.list(),hibSession,context);
				q = hibSession.createQuery("select distinct a from DepartmentalInstructor i inner join i.assignments as a "+
						"where i.uniqueId=:instructorId and a.solution.commited=true and a.solution.owner.session.uniqueId=:sessionId and a.solution.owner.uniqueId not in ("+ownerIds+")");
				q.setLong("instructorId",instructor.getUniqueId());
	            q.setLong("sessionId", instructor.getDepartment().getSession().getUniqueId().longValue());
				q.setCacheable(true);
				commitedAssignments = q.list();
			}
		}
		
		setUtilization(getUtilization() + countUtilization(context, commitedAssignments));
		for (Iterator x=commitedAssignments.iterator();x.hasNext();) {
			Assignment a = (Assignment)x.next();
			init(a,hibSession,context.getFirstDay(),sBgModeNotAvailable);
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
		if (context.isShowEvents() && RoomAvailability.getInstance() != null) {
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
	        			if (context.getFirstDay()>=0 && !timeLocation.getWeekCode().get(context.getFirstDay()+(slot/Constants.SLOTS_PER_DAY))) continue;
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
	
	public SolutionGridModel(String solutionIdsStr, Department dept, org.hibernate.Session hibSession, TimetableGridContext context) {
		super(sResourceTypeDepartment, dept.getUniqueId().longValue());
		setName(dept.getShortLabel());
		setFirstDay(context.getFirstDay());
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
		init(a,hibSession,context);
	}
	
	public SolutionGridModel(String solutionIdsStr, SubjectArea sa, org.hibernate.Session hibSession, TimetableGridContext context) {
		super(sResourceTypeSubjectArea, sa.getUniqueId().longValue());
		setName(sa.getSubjectAreaAbbreviation());
		setFirstDay(context.getFirstDay());
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
		Query q = hibSession.createQuery("select distinct a from Assignment as a inner join a.clazz.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings as o inner join o.subjectArea as sa where " +
				"a.solution.uniqueId in ("+solutionIdsStr+") and sa.uniqueId=:resourceId and " +
				"o.isControl=true");
		q.setCacheable(true);
		q.setLong("resourceId", sa.getUniqueId().longValue());
		List a = q.list();
		setSize(a.size());
		init(a,hibSession,context);
	}
	
	public SolutionGridModel(String solutionIdsStr, CurriculumClassification cc, org.hibernate.Session hibSession, TimetableGridContext context) {
		super(sResourceTypeCurriculum, cc.getUniqueId().longValue());
		setName(cc.getCurriculum().getAbbv() + " " + cc.getName());
		setFirstDay(context.getFirstDay());
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
		Query q = hibSession.createQuery("select distinct a from CurriculumClassification cc inner join cc.courses cx, Assignment a inner join a.clazz.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings co where " +
				"a.solution.uniqueId in ("+solutionIdsStr+") and cc.uniqueId=:resourceId and " +
				"cx.course = co");
		q.setCacheable(true);
		q.setLong("resourceId", cc.getUniqueId());
		List a = q.list();
		Map<Long, Set<Long>[]> restrictions = new Hashtable<Long, Set<Long>[]>();
		for (Object[] o: (List<Object[]>)hibSession.createQuery(
				"select distinct cc.course.instructionalOffering.uniqueId, (case when g.uniqueId is null then x.uniqueId else g.uniqueId end), z.uniqueId " +
				"from CurriculumReservation r left outer join r.configurations g left outer join r.classes z left outer join z.schedulingSubpart.instrOfferingConfig x " +
				"left outer join r.majors rm left outer join r.classifications rc, " +
				"CurriculumCourse cc inner join cc.classification.curriculum.majors cm " +
				"where cc.classification.uniqueId = :resourceId " +
				"and cc.course.instructionalOffering = r.instructionalOffering and r.area = cc.classification.curriculum.academicArea "+
				"and (rm is null or rm = cm) and (rc is null or rc = cc.classification.academicClassification)")
				.setLong("resourceId", cc.getUniqueId()).setCacheable(true).list()) {
			Long offeringId = (Long)o[0];
			Long configId = (Long)o[1];
			Long clazzId = (Long)o[2];
			Set<Long>[] r = restrictions.get(offeringId);
			if (r == null) {
				r = new Set[] { new HashSet<Long>(), new HashSet<Long>()};
				restrictions.put(offeringId, r);
			}
			if (configId != null) r[0].add(configId);
			if (clazzId != null) r[1].add(clazzId);
		}
		if (!restrictions.isEmpty())
			for (Iterator i = a.iterator(); i.hasNext(); ) {
				Assignment asgn = (Assignment)i.next();
				Set<Long>[] r = (restrictions == null ? null : restrictions.get(asgn.getClazz().getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().getUniqueId()));
	    		if (r != null && EventLookupBackend.hide(r, asgn.getClazz())) i.remove();
			}
		setSize(a.size());
		init(a,hibSession,context);
	}
	
	public SolutionGridModel(String solutionIdsStr, StudentGroup g, org.hibernate.Session hibSession, TimetableGridContext context) {
		super(sResourceTypeStudentGroup, g.getUniqueId());
		setName(g.getGroupAbbreviation());
		setFirstDay(context.getFirstDay());
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
		Query q = hibSession.createQuery(
				"select distinct a from StudentGroupReservation r, Assignment a inner join a.clazz.schedulingSubpart.instrOfferingConfig.instructionalOffering as io where "+
				"a.solution.uniqueId in ("+solutionIdsStr+") and io = r.instructionalOffering and r.group.uniqueId=:resourceId");
		q.setCacheable(true);
		q.setLong("resourceId", g.getUniqueId());
		List a = q.list();
		Map<Long, Set<Long>[]> restrictions = new Hashtable<Long, Set<Long>[]>();
		for (Object[] o: (List<Object[]>)hibSession.createQuery(
				"select distinct r.instructionalOffering.uniqueId, (case when g.uniqueId is null then x.uniqueId else g.uniqueId end), z.uniqueId " +
				"from StudentGroupReservation r left outer join r.configurations g left outer join r.classes z left outer join z.schedulingSubpart.instrOfferingConfig x " +
				"where r.group.uniqueId = :resourceId")
				.setLong("resourceId", g.getUniqueId()).setCacheable(true).list()) {
			Long offeringId = (Long)o[0];
			Long configId = (Long)o[1];
			Long clazzId = (Long)o[2];
			Set<Long>[] r = restrictions.get(offeringId);
			if (r == null) {
				r = new Set[] { new HashSet<Long>(), new HashSet<Long>()};
				restrictions.put(offeringId, r);
			}
			if (configId != null) r[0].add(configId);
			if (clazzId != null) r[1].add(clazzId);
		}
		if (!restrictions.isEmpty())
			for (Iterator i = a.iterator(); i.hasNext(); ) {
				Assignment asgn = (Assignment)i.next();
				Set<Long>[] r = (restrictions == null ? null : restrictions.get(asgn.getClazz().getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().getUniqueId()));
	    		if (r != null && EventLookupBackend.hide(r, asgn.getClazz())) i.remove();
			}
		setSize(a.size());
		init(a,hibSession,context);
	}
	
	private void init(List assignments, org.hibernate.Session hibSession, TimetableGridContext context) {
		for (Iterator i=assignments.iterator();i.hasNext();) {
			Assignment assignment = (Assignment)i.next();
			init(assignment, hibSession, context.getFirstDay(), context.getBgMode());
		}
		setUtilization(countUtilization(context, assignments));
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
			DurationModel dm = assignment.getClazz().getSchedulingSubpart().getInstrOfferingConfig().getDurationModel();
			int minsPerMeeting = dm.getExactTimeMinutesPerMeeting(assignment.getClazz().getSchedulingSubpart().getMinutesPerWk(), assignment.getDatePattern(), assignment.getDays()); 
			length = ExactTimeMins.getNrSlotsPerMtg(minsPerMeeting);
			nrMeetings = DayCode.nrDays(assignment.getDays());
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
		if (ApplicationProperty.TimetableGridUseClassInstructors.isTrue()) {
			if (!ApplicationProperty.TimetableGridUseClassInstructorsCheckClassDisplayInstructors.isTrue() || assignment.getClazz().isDisplayInstructor()) {
				for (Iterator<ClassInstructor> i = assignment.getClazz().getClassInstructors().iterator(); i.hasNext();) {
					ClassInstructor instructor = i.next();
					if (!instructor.isLead() && ApplicationProperty.TimetableGridUseClassInstructorsCheckLead.isTrue())
						continue;
					if (instructors==null) {
						instructors = "";
					} else {
						instructors += ", ";
					}
					instructors += instructor.getInstructor().getName(DepartmentalInstructor.sNameFormatShort);
				}
			}
		} else {
			for (Iterator i=assignment.getInstructors().iterator(); i.hasNext();) {
				DepartmentalInstructor instructor = (DepartmentalInstructor)i.next();
				if (instructors==null) {
					instructors = "";
				} else {
					instructors += ", ";
				}
				instructors += instructor.getName(DepartmentalInstructor.sNameFormatShort);
			}
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
	
    private double countUtilization(TimetableGridContext context, List assignments) {
    	Set<Integer> slots = new HashSet<Integer>();
        for (Iterator i = assignments.iterator(); i.hasNext(); ) {
        	Assignment assignment = (Assignment) i.next();
        	TimeLocation t = (assignment == null ? null : assignment.getTimeLocation());
            if (t == null) continue;
            int start = Math.max(context.getFirstSlot(), t.getStartSlot());
            int stop = Math.min(context.getLastSlot(), t.getStartSlot() + t.getLength() - 1);
            if (start > stop) continue;
            if (context.getFirstDay() >= 0) {
                for (int idx = context.getFirstDay(); idx < 7 + context.getFirstDay(); idx ++) {
                	int dow = ((idx + context.getStartDayDayOfWeek()) % 7);
                	if (t.getWeekCode().get(idx) && (t.getDayCode() & Constants.DAY_CODES[dow]) != 0 && (context.getDayCode() & Constants.DAY_CODES[dow]) != 0) {
                		for (int slot = start; slot <= stop; slot ++)
                			slots.add(288 * idx + slot);
                	}
                }
            } else {
                int idx = -1;
                while ((idx = t.getWeekCode().nextSetBit(1 + idx)) >= 0) {
                	int dow = ((idx + context.getStartDayDayOfWeek()) % 7);
                	if (context.getDefaultDatePattern().get(idx) && (t.getDayCode() & Constants.DAY_CODES[dow]) != 0 && (context.getDayCode() & Constants.DAY_CODES[dow]) != 0) {
                		for (int slot = start; slot <= stop; slot ++)
                			slots.add(288 * idx + slot);
                	}
                }
            }
        }
        return slots.size() / context.getNumberOfWeeks();
    }	
}
