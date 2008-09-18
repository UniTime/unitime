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
package org.unitime.timetable.form;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.Globals;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.util.MessageResources;
import org.unitime.commons.web.Web;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.Preference;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.CourseOfferingDAO;
import org.unitime.timetable.util.ComboBoxLookup;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.DynamicList;
import org.unitime.timetable.util.DynamicListObjectFactory;
import org.unitime.timetable.util.IdValue;


public class ExamDistributionPrefsForm extends ActionForm {
    private String op;
    private String distPrefId;
    private String distType;
    private String prefLevel;
    private String description;
    
    private List subjectArea;
    private List courseNbr;
    private List exam;
    private int iExamType;
    private boolean iHasMidtermExams = false;
    
	private String filterSubjectAreaId;
	private Collection filterSubjectAreas;
	private String filterCourseNbr;
	
	private boolean canAdd;
	private boolean canSeeAll;
    

    protected DynamicListObjectFactory factory = new DynamicListObjectFactory() {
        public Object create() { return new Long(-1); }
    };

    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {

        ActionErrors errors = new ActionErrors();

        // Get Message Resources
        MessageResources rsc = 
            (MessageResources) super.getServlet()
            	.getServletContext().getAttribute(Globals.MESSAGES_KEY);

        // Distribution Type must be selected
        if(distType==null || distType.equals(Preference.BLANK_PREF_VALUE)) {
	        errors.add("distType", 
	                new ActionMessage(
	                        "errors.generic", "Select a distribution type. ") );
        }
        
        // Distribution Pref Level must be selected
        if(prefLevel==null || prefLevel.equals(Preference.BLANK_PREF_VALUE)) {
	        errors.add("prefLevel", 
	                new ActionMessage(
	                        "errors.generic", "Select a preference level. ") );
        }
        
        // Save/Update clicked
        if(op.equals(rsc.getMessage("button.addNew")) || op.equals(rsc.getMessage("button.update")) ) {

        }

        return errors;
    }

