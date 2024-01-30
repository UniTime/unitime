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

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

import java.io.Serializable;
import java.util.Date;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.unitime.commons.hibernate.id.UniqueIdGenerator;
import org.unitime.timetable.model.AccessStatistics;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BaseAccessStatistics implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private Date iTimeStamp;
	private String iHost;
	private String iPage;
	private Integer iAccess;
	private Integer iActive;
	private Integer iWaiting;
	private Integer iOpened;
	private Integer iTracking;
	private Integer iActive1m;
	private Integer iActive2m;
	private Integer iActive5m;
	private Integer iActive10m;
	private Integer iActive15m;


	public BaseAccessStatistics() {
	}

	public BaseAccessStatistics(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Id
	@GenericGenerator(name = "access_stats_id", type = UniqueIdGenerator.class, parameters = {
		@Parameter(name = "sequence", value = "pref_group_seq")
	})
	@GeneratedValue(generator = "access_stats_id")
	@Column(name="uniqueid")
	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	@Column(name = "time_stamp", nullable = false)
	public Date getTimeStamp() { return iTimeStamp; }
	public void setTimeStamp(Date timeStamp) { iTimeStamp = timeStamp; }

	@Column(name = "host", nullable = false, length = 50)
	public String getHost() { return iHost; }
	public void setHost(String host) { iHost = host; }

	@Column(name = "page", nullable = false, length = 50)
	public String getPage() { return iPage; }
	public void setPage(String page) { iPage = page; }

	@Column(name = "nbr_access", nullable = false)
	public Integer getAccess() { return iAccess; }
	public void setAccess(Integer access) { iAccess = access; }

	@Column(name = "nbr_active", nullable = false)
	public Integer getActive() { return iActive; }
	public void setActive(Integer active) { iActive = active; }

	@Column(name = "nbr_waiting", nullable = false)
	public Integer getWaiting() { return iWaiting; }
	public void setWaiting(Integer waiting) { iWaiting = waiting; }

	@Column(name = "nbr_opened", nullable = false)
	public Integer getOpened() { return iOpened; }
	public void setOpened(Integer opened) { iOpened = opened; }

	@Column(name = "nbr_tracking", nullable = false)
	public Integer getTracking() { return iTracking; }
	public void setTracking(Integer tracking) { iTracking = tracking; }

	@Column(name = "nbr_active1m", nullable = false)
	public Integer getActive1m() { return iActive1m; }
	public void setActive1m(Integer active1m) { iActive1m = active1m; }

	@Column(name = "nbr_active2m", nullable = false)
	public Integer getActive2m() { return iActive2m; }
	public void setActive2m(Integer active2m) { iActive2m = active2m; }

	@Column(name = "nbr_active5m", nullable = false)
	public Integer getActive5m() { return iActive5m; }
	public void setActive5m(Integer active5m) { iActive5m = active5m; }

	@Column(name = "nbr_active10m", nullable = false)
	public Integer getActive10m() { return iActive10m; }
	public void setActive10m(Integer active10m) { iActive10m = active10m; }

	@Column(name = "nbr_active15m", nullable = false)
	public Integer getActive15m() { return iActive15m; }
	public void setActive15m(Integer active15m) { iActive15m = active15m; }

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof AccessStatistics)) return false;
		if (getUniqueId() == null || ((AccessStatistics)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((AccessStatistics)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
	public String toString() {
		return "AccessStatistics["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "AccessStatistics[" +
			"\n	Access: " + getAccess() +
			"\n	Active: " + getActive() +
			"\n	Active10m: " + getActive10m() +
			"\n	Active15m: " + getActive15m() +
			"\n	Active1m: " + getActive1m() +
			"\n	Active2m: " + getActive2m() +
			"\n	Active5m: " + getActive5m() +
			"\n	Host: " + getHost() +
			"\n	Opened: " + getOpened() +
			"\n	Page: " + getPage() +
			"\n	TimeStamp: " + getTimeStamp() +
			"\n	Tracking: " + getTracking() +
			"\n	UniqueId: " + getUniqueId() +
			"\n	Waiting: " + getWaiting() +
			"]";
	}
}
