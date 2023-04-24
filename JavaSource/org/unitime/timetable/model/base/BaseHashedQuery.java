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

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import java.io.Serializable;
import java.util.Date;

import org.unitime.timetable.model.HashedQuery;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BaseHashedQuery implements Serializable {
	private static final long serialVersionUID = 1L;

	private String iQueryHash;
	private String iQueryText;
	private Date iCreated;
	private Long iNbrUsed;
	private Date iLastUsed;


	public BaseHashedQuery() {
	}

	public BaseHashedQuery(String queryHash) {
		setQueryHash(queryHash);
	}


	@Id
	@Column(name="query_hash")
	public String getQueryHash() { return iQueryHash; }
	public void setQueryHash(String queryHash) { iQueryHash = queryHash; }

	@Column(name = "query_text", nullable = false, length = 2048)
	public String getQueryText() { return iQueryText; }
	public void setQueryText(String queryText) { iQueryText = queryText; }

	@Column(name = "ts_create", nullable = false)
	public Date getCreated() { return iCreated; }
	public void setCreated(Date created) { iCreated = created; }

	@Column(name = "nbr_use", nullable = false)
	public Long getNbrUsed() { return iNbrUsed; }
	public void setNbrUsed(Long nbrUsed) { iNbrUsed = nbrUsed; }

	@Column(name = "ts_use", nullable = true)
	public Date getLastUsed() { return iLastUsed; }
	public void setLastUsed(Date lastUsed) { iLastUsed = lastUsed; }

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof HashedQuery)) return false;
		if (getQueryHash() == null || ((HashedQuery)o).getQueryHash() == null) return false;
		return getQueryHash().equals(((HashedQuery)o).getQueryHash());
	}

	@Override
	public int hashCode() {
		if (getQueryHash() == null) return super.hashCode();
		return getQueryHash().hashCode();
	}

	@Override
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
