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
package org.unitime.commons.jgroups;

import java.util.Date;
import java.util.List;

import org.hibernate.Session;
import org.jgroups.Address;
import org.jgroups.conf.ClassConfigurator;
import org.jgroups.protocols.JDBC_PING;
import org.jgroups.protocols.PingData;
import org.jgroups.util.Responses;
import org.unitime.commons.hibernate.util.HibernateUtil;
import org.unitime.timetable.model.ClusterDiscovery;
import org.unitime.timetable.model.base.ClusterDiscoveryId;
import org.unitime.timetable.model.dao.ClusterDiscoveryDAO;

/**
 * @author Tomas Muller
 */
public class UniTimeClusterDiscovery extends JDBC_PING {
	static {
		ClassConfigurator.addProtocol((short)666, UniTimeClusterDiscovery.class);
	}
	
	@Override
    public void init() throws Exception {
		super.init();		
	}
	
	@Override
	protected void verifyConfigurationParameters() {}
	
	@Override
	public void attemptSchemaInitialization() {}
	
	@Override
	protected void loadDriver() {}
	
	
	@Override
	protected void remove(String clustername, Address addr) {
		if (!HibernateUtil.isConfigured()) return;
        final String addressAsString = addressAsString(addr);
		Session hibSession = HibernateUtil.createNewSession();
		try {
			ClusterDiscovery cd = ClusterDiscoveryDAO.getInstance().get(new ClusterDiscoveryId(addressAsString, clustername), hibSession);
			if (cd != null)
				hibSession.delete(cd);
			hibSession.flush();
		} finally {
			hibSession.close();
		}
	}
	
	@Override
	protected boolean contains(String cluster_name, Address addr) {
		if (!HibernateUtil.isConfigured()) return false;
        final String addressAsString = addressAsString(addr);
        Session hibSession = HibernateUtil.createNewSession();
        try {
        	ClusterDiscovery cd = ClusterDiscoveryDAO.getInstance().get(new ClusterDiscoveryId(addressAsString, cluster_name), hibSession);
        	return (cd != null);
        } finally {
			hibSession.close();
		}
	}
	
	@Override
    protected void removeAll(String clustername) {
		if (!HibernateUtil.isConfigured()) return;
		Session hibSession = HibernateUtil.createNewSession();
		try {
			hibSession.createQuery(
					"delete ClusterDiscovery where clusterName = :clustername")
				.setParameter("clustername", clustername).executeUpdate();
        } finally {
			hibSession.close();
		}
	}
	
	@Override
	protected void readAll(List<Address> members, String clustername, Responses responses) {
		if (!HibernateUtil.isConfigured()) return;
		Session hibSession = HibernateUtil.createNewSession();
		try {
			for (ClusterDiscovery cd: hibSession.createQuery(
					"from ClusterDiscovery where clusterName = :clustername", ClusterDiscovery.class)
				.setParameter("clustername", clustername).list()) {
				try {
					PingData data=deserialize(cd.getPingData());
	                if(data == null || (members != null && !members.contains(data.getAddress())))
	                    continue;
	                responses.addResponse(data, false);
	                if(local_addr != null && !local_addr.equals(data.getAddress()))
	                    addDiscoveryResponseToCaches(data.getAddress(), data.getLogicalName(), data.getPhysicalAddr());
				} catch(Exception e) {
                    log.error("%s: failed deserializing row %s: %s; removing it from the table", local_addr, cd.getOwnAddress(), e);
                    hibSession.delete(cd);
                }
			}
			hibSession.flush();
		} finally {
			hibSession.close();
		}
	}
	
	@Override
	protected synchronized void writeToDB(PingData data, String clustername, boolean overwrite) {
		if (!HibernateUtil.isConfigured()) return;
		final String ownAddress = addressAsString(data.getAddress());
		final byte[] serializedPingData = serializeWithoutView(data);
        Session hibSession = HibernateUtil.createNewSession();
        try {
        	ClusterDiscovery cd = ClusterDiscoveryDAO.getInstance().get(new ClusterDiscoveryId(ownAddress, cluster_name), hibSession);
        	if (cd != null) {
        		if (overwrite) {
        			cd.setPingData(serializedPingData);
        			cd.setTimeStamp(new Date());
        			hibSession.update(cd);
        		}
        	} else {
        		cd = new ClusterDiscovery();
        		cd.setClusterName(clustername);
        		cd.setOwnAddress(ownAddress);
        		cd.setPingData(serializedPingData);
        		cd.setTimeStamp(new Date());
        		hibSession.save(cd);
        	}
        	hibSession.flush();
        } finally {
			hibSession.close();
		}
	}
	
}
