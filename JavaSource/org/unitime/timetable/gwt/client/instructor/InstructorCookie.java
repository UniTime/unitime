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
package org.unitime.timetable.gwt.client.instructor;

import java.util.Date;

import com.google.gwt.user.client.Cookies;

/**
 * @author Tomas Muller
 */
public class InstructorCookie {
	private static InstructorCookie sInstance = null;
	private int iSortAttributesBy = 0;
	private int iSortInstructorsBy = 0;
	private int[] iSortTeachingRequestsBy = new int[] {0, 0, 0};
	private int[] iTeachingRequestsColumns = new int[] {0xffff, 0xffff, 0xffff};
	private int iSortTeachingAssignmentsBy = 0;
	private int iTeachingAssignmentsColumns = 0xffff;
	private int iAssignmentChangesBase = 1;
	private int iSortAssignmentChangesBy = 0;
	private int iAssignmentChangesColumns = 0xffff;
	private boolean iShowTeachingRequests = false;
	private boolean iShowTeachingAssignments = false;
	private String[] iQuery = new String[] {"", "", ""};

	private InstructorCookie() {
		try {
			String cookie = Cookies.getCookie("UniTime:Instructor");
			if (cookie != null) {
				String[] params = cookie.split("\\|");
				int idx = 0;
				iSortAttributesBy = Integer.valueOf(params[idx++]);
				iSortInstructorsBy = Integer.valueOf(params[idx++]);
				iSortTeachingRequestsBy = new int[] {Integer.valueOf(params[idx++]), Integer.valueOf(params[idx++]), Integer.valueOf(params[idx++])};
				iTeachingRequestsColumns = new int[] {Integer.valueOf(params[idx++]), Integer.valueOf(params[idx++]), Integer.valueOf(params[idx++])};
				iSortTeachingAssignmentsBy = Integer.valueOf(params[idx++]);
				iTeachingAssignmentsColumns = Integer.valueOf(params[idx++]);
				iAssignmentChangesBase = Integer.valueOf(params[idx++]);
				iSortAssignmentChangesBy = Integer.valueOf(params[idx++]);
				iAssignmentChangesColumns = Integer.valueOf(params[idx++]);
				iShowTeachingRequests = "T".equals(params[idx++]);
				iShowTeachingAssignments = "T".equals(params[idx++]);
				iQuery = new String[] {params[idx++], params[idx++], params[idx++]};
			}
		} catch (Exception e) {
		}
	}
	
	public static InstructorCookie getInstance() { 
		if (sInstance == null)
			sInstance = new InstructorCookie();
		return sInstance;
	}

	private void save() {
		String cookie = iSortAttributesBy + "|" + iSortInstructorsBy +
				"|" + iSortTeachingRequestsBy[0] + "|" + iSortTeachingRequestsBy[1] + "|" + iSortTeachingRequestsBy[2] +
				"|" + iTeachingRequestsColumns[0] + "|" + iTeachingRequestsColumns[1] + "|" + iTeachingRequestsColumns[2] +
				"|" + iSortTeachingAssignmentsBy + "|" + iTeachingAssignmentsColumns +
				"|" + iAssignmentChangesBase + "|" + iSortAssignmentChangesBy + "|" + iAssignmentChangesColumns +
				"|" + (iShowTeachingRequests ? "T" : "F") +
				"|" + (iShowTeachingAssignments ? "T" : "F") +
				"|" + (iQuery[0] == null ? "" : iQuery[0]) + "|" + (iQuery[1] == null ? "" : iQuery[1]) + "|" + (iQuery[2] == null ? "" : iQuery[2]);
		Date expires = new Date(new Date().getTime() + 604800000l); // expires in 7 days
		Cookies.setCookie("UniTime:Instructor", cookie, expires);
	}
	
	public int getSortAttributesBy() {
		return iSortAttributesBy;
	}
	
	public void setSortAttributesBy(int sortAttributesBy) {
		iSortAttributesBy = sortAttributesBy;
		save();
	}

	public int getSortInstructorsBy() {
		return iSortInstructorsBy;
	}
	
	public void setSortInstructorsBy(int sortInstructorsBy) {
		iSortInstructorsBy = sortInstructorsBy;
		save();
	}
	
