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
package org.unitime.timetable.webutil;

import java.io.OutputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;

import org.unitime.commons.web.WebTable;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.DistributionObject;
import org.unitime.timetable.model.DistributionPref;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.PreferenceGroup;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.comparators.ClassComparator;
import org.unitime.timetable.model.comparators.ClassInstructorComparator;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.model.dao.SubjectAreaDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.PdfEventHandler;
import org.unitime.timetable.util.PdfFont;

import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;


/**
 * Builds HTML tables for distribution preferences
 * 
 * @author Heston Fernandes, Tomas Muller, Zuzana Mullerova
 */
public class DistributionPrefsTableBuilder {
	
	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);
	
	public String getAllDistPrefsTableForCurrentUser(HttpServletRequest request, SessionContext context, String subjectAreaId, String courseNbr) throws Exception {
		if (subjectAreaId.equals(Constants.BLANK_OPTION_VALUE))
		    return "";
		if (subjectAreaId.equals(Constants.ALL_OPTION_VALUE))
		    subjectAreaId = null;
		
		Long subjAreaId = null;
		if (subjectAreaId!=null && subjectAreaId.length()>0) {
			subjAreaId = Long.valueOf(subjectAreaId); 
		}
		
        Collection prefs = new HashSet();
        for (Department d: Department.getUserDepartments(context.getUser())) {
			prefs.addAll(DistributionPref.getPreferences(context.getUser().getCurrentAcademicSessionId(), d.getUniqueId(), true, null, subjAreaId, (courseNbr==null || courseNbr.length()==0 ? null : courseNbr)));
            prefs.addAll(DistributionPref.getInstructorPreferences(context.getUser().getCurrentAcademicSessionId(),d.getUniqueId(),subjAreaId, (courseNbr==null || courseNbr.length()==0 ? null : courseNbr)));
		}
		
		return toHtmlTable(request, context, prefs, true); 
	}

	public void getAllDistPrefsTableForCurrentUserAsPdf(OutputStream out, SessionContext context, String subjectAreaId, String courseNbr) throws Exception {

		if (subjectAreaId.equals(Constants.BLANK_OPTION_VALUE))
            subjectAreaId = null;
        else if (subjectAreaId.equals(Constants.ALL_OPTION_VALUE))
            subjectAreaId = null;

		String title = null;
		
		Long subjAreaId = null;
		if (subjectAreaId!=null && subjectAreaId.length()>0) {
			subjAreaId = Long.valueOf(subjectAreaId);
			SubjectArea area = (new SubjectAreaDAO()).get(subjAreaId);
			title = area.getSubjectAreaAbbreviation()+(courseNbr==null?"":" "+courseNbr);
		}
		
        Collection prefs = new HashSet();
		for (Department d: Department.getUserDepartments(context.getUser())) {
            prefs.addAll(DistributionPref.getPreferences(context.getUser().getCurrentAcademicSessionId(), d.getUniqueId(), true, null, subjAreaId, (courseNbr==null || courseNbr.length()==0 ? null : courseNbr)));
            prefs.addAll(DistributionPref.getInstructorPreferences(context.getUser().getCurrentAcademicSessionId(),d.getUniqueId(),subjAreaId, (courseNbr==null || courseNbr.length()==0 ? null : courseNbr)));
        }
		
		Session session = SessionDAO.getInstance().get(context.getUser().getCurrentAcademicSessionId());
		if (title==null)
			title = session.getLabel()+" Distribution Preferences";
		else
			title += " - "+session.getLabel()+" Distribution Preferences";
		
		toPdfTable(out, context, prefs, title); 
	}

	public String getDistPrefsTableForClass(HttpServletRequest request, SessionContext context, Class_ clazz) {
		if (clazz.getManagingDept()==null) return null;
		
		Set prefs = clazz.effectiveDistributionPreferences(null); 
			//DistributionPref.getPreferences(clazz.getSessionId(), clazz.getManagingDept().getUniqueId(), false, clazz.getUniqueId());

		List<DepartmentalInstructor> leadInstructors = clazz.getLeadInstructors();
		if (!leadInstructors.isEmpty()) {
			for (DepartmentalInstructor instructor: leadInstructors) {
				prefs.addAll(instructor.getDistributionPreferences());
			}
		}
		
		return toHtmlTable(request, context, prefs, false); 
	}

    public String getDistPrefsTableForExam(HttpServletRequest request, SessionContext context, Exam exam, boolean editable) {
        Set prefs = exam.effectivePreferences(DistributionPref.class); 

        return toHtmlTable(request, context, prefs, false); 
    }

    public String getDistPrefsTableForSchedulingSubpart(HttpServletRequest request, SessionContext context, SchedulingSubpart subpart) {
		if (subpart.getManagingDept()==null) return null;
		
		
		// Collection prefs = DistributionPref.getPreferences(subpart.getSessionId(), subpart.getManagingDept().getUniqueId(), false, subpart.getUniqueId());
		
		Set leadInstructors = new HashSet();
		Set prefs = subpart.getDistributionPreferences();
		for (Iterator i=subpart.getClasses().iterator();i.hasNext();) {
			Class_ clazz = (Class_)i.next();
			prefs.addAll(clazz.getDistributionPreferences());
			leadInstructors.addAll(clazz.getLeadInstructors());
		}
		
		for (Iterator i=leadInstructors.iterator();i.hasNext();) {
			DepartmentalInstructor instructor = (DepartmentalInstructor)i.next();
			prefs.addAll(instructor.getDistributionPreferences());
		}
		
		return toHtmlTable(request, context, prefs, false); 
	}
	
	public String getDistPrefsTableForInstructionalOffering(HttpServletRequest request, SessionContext context, InstructionalOffering instructionalOffering) throws Exception {
		//Collection prefs = DistributionPref.getPreferences(instructionalOffering.getSessionId(), null, false, new Long(instructionalOffering.getUniqueId().intValue()));
		
		Set leadInstructors = new HashSet();
		Set prefs = new TreeSet();
		for (Iterator i=instructionalOffering.getInstrOfferingConfigs().iterator();i.hasNext();) {
			InstrOfferingConfig config = (InstrOfferingConfig)i.next();
			for (Iterator j=config.getSchedulingSubparts().iterator();j.hasNext();) {
				SchedulingSubpart subpart = (SchedulingSubpart)j.next();
				prefs.addAll(subpart.getDistributionPreferences());
				for (Iterator k=subpart.getClasses().iterator();k.hasNext();) {
					Class_ clazz = (Class_)k.next();
					prefs.addAll(clazz.getDistributionPreferences());
					leadInstructors.addAll(clazz.getLeadInstructors());
				}
			}
		}

		for (Iterator i=leadInstructors.iterator();i.hasNext();) {
			DepartmentalInstructor instructor = (DepartmentalInstructor)i.next();
			prefs.addAll(instructor.getDistributionPreferences());
		}
		
		return toHtmlTable(request, context, prefs, false); 
	}


	/**
     * Build a html table with the list representing distribution prefs 
     * @param distPrefs
     * @param ordCol
     * @param editable
     * @return
     */
    public String toHtmlTable(HttpServletRequest request, SessionContext context, Collection distPrefs, boolean addButton) {
    	String title = MSG.sectionTitleDistributionPreferences();
 
    	String backType = request.getParameter("backType");
    	String backId = request.getParameter("backId");
    	
    	String instructorFormat = UserProperty.NameFormat.get(context.getUser());
        
        if (addButton && context.hasPermission(Right.DistributionPreferenceAdd)) {
        	title = "<table width='100%'><tr><td width='100%'>" + 
        		 	"<DIV class=\"WelcomeRowHeadNoLine\">" + MSG.sectionTitleDistributionPreferences() +"</DIV>"+
        		 	"</td><td style='padding-bottom: 2px'>"+
        		 	"<input type=\"submit\" name=\"op\" class=\"btn\" accesskey='A' title='Add New Distribution Preference (Alt+A)' value=\"Add Distribution Preference\">"+
        		 	"</td></tr></table>";
        }
        
        WebTable.setOrder(context,"distPrefsTable.ord",request.getParameter("order"),4);
        
        WebTable tbl = new WebTable(4, 
        		title,  
    			"distributionPrefs.do?order=%%",
    			new String[] {MSG.columnDistrPrefType(), MSG.columnDistrPrefStructure(), MSG.columnDistrPrefOwner(), MSG.columnDistrPrefClass() },
    			new String[] { "left", "left", "left", "left"},
    			new boolean[] { true, true, true, true } );
        
        int nrPrefs = 0;

        for (Iterator i1=distPrefs.iterator();i1.hasNext();) {
        	DistributionPref dp = (DistributionPref)i1.next();
        	
        	if (!context.hasPermission(dp, Right.DistributionPreferenceDetail)) continue;

        	nrPrefs++;
        	
        	String objStr = "";
        	
        	PreferenceGroup pg = dp.getOwner();
        	
        	String ownerType = "Unknown";
        	if (pg instanceof Department) {
        		Department d = (Department)pg;
        		ownerType = d.getManagingDeptAbbv();
        	}
        	
        	for (Iterator i2=dp.getOrderedSetOfDistributionObjects().iterator();i2.hasNext();) {
        		DistributionObject dO = (DistributionObject)i2.next();
        		objStr += dO.preferenceText();
        		if (i2.hasNext()) objStr += "<BR>";
        	}

            String groupingText = dp.getGroupingName();
            Comparable groupingCmp = (dp.getGrouping()==null?"0":dp.getGrouping().toString());

            if (pg instanceof DepartmentalInstructor) {
        		DepartmentalInstructor instructor = (DepartmentalInstructor)pg;
        		Set<Department> owners = new TreeSet<Department>();
        		TreeSet classes = new TreeSet(new ClassInstructorComparator(new ClassComparator(ClassComparator.COMPARE_BY_HIERARCHY)));
        		classes.addAll(instructor.getClasses());
        		for (Iterator i2=classes.iterator();i2.hasNext();) {
        			ClassInstructor clazz = (ClassInstructor)i2.next();
        			if (!clazz.isLead().booleanValue()) continue;
        			if (objStr.length()>0) objStr += "<BR>";
            		objStr += clazz.getClassInstructing().toString();
        			Department dept = clazz.getClassInstructing().getManagingDept();
            		if (dept.isInheritInstructorPreferences()) owners.add(dept);
        		}
        		ownerType = "";
        		for (Department owner: owners)
        			ownerType += (ownerType.isEmpty() ? "" : "<br>") + owner.getManagingDeptAbbv();
        		groupingText = "Instructor "+instructor.getName(instructorFormat);
        		groupingCmp = instructor.getName(instructorFormat);
                //prefEditable = false;
        		if (owners.isEmpty()) continue;
        	}
             
        	String distType = dp.getDistributionType().getLabel();
            String prefLevel = dp.getPrefLevel().getPrefName();
            String prefColor = dp.getPrefLevel().prefcolor();
        	if (PreferenceLevel.sNeutral.equals(dp.getPrefLevel().getPrefProlog()))
        		prefColor = "gray";
            String onClick = null;
            
            boolean gray = false;
            
            if (pg instanceof DepartmentalInstructor) {
            	if (context.hasPermission(pg, Right.InstructorDetail))
            		onClick = "onClick=\"document.location='instructorDetail.do"
            				+ "?instructorId=" + dp.getOwner().getUniqueId().toString() 
            				+ "&op=Show%20Instructor%20Preferences'\"";
            } else {
            	if (context.hasPermission(dp, Right.DistributionPreferenceEdit))
            		onClick = "onClick=\"document.location='distributionPrefs.do"
            				+ "?dp=" + dp.getUniqueId().toString() 
            				+ "&op=view'\"";
            }
            
            boolean back = "PreferenceGroup".equals(backType) && dp.getUniqueId().toString().equals(backId);
            
            tbl.addLine(
                    onClick, 
                	new String[] { 
                    		(back?"<A name=\"back\"</A>":"")+
                    		(gray?"<span style='color:gray;'>":"<span style='color:"+prefColor+";font-weight:bold;' title='"+prefLevel+" "+distType+"'>")+distType+"</span>",
                    		(gray?"<span style='color:gray;'>":"")+groupingText+(gray?"</span>":""),
                    		(gray?"<span style='color:gray;'>":"")+ownerType+(gray?"</span>":""), 
                    		(gray?"<span style='color:gray;'>":"")+objStr+(gray?"</span>":"")
                    	}, 
                   	new Comparable[] { distType, groupingCmp, ownerType, objStr });
            
        }       
        
        if (nrPrefs==0)
            tbl.addLine(null,  new String[] { MSG.noPreferencesFound(), "", "", "" }, null);
        
        return tbl.printTable(WebTable.getOrder(context,"distPrefsTable.ord"));
    }

    public void toPdfTable(OutputStream out, SessionContext context, Collection distPrefs, String title) throws Exception {
    	String instructorFormat = UserProperty.NameFormat.get(context.getUser());
        
        PdfWebTable tbl = new PdfWebTable(5, 
        		title,  
    			null,
    			new String[] {"Preference", "Type", "Structure", "Owner", "Class" },
    			new String[] { "left", "left", "left", "left", "left"},
    			new boolean[] { true, true, true, true, true} );
        
        int nrPrefs = 0;

        for (Iterator i1=distPrefs.iterator();i1.hasNext();) {
        	DistributionPref dp = (DistributionPref)i1.next();
        	
        	if (!context.hasPermission(dp, Right.DistributionPreferenceDetail)) continue;
        	
        	nrPrefs++;
        	
        	String objStr = "";
        	
        	PreferenceGroup pg = dp.getOwner();
        	
        	String ownerType = "Unknown";
        	if (pg instanceof Department) {
        		Department d = (Department)pg;
        		ownerType = d.getShortLabel();
        	}
        	
        	for (Iterator i2=dp.getOrderedSetOfDistributionObjects().iterator();i2.hasNext();) {
        		DistributionObject dO = (DistributionObject)i2.next();
        		objStr += dO.preferenceText();
        		if (i2.hasNext()) objStr += "\n";
        	}

            String groupingText = dp.getGroupingName();
            Comparable groupingCmp = (dp.getGrouping()==null?"0":dp.getGrouping().toString());

            if (pg instanceof DepartmentalInstructor) {
        		DepartmentalInstructor instructor = (DepartmentalInstructor)pg;
        		Department d = instructor.getDepartment();
        		ownerType = d.getShortLabel();
        		TreeSet classes = new TreeSet(new ClassInstructorComparator(new ClassComparator(ClassComparator.COMPARE_BY_HIERARCHY)));
        		classes.addAll(instructor.getClasses());
        		for (Iterator i2=classes.iterator();i2.hasNext();) {
        			ClassInstructor clazz = (ClassInstructor)i2.next();
        			if (!clazz.isLead().booleanValue()) continue;
        			if (objStr.length()>0) objStr += "\n";
            		objStr += clazz.getClassInstructing().toString();
        		}
        		groupingText = "Instructor "+instructor.getName(instructorFormat);
        		groupingCmp = instructor.getName(instructorFormat);
        	}
             
        	String distType = dp.getDistributionType().getLabel();
            String prefLevel = dp.getPrefLevel().getPrefName();
            String onClick = null;
            
            tbl.addLine(
                    onClick, 
                	new String[] { 
                    		prefLevel,
                    		distType,
                    		groupingText,
                    		ownerType,
                    		objStr
                    	}, 
                   	new Comparable[] {
                    		prefLevel,
                    		distType,
                            groupingCmp,
                    		ownerType,
                    		objStr
                    });
            
        }
        
        if (nrPrefs==0)
            tbl.addLine(null,  new String[] { "No preferences found", "", "", "", "" }, null);
        
        int ord = WebTable.getOrder(context, "distPrefsTable.ord");
        ord = (ord>0?1:-1)*(1+Math.abs(ord));

        PdfPTable table = tbl.printPdfTable(ord);

        float width = tbl.getWidth();

        Document doc = new Document(new Rectangle(60f + width, 60f + 1.30f * width),30,30,30,30); 

        PdfWriter iWriter = PdfWriter.getInstance(doc, out);
        iWriter.setPageEvent(new PdfEventHandler());
        doc.open();

        if (tbl.getName()!=null)
        	doc.add(new Paragraph(tbl.getName(), PdfFont.getBigFont(true)));

        doc.add(table);

        doc.close();
    }
}
