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
import java.util.List;
import java.util.TreeSet;
import java.util.Vector;

import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.ExaminationMessages;
import org.unitime.timetable.action.UniTimeAction;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.ExamOwner;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.comparators.ClassComparator;
import org.unitime.timetable.model.comparators.InstrOfferingConfigComparator;
import org.unitime.timetable.model.comparators.SchedulingSubpartComparator;
import org.unitime.timetable.model.dao.Class_DAO;
import org.unitime.timetable.model.dao.CourseOfferingDAO;
import org.unitime.timetable.model.dao.DepartmentalInstructorDAO;
import org.unitime.timetable.model.dao.InstrOfferingConfigDAO;
import org.unitime.timetable.model.dao.SchedulingSubpartDAO;
import org.unitime.timetable.util.IdValue;

/**
 * @author Tomas Muller
 */
public class ExamEditForm extends PreferencesForm {
	private static final long serialVersionUID = -5083087578026654516L;
	protected static ExaminationMessages EXMSG = Localization.create(ExaminationMessages.class);
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
    
    private List<String> instructors;
    
    private List<Long> subjectArea;
    private List<Long> courseNbr;
    private List<Long> itype;
    private List<Long> classNumber;
    private Collection<SubjectArea> subjectAreas;
    
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
    public String[] getSeatingTypes() { return new String[] {MSG.examSeatingTypeNormal(), MSG.examSeatingTypeExam()}; }
    public int getSeatingTypeIdx() {
    	if (MSG.examSeatingTypeNormal().equals(seatingType)) return Exam.sSeatingTypeNormal;
        return Exam.sSeatingTypeExam;
    }
    public boolean isExamSeating() {
    	return getSeatingTypeIdx() == 1;
    }
    public String[] getObjectTypes() { return ExamOwner.sOwnerTypes; }
    
    public ExamEditForm() {
    	super();
    	reset();
    }

    public List<String> getInstructors() { return instructors; }
    public String getInstructors(int idx) { return instructors.get(idx); }
    public void setInstructors(int idx, String value) { instructors.set(idx, value); }
    public void setInstructors(List<String> instructors) { this.instructors = instructors; }
    
    public List<Long> getSubjectArea() { return subjectArea; }
    public List<Long> getSubjectAreaList() { return getSubjectArea(); }
    public Long getSubjectArea(int idx) { return subjectArea.get(idx); }
    public void setSubjectArea(int idx, Long id) { subjectArea.set(idx, id); }
    public void setSubjectArea(List<Long> subjectArea) { this.subjectArea = subjectArea; }
    public List<Long> getCourseNbr() { return courseNbr; }
    public Long getCourseNbr(int idx) { return courseNbr.get(idx); }
    public void setCourseNbr(int idx, Long id) { courseNbr.set(idx, id); }
    public void setCourseNbr(List<Long> courseNbr) { this.courseNbr = courseNbr; }
    public List<Long> getItype() { return itype; }
    public Long getItype(int idx) { return itype.get(idx); }
    public void setItype(int idx, Long id) { itype.set(idx, id); }
    public void setItype(List<Long> itype) { this.itype = itype; }
    public List<Long> getClassNumber() { return classNumber; }
    public Long getClassNumber(int idx) { return classNumber.get(idx); }
    public void setClassNumber(int idx, Long id) { classNumber.set(idx, id); }
    public void setClassNumber(List<Long> classNumber) { this.classNumber = classNumber; }
    
    public void deleteExamOwner(int idx) {
        getSubjectArea().remove(idx);
        getCourseNbr().remove(idx);
        getItype().remove(idx);
        getClassNumber().remove(idx);
    }
    
