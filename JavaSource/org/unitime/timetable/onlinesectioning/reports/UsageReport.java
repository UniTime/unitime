/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

/**
 * @author Tomas Muller
 */
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
	public String[] getExcludeUsers() {
		return System.getProperty("exclude", "TEST").split(",");
	}
	
	@Override
	public String getLastTimeStamp() {
		return System.getProperty("before", null);
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