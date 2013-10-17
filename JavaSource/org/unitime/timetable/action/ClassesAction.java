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
package org.unitime.timetable.action;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.cpsolver.coursett.model.TimeLocation;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.unitime.commons.MultiComparable;
import org.unitime.commons.web.WebTable;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.form.ClassesForm;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.Meeting;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.dao.Class_DAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.UserAuthority;
import org.unitime.timetable.security.UserContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.Formats;
import org.unitime.timetable.util.LoginManager;
import org.unitime.timetable.webutil.PdfWebTable;

/** 
 * @author Tomas Muller, Stephanie Schluttenhofer
 */
@Service("/classes")
public class ClassesAction extends Action {
	
	@Autowired AuthenticationManager authenticationManager;
	
	@Autowired SessionContext sessionContext;
	
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
	    ClassesForm myForm = (ClassesForm)form;

        String op = (myForm.getOp()!=null?myForm.getOp():request.getParameter("op"));
        
        if (request.getParameter("select")!=null) {
            myForm.load(request.getSession());
            if (request.getParameter("subject")!=null) {
                myForm.setSubjectArea(request.getParameter("subject"));
            } else {
                if (myForm.canDisplayAllSubjectsAtOnce()){
            		myForm.setSubjectArea("--ALL--");
            	}
            }
            if (request.getParameter("year")!=null && request.getParameter("term")!=null && request.getParameter("campus")!=null) {
                Session session = Session.getSessionUsingInitiativeYearTerm(
                        request.getParameter("campus"), 
                        request.getParameter("year"), 
                        request.getParameter("term"));
                if (session!=null) myForm.setSession(session.getUniqueId());
            } 
            if (request.getParameter("course")!=null) {
                myForm.setCourseNumber(request.getParameter("course"));
            } else {
                myForm.setCourseNumber(null);
            }
            op = "Apply";
        }

        if ("Apply".equals(op)) {
            myForm.save(request.getSession());
            if (myForm.getUsername()!=null && myForm.getUsername().length()>0 && myForm.getPassword()!=null && myForm.getPassword().length()>0) {
            	try {
            		Authentication authRequest = new UsernamePasswordAuthenticationToken(myForm.getUsername(), myForm.getPassword());
            		Authentication authResult = authenticationManager.authenticate(authRequest);
            		SecurityContextHolder.getContext().setAuthentication(authResult);
            		UserContext user = (UserContext)authResult.getPrincipal();
            		if (user.getCurrentAuthority() == null || !user.getCurrentAuthority().hasRight(Right.PersonalSchedule))
            			for (UserAuthority auth: user.getAuthorities()) {
            				if (auth.getAcademicSession() != null && auth.getAcademicSession().getQualifierId().equals(myForm.getSession()) && auth.hasRight(Right.PersonalSchedule)) {
            					user.setCurrentAuthority(auth); break;
            				}
            			}
            		request.getSession().setAttribute("loginPage", "classes");
            		LoginManager.loginSuceeded(authResult.getName());
            		if (user.getCurrentAuthority() == null) {
            			response.sendRedirect("selectPrimaryRole.do");
            			return null;
            		}
            		return mapping.findForward("personal");
            	} catch (Exception e) {
            		myForm.setMessage("Authentication failed: " + e.getMessage());
            		LoginManager.addFailedLoginAttempt(myForm.getUsername(), new Date());
            	}
            }
        }
        myForm.load(request.getSession());
        
        WebTable.setOrder(sessionContext,"classes.order",request.getParameter("ord"),1);
        
