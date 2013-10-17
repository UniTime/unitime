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

import java.io.OutputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;
import org.hibernate.HibernateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unitime.commons.web.WebTable;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.form.RoomFeatureListForm;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentRoomFeature;
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.GlobalRoomFeature;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.RoomDept;
import org.unitime.timetable.model.RoomFeature;
import org.unitime.timetable.model.RoomFeatureType;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.ExportUtils;
import org.unitime.timetable.util.LookupTables;
import org.unitime.timetable.util.PdfEventHandler;
import org.unitime.timetable.util.PdfFont;
import org.unitime.timetable.webutil.PdfWebTable;

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
 * @struts:action path="/roomFeatureList" name="roomFeatureListForm"
 *                input="/admin/roomFeatureList.jsp" scope="request"
 *                validate="true"
 *
 * @author Tomas Muller
 */
@Service("/roomFeatureList")
public class RoomFeatureListAction extends Action {
	
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
		RoomFeatureListForm roomFeatureListForm = (RoomFeatureListForm) form;

		sessionContext.checkPermission(Right.RoomFeatures);

		ActionMessages errors = new ActionMessages();
		
		//get deptCode from request - for user with only one department
		String deptCode = (String)request.getAttribute("deptCode");
		if (deptCode != null) {
			roomFeatureListForm.setDeptCodeX(deptCode);
		}

		if (sessionContext.getAttribute(SessionAttribute.DepartmentCodeRoom) != null && roomFeatureListForm.getDeptCodeX() == null) {
			deptCode = (String)sessionContext.getAttribute(SessionAttribute.DepartmentCodeRoom);
			if (deptCode != null && ("All".equals(deptCode) || deptCode.matches("Exam[0-9]*") || sessionContext.hasPermission(deptCode, "Department", Right.RoomFeatures)))
				roomFeatureListForm.setDeptCodeX((String)sessionContext.getAttribute(SessionAttribute.DepartmentCodeRoom));
		}

		// Set Session Variable
		if (roomFeatureListForm.getDeptCodeX() != null && !roomFeatureListForm.getDeptCodeX().isEmpty())
			sessionContext.setAttribute(SessionAttribute.DepartmentCodeRoom, roomFeatureListForm.getDeptCodeX());
		
		// Validate input
		errors = roomFeatureListForm.validate(mapping, request);

		//set request attribute for department
		LookupTables.setupDepartments(request, sessionContext, true);
		LookupTables.setupExamTypes(request, sessionContext.getUser().getCurrentAcademicSessionId());

		// Validation fails
		if (errors.size() > 0) {
			saveErrors(request, errors);
			return mapping.findForward("showRoomFeatureSearch");
		}
		
		roomFeatureListForm.setGlobalRoomFeatures(RoomFeature.getAllGlobalRoomFeatures(sessionContext.getUser().getCurrentAcademicSessionId()));
		
		Set<DepartmentRoomFeature> departmentRoomFeatures = new TreeSet<DepartmentRoomFeature>();
		for (Department d: Department.getUserDepartments(sessionContext.getUser())) {
			if ("All".equals(roomFeatureListForm.getDeptCodeX()) || d.getDeptCode().equals(roomFeatureListForm.getDeptCodeX()))
				departmentRoomFeatures.addAll(RoomFeature.getAllDepartmentRoomFeatures(d));
		}
        if (roomFeatureListForm.getDeptCodeX() != null && !roomFeatureListForm.getDeptCodeX().isEmpty() && !"All".equals(roomFeatureListForm.getDeptCodeX()) && !roomFeatureListForm.getDeptCodeX().matches("Exam[0-9]*")) {
        	Department department = Department.findByDeptCode(roomFeatureListForm.getDeptCodeX(), sessionContext.getUser().getCurrentAcademicSessionId());
        	if (department != null && department.isExternalManager())
        		departmentRoomFeatures.addAll(RoomFeature.getAllDepartmentRoomFeatures(department));
        }
		roomFeatureListForm.setDepartmentRoomFeatures(departmentRoomFeatures);

		buildFeatureTable(request, roomFeatureListForm);
		
