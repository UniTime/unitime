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
package org.unitime.timetable.server;

import java.util.Set;


import org.cpsolver.coursett.model.TimeLocation;
import org.unitime.timetable.gwt.client.TimeHint.TimeHintRequest;
import org.unitime.timetable.gwt.client.TimeHint.TimeHintResponse;
import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.ExamPeriod;
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.MidtermPeriodPreferenceModel;
import org.unitime.timetable.model.PeriodPreferenceModel;
import org.unitime.timetable.model.TimePattern;
import org.unitime.timetable.model.TimePatternDays;
import org.unitime.timetable.model.TimePatternTime;
import org.unitime.timetable.model.TimePref;
import org.unitime.timetable.model.dao.Class_DAO;
import org.unitime.timetable.model.dao.ExamDAO;
import org.unitime.timetable.model.dao.ExamPeriodDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.webutil.RequiredTimeTable;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(TimeHintRequest.class)
public class TimeHintBackend implements GwtRpcImplementation<TimeHintRequest, TimeHintResponse>{

	@Override
	public TimeHintResponse execute(TimeHintRequest request, SessionContext context) {
		String[] params = request.getParameter().split(",");
		if (params.length == 2) {
			Long examId = Long.valueOf(params[0]);
			Long periodId = Long.valueOf(params[1]);
			Exam exam = ExamDAO.getInstance().get(examId);
			ExamPeriod period = ExamPeriodDAO.getInstance().get(periodId);
			if (exam.getExamType().getType() == ExamType.sExamTypeMidterm) {
				MidtermPeriodPreferenceModel mpp = new MidtermPeriodPreferenceModel(exam.getSession(), exam.getExamType(), period);
				mpp.load(exam);
				return new TimeHintResponse("<div style='max-width: 200px;'>" + mpp.toString(true) + "</div>");
			} else {
				PeriodPreferenceModel px = new PeriodPreferenceModel(exam.getSession(), period, exam.getExamType().getUniqueId());
				px.load(exam);
				RequiredTimeTable m = new RequiredTimeTable(px);
				return new TimeHintResponse("$wnd." + m.print(false, false).replace(");\n</script>", "").replace("<script language=\"javascript\">\ndocument.write(", "").replace("\n", " "));
			}
		} else if (params.length == 3) {
			Long classId = Long.valueOf(params[0]);
			int days = Integer.parseInt(params[1]);
			int slot = Integer.parseInt(params[2]);
			
			Class_ clazz = Class_DAO.getInstance().get(classId);
			for (TimePref p: (Set<TimePref>)clazz.effectivePreferences(TimePref.class)) {
				if (p.getTimePattern().getType() == TimePattern.sTypeExactTime) continue;
				boolean match = false;
				for (TimePatternDays d: p.getTimePattern().getDays()) {
					if (d.getDayCode() == days) { match = true; break; }
				}
				if (!match) continue;
				match = false;
				for (TimePatternTime t: p.getTimePattern().getTimes()) {
					if (t.getStartSlot() == slot) { match = true; break; }
				}
				if (!match) continue;
				RequiredTimeTable m = p.getRequiredTimeTable(new TimeLocation(days, slot, 0, 0, 0.0, null, null, null, 0));
				return new TimeHintResponse("$wnd." + m.print(false, false).replace(");\n</script>", "").replace("<script language=\"javascript\">\ndocument.write(", "").replace("\n", " "));
			}
		}
		throw new GwtRpcException("No matching time preference found.");
	}

}
