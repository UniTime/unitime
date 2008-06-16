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
package org.unitime.timetable.webutil.timegrid;

import java.util.BitSet;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import net.sf.cpsolver.coursett.preference.PreferenceCombination;

import org.hibernate.Query;
import org.unitime.commons.Debug;
import org.unitime.commons.web.Web;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.ExactTimeMins;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.Room;
import org.unitime.timetable.model.RoomSharingModel;
import org.unitime.timetable.model.Solution;
import org.unitime.timetable.model.TimePattern;
import org.unitime.timetable.model.dao.SolutionDAO;
import org.unitime.timetable.solver.ui.AssignmentPreferenceInfo;
import org.unitime.timetable.solver.ui.GroupConstraintInfo;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.DateUtils;


/**
 * @author Tomas Muller
 */
public class SolutionGridModel extends TimetableGridModel {
	private transient Long iRoomId = null;
    private transient int iStartDay = 0;
    private transient int iEndDay = 0;
    
	public SolutionGridModel(String solutionIdsStr, Location room, org.hibernate.Session hibSession, int firstDay, int bgMode) {
		super(sResourceTypeRoom, room.getUniqueId().intValue());
		setName(room.getLabel());
		setSize(room.getCapacity().intValue());
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
		iStartDay = DateUtils.getDayOfYear(firstSolution.getSession().getSessionBeginDateTime());
		iEndDay = DateUtils.getDayOfYear(firstSolution.getSession().getSessionEndDateTime());
		Query q = hibSession.createQuery("select distinct a from Assignment as a inner join a.rooms as r where a.solution.uniqueId in ("+solutionIdsStr+") and r.uniqueId=:resourceId");
		q.setInteger("resourceId", room.getUniqueId().intValue());
		q.setCacheable(true);
		init(q.list(),hibSession,firstDay,bgMode);
		
		q = hibSession.createQuery("select distinct a from Room r inner join r.assignments as a "+
		"where r.uniqueId=:roomId and a.solution.commited=true and a.solution.owner.session.uniqueId=:sessionId and a.solution.owner.uniqueId not in ("+ownerIds+")");
		q.setInteger("roomId",room.getUniqueId().intValue());
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
				for (int j=0;j<Constants.SLOTS_PER_DAY/6;j++) {
					if (sharing.isFreeForAll(i,j)) continue;
					if (sharing.isNotAvailable(i,j)) {
						for (int x=0;x<6;x++)
							setAvailable(i,6*j+x,false);
						continue;
					}
					Long dept = sharing.getDepartmentId(i,j);
					if (dept!=null && !deptIds.contains(dept)) { 
						for (int x=0;x<6;x++)
							setAvailable(i,6*j+x,false);
					}
				}
		}
        setType(room instanceof Room ? ((Room)room).getRoomType().getUniqueId(): null);
	}
	
	public SolutionGridModel(String solutionIdsStr, DepartmentalInstructor instructor, org.hibernate.Session hibSession, int firstDay, int bgMode) {
		super(sResourceTypeInstructor, instructor.getUniqueId().intValue());
		setName(instructor.getLastName()+", "+instructor.getFirstName()+(instructor.getMiddleName()==null?"":" "+instructor.getMiddleName()));
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
		iStartDay = DateUtils.getDayOfYear(firstSolution.getSession().getSessionBeginDateTime());
		iEndDay = DateUtils.getDayOfYear(firstSolution.getSession().getSessionEndDateTime());
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
			q.setInteger("resourceId", instructor.getUniqueId().intValue());
			q.setCacheable(true);
			init(q.list(),hibSession,firstDay,bgMode);
			q = hibSession.createQuery("select distinct a from DepartmentalInstructor i inner join i.assignments as a "+
					"where i.uniqueId=:instructorId and a.solution.commited=true and a.solution.owner.session.uniqueId=:sessionId and a.solution.owner.uniqueId not in ("+ownerIds+")");
			q.setInteger("instructorId",instructor.getUniqueId().intValue());
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
        if (instructor.getPositionType()!=null)
            setType(new Long(instructor.getPositionType().getSortOrder()));
	}
	
	public SolutionGridModel(String solutionIdsStr, Department dept, org.hibernate.Session hibSession, int firstDay, int bgMode) {
		super(sResourceTypeInstructor, dept.getUniqueId().longValue());
		setName(dept.getShortLabel());
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
		iStartDay = DateUtils.getDayOfYear(firstSolution.getSession().getSessionBeginDateTime());
		iEndDay = DateUtils.getDayOfYear(firstSolution.getSession().getSessionEndDateTime());
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
			else
				cell = cell.copyCell(j,cell.getMeetingNumber()+1);
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
		String onClick = "window.open('suggestions.do?id="+assignment.getClassId()+"&op=Reset','suggestions','width=1000,height=600,resizable=yes,scrollbars=yes,toolbar=no,location=no,directories=no,status=yes,menubar=no,copyhistory=no').focus();";
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
					shortComment = "<span style='color:rgb(200,200,200)'>"+(assignmentInfo.getNrRoomLocations()==1?"<u>":"")+roomCap+" / "+minRoomSize+" / "+roomSize+(assignmentInfo.getNrRoomLocations()==1?"</u>":"")+"</span>";
					shortCommentNoColor = roomCap+" / "+minRoomSize+" / "+roomSize;
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
			
			title = "timePref:"+(int)assignmentInfo.getNormalizedTimePreference()+", "+
					"studConf:"+assignmentInfo.getNrStudentConflicts()+", "+
					"roomPref:"+roomPref+", "+
					"btbInstrPref:"+assignmentInfo.getBtbInstructorPreference()+", "+
					(assignmentInfo.getInitialAssignment()!=null?"initial:"+(assignmentInfo.getIsInitial()?"this one":assignmentInfo.getInitialAssignment())+", ":"")+
					(assignmentInfo.getInitialAssignment()!=null?"pert:"+Web.format(assignmentInfo.getPerturbationPenalty())+", ":"")+
					"noConfPlacements:"+assignmentInfo.getNrPlacementsNoConf()+", "+
					"deptBal:"+assignmentInfo.getDeptBalancPenalty();
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
				title = title+", distrPref:"+pref.getPreferenceProlog();
				background=TimetableGridCell.pref2color(pref.getPreferenceProlog());
			}
		}
		
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
				assignment.getDatePattern().getName(),
				assignment.getDatePattern().getPatternBitSet());
	}
}
