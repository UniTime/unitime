/*
 * UniTime 3.4 (University Timetabling Application)
 * Copyright (C) 2012, UniTime LLC, and individual contributors
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
package org.unitime.timetable.server;

import java.util.Set;

import net.sf.cpsolver.coursett.model.TimeLocation;

import org.unitime.timetable.gwt.client.TimeHint.TimeHintRequest;
import org.unitime.timetable.gwt.client.TimeHint.TimeHintResponse;
import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.ExamPeriod;
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
        	PeriodPreferenceModel px = new PeriodPreferenceModel(exam.getSession(), period, exam.getExamType().getUniqueId());
            px.load(exam);
            RequiredTimeTable m = new RequiredTimeTable(px);
            return new TimeHintResponse("$wnd." + m.print(false, false).replace(");\n</script>", "").replace("<script language=\"javascript\">\ndocument.write(", "").replace("\n", " "));
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
