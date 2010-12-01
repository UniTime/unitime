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
package org.unitime.timetable.solver.remote.core;

import java.io.File;
import java.security.Permission;

/**
 * @author Tomas Muller
 */
public class StartupMinimal {
	public static class MySecurityManager extends SecurityManager {
		public void checkPermission(Permission perm) {}
		public void checkPermission(Permission perm, Object context) {}
	}
	
    public static void main(String[] args) {
    	SolverTray.init();
		try {
			String solverHome = System.getProperty("tmtbl.solver.home");
			if (solverHome==null) solverHome = System.getProperty("user.home")+File.separator+"solver";
			System.setProperty("java.io.tmpdir",solverHome+File.separator+"tmp");
			System.setSecurityManager(new MySecurityManager());
			try {
				(new File(System.getProperty("java.io.tmpdir"))).mkdirs();
			} catch (Exception e) {}
			ServerClassLoader.
				getInstance().
				findClass("org.unitime.timetable.solver.remote.core.RemoteSolverServer").
				getMethod("main",new Class[] {String[].class}).
				invoke(null, new Object[] {args});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
