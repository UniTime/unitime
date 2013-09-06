/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2008 - 2010, UniTime LLC, and individual contributors
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unitime.commons.web.WebTable;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.form.RoomGroupListForm;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.RoomDept;
import org.unitime.timetable.model.RoomGroup;
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
 * MyEclipse Struts
 * Creation date: 05-02-2006
 * 
 * XDoclet definition:
 * @struts.action path="/roomGroupList" name="roomGroupListForm" input="/admin/roomGroupList.jsp" parameter="doit" scope="request" validate="true"
 * @struts.action-forward name="showRoomGroupList" path="roomGroupListTile"
 * @struts.action-forward name="showAdd" path="/roomGroupEdit.do"
 */
@Service("/roomGroupList")
public class RoomGroupListAction extends Action {
	
	@Autowired SessionContext sessionContext;

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
		RoomGroupListForm roomGroupListForm = (RoomGroupListForm) form;
		
		sessionContext.checkPermission(Right.RoomGroups);
		
		ActionMessages errors = new ActionMessages();
		
		//get deptCode from request - for user with only one department
		String deptCode = (String)request.getAttribute("deptCode");
		if (deptCode != null) {
			roomGroupListForm.setDeptCodeX(deptCode);
		}
		
		if (sessionContext.getAttribute(SessionAttribute.DepartmentCodeRoom) != null && roomGroupListForm.getDeptCodeX() == null) {
			deptCode = (String)sessionContext.getAttribute(SessionAttribute.DepartmentCodeRoom);
			if (deptCode != null && ("All".equals(deptCode) || deptCode.matches("Exam[0-9]*") || sessionContext.hasPermission(deptCode, "Department", Right.RoomFeatures)))
				roomGroupListForm.setDeptCodeX((String)sessionContext.getAttribute(SessionAttribute.DepartmentCodeRoom));
		}

		// Set Session Variable
		if (roomGroupListForm.getDeptCodeX() != null && !roomGroupListForm.getDeptCodeX().isEmpty())
			sessionContext.setAttribute(SessionAttribute.DepartmentCodeRoom, roomGroupListForm.getDeptCodeX());

		//set request attribute for department
		LookupTables.setupDepartments(request, sessionContext, true);
		LookupTables.setupExamTypes(request, sessionContext.getUser().getCurrentAcademicSessionId());
		
		// Validate input
		errors = roomGroupListForm.validate(mapping, request);

		// Validation fails
		if (errors.size() > 0) {
			saveErrors(request, errors);
			return mapping.findForward("showRoomGroupSearch");
		}

		buildGroupTable(request, roomGroupListForm);
		
		if ("Export PDF".equals(request.getParameter("op"))) {
			sessionContext.checkPermission(Right.RoomGroupsExportPdf);
			OutputStream out = ExportUtils.getPdfOutputStream(response, "roomGroups");
			printPdfGroupTable(out, sessionContext, roomGroupListForm);
			out.flush(); out.close();
			return null;
		}

