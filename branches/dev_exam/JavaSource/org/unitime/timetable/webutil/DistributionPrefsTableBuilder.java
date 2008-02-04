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
package org.unitime.timetable.webutil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.unitime.commons.Debug;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.commons.web.WebTable;
import org.unitime.timetable.ApplicationProperties;
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
import org.unitime.timetable.model.Roles;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Settings;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.comparators.ClassComparator;
import org.unitime.timetable.model.comparators.ClassInstructorComparator;
import org.unitime.timetable.model.dao.SubjectAreaDAO;
import org.unitime.timetable.model.dao.TimetableManagerDAO;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.PdfEventHandler;

import com.lowagie.text.Document;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;


/**
 * Builds HTML tables for distribution preferences
 * 
 * @author Heston Fernandes
 */
public class DistributionPrefsTableBuilder {
	
	public String getAllDistPrefsTableForCurrentUser(HttpServletRequest request, String subjectAreaId, String courseNbr) throws Exception {
        User user = Web.getUser(request.getSession());
		Session session = Session.getCurrentAcadSession(user);
		boolean isAdmin = user.getCurrentRole().equals(Roles.ADMIN_ROLE);
		boolean isViewAll = user.getCurrentRole().equals(Roles.VIEW_ALL_ROLE);
		
		if (subjectAreaId.equals(Constants.BLANK_OPTION_VALUE))
		    return "";
		if (subjectAreaId.equals(Constants.ALL_OPTION_VALUE))
		    subjectAreaId = null;
		
		String ownerId = (String) user.getAttribute(Constants.TMTBL_MGR_ID_ATTR_NAME);
		TimetableManager manager = new TimetableManagerDAO().get(new Long(ownerId));
		
		Long subjAreaId = null;
		if (subjectAreaId!=null && subjectAreaId.length()>0) {
			subjAreaId = Long.valueOf(subjectAreaId); 
		}
		
        Collection prefs = new HashSet();
		if (isAdmin || isViewAll) {
			prefs.addAll(DistributionPref.getPreferences(session.getUniqueId(), null, true, null, subjAreaId, (courseNbr==null || courseNbr.length()==0 ? null : courseNbr)));
            prefs.addAll(DistributionPref.getInstructorPreferences(session.getUniqueId(),null,subjAreaId, (courseNbr==null || courseNbr.length()==0 ? null : courseNbr)));
		} else {
			for (Iterator i=manager.departmentsForSession(session.getUniqueId()).iterator();i.hasNext();) {
				Department d = (Department)i.next();
				prefs.addAll(DistributionPref.getPreferences(session.getUniqueId(), d.getUniqueId(), true, null, subjAreaId, (courseNbr==null || courseNbr.length()==0 ? null : courseNbr)));
                prefs.addAll(DistributionPref.getInstructorPreferences(session.getUniqueId(),d.getUniqueId(),subjAreaId, (courseNbr==null || courseNbr.length()==0 ? null : courseNbr)));
			}
		}
		
		return toHtmlTable(request, session, (isAdmin || isViewAll?null:manager), (isAdmin || isViewAll?null:manager.departmentsForSession(session.getUniqueId())), prefs, !isViewAll, !isViewAll); 
	}

