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
package org.unitime.timetable.server.rooms;

import java.util.Iterator;
import java.util.Set;

import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.RoomInterface.ExamTypeInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.PeriodInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.PeriodPreferenceModel;
import org.unitime.timetable.gwt.shared.RoomInterface.PeriodPreferenceRequest;
import org.unitime.timetable.gwt.shared.RoomInterface.PreferenceInterface;
import org.unitime.timetable.model.ExamLocationPref;
import org.unitime.timetable.model.ExamPeriod;
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.dao.ExamTypeDAO;
import org.unitime.timetable.model.dao.LocationDAO;
import org.unitime.timetable.security.SessionContext;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(PeriodPreferenceRequest.class)
public class PeriodPreferencesBackend implements GwtRpcImplementation<PeriodPreferenceRequest, PeriodPreferenceModel> {
	protected static final GwtConstants CONSTANTS = Localization.create(GwtConstants.class);
	protected static final GwtMessages MESSAGES = Localization.create(GwtMessages.class);

	@Override
	public PeriodPreferenceModel execute(PeriodPreferenceRequest request, SessionContext context) {
		switch (request.getOperation()) {
		case LOAD:
			return loadPeriodPreferences(request, context);
		case SAVE:
			return savePeriodPreferences(request, context);
		default:
			return null;
		}
	}
	
	public PeriodPreferenceModel loadPeriodPreferences(PeriodPreferenceRequest request, SessionContext context) {
		PeriodPreferenceModel model = new PeriodPreferenceModel();
		Location location = LocationDAO.getInstance().get(request.getLocationId());
		ExamType type = ExamTypeDAO.getInstance().get(request.getExamTypeId());
		model.setLocationId(location.getUniqueId());
		model.setExamType(new ExamTypeInterface(type.getUniqueId(), type.getReference(), type.getLabel(), type.getType() == ExamType.sExamTypeFinal));
		model.setFirstDate(location.getSession().getExamBeginDate());
		for (ExamPeriod period: (Set<ExamPeriod>)ExamPeriod.findAll(location.getSession().getUniqueId(), type)) {
			model.addPeriod(new PeriodInterface(period.getUniqueId(), period.getDateOffset(), period.getStartSlot(), period.getLength()));
		}
		for (PreferenceLevel pref: PreferenceLevel.getPreferenceLevelList(model.getPeriods().size() < model.getDays().size() * model.getSlots().size())) {
			PreferenceInterface p = new PreferenceInterface(pref.getUniqueId(), PreferenceLevel.prolog2bgColor(pref.getPrefProlog()), pref.getPrefProlog(), pref.getPrefName(), true);
			if (PreferenceLevel.sNeutral.equals(pref.getPrefProlog()))
				model.setDefaultPreference(p);
			model.addPreference(p);
			
		}
		for (Iterator i=location.getExamPreferences().iterator();i.hasNext();) {
            ExamLocationPref pref = (ExamLocationPref)i.next();
            if (!type.equals(pref.getExamPeriod().getExamType())) continue;
            model.setPreference(pref.getExamPeriod().getDateOffset(), pref.getExamPeriod().getStartSlot(), pref.getPrefLevel().getUniqueId());
        }
		return model;
	}
	
	public PeriodPreferenceModel savePeriodPreferences(PeriodPreferenceRequest request, SessionContext context) {
		return null;
	}

}
