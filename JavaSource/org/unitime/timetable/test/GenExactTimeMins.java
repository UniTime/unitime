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
package org.unitime.timetable.test;

import org.unitime.timetable.util.Constants;

/**
 * @author Tomas Muller
 */
public class GenExactTimeMins {
	public static void main(String[] args) {
		System.out.println("insert into EXACT_TIME_MINS (UNIQUEID, MINS_MIN, MINS_MAX, NR_SLOTS, BREAK_TIME)");
		System.out.println("  values (PREF_GROUP_SEQ.nextval, 0, 0, 0, 0);");
		for (int minPerMtg=5;minPerMtg<=720;minPerMtg+=5) {
			int slotsPerMtg = (int)Math.round((6.0/5.0) * minPerMtg / Constants.SLOT_LENGTH_MIN);
			if (minPerMtg<30.0) slotsPerMtg = Math.min(6,slotsPerMtg);
			int breakTime = 0;
			if (slotsPerMtg%12==0) breakTime = 10;
			else if (slotsPerMtg>6) breakTime = 15;
			System.out.println("insert into EXACT_TIME_MINS (UNIQUEID, MINS_MIN, MINS_MAX, NR_SLOTS, BREAK_TIME)");
			System.out.println("  values (PREF_GROUP_SEQ.nextval, "+(minPerMtg-4)+", "+minPerMtg+", "+slotsPerMtg+", "+breakTime+");");
		}
	}
}
