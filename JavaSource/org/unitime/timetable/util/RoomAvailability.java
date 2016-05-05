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
package org.unitime.timetable.util;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.unitime.commons.Debug;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.interfaces.RoomAvailabilityInterface;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.ExamPeriod;
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.ExamTypeDAO;
import org.unitime.timetable.solver.SolverProxy;
import org.unitime.timetable.solver.WebSolver;
import org.unitime.timetable.solver.exam.ExamSolverProxy;

/**
 * @author Tomas Muller
 */
public class RoomAvailability {
	protected static GwtMessages MESSAGES = Localization.create(GwtMessages.class);
    private static RoomAvailabilityInterface sInstance = null;
    
    public static RoomAvailabilityInterface getInstance() {
        if (sInstance!=null) return sInstance;
        if (ApplicationProperty.RoomAvailabilityImplementation.value()==null) return null;
        try {
            sInstance = (RoomAvailabilityInterface)Class.forName(ApplicationProperty.RoomAvailabilityImplementation.value()).getConstructor().newInstance();
            return sInstance;
        } catch (Exception e) {
            Debug.error(e);
            return null;
        }
    }
    
    public static void setAvailabilityWarning(HttpServletRequest request, Session acadSession, Long examType, boolean checkSolver, boolean checkAvailability) {
        if (acadSession==null || examType==null || getInstance()==null) return;
        if (checkSolver) {
            ExamSolverProxy solver = WebSolver.getExamSolver(request.getSession());
            if (solver!=null && solver.getExamTypeId().equals(examType)) {
                String ts = solver.getProperties().getProperty("RoomAvailability.TimeStamp");
                ExamType type = ExamTypeDAO.getInstance().get(examType);
                if (type == null) return;
                if (ts==null)
                    request.setAttribute(Constants.REQUEST_WARN, MESSAGES.warnExamSolverNoRoomAvailability(type.getLabel().toLowerCase()));
                else
                    request.setAttribute(Constants.REQUEST_MSSG, MESSAGES.infoExamSolverRoomAvailabilityLastUpdated(type.getLabel().toLowerCase(), ts));
                return;
            }
        }
        if (checkAvailability) {
            Date[] bounds = ExamPeriod.getBounds(acadSession, examType);
            ExamType type = ExamTypeDAO.getInstance().get(examType);
            if (type == null) return;
            String exclude = (type.getType() == ExamType.sExamTypeFinal ? RoomAvailabilityInterface.sFinalExamType : RoomAvailabilityInterface.sMidtermExamType);
            String ts = getInstance().getTimeStamp(bounds[0], bounds[1], exclude);
            if (ts==null)
                request.setAttribute(Constants.REQUEST_WARN, MESSAGES.warnExamSolverNoRoomAvailability(type.getLabel().toLowerCase()));
            else
                request.setAttribute(Constants.REQUEST_MSSG, MESSAGES.infoExamSolverRoomAvailabilityLastUpdated(type.getLabel().toLowerCase(), ts));
        }
    }

    public static void setAvailabilityWarning(HttpServletRequest request, Session acadSession, boolean checkSolver, boolean checkAvailability) {
        if (acadSession==null || getInstance()==null) return;
        if (checkSolver) {
            SolverProxy solver = WebSolver.getSolver(request.getSession());
            if (solver!=null) {
            	String ts = null;
            	try {
            		ts = solver.getProperties().getProperty("RoomAvailability.TimeStamp");
            	} catch (Exception e) {}
                if (ts==null)
                    request.setAttribute(Constants.REQUEST_WARN, MESSAGES.warnCourseSolverNoRoomAvailability());
                else
                    request.setAttribute(Constants.REQUEST_MSSG, MESSAGES.infoCourseSolverRoomAvailabilityLastUpdated(ts));
                return;
            }
        }
        if (checkAvailability) {
            Date[] bounds = DatePattern.getBounds(acadSession.getUniqueId());
            String ts = getInstance().getTimeStamp(bounds[0], bounds[1], RoomAvailabilityInterface.sClassType);
            if (ts==null)
                request.setAttribute(Constants.REQUEST_WARN, MESSAGES.warnCourseSolverNoRoomAvailability());
            else
                request.setAttribute(Constants.REQUEST_MSSG, MESSAGES.infoCourseSolverRoomAvailabilityLastUpdated(ts));
        }
    }
}
