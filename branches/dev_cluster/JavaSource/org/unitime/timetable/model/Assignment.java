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
package org.unitime.timetable.model;

import java.util.BitSet;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.Vector;

import net.sf.cpsolver.coursett.model.Lecture;
import net.sf.cpsolver.coursett.model.Placement;
import net.sf.cpsolver.coursett.model.RoomLocation;
import net.sf.cpsolver.coursett.model.TimeLocation;

import org.hibernate.Hibernate;
import org.hibernate.LazyInitializationException;
import org.hibernate.Query;
import org.hibernate.criterion.Restrictions;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.model.base.BaseAssignment;
import org.unitime.timetable.model.dao.AssignmentDAO;
import org.unitime.timetable.model.dao.AssignmentInfoDAO;
import org.unitime.timetable.model.dao.ConstraintInfoDAO;
import org.unitime.timetable.solver.ui.TimetableInfo;
import org.unitime.timetable.util.Constants;




public class Assignment extends BaseAssignment {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public Assignment () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public Assignment (java.lang.Long uniqueId) {
		super(uniqueId);
	}

/*[CONSTRUCTOR MARKER END]*/
	
	public int[] getStartSlots() {
		int ret[] = new int[getTimePattern().getNrMeetings().intValue()];
		int idx=0;
		for (int i=0;i<Constants.DAY_CODES.length;i++)
			if ((getDays().intValue()&Constants.DAY_CODES[i])!=0)
				ret[idx++]=getStartSlot().intValue()+i*Constants.SLOTS_PER_DAY;
		return ret;
	}

	public Vector getStartSlotsVect() {
		Vector ret = new Vector();
		for (int i=0;i<Constants.DAY_CODES.length;i++)
			if ((getDays().intValue()&Constants.DAY_CODES[i])!=0)
				ret.addElement(new Integer(getStartSlot().intValue()+i*Constants.SLOTS_PER_DAY));
		return ret;
	}
	
	public transient Hashtable iAssignmentInfoCache = new Hashtable();

	public TimetableInfo getAssignmentInfo(String name) throws Exception {
		TimetableInfo tInfo = (TimetableInfo)iAssignmentInfoCache.get(name);
		if (tInfo==null) {
			try {
				for (Iterator i=getAssignmentInfo().iterator();i.hasNext();) {
					AssignmentInfo info = (AssignmentInfo)i.next();
					if (info.getDefinition()!=null && info.getDefinition().getName().equals(name)) {
						tInfo = info.getInfo();
						break;
					}
				}
			} catch (LazyInitializationException e) {
				org.hibernate.Session session = (new AssignmentInfoDAO()).getSession();
				SolverInfoDef def = SolverInfoDef.findByName(session,name);
				if (def==null) return null;
				AssignmentInfo info = (AssignmentInfo)session.createCriteria(AssignmentInfo.class).add(Restrictions.eq("definition",def)).add(Restrictions.eq("assignment",this)).uniqueResult();
				if (info==null) return null;
				tInfo = info.getInfo();
			}
			if (tInfo!=null) iAssignmentInfoCache.put(name, tInfo);
		}
		return tInfo;
	}
	
	public void cleastAssignmentInfoCache() { iAssignmentInfoCache.clear(); }
	
	public transient Hashtable iConstraintInfoCache = new Hashtable();

	public TimetableInfo getConstraintInfo(String name) throws Exception {
		Vector infos = getConstraintInfos(name);
		if (infos==null || infos.isEmpty()) return null;
		return (TimetableInfo)infos.firstElement();
	}

	public Vector getConstraintInfos(String name) throws Exception {
		Vector tInfos = (Vector)iConstraintInfoCache.get(name);
		if (tInfos==null) {
			try {
				tInfos = new Vector();
				for (Iterator i=getConstraintInfo().iterator();i.hasNext();) {
					ConstraintInfo info = (ConstraintInfo)i.next();
					if (info.getDefinition()!=null && info.getDefinition().getName().equals(name)) {
						TimetableInfo tInfo = info.getInfo();
						if (tInfo!=null)
							tInfos.add(tInfo);
					}
				}
			} catch (LazyInitializationException e) {
				org.hibernate.Session session = (new ConstraintInfoDAO()).getSession();
				Query q = session.createQuery("select distinct c from ConstraintInfo as c inner join c.assignments as a where " +
						"c.definition.name=:name and a.uniqueId=:assignmentId");
				q.setLong("assignmentId",getUniqueId());
				q.setString("name",name);
				tInfos = new Vector();
				for (Iterator i=q.list().iterator();i.hasNext();) {
					ConstraintInfo info = (ConstraintInfo)i.next();
					TimetableInfo tInfo = info.getInfo();
					if (tInfo!=null)
						tInfos.add(tInfo);
				}
			}
			if (tInfos!=null) iConstraintInfoCache.put(name, tInfos);
		}
		return tInfos;
	}
	