        if (myForm.getSession()!=null && myForm.getSubjectArea()!=null && myForm.getSubjectArea().length()>0) {
            org.unitime.timetable.model.Session session = new SessionDAO().get(myForm.getSession());
            if (session.getStatusType().canNoRoleReportClass()) {
                List classes = null;
                SubjectArea sa = null;
                if ("--ALL--".equals(myForm.getSubjectArea())) 
                    classes = Class_.findAll(myForm.getSession());
                else {
                    sa = SubjectArea.findByAbbv(myForm.getSession(), myForm.getSubjectArea());
                    if (sa!=null) {
                        if (myForm.getCourseNumber()!=null && myForm.getCourseNumber().length()>0) {
                            classes = Class_DAO.getInstance().getSession().createQuery(
                                    "select distinct c from Class_ c inner join c.schedulingSubpart.instrOfferingConfig.instructionalOffering io inner join io.courseOfferings co where " +
                                    "c.schedulingSubpart.instrOfferingConfig.instructionalOffering.session.uniqueId=:sessionId and "+
                                    "co.subjectArea.uniqueId=:subjectAreaId and co.courseNbr like :courseNbr").
                            setLong("sessionId",myForm.getSession()).
                            setLong("subjectAreaId",sa.getUniqueId()).
                            setString("courseNbr",myForm.getCourseNumber().replaceAll("\\*", "%")).
                            setCacheable(true).list();
                        } else {
                            classes = Class_DAO.getInstance().getSession().createQuery(
                                    "select distinct c from Class_ c inner join c.schedulingSubpart.instrOfferingConfig.instructionalOffering io inner join io.courseOfferings co where " +
                                    "c.schedulingSubpart.instrOfferingConfig.instructionalOffering.session.uniqueId=:sessionId and "+
                                    "co.subjectArea.uniqueId=:subjectAreaId").
                            setLong("sessionId",myForm.getSession()).
                            setLong("subjectAreaId",sa.getUniqueId()).
                            setCacheable(true).list();
                        }
                    }
                }
                if (classes!=null && !classes.isEmpty()) {
                    int ord = WebTable.getOrder(sessionContext,"classes.order");
                    PdfWebTable table = getTable(true, myForm, classes, ord);
                    if (table!=null)
                        myForm.setTable(table.printTable(ord), table.getNrColumns(), table.getLines().size());
                }
            }
        }
        
        String msg = ApplicationProperties.getProperty("tmtbl.classes.message");
        if (msg!=null && msg.length()>0)
            request.setAttribute(Constants.REQUEST_MSSG, msg);
		
