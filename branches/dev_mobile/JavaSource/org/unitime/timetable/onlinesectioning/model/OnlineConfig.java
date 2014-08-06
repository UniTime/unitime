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
package org.unitime.timetable.onlinesectioning.model;

import org.cpsolver.studentsct.model.Config;
import org.cpsolver.studentsct.model.Offering;

/**
 * @author Tomas Muller
 */
public class OnlineConfig extends Config {
	private int iEnrollment = 0;
	
	public OnlineConfig(long id, int limit, String name, Offering offering) {
		super(id, limit, name, offering);
	}
	
	public void setEnrollment(int enrollment) { iEnrollment = enrollment; }
	public int getEnrollment() { return iEnrollment; }
}