	public Hashtable getConstraintInfoTable(String name) throws Exception {
		Hashtable ret = new Hashtable();
		try {
			for (Iterator i=getConstraintInfo().iterator();i.hasNext();) {
				ConstraintInfo info = (ConstraintInfo)i.next();
				if (info.getDefinition()!=null && info.getDefinition().getName().equals(name)) {
					TimetableInfo tInfo = info.getInfo();
					if (tInfo!=null)
						ret.put(info, tInfo);
				}
			}
		} catch (LazyInitializationException e) {
			org.hibernate.Session session = (new ConstraintInfoDAO()).getSession();
			Query q = session.createQuery("select distinct c from ConstraintInfo as c inner join c.assignments as a where " +
					"c.definition.name=:name and a.uniqueId=:assignmentId");
			q.setLong("assignmentId",getUniqueId());
			q.setString("name",name);
			for (Iterator i=q.list().iterator();i.hasNext();) {
				ConstraintInfo info = (ConstraintInfo)i.next();
				TimetableInfo tInfo = info.getInfo();
				if (tInfo!=null)
					ret.put(info, tInfo);
			}
		}
		return ret;
	}
	
	private int iSlotsPerMtg = -1;
	public void setSlotsPerMtg(int slotsPerMtg) {
		iSlotsPerMtg = slotsPerMtg;
	}
	public int getSlotPerMtg() {
		if (iSlotsPerMtg>=0) return iSlotsPerMtg;
		TimePattern pattern = getTimePattern();
		iSlotsPerMtg = pattern.getSlotsPerMtg().intValue();
		if (pattern.getType().intValue()==TimePattern.sTypeExactTime)
			iSlotsPerMtg = ExactTimeMins.getNrSlotsPerMtg(getDays().intValue(),getClazz().getSchedulingSubpart().getMinutesPerWk().intValue()); 
		return iSlotsPerMtg;
	}
	
	private int iBreakTime = -1;
	public void setBreakTime(int breakTime) {
		iBreakTime = breakTime;
	}
	public int getBreakTime() {
		if (iBreakTime>=0) return iBreakTime;
		TimePattern pattern = getTimePattern();
		iBreakTime = pattern.getBreakTime().intValue();
		if (pattern.getType().intValue()==TimePattern.sTypeExactTime)
			iBreakTime = ExactTimeMins.getBreakTime(getDays().intValue(),getClazz().getSchedulingSubpart().getMinutesPerWk().intValue()); 
		return iBreakTime;
	}

	private transient TimeLocation iTimeLocation = null;
	public TimeLocation getTimeLocation() {
		if (iPlacement!=null) return iPlacement.getTimeLocation();
		if (iTimeLocation==null) {
			DatePattern datePattern = getDatePattern();
			iTimeLocation = new TimeLocation(
				getDays().intValue(),
				getStartSlot().intValue(),
				getSlotPerMtg(),
				0,0,
				(datePattern == null ? null : datePattern.getUniqueId()),
				(datePattern == null ? "?" : datePattern.getName()),
				(datePattern == null ? new BitSet() : datePattern.getPatternBitSet()),
				getBreakTime()
				);
			iTimeLocation.setTimePatternId(getTimePattern().getUniqueId());
		}
		return iTimeLocation;
	}
	public Vector getRoomLocations() {
		Vector ret = new Vector();
		for (Iterator i=getRooms().iterator();i.hasNext();) {
			Location room = (Location)i.next();
			RoomLocation roomLocation = new RoomLocation(
					room.getUniqueId(),
					room.getLabel(),
					(room instanceof Room? ((Room)room).getBuilding().getUniqueId() : null),
					0,
					room.getCapacity().intValue(),
					room.getCoordinateX(),
					room.getCoordinateY(),
					room.isIgnoreTooFar().booleanValue(),
					null);
			ret.addElement(roomLocation);
		}
		return ret;
	}
	
