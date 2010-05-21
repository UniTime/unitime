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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.sf.cpsolver.coursett.model.TimeLocation.IntEnumeration;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.util.MessageResources;
import org.unitime.commons.Debug;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.commons.web.WebTable;
import org.unitime.timetable.form.InstructorEditForm;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.Roles;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.TimePattern;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.UserData;
import org.unitime.timetable.model.comparators.ClassComparator;
import org.unitime.timetable.model.comparators.ClassInstructorComparator;
import org.unitime.timetable.model.dao.DepartmentalInstructorDAO;
import org.unitime.timetable.solver.ClassAssignmentProxy;
import org.unitime.timetable.solver.WebSolver;
import org.unitime.timetable.solver.ui.AssignmentPreferenceInfo;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.LookupTables;
import org.unitime.timetable.webutil.BackTracker;
import org.unitime.timetable.webutil.DesignatorListBuilder;
import org.unitime.timetable.webutil.Navigation;
import org.unitime.timetable.webutil.RequiredTimeTable;


/** 
 * MyEclipse Struts
 * Creation date: 07-18-2006
 * 
 * XDoclet definition:
 * @struts.action path="/instructorDetail" name="instructorEditForm" input="/user/instructorDetail.jsp" scope="request"
 */
public class InstructorDetailAction extends PreferencesAction {

	// --------------------------------------------------------- Instance Variables

