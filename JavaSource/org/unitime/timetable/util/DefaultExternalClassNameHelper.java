/**
 * 
 */
package org.unitime.timetable.util;

import org.unitime.timetable.interfaces.ExternalClassNameHelperInterface;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;

/**
 * @author says
 *
 */
public class DefaultExternalClassNameHelper implements
		ExternalClassNameHelperInterface {

	/**
	 * 
	 */
	public DefaultExternalClassNameHelper() {
		// do nothing
	}

	/* (non-Javadoc)
	 * @see org.unitime.timetable.interfaces.ExternalClassNameHelperInterface#getClassLabel(org.unitime.timetable.model.Class_, org.unitime.timetable.model.CourseOffering)
	 */
	public String getClassLabel(Class_ clazz, CourseOffering courseOffering) {
		return(clazz.getClassLabel());
	}

	/* (non-Javadoc)
	 * @see org.unitime.timetable.interfaces.ExternalClassNameHelperInterface#getClassSuffix(org.unitime.timetable.model.Class_, org.unitime.timetable.model.CourseOffering)
	 */
	public String getClassSuffix(Class_ clazz, CourseOffering courseOffering) {
		return(clazz.getClassSuffix());
	}

	/* (non-Javadoc)
	 * @see org.unitime.timetable.interfaces.ExternalClassNameHelperInterface#getClassLabelWithTitle(org.unitime.timetable.model.Class_, org.unitime.timetable.model.CourseOffering)
	 */
	public String getClassLabelWithTitle(Class_ clazz, CourseOffering courseOffering) {
		return(clazz.getClassLabelWithTitle());
	}

	/* (non-Javadoc)
	 * @see org.unitime.timetable.interfaces.ExternalClassNameHelperInterface#getClassLabelWithTitle(org.unitime.timetable.model.Class_, org.unitime.timetable.model.CourseOffering)
	 */
	public String getExternalId(Class_ clazz, CourseOffering courseOffering) {
		return(clazz.getExternalUniqueId());
	}


}
