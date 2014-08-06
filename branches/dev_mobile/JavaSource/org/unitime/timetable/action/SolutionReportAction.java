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
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unitime.commons.Debug;
import org.unitime.commons.web.WebTable;
import org.unitime.timetable.form.SolutionReportForm;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.RoomType;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.RoomTypeDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.SolverProxy;
import org.unitime.timetable.solver.interactive.ClassAssignmentDetails;
import org.unitime.timetable.solver.service.SolverService;
import org.unitime.timetable.solver.ui.DeptBalancingReport;
import org.unitime.timetable.solver.ui.DiscouragedInstructorBtbReport;
import org.unitime.timetable.solver.ui.JenrlInfo;
import org.unitime.timetable.solver.ui.PerturbationReport;
import org.unitime.timetable.solver.ui.RoomReport;
import org.unitime.timetable.solver.ui.SameSubpartBalancingReport;
import org.unitime.timetable.solver.ui.StudentConflictsReport;
import org.unitime.timetable.solver.ui.ViolatedDistrPreferencesReport;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.ExportUtils;
import org.unitime.timetable.util.PdfEventHandler;
import org.unitime.timetable.util.PdfFont;
import org.unitime.timetable.webutil.PdfWebTable;

import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;


/** 
 * @author Tomas Muller
 */
@Service("/solutionReport")
public class SolutionReportAction extends Action {
	private static java.text.DecimalFormat sDoubleFormat = new java.text.DecimalFormat("0.00",new java.text.DecimalFormatSymbols(Locale.US));
	
	@Autowired SolverService<SolverProxy> courseTimetablingSolverService;
	
	@Autowired SessionContext sessionContext;

	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		SolutionReportForm myForm = (SolutionReportForm) form;
		
		sessionContext.checkPermission(Right.SolutionReports);
        
        // Read operation to be performed
        String op = (myForm.getOp()!=null?myForm.getOp():request.getParameter("op"));
		Session session = SessionDAO.getInstance().get(sessionContext.getUser().getCurrentAcademicSessionId());
		BitSet sessionDays = session.getDefaultDatePattern().getPatternBitSet();
		int startDayDayOfWeek = Constants.getDayOfWeek(session.getDefaultDatePattern().getStartDate());
		
		SolverProxy solver = courseTimetablingSolverService.getSolver();
        if (solver==null) {
        	request.setAttribute("SolutionReport.message","Neither a solver is started nor solution is loaded.");
        } else {
        	try {
                for (RoomType type : RoomType.findAll()) {
                    RoomReport roomReport = solver.getRoomReport(sessionDays, startDayDayOfWeek, type.getUniqueId());
                    if (roomReport!=null && !roomReport.getGroups().isEmpty()) {
                        WebTable t = getRoomReportTable(request, roomReport, false, type.getUniqueId());
                        if (t!=null)
                            request.setAttribute("SolutionReport.roomReportTable."+type.getReference(), t.printTable(WebTable.getOrder(sessionContext,"solutionReports.roomReport.ord")));
                    }
                }
                RoomReport roomReport = solver.getRoomReport(sessionDays, startDayDayOfWeek, null);
                if (roomReport!=null && !roomReport.getGroups().isEmpty()) {
                    WebTable t = getRoomReportTable(request, roomReport, false, null);
                    if (t!=null)
                        request.setAttribute("SolutionReport.roomReportTable.nonUniv", t.printTable(WebTable.getOrder(sessionContext,"solutionReports.roomReport.ord")));
                }
        		DeptBalancingReport deptBalancingReport = solver.getDeptBalancingReport();
        		if (deptBalancingReport!=null && !deptBalancingReport.getGroups().isEmpty())
        			request.setAttribute("SolutionReport.deptBalancingReportTable", getDeptBalancingReportTable(request, deptBalancingReport, false).printTable(WebTable.getOrder(sessionContext,"solutionReports.deptBalancingReport.ord")));
        		ViolatedDistrPreferencesReport violatedDistrPreferencesReport = solver.getViolatedDistrPreferencesReport();
        		if (violatedDistrPreferencesReport!=null && !violatedDistrPreferencesReport.getGroups().isEmpty())
        			request.setAttribute("SolutionReport.violatedDistrPreferencesReportTable", getViolatedDistrPreferencesReportTable(request, violatedDistrPreferencesReport, false).printTable(WebTable.getOrder(sessionContext,"solutionReports.violDistPrefReport.ord")));
        		DiscouragedInstructorBtbReport discouragedInstructorBtbReportReport = solver.getDiscouragedInstructorBtbReport();
        		if (discouragedInstructorBtbReportReport!=null && !discouragedInstructorBtbReportReport.getGroups().isEmpty())
        			request.setAttribute("SolutionReport.discouragedInstructorBtbReportReportTable", getDiscouragedInstructorBtbReportReportTable(request, discouragedInstructorBtbReportReport, false).printTable(WebTable.getOrder(sessionContext,"solutionReports.violInstBtb.ord")));
        		StudentConflictsReport studentConflictsReport = solver.getStudentConflictsReport();
        		if (studentConflictsReport!=null && !studentConflictsReport.getGroups().isEmpty())
        			request.setAttribute("SolutionReport.studentConflictsReportTable", getStudentConflictsReportTable(request, studentConflictsReport, false).printTable(WebTable.getOrder(sessionContext,"solutionReports.studConf.ord")));
        		SameSubpartBalancingReport sameSubpartBalancingReport = solver.getSameSubpartBalancingReport();
        		if (sameSubpartBalancingReport!=null && !sameSubpartBalancingReport.getGroups().isEmpty())
        			request.setAttribute("SolutionReport.sameSubpartBalancingReportTable", getSameSubpartBalancingReportTable(request, sameSubpartBalancingReport, false).printTable(PdfWebTable.getOrder(sessionContext,"solutionReports.sectBalancingReport.ord")));
        		PerturbationReport perturbationReport = solver.getPerturbationReport();
        		if (perturbationReport!=null && !perturbationReport.getGroups().isEmpty())
        			request.setAttribute("SolutionReport.perturbationReportTable", getPerturbationReportTable(request, perturbationReport, false).printTable(WebTable.getOrder(sessionContext,"solutionReports.pert.ord")));
        	} catch (Exception e) {
        		e.printStackTrace();
        	}
        }
        
