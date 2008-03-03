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
package org.unitime.timetable.action;

import java.awt.Image;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
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
import org.apache.struts.util.MessageResources;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Order;
import org.unitime.commons.Debug;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.commons.web.WebTable;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.form.RoomListForm;
import org.unitime.timetable.model.Building;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentRoomFeature;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.ExternalRoom;
import org.unitime.timetable.model.GlobalRoomFeature;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.NonUniversityLocation;
import org.unitime.timetable.model.PeriodPreferenceModel;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.Roles;
import org.unitime.timetable.model.Room;
import org.unitime.timetable.model.RoomDept;
import org.unitime.timetable.model.RoomGroup;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Settings;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.comparators.DepartmentNameComparator;
import org.unitime.timetable.model.dao.RoomFeatureDAO;
import org.unitime.timetable.model.dao.TimetableManagerDAO;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.LookupTables;
import org.unitime.timetable.util.PdfEventHandler;
import org.unitime.timetable.webutil.PdfWebTable;
import org.unitime.timetable.webutil.RequiredTimeTable;

import com.lowagie.text.Document;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;


/**
 * MyEclipse Struts Creation date: 02-18-2005
 * 
 * XDoclet definition:
 * 
 * @struts:action path="/RoomList" name="roomListForm"
 *                input="/admin/roomList.jsp" scope="request" validate="false"
 */
public class RoomListAction extends Action {

	// --------------------------------------------------------- Instance
	// Variables

	// --------------------------------------------------------- Methods

	/**
	 * Method execute
	 * 
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return ActionForward
	 * @throws HibernateException
	 */
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		HttpSession webSession = request.getSession();
		if (!Web.isLoggedIn(webSession)) {
			throw new Exception("Access Denied.");
		}

		User user = Web.getUser(webSession);
		RoomListForm roomListForm = (RoomListForm) form;
		ActionMessages errors = new ActionMessages();
		
		//get deptCode from request - for user with only one department
		String dept = (String)request.getAttribute("deptCode");
		if (dept != null) {
			roomListForm.setDeptCodeX(dept);
		}
		
		if (webSession.getAttribute(Constants.DEPT_CODE_ATTR_ROOM_NAME) != null
				&& (roomListForm.getDeptCodeX() == null)) {
			roomListForm.setDeptCodeX(webSession.getAttribute(
					Constants.DEPT_CODE_ATTR_ROOM_NAME).toString());
		}

		// Set Session Variable
		if (roomListForm.getDeptCodeX()!=null && !roomListForm.getDeptCodeX().equalsIgnoreCase("")) {
			webSession.setAttribute(Constants.DEPT_CODE_ATTR_ROOM_NAME,
					roomListForm.getDeptCodeX());
		}
		
		// Validate input
		errors = roomListForm.validate(mapping, request);

		// Validation fails
		if (errors.size() > 0) {
			saveErrors(request, errors);
			return mapping.findForward("showRoomSearch");
		}

		Long sessionId = Session.getCurrentAcadSession(user).getSessionId();
		String mgrId = (String)user.getAttribute(Constants.TMTBL_MGR_ID_ATTR_NAME);
        TimetableManager owner = (new TimetableManagerDAO()).get(new Long(mgrId));

        roomListForm.setEditRoomSharing(false);
		roomListForm.setCanAdd(false);
        roomListForm.setCanAddNonUniv(false);
        roomListForm.setCanAddSpecial(false);
        boolean hasExternalRooms = !ExternalRoom.findAll(sessionId).isEmpty();
		if (Constants.ALL_OPTION_LABEL.equals(roomListForm.getDeptCodeX())) {
	        for (Iterator i=owner.departmentsForSession(sessionId).iterator();i.hasNext();) {
	        	Department d = (Department)i.next();
	        	if (d.isEditableBy(user)) {
                    roomListForm.setCanAdd(user.getRole().equals(Roles.ADMIN_ROLE)); //&& !hasExternalRooms
                    roomListForm.setCanAddNonUniv(true);
                    roomListForm.setCanAddSpecial(hasExternalRooms);
	        		break;
	        	}
	        }
		} else {
			if (user.getRole().equals(Roles.ADMIN_ROLE)) {
				roomListForm.setEditRoomSharing(true);
				roomListForm.setCanAdd(true); // !hasExternalRooms
                roomListForm.setCanAddNonUniv(true);
                roomListForm.setCanAddSpecial(hasExternalRooms);
			} else {
		        for (Iterator i=owner.departmentsForSession(sessionId).iterator();i.hasNext();) {
		        	Department d = (Department)i.next();
		        	if (roomListForm.getDeptCodeX().equals(d.getDeptCode())) {
		        		if (d.isEditableBy(user)) {
		        			roomListForm.setEditRoomSharing(true);
		        			roomListForm.setCanAddNonUniv(true);
                            roomListForm.setCanAddSpecial(hasExternalRooms);
		        			break;
		        		}
		        	}
		        }
			}
		}

		if (roomListForm.getDeptCodeX().equalsIgnoreCase("All")) {
            roomListForm.setRooms(Session.getCurrentAcadSession(user).getRoomsFast(Department.getDeptCodesForUser(user, false)));
		} else if (roomListForm.getDeptCodeX().equalsIgnoreCase("Exam")) {
		    roomListForm.setRooms(Location.findAllExamLocations(sessionId));
		} else {
		    roomListForm.setRooms(Session.getCurrentAcadSession(user).getRoomsFast(new String[] {roomListForm.getDeptCodeX()}));
		}
		
		if ("Export PDF".equals(request.getParameter("op"))) {
			buildPdfWebTable(request, roomListForm, "yes".equals(Settings.getSettingValue(user, Constants.SETTINGS_ROOMS_FEATURES_ONE_COLUMN)), "Exam".equals(roomListForm.getDeptCodeX()));
		}
		
		// build web table for university locations
		buildWebTable(request, roomListForm, "yes".equals(Settings.getSettingValue(user, Constants.SETTINGS_ROOMS_FEATURES_ONE_COLUMN)), "Exam".equals(roomListForm.getDeptCodeX()));
		
		//set request attribute for department
		LookupTables.setupDeptsForUser(request, user, sessionId, true);