	// --------------------------------------------------------- Methods

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
		InstructorEditForm instructorEditForm = (InstructorEditForm) form;
		try {
			
	        // Set common lookup tables
	        super.execute(mapping, form, request, response);

			HttpSession httpSession = request.getSession();  
			User user = Web.getUser(httpSession);
			Long sessionId = (Long) user.getAttribute(Constants.SESSION_ID_ATTR_NAME);
			InstructorEditForm frm = (InstructorEditForm) form;
	        MessageResources rsc = getResources(request);
	        ActionMessages errors = new ActionMessages();
	        
	        //Read parameters
	        String instructorId = (request.getParameter("instructorId")==null) 
							        ? (request.getAttribute("instructorId")==null)
							                ? null
							                : request.getAttribute("instructorId").toString()
									: request.getParameter("instructorId");		        
		        
	        String op = frm.getOp();
	        String deleteType = request.getParameter("deleteType");
	        boolean timeVertical = RequiredTimeTable.getTimeGridVertical(user);
	        
	        if (request.getParameter("op2")!=null && request.getParameter("op2").length()>0)
	        	op = request.getParameter("op2");

	        if (request.getAttribute("fromChildScreen")!=null
	                && request.getAttribute("fromChildScreen").toString().equals("true") ) {
	            op = "";
	            frm.setOp(op);
	        }
	        
	        // Read instructor id from form
	        if(op.equals(rsc.getMessage("button.editInstructorInfo"))
	                || op.equals(rsc.getMessage("button.editInstructorPref"))
	                || op.equals(rsc.getMessage("button.backToInstructorList"))
	                || op.equals(rsc.getMessage("button.displayPrefs"))
	                || op.equals(rsc.getMessage("button.nextInstructor"))
	                || op.equals(rsc.getMessage("button.previousInstructor"))
	                ) {
	        	instructorId = frm.getInstructorId();
	        }else {
	        	frm.reset(mapping, request);
	        }
	        
	        //Check op exists
	        if(op==null) 
	            throw new Exception ("Null Operation not supported.");
	        
	        Debug.debug("op: " + op);
	        Debug.debug("instructor: " + instructorId);
	        
	        //Check instructor exists
	        if(instructorId==null || instructorId.trim()=="") 
	            throw new Exception ("Instructor Info not supplied.");
	        
	        // Cancel - Go back to Instructors List Screen
	        if(op.equals(rsc.getMessage("button.backToInstructorList")) 
	                && instructorId!=null && instructorId.trim()!="") {
	        	response.sendRedirect( response.encodeURL("instructorList.do"));
	        	return null;
	        }
	        
	        // If subpart id is not null - load subpart info
	        DepartmentalInstructorDAO idao = new DepartmentalInstructorDAO();
	        DepartmentalInstructor inst = idao.get(new Long(instructorId));
	        
	        //Edit Information - Redirect to info edit screen
	        if(op.equals(rsc.getMessage("button.editInstructorInfo")) 
	                && instructorId!=null && instructorId.trim()!="") {
	        	response.sendRedirect( response.encodeURL("instructorInfoEdit.do?instructorId="+instructorId) );
	        	return null;
	        }
	        
	        // Edit Preference - Redirect to prefs edit screen
	        if(op.equals(rsc.getMessage("button.editInstructorPref")) 
	                && instructorId!=null && instructorId.trim()!="") {
	        	response.sendRedirect( response.encodeURL("instructorPrefEdit.do?instructorId="+instructorId) );
	        	return null;
	        }
	        
	        // Add Designator
	        if(op.equals(rsc.getMessage("button.addDesignator2")) 
	                && instructorId!=null && instructorId.trim()!="") {
	            request.setAttribute("instructorId", instructorId);
	        	return mapping.findForward("addDesignator");
	        }
	        
            if (op.equals(rsc.getMessage("button.nextInstructor"))) {
            	response.sendRedirect(response.encodeURL("instructorDetail.do?instructorId="+frm.getNextId()));
            	return null;
            }
            
            if (op.equals(rsc.getMessage("button.previousInstructor"))) {
            	response.sendRedirect(response.encodeURL("instructorDetail.do?instructorId="+frm.getPreviousId()));
            	return null;
            }
	        
	        // Load form attributes that are constant
	        doLoad(request, frm, inst, instructorId);
	        
	        BackTracker.markForBack(
	        		request,
	        		"instructorDetail.do?instructorId=" + instructorId,
	        		"Instructor ("+ (frm.getName()==null?"null":frm.getName().trim()) +")",
	        		true, false);

	        //load class assignments
            Set allClasses = new HashSet();
            for (Iterator i=DepartmentalInstructor.getAllForInstructor(inst, inst.getDepartment().getSession().getUniqueId()).iterator();i.hasNext();) {
                DepartmentalInstructor di = (DepartmentalInstructor)i.next();
                allClasses.addAll(di.getClasses());
            }
			if (!allClasses.isEmpty()) {
				ClassAssignmentProxy proxy = WebSolver.getClassAssignmentProxy(request.getSession());
		        TimetableManager manager = TimetableManager.getManager(user);
								
		        boolean hasTimetable = false;
				try {
					if (manager!=null && manager.canSeeTimetable(Session.getCurrentAcadSession(user), user) && proxy!=null) {
						for (Iterator iterInst = allClasses.iterator(); iterInst.hasNext();) {
							ClassInstructor ci = (ClassInstructor) iterInst.next();
							Class_ c = ci.getClassInstructing();
							if (proxy.getAssignment(c)!=null) {
								hasTimetable = true; break;
							}
						}
					}
		        } catch (Exception e) {}
				
				
			    WebTable classTable =
			    	(hasTimetable? 
			    			new WebTable( 7,
			    					null,
			    					new String[] {"Class", "Check Conflicts", "Share", "Limit", "Manager", "Time", "Room"},
			    					new String[] {"left", "left","left", "left", "left", "left", "left"},
			    					null )
			    	:
		    			new WebTable( 5,
		    					null,
		    					new String[] {"Class", "Check Conflicts", "Share", "Limit", "Manager"},
		    					new String[] {"left", "left","left", "left", "left"},
		    					null )
			    	);
			    
			    String backType = request.getParameter("backType");
			    String backId = request.getParameter("backId");
						
				TreeSet classes = new TreeSet(new ClassInstructorComparator(new ClassComparator(ClassComparator.COMPARE_BY_LABEL)));
				classes.addAll(allClasses);
				
				Vector classIds = new Vector(classes.size());
				
				//Get class assignment information
				for (Iterator iterInst = classes.iterator(); iterInst.hasNext();) {
					ClassInstructor ci = (ClassInstructor) iterInst.next();
					Class_ c = ci.getClassInstructing();
					classIds.add(c.getUniqueId());
					
					String limitString = "";
			    	if (!c.getSchedulingSubpart().getInstrOfferingConfig().isUnlimitedEnrollment().booleanValue()) {
			    		if (c.getExpectedCapacity() != null) {
			    			limitString = c.getExpectedCapacity().toString();
			    			if (c.getMaxExpectedCapacity() != null && !c.getMaxExpectedCapacity().equals(c.getExpectedCapacity())){
			    				limitString = limitString + "-" + c.getMaxExpectedCapacity().toString();
			    			}
			    		} else {
			    			limitString = "0";
			    			if (c.getMaxExpectedCapacity() != null && c.getMaxExpectedCapacity().intValue() != 0){
			    				limitString = limitString + "-" + c.getMaxExpectedCapacity().toString();
			    			}
			    		}
			    	}
			    	
			    	String managingDept = null;
			    	if (c.getManagingDept()!=null) {
			    		Department d = c.getManagingDept();
			    		managingDept = d.getManagingDeptAbbv();
			    	}
			    	
			    	String assignedTime = "";
			    	String assignedRoom = "";
			    	Assignment a = null;
		    		AssignmentPreferenceInfo info = null;
		    		try {
		    			a = proxy.getAssignment(c);
		    			info = proxy.getAssignmentInfo(c);
		    		} catch (Exception e) {
		    			Debug.error(e);
		    		}
		    		if (a!=null) {
		    			if (info!=null)
		    				assignedTime += "<font color='"+PreferenceLevel.int2color(info.getTimePreference())+"'>";
		    			IntEnumeration e = a.getTimeLocation().getDays();
		   				while (e.hasMoreElements()){
		   					assignedTime += Constants.DAY_NAMES_SHORT[(int)e.nextInt()];
		   				}
		   				assignedTime += " ";
		   				assignedTime += a.getTimeLocation().getStartTimeHeader();
		   				assignedTime += "-";
		   				assignedTime += a.getTimeLocation().getEndTimeHeader();
		   				if (info!=null)
		   					assignedTime += "</font>";
		   				
			    		Iterator it2 = a.getRooms().iterator();
			    		while (it2.hasNext()){
			    			Location room = (Location)it2.next();
			    			if (info!=null)
			    				assignedRoom += "<font color='"+PreferenceLevel.int2color(info.getRoomPreference(room.getUniqueId()))+"'>";
			    			assignedRoom += room.getLabel();
			    			if (info!=null)
			    				assignedRoom += "</font>";
			    			if (it2.hasNext())
			    				assignedRoom += ", ";
			    		}	
		    		}
		    		
		    		String onClick = null;
		    		if (c.isViewableBy(user)) {
		    			onClick = "onClick=\"document.location='classDetail.do?cid="+c.getUniqueId()+"';\"";
		    		}
		    		
		    		boolean back = "PreferenceGroup".equals(backType) && c.getUniqueId().toString().equals(backId);
			    	
					if (hasTimetable) {
						classTable.addLine(
								onClick,
								new String[] {
									(back?"<A name=\"back\"></A>":"")+
									c.getClassLabel(),
									(ci.isLead().booleanValue()?"<IMG border='0' alt='true' align='absmiddle' src='images/tick.gif'>":""),
									ci.getPercentShare()+"%",
									limitString,
									managingDept,
									assignedTime,
									assignedRoom
								},
								null,null);
					} else {
						classTable.addLine(
								onClick,
								new String[] {
									(back?"<A name=\"back\"></A>":"")+
									c.getClassLabel(),
									(ci.isLead().booleanValue()?"<IMG border='0' alt='true' align='absmiddle' src='images/tick.gif'>":""),
									ci.getPercentShare()+"%",
									limitString,
									managingDept
								},
								null,null);
					}
				}
				
				Navigation.set(request.getSession(), Navigation.sClassLevel, classIds);

				String tblData = classTable.printTable();
				request.setAttribute("classTable", tblData);
			}
			
			//// Set display distribution to Not Applicable
			/*
			request.setAttribute(DistributionPref.DIST_PREF_REQUEST_ATTR, 
					"<FONT color=696969>Distribution Preferences Not Applicable</FONT>");
					*/

			frm.setDisplayPrefs(UserData.getPropertyBoolean(request.getSession(),"InstructorDetail.distPref", false));
			
			if (op.equals(rsc.getMessage("button.displayPrefs"))
			        || ( request.getAttribute("showPrefs")!=null 
			                && request.getAttribute("showPrefs").equals("true")) ) {
				frm.setDisplayPrefs(true);
				UserData.setPropertyBoolean(request.getSession(),"InstructorDetail.distPref", true);
			}
			
			if (op.equals(rsc.getMessage("button.hidePrefs"))
			        || ( request.getAttribute("showPrefs")!=null 
			                && request.getAttribute("showPrefs").equals("false")) ) {
				frm.setDisplayPrefs(false);
				UserData.setPropertyBoolean(request.getSession(),"InstructorDetail.distPref", false);
			}

			if (frm.isDisplayPrefs()) {
		        // Initialize Preferences for initial load 
		        Set timePatterns = new HashSet();
		        frm.setAvailableTimePatterns(null);
		        initPrefs(user, frm, inst, null, false);
		        timePatterns.add(new TimePattern(new Long(-1)));
		    	//timePatterns.addAll(TimePattern.findApplicable(request,30,false));
		        
				// Process Preferences Action
				processPrefAction(request, frm, errors);
				
				// Generate Time Pattern Grids
				super.generateTimePatternGrids(request, frm, inst, timePatterns, "init", timeVertical, false, null);
	
		        LookupTables.setupRooms(request, inst);		 // Room Prefs
		        LookupTables.setupBldgs(request, inst);		 // Building Prefs
		        LookupTables.setupRoomFeatures(request, inst); // Preference Levels
		        LookupTables.setupRoomGroups(request, inst);   // Room Groups
			}
			
			DepartmentalInstructor previous = inst.getPreviousDepartmentalInstructor(request.getSession(),user,false,true);
			frm.setPreviousId(previous==null?null:previous.getUniqueId().toString());
			DepartmentalInstructor next = inst.getNextDepartmentalInstructor(request.getSession(),user,false,true);
			frm.setNextId(next==null?null:next.getUniqueId().toString());
		
	        return mapping.findForward("showInstructorDetail");
		
		} catch (Exception e) {
			Debug.error(e);
			throw e;
		}
	}

	/**
	 * Loads the non-editable instructor info into the form
	 * @param request
	 * @param frm
	 * @param inst
	 * @param instructorId
	 */
	private void doLoad(HttpServletRequest request, InstructorEditForm frm, DepartmentalInstructor inst, String instructorId) {
        // populate form
		frm.setInstructorId(instructorId);
		
		frm.setName( ((inst.getFirstName() == null) ?"": Constants.toInitialCase(inst.getFirstName(), "-".toCharArray()) )+ " " 
        			+ ((inst.getMiddleName() == null) ?"": Constants.toInitialCase(inst.getMiddleName(), "-".toCharArray()) )+ " " 
        			+ Constants.toInitialCase(inst.getLastName(), "-".toCharArray()));
		
		frm.setEmail(inst.getEmail());
		
		String puid = inst.getExternalUniqueId();
		if (puid != null) {
			frm.setPuId(puid);
		}
				
		if (inst.getPositionType() != null) {
			frm.setPosType(inst.getPositionType().getLabel().trim());
		}
		
		if (inst.getCareerAcct() != null) {
			frm.setCareerAcct(inst.getCareerAcct().trim());
		}
		else {
		    if (puid != null && User.canIdentify()) {
		        User user = User.identify(puid);
		        if (user!=null && user.getLogin()!=null) {
		            frm.setCareerAcct(user.getLogin());
		        }
		    }
		}
		
		if (inst.getNote() != null) {
			frm.setNote(inst.getNote().trim());
		}
				
        User user = Web.getUser(request.getSession());

		frm.setEditable(inst.isEditableBy(user));
		frm.setLimitedEditable(frm.isEditable() || inst.getDepartment().isLimitedEditableBy(user));
		if (frm.isEditable() && user.getRole().equals(Roles.EXAM_MGR_ROLE)) {
		    frm.setLimitedEditable(true);
		    frm.setEditable(false);
		}

		request.getSession().setAttribute(Constants.DEPT_ID_ATTR_NAME, inst.getDepartment().getUniqueId().toString());
		
		// Check column ordering - default to name
		String orderStr = request.getParameter("order");
		int cols = 2;
		int order = 1;

		if (orderStr != null && orderStr.trim().length() != 0) {
			try {
				order = Integer.parseInt(orderStr);
				if (Math.abs(order) > cols)
					order = 1;
			} catch (Exception e) {
				order = 1;
			}
		}
        
        frm.setIgnoreDist(inst.isIgnoreToFar()==null?false:inst.isIgnoreToFar().booleanValue());
		
		String designatorTable = new DesignatorListBuilder().htmlTableForInstructor(request, instructorId, order);
		request.setAttribute("designatorList", designatorTable);
	}
	
	

}

