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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.ExamOwner;
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.Preference;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.comparators.ClassComparator;
import org.unitime.timetable.model.comparators.InstrOfferingConfigComparator;
import org.unitime.timetable.model.comparators.SchedulingSubpartComparator;
import org.unitime.timetable.model.dao.Class_DAO;
import org.unitime.timetable.model.dao.CourseOfferingDAO;
import org.unitime.timetable.model.dao.DepartmentalInstructorDAO;
import org.unitime.timetable.model.dao.InstrOfferingConfigDAO;
import org.unitime.timetable.model.dao.SchedulingSubpartDAO;
import org.unitime.timetable.security.context.HttpSessionContext;
import org.unitime.timetable.util.DynamicList;
import org.unitime.timetable.util.DynamicListObjectFactory;
import org.unitime.timetable.util.IdValue;

/**
 * @author Tomas Muller
 */
public class ExamEditForm extends PreferencesForm {
	private static final long serialVersionUID = -5083087578026654516L;
	private String examId;
    private String label;
    private String name;
    private String note;
    
    private Integer maxNbrRooms;
    private Integer length;
    private String size;
    private String sizeNote;
    private String seatingType;
    private String printOffset;
    
    private List instructors;
    
    private List subjectArea;
    private List courseNbr;
    private List itype;
    private List classNumber;
    private Collection subjectAreas;
    
    private Long examType;
    
    private String avgPeriod;
    
    private boolean clone;
    private String accommodation;
    
    public String getExamId() { return examId; }
    public void setExamId(String examId) { this.examId = examId; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public Integer getMaxNbrRooms() { return maxNbrRooms; }
    public void setMaxNbrRooms(Integer maxNbrRooms) { this.maxNbrRooms = maxNbrRooms; }
    public Integer getLength() { return length; }
    public void setLength(Integer length) { this.length = length; }
    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }
    public String getSizeNote() { return sizeNote; }
    public void setSizeNote(String sizeNote) { this.sizeNote = sizeNote; }
    public String getPrintOffset() { return printOffset; }
    public void setPrintOffset(String printOffset) { this.printOffset = printOffset; }
    public String getSeatingType() { return seatingType; }
    public void setSeatingType(String seatingType) { this.seatingType = seatingType; }
    public String[] getSeatingTypes() { return Exam.sSeatingTypes; }
    public int getSeatingTypeIdx() {
        for (int i=0;i<Exam.sSeatingTypes.length;i++)
            if (Exam.sSeatingTypes[i].equals(seatingType)) return i;
        return Exam.sSeatingTypeExam;
    }
    public String[] getObjectTypes() { return ExamOwner.sOwnerTypes; }

    protected DynamicListObjectFactory factory = new DynamicListObjectFactory() {
        public Object create() {
            return new String(Preference.BLANK_PREF_VALUE);
        }
    };

    protected DynamicListObjectFactory idfactory = new DynamicListObjectFactory() {
        public Object create() {
            return new Long(-1);
        }
    };

    public void reset(ActionMapping mapping, HttpServletRequest request) {
        examId = null;
        name = null;
        note = null;
        maxNbrRooms = 1;
        length = null;
        size = null;
        sizeNote = null;
        printOffset = null;
        avgPeriod = null;
        seatingType = Exam.sSeatingTypes[Exam.sSeatingTypeExam];
        instructors = DynamicList.getInstance(new ArrayList(), factory);
        subjectArea = DynamicList.getInstance(new ArrayList(), idfactory);
        courseNbr = DynamicList.getInstance(new ArrayList(), idfactory);
        itype = DynamicList.getInstance(new ArrayList(), idfactory);
        classNumber = DynamicList.getInstance(new ArrayList(), idfactory);
        examType = null;
        if (request.getSession().getAttribute("Exam.Type")!=null)
        	examType = (Long)request.getSession().getAttribute("Exam.Type");
        if (examType == null) {
        	TreeSet<ExamType> types = ExamType.findAllUsed(HttpSessionContext.getSessionContext(request.getSession().getServletContext()).getUser().getCurrentAcademicSessionId());
        	if (!types.isEmpty()) examType = types.first().getUniqueId();
        }
        clone = false;
        accommodation = null;
        super.reset(mapping, request);
    }