        if ("Export PDF".equals(op)) {
        	OutputStream out = ExportUtils.getPdfOutputStream(response, "report");
    		
    		Document doc = new Document(new Rectangle(60f + PageSize.LETTER.getHeight(), 60f + 0.75f * PageSize.LETTER.getHeight()),30,30,30,30);
    		
			PdfWriter iWriter = PdfWriter.getInstance(doc, out);
			iWriter.setPageEvent(new PdfEventHandler());
    		doc.open();
    		
            boolean atLeastOneRoomReport = false;
            for (RoomType type : RoomType.findAll()) {
                RoomReport roomReport = solver.getRoomReport(sessionDays, startDayDayOfWeek, type.getUniqueId());
                if (roomReport==null || roomReport.getGroups().isEmpty()) continue;
                PdfWebTable table = getRoomReportTable(request, roomReport, true, type.getUniqueId());
                if (table==null) continue;
                PdfPTable pdfTable = table.printPdfTable(WebTable.getOrder(sessionContext,"solutionReports.roomReport.ord"));
                if (!atLeastOneRoomReport) {
                    doc.setPageSize(new Rectangle(60f + table.getWidth(), 60f + 0.75f * table.getWidth()));
                    doc.newPage();
                }
                doc.add(new Paragraph(table.getName(), PdfFont.getBigFont(true)));
                doc.add(pdfTable);
                atLeastOneRoomReport = true;
            }
            RoomReport roomReport = solver.getRoomReport(sessionDays, startDayDayOfWeek, null);
            if (roomReport!=null && !roomReport.getGroups().isEmpty()) {
                PdfWebTable table = getRoomReportTable(request, roomReport, true, null);
                if (table!=null) {
                    PdfPTable pdfTable = table.printPdfTable(WebTable.getOrder(sessionContext,"solutionReports.roomReport.ord"));
                    if (!atLeastOneRoomReport) {
                        doc.setPageSize(new Rectangle(60f + table.getWidth(), 60f + 0.75f * table.getWidth()));
                        doc.newPage();
                    }
                    doc.add(new Paragraph(table.getName(), PdfFont.getBigFont(true)));
                    doc.add(pdfTable);
                    atLeastOneRoomReport = true;
                }
            }

            if (atLeastOneRoomReport) {
                PdfPTable pdfTable = new PdfPTable(new float[] {10f,100f});
    			pdfTable.setWidthPercentage(100);
    			pdfTable.getDefaultCell().setPadding(3);
    			pdfTable.getDefaultCell().setBorderWidth(0);
    			pdfTable.setSplitRows(false);
    			pdfTable.addCell("Group");
    			pdfTable.addCell("group size <minimum, maximum)");
    			pdfTable.addCell("Size");
    			pdfTable.addCell("actual group size (size of the smallest and the biggest room in the group)");
    			pdfTable.addCell("NrRooms");
    			pdfTable.addCell("number of rooms in the group");
    			pdfTable.addCell("ClUse");
    			pdfTable.addCell("number of classes that are using a room from the group (actual solution)");
    			pdfTable.addCell("ClShould");
    			pdfTable.addCell("number of classes that \"should\" use a room of the group (smallest available room of a class is in this group)");
    			pdfTable.addCell("ClMust");
    			pdfTable.addCell("number of classes that must use a room of the group (all available rooms of a class are in this group)");
    			pdfTable.addCell("HrUse");
    			pdfTable.addCell("average hours a room of the group is used (actual solution)");
    			pdfTable.addCell("HrShould");
    			pdfTable.addCell("average hours a room of the group should be used (smallest available room of a class is in this group)");
    			pdfTable.addCell("HrMust");
    			pdfTable.addCell("average hours a room of this group must be used (all available rooms of a class are in this group)");
    			pdfTable.addCell("");
    			pdfTable.addCell("*) cumulative numbers (group minimum ... inf) are displayed in parentheses.");
    			doc.add(pdfTable);
    		}
    		
    		DiscouragedInstructorBtbReport discouragedInstructorBtbReportReport = solver.getDiscouragedInstructorBtbReport();
    		if (discouragedInstructorBtbReportReport!=null && !discouragedInstructorBtbReportReport.getGroups().isEmpty()) {
    			PdfWebTable table = getDiscouragedInstructorBtbReportReportTable(request, discouragedInstructorBtbReportReport, true);
    			PdfPTable pdfTable = table.printPdfTable(WebTable.getOrder(sessionContext,"solutionReports.violInstBtb.ord"));
    			doc.setPageSize(new Rectangle(60f + table.getWidth(), 60f + 0.75f * table.getWidth()));
    			doc.newPage();
    			doc.add(new Paragraph(table.getName(), PdfFont.getBigFont(true)));
    			doc.add(pdfTable);
    		}

    		ViolatedDistrPreferencesReport violatedDistrPreferencesReport = solver.getViolatedDistrPreferencesReport();
    		if (violatedDistrPreferencesReport!=null && !violatedDistrPreferencesReport.getGroups().isEmpty()) {
    			PdfWebTable table = getViolatedDistrPreferencesReportTable(request, violatedDistrPreferencesReport, true);
    			PdfPTable pdfTable = table.printPdfTable(WebTable.getOrder(sessionContext,"solutionReports.violDistPrefReport.ord"));
    			doc.setPageSize(new Rectangle(60f + table.getWidth(), 60f + 0.75f * table.getWidth()));
    			doc.newPage();
    			doc.add(new Paragraph(table.getName(), PdfFont.getBigFont(true)));
    			doc.add(pdfTable);
    		}
    		
    		StudentConflictsReport studentConflictsReport = solver.getStudentConflictsReport();
    		if (studentConflictsReport!=null && !studentConflictsReport.getGroups().isEmpty()) {
    			PdfWebTable table = getStudentConflictsReportTable(request, studentConflictsReport, true);
    			PdfPTable pdfTable = table.printPdfTable(WebTable.getOrder(sessionContext,"solutionReports.studConf.ord"));
    			doc.setPageSize(new Rectangle(60f + table.getWidth(), 60f + 0.75f * table.getWidth()));
    			doc.newPage();
    			doc.add(new Paragraph(table.getName(), PdfFont.getBigFont(true)));
    			doc.add(pdfTable);
    		}
    		
    		SameSubpartBalancingReport sameSubpartBalancingReport = solver.getSameSubpartBalancingReport();
    		if (sameSubpartBalancingReport!=null && !sameSubpartBalancingReport.getGroups().isEmpty()) {
    			PdfWebTable table = getSameSubpartBalancingReportTable(request, sameSubpartBalancingReport, true);
    			PdfPTable pdfTable = table.printPdfTable(WebTable.getOrder(sessionContext,"solutionReports.sectBalancingReport.ord"));
    			doc.setPageSize(new Rectangle(60f + table.getWidth(), 60f + 0.75f * table.getWidth()));
    			doc.newPage();
    			doc.add(new Paragraph(table.getName(), PdfFont.getBigFont(true)));
    			doc.add(pdfTable);
    		}
    		
    		DeptBalancingReport deptBalancingReport = solver.getDeptBalancingReport();
    		if (deptBalancingReport!=null && !deptBalancingReport.getGroups().isEmpty()) {
    			PdfWebTable table = getDeptBalancingReportTable(request, deptBalancingReport, true);
    			PdfPTable pdfTable = table.printPdfTable(WebTable.getOrder(sessionContext,"solutionReports.deptBalancingReport.ord"));
    			doc.setPageSize(new Rectangle(60f + table.getWidth(), 60f + 0.75f * table.getWidth()));
    			doc.newPage();
    			doc.add(new Paragraph(table.getName(), PdfFont.getBigFont(true)));
    			doc.add(pdfTable);
    		}

    		PerturbationReport perturbationReport = solver.getPerturbationReport();
    		if (perturbationReport!=null && !perturbationReport.getGroups().isEmpty()) {
    			PdfWebTable table = getPerturbationReportTable(request, perturbationReport, true);
    			PdfPTable pdfTable = table.printPdfTable(WebTable.getOrder(sessionContext,"solutionReports.pert.ord"));
    			doc.setPageSize(new Rectangle(60f + table.getWidth(), 60f + 0.75f * table.getWidth()));
    			doc.newPage();
    			doc.add(new Paragraph(table.getName(), PdfFont.getBigFont(true)));
    			doc.add(pdfTable);
    			pdfTable = new PdfPTable(new float[] {5f,100f});
    			pdfTable.setWidthPercentage(100);
    			pdfTable.getDefaultCell().setPadding(3);
    			pdfTable.getDefaultCell().setBorderWidth(0);
    			pdfTable.setSplitRows(false);
    			pdfTable.addCell("Class");
    			pdfTable.addCell("Class name");
    			pdfTable.addCell("Time");
    			pdfTable.addCell("Time (initial -> assigned)");
    			pdfTable.addCell("Room");
    			pdfTable.addCell("Room (initial -> assigned)");
    			pdfTable.addCell("Dist");
    			pdfTable.addCell("Distance between assignments (if different are used buildings)");
    			pdfTable.addCell("St");
    			pdfTable.addCell("Number of affected students");
    			pdfTable.addCell("StT");
    			pdfTable.addCell("Number of affected students by time change");
    			pdfTable.addCell("StR");
    			pdfTable.addCell("Number of affected students by room change");
    			pdfTable.addCell("StB");
    			pdfTable.addCell("Number of affected students by building change");
    			pdfTable.addCell("Ins");
    			pdfTable.addCell("Number of affected instructors");
    			pdfTable.addCell("InsT");
    			pdfTable.addCell("Number of affected instructors by time change");
    			pdfTable.addCell("InsR");
    			pdfTable.addCell("Number of affected instructors by room change");
    			pdfTable.addCell("InsB");
    			pdfTable.addCell("Number of affected instructors by building change");
    			pdfTable.addCell("Rm");
    			pdfTable.addCell("Number of rooms changed");
    			pdfTable.addCell("Bld");
    			pdfTable.addCell("Number of buildings changed");
    			pdfTable.addCell("Tm");
    			pdfTable.addCell("Number of times changed");
    			pdfTable.addCell("Day");
    			pdfTable.addCell("Number of days changed");
    			pdfTable.addCell("Hr");
    			pdfTable.addCell("Number of hours changed");
    			pdfTable.addCell("TFSt");
    			pdfTable.addCell("Assigned building too far for instructor (from the initial one)");
    			pdfTable.addCell("TFIns");
    			pdfTable.addCell("Assigned building too far for students (from the initial one)");
    			pdfTable.addCell("DStC");
    			pdfTable.addCell("Difference in student conflicts");
    			pdfTable.addCell("NStC");
    			pdfTable.addCell("Number of new student conflicts");
    			pdfTable.addCell("DTPr");
    			pdfTable.addCell("Difference in time preferences");
    			pdfTable.addCell("DRPr");
    			pdfTable.addCell("Difference in room preferences");
    			pdfTable.addCell("DInsB");
    			pdfTable.addCell("Difference in back-to-back instructor preferences");
    			doc.add(pdfTable);
    		}
    		
    		doc.close();
    		
    		out.flush(); out.close();
    		
    		return null;
        }
		
