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
import java.util.Date;

import org.unitime.timetable.model.HashedQuery;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseHashedQuery implements Serializable {
	private static final long serialVersionUID = 1L;

	private String iQueryHash;
	private String iQueryText;
	private Date iCreated;
	private Long iNbrUsed;
	private Date iLastUsed;


	public static String PROP_QUERY_HASH = "queryHash";
	public static String PROP_QUERY_TEXT = "queryText";
	public static String PROP_TS_CREATE = "created";
	public static String PROP_NBR_USE = "nbrUsed";
	public static String PROP_TS_USE = "lastUsed";

	public BaseHashedQuery() {
		initialize();
	}

	public BaseHashedQuery(String queryHash) {
		setQueryHash(queryHash);
		initialize();
	}

	protected void initialize() {}

	public String getQueryHash() { return iQueryHash; }
	public void setQueryHash(String queryHash) { iQueryHash = queryHash; }

	public String getQueryText() { return iQueryText; }
	public void setQueryText(String queryText) { iQueryText = queryText; }

	public Date getCreated() { return iCreated; }
	public void setCreated(Date created) { iCreated = created; }

	public Long getNbrUsed() { return iNbrUsed; }
	public void setNbrUsed(Long nbrUsed) { iNbrUsed = nbrUsed; }

	public Date getLastUsed() { return iLastUsed; }
	public void setLastUsed(Date lastUsed) { iLastUsed = lastUsed; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof HashedQuery)) return false;
		if (getQueryHash() == null || ((HashedQuery)o).getQueryHash() == null) return false;
		return getQueryHash().equals(((HashedQuery)o).getQueryHash());
	}

	public int hashCode() {
		if (getQueryHash() == null) return super.hashCode();
		return getQueryHash().hashCode();
	}

	public String toString() {
		return "HashedQuery["+getQueryHash()+"]";
	}

	public String toDebugString() {
		return "HashedQuery[" +
			"\n	Created: " + getCreated() +
			"\n	LastUsed: " + getLastUsed() +
			"\n	NbrUsed: " + getNbrUsed() +
			"\n	QueryHash: " + getQueryHash() +
			"\n	QueryText: " + getQueryText() +
			"]";
	}
}
