/*
 * UniTime 3.4 (University Timetabling Application)
 * Copyright (C) 2012, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
*/
package org.unitime.timetable.onlinesectioning.reports;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.unitime.timetable.onlinesectioning.OnlineSectioningLog.Action;

public class UsageReport implements OnlineSectioningReport.Report {
	private DateFormat dfHour = new SimpleDateFormat("yy-MM-dd HH");
	private DateFormat dfDay = new SimpleDateFormat("yy-MM-dd");

	@Override
	public String getYear() {
		return System.getProperty("year", "2012");
	}

	@Override
	public String getTerm() {
		return System.getProperty("term", "Fall");
	}

	@Override
	public String getCampus() {
		return System.getProperty("campus", "PWL");
	}

	@Override
	public File getReportFolder() {
		return new File(System.getProperty("user.home", "~"));
	}

	@Override
	public String[] getOperations() {
		return new String[] { "section", "suggestions", "reload-student" };
	}

	@Override
	public void process(OnlineSectioningReport report, String student, List<Action> actions) {
		Set<String> hours = new HashSet<String>();
		Set<String> days = new HashSet<String>();
		for (Action action: actions) {
			Date d = new Date(action.getStartTime());
			report.inc(action.getOperation() + " [" + dfHour.format(d) + "]", 1);
			report.inc("HourDistribution", dfHour.format(d), action.getOperation(), 1);
			report.inc("DayDistribution", dfDay.format(d), action.getOperation(), 1);
			if (!"reload-student".equals(action.getOperation())) {
				hours.add(dfHour.format(d));
				days.add(dfDay.format(d));
			}
		}
		for (String s: days) {
			report.inc("Student [" + s + "]", 1);
			report.inc("DayDistribution", s, "Students", 1);
		}
		for (String s: hours) {
			report.inc("Student [" + s + "]", 1);
			report.inc("HourDistribution", s, "Students", 1);
		}
		
		long done = Math.round(report.inc("Students", 1.0));
		if ((done % 100) == 0) {
			OnlineSectioningReport.sLog.info("---- after " + done + " students");
			for (String name: new TreeSet<String>(report.iCounters.keySet())) {
				OnlineSectioningReport.sLog.info(name + ": " + report.iCounters.get(name));
			}			
		}
	}
	
	public static void main(String[] args) {
		try {
			new OnlineSectioningReport(new UsageReport()).run();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}