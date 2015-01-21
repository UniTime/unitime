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
package org.unitime.timetable.solver.exam;


import org.apache.log4j.Logger;
import org.cpsolver.exam.model.Exam;
import org.cpsolver.exam.model.ExamPlacement;
import org.cpsolver.ifs.assignment.Assignment;
import org.cpsolver.ifs.util.Callback;
import org.unitime.timetable.ApplicationProperties;

/**
 * @author Tomas Muller
 */
public abstract class ExamLoader implements Runnable {
    private ExamModel iModel = null;
    private Assignment<Exam, ExamPlacement> iAssignment = null;
    private Callback iCallback = null;
    
    /** Constructor 
     * @param model an empty instance of timetable model 
     */
    public ExamLoader(ExamModel model, Assignment<Exam, ExamPlacement> assignment) {
        iModel = model;
        iAssignment = assignment;
    }
    
    /** Returns provided model.
     * @return provided model
     */
    protected ExamModel getModel() { return iModel; }
    
    /**
     * Returns provided assignment
     */
    protected Assignment<Exam, ExamPlacement> getAssignment() { return iAssignment; }
    
    /** Load the model.
     */
    public abstract void load() throws Exception;
    
    /** Sets callback class
     * @param callback method {@link Callback#execute()} is executed when load is done
     */
    public void setCallback(Callback callback) { iCallback = callback; }

    public void run() { 
        try {
        	if (getModel() != null)
        		ApplicationProperties.setSessionId(getModel().getProperties().getPropertyLong("General.SessionId", (Long)null));
            load(); 
        } catch (Exception e) {
            Logger.getLogger(this.getClass()).error(e.getMessage(),e);
        } finally {
            if (iCallback!=null)
                iCallback.execute();
            ApplicationProperties.setSessionId(null);
        }
    }
    
}
