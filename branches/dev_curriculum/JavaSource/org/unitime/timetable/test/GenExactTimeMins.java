/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2008 - 2010, UniTime LLC, and individual contributors
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
