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
package org.unitime.timetable.onlinesectioning.solver;

import java.util.Comparator;

/**
 * @author Tomas Muller
 */
public class SectioningRequestComparator implements Comparator<SectioningRequest>{
	
	protected int compareBothAssignedOrNotAssigned(SectioningRequest s, SectioningRequest r) {
		// Request Priority
		if (s.getRequestPriority() != r.getRequestPriority())
			return s.getRequestPriority().ordinal() < r.getRequestPriority().ordinal() ? -1 : 1;

		// Student Priority
		if (s.getStudentPriority() != r.getStudentPriority())
			return s.getStudentPriority().ordinal() < r.getStudentPriority().ordinal() ? -1 : 1;

		return 0;
	}
	
	protected int compareBothNotAssigned(SectioningRequest s, SectioningRequest r) {
		// Alternativity (first choice before first alternative, etc.)
		if (s.getAlternativity() != r.getAlternativity())
			return s.getAlternativity() < r.getAlternativity() ? -1 : 1;

		// Use wait-listed time stamp
		if (s.getRequest().getWaitListedTimeStamp() != null) {
			if (r.getRequest().getWaitListedTimeStamp() != null) {
				int cmp = s.getRequest().getWaitListedTimeStamp().compareTo(r.getRequest().getWaitListedTimeStamp());
				if (cmp != 0) return cmp;
			} else {
				return 1;
			}
		} else if (r.getRequest().getWaitListedTimeStamp() != null) {
			return -1;
		}
		
		return 0;
	}
	
	protected int compareBothAssigned(SectioningRequest s, SectioningRequest r) {
		// Check individual reservations
		if (s.hasIndividualReservation() && !r.hasIndividualReservation()) return -1;
		if (!s.hasIndividualReservation() && r.hasIndividualReservation()) return 1;

		// Substitute requests last
		if (s.getRequest().isAlternative() && !r.getRequest().isAlternative()) return 1;
		if (!s.getRequest().isAlternative() && r.getRequest().isAlternative()) return -1;
		
		// Use priority
		int cmp = Integer.compare(s.getRequest().getPriority(), r.getRequest().getPriority());
		if (cmp != 0) return cmp;
		
		// Alternativity (first choice before first alternative, etc.)
		if (s.getAlternativity() != r.getAlternativity())
			return s.getAlternativity() < r.getAlternativity() ? -1 : 1;
		
		// Use enrollment time stamp
		if (s.getLastEnrollment().getTimeStamp() != null) {
			if (r.getLastEnrollment().getTimeStamp() != null) {
				return s.getLastEnrollment().getTimeStamp().compareTo(r.getLastEnrollment().getTimeStamp());
			} else {
				return 1;
			}
		} else if (r.getLastEnrollment().getTimeStamp() != null) {
			return -1;
		} else {
			return 0;
		}
	}
	
	protected int compareFallBack(SectioningRequest s, SectioningRequest r) {
		// Use request time stamp
		if (s.getRequest().getTimeStamp() != null) {
			if (r.getRequest().getTimeStamp() != null) {
				int cmp = s.getRequest().getTimeStamp().compareTo(r.getRequest().getTimeStamp());
				if (cmp != 0) return cmp;
			} else {
				return 1;
			}
		} else if (r.getRequest().getTimeStamp() != null) {
			return -1;
		}
		
		return Long.compare(s.getRequest().getRequestId(), r.getRequest().getRequestId());
	}
    
	@Override
	public int compare(SectioningRequest s, SectioningRequest r) {
		// Requests with last enrollment (recently unassigned requests) have priority
		if (s.getLastEnrollment() == null && r.getLastEnrollment() != null) return 1;
		if (s.getLastEnrollment() != null && r.getLastEnrollment() == null) return -1;
		
		int cmp = compareBothAssignedOrNotAssigned(s, r);
		if (cmp != 0) return cmp;
		
		if (s.getLastEnrollment() == null) {
			cmp = compareBothNotAssigned(s, r);
		} else {
			cmp = compareBothAssigned(s, r);
		}
		if (cmp != 0) return cmp;
		
		return compareFallBack(s, r);
	}
}
