/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
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
package org.unitime.timetable.solver.curricula.students;

import net.sf.cpsolver.ifs.solution.Solution;
import net.sf.cpsolver.ifs.termination.GeneralTerminationCondition;
import net.sf.cpsolver.ifs.termination.TerminationCondition;
import net.sf.cpsolver.ifs.util.DataProperties;

/**
 * @author Tomas Muller
 */
public class CurTermination implements TerminationCondition<CurVariable, CurValue>{
    protected static org.apache.log4j.Logger sLogger = org.apache.log4j.Logger.getLogger(GeneralTerminationCondition.class);
    private int iMaxIter;
    private double iTimeOut;
    private boolean iStopWhenComplete;
    private long iMaxIdle;

	public CurTermination(DataProperties properties) {
        iMaxIter = properties.getPropertyInt("Termination.MaxIters", -1);
        iTimeOut = properties.getPropertyDouble("Termination.TimeOut", -1.0);
        iStopWhenComplete = properties.getPropertyBoolean("Termination.StopWhenComplete", false);
        iMaxIdle = properties.getPropertyLong("Termination.MaxIdle", 10000);
    }
	
    public boolean canContinue(Solution<CurVariable, CurValue> currentSolution) {
        if (iMaxIter >= 0 && currentSolution.getIteration() >= iMaxIter) {
            sLogger.info("Maximum number of iteration reached.");
            return false;
        }
        if (iTimeOut >= 0 && currentSolution.getTime() > iTimeOut) {
            sLogger.info("Timeout reached.");
            return false;
        }
        if (iStopWhenComplete || (iMaxIter < 0 && iTimeOut < 0)) {
            boolean ret = (currentSolution.getModel().nrUnassignedVariables() != 0);
            if (!ret)
                sLogger.info("Complete solution found.");
            return ret;
        }
        if (currentSolution.getIteration() - currentSolution.getBestIteration() > iMaxIdle) {
        	sLogger.info("Maximum idle iterations reached.");
            return false;
        }
        return true;
    }

}
