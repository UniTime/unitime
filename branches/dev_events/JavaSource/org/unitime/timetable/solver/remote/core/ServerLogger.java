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

/**
 * @author Tomas Muller
 */
public class ServerLogger {
	public Object iLoggerObject = null;
	public Class iCaller = null;
	public static boolean sInitialized = false; 
	
	public static void setInitialized(boolean initialized) {
		sInitialized = initialized;
	}
	
	public ServerLogger(Class caller) {
		iCaller = caller;
	}
	
	public void x(String method, Object message, Throwable e) {
		try {
			if (iLoggerObject==null && sInitialized) {
				iLoggerObject = ServerClassLoader.getInstance().loadClass("org.apache.log4j.Logger").getMethod("getLogger", new Class[]{Class.class}).invoke(null, new Object[] {iCaller});
			}
			if (iLoggerObject!=null)
				iLoggerObject.getClass().getMethod(method, new Class[] {Object.class, Throwable.class}).invoke(iLoggerObject, new Object[] {message,e});
			return;
		} catch (Exception ex) {}
		System.out.println(method.toUpperCase()+": "+message);
		if (e!=null)
			e.printStackTrace(System.out);
	}
	
	public void error(Object msg, Throwable e) { x("error", msg, e); }
	public void error(Object msg) { x("error", msg, null); }
	public void debug(Object msg, Throwable e) { x("debug", msg, e); }
	public void debug(Object msg) { x("debug", msg, null); }
	public void info(Object msg, Throwable e) { x("info", msg, e);}
	public void info(Object msg) { x("info", msg, null);}
	public void warn(Object msg, Throwable e) { x("warn", msg, e);}
	public void warn(Object msg) { x("warn", msg, null);}

}