		return mapping.findForward("showSolutionReport");
	}
	
	public PdfWebTable getRoomReportTable(HttpServletRequest request, RoomReport report, boolean noHtml, Long type) {
		WebTable.setOrder(sessionContext,"solutionReports.roomReport.ord",request.getParameter("room_ord"),-1);
		String name = "Room Allocation - "+(type==null?"Non University Locations":RoomTypeDAO.getInstance().get(type).getLabel());
        PdfWebTable webTable = new PdfWebTable( 9,
   	        	name, "solutionReport.do?room_ord=%%",
   				new String[] {"Group", "Size", "NrRooms*", "ClUse", "ClShould", "ClMust*", "HrUse", "HrShould", "HrMust*"},
   				new String[] {"center", "center", "left","left","left","left","left","left","left"},
   				null);
        webTable.setRowStyle("white-space:nowrap");
        int nrLines = 0;
        
        try {
            int nrAllRooms = 0, nrAllLectureUse = 0, nrAllLectureShouldUse = 0;
            double allSlotsUse = 0.0, allSlotsShouldUse = 0.0;
            
            TreeSet groups = new TreeSet(new Comparator() {
                public int compare(Object o1, Object o2) {
                    RoomReport.RoomAllocationGroup g1 = (RoomReport.RoomAllocationGroup)o1;
                    RoomReport.RoomAllocationGroup g2 = (RoomReport.RoomAllocationGroup)o2;
                    return -Double.compare(g1.getMinRoomSize(),g2.getMinRoomSize());
                }
            });
            groups.addAll(report.getGroups());
            
        	for (Iterator i=groups.iterator();i.hasNext();) {
        		RoomReport.RoomAllocationGroup g = (RoomReport.RoomAllocationGroup)i.next();
        		if (g.getNrRooms()==0) continue;
        		
        		double factor = ((double)Constants.SLOT_LENGTH_MIN) / 60.0;
                
                nrAllRooms+=g.getNrRooms();
                allSlotsUse+=g.getSlotsUse();
                allSlotsShouldUse+=g.getSlotsShouldUse();
                nrAllLectureUse+=g.getLecturesUse();
                nrAllLectureShouldUse+=g.getLecturesShouldUse();
                
                nrLines++;
        		
        		webTable.addLine(null,
    	    		new String[] {
        				g.getMinRoomSize()+" ... "+(g.getMaxRoomSize()==Integer.MAX_VALUE?(noHtml?"inf":"<i>inf</i>"):String.valueOf(g.getMaxRoomSize())),
        				g.getActualMinRoomSize()+" ... "+g.getActualMaxRoomSize(),
        				g.getNrRooms()+" ("+g.getNrRoomsThisSizeOrBigger()+")",
        				//""+g.getLecturesCanUse(),
        				""+g.getLecturesUse()+" ("+nrAllLectureUse+")",
        				""+g.getLecturesShouldUse()+" ("+nrAllLectureShouldUse+")",
        				g.getLecturesMustUse()+" ("+g.getLecturesMustUseThisSizeOrBigger()+")",
        				//sDoubleFormat.format(factor*g.getSlotsCanUse()/g.getNrRooms()),
        				sDoubleFormat.format(factor*g.getSlotsUse()/g.getNrRooms())+" ("+sDoubleFormat.format(factor*allSlotsUse/nrAllRooms)+")",
        				sDoubleFormat.format(factor*g.getSlotsShouldUse()/g.getNrRooms())+" ("+sDoubleFormat.format(factor*allSlotsShouldUse/nrAllRooms)+")",
        				sDoubleFormat.format(factor*g.getSlotsMustUse()/g.getNrRooms())+" ("+sDoubleFormat.format(factor*g.getSlotsMustUseThisSizeOrBigger()/g.getNrRoomsThisSizeOrBigger())+")"
        			},
        			new Comparable[] {
        				new Integer(g.getMinRoomSize()),
        				new Integer(g.getActualMinRoomSize()),
        				new Integer(g.getNrRooms()),
        				//new Integer(g.getLecturesCanUse()),
        				new Integer(g.getLecturesUse()),
        				new Integer(g.getLecturesShouldUse()),
        				new Integer(g.getLecturesMustUse()),
        				//new Double(factor*g.getSlotsCanUse()/g.getNrRooms()),
        				new Double(factor*g.getSlotsUse()/g.getNrRooms()),
        				new Double(factor*g.getSlotsShouldUse()/g.getNrRooms()),
        				new Double(factor*g.getSlotsMustUse()/g.getNrRooms())
        			});
        	}
        } catch (Exception e) {
        	Debug.error(e);
        	webTable.addLine(new String[] {"<font color='red'>ERROR:"+e.getMessage()+"</font>"},null);
            nrLines++;
        }
        if (nrLines==0) return null;
        return webTable;
	}
	
	public PdfWebTable getDeptBalancingReportTable(HttpServletRequest request, DeptBalancingReport deptBalancingReport, boolean noHtml) {
		WebTable.setOrder(sessionContext,"solutionReports.deptBalancingReport.ord",request.getParameter("dept_ord"),1);
		String[] header = new String[2+Constants.SLOTS_PER_DAY_NO_EVENINGS/6];
		String[] pos = new String[2+Constants.SLOTS_PER_DAY_NO_EVENINGS/6];
		header[0]="Department";
		pos[0]="left";
		header[1]="Penalty";
		pos[1]="center";
		for (int i=0;i<Constants.SLOTS_PER_DAY_NO_EVENINGS/6;i++) {
			header[i+2]=Constants.slot2str(Constants.DAY_SLOTS_FIRST + i*6);
			pos[i+2]="center";
		}
		
		PdfWebTable webTable = new PdfWebTable( header.length,
   	        	"Departmental Balancing", "solutionReport.do?dept_ord=%%",
   	        	header, pos, null);
        webTable.setRowStyle("white-space:nowrap");
        
        try {
        	for (Iterator it=deptBalancingReport.getGroups().iterator();it.hasNext();) {
        		DeptBalancingReport.DeptBalancingGroup g = (DeptBalancingReport.DeptBalancingGroup)it.next();
        		
        		String[] line = new String[2+Constants.SLOTS_PER_DAY_NO_EVENINGS/6];
        		Comparable[] cmp = new Comparable[2+Constants.SLOTS_PER_DAY_NO_EVENINGS/6];
        		
        		line[0]=g.getDepartmentName();
        		cmp[0]=g.getDepartmentName();
        		int penalty = 0;
        		for (int i=0;i<Constants.SLOTS_PER_DAY_NO_EVENINGS/6;i++) {
        			int slot = Constants.DAY_SLOTS_FIRST + i*6;
        			int usage = g.getUsage(slot);
        			int limit = g.getLimit(slot);
        			if (usage>limit)
        				penalty += g.getExcess(slot);
        			Vector classes = new Vector(g.getClasses(slot));
        			Collections.sort(classes);
        			StringBuffer sb = new StringBuffer();
        			StringBuffer toolTip = new StringBuffer();
        			int u = 0; boolean over = false;
        			for (Enumeration e=classes.elements();e.hasMoreElements();) {
        				ClassAssignmentDetails ca = (ClassAssignmentDetails)e.nextElement();
        				int nrMeetings = 0;
        				for (int j=0;j<Constants.NR_DAYS_WEEK;j++)
        					if ((Constants.DAY_CODES[j]&ca.getTime().getDays())!=0) nrMeetings++;
        				u+=nrMeetings;
        				if (u>limit && !over) {
        					over=true;
        					sb.append("<hr>");
        				}
        				sb.append(noHtml?ca.getClazz().getName():ca.getClazz().toHtml(true,true));//+" ("+nrMeetings+"x"+ca.getTime().getMin()+")");
        				if (e.hasMoreElements()) sb.append(noHtml?"\n":"<br>");
        				toolTip.append(ca.getClassName());
        				if (e.hasMoreElements()) toolTip.append(", ");
        			}
        			if (noHtml) {
        				line[i+2]=usage+" / "+limit;
        				line[i+2]+=(classes.isEmpty()?"":"\n"+sb.toString());
        			} else {
        				line[i+2]="<a title='"+toolTip+"'>"+(limit==0?"":(usage>limit?"<font color='red'>":"")+usage+" / "+limit+(usage>limit?"</font>":""))+"</a>";
        				line[i+2]+=(classes.isEmpty()?"":"<br>"+sb.toString());
        			}
        			cmp[i+2]=new Integer(usage*1000+limit);
        		}
        		line[1]=(noHtml?""+penalty:(penalty==0?"":"<font color='red'>+"+penalty+"</font>"));
        		cmp[1]=new Integer(penalty);
        		webTable.addLine(null,line,cmp);

        	}
        } catch (Exception e) {
        	Debug.error(e);
        	webTable.addLine(new String[] {"<font color='red'>ERROR:"+e.getMessage()+"</font>"},null);
        }
        return webTable;
	}

	public PdfWebTable getViolatedDistrPreferencesReportTable(HttpServletRequest request, ViolatedDistrPreferencesReport report, boolean noHtml) {
		WebTable.setOrder(sessionContext,"solutionReports.violDistPrefReport.ord",request.getParameter("vdist_ord"),1);
		PdfWebTable webTable = new PdfWebTable( 5,
   	        	"Violated Distribution Preferences", "solutionReport.do?vdist_ord=%%",
   				new String[] {"Type", "Preference", "Class", "Time", "Room"},
   				new String[] {"left", "left", "left", "left", "left"},
   				null);
        webTable.setRowStyle("white-space:nowrap");
        
        try {
        	for (Iterator i=report.getGroups().iterator();i.hasNext();) {
        		ViolatedDistrPreferencesReport.ViolatedDistrPreference g = (ViolatedDistrPreferencesReport.ViolatedDistrPreference)i.next();
        		
        		StringBuffer cSB = new StringBuffer();
        		MultiComparable ord = new MultiComparable();
        		StringBuffer tSB = new StringBuffer();
        		StringBuffer rSB = new StringBuffer();
        		for (Enumeration e=g.getClasses().elements();e.hasMoreElements();) {
        			ClassAssignmentDetails ca = (ClassAssignmentDetails)e.nextElement();
        			if (noHtml) {
        				cSB.append(ca.getClazz().getName());
        				tSB.append(ca.getTime().getDaysName()+" "+ca.getTime().getStartTime()+" - "+ca.getTime().getEndTime());
        				for (int j=0;j<ca.getRoom().length;j++)
        					rSB.append((j>0?", ":"")+ca.getRoom()[j].getName());
        			} else {
        				cSB.append(ca.getClazz().toHtml(true,true));
        				tSB.append(ca.getTime().toHtml(false,false,true,true));
        				for (int j=0;j<ca.getRoom().length;j++)
        					rSB.append((j>0?", ":"")+ca.getRoom()[j].toHtml(false,false,true));
        			}
        			ord.add(ca);
        			if (e.hasMoreElements()) {
        				if (noHtml) {
        					cSB.append("\n");tSB.append("\n");rSB.append("\n");
        				} else {
        					cSB.append("<BR>");tSB.append("<BR>");rSB.append("<BR>");
        				}
        			}
        		}
        		
        		webTable.addLine(null,
    	    		new String[] {
        				g.getName(),
        				(noHtml?"":"<font color='"+PreferenceLevel.int2color(g.getPreference())+"'>")+
        				PreferenceLevel.getPreferenceLevel(PreferenceLevel.int2prolog(g.getPreference())).getPrefName()+
        				(noHtml?"":"</font>"),
        				cSB.toString(), tSB.toString(), rSB.toString()
        			},
        			new Comparable[] {
        				g.getName(),
        				new Integer(g.getPreference()),
        				ord, null, null
        			});
        	}
        } catch (Exception e) {
        	Debug.error(e);
        	webTable.addLine(new String[] {"<font color='red'>ERROR:"+e.getMessage()+"</font>"},null);
        }
        return webTable;
	}

	public PdfWebTable getDiscouragedInstructorBtbReportReportTable(HttpServletRequest request, DiscouragedInstructorBtbReport report, boolean noHtml) {
		WebTable.setOrder(sessionContext,"solutionReports.violInstBtb.ord",request.getParameter("vinbtb_ord"),1);
		PdfWebTable webTable = new PdfWebTable( 7,
   	        	"Instructor Back-to-Back Preferences", "solutionReport.do?vinbtb_ord=%%",
   				new String[] {"Instructor", "Preference", "Distance", "Class", "Time", "Date", "Room"},
   				new String[] {"left", "left", "left", "left", "left", "left", "left"},
   				null);
        webTable.setRowStyle("white-space:nowrap");
        
        try {
        	for (Iterator i=report.getGroups().iterator();i.hasNext();) {
        		DiscouragedInstructorBtbReport.DiscouragedBtb g = (DiscouragedInstructorBtbReport.DiscouragedBtb)i.next();
        		
        		StringBuffer rSB = new StringBuffer();
        		for (int j=0;j<g.getFirst().getRoom().length;j++)
        				rSB.append((j>0?", ":"")+(noHtml?g.getFirst().getRoom()[j].getName():g.getFirst().getRoom()[j].toHtml(false,false,true)));
        		rSB.append(noHtml?"\n":"<BR>");
        		for (int j=0;j<g.getSecond().getRoom().length;j++)
    				rSB.append((j>0?", ":"")+(noHtml?g.getSecond().getRoom()[j].getName():g.getSecond().getRoom()[j].toHtml(false,false,true)));
        		
        		webTable.addLine(null,
    	    		new String[] {
        				g.getInstructorName(),
        				(noHtml?"":"<font color='"+PreferenceLevel.prolog2color(g.getPreference())+"'>")+
        				PreferenceLevel.getPreferenceLevel(g.getPreference()).getPrefName()+
        				(noHtml?"":"</font>"),
        				String.valueOf(Math.round(g.getDistance()))+"m",
        				(noHtml?g.getFirst().getClazz().getName()+"\n"+g.getSecond().getClazz().getName():
        				g.getFirst().getClazz().toHtml(true,true)+"<BR>"+g.getSecond().getClazz().toHtml(true,true)),
        				(noHtml?g.getFirst().getTime().getName(true)+"\n"+g.getSecond().getTime().getName(true):
        				g.getFirst().getTime().toHtml(false,false,true,true)+"<BR>"+g.getSecond().getTime().toHtml(false,false,true,true)),
        				(noHtml?g.getFirst().getTime().getDatePatternName()+"\n"+g.getSecond().getTime().getDatePatternName():
        				g.getFirst().getTime().toDatesHtml(false,false,true)+"<BR>"+g.getSecond().getTime().toDatesHtml(false,false,true)),
        				rSB.toString()
        			},
        			new Comparable[] {
        				g.getInstructorName(),
        				g.getPreference(),
        				new Double(g.getDistance()),
        				new DuoComparable(g.getFirst(),g.getSecond()), null, null, null
        			});
        	}
        } catch (Exception e) {
        	Debug.error(e);
        	webTable.addLine(new String[] {"<font color='red'>ERROR:"+e.getMessage()+"</font>"},null);
        }
        return webTable;
	}

	public PdfWebTable getStudentConflictsReportTable(HttpServletRequest request, StudentConflictsReport report, boolean noHtml) {
		WebTable.setOrder(sessionContext,"solutionReports.studConf.ord",request.getParameter("studconf_ord"),-1);
		boolean hasImportant = false;
		boolean hasInstructor = false;
		for (JenrlInfo g: (Set<JenrlInfo>)report.getGroups()) {
			if (g.isImportant()) hasImportant = true;
			if (g.isInstructor()) hasInstructor = true;
		}
		PdfWebTable webTable = null;
		if (hasImportant) {
			if (hasInstructor) {
				webTable = new PdfWebTable(12,
		   	        	"Student Conflicts", "solutionReport.do?studconf_ord=%%",
		   				new String[] {"NrConflicts", "Class", "Date", "Time", "Room", "Hard", "Distance", "Fixed", "Commited", "Important", "Instructor", "Curriculum"},
		   				new String[] {"left", "left", "left", "left", "left", "left", "left","left","left", "left", "left", "left"},
		   				null);
			} else {
				webTable = new PdfWebTable(11,
		   	        	"Student Conflicts", "solutionReport.do?studconf_ord=%%",
		   				new String[] {"NrConflicts", "Class", "Date", "Time", "Room", "Hard", "Distance", "Fixed", "Commited", "Important", "Curriculum"},
		   				new String[] {"left", "left", "left", "left", "left", "left", "left","left","left", "left", "left"},
		   				null);
			}
		} else {
			if (hasInstructor) {
				webTable = new PdfWebTable(11,
		   	        	"Student Conflicts", "solutionReport.do?studconf_ord=%%",
		   				new String[] {"NrConflicts", "Class", "Date", "Time", "Room", "Hard", "Distance", "Fixed", "Commited", "Instructor", "Curriculum"},
		   				new String[] {"left", "left", "left", "left", "left", "left", "left","left","left", "left", "left"},
		   				null);
			} else {
				webTable = new PdfWebTable(10,
		   	        	"Student Conflicts", "solutionReport.do?studconf_ord=%%",
		   				new String[] {"NrConflicts", "Class", "Date", "Time", "Room", "Hard", "Distance", "Fixed", "Commited", "Curriculum"},
		   				new String[] {"left", "left", "left", "left", "left", "left", "left","left","left", "left"},
		   				null);
			}
		}
        webTable.setRowStyle("white-space:nowrap");
        
        try {
        	int total[] = new int [] { 0, 0, 0, 0, 0, 0, 0};
        	for (Iterator i=report.getGroups().iterator();i.hasNext();) {
        		JenrlInfo g = (JenrlInfo)i.next();
        		
        		if (Math.round(g.getJenrl()) <= 0) continue;
        		
        		StringBuffer rSB = new StringBuffer();
        		for (int j=0;j<g.getFirst().getRoom().length;j++)
        				rSB.append((j>0?", ":"")+(noHtml?g.getFirst().getRoom()[j].getName():g.getFirst().getRoom()[j].toHtml(false,false,true)));
        		rSB.append(noHtml?"\n":"<BR>");
        		for (int j=0;j<g.getSecond().getRoom().length;j++)
    				rSB.append((j>0?", ":"")+(noHtml?g.getSecond().getRoom()[j].getName():g.getSecond().getRoom()[j].toHtml(false,false,true)));
        		
        		String[] line = new String[hasImportant ? hasInstructor ? 12 : 11 : hasInstructor ? 11 : 10];
        		Comparable[] cmp = new Comparable[hasImportant ? hasInstructor ? 12 : 11 : hasInstructor ? 11 : 10];
        		int idx = 0;
        		
        		line[idx] = String.valueOf(Math.round(g.getJenrl()));
        		cmp[idx++] = new Double(g.getJenrl());
        		
        		line[idx] = (noHtml?g.getFirst().getClazz().getName()+"\n"+g.getSecond().getClazz().getName():
        			g.getFirst().getClazz().toHtml(true,true)+"<BR>"+g.getSecond().getClazz().toHtml(true,true));
        		cmp[idx++] = new DuoComparable(g.getFirst(),g.getSecond());
        		
        		line[idx] = g.getFirst().getDaysName()+(noHtml?"\n":"<BR>")+g.getSecond().getDaysName();
        		cmp[idx++] = null;
        		
        		line[idx] = (noHtml?g.getFirst().getTime().getName(true)+"\n"+g.getSecond().getTime().getName(true):
        			g.getFirst().getTime().toHtml(false,false,true,true)+"<BR>"+g.getSecond().getTime().toHtml(false,false,true,true));
        		cmp[idx++] = null;
        		
        		line[idx] = rSB.toString();
        		cmp[idx++] = null;
        		
        		line[idx] = (noHtml?(g.isHard()?"true":""):g.isHard()?"<img src='images/checkmark.gif' border='0'/>":"");
        		cmp[idx++] = new Integer(g.isHard()?1:0);
        		
        		line[idx] = (g.isDistance()?String.valueOf(Math.round(g.getDistance()))+"m":"");
        		cmp[idx++] = new Double(g.getDistance());
        		
        		line[idx] = (noHtml?(g.isFixed()?"true":""):g.isFixed()?"<img src='images/checkmark.gif' border='0'/>":"");
        		cmp[idx++] = new Integer(g.isFixed()?1:0);
        		
        		line[idx] = (noHtml?(g.isCommited()?"true":""):g.isCommited()?"<img src='images/checkmark.gif' border='0'/>":"");
        		cmp[idx++] = new Integer(g.isCommited()?1:0);
        		
        		if (hasImportant) {
        			line[idx] = (noHtml?(g.isImportant()?"true":""):g.isImportant()?"<img src='images/checkmark.gif' border='0'/>":"");
        			cmp[idx++] = new Integer(g.isImportant()?1:0);
        		}
        		
        		if (hasInstructor) {
        			line[idx] = (noHtml?(g.isInstructor()?"true":""):g.isInstructor()?"<img src='images/checkmark.gif' border='0'/>":"");
        			cmp[idx++] = new Integer(g.isInstructor()?1:0);
        		}
        		
        		line[idx] = g.getCurriculumText();
        		cmp[idx++] = null;
        		
        		webTable.addLine(null, line, cmp);
        		
        		total[0] += Math.round(g.getJenrl());
        		if (g.isHard()) total[1] += Math.round(g.getJenrl());
        		if (g.isDistance()) total[2] += Math.round(g.getJenrl());
        		if (g.isFixed()) total[3] += Math.round(g.getJenrl());
        		if (g.isCommited()) total[4] += Math.round(g.getJenrl());
        		if (g.isImportant()) total[5] += Math.round(g.getJenrl());
        		if (g.isInstructor()) total[6] += (g.isInstructor() ? 1 : 0);
        	}
        	
    		String[] line = new String[hasImportant ? hasInstructor ? 12 : 11 : hasInstructor ? 11 : 10];
    		Comparable[] cmp = new Comparable[hasImportant ? hasInstructor ? 12 : 11 : hasInstructor ? 11 : 10];
    		int idx = 0;

    		line[idx] = String.valueOf(total[0]);
    		cmp[idx++] = new Double(total[0]);
    		
    		line[idx] = "<i>Total</i>";
    		cmp[idx++] = new DuoComparable(null, null);
    		
    		line[idx] = "";
    		cmp[idx++] = null;
    		
    		line[idx] = "";
    		cmp[idx++] = null;
    		
    		line[idx] = "";
    		cmp[idx++] = null;
    		
    		line[idx] = String.valueOf(total[1]);
    		cmp[idx++] = new Integer(total[1]);
    		
    		line[idx] = String.valueOf(total[2]);
    		cmp[idx++] = new Double(1000.0 * total[2]);
    		
    		line[idx] = String.valueOf(total[3]);
    		cmp[idx++] = new Integer(total[3]);
    		
    		line[idx] = String.valueOf(total[4]);
    		cmp[idx++] = new Integer(total[4]);
    		
    		if (hasImportant) {
    			line[idx] = String.valueOf(total[5]);
    			cmp[idx++] = new Integer(total[5]);
    		}
    		
    		if (hasInstructor) {
    			line[idx] = String.valueOf(total[6]);
    			cmp[idx++] = new Integer(total[6]);
    		}
    		
    		line[idx] = "";
        	cmp[idx++] = null;
        	
        	webTable.addLine(null, line, cmp);
        } catch (Exception e) {
        	Debug.error(e);
        	webTable.addLine(new String[] {"<font color='red'>ERROR:"+e.getMessage()+"</font>"},null);
        }
        return webTable;
	}
	
	public PdfWebTable getSameSubpartBalancingReportTable(HttpServletRequest request, SameSubpartBalancingReport report, boolean noHtml) {
		WebTable.setOrder(sessionContext,"solutionReports.sectBalancingReport.ord",request.getParameter("sect_ord"),1);
		String[] header = new String[2+Constants.SLOTS_PER_DAY_NO_EVENINGS/6];
		String[] pos = new String[2+Constants.SLOTS_PER_DAY_NO_EVENINGS/6];
		header[0]="Department";
		pos[0]="left";
		header[1]="Penalty";
		pos[1]="center";
		for (int i=0;i<Constants.SLOTS_PER_DAY_NO_EVENINGS/6;i++) {
			header[i+2]=Constants.slot2str(Constants.DAY_SLOTS_FIRST + i*6);
			pos[i+2]="center";
		}
		
		PdfWebTable webTable = new PdfWebTable( header.length,
   	        	"Section Balancing", "solutionReport.do?sect_ord=%%",
   	        	header, pos, null);
        webTable.setRowStyle("white-space:nowrap");
        
        try {
        	for (Iterator it=report.getGroups().iterator();it.hasNext();) {
        		SameSubpartBalancingReport.SameSubpartBalancingGroup g = (SameSubpartBalancingReport.SameSubpartBalancingGroup)it.next();
        		
        		String[] line = new String[2+Constants.SLOTS_PER_DAY_NO_EVENINGS/6];
        		Comparable[] cmp = new Comparable[2+Constants.SLOTS_PER_DAY_NO_EVENINGS/6];
        		
        		line[0]=g.getName();
        		cmp[0]=g.getName();
        		int penalty = 0;
        		for (int i=0;i<Constants.SLOTS_PER_DAY_NO_EVENINGS/6;i++) {
        			int slot = Constants.DAY_SLOTS_FIRST + i*6;
        			int usage = g.getUsage(slot);
        			int limit = g.getLimit(slot);
        			if (usage>limit)
        				penalty += g.getExcess(slot);
        			Vector classes = new Vector(g.getClasses(slot));
        			Collections.sort(classes);
        			StringBuffer sb = new StringBuffer();
        			StringBuffer toolTip = new StringBuffer();
        			int u = 0; boolean over = false;
        			for (Enumeration e=classes.elements();e.hasMoreElements();) {
        				ClassAssignmentDetails ca = (ClassAssignmentDetails)e.nextElement();
        				int nrMeetings = 0;
        				for (int j=0;j<Constants.NR_DAYS_WEEK;j++)
        					if ((Constants.DAY_CODES[j]&ca.getTime().getDays())!=0) nrMeetings++;
        				u+=nrMeetings;
        				if (u>limit && !over) {
        					over=true;
        					sb.append(noHtml?"\n":"<hr>");
        				}
        				sb.append(noHtml?ca.getClazz().getName():ca.getClazz().toHtml(true,true));//+" ("+nrMeetings+"x"+ca.getTime().getMin()+")");
        				if (e.hasMoreElements()) sb.append(noHtml?"\n":"<br>");
        				toolTip.append(ca.getClassName());
        				if (e.hasMoreElements()) toolTip.append(", ");
        			}
        			if (noHtml) {
        				line[i+2]=usage+" / "+limit;
        				line[i+2]+=(classes.isEmpty()?"":"\n"+sb.toString());
        			} else {
        				line[i+2]="<a title='"+toolTip+"'>"+(limit==0?"":(usage>limit?"<font color='red'>":"")+usage+" / "+limit+(usage>limit?"</font>":""))+"</a>";
        				line[i+2]+=(classes.isEmpty()?"":"<br>"+sb.toString());
        			}
        			cmp[i+2]=new Integer(usage*1000+limit);
        		}
        		line[1]=(noHtml?""+penalty:(penalty==0?"":"<font color='red'>+"+penalty+"</font>"));
        		cmp[1]=new Integer(penalty);
        		webTable.addLine(null,line,cmp);

        	}
        } catch (Exception e) {
        	Debug.error(e);
        	webTable.addLine(new String[] {"<font color='red'>ERROR:"+e.getMessage()+"</font>"},null);
        }
        return webTable;
	}
	
	private String disp(long value, boolean noHtml) {
		if (value==0) return "";
		return (noHtml?ClassAssignmentDetails.dispNumberNoHtml(value):ClassAssignmentDetails.dispNumber(value));
		//return String.valueOf(value);
	}
	
	public PdfWebTable getPerturbationReportTable(HttpServletRequest request, PerturbationReport report, boolean noHtml) {
		WebTable.setOrder(sessionContext,"solutionReports.pert.ord",request.getParameter("pert_ord"),1);
		PdfWebTable webTable = new PdfWebTable( 24,
   	        	"Perturbations", "solutionReport.do?pert_ord=%%",
   				new String[] {"Class", "Time", "Room", "Dist", "St", "StT", "StR", "StB", "Ins", "InsT", "InsR", "InsB", "Rm", "Bld", "Tm", "Day", "Hr", "TFSt", "TFIns", "DStC", "NStC", "DTPr", "DRPr", "DInsB"},
   				new String[] {"left", "left", "left", "left", "left", "left","left","left","left", "left", "left", "left", "left", "left","left","left","left", "left", "left", "left", "left", "left","left","left"},
   				null);
        webTable.setRowStyle("white-space:nowrap");
        
        try {
        	for (Iterator i=report.getGroups().iterator();i.hasNext();) {
        		PerturbationReport.PerturbationGroup g = (PerturbationReport.PerturbationGroup)i.next();
        		
        		webTable.addLine(null,
    	    		new String[] {
        				(noHtml?g.getClazz().getClazz().getName():g.getClazz().getClazz().toHtml(true, true)),
        				(noHtml?g.getClazz().getTimeNoHtml():g.getClazz().getTimeHtml()),
        				(noHtml?g.getClazz().getRoomNoHtml():g.getClazz().getRoomHtml()),
        				(Math.round(g.distance)>0?Math.round(g.distance)+"m":""),
        				disp(g.affectedStudents, noHtml),
        				disp(g.affectedStudentsByTime, noHtml),
        				disp(g.affectedStudentsByRoom, noHtml),
        				disp(g.affectedStudentsByBldg, noHtml),
        				disp(g.affectedInstructors, noHtml),
        				disp(g.affectedInstructorsByTime, noHtml),
        				disp(g.affectedInstructorsByRoom, noHtml),
        				disp(g.affectedInstructorsByBldg, noHtml),
        				disp(g.differentRoom, noHtml),
        				disp(g.differentBuilding, noHtml),
        				disp(g.differentTime, noHtml),
        				disp(g.differentDay, noHtml),
        				disp(g.differentHour, noHtml),
        				disp(g.tooFarForStudents, noHtml),
        				disp(g.tooFarForInstructors, noHtml),
        				disp(g.deltaStudentConflicts, noHtml),
        				disp(g.newStudentConflicts, noHtml),
        				disp(Math.round(g.deltaTimePreferences), noHtml),
        				disp(g.deltaRoomPreferences, noHtml),
        				disp(g.deltaInstructorDistancePreferences, noHtml)
        			},
        			new Comparable[] {
        				g.getClazz(),
        				g.getClazz().getTimeName(),
        				g.getClazz().getRoomName(),
        				new Double(g.distance),
        				new Long(g.affectedStudents),
        				new Long(g.affectedStudentsByTime),
        				new Long(g.affectedStudentsByRoom),
        				new Long(g.affectedStudentsByBldg),
        				new Integer(g.affectedInstructors),
        				new Integer(g.affectedInstructorsByTime),
        				new Integer(g.affectedInstructorsByRoom),
        				new Integer(g.affectedInstructorsByBldg),
        				new Integer(g.differentRoom),
        				new Integer(g.differentBuilding),
        				new Integer(g.differentTime),
        				new Integer(g.differentDay),
        				new Integer(g.differentHour),
        				new Integer(g.tooFarForStudents),
        				new Integer(g.tooFarForInstructors),
        				new Integer(g.deltaStudentConflicts),
        				new Integer(g.newStudentConflicts),
        				new Double(g.deltaTimePreferences),
        				new Integer(g.deltaRoomPreferences),
        				new Integer(g.deltaInstructorDistancePreferences)
        			});
        	}
        } catch (Exception e) {
        	Debug.error(e);
        	webTable.addLine(new String[] {"<font color='red'>ERROR:"+e.getMessage()+"</font>"},null);
        }
        return webTable;
	}
	
	public static class DuoComparable implements Comparable {
		private Comparable iA = null, iB = null;
		public DuoComparable(Comparable a, Comparable b) {
			iA = a; iB = b;
		}
		public int compareTo(Object o) {
			if (o==null || !(o instanceof DuoComparable)) return -1;
			DuoComparable d = (DuoComparable)o;
			int cmp = (iA == null ? (d.iA == null ? 0 : -1) : d.iA == null ? 1 : iA.compareTo(d.iA));
			if (cmp!=0) return cmp;
			return (iB == null ? (d.iB == null ? 0 : -1) : d.iB == null ? 1 : iB.compareTo(d.iB));
		}
	}
	public static class MultiComparable implements Comparable {
		private Vector iX = new Vector();
		public MultiComparable() {}
		public void add(Comparable x) { iX.addElement(x); }
		public int compareTo(Object o) {
			if (o==null || !(o instanceof MultiComparable)) return -1;
			MultiComparable m = (MultiComparable)o;
			Enumeration e1 = iX.elements(); Enumeration e2 = m.iX.elements();
			while (e1.hasMoreElements() && e2.hasMoreElements()) {
				int cmp = ((Comparable)e1.nextElement()).compareTo((Comparable)e2.nextElement());
				if (cmp!=0) return cmp;
			}
			return Double.compare(e1.hasMoreElements()?1:0,e2.hasMoreElements()?1:0);
		}
		
	}
	
}
