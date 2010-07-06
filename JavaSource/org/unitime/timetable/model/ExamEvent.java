package org.unitime.timetable.model;

import java.util.Collection;
import java.util.Set;

import org.unitime.timetable.model.base.BaseExamEvent;



public abstract class ExamEvent extends BaseExamEvent {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public ExamEvent () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public ExamEvent (java.lang.Long uniqueId) {
		super(uniqueId);
	}

/*[CONSTRUCTOR MARKER END]*/

    public Set<Student> getStudents() {
        return getExam().getStudents();
  
    }
    
    public Set<DepartmentalInstructor> getInstructors() {
        return getExam().getInstructors();
    }
    
    public Session getSession() { return getExam().getSession(); }
    
    public Collection<Long> getStudentIds() {
        return getExam().getStudentIds();
    }
}