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

import org.unitime.timetable.model.Session;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public class SessionConfigId implements Serializable {
	private static final long serialVersionUID = 1L;

	private Session iSession;
	private String iKey;

	public SessionConfigId() {}

	public SessionConfigId(Session session, String key) {
		iSession = session;
		iKey = key;
	}

	public Session getSession() { return iSession; }
	public void setSession(Session session) { iSession = session; }

	public String getKey() { return iKey; }
	public void setKey(String key) { iKey = key; }


	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof SessionConfigId)) return false;
		SessionConfigId sessionConfig = (SessionConfigId)o;
		if (getSession() == null || sessionConfig.getSession() == null || !getSession().equals(sessionConfig.getSession())) return false;
		if (getKey() == null || sessionConfig.getKey() == null || !getKey().equals(sessionConfig.getKey())) return false;
		return true;
	}

	@Override
	public int hashCode() {
		if (getSession() == null || getKey() == null) return super.hashCode();
		return getSession().hashCode() ^ getKey().hashCode();
	}

}
