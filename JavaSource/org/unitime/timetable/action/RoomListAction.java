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

import java.awt.Image;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.cpsolver.ifs.util.CSVFile;
import org.cpsolver.ifs.util.CSVFile.CSVField;
import org.cpsolver.ifs.util.CSVFile.CSVLine;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unitime.commons.Debug;
import org.unitime.commons.web.WebTable;
import org.unitime.commons.web.WebTable.WebTableLine;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.defaults.CommonValues;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.form.RoomListForm;
import org.unitime.timetable.model.Building;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentRoomFeature;
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.MidtermPeriodPreferenceModel;
import org.unitime.timetable.model.GlobalRoomFeature;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.NonUniversityLocation;
import org.unitime.timetable.model.PeriodPreferenceModel;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.Room;
import org.unitime.timetable.model.RoomDept;
import org.unitime.timetable.model.RoomGroup;
import org.unitime.timetable.model.RoomType;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.comparators.DepartmentNameComparator;
import org.unitime.timetable.model.dao.ExamTypeDAO;
import org.unitime.timetable.model.dao.LocationDAO;
import org.unitime.timetable.model.dao.RoomFeatureDAO;
import org.unitime.timetable.model.dao.RoomTypeDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.Qualifiable;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.ExportUtils;
import org.unitime.timetable.util.LookupTables;
import org.unitime.timetable.util.PdfEventHandler;
import org.unitime.timetable.util.PdfFont;
import org.unitime.timetable.webutil.BackTracker;
import org.unitime.timetable.webutil.Navigation;
import org.unitime.timetable.webutil.PdfWebTable;
import org.unitime.timetable.webutil.RequiredTimeTable;

import com.lowagie.text.Document;
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
 *
 * @author Tomas Muller
 */
@Service("/roomList")
public class RoomListAction extends Action {
	public static final CourseMessages MSG = Localization.create(CourseMessages.class);
	
	@Autowired SessionContext sessionContext;

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
		
		sessionContext.checkPermission(Right.Rooms);
		
		RoomListForm roomListForm = (RoomListForm) form;
		roomListForm.save(request.getSession());
		ActionMessages errors = new ActionMessages();
		
		//get deptCode from request - for user with only one department
		String dept = (String)request.getAttribute("deptCode");
		if (dept != null) {
			roomListForm.setDeptCodeX(dept);
		}
		
		if (sessionContext.getAttribute(SessionAttribute.DepartmentCodeRoom) != null && roomListForm.getDeptCodeX() == null)
			roomListForm.setDeptCodeX((String)sessionContext.getAttribute(SessionAttribute.DepartmentCodeRoom));

		// Set Session Variable
		if (roomListForm.getDeptCodeX() != null && !roomListForm.getDeptCodeX().isEmpty()) {
			sessionContext.setAttribute(SessionAttribute.DepartmentCodeRoom, roomListForm.getDeptCodeX());
		}
		
		//set request attribute for department
		LookupTables.setupDepartments(request, sessionContext, true);
		LookupTables.setupExamTypes(request, sessionContext.getUser().getCurrentAcademicSessionId());

		// Validate input
		errors = roomListForm.validate(mapping, request);

		// Validation fails
		if (errors.size() > 0) {
			saveErrors(request, errors);
			return mapping.findForward("showRoomSearch");
		}
		
		lookupRooms(roomListForm, request.getParameter("op"));
		
		if (roomListForm.getRooms() == null || roomListForm.getRooms().isEmpty()) {
			errors.add("searchResult", new ActionMessage("errors.generic", "No room matching the above criteria was found."));
			request.setAttribute("colspan","7"); 
			saveErrors(request, errors);
			return mapping.findForward("showRoomSearch");
		}
		
		ExamType examType = null;
		if (roomListForm.getDeptCodeX() != null && roomListForm.getDeptCodeX().matches("Exam[0-9]*"))
			examType = ExamTypeDAO.getInstance().get(Long.valueOf(roomListForm.getDeptCodeX().substring(4)));
		
		if ("Export PDF".equals(request.getParameter("op"))) {
			OutputStream out = ExportUtils.getPdfOutputStream(response, "rooms");
			buildPdfWebTable(out, roomListForm, CommonValues.Yes.eq(UserProperty.RoomFeaturesInOneColumn.get(sessionContext.getUser())), examType);
			out.flush(); out.close();
			return null;
		}
		
		if ("Export CSV".equals(request.getParameter("op"))) {
			PrintWriter pw = ExportUtils.getCsvWriter(response, "rooms");
			buildCsvWebTable(pw, roomListForm, CommonValues.Yes.eq(UserProperty.RoomFeaturesInOneColumn.get(sessionContext.getUser())), examType);
			pw.flush(); pw.close();
			return null;
		}
		
		// build web table for university locations
		buildWebTable(request, roomListForm, CommonValues.Yes.eq(UserProperty.RoomFeaturesInOneColumn.get(sessionContext.getUser())), examType);
		
