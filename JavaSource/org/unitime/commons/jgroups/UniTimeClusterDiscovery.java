package org.unitime.commons.jgroups;
/*
 * UniTime 3.5 (University Timetabling Application)
 * Copyright (C) 2013, UniTime LLC, and individual contributors
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.hibernate.Transaction;
import org.jgroups.Address;
import org.jgroups.Event;
import org.jgroups.Global;
import org.jgroups.PhysicalAddress;
import org.jgroups.View;
import org.jgroups.annotations.Property;
import org.jgroups.conf.ClassConfigurator;
import org.jgroups.conf.PropertyConverters;
import org.jgroups.protocols.Discovery;
import org.jgroups.protocols.PingData;
import org.jgroups.stack.IpAddress;
import org.jgroups.util.UUID;
import org.unitime.timetable.model.ClusterDiscovery;
import org.unitime.timetable.model.dao.ClusterDiscoveryDAO;

/**
 * @author Tomas Muller
 */
public class UniTimeClusterDiscovery extends Discovery {
	static {
		ClassConfigurator.addProtocol((short) 666, UniTimeClusterDiscovery.class);
	}
	
    @Property(description="Number of additional ports to be probed for membership. A port_range of 0 does not " +
    	      "probe additional ports. Example: initial_hosts=A[7800] port_range=0 probes A:7800, port_range=1 probes " +
    	      "A:7800 and A:7801")
    protected int port_range=1;

    @Property(name="initial_hosts", description="Comma delimited list of hosts to be contacted for initial membership",
    		converter=PropertyConverters.InitialHosts.class, dependsUpon="port_range", systemProperty=Global.TCPPING_INITIAL_HOSTS)
    protected List<IpAddress> initial_hosts=Collections.EMPTY_LIST;

	@Property(description="Interval (in milliseconds) at which the own Address is written. 0 disables it.")
	protected long interval = 60000;
	
	@Property(description="Interval (in milliseconds) after which an old record is deleted from the cluster discovery table.")
	protected long time_to_live = 600000;
	
	private Future<?> writer_future;
	
	@Override
    public void stop() {
		final Future<?> wf = writer_future;
		if (wf != null) {
			wf.cancel(false);
			writer_future = null;
		}
        try {
        	deleteSelf();   
        } catch (Exception e) {
            log.error("Failed to remove my address from the " + group_addr + " cluster database.");
        }
        super.stop();
    }
	
	@Override
	public void start() throws Exception {
		super.start();
		if (interval > 0) {
			 writer_future = timer.scheduleWithFixedDelay(new WriterTask(), interval, interval, TimeUnit.MILLISECONDS);
		}
	}

	@Override
	public Collection<PhysicalAddress> fetchClusterMembers(String cluster_name) {
        Set<PhysicalAddress> retval = new HashSet<PhysicalAddress>(initial_hosts);
        
		if (!ClusterDiscoveryDAO.isConfigured()) {
			log.info("Hibernate not configured yet, returning initial hosts for " + group_addr + " cluster members.");
			return retval;
		}
		
		List<PingData> members = readAllMembers();

		if (members.isEmpty())
            return retval;

        for(PingData tmp: members) {
            Collection<PhysicalAddress> dests = (tmp != null ? tmp.getPhysicalAddrs() : null);
            if (dests == null) continue;
            for (PhysicalAddress dest: dests) {
                if (dest == null) continue;
                retval.add(dest);
            }
        }
        
        PhysicalAddress my_addr = getAndSavePhysicalAddress();
		
		log.debug("Cluster " + cluster_name + " has members " + retval + " (I am " + my_addr + ")");
		
        return retval;
	}
	
	protected synchronized List<PingData> readAllMembers() {
		List<PingData> members = new ArrayList<PingData>();
		org.hibernate.Session hibSession = ClusterDiscoveryDAO.getInstance().createNewSession();
        Transaction tx = null;
        try {
        	tx = hibSession.beginTransaction();
			long deadline = new Date().getTime() - time_to_live;
			for (ClusterDiscovery cluster: (List<ClusterDiscovery>)hibSession.createQuery("from ClusterDiscovery where clusterName = :clusterName").setString("clusterName", group_addr).list()) {
				if (cluster.getTimeStamp().getTime() >= deadline)
					members.add(deserialize(cluster.getPingData()));
			}
            if (tx != null && tx.isActive()) tx.commit();
		} catch (IllegalStateException e) {
			log.info("Failed to read  all members of cluster " + group_addr + ": " + e.getMessage());
			if (tx!=null && tx.isActive()) tx.rollback();
		} catch (Exception e) {
			if (tx!=null && tx.isActive()) tx.rollback();
		} finally {
			hibSession.close();
		}
		return members;
	}
	
