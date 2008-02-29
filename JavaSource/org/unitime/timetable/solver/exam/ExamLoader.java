package org.unitime.timetable.solver.exam;

import net.sf.cpsolver.exam.model.ExamModel;
import net.sf.cpsolver.ifs.util.Callback;

import org.apache.log4j.Logger;

public abstract class ExamLoader implements Runnable {
    private ExamModel iModel = null;
    private Callback iCallback = null;
    
    /** Constructor 
     * @param model an empty instance of timetable model 
     */
    public ExamLoader(ExamModel model) {
        iModel = model;
    }
    
    /** Returns provided model.
     * @return provided model
     */
    protected ExamModel getModel() { return iModel; }
    
    /** Load the model.
     */
    public abstract void load() throws Exception;
    
    /** Sets callback class
     * @param callback method {@link Callback#execute()} is executed when load is done
     */
    public void setCallback(Callback callback) { iCallback = callback; }

    public void run() { 
        try {
            load(); 
        } catch (Exception e) {
            Logger.getLogger(this.getClass()).error(e.getMessage(),e);
        } finally {
            if (iCallback!=null)
                iCallback.execute();
        }
    }

}