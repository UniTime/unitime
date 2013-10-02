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
package org.unitime.timetable.onlinesectioning;

import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.onlinesectioning.server.InMemoryServer;

public class OnlineSectioningServerFactory {
	
	public OnlineSectioningServerFactory() {
		
	}
	
	public OnlineSectioningServer create(Long sessionId, boolean waitTillStarted) throws SectioningException {
		try {
			Class server = Class.forName(ApplicationProperties.getProperty("unitime.enrollment.server.class", InMemoryServer.class.getName()));
			return (OnlineSectioningServer)server.getConstructor(Long.class, boolean.class).newInstance(sessionId, waitTillStarted);
		} catch (SectioningException e) {
			throw e;
		} catch (Exception e) {
			throw new SectioningException(e.getMessage(), e);
		}
	}

}
