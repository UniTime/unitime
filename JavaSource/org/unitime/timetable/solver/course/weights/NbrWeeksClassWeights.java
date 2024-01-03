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
package org.unitime.timetable.solver.course.weights;

import org.cpsolver.coursett.model.Lecture;
import org.cpsolver.ifs.util.DataProperties;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.SessionDAO;

public class NbrWeeksClassWeights implements ClassWeightProvider {
	private double iDefaultNumberOfWeeks = 1.0;
	
	public NbrWeeksClassWeights(DataProperties config) {
		org.hibernate.Session hibSession = SessionDAO.getInstance().createNewSession();
		try {
			Session session = SessionDAO.getInstance().get(config.getPropertyLong("General.SessionId", -1l));
			DatePattern dp = session.getDefaultDatePattern();
			if (dp != null)
				iDefaultNumberOfWeeks = dp.getEffectiveNumberOfWeeks();
		} finally {
			hibSession.close();
		}
	}

	@Override
	public double getWeight(Lecture lecture, Class_ clazz) {
		DatePattern dp = clazz.effectiveDatePattern();
		return (dp == null ? 1.0 : dp.getEffectiveNumberOfWeeks() / iDefaultNumberOfWeeks);
	}

}
