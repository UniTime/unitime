package org.unitime.timetable.onlinesectioning.custom;

import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.model.XStudent;

public interface SpecialRegistrationDashboardUrlProvider {
	public String getDashboardUrl(OnlineSectioningServer server, OnlineSectioningHelper helper, XStudent student) throws SectioningException;
	public String getDashboardUrl(Student student) throws SectioningException;
}
