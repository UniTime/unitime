/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2008 - 2013, UniTime LLC, and individual contributors
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
package org.unitime.commons.hibernate.util;

import org.dom4j.Document;
import org.unitime.timetable.defaults.ApplicationProperty;

/**
 * @author Stephanie Schluttenhofer
 *
 */
public class UniTimeCoreDatabaseUpdate extends DatabaseUpdate {

	/**
	 * @param document
	 * @throws Exception
	 */
	public UniTimeCoreDatabaseUpdate(Document document) throws Exception {
		super(document);
	}
	public UniTimeCoreDatabaseUpdate() throws Exception {
		super();
	}

	@Override
	protected String findDbUpdateFileName() {
		return ApplicationProperty.DatabaseUpdateFile.value();
	}
	@Override
	protected String versionParameterName() {
		return("tmtbl.db.version");
	}
	@Override
	protected String updateName() {
		return("UniTime");
	}

}
