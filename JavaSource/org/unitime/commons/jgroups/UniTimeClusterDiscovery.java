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
import org.hibernate.Transaction;
import org.jgroups.Address;
import org.jgroups.conf.ClassConfigurator;
import org.jgroups.protocols.JDBC_PING;
import org.jgroups.protocols.PingData;
import org.jgroups.util.ByteArray;
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
		Transaction tx = hibSession.beginTransaction();
		try {
			ClusterDiscovery cd = hibSession.get(ClusterDiscovery.class, new ClusterDiscoveryId(addressAsString, clustername));
			if (cd != null)
				hibSession.remove(cd);
			tx.commit();
		} catch (Exception e) {
			log.error("%s: failed to remove address %s: %s", clustername, addr, e);
			if (tx.isActive()) tx.rollback();
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
		Transaction tx = hibSession.beginTransaction();
		try {
			hibSession.createMutationQuery(
					"delete ClusterDiscovery where clusterName = :clustername")
				.setParameter("clustername", clustername).executeUpdate();
			tx.commit();
		} catch (Exception e) {
			log.error("%s: failed to remove all: %s", clustername, e);
			if (tx.isActive()) tx.rollback();
        } finally {
			hibSession.close();
		}
	}
	
	@Override
	protected void readAll(List<Address> members, String clustername, Responses responses) {
		if (!HibernateUtil.isConfigured()) return;
		Session hibSession = HibernateUtil.createNewSession();
		Transaction tx = hibSession.beginTransaction();
		try {
			for (ClusterDiscovery cd: hibSession.createQuery(
					"from ClusterDiscovery where clusterName = :clustername", ClusterDiscovery.class)
				.setParameter("clustername", clustername).list()) {
				try {
					List<PingData> data=deserialize(cd.getPingData(), 0, cd.getPingData().length);
					if (data == null || data.isEmpty()) continue;
					for (PingData pd: data) {
						if (members != null && !members.contains(pd.getAddress())) continue;
						responses.addResponse(pd, false);
						if(local_addr != null && !local_addr.equals(pd.getAddress()))
							addDiscoveryResponseToCaches(pd.getAddress(), pd.getLogicalName(), pd.getPhysicalAddr());
					}
				} catch(Exception e) {
                    log.error("%s: failed deserializing row %s: %s; removing it from the table", local_addr, cd.getOwnAddress(), e);
                    hibSession.remove(cd);
                }
			}
			tx.commit();
		} catch (Exception e) {
			log.error("%s: failed to read all: %s", clustername, e);
			if (tx.isActive()) tx.rollback();
		} finally {
			hibSession.close();
		}
	}
	
	@Override
	protected synchronized void writeToDB(PingData data, String clustername, boolean overwrite) {
		if (!HibernateUtil.isConfigured()) return;
		final String ownAddress = addressAsString(data.getAddress());
		final ByteArray serializedPingData = serializeWithoutView(data);
        Session hibSession = HibernateUtil.createNewSession();
        Transaction tx = hibSession.beginTransaction();
        try {
        	ClusterDiscovery cd = ClusterDiscoveryDAO.getInstance().get(new ClusterDiscoveryId(ownAddress, cluster_name), hibSession);
        	if (cd != null) {
        		if (overwrite) {
        			cd.setPingData(serializedPingData.getBytes());
        			cd.setTimeStamp(new Date());
        			hibSession.merge(cd);
        		}
        	} else {
        		cd = new ClusterDiscovery();
        		cd.setClusterName(clustername);
        		cd.setOwnAddress(ownAddress);
        		cd.setPingData(serializedPingData.getBytes());
        		cd.setTimeStamp(new Date());
        		hibSession.persist(cd);
        	}
			tx.commit();
		} catch (Exception e) {
			log.error("%s: failed to update database: %s", clustername, e);
			if (tx.isActive()) tx.rollback();
        } finally {
			hibSession.close();
		}
	}
	
}
