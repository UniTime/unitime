/*
 * UniTime 3.5 (University Timetabling Application)
 * Copyright (C) 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.onlinesectioning.solver;

import org.apache.log4j.Logger;
import org.unitime.timetable.onlinesectioning.solver.expectations.OverExpectedCriterion;
import org.unitime.timetable.onlinesectioning.solver.expectations.MoreSpaceThanExpected;

import net.sf.cpsolver.ifs.util.DataProperties;
import net.sf.cpsolver.studentsct.StudentSectioningModel;
import net.sf.cpsolver.studentsct.model.Request;
import net.sf.cpsolver.studentsct.model.Section;

/**
 * @author Tomas Muller
 */
public class OnlineSectioningModel extends StudentSectioningModel {
	private static Logger sLog = Logger.getLogger(OnlineSectioningModel.class);
	private OverExpectedCriterion iOverExpectedCriterion;

	public OnlineSectioningModel(DataProperties properties) {
		super(properties);
		try {
            Class<OverExpectedCriterion> overExpectedCriterionClass = (Class<OverExpectedCriterion>)Class.forName(properties.getProperty("OverExpectedCriterion.Class", MoreSpaceThanExpected.class.getName()));
            iOverExpectedCriterion = overExpectedCriterionClass.getConstructor(DataProperties.class).newInstance(properties);
        } catch (Exception e) {
        	sLog.error("Unable to create custom over-expected criterion (" + e.getMessage() + "), using default.", e);
        	iOverExpectedCriterion = new MoreSpaceThanExpected(properties);
        }
	}
	
	public OverExpectedCriterion getOverExpectedCriterion() { return iOverExpectedCriterion; }
	
	public void setOverExpectedCriterion(OverExpectedCriterion overExpectedCriterion) { iOverExpectedCriterion = overExpectedCriterion; }
	
	public boolean isOverExpected(Section section, Request request) {
		return getOverExpectedCriterion().isOverExpected(section, request);
	}

}
