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
import org.hibernate.criterion.Order;
import org.unitime.commons.Debug;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.commons.web.WebTable;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.form.RoomGroupListForm;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.Roles;
import org.unitime.timetable.model.RoomDept;
import org.unitime.timetable.model.RoomGroup;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.dao.RoomGroupDAO;
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
 * MyEclipse Struts
 * Creation date: 05-02-2006
 * 
 * XDoclet definition:
 * @struts.action path="/roomGroupList" name="roomGroupListForm" input="/admin/roomGroupList.jsp" parameter="doit" scope="request" validate="true"
 * @struts.action-forward name="showRoomGroupList" path="roomGroupListTile"
 * @struts.action-forward name="showAdd" path="/roomGroupEdit.do"
 */
public class RoomGroupListAction extends Action {

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
		
		HttpSession webSession = request.getSession();
		if (!Web.isLoggedIn(webSession)) {
			throw new Exception("Access Denied.");
		}
		
		User user = Web.getUser(webSession);
		ActionMessages errors = new ActionMessages();
		
		//get deptCode from request - for user with only one department
		String deptCode = (String)request.getAttribute("deptCode");
		if (deptCode != null) {
			roomGroupListForm.setDeptCodeX(deptCode);
		}

		if (webSession.getAttribute(Constants.DEPT_CODE_ATTR_ROOM_NAME) != null
				&& (roomGroupListForm.getDeptCodeX() == null)) {
			roomGroupListForm.setDeptCodeX(webSession.getAttribute(
					Constants.DEPT_CODE_ATTR_ROOM_NAME).toString());
		}

		// Set Session Variable
		if (!roomGroupListForm.getDeptCodeX().equalsIgnoreCase("")) {
			webSession.setAttribute(Constants.DEPT_CODE_ATTR_ROOM_NAME,
					roomGroupListForm.getDeptCodeX());
		}

		// Validate input
		errors = roomGroupListForm.validate(mapping, request);

		// Validation fails
		if (errors.size() > 0) {
			saveErrors(request, errors);
			return mapping.findForward("showRoomGroupSearch");
		}
		
		buildGroupTable(request, roomGroupListForm);
		
		//set request attribute for department
		Session session = Session.getCurrentAcadSession(user);
		Long sessionId = session.getSessionId();
		LookupTables.setupDeptsForUser(request, user, sessionId, true);
		
		if (user.getRole().equals(Roles.ADMIN_ROLE) || (user.getRole().equals(Roles.EXAM_MGR_ROLE)
		        && session.getStatusType().canExamTimetable()))
			roomGroupListForm.setCanAdd(true);
		else if (Constants.ALL_OPTION_LABEL.equals(roomGroupListForm.getDeptCodeX())) {
			roomGroupListForm.setCanAdd(false);
			String mgrId = (String)user.getAttribute(Constants.TMTBL_MGR_ID_ATTR_NAME);
			TimetableManager owner = (new TimetableManagerDAO()).get(new Long(mgrId));
	        for (Iterator i=owner.departmentsForSession(sessionId).iterator();i.hasNext();) {
	        	Department d = (Department)i.next();
	        	if (d.isEditableBy(user)) {
	        		roomGroupListForm.setCanAdd(true);
	        		break;
	        	}
	        }
		} else {
			roomGroupListForm.setCanAdd(false);
			String mgrId = (String)user.getAttribute(Constants.TMTBL_MGR_ID_ATTR_NAME);
			TimetableManager owner = (new TimetableManagerDAO()).get(new Long(mgrId));
	        for (Iterator i=owner.departmentsForSession(sessionId).iterator();i.hasNext();) {
	        	Department d = (Department)i.next();
	        	if (d.getDeptCode().equals(roomGroupListForm.getDeptCodeX()) && d.isEditableBy(user)) {
	        		roomGroupListForm.setCanAdd(true);
	        		break;
	        	}
	        }
		}
		
		if ("Export PDF".equals(request.getParameter("op"))) {
			buildPdfGroupTable(request, roomGroupListForm);
		}

