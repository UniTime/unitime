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
package org.unitime.timetable.test;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;


import org.cpsolver.ifs.util.ToolBox;
import org.hibernate.Transaction;
import org.unitime.commons.hibernate.util.HibernateUtil;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.ClassEvent;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.Event;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.Meeting;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Solution;
import org.unitime.timetable.model.SolverGroup;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.TimePattern;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.model.dao.SubjectAreaDAO;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.util.Constants;

/**
 * @author Tomas Muller
 */
public class MakeAssignmentsForClassEvents {
	protected static GwtConstants CONSTANTS = Localization.create(GwtConstants.class);

	private Session iSession = null;
    private Vector<Date> iWeekDate = null;
    private org.hibernate.Session iHibSession = null;
    private Hashtable<String,DatePattern> iDatePatterns = null;
    private TimePattern iExactTimePattern = null;
    private Hashtable<Long,Location> iLocations = null;
    
    public MakeAssignmentsForClassEvents(Session session, org.hibernate.Session hibSession) {
        iHibSession = hibSession;
        iSession = session;
        Calendar date = Calendar.getInstance(Locale.US);
        date.setLenient(true);
        date.setTime(iSession.getSessionBeginDateTime());
        iWeekDate = new Vector(); 
        while (date.getTime().compareTo(iSession.getSessionEndDateTime())<=0) {
            iWeekDate.add(date.getTime());
            int idx = 0;
            while (idx<7) {
                date.add(Calendar.DAY_OF_YEAR, 1);
                if (iSession.getHoliday(date.get(Calendar.DAY_OF_MONTH),date.get(Calendar.MONTH))!=Session.sHolidayTypeBreak) idx++;
            }
        }
        iWeekDate.add(date.getTime());
        iDatePatterns = new Hashtable();
        for (Iterator i=DatePattern.findAll(iSession, true, null, null).iterator();i.hasNext();) {
            DatePattern dp = (DatePattern)i.next();
            iDatePatterns.put(dp.getName(), dp);
        }
        iExactTimePattern = TimePattern.findExactTime(iSession.getUniqueId());
        iLocations = new Hashtable();
        for (Iterator i=Location.findAll(iSession.getUniqueId()).iterator();i.hasNext();) {
            Location location = (Location)i.next();
            if (location.getPermanentId()!=null)
                iLocations.put(location.getPermanentId(), location);
        }
    }
    
    protected int getWeek(Date date) {
        for (int idx=0;idx+1<iWeekDate.size();idx++)
            if (date.compareTo(iWeekDate.elementAt(idx))<0) return idx;
        return iWeekDate.size()-1;
    }
    
    protected TreeSet<Integer> getWeeks(Event event) {
        TreeSet<Integer> weeks = new TreeSet();
        for (Iterator i=event.getMeetings().iterator();i.hasNext();) {
            Meeting meeting = (Meeting)i.next();
            weeks.add(getWeek(meeting.getMeetingDate()));
        }
        return weeks;
    }
    
    protected DatePattern getDefaultDatePattern() {
        TreeSet<Integer> weeks = new TreeSet();
        for (int i=1;i+2<iWeekDate.size();i++) weeks.add(i);
        return getDatePattern(weeks);
    }
    
