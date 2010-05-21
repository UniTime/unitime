/**
 * 
 */
package org.unitime.timetable.interfaces;

import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;

/**
 * @author says
 *
 */
public interface ExternalClassNameHelperInterface {

	public String getClassSuffix(Class_ clazz, CourseOffering courseOffering);
	public String getClassLabel(Class_ clazz, CourseOffering courseOffering);
	public String getClassLabelWithTitle(Class_ clazz, CourseOffering courseOffering);
	public String getExternalId(Class_ clazz, CourseOffering courseOffering);
	
}
