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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
import org.apache.struts.action.ActionMessages;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Order;
import org.unitime.commons.Debug;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.commons.web.WebTable;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.form.RoomFeatureListForm;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentRoomFeature;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.GlobalRoomFeature;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.Roles;
import org.unitime.timetable.model.RoomDept;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.dao.RoomFeatureDAO;
import org.unitime.timetable.model.dao.TimetableManagerDAO;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.LookupTables;
import org.unitime.timetable.util.PdfEventHandler;
import org.unitime.timetable.webutil.PdfWebTable;

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
 * @struts:action path="/roomFeatureList" name="roomFeatureListForm"
 *                input="/admin/roomFeatureList.jsp" scope="request"
 *                validate="true"
 */
public class RoomFeatureListAction extends Action {

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
		RoomFeatureListForm roomFeatureListForm = (RoomFeatureListForm) form;

		HttpSession webSession = request.getSession();
		if (!Web.isLoggedIn(webSession)) {
			throw new Exception("Access Denied.");
		}
		
		User user = Web.getUser(webSession);
		Long sessionId = org.unitime.timetable.model.Session.getCurrentAcadSession(user).getSessionId();
		ActionMessages errors = new ActionMessages();
		
		//get deptCode from request - for user with only one department
		String deptCode = (String)request.getAttribute("deptCode");
		if (deptCode != null) {
			roomFeatureListForm.setDeptCodeX(deptCode);
		}

		if (webSession.getAttribute(Constants.DEPT_CODE_ATTR_ROOM_NAME) != null
				&& (roomFeatureListForm.getDeptCodeX() == null)) {
			roomFeatureListForm.setDeptCodeX(webSession.getAttribute(
					Constants.DEPT_CODE_ATTR_ROOM_NAME).toString());
		}

		// Set Session Variable
		if (!roomFeatureListForm.getDeptCodeX().equalsIgnoreCase("")) {
			webSession.setAttribute(Constants.DEPT_CODE_ATTR_ROOM_NAME,
					roomFeatureListForm.getDeptCodeX());
		}

		// Validate input
		errors = roomFeatureListForm.validate(mapping, request);

		// Validation fails
		if (errors.size() > 0) {
			saveErrors(request, errors);
			return mapping.findForward("showRoomFeatureSearch");
		}
		
		String mgrId = (String)user.getAttribute(Constants.TMTBL_MGR_ID_ATTR_NAME);
		TimetableManagerDAO tdao = new TimetableManagerDAO();
        TimetableManager mgr = tdao.get(new Long(mgrId));
        boolean isAdmin = user.getRole().equals(Roles.ADMIN_ROLE);

		ArrayList globalRoomFeatures = new ArrayList();
		Set departmentRoomFeatures = new HashSet();
		
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

			list = hibSession
					.createQuery("select distinct f from DepartmentRoomFeature f where f.department.session=:sessionId order by label")
					.setLong("sessionId", sessionId.longValue())
					.list();
			
