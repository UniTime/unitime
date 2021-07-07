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
package org.unitime.timetable.onlinesectioning.custom;

import java.util.Comparator;

import org.cpsolver.ifs.util.DataProperties;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.solver.SectioningRequest;
import org.unitime.timetable.onlinesectioning.solver.SectioningRequestComparator;

/**
 * @author Tomas Muller
 */
public class DefaultSectioningRequestComparatorProvider implements WaitListComparatorProvider {
	public DefaultSectioningRequestComparatorProvider() {}
	
	@Override
	public Comparator<SectioningRequest> getComparator(OnlineSectioningServer server, OnlineSectioningHelper helper) {
		return new DefaultSectioningRequestComparator(server.getConfig());
	}
	
	public class DefaultSectioningRequestComparator extends SectioningRequestComparator{
		private boolean iConsiderRequestPriority = true;
		private boolean iConsiderStudentPriority = true;
	    private boolean iStudentPriorityIsMoreImportant = false;
	    
		public DefaultSectioningRequestComparator(DataProperties properties) {
			// Request Priority: critical courses first
			iConsiderRequestPriority = properties.getPropertyBoolean("Sectioning.UseCriticalCoursesSelection", iConsiderRequestPriority);
			// Student Priority: priority students first
			iConsiderStudentPriority = properties.getPropertyBoolean("Sectioning.PriorityStudentsFirstSelection", iConsiderStudentPriority);
			// Student Priority: priority is more important than critical (request priority is more important otherwise)
			iStudentPriorityIsMoreImportant = properties.getPropertyBoolean("Sectioning.PriorityStudentsFirstSelection.AllIn", iStudentPriorityIsMoreImportant);
		}
		
		@Override
		protected int compareBothAssignedOrNotAssigned(SectioningRequest s, SectioningRequest r) {
			if (iStudentPriorityIsMoreImportant) {
				if (iConsiderStudentPriority) {
					// Student Priority
					if (s.getStudentPriority() != r.getStudentPriority())
						return s.getStudentPriority().ordinal() < r.getStudentPriority().ordinal() ? -1 : 1;
				}
				if (iConsiderRequestPriority) {
					// Request Priority
					if (s.getRequestPriority() != r.getRequestPriority())
						return s.getRequestPriority().ordinal() < r.getRequestPriority().ordinal() ? -1 : 1;
	            }
			} else {
				if (iConsiderRequestPriority) {
					// Request Priority
					if (s.getRequestPriority() != r.getRequestPriority())
						return s.getRequestPriority().ordinal() < r.getRequestPriority().ordinal() ? -1 : 1;
				}
				if (iConsiderStudentPriority) {
					// Student Priority
					if (s.getStudentPriority() != r.getStudentPriority())
						return s.getStudentPriority().ordinal() < r.getStudentPriority().ordinal() ? -1 : 1;
				}
			}
			return 0;
		}
	}
}
