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
package org.unitime.timetable.webutil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;

import org.unitime.commons.Debug;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.commons.web.WebTable;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.Designator;
import org.unitime.timetable.model.Settings;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.dao.DepartmentalInstructorDAO;
import org.unitime.timetable.model.dao.SubjectAreaDAO;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.PdfEventHandler;

import com.itextpdf.text.Document;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;


/**
 * Build Designator List for a specific subject area or instructor
 * 
 * @author Heston Fernandes
 */
public class DesignatorListBuilder {

    /**
     * Build Designator List for a specific subject area
     * @param request
     * @param subjectAreaId
     * @return
     */
    public String htmlTableForSubjectArea(
            HttpServletRequest request, 
            String subjectAreaId, 
            int order ) {
        
		// Create new table
        WebTable webTable = new WebTable( 2,
			    "",
			    "designatorList.do?order=%%&subjectAreaId=" + subjectAreaId,
			    new String[] {"Instructor", "Code"},
			    new String[] {"left", "left"},
			    new boolean[] {true, true} );
        webTable.enableHR("#EFEFEF");


        SubjectAreaDAO sDao = new SubjectAreaDAO();
        SubjectArea sa = sDao.get(new Long(subjectAreaId));
        Set designators = sa.getDesignatorInstructors();
        
        if (designators==null || designators.size()==0) {
		    webTable.addLine(
		        	"",
		        	new String[] { "<font class='errorCell'>No designators found for this subject area</font>", "&nbsp;" },
		        	null );
            
        }
        else {
            User user = Web.getUser(request.getSession());            
            String nameFormat = Settings.getSettingValue(user, Constants.SETTINGS_INSTRUCTOR_NAME_FORMAT);
			String instructorSortOrder = Settings.getSettingValue(user, Constants.SETTINGS_INSTRUCTOR_SORT);
            
            for (Iterator i=designators.iterator(); i.hasNext(); ) {
                Designator d = (Designator) i.next();
                String name = d.getInstructor().getName(nameFormat);
				String nameOrd = d.getInstructor().nameLastNameFirst().toLowerCase();
				if (instructorSortOrder!=null && instructorSortOrder.equals(Constants.SETTINGS_INSTRUCTOR_SORT_NATURAL))
				    nameOrd = name.toLowerCase();

				webTable.addLine(
    		        	d.canUserEdit(user) ? "onClick=\"document.location.href='designatorEdit.do?op=Edit&id="+d.getUniqueId()+"';\"" : null,
    		        	new String[] { name + " &nbsp;", d.getCode() },
    		        	new Comparable[] { nameOrd, d.getCode() } );
                
            }
            
        }
        
        return webTable.printTable(order);
    }    
    
    /**
     * Build Designator List for a specific instructor
     * @param request
     * @param instructorId
     * @return
     */
    public String htmlTableForInstructor(
            HttpServletRequest request, 
            String instructorId, 
            int order ) {
        
        User user = Web.getUser(request.getSession());
        DepartmentalInstructorDAO iDao = new DepartmentalInstructorDAO() ;
        DepartmentalInstructor di = iDao.get(new Long(instructorId));
        String puid = di.getExternalUniqueId();
        Set designators = null;
        
        if (puid==null || puid.trim().length()==0) {
            designators = di.getDesignatorSubjectAreas();
        }
        else {
            designators = new HashSet();
    		String sessionId = user.getAttribute(Constants.SESSION_ID_ATTR_NAME).toString();
    		List all = DepartmentalInstructor.getAllForInstructor(di, new Long(sessionId));
    		TreeSet sortedAll = new TreeSet(all);
    		for (Iterator iterInstDept = sortedAll.iterator(); iterInstDept.hasNext();) {
    			DepartmentalInstructor anotherDi = (DepartmentalInstructor) iterInstDept.next();
    			designators.addAll(anotherDi.getDesignatorSubjectAreas());
    		}
        }
        
        if (designators!=null && designators.size()>0) {
            WebTable webTable = new WebTable( 2,
    			    "",
    			    "instructorDetail.do?order=%%&instructorId=" + instructorId,
    			    new String[] {"Subject Area", "Code"},
    			    new String[] {"left", "left"},
    			    new boolean[] {true, true} );

            webTable.enableHR("#EFEFEF");

            for (Iterator i=designators.iterator(); i.hasNext(); ) {
                Designator d = (Designator) i.next();
                String sa = d.getSubjectArea().getSubjectAreaAbbreviation();
    		    webTable.addLine(
    		            d.canUserEdit(user) ? "onClick=\"document.location.href='designatorEdit.do?op=Edit&id="+d.getUniqueId()+"';\"" : null,
    		        	new String[] { d.getSubjectArea().getSubjectAreaAbbreviation() + " &nbsp;", d.getCode() },
    		        	new Comparable[] { sa, d.getCode() }  );
            }

            return webTable.printTable(order);
        }
        
        return "<TR><TD>&nbsp;</TD></TR>";
    }    
    
    public void pdfTableForSubjectArea(
            HttpServletRequest request, 
            String subjectAreaId, 
            int order ) {
        
        SubjectAreaDAO sDao = new SubjectAreaDAO();
        SubjectArea sa = sDao.get(new Long(subjectAreaId));
        Set designators = sa.getDesignatorInstructors();
    	
		// Create new table
        PdfWebTable webTable = new PdfWebTable( 2,
        		sa.getSubjectAreaAbbreviation()+" Designator List",
			    null,
			    new String[] {"Instructor", "Code"},
			    new String[] {"left", "left"},
			    new boolean[] {true, true} );
        webTable.enableHR("#EFEFEF");
        
        if (designators==null || designators.size()==0) 
        	return;
        User user = Web.getUser(request.getSession());            
        String nameFormat = Settings.getSettingValue(user, Constants.SETTINGS_INSTRUCTOR_NAME_FORMAT);
		String instructorSortOrder = Settings.getSettingValue(user, Constants.SETTINGS_INSTRUCTOR_SORT);
        
        for (Iterator i=designators.iterator(); i.hasNext(); ) {
            Designator d = (Designator) i.next();
            String name = d.getInstructor().getName(nameFormat);
			String nameOrd = d.getInstructor().nameLastNameFirst().toLowerCase();
			if (instructorSortOrder!=null && instructorSortOrder.equals(Constants.SETTINGS_INSTRUCTOR_SORT_NATURAL))
			    nameOrd = name.toLowerCase();

			webTable.addLine(
		        	null,
		        	new String[] { name, d.getCode() },
		        	new Comparable[] { nameOrd, d.getCode() } );
        }
        
        FileOutputStream out = null;
		try {
			File file = ApplicationProperties.getTempFile("designators", "pdf");
			
			out = new FileOutputStream(file);

			Document doc = new Document(PageSize.LETTER,30,30,30,30);
			PdfEventHandler.initFooter(doc, out);
			doc.open();
			
			doc.add(new Paragraph(webTable.getName(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16)));
			doc.add(webTable.printPdfTable(order));
			
			doc.close();
		
			request.setAttribute(Constants.REQUEST_OPEN_URL, "temp/"+file.getName());
		} catch (Exception e) {
			Debug.error(e);
    	} finally {
    		try {
    			if (out!=null) out.close();
    		} catch (IOException e) {}
    	}
    }    
}