	protected static String addressAsString(Address address) {
        if(address == null)
            return "";
        if(address instanceof UUID)
            return ((UUID) address).toStringLong();
        return address.toString();
    }
	
	protected PhysicalAddress getAndSavePhysicalAddress() {
        PhysicalAddress physical_addr = (PhysicalAddress)down(new Event(Event.GET_PHYSICAL_ADDRESS, local_addr));

        if (!ClusterDiscoveryDAO.isConfigured()) {
			log.info("Hibernate not configured yet, skiping save of physical address for cluster " + group_addr + ".");
			return physical_addr;
		}
		
        List<PhysicalAddress> physical_addrs = Arrays.asList(physical_addr);
		PingData data = new PingData(local_addr, null, false, UUID.get(local_addr), physical_addrs);
		
		updateMyData(data);
		
		return physical_addr;
	}
	
	protected synchronized void updateMyData(PingData data) {
		org.hibernate.Session hibSession = ClusterDiscoveryDAO.getInstance().createNewSession();
		String own_address = addressAsString(data.getAddress());
        Transaction tx = null;
        try {
        	tx = hibSession.beginTransaction();
			ClusterDiscovery cluster = ClusterDiscoveryDAO.getInstance().get(new ClusterDiscovery(own_address, group_addr), hibSession);
			if (cluster == null)
				cluster = new ClusterDiscovery(own_address, group_addr);
			cluster.setPingData(serializeWithoutView(data));
			cluster.setTimeStamp(new Date());
			hibSession.saveOrUpdate(cluster);
			hibSession.flush();
            if (tx != null && tx.isActive()) tx.commit();
		} catch (IllegalStateException e) {
			if (tx!=null && tx.isActive()) tx.rollback();
			log.info("Failed to update my data for cluster " + group_addr + ": " + e.getMessage());
		} catch (Exception e) {
			if (tx!=null && tx.isActive()) tx.rollback();
		} finally {
			hibSession.close();
		}
	}
	
	@Override
	public Object down(Event evt) {
		final Object retval = super.down(evt);
		
		if (evt.getType() == Event.VIEW_CHANGE)
			handleView((View) evt.getArg());
		
		return retval;
	}
	
	protected void handleView(View view) {
		if (!ClusterDiscoveryDAO.isConfigured()) {
			log.info("Hibernate not configured yet, ignoring view change for cluster " + group_addr + ".");
			return;
		}
		
		final Collection<Address> mbrs = view.getMembers();
		final boolean is_coordinator = !mbrs.isEmpty() && mbrs.iterator().next().equals(local_addr);
		if (is_coordinator) {
			purgeOtherAddresses(mbrs);
		}
	}
	
	protected synchronized void purgeOtherAddresses(Collection<Address> members) { 
		org.hibernate.Session hibSession = ClusterDiscoveryDAO.getInstance().createNewSession();
		Transaction tx = null;
		try {
			tx = hibSession.beginTransaction();
			long deadline = new Date().getTime() - time_to_live;
			cluster: for (ClusterDiscovery cluster: (List<ClusterDiscovery>)hibSession.createQuery("from ClusterDiscovery where clusterName = :clusterName").setString("clusterName", group_addr).list()) {
				for (Address address: members)
					if (cluster.getOwnAddress().equals(addressAsString(address))) continue cluster;
				
				PingData pd = deserialize(cluster.getPingData());
				if (pd != null && cluster.getTimeStamp().getTime() < deadline) {
					log.debug("Purging " + pd.getPhysicalAddrs() + " from cluster " + group_addr + ".");
				
					hibSession.delete(cluster);
				}
			}
			hibSession.flush();
            if (tx != null && tx.isActive()) tx.commit();
		} catch (Exception e) {
			if (tx!=null && tx.isActive()) tx.rollback();
		} finally {
			hibSession.close();
		}
	}
	
	protected synchronized void deleteSelf() {
        final String ownAddress = addressAsString(local_addr);
        org.hibernate.Session hibSession = ClusterDiscoveryDAO.getInstance().createNewSession();
        Transaction tx = null;
        try {
        	tx = hibSession.beginTransaction();
        	ClusterDiscovery cluster = ClusterDiscoveryDAO.getInstance().get(new ClusterDiscovery(ownAddress, group_addr), hibSession);
        	if (cluster != null)
        		hibSession.delete(cluster);
        	hibSession.flush();
            if (tx != null && tx.isActive()) tx.commit();
		} catch (Exception e) {
			if (tx!=null && tx.isActive()) tx.rollback();
		} finally {
			hibSession.close();
		}
    }
	
	@Override
	public boolean sendDiscoveryRequestsInParallel() {
		return true;
	}

	@Override
	public boolean isDynamic() {
		return true;
	}
	
	protected final class WriterTask implements Runnable {
		public void run() {
			getAndSavePhysicalAddress();
		}
	}

}