        return mapping.findForward("show");
	}
	
    public int getDaysCode(Set meetings) {
        int daysCode = 0;
        for (Iterator i=meetings.iterator();i.hasNext();) {
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
    
    protected String getMeetingTime(Class_ clazz) {
        String meetingTime = "";
        Formats.Format<Date> dpf = Formats.getDateFormat(Formats.Pattern.DATE_EVENT_SHORT);
        Assignment assignment = clazz.getCommittedAssignment();
        TreeSet meetings = (clazz.getEvent()==null?null:new TreeSet(clazz.getEvent().getMeetings()));
        DatePattern dp = (assignment==null?null:assignment.getDatePattern());
        if (meetings!=null && !meetings.isEmpty()) {
            int dayCode = getDaysCode(meetings);
            String days = "";
            for (int i=0;i<Constants.DAY_CODES.length;i++)
                if ((dayCode & Constants.DAY_CODES[i])!=0) days += Constants.DAY_NAMES_SHORT[i];
            meetingTime += days;
            Meeting first = (Meeting)meetings.first();
            meetingTime += " "+first.startTime()+" - "+first.stopTime();
        } else if (assignment!=null) {
            TimeLocation t = assignment.getTimeLocation();
            meetingTime += t.getDayHeader()+" "+t.getStartTimeHeader()+" - "+t.getEndTimeHeader();
        } else {
            meetingTime += "Arr Hrs";
        }
        if (meetings!=null && !meetings.isEmpty()) {
            if (dp==null || !dp.isDefault()) {
                Date first = ((Meeting)meetings.first()).getMeetingDate();
                Date last = ((Meeting)meetings.last()).getMeetingDate();
                if (dp!=null && dp.getType()==DatePattern.sTypeAlternate) 
                    meetingTime += " ("+dpf.format(first)+" - "+dpf.format(last)+" "+dp.getName()+")";
                else
                    meetingTime += " ("+dpf.format(first)+" - "+dpf.format(last)+")";
            }
        } else if (dp!=null && !dp.isDefault()) {
            if (dp.getType()==DatePattern.sTypeAlternate) 
                meetingTime += " ("+dpf.format(dp.getStartDate())+" - "+dpf.format(dp.getEndDate())+" "+dp.getName()+")";
            else
                meetingTime += " ("+dpf.format(dp.getStartDate())+" - "+dpf.format(dp.getEndDate())+")";
        }
        return meetingTime;
    }
    
    protected String getMeetingRooms(boolean html, Class_ clazz) {
        String meetingRooms = "";
        Assignment assignment = clazz.getCommittedAssignment();
        TreeSet<Meeting> meetings = (clazz.getEvent()==null?null:new TreeSet(clazz.getEvent().getMeetings()));
        TreeSet<Location> locations = new TreeSet<Location>();
        if (meetings!=null && !meetings.isEmpty()) {
            for (Meeting meeting : meetings)
                if (meeting.getLocation()!=null) locations.add(meeting.getLocation());
        } else if (assignment!=null && assignment.getDatePattern()!=null) {
            for (Iterator i=assignment.getRooms().iterator();i.hasNext();) {
                locations.add((Location)i.next());
            }
        }
        for (Location location: locations) {
            if (meetingRooms.length()>0) meetingRooms+=", ";
            meetingRooms+=(html ? location.getLabelWithHint() : location.getLabel());
        }
        return meetingRooms;
    }
    
    protected long getMeetingComparable(Class_ clazz) {
        Assignment assignment = clazz.getCommittedAssignment();
        TreeSet meetings = (clazz.getEvent()==null?null:new TreeSet(clazz.getEvent().getMeetings()));
        if (meetings!=null && !meetings.isEmpty()) {
            return ((Meeting)meetings.first()).getMeetingDate().getTime();
        } else if (assignment!=null) {
            return assignment.getTimeLocation().getStartSlot();
        }
        return -1;
    }
    
    protected String getMeetingInstructor(Class_ clazz) {
        String meetingInstructor = "";
        if (!clazz.isDisplayInstructor()) return meetingInstructor;
        for (Iterator i=new TreeSet(clazz.getClassInstructors()).iterator();i.hasNext();) {
            ClassInstructor ci = (ClassInstructor)i.next();
            if (meetingInstructor.length()>0) meetingInstructor+=", ";
            meetingInstructor += ci.getInstructor().getName(DepartmentalInstructor.sNameFormatLastInitial);
        }
        return meetingInstructor;
    }
    
    public boolean match(ClassesForm form, CourseOffering co) {
        if ("--ALL--".equals(form.getSubjectArea())) return true;
        if (!co.getSubjectArea().getSubjectAreaAbbreviation().equals(form.getSubjectArea())) return false;
        if (form.getCourseNumber()!=null && form.getCourseNumber().length()>0) {
            return co.getCourseNbr().matches(form.getCourseNumber().replaceAll("\\*", ".*"));
        }
        return true;
    }
    
	private PdfWebTable getTable(boolean html, ClassesForm form, Collection<Class_> classes, int ord) {
	    String nl = (html?"<br>":"\n");
        PdfWebTable table = new PdfWebTable( 6,
                form.getSessionLabel()+" classes"+("--ALL--".equals(form.getSubjectArea())?"":" ("+form.getSubjectArea()+(form.getCourseNumber()!=null && form.getCourseNumber().length()>0?" "+form.getCourseNumber():"")+")"), 
                "classes.do?ord=%%",
                new String[] {
                    "Course",
                    "Instruction"+nl+"Type",
                    "Section",
                    "Time",
                    "Room",
                    "Instructor"},
                    new String[] {"left", "left", "left", "left", "left", "left"},
                    new boolean[] {true, true, true, true, true, true} );
        table.setRowStyle("white-space:nowrap");
        table.setBlankWhenSame(true);
        boolean suffix = "true".equals(ApplicationProperties.getProperty("tmtbl.exam.report.suffix","false"));
        for (Class_ clazz: classes) {
            for (CourseOffering co : (Collection<CourseOffering>)clazz.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().getCourseOfferings()) {
                if (!match(form, co)) continue;
                String course = co.getCourseName();
                String itype =  clazz.getSchedulingSubpart().getItypeDesc();
                int itypeCmp = clazz.getSchedulingSubpart().getItype().getItype();
                String section = (suffix && clazz.getClassSuffix(co)!=null?clazz.getClassSuffix(co):clazz.getSectionNumberString());
                String time = getMeetingTime(clazz);
                long timeCmp = getMeetingComparable(clazz);
                String room = getMeetingRooms(html, clazz);
                String instr = getMeetingInstructor(clazz);
                table.addLine(
                        new String[] {
                                course,
                                itype,
                                section,
                                time,
                                room,
                                instr
                            },
                            new Comparable[] {
                                new MultiComparable(course, itypeCmp, section, timeCmp, room, instr),
                                new MultiComparable(itypeCmp, course, section, timeCmp, room, instr),
                                new MultiComparable(course, section, itypeCmp, timeCmp, room, instr),
                                new MultiComparable(timeCmp, room, course, itypeCmp, section, instr),
                                new MultiComparable(room, timeCmp, course, itypeCmp, section, instr),
                                new MultiComparable(instr, course, itypeCmp, section, timeCmp, room)
                            });                
            }
        }
        return table;	    
	}
}

