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
package org.unitime.timetable.action.ajax;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.TimePattern;
import org.unitime.timetable.model.TimePatternModel;
import org.unitime.timetable.model.dao.InstructionalOfferingDAO;
import org.unitime.timetable.model.dao.TimePatternDAO;

/**
 * 
 * @author Tomas Muller
 *
 */
public class SectioningDemoAjax extends Action {

    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        
        response.addHeader("Content-Type", "text/xml");
        
        String word = request.getParameter("word");
        
        ServletOutputStream out = response.getOutputStream();
        
        out.print("<?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?>\n");
        out.print("<results>");
        coumputeSuggestionList(request, out);
        out.print("</results>");
        
        return null;        

    }
    
    protected void print(ServletOutputStream out, String id, String value) throws IOException {
        out.print("<result id=\""+id+"\" value=\""+value+"\" />");
    }
    
    protected void coumputeSuggestionList(HttpServletRequest request, ServletOutputStream out) throws Exception {
        if ("subjectArea".equals(request.getParameter("type"))) {
            coumputeCourseNumbers(request.getParameter("id"),out);
        } else if ("timePattern".equals(request.getParameter("type"))) {
            out.print("<days>");
            coumputeDays(request.getParameter("id"),out);
            out.print("</days>");
            out.print("<times>");
            coumputeTimes(request.getParameter("id"),out);
            out.print("</times>");
        }
    }
    
    protected void coumputeCourseNumbers(String subjectAreaId, ServletOutputStream out) throws Exception {
        if (subjectAreaId==null || subjectAreaId.length()==0) return;
        List courseNumbers = new InstructionalOfferingDAO().
            getSession().
            createQuery("select co from InstructionalOffering as io , CourseOffering co "+
                    "where co.subjectArea.uniqueId = :subjectAreaId "+
                    "and io.uniqueId = co.instructionalOffering.uniqueId "+
                    "and co.instructionalOffering.notOffered = false "+
                    "and io.notOffered = false order by co.courseNbr ").
            setFetchSize(200).
            setCacheable(true).
            setLong("subjectAreaId", Long.parseLong(subjectAreaId)).
            list();
        for (Iterator i=courseNumbers.iterator();i.hasNext();) {
            CourseOffering co = (CourseOffering)i.next();
            print(out, co.getUniqueId().toString(), (co.getCourseNbr() + " - " + (co.getTitle() == null?"":co.getTitle().replaceAll(">", "&gt;").replaceAll("<", "&lt;").replaceAll("'", "&quot;").replaceAll("&", "&amp;"))));
        }
    }
    
    protected void coumputeTimes(String timePatternId, ServletOutputStream out) throws Exception {
        if (timePatternId==null || timePatternId.length()==0) return;
        TimePattern tp = new TimePatternDAO().get(Long.valueOf(timePatternId));
        if (tp==null) return;
        TimePatternModel m = tp.getTimePatternModel();
        Vector ret = new Vector();
        for (int i=0;i<m.getNrTimes();i++)
            print(out, String.valueOf(i), m.getStartTime(i)+" - "+m.getEndTime(i));
    }

    protected void coumputeDays(String timePatternId, ServletOutputStream out) throws Exception {
        if (timePatternId==null || timePatternId.length()==0) return;
        TimePattern tp = new TimePatternDAO().get(Long.valueOf(timePatternId));
        if (tp==null) return;
        TimePatternModel m = tp.getTimePatternModel();
        Vector ret = new Vector();
        for (int i=0;i<m.getNrDays();i++)
            print(out, String.valueOf(i), m.getDayHeader(i));
    }
}