    protected DatePattern getDatePattern(TreeSet<Integer> weeks) {
        if (weeks.isEmpty()) return getDefaultDatePattern();
        String patternName = null;
        int firstWeek = -1, lastWeek = -1;
        for (Integer week : weeks) {
            if (lastWeek<0) { 
                firstWeek = week;
            } else if (lastWeek+1 != week) {
                patternName = (patternName==null?"":patternName+",") + (lastWeek==firstWeek?String.valueOf(lastWeek):firstWeek+"-"+lastWeek);
                firstWeek = week;
            }
            lastWeek = week;
        }
        patternName = "Week "+(patternName==null?"":patternName+",") + (lastWeek==firstWeek?String.valueOf(lastWeek):firstWeek+"-"+lastWeek);
        DatePattern dp = iDatePatterns.get(patternName);
        if (dp!=null) return dp;
        dp = new DatePattern();
        dp.setName(patternName);
        dp.setSession(iSession);
        dp.setType(DatePattern.sTypeStandard);
        dp.setVisible(true);
        Calendar date = Calendar.getInstance(Locale.US);
        int week = weeks.first();
        date.setTime(iWeekDate.get(week==0?0:week-1));
        if (week==0) date.add(Calendar.DAY_OF_YEAR, -7);
        dp.setPatternOffset(date.getTime());
        String pattern = "";
        while (week<=weeks.last()) {
            while (date.getTime().compareTo(iWeekDate.elementAt(week))<0) {
                pattern += (!weeks.contains(week)?"0":(iSession.getHoliday(date.get(Calendar.DAY_OF_MONTH),date.get(Calendar.MONTH))==Session.sHolidayTypeNone?"1":"0"));
                date.add(Calendar.DAY_OF_YEAR, 1);
            }
            week++;
        }
        dp.setPattern(pattern);
        iHibSession.save(dp);
        iDatePatterns.put(patternName, dp);
        return dp;
    }
    
    public int getStartSlot(Event event) {
        for (Iterator i=event.getMeetings().iterator();i.hasNext();) {
            Meeting meeting = (Meeting)i.next();
            return meeting.getStartPeriod();
        }
        return 0;
    }
    
    public int getDaysCode(Event event) {
        int daysCode = 0;
        for (Iterator i=event.getMeetings().iterator();i.hasNext();) {
            Meeting meeting = (Meeting)i.next();
            Calendar date = Calendar.getInstance(Locale.US);
            date.setTime(meeting.getMeetingDate());
            switch (date.get(Calendar.DAY_OF_WEEK)) {
            case Calendar.MONDAY : daysCode |= Constants.DAY_CODES[Constants.DAY_MON]; break;
            case Calendar.TUESDAY : daysCode |= Constants.DAY_CODES[Constants.DAY_TUE]; break;
            case Calendar.WEDNESDAY : daysCode |= Constants.DAY_CODES[Constants.DAY_WED]; break;
            case Calendar.THURSDAY : daysCode |= Constants.DAY_CODES[Constants.DAY_THU]; break;
            case Calendar.FRIDAY : daysCode |= Constants.DAY_CODES[Constants.DAY_FRI]; break;
            case Calendar.SATURDAY : daysCode |= Constants.DAY_CODES[Constants.DAY_SAT]; break;
            case Calendar.SUNDAY : daysCode |= Constants.DAY_CODES[Constants.DAY_SUN]; break;
            }
        }
        return daysCode;
    }
    
    protected SolverGroup getSolverGroup(Department department) {
        if (department.getSolverGroup()!=null) return department.getSolverGroup();
        SolverGroup sg = new SolverGroup();
        sg.setDepartments(new HashSet()); sg.getDepartments().add(department); department.setSolverGroup(sg);
        sg.setAbbv(department.getAbbreviation()==null?department.getDeptCode():department.getAbbreviation());
        sg.setName(department.getName());
        sg.setSession(iSession);
        sg.setTimetableManagers(new HashSet(department.getTimetableManagers()));
        sg.setSolutions(new HashSet());
        iHibSession.save(sg); iHibSession.update(department);
        return sg;
    }
    
    protected Solution getSolution(SolverGroup sg) {
        if (sg.getCommittedSolution()!=null) return sg.getCommittedSolution();
        Solution solution = new Solution();
        solution.setCommitDate(new Date());
        solution.setCommited(true);
        solution.setCreated(new Date());
        solution.setCreator("MakeAssignmentsForClassEvents");
        solution.setOwner(sg); sg.getSolutions().add(solution);
        solution.setValid(true);
        solution.setAssignments(new HashSet());
        iHibSession.save(solution); iHibSession.update(sg);
        return solution;
    }
    
    public Solution getSolution(Department dept) {
        return getSolution(getSolverGroup(dept));
    }
    
