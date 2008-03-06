/*
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2008, UniTime.org, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package org.unitime.timetable.solver.exam;

import net.sf.cpsolver.exam.model.ExamModel;
import net.sf.cpsolver.ifs.solution.Solution;
import net.sf.cpsolver.ifs.solver.Solver;
import net.sf.cpsolver.ifs.util.Callback;

import org.apache.log4j.Logger;

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
    /** Save the solution*/
    public abstract void save() throws Exception;
    /** Sets callback class
     * @param callback method {@link Callback#execute()} is executed when save is done
     */
    public void setCallback(Callback callback) { iCallback = callback; }

    public void run() { 
        try {
            save(); 
        } catch (Exception e) {
            Logger.getLogger(this.getClass()).error(e.getMessage(),e);
        } finally {
            if (iCallback!=null)
                iCallback.execute();
        }
    }
}