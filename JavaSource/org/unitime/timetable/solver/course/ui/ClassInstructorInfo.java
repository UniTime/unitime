package org.unitime.timetable.solver.course.ui;

import java.io.Serializable;

import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.dao.ClassInstructorDAO;

public class ClassInstructorInfo implements Serializable, Comparable<ClassInstructorInfo> {
	private static final long serialVersionUID = 8576391767085203451L;
	protected Long iId;
    protected String iExternalUniqueId = null;
    protected String iName = null;
    protected boolean iIsLead = false;
    protected int iShare = 0;
    protected transient ClassInstructor iInstructor;
    public ClassInstructorInfo(ClassInstructor instructor) {
        iId = instructor.getUniqueId();
        iName = instructor.getInstructor().getNameLastFirst();
        iIsLead = instructor.isLead();
        iShare = instructor.getPercentShare();
        iExternalUniqueId = instructor.getInstructor().getExternalUniqueId();
        iInstructor = instructor;
    }
    public Long getId() { return iId; }
    public String getName() { return iName; }
    public boolean isLead() { return iIsLead; }
    public int getShare() { return iShare; }
    public String getExternalUniqueId() {
        if (iExternalUniqueId==null && iInstructor==null)
            iExternalUniqueId = getInstructor().getInstructor().getExternalUniqueId();
        return iExternalUniqueId; 
    }
    public ClassInstructor getInstructor() {
        if (iInstructor==null)
            iInstructor = ClassInstructorDAO.getInstance().get(getId());
        return iInstructor;
    }
    public ClassInstructor getInstructor(org.hibernate.Session hibSession) {
        return ClassInstructorDAO.getInstance().get(getId(), hibSession);
    }
    public int compareTo(ClassInstructorInfo i) {
        int cmp = getName().compareTo(i.getName());
        if (cmp!=0) return cmp;
        return getId().compareTo(i.getId());
    }
    public int hashCode() {
        if (getExternalUniqueId()!=null) return getExternalUniqueId().hashCode();
        return getId().hashCode();
    }
    public boolean equals(Object o) {
        if (o==null || !(o instanceof ClassInstructorInfo)) return false;
        ClassInstructorInfo i = (ClassInstructorInfo)o;
        if (getExternalUniqueId()!=null && getExternalUniqueId().equals(i.getExternalUniqueId())) return true;
        return getId().equals(i.getId());
    }

}