    public List getInstructors() { return instructors; }
    public String getInstructors(int key) { return instructors.get(key).toString(); }
    public void setInstructors(int key, Object value) { this.instructors.set(key, value); }
    public void setInstructors(List instructors) { this.instructors = instructors; }
    
    public List getSubjectAreaList() { return subjectArea; }
    public List getSubjectArea() { return subjectArea; }
    public Long getSubjectArea(int key) { return (Long)subjectArea.get(key); }
    public void setSubjectArea(int key, Long value) { this.subjectArea.set(key, value); }
    public void setSubjectArea(List subjectArea) { this.subjectArea = subjectArea; }
    public List getCourseNbr() { return courseNbr; }
    public Long getCourseNbr(int key) { return (Long)courseNbr.get(key); }
    public void setCourseNbr(int key, Long value) { this.courseNbr.set(key, value); }
    public void setCourseNbr(List courseNbr) { this.courseNbr = courseNbr; }
    public List getItype() { return itype; }
    public Long getItype(int key) { return (Long)itype.get(key); }
    public void setItype(int key, Long value) { this.itype.set(key, value); }
    public void setItype(List itype) { this.itype = itype; }
    public List getClassNumber() { return classNumber; }
    public Long getClassNumber(int key) { return (Long)classNumber.get(key); }
    public void setClassNumber(int key, Long value) { this.classNumber.set(key, value); }
    public void setClassNumber(List classNumber) { this.classNumber = classNumber; }
    
    public void deleteExamOwner(int idx) {
        getSubjectArea().remove(idx);
        getCourseNbr().remove(idx);
        getItype().remove(idx);
        getClassNumber().remove(idx);
    }
    
    public void addExamOwner(ExamOwner owner) {
        if (owner==null) {
            getSubjectArea().add(new Long(-1));
            getCourseNbr().add(new Long(-1));
            getItype().add(new Long(-1));
            getClassNumber().add(new Long(-1));
        } else {
            switch (owner.getOwnerType()) {
            case ExamOwner.sOwnerTypeClass :
                Class_ clazz = (Class_)owner.getOwnerObject();
                getSubjectArea().add(clazz.getSchedulingSubpart().getControllingCourseOffering().getSubjectArea().getUniqueId());
                getCourseNbr().add(clazz.getSchedulingSubpart().getControllingCourseOffering().getUniqueId());
                getItype().add(clazz.getSchedulingSubpart().getUniqueId());
                getClassNumber().add(clazz.getUniqueId());
                break;
            case ExamOwner.sOwnerTypeConfig :
                InstrOfferingConfig config = (InstrOfferingConfig)owner.getOwnerObject();
                getSubjectArea().add(config.getControllingCourseOffering().getSubjectArea().getUniqueId());
                getCourseNbr().add(config.getControllingCourseOffering().getUniqueId());
                getItype().add(-config.getUniqueId());
                getClassNumber().add(new Long(-1));
                break;
            case ExamOwner.sOwnerTypeCourse :
                CourseOffering course = (CourseOffering)owner.getOwnerObject();
                getSubjectArea().add(course.getSubjectArea().getUniqueId());
                getCourseNbr().add(course.getUniqueId());
                getItype().add(Long.MIN_VALUE);
                getClassNumber().add(new Long(-1));
                break;
            case ExamOwner.sOwnerTypeOffering :
                InstructionalOffering offering = (InstructionalOffering)owner.getOwnerObject();
                getSubjectArea().add(offering.getControllingCourseOffering().getSubjectArea().getUniqueId());
                getCourseNbr().add(offering.getControllingCourseOffering().getUniqueId());
                getItype().add(Long.MIN_VALUE+1);
                getClassNumber().add(new Long(-1));
                break;
            }
        }
    }
    
    public Collection getSubjectAreas() { return subjectAreas; }
    public void setSubjectAreas(Collection subjectAreas) { this.subjectAreas = subjectAreas; }
    
