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
import org.unitime.timetable.model.DistributionPref;
import org.unitime.timetable.model.DistributionType;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.Preference;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.comparators.ClassComparator;
import org.unitime.timetable.model.comparators.SchedulingSubpartComparator;
import org.unitime.timetable.model.dao.Class_DAO;
import org.unitime.timetable.model.dao.CourseOfferingDAO;
import org.unitime.timetable.model.dao.DistributionTypeDAO;
import org.unitime.timetable.model.dao.SchedulingSubpartDAO;

/**
 * 
 * @author Tomas Muller, Stephanie Schluttenhofer
 *
 */
@Service("/distributionPrefsAjax")
public class DistributionPrefsAjax extends Action {
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        
        response.addHeader("Content-Type", "text/xml; charset=UTF-8");
        request.setCharacterEncoding("UTF-8");
        
        //System.out.println("type:"+request.getParameter("type")); 
        //System.out.println("id:  "+request.getParameter("id"));
        
        PrintWriter out = response.getWriter();
        
        //System.out.println("response:");
        out.print("<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n");
        out.print("<results>");
        coumputeSuggestionList(request, out);
        out.print("</results>");
        
        return null;        

    }
    
    public String escapeXml(String s) {
        return (s == null ? "" : s).replaceAll("&", "&amp;").replaceAll(">", "&gt;").replaceAll("<", "&lt;").replaceAll("\"", "&quot;").replaceAll("'", "&apos;");
    }
    
    protected void print(PrintWriter out, String id, String value) throws IOException {
        //System.out.println("  <result id=\""+id+"\" value=\""+value+"\" />");
        out.print("<result id=\""+id+"\" value=\""+escapeXml(value)+"\" />");
    }
    
    protected void print(PrintWriter out, String id, String value, String extra) throws IOException {
        //System.out.println("  <result id=\""+id+"\" value=\""+value+"\" extra=\""+extra+"\" />");
        out.print("<result id=\""+id+"\" value=\""+escapeXml(value)+"\" extra=\""+escapeXml(extra)+"\" />");
    }

    protected void coumputeSuggestionList(HttpServletRequest request, PrintWriter out) throws Exception {
        if ("subjectArea".equals(request.getParameter("type"))) {
            coumputeCourseNumbers(request.getParameter("id"),out);
        } else if ("courseNbr".equals(request.getParameter("type"))) {
            coumputeSubparts(request.getParameter("id"),out);
        } else if ("itype".equals(request.getParameter("type"))) {
            coumputeClasses(request.getParameter("id"),out);
        } else if ("grouping".equals(request.getParameter("type"))) {
            coumputeGroupingDesc(request.getParameter("id"),out);
        } else if ("distType".equals(request.getParameter("type"))) {
            computePreferenceLevels(request.getParameter("id"),out);
        } else if ("exam".equals(request.getParameter("type"))) {
            coumputeExams(request.getParameter("id"), Long.valueOf(request.getParameter("examType")),out);
        }
    }
    
    protected void coumputeGroupingDesc(String groupingId, PrintWriter out) throws Exception {
        try {
            for (int i=0;i<DistributionPref.sGroupings.length;i++)
                if (DistributionPref.sGroupings[i].equals(groupingId))
                    print(out, "desc", DistributionPref.getGroupingDescription(i).replaceAll("<", "@lt@").replaceAll(">", "@gt@").replaceAll("\"","@quot@").replaceAll("&","@amp@"));
        } catch (Exception e) {
            print(out, "desc", "");
        }
    }
    
    protected void computePreferenceLevels(String distTypeId, PrintWriter out) throws Exception {
        if (distTypeId==null || distTypeId.length()==0 || distTypeId.equals(Preference.BLANK_PREF_VALUE)) return;
        DistributionType dist = new DistributionTypeDAO().get(Long.valueOf(distTypeId));
        print(out, "desc", dist.getDescr().replaceAll("<", "@lt@").replaceAll(">", "@gt@").replaceAll("\"","@quot@").replaceAll("&","@amp@"));
        for (PreferenceLevel pref: PreferenceLevel.getPreferenceLevelList()) {
            if (dist.isAllowed(pref))
                print(out, pref.getPrefId().toString(), pref.getPrefName(), pref.prefcolor());
        }
    }
    
    
    protected void coumputeCourseNumbers(String subjectAreaId, PrintWriter out) throws Exception {
        if (subjectAreaId==null || subjectAreaId.length()==0 || subjectAreaId.equals(Preference.BLANK_PREF_VALUE)) return;
        List courseNumbers = new CourseOfferingDAO().
            getSession().
            createQuery("select co.uniqueId, co.courseNbr, co.title from CourseOffering co "+
                    "where co.subjectArea.uniqueId = :subjectAreaId "+
                    "and co.instructionalOffering.notOffered = false and co.isControl = true " +
                    "order by co.courseNbr ").
            setFetchSize(200).
            setCacheable(true).
            setLong("subjectAreaId", Long.parseLong(subjectAreaId)).
            list();
        for (Iterator i=courseNumbers.iterator();i.hasNext();) {
            Object[] o = (Object[])i.next();
            print(out, o[0].toString(), (o[1].toString() + " - " + (o[2] == null?"":o[2])));
        }
    }
    
    protected void coumputeSubparts(String courseOfferingId, PrintWriter out) throws Exception {
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
    
    protected void coumputeClasses(String schedulingSubpartId, PrintWriter out) throws Exception {
        if (schedulingSubpartId==null || schedulingSubpartId.length()==0 || schedulingSubpartId.equals(Preference.BLANK_PREF_VALUE)) return;
        TreeSet classes = new TreeSet(new ClassComparator(ClassComparator.COMPARE_BY_HIERARCHY));
        classes.addAll(new Class_DAO().
            getSession().
            createQuery("select distinct c from Class_ c "+
                    "where c.schedulingSubpart.uniqueId=:schedulingSubpartId").
            setFetchSize(200).
            setCacheable(true).
            setLong("schedulingSubpartId", Long.parseLong(schedulingSubpartId)).
            list());
        print(out, "-1", "All");
        for (Iterator i=classes.iterator();i.hasNext();) {
            Class_ c = (Class_)i.next();
            print(out, c.getUniqueId().toString(), c.getSectionNumberString()); 
        }
    }
   
    protected void coumputeExams(String courseOfferingId, Long examType, PrintWriter out) throws Exception {
        if (courseOfferingId==null || courseOfferingId.length()==0 || courseOfferingId.equals(Preference.BLANK_PREF_VALUE)) return;
        TreeSet exams = new TreeSet(Exam.findExamsOfCourseOffering(Long.valueOf(courseOfferingId),examType));
        if (exams.size()>1 || exams.isEmpty())
            print(out, "-1", "-");
        for (Iterator i=exams.iterator();i.hasNext();) {
            Exam exam = (Exam)i.next();
            print(out, exam.getUniqueId().toString(), exam.getLabel().replaceAll("&","@amp@")); 
        }
    }

}
