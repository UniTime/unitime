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
package org.unitime.timetable.server.solver;

import java.text.DecimalFormat;
import java.util.ArrayList;
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
import java.util.TreeSet;
import java.util.Vector;

import org.cpsolver.coursett.model.TimeLocation;
import org.cpsolver.coursett.preference.PreferenceCombination;
import org.hibernate.Query;
import org.hibernate.type.LongType;
import org.unitime.commons.Debug;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.events.EventLookupBackend;
import org.unitime.timetable.gwt.server.DayCode;
import org.unitime.timetable.gwt.shared.TimetableGridInterface.TimetableGridBackground;
import org.unitime.timetable.gwt.shared.TimetableGridInterface.TimetableGridCell;
import org.unitime.timetable.gwt.shared.TimetableGridInterface.TimetableGridCell.Property;
import org.unitime.timetable.gwt.shared.TimetableGridInterface.TimetableGridModel;
import org.unitime.timetable.interfaces.RoomAvailabilityInterface;
import org.unitime.timetable.interfaces.RoomAvailabilityInterface.TimeBlock;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.CurriculumClassification;
import org.unitime.timetable.model.DatePattern;
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
import org.unitime.timetable.solver.ui.StudentGroupInfo;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.DateUtils;
import org.unitime.timetable.util.Formats;
import org.unitime.timetable.util.NameFormat;
import org.unitime.timetable.util.RoomAvailability;
import org.unitime.timetable.util.duration.DurationModel;

/**
 * @author Tomas Muller
 */
public class TimetableGridSolutionHelper extends TimetableGridHelper {
	private static DecimalFormat sDF = new DecimalFormat("0.0");

	protected static void createCells(TimetableGridModel model, List<Assignment> assignments, org.hibernate.Session hibSession, TimetableGridContext context, boolean notAvailable) {
    	for (Assignment assignment: assignments) {
    		createCells(model, assignment, hibSession, context, false);
		}
    }
    
	protected static List<TimetableGridCell> createCells(TimetableGridModel model, Assignment assignment, org.hibernate.Session hibSession, TimetableGridContext context, boolean notAvailable) {
    	List<TimetableGridCell> cells = new ArrayList<TimetableGridCell>();
    	TimetableGridCell cell = null;

		int days = assignment.getDays().intValue();
		int start = assignment.getStartSlot().intValue();

        BitSet weekCode = (assignment.getDatePattern() == null ? null : assignment.getDatePattern().getPatternBitSet());
		
		for (int j = 0; j < Constants.DAY_CODES.length; j++) {
			int d = (j + context.getWeekOffset()) % 7;
			if ((Constants.DAY_CODES[d] & days) == 0) continue;
			if (context.getFirstDay() >= 0) {
				int day = context.getFirstDay() + j;
				if (!weekCode.get(day)) continue;
			}
			if (cell == null)
				cell = createCell(model, d, start, hibSession, assignment, context, notAvailable);
			else {
				cell = new TimetableGridCell(cell, d, formatDatePattern(assignment.getDatePattern(), Constants.DAY_CODES[d]));
			}
			
			model.addCell(cell);
			cells.add(cell);
		}
		
		return cells;
    }
    
