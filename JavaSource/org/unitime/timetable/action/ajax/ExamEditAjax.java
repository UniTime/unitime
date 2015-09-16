/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
*/
package org.unitime.timetable.action.ajax;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.springframework.stereotype.Service;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.Preference;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.comparators.ClassComparator;
import org.unitime.timetable.model.comparators.InstrOfferingConfigComparator;
import org.unitime.timetable.model.comparators.SchedulingSubpartComparator;
import org.unitime.timetable.model.dao.Class_DAO;
import org.unitime.timetable.model.dao.CourseOfferingDAO;
import org.unitime.timetable.model.dao.InstrOfferingConfigDAO;
import org.unitime.timetable.model.dao.SchedulingSubpartDAO;

/**
 * 
 * @author Tomas Muller, Stephanie Schluttenhofer
 *
 */
@Service("/examEditAjax")
public class ExamEditAjax extends Action {
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        
        response.addHeader("Content-Type", "text/xml; charset=UTF-8");
        request.setCharacterEncoding("UTF-8");
        
        PrintWriter out = response.getWriter();
        
        out.print("<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n");
        out.print("<results>");
        coumputeSuggestionList(request, out);
        out.print("</results>");
        
        return null;        

    }
    
    protected void print(PrintWriter out, String id, String value) throws IOException {
        out.print("<result id=\""+id+"\" value=\""+value+"\" />");
    }
    
    protected void print(PrintWriter out, String id, String value, String extra) throws IOException {
        out.print("<result id=\""+id+"\" value=\""+value+"\" extra=\""+extra+"\" />");
    }

    protected void coumputeSuggestionList(HttpServletRequest request, PrintWriter out) throws Exception {
        if ("subjectArea".equals(request.getParameter("type"))) {
            coumputeCourseNumbers(request.getParameter("id"),out);
        } else if ("courseNbr".equals(request.getParameter("type"))) {
            coumputeSubparts(request.getParameter("id"),out);
        } else if ("itype".equals(request.getParameter("type"))) {
            coumputeClasses(request.getParameter("id"), request.getParameter("courseId"), out);
        }
    }
    
    protected void coumputeCourseNumbers(String subjectAreaId, PrintWriter out) throws Exception {
        if (subjectAreaId==null || subjectAreaId.length()==0 || subjectAreaId.equals(Preference.BLANK_PREF_VALUE)) {
            print(out, "-1", "N/A");
            return;
        }
        List courseNumbers = new CourseOfferingDAO().
            getSession().
            createQuery("select co.uniqueId, co.courseNbr, co.title from CourseOffering co "+
                    "where co.subjectArea.uniqueId = :subjectAreaId "+
                    "and co.instructionalOffering.notOffered = false " +
                    "order by co.courseNbr ").
            setFetchSize(200).
            setCacheable(true).
            setLong("subjectAreaId", Long.parseLong(subjectAreaId)).
            list();
        if (courseNumbers.isEmpty()) print(out, "-1", "N/A");
        if (courseNumbers.size()>1) print(out, "-1", "-");
        for (Iterator i=courseNumbers.iterator();i.hasNext();) {
            Object[] o = (Object[])i.next();
            print(out, o[0].toString(), (o[1].toString() + " - " + (o[2] == null?"":o[2].toString().replaceAll(">", "&gt;").replaceAll("<", "&lt;").replaceAll("'", "&quot;").replaceAll("&", "&amp;"))));
        }
    }
    
    protected void coumputeSubparts(String courseOfferingId, PrintWriter out) throws Exception {
        if (courseOfferingId==null || courseOfferingId.length()==0 || courseOfferingId.equals(Preference.BLANK_PREF_VALUE)) {
            print(out, "0", "N/A");
            return;
        }
        CourseOffering course = new CourseOfferingDAO().get(Long.parseLong(courseOfferingId));
        if (course==null) {
            print(out, "0", "N/A");
            return;
        }
        if (course.isIsControl()) print(out,String.valueOf(Long.MIN_VALUE+1),"Offering");
        print(out, String.valueOf(Long.MIN_VALUE), "Course");
        if (!course.isIsControl()) return;
        TreeSet configs = new TreeSet(new InstrOfferingConfigComparator(null));
        configs.addAll(new InstrOfferingConfigDAO().
            getSession().
            createQuery("select distinct c from " +
                    "InstrOfferingConfig c inner join c.instructionalOffering.courseOfferings co "+
                    "where co.uniqueId = :courseOfferingId").
            setFetchSize(200).
            setCacheable(true).
            setLong("courseOfferingId", course.getUniqueId()).
            list());
        TreeSet subparts = new TreeSet(new SchedulingSubpartComparator(null));
        subparts.addAll(new SchedulingSubpartDAO().
            getSession().
            createQuery("select distinct s from " +
                    "SchedulingSubpart s inner join s.instrOfferingConfig.instructionalOffering.courseOfferings co "+
                    "where co.uniqueId = :courseOfferingId").
            setFetchSize(200).
            setCacheable(true).
            setLong("courseOfferingId", course.getUniqueId()).
            list());
        if (!configs.isEmpty()) {
            print(out, String.valueOf(Long.MIN_VALUE+2),"-- Configurations --");
            for (Iterator i=configs.iterator();i.hasNext();) {
                InstrOfferingConfig c = (InstrOfferingConfig)i.next();
                print(out,String.valueOf(-c.getUniqueId()), c.getName() + (c.getInstructionalMethod() == null ? "" : " (" + c.getInstructionalMethod().getLabel() + ")"));
            }
        }
        if (!configs.isEmpty() && !subparts.isEmpty())
            print(out,String.valueOf(Long.MIN_VALUE+2),"-- Subparts --");
        for (Iterator i=subparts.iterator();i.hasNext();) {
            SchedulingSubpart s = (SchedulingSubpart)i.next();
            String id = s.getUniqueId().toString();
            String name = s.getItype().getAbbv();
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
    
    protected void coumputeClasses(String schedulingSubpartId, String courseId, PrintWriter out) throws Exception {
        if (schedulingSubpartId==null || schedulingSubpartId.length()==0 || schedulingSubpartId.equals(Preference.BLANK_PREF_VALUE)) {
            print(out, "-1", "N/A");
            return;
        }
        SchedulingSubpart subpart = (Long.parseLong(schedulingSubpartId)>0?new SchedulingSubpartDAO().get(Long.valueOf(schedulingSubpartId)):null);
        if (subpart==null) {
            print(out, "-1", "N/A");
            return;
        }
        CourseOffering co = null;
        if (courseId != null && !courseId.isEmpty()) {
        	co = CourseOfferingDAO.getInstance().get(Long.valueOf(courseId));
        }
        TreeSet classes = new TreeSet(new ClassComparator(ClassComparator.COMPARE_BY_HIERARCHY));
        classes.addAll(new Class_DAO().
            getSession().
            createQuery("select distinct c from Class_ c "+
                    "where c.schedulingSubpart.uniqueId=:schedulingSubpartId").
            setFetchSize(200).
            setCacheable(true).
            setLong("schedulingSubpartId", Long.parseLong(schedulingSubpartId)).
            list());
        if (classes.size()>1)
            print(out, "-1", "-");
        for (Iterator i=classes.iterator();i.hasNext();) {
            Class_ c = (Class_)i.next();
            String extId = c.getClassSuffix(co);
            print(out, c.getUniqueId().toString(), c.getSectionNumberString() + (extId == null || extId.isEmpty() || extId.equalsIgnoreCase(c.getSectionNumberString()) ? "" : " - " + extId)); 
        }
    }
   

}
