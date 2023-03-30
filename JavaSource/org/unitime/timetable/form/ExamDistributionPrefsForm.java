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
package org.unitime.timetable.form;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import java.util.Vector;

import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.ExaminationMessages;
import org.unitime.timetable.action.UniTimeAction;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.Preference;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.dao.CourseOfferingDAO;
import org.unitime.timetable.util.IdValue;


/**
 * @author Tomas Muller, Stephanie Schluttenhofer
 */
public class ExamDistributionPrefsForm implements UniTimeForm {
	private static final long serialVersionUID = -822886662425670241L;
	protected static ExaminationMessages MSG = Localization.create(ExaminationMessages.class);
	private String op;
    private String distPrefId;
    private String distType;
    private String prefLevel;
    private String description;
    
    private List<Long> subjectArea;
    private List<Long> courseNbr;
    private List<Long> exam;
    private Long iExamType;
    
	private String filterSubjectAreaId;
	private Collection<SubjectArea> filterSubjectAreas;
	private List<IdValue> subjectAreas;
	private String filterCourseNbr;
    
    public ExamDistributionPrefsForm() {
    	 reset();
    }

    public void validate(UniTimeAction action) {
        // Distribution Type must be selected
        if (distType==null || distType.equals(Preference.BLANK_PREF_VALUE)) {
        	action.addFieldError("distType", MSG.errorSelectDistributionType());
        }
        
        // Distribution Pref Level must be selected
        if (prefLevel==null || prefLevel.equals(Preference.BLANK_PREF_VALUE)) {
        	action.addFieldError("prefLevel", MSG.errorSelectPreferenceLevel());
        }
    }

    /** 
     * Method reset
     * @param mapping
     * @param request
     */
    public void reset() {
        op="";
        distPrefId=null;
        distType=Preference.BLANK_PREF_VALUE;
        prefLevel=Preference.BLANK_PREF_VALUE;
        subjectArea = new ArrayList<Long>();
        courseNbr = new ArrayList<Long>();
        exam = new ArrayList<Long>();
        filterSubjectAreaId = null;
        filterCourseNbr = null; 
        filterSubjectAreas = new ArrayList<SubjectArea>();
        iExamType = null;
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

    public List<Long> getSubjectAreaList() { return subjectArea; }
    public List<Long> getSubjectArea() { return subjectArea; }
    public Long getSubjectArea(int key) {
    	return subjectArea.get(key);
    }
    public void setSubjectArea(int key, Long value) { this.subjectArea.set(key, value); }
    public void setSubjectArea(List<Long> subjectArea) { this.subjectArea = subjectArea; }
    public List<Long> getCourseNbr() { return courseNbr; }
    public Long getCourseNbr(int key) {
    	return courseNbr.get(key);
    }
    public void setCourseNbr(int key, Long value) { this.courseNbr.set(key, value); }
    public void setCourseNbr(List courseNbr) { this.courseNbr = courseNbr; }
    public List<Long> getExam() { return exam; }
    public Long getExam(int key) {
    	return exam.get(key);
    }
    public void setExam(int key, Long value) { this.exam.set(key, value); }
    public void setExam(List itype) { this.exam = itype; }
    
    public void deleteExam(int key) {
        subjectArea.remove(key);
        courseNbr.remove(key);
        exam.remove(key);
    }

    public void swapExams(int i1, int i2) {
    	Long objSa1 = subjectArea.get(i1);
        Long objCo1 = courseNbr.get(i1);
        Long objEx1 = exam.get(i1);
        
        Long objSa2 = subjectArea.get(i2);
        Long objCo2 = courseNbr.get(i2);
        Long objEx2 = exam.get(i2);
        
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
    public Collection<SubjectArea> getFilterSubjectAreas() { return filterSubjectAreas; }
    public void setFilterSubjectAreas(Collection<SubjectArea> filterSubjectAreas) { this.filterSubjectAreas = filterSubjectAreas;}
    public List<IdValue> getSubjectAreas() { return subjectAreas; }
    public void setSubjectAreas(List<IdValue> subjectAreas) { this.subjectAreas = subjectAreas; }
    
    public Collection<IdValue> getCourseNbrs(int idx) {
        Vector ret = new Vector();
        boolean contains = false;
        if (getSubjectArea(idx)>=0) {
            for (Object[] o: new CourseOfferingDAO().
                    getSession().
                    createQuery("select co.uniqueId, co.courseNbr, co.title from CourseOffering co "+
                            "where co.subjectArea.uniqueId = :subjectAreaId "+
                            "and co.instructionalOffering.notOffered = false "+
                            "order by co.courseNbr ", Object[].class).
                    setFetchSize(200).
                    setCacheable(true).
                    setParameter("subjectAreaId", getSubjectArea(idx), org.hibernate.type.LongType.INSTANCE).list()) {
                ret.add(new IdValue((Long)o[0],((String)o[1]) + " - " + (String) o[2]));
                if (o[0].equals(getCourseNbr(idx))) contains = true;
            }
        }
        if (!contains) setCourseNbr(idx, -1L);
        if (ret.size()==1) setCourseNbr(idx, ((IdValue)ret.firstElement()).getId());
        else ret.insertElementAt(new IdValue(-1L,"-"), 0);
        return ret;
    }
    
    public Collection<IdValue> getExams(int idx) { 
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
    
    public Long getExamType() { return iExamType; }
    public void setExamType(Long type) { iExamType = type; }    
}
