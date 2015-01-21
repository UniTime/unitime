/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