	public File getAllDistPrefsTableForCurrentUserAsPdf(HttpServletRequest request, String subjectAreaId, String courseNbr) throws Exception {
        User user = Web.getUser(request.getSession());
		Session session = Session.getCurrentAcadSession(user);
		boolean isAdmin = user.getCurrentRole().equals(Roles.ADMIN_ROLE);
		boolean isViewAll = user.getCurrentRole().equals(Roles.VIEW_ALL_ROLE);
		
        if (subjectAreaId.equals(Constants.BLANK_OPTION_VALUE))
            subjectAreaId = null;
        else if (subjectAreaId.equals(Constants.ALL_OPTION_VALUE))
            subjectAreaId = null;

		String ownerId = (String) user.getAttribute(Constants.TMTBL_MGR_ID_ATTR_NAME);
		TimetableManager manager = new TimetableManagerDAO().get(new Long(ownerId));
		
		String title = null;
		
		Long subjAreaId = null;
		if (subjectAreaId!=null && subjectAreaId.length()>0) {
			subjAreaId = Long.valueOf(subjectAreaId);
			SubjectArea area = (new SubjectAreaDAO()).get(subjAreaId);
			title = area.getSubjectAreaAbbreviation()+(courseNbr==null?"":" "+courseNbr);
		}
		
        Collection prefs = new HashSet();
        if (isAdmin || isViewAll) {
            prefs.addAll(DistributionPref.getPreferences(session.getUniqueId(), null, true, null, subjAreaId, (courseNbr==null || courseNbr.length()==0 ? null : courseNbr)));
            prefs.addAll(DistributionPref.getInstructorPreferences(session.getUniqueId(),null,subjAreaId, (courseNbr==null || courseNbr.length()==0 ? null : courseNbr)));
        } else {
            for (Iterator i=manager.departmentsForSession(session.getUniqueId()).iterator();i.hasNext();) {
                Department d = (Department)i.next();
                prefs.addAll(DistributionPref.getPreferences(session.getUniqueId(), d.getUniqueId(), true, null, subjAreaId, (courseNbr==null || courseNbr.length()==0 ? null : courseNbr)));
                prefs.addAll(DistributionPref.getInstructorPreferences(session.getUniqueId(),d.getUniqueId(),subjAreaId, (courseNbr==null || courseNbr.length()==0 ? null : courseNbr)));
            }
        }
		
		if (title==null)
			title = session.getLabel()+" Distribution Preferences";
		else
			title += " - "+session.getLabel()+" Distribution Preferences";
		
		return toPdfTable(request, session, (isAdmin || isViewAll?null:manager), (isAdmin || isViewAll?null:manager.departmentsForSession(session.getUniqueId())), prefs, title); 
	}

	public String getDistPrefsTableForClass(HttpServletRequest request, Class_ clazz, boolean editable) {
		if (clazz.getManagingDept()==null) return null;
		
		Set prefs = clazz.effectiveDistributionPreferences(null); 
			//DistributionPref.getPreferences(clazz.getSessionId(), clazz.getManagingDept().getUniqueId(), false, clazz.getUniqueId());

		Vector leadInstructors = clazz.getLeadInstructors();
		if (!leadInstructors.isEmpty()) {
			for (Enumeration e=leadInstructors.elements();e.hasMoreElements();) {
				DepartmentalInstructor instructor = (DepartmentalInstructor)e.nextElement();
				prefs.addAll(instructor.getDistributionPreferences());
			}
		}
		
		Vector depts = new Vector(1); depts.addElement(clazz.getManagingDept());

        User user = Web.getUser(request.getSession());
		boolean isAdmin = user.getCurrentRole().equals(Roles.ADMIN_ROLE);
		String ownerId = (String) user.getAttribute(Constants.TMTBL_MGR_ID_ATTR_NAME);
		TimetableManager manager = new TimetableManagerDAO().get(new Long(ownerId));

		return toHtmlTable(request, clazz.getSession(), (isAdmin?null:manager), depts, prefs, editable, false); 
	}

    public String getDistPrefsTableForExam(HttpServletRequest request, Exam exam, boolean editable) {
        Set prefs = exam.effectivePreferences(DistributionPref.class); 

        User user = Web.getUser(request.getSession());
        boolean isAdmin = user.getCurrentRole().equals(Roles.ADMIN_ROLE);
        String ownerId = (String) user.getAttribute(Constants.TMTBL_MGR_ID_ATTR_NAME);
        TimetableManager manager = new TimetableManagerDAO().get(new Long(ownerId));

        return toHtmlTable(request, exam.getSession(), (isAdmin?null:manager), null, prefs, manager.canEditExams(exam.getSession(), user), false); 
    }