    public Collection getCourseNbrs(int idx) { 
        Vector ret = new Vector();
        boolean contains = false;
        if (getSubjectArea(idx)>=0) {
            for (Iterator i= new CourseOfferingDAO().
                    getSession().
                    createQuery("select co.uniqueId, co.courseNbr, co.title from CourseOffering co "+
                            "where co.subjectArea.uniqueId = :subjectAreaId "+
                            "and co.instructionalOffering.notOffered = false "+
                            "order by co.courseNbr ").
                    setFetchSize(200).
                    setCacheable(true).
                    setLong("subjectAreaId", getSubjectArea(idx)).iterate();i.hasNext();) {
                Object[] o = (Object[])i.next();
                ret.add(new IdValue((Long)o[0],((String)o[1] + " - " + (String)o[2])));
                if (o[0].equals(getCourseNbr(idx))) contains = true;
            }
        }
        if (!contains) setCourseNbr(idx, -1L);
        if (ret.size()==1) setCourseNbr(idx, ((IdValue)ret.firstElement()).getId());
        else ret.insertElementAt(new IdValue(-1L,"-"), 0);
        return ret;
    }
    
    public Collection getItypes(int idx) { 
        Vector ret = new Vector();
        boolean contains = false;
        if (getCourseNbr(idx)>=0) {
            CourseOffering course = new CourseOfferingDAO().get(getCourseNbr(idx));
            if (course.isIsControl())
                ret.add(new IdValue(Long.MIN_VALUE+1,"Offering"));
            ret.add(new IdValue(Long.MIN_VALUE,"Course"));
            if (!course.isIsControl()) {
                setItype(idx, Long.MIN_VALUE);
                return ret;
            }
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
            if (!configs.isEmpty()) {
                ret.add(new IdValue(Long.MIN_VALUE+2,"-- Configurations --"));
                for (Iterator i=configs.iterator();i.hasNext();) {
                    InstrOfferingConfig c = (InstrOfferingConfig)i.next();
                    if (c.getUniqueId().equals(getItype(idx))) contains = true;
                    ret.add(new IdValue(-c.getUniqueId(), c.getName()));
                }
            }
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
            if (!configs.isEmpty() && !subparts.isEmpty())
                ret.add(new IdValue(Long.MIN_VALUE+2,"-- Subparts --"));
            for (Iterator i=subparts.iterator();i.hasNext();) {
                SchedulingSubpart s = (SchedulingSubpart)i.next();
                Long sid = s.getUniqueId();
                String name = s.getItype().getAbbv();
                String sufix = s.getSchedulingSubpartSuffix();
                while (s.getParentSubpart()!=null) {
                    name = "&nbsp;&nbsp;&nbsp;&nbsp;"+name;
                    s = s.getParentSubpart();
                }
                if (s.getInstrOfferingConfig().getInstructionalOffering().getInstrOfferingConfigs().size()>1)
                    name += " ["+s.getInstrOfferingConfig().getName()+"]";
                if (sid.equals(getItype(idx))) contains = true;
                ret.add(new IdValue(sid, name+(sufix==null || sufix.length()==0?"":" ("+sufix+")")));
            }
        } else {
            ret.addElement(new IdValue(0L,"N/A"));
        }
        if (!contains) setItype(idx, ((IdValue)ret.firstElement()).getId());
        return ret;
    }
    
    public ExamOwner getExamOwner(int idx) {
        if (getSubjectArea(idx)<0 || getCourseNbr(idx)<0) return null;
        CourseOffering course = new CourseOfferingDAO().get(getCourseNbr(idx));
        if (course==null) return null;
        if (getItype(idx)==Long.MIN_VALUE) { //course
            ExamOwner owner = new ExamOwner();
            owner.setOwner(course);
            return owner;
        } else if (getItype(idx)==Long.MIN_VALUE+1 || getItype(idx)==Long.MIN_VALUE+2) { //offering
            ExamOwner owner = new ExamOwner();
            owner.setOwner(course.getInstructionalOffering());
            return owner;
        } else if (getItype(idx)<0) { //config
            InstrOfferingConfig config = new InstrOfferingConfigDAO().get(-getItype(idx));
            if (config==null) return null;
            ExamOwner owner = new ExamOwner();
            owner.setOwner(config);
            return owner;
        } else if (getClassNumber(idx)>=0) { //class
            Class_ clazz = new Class_DAO().get(getClassNumber(idx));
            if (clazz==null) return null;
            ExamOwner owner = new ExamOwner();
            owner.setOwner(clazz);
            return owner;
        }
        return null;
    }
    
    public void setExamOwners(Exam exam) {
        if (exam.getOwners()==null) exam.setOwners(new HashSet());
        exam.getOwners().clear();
        for (int idx=0;idx<getSubjectAreaList().size();idx++) {
            ExamOwner owner = getExamOwner(idx);
            if (owner!=null) {
                exam.getOwners().add(owner);
                owner.setExam(exam);
            }
        }
    }
    