		if ("Export PDF".equals(request.getParameter("op"))) {
			sessionContext.checkPermission(Right.RoomFeaturesExportPdf);
			OutputStream out = ExportUtils.getPdfOutputStream(response, "roomFeatures");
			printPdfFeatureTable(out, sessionContext, roomFeatureListForm);
			out.flush(); out.close();
			return null;
		}

		return mapping.findForward("showRoomFeatureList");

	}

	/**
	 * 
	 * @param request
	 * @param roomFeatureListForm
	 * @throws Exception
	 */
	private void buildFeatureTable(HttpServletRequest request, RoomFeatureListForm roomFeatureListForm) throws Exception {

        WebTable.setOrder(sessionContext,"roomFeatureList.gord",request.getParameter("gord"),1);
        WebTable.setOrder(sessionContext,"roomFeatureList.mord",request.getParameter("mord"),1);
        
        boolean hasTypes = RoomFeatureType.hasFeatureTypes(sessionContext.getUser().getCurrentAcademicSessionId());
        
		WebTable globalWebTable = new WebTable(5, "Global Room Features", "roomFeatureList.do?gord=%%", new String[] {
				"Name", "Abbreviation", hasTypes ? "Type" : "", "", "Rooms" },
				new String[] { "left", "left", "left", "left", "left" }, new boolean[] { true, true, true, true, true});

		WebTable departmentWebTable = new WebTable(5, "Department Room Features", "roomFeatureList.do?mord=%%", new String[] {
				"Name", "Abbreviation", hasTypes ? "Type" : "", "Department", "Rooms" },
				new String[] { "left", "left", "left", "left", "left" }, new boolean[] { true, true, true, true, true});
		
		Set<Department> depts = Department.getUserDepartments(sessionContext.getUser());
        Long examType = null;
        Department department = null;
        if ("Exam".equals(roomFeatureListForm.getDeptCodeX())) {
			TreeSet<ExamType> types = ExamType.findAllUsed(sessionContext.getUser().getCurrentAcademicSessionId());
			if (!types.isEmpty()) {
				examType =  types.first().getUniqueId();
				roomFeatureListForm.setDeptCodeX("Exam" + examType);
			}
        } else if (roomFeatureListForm.getDeptCodeX() != null && roomFeatureListForm.getDeptCodeX().matches("Exam[0-9]*"))
        	examType = Long.valueOf(roomFeatureListForm.getDeptCodeX().substring(4));
        else if (roomFeatureListForm.getDeptCodeX() != null && !roomFeatureListForm.getDeptCodeX().isEmpty() && !"All".equals(roomFeatureListForm.getDeptCodeX()))
        	department = Department.findByDeptCode(roomFeatureListForm.getDeptCodeX(), sessionContext.getUser().getCurrentAcademicSessionId());
        boolean deptCheck = examType == null && !sessionContext.getUser().getCurrentAuthority().hasRight(Right.DepartmentIndependent);
        if (department != null) {
        	deptCheck = true; depts = new TreeSet<Department>(); depts.add(department);
        }
        
		// build global room features rows
		Collection globalRoomFeatures = roomFeatureListForm.getGlobalRoomFeatures();
		boolean haveGlobalRoomFeature = false;
		for (Iterator it = globalRoomFeatures.iterator(); it.hasNext();) {
			GlobalRoomFeature gr = (GlobalRoomFeature) it.next();
			Collection rs = new TreeSet(gr.getRooms());

			// get rooms
			StringBuffer assignedRoom = new StringBuffer();
			
			boolean haveRooms = false;
			for (Iterator iter = rs.iterator();iter.hasNext();) {
				Location r = (Location) iter.next();
				if (examType != null && !r.isExamEnabled(examType)) continue;
				if (deptCheck) {
					boolean skip = true;
					for (RoomDept rd: r.getRoomDepts())
						if (depts.contains(rd.getDepartment())) { skip = false; break; }
					if (skip) continue;
				}
				if (assignedRoom.length() > 0) assignedRoom.append(", ");
				assignedRoom.append(r.getLabel().replaceAll(" ","&nbsp;"));
				haveRooms = true;
			}
			
			boolean editable = sessionContext.hasPermission(gr, Right.GlobalRoomFeatureEdit);
			
			if (!haveRooms && !editable) continue;

			globalWebTable.addLine(
					(editable ? "onClick=\"document.location='roomFeatureEdit.do?doit=editRoomFeature&id=" + gr.getUniqueId() + "';\"":null), 
					new String[] {
							"<A name=\"A"+gr.getUniqueId()+"\"></A>"+
							(editable?"":"<font color=gray>")+gr.getLabel().replaceAll(" ","&nbsp;")+(editable?"":"</font>"),
                            (editable?"":"<font color=gray>")+gr.getAbbv().replaceAll(" ","&nbsp;")+(editable?"":"</font>"),
                            gr.getFeatureType() == null ? "" : ((editable?"":"<font color=gray>")+gr.getFeatureType().getLabel().replaceAll(" ","&nbsp;")+(editable?"":"</font>")),
							"",
							(editable?"":"<font color=gray>")+assignedRoom+(editable?"":"</font>") 
							 }, 
					new Comparable[] {
							gr.getLabel(),
                            gr.getAbbv(),
                            gr.getFeatureType() == null ? "" : gr.getFeatureType().getLabel(),
							"",
							null});
			haveGlobalRoomFeature = true;
		}

		// build department room features rows
		Collection departmentRoomFeatures = roomFeatureListForm.getDepartmentRoomFeatures();
		for (Iterator it = departmentRoomFeatures.iterator(); it.hasNext();) {
			DepartmentRoomFeature drf = (DepartmentRoomFeature) it.next();
			
			boolean editable = sessionContext.hasPermission(drf, Right.DepartmenalRoomFeatureEdit);
			
			String ownerName = "<span title='"+drf.getDepartment().getHtmlTitle()+"'>" + drf.getDepartment().getShortLabel() + "</span>";

			Collection rs = new TreeSet(drf.getRooms());

			// get rooms
			StringBuffer assignedRoom = new StringBuffer();
			
			for (Iterator iter = rs.iterator();iter.hasNext();) {
				Location r = (Location) iter.next();
                if (examType != null) {
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
					(editable?"onClick=\"document.location='roomFeatureEdit.do?doit=editRoomFeature&id=" + drf.getUniqueId() + "';\"":null), 
						new String[] {
							"<A name=\"A"+drf.getUniqueId()+"\"></A>"+
							(editable?"":"<font color=gray>")+drf.getLabel().replaceAll(" ","&nbsp;")+(editable?"":"</font>"),
                            (editable?"":"<font color=gray>")+drf.getAbbv().replaceAll(" ","&nbsp;")+(editable?"":"</font>"),
                            drf.getFeatureType() == null ? "" :  ((editable?"":"<font color=gray>")+drf.getFeatureType().getLabel().replaceAll(" ","&nbsp;")+(editable?"":"</font>")),
							(editable?"":"<font color=gray>")+ownerName+(editable?"":"</font>"),
							(editable?"":"<font color=gray>")+assignedRoom+(editable?"":"</font>")},
						new Comparable[] {
								drf.getLabel(),
                                drf.getAbbv(),
                                drf.getFeatureType() == null ? "" : drf.getFeatureType().getLabel(),
								ownerName,
								null}
						);
		}

		// set request attributes
		if (haveGlobalRoomFeature)
			request.setAttribute("roomFeaturesGlobal", globalWebTable.printTable(WebTable.getOrder(sessionContext,"roomFeatureList.gord")));
		else
			request.removeAttribute("roomFeaturesGlobal");
		if (!departmentRoomFeatures.isEmpty())
			request.setAttribute("roomFeaturesDepartment", departmentWebTable.printTable(WebTable.getOrder(sessionContext,"roomFeatureList.mord")));
		else
			request.removeAttribute("roomFeaturesDepartment");
	}
	
	public static void printPdfFeatureTable(OutputStream out, SessionContext context, RoomFeatureListForm roomFeatureListForm) throws Exception {
		boolean hasTypes = RoomFeatureType.hasFeatureTypes(context.getUser().getCurrentAcademicSessionId());
		
        PdfWebTable globalWebTable = new PdfWebTable(5, "Global Room Features", null, new String[] {
				"Name", "Abbreviation", hasTypes ? "Type" : "", "", "Rooms" },
				new String[] { "left", "left", "left", "left", "left" }, new boolean[] { true, true, true, true, true});

		PdfWebTable departmentWebTable = new PdfWebTable(5, "Department Room Features", null, new String[] {
				"Name", "Abbreviation", hasTypes ? "Type" : "", "Department ", "Rooms" },
				new String[] { "left", "left", "left", "left", "left" }, new boolean[] { true, true, true, true, true});

		Set<Department> depts = Department.getUserDepartments(context.getUser());
        Long examType = null;
        Department department = null;
        if (roomFeatureListForm.getDeptCodeX() != null && roomFeatureListForm.getDeptCodeX().matches("Exam[0-9]*"))
        	examType = Long.valueOf(roomFeatureListForm.getDeptCodeX().substring(4));
        else if (roomFeatureListForm.getDeptCodeX() != null && !roomFeatureListForm.getDeptCodeX().isEmpty() && !"All".equals(roomFeatureListForm.getDeptCodeX()))
        	department = Department.findByDeptCode(roomFeatureListForm.getDeptCodeX(), context.getUser().getCurrentAcademicSessionId());
        boolean deptCheck = examType == null && !context.getUser().getCurrentAuthority().hasRight(Right.DepartmentIndependent);
        if (department != null) {
        	deptCheck = true; depts = new TreeSet<Department>(); depts.add(department);
        }
		
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
				if (examType != null && !r.isExamEnabled(examType)) continue;
				if (deptCheck) {
					boolean skip = true;
					for (RoomDept rd: r.getRoomDepts())
						if (depts.contains(rd.getDepartment())) { skip = false; break; }
					if (skip) continue;
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
			
			if (!haveRooms && !context.hasPermission(gr, Right.GlobalRoomFeatureEdit)) continue;

			globalWebTable.addLine(
					null, 
					new String[] {
							gr.getLabel(),
                            gr.getAbbv(),
                            gr.getFeatureType() == null ? "" : gr.getFeatureType().getLabel(),
							"",
							assignedRoom.toString() 
							 }, 
					new Comparable[] {
							gr.getLabel(),
                            gr.getAbbv(),
                            gr.getFeatureType() == null ? "" : gr.getFeatureType().getLabel(),
							"",
							null});
			haveGlobalRoomFeature = true;
		}

		// build department room features rows
		Collection departmentRoomFeatures = roomFeatureListForm
				.getDepartmentRoomFeatures();
		for (Iterator it = departmentRoomFeatures.iterator(); it.hasNext();) {
			DepartmentRoomFeature drf = (DepartmentRoomFeature) it.next();
			
			String ownerName = drf.getDepartment().getShortLabel();

			Collection rs = new TreeSet(drf.getRooms());

			// get rooms
			StringBuffer assignedRoom = new StringBuffer();
			
			for (Iterator iter = rs.iterator();iter.hasNext();) {
				Location r = (Location) iter.next();
                if (examType != null) {
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
                            drf.getFeatureType() == null ? "" : drf.getFeatureType().getLabel(),
							ownerName,
							assignedRoom.toString()},
						new Comparable[] {
								drf.getLabel(),
                                drf.getAbbv(),
                                drf.getFeatureType() == null ? "" : drf.getFeatureType().getLabel(),
								ownerName,
								null}
						);
		}

		Document doc = null;
		
		if (haveGlobalRoomFeature) {
			PdfWebTable table = globalWebTable;
			PdfPTable pdfTable = table.printPdfTable(WebTable.getOrder(context,"roomFeatureList.gord"));
			pdfTable.setSplitRows(splitRows);
			if (doc==null) {
				doc = new Document(new Rectangle(60f + table.getWidth(), 60f + 0.75f * table.getWidth()),30,30,30,30);
				PdfWriter iWriter = PdfWriter.getInstance(doc, out);
				iWriter.setPageEvent(new PdfEventHandler());
				doc.open();
			}
			doc.add(new Paragraph(table.getName(),	PdfFont.getBigFont(true)));
			doc.add(pdfTable);
		}
		
		if (!departmentRoomFeatures.isEmpty()) {
			PdfWebTable table = departmentWebTable;
			PdfPTable pdfTable = table.printPdfTable(WebTable.getOrder(context,"roomFeatureList.mord"));
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
		
		if (doc!=null) doc.close();
	}
	
	
}
