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
package org.unitime.timetable.action;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.hibernate.Transaction;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.commons.web.WebTable;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.form.EventDetailForm;
import org.unitime.timetable.form.EventDetailForm.MeetingBean;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.ClassEvent;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseEvent;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.Event;
import org.unitime.timetable.model.EventContact;
import org.unitime.timetable.model.EventNote;
import org.unitime.timetable.model.ExamEvent;
import org.unitime.timetable.model.ExamOwner;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.Meeting;
import org.unitime.timetable.model.RelatedCourseInfo;
import org.unitime.timetable.model.Roles;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.dao.ClassEventDAO;
import org.unitime.timetable.model.dao.CourseEventDAO;
import org.unitime.timetable.model.dao.EventDAO;
import org.unitime.timetable.model.dao.MeetingDAO;
import org.unitime.timetable.webutil.BackTracker;
import org.unitime.timetable.webutil.EventEmail;
import org.unitime.timetable.webutil.Navigation;

/**
 * @author Zuzana Mullerova
 */
public class EventDetailAction extends Action {

	/** 
	 * Method execute
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return ActionForward
	 */	
	public ActionForward execute(
			ActionMapping mapping,
			ActionForm form,
			HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		EventDetailForm myForm = (EventDetailForm) form;
		
        HttpSession webSession = request.getSession();
        if (!Web.isLoggedIn(webSession)) {
            throw new Exception("Access Denied.");
        }           

        String iOp = myForm.getOp();
		User user = Web.getUser(webSession);
		Event event = EventDAO.getInstance().get(Long.valueOf(myForm.getId()));

        Set<Department> userDepartments = null;
		String uname = (event==null || event.getMainContact()==null?user.getName():event.getMainContact().getShortName()); 
		if (user!=null && Roles.EVENT_MGR_ROLE.equals(user.getRole())) {
			TimetableManager mgr = TimetableManager.getManager(user);
			if (mgr!=null) {
			    userDepartments = mgr.getDepartments();
			    uname = mgr.getShortName();
			}
		} else if (user!=null && user.isAdmin()) {
		    TimetableManager mgr = TimetableManager.getManager(user);
            if (mgr!=null) uname = mgr.getShortName();
		}
		
		
		if (iOp != null) {
		
			if("Edit Event".equals(iOp)) {
				response.sendRedirect("eventEdit.do?id="+myForm.getId());
			}
			
			//return to event list
			if(iOp.equals("Back")) {
				if (myForm.getId()!=null)
					request.setAttribute("hash", "A"+myForm.getId());
				return mapping.findForward("showEventList");
			}

			if(iOp.equals("Previous")) {
				if (myForm.getPreviousId() != null)
					response.sendRedirect(response.encodeURL("eventDetail.do?id="+myForm.getPreviousId()));
				return null;
			}

			if(iOp.equals("Next")) {
				if (myForm.getPreviousId() != null)
					response.sendRedirect(response.encodeURL("eventDetail.do?id="+myForm.getNextId()));
				return null;
			}

			if(iOp.equals("Add Meetings")) {
				response.sendRedirect(response.encodeURL("eventAdd.do?op=view&id="+myForm.getId()));
				return null;
			}
			
			if(iOp.equals("Approve") && myForm.getSelectedMeetings()!=null && myForm.getSelectedMeetings().length>0) {
				Long[] selectedMeetings = myForm.getSelectedMeetings();
                org.hibernate.Session hibSession = new EventDAO().getSession();
                Transaction tx = null;
                try {
                    tx = hibSession.beginTransaction();
                    ActionMessages errors = new ActionMessages();
                    HashSet<Meeting> meetings = new HashSet();
					for (int i=0; i<selectedMeetings.length; i++) {
						Meeting approvedMeeting = MeetingDAO.getInstance().get(selectedMeetings[i]);
						if (Roles.EVENT_MGR_ROLE.equals(user.getRole())) {
						    Location location = approvedMeeting.getLocation();
						    if (location!=null && (userDepartments==null || location.getControllingDepartment()==null || !userDepartments.contains(location.getControllingDepartment()))) {
						        errors.add("approve", new ActionMessage("errors.generic", "Insufficient rights to approve "+approvedMeeting.toString()+" (controlling department does not match)."));
						        continue;
						    }
                        }
						approvedMeeting.setApprovedDate(new Date());
						meetings.add(approvedMeeting);
						hibSession.saveOrUpdate(approvedMeeting);
                        ChangeLog.addChange(
                                hibSession,
                                request,
                                event,
                                approvedMeeting.toString()+" of "+event.getEventName(),
                                ChangeLog.Source.EVENT_EDIT,
                                ChangeLog.Operation.APPROVE,
                                null,null);
					}
					if (!meetings.isEmpty()) {
	                    EventNote en = new EventNote();
	                    en.setTimeStamp(new Date());
	                    en.setNoteType(EventNote.sEventNoteTypeApproval);
	                    en.setUser(uname);
	                    en.setMeetingCollection(meetings);
	                    en.setTextNote(myForm.getNewEventNote());
	                    en.setEvent(event);
	                    hibSession.saveOrUpdate(en);
	                    event.getNotes().add(en);
	                    hibSession.saveOrUpdate(event);                 
	                    
	                    new EventEmail(event, EventEmail.sActionApprove, Event.getMultiMeetings(meetings), myForm.getNewEventNote()).send(request);
	                    
	                    myForm.setSelectedMeetings(null);
	                    myForm.setNewEventNote(null);
					}
                    tx.commit();
					if (!errors.isEmpty())
					    saveErrors(request, errors);
                } catch (Exception e) {
                    if (tx!=null) tx.rollback();
                    throw e;
                }		                

			}
			
			if (iOp.equals("Reject") && myForm.getSelectedMeetings()!=null && myForm.getSelectedMeetings().length>0) {
				Long[] selectedMeetings = myForm.getSelectedMeetings();
				org.hibernate.Session hibSession = new EventDAO().getSession();
                Transaction tx = null;
                boolean eventDeleted = false;
                try {
                    tx = hibSession.beginTransaction();
                    ActionMessages errors = new ActionMessages();
                    HashSet<Meeting> meetings = new HashSet();        			
					for (int i=0; i<selectedMeetings.length; i++) {
						Meeting rejectedMeeting = MeetingDAO.getInstance().get(selectedMeetings[i]);
                        if (Roles.EVENT_MGR_ROLE.equals(user.getRole())) {
                            Location location = rejectedMeeting.getLocation();
                            if (location!=null && (userDepartments==null || location.getControllingDepartment()==null || !userDepartments.contains(location.getControllingDepartment()))) {
                                errors.add("approve", new ActionMessage("errors.generic", "Insufficient rights to reject "+rejectedMeeting.toString()+" (controlling department does not match)."));
                                continue;
                            }
                        }
	                    event.getMeetings().remove(rejectedMeeting);
	                    meetings.add(rejectedMeeting);
	        			ChangeLog.addChange(
	                            hibSession,
	                            request,
	                            event,
	                            rejectedMeeting.toString()+" of "+event.getEventName(),
	                            ChangeLog.Source.EVENT_EDIT,
	                            ChangeLog.Operation.REJECT,
	                            null,null);
					}
					if (!meetings.isEmpty()) {
	                    EventNote en = new EventNote();
	                    en.setTimeStamp(new Date());
	                    en.setNoteType(EventNote.sEventNoteTypeRejection);
	                    en.setUser(uname);
	                    en.setMeetingCollection(meetings);
	                    en.setTextNote(myForm.getNewEventNote());
	                    en.setEvent(event);
	                    hibSession.saveOrUpdate(en);
	                    event.getNotes().add(en);
	                    hibSession.saveOrUpdate(event);     
	                    
	                    new EventEmail(event, EventEmail.sActionReject, Event.getMultiMeetings(meetings), myForm.getNewEventNote()).send(request);

	                    myForm.setSelectedMeetings(null);
	                    myForm.setNewEventNote(null);
					}
	                if (event.getMeetings().isEmpty()) {
	                	String msg = "All meetings of "+event.getEventName()+" ("+event.getEventTypeLabel()+") have been deleted.";
	        			ChangeLog.addChange(
	                            hibSession,
	                            request,
	                            event,
	                            msg,
	                            ChangeLog.Source.EVENT_EDIT,
	                            ChangeLog.Operation.DELETE,
	                            null,null);
	                	hibSession.delete(event);
	                	eventDeleted = true;
	                } else {
	                    hibSession.saveOrUpdate(event);
	                }
                    tx.commit();
                    if (!errors.isEmpty())
                        saveErrors(request, errors);
                } catch (Exception e) {
                    if (tx!=null) tx.rollback();
                    throw e;
                }
                if (eventDeleted) return mapping.findForward("showEventList");
			}				
			
			if(iOp.equals("Delete")) {
					Long[] selectedMeetings = myForm.getSelectedMeetings();
	                org.hibernate.Session hibSession = new EventDAO().getSession();
	                Transaction tx = null;
	                boolean eventDeleted = false;
	                try {
	                    tx = hibSession.beginTransaction();
	                    HashSet<Meeting> meetings = new HashSet();
						for (int i=0; i<selectedMeetings.length; i++) {
							Meeting deletedMeeting = MeetingDAO.getInstance().get(selectedMeetings[i]);
		                    meetings.add(deletedMeeting);
		                    event.getMeetings().remove(deletedMeeting);
		        			ChangeLog.addChange(
		                            hibSession,
		                            request,
		                            event,
		                            deletedMeeting.toString()+" of "+event.getEventName(),
		                            ChangeLog.Source.EVENT_EDIT,
		                            ChangeLog.Operation.UPDATE,
		                            null,null);
						}	  

						EventNote en = new EventNote();
	                    en.setTimeStamp(new Date());
	                    en.setNoteType(EventNote.sEventNoteTypeDeletion);
	                    en.setUser(uname);
	                    en.setMeetingCollection(meetings);
	                    en.setTextNote(myForm.getNewEventNote());
	                    en.setEvent(event);
						hibSession.saveOrUpdate(en);
						event.getNotes().add(en);
						hibSession.saveOrUpdate(event);		
						
						new EventEmail(event, EventEmail.sActionDelete, Event.getMultiMeetings(meetings), myForm.getNewEventNote()).send(request);

		                if (event.getMeetings().isEmpty()) {
		                	String msg = "All meetings of "+event.getEventName()+" ("+event.getEventTypeLabel()+") have been deleted.";
		                	ChangeLog.addChange(
		                            hibSession,
		                            request,
		                            event,
		                            msg,
		                            ChangeLog.Source.EVENT_EDIT,
		                            ChangeLog.Operation.DELETE,
		                            null,null);
		                	hibSession.delete(event);
		                	eventDeleted = true;
		                } else {
		                    hibSession.saveOrUpdate(event);
		                }
	                    tx.commit();
	                } catch (Exception e) {
	                    if (tx!=null) tx.rollback();
	                    throw e;
	                }
	                if (eventDeleted) return mapping.findForward("showEventList");
				}

		}

		
		if (request.getParameter("id")==null && myForm.getId()==null)
		    return mapping.findForward("showEventList");		
		
		
		if (request.getParameter("id")!=null) {
			String id = request.getParameter("id");
			myForm.setEvent(event);
			if (event!=null) {
				myForm.setEventName(event.getEventName()==null?"":event.getEventName());
				myForm.setEventType(event.getEventTypeLabel());
				myForm.setMinCapacity(event.getMinCapacity()==null?"":event.getMinCapacity().toString());
				myForm.setMaxCapacity(event.getMaxCapacity()==null?"":event.getMaxCapacity().toString());
				myForm.setAdditionalEmails(event.getEmail());
				if ("Course Event".equals(myForm.getEventType())) {
					myForm.setAttendanceRequired(((CourseEvent) event).isReqAttendance());
				} else
					myForm.setSponsoringOrgName(event.getSponsoringOrganization()==null?"":event.getSponsoringOrganization().getName());
				myForm.setIsManager(user.isAdmin()||user.hasRole(Roles.EVENT_MGR_ROLE));
				for (Iterator i = new TreeSet(event.getNotes()).iterator(); i.hasNext();) {
					EventNote en = (EventNote) i.next();
					myForm.addNote(en.toHtmlString(myForm.getIsManager()));
				}
				if (event.getMainContact()!=null)
				    myForm.setMainContact(event.getMainContact());
				for (Iterator i = event.getAdditionalContacts().iterator(); i.hasNext();) {
					EventContact ec = (EventContact) i.next();
					myForm.addAdditionalContact(
							(ec.getFirstName()==null?"":ec.getFirstName()),
							(ec.getMiddleName()==null?"":ec.getMiddleName()),
							(ec.getLastName()==null?"":ec.getLastName()),
							(ec.getEmailAddress()==null?"":ec.getEmailAddress()),
							(ec.getPhone()==null?"":ec.getPhone()));
				}
				SimpleDateFormat iDateFormat = new SimpleDateFormat("EEE MM/dd, yyyy", Locale.US);
				if (event.getMainContact()!=null && user.getId().equals(event.getMainContact().getExternalUniqueId())) {
				    myForm.setCanDelete(true);
				    myForm.setCanEdit(true);
				}
                if (user.isAdmin()||user.hasRole(Roles.EVENT_MGR_ROLE)) {
                    myForm.setCanApprove(true);
                    myForm.setCanEdit(true);
                }
                if (event instanceof ClassEvent || event instanceof ExamEvent) {
                    //myForm.setCanEdit(false);
                    myForm.setCanApprove(false);
                    myForm.setCanDelete(false);
                }
				for (Iterator i=new TreeSet(event.getMeetings()).iterator();i.hasNext();) {
					Meeting meeting = (Meeting)i.next();
					Location location = meeting.getLocation();
					MeetingBean mb = new MeetingBean(meeting);
					if (myForm.getCanEdit() && (!mb.getIsPast() || "true".equals(ApplicationProperties.getProperty("tmtbl.event.allowEditPast","false")))) {
                        if (myForm.getCanDelete()) {
                            mb.setCanSelect(true);
                        } else if (user.isAdmin()) {
							mb.setCanSelect(myForm.getCanApprove());
						} else if (userDepartments!=null && location!=null && location.getControllingDepartment()!=null) {
						    mb.setCanSelect(myForm.getCanApprove() && userDepartments.contains(location.getControllingDepartment()));
						}
					}
					for (Meeting overlap : meeting.getTimeRoomOverlaps())
					    mb.getOverlaps().add(new MeetingBean(overlap));
					myForm.addMeeting(mb);
				}
				if (myForm.getCanApprove() && !myForm.getCanSelectAll())
				    myForm.setCanApprove(false);
		        Long nextId = Navigation.getNext(request.getSession(), Navigation.sInstructionalOfferingLevel, event.getUniqueId());
		        Long prevId = Navigation.getPrevious(request.getSession(), Navigation.sInstructionalOfferingLevel, event.getUniqueId());
		        myForm.setPreviousId(prevId==null?null:prevId.toString());
		        myForm.setNextId(nextId==null?null:nextId.toString());
			
		        if (Event.sEventTypes[Event.sEventTypeCourse].equals(myForm.getEventType())) {
		            CourseEvent courseEvent = new CourseEventDAO().get(Long.valueOf(id));
		            if (!courseEvent.getRelatedCourses().isEmpty()) {
			        	WebTable table = new WebTable(5, null, new String[] {"Object", "Type", "Title","Limit","Assignment"}, new String[] {"left", "left", "left","right","left"}, new boolean[] {true, true, true, true,true});
			            for (Iterator i=new TreeSet(courseEvent.getRelatedCourses()).iterator();i.hasNext();) {
			                RelatedCourseInfo rci = (RelatedCourseInfo)i.next();
			                String onclick = null, name = null, type = null, title = null, assignment = null;
		                    String students = String.valueOf(rci.countStudents());
			                switch (rci.getOwnerType()) {
			                    case ExamOwner.sOwnerTypeClass :
			                        Class_ clazz = (Class_)rci.getOwnerObject();
			                        if (user.getRole()!=null && clazz.isViewableBy(user))
			                            onclick = "onClick=\"document.location='classDetail.do?cid="+clazz.getUniqueId()+"';\"";
			                        name = rci.getLabel();//clazz.getClassLabel();
			                        type = "Class";
			                        title = clazz.getSchedulePrintNote();
			                        if (title==null || title.length()==0) title=clazz.getSchedulingSubpart().getControllingCourseOffering().getTitle();
		                            if (clazz.getCommittedAssignment()!=null)
		                                assignment = clazz.getCommittedAssignment().getPlacement().getLongName();
			                        break;
			                    case ExamOwner.sOwnerTypeConfig :
			                        InstrOfferingConfig config = (InstrOfferingConfig)rci.getOwnerObject();
			                        if (user.getRole()!=null && config.isViewableBy(user))
			                            onclick = "onClick=\"document.location='instructionalOfferingDetail.do?io="+config.getInstructionalOffering().getUniqueId()+"';\"";
			                        name = rci.getLabel();//config.getCourseName()+" ["+config.getName()+"]";
			                        type = "Configuration";
			                        title = config.getControllingCourseOffering().getTitle();
			                        break;
			                    case ExamOwner.sOwnerTypeOffering :
			                        InstructionalOffering offering = (InstructionalOffering)rci.getOwnerObject();
			                        if (user.getRole()!=null && offering.isViewableBy(user))
			                            onclick = "onClick=\"document.location='instructionalOfferingDetail.do?io="+offering.getUniqueId()+"';\"";
			                        name = rci.getLabel();//offering.getCourseName();
			                        type = "Offering";
			                        title = offering.getControllingCourseOffering().getTitle();
			                        break;
			                    case ExamOwner.sOwnerTypeCourse :
			                        CourseOffering course = (CourseOffering)rci.getOwnerObject();
			                        if (user.getRole()!=null && course.isViewableBy(user))
			                            onclick = "onClick=\"document.location='instructionalOfferingDetail.do?io="+course.getInstructionalOffering().getUniqueId()+"';\"";
			                        name = rci.getLabel();//course.getCourseName();
			                        type = "Course";
			                        title = course.getTitle();
			                        break;
			                            
			                }
			                table.addLine(onclick, new String[] { name, type, title, students, assignment}, null);
			            }
			            request.setAttribute("EventDetail.table",table.printTable());
		            }			
		        }
		        /*
                if (Event.sEventTypes[Event.sEventTypeFinalExam].equals(myForm.getEventType()) || Event.sEventTypes[Event.sEventTypeMidtermExam].equals(myForm.getEventType())) {
                    ExamEvent examEvent = new ExamEventDAO().get(Long.valueOf(id));
                    if (examEvent.getExam()!=null && !examEvent.getExam().getOwners().isEmpty()) {
                        WebTable table = new WebTable(5, null, new String[] {"Object", "Type", "Title","Limit","Assignment"}, new String[] {"left", "left", "left","right","left"}, new boolean[] {true, true, true, true,true});
                        for (Iterator i=new TreeSet(examEvent.getExam().getOwners()).iterator();i.hasNext();) {
                            ExamOwner owner = (ExamOwner)i.next();
                            String onclick = null, name = null, type = null, title = null, assignment = null;
                            String students = String.valueOf(owner.countStudents());
                            switch (owner.getOwnerType()) {
                                case ExamOwner.sOwnerTypeClass :
                                    Class_ clazz = (Class_)owner.getOwnerObject();
                                    if (user.getRole()!=null && clazz.isViewableBy(user))
                                        onclick = "onClick=\"document.location='classDetail.do?cid="+clazz.getUniqueId()+"';\"";
                                    name = owner.getLabel();//clazz.getClassLabel();
                                    type = "Class";
                                    title = clazz.getSchedulePrintNote();
                                    if (title==null || title.length()==0) title=clazz.getSchedulingSubpart().getControllingCourseOffering().getTitle();
                                    if (clazz.getCommittedAssignment()!=null)
                                        assignment = clazz.getCommittedAssignment().getPlacement().getLongName();
                                    break;
                                case ExamOwner.sOwnerTypeConfig :
                                    InstrOfferingConfig config = (InstrOfferingConfig)owner.getOwnerObject();
                                    if (user.getRole()!=null && config.isViewableBy(user))
                                        onclick = "onClick=\"document.location='instructionalOfferingDetail.do?io="+config.getInstructionalOffering().getUniqueId()+"';\"";
                                    name = owner.getLabel();//config.getCourseName()+" ["+config.getName()+"]";
                                    type = "Configuration";
                                    title = config.getControllingCourseOffering().getTitle();
                                    break;
                                case ExamOwner.sOwnerTypeOffering :
                                    InstructionalOffering offering = (InstructionalOffering)owner.getOwnerObject();
                                    if (user.getRole()!=null && offering.isViewableBy(user))
                                        onclick = "onClick=\"document.location='instructionalOfferingDetail.do?io="+offering.getUniqueId()+"';\"";
                                    name = owner.getLabel();//offering.getCourseName();
                                    type = "Offering";
                                    title = offering.getControllingCourseOffering().getTitle();
                                    break;
                                case ExamOwner.sOwnerTypeCourse :
                                    CourseOffering course = (CourseOffering)owner.getOwnerObject();
                                    if (user.getRole()!=null && course.isViewableBy(user))
                                        onclick = "onClick=\"document.location='instructionalOfferingDetail.do?io="+course.getInstructionalOffering().getUniqueId()+"';\"";
                                    name = owner.getLabel();//course.getCourseName();
                                    type = "Course";
                                    title = course.getTitle();
                                    break;
                                        
                            }
                            table.addLine(onclick, new String[] { name, type, title, students, assignment}, null);
                        }
                        request.setAttribute("EventDetail.table",table.printTable());
                    }           
                }
                */
                if (Event.sEventTypes[Event.sEventTypeClass].equals(myForm.getEventType())) {
                    ClassEvent classEvent = new ClassEventDAO().get(Long.valueOf(id));
                    if (classEvent.getClazz()!=null) {
                        WebTable table = new WebTable(4, null, new String[] {"Name","Title","Limit","Assignment"}, new String[] {"left","left","right","left"}, new boolean[] {true, true, true, true});
                        Class_ clazz = (Class_)classEvent.getClazz();
                        String onclick = null, assignment = null;
                        if (user.getRole()!=null && clazz.isViewableBy(user))
                            onclick = "onClick=\"document.location='classDetail.do?cid="+clazz.getUniqueId()+"';\"";
                        String name = clazz.getClassLabel();
                        String title = clazz.getSchedulePrintNote();
                        String students = String.valueOf(clazz.getStudentEnrollments().size());
                        if (title==null || title.length()==0) title=clazz.getSchedulingSubpart().getControllingCourseOffering().getTitle();
                        if (clazz.getCommittedAssignment()!=null)
                            assignment = clazz.getCommittedAssignment().getPlacement().getLongName();
                        table.addLine(onclick, new String[] { name, title, students, assignment}, null);
                        request.setAttribute("EventDetail.table",table.printTable());
                    }           
                }   
			} else {
				throw new Exception("There is no event with this ID");
			}	
		}
		
        BackTracker.markForBack(
                request,
                "eventDetail.do?op=view&id=" + myForm.getId(),
                myForm.getEventName(),
                true, false);

		return mapping.findForward("show");
	}
	
}
