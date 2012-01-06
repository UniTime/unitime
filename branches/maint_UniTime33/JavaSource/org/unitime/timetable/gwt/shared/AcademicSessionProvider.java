/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
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
package org.unitime.timetable.gwt.shared;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author Tomas Muller
 */
public interface AcademicSessionProvider {
	public Long getAcademicSessionId();
	public String getAcademicSessionName();
	public void addAcademicSessionChangeHandler(AcademicSessionChangeHandler handler);
	
	public static interface AcademicSessionChangeEvent {
		public Long getNewAcademicSessionId();
		public Long getOldAcademicSessionId();
		public boolean isChanged();
	}


	public static interface AcademicSessionChangeHandler {
		public void onAcademicSessionChange(AcademicSessionChangeEvent event);
	}
	
	public void selectSession(Long sessionId, AsyncCallback<Boolean> callback);
}
