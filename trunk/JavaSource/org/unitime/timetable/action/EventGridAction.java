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

import java.io.File;
import java.util.Collection;
import java.util.Date;
import java.util.StringTokenizer;
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
import org.unitime.commons.Debug;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.form.EventGridForm;
import org.unitime.timetable.form.EventRoomAvailabilityForm.DateLocation;
import org.unitime.timetable.model.Building;
import org.unitime.timetable.model.Event;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.RoomType;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.dao.BuildingDAO;
import org.unitime.timetable.model.dao.LocationDAO;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.webutil.timegrid.PdfEventGridTable;

/**
 * @author Tomas Muller
 *
 */

public class EventGridAction extends Action{

    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {

        EventGridForm myForm = (EventGridForm) form;
        
        if (!Web.isLoggedIn(request.getSession())) {
            throw new Exception("Access Denied.");
        }

        String op = myForm.getOp();

        if (request.getParameter("op2")!=null && request.getParameter("op2").length()>0)
            op = request.getParameter("op2");
        
        if (op == null  || op.trim().length() == 0){
        	myForm.reset(mapping, request);
    		User user = Web.getUser(request.getSession());
        	TimetableManager mgr = (user==null?null:TimetableManager.getManager(user));
    		if (mgr != null){
        		if (myForm.getRoomTypes() == null || myForm.getRoomTypes().length == 0){	
            		Collection<RoomType> allRoomTypes = myForm.getAllRoomTypes();
            		Vector<RoomType> defaultRoomTypes = mgr.findDefaultEventManagerRoomTimesFor(user.getRole(), myForm.getSessionId());
            		Vector<Long> orderedTypeList = new Vector(allRoomTypes.size());
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
 
        if ("Show Availability".equals(op) || "Export PDF".equals(op)) {
            myForm.loadDates(request);
            myForm.save(request.getSession());
        }
        
        if ("Clear".equals(op)) {
            Long sessionId = myForm.getSessionId();
            myForm.reset(mapping, request);
            myForm.setSessionId(sessionId);
    		User user = Web.getUser(request.getSession());
        	TimetableManager mgr = (user==null?null:TimetableManager.getManager(user));
    		if (mgr != null){
        		if (myForm.getRoomTypes() == null || myForm.getRoomTypes().length == 0){	
	        		Collection<RoomType> allRoomTypes = myForm.getAllRoomTypes();
	        		Vector<RoomType> defaultRoomTypes = mgr.findDefaultEventManagerRoomTimesFor(user.getRole(), myForm.getSessionId());
	        		Vector<Long> orderedTypeList = new Vector(allRoomTypes.size());
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
            myForm.save(request.getSession());
        }
        
        if ("SessionChanged".equals(op)) {
            if (myForm.getBuildingId()!=null) {
                Building b = BuildingDAO.getInstance().get(myForm.getBuildingId());
                Building nb = null;
                if (b!=null && !b.getSession().getUniqueId().equals(myForm.getSessionId())) {
                    if (b.getExternalUniqueId()!=null)
                        nb = Building.findByExternalIdAndSession(b.getExternalUniqueId(), myForm.getSessionId());
                    else
                        nb = Building.findByBldgAbbv(b.getAbbreviation(), myForm.getSessionId());
                }
                myForm.setBuildingId(nb==null?null:nb.getUniqueId());
            }
    		User user = Web.getUser(request.getSession());
        	TimetableManager mgr = (user==null?null:TimetableManager.getManager(user));
    		if (mgr != null){
        		if (myForm.getRoomTypes() == null || myForm.getRoomTypes().length == 0){	
	        		Collection<RoomType> allRoomTypes = myForm.getAllRoomTypes();
	        		Vector<RoomType> defaultRoomTypes = mgr.findDefaultEventManagerRoomTimesFor(user.getRole(), myForm.getSessionId());
	        		Vector<Long> orderedTypeList = new Vector(allRoomTypes.size());
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

            myForm.save(request.getSession());
        }
        
        if (request.getParameter("backId")!=null) {
            request.setAttribute("hash", request.getParameter("backId"));
        }
        
        if (request.getSession().getAttribute("EventGrid.StartTime")!=null) {
            myForm.load(request.getSession());
        }

        ActionMessages errors = myForm.validate(mapping, request);
        if (!errors.isEmpty()) { 
            saveErrors(request, errors);
            return mapping.findForward("show");
        }
        
        if ("Add Event".equals(op)) {
            if (request.getParameter("select")!=null) {
                String[] select = (String[])request.getParameterValues("select");
                TreeSet<DateLocation> locations = new TreeSet();
                select: for (int i=0;i<select.length;i++) {
                    StringTokenizer stk = new StringTokenizer(select[i],":");
                    Location location = LocationDAO.getInstance().get(Long.valueOf(stk.nextToken()));
                    Date date = new Date(Long.parseLong(stk.nextToken()));
                    int startTime = Integer.valueOf(stk.nextToken());
                    int stopTime = Integer.valueOf(stk.nextToken());
                    for (DateLocation last : locations) {
                        if (last.getDate().equals(date) && last.getLocation().equals(location.getPermanentId()) && startTime==last.getStopTime()) {
                            last.setStopTime(stopTime);
                            continue select;
                        }
                    }
                    locations.add(new DateLocation(date, location, startTime, stopTime));
                }
                HttpSession session = request.getSession();
                session.setAttribute("Event.DateLocations", locations);
                session.setAttribute("Event.EventType", Event.sEventTypes[Event.sEventTypeSpecial]);
                session.setAttribute("Event.SessionId", myForm.getSessionId());
                session.setAttribute("Event.StartTime", -1);
                session.setAttribute("Event.StopTime", -1);
                session.setAttribute("Event.IsAddMeetings", false);
                return mapping.findForward("add");
            } else {
                errors.add("select", new ActionMessage("errors.generic", "No available time/room selected."));
                saveErrors(request, errors);
            }
        }
        
        if ("Export PDF".equals(op)) {
            File f = ApplicationProperties.getTempFile("events", ".pdf");
            try {
                new PdfEventGridTable(myForm).export(f);
                if (f.exists()) request.setAttribute(Constants.REQUEST_OPEN_URL, "temp/"+f.getName()); 
            } catch (Exception e) {
                Debug.error(e);
            }
        }
 

        return mapping.findForward("show");
    }
}
