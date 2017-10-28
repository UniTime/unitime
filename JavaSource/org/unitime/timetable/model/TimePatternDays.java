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

import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.model.base.BaseTimePatternDays;
import org.unitime.timetable.util.Constants;



/**
 * @author Tomas Muller
 */
public class TimePatternDays extends BaseTimePatternDays implements Comparable {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public TimePatternDays () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public TimePatternDays (java.lang.Long uniqueId) {
		super(uniqueId);
	}

/*[CONSTRUCTOR MARKER END]*/
	
	public boolean equals(Object o) {
		return (compareTo(o)==0);
	}

	public int compareTo(Object o) {
		if (o==null || !(o instanceof TimePatternDays)) return -1;
		Integer firstDayOfWeek = ApplicationProperty.TimePatternFirstDayOfWeek.intValue();
		if (firstDayOfWeek == null || firstDayOfWeek.equals(0)) {
			return -getDayCode().compareTo(((TimePatternDays)o).getDayCode());
		} else {
			for (int i = 0; i < Constants.DAY_CODES.length; i++) {
				int idx = (i + firstDayOfWeek) % 7;
				boolean a = (getDayCode() & Constants.DAY_CODES[idx]) != 0;
				boolean b = (((TimePatternDays)o).getDayCode() & Constants.DAY_CODES[idx]) != 0;
				if (a != b)
					return (a ? -1 : 1);
			}
			return 0;
		}
	}

	public int hashCode() {
		return getDayCode().hashCode();
	}
}
