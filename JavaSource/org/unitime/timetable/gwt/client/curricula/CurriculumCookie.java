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
package org.unitime.timetable.gwt.client.curricula;

import com.google.gwt.user.client.Cookies;

/**
 * @author Tomas Muller
 */
public class CurriculumCookie {
	private CourseCurriculaTable.Type iType = CourseCurriculaTable.Type.EXP;
	private CurriculaCourses.Mode iMode = CurriculaCourses.Mode.NONE;
	private boolean iPercent = true;
	private boolean iRulesPercent = true;
	private boolean iRulesShowLastLike = false;
	private boolean iCourseDetails = false;
	private boolean iShowLast = true, iShowProjected = true, iShowExpected = true, iShowEnrolled = true, iShowRequested = false;
	private CurriculaTable.DisplayMode iCurMode = new CurriculaTable.DisplayMode() {
		@Override
		public void changed() {
			save();
		}
	};
	
	private static CurriculumCookie sInstance = null;
	
	private CurriculumCookie() {
		try {
			String cookie = Cookies.getCookie("UniTime:Curriculum");
			if (cookie != null && cookie.length() > 0) {
				String[] values = cookie.split(":");
				iType = CourseCurriculaTable.Type.valueOf(values[0]);
				iMode = CurriculaCourses.Mode.valueOf(values[1]);
				iPercent = "T".equals(values[2]);
				iRulesPercent = "T".equals(values[3]);
				iRulesShowLastLike = "T".equals(values[4]);
				iCourseDetails = "T".equals(values[5]);
				iCurMode.fromString(values[6]);
				iShowLast = !"F".equals(values[7]);
				iShowProjected = !"F".equals(values[8]);
				iShowExpected = !"F".equals(values[9]);
				iShowEnrolled = !"F".equals(values[10]);
				iShowRequested = "T".equals(values[11]);
			}
		} catch (Exception e) {
		}
	}
	
	private void save() {
		String cookie = 
			(iType == null ? "" : iType.name()) + ":" +
			(iMode == null ? "" : iMode.name()) + ":" +
			(iPercent ? "T": "F") + ":" +
			(iRulesPercent ? "T" : "F") + ":" +
			(iRulesShowLastLike ? "T" : "F") + ":" +
			(iCourseDetails ? "T": "F") + ":" +
			iCurMode.toString() + ":" +
			(iShowLast ? "T" : "F") + ":" +
			(iShowProjected ? "T" : "F") + ":" +
			(iShowExpected ? "T" : "F") + ":" +
			(iShowEnrolled ? "T" : "F") + ":" +
			(iShowRequested ? "T" : "F")
			;
		Cookies.setCookie("UniTime:Curriculum", cookie);
	}
	
	public static CurriculumCookie getInstance() {
		if (sInstance == null)
			sInstance = new CurriculumCookie();
		return sInstance;
	}
	
	public CourseCurriculaTable.Type getCourseCurriculaTableType() { 
		return (iType == null ? CourseCurriculaTable.Type.EXP : iType);
	}
	
	public void setCourseCurriculaTableType(CourseCurriculaTable.Type type) {
		iType = type;
		save();
	}
	
	public CurriculaCourses.Mode getCurriculaCoursesMode() {
		return (iMode == null ? CurriculaCourses.Mode.NONE : iMode);
	}
	
	public void setCurriculaCoursesMode(CurriculaCourses.Mode mode) {
		iMode = mode;
		save();
	}
	
	public boolean getCurriculaCoursesPercent() {
		return iPercent;
	}
	
	public void setCurriculaCoursesPercent(boolean percent) {
		iPercent = percent;
		save();
	}
	
	public boolean getCurriculumProjectionRulesPercent() {
		return iRulesPercent;
	}
	
	public void setCurriculumProjectionRulesPercent(boolean percent) {
		iRulesPercent = percent;
		save();
	}
	
	public boolean getCurriculumProjectionRulesShowLastLike() {
		return iRulesShowLastLike;
	}
	
	public void setCurriculumProjectionRulesShowLastLike(boolean showLastLike) {
		iRulesShowLastLike = showLastLike;
		save();
	}
	
	public boolean getCurriculaCoursesDetails() {
		return iCourseDetails;
	}
	
	public void setCurriculaCoursesDetails(boolean details) {
		iCourseDetails = details;
		save();
	}
	
	public CurriculaTable.DisplayMode getCurriculaDisplayMode() {
		return iCurMode;
	}
	
	public boolean isShowLast() { return iShowLast; }
	public void setShowLast(boolean show) { iShowLast = show; save(); }

	public boolean isShowProjected() { return iShowProjected; }
	public void setShowProjected(boolean show) { iShowProjected = show; save(); }

	public boolean isShowEnrolled() { return iShowEnrolled; }
	public void setShowEnrolled(boolean show) { iShowEnrolled = show; save(); }

	public boolean isShowExpected() { return iShowExpected; }
	public void setShowExpected(boolean show) { iShowExpected = show; save(); }

	public boolean isShowRequested() { return iShowRequested; }
	public void setShowRequested(boolean show) { iShowRequested = show; save(); }
	
	public boolean isAllHidden() {
		return !iShowLast && !iShowProjected && !iShowExpected && !iShowEnrolled && !iShowRequested;
	}
}
