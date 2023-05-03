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

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public class ClusterDiscoveryId implements Serializable {
	private static final long serialVersionUID = 1L;

	private String iOwnAddress;
	private String iClusterName;

	public ClusterDiscoveryId() {}

	public ClusterDiscoveryId(String ownAddress, String clusterName) {
		iOwnAddress = ownAddress;
		iClusterName = clusterName;
	}

	public String getOwnAddress() { return iOwnAddress; }
	public void setOwnAddress(String ownAddress) { iOwnAddress = ownAddress; }

	public String getClusterName() { return iClusterName; }
	public void setClusterName(String clusterName) { iClusterName = clusterName; }


	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof ClusterDiscoveryId)) return false;
		ClusterDiscoveryId clusterDiscovery = (ClusterDiscoveryId)o;
		if (getOwnAddress() == null || clusterDiscovery.getOwnAddress() == null || !getOwnAddress().equals(clusterDiscovery.getOwnAddress())) return false;
		if (getClusterName() == null || clusterDiscovery.getClusterName() == null || !getClusterName().equals(clusterDiscovery.getClusterName())) return false;
		return true;
	}

	@Override
	public int hashCode() {
		if (getOwnAddress() == null || getClusterName() == null) return super.hashCode();
		return getOwnAddress().hashCode() ^ getClusterName().hashCode();
	}

}