	public int getSortTeachingRequestsBy(Boolean assigned) {
		return iSortTeachingRequestsBy[assigned == null ? 2 : assigned ? 0 : 1];
	}
	
	public void setSortTeachingRequestsBy(Boolean assigned, int sortTeachingRequestsBy) {
		iSortTeachingRequestsBy[assigned == null ? 2 : assigned ? 0 : 1] = sortTeachingRequestsBy;
		save();
	}
	
	public int getTeachingRequestsColumns(Boolean assigned) {
		return iTeachingRequestsColumns[assigned == null ? 2 : assigned ? 0 : 1];
	}
	
	public boolean isTeachingRequestsColumnVisible(Boolean assigned, int ordinal) {
		return (iTeachingRequestsColumns[assigned == null ? 2 : assigned ? 0 : 1] & (1 << ordinal)) != 0;
	}
	
	public void setTeachingRequestsColumnVisible(Boolean assigned, int ordinal, boolean visible) {
		boolean old = (iTeachingRequestsColumns[assigned == null ? 2 : assigned ? 0 : 1] & (1 << ordinal)) != 0;
		if (old != visible) {
			if (visible)
				iTeachingRequestsColumns[assigned == null ? 2 : assigned ? 0 : 1] += (1 << ordinal);
			else
				iTeachingRequestsColumns[assigned == null ? 2 : assigned ? 0 : 1] -= (1 << ordinal);
		}
		save();
	}
	
	public int getSortTeachingAssignmentsBy() {
		return iSortTeachingAssignmentsBy;
	}
	
	public void setSortTeachingAssignmentsBy(int sortTeachingAssignmentsBy) {
		iSortTeachingAssignmentsBy = sortTeachingAssignmentsBy;
		save();
	}
	
	public int getTeachingAssignmentsColumns() {
		return iTeachingAssignmentsColumns;
	}
	
	public boolean isTeachingAssignmentsColumnVisible(int ordinal) {
		return (iTeachingAssignmentsColumns & (1 << ordinal)) != 0;
	}
	
	public void setTeachingAssignmentsColumnVisible(int ordinal, boolean visible) {
		boolean old = (iTeachingAssignmentsColumns & (1 << ordinal)) != 0;
		if (old != visible) {
			if (visible)
				iTeachingAssignmentsColumns += (1 << ordinal);
			else
				iTeachingAssignmentsColumns -= (1 << ordinal);
		}
		save();
	}
	
	public int getAssignmentChangesBase() {
		return iAssignmentChangesBase;
	}
	
	public void setAssignmentChangesBase(int assignmentChangesBase) {
		iAssignmentChangesBase = assignmentChangesBase;
		save();
	}
	
	public int getSortAssignmentChangesBy() {
		return iSortAssignmentChangesBy;
	}
	
	public void setSortAssignmentChangesBy(int sortAssignmentChangesBy) {
		iSortAssignmentChangesBy = sortAssignmentChangesBy;
		save();
	}
	
	public boolean isAssignmentChangesColumnVisible(int ordinal) {
		return (iAssignmentChangesColumns & (1 << ordinal)) != 0;
	}
	
	public void setAssignmentChangesColumnVisible(int ordinal, boolean visible) {
		boolean old = (iAssignmentChangesColumns & (1 << ordinal)) != 0;
		if (old != visible) {
			if (visible)
				iAssignmentChangesColumns += (1 << ordinal);
			else
				iAssignmentChangesColumns -= (1 << ordinal);
		}
		save();
	}
	
	public boolean isShowTeachingRequests() {
		return iShowTeachingRequests;
	}
	
	public void setShowTeachingRequests(boolean showTeachingRequests) {
		iShowTeachingRequests = showTeachingRequests;
		save();
	}
	
	public boolean isShowTeachingAssignments() { return iShowTeachingAssignments; }
	public void setShowTeachingAssignments(boolean showTeachingAssignments) { iShowTeachingAssignments = showTeachingAssignments; save(); }
	
	public String getQuery(Boolean assigned) { return iQuery[assigned == null ? 2 : assigned ? 0 : 1]; }
	public void setQuery(Boolean assigned, String query) { iQuery[assigned == null ? 2 : assigned ? 0 : 1] = query; save(); }
}
