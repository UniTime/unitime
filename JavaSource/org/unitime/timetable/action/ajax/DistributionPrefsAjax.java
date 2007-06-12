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
package org.unitime.timetable.action.ajax;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.Preference;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.comparators.SchedulingSubpartComparator;
import org.unitime.timetable.model.dao.Class_DAO;
import org.unitime.timetable.model.dao.CourseOfferingDAO;
import org.unitime.timetable.model.dao.SchedulingSubpartDAO;

/**
 * 
 * @author Tomas Muller
 *
 */
public class DistributionPrefsAjax extends Action {
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
        } else if ("courseNbr".equals(request.getParameter("type"))) {
            coumputeSubparts(request.getParameter("id"),out);
        } else  if ("itype".equals(request.getParameter("type"))) {
            coumputeClasses(request.getParameter("id"),out);
        }
    }
    
    protected void coumputeCourseNumbers(String subjectAreaId, ServletOutputStream out) throws Exception {
        if (subjectAreaId==null || subjectAreaId.length()==0 || subjectAreaId.equals(Preference.BLANK_PREF_VALUE)) return;
        List courseNumbers = new CourseOfferingDAO().
            getSession().
            createQuery("select co.uniqueId, co.courseNbr from CourseOffering co "+
                    "where co.uniqueCourseNbr.subjectArea.uniqueId = :subjectAreaId "+
                    "and co.instructionalOffering.notOffered = false and co.isControl = true " +
                    "order by co.courseNbr ").
            setFetchSize(200).
            setCacheable(true).
            setLong("subjectAreaId", Long.parseLong(subjectAreaId)).
            list();
        for (Iterator i=courseNumbers.iterator();i.hasNext();) {
            Object[] o = (Object[])i.next();
            print(out, o[0].toString(), o[1].toString());
        }
    }
    
    protected void coumputeSubparts(String courseOfferingId, ServletOutputStream out) throws Exception {
        if (courseOfferingId==null || courseOfferingId.length()==0 || courseOfferingId.equals(Preference.BLANK_PREF_VALUE)) return;
        TreeSet subparts = new TreeSet(new SchedulingSubpartComparator(null));
        subparts.addAll(new SchedulingSubpartDAO().
            getSession().
            createQuery("select distinct s from " +
                    "SchedulingSubpart s inner join s.instrOfferingConfig.instructionalOffering.courseOfferings co "+
                    "where co.uniqueId = :courseOfferingId").
            setFetchSize(200).
            setCacheable(true).
            setLong("courseOfferingId", Long.parseLong(courseOfferingId)).
            list());
        for (Iterator i=subparts.iterator();i.hasNext();) {
            SchedulingSubpart s = (SchedulingSubpart)i.next();
            String id = s.getUniqueId().toString();
            String name = s.getItype().getSmas_abbv();
            String sufix = s.getSchedulingSubpartSuffix();
            while (s.getParentSubpart()!=null) {
                name = "_"+name;
                s = s.getParentSubpart();
            }
            if (s.getInstrOfferingConfig().getInstructionalOffering().getInstrOfferingConfigs().size()>1)
                name += " ["+s.getInstrOfferingConfig().getName()+"]";
            print(out, id, name+(sufix==null || sufix.length()==0?"":" ("+sufix+")"));
        }
    }
    
    protected void coumputeClasses(String schedulingSubpartId, ServletOutputStream out) throws Exception {
        if (schedulingSubpartId==null || schedulingSubpartId.length()==0 || schedulingSubpartId.equals(Preference.BLANK_PREF_VALUE)) return;
        List classes = new Class_DAO().
            getSession().
            createQuery("select distinct c from Class_ c "+
                    "where c.schedulingSubpart.uniqueId=:schedulingSubpartId "+
                    "order by c.uniqueId").
            setFetchSize(200).
            setCacheable(true).
            setLong("schedulingSubpartId", Long.parseLong(schedulingSubpartId)).
            list();
        print(out, "-1", "All");
        for (Iterator i=classes.iterator();i.hasNext();) {
            Class_ c = (Class_)i.next();
            print(out, c.getUniqueId().toString(), c.getSectionNumberString()); 
        }
    }
   

}
