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
package org.unitime.timetable.model;

import org.unitime.timetable.model.base.BaseExamConflict;

/**
 * @author Tomas Muller
 */
public class ExamConflict extends BaseExamConflict implements Comparable<ExamConflict> {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public ExamConflict () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public ExamConflict (java.lang.Long uniqueId) {
		super(uniqueId);
	}
	
/*[CONSTRUCTOR MARKER END]*/

	
	public static final int sConflictTypeDirect = 0;
    public static final int sConflictTypeMoreThanTwoADay = 1;
	public static final int sConflictTypeBackToBackDist = 2;
	public static final int sConflictTypeBackToBack = 3;
	
	public static String[] sConflictTypes = new String[] {"Distance", ">2 A Day", "Distance Back-To-Back", "Back-To-Back"};
	
    public boolean isDirectConflict() {
        return sConflictTypeDirect==getConflictType();
    }

    public boolean isMoreThanTwoADayConflict() {
        return sConflictTypeMoreThanTwoADay==getConflictType();
    }

    public boolean isBackToBackConflict() {
        return sConflictTypeBackToBack==getConflictType() || sConflictTypeBackToBackDist==getConflictType();
    }

    public boolean isDistanceBackToBackConflict() {
        return sConflictTypeBackToBackDist==getConflictType();
    }
    
    public int compareTo(ExamConflict conflict) {
        int cmp = getConflictType().compareTo(conflict.getConflictType());
        if (cmp!=0) return cmp;
        cmp = getNrStudents().compareTo(conflict.getNrStudents());
        if (cmp!=0) return cmp;
        return (getUniqueId() == null ? new Long(-1) : getUniqueId()).compareTo(conflict.getUniqueId() == null ? -1 : conflict.getUniqueId());
    }
}