	protected static void createMeetingCells(TimetableGridModel model, Session session, TimetableGridContext context, Collection<TimeBlock> times, String room) {
		if (times == null) return;
		int sessionYear = session.getSessionStartYear();
        int firstDOY = session.getDayOfYear(1, session.getPatternStartMonth());
        int lastDOY = session.getDayOfYear(0, session.getPatternEndMonth() + 1);
        Calendar c = Calendar.getInstance(Locale.US);
        Formats.Format<Date> df = Formats.getDateFormat(Formats.Pattern.DATE_EVENT_SHORT);
		for (TimeBlock time: times) {
			if (time.getEndTime().before(context.getSessionStartDate()) || time.getStartTime().after(context.getSessionEndDate())) continue;
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
    			int idx = (7 + slot/Constants.SLOTS_PER_DAY - context.getWeekOffset()) % 7;
    			if (context.getFirstDay()>=0 && !timeLocation.getWeekCode().get(context.getFirstDay() + idx)) continue;
    			if (cell == null) {
    				cell = new TimetableGridCell();
    				cell.setType(TimetableGridCell.Type.Event);
    				cell.setId(time.getEventId());
    				cell.setDay(slot / Constants.SLOTS_PER_DAY);
    				cell.setSlot(slot % Constants.SLOTS_PER_DAY);
    				cell.addRoom(room);
    				cell.addName(time.getEventName());
    				cell.setProperty(Property.EventType, time.getEventType());
    				cell.setBackground(sBgColorNotAvailable);
    				cell.setLength(length);
    				cell.setTime(Constants.toTime(Constants.SLOT_LENGTH_MIN * startSlot + Constants.FIRST_SLOT_TIME_MIN) + " - " + Constants.toTime(Constants.SLOT_LENGTH_MIN  * endSlot + Constants.FIRST_SLOT_TIME_MIN));
    				cell.setDate(df.format(time.getStartTime()));
    				cell.setWeekCode(pattern2string(weekCode));
    			} else {
    				cell = new TimetableGridCell(cell, slot / Constants.SLOTS_PER_DAY, null);
    			}
    			model.addCell(cell);
    		}
		}
    }
    
    protected static TimetableGridCell createCell(TimetableGridModel model, int day, int slot, org.hibernate.Session hibSession, Assignment assignment, TimetableGridContext context, boolean notAvailable) {
    	TimetableGridCell cell = new TimetableGridCell();
    	cell.setType(TimetableGridCell.Type.Class);
    	cell.setId(assignment.getClassId());
    	cell.addName(assignment.getClazz().getClassLabel(context.isShowClassSuffix()));
    	cell.setCommitted(notAvailable);
    	cell.setDay(day);
    	cell.setSlot(slot);
    	
    	if (context.isShowCrossLists()) {
    		Set<CourseOffering> courses = assignment.getClazz().getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().getCourseOfferings();
    		if (courses.size() > 1) {
    			for (CourseOffering course: new TreeSet<CourseOffering>(courses)) {
    				if (course.isIsControl()) continue;
    				cell.addName(assignment.getClazz().getClassLabel(course, context.isShowClassSuffix()));
    			}
    		}
    	}
    	
		cell.setLength(assignment.getTimePattern().getSlotsPerMtg());
		if (assignment.getTimePattern().getType() == TimePattern.sTypeExactTime) {
			DurationModel dm = assignment.getClazz().getSchedulingSubpart().getInstrOfferingConfig().getDurationModel();
			int minsPerMeeting = dm.getExactTimeMinutesPerMeeting(assignment.getClazz().getSchedulingSubpart().getMinutesPerWk(), assignment.getDatePattern(), assignment.getDays());
			cell.setLength(ExactTimeMins.getNrSlotsPerMtg(minsPerMeeting));
		}
		for (Location location: assignment.getRooms())
			cell.addRoom(location.getLabel());
		
		int bgMode = context.getBgMode();
		AssignmentPreferenceInfo assignmentInfo = null; 
		if (notAvailable) {
			cell.setBackground(sBgColorNotAvailable);
		} else {
			try {
				assignmentInfo = (AssignmentPreferenceInfo)assignment.getAssignmentInfo("AssignmentInfo"); 
			} catch (Exception e) {
				Debug.error(e);
			}
		}
		
		if (assignmentInfo != null) {
			int roomPref = assignmentInfo.combineRoomPreference();
			if (model.getResourceType() == ResourceType.ROOM.ordinal() && model.getResourceId() != null)
				roomPref = assignmentInfo.getRoomPreference(model.getResourceId());
			switch (BgMode.values()[bgMode]) {
			case TimePref:
				cell.setBackground(pref2color(assignmentInfo.getTimePreference()));
				break;
			case RoomPref:
				cell.setBackground(pref2color(roomPref));
				break;
			case StudentConf:
				cell.setBackground(conflicts2color(assignmentInfo.getNrStudentConflicts()));
				break;
			case InstructorBtbPref:
				cell.setBackground(pref2color(assignmentInfo.getBtbInstructorPreference()));
				break;
			case Perturbations:
				String pref = PreferenceLevel.sNeutral;
				if (assignmentInfo.getInitialAssignment()!=null) {
					if (assignmentInfo.getIsInitial()) pref = PreferenceLevel.sStronglyPreferred;
					else if (assignmentInfo.getHasInitialSameTime()) pref = PreferenceLevel.sDiscouraged;
					else if (assignmentInfo.getHasInitialSameRoom()) pref = PreferenceLevel.sStronglyDiscouraged;
					else pref=PreferenceLevel.sProhibited;
				}
				cell.setBackground(pref2color(pref));
				break;
			case PerturbationPenalty:
				cell.setBackground(conflicts2color((int)Math.ceil(assignmentInfo.getPerturbationPenalty())));
				break;
			case HardConflicts:
				cell.setBackground(pref2color(hardConflicts2pref(assignmentInfo)));
				break;
			case DepartmentalBalancing:
				cell.setBackground(conflicts2colorFast((int)assignmentInfo.getMaxDeptBalancPenalty()));
				break;
			case TooBigRooms:
				//FIXME: this needs to be changed to reflect the new maxLimit/room ratio model
				int roomCap = assignment.getClazz().getMinRoomLimit().intValue();
				long minRoomSize = assignmentInfo.getMinRoomSize();
				int roomSize = 0;
				for (Iterator i=assignment.getRooms().iterator();i.hasNext();) {
					Location r = (Location)i.next();
					roomSize += r.getCapacity().intValue();
				}
				if (roomSize < roomCap)
					cell.setBackground(pref2color(PreferenceLevel.sRequired));
				else
					cell.setBackground(pref2color(assignmentInfo.getTooBigRoomPreference()));
				if (!assignment.getRooms().isEmpty()) {
					cell.setPreference((assignmentInfo.getNrRoomLocations() == 1 ? "<u>" : "") +
							roomCap + " / " + (minRoomSize == Integer.MAX_VALUE ? "-" : String.valueOf(minRoomSize)) + " / " + roomSize +
							(assignmentInfo.getNrRoomLocations() == 1 ? "</u>":""));
				}
				break;
			case StudentGroups:
				if (assignmentInfo.getStudentGroupPercent() != null)
					cell.setBackground(percentage2color(assignmentInfo.getStudentGroupPercent()));
				if (assignmentInfo.getStudentGroupComment() != null) {
					cell.setPreference(assignmentInfo.getStudentGroupComment());
				}
			}
			
			if (!cell.hasPreference()) {
				cell.setPreference(
					(assignmentInfo.getBestNormalizedTimePreference()<assignmentInfo.getNormalizedTimePreference()?"<span style='color:red'>"+(int)(assignmentInfo.getNormalizedTimePreference()-assignmentInfo.getBestNormalizedTimePreference())+"</span>":""+(int)(assignmentInfo.getNormalizedTimePreference()-assignmentInfo.getBestNormalizedTimePreference())) + ", " +
					(assignmentInfo.getNrStudentConflicts()>0?"<span style='color:rgb(20,130,10)'>"+assignmentInfo.getNrStudentConflicts()+"</span>":""+assignmentInfo.getNrStudentConflicts()) + ", " +
					(assignmentInfo.getBestRoomPreference()<roomPref?"<span style='color:blue'>"+(roomPref-assignmentInfo.getBestRoomPreference())+"</span>":""+(roomPref-assignmentInfo.getBestRoomPreference()))
					);
			}
			cell.setProperty(Property.TimePreference, (int)assignmentInfo.getNormalizedTimePreference());
			cell.setProperty(Property.StudentConflicts,  assignmentInfo.getNrStudentConflicts());
			cell.setProperty(Property.StudentConflictsCommitted, assignmentInfo.getNrCommitedStudentConflicts());
			cell.setProperty(Property.StudentConflictsDistance, assignmentInfo.getNrDistanceStudentConflicts());
			cell.setProperty(Property.StudentConflictsHard, assignmentInfo.getNrHardStudentConflicts());
			cell.setProperty(Property.RoomPreference, roomPref);
			cell.setProperty(Property.InstructorPreference, assignmentInfo.getBtbInstructorPreference());
			if (assignmentInfo.getInitialAssignment() != null) {
				cell.setProperty(Property.InitialAssignment, assignmentInfo.getIsInitial() ? "-" : assignmentInfo.getInitialAssignment());
				cell.setProperty(Property.PerturbationPenalty, sDF.format(assignmentInfo.getPerturbationPenalty()));
			}
			cell.setProperty(Property.NonConflictingPlacements, assignmentInfo.getNrPlacementsNoConf());
			cell.setProperty(Property.DepartmentBalance, sDF.format(assignmentInfo.getDeptBalancPenalty()));
		} else {
			cell.setProperty(Property.Owner, assignment.getSolution().getOwner().getName());
		}
		
		if (bgMode == BgMode.DistributionConstPref.ordinal()) {
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
				cell.setProperty(Property.DistributionPreference, pref.getPreferenceProlog());
				cell.setBackground(pref2color(pref.getPreferenceProlog()));
			}
		}
		
		if (ApplicationProperty.TimetableGridUseClassInstructors.isTrue()) {
			if (!ApplicationProperty.TimetableGridUseClassInstructorsCheckClassDisplayInstructors.isTrue() || assignment.getClazz().isDisplayInstructor()) {
				for (ClassInstructor instructor: assignment.getClazz().getClassInstructors()) {
					if (instructor.isLead() || ApplicationProperty.TimetableGridUseClassInstructorsCheckLead.isFalse())
						cell.addInstructor(instructor.getInstructor().getName(context.getInstructorNameFormat()));
				}
			}
		} else {
			for (DepartmentalInstructor instructor: assignment.getInstructors()) {
				cell.addInstructor(instructor.getName(context.getInstructorNameFormat()));
			}
		}
		
		String days = "";
        for (int i = 0; i < Constants.DAY_CODES.length; i++)
            if ((assignment.getDays() & Constants.DAY_CODES[i]) != 0)
            	days += CONST.shortDays()[i];
		cell.setDays(days);
		cell.setTime(
				Constants.toTime(assignment.getStartSlot() * Constants.SLOT_LENGTH_MIN + Constants.FIRST_SLOT_TIME_MIN) +
				" - " + Constants.toTime((assignment.getStartSlot() + assignment.getSlotPerMtg()) * Constants.SLOT_LENGTH_MIN + Constants.FIRST_SLOT_TIME_MIN - assignment.getBreakTime())
				);
		cell.setDate(formatDatePattern(assignment.getDatePattern(), Constants.DAY_CODES[day]));
		cell.setWeekCode(pattern2string(assignment.getDatePattern().getPatternBitSet()));
		return cell;
	}
    
    protected static double countUtilization(Iterable<Assignment> assignments, TimetableGridContext context) {
    	Set<Integer> slots = new HashSet<Integer>();
        for (Assignment assignment: assignments) {
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
        return slots.size() / (context.getNumberOfWeeks() * 12);
    }
    
    public static String formatDatePattern(DatePattern dp, int dayCode) {
    	if (dp == null || dp.isDefault()) return null;
    	String format = ApplicationProperty.DatePatternFormatUseDates.value();
    	if ("never".equals(format)) return dp.getName();
    	if ("extended".equals(format) && dp.getType() != DatePattern.sTypeExtended) return dp.getName();
    	if ("alternate".equals(format) && dp.getType() == DatePattern.sTypeAlternate) return dp.getName();
    	BitSet weekCode = dp.getPatternBitSet();
    	if (weekCode.isEmpty()) return dp.getName();
    	Calendar cal = Calendar.getInstance(Locale.US); cal.setLenient(true);
    	Date dpFirstDate = DateUtils.getDate(1, dp.getSession().getPatternStartMonth(), dp.getSession().getSessionStartYear());
    	cal.setTime(dpFirstDate);
    	int idx = weekCode.nextSetBit(0);
    	cal.add(Calendar.DAY_OF_YEAR, idx);
    	Date first = null;
    	while (idx < weekCode.size() && first == null) {
    		if (weekCode.get(idx)) {
        		int dow = cal.get(Calendar.DAY_OF_WEEK);
        		switch (dow) {
        		case Calendar.MONDAY:
        			if ((dayCode & DayCode.MON.getCode()) != 0) first = cal.getTime();
        			break;
        		case Calendar.TUESDAY:
        			if ((dayCode & DayCode.TUE.getCode()) != 0) first = cal.getTime();
        			break;
        		case Calendar.WEDNESDAY:
        			if ((dayCode & DayCode.WED.getCode()) != 0) first = cal.getTime();
        			break;
        		case Calendar.THURSDAY:
        			if ((dayCode & DayCode.THU.getCode()) != 0) first = cal.getTime();
        			break;
        		case Calendar.FRIDAY:
        			if ((dayCode & DayCode.FRI.getCode()) != 0) first = cal.getTime();
        			break;
        		case Calendar.SATURDAY:
        			if ((dayCode & DayCode.SAT.getCode()) != 0) first = cal.getTime();
        			break;
        		case Calendar.SUNDAY:
        			if ((dayCode & DayCode.SUN.getCode()) != 0) first = cal.getTime();
        			break;
        		}
        	}
    		cal.add(Calendar.DAY_OF_YEAR, 1); idx++;
    	}
    	if (first == null) return dp.getName();
    	cal.setTime(dpFirstDate);
    	idx = weekCode.length() - 1;
    	cal.add(Calendar.DAY_OF_YEAR, idx);
    	Date last = null;
    	while (idx >= 0 && last == null) {
    		if (weekCode.get(idx)) {
        		int dow = cal.get(Calendar.DAY_OF_WEEK);
        		switch (dow) {
        		case Calendar.MONDAY:
        			if ((dayCode & DayCode.MON.getCode()) != 0) last = cal.getTime();
        			break;
        		case Calendar.TUESDAY:
        			if ((dayCode & DayCode.TUE.getCode()) != 0) last = cal.getTime();
        			break;
        		case Calendar.WEDNESDAY:
        			if ((dayCode & DayCode.WED.getCode()) != 0) last = cal.getTime();
        			break;
        		case Calendar.THURSDAY:
        			if ((dayCode & DayCode.THU.getCode()) != 0) last = cal.getTime();
        			break;
        		case Calendar.FRIDAY:
        			if ((dayCode & DayCode.FRI.getCode()) != 0) last = cal.getTime();
        			break;
        		case Calendar.SATURDAY:
        			if ((dayCode & DayCode.SAT.getCode()) != 0) last = cal.getTime();
        			break;
        		case Calendar.SUNDAY:
        			if ((dayCode & DayCode.SUN.getCode()) != 0) last = cal.getTime();
        			break;
        		}
        	}
    		cal.add(Calendar.DAY_OF_YEAR, -1); idx--;
    	}
    	if (last == null) return dp.getName();
        Formats.Format<Date> dpf = Formats.getDateFormat(Formats.Pattern.DATE_EVENT_SHORT);
    	return dpf.format(first) + (first.equals(last) ? "" : " - " + dpf.format(last));
    }
    
    public static TimetableGridModel createModel(String solutionIdsStr, Location room, org.hibernate.Session hibSession, TimetableGridContext context) {
    	TimetableGridModel model = new TimetableGridModel(ResourceType.ROOM.ordinal(), room.getUniqueId());
    	model.setName(room.getLabel());
    	model.setSize(room.getCapacity().intValue());
    	model.setFirstDay(context.getFirstDay());
    	model.setFirstSessionDay(context.getFirstSessionDay());
    	model.setFirstDate(context.getFirstDate());
    	Solution firstSolution = null;
		String ownerIds = "";
		HashSet deptIds = new HashSet();
		for (StringTokenizer s=new StringTokenizer(solutionIdsStr,",");s.hasMoreTokens();) {
			Long solutionId = Long.valueOf(s.nextToken());
			Solution solution = SolutionDAO.getInstance().get(solutionId, hibSession);
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
		List<Assignment> assignments = q.list();
		createCells(model, assignments, hibSession, context, false);
		q = hibSession.createQuery("select distinct a from Room r inner join r.assignments as a "+
				"where r.uniqueId=:roomId and a.solution.commited=true and a.solution.owner.session.uniqueId=:sessionId and a.solution.owner.uniqueId not in ("+ownerIds+")");
		q.setLong("roomId",room.getUniqueId());
        q.setLong("sessionId", room.getSession().getUniqueId().longValue());
		q.setCacheable(true);
		List<Assignment> committed = q.list();
		createCells(model, committed, hibSession, context, true);
		model.setUtilization(countUtilization(new Combine<Assignment>(assignments, committed), context));
		RoomSharingModel sharing = room.getRoomSharingModel();
		if (sharing != null) {
			for (int i = 0; i < Constants.DAY_CODES.length; i++) {
				int start = 0; Boolean av = null;
				for (int j = 0; j < Constants.SLOTS_PER_DAY; j++) {
					Boolean available;
					if (sharing.isFreeForAll(i,j)) {
						available = true;
					} else if (sharing.isNotAvailable(i,j)) {
						available = false;
					} else {
						Long dept = sharing.getDepartmentId(i,j);
						available = (dept == null || deptIds.contains(dept));
					}
					if (av == null) {
						av = available; start = j;
					} else if (!av.equals(available)) {
						if (!av) {
							TimetableGridBackground bg = new TimetableGridBackground();
							bg.setBackground(sBgColorNotAvailable);
							bg.setSlot(start);
							bg.setLength(j - start);
							bg.setDay(i);
							bg.setAvailable(false);
							model.addBackground(bg);
						}
						av = available; start = j;
					}
				}
				if (av != null && !av) {
					TimetableGridBackground bg = new TimetableGridBackground();
					bg.setBackground(sBgColorNotAvailable);
					bg.setSlot(start);
					bg.setLength(Constants.SLOTS_PER_DAY - start);
					bg.setDay(i);
					bg.setAvailable(false);
					model.addBackground(bg);
				}
			}
		}
		if (context.isShowEvents() && RoomAvailability.getInstance() != null) {
			createMeetingCells(model, room.getSession(), context,
					RoomAvailability.getInstance().getRoomAvailability(room.getUniqueId(), context.getSessionStartDate(), context.getSessionEndDate(), RoomAvailabilityInterface.sClassType),
					room.getLabel());
		}
        model.setType(room instanceof Room ? ((Room)room).getRoomType().getUniqueId(): null);
        return model;
	}
    
    public static TimetableGridModel createModel(String solutionIdsStr, DepartmentalInstructor instructor, org.hibernate.Session hibSession, TimetableGridContext context) {
    	TimetableGridModel model = new TimetableGridModel(ResourceType.INSTRUCTOR.ordinal(), instructor.getUniqueId());
    	model.setName(instructor.getName(NameFormat.LAST_FIRST_MIDDLE.reference()));
    	model.setFirstDay(context.getFirstDay());
    	model.setFirstSessionDay(context.getFirstSessionDay());
    	model.setFirstDate(context.getFirstDate());
    	
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
		
		List<Assignment> assignments = null;
		List<Assignment> committed = null;
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
				assignments = q.list();
				q = hibSession.createQuery("select distinct a from ClassInstructor i inner join i.classInstructing.assignments as a "+
						"where i.instructor.externalUniqueId = :extId and a.solution.commited = true and a.solution.owner.session.uniqueId = :sessionId and a.solution.owner.uniqueId not in (" + ownerIds + ")" + check);
				q.setString("extId",instructor.getExternalUniqueId());
	            q.setLong("sessionId", instructor.getDepartment().getSession().getUniqueId().longValue());
				q.setCacheable(true);
				committed = q.list();
			} else {
				Query q = hibSession.createQuery("select distinct a from Assignment as a inner join a.clazz.classInstructors as i where a.solution.uniqueId in (" + solutionIdsStr + ") and i.instructor.uniqueId = :instructorId" + check);
				q.setLong("instructorId", instructor.getUniqueId());
				q.setCacheable(true);
				assignments = q.list();
				q = hibSession.createQuery("select distinct a from ClassInstructor i inner join i.classInstructing.assignments as a "+
						"where i.instructor.uniqueId = :instructorId and a.solution.commited = true and a.solution.owner.session.uniqueId = :sessionId and a.solution.owner.uniqueId not in (" + ownerIds + ")" + check);
				q.setLong("instructorId",instructor.getUniqueId());
	            q.setLong("sessionId", instructor.getDepartment().getSession().getUniqueId().longValue());
				q.setCacheable(true);
				committed = q.list();
			}
		} else {
			if (instructor.getExternalUniqueId()!=null && instructor.getExternalUniqueId().length()>0) {
				Query q = hibSession.createQuery("select distinct a from Assignment as a inner join a.instructors as i where a.solution.uniqueId in ("+solutionIdsStr+") and i.externalUniqueId=:puid");
				q.setString("puid", instructor.getExternalUniqueId());
				q.setCacheable(true);
				assignments = q.list();
				q = hibSession.createQuery("select distinct a from DepartmentalInstructor i inner join i.assignments as a "+
						"where i.externalUniqueId=:puid and a.solution.commited=true and a.solution.owner.session.uniqueId=:sessionId and a.solution.owner.uniqueId not in ("+ownerIds+")");
				q.setString("puid",instructor.getExternalUniqueId());
	            q.setLong("sessionId", instructor.getDepartment().getSession().getUniqueId().longValue());
				q.setCacheable(true);
				committed = q.list();
			} else {
				Query q = hibSession.createQuery("select distinct a from Assignment as a inner join a.instructors as i where a.solution.uniqueId in ("+solutionIdsStr+") and i.uniqueId=:resourceId");
				q.setLong("resourceId", instructor.getUniqueId());
				q.setCacheable(true);
				assignments = q.list();
				q = hibSession.createQuery("select distinct a from DepartmentalInstructor i inner join i.assignments as a "+
						"where i.uniqueId=:instructorId and a.solution.commited=true and a.solution.owner.session.uniqueId=:sessionId and a.solution.owner.uniqueId not in ("+ownerIds+")");
				q.setLong("instructorId",instructor.getUniqueId());
	            q.setLong("sessionId", instructor.getDepartment().getSession().getUniqueId().longValue());
				q.setCacheable(true);
				committed = q.list();
			}
		}
		
		createCells(model, assignments, hibSession, context, false);
		createCells(model, committed, hibSession, context, true);
		model.setUtilization(countUtilization(new Combine<Assignment>(assignments, committed), context));

		if (context.isShowEvents() && RoomAvailability.getInstance() != null) {
			createMeetingCells(model, instructor.getDepartment().getSession(), context,
					RoomAvailability.getInstance().getInstructorAvailability(instructor.getUniqueId(), context.getSessionStartDate(), context.getSessionEndDate(), RoomAvailabilityInterface.sClassType),
					null);
		}
        if (instructor.getPositionType()!=null)
            model.setType(new Long(instructor.getPositionType().getSortOrder()));
    	
    	return model;
    }
    
    public static TimetableGridModel createModel(String solutionIdsStr, Department department, org.hibernate.Session hibSession, TimetableGridContext context) {
    	TimetableGridModel model = new TimetableGridModel(ResourceType.DEPARTMENT.ordinal(), department.getUniqueId());
    	model.setName(department.getShortLabel());
    	model.setFirstDay(context.getFirstDay());
    	model.setFirstSessionDay(context.getFirstSessionDay());
    	model.setFirstDate(context.getFirstDate());

    	Query q = hibSession.createQuery("select distinct a from Assignment as a inner join a.clazz.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings as o inner join o.subjectArea.department as d where " +
				"a.solution.uniqueId in ("+solutionIdsStr+") and d.uniqueId=:resourceId and " +
				"o.isControl=true");
		q.setCacheable(true);
		q.setLong("resourceId", department.getUniqueId());
		List assignments = q.list();
		createCells(model, assignments, hibSession, context, false);

		model.setSize(assignments.size());
		model.setUtilization(countUtilization(assignments, context));
		
		return model;
    }
    
    public static TimetableGridModel createModel(String solutionIdsStr, SubjectArea sa, org.hibernate.Session hibSession, TimetableGridContext context) {
    	TimetableGridModel model = new TimetableGridModel(ResourceType.SUBJECT_AREA.ordinal(), sa.getUniqueId());
    	model.setName(sa.getSubjectAreaAbbreviation());
    	model.setFirstDay(context.getFirstDay());
    	model.setFirstSessionDay(context.getFirstSessionDay());
    	model.setFirstDate(context.getFirstDate());

    	Query q = hibSession.createQuery("select distinct a from Assignment as a inner join a.clazz.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings as o inner join o.subjectArea as sa where " +
				"a.solution.uniqueId in ("+solutionIdsStr+") and sa.uniqueId=:resourceId and " +
				"o.isControl=true");
		q.setCacheable(true);
		q.setLong("resourceId", sa.getUniqueId());
		List assignments = q.list();
		
		createCells(model, assignments, hibSession, context, false);
		model.setSize(assignments.size());
		model.setUtilization(countUtilization(assignments, context));
		
		return model;
    }
    
    public static TimetableGridModel createModel(String solutionIdsStr, CurriculumClassification cc, org.hibernate.Session hibSession, TimetableGridContext context) {
    	TimetableGridModel model = new TimetableGridModel(ResourceType.CURRICULUM.ordinal(), cc.getUniqueId());
    	model.setName(cc.getCurriculum().getAbbv() + " " + cc.getName());
    	model.setFirstDay(context.getFirstDay());
    	model.setFirstSessionDay(context.getFirstSessionDay());
    	model.setFirstDate(context.getFirstDate());

		Query q = hibSession.createQuery("select distinct a from CurriculumClassification cc inner join cc.courses cx, Assignment a inner join a.clazz.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings co where " +
				"a.solution.uniqueId in ("+solutionIdsStr+") and cc.uniqueId=:resourceId and " +
				"cx.course = co");
		q.setCacheable(true);
		q.setLong("resourceId", cc.getUniqueId());
		List assignments = q.list();
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
			for (Iterator i = assignments.iterator(); i.hasNext(); ) {
				Assignment asgn = (Assignment)i.next();
				Set<Long>[] r = (restrictions == null ? null : restrictions.get(asgn.getClazz().getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().getUniqueId()));
	    		if (r != null && EventLookupBackend.hide(r, asgn.getClazz())) i.remove();
			}
		
		createCells(model, assignments, hibSession, context, false);
		model.setSize(assignments.size());
		model.setUtilization(countUtilization(assignments, context));
		
		return model;
    }
    
    public static TimetableGridModel createModel(String solutionIdsStr, StudentGroup g, org.hibernate.Session hibSession, TimetableGridContext context) {
    	TimetableGridModel model = new TimetableGridModel(ResourceType.STUDENT_GROUP.ordinal(), g.getUniqueId());
    	model.setName(g.getGroupAbbreviation());
    	model.setFirstDay(context.getFirstDay());
    	model.setFirstSessionDay(context.getFirstSessionDay());
    	model.setFirstDate(context.getFirstDate());

    	Query q = hibSession.createQuery(
				"select distinct a from StudentGroupReservation r, Assignment a inner join a.clazz.schedulingSubpart.instrOfferingConfig.instructionalOffering as io where "+
				"a.solution.uniqueId in ("+solutionIdsStr+") and io = r.instructionalOffering and r.group.uniqueId=:resourceId");
		q.setCacheable(true);
		q.setLong("resourceId", g.getUniqueId());
		List assignments = q.list();
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
			for (Iterator i = assignments.iterator(); i.hasNext(); ) {
				Assignment asgn = (Assignment)i.next();
				Set<Long>[] r = (restrictions == null ? null : restrictions.get(asgn.getClazz().getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().getUniqueId()));
	    		if (r != null && EventLookupBackend.hide(r, asgn.getClazz())) i.remove();
			}
		
		createCells(model, assignments, hibSession, context, false);
		model.setSize(assignments.size());
		model.setUtilization(countUtilization(assignments, context));
		
		return model;
    }
    
    public static TimetableGridModel createModel(String solutionIdsStr, StudentGroupInfo g, org.hibernate.Session hibSession, TimetableGridContext context) {
    	TimetableGridModel model = new TimetableGridModel(ResourceType.STUDENT_GROUP.ordinal(), g.getGroupId());
    	model.setName(g.getGroupName());
    	model.setFirstDay(context.getFirstDay());
    	model.setFirstSessionDay(context.getFirstSessionDay());
    	model.setFirstDate(context.getFirstDate());

		List<Long> classIds = new ArrayList<Long>();
		for (StudentGroupInfo.ClassInfo clazz: g.getGroupAssignments())
			classIds.add(clazz.getClassId());
		if (classIds.isEmpty()) return null;
		
		Query q = hibSession.createQuery(
				"select distinct a from Assignment a where a.solution.uniqueId in ("+solutionIdsStr+") and a.classId in (:classIds)");
		q.setParameterList("classIds", classIds, new LongType());
		q.setCacheable(true);
		List assignments = q.list();
		model.setSize((int)Math.round(g.countStudentWeights()));
		
		for (Iterator i = assignments.iterator(); i.hasNext(); ) {
			Assignment assignment = (Assignment)i.next();
			List<TimetableGridCell> cells = createCells(model, assignment, hibSession, context, false); 
			StudentGroupInfo.ClassInfo ci = g.getGroupAssignment(assignment.getClassId());
			if (ci != null) {
				int total = g.countStudentsOfOffering(assignment.getClazz().getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().getUniqueId());
				for (TimetableGridCell cell: cells) {
					cell.setGroup("(" + Math.round(ci.countStudentsWeight()) + ")");
					if (ci.getStudents() != null && !ci.getStudents().isEmpty() && total > 1) {
						int assigned = ci.getStudents().size();
						int minLimit = assignment.getClazz().getExpectedCapacity();
	                	int maxLimit = assignment.getClazz().getMaxExpectedCapacity();
	                	int limit = maxLimit;
	                	if (minLimit < maxLimit) {
	                		int roomLimit = (int) Math.floor(assignment.getPlacement().getRoomSize() / (assignment.getClazz().getRoomRatio() == null ? 1.0f : assignment.getClazz().getRoomRatio()));
	                		// int roomLimit = Math.round((c.getRoomRatio() == null ? 1.0f : c.getRoomRatio()) * p.getRoomSize());
	                		limit = Math.min(Math.max(minLimit, roomLimit), maxLimit);
	                	}
	                    if (assignment.getClazz().getSchedulingSubpart().getInstrOfferingConfig().isUnlimitedEnrollment() || limit >= 9999) limit = Integer.MAX_VALUE;
						int p = 100 * assigned / Math.min(limit, total);
						cell.setBackground(percentage2color(p));
						cell.setPreference(assigned + " of " + total);
					}
				}
			}
		}
		model.setUtilization(g.getGroupValue());
		
		return model;
    }

}