    public void addExamOwner(ExamOwner owner) {
        if (owner==null) {
            getSubjectArea().add(Long.valueOf(-1));
            getCourseNbr().add(Long.valueOf(-1));
            getItype().add(Long.valueOf(-1));
            getClassNumber().add(Long.valueOf(-1));
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
                getClassNumber().add(Long.valueOf(-1));
                break;
            case ExamOwner.sOwnerTypeCourse :
                CourseOffering course = (CourseOffering)owner.getOwnerObject();
                getSubjectArea().add(course.getSubjectArea().getUniqueId());
                getCourseNbr().add(course.getUniqueId());
                getItype().add(Long.MIN_VALUE);
                getClassNumber().add(Long.valueOf(-1));
                break;
            case ExamOwner.sOwnerTypeOffering :
                InstructionalOffering offering = (InstructionalOffering)owner.getOwnerObject();
                getSubjectArea().add(offering.getControllingCourseOffering().getSubjectArea().getUniqueId());
                getCourseNbr().add(offering.getControllingCourseOffering().getUniqueId());
                getItype().add(Long.MIN_VALUE+1);
                getClassNumber().add(Long.valueOf(-1));
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
            for (Object[] o: CourseOfferingDAO.getInstance().
                    getSession().
                    createQuery("select co.uniqueId, co.courseNbr, co.title from CourseOffering co "+
                            "where co.subjectArea.uniqueId = :subjectAreaId "+
                            "and co.instructionalOffering.notOffered = false "+
                            "order by co.courseNbr ", Object[].class).
                    setFetchSize(200).
                    setCacheable(true).
                    setParameter("subjectAreaId", getSubjectArea(idx), org.hibernate.type.LongType.INSTANCE).list()) {
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
            CourseOffering course = CourseOfferingDAO.getInstance().get(getCourseNbr(idx));
            if (course.isIsControl())
                ret.add(new IdValue(Long.MIN_VALUE+1,"Offering"));
            ret.add(new IdValue(Long.MIN_VALUE,"Course"));
            if (!course.isIsControl()) {
                setItype(idx, Long.MIN_VALUE);
                return ret;
            }
            TreeSet<InstrOfferingConfig> configs = new TreeSet<InstrOfferingConfig>(new InstrOfferingConfigComparator(null));
            configs.addAll(InstrOfferingConfigDAO.getInstance().
                getSession().
                createQuery("select distinct c from " +
                        "InstrOfferingConfig c inner join c.instructionalOffering.courseOfferings co "+
                        "where co.uniqueId = :courseOfferingId", InstrOfferingConfig.class).
                setFetchSize(200).
                setCacheable(true).
                setParameter("courseOfferingId", course.getUniqueId(), org.hibernate.type.LongType.INSTANCE).
                list());
            if (!configs.isEmpty()) {
                ret.add(new IdValue(Long.MIN_VALUE+2,"-- Configurations --"));
                for (InstrOfferingConfig c: configs) {
                    if (c.getUniqueId().equals(getItype(idx))) contains = true;
                    ret.add(new IdValue(-c.getUniqueId(), c.getName() + (c.getInstructionalMethod() == null ? "" : " (" + c.getInstructionalMethod().getLabel() + ")")));
                }
            }
            TreeSet<SchedulingSubpart> subparts = new TreeSet<SchedulingSubpart>(new SchedulingSubpartComparator(null));
            subparts.addAll(SchedulingSubpartDAO.getInstance().
                getSession().
                createQuery("select distinct s from " +
                        "SchedulingSubpart s inner join s.instrOfferingConfig.instructionalOffering.courseOfferings co "+
                        "where co.uniqueId = :courseOfferingId", SchedulingSubpart.class).
                setFetchSize(200).
                setCacheable(true).
                setParameter("courseOfferingId", course.getUniqueId(), org.hibernate.type.LongType.INSTANCE).
                list());
            if (!configs.isEmpty() && !subparts.isEmpty())
                ret.add(new IdValue(Long.MIN_VALUE+2,"-- Subparts --"));
            for (SchedulingSubpart s: subparts) {
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
        CourseOffering course = CourseOfferingDAO.getInstance().get(getCourseNbr(idx));
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
            InstrOfferingConfig config = InstrOfferingConfigDAO.getInstance().get(-getItype(idx));
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
        SchedulingSubpart subpart = (getItype(idx)>0?SchedulingSubpartDAO.getInstance().get(getItype(idx)):null);
        CourseOffering co = (getItype(idx)>0?CourseOfferingDAO.getInstance().get(getCourseNbr(idx)):null);
        if (subpart!=null) {
            TreeSet<Class_> classes = new TreeSet<Class_>(new ClassComparator(ClassComparator.COMPARE_BY_HIERARCHY));
            classes.addAll(new Class_DAO().
                getSession().
                createQuery("select distinct c from Class_ c "+
                        "where c.schedulingSubpart.uniqueId=:schedulingSubpartId", Class_.class).
                setFetchSize(200).
                setCacheable(true).
                setParameter("schedulingSubpartId", getItype(idx), org.hibernate.type.LongType.INSTANCE).
                list());
            for (Class_ c: classes) {
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
    
    
    public void addBlankPrefRows() {
        super.addBlankPrefRows();
    }
    
    public Long getExamType() { return examType; }
    public void setExamType(Long examType) { this.examType = examType; }
    
    public String getAvgPeriod() { return avgPeriod; }
    public void setAvgPeriod(String avgPeriod) { this.avgPeriod = avgPeriod; }
    
    public String getEmail(String instructorId) {
        DepartmentalInstructor instructor = DepartmentalInstructorDAO.getInstance().get(Long.valueOf(instructorId));
        return (instructor.getEmail()==null?"":instructor.getEmail());
    }
    
    public boolean getClone() { return clone; }
    public void setClone(boolean clone) { this.clone = clone; }
    
    public String getAccommodation() { return accommodation; }
    public void setAccommodation(String accommodation) { this.accommodation = accommodation; }
    
    @Override
	public void reset() {
    	examId = null;
        name = null;
        note = null;
        maxNbrRooms = 1;
        length = null;
        size = null;
        sizeNote = null;
        printOffset = null;
        avgPeriod = null;
        seatingType = MSG.examSeatingTypeExam();
        instructors = new ArrayList();
        subjectArea = new ArrayList();
        courseNbr = new ArrayList();
        itype = new ArrayList();
        classNumber = new ArrayList();
        examType = null;
        clone = false;
        accommodation = null;
        super.reset();
    }
	
	@Override
	public void validate(UniTimeAction action) {
		super.validate(action);
        
        if (maxNbrRooms!=null && maxNbrRooms.intValue()<0)
        	action.addFieldError("form.maxNbrRooms", EXMSG.errorNegativeMaxNbrRooms());
        
        if (length==null || length.intValue() < 0)
        	action.addFieldError("form.length", EXMSG.errorZeroExamLength());
        
        if (size!=null && size.length()>0) {
            try {
                Integer.parseInt(size);
            } catch (NumberFormatException e) {
            	action.addFieldError("form.size", EXMSG.errorExamSizeNotNumber());
            }
        }
        
        if (printOffset!=null && printOffset.length()>0) {
            try {
                Integer.parseInt(printOffset);
            } catch (NumberFormatException e) {
            	action.addFieldError("form.printOffset", EXMSG.errorExamPrintOffsetNotNumber());
            }
        }

        // Notes has 1000 character limit
        if (note!=null && note.length()>999)
        	action.addFieldError("note", "Note is too long.");
        
        // At least one instructor is selected
        if (instructors != null && instructors.size()>0) {
            // Check no duplicates or blank instructors
            if (!checkPrefs(instructors))
            	action.addFieldError("form.instructors", EXMSG.errorDuplicateExamInstructors());
        }
        
        boolean hasOwner = false;
        if (getSubjectAreaList() != null)
        	for (int idx=0;idx<getSubjectAreaList().size();idx++) {
                ExamOwner owner = getExamOwner(idx);
                if (owner!=null) { hasOwner = true; break; }
            }
        if (!hasOwner) {
        	action.addFieldError("form.owners", EXMSG.errorNoExamOwners());
        }
    }
}
