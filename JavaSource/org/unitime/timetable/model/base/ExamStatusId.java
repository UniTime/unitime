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
package org.unitime.timetable.model.base;

import java.io.Serializable;

import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.Session;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public class ExamStatusId implements Serializable {
	private static final long serialVersionUID = 1L;

	private Session iSession;
	private ExamType iType;

	public ExamStatusId() {}

	public ExamStatusId(Session session, ExamType type) {
		iSession = session;
		iType = type;
	}

	public Session getSession() { return iSession; }
	public void setSession(Session session) { iSession = session; }

	public ExamType getType() { return iType; }
	public void setType(ExamType type) { iType = type; }


	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof ExamStatusId)) return false;
		ExamStatusId examStatus = (ExamStatusId)o;
		if (getSession() == null || examStatus.getSession() == null || !getSession().equals(examStatus.getSession())) return false;
		if (getType() == null || examStatus.getType() == null || !getType().equals(examStatus.getType())) return false;
		return true;
	}

	@Override
	public int hashCode() {
		if (getSession() == null || getType() == null) return super.hashCode();
		return getSession().hashCode() ^ getType().hashCode();
	}

}
