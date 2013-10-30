/*
 * UniTime 3.5 (University Timetabling Application)
 * Copyright (C) 2013, UniTime LLC, and individual contributors
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog;

import com.google.protobuf.CodedOutputStream;
import com.google.protobuf.TextFormat;

/**
 * @author Tomas Muller
 */
public class ExportReport implements OnlineSectioningReport.Report {
	protected static Logger sLog = Logger.getLogger(ExportReport.class);
	private CodedOutputStream iOut = null;
	private PrintWriter iPrint = null;
	
	public ExportReport(OutputStream out, PrintWriter print) {
		iOut = CodedOutputStream.newInstance(out);
		iPrint = print;
	}
	
	@Override
	public String getYear() {
		return System.getProperty("year", "2013");
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
		return new File(System.getProperty("user.home", "."));
	}

	@Override
	public String[] getOperations() {
		return System.getProperty("operations", "section,suggestions,reload-student").split(",");
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
	public void process(OnlineSectioningReport report, String student, List<OnlineSectioningLog.Action> actions) {
		try {
			OnlineSectioningLog.ExportedLog log = OnlineSectioningLog.ExportedLog.newBuilder()
					.setStudent(student)
					.addAllAction(actions)
					.build();
			
			if (iOut != null) {
				iOut.writeInt32NoTag(log.getSerializedSize());
				log.writeTo(iOut);
				iOut.flush();
			}
			
			if (iPrint != null) {
				iPrint.println("student: " + student);
				for (OnlineSectioningLog.Action action: actions)
					iPrint.println(TextFormat.shortDebugString(action));
				iPrint.flush();
			}
			
			report.inc("Log size [MB]", log.getSerializedSize() / 1048576.0);
			report.inc("Log count", actions.size());
			Map<String, Integer> op2cnt = new HashMap<String, Integer>();
			Map<String, Long> op2size = new HashMap<String, Long>();
			for (OnlineSectioningLog.Action action: actions) {
				Integer cnt = op2cnt.get(action.getOperation());
				op2cnt.put(action.getOperation(), 1 + (cnt == null ? 0 : cnt));
				Long size = op2size.get(action.getOperation());
				op2size.put(action.getOperation(), action.getSerializedSize() + (size == null ? 0 : size));
			}
			for(Map.Entry<String, Integer> entry: op2cnt.entrySet())
				report.inc("Count " + entry.getKey(), entry.getValue());
			for(Map.Entry<String, Long> entry: op2size.entrySet())
				report.inc("Avg. size " + entry.getKey() + " [kB]", entry.getValue() / (1024.0 * op2cnt.get(entry.getKey())));
		} catch (IOException e) {
			sLog.error("Failed to export student " + student + ": " + e.getMessage(), e);
		}
	}
	
	public static void main(String[] args) {
		try {
			FileOutputStream out = new FileOutputStream("sectioning.dat");
			PrintWriter pw = new PrintWriter("sectioning.log");
			new OnlineSectioningReport(new ExportReport(out, pw)).run();
			out.close();
			pw.flush(); pw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
