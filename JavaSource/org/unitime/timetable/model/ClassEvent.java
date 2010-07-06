package org.unitime.timetable.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.unitime.timetable.model.base.BaseClassEvent;
import org.unitime.timetable.model.dao.RelatedCourseInfoDAO;



public class ClassEvent extends BaseClassEvent {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public ClassEvent () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public ClassEvent (java.lang.Long uniqueId) {
		super(uniqueId);
	}

/*[CONSTRUCTOR MARKER END]*/

    public Set<Student> getStudents() {
        HashSet<Student> students = new HashSet();
        for (Iterator i=getClazz().getStudentEnrollments().iterator();i.hasNext();)
            students.add(((StudentClassEnrollment)i.next()).getStudent());
        return students;
    }
    
    public Collection<Long> getStudentIds() {
        return new RelatedCourseInfoDAO().getSession().createQuery(
                "select distinct e.student.uniqueId from StudentClassEnrollment e where e.clazz.uniqueId = :classId")
                .setLong("classId", getClazz().getUniqueId())
                .setCacheable(true)
                .list();
    }

    
    public Set<DepartmentalInstructor> getInstructors() {
        HashSet<DepartmentalInstructor> instructors = new HashSet();
        for (Iterator i=getClazz().getClassInstructors().iterator();i.hasNext();) {
            ClassInstructor ci = (ClassInstructor)i.next();
            if (ci.isLead()) instructors.add(ci.getInstructor());
        }
        return instructors;
    }
    
    public int getEventType() { return sEventTypeClass; }
    
    public Session getSession() { return getClazz().getSession(); }

}