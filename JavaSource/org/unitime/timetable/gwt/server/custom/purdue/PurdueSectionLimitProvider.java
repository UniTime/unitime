/*
 * UniTime 4.0 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package org.unitime.timetable.gwt.server.custom.purdue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.unitime.timetable.gwt.server.AcademicSessionInfo;
import org.unitime.timetable.gwt.server.custom.CustomSectionNames;
import org.unitime.timetable.gwt.server.custom.SectionLimitProvider;
import org.unitime.timetable.gwt.server.custom.SectionUrlProvider;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.gwt.shared.SectioningExceptionType;

public class PurdueSectionLimitProvider implements SectionLimitProvider, SectionUrlProvider {
    private static Logger sLog = Logger.getLogger(PurdueSectionLimitProvider.class);

	public static String sUrl = "https://esa-oas-prod-wl.itap.purdue.edu/prod/bzwsrch.p_schedule_detail?term=:year:term&crn=:crn";
	public static String sDummyUrl = "https://esa-oas-prod-wl.itap.purdue.edu/prod/bzwsrch.p_schedule_detail?term=201010&crn=10001";
	public static String sContentRE = "(<table [ ]*class=\"[a-z]*\" summary=\"This layout table is used to present the seating numbers.\" .*</table>)";
	public static String sTableRE = "<td class=\"dddefault\">(\\-?[0-9]*)</td>";
	private Pattern iContentRE = Pattern.compile(sContentRE, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.UNIX_LINES);
	private Pattern iTableRE = Pattern.compile(sTableRE, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.UNIX_LINES);
	public static int sConcurrencyLimit = 10;
	
	private Hashtable<Long, int[]> iCache = new Hashtable<Long, int[]>();

	private String getTerm(AcademicSessionInfo session) throws SectioningException {
		if (session.getTerm().toLowerCase().startsWith("spr")) return "20";
		if (session.getTerm().toLowerCase().startsWith("sum")) return "30";
		if (session.getTerm().toLowerCase().startsWith("fal")) return "10";
		throw new SectioningException(SectioningExceptionType.CUSTOM_SECTION_LIMITS_FAILURE, "academic term "+session.getTerm()+" not known");
	}
	
	private String getYear(AcademicSessionInfo session) throws SectioningException {
		if (session.getTerm().toLowerCase().startsWith("fal"))
			return String.valueOf(Integer.parseInt(session.getYear()) + 1);
		return session.getYear();
	}
	
	public URL getSectionUrl(AcademicSessionInfo session, Long courseId, Long classId, String customClassSuffix) {
		try {
			if (customClassSuffix == null || customClassSuffix.isEmpty()) throw new SectioningException(SectioningExceptionType.CUSTOM_SECTION_LIMITS_FAILURE, "class CRN not provided");
			String crn = customClassSuffix;
			if (customClassSuffix.indexOf('-') >= 0)
				crn = customClassSuffix.substring(0, customClassSuffix.indexOf('-'));
			URL url = new URL(sUrl
				.replace(":year", getYear(session))
				.replace(":term", getTerm(session))
				.replace(":initiative", session.getCampus())
				.replace(":crn", crn));
			return url;
		} catch (MalformedURLException e) {
			throw new SectioningException(SectioningExceptionType.CUSTOM_SECTION_LIMITS_FAILURE, "course detail url is wrong");
		}
	}
	
	public int[] getSectionLimit(AcademicSessionInfo session, Long courseId, Long classId, String customClassSuffix) throws SectioningException {
		int[] ret = getSectionLimit(getSectionUrl(session, courseId, classId, customClassSuffix));
		iCache.put(classId, ret);
		return ret;
	}
	
	public int[] getSectionLimit(URL secionUrl) throws SectioningException {
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(secionUrl.openStream()));
			StringBuffer content = new StringBuffer();
			String line;
			while ((line = in.readLine()) != null)
				content.append(line);
			in.close();
			
			Matcher match = iContentRE.matcher(content);
			if (!match.find()) throw new SectioningException(SectioningExceptionType.CUSTOM_SECTION_LIMITS_FAILURE, "unable to parse <a href='"+secionUrl+"'>class detial page</a>");
			String table = match.group(1);

			match = iTableRE.matcher(table);
			if (!match.find()) throw new SectioningException(SectioningExceptionType.CUSTOM_SECTION_LIMITS_FAILURE, "unable to parse <a href='"+secionUrl+"'>class detial page</a>");
			int capacity = Integer.parseInt(match.group(1));
			if (!match.find()) throw new SectioningException(SectioningExceptionType.CUSTOM_SECTION_LIMITS_FAILURE, "unable to parse <a href='"+secionUrl+"'>class detial page</a>");
			int actual = Integer.parseInt(match.group(1));
			if (!match.find()) throw new SectioningException(SectioningExceptionType.CUSTOM_SECTION_LIMITS_FAILURE, "unable to parse <a href='"+secionUrl+"'>class detial page</a>");
//			int remaning = Integer.parseInt(match.group(1));
			
			return new int[] {actual, capacity};
		} catch (IOException e) {
			throw new SectioningException(SectioningExceptionType.CUSTOM_SECTION_LIMITS_FAILURE, "unable to read <a href='"+secionUrl+"'>class detial page</a>");
		}
	}
	
	public Hashtable<Long, int[]> getSectionLimits(AcademicSessionInfo session, Long courseId, ArrayList<Long> classIds, CustomSectionNames names) {
		Hashtable<Long, int[]> ret = new Hashtable<Long, int[]>();
		ThreadPool pool = new ThreadPool();
		for (Long classId: classIds) {
			String customClassSuffix = names.getClassSuffix(session.getUniqueId(), courseId, classId);
			pool.retrieveLimit(session, courseId, classId, customClassSuffix, ret);
		}
		pool.waitForAll();
		return ret;
	}

	public Hashtable<Long, int[]> getSectionLimitsFromCache(AcademicSessionInfo session, Long courseId, ArrayList<Long> classIds, CustomSectionNames names) {
		Hashtable<Long, int[]> ret = new Hashtable<Long, int[]>();
		ThreadPool pool = new ThreadPool();
		for (Long classId: classIds) {
			int[] limits = iCache.get(classId);
			if (limits != null) {
				ret.put(classId, limits);
			} else {
				String customClassSuffix = names.getClassSuffix(session.getUniqueId(), courseId, classId);
				pool.retrieveLimit(session, courseId, classId, customClassSuffix, ret);
			}
		}
		pool.waitForAll();
		return ret;
	}

	public static void main(String[] args) {
		try {
			// int[] l = new PurdueSectionLimitProvider().getSectionLimit(new URL(sDummyUrl));
			AcademicSessionInfo session = new AcademicSessionInfo(-1l, "2009", "Fall", "PWL");
			ArrayList<Long> classIds = new ArrayList<Long>();
			for (int i = 0; i < 100; i++)
				classIds.add(new Long(10001 + i));
			CustomSectionNames names = new CustomSectionNames() {
				public void update(AcademicSessionInfo session) {
				}
				public String getClassSuffix(Long sessionId, Long courseId, Long classId) {
					return classId.toString() + "-dummy";
				}
			};
			long t0 = System.currentTimeMillis();
			Hashtable<Long, int[]> limits = new PurdueSectionLimitProvider().getSectionLimits(session, -1l, classIds, names);
			long t1 = System.currentTimeMillis();
			System.out.println("limits:");
			for (Long classId : new TreeSet<Long>(limits.keySet())) {
				int[] limit = limits.get(classId);
				System.out.println("  " + classId + " .. " + limit[0] + "/" + limit[1]);
			}
			System.out.println("  lookup took " + (t1 - t0) + "ms");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

	class ThreadPool {
		Set<Worker> iWorkers = new HashSet<Worker>();
		
		void retrieveLimit(AcademicSessionInfo session, Long courseId, Long classId, String customClassSuffix, Hashtable<Long, int[]> ret) {
			synchronized (iWorkers) {
				while (iWorkers.size() > sConcurrencyLimit) {
					try {
						iWorkers.wait();
					} catch (InterruptedException e) {}
				}
				Worker w = new Worker(session, courseId, classId, customClassSuffix, ret);
				iWorkers.add(w);
				w.start();
			}
		}
		
		void done(Worker w) {
			synchronized (iWorkers) {
				iWorkers.remove(w);
				iWorkers.notify();
			}
		}
		
		public void waitForAll() {
			synchronized (iWorkers) {
				while (!iWorkers.isEmpty()) {
					try {
						iWorkers.wait();
					} catch (InterruptedException e) {}
				}
			}
		}
	
		class Worker extends Thread {
			private AcademicSessionInfo iSession;
			private Long iCourseId, iClassId;
			private String iCustomClassSuffix;
			private Hashtable<Long, int[]> iResults;
			
			public Worker(AcademicSessionInfo session, Long courseId, Long classId, String customClassSuffix, Hashtable<Long, int[]> ret) {
				iSession = session;
				iCourseId = courseId;
				iClassId = classId;
				iCustomClassSuffix = customClassSuffix;
				iResults = ret;
				setName("PuSectLimitP-" + classId);
			}
			
			public void run() {
				try {
					int[] limit = getSectionLimit(iSession, iCourseId, iClassId, iCustomClassSuffix);
					iResults.put(iClassId, limit);
				} catch (SectioningException e) {
					sLog.warn("Failed to retrieve section limit for "+iCustomClassSuffix+" ("+iSession.getTerm()+" "+iSession.getYear()+"): "+e.getMessage());
				} finally {
					done(this);
				}
			}
		}
	}

}
