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

import java.util.BitSet;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.cpsolver.coursett.model.Lecture;
import org.cpsolver.coursett.model.TimeLocation;
import org.cpsolver.ifs.util.DataProperties;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.DateUtils;


public class AverageHoursAWeekClassWeights implements ClassWeightProvider {
	private BitSet[] iDaysOfWeek = null;
	private double iCoeficient = 1.0;
	
	public AverageHoursAWeekClassWeights(DataProperties config) {
		org.hibernate.Session hibSession = SessionDAO.getInstance().createNewSession();
		try {
			Session session = SessionDAO.getInstance().get(config.getPropertyLong("General.SessionId", -1l));
			iDaysOfWeek = new BitSet[Constants.DAY_CODES.length];
			for (int i = 0; i < iDaysOfWeek.length; i++)
				iDaysOfWeek[i] = new BitSet();
			Calendar cal = Calendar.getInstance(Locale.US); cal.setLenient(true);
			cal.setTime(DateUtils.getDate(1, session.getPatternStartMonth(), session.getSessionStartYear()));
			Date last = DateUtils.getDate(1, session.getPatternEndMonth(), session.getSessionStartYear());
			int idx = 0;
			while (cal.getTime().before(last)) {
        		int dow = cal.get(Calendar.DAY_OF_WEEK);
        		switch (dow) {
        		case Calendar.MONDAY:
        			iDaysOfWeek[Constants.DAY_MON].set(idx);
        			break;
        		case Calendar.TUESDAY:
        			iDaysOfWeek[Constants.DAY_TUE].set(idx);
        			break;
        		case Calendar.WEDNESDAY:
        			iDaysOfWeek[Constants.DAY_WED].set(idx);
        			break;
        		case Calendar.THURSDAY:
        			iDaysOfWeek[Constants.DAY_THU].set(idx);
        			break;
        		case Calendar.FRIDAY:
        			iDaysOfWeek[Constants.DAY_FRI].set(idx);
        			break;
        		case Calendar.SATURDAY:
        			iDaysOfWeek[Constants.DAY_SAT].set(idx);
        			break;
        		case Calendar.SUNDAY:
        			iDaysOfWeek[Constants.DAY_SUN].set(idx);
        			break;
        		}
        		cal.add(Calendar.DAY_OF_YEAR, 1); idx++;
			}
			if (session.getDefaultDatePattern() != null) {
				BitSet ddp = session.getDefaultDatePattern().getPatternBitSet();
				iCoeficient = 5.0 / (
						intersection(ddp, iDaysOfWeek[Constants.DAY_MON]) +
						intersection(ddp, iDaysOfWeek[Constants.DAY_TUE]) +
						intersection(ddp, iDaysOfWeek[Constants.DAY_WED]) +
						intersection(ddp, iDaysOfWeek[Constants.DAY_THU]) +
						intersection(ddp, iDaysOfWeek[Constants.DAY_FRI]));
						
			}
		} finally {
			hibSession.close();
		}
	}
	
	public int intersection(BitSet a, BitSet b) {
		BitSet c = (BitSet)a.clone();
		c.and(b);
		return c.cardinality();
	}

	@Override
	public double getWeight(Lecture lecture) {
		double nrMeetingSlots = 0;
		int nrTimes = 0;
		for (TimeLocation time : lecture.timeLocations()) {
			for (int d = 0; d < Constants.DAY_CODES.length; d++)
				if ((time.getDayCode() & Constants.DAY_CODES[d]) != 0) {
					nrMeetingSlots += intersection(time.getWeekCode(), iDaysOfWeek[d]) * time.getNrSlotsPerMeeting();
				}
			nrTimes ++;
		}
		if (nrTimes == 0) return 1.0;
		return Math.round(100.0 * iCoeficient * nrMeetingSlots / (12.0 * nrTimes)) / 100.0;
	}

}