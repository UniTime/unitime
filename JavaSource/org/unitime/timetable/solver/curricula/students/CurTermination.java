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
package org.unitime.timetable.solver.curricula.students;

import org.cpsolver.ifs.solution.Solution;
import org.cpsolver.ifs.termination.GeneralTerminationCondition;
import org.cpsolver.ifs.termination.TerminationCondition;
import org.cpsolver.ifs.util.DataProperties;

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
            boolean ret = (currentSolution.getModel().nrUnassignedVariables(currentSolution.getAssignment()) != 0);
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
