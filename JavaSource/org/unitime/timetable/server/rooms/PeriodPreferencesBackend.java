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

import org.springframework.beans.factory.annotation.Autowired;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.defaults.CommonValues;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.events.EventAction.EventContext;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.RoomInterface.ExamTypeInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.PeriodInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.PeriodPreferenceModel;
import org.unitime.timetable.gwt.shared.RoomInterface.PeriodPreferenceRequest;
import org.unitime.timetable.gwt.shared.RoomInterface.PreferenceInterface;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.ExamLocationPref;
import org.unitime.timetable.model.ExamPeriod;
import org.unitime.timetable.model.ExamPeriodPref;
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.ExamDAO;
import org.unitime.timetable.model.dao.ExamTypeDAO;
import org.unitime.timetable.model.dao.LocationDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.exam.ExamSolverProxy;
import org.unitime.timetable.solver.exam.ui.ExamAssignment;
import org.unitime.timetable.solver.service.SolverService;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(PeriodPreferenceRequest.class)
public class PeriodPreferencesBackend implements GwtRpcImplementation<PeriodPreferenceRequest, PeriodPreferenceModel> {
	protected static final GwtConstants CONSTANTS = Localization.create(GwtConstants.class);
	protected static final GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	
	@Autowired SolverService<ExamSolverProxy> examinationSolverService;

	@Override
	public PeriodPreferenceModel execute(PeriodPreferenceRequest request, SessionContext context) {
		if (request.hasSessionId())
			context = new EventContext(context, request.getSessionId());

		switch (request.getOperation()) {
		case LOAD:
			return loadPeriodPreferences(request, context);
		case LOAD_FOR_EXAM:
			return loadExamPeriodPreferences(request, context);
		case SAVE:
			return savePeriodPreferences(request, context);
		default:
			return null;
		}
	}
	
	public PeriodPreferenceModel loadPeriodPreferences(PeriodPreferenceRequest request, SessionContext context) {
		Location location = (request.getLocationId() == null ? null : LocationDAO.getInstance().get(request.getLocationId()));
		ExamType type = ExamTypeDAO.getInstance().get(request.getExamTypeId());
		context.checkPermission(location, Right.RoomDetailPeriodPreferences);
		return loadPeriodPreferences(location, type, context);
	}
	
	public PeriodPreferenceModel loadExamPeriodPreferences(PeriodPreferenceRequest request, SessionContext context) {
		Exam exam = (request.getExamId() == null ? null : ExamDAO.getInstance().get(request.getExamId()));
		ExamType type = (exam == null ? ExamTypeDAO.getInstance().get(request.getExamTypeId()) : exam.getExamType());
		return loadExamPeriodPreferences(exam, type, context);
	}
	
	public static PeriodPreferenceModel createPeriodPreferenceModel(Session session, ExamType type, SessionContext context, boolean roomPref) {
		PeriodPreferenceModel model = new PeriodPreferenceModel();
		model.setDefaultHorizontal(CommonValues.HorizontalGrid.eq(context.getUser().getProperty(UserProperty.GridOrientation)));
		model.setReqConfirmation(CommonValues.Yes.eq(context.getUser().getProperty(UserProperty.ConfirmationDialogs)));
		model.setExamType(new ExamTypeInterface(type.getUniqueId(), type.getReference(), type.getLabel(), type.getType() == ExamType.sExamTypeFinal));
		model.setFirstDate(session.getExamBeginDate());
		for (ExamPeriod period: (Set<ExamPeriod>)ExamPeriod.findAll(session.getUniqueId(), type)) {
			model.addPeriod(new PeriodInterface(period.getUniqueId(), period.getDateOffset(), period.getStartSlot(), period.getLength()));
		}
		for (PreferenceLevel pref: PreferenceLevel.getPreferenceLevelList(model.getPeriods().size() < model.getDays().size() * model.getSlots().size())) {
			PreferenceInterface p = new PreferenceInterface(pref.getUniqueId(), PreferenceLevel.prolog2bgColor(pref.getPrefProlog()), pref.getPrefProlog(), pref.getPrefName(), true);
			if (PreferenceLevel.sRequired.equals(pref.getPrefProlog()) && roomPref)
				continue;
			if ((roomPref || model.getExamType().isFinal() ? PreferenceLevel.sNeutral : PreferenceLevel.sProhibited).equals(pref.getPrefProlog()))
				model.setDefaultPreference(p);
			if (PreferenceLevel.sNeutral.equals(pref.getPrefProlog()))
				model.setSelectedPreference(p);
			if (PreferenceLevel.sNotAvailable.equals(pref.getPrefProlog()))
				p.setEditable(false);
			model.addPreference(p);
		}
		return model;
	}
	