		return mapping.findForward("showRoomList");

	}
	
	public void lookupRooms(RoomListForm form, String op) {
		String from = "Location l" +
				" left join l.roomDepts rd" +
				" left join fetch l.examTypes xt" +
				" left join fetch l.features f" +
				" left join fetch l.roomGroups g" +
				" left join fetch l.roomType t";
		String where = "l.session.uniqueId = :sessionId";
		
		List<Long> departmentIds = null;
		Long examTypeId = null;

		if (form.getDeptCodeX().equalsIgnoreCase("All")) {
			if (sessionContext.getUser().getCurrentAuthority().hasRight(Right.DepartmentIndependent)) {
			} else {
				departmentIds = new ArrayList<Long>();
				String depts = "";
				for (Department department: Department.getUserDepartments(sessionContext.getUser())) {
					depts += (depts.isEmpty() ? "" : ",") + ":dept" + (departmentIds.size());
					departmentIds.add(department.getUniqueId());
				}
				where += " and (rd.department.uniqueId in (" + depts + ") or l.eventDepartment.uniqueId in (" + depts + "))";

			}
		} else if (form.getDeptCodeX().equals("Exam")) {
			TreeSet<ExamType> types = ExamType.findAllUsed(sessionContext.getUser().getCurrentAcademicSessionId());
			if (!types.isEmpty()) {
				examTypeId = types.first().getUniqueId();
				form.setDeptCodeX("Exam" + examTypeId);
				where += " and xt.uniqueId = :examTypeId";
			}
		} else if (form.getDeptCodeX().matches("Exam[0-9]*")) {
			// from = "Location l inner join l.examTypes xt";
			examTypeId = Long.valueOf(form.getDeptCodeX().substring(4));
			where += " and xt.uniqueId = :examTypeId";
		} else {
			Department department = Department.findByDeptCode(form.getDeptCodeX(), sessionContext.getUser().getCurrentAcademicSessionId());
			if (department != null) {
				if ("Export PDF".equals(op)) {
					sessionContext.checkPermission(department, Right.RoomsExportPdf);
				} else if ("Export CSV".equals(op)) {
					sessionContext.checkPermission(department, Right.RoomsExportCsv);
				} else {
					sessionContext.checkPermission(department, Right.Rooms);
				}
				where += " and (rd.department.uniqueId = :dept0 or l.eventDepartment.uniqueId = :dept0)";
				departmentIds = new ArrayList<Long>(); departmentIds.add(department.getUniqueId());
			} else return;
		}
		
		Integer minSize = null, maxSize = null;
		if (form.getMinRoomSize() != null && !form.getMinRoomSize().isEmpty()) {
			try {
				minSize = Integer.valueOf(form.getMinRoomSize());
				where += " and l.capacity >= :minSize";
			} catch (NumberFormatException e) {
			}
		}
		if (form.getMaxRoomSize() != null && !form.getMaxRoomSize().isEmpty()) {
			try {
				maxSize = Integer.valueOf(form.getMaxRoomSize());
				where += " and l.capacity <= :maxSize";
			} catch (NumberFormatException e) {
			}
		}
		
		List<Long> roomTypes = null;
		if (form.getRoomTypes() != null && form.getRoomTypes().length > 0) {
			String types = "";
			roomTypes = new ArrayList<Long>();
			for (int i = 0; i < form.getRoomTypes().length; i++) {
				types += (types.isEmpty() ? "" : ",") + ":type" + i;
				roomTypes.add(form.getRoomTypes()[i]);
			}
			where += " and l.roomType.uniqueId in (" + types + ")";
		}
		
		List<Long> roomGroups = null;
		if (form.getRoomGroups() != null && form.getRoomGroups().length > 0) {
			String groups = "";
			roomGroups = new ArrayList<Long>();
			for (int i = 0; i < form.getRoomGroups().length; i++) {
				groups += (groups.isEmpty() ? "" : ",") + ":group" + i;
				roomGroups.add(form.getRoomGroups()[i]);
			}
			where += " and g.uniqueId in (" + groups + ")";
		}
		
		List<Long> roomFeatures = null;
		if (form.getRoomFeatures() != null && form.getRoomFeatures().length > 0) {
			roomFeatures = new ArrayList<Long>();
			for (int i = 0; i < form.getRoomFeatures().length; i++) {
				from += " inner join l.features f" + i;
				where += " and f" + i + ".uniqueId = :feature" + i;
				roomFeatures.add(form.getRoomFeatures()[i]);
			}
		}
		
		String filter = null;
		if (form.getFilter() != null && !form.getFilter().isEmpty()) {
			filter = form.getFilter();
			where += " and ((l.class = Room and (lower(l.buildingAbbv || ' ' || l.roomNumber) like :filter or lower(l.displayName) like :filter)) or (l.class = NonUniversityLocation and lower(l.name) like :filter))";
		}
		
		Query query = LocationDAO.getInstance().getSession().createQuery("select distinct l from " + from + " where " + where);
		
		query.setLong("sessionId", sessionContext.getUser().getCurrentAcademicSessionId());
		if (departmentIds != null)
			for (int i = 0; i < departmentIds.size(); i++)
				query.setLong("dept" + i, departmentIds.get(i));
		if (examTypeId != null)
			query.setLong("examTypeId", examTypeId);
		if (minSize != null)
			query.setInteger("minSize", minSize);
		if (maxSize != null)
			query.setInteger("maxSize", maxSize);
		if (filter != null)
			query.setString("filter", "%" + filter.toLowerCase() + "%");
		if (roomTypes != null)
			for (int i = 0; i < roomTypes.size(); i++)
				query.setLong("type" + i, roomTypes.get(i));
		if (roomGroups != null)
			for (int i = 0; i < roomGroups.size(); i++)
				query.setLong("group" + i, roomGroups.get(i));
		if (roomFeatures != null)
			for (int i = 0; i < roomFeatures.size(); i++)
				query.setLong("feature" + i, roomFeatures.get(i));
		
		form.setRooms((List<Location>)query.setCacheable(true).list());
	}

	/**
	 * 
	 * @param request
	 * @param roomListForm
	 * @throws Exception 
	 */
	private void buildWebTable(HttpServletRequest request, RoomListForm roomListForm, boolean featuresOneColumn, ExamType examType) throws Exception {
		
		ActionMessages errors = new ActionMessages();
		
		Long sessionId = sessionContext.getUser().getCurrentAcademicSessionId();
		Session session = SessionDAO.getInstance().get(sessionId);
		DecimalFormat dfa = new DecimalFormat(ApplicationProperties.getProperty("unitime.room.area.units.format", "#,##0.00"));
		
		Collection rooms = roomListForm.getRooms();
		if (rooms.size() == 0) {
			errors.add("searchResult", new ActionMessage("errors.generic", "No rooms for the selected department were found."));
			saveErrors(request, errors);
		} else {
			ArrayList globalRoomFeatures = new ArrayList();
			Set deptRoomFeatures = new TreeSet();
			
			Set depts = null;
			if (roomListForm.getDeptCodeX().equalsIgnoreCase("All")) {
				depts = Department.getUserDepartments(sessionContext.getUser());
			} else if (roomListForm.getDeptCodeX().matches("Exam[0-9]*")) {
			    depts = new HashSet(0);
			} else {
				depts = new HashSet(1);
				depts.add(Department.findByDeptCode(roomListForm.getDeptCodeX(), sessionId));
			}
			
			org.hibernate.Session hibSession = null;
			try {
				
				RoomFeatureDAO d = new RoomFeatureDAO();
				hibSession = d.getSession();
				
				List list = hibSession
							.createCriteria(GlobalRoomFeature.class)
							.add(Restrictions.eq("session.uniqueId", sessionId))
							.addOrder(Order.asc("label"))
							.list();
				
				for (Iterator iter = list.iterator();iter.hasNext();) {
					GlobalRoomFeature rf = (GlobalRoomFeature) iter.next();
					globalRoomFeatures.add(rf);
				}
				Debug.debug("global room feature: " + globalRoomFeatures.size());
				
				if (roomListForm.getDeptCodeX().equalsIgnoreCase("All")) {
					if (sessionContext.getUser().getCurrentAuthority().hasRight(Right.DepartmentIndependent)) {
						deptRoomFeatures.addAll(hibSession.createQuery(
								"select distinct f from DepartmentRoomFeature f inner join f.department d where " +
								"d.session.uniqueId=:sessionId order by f.label").
								setLong("sessionId",sessionId).
								setCacheable(true).
								list());						
					} else {
						String deptIds = "";
						for (Qualifiable q: sessionContext.getUser().getCurrentAuthority().getQualifiers("Department")) {
							if (!deptIds.isEmpty()) deptIds += ",";
							deptIds += q.getQualifierId();
						}
						if (!deptIds.isEmpty())
							deptRoomFeatures.addAll(hibSession.createQuery(
									"select distinct f from DepartmentRoomFeature f inner join f.department d " +
									"where d.session.uniqueId=:sessionId and d.uniqueId in ("+ deptIds + ") order by f.label").
									setLong("sessionId",sessionId).
									setCacheable(true).
									list());
					} 
	            } else if (roomListForm.getDeptCodeX().matches("Exam[0-9]*")) {
				} else {
					deptRoomFeatures.addAll(hibSession.
						createQuery(
								"select distinct f from DepartmentRoomFeature f inner join f.department d " +
								"where d.session.uniqueId=:sessionId and d.deptCode = :deptCode order by f.label").
						setLong("sessionId",sessionId).
						setString("deptCode",roomListForm.getDeptCodeX()).
						setCacheable(true).
						list());
				}
				
				Debug.debug("manager room feature: " + deptRoomFeatures.size());
				
			} catch (Exception e) {
				Debug.error(e);
			}
	
			//build headings for university rooms
			String fixedHeading1[][] =
			    (examType != null?
		                (featuresOneColumn? new String[][]
		                                                 { { "Bldg", "left", "true" },
		                                                 { "Room", "left", "true" },
		                                                 { "Capacity", "right", "false" },
		                                                 { "Exam Capacity", "right", "false" },
		                                                 { MSG.columnArea(), "right", "false" },
		                                                 { "Period Preferences", "center", "false" },
		                                                 { "Groups", "left", "true" },
		                                                 { "Features", "left", "true" } } 
		                                             : new String[][]
		                                                 { { "Bldg", "left", "true" },
		                                                 { "Room", "left", "true" },
		                                                 { "Capacity", "right", "false" },
		                                                 { MSG.columnArea(), "right", "false" },
		                                                 { "Exam Capacity", "right", "false" },
		                                                 { "Period Preferences", "center", "false" },
		                                                 { "Groups", "left", "true" } })                    
			    :
	                (featuresOneColumn? new String[][]
	                                                 { { "Bldg", "left", "true" },
	                                                 { "Room", "left", "true" },
	                                                 { "Capacity", "right", "false" },
	                                                 { MSG.columnArea(), "right", "false" },
	                                                 { "Availability", "left", "true" },
	                                                 { "Departments", "left", "true" },
	                                                 { "Control", "left", "true" },
	                                                 { "Events", "left", "true" },
	                                                 { "Groups", "left", "true" },
	                                                 { "Features", "left", "true" } } 
	                                             : new String[][]
	                                                 { { "Bldg", "left", "true" },
	                                                 { "Room", "left", "true" },
	                                                 { "Capacity", "right", "false" },
	                                                 { MSG.columnArea(), "right", "false" },
	                                                 { "Availability", "left", "true" },
	                                                 { "Departments", "left", "true" },
	                                                 { "Control", "left", "true" },
	                                                 { "Events", "left", "true" },
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
			}
			
			
			//build headings for non-univ locations
			String fixedHeading2[][] =
			    ( examType != null ?
		                ( featuresOneColumn ? new String[][]
		                                                   {{ "Location", "left", "true" },
		                                                   { "Capacity", "right", "false" },
		                                                   { "Exam Capacity", "right", "false" },
		                                                   { MSG.columnArea(), "right", "false" },
		                                                   { "Period Preferences", "center", "false" },
		                                                   { "Groups", "left", "true" },
		                                                   { "Features", "left", "true" }}
		                                           : new String[][]
		                                                   {{ "Location", "left", "true" },
		                                                   { "Capacity", "right", "false" },
		                                                   { "Exam Capacity", "right", "false" },
		                                                   { MSG.columnArea(), "right", "false" },
		                                                   { "Period Preferences", "center", "false" },
		                                                   { "Groups", "left", "true" } })
			     :
	                ( featuresOneColumn ? new String[][]
	                                                   {{ "Location", "left", "true" },
	                                                   { "Capacity", "right", "false" },
	                                                   { MSG.columnArea(), "right", "false" },
	                                                   { "IgnTooFar", "center", "true" },
	                                                   { "IgnChecks", "center", "true" },
	                                                   { "Availability", "left", "true" },
	                                                   { "Departments", "left", "true" },
	                                                   { "Control", "left", "true" },
	                                                   { "Events", "left", "true" },
	                                                   { "Groups", "left", "true" },
	                                                   { "Features", "left", "true" }}
	                                           : new String[][]
	                                                   {{ "Location", "left", "true" },
	                                                   { "Capacity", "right", "false" },
	                                                   { MSG.columnArea(), "right", "false" },
	                                                   { "IgnTooFar", "center", "true" },
	                                                   { "IgnChecks", "center", "true" },
	                                                   { "Availability", "left", "true" },
	                                                   { "Departments", "left", "true" },
	                                                   { "Control", "left", "true" },
	                                                   { "Events", "left", "true" },
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
	
			TreeSet<RoomType> roomTypes = new TreeSet<RoomType>(RoomTypeDAO.getInstance().findAll());
			Hashtable<RoomType, WebTable> tables = new Hashtable();
			for (RoomType t:roomTypes) {
			    WebTable.setOrder(sessionContext, t.getReference() + ".ord", request.getParameter(t.getReference() + "Ord"), 1);
	            WebTable table = (t.isRoom()?new WebTable(heading1.length, t.getLabel(), "roomList.do?"+t.getReference()+"Ord=%%", heading1, alignment1, sorted1):
	                                         new WebTable(heading2.length, t.getLabel(), "roomList.do?"+t.getReference()+"Ord=%%", heading2, alignment2, sorted2));
	            table.setRowStyle("white-space:nowrap");
	            tables.put(t,table);
			}

			boolean timeVertical = CommonValues.VerticalGrid.eq(UserProperty.GridOrientation.get(sessionContext.getUser()));
			boolean gridAsText = CommonValues.TextGrid.eq(UserProperty.GridOrientation.get(sessionContext.getUser()));
			String timeGridSize = UserProperty.GridSize.get(sessionContext.getUser()); 
			
			Department dept = new Department();
			if (!roomListForm.getDeptCodeX().equalsIgnoreCase("All")) {
				dept = Department.findByDeptCode(roomListForm.getDeptCodeX(), sessionId);
			} else {
				dept = null;
			}
			
			for (Iterator iter = rooms.iterator(); iter.hasNext();) {
				Location location = (Location) iter.next();
				
				boolean editable = sessionContext.hasPermission(location, Right.RoomDetail);
				
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
						"<span onmouseover=\"showGwtRoomHint(this, '-" + bldg.getUniqueId() + "');\" onmouseout=\"hideGwtRoomHint();\">" + bldg.getAbbreviation() + "</span>"+
						(location.isIgnoreRoomCheck().booleanValue()?"</i>":"")+
						(editable?"":"</font>");
					comp[0] = location.getLabel();
					idx++;
				}
				
				text[idx] = 
					(editable?"":"<font color='gray'>")+
					(location.isIgnoreRoomCheck().booleanValue()?"<i>":"")+
					(room==null?location.getLabelWithHint():"<span onmouseover=\"showGwtRoomHint(this, '" + location.getUniqueId() + "');\" onmouseout=\"hideGwtRoomHint();\">" + room.getRoomNumber() + "</span>")+
					(location.isIgnoreRoomCheck().booleanValue()?"</i>":"")+
					(editable?"":"</font>");
				comp[idx] = location.getLabel();
				idx++;
				
				text[idx] = (editable?"":"<font color='gray'>")+df5.format(location.getCapacity())+(editable?"":"</font>");
				comp[idx] = new Long(location.getCapacity().intValue());
				idx++;
				
				if (examType != null) {
	                if (location.isExamEnabled(examType)) {
	                    text[idx] = (editable?"":"<font color='gray'>")+df5.format(location.getExamCapacity())+(editable?"":"</font>");
	                    comp[idx] = location.getExamCapacity();
	                } else {
	                    text[idx] = "&nbsp;";
	                    comp[idx] = new Integer(0);
	                }
	                idx++;
	                
	                if (location.getArea() == null) {
	                	text[idx] = "&nbsp;";
	                    comp[idx] = new Double(0);
	                } else {
	                	text[idx] = dfa.format(location.getArea());
	                	comp[idx] = location.getArea();
	                }
	                idx++;

	                if (location.isExamEnabled(examType)) {
	                    if (gridAsText)
	                        text[idx] = location.getExamPreferencesAbbreviationHtml(examType);
	                    else {
	                        if (examType.getType() == ExamType.sExamTypeMidterm) {
	                            MidtermPeriodPreferenceModel epx = new MidtermPeriodPreferenceModel(location.getSession(), examType);
	                            epx.load(location);
	                            text[idx]=epx.toString(true).replaceAll(", ", "<br>");
	                        } else {
	                            PeriodPreferenceModel px = new PeriodPreferenceModel(location.getSession(), examType.getUniqueId());
	                            px.load(location);
	                            RequiredTimeTable rtt = new RequiredTimeTable(px);
	                            String hint = rtt.print(false, timeVertical).replace(");\n</script>", "").replace("<script language=\"javascript\">\ndocument.write(", "").replace("\n", " ");
                                text[idx] = "<img border='0' src='" +
                                	"pattern?v=" + (timeVertical ? 1 : 0) + "&loc=" + location.getUniqueId() + "&xt=" + examType.getUniqueId() + 
                                	"' onmouseover=\"showGwtHint(this, " + hint + ");\" onmouseout=\"hideGwtHint();\">";
	                        }
	                    }
	                    comp[idx] = null;
	                } else {
	                    text[idx] = "";
                        comp[idx] = null;
	                }
                    idx++;
				} else {
	                if (location.getArea() == null) {
	                	text[idx] = "&nbsp;";
	                    comp[idx] = new Double(0);
	                } else {
	                	text[idx] = dfa.format(location.getArea());
	                	comp[idx] = location.getArea();
	                }
	                idx++;

	                PreferenceLevel roomPref = location.getRoomPreferenceLevel(dept);
	                if (editable && roomPref!=null && !PreferenceLevel.sNeutral.equals(roomPref.getPrefProlog())) {
	                    if (room==null) {
	                        text[0] =
	                            (location.isIgnoreRoomCheck().booleanValue()?"<i>":"")+
	                            "<span style='color:"+roomPref.prefcolor()+";font-weight:bold;' onmouseover=\"showGwtRoomHint(this, '" + location.getUniqueId() + "', '" +roomPref.getPrefName() + "');\" onmouseout=\"hideGwtRoomHint();\">"+location.getLabel()+"</span>"+
	                            (location.isIgnoreRoomCheck().booleanValue()?"</i>":"");
	                    } else {
	                        text[0] = 
	                            (location.isIgnoreRoomCheck().booleanValue()?"<i>":"")+
	                            "<span style='color:"+roomPref.prefcolor()+";font-weight:bold;' onmouseover=\"showGwtRoomHint(this, '-" + bldg.getUniqueId() + "', '" +(bldg == null ? roomPref.getPrefName() : "") + "');\" onmouseout=\"hideGwtRoomHint();\">"+(bldg==null?"":bldg.getAbbreviation())+"</span>"+
	                            (location.isIgnoreRoomCheck().booleanValue()?"</i>":"");
	                        text[1] = 
	                            (location.isIgnoreRoomCheck().booleanValue()?"<i>":"")+
	                            "<span style='color:"+roomPref.prefcolor()+";font-weight:bold;' onmouseover=\"showGwtRoomHint(this, '" + location.getUniqueId() + "', '" +roomPref.getPrefName() + "');\" onmouseout=\"hideGwtRoomHint();\">"+room.getRoomNumber()+"</span>"+
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
	    
	                if (sessionContext.hasPermission(location, Right.RoomDetailAvailability)) {
		                // get pattern column
		                RequiredTimeTable rtt = location.getRoomSharingTable();
		                rtt.getModel().setDefaultSelection(timeGridSize);
		                if (gridAsText) {
		                    // String hint = rtt.print(false, timeVertical).replace(");\n</script>", "").replace("<script language=\"javascript\">\ndocument.write(", "").replace("\n", " ");
	                        // text[idx] = "<span onmouseover=\"showGwtHint(this, " + hint + ");\" onmouseout=\"hideGwtHint();\">" + rtt.getModel().toString().replaceAll(", ","<br>") + "</span>";
		                	text[idx] = "<span onmouseover=\"showGwtRoomAvailabilityHint(this, '" + location.getUniqueId() + "');\" onmouseout=\"hideGwtRoomAvailabilityHint();\">" + rtt.getModel().toString().replaceAll(", ","<br>") + "</span>";
		                } else {
		                    // String hint = rtt.print(false, timeVertical).replace(");\n</script>", "").replace("<script language=\"javascript\">\ndocument.write(", "").replace("\n", " ");
		                    // text[idx] = "<img border='0' onmouseover=\"showGwtHint(this, " + hint + ");\" onmouseout=\"hideGwtHint();\" src='" +
		                	text[idx] = "<img border='0' onmouseover=\"showGwtRoomAvailabilityHint(this, '" + location.getUniqueId() + "');\" onmouseout=\"hideGwtRoomAvailabilityHint();\" src='" +
		                    		"pattern?v=" + (timeVertical ? 1 : 0) + "&s=" + rtt.getModel().getDefaultSelection() + "&loc=" + location.getUniqueId() + "'>&nbsp;";
		                }
	                } else if (sessionContext.hasPermission(location, Right.RoomDetailEventAvailability)) {
	                	// get pattern column
		                RequiredTimeTable rtt = location.getEventAvailabilityTable();
		                rtt.getModel().setDefaultSelection(timeGridSize);
		                if (gridAsText) {
		                    // String hint = rtt.print(false, timeVertical).replace(");\n</script>", "").replace("<script language=\"javascript\">\ndocument.write(", "").replace("\n", " ");
	                        // text[idx] = "<span onmouseover=\"showGwtHint(this, " + hint + ");\" onmouseout=\"hideGwtHint();\">" + rtt.getModel().toString().replaceAll(", ","<br>") + "</span>";
		                	text[idx] = "<span onmouseover=\"showGwtRoomEventAvailabilityHint(this, '" + location.getUniqueId() + "');\" onmouseout=\"hideGwtRoomEventAvailabilityHint();\">" + rtt.getModel().toString().replaceAll(", ","<br>") + "</span>";
		                } else {
		                    // String hint = rtt.print(false, timeVertical).replace(");\n</script>", "").replace("<script language=\"javascript\">\ndocument.write(", "").replace("\n", " ");
		                    // text[idx] = "<img border='0' onmouseover=\"showGwtHint(this, " + hint + ");\" onmouseout=\"hideGwtHint();\" src='" +
		                	text[idx] = "<img border='0' onmouseover=\"showGwtRoomEventAvailabilityHint(this, '" + location.getUniqueId() + "');\" onmouseout=\"hideGwtRoomEventAvailabilityHint();\" src='" +
		                    		"pattern?v=" + (timeVertical ? 1 : 0) + "&s=" + rtt.getModel().getDefaultSelection() + "&loc=" + location.getUniqueId() + "&e=1'>&nbsp;";
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
	                if (ts.size() == session.getDepartments().size()) {
	                	text[idx] = "<b>All</b>";
	                	comp[idx] = ""; 
	                } else {
	                	int cnt = 0;
		                for (Iterator it = ts.iterator(); it.hasNext(); cnt++) {
		                    Department d = (Department) it.next();
		                    if (text[idx].length() > 0)
		                        text[idx] = text[idx] + (ts.size() <= 5 || cnt % 5 == 0 ? "<br>" : ", ");
		                    else
		                        comp[idx] = d.getDeptCode();
		                    text[idx] = text[idx] + d.htmlShortLabel(); 
		                }
	                }
	                idx++;
	                
	                //control column
	                if (!roomListForm.getDeptCodeX().equalsIgnoreCase("All") && !roomListForm.getDeptCodeX().matches("Exam[0-9]*")) {
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
	                
	                //events column
                	if (location.getEventDepartment() != null && location.getEventDepartment().isAllowEvents()) {
                		if (sessionContext.hasPermission(location, Right.RoomDetailEventAvailability))
                			text[idx] = "<span onmouseover=\"showGwtRoomEventAvailabilityHint(this, '" + location.getUniqueId() + "');\" onmouseout=\"hideGwtRoomEventAvailabilityHint();\">" + location.getEventDepartment().htmlShortLabel() + "</span>";
                		else
                			text[idx] = location.getEventDepartment().htmlShortLabel();
                        comp[idx] = location.getEventDepartment().getDeptCode();
                    } else {
                        text[idx] = "";
                        comp[idx] = "";
                	}
	                idx++;
				}
				
				text[0] += "<A name=\"A"+location.getUniqueId()+"\"></A>";
				
				// get groups column
				text[idx] = "";
                comp[idx] = "";
				for (Iterator it = new TreeSet(location.getRoomGroups()).iterator(); it.hasNext();) {
					RoomGroup rg = (RoomGroup) it.next();
					if (!rg.isGlobal().booleanValue() && (examType != null || !depts.contains(rg.getDepartment()))) continue;
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
					if (examType == null)
					    for (Iterator it = new TreeSet(location.getDepartmentRoomFeatures()).iterator(); it.hasNext();) {
					        DepartmentRoomFeature drf = (DepartmentRoomFeature) it.next();
							if (!depts.contains(drf.getDepartment())) continue;
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
				if (location instanceof NonUniversityLocation) {
				    tables.get(location.getRoomType()).addLine(
					        (editable?"onClick=\"document.location='roomDetail.do?id="+ location.getUniqueId() + "';\"":null),
							text, 
							comp,
							location.getUniqueId().toString());
				} else {
				    tables.get(room.getRoomType()).addLine(
	                        (editable?"onClick=\"document.location='roomDetail.do?id="+ room.getUniqueId() + "';\"":null),
	                        text, 
	                        comp,
	                        room.getUniqueId().toString());
				}
			}
	
			List<Long> ids = new ArrayList<Long>();
			for (Map.Entry<RoomType,WebTable> entry: tables.entrySet()) {
			    int ord = WebTable.getOrder(sessionContext, entry.getKey().getReference()+".ord");
			    if (ord>heading1.length) ord = 0;
			    if (!entry.getValue().getLines().isEmpty()) {
			        request.setAttribute(entry.getKey().getReference(), entry.getValue().printTable(ord));
			    }
			    if (!ids.isEmpty()) ids.add(-1l);
			    for (Enumeration<WebTableLine> e = entry.getValue().getLines().elements(); e.hasMoreElements(); ) {
			    	ids.add(Long.parseLong(e.nextElement().getUniqueId()));
			    }
			}
			Navigation.set(sessionContext, Navigation.sInstructionalOfferingLevel, ids);
			
			if (request.getParameter("backId")!=null)
	            request.setAttribute("hash", "A" + request.getParameter("backId"));
			
			BackTracker.markForBack(
	        		request,
	        		"roomSearch.do",
	        		"Rooms",
	        		true, true);
			
			request.setAttribute("colspan", String.valueOf(Math.max(heading1.length, heading2.length)));
		}
	}
	
	public void buildPdfWebTable(OutputStream out, RoomListForm roomListForm, boolean featuresOneColumn, ExamType examType) throws Exception {
		Long sessionId = sessionContext.getUser().getCurrentAcademicSessionId();
		Session session = SessionDAO.getInstance().get(sessionId);
		DecimalFormat dfa = new DecimalFormat(ApplicationProperties.getProperty("unitime.room.area.units.format", "#,##0.00"));

		Collection rooms = roomListForm.getRooms();

		ArrayList globalRoomFeatures = new ArrayList();
		Set deptRoomFeatures = new TreeSet();
		
		Set depts = null;
		if (roomListForm.getDeptCodeX().equalsIgnoreCase("All")) {
			depts = Department.getUserDepartments(sessionContext.getUser());
        } else if (roomListForm.getDeptCodeX().matches("Exam[0-9]*")) {
            depts = new HashSet(0);
		} else {
			depts = new HashSet(1);
			depts.add(Department.findByDeptCode(roomListForm.getDeptCodeX(), sessionId));
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
				if (sessionContext.getUser().getCurrentAuthority().hasRight(Right.DepartmentIndependent)) {
					deptRoomFeatures.addAll(hibSession.createQuery(
							"select distinct f from DepartmentRoomFeature f inner join f.department d where " +
							"d.session.uniqueId=:sessionId order by f.label").
							setLong("sessionId",sessionId).
							setCacheable(true).
							list());						
				} else {
					String deptIds = "";
					for (Qualifiable q: sessionContext.getUser().getCurrentAuthority().getQualifiers("Department")) {
						if (!deptIds.isEmpty()) deptIds += ",";
						deptIds += q.getQualifierId();
					}
					if (!deptIds.isEmpty())
						deptRoomFeatures.addAll(hibSession.createQuery(
								"select distinct f from DepartmentRoomFeature f inner join f.department d " +
								"where d.session.uniqueId=:sessionId and d.uniqueId in ("+ deptIds + ") order by f.label").
								setLong("sessionId",sessionId).
								setCacheable(true).
								list());
				} 
            } else if (roomListForm.getDeptCodeX().matches("Exam[0-9]*")) {
			} else {
				deptRoomFeatures.addAll(hibSession.
					createQuery(
							"select distinct f from DepartmentRoomFeature f inner join f.department d " +
							"where d.session.uniqueId=:sessionId and d.deptCode = :deptCode order by f.label").
					setLong("sessionId",sessionId).
					setString("deptCode",roomListForm.getDeptCodeX()).
					setCacheable(true).
					list());
			}
			
		} catch (Exception e) {
			Debug.error(e);
		}

		//build headings for university rooms
		String fixedHeading1[][] =
		    (examType != null?
		            (featuresOneColumn? new String[][]
		                                             { { "Bldg", "left", "true" },
		                                             { "Room", "left", "true" },
		                                             { "Capacity", "right", "false" },
		                                             { "Exam Capacity", "right", "false" },
		                                             { MSG.columnAreaPDF(), "right", "false" },
		                                             { "Period Preferences", "center", "true" },
		                                             { "Groups", "left", "true" },
		                                             { "Features", "left", "true" } } 
		                                         : new String[][]
		                                             { { "Bldg", "left", "true" },
		                                             { "Room", "left", "true" },
		                                             { "Capacity", "right", "false" },
		                                             { "Exam Capacity", "right", "false" },
		                                             { MSG.columnAreaPDF(), "right", "false" },
		                                             { "Period Preferences", "center", "true" },
		                                             { "Groups", "left", "true" } })			    
            :
                (featuresOneColumn? new String[][]
                                                 { { "Bldg", "left", "true" },
                                                 { "Room", "left", "true" },
                                                 { "Capacity", "right", "false" },
                                                 { MSG.columnAreaPDF(), "right", "false" },
                                                 { "Availability", "left", "true" },
                                                 { "Departments", "left", "true" },
                                                 { "Control", "left", "true" },
                                                 { "Events", "left", "true" },
                                                 { "Groups", "left", "true" },
                                                 { "Features", "left", "true" } } 
                                             : new String[][]
                                                 { { "Bldg", "left", "true" },
                                                 { "Room", "left", "true" },
                                                 { "Capacity", "right", "false" },
                                                 { MSG.columnAreaPDF(), "right", "false" },
                                                 { "Availability", "left", "true" },
                                                 { "Departments", "left", "true" },
                                                 { "Control", "left", "true" },
                                                 { "Events", "left", "true" },
                                                 { "Groups", "left", "true" } }));                   

		String heading1[] = new String[fixedHeading1.length
		                               + (featuresOneColumn ? 0 : (globalRoomFeatures.size() + deptRoomFeatures.size()))];
		String alignment1[] = new String[heading1.length];
		boolean sorted1[] = new boolean[heading1.length];
		
		for (int i = 0; i < fixedHeading1.length; i++) {
			heading1[i] = fixedHeading1[i][0];
			alignment1[i] = fixedHeading1[i][1];
			sorted1[i] = (Boolean.valueOf(fixedHeading1[i][2])).booleanValue();
		}
		
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
		}			
		
		//build headings for non-univ locations
		String fixedHeading2[][] = 
		    (examType != null ?
	                ( featuresOneColumn ? new String[][]
	                                                   {{ "Location", "left", "true" },
	                                                   { "Capacity", "right", "false" },
	                                                   { "Exam Capacity", "right", "false" },
	                                                   { MSG.columnAreaPDF(), "right", "false" },
	                                                   { "Period Preferences", "center", "true" },
	                                                   { "Groups", "left", "true" },
	                                                   { "Features", "left", "true" }}
	                                           : new String[][]
	                                                   {{ "Location", "left", "true" },
	                                                   { "Capacity", "right", "false" },
	                                                   { MSG.columnAreaPDF(), "right", "false" },
	                                                   { "Period Preferences", "center", "true" },
	                                                   { "Groups", "left", "true" } })
		    :
                ( featuresOneColumn ? new String[][]
                                                   {{ "Location", "left", "true" },
                                                   { "Capacity", "right", "false" },
                                                   { MSG.columnAreaPDF(), "right", "false" },
                                                   { "IgnTooFar", "center", "true" },
                                                   { "IgnChecks", "center", "true" },
                                                   { "Availability", "left", "true" },
                                                   { "Departments", "left", "true" },
                                                   { "Control", "left", "true" },
                                                   { "Events", "left", "true" },
                                                   { "Groups", "left", "true" },
                                                   { "Features", "left", "true" }}
                                           : new String[][]
                                                   {{ "Location", "left", "true" },
                                                   { "Capacity", "right", "false" },
                                                   { "Exam Capacity", "right", "false" },
                                                   { MSG.columnAreaPDF(), "right", "false" },
                                                   { "IgnTooFar", "center", "true" },
                                                   { "IgnChecks", "center", "true" },
                                                   { "Availability", "left", "true" },
                                                   { "Departments", "left", "true" },
                                                   { "Control", "left", "true" },
                                                   { "Events", "left", "true" },
                                                   { "Groups", "left", "true" } })
		    );
		
		String heading2[] = new String[fixedHeading2.length
		                               + (featuresOneColumn ? 0 : (globalRoomFeatures.size() + deptRoomFeatures.size()))];
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
		
        TreeSet<RoomType> roomTypes = new TreeSet<RoomType>(RoomTypeDAO.getInstance().findAll());
        Hashtable<RoomType, PdfWebTable> tables = new Hashtable();
        for (RoomType t:roomTypes) {
            PdfWebTable table = (t.isRoom()?
                    new PdfWebTable(heading1.length, t.getLabel(), null, heading1, alignment1, sorted1):
                    new PdfWebTable(heading2.length, t.getLabel(), null, heading2, alignment2, sorted2));
            tables.put(t,table);
        }
		
		boolean timeVertical = CommonValues.VerticalGrid.eq(UserProperty.GridOrientation.get(sessionContext.getUser()));
		boolean gridAsText = CommonValues.TextGrid.eq(UserProperty.GridOrientation.get(sessionContext.getUser()));
		String timeGridSize = UserProperty.GridSize.get(sessionContext.getUser()); 

		Department dept = new Department();
		if (!roomListForm.getDeptCodeX().equalsIgnoreCase("All")) {
			dept = Department.findByDeptCode(roomListForm.getDeptCodeX(), sessionId);
		} else {
			dept = null;
		}
		
		for (Iterator iter = rooms.iterator(); iter.hasNext();) {
			Location location = (Location) iter.next();
			
			boolean editable = sessionContext.hasPermission(location, Right.RoomDetail);

			Room room = (location instanceof Room ? (Room)location : null);
			Building bldg = (room==null?null:room.getBuilding());
			
			PdfWebTable table = tables.get(location.getRoomType());
			
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
			
			if (examType != null) {
                if (location.isExamEnabled(examType)) {
                    text[idx] = df5.format(location.getExamCapacity());
                    comp[idx] = location.getExamCapacity();
                } else {
                    text[idx] = "";
                    comp[idx] = new Integer(0);
                }
                idx++;
                
                if (location.getArea() == null) {
                	text[idx] = "";
                    comp[idx] = new Double(0);
                } else {
                	text[idx] = dfa.format(location.getArea());
                	comp[idx] = location.getArea();
                }
                idx++;

                if (location.isExamEnabled(examType)) {
                    text[idx] = location.getExamPreferencesAbbreviation(examType);
                    comp[idx] = null;
                } else {
                    text[idx] = "";
                    comp[idx] = null;
                }
                idx++;
			} else {
                if (location.getArea() == null) {
                	text[idx] = "";
                    comp[idx] = new Double(0);
                } else {
                	text[idx] = dfa.format(location.getArea());
                	comp[idx] = location.getArea();
                }
                idx++;

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
                if (ts.size() == session.getDepartments().size()) {
                	text[idx] = "@@BOLD All";
                	comp[idx] = ""; 
                } else {
                	int cnt = 0;
	                for (Iterator it = ts.iterator(); it.hasNext(); cnt++) {
	                    Department d = (Department) it.next();
	                    if (text[idx].length() > 0)
	                        text[idx] = text[idx] + (ts.size() <= 5 || cnt % 5 == 0 ? "\n" : ", ");
	                    else
	                        comp[idx] = d.getDeptCode();
	                    text[idx] = text[idx] + "@@COLOR "+d.getRoomSharingColor(null)+" "+d.getShortLabel(); 
	                }
                }
                idx++;
                
                //control column
                if (!roomListForm.getDeptCodeX().equalsIgnoreCase("All") && !roomListForm.getDeptCodeX().matches("Exam[0-9]*")) {
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
                
                //events column
            	if (location.getEventDepartment() != null && location.getEventDepartment().isAllowEvents()) {
                    text[idx] = "@@COLOR "+location.getEventDepartment().getRoomSharingColor(null)+" "+location.getEventDepartment().getShortLabel();
                    comp[idx] = location.getEventDepartment().getDeptCode();
                } else {
                    text[idx] = "";
                    comp[idx] = "";
            	}
                idx++;
			}
			
			// get groups column
			text[idx] = "";
			for (Iterator it = new TreeSet(location.getRoomGroups()).iterator(); it.hasNext();) {
				RoomGroup rg = (RoomGroup) it.next();
				if (!rg.isGlobal().booleanValue() && (examType != null || !depts.contains(rg.getDepartment()))) continue;
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
					text[idx] += rf.getLabelWithType();
				}
				if (examType == null)
                    for (Iterator it = new TreeSet(location.getDepartmentRoomFeatures()).iterator(); it.hasNext();) {
                        DepartmentRoomFeature drf = (DepartmentRoomFeature) it.next();
						if (!depts.contains(drf.getDepartment())) continue;
                        boolean skip = true;
                        for (Iterator j=location.getRoomDepts().iterator();j.hasNext();) {
                            RoomDept rd = (RoomDept)j.next();
                            if (drf.getDepartment().equals(rd.getDepartment())) { skip=false; break; }
                        }
                        if (skip) continue;
                        if (text[idx].length()>0) text[idx] += "\n";
                        comp[idx] = comp[idx] + drf.getLabel().trim();
                        text[idx] +="@@COLOR "+drf.getDepartment().getRoomSharingColor(null)+" "+drf.getLabelWithType();
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
            table.addLine(
                    (editable?"onClick=\"document.location='roomDetail.do?id="+ location.getUniqueId() + "';\"":null),
                    text, 
                    comp);
		}

		Document doc = null;
		// set request attributes
		for (RoomType t : roomTypes) {
		    PdfWebTable table = tables.get(t);
		    if (!table.getLines().isEmpty()) {
                int ord = WebTable.getOrder(sessionContext, t.getReference()+".ord");
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
                doc.add(new Paragraph(table.getName(), PdfFont.getBigFont(true)));
                doc.add(pdfTable);
		        
		    }
		}

		if (doc==null) return;
		
		doc.close();
	}

	public void buildCsvWebTable(PrintWriter out, RoomListForm roomListForm, boolean featuresOneColumn, ExamType examType) throws Exception {
		Long sessionId = sessionContext.getUser().getCurrentAcademicSessionId();
		Session session = SessionDAO.getInstance().get(sessionId);
		DecimalFormat dfa = new DecimalFormat(ApplicationProperties.getProperty("unitime.room.area.units.format", "#,##0.00"));

		Collection rooms = roomListForm.getRooms();

		ArrayList globalRoomFeatures = new ArrayList();
		Set deptRoomFeatures = new TreeSet();
		
		Set depts = null;
		if (roomListForm.getDeptCodeX().equalsIgnoreCase("All")) {
			depts = Department.getUserDepartments(sessionContext.getUser());
		} else if (roomListForm.getDeptCodeX().matches("Exam[0-9]*")) {
		    depts = new HashSet(0);
		} else {
			depts = new HashSet(1);
			depts.add(Department.findByDeptCode(roomListForm.getDeptCodeX(), sessionId));
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
				if (sessionContext.getUser().getCurrentAuthority().hasRight(Right.DepartmentIndependent)) {
					deptRoomFeatures.addAll(hibSession.createQuery(
							"select distinct f from DepartmentRoomFeature f inner join f.department d where " +
							"d.session.uniqueId=:sessionId order by f.label").
							setLong("sessionId",sessionId).
							setCacheable(true).
							list());						
				} else {
					String deptIds = "";
					for (Qualifiable q: sessionContext.getUser().getCurrentAuthority().getQualifiers("Department")) {
						if (!deptIds.isEmpty()) deptIds += ",";
						deptIds += q.getQualifierId();
					}
					if (!deptIds.isEmpty())
						deptRoomFeatures.addAll(hibSession.createQuery(
								"select distinct f from DepartmentRoomFeature f inner join f.department d " +
								"where d.session.uniqueId=:sessionId and d.uniqueId in ("+ deptIds + ") order by f.label").
								setLong("sessionId",sessionId).
								setCacheable(true).
								list());
				} 
            } else if (roomListForm.getDeptCodeX().matches("Exam[0-9]*")) {
			} else {
				deptRoomFeatures.addAll(hibSession.
					createQuery(
							"select distinct f from DepartmentRoomFeature f inner join f.department d " +
							"where d.session.uniqueId=:sessionId and d.deptCode = :deptCode order by f.label").
					setLong("sessionId",sessionId).
					setString("deptCode",roomListForm.getDeptCodeX()).
					setCacheable(true).
					list());
			}
			
		} catch (Exception e) {
			Debug.error(e);
		}

		//build headings for university rooms
		String fixedHeading1[][] =
		    (examType != null?
		            (featuresOneColumn? new String[][]
		                                             { { "Bldg", "left", "true" },
		                                             { "Room", "left", "true" },
		                                             { "Capacity", "right", "false" },
		                                             { "Exam Capacity", "right", "false" },
		                                             { MSG.columnAreaPDF(), "right", "false" },
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
                                                 { MSG.columnAreaPDF(), "right", "false" },
                                                 { "Availability", "left", "true" },
                                                 { "Departments", "left", "true" },
                                                 { "Control", "left", "true" },
                                                 { "Events", "left", "true" },
                                                 { "Groups", "left", "true" },
                                                 { "Features", "left", "true" } } 
                                             : new String[][]
                                                 { { "Bldg", "left", "true" },
                                                 { "Room", "left", "true" },
                                                 { "Capacity", "right", "false" },
                                                 { MSG.columnAreaPDF(), "right", "false" },
                                                 { "Availability", "left", "true" },
                                                 { "Departments", "left", "true" },
                                                 { "Control", "left", "true" },
                                                 { "Events", "left", "true" },
                                                 { "Groups", "left", "true" } }));                   

		String heading1[] = new String[fixedHeading1.length
				+ + (featuresOneColumn ? 0 : (globalRoomFeatures.size() + deptRoomFeatures.size()))];
		
		for (int i = 0; i < fixedHeading1.length; i++) {
			heading1[i] = fixedHeading1[i][0];
		}
		
		if (!featuresOneColumn) {
			int i = fixedHeading1.length;
			for (Iterator it = globalRoomFeatures.iterator(); it.hasNext();) {
				heading1[i] = ((GlobalRoomFeature) it.next()).getLabel();
				heading1[i] = heading1[i].replaceFirst(" ", "\n");
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
				i++;
			}
		}			
		
		//build headings for non-univ locations
		String fixedHeading2[][] = 
		    (examType != null ?
	                ( featuresOneColumn ? new String[][]
	                                                   {{ "Location", "left", "true" },
	                                                   { "Capacity", "right", "false" },
	                                                   { "Exam Capacity", "right", "false" },
	                                                   { MSG.columnAreaPDF(), "right", "false" },
	                                                   { "Period Preferences", "center", "true" },
	                                                   { "Groups", "left", "true" },
	                                                   { "Features", "left", "true" }}
	                                           : new String[][]
	                                                   {{ "Location", "left", "true" },
	                                                   { "Capacity", "right", "false" },
	                                                   { MSG.columnAreaPDF(), "right", "false" },
	                                                   { "Period Preferences", "center", "true" },
	                                                   { "Groups", "left", "true" } })
		    :
                ( featuresOneColumn ? new String[][]
                                                   {{ "Location", "left", "true" },
                                                   { "Capacity", "right", "false" },
                                                   { MSG.columnAreaPDF(), "right", "false" },
                                                   { "IgnTooFar", "center", "true" },
                                                   { "IgnChecks", "center", "true" },
                                                   { "Availability", "left", "true" },
                                                   { "Departments", "left", "true" },
                                                   { "Control", "left", "true" },
                                                   { "Events", "left", "true" },
                                                   { "Groups", "left", "true" },
                                                   { "Features", "left", "true" }}
                                           : new String[][]
                                                   {{ "Location", "left", "true" },
                                                   { "Capacity", "right", "false" },
                                                   { "Exam Capacity", "right", "false" },
                                                   { MSG.columnAreaPDF(), "right", "false" },
                                                   { "IgnTooFar", "center", "true" },
                                                   { "IgnChecks", "center", "true" },
                                                   { "Availability", "left", "true" },
                                                   { "Departments", "left", "true" },
                                                   { "Control", "left", "true" },
                                                   { "Events", "left", "true" },
                                                   { "Groups", "left", "true" } })
		    );
		
		String heading2[] = new String[fixedHeading2.length
		                               + (featuresOneColumn ? 0 : (globalRoomFeatures.size() + deptRoomFeatures.size()))];
		
		for (int i = 0; i < fixedHeading2.length; i++) {
			heading2[i] = fixedHeading2[i][0];
		}

		if (!featuresOneColumn) {
			int i = fixedHeading2.length;
			for (Iterator it = globalRoomFeatures.iterator(); it.hasNext();) {
				heading2[i] = ((GlobalRoomFeature) it.next()).getLabel();
				heading2[i] = heading2[i].replaceFirst(" ", "\n");
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
				i++;
			}
		}
		
        TreeSet<RoomType> roomTypes = new TreeSet<RoomType>(RoomTypeDAO.getInstance().findAll());
        Hashtable<RoomType, CSVFile> tables = new Hashtable();
        for (RoomType t:roomTypes) {
        	CSVFile table = new CSVFile();
        	List<CSVField> header = new ArrayList<CSVFile.CSVField>();
        	for (String h: (t.isRoom() ? heading1 : heading2)) {
        		header.add(new CSVField(h));
        	}
        	table.setHeader(header);
        	tables.put(t,table);
        }
		
		Department dept = new Department();
		if (!roomListForm.getDeptCodeX().equalsIgnoreCase("All")) {
			dept = Department.findByDeptCode(roomListForm.getDeptCodeX(), sessionId);
		} else {
			dept = null;
		}
		
		for (Iterator iter = rooms.iterator(); iter.hasNext();) {
			Location location = (Location) iter.next();
			
			boolean editable = sessionContext.hasPermission(location, Right.RoomDetail);

			Room room = (location instanceof Room ? (Room)location : null);
			Building bldg = (room==null?null:room.getBuilding());
			
			CSVFile table = tables.get(location.getRoomType());
			
			DecimalFormat df5 = new DecimalFormat("####0");
            CSVField text[] = new CSVField[location instanceof Room ? heading1.length : heading2.length];
			int idx = 0;
			if (bldg!=null) {
				text[idx] =  new CSVField(bldg.getAbbreviation());
				idx++;
			}
			
			text[idx] = new CSVField(room == null ? location.getLabel() : room.getRoomNumber());
			idx++;
			
			text[idx] = new CSVField(df5.format(location.getCapacity()));
			idx++;
			
			if (examType != null) {
                if (location.isExamEnabled(examType)) {
                    text[idx] = new CSVField(df5.format(location.getExamCapacity()));
                } else {
                    text[idx] = new CSVField("");
                }
                idx++;

                if (location.getArea() == null) {
                	text[idx] = new CSVField("");
                } else {
                	text[idx] = new CSVField(dfa.format(location.getArea()));
                }
                idx++;

                if (location.isExamEnabled(examType)) {
                    text[idx] = new CSVField(location.getExamPreferencesAbbreviation(examType));
                } else {
                    text[idx] = new CSVField("");
                }
                idx++;
			} else {
                if (location.getArea() == null) {
                	text[idx] = new CSVField("");
                } else {
                	text[idx] = new CSVField(dfa.format(location.getArea()));
                }
                idx++;

                PreferenceLevel roomPref = location.getRoomPreferenceLevel(dept);
                if (editable && roomPref!=null && !PreferenceLevel.sNeutral.equals(roomPref.getPrefProlog())) {
                    if (room==null) {
                        text[0] = new CSVField(location.getLabel()+" ("+PreferenceLevel.prolog2abbv(roomPref.getPrefProlog())+")");
                    } else {
                        text[0] = new CSVField(bldg==null?"":bldg.getAbbreviation());
                        text[1] = new CSVField(room.getRoomNumber()+" ("+PreferenceLevel.prolog2abbv(roomPref.getPrefProlog())+")");
                    }
                }
                
                //ignore too far
                if (location instanceof NonUniversityLocation) {
                    boolean itf = (location.isIgnoreTooFar()==null?false:location.isIgnoreTooFar().booleanValue());
                    text[idx] = new CSVField(itf?"Yes":"No");
                    idx++;
                    boolean con = (location.isIgnoreRoomCheck()==null?true:location.isIgnoreRoomCheck().booleanValue());
                    text[idx] = new CSVField(con?"YES":"No");
                    idx++;
                }
    
                // get pattern column
                RequiredTimeTable rtt = location.getRoomSharingTable();
                text[idx] = new CSVField(rtt.getModel().toString().replaceAll(", ","\n"));
                idx++;
    
                // get departments column
                Department controlDept = null;
                text[idx] = new CSVField("");
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
                if (ts.size() == session.getDepartments().size()) {
                	text[idx] = new CSVField("");
                } else {
	                for (Iterator it = ts.iterator(); it.hasNext();) {
	                    Department d = (Department) it.next();
	                    text[idx] = new CSVField(text[idx].toString() + (text[idx].toString().isEmpty() ? "" : "\n") + d.getShortLabel()); 
	                }
                }
                idx++;
                
                //control column
                if (!roomListForm.getDeptCodeX().equalsIgnoreCase("All") && !roomListForm.getDeptCodeX().matches("Exam[0-9]*")) {
                    if (controlDept!=null && controlDept.getDeptCode().equals(roomListForm.getDeptCodeX())) {
                        text[idx] = new CSVField("Yes");
                    } else {
                        text[idx] = new CSVField("No");
                    }
                } else {
                    if (controlDept!=null) {
                        text[idx] = new CSVField(controlDept.getShortLabel());
                    } else {
                        text[idx] = new CSVField("");
                    }
                }
                idx++;

                //events column
            	if (location.getEventDepartment() != null && location.getEventDepartment().isAllowEvents()) {
            		text[idx] = new CSVField(location.getEventDepartment().getShortLabel());
                } else {
                	text[idx] = new CSVField("");
            	}
                idx++;
			}
			
			// get groups column
			text[idx] = new CSVField("");
			for (Iterator it = new TreeSet(location.getRoomGroups()).iterator(); it.hasNext();) {
				RoomGroup rg = (RoomGroup) it.next();
				if (!rg.isGlobal().booleanValue() && (examType != null || !depts.contains(rg.getDepartment()))) continue;
                if (!rg.isGlobal().booleanValue()) {
                    boolean skip = true;
                    for (Iterator j=location.getRoomDepts().iterator();j.hasNext();) {
                        RoomDept rd = (RoomDept)j.next();
                        if (rg.getDepartment().equals(rd.getDepartment())) { skip=false; break; }
                    }
                    if (skip) continue;
                }
				text[idx] = new CSVField(text[idx].toString() + (text[idx].toString().isEmpty() ? "" : "\n") +rg.getName()); 
			}
			idx++;
			
			if (featuresOneColumn) {
				// get features column
				text[idx] = new CSVField("");
				for (Iterator it = new TreeSet(location.getGlobalRoomFeatures()).iterator(); it.hasNext();) {
					GlobalRoomFeature rf = (GlobalRoomFeature) it.next();
                    if (text[idx].toString().length()>0) text[idx] = new CSVField(text[idx].toString() + "\n");
					text[idx] = new CSVField(text[idx].toString() + " " +rf.getLabelWithType());
				}
				if (examType == null)
                    for (Iterator it = new TreeSet(location.getDepartmentRoomFeatures()).iterator(); it.hasNext();) {
                        DepartmentRoomFeature drf = (DepartmentRoomFeature) it.next();
						if (!depts.contains(drf.getDepartment())) continue;
                        boolean skip = true;
                        for (Iterator j=location.getRoomDepts().iterator();j.hasNext();) {
                            RoomDept rd = (RoomDept)j.next();
                            if (drf.getDepartment().equals(rd.getDepartment())) { skip=false; break; }
                        }
                        if (skip) continue;
                        text[idx] = new CSVField(text[idx].toString() + (text[idx].toString().isEmpty() ? "" : "\n") +drf.getLabelWithType());
                    }
				idx++;
			} else {
				// get features columns
				for (Iterator it = globalRoomFeatures.iterator(); it.hasNext();) {
				    GlobalRoomFeature grf = (GlobalRoomFeature) it.next();
					boolean b = location.hasFeature(grf);
					text[idx] = new CSVField(b ? "Yes" : "No");
					idx++;
				}
				for (Iterator it = deptRoomFeatures.iterator(); it.hasNext();) {
				    DepartmentRoomFeature drf = (DepartmentRoomFeature) it.next();
                    boolean b = location.hasFeature(drf);
                    for (Iterator j=location.getRoomDepts().iterator();j.hasNext();) {
                        RoomDept rd = (RoomDept)j.next();
                        if (drf.getDepartment().equals(rd.getDepartment())) { b=false; break; }
                    }
					text[idx] = new CSVField(b ? "Yes" : "No");
					idx++;
				}
			}
			
			// build rows
            table.addLine(text);
		}

		// set request attributes
		for (RoomType t : roomTypes) {
		    CSVFile table = tables.get(t);
		    if (table.getLines() == null || table.getLines().isEmpty()) continue;
		    out.println(t.getLabel());
		    out.println(table.getHeader().toString());
		    for (CSVLine l: table.getLines())
		    	out.println(l.toString());
		    out.println();
		}
	}

}
