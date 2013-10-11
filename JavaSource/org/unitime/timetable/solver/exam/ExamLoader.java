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
package org.unitime.timetable.solver.exam;

import net.sf.cpsolver.ifs.util.Callback;

import org.apache.log4j.Logger;
import org.unitime.timetable.ApplicationProperties;

/**
 * @author Tomas Muller
 */
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
