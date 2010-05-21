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

import java.util.Collection;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.commons.web.WebTable;
import org.unitime.timetable.form.EventRoomAvailabilityForm;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseEvent;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.ExamOwner;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.RelatedCourseInfo;
import org.unitime.timetable.model.RoomType;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.dao.CourseEventDAO;

/**
 * @author Zuzana Mullerova
 */
public class EventRoomAvailabilityAction extends Action {

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

		HttpSession webSession = request.getSession();
		User user = Web.getUser(webSession);
			
		if (!Web.isLoggedIn(webSession)) {
			throw new Exception("Access Denied.");
		}					
		
		EventRoomAvailabilityForm myForm = (EventRoomAvailabilityForm) form;
		try { 
			myForm.load(webSession);
		} catch (Exception e) {
		    ActionMessages errors = myForm.validate(mapping, request);
			errors.add("dates", new ActionMessage("errors.generic", e.getMessage()));
			saveErrors(request, errors);
			return  mapping.findForward("show");
		}

		String iOp = myForm.getOp();
		
		if (iOp != null) {
			
			// if the user is returning from the Event Add Info screen
			if ("eventAddInfo".equals(request.getAttribute("back"))) {
				myForm.load(request.getSession());
				iOp = null;
			}
			
			//return to event list
			if("Change Request".equals(iOp)) {
				myForm.loadData(request); myForm.save(webSession);
				request.setAttribute("back", "eventRoomAvailability");
				return mapping.findForward("back");
			}
			
			if("Continue".equals(iOp)) {
                myForm.loadData(request); myForm.save(webSession);  
                ActionMessages errors = myForm.validate(mapping, request);
                if (!errors.isEmpty()) {
                    saveErrors(request, errors);
                    return  mapping.findForward("show");
                }
				if (myForm.getIsAddMeetings()) {
				    if ("Course Related Event".equals(myForm.getEventType())) {
			            CourseEvent courseEvent = new CourseEventDAO().get((myForm.getEventId()));;
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
			                                onclick = "onClick=\"document.location='instructionalOfferingDetail.do?io="+config.getInstructionalOffering().getUniqueId()+"';\"";;
			                            name = rci.getLabel();//config.getCourseName()+" ["+config.getName()+"]";
			                            type = "Configuration";
			                            title = config.getControllingCourseOffering().getTitle();
			                            break;
			                        case ExamOwner.sOwnerTypeOffering :
			                            InstructionalOffering offering = (InstructionalOffering)rci.getOwnerObject();
			                            if (user.getRole()!=null && offering.isViewableBy(user))
			                                onclick = "onClick=\"document.location='instructionalOfferingDetail.do?io="+offering.getUniqueId()+"';\"";;
			                            name = rci.getLabel();//offering.getCourseName();
			                            type = "Offering";
			                            title = offering.getControllingCourseOffering().getTitle();
			                            break;
			                        case ExamOwner.sOwnerTypeCourse :
			                            CourseOffering course = (CourseOffering)rci.getOwnerObject();
			                            if (user.getRole()!=null && course.isViewableBy(user))
			                                onclick = "onClick=\"document.location='instructionalOfferingDetail.do?io="+course.getInstructionalOffering().getUniqueId()+"';\"";;
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
				    return mapping.findForward("eventUpdateMeetings");
				} else return mapping.findForward("eventAddInfo");
			}
			
			if("Change".equals(iOp)) {
			    myForm.setMaxRooms(request.getParameter("maxRooms"));
			    myForm.save(webSession);
			}
			
		} 

        if (myForm.getEventId()==null || myForm.getEventId()==0) {
        	TimetableManager mgr = (user==null?null:TimetableManager.getManager(user));
    		if (mgr != null){
        		if (myForm.getRoomTypes() == null || myForm.getRoomTypes().length == 0){	
	        		Collection<RoomType> allRoomTypes = myForm.getAllRoomTypes();
	        		Vector<RoomType> defaultRoomTypes = mgr.findDefaultEventManagerRoomTimesFor(user.getRole(), myForm.getSessionId());
	        		Vector<Long> orderedTypeList = new Vector();
        			for(RoomType displayedRoomType : allRoomTypes){
		        		for(RoomType rt : defaultRoomTypes){
	        				if (displayedRoomType.getUniqueId().equals(rt.getUniqueId())){
	        					orderedTypeList.add(rt.getUniqueId());
	        					break;
	        				}
	        			}	        			
	        		}
	        		myForm.setRoomTypes(new Long[orderedTypeList.size()]);
	        		int i = 0;
	        		for (Long l : orderedTypeList){
	        			myForm.getRoomTypes()[i] = l;
	        			i++;
	        		}
        		}
        	}
        }		
		
		return  mapping.findForward("show");
	}
		
	
}