    public String getDistPrefsTableForSchedulingSubpart(HttpServletRequest request, SchedulingSubpart subpart, boolean editable) {
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
		
        User user = Web.getUser(request.getSession());
		boolean isAdmin = user.getCurrentRole().equals(Roles.ADMIN_ROLE);
		String ownerId = (String) user.getAttribute(Constants.TMTBL_MGR_ID_ATTR_NAME);
		TimetableManager manager = new TimetableManagerDAO().get(new Long(ownerId));

		Vector depts = new Vector(1); depts.addElement(subpart.getManagingDept());
		return toHtmlTable(request, subpart.getSession(), (isAdmin?null:manager), depts, prefs, editable, false); 
	}
	
	public String getDistPrefsTableForInstructionalOffering(HttpServletRequest request, InstructionalOffering instructionalOffering, boolean editable) throws Exception {
		//Collection prefs = DistributionPref.getPreferences(instructionalOffering.getSessionId(), null, false, new Long(instructionalOffering.getUniqueId().intValue()));
		
        User user = Web.getUser(request.getSession());
        boolean isAdmin = user.getCurrentRole().equals(Roles.ADMIN_ROLE);

		String ownerId = (String) user.getAttribute(Constants.TMTBL_MGR_ID_ATTR_NAME);
		TimetableManager manager = new TimetableManagerDAO().get(new Long(ownerId));
		Session session = Session.getCurrentAcadSession(user);
		
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
		
		return toHtmlTable(request, instructionalOffering.getSession(), (isAdmin?null:manager), (isAdmin?null:manager.departmentsForSession(session.getUniqueId())), prefs, editable, false); 
	}


