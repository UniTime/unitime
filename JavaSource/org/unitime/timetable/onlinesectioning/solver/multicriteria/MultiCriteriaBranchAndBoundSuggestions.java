/*
 * UniTime 3.3 - 3.5 (University Timetabling Application)
 * Copyright (C) 2011 - 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.onlinesectioning.solver.multicriteria;

import java.util.Date;
import java.util.Hashtable;
import java.util.Set;

import net.sf.cpsolver.ifs.util.DataProperties;
import net.sf.cpsolver.studentsct.model.CourseRequest;
import net.sf.cpsolver.studentsct.model.FreeTimeRequest;
import net.sf.cpsolver.studentsct.model.Request;
import net.sf.cpsolver.studentsct.model.Section;
import net.sf.cpsolver.studentsct.model.Student;

import org.unitime.timetable.onlinesectioning.solver.OnlineSectioningModel;
import org.unitime.timetable.onlinesectioning.solver.SuggestionsBranchAndBound;
import org.unitime.timetable.onlinesectioning.solver.multicriteria.MultiCriteriaBranchAndBoundSelection.SelectionCriterion;

/**
 * @author Tomas Muller
 */
public class MultiCriteriaBranchAndBoundSuggestions extends SuggestionsBranchAndBound {
	
	public MultiCriteriaBranchAndBoundSuggestions(DataProperties properties, Student student,
			Hashtable<CourseRequest, Set<Section>> requiredSections,
			Set<FreeTimeRequest> requiredFreeTimes,
			Hashtable<CourseRequest, Set<Section>> preferredSections,
			Request selectedRequest, Section selectedSection, String filter, Date firstDate, int maxSectionsWithPenalty,
			boolean priorityWeighting) {
		super(properties, student, requiredSections, requiredFreeTimes, preferredSections, selectedRequest, selectedSection, filter, firstDate, maxSectionsWithPenalty);
		if (priorityWeighting)
			iComparator = new OnlineSectioningCriterion(student, (OnlineSectioningModel)selectedRequest.getModel(), preferredSections);
		else
			iComparator = new EqualWeightCriterion(student, (OnlineSectioningModel)selectedRequest.getModel(), preferredSections);
	}
	
	@Override
	protected int compare(Suggestion s1, Suggestion s2) {
		return ((SelectionCriterion)iComparator).compare(s1.getEnrollments(), s2.getEnrollments());
	}

}