    public Set<Location> getRooms(Event event) {
        Set<Location> rooms = new HashSet();
        for (Iterator i=event.getMeetings().iterator();i.hasNext();) {
            Meeting meeting = (Meeting)i.next();
            if (meeting.getLocationPermanentId()==null) continue;
            Location location = iLocations.get(meeting.getLocationPermanentId());
            if (location!=null) rooms.add(location);
        }
        return rooms;
    }
    
    public TimePattern getTimePattern(Event event) {
        if (iExactTimePattern!=null) return iExactTimePattern;
        iExactTimePattern = new TimePattern();
        iExactTimePattern.setName("Exact Time");
        iExactTimePattern.setBreakTime(0);
        iExactTimePattern.setMinPerMtg(0);
        iExactTimePattern.setNrMeetings(0);
        iExactTimePattern.setSession(iSession);
        iExactTimePattern.setSlotsPerMtg(0);
        iExactTimePattern.setType(TimePattern.sTypeExactTime);
        iExactTimePattern.setVisible(true);
        iHibSession.save(iExactTimePattern);
        return iExactTimePattern;
    }
    
    public Assignment createAssignment(ClassEvent event) {
        if (event==null || event.getClazz()==null || event.getMeetings().isEmpty()) return null;
        Class_ clazz = event.getClazz();
        Assignment assignment = clazz.getCommittedAssignment();
        if (assignment==null) {
            assignment = new Assignment();
            assignment.setClazz(clazz);
            Department dept = clazz.getManagingDept();
            if (dept==null) dept = clazz.getSchedulingSubpart().getControllingDept();
            assignment.setSolution(getSolution(dept));
            assignment.setClassName(clazz.getClassLabel(ApplicationProperty.SolverShowClassSufix.isTrue()));
            assignment.setClassId(clazz.getUniqueId());
            clazz.setCommittedAssignment(assignment);
        }
        assignment.setDays(getDaysCode(event));
        assignment.setStartSlot(getStartSlot(event));
        assignment.setRooms(getRooms(event));
        assignment.setTimePattern(getTimePattern(event));
        iHibSession.saveOrUpdate(clazz);
        iHibSession.saveOrUpdate(assignment);
        return assignment;
    }

    public DatePattern getDatePattern(Event event) {
        return getDatePattern(getWeeks(event));
    }
    
    public static void main(String args[]) {
        try {
            ToolBox.configureLogging();
            HibernateUtil.configureHibernate(new Properties());
            
            org.hibernate.Session hibSession = new _RootDAO().getSession();
            List subjects = new SubjectAreaDAO().findAll();
            
            boolean excludeCommittedAssignments = !"true".equals(System.getProperty("redo"));
            
            for (Iterator i=subjects.iterator();i.hasNext();) {
                SubjectArea sa = (SubjectArea)i.next();
                
                System.out.println("Procession subject area "+sa.getSubjectAreaAbbreviation()+" for "+sa.getSession().getLabel());
                
                Transaction tx = null;
                try {
                    tx = hibSession.beginTransaction();
                    
                    MakeAssignmentsForClassEvents m = new MakeAssignmentsForClassEvents(new SessionDAO().get(sa.getSession().getUniqueId()), hibSession);
                    
                    for (Iterator j=hibSession.createQuery(
                            "select e from ClassEvent e inner join e.clazz c " +
                            "inner join c.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings co " +
                            "where co.isControl=true and co.subjectArea.uniqueId=:subjectId "+
                            (excludeCommittedAssignments?" and c.committedAssignment is null":""))
                            .setLong("subjectId", sa.getUniqueId())
                            .iterate();j.hasNext();) {
                        ClassEvent e = (ClassEvent)j.next();
                        Assignment a = m.createAssignment(e);
                        e.getClazz().setDatePattern(m.getDatePattern(e));
                        System.out.println("  "+e.getEventName()+" -- "+(a==null?"Not Assigned":a.getPlacement().getLongName(CONSTANTS.useAmPm())));
                    }

                    tx.commit();
                } catch (Exception e) {
                    if (tx!=null) tx.rollback();
                    throw e;
                }
                
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
