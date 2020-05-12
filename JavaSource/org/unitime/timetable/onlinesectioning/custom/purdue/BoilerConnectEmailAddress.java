package org.unitime.timetable.onlinesectioning.custom.purdue;

import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.custom.StudentEmailProvider;

public class BoilerConnectEmailAddress implements StudentEmailProvider {

	@Override
	public String getEmailAddress(OnlineSectioningServer server, OnlineSectioningHelper helper, Student student, Boolean optional) {
		if (optional == null || !optional.booleanValue()) return student.getEmail();
		String suffix = ApplicationProperties.getProperty("purdue.boilerconnect.oldSuffix", "@purdue.edu");
		if (student.getEmail() != null && student.getEmail().endsWith(suffix)) {
			return student.getEmail().replace(suffix, ApplicationProperties.getProperty("purdue.boilerconnect.newSuffix", "@boilerconnect.purdue.edu"));
		} else {
			return student.getEmail();
		}
	}

	@Override
	public String isOptional(Long sessionId) {
		return ApplicationProperties.getProperty("purdue.boilerconnect.toggle", "Send via BoilerConnect (use <i>@boilerconnect.purdue.edu</i> email address)");
	}

}
