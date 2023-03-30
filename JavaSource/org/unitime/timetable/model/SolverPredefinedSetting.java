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
package org.unitime.timetable.model;

import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.model.SolverParameterGroup.SolverType;
import org.unitime.timetable.model.base.BaseSolverPredefinedSetting;
import org.unitime.timetable.model.dao.SolverPredefinedSettingDAO;



/**
 * @author Tomas Muller
 */
public class SolverPredefinedSetting extends BaseSolverPredefinedSetting {
	private static final long serialVersionUID = 1L;
	protected static final CourseMessages MSG = Localization.create(CourseMessages.class);
	
	public static enum Appearance {
		TIMETABLES,
		SOLVER,
		EXAM_SOLVER,
		STUDENT_SOLVER,
		INSTRUCTOR_SOLVER,
		;
		
		public String getLabel() {
			switch (this) {
			case TIMETABLES:
				return MSG.solverConfigAppearanceTimetables();
			case SOLVER:
				return MSG.solverConfigAppearanceSolver();
			case EXAM_SOLVER:
				return MSG.solverConfigAppearanceExamSolver();
			case STUDENT_SOLVER:
				return MSG.solverConfigAppearanceStudentSolver();
			case INSTRUCTOR_SOLVER:
				return MSG.solverConfigAppearanceInstructorSolver();
			default:
				return name();
			}
		}
		
		public SolverType getSolverType() {
			switch (this) {
			case TIMETABLES:
			case SOLVER:
				return SolverType.COURSE;
			case EXAM_SOLVER:
				return SolverType.EXAM;
			case STUDENT_SOLVER:
				return SolverType.STUDENT;
			case INSTRUCTOR_SOLVER:
				return SolverType.INSTRUCTOR;
			default:
				return SolverType.COURSE;
			}
		}
	}

	/*
	@Deprecated
	public static final int APPEARANCE_TIMETABLES = Appearance.TIMETABLES.ordinal();
	@Deprecated
	public static final int APPEARANCE_SOLVER = Appearance.SOLVER.ordinal();
	@Deprecated
	public static final int APPEARANCE_EXAM_SOLVER = Appearance.EXAM_SOLVER.ordinal();
	@Deprecated
	public static final int APPEARANCE_STUDENT_SOLVER = Appearance.STUDENT_SOLVER.ordinal();
	@Deprecated
	public static final int APPEARANCE_INSTRUCTOR_SOLVER = Appearance.INSTRUCTOR_SOLVER.ordinal();
*/

/*[CONSTRUCTOR MARKER BEGIN]*/
	public SolverPredefinedSetting () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public SolverPredefinedSetting (java.lang.Long uniqueId) {
		super(uniqueId);
	}

/*[CONSTRUCTOR MARKER END]*/


	/**
	 * Get the default value for a given key
	 * @param key Setting key
	 * @return Default value if found, null otherwise
	 */
	public static SolverPredefinedSetting findByName(String name) {
		return SolverPredefinedSettingDAO.getInstance().getSession()
				.createQuery("from SolverPredefinedSetting where name = :name", SolverPredefinedSetting.class)
				.setParameter("name", name)
				.setCacheable(true)
				.setMaxResults(1)
                .uniqueResult();
	}
	
	public Appearance getAppearanceType() {
		if (getAppearance() == null) return null;
		return Appearance.values()[getAppearance()];
	}
	
	public void setAppearanceType(Appearance appearance) {
		if (appearance == null)
			setAppearance(null);
		else
			setAppearance(appearance.ordinal());
	}
}
