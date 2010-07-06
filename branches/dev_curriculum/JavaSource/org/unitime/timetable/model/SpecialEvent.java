package org.unitime.timetable.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.unitime.timetable.model.base.BaseSpecialEvent;



public class SpecialEvent extends BaseSpecialEvent {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public SpecialEvent () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public SpecialEvent (java.lang.Long uniqueId) {
		super(uniqueId);
	}

/*[CONSTRUCTOR MARKER END]*/

    public Set<Student> getStudents() {
        return new HashSet<Student>();
    }
    
    public Set<DepartmentalInstructor> getInstructors() {
        return new HashSet<DepartmentalInstructor>();
    }
    
    public int getEventType() { return sEventTypeSpecial; }

    public Collection<Long> getStudentIds() { return null; }
}