	private transient Placement iPlacement = null;
	public Placement getPlacement() {
		if (iPlacement!=null) return iPlacement;
		TimeLocation timeLocation = getTimeLocation();
		Vector timeLocations = new Vector(1); timeLocations.addElement(timeLocation);
		Vector roomLocations = getRoomLocations();
    	Lecture lecture = new Lecture(getClassId(), (getSolution()==null || getSolution().getOwner()==null?null:getSolution().getOwner().getUniqueId()), (getClazz()==null?null:getClazz().getSchedulingSubpart().getUniqueId()), getClassName(), timeLocations, roomLocations, roomLocations.size(), new Placement(null,timeLocation,roomLocations),
    			(getClazz() == null ? 0 : getClazz().getExpectedCapacity()), (getClazz() == null ? 0 : getClazz().getMaxExpectedCapacity()), (getClazz() == null ? 1.0f : getClazz().getRoomRatio()));
		if (getClazz()!=null)
			lecture.setNote(getClazz().getNotes());
		iPlacement = (Placement)lecture.getInitialAssignment();
		iPlacement.setVariable(lecture);
		iPlacement.setAssignmentId(getUniqueId());
		lecture.setBestAssignment(iPlacement);
		if (getSolution()!=null && getSolution().isCommited()!=null)
			lecture.setCommitted(getSolution().isCommited().booleanValue());
        iPlacement.setAssignment(this);
		return iPlacement;
	}
	
	public String toString() {
		return getClassName()+" "+getPlacement().getName();
	}
	
	public DatePattern getDatePattern() {
		DatePattern dp = super.getDatePattern();
		if (dp != null && !Hibernate.isInitialized(dp.getSession()))
			return (DatePattern)AssignmentDAO.getInstance().getSession().merge(dp);
		if (dp == null && getClazz() != null)
			dp = getClazz().effectiveDatePattern();
		return dp;
	}
	
	public String getClassName() {
		if (super.getClassName()!=null) return super.getClassName();
		return getClazz().getClassLabel();
	}
	
	public Set<Location> getRooms() {
		try {
			return super.getRooms();
		} catch (LazyInitializationException e) {
			(new AssignmentDAO()).getSession().merge(this);
			return super.getRooms();
		}
	}
	
	public static double getDistance(Assignment a1, Assignment a2) {
		double dist = 0.0;
		for (Iterator i1=a1.getRooms().iterator();i1.hasNext();) {
			Location r1 = (Location)i1.next();
			for (Iterator i2=a2.getRooms().iterator();i2.hasNext();) {
				Location r2 = (Location)i2.next();
				dist = Math.max(dist,r1.getDistance(r2));
			}
		}
		return dist;
	}
	
	public boolean isInConflict(Assignment other) {
		return isInConflict(this, other, true);
	}
	
	public static boolean isInConflict(Assignment a1, Assignment a2, boolean useDistances) {
		if (a1==null || a2==null) return false;
	       TimeLocation t1=a1.getTimeLocation(), t2=a2.getTimeLocation();
	       if (!t1.shareDays(t2)) return false;
	       if (!t1.shareWeeks(t2)) return false;
	       if (t1.shareHours(t2)) return true;
	       if (!useDistances) return false;
	       int s1 = t1.getStartSlot(), s2 = t2.getStartSlot();
	       if (s1+t1.getNrSlotsPerMeeting()!=s2 &&
	           s2+t2.getNrSlotsPerMeeting()!=s1) return false;
	       double distance = getDistance(a1,a2);
	       if (distance <= a1.getSolution().getProperties().getPropertyDouble("Student.DistanceLimit",67.0)) return false;
	       if (distance <= a1.getSolution().getProperties().getPropertyDouble("Student.DistanceLimit75min",100.0) && (
	           (t1.getLength()==18 && s1+t1.getLength()==s2) ||
	           (t2.getLength()==18 && s2+t2.getLength()==s1)))
	           return false;
	       return true;
   }
	
