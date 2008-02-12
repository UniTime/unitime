package org.unitime.timetable.solver.exam;

import net.sf.cpsolver.exam.model.ExamModel;
import net.sf.cpsolver.ifs.solution.Solution;
import net.sf.cpsolver.ifs.solver.Solver;
import net.sf.cpsolver.ifs.util.Callback;

import org.apache.log4j.Logger;

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