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
import java.util.Iterator;
import java.util.Locale;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.hibernate.Transaction;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.commons.web.WebTable;
import org.unitime.timetable.form.EventDetailForm;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.ClassEvent;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseEvent;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.Event;
import org.unitime.timetable.model.EventContact;
import org.unitime.timetable.model.EventNote;
import org.unitime.timetable.model.ExamEvent;
import org.unitime.timetable.model.ExamOwner;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.Meeting;
import org.unitime.timetable.model.RelatedCourseInfo;
import org.unitime.timetable.model.Roles;
import org.unitime.timetable.model.StandardEventNote;
import org.unitime.timetable.model.dao.CourseEventDAO;
import org.unitime.timetable.model.dao.EventDAO;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.webutil.BackTracker;
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
		
		String iOp = myForm.getOp();
		HttpSession webSession = request.getSession();
		User user = Web.getUser(webSession);
		Event event = EventDAO.getInstance().get(Long.valueOf(myForm.getId()));
		
		if (!Web.isLoggedIn(webSession)) {
			throw new Exception("Access Denied.");
		}			
		
		if (iOp != null) {
		
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
			
			if(iOp.equals("Delete")) {
	                org.hibernate.Session hibSession = new EventDAO().getSession();
	                Transaction tx = null;
	                boolean eventDeleted = false;
	                try {
	                    tx = hibSession.beginTransaction();
	        			
	                    Meeting m = myForm.getSelectedMeeting();
	                    String msg = "Deleted meeting "+m.toString()+" of "+event.getEventName()+" ("+event.getEventTypeLabel()+")";
	                    event.getMeetings().remove(m);
		                if (event.getMeetings().isEmpty()) {
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
		        			ChangeLog.addChange(
		                            hibSession,
		                            request,
		                            event,
		                            msg,
		                            ChangeLog.Source.EVENT_EDIT,
		                            ChangeLog.Operation.UPDATE,
		                            null,null);
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
				for (Iterator i = event.getNotes().iterator(); i.hasNext();) {
					EventNote en = (EventNote) i.next();
					if (en.getTextNote()!= null) {myForm.addNote(en.getTextNote());}
				}
				for (Iterator i = event.getNotes().iterator(); i.hasNext();) {
					EventNote en2 = (EventNote) i.next();
					StandardEventNote sen = en2.getStandardNote();
					if (sen!=null) {myForm.addNote(sen.getNote());}
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
				SimpleDateFormat iDateFormat2 = new SimpleDateFormat("MM/dd/yy", Locale.US);
				for (Iterator i=new TreeSet(event.getMeetings()).iterator();i.hasNext();) {
					Meeting meeting = (Meeting)i.next();
					int start = Constants.SLOT_LENGTH_MIN*meeting.getStartPeriod()+
								Constants.FIRST_SLOT_TIME_MIN+
								(meeting.getStartOffset()==null?0:meeting.getStartOffset());
					int startHour = start/60;
					int startMin = start%60;
					int end = Constants.SLOT_LENGTH_MIN*meeting.getStopPeriod()+
					Constants.FIRST_SLOT_TIME_MIN+
					(meeting.getStopOffset()==null?0:meeting.getStopOffset());
					int endHour = end/60;
					int endMin = end%60;
					String location = (meeting.getLocation()==null?"":meeting.getLocation().getLabel());
					String locationCapacity = meeting.getLocation().getCapacity().toString();
					String approvedDate = (meeting.getApprovedDate()==null?"":iDateFormat2.format(meeting.getApprovedDate()));
					myForm.addMeeting(meeting.getUniqueId(),
							iDateFormat.format(meeting.getMeetingDate()),
							(startHour>12?startHour-12:startHour)+":"+(startMin<10?"0":"")+startMin+(startHour>=12?"p":"a"),
							(endHour>12?endHour-12:endHour)+":"+(endMin<10?"0":"")+endMin+(endHour>=12?"p":"a"), 
							location, locationCapacity, approvedDate);
				}
				myForm.setCanEdit(user.isAdmin()||user.hasRole(Roles.EVENT_MGR_ROLE)||user.getId().equals(event.getMainContact().getExternalUniqueId()));
				if (event instanceof ClassEvent || event instanceof ExamEvent) {
					myForm.setCanEdit(false);
				}
					
				
		        Long nextId = Navigation.getNext(request.getSession(), Navigation.sInstructionalOfferingLevel, event.getUniqueId());
		        Long prevId = Navigation.getPrevious(request.getSession(), Navigation.sInstructionalOfferingLevel, event.getUniqueId());
		        myForm.setPreviousId(prevId==null?null:prevId.toString());
		        myForm.setNextId(nextId==null?null:nextId.toString());

			
		        if ("Course Event".equals(myForm.getEventType())) {
		            CourseEvent courseEvent = new CourseEventDAO().get(Long.valueOf(id));;
		            if (!courseEvent.getRelatedCourses().isEmpty()) {
			        	WebTable table = new WebTable(3, null, new String[] {"Object", "Type", "Title"}, new String[] {"left", "left", "left"}, new boolean[] {true, true, true});
			            for (Iterator i=new TreeSet(courseEvent.getRelatedCourses()).iterator();i.hasNext();) {
			                RelatedCourseInfo rci = (RelatedCourseInfo)i.next();
			                String onclick = null, name = null, type = null, title = null;
			                switch (rci.getOwnerType()) {
			                    case ExamOwner.sOwnerTypeClass :
			                        Class_ clazz = (Class_)rci.getOwnerObject();
			                        if (clazz.isViewableBy(user))
			                            onclick = "onClick=\"document.location='classDetail.do?cid="+clazz.getUniqueId()+"';\"";
			                        name = rci.getLabel();//clazz.getClassLabel();
			                        type = "Class";
			                        title = clazz.getSchedulePrintNote();
			                        if (title==null || title.length()==0) title=clazz.getSchedulingSubpart().getControllingCourseOffering().getTitle();
			                        break;
			                    case ExamOwner.sOwnerTypeConfig :
			                        InstrOfferingConfig config = (InstrOfferingConfig)rci.getOwnerObject();
			                        if (config.isViewableBy(user))
			                            onclick = "onClick=\"document.location='instructionalOfferingDetail.do?io="+config.getInstructionalOffering().getUniqueId()+"';\"";;
			                        name = rci.getLabel();//config.getCourseName()+" ["+config.getName()+"]";
			                        type = "Configuration";
			                        title = config.getControllingCourseOffering().getTitle();
			                        break;
			                    case ExamOwner.sOwnerTypeOffering :
			                        InstructionalOffering offering = (InstructionalOffering)rci.getOwnerObject();
			                        if (offering.isViewableBy(user))
			                            onclick = "onClick=\"document.location='instructionalOfferingDetail.do?io="+offering.getUniqueId()+"';\"";;
			                        name = rci.getLabel();//offering.getCourseName();
			                        type = "Offering";
			                        title = offering.getControllingCourseOffering().getTitle();
			                        break;
			                    case ExamOwner.sOwnerTypeCourse :
			                        CourseOffering course = (CourseOffering)rci.getOwnerObject();
			                        if (course.isViewableBy(user))
			                            onclick = "onClick=\"document.location='instructionalOfferingDetail.do?io="+course.getInstructionalOffering().getUniqueId()+"';\"";;
			                        name = rci.getLabel();//course.getCourseName();
			                        type = "Course";
			                        title = course.getTitle();
			                        break;
			                            
			                }
			                table.addLine(onclick, new String[] { name, type, title}, null);
			            }
			            request.setAttribute("EventDetail.table",table.printTable());
		            }			
		        }
			
			} else {
				myForm.setEventName("There is no event with this ID");
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