			Set departments = mgr.departmentsForSession(sessionId);
			for (Iterator i1 = list.iterator();i1.hasNext();) {
				DepartmentRoomFeature rf = (DepartmentRoomFeature) i1.next();
				if (rf.getDeptCode()==null) continue;
				if (roomFeatureListForm.getDeptCodeX().equalsIgnoreCase("All")) {
					if (departments.contains(rf.getDepartment()) || isAdmin) {
						departmentRoomFeatures.add(rf);
					}
				} else if (roomFeatureListForm.getDeptCodeX().equalsIgnoreCase("Exam")) {
                } else if (roomFeatureListForm.getDeptCodeX().equalsIgnoreCase("EExam")) {
				} else if (rf.getDeptCode().equalsIgnoreCase(roomFeatureListForm.getDeptCodeX())) {
					departmentRoomFeatures.add(rf);
				}
			}
			
		} catch (Exception e) {
			Debug.error(e);
		}
		
		roomFeatureListForm.setGlobalRoomFeatures(globalRoomFeatures);
		roomFeatureListForm.setDepartmentRoomFeatures(departmentRoomFeatures);

		buildFeatureTable(request, roomFeatureListForm);
		
		//set request attribute for department
		LookupTables.setupDeptsForUser(request, user, sessionId, true);
		
		if (user.getRole().equals(Roles.ADMIN_ROLE))
			roomFeatureListForm.setCanAdd(true);
		else if (Constants.ALL_OPTION_LABEL.equals(roomFeatureListForm.getDeptCodeX())) {
			roomFeatureListForm.setCanAdd(false);
	        for (Iterator i=mgr.departmentsForSession(sessionId).iterator();i.hasNext();) {
	        	Department d = (Department)i.next();
	        	if (d.isEditableBy(user)) {
	        		roomFeatureListForm.setCanAdd(true);
	        		break;
	        	}
	        }
		} else {
			roomFeatureListForm.setCanAdd(false);
	        for (Iterator i=mgr.departmentsForSession(sessionId).iterator();i.hasNext();) {
	        	Department d = (Department)i.next();
	        	if (d.getDeptCode().equals(roomFeatureListForm.getDeptCodeX()) && d.isEditableBy(user)) {
	        		roomFeatureListForm.setCanAdd(true);
	        		break;
	        	}
	        }
		}

		if ("Export PDF".equals(request.getParameter("op"))) {
			buildPdfFeatureTable(request, roomFeatureListForm);
		}

		return mapping.findForward("showRoomFeatureList");

	}

	/**
	 * 
	 * @param request
	 * @param roomFeatureListForm
	 * @throws Exception
	 */
	private void buildFeatureTable(HttpServletRequest request,
			RoomFeatureListForm roomFeatureListForm) throws Exception {

		HttpSession webSession = request.getSession();
		User user = Web.getUser(webSession);
		Long sessionId = Session.getCurrentAcadSession(user).getSessionId();
		
		String mgrId = (String)user.getAttribute(Constants.TMTBL_MGR_ID_ATTR_NAME);
		TimetableManagerDAO tdao = new TimetableManagerDAO();
        TimetableManager manager = tdao.get(new Long(mgrId));
        TimetableManager owner = manager;
		
        WebTable.setOrder(request.getSession(),"roomFeatureList.gord",request.getParameter("gord"),1);
        WebTable.setOrder(request.getSession(),"roomFeatureList.mord",request.getParameter("mord"),1);
        
		WebTable globalWebTable = new WebTable(4, "Global Room Features", "roomFeatureList.do?gord=%%", new String[] {
				"Name", "Abbreviation", "", "Rooms" },
				new String[] { "left", "left", "left", "left" }, new boolean[] { true, true, true, true});

		WebTable departmentWebTable = new WebTable(4, "Department Room Features", "roomFeatureList.do?mord=%%", new String[] {
				"Name", "Abbreviation", "Department", "Rooms" },
				new String[] { "left", "left", "left", "left" }, new boolean[] { true, true, true, true});
		
		boolean isAdmin = user.getRole().equals(Roles.ADMIN_ROLE);
		boolean showAll = false;
		Set depts = null;
        int examType = -1;
        if (roomFeatureListForm.getDeptCodeX().equalsIgnoreCase("exam")) examType = Exam.sExamTypeFinal;
        if (roomFeatureListForm.getDeptCodeX().equalsIgnoreCase("eexam")) examType = Exam.sExamTypeEvening;
		if (roomFeatureListForm.getDeptCodeX().equalsIgnoreCase("All")) {
			if (isAdmin) {
				showAll = true;
				depts = Department.findAll(sessionId);
			} else {
				depts = Department.findAllOwned(sessionId, owner, false);
			}
		} else if (roomFeatureListForm.getDeptCodeX().equalsIgnoreCase("Exam")) {
		    depts = new HashSet(0);
        } else if (roomFeatureListForm.getDeptCodeX().equalsIgnoreCase("EExam")) {
            depts = new HashSet(0);
		} else {
			depts = new HashSet(1);
			depts.add(Department.findByDeptCode(roomFeatureListForm.getDeptCodeX(),sessionId));
		}
		
		
		// build global room features rows
		Collection globalRoomFeatures = roomFeatureListForm.getGlobalRoomFeatures();
		boolean haveGlobalRoomFeature = false;
		for (Iterator it = globalRoomFeatures.iterator(); it.hasNext();) {
			GlobalRoomFeature gr = (GlobalRoomFeature) it.next();
			Collection rs = new TreeSet(gr.getRooms());
			Debug.debug(gr.getLabel() + " has " + rs.size() + " rooms");

			// get rooms
			StringBuffer assignedRoom = new StringBuffer();
			
			boolean haveRooms = false;
			for (Iterator iter = rs.iterator();iter.hasNext();) {
				Location r = (Location) iter.next();
				if (!sessionId.equals(r.getSession().getUniqueId())) continue;
				if (examType>=0) {
				    if (!r.isExamEnabled(examType)) continue;
				} else {
	                if (!showAll) {
	                    boolean skip = true;
	                    for (Iterator j=r.getRoomDepts().iterator();j.hasNext();) {
	                        RoomDept rd = (RoomDept)j.next();
	                        if (depts.contains(rd.getDepartment())) { skip=false; break; }
	                    }
	                    if (skip) continue;
	                }
				}
				if (assignedRoom.length() > 0) assignedRoom.append(", ");
				assignedRoom.append(r.getLabel().replaceAll(" ","&nbsp;"));
				haveRooms = true;
			}
			
			if (!isAdmin && !haveRooms) continue;

			globalWebTable.addLine(
					(isAdmin?"onClick=\"document.location='roomFeatureEdit.do?doit=editRoomFeature&id=" + gr.getUniqueId() + "';\"":null), 
					new String[] {
							"<A name=\"A"+gr.getUniqueId()+"\"></A>"+
							(isAdmin?"":"<font color=gray>")+gr.getLabel().replaceAll(" ","&nbsp;")+(isAdmin?"":"</font>"),
                            (isAdmin?"":"<font color=gray>")+gr.getAbbv().replaceAll(" ","&nbsp;")+(isAdmin?"":"</font>"),
							"",
							(isAdmin?"":"<font color=gray>")+assignedRoom+(isAdmin?"":"</font>") 
							 }, 
					new Comparable[] {
							gr.getLabel(),
                            gr.getAbbv(),
							"",
							null});
			haveGlobalRoomFeature = true;
		}

		// build department room features rows
		Collection departmentRoomFeatures = roomFeatureListForm
				.getDepartmentRoomFeatures();
		for (Iterator it = departmentRoomFeatures.iterator(); it.hasNext();) {
			DepartmentRoomFeature drf = (DepartmentRoomFeature) it.next();
			Department rfOwner = Department.findByDeptCode(drf.getDeptCode(), sessionId);

			boolean isOwner = isAdmin || owner.getDepartments().contains(rfOwner);
			boolean isEditable = rfOwner.isEditableBy(user);
			String ownerName = "<i>Not defined</i>";
			if (rfOwner != null) {
				ownerName = "<span title='"+rfOwner.getHtmlTitle()+"'>"+
					rfOwner.getShortLabel()+
					"</span>";
			}
				
			
			Collection rs = new TreeSet(drf.getRooms());

			// get rooms
			StringBuffer assignedRoom = new StringBuffer();
			
			for (Iterator iter = rs.iterator();iter.hasNext();) {
				Location r = (Location) iter.next();
                if (examType>=0) {
                    if (!r.isExamEnabled(examType)) continue;
                } else {
                    boolean skip = true;
                    for (Iterator j=r.getRoomDepts().iterator();j.hasNext();) {
                        RoomDept rd = (RoomDept)j.next();
                        if (drf.getDepartment().equals(rd.getDepartment())) { skip=false; break; }
                    }
                    if (skip) continue;
                }
				if (assignedRoom.length() > 0) assignedRoom.append(", ");
				assignedRoom.append(r.getLabel().replaceAll(" ","&nbsp;"));
			}

			departmentWebTable.addLine(
					(isEditable?"onClick=\"document.location='roomFeatureEdit.do?doit=editRoomFeature&id=" + drf.getUniqueId() + "';\"":null), 
						new String[] {
							"<A name=\"A"+drf.getUniqueId()+"\"></A>"+
							(isOwner?"":"<font color=gray>")+drf.getLabel().replaceAll(" ","&nbsp;")+(isOwner?"":"</font>"),
                            (isOwner?"":"<font color=gray>")+drf.getAbbv().replaceAll(" ","&nbsp;")+(isOwner?"":"</font>"),
							(isOwner?"":"<font color=gray>")+ownerName+(isOwner?"":"</font>"),
							(isOwner?"":"<font color=gray>")+assignedRoom+(isOwner?"":"</font>")},
						new Comparable[] {
								drf.getLabel(),
                                drf.getAbbv(),
								ownerName,
								null}
						);
		}

		// set request attributes
		if (haveGlobalRoomFeature)
			request.setAttribute("roomFeaturesGlobal", globalWebTable.printTable(WebTable.getOrder(request.getSession(),"roomFeatureList.gord")));
		else
			request.removeAttribute("roomFeaturesGlobal");
		if (!departmentRoomFeatures.isEmpty())
			request.setAttribute("roomFeaturesDepartment", departmentWebTable.printTable(WebTable.getOrder(request.getSession(),"roomFeatureList.mord")));
		else
			request.removeAttribute("roomFeaturesDepartment");
	}
	
	/**
	 * 
	 * @param request
	 * @param roomFeatureEditForm
	 * @throws Exception 
	 */
	private String getDeptCode(HttpServletRequest request) throws Exception {
		HttpSession webSession = request.getSession();
		User user = Web.getUser(webSession);
		boolean isAdmin = user.getRole().equals(Roles.ADMIN_ROLE);
		Long sessionId = Session.getCurrentAcadSession(user).getUniqueId();		
		String mgrId = (String)user.getAttribute(Constants.TMTBL_MGR_ID_ATTR_NAME);
		TimetableManagerDAO tdao = new TimetableManagerDAO();
        TimetableManager manager = tdao.get(new Long(mgrId));  
		Set departments = new TreeSet(manager.departmentsForSession(sessionId));
        
        //set default department
        if (!isAdmin && (departments.size() == 1)) {
        	Department d = (Department) departments.iterator().next();
        	return d.getDeptCode();
        } else if (webSession.getAttribute(Constants.DEPT_CODE_ATTR_ROOM_NAME) != null) {
        	String code = webSession.getAttribute(Constants.DEPT_CODE_ATTR_ROOM_NAME).toString();
        	return code;
		} else {
			return null;
		}
	}
	

	public static void buildPdfFeatureTable(HttpServletRequest request, RoomFeatureListForm roomFeatureListForm) throws Exception {
    	FileOutputStream out = null;
    	try {
    		File file = ApplicationProperties.getTempFile("room_features", "pdf");
    		
    		out = new FileOutputStream(file);
    		HttpSession webSession = request.getSession();
    		User user = Web.getUser(webSession);
    		Long sessionId = Session.getCurrentAcadSession(user).getSessionId();
    		
    		String mgrId = (String)user.getAttribute(Constants.TMTBL_MGR_ID_ATTR_NAME);
    		TimetableManagerDAO tdao = new TimetableManagerDAO();
            TimetableManager manager = tdao.get(new Long(mgrId));
            TimetableManager owner = manager;
    		
    		PdfWebTable globalWebTable = new PdfWebTable(4, "Global Room Features", null, new String[] {
    				"Name", "Abbreviation", "", "Rooms" },
    				new String[] { "left", "left", "left", "left" }, new boolean[] { true, true, true, true});

    		PdfWebTable departmentWebTable = new PdfWebTable(4, "Department Room Features", null, new String[] {
    				"Name", "Abbreviation", "Department ", "Rooms" },
    				new String[] { "left", "left", "left", "left" }, new boolean[] { true, true, true, true});
    		
    		boolean isAdmin = user.getRole().equals(Roles.ADMIN_ROLE);
    		boolean showAll = false;
    		Set depts = null;
    		if (roomFeatureListForm.getDeptCodeX().equalsIgnoreCase("All")) {
    			if (isAdmin) {
    				showAll = true;
    				depts = Department.findAll(sessionId);
    			} else {
    				depts = Department.findAllOwned(sessionId, owner, false);
    			}
    		} else if (roomFeatureListForm.getDeptCodeX().equalsIgnoreCase("Exam")) {
            } else if (roomFeatureListForm.getDeptCodeX().equalsIgnoreCase("EExam")) {
    		} else {
    			depts = new HashSet(1);
    			depts.add(Department.findByDeptCode(roomFeatureListForm.getDeptCodeX(),sessionId));
    		}
    		
            int examType = -1;
            if (roomFeatureListForm.getDeptCodeX().equalsIgnoreCase("exam")) examType = Exam.sExamTypeFinal;
            if (roomFeatureListForm.getDeptCodeX().equalsIgnoreCase("eexam")) examType = Exam.sExamTypeEvening;
    		
    		boolean splitRows = false;
    		
    		// build global room features rows
    		Collection globalRoomFeatures = roomFeatureListForm.getGlobalRoomFeatures();
    		boolean haveGlobalRoomFeature = false;
    		for (Iterator it = globalRoomFeatures.iterator(); it.hasNext();) {
    			GlobalRoomFeature gr = (GlobalRoomFeature) it.next();
    			Collection rs = new TreeSet(gr.getRooms());

    			// get rooms
    			StringBuffer assignedRoom = new StringBuffer();
    			int nrRows = 0;
    			
    			boolean haveRooms = false;
    			for (Iterator iter = rs.iterator();iter.hasNext();) {
    				Location r = (Location) iter.next();
    				if (!sessionId.equals(r.getSession().getUniqueId())) continue;
                    if (examType>=0) {
                        if (!r.isExamEnabled(examType)) continue;
                    } else {
                        if (!showAll) {
                            boolean skip = true;
                            for (Iterator j=r.getRoomDepts().iterator();j.hasNext();) {
                                RoomDept rd = (RoomDept)j.next();
                                if (depts.contains(rd.getDepartment())) { skip=false; break; }
                            }
                            if (skip) continue;
                        }
                    }
    				if (assignedRoom.length()>0) assignedRoom.append(", ");
    				if (PdfWebTable.getWidthOfLastLine(assignedRoom.toString(),false,false)>750) {
    					assignedRoom.append("\n");
    					nrRows++;
    				}
    				assignedRoom.append(r.getLabel());
    				haveRooms = true;
    			}
    			
    			if (nrRows>40) splitRows=true;
    			
    			if (!isAdmin && !haveRooms) continue;

    			globalWebTable.addLine(
    					null, 
    					new String[] {
    							gr.getLabel(),
                                gr.getAbbv(),
    							"",
    							assignedRoom.toString() 
    							 }, 
    					new Comparable[] {
    							gr.getLabel(),
                                gr.getAbbv(),
    							"",
    							null});
    			haveGlobalRoomFeature = true;
    		}

    		// build department room features rows
    		Collection departmentRoomFeatures = roomFeatureListForm
    				.getDepartmentRoomFeatures();
    		for (Iterator it = departmentRoomFeatures.iterator(); it.hasNext();) {
    			DepartmentRoomFeature drf = (DepartmentRoomFeature) it.next();
    			Department rfOwner = Department.findByDeptCode(drf.getDeptCode(), sessionId);

    			boolean isOwner = isAdmin || owner.getDepartments().contains(rfOwner);
    			boolean isEditable = rfOwner.isEditableBy(user);
    			String ownerName = "@@ITALIC Not defined";
    			if (rfOwner != null) {
    				ownerName = rfOwner.getShortLabel();
    			}
    				
    			
    			Collection rs = new TreeSet(drf.getRooms());

    			// get rooms
    			StringBuffer assignedRoom = new StringBuffer();
    			
    			for (Iterator iter = rs.iterator();iter.hasNext();) {
    				Location r = (Location) iter.next();
                    if (examType>=0) {
                        if (!r.isExamEnabled(examType)) continue;
                    } else {
                        boolean skip = true;
                        for (Iterator j=r.getRoomDepts().iterator();j.hasNext();) {
                            RoomDept rd = (RoomDept)j.next();
                            if (drf.getDepartment().equals(rd.getDepartment())) { skip=false; break; }
                        }
                        if (skip) continue;
                    }
    				if (assignedRoom.length() > 0) assignedRoom.append(", ");
    				if (PdfWebTable.getWidthOfLastLine(assignedRoom.toString(),false,false)>750)
    					assignedRoom.append("\n");
    				assignedRoom.append(r.getLabel());
    			}

    			departmentWebTable.addLine(
    					null, 
    						new String[] {
    							drf.getLabel(),
                                drf.getAbbv(),
    							ownerName,
    							assignedRoom.toString()},
    						new Comparable[] {
    								drf.getLabel(),
                                    drf.getAbbv(),
    								ownerName,
    								null}
    						);
    		}

    		Document doc = null;
    		
    		if (haveGlobalRoomFeature) {
				PdfWebTable table = globalWebTable;
    			PdfPTable pdfTable = table.printPdfTable(WebTable.getOrder(request.getSession(),"roomFeatureList.gord"));
    			pdfTable.setSplitRows(splitRows);
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
    		
    		if (!departmentRoomFeatures.isEmpty()) {
				PdfWebTable table = departmentWebTable;
    			PdfPTable pdfTable = table.printPdfTable(WebTable.getOrder(request.getSession(),"roomFeatureList.mord"));
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