		return mapping.findForward("showRoomGroupList");
	}

	private void buildGroupTable(
			HttpServletRequest request, 
			RoomGroupListForm roomGroupListForm) throws Exception{

		HttpSession webSession = request.getSession();
		
		ArrayList globalRoomGroups = new ArrayList();
		Set departmentRoomGroups = new HashSet();
		
		User user = Web.getUser(webSession);
		Session session = org.unitime.timetable.model.Session.getCurrentAcadSession(user);
		Long sessionId = session.getSessionId();

		String mgrId = (String)user.getAttribute(Constants.TMTBL_MGR_ID_ATTR_NAME);
		TimetableManagerDAO tdao = new TimetableManagerDAO();
        TimetableManager manager = tdao.get(new Long(mgrId));
		boolean isAdmin = user.getRole().equals(Roles.ADMIN_ROLE) || 
		    (user.getRole().equals(Roles.EXAM_MGR_ROLE) && session.getStatusType().canExamTimetable());
		boolean showAll = false;
		Set depts = null;
		if (roomGroupListForm.getDeptCodeX().equalsIgnoreCase("All")) {
			if (isAdmin) {
				showAll = true;
				depts = Department.findAll(sessionId);
			} else {
				depts = Department.findAllOwned(sessionId, manager, false);
			}
		} else if (roomGroupListForm.getDeptCodeX().equalsIgnoreCase("Exam")) {
		    depts = new HashSet(0);
        } else if (roomGroupListForm.getDeptCodeX().equalsIgnoreCase("EExam")) {
            depts = new HashSet(0);
		} else {
			depts = new HashSet(1);
			depts.add(Department.findByDeptCode(roomGroupListForm.getDeptCodeX(),sessionId));
		}
           		
		org.hibernate.Session hibSession = null;
		try {
			RoomGroupDAO d = new RoomGroupDAO();
			hibSession = d.getSession();
			
			List list = hibSession
						.createCriteria(RoomGroup.class)
						.addOrder(Order.asc("name"))
						.list();
			
			for (Iterator iter = list.iterator();iter.hasNext();) {
				RoomGroup rg = (RoomGroup) iter.next();
				if (rg.isGlobal()!=null && rg.isGlobal().booleanValue()) {
					globalRoomGroups.add(rg);
				} else {
					if (rg.getDepartment()==null) continue;
					if (!rg.getDepartment().getSessionId().equals(sessionId)) continue;
					if (roomGroupListForm.getDeptCodeX().equalsIgnoreCase("All")) {
						if (depts.contains(rg.getDepartment()) || isAdmin) {
							departmentRoomGroups.add(rg);
						}
                    } else if (roomGroupListForm.getDeptCodeX().equalsIgnoreCase("EExam")) {
					} else if (roomGroupListForm.getDeptCodeX().equalsIgnoreCase("Exam")) {
					} else if (rg.getDepartment().getDeptCode().equalsIgnoreCase(roomGroupListForm.getDeptCodeX())) {
						departmentRoomGroups.add(rg);
					}					
				}
			}

		} catch (Exception e) {
			Debug.error(e);
		}

        WebTable.setOrder(request.getSession(),"roomGroupList.gord",request.getParameter("gord"),1);
        WebTable.setOrder(request.getSession(),"roomGroupList.mord",request.getParameter("mord"),1);
        
		WebTable globalWebTable = new WebTable(5, "Global Room Groups", "roomGroupList.do?gord=%%", new String[] {
				"Name", "Abbreviation", "Default", "Rooms", "Description" },
				new String[] { "left", "left", "middle", "left", "left" }, 
				new boolean[] { true, true, true, true, true} );
		
		WebTable departmentWebTable = new WebTable(5, "Department Room Groups", "roomGroupList.do?mord=%%", new String[] {
				"Name", "Abbreviation", "Department", "Rooms", "Description" },
				new String[] { "left", "left", "left", "left", "left" }, 
				new boolean[] { true, true, true, true, true} );

        int examType = -1;
        if (roomGroupListForm.getDeptCodeX().equalsIgnoreCase("exam")) examType = Exam.sExamTypeFinal;
        if (roomGroupListForm.getDeptCodeX().equalsIgnoreCase("eexam")) examType = Exam.sExamTypeMidterm;

        boolean haveGlobalRoomGroup = false;
		for (Iterator it = globalRoomGroups.iterator(); it.hasNext();) {
			RoomGroup rg = (RoomGroup) it.next();
			Collection rs = new TreeSet(rg.getRooms());

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
				if (assignedRoom.length() > 0) 
					assignedRoom.append(", ");
				assignedRoom.append(r.getLabel().replaceAll(" ","&nbsp;"));
				haveRooms = true;
			}

			if (!isAdmin && !haveRooms) continue;
			
			globalWebTable.addLine(
					(isAdmin?"onClick=\"document.location='roomGroupEdit.do?doit=editRoomGroup&id="+rg.getUniqueId()+ "';\"":null), 
					new String[] {
							"<A name=\"A"+rg.getUniqueId()+"\"></A>"+
							(isAdmin?"":"<font color=gray>")+rg.getName().replaceAll(" ","&nbsp;")+(isAdmin?"":"</font>"),
                            (isAdmin?"":"<font color=gray>")+rg.getAbbv().replaceAll(" ","&nbsp;")+(isAdmin?"":"</font>"),
							(rg.isDefaultGroup().booleanValue()?"<IMG border='0' title='This group is default group.' alt='Default' align='absmiddle' src='images/tick.gif'>":""),
							(isAdmin?"":"<font color=gray>")+assignedRoom+(isAdmin?"":"</font>"), 
							(isAdmin?"":"<font color=gray>")+(rg.getDescription() == null ? "" : rg.getDescription()).replaceAll(" ","&nbsp;").replaceAll("\\\n","<BR>")+(isAdmin?"":"</font>") }, 
					new Comparable[] {
							rg.getName(),
                            rg.getAbbv(),
							new Integer(rg.isDefaultGroup().booleanValue()?0:1),
							null,
							(rg.getDescription() == null ? "" : rg.getDescription())
					});
		
			haveGlobalRoomGroup = true;
		}
		
		for (Iterator it = departmentRoomGroups.iterator(); it.hasNext();) {
			RoomGroup rg = (RoomGroup) it.next();
			Collection rs = new TreeSet(rg.getRooms());
			
			Department rgOwningDept = rg.getDepartment();

			//boolean isOwner = isAdmin || rgOwningDept.equals(manager);
			boolean isOwner = isAdmin || manager.getDepartments().contains(rgOwningDept);
			boolean isEditable = rgOwningDept.isEditableBy(user);
			String ownerName = "<i>Not defined</i>";
			if (rgOwningDept != null) {
				ownerName = "<span title='"+rgOwningDept.getHtmlTitle()+"'>"+
				rgOwningDept.getShortLabel()+
				"</span>";
			}

			StringBuffer assignedRoom = new StringBuffer();
			StringBuffer availableRoom = new StringBuffer();
			
			for (Iterator iter = rs.iterator();iter.hasNext();) {
				Location r = (Location) iter.next();
                boolean skip = true;
                if (examType>=0) {
                    if (!r.isExamEnabled(examType)) continue;
                } else {
                    for (Iterator j=r.getRoomDepts().iterator();j.hasNext();) {
                        RoomDept rd = (RoomDept)j.next();
                        if (rg.getDepartment().equals(rd.getDepartment())) { skip=false; break; }
                    }
                }
                if (skip) continue;
				if (assignedRoom.length() > 0) 
					assignedRoom.append(", ");
				assignedRoom.append(r.getLabel().replaceAll(" ","&nbsp;"));
			}

			departmentWebTable.addLine(
					(isEditable?"onClick=\"document.location='roomGroupEdit.do?doit=editRoomGroup&id="+rg.getUniqueId()+ "';\"":null), 
					new String[] {
							"<A name=\"A"+rg.getUniqueId()+"\"></A>"+
							(isOwner?"":"<font color=gray>")+rg.getName().replaceAll(" ","&nbsp;")+(isOwner?"":"</font>"),
                            (isOwner?"":"<font color=gray>")+rg.getAbbv().replaceAll(" ","&nbsp;")+(isOwner?"":"</font>"),
							(isOwner?"":"<font color=gray>")+ownerName+(isOwner?"":"</font>"),
							(isOwner?"":"<font color=gray>")+assignedRoom+(isOwner?"":"</font>"), 
							(isOwner?"":"<font color=gray>")+(rg.getDescription() == null ? "" : rg.getDescription()).replaceAll(" ","&nbsp;").replaceAll("\\\n","<BR>")+(isOwner?"":"</font>") }, 
					new Comparable[] {
							rg.getName(),
                            rg.getAbbv(),
							ownerName,
							null,
							(rg.getDescription() == null ? "" : rg.getDescription())
					});
			}
		
		if (haveGlobalRoomGroup)
			request.setAttribute("roomGroupsGlobal", globalWebTable.printTable(WebTable.getOrder(request.getSession(),"roomGroupList.gord")));
		else
			request.removeAttribute("roomGroupsGlobal");
		if (!departmentRoomGroups.isEmpty())
			request.setAttribute("roomGroupsDepartment", departmentWebTable.printTable(WebTable.getOrder(request.getSession(),"roomGroupList.mord")));
		else
			request.removeAttribute("roomGroupsDepartment");
	}

	public static void buildPdfGroupTable(HttpServletRequest request, RoomGroupListForm roomGroupListForm) throws Exception{
    	FileOutputStream out = null;
    	try {
    		File file = ApplicationProperties.getTempFile("room_groups", "pdf");
    		
    		out = new FileOutputStream(file);

    		HttpSession webSession = request.getSession();
    		
    		ArrayList globalRoomGroups = new ArrayList();
    		Set departmentRoomGroups = new HashSet();
    		
    		User user = Web.getUser(webSession);
    		Long sessionId = org.unitime.timetable.model.Session.getCurrentAcadSession(user).getSessionId();

    		String mgrId = (String)user.getAttribute(Constants.TMTBL_MGR_ID_ATTR_NAME);
    		TimetableManagerDAO tdao = new TimetableManagerDAO();
            TimetableManager manager = tdao.get(new Long(mgrId));
    		boolean isAdmin = user.getRole().equals(Roles.ADMIN_ROLE);
    		boolean showAll = false;
    		Set depts = null;
    		if (roomGroupListForm.getDeptCodeX().equalsIgnoreCase("All")) {
    			if (isAdmin) {
    				showAll = true;
    				depts = Department.findAll(sessionId);
    			} else {
    				depts = Department.findAllOwned(sessionId, manager, false);
    			}
            } else if (roomGroupListForm.getDeptCodeX().equalsIgnoreCase("eexam")) {
                depts = new HashSet(0);
    		} else if (roomGroupListForm.getDeptCodeX().equalsIgnoreCase("exam")) {
    		    depts = new HashSet(0);
    		} else {
    			depts = new HashSet(1);
    			depts.add(Department.findByDeptCode(roomGroupListForm.getDeptCodeX(),sessionId));
    		}
               		
    		org.hibernate.Session hibSession = null;
    		boolean splitRows = false;
    		try {
    			RoomGroupDAO d = new RoomGroupDAO();
    			hibSession = d.getSession();
    			
    			List list = hibSession
    						.createCriteria(RoomGroup.class)
    						.addOrder(Order.asc("name"))
    						.list();
    			
    			for (Iterator iter = list.iterator();iter.hasNext();) {
    				RoomGroup rg = (RoomGroup) iter.next();
    				if (rg.isGlobal()!=null && rg.isGlobal().booleanValue()) {
    					globalRoomGroups.add(rg);
    				} else {
    					if (rg.getDepartment()==null) continue;
    					if (!rg.getDepartment().getSessionId().equals(sessionId)) continue;
    					if (roomGroupListForm.getDeptCodeX().equalsIgnoreCase("All")) {
    						if (depts.contains(rg.getDepartment()) || isAdmin) {
    							departmentRoomGroups.add(rg);
    						}
                        } else if (roomGroupListForm.getDeptCodeX().equalsIgnoreCase("EExam")) {
    					} else if (roomGroupListForm.getDeptCodeX().equalsIgnoreCase("Exam")) {
    					} else if (rg.getDepartment().getDeptCode().equalsIgnoreCase(roomGroupListForm.getDeptCodeX())) {
    						departmentRoomGroups.add(rg);
    					}					
    				}
    			}

    		} catch (Exception e) {
    			Debug.error(e);
    		}
            
    		PdfWebTable globalWebTable = new PdfWebTable(5, "Global Room Groups", null, new String[] {
    				"Name", "Abbreviation", "Default ", "Rooms", "Description" },
    				new String[] { "left", "left", "middle", "left", "left" }, 
    				new boolean[] { true, true, true, true, true} );
    		
    		PdfWebTable departmentWebTable = new PdfWebTable(5, "Department Room Groups", null, new String[] {
    				"Name", "Abbreviation", "Department ", "Rooms", "Description" },
    				new String[] { "left", "left", "left", "left", "left" }, 
    				new boolean[] { true, true, true, true, true} );
    		
    		int examType = -1;
    		if (roomGroupListForm.getDeptCodeX().equalsIgnoreCase("exam")) examType = Exam.sExamTypeFinal;
    		if (roomGroupListForm.getDeptCodeX().equalsIgnoreCase("eexam")) examType = Exam.sExamTypeMidterm;

    		boolean haveGlobalRoomGroup = false;
    		for (Iterator it = globalRoomGroups.iterator(); it.hasNext();) {
    			RoomGroup rg = (RoomGroup) it.next();
    			Collection rs = new TreeSet(rg.getRooms());

    			StringBuffer assignedRoom = new StringBuffer();
    			
    			boolean haveRooms = false;
    			int nrRows = 0;
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
    				if (PdfWebTable.getWidthOfLastLine(assignedRoom.toString(),false,false)>500) {
    					assignedRoom.append("\n");
    					nrRows++;
    				}
    				assignedRoom.append(r.getLabel());
    				haveRooms = true;
    			}
    			
    			if (nrRows>40)
    				splitRows = true;

    			if (!isAdmin && !haveRooms) continue;
    			
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

    		for (Iterator it = departmentRoomGroups.iterator(); it.hasNext();) {
    			RoomGroup rg = (RoomGroup) it.next();
    			Collection rs = new TreeSet(rg.getRooms());
    			
    			Department rgOwningDept = rg.getDepartment();

    			//boolean isOwner = isAdmin || rgOwningDept.equals(manager);
    			boolean isOwner = isAdmin || manager.getDepartments().contains(rgOwningDept);
    			boolean isEditable = rgOwningDept.isEditableBy(user);
    			String ownerName = "@@ITALIC Not defined";
    			if (rgOwningDept != null) {
    				ownerName = rgOwningDept.getShortLabel();
    			}

    			StringBuffer assignedRoom = new StringBuffer();
    			StringBuffer availableRoom = new StringBuffer();
    			
    			for (Iterator iter = rs.iterator();iter.hasNext();) {
    				Location r = (Location) iter.next();
                    boolean skip = true;
                    if (examType>=0) {
                        if (!r.isExamEnabled(examType)) continue;
                    } else {
                        for (Iterator j=r.getRoomDepts().iterator();j.hasNext();) {
                            RoomDept rd = (RoomDept)j.next();
                            if (rg.getDepartment().equals(rd.getDepartment())) { skip=false; break; }
                        }
                        if (skip) continue;
                    }
    				if (assignedRoom.length() > 0) assignedRoom.append(", ");
    				if (PdfWebTable.getWidthOfLastLine(assignedRoom.toString(),false,false)>500)
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
    			PdfPTable pdfTable = table.printPdfTable(WebTable.getOrder(request.getSession(),"roomGroupList.gord"));
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
    		
    		if (!departmentRoomGroups.isEmpty()) {
				PdfWebTable table = departmentWebTable;
    			PdfPTable pdfTable = table.printPdfTable(WebTable.getOrder(request.getSession(),"roomGroupList.mord"));
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