	/**
     * Build a html table with the list representing distribution prefs 
     * @param distPrefs
     * @param ordCol
     * @param editable
     * @return
     */
    public String toHtmlTable(HttpServletRequest request, Session session, TimetableManager manager, Collection departments, Collection distPrefs, boolean editable, boolean showAddButton) {
    	String title = "Distribution Preferences";
    	
    	String backType = request.getParameter("backType");
    	String backId = request.getParameter("backId");
    	
    	User user = Web.getUser(request.getSession());
    	String instructorFormat = Settings.getSettingValue(user, Constants.SETTINGS_INSTRUCTOR_NAME_FORMAT);
        
        if (showAddButton) {
        	title = "<table width='100%'><tr><td width='100%'>" + 
        		 	"<DIV class=\"WelcomeRowHeadNoLine\">Distribution Preferences</DIV>"+
        		 	"</td><td style='padding-bottom: 2px'>"+
        		 	"<input type=\"submit\" name=\"op\" class=\"btn\" accesskey='A' title='Add New Distribution Preference (Alt+A)' value=\"Add Distribution Preference\">"+
        		 	"</td></tr></table>";
        }
        
        WebTable.setOrder(request.getSession(),"distPrefsTable.ord",request.getParameter("order"),4);
        
        WebTable tbl = new WebTable(4, 
        		title,  
    			"distributionPrefs.do?order=%%",
    			new String[] {" Type ", " Structure ", " Owner ", " Class " },
    			new String[] { "left", "left", "left", "left"},
    			new boolean[] { true, true, true, true } );
        
        int nrPrefs = 0;

        for (Iterator i1=distPrefs.iterator();i1.hasNext();) {
        	DistributionPref dp = (DistributionPref)i1.next();
            boolean prefEditable = editable;
        	
        	if (departments!=null) {
        		boolean visible = false;
        		
        		for (Iterator i2=departments.iterator();i2.hasNext();) {
        			Department department = (Department)i2.next();
        			if (dp.isVisible(session, department)) {
        				visible = true; break;
        			}
        		}
        	
        		if (!visible) continue;
        	}
        	
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
        		ownerType = instructor.getDepartment().getManagingDeptAbbv();
        		TreeSet classes = new TreeSet(new ClassInstructorComparator(new ClassComparator(ClassComparator.COMPARE_BY_HIERARCHY)));
        		classes.addAll(instructor.getClasses());
        		boolean allExternallyManaged = true;
        		for (Iterator i2=classes.iterator();i2.hasNext();) {
        			ClassInstructor clazz = (ClassInstructor)i2.next();
        			if (!clazz.isLead().booleanValue()) continue;
        			if (allExternallyManaged && !clazz.getClassInstructing().getManagingDept().isExternalManager().booleanValue())
        				allExternallyManaged=false;
        			if (objStr.length()>0) objStr += "<BR>";
            		objStr += clazz.getClassInstructing().toString();
        		}
        		groupingText = "Instructor "+instructor.getName(instructorFormat);
        		groupingCmp = instructor.getName(instructorFormat);
                //prefEditable = false;
        		if (allExternallyManaged) continue;
        	}
             
        	String distType = dp.getDistributionType().getLabel();
            String prefLevel = dp.getPrefLevel().getPrefName();
            String prefColor = dp.getPrefLevel().prefcolor();
        	if (PreferenceLevel.sNeutral.equals(dp.getPrefLevel().getPrefProlog()))
        		prefColor = "gray";
            String onClick = null;
            
            boolean gray = false;
            
            if (prefEditable) {
            	if (manager==null || dp.isEditable(session,manager)) {
                    if (pg instanceof DepartmentalInstructor) {
                        onClick = "onClick=\"document.location='instructorDetail.do"
                            + "?instructorId=" + dp.getOwner().getUniqueId().toString() 
                            + "&op=Show%20Instructor%20Preferences'\"";
                    } else {
                        onClick = "onClick=\"document.location='distributionPrefs.do"
                            + "?dp=" + dp.getUniqueId().toString() 
                            + "&op=view'\"";
                    }
            	} //else gray = true;
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
            tbl.addLine(null,  new String[] { "No preferences found", "", "", "" }, null);
        
        return tbl.printTable(WebTable.getOrder(request.getSession(),"distPrefsTable.ord"));
    }

    public File toPdfTable(HttpServletRequest request, Session session, TimetableManager manager, Collection departments, Collection distPrefs, String title) {
    	User user = Web.getUser(request.getSession());
    	String instructorFormat = Settings.getSettingValue(user, Constants.SETTINGS_INSTRUCTOR_NAME_FORMAT);
        
        PdfWebTable tbl = new PdfWebTable(5, 
        		title,  
    			null,
    			new String[] {"Preference", "Type", "Structure", "Owner", "Class" },
    			new String[] { "left", "left", "left", "left", "left"},
    			new boolean[] { true, true, true, true, true} );
        
        int nrPrefs = 0;

        for (Iterator i1=distPrefs.iterator();i1.hasNext();) {
        	DistributionPref dp = (DistributionPref)i1.next();
        	
        	if (departments!=null) {
        		boolean visible = false;
        		
        		for (Iterator i2=departments.iterator();i2.hasNext();) {
        			Department department = (Department)i2.next();
        			if (dp.isVisible(session, department)) {
        				visible = true; break;
        			}
        		}
        	
        		if (!visible) continue;
        	}
        	
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
            String prefColor = dp.getPrefLevel().prefcolor();
        	if (PreferenceLevel.sNeutral.equals(dp.getPrefLevel().getPrefProlog()))
        		prefColor = "gray";
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
        
        FileOutputStream out = null;
        try {
        	File file = ApplicationProperties.getTempFile("distpref", "pdf");
            int ord = WebTable.getOrder(request.getSession(),"distPrefsTable.ord");
            ord = (ord>0?1:-1)*(1+Math.abs(ord));
        	
        	PdfPTable table = tbl.printPdfTable(ord);
        	
        	float width = tbl.getWidth();
        	
        	Document doc = new Document(new Rectangle(60f + width, 60f + 1.30f * width),30,30,30,30); 

        	out = new FileOutputStream(file);
			PdfWriter iWriter = PdfWriter.getInstance(doc, out);
			iWriter.setPageEvent(new PdfEventHandler());
    		doc.open();
    		
    		if (tbl.getName()!=null)
    			doc.add(new Paragraph(tbl.getName(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16)));
    		
    		doc.add(table);
    		
    		doc.close();
            
        	return file;
        } catch (Exception e) {
        	Debug.error(e);
        } finally {
        	try {
        		if (out!=null) out.close();
        	} catch (IOException e) {}
        }
    	return null;
    }
}
