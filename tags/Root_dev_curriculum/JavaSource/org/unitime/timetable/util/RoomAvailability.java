package org.unitime.timetable.util;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.unitime.commons.Debug;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.interfaces.RoomAvailabilityInterface;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.ExamPeriod;
import org.unitime.timetable.model.Session;
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
    
    public static void setAvailabilityWarning(HttpServletRequest request, Session acadSession, int examType, boolean checkSolver, boolean checkAvailability) {
        if (acadSession==null || examType<0 || getInstance()==null) return;
        if (checkSolver) {
            ExamSolverProxy solver = WebSolver.getExamSolver(request.getSession());
            if (solver!=null && solver.getExamType()==examType) {
                String ts = solver.getProperties().getProperty("RoomAvailability.TimeStamp");
                if (ts==null)
                    request.setAttribute(Constants.REQUEST_WARN,"Room availability is not available for "+Exam.sExamTypes[examType].toLowerCase()+" examinations.");
                else
                    request.setAttribute(Constants.REQUEST_MSSG,"Room availability for "+Exam.sExamTypes[examType].toLowerCase()+" examination solver was updated on "+ts+".");
                return;
            }
        }
        if (checkAvailability) {
            Date[] bounds = ExamPeriod.getBounds(acadSession, examType);
            String exclude = (examType==org.unitime.timetable.model.Exam.sExamTypeFinal?RoomAvailabilityInterface.sFinalExamType:RoomAvailabilityInterface.sMidtermExamType);
            String ts = getInstance().getTimeStamp(bounds[0], bounds[1], exclude);
            if (ts==null)
                request.setAttribute(Constants.REQUEST_WARN,"Room availability is not available for "+Exam.sExamTypes[examType].toLowerCase()+" examinations.");
            else
                request.setAttribute(Constants.REQUEST_MSSG,"Room availability for "+Exam.sExamTypes[examType].toLowerCase()+" examinations was updated on "+ts+".");
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
