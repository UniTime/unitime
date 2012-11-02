/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2008 - 2010, UniTime LLC, and individual contributors
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
package org.unitime.timetable.util;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.unitime.commons.Debug;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.interfaces.RoomAvailabilityInterface;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.ExamPeriod;
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.ExamTypeDAO;
import org.unitime.timetable.solver.SolverProxy;
import org.unitime.timetable.solver.WebSolver;
import org.unitime.timetable.solver.exam.ExamSolverProxy;

public class RoomAvailability {
    private static RoomAvailabilityInterface sInstance = null;
    
    public static RoomAvailabilityInterface getInstance() {
        if (sInstance!=null) return sInstance;
        if (ApplicationProperties.getProperty("tmtbl.room.availability.class")==null) return null;
        try {
            sInstance = (RoomAvailabilityInterface)Class.forName(ApplicationProperties.getProperty("tmtbl.room.availability.class")).getConstructor().newInstance();
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
                    request.setAttribute(Constants.REQUEST_WARN,"Room availability is not available for "+type.getLabel().toLowerCase()+" examinations.");
                else
                    request.setAttribute(Constants.REQUEST_MSSG,"Room availability for "+type.getLabel().toLowerCase()+" examination solver was updated on "+ts+".");
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
                request.setAttribute(Constants.REQUEST_WARN,"Room availability is not available for "+type.getLabel().toLowerCase()+" examinations.");
            else
                request.setAttribute(Constants.REQUEST_MSSG,"Room availability for "+type.getLabel().toLowerCase()+" examinations was updated on "+ts+".");
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
                    request.setAttribute(Constants.REQUEST_WARN,"Room availability is not available for classes.");
                else
                    request.setAttribute(Constants.REQUEST_MSSG,"Room availability for course timetabling solver was updated on "+ts+".");
                return;
            }
        }
        if (checkAvailability) {
            Date[] bounds = DatePattern.getBounds(acadSession.getUniqueId());
            String ts = getInstance().getTimeStamp(bounds[0], bounds[1], RoomAvailabilityInterface.sClassType);
            if (ts==null)
                request.setAttribute(Constants.REQUEST_WARN,"Room availability is not available for classes.");
            else
                request.setAttribute(Constants.REQUEST_MSSG,"Room availability for classes was updated on "+ts+".");
        }
    }
}