    /** 
     * Method reset
     * @param mapping
     * @param request
     */
    public void reset(ActionMapping mapping, HttpServletRequest request) {
        op="";
        distPrefId="";
        distType=Preference.BLANK_PREF_VALUE;
        prefLevel=Preference.BLANK_PREF_VALUE;
        subjectArea = DynamicList.getInstance(new ArrayList(), factory);    
        courseNbr = DynamicList.getInstance(new ArrayList(), factory);    
        exam = DynamicList.getInstance(new ArrayList(), factory);    
        filterSubjectAreaId = (String)request.getSession().getAttribute(Constants.SUBJ_AREA_ID_ATTR_NAME);
        filterCourseNbr = (String)request.getSession().getAttribute(Constants.CRS_NBR_ATTR_NAME); 
        filterSubjectAreas = new ArrayList();
        iExamType = Exam.sExamTypeFinal;
        if (request.getSession().getAttribute("Exam.Type")!=null)
        	iExamType = (Integer)request.getSession().getAttribute("Exam.Type");
        canAdd = false; canSeeAll = false;
        try {
            iHasMidtermExams = Exam.hasMidtermExams(Session.getCurrentAcadSession(Web.getUser(request.getSession())).getUniqueId());
        } catch (Exception e) {}
    }

    
    public String getDistPrefId() { return distPrefId; }
    public void setDistPrefId(String distPrefId) { this.distPrefId = distPrefId; }
    public String getOp() { return op; }
    public void setOp(String op) { this.op = op; }
    public String getPrefLevel() { return prefLevel; }
    public void setPrefLevel(String prefLevel) { this.prefLevel = prefLevel; }
    public String getDistType() { return distType; }
    public void setDistType(String distType) { this.distType = distType; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List getSubjectAreaList() { return subjectArea; }
    public List getSubjectArea() { return subjectArea; }
    public Long getSubjectArea(int key) { return (Long)subjectArea.get(key); }
    public void setSubjectArea(int key, Long value) { this.subjectArea.set(key, value); }
    public void setSubjectArea(List subjectArea) { this.subjectArea = subjectArea; }
    public List getCourseNbr() { return courseNbr; }
    public Long getCourseNbr(int key) { return (Long)courseNbr.get(key); }
    public void setCourseNbr(int key, Long value) { this.courseNbr.set(key, value); }
    public void setCourseNbr(List courseNbr) { this.courseNbr = courseNbr; }
    public List getExam() { return exam; }
    public Long getExam(int key) { return (Long)exam.get(key); }
    public void setExam(int key, Long value) { this.exam.set(key, value); }
    public void setExam(List itype) { this.exam = itype; }
    
    public boolean getCanAdd() { return canAdd; }
    public void setCanAdd(boolean canAdd) { this.canAdd = canAdd; }

    public boolean getCanSeeAll() { return canSeeAll; }
    public void setCanSeeAll(boolean canSeeAll) { this.canSeeAll = canSeeAll; }

    public void deleteExam(int key) {
        subjectArea.remove(key);
        courseNbr.remove(key);
        exam.remove(key);
    }

    public void swapExams(int i1, int i2) {
        Object objSa1 = subjectArea.get(i1);
        Object objCo1 = courseNbr.get(i1);
        Object objEx1 = exam.get(i1);
        
        Object objSa2 = subjectArea.get(i2);
        Object objCo2 = courseNbr.get(i2);
        Object objEx2 = exam.get(i2);
        
        subjectArea.set(i1, objSa2);
        subjectArea.set(i2, objSa1);
        courseNbr.set(i1, objCo2);
        courseNbr.set(i2, objCo1);
        exam.set(i1, objEx2);
        exam.set(i2, objEx1);
    }
    
    public String getFilterSubjectAreaId() { return filterSubjectAreaId; }
    public void setFilterSubjectAreaId(String filterSubjectAreaId) { this.filterSubjectAreaId = filterSubjectAreaId; }
    public String getFilterCourseNbr() { return filterCourseNbr; }
    public void setFilterCourseNbr(String filterCourseNbr) { this.filterCourseNbr = filterCourseNbr; }
    public Collection getFilterSubjectAreas() { return filterSubjectAreas; }
    public void setFilterSubjectAreas(Collection filterSubjectAreas) { this.filterSubjectAreas = filterSubjectAreas;}
    
    public Collection getCourseNbrs(int idx) { 
        Vector ret = new Vector();
        boolean contains = false;
        if (getSubjectArea(idx)>=0) {
            for (Iterator i= new CourseOfferingDAO().
                    getSession().
                    createQuery("select co.uniqueId, co.courseNbr from CourseOffering co "+
                            "where co.uniqueCourseNbr.subjectArea.uniqueId = :subjectAreaId "+
                            "and co.instructionalOffering.notOffered = false "+
                            "order by co.courseNbr ").
                    setFetchSize(200).
                    setCacheable(true).
                    setLong("subjectAreaId", getSubjectArea(idx)).iterate();i.hasNext();) {
                Object[] o = (Object[])i.next();
                ret.add(new IdValue((Long)o[0],(String)o[1]));
                if (o[0].equals(getCourseNbr(idx))) contains = true;
            }
        }
        if (!contains) setCourseNbr(idx, -1L);
        if (ret.size()==1) setCourseNbr(idx, ((IdValue)ret.firstElement()).getId());
        else ret.insertElementAt(new IdValue(-1L,"-"), 0);
        return ret;
    }
    
    public Collection getExams(int idx) { 
        Vector ret = new Vector();
        boolean contains = false;
        if (getCourseNbr(idx)>=0) {
            TreeSet exams = new TreeSet(Exam.findExamsOfCourseOffering(getCourseNbr(idx),getExamType())); 
            for (Iterator i=exams.iterator();i.hasNext();) {
                Exam exam = (Exam)i.next();
                ret.add(new IdValue(exam.getUniqueId(),exam.getLabel()));
                if (exam.getUniqueId().equals(getExam(idx))) contains = true;
            }
        }
        if (!contains) setExam(idx, -1L);
        if (ret.size()==1) setCourseNbr(idx, ((IdValue)ret.firstElement()).getId());
        else ret.insertElementAt(new IdValue(-1L,"-"), 0);
        return ret;
    }
    
    public int getExamType() { return iExamType; }
    public void setExamType(int type) { iExamType = type; }
    public Collection getExamTypes() {
    	Vector ret = new Vector(Exam.sExamTypes.length);
    	for (int i=0;i<Exam.sExamTypes.length;i++) {
            if (i==Exam.sExamTypeMidterm && !iHasMidtermExams) continue;
    		ret.add(new ComboBoxLookup(Exam.sExamTypes[i], String.valueOf(i)));
    	}
    	return ret;
    }
    
}
