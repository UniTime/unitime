/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2010 - 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.model.base;

import java.io.Serializable;
import java.util.Date;

import org.unitime.timetable.model.ClusterDiscovery;

/**
 * @author Tomas Muller
 */
public abstract class BaseClusterDiscovery implements Serializable {
	private static final long serialVersionUID = 1L;

	private String iOwnAddress;
	private String iClusterName;
	private byte[] iPingData;
	private Date iTimeStamp;


	public static String PROP_PING_DATA = "pingData";
	public static String PROP_TIME_STAMP = "timeStamp";

	public BaseClusterDiscovery() {
		initialize();
	}

	protected void initialize() {}

	public String getOwnAddress() { return iOwnAddress; }
	public void setOwnAddress(String ownAddress) { iOwnAddress = ownAddress; }

	public String getClusterName() { return iClusterName; }
	public void setClusterName(String clusterName) { iClusterName = clusterName; }

	public byte[] getPingData() { return iPingData; }
	public void setPingData(byte[] pingData) { iPingData = pingData; }

	public Date getTimeStamp() { return iTimeStamp; }
	public void setTimeStamp(Date timeStamp) { iTimeStamp = timeStamp; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof ClusterDiscovery)) return false;
		ClusterDiscovery clusterDiscovery = (ClusterDiscovery)o;
		if (getOwnAddress() == null || clusterDiscovery.getOwnAddress() == null || !getOwnAddress().equals(clusterDiscovery.getOwnAddress())) return false;
		if (getClusterName() == null || clusterDiscovery.getClusterName() == null || !getClusterName().equals(clusterDiscovery.getClusterName())) return false;
		return true;
	}

	public int hashCode() {
		if (getOwnAddress() == null || getClusterName() == null) return super.hashCode();
		return getOwnAddress().hashCode() ^ getClusterName().hashCode();
	}

	public String toString() {
		return "ClusterDiscovery[" + getOwnAddress() + ", " + getClusterName() + "]";
	}

	public String toDebugString() {
		return "ClusterDiscovery[" +
			"\n	ClusterName: " + getClusterName() +
			"\n	OwnAddress: " + getOwnAddress() +
			"\n	PingData: " + getPingData() +
			"]";
	}
}