		return mapping.findForward("showRoomGroupList");
	}

	private void buildGroupTable(HttpServletRequest request, RoomGroupListForm roomGroupListForm) throws Exception{

        WebTable.setOrder(sessionContext,"roomGroupList.gord",request.getParameter("gord"),1);
        WebTable.setOrder(sessionContext,"roomGroupList.mord",request.getParameter("mord"),1);
        
		WebTable globalWebTable = new WebTable(5, "Global Room Groups", "roomGroupList.do?gord=%%", new String[] {
				"Name", "Abbreviation", "Default", "Rooms", "Description" },
				new String[] { "left", "left", "middle", "left", "left" }, 
				new boolean[] { true, true, true, true, true} );
		
		WebTable departmentWebTable = new WebTable(5, "Department Room Groups", "roomGroupList.do?mord=%%", new String[] {
				"Name", "Abbreviation", "Department", "Rooms", "Description" },
				new String[] { "left", "left", "left", "left", "left" }, 
				new boolean[] { true, true, true, true, true} );

		
		Set<Department> depts = Department.getUserDepartments(sessionContext.getUser());
        Long examType = null;
        Department department = null;
        if ("Exam".equals(roomGroupListForm.getDeptCodeX())) {
			TreeSet<ExamType> types = ExamType.findAllUsed(sessionContext.getUser().getCurrentAcademicSessionId());
			if (!types.isEmpty()) {
				examType =  types.first().getUniqueId();
				roomGroupListForm.setDeptCodeX("Exam" + examType);
			}
        } else if (roomGroupListForm.getDeptCodeX() != null && roomGroupListForm.getDeptCodeX().matches("Exam[0-9]*"))
        	examType = Long.valueOf(roomGroupListForm.getDeptCodeX().substring(4));
        else if (roomGroupListForm.getDeptCodeX() != null && !roomGroupListForm.getDeptCodeX().isEmpty() && !"All".equals(roomGroupListForm.getDeptCodeX()))
        	department = Department.findByDeptCode(roomGroupListForm.getDeptCodeX(), sessionContext.getUser().getCurrentAcademicSessionId());
        boolean deptCheck = examType == null && !sessionContext.getUser().getCurrentAuthority().hasRight(Right.DepartmentIndependent);
        if (department != null) {
        	deptCheck = true; depts = new TreeSet<Department>(); depts.add(department);
        }

		boolean haveGlobalRoomGroup = false;
		for (RoomGroup rg: RoomGroup.getAllGlobalRoomGroups(sessionContext.getUser().getCurrentAcademicSessionId())) {
			Collection rs = new TreeSet(rg.getRooms());

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
			
			boolean editable = sessionContext.hasPermission(rg, Right.GlobalRoomGroupEdit);
			
			if (!haveRooms && !editable) continue;
			
			globalWebTable.addLine(
					(editable?"onClick=\"document.location='roomGroupEdit.do?doit=editRoomGroup&id="+rg.getUniqueId()+ "';\"":null), 
					new String[] {
							"<A name=\"A"+rg.getUniqueId()+"\"></A>"+
							(editable?"":"<font color=gray>")+rg.getName().replaceAll(" ","&nbsp;")+(editable?"":"</font>"),
                            (editable?"":"<font color=gray>")+rg.getAbbv().replaceAll(" ","&nbsp;")+(editable?"":"</font>"),
							(rg.isDefaultGroup().booleanValue()?"<IMG border='0' title='This group is default group.' alt='Default' align='absmiddle' src='images/tick.gif'>":""),
							(editable?"":"<font color=gray>")+assignedRoom+(editable?"":"</font>"), 
							(editable?"":"<font color=gray>")+(rg.getDescription() == null ? "" : rg.getDescription()).replaceAll(" ","&nbsp;").replaceAll("\\\n","<BR>")+(editable?"":"</font>") }, 
					new Comparable[] {
							rg.getName(),
                            rg.getAbbv(),
							new Integer(rg.isDefaultGroup().booleanValue()?0:1),
							null,
							(rg.getDescription() == null ? "" : rg.getDescription())
					});
		
			haveGlobalRoomGroup = true;
		}
		
		Set<RoomGroup> departmentRoomGroups = new TreeSet<RoomGroup>();
		for (Department d: Department.getUserDepartments(sessionContext.getUser())) {
			if ("All".equals(roomGroupListForm.getDeptCodeX()) || d.getDeptCode().equals(roomGroupListForm.getDeptCodeX()))
				departmentRoomGroups.addAll(RoomGroup.getAllDepartmentRoomGroups(d));
		}
		if (department != null && department.isExternalManager()) {
			departmentRoomGroups.addAll(RoomGroup.getAllDepartmentRoomGroups(department));
		}
		
		for (RoomGroup rg: departmentRoomGroups) {
			boolean editable = sessionContext.hasPermission(rg, Right.DepartmenalRoomGroupEdit);
			
			String ownerName = "<span title='"+rg.getDepartment().getHtmlTitle()+"'>" + rg.getDepartment().getShortLabel() + "</span>";

			Collection rs = new TreeSet(rg.getRooms());

			StringBuffer assignedRoom = new StringBuffer();
			
			for (Iterator iter = rs.iterator();iter.hasNext();) {
				Location r = (Location) iter.next();
                if (examType != null) {
                    if (!r.isExamEnabled(examType)) continue;
                } else {
                    boolean skip = true;
                    for (Iterator j=r.getRoomDepts().iterator();j.hasNext();) {
                        RoomDept rd = (RoomDept)j.next();
                        if (rg.getDepartment().equals(rd.getDepartment())) { skip=false; break; }
                    }
                    if (skip) continue;
                }
				if (assignedRoom.length() > 0) assignedRoom.append(", ");
				assignedRoom.append(r.getLabel().replaceAll(" ","&nbsp;"));
			}

			departmentWebTable.addLine(
					(editable?"onClick=\"document.location='roomGroupEdit.do?doit=editRoomGroup&id="+rg.getUniqueId()+ "';\"":null), 
					new String[] {
							"<A name=\"A"+rg.getUniqueId()+"\"></A>"+
							(editable?"":"<font color=gray>")+rg.getName().replaceAll(" ","&nbsp;")+(editable?"":"</font>"),
                            (editable?"":"<font color=gray>")+rg.getAbbv().replaceAll(" ","&nbsp;")+(editable?"":"</font>"),
							(editable?"":"<font color=gray>")+ownerName+(editable?"":"</font>"),
							(editable?"":"<font color=gray>")+assignedRoom+(editable?"":"</font>"), 
							(editable?"":"<font color=gray>")+(rg.getDescription() == null ? "" : rg.getDescription()).replaceAll(" ","&nbsp;").replaceAll("\\\n","<BR>")+(editable?"":"</font>") }, 
					new Comparable[] {
							rg.getName(),
                            rg.getAbbv(),
							ownerName,
							null,
							(rg.getDescription() == null ? "" : rg.getDescription())
					});
			}
		
		if (haveGlobalRoomGroup)
			request.setAttribute("roomGroupsGlobal", globalWebTable.printTable(WebTable.getOrder(sessionContext,"roomGroupList.gord")));
		else
			request.removeAttribute("roomGroupsGlobal");
		if (!departmentRoomGroups.isEmpty())
			request.setAttribute("roomGroupsDepartment", departmentWebTable.printTable(WebTable.getOrder(sessionContext,"roomGroupList.mord")));
		else
			request.removeAttribute("roomGroupsDepartment");
	}

	public static void printPdfGroupTable(OutputStream out, SessionContext context, RoomGroupListForm roomGroupListForm) throws Exception{
		PdfWebTable globalWebTable = new PdfWebTable(5, "Global Room Groups", null, new String[] {
				"Name", "Abbreviation", "Default ", "Rooms", "Description" },
				new String[] { "left", "left", "middle", "left", "left" }, 
				new boolean[] { true, true, true, true, true} );
		
		PdfWebTable departmentWebTable = new PdfWebTable(5, "Department Room Groups", null, new String[] {
				"Name", "Abbreviation", "Department ", "Rooms", "Description" },
				new String[] { "left", "left", "left", "left", "left" }, 
				new boolean[] { true, true, true, true, true} );
		
		Set<Department> depts = Department.getUserDepartments(context.getUser());
        Long examType = null;
        Department department = null;
        if (roomGroupListForm.getDeptCodeX() != null && roomGroupListForm.getDeptCodeX().matches("Exam[0-9]*"))
        	examType = Long.valueOf(roomGroupListForm.getDeptCodeX().substring(4));
        else if (roomGroupListForm.getDeptCodeX() != null && !roomGroupListForm.getDeptCodeX().isEmpty() && !"All".equals(roomGroupListForm.getDeptCodeX()))
        	department = Department.findByDeptCode(roomGroupListForm.getDeptCodeX(), context.getUser().getCurrentAcademicSessionId());
        boolean deptCheck = examType == null && !context.getUser().getCurrentAuthority().hasRight(Right.DepartmentIndependent);
        if (department != null) {
        	deptCheck = true; depts = new TreeSet<Department>(); depts.add(department);
        }

        boolean splitRows = false;
        
		boolean haveGlobalRoomGroup = false;
		for (RoomGroup rg: RoomGroup.getAllGlobalRoomGroups(context.getUser().getCurrentAcademicSessionId())) {
			Collection rs = new TreeSet(rg.getRooms());

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
			
			if (!haveRooms && !context.hasPermission(rg, Right.GlobalRoomGroupEdit)) continue;
			
			globalWebTable.addLine(
					null, 
					new String[] {
							rg.getName(),
                            rg.getAbbv(),
							(rg.isDefaultGroup().booleanValue()?"Yes":"No"),
							assignedRoom.toString(), 
							(rg.getDescription() == null ? "" : rg.getDescription())}, 
					new Comparable[] {
							rg.getName(),
                            rg.getAbbv(),
							new Integer(rg.isDefaultGroup().booleanValue()?0:1),
							null,
							(rg.getDescription() == null ? "" : rg.getDescription())
					});
		
			haveGlobalRoomGroup = true;
		}

		Set<RoomGroup> departmentRoomGroups = new TreeSet<RoomGroup>();
		for (Department d: Department.getUserDepartments(context.getUser())) {
			if ("All".equals(roomGroupListForm.getDeptCodeX()) || d.getDeptCode().equals(roomGroupListForm.getDeptCodeX()))
				departmentRoomGroups.addAll(RoomGroup.getAllDepartmentRoomGroups(d));
		}
		if (department != null && department.isExternalManager()) {
			departmentRoomGroups.addAll(RoomGroup.getAllDepartmentRoomGroups(department));
		}

		for (RoomGroup rg: departmentRoomGroups) {
			String ownerName = rg.getDepartment().getShortLabel();

			Collection rs = new TreeSet(rg.getRooms());

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
                        if (rg.getDepartment().equals(rd.getDepartment())) { skip=false; break; }
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
							rg.getName(),
                            rg.getAbbv(),
							ownerName,
							assignedRoom.toString(), 
							(rg.getDescription() == null ? "" : rg.getDescription())}, 
					new Comparable[] {
							rg.getName(),
                            rg.getAbbv(),
							ownerName,
							null,
							(rg.getDescription() == null ? "" : rg.getDescription())
					});
			}
		
		Document doc = null;
		
		if (haveGlobalRoomGroup) {
			PdfWebTable table = globalWebTable;
			PdfPTable pdfTable = table.printPdfTable(WebTable.getOrder(context,"roomGroupList.gord"));
			pdfTable.setSplitRows(splitRows);
			if (doc==null) {
				doc = new Document(new Rectangle(60f + table.getWidth(), 60f + 0.75f * table.getWidth()),30,30,30,30);
				PdfWriter iWriter = PdfWriter.getInstance(doc, out);
				iWriter.setPageEvent(new PdfEventHandler());
				doc.open();
			}
			doc.add(new Paragraph(table.getName(), PdfFont.getBigFont(true)));
			doc.add(pdfTable);
		}
		
		if (!departmentRoomGroups.isEmpty()) {
			PdfWebTable table = departmentWebTable;
			PdfPTable pdfTable = table.printPdfTable(WebTable.getOrder(context,"roomGroupList.mord"));
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

