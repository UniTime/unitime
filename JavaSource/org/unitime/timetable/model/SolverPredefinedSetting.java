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

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
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
		List list = (new SolverPredefinedSettingDAO()).getSession().
			createCriteria(SolverPredefinedSetting.class).add(Restrictions.eq("name", name)).setCacheable(true).list();
		
		if (list.isEmpty()) return null;

		return (SolverPredefinedSetting)list.get(0);
	}
	
	public static String[] getNames(Integer appearance) {
		List list = (new SolverPredefinedSettingDAO()).getSession().
			createCriteria(SolverPredefinedSetting.class).add(Restrictions.eq("appearance", appearance)).addOrder(Order.asc("name")).setCacheable(true).list();

		if (list.isEmpty())
			return new String[] {};
		
		String[] names = new String[list.size()];
		for (int i=0;i<list.size();i++) {
			SolverPredefinedSetting set = (SolverPredefinedSetting)list.get(i);
			names[i]=set.getName();
		}
		return names;
	}	

	public static Vector getIdValueList(Integer appearance) {
		List list = (new SolverPredefinedSettingDAO()).getSession().
			createCriteria(SolverPredefinedSetting.class).add(Restrictions.eq("appearance", appearance)).addOrder(Order.asc("name")).setCacheable(true).list();
		
		Vector idValueList = new Vector();

		for (Iterator i=list.iterator();i.hasNext();) {
			SolverPredefinedSetting set = (SolverPredefinedSetting)i.next();
			idValueList.add(new IdValue(set.getUniqueId(),set.getDescription()));
		}
		
		return idValueList;
	}	
	
	public static class IdValue {
		private Long iId;
		private String iValue;
		private String iType;
		private boolean iEnabled;
		public IdValue(Long id, String value) {
			this(id,value,null,true);
		}
		public IdValue(Long id, String value, String type) {
			this(id,value,type,true);
		}
		public IdValue(Long id, String value, String type, boolean enabled) {
			iId = id; iValue = value; iType = type; iEnabled = enabled;
		}
		public Long getId() { return iId; }
		public String getValue() { return iValue; }
		public String getType() { return iType;}
		public boolean getEnabled() { return iEnabled; }
		public boolean getDisabled() { return !iEnabled; }
	}	
}
