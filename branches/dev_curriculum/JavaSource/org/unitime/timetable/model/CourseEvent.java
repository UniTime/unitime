package org.unitime.timetable.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.unitime.timetable.model.base.BaseCourseEvent;



public class CourseEvent extends BaseCourseEvent {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public CourseEvent () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public CourseEvent (java.lang.Long uniqueId) {
		super(uniqueId);
	}

	/**
	 * Constructor for required fields
	 */
	public CourseEvent (
		java.lang.Long uniqueId,
		java.lang.Integer minCapacity,
		java.lang.Integer maxCapacity) {

		super (
			uniqueId,
			minCapacity,
			maxCapacity);
	}

/*[CONSTRUCTOR MARKER END]*/

    public Set<Student> getStudents() {
        HashSet<Student> students = new HashSet();
        if (isReqAttendance()==null || !isReqAttendance().booleanValue()) return students;
        for (Iterator i=getRelatedCourses().iterator();i.hasNext();)
            students.addAll(((RelatedCourseInfo)i.next()).getStudents());
        return students;
  
    }
    
    public Set<DepartmentalInstructor> getInstructors() {
        HashSet<DepartmentalInstructor> instructors = new HashSet();
        if (isReqAttendance()==null || !isReqAttendance().booleanValue()) return instructors;
        for (Iterator i=getRelatedCourses().iterator();i.hasNext();)
            instructors.addAll(((RelatedCourseInfo)i.next()).getInstructors());
        return instructors;
    }

    public int getEventType() { return sEventTypeCourse; }

    public Collection<Long> getStudentIds() {
        HashSet<Long> studentIds = new HashSet();
        for (Iterator i=getRelatedCourses().iterator();i.hasNext();)
            studentIds.addAll(((RelatedCourseInfo)i.next()).getStudentIds());
        return studentIds;
    }

}