		return mapping.findForward("showRoomList");

	}

	/**
	 * 
	 * @param request
	 * @param roomListForm
	 * @throws Exception 
	 */
	private void buildWebTable(HttpServletRequest request, RoomListForm roomListForm, boolean featuresOneColumn, boolean periodPrefs ) throws Exception {
		
		MessageResources rsc = getResources(request);
		ActionMessages errors = new ActionMessages();
		HttpSession httpSession = request.getSession();
		
		Collection rooms = roomListForm.getRooms();
		if (rooms.size() == 0) {
			errors.add("searchResult", new ActionMessage("errors.generic", "No rooms for the selected department were found."));
			saveErrors(request, errors);
			request.setAttribute("classrooms", null);
			request.setAttribute("additionalRooms", null);
			request.setAttribute("specialRooms", null);
			request.setAttribute("nonUnivLocation", null);
		} else {		
			User user = Web.getUser(httpSession);
			Long sessionId = Session.getCurrentAcadSession(user).getSessionId();
			ArrayList globalRoomFeatures = new ArrayList();
			Set deptRoomFeatures = new TreeSet();
			int colspan=0;
			
			String mgrId = (String)user.getAttribute(Constants.TMTBL_MGR_ID_ATTR_NAME);
			TimetableManagerDAO tdao = new TimetableManagerDAO();
	        TimetableManager owner = tdao.get(new Long(mgrId));
	        boolean isAdmin = user.getRole().equals(Roles.ADMIN_ROLE);
			Set ownerDepts = owner.departmentsForSession(sessionId);
			Set externalDepartments = Department.findAllExternal(sessionId);
			Set depts = null;
			if (roomListForm.getDeptCodeX().equalsIgnoreCase("All")) {
				if (isAdmin) {
					depts = Department.findAll(sessionId);
				} else {
					depts = Department.findAllOwned(sessionId, owner, false);
				}
			} else if (roomListForm.getDeptCodeX().equalsIgnoreCase("Exam")) {
			    depts = new HashSet(0);
			} else {
				depts = new HashSet(1);
				depts.add(Department.findByDeptCode(roomListForm.getDeptCodeX(),sessionId));
			}
			
			org.hibernate.Session hibSession = null;
			try {
				
				RoomFeatureDAO d = new RoomFeatureDAO();
				hibSession = d.getSession();
				
				List list = hibSession
							.createCriteria(GlobalRoomFeature.class)
							.addOrder(Order.asc("label"))
							.list();
				
				for (Iterator iter = list.iterator();iter.hasNext();) {
					GlobalRoomFeature rf = (GlobalRoomFeature) iter.next();
					globalRoomFeatures.add(rf);
				}
				Debug.debug("global room feature: " + globalRoomFeatures.size());
				
				if (roomListForm.getDeptCodeX().equalsIgnoreCase("All")) {
					String[] deptCodes = Department.getDeptCodesForUser(user, false);
					if (deptCodes!=null)
						deptRoomFeatures.addAll(hibSession.createQuery(
								"select distinct f from DepartmentRoomFeature f inner join f.department d " +
								"where d.session.uniqueId=:sessionId and d.deptCode in ("+
								Constants.arrayToStr(deptCodes, "'", ", ")+") order by f.label").
								setLong("sessionId",sessionId.longValue()).
								list());
					else
						deptRoomFeatures.addAll(hibSession.createQuery(
								"select distinct f from DepartmentRoomFeature f inner join f.department d where " +
								"d.session.uniqueId=:sessionId order by f.label").
								setLong("sessionId",sessionId.longValue()).
								list());						
				} else if (roomListForm.getDeptCodeX().equalsIgnoreCase("Exam")) {
				    
				} else {
					deptRoomFeatures.addAll(hibSession.
						createQuery(
								"select distinct f from DepartmentRoomFeature f inner join f.department d " +
								"where d.session.uniqueId=:sessionId and d.deptCode = :deptCode order by f.label").
						setLong("sessionId",sessionId.longValue()).
						setString("deptCode",roomListForm.getDeptCodeX()).list());
				}
				
				Debug.debug("manager room feature: " + deptRoomFeatures.size());
				
			} catch (Exception e) {
				Debug.error(e);
			}
	
			//build headings for university rooms
			String fixedHeading1[][] =
			    (periodPrefs?
		                (featuresOneColumn? new String[][]
		                                                 { { "Bldg", "left", "true" },
		                                                 { "Room", "left", "true" },
		                                                 { "Capacity", "right", "false" },
		                                                 { "Exam Capacity", "right", "false" },
		                                                 { "Period Preferences", "center", "false" },
		                                                 { "Groups", "left", "true" },
		                                                 { "Features", "left", "true" } } 
		                                             : new String[][]
		                                                 { { "Bldg", "left", "true" },
		                                                 { "Room", "left", "true" },
		                                                 { "Capacity", "right", "false" },
		                                                 { "Exam Capacity", "right", "false" },
		                                                 { "Period Preferences", "center", "false" },
		                                                 { "Groups", "left", "true" } })                    
			    :
	                (featuresOneColumn? new String[][]
	                                                 { { "Bldg", "left", "true" },
	                                                 { "Room", "left", "true" },
	                                                 { "Capacity", "right", "false" },
	                                                 { "Availability", "left", "true" },
	                                                 { "Departments", "left", "true" },
	                                                 { "Control", "left", "true" },
	                                                 { "Groups", "left", "true" },
	                                                 { "Features", "left", "true" } } 
	                                             : new String[][]
	                                                 { { "Bldg", "left", "true" },
	                                                 { "Room", "left", "true" },
	                                                 { "Capacity", "right", "false" },
	                                                 { "Availability", "left", "true" },
	                                                 { "Departments", "left", "true" },
	                                                 { "Control", "left", "true" },
	                                                 { "Groups", "left", "true" } })                    
			    );
	
			String heading1[] = new String[fixedHeading1.length
					+ (featuresOneColumn?0:(globalRoomFeatures.size() + deptRoomFeatures.size()))];
			String alignment1[] = new String[heading1.length];
			boolean sorted1[] = new boolean[heading1.length];
			
			for (int i = 0; i < fixedHeading1.length; i++) {
				heading1[i] = fixedHeading1[i][0];
				alignment1[i] = fixedHeading1[i][1];
				sorted1[i] = (Boolean.valueOf(fixedHeading1[i][2])).booleanValue();
			}
			colspan = fixedHeading1.length;
			if (!featuresOneColumn) {
				int i = fixedHeading1.length;
				for (Iterator it = globalRoomFeatures.iterator(); it.hasNext();) {
                    GlobalRoomFeature grf =(GlobalRoomFeature) it.next(); 
					heading1[i] = "<span title='"+grf.getLabel()+"'>"+grf.getAbbv()+"</span>"; 
					alignment1[i] = "center";
					sorted1[i] = true;
					i++;
				}
				for (Iterator it = deptRoomFeatures.iterator(); it.hasNext();) {
					DepartmentRoomFeature drf = (DepartmentRoomFeature)it.next();
					String title = drf.getLabel();
					if (roomListForm.getDeptCodeX().equalsIgnoreCase("All")) {
						Department dept = drf.getDepartment();
						title+=" ("+dept.getShortLabel()+")";
					}
					heading1[i] = "<span title='"+title+"'>"+drf.getAbbv()+"</span>";
					alignment1[i] = "center";
					sorted1[i] = true;
					i++;
				}
				colspan = i;
			}
			
			
			//build headings for non-univ locations
			String fixedHeading2[][] =
			    ( periodPrefs ?
		                ( featuresOneColumn ? new String[][]
		                                                   {{ "Location", "left", "true" },
		                                                   { "Capacity", "right", "false" },
		                                                   { "Exam Capacity", "right", "false" },
		                                                   { "Period Preferences", "center", "false" },
		                                                   { "Groups", "left", "true" },
		                                                   { "Features", "left", "true" }}
		                                           : new String[][]
		                                                   {{ "Location", "left", "true" },
		                                                   { "Capacity", "right", "false" },
		                                                   { "Exam Capacity", "right", "false" },
		                                                   { "Period Preferences", "center", "false" },
		                                                   { "Groups", "left", "true" } })
			     :
	                ( featuresOneColumn ? new String[][]
	                                                   {{ "Location", "left", "true" },
	                                                   { "Capacity", "right", "false" },
	                                                   { "IgnTooFar", "center", "true" },
	                                                   { "IgnChecks", "center", "true" },
	                                                   { "Availability", "left", "true" },
	                                                   { "Departments", "left", "true" },
	                                                   { "Control", "left", "true" },
	                                                   { "Groups", "left", "true" },
	                                                   { "Features", "left", "true" }}
	                                           : new String[][]
	                                                   {{ "Location", "left", "true" },
	                                                   { "Capacity", "right", "false" },
	                                                   { "IgnTooFar", "center", "true" },
	                                                   { "IgnChecks", "center", "true" },
	                                                   { "Availability", "left", "true" },
	                                                   { "Departments", "left", "true" },
	                                                   { "Control", "left", "true" },
	                                                   { "Groups", "left", "true" } })
	        );
			
			String heading2[] = new String[fixedHeading2.length
			                               + (featuresOneColumn?0:(globalRoomFeatures.size() + deptRoomFeatures.size()))];
			String alignment2[] = new String[heading2.length];
			boolean sorted2[] = new boolean[heading2.length];
			
			for (int i = 0; i < fixedHeading2.length; i++) {
				heading2[i] = fixedHeading2[i][0];
				alignment2[i] = fixedHeading2[i][1];
				sorted2[i] = (Boolean.valueOf(fixedHeading2[i][2])).booleanValue();
			}
			if (!featuresOneColumn) {
				int i = fixedHeading2.length;
				for (Iterator it = globalRoomFeatures.iterator(); it.hasNext();) {
                    GlobalRoomFeature grf = (GlobalRoomFeature)it.next();
					heading2[i] = "<span title='"+grf.getLabel()+"'>"+grf.getAbbv()+"</span>"; 
					alignment2[i] = "center";
					sorted2[i] = true;
					i++;
				}
				for (Iterator it = deptRoomFeatures.iterator(); it.hasNext();) {
					DepartmentRoomFeature drf = (DepartmentRoomFeature)it.next();
					String title = drf.getLabel();
					if (roomListForm.getDeptCodeX().equalsIgnoreCase("All")) {
						Department dept = drf.getDepartment();
						title+=" ("+dept.getShortLabel()+")";
					}
					heading2[i] = "<span title='"+title+"'>"+drf.getAbbv()+"</span>";
					alignment2[i] = "center";
					sorted2[i] = true;
					i++;
				}
			}
	
			// build webtables
			WebTable.setOrder(httpSession, "classrooms.ord", request
					.getParameter("classOrd"), 1);
			WebTable.setOrder(httpSession, "additionalRooms.ord", request
					.getParameter("addOrd"), 1);
			WebTable.setOrder(httpSession, "specialRooms.ord", request
					.getParameter("speOrd"), 1);
			WebTable.setOrder(httpSession, "nonUniv.ord", request
					.getParameter("nonOrd"), 1);
			WebTable classRoomsTable = new WebTable(heading1.length, "Classrooms",
					"roomList.do?classOrd=%%", heading1, alignment1, sorted1);
			classRoomsTable.setRowStyle("white-space:nowrap");
			WebTable additionalRoomsTable = new WebTable(heading1.length,
					"Additional Instructional Rooms", "roomList.do?addOrd=%%",
					heading1, alignment1, sorted1);
			additionalRoomsTable.setRowStyle("white-space:nowrap");
			WebTable specialRoomsTable = new WebTable(heading1.length,
					"Special Use Rooms", "roomList.do?speOrd=%%", heading1,
					alignment1, sorted1);
			specialRoomsTable.setRowStyle("white-space:nowrap");
			WebTable nonUnivTable = new WebTable(heading2.length,
					"Non-University Locations", "roomList.do?nonOrd=%%",heading2,
					alignment2, sorted2);
			nonUnivTable.setRowStyle("white-space:nowrap");
			
			boolean timeVertical = RequiredTimeTable.getTimeGridVertical(user);
			boolean gridAsText = RequiredTimeTable.getTimeGridAsText(user);
			String timeGridSize = RequiredTimeTable.getTimeGridSize(user); 
	
			// build webtable rows
			int classRoomsSize = 0;
			int additonalRoomsSize = 0;
			int specialRoomsSize = 0;
			int nonUnivSize = 0;
			
			Department dept = new Department();
			if (!roomListForm.getDeptCodeX().equalsIgnoreCase("All")) {
				dept = Department.findByDeptCode(roomListForm.getDeptCodeX(), sessionId);
			} else {
				dept = null;
			}
			
			for (Iterator iter = rooms.iterator(); iter.hasNext();) {
				Location location = (Location) iter.next();
				
				boolean editable = false;
				for (Iterator x=location.getRoomDepts().iterator();!editable && x.hasNext();) {
					RoomDept rd = (RoomDept)x.next();
					if (ownerDepts.contains(rd.getDepartment())) {
						editable = true;
						break;
					}
				}
				if (isAdmin) editable = true;
				
				Room room = (location instanceof Room ? (Room)location : null);
				Building bldg = (room==null?null:room.getBuilding());
				DecimalFormat df5 = new DecimalFormat("####0");
				String text[] = new String[Math.max(heading1.length,heading2.length)];
				Comparable comp[] = new Comparable[text.length];
				int idx = 0;
				if (bldg!=null) {
					text[idx] = 
						(editable?"":"<font color='gray'>")+
						(location.isIgnoreRoomCheck().booleanValue()?"<i>":"")+
						bldg.getAbbreviation()+
						(location.isIgnoreRoomCheck().booleanValue()?"</i>":"")+
						(editable?"":"</font>");
					comp[0] = location.getLabel();
					idx++;
				}
				
				text[idx] = 
					(editable?"":"<font color='gray'>")+
					(location.isIgnoreRoomCheck().booleanValue()?"<i>":"")+
					(room==null?location.getLabel():room.getRoomNumber())+
					(location.isIgnoreRoomCheck().booleanValue()?"</i>":"")+
					(editable?"":"</font>");
				comp[idx] = location.getLabel();
				idx++;
				
				text[idx] = (editable?"":"<font color='gray'>")+df5.format(location.getCapacity())+(editable?"":"</font>");
				comp[idx] = new Long(location.getCapacity().intValue());
				idx++;
				
				if (periodPrefs) {
	                if (location.isExamEnabled()) {
	                    text[idx] = (editable?"":"<font color='gray'>")+df5.format(location.getExamCapacity())+(editable?"":"</font>");
	                    comp[idx] = location.getExamCapacity();
	                } else {
	                    text[idx] = "&nbsp;";
	                    comp[idx] = new Integer(0);
	                }
	                idx++;

	                if (location.isExamEnabled()) {
	                    if (gridAsText)
	                        text[idx] = location.getExamPreferencesAbbreviationHtml();
	                    else {
	                        PeriodPreferenceModel px = new PeriodPreferenceModel(location.getSession(), Exam.sExamTypeFinal);
	                        px.load(location);
	                        RequiredTimeTable rtt = new RequiredTimeTable(px);
	                        File imageFileName = null;
	                        try {
	                            imageFileName = rtt.createImage(timeVertical);
	                        } catch (IOException ex) {
	                            ex.printStackTrace();
	                        }
	                        String title = rtt.getModel().toString();
	                        if (imageFileName!=null)
	                            text[idx] = "<img border='0' src='temp/"+(imageFileName.getName())+"' title='"+title+"'>";
	                        else
	                            text[idx] = location.getExamPreferencesAbbreviationHtml();
	                    }
	                    comp[idx] = null;
	                } else {
	                    text[idx] = "";
                        comp[idx] = null;
	                }
                    idx++;
				} else {
	                PreferenceLevel roomPref = location.getRoomPreferenceLevel(dept);
	                if (editable && roomPref!=null && !PreferenceLevel.sNeutral.equals(roomPref.getPrefProlog())) {
	                    if (room==null) {
	                        text[0] =
	                            (location.isIgnoreRoomCheck().booleanValue()?"<i>":"")+
	                            "<span style='color:"+roomPref.prefcolor()+";font-weight:bold;' title='"+roomPref.getPrefName()+" "+location.getLabel()+"'>"+location.getLabel()+"</span>"+
	                            (location.isIgnoreRoomCheck().booleanValue()?"</i>":"");
	                    } else {
	                        text[0] = 
	                            (location.isIgnoreRoomCheck().booleanValue()?"<i>":"")+
	                            "<span style='color:"+roomPref.prefcolor()+";font-weight:bold;' title='"+roomPref.getPrefName()+" "+location.getLabel()+"'>"+(bldg==null?"":bldg.getAbbreviation())+"</span>"+
	                            (location.isIgnoreRoomCheck().booleanValue()?"</i>":"");
	                        text[1] = 
	                            (location.isIgnoreRoomCheck().booleanValue()?"<i>":"")+
	                            "<span style='color:"+roomPref.prefcolor()+";font-weight:bold;' title='"+roomPref.getPrefName()+" "+location.getLabel()+"'>"+room.getRoomNumber()+"</span>"+
	                            (location.isIgnoreRoomCheck().booleanValue()?"</i>":"");
	                    }
	                }

	                //ignore too far
	                if (location instanceof NonUniversityLocation) {
	                    boolean itf = (location.isIgnoreTooFar()==null?false:location.isIgnoreTooFar().booleanValue());
	                    text[idx] = (itf?"<IMG border='0' title='Ignore too far distances' alt='true' align='absmiddle' src='images/tick.gif'>":"&nbsp;");
	                    comp[idx] = new Integer(itf?1:0);
	                    idx++;
	                    boolean con = (location.isIgnoreRoomCheck()==null?true:location.isIgnoreRoomCheck().booleanValue());
	                    text[idx] = (con?"<IMG border='0' title='Create Constraint' alt='true' align='absmiddle' src='images/tick.gif'>":"&nbsp;");
	                    comp[idx] = new Integer(con?1:0);
	                    idx++;
	                }
	    
	                // get pattern column
	                RequiredTimeTable rtt = location.getRoomSharingTable();
	                rtt.getModel().setDefaultSelection(timeGridSize);
	                if (gridAsText) {
	                    text[idx] = rtt.getModel().toString().replaceAll(", ","<br>");;
	                } else {
	                    File imageFileName = null;
	                    try {
	                        imageFileName = rtt.createImage(timeVertical);
	                    } catch (IOException ex) {
	                        ex.printStackTrace();
	                    }
	                    if (imageFileName!=null){
	                        text[idx] = ("<img border='0' title='"+rtt.getModel().toString()+"' src='temp/"+(imageFileName.getName())+"'>&nbsp;");
	                    } else {
	                        text[idx] = rtt.getModel().toString().replaceAll(", ","<br>");;
	                    }
	                }
	                comp[idx]=null;
	                idx++;
	    
	                // get departments column
	                Department controlDept = null;
	                text[idx] = "";
	                Set rds = location.getRoomDepts();
	                Set departments = new HashSet();
	                for (Iterator iterRds = rds.iterator(); iterRds.hasNext();) {
	                    RoomDept rd = (RoomDept) iterRds.next();
	                    Department d = rd.getDepartment();
	                    if (rd.isControl().booleanValue()) controlDept = d;
	                    departments.add(d);
	                }
	                TreeSet ts = new TreeSet(new DepartmentNameComparator());
	                ts.addAll(departments);
	                for (Iterator it = ts.iterator(); it.hasNext();) {
	                    Department d = (Department) it.next();
	                    if (text[idx].length() > 0)
	                        text[idx] = text[idx] + "<br>";
	                    else
	                        comp[idx] = d.getDeptCode();
	                    text[idx] = text[idx] + d.htmlShortLabel(); 
	                }
	                idx++;
	                
	                //control column
	                if (!roomListForm.getDeptCodeX().equalsIgnoreCase("All") && !roomListForm.getDeptCodeX().equalsIgnoreCase("Exam")) {
	                    if (controlDept!=null && controlDept.getDeptCode().equals(roomListForm.getDeptCodeX())) {
	                        text[idx] = "<IMG border='0' title='Selected department is controlling this room.' alt='true' align='absmiddle' src='images/tick.gif'>";
	                        comp[idx] = new Integer(1);
	                    } else {
	                        text[idx] = "";
	                        comp[idx] = new Integer(0);
	                    }
	                } else {
	                    if (controlDept!=null) {
	                        text[idx] = controlDept.htmlShortLabel();
	                        comp[idx] = controlDept.getDeptCode();
	                    } else {
	                        text[idx] = "";
	                        comp[idx] = "";
	                    }
	                }
	                idx++;
				}
				
				text[0] += "<A name=\"A"+location.getUniqueId()+"\"></A>";
				
				// get groups column
				text[idx] = "";
                comp[idx] = "";
				for (Iterator it = new TreeSet(location.getRoomGroups()).iterator(); it.hasNext();) {
					RoomGroup rg = (RoomGroup) it.next();
					if (!rg.isGlobal().booleanValue() && !depts.contains(rg.getDepartment())) continue;
					if (!rg.isGlobal().booleanValue()) {
                        boolean skip = true;
                        for (Iterator j=location.getRoomDepts().iterator();j.hasNext();) {
                            RoomDept rd = (RoomDept)j.next();
                            if (rg.getDepartment().equals(rd.getDepartment())) { skip=false; break; }
                        }
                        if (skip) continue;
					}
                    if (text[idx].length()>0) text[idx] += "<br>";
                    comp[idx] = comp[idx] + rg.getName().trim();
					text[idx] += rg.htmlLabel();
				}
				idx++;
				
				if (featuresOneColumn) {
					// get features column
					text[idx] = "";
                    comp[idx] = "";
					for (Iterator it = new TreeSet(location.getGlobalRoomFeatures()).iterator(); it.hasNext();) {
						GlobalRoomFeature rf = (GlobalRoomFeature) it.next();
                        if (text[idx].length()>0) text[idx] += "<br>";
                        comp[idx] = comp[idx] + rf.getLabel().trim();
						text[idx] += rf.htmlLabel();
					}
					for (Iterator it = new TreeSet(location.getDepartmentRoomFeatures()).iterator(); it.hasNext();) {
						DepartmentRoomFeature drf = (DepartmentRoomFeature) it.next();
	                    boolean skip = true;
	                    for (Iterator j=location.getRoomDepts().iterator();j.hasNext();) {
	                        RoomDept rd = (RoomDept)j.next();
	                        if (drf.getDepartment().equals(rd.getDepartment())) { skip=false; break; }
	                    }
	                    if (skip) continue;
                        if (text[idx].length()>0) text[idx] += "<br>";
                        comp[idx] = comp[idx] + drf.getLabel().trim();
						text[idx] += drf.htmlLabel();
					}
					idx++;
				} else {
					// get features columns
					for (Iterator it = globalRoomFeatures.iterator(); it.hasNext();) {
					    GlobalRoomFeature grf = (GlobalRoomFeature) it.next();
						boolean b = location.hasFeature(grf);
						text[idx] = b ? "<IMG border='0' title='" + grf.getLabel() + "' alt='" + grf.getLabel() + "' align='absmiddle' src='images/tick.gif'>" : "&nbsp;";
						comp[idx] = "" + b;
						idx++;
					}
					for (Iterator it = deptRoomFeatures.iterator(); it.hasNext();) {
					    DepartmentRoomFeature drf = (DepartmentRoomFeature) it.next();
						boolean b = location.hasFeature(drf);
                        for (Iterator j=location.getRoomDepts().iterator();j.hasNext();) {
                            RoomDept rd = (RoomDept)j.next();
                            if (drf.getDepartment().equals(rd.getDepartment())) { b=false; break; }
                        }
						text[idx] = b ? "<IMG border='0' title='" + drf.getLabel() + "' alt='" + drf.getLabel() + "' align='absmiddle' src='images/tick.gif'>" : "&nbsp;";
						comp[idx] = "" + b;
						idx++;
					}
				}
				
				// build rows
				String roomType = (room==null?"":room.getScheduledRoomType().trim());
				if (roomType.equalsIgnoreCase("genClassroom")
						|| roomType.equalsIgnoreCase("computingLab")) {
					classRoomsTable.addLine(
						(editable?"onClick=\"document.location='roomDetail.do?id="+ room.getUniqueId() + "';\"":null),
						text, 
						comp);
					classRoomsSize++;
				} 
				if (roomType.equalsIgnoreCase("departmental")) {
					additionalRoomsTable.addLine(
						(editable?"onClick=\"document.location='roomDetail.do?id="+ room.getUniqueId() + "';\"":null),
						text, 
						comp);
					additonalRoomsSize++;
				} 
				if (roomType.equalsIgnoreCase("specialUse")) {
					specialRoomsTable.addLine(
						(editable?"onClick=\"document.location='roomDetail.do?id="+ room.getUniqueId() + "';\"":null),
						text, 
						comp);
					specialRoomsSize++;
				}
				if (location instanceof NonUniversityLocation) {
					nonUnivSize++;
					nonUnivTable.addLine(
					        (editable?"onClick=\"document.location='roomDetail.do?id="+ location.getUniqueId() + "';\"":null),
							text, 
							comp);
				}
			}
			Debug.debug("classRoomsSize: " + classRoomsSize);
			Debug.debug("additonalRoomsSize: " + additonalRoomsSize);
			Debug.debug("specialRoomsSize: " + specialRoomsSize);
	
			// set request attributes
			if (classRoomsSize != 0) {
				int ord = WebTable.getOrder(httpSession, "classrooms.ord");
				if (ord>heading1.length) ord = 0;
				request.setAttribute("classrooms", classRoomsTable.printTable(ord));
			}
			if (additonalRoomsSize != 0) {
				int ord = WebTable.getOrder(httpSession, "additionalRooms.ord");
				if (ord>heading1.length) ord = 0;
				request.setAttribute("additionalRooms", additionalRoomsTable.printTable(ord));
			}
			if (specialRoomsSize != 0) {
				int ord = WebTable.getOrder(httpSession, "specialRooms.ord");
				if (ord>heading1.length) ord = 0;
				request.setAttribute("specialRooms", specialRoomsTable.printTable(ord));
			}
			
			if (nonUnivSize>0) {
				int ord = WebTable.getOrder(httpSession, "nonUniv.ord");
				if (ord>heading2.length) ord = 0;
				request.setAttribute("nonUnivLocation", nonUnivTable.printTable(ord));
			}
			
			request.setAttribute("colspan", ""+colspan);
		}
	}
	
	public static void buildPdfWebTable(HttpServletRequest request, RoomListForm roomListForm, boolean featuresOneColumn, boolean periodPrefs) throws Exception {
    	FileOutputStream out = null;
    	try {
    		File file = ApplicationProperties.getTempFile("rooms", "pdf");
    		
    		out = new FileOutputStream(file);
    		HttpSession httpSession = request.getSession();
		
    		Collection rooms = roomListForm.getRooms();
			User user = Web.getUser(httpSession);
			Long sessionId = Session.getCurrentAcadSession(user).getSessionId();
			ArrayList globalRoomFeatures = new ArrayList();
			Set deptRoomFeatures = new TreeSet();
			int colspan=0;
			
			String mgrId = (String)user.getAttribute(Constants.TMTBL_MGR_ID_ATTR_NAME);
			TimetableManagerDAO tdao = new TimetableManagerDAO();
	        TimetableManager owner = tdao.get(new Long(mgrId));
	        boolean isAdmin = user.getRole().equals(Roles.ADMIN_ROLE);
			Set ownerDepts = owner.departmentsForSession(sessionId);
			Set externalDepartments = Department.findAllExternal(sessionId);
			Set depts = null;
			if (roomListForm.getDeptCodeX().equalsIgnoreCase("All")) {
				if (isAdmin) {
					depts = Department.findAll(sessionId);
				} else {
					depts = Department.findAllOwned(sessionId, owner, false);
				}
			} else if (roomListForm.getDeptCodeX().equalsIgnoreCase("Exam")) {
			    depts = new HashSet();
			} else {
				depts = new HashSet(1);
				depts.add(Department.findByDeptCode(roomListForm.getDeptCodeX(),sessionId));
			}
			
			org.hibernate.Session hibSession = null;
			try {
				
				RoomFeatureDAO d = new RoomFeatureDAO();
				hibSession = d.getSession();
				
				List list = hibSession
							.createCriteria(GlobalRoomFeature.class)
							.addOrder(Order.asc("label"))
							.list();
				
				for (Iterator iter = list.iterator();iter.hasNext();) {
					GlobalRoomFeature rf = (GlobalRoomFeature) iter.next();
					globalRoomFeatures.add(rf);
				}
				
				if (roomListForm.getDeptCodeX().equalsIgnoreCase("All")) {
					String[] deptCodes = Department.getDeptCodesForUser(user, false);
					if (deptCodes!=null)
						deptRoomFeatures.addAll(hibSession.createQuery(
								"select distinct f from DepartmentRoomFeature f inner join f.department d " +
								"where d.session.uniqueId=:sessionId and d.deptCode in ("+
								Constants.arrayToStr(deptCodes, "'", ", ")+") order by f.label").
								setLong("sessionId",sessionId.longValue()).
								list());
					else
						deptRoomFeatures.addAll(hibSession.createQuery(
								"select distinct f from DepartmentRoomFeature f inner join f.department d where " +
								"d.session.uniqueId=:sessionId order by f.label").
								setLong("sessionId",sessionId.longValue()).
								list());
				} else if (roomListForm.getDeptCodeX().equalsIgnoreCase("Exam")) {
				} else {
					deptRoomFeatures.addAll(hibSession.
						createQuery(
								"select distinct f from DepartmentRoomFeature f inner join f.department d " +
								"where d.session.uniqueId=:sessionId and d.deptCode = :deptCode order by f.label").
						setLong("sessionId",sessionId.longValue()).
						setString("deptCode",roomListForm.getDeptCodeX()).list());
				}
				
			} catch (Exception e) {
				Debug.error(e);
			}
	
			//build headings for university rooms
			String fixedHeading1[][] =
			    (periodPrefs?
			            (featuresOneColumn? new String[][]
			                                             { { "Bldg", "left", "true" },
			                                             { "Room", "left", "true" },
			                                             { "Capacity", "right", "false" },
			                                             { "Exam Capacity", "right", "false" },
			                                             { "Period Preferences", "center", "true" },
			                                             { "Groups", "left", "true" },
			                                             { "Features", "left", "true" } } 
			                                         : new String[][]
			                                             { { "Bldg", "left", "true" },
			                                             { "Room", "left", "true" },
			                                             { "Capacity", "right", "false" },
			                                             { "Exam Capacity", "right", "false" },
			                                             { "Period Preferences", "center", "true" },
			                                             { "Groups", "left", "true" } })			    
                :
                    (featuresOneColumn? new String[][]
                                                     { { "Bldg", "left", "true" },
                                                     { "Room", "left", "true" },
                                                     { "Capacity", "right", "false" },
                                                     { "Availability", "left", "true" },
                                                     { "Departments", "left", "true" },
                                                     { "Control", "left", "true" },
                                                     { "Groups", "left", "true" },
                                                     { "Features", "left", "true" } } 
                                                 : new String[][]
                                                     { { "Bldg", "left", "true" },
                                                     { "Room", "left", "true" },
                                                     { "Capacity", "right", "false" },
                                                     { "Availability", "left", "true" },
                                                     { "Departments", "left", "true" },
                                                     { "Control", "left", "true" },
                                                     { "Groups", "left", "true" } }));                   
	
			String heading1[] = new String[fixedHeading1.length
					+ globalRoomFeatures.size() + deptRoomFeatures.size()];
			String alignment1[] = new String[heading1.length];
			boolean sorted1[] = new boolean[heading1.length];
			
			for (int i = 0; i < fixedHeading1.length; i++) {
				heading1[i] = fixedHeading1[i][0];
				alignment1[i] = fixedHeading1[i][1];
				sorted1[i] = (Boolean.valueOf(fixedHeading1[i][2])).booleanValue();
			}
			colspan = fixedHeading1.length;
			
			if (!featuresOneColumn) {
				int i = fixedHeading1.length;
				for (Iterator it = globalRoomFeatures.iterator(); it.hasNext();) {
					heading1[i] = ((GlobalRoomFeature) it.next()).getLabel();
					heading1[i] = heading1[i].replaceFirst(" ", "\n");
					alignment1[i] = "center";
					sorted1[i] = true;
					i++;
				}
				for (Iterator it = deptRoomFeatures.iterator(); it.hasNext();) {
					DepartmentRoomFeature drf = (DepartmentRoomFeature)it.next();
					heading1[i] = drf.getLabel();
					heading1[i] = heading1[i].replaceFirst(" ", "\n");
					if (roomListForm.getDeptCodeX().equalsIgnoreCase("All")) {
						Department dept = drf.getDepartment();
						heading1[i]+=" ("+dept.getShortLabel()+")";
					}
					alignment1[i] = "center";
					sorted1[i] = true;
					i++;
				}
			    colspan = i;
			}			
			
			//build headings for non-univ locations
			String fixedHeading2[][] = 
			    (periodPrefs ?
		                ( featuresOneColumn ? new String[][]
		                                                   {{ "Location", "left", "true" },
		                                                   { "Capacity", "right", "false" },
		                                                   { "Exam Capacity", "right", "false" },
		                                                   { "Period Preferences", "center", "true" },
		                                                   { "Groups", "left", "true" },
		                                                   { "Features", "left", "true" }}
		                                           : new String[][]
		                                                   {{ "Location", "left", "true" },
		                                                   { "Capacity", "right", "false" },
		                                                   { "Period Preferences", "center", "true" },
		                                                   { "Groups", "left", "true" } })
			    :
	                ( featuresOneColumn ? new String[][]
	                                                   {{ "Location", "left", "true" },
	                                                   { "Capacity", "right", "false" },
	                                                   { "IgnTooFar", "center", "true" },
	                                                   { "IgnChecks", "center", "true" },
	                                                   { "Availability", "left", "true" },
	                                                   { "Departments", "left", "true" },
	                                                   { "Control", "left", "true" },
	                                                   { "Groups", "left", "true" },
	                                                   { "Features", "left", "true" }}
	                                           : new String[][]
	                                                   {{ "Location", "left", "true" },
	                                                   { "Capacity", "right", "false" },
	                                                   { "Exam Capacity", "right", "false" },
	                                                   { "IgnTooFar", "center", "true" },
	                                                   { "IgnChecks", "center", "true" },
	                                                   { "Availability", "left", "true" },
	                                                   { "Departments", "left", "true" },
	                                                   { "Control", "left", "true" },
	                                                   { "Groups", "left", "true" } })
			    );
			
			String heading2[] = new String[fixedHeading2.length
			        + globalRoomFeatures.size() + deptRoomFeatures.size()];
			String alignment2[] = new String[heading2.length];
			boolean sorted2[] = new boolean[heading2.length];
			
			for (int i = 0; i < fixedHeading2.length; i++) {
				heading2[i] = fixedHeading2[i][0];
				alignment2[i] = fixedHeading2[i][1];
				sorted2[i] = (Boolean.valueOf(fixedHeading2[i][2])).booleanValue();
			}

			if (!featuresOneColumn) {
				int i = fixedHeading2.length;
				for (Iterator it = globalRoomFeatures.iterator(); it.hasNext();) {
					heading2[i] = ((GlobalRoomFeature) it.next()).getLabel();
					heading2[i] = heading2[i].replaceFirst(" ", "\n");
					alignment2[i] = "center";
					sorted2[i] = true;
					i++;
				}
				for (Iterator it = deptRoomFeatures.iterator(); it.hasNext();) {
					DepartmentRoomFeature drf = (DepartmentRoomFeature) it.next(); 
					heading2[i] = drf.getLabel();
					heading2[i] = heading2[i].replaceFirst(" ", "\n");
					if (roomListForm.getDeptCodeX().equalsIgnoreCase("All")) {
						Department dept = Department.findByDeptCode(drf.getDeptCode(),sessionId);
						heading2[i]+=" ("+dept.getShortLabel()+")";
					}
					alignment2[i] = "center";
					sorted2[i] = true;
					i++;
				}
			}
			
			// build webtables
			PdfWebTable classRoomsTable = new PdfWebTable(heading1.length, "Classrooms", null, heading1, alignment1, sorted1);
			PdfWebTable additionalRoomsTable = new PdfWebTable(heading1.length, "Additional Instructional Rooms", null, heading1, alignment1, sorted1);
			PdfWebTable specialRoomsTable = new PdfWebTable(heading1.length, "Special Use Rooms", null, heading1, alignment1, sorted1);
			PdfWebTable nonUnivTable = new PdfWebTable(heading2.length, "Non-University Locations", null, heading2, alignment2, sorted2);
			
			boolean timeVertical = RequiredTimeTable.getTimeGridVertical(user);
			boolean gridAsText = RequiredTimeTable.getTimeGridAsText(user);
			String timeGridSize = RequiredTimeTable.getTimeGridSize(user); 
	
			// build webtable rows
			int classRoomsSize = 0;
			int additonalRoomsSize = 0;
			int specialRoomsSize = 0;
			int nonUnivSize = 0;
			
			Department dept = new Department();
			if (!roomListForm.getDeptCodeX().equalsIgnoreCase("All")) {
				dept = Department.findByDeptCode(roomListForm.getDeptCodeX(), sessionId);
			} else {
				dept = null;
			}
			
			for (Iterator iter = rooms.iterator(); iter.hasNext();) {
				Location location = (Location) iter.next();
				
				boolean editable = false;
				for (Iterator x=location.getRoomDepts().iterator();!editable && x.hasNext();) {
					RoomDept rd = (RoomDept)x.next();
					if (ownerDepts.contains(rd.getDepartment())) {
						editable = true;
						break;
					}
				}
				if (isAdmin) editable = true;

				Room room = (location instanceof Room ? (Room)location : null);
				Building bldg = (room==null?null:room.getBuilding());
				
				PdfWebTable table = null;
				String roomType = (room==null?"":room.getScheduledRoomType().trim());
				if (roomType.equalsIgnoreCase("genClassroom") || roomType.equalsIgnoreCase("computingLab")) {
					table = classRoomsTable;
				} 
				if (roomType.equalsIgnoreCase("departmental")) {
					table = additionalRoomsTable;
				} 
				if (roomType.equalsIgnoreCase("specialUse")) {
					table = specialRoomsTable;
				}
				if (location instanceof NonUniversityLocation) {
					table = nonUnivTable;
				}				
				
				DecimalFormat df5 = new DecimalFormat("####0");
                String text[] = new String[Math.max(heading1.length,heading2.length)];
                Comparable comp[] = new Comparable[text.length];
				int idx = 0;
				if (bldg!=null) {
					text[idx] = 
						(location.isIgnoreRoomCheck().booleanValue()?"@@ITALIC ":"")+
						bldg.getAbbreviation()+
						(location.isIgnoreRoomCheck().booleanValue()?"@@END_ITALIC ":"");
					comp[0] = location.getLabel();
					idx++;
				}
				
				text[idx] = 
					(location.isIgnoreRoomCheck().booleanValue()?"@@ITALIC ":"")+
					(room==null?location.getLabel():room.getRoomNumber())+
					(location.isIgnoreRoomCheck().booleanValue()?"@@END_ITALIC ":"");
				comp[idx] = location.getLabel();
				idx++;
				
				text[idx] = df5.format(location.getCapacity());
				comp[idx] = new Long(location.getCapacity().intValue());
				idx++;
				
				if (periodPrefs) {
	                if (location.isExamEnabled()) {
	                    text[idx] = df5.format(location.getExamCapacity());
	                    comp[idx] = location.getExamCapacity();
	                } else {
	                    text[idx] = "";
	                    comp[idx] = new Integer(0);
	                }
	                idx++;

	                if (location.isExamEnabled()) {
	                    text[idx] = location.getExamPreferencesAbbreviation();
	                    comp[idx] = null;
	                } else {
	                    text[idx] = "";
	                    comp[idx] = null;
	                }
	                idx++;
				} else {
	                PreferenceLevel roomPref = location.getRoomPreferenceLevel(dept);
	                if (editable && roomPref!=null && !PreferenceLevel.sNeutral.equals(roomPref.getPrefProlog())) {
	                    if (room==null) {
	                        text[0] =
	                            (location.isIgnoreRoomCheck().booleanValue()?"@@ITALIC ":"")+
	                            location.getLabel()+" ("+PreferenceLevel.prolog2abbv(roomPref.getPrefProlog())+")"+
	                            (location.isIgnoreRoomCheck().booleanValue()?"@@END_ITALIC ":"");
	                    } else {
	                        text[0] = 
	                            (location.isIgnoreRoomCheck().booleanValue()?"@@ITALIC ":"")+
	                            (bldg==null?"":bldg.getAbbreviation())+
	                            (location.isIgnoreRoomCheck().booleanValue()?"@@END_ITALIC ":"");
	                        text[1] = 
	                            (location.isIgnoreRoomCheck().booleanValue()?"@@ITALIC ":"")+
	                            room.getRoomNumber()+" ("+PreferenceLevel.prolog2abbv(roomPref.getPrefProlog())+")"+
	                            (location.isIgnoreRoomCheck().booleanValue()?"@@END_ITALIC ":"");
	                    }
	                }
	                
	                //ignore too far
	                if (location instanceof NonUniversityLocation) {
	                    boolean itf = (location.isIgnoreTooFar()==null?false:location.isIgnoreTooFar().booleanValue());
	                    text[idx] = (itf?"Yes":"No");
	                    comp[idx] = new Integer(itf?1:0);
	                    idx++;
	                    boolean con = (location.isIgnoreRoomCheck()==null?true:location.isIgnoreRoomCheck().booleanValue());
	                    text[idx] = (con?"YES":"No");
	                    comp[idx] = new Integer(con?1:0);
	                    idx++;
	                }
	    
	                // get pattern column
	                RequiredTimeTable rtt = location.getRoomSharingTable();
	                if (gridAsText) {
	                    text[idx] = rtt.getModel().toString().replaceAll(", ","\n");
	                } else {
	                    rtt.getModel().setDefaultSelection(timeGridSize);
	                    Image image = rtt.createBufferedImage(timeVertical);
	                    if (image!=null){
	                        table.addImage(location.getUniqueId().toString(), image);
	                        text[idx] = ("@@IMAGE "+location.getUniqueId().toString()+" ");
	                    } else {
	                        text[idx] = rtt.getModel().toString().replaceAll(", ","\n");
	                    }
	                }
	                comp[idx]=null;
	                idx++;
	    
	                // get departments column
	                Department controlDept = null;
	                text[idx] = "";
	                Set rds = location.getRoomDepts();
	                Set departments = new HashSet();
	                for (Iterator iterRds = rds.iterator(); iterRds.hasNext();) {
	                    RoomDept rd = (RoomDept) iterRds.next();
	                    Department d = rd.getDepartment();
	                    if (rd.isControl().booleanValue()) controlDept = d;
	                    departments.add(d);
	                }
	                TreeSet ts = new TreeSet(new DepartmentNameComparator());
	                ts.addAll(departments);
	                for (Iterator it = ts.iterator(); it.hasNext();) {
	                    Department d = (Department) it.next();
	                    if (text[idx].length() > 0)
	                        text[idx] = text[idx] + "\n";
	                    else
	                        comp[idx] = d.getDeptCode();
	                    text[idx] = text[idx] + "@@COLOR "+d.getRoomSharingColor(null)+" "+d.getShortLabel(); 
	                }
	                idx++;
	                
	                //control column
	                if (!roomListForm.getDeptCodeX().equalsIgnoreCase("All") && !roomListForm.getDeptCodeX().equalsIgnoreCase("Exam")) {
	                    if (controlDept!=null && controlDept.getDeptCode().equals(roomListForm.getDeptCodeX())) {
	                        text[idx] = "Yes";
	                        comp[idx] = new Integer(1);
	                    } else {
	                        text[idx] = "No";
	                        comp[idx] = new Integer(0);
	                    }
	                } else {
	                    if (controlDept!=null) {
	                        text[idx] = "@@COLOR "+controlDept.getRoomSharingColor(null)+" "+controlDept.getShortLabel();
	                        comp[idx] = controlDept.getDeptCode();
	                    } else {
	                        text[idx] = "";
	                        comp[idx] = "";
	                    }
	                }
	                idx++;
				}
				
				// get groups column
				text[idx] = "";
				for (Iterator it = new TreeSet(location.getRoomGroups()).iterator(); it.hasNext();) {
					RoomGroup rg = (RoomGroup) it.next();
					if (!rg.isGlobal().booleanValue() && !depts.contains(rg.getDepartment())) continue;
                    if (!rg.isGlobal().booleanValue()) {
                        boolean skip = true;
                        for (Iterator j=location.getRoomDepts().iterator();j.hasNext();) {
                            RoomDept rd = (RoomDept)j.next();
                            if (rg.getDepartment().equals(rd.getDepartment())) { skip=false; break; }
                        }
                        if (skip) continue;
                    }
					if (text[idx].length()>0) text[idx] += "\n";
                    comp[idx] = comp[idx] + rg.getName().trim();
					text[idx] += (rg.isGlobal().booleanValue()?"":"@@COLOR "+rg.getDepartment().getRoomSharingColor(null)+" ")+rg.getName(); 
				}
				idx++;
				
				if (featuresOneColumn) {
					// get features column
					text[idx] = "";
					for (Iterator it = new TreeSet(location.getGlobalRoomFeatures()).iterator(); it.hasNext();) {
						GlobalRoomFeature rf = (GlobalRoomFeature) it.next();
                        if (text[idx].length()>0) text[idx] += "\n";
                        comp[idx] = comp[idx] + rf.getLabel().trim();
						text[idx] += rf.getLabel();
					}
					for (Iterator it = new TreeSet(location.getDepartmentRoomFeatures()).iterator(); it.hasNext();) {
						DepartmentRoomFeature drf = (DepartmentRoomFeature) it.next();
                        boolean skip = true;
                        for (Iterator j=location.getRoomDepts().iterator();j.hasNext();) {
                            RoomDept rd = (RoomDept)j.next();
                            if (drf.getDepartment().equals(rd.getDepartment())) { skip=false; break; }
                        }
                        if (skip) continue;
                        if (text[idx].length()>0) text[idx] += "\n";
                        comp[idx] = comp[idx] + drf.getLabel().trim();
						text[idx] +="@@COLOR "+drf.getDepartment().getRoomSharingColor(null)+" "+drf.getLabel();
					}
					idx++;
				} else {
					// get features columns
					for (Iterator it = globalRoomFeatures.iterator(); it.hasNext();) {
					    GlobalRoomFeature grf = (GlobalRoomFeature) it.next();
						boolean b = location.hasFeature(grf);
						text[idx] = b ? "Yes" : "No";
						comp[idx] = "" + b;
						idx++;
					}
					for (Iterator it = deptRoomFeatures.iterator(); it.hasNext();) {
					    DepartmentRoomFeature drf = (DepartmentRoomFeature) it.next();
                        boolean b = location.hasFeature(drf);
                        for (Iterator j=location.getRoomDepts().iterator();j.hasNext();) {
                            RoomDept rd = (RoomDept)j.next();
                            if (drf.getDepartment().equals(rd.getDepartment())) { b=false; break; }
                        }
						text[idx] = b ? "Yes" : "No";
						comp[idx] = "" + b;
						idx++;
					}
				}
				
				// build rows
				if (roomType.equalsIgnoreCase("genClassroom") || roomType.equalsIgnoreCase("computingLab")) {
					classRoomsTable.addLine(
						(editable?"onClick=\"document.location='roomDetail.do?id="+ room.getUniqueId() + "';\"":null),
						text, 
						comp);
					classRoomsSize++;
				} 
				if (roomType.equalsIgnoreCase("departmental")) {
					additionalRoomsTable.addLine(
						(editable?"onClick=\"document.location='roomDetail.do?id="+ room.getUniqueId() + "';\"":null),
						text, 
						comp);
					additonalRoomsSize++;
				} 
				if (roomType.equalsIgnoreCase("specialUse")) {
					specialRoomsTable.addLine(
						(editable?"onClick=\"document.location='roomDetail.do?id="+ room.getUniqueId() + "';\"":null),
						text, 
						comp);
					specialRoomsSize++;
				}
				if (location instanceof NonUniversityLocation) {
					nonUnivSize++;
					nonUnivTable.addLine(
					        (editable?"onClick=\"document.location='roomDetail.do?id="+ location.getUniqueId() + "';\"":null),
							text, 
							comp);
				}
			}
	
			Document doc = null;
			// set request attributes
			if (classRoomsSize != 0) {
				PdfWebTable table = classRoomsTable;
                int ord = WebTable.getOrder(httpSession, "classrooms.ord");
                if (ord>heading1.length) ord = 0;
    			PdfPTable pdfTable = table.printPdfTable(ord);
    			if (doc==null) {
    				doc = new Document(new Rectangle(60f + table.getWidth(), 60f + 0.75f * table.getWidth()),30,30,30,30);
    				PdfWriter iWriter = PdfWriter.getInstance(doc, out);
    				iWriter.setPageEvent(new PdfEventHandler());
    				doc.open();
    			} else {
    				doc.setPageSize(new Rectangle(60f + table.getWidth(), 60f + 0.75f * table.getWidth()));
    				doc.newPage();
    			}
    			doc.add(new Paragraph(table.getName(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16)));
    			doc.add(pdfTable);
			}
			if (additonalRoomsSize != 0) {
				PdfWebTable table = additionalRoomsTable;
                int ord = WebTable.getOrder(httpSession, "additionalRooms.ord");
                if (ord>heading1.length) ord = 0;
    			PdfPTable pdfTable = table.printPdfTable(ord);
    			if (doc==null) {
    				doc = new Document(new Rectangle(60f + table.getWidth(), 60f + 0.75f * table.getWidth()),30,30,30,30);
    				PdfWriter iWriter = PdfWriter.getInstance(doc, out);
    				iWriter.setPageEvent(new PdfEventHandler());
    				doc.open();
    			} else {
    				doc.setPageSize(new Rectangle(60f + table.getWidth(), 60f + 0.75f * table.getWidth()));
    				doc.newPage();
    			}
    			doc.add(new Paragraph(table.getName(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16)));
    			doc.add(pdfTable);
			}
			if (specialRoomsSize != 0) {
				PdfWebTable table = specialRoomsTable;
                int ord = WebTable.getOrder(httpSession, "specialRooms.ord");
                if (ord>heading1.length) ord = 0;
    			PdfPTable pdfTable = table.printPdfTable(ord);
    			if (doc==null) {
    				doc = new Document(new Rectangle(60f + table.getWidth(), 60f + 0.75f * table.getWidth()),30,30,30,30);
    				PdfWriter iWriter = PdfWriter.getInstance(doc, out);
    				iWriter.setPageEvent(new PdfEventHandler());
    				doc.open();
    			} else {
    				doc.setPageSize(new Rectangle(60f + table.getWidth(), 60f + 0.75f * table.getWidth()));
    				doc.newPage();
    			}
    			doc.add(new Paragraph(table.getName(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16)));
    			doc.add(pdfTable);
			}
			
			if (nonUnivSize>0) {
				PdfWebTable table = nonUnivTable;
                int ord = WebTable.getOrder(httpSession, "nonUniv.ord");
                if (ord>heading2.length) ord = 0;
    			PdfPTable pdfTable = table.printPdfTable(ord);
    			if (doc==null) {
    				doc = new Document(new Rectangle(60f + table.getWidth(), 60f + 0.75f * table.getWidth()),30,30,30,30);
    				PdfWriter iWriter = PdfWriter.getInstance(doc, out);
    				iWriter.setPageEvent(new PdfEventHandler());
    				doc.open();
    			} else {
    				doc.setPageSize(new Rectangle(60f + table.getWidth(), 60f + 0.75f * table.getWidth()));
    				doc.newPage();
    			}
    			doc.add(new Paragraph(table.getName(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16)));
    			doc.add(pdfTable);
			}
			
			if (doc==null) return;
			
    		doc.close();

    		request.setAttribute(Constants.REQUEST_OPEN_URL, "temp/"+file.getName());
    	} catch (Exception e) {
    		e.printStackTrace();
    	} finally {
    		try {
    			if (out!=null) out.close();
    		} catch (IOException e) {}
    	}
	}
	
}