    public Collection getClassNumbers(int idx) {
        Vector ret = new Vector();
        boolean contains = false;
        SchedulingSubpart subpart = (getItype(idx)>0?new SchedulingSubpartDAO().get(getItype(idx)):null);
        CourseOffering co = (getItype(idx)>0?new CourseOfferingDAO().get(getCourseNbr(idx)):null);
        if (subpart!=null) {
            TreeSet classes = new TreeSet(new ClassComparator(ClassComparator.COMPARE_BY_HIERARCHY));
            classes.addAll(new Class_DAO().
                getSession().
                createQuery("select distinct c from Class_ c "+
                        "where c.schedulingSubpart.uniqueId=:schedulingSubpartId").
                setFetchSize(200).
                setCacheable(true).
                setLong("schedulingSubpartId", getItype(idx)).
                list());
            for (Iterator i=classes.iterator();i.hasNext();) {
                Class_ c = (Class_)i.next();
                if (c.getUniqueId().equals(getClassNumber(idx))) contains = true;
                String extId = c.getClassSuffix(co);
                ret.add(new IdValue(c.getUniqueId(), c.getSectionNumberString() +
                		 (extId == null || extId.isEmpty() || extId.equalsIgnoreCase(c.getSectionNumberString()) ? "" : " - " + extId))); 
            }
        }
        if (ret.isEmpty()) ret.add(new IdValue(-1L,"N/A"));
        if (!contains) setClassNumber(idx, -1L);
        if (ret.size()==1) setClassNumber(idx, ((IdValue)ret.firstElement()).getId());
        else ret.insertElementAt(new IdValue(-1L,"-"), 0);
        return ret;
    }
    
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        
        ActionErrors errors = new ActionErrors();
        
        if(maxNbrRooms!=null && maxNbrRooms.intValue()<0)
            errors.add("maxNbrRooms", new ActionMessage("errors.integerGtEq", "Maximal Number of Rooms", "0") );
        
        if (length==null || length.intValue()<0)
            errors.add("length", new ActionMessage("errors.integerGtEq", "Length", "0") );
        
        if (size!=null && size.length()>0) {
            try {
                Integer.parseInt(size);
            } catch (NumberFormatException e) {
                errors.add("size", new ActionMessage("errors.numeric", "Size") );
            }
        }
        
        if (printOffset!=null && printOffset.length()>0) {
            try {
                Integer.parseInt(printOffset);
            } catch (NumberFormatException e) {
                errors.add("printOffset", new ActionMessage("errors.numeric", "Print Offset") );
            }
        }

        // Notes has 1000 character limit
        if(note!=null && note.length()>999)
            errors.add("note", new ActionMessage("errors.maxlength", "Note", "999") );
        
        // At least one instructor is selected
        if (instructors.size()>0) {
            
            // Check no duplicates or blank instructors
            super.checkPrefs(instructors);
        }        
        
        boolean hasOwner = false;
        for (int idx=0;idx<getSubjectAreaList().size();idx++) {
            ExamOwner owner = getExamOwner(idx);
            if (owner!=null) { hasOwner = true; break; }
        }
        if (!hasOwner) {
            errors.add("owners", new ActionMessage("errors.generic", "At least one class/course has to be specified.") );
        }
        
        // Check Other Preferences
        errors.add(super.validate(mapping, request));
        
        return errors;
    }
    
    public void addBlankPrefRows() {
        super.addBlankPrefRows();
    }
    
    public Long getExamType() { return examType; }
    public void setExamType(Long examType) { this.examType = examType; }
    
    public String getAvgPeriod() { return avgPeriod; }
    public void setAvgPeriod(String avgPeriod) { this.avgPeriod = avgPeriod; }
    
    public String getEmail(String instructorId) {
        DepartmentalInstructor instructor = new DepartmentalInstructorDAO().get(Long.valueOf(instructorId));
        return (instructor.getEmail()==null?"":instructor.getEmail());
    }
    
    public boolean getClone() { return clone; }
    public void setClone(boolean clone) { this.clone = clone; }
    
    public String getAccommodation() { return accommodation; }
    public void setAccommodation(String accommodation) { this.accommodation = accommodation; }
}
