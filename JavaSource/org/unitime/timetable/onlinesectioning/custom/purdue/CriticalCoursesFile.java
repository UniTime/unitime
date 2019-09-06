package org.unitime.timetable.onlinesectioning.custom.purdue;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.cpsolver.ifs.util.CSVFile;
import org.cpsolver.ifs.util.CSVFile.CSVField;
import org.cpsolver.ifs.util.CSVFile.CSVLine;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog.Action.Builder;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.custom.CriticalCoursesProvider;
import org.unitime.timetable.onlinesectioning.model.XAreaClassificationMajor;
import org.unitime.timetable.onlinesectioning.model.XStudent;
import org.unitime.timetable.onlinesectioning.model.XStudentId;

public class CriticalCoursesFile implements CriticalCoursesProvider {
	private static Logger sLog = Logger.getLogger(CriticalCoursesFile.class);
	
	private static Map<String, CriticalCoursesImpl> iCriticalCourses = null;
	
	public CriticalCoursesFile() throws ServletException, IOException {
		String courses = ApplicationProperties.getProperty("purdue.critical.file", "critical-courses.csv");
		CSVFile file = null;
        if (new File(courses).exists()) {
            sLog.info("Reading menu from " + courses + " ...");
            file = new CSVFile(new File(courses));
        } else {
        	throw new ServletException("Unable to create critical course table, reason: resource " + courses + " not found.");
        }
        iCriticalCourses = new HashMap<String, CriticalCoursesImpl>();
        for (CSVLine line: file.getLines()) {
        	CSVField area = line.getField("Area");
        	CSVField major = line.getField("Major");
        	CSVField subject = line.getField("Subject");
        	CSVField course = line.getField("Course");
        	if (area != null && major != null && subject != null && course != null) {
        		CriticalCoursesImpl critical = iCriticalCourses.get(area + "/" + major);
        		if (critical == null) {
        			critical = new CriticalCoursesImpl();
        			iCriticalCourses.put(area + "/" + major, critical);
        		}
        		critical.add(subject.toString(), course.toString());
        	}
        }
	}
	
	@Override
	public CriticalCourses getCriticalCourses(OnlineSectioningServer server, OnlineSectioningHelper helper, XStudentId student) {
		return getCriticalCourses(server, helper, student, helper.getAction());
	}

	@Override
	public CriticalCourses getCriticalCourses(OnlineSectioningServer server, OnlineSectioningHelper helper, XStudentId studentId, Builder action) {
		if (iCriticalCourses == null || iCriticalCourses.isEmpty()) return null;
		XStudent student = (studentId instanceof XStudent ? (XStudent)studentId : server.getStudent(studentId.getStudentId()));
		if (student == null) return null;
		for (XAreaClassificationMajor acm: student.getMajors()) {
			CriticalCourses cc = iCriticalCourses.get(acm.getArea() + "/" + acm.getMajor());
			if (cc != null) {
				if (action != null)
					action.addOptionBuilder().setKey("critical").setValue(cc.toString());
				return cc;
			}
		}
		return null;
	}

	@Override
	public void dispose() {
	}
	
	protected static class CriticalCoursesImpl implements CriticalCourses {
		private Set<String> iCriticalCourses = new TreeSet<String>();
		
		public boolean add(String subject, String course) { return iCriticalCourses.add(subject + " " + course); }
		
		@Override
		public boolean isEmpty() { return iCriticalCourses.isEmpty(); }

		@Override
		public boolean isCritical(CourseOffering course) {
			for (String c: iCriticalCourses)
				if (course.getCourseName().startsWith(c)) return true;
			return false;
		}
		
		@Override
		public String toString() {
			Set<String> courses = new TreeSet<String>(iCriticalCourses);
			return courses.toString();
		}
	}
}
