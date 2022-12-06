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
package org.unitime.timetable.onlinesectioning.custom.purdue;

import java.util.Comparator;

import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.custom.WaitListComparatorProvider;
import org.unitime.timetable.onlinesectioning.solver.SectioningRequest;
import org.unitime.timetable.onlinesectioning.solver.SectioningRequestComparator;

/**
 * @author Tomas Muller
 */
public class TimeStampOnlyWaitListComparatorProvider implements WaitListComparatorProvider {

	@Override
	public Comparator<SectioningRequest> getComparator(OnlineSectioningServer server, OnlineSectioningHelper helper) {
		return new TimeStampOnlyWaitListComparator();
	}
	
	public static class TimeStampOnlyWaitListComparator extends SectioningRequestComparator {
		@Override
		protected int compareBothAssignedOrNotAssigned(SectioningRequest s, SectioningRequest r) {
			return 0;
		}
		
		@Override
		protected int compareBothNotAssigned(SectioningRequest s, SectioningRequest r) {
			// Use wait-listed time stamp
			if (s.getRequest().getWaitListedTimeStamp() != null) {
				if (r.getRequest().getWaitListedTimeStamp() != null) {
					return s.getRequest().getWaitListedTimeStamp().compareTo(r.getRequest().getWaitListedTimeStamp());
				} else {
					return 1;
				}
			} else if (r.getRequest().getWaitListedTimeStamp() != null) {
				return -1;
			} else {
				return 0;
			}
		}
		
		@Override
		protected int compareBothAssigned(SectioningRequest s, SectioningRequest r) {
			// check student priority and request priority
			int cmp = super.compareBothAssignedOrNotAssigned(s, r);
			if (cmp != 0) return cmp;
			
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
	}
}
