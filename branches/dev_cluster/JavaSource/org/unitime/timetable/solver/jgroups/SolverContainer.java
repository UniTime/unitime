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
package org.unitime.timetable.solver.jgroups;

import java.util.Set;

import net.sf.cpsolver.ifs.util.DataProperties;

public interface SolverContainer<T> {
	public Set<String> getSolvers();
	
	public T getSolver(String user);
	
	public boolean hasSolver(String user);
	
	public T createSolver(String user, DataProperties config);
	
	public int getUsage();
	
	public void start();
	
	public void stop();
}