	public PeriodPreferenceModel loadPeriodPreferences(Location location, ExamType type, SessionContext context) {
		Session session = (location == null ? SessionDAO.getInstance().get(context.getUser().getCurrentAcademicSessionId()) : location.getSession());
		PeriodPreferenceModel model = createPeriodPreferenceModel(session, type, context, true);
		if (location != null) {
			model.setLocationId(location.getUniqueId());
			for (Iterator i=location.getExamPreferences().iterator();i.hasNext();) {
	            ExamLocationPref pref = (ExamLocationPref)i.next();
	            if (!type.equals(pref.getExamPeriod().getExamType())) continue;
	            model.setPreference(pref.getExamPeriod().getDateOffset(), pref.getExamPeriod().getStartSlot(), pref.getPrefLevel().getUniqueId());
	        }
		}
		return model;
	}
	
	public PeriodPreferenceModel loadExamPeriodPreferences(Exam exam, ExamType type, SessionContext context) {
		return loadExamPeriodPreferences(examinationSolverService == null ? null : examinationSolverService.getSolver(), exam, type, context);
	}
	
	public PeriodPreferenceModel loadExamPeriodPreferences(ExamSolverProxy solver, Exam exam, ExamType type, SessionContext context) {
		Session session = (exam == null ? SessionDAO.getInstance().get(context.getUser().getCurrentAcademicSessionId()) : exam.getSession());
		PeriodPreferenceModel model = createPeriodPreferenceModel(session, type, context, false);
		if (exam != null) {
			model.setExamId(exam.getUniqueId());
			for (Iterator i=exam.getPreferences(ExamPeriodPref.class).iterator();i.hasNext();) {
				ExamPeriodPref pref = (ExamPeriodPref)i.next();
	            model.setPreference(pref.getExamPeriod().getDateOffset(), pref.getExamPeriod().getStartSlot(), pref.getPrefLevel().getUniqueId());
	        }
			if (solver != null && solver.getExamTypeId().equals(exam.getExamType().getUniqueId())) {
				ExamAssignment assignment = solver.getAssignment(exam.getUniqueId());
				if (assignment != null)
					model.setAssignedPeriodId(assignment.getPeriodId());
			} else if (exam.getAssignedPeriod() != null) {
				model.setAssignedPeriodId(exam.getAssignedPeriod().getUniqueId());
			}
		}
		return model;
	}
	
	public PeriodPreferenceModel savePeriodPreferences(PeriodPreferenceRequest request, SessionContext context) {
		Location location = (request.getLocationId() == null ? null : LocationDAO.getInstance().get(request.getLocationId()));
		ExamType type = ExamTypeDAO.getInstance().get(request.getExamTypeId());
		context.checkPermission(location, Right.RoomEditChangeExaminationStatus);
		return savePeriodPreferences(location, type, request.getModel(), context);
	}
	
	public PeriodPreferenceModel savePeriodPreferences(Location location, ExamType type, PeriodPreferenceModel model, SessionContext context) {
		location.clearExamPreferences(type);
		for (ExamPeriod period: ExamPeriod.findAll(location.getSession().getUniqueId(), type)) {
			PreferenceInterface pref = model.getPreference(period.getDateOffset(), period.getStartSlot());
			if (pref != null && !PreferenceLevel.sNeutral.equals(pref.getCode())) {
				location.addExamPreference(period, PreferenceLevel.getPreferenceLevel(pref.getCode()));
			}
		}
		return model;
	}

}