    public ClassEvent generateCommittedEvent(ClassEvent event, boolean createNoRoomMeetings) {
    	Class_ clazz = getClazz();
        if (event==null) {
            event = new ClassEvent();
            event.setClazz(clazz); clazz.setEvent(event);
            if (getClazz().getSession().getStatusType().isTestSession()) return null;
        }
        event.setEventName(getClassName());
        event.setMinCapacity(clazz.getClassLimit());
        event.setMaxCapacity(clazz.getClassLimit());
        
        boolean changePast = "true".equals(ApplicationProperties.getProperty("tmtbl.classAssign.changePastMeetings", "true"));
		Calendar cal = Calendar.getInstance(Locale.US);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		Date today = cal.getTime();
        
		if (event.getMeetings() != null) {
			if (changePast) {
				event.getMeetings().clear();
			} else {
		    	for (Iterator<Meeting> i = event.getMeetings().iterator(); i.hasNext(); )
		    		if (!i.next().getMeetingDate().before(today)) i.remove();
			}
		} else {
			event.setMeetings(new HashSet());
        }
		
        DatePattern dp = getDatePattern();
        cal.setTime(dp.getStartDate()); cal.setLenient(true);
        TimeLocation time = getTimeLocation(); 
        EventDateMapping.Class2EventDateMap class2eventDates = EventDateMapping.getMapping(clazz.getSessionId());
        for (int idx=0;idx<dp.getPattern().length();idx++) {
            if (dp.getPattern().charAt(idx)=='1') {
                boolean offered = false;
                switch (cal.get(Calendar.DAY_OF_WEEK)) {
                    case Calendar.MONDAY : offered = ((time.getDayCode() & Constants.DAY_CODES[Constants.DAY_MON]) != 0); break;
                    case Calendar.TUESDAY : offered = ((time.getDayCode() & Constants.DAY_CODES[Constants.DAY_TUE]) != 0); break;
                    case Calendar.WEDNESDAY : offered = ((time.getDayCode() & Constants.DAY_CODES[Constants.DAY_WED]) != 0); break;
                    case Calendar.THURSDAY : offered = ((time.getDayCode() & Constants.DAY_CODES[Constants.DAY_THU]) != 0); break;
                    case Calendar.FRIDAY : offered = ((time.getDayCode() & Constants.DAY_CODES[Constants.DAY_FRI]) != 0); break;
                    case Calendar.SATURDAY : offered = ((time.getDayCode() & Constants.DAY_CODES[Constants.DAY_SAT]) != 0); break;
                    case Calendar.SUNDAY : offered = ((time.getDayCode() & Constants.DAY_CODES[Constants.DAY_SUN]) != 0); break;
                }
                Date meetingDate = class2eventDates.getEventDate(cal.getTime());
                if (offered && (changePast || !meetingDate.before(today))) {
                    boolean created = false;
                    for (Iterator i=getRooms().iterator();i.hasNext();) {
                        Location location = (Location)i.next();
                        if (location.getPermanentId()!=null) {
                            Meeting m = new Meeting();
                            m.setMeetingDate(meetingDate);
                            m.setStartPeriod(time.getStartSlot());
                            m.setStartOffset(0);
                            m.setStopPeriod(time.getStartSlot()+time.getLength());
                            m.setStopOffset(-time.getBreakTime());
                            m.setClassCanOverride(false);
                            m.setLocationPermanentId(location.getPermanentId());
                            m.setStatus(Meeting.Status.APPROVED);
                            m.setApprovalDate(getSolution().getCommitDate());
                            m.setEvent(event);
                            event.getMeetings().add(m);
                            created = true;
                        }
                    }
                    if (!created && createNoRoomMeetings) {
                        Meeting m = new Meeting();
                        m.setMeetingDate(meetingDate);
                        m.setStartPeriod(time.getStartSlot());
                        m.setStartOffset(0);
                        m.setStopPeriod(time.getStartSlot()+time.getLength());
                        m.setStopOffset(-time.getBreakTime());
                        m.setClassCanOverride(false);
                        m.setLocationPermanentId(null);
                        m.setStatus(Meeting.Status.APPROVED);
                        m.setApprovalDate(getSolution().getCommitDate());
                        m.setEvent(event);
                        event.getMeetings().add(m);
                    }
                }
            }
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }
        return event;
    }
}
