/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2008 - 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.solver.exam;


import org.apache.log4j.Logger;
import org.cpsolver.exam.model.Exam;
import org.cpsolver.exam.model.ExamPlacement;
import org.cpsolver.ifs.assignment.Assignment;
import org.cpsolver.ifs.solution.Solution;
import org.cpsolver.ifs.solver.Solver;
import org.cpsolver.ifs.util.Callback;
import org.unitime.timetable.ApplicationProperties;

/**
 * @author Tomas Muller
 */
public abstract class ExamSaver implements Runnable {
    private Solver iSolver = null; 
    private Callback iCallback = null;
    /** Constructor
     */
    public ExamSaver(Solver solver) {
        iSolver = solver;
    }
    /** Solver */
    public Solver getSolver() { return iSolver; }
    /** Solution to be saved */
    protected Solution getSolution() { return iSolver.currentSolution(); }
    /** Model of the solution */
    protected ExamModel getModel() { return (ExamModel)iSolver.currentSolution().getModel(); }
    /** Assignment of the solutioon */
    protected Assignment<Exam, ExamPlacement> getAssignment() { return iSolver.currentSolution().getAssignment(); }
    /** Save the solution*/
    public abstract void save() throws Exception;
    /** Sets callback class
     * @param callback method {@link Callback#execute()} is executed when save is done
     */
    public void setCallback(Callback callback) { iCallback = callback; }

    public void run() { 
        try {
        	if (getModel() != null)
        		ApplicationProperties.setSessionId(getModel().getProperties().getPropertyLong("General.SessionId", (Long)null));
            save(); 
        } catch (Exception e) {
            Logger.getLogger(this.getClass()).error(e.getMessage(),e);
        } finally {
            if (iCallback!=null)
                iCallback.execute();
            ApplicationProperties.setSessionId(null);
        }
    }
}
