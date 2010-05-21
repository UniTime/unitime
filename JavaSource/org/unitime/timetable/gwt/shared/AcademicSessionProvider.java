/*
 * UniTime 4.0 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package org.unitime.timetable.gwt.shared;

public interface AcademicSessionProvider {
	public Long getAcademicSessionId();
	public String getAcademicSessionName();
	public void addAcademicSessionChangeHandler(AcademicSessionChangeHandler handler);
	
	public static interface AcademicSessionChangeEvent {
		public Long getNewAcademicSessionId();
	}


	public static interface AcademicSessionChangeHandler {
		public void onAcademicSessionChange(AcademicSessionChangeEvent event);
	}
}
