package org.unitime.timetable.onlinesectioning.custom;

import org.unitime.timetable.model.Student;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;

public interface StudentEmailProvider {
	
	/**
	 * Return student email address
	 * */
	public String getEmailAddress(OnlineSectioningServer server, OnlineSectioningHelper helper, Student student, Boolean optional);
	
	/**
	 * If optional, return display message that needs to be toggled. Return null otherwise.
	 */
	public String isOptional(Long sessionId);

}
