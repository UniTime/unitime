/*
 * UniTime 3.4 (University Timetabling Application)
 * Copyright (C) 2012, UniTime LLC, and individual contributors
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
package org.unitime.timetable.defaults;

public enum ApplicationProperty {
	KeepLastUsedAcademicSession("tmtbl.keeplastused.session", "false", "On login, automatically select the last used academic session."),
	DistanceEllipsoid("unitime.distance.ellipsoid", "LEGACY", "Distance matrix ellipsid"),
	SolverMemoryLimit("tmtbl.solver.mem_limit", "200", "Minimal amount of free memory (in MB) for the solver to load."),
	
	;

	String iKey, iDefault, iDescription;
	ApplicationProperty(String key, String defaultValue, String description) {
		iKey = key; iDefault = defaultValue; iDescription = defaultValue;
	}
	
	public String key() { return iKey; }
	public String defaultValue() { return iDefault; }
	public String description() { return iDescription; }
}
