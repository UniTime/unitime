package org.unitime.commons.jgroups;
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Future;

import org.hibernate.Transaction;
import org.jgroups.Address;
import org.jgroups.Event;
import org.jgroups.Global;
import org.jgroups.Message;
import org.jgroups.PhysicalAddress;
import org.jgroups.View;
import org.jgroups.annotations.ManagedAttribute;
import org.jgroups.annotations.ManagedOperation;
import org.jgroups.annotations.Property;
import org.jgroups.conf.ClassConfigurator;
import org.jgroups.conf.PropertyConverters;
import org.jgroups.protocols.Discovery;
import org.jgroups.protocols.PingData;
import org.jgroups.protocols.PingHeader;
import org.jgroups.util.BoundedList;
import org.jgroups.util.Responses;
import org.jgroups.util.TimeScheduler;
import org.jgroups.util.Tuple;
import org.jgroups.util.UUID;
import org.jgroups.util.Util;
import org.unitime.timetable.model.ClusterDiscovery;
import org.unitime.timetable.model.dao.ClusterDiscoveryDAO;

/**
 * @author Tomas Muller
 */
public class UniTimeClusterDiscovery extends Discovery {
	static {
		ClassConfigurator.addProtocol((short) 666, UniTimeClusterDiscovery.class);
	}
	
    protected Future<?> info_writer;

    @Override
    public boolean isDynamic() {return true;}

    @ManagedAttribute(description="Whether the InfoWriter task is running")
    public synchronized boolean isInfoWriterRunning() {return info_writer != null && !info_writer.isDone();}

    @ManagedOperation(description="Causes the member to write its own information into the DB, replacing an existing entry")
    public void writeInfo() {writeOwnInformation();}
    
    @Property(description="Removes the table contents a view change. Enabling this can help removing crashed members " +
    	      "that are still in the table, but generates more DB traffic")
    protected boolean clear_table_on_view_change=true;
    
    @Property(description="The max number of times my own information should be written to the DB after a view change")
    protected int info_writer_max_writes_after_view=5;
    
    @Property(description="Interval (in ms) at which the info writer should kick in")
    protected long info_writer_sleep_time=10000;
    
    @Property(description="Number of additional ports to be probed for membership. A port_range of 0 does not " +
    	      "probe additional ports. Example: initial_hosts=A[7800] port_range=0 probes A:7800, port_range=1 probes " +
    	      "A:7800 and A:7801")
    private int port_range=1;

    @Property(name="initial_hosts", description="Comma delimited list of hosts to be contacted for initial membership",
	        converter=PropertyConverters.InitialHosts.class, dependsUpon="port_range",
	        systemProperty=Global.TCPPING_INITIAL_HOSTS)
	private List<PhysicalAddress> initial_hosts=Collections.emptyList();

    @Property(description="max number of hosts to keep beyond the ones in initial_hosts")
    protected int max_dynamic_hosts=2000;
    
    protected BoundedList<PhysicalAddress> dynamic_hosts;
    
    public List<PhysicalAddress> getInitialHosts() {
        return initial_hosts;
    }

    public void setInitialHosts(List<PhysicalAddress> initial_hosts) {
        this.initial_hosts=initial_hosts;
    }

    public int getPortRange() {
        return port_range;
    }

    public void setPortRange(int port_range) {
        this.port_range=port_range;
    }

    @ManagedAttribute
    public String getDynamicHostList() {
        return dynamic_hosts.toString();
    }

    @ManagedOperation
    public void clearDynamicHostList() {
        dynamic_hosts.clear();
    }

    @ManagedAttribute
    public String getInitialHostsList() {
        return initial_hosts.toString();
    }

    public void init() throws Exception {
        super.init();
        dynamic_hosts=new BoundedList<PhysicalAddress>(max_dynamic_hosts);
    }

    @Override
    public void stop() {
        stopInfoWriter();
        deleteSelf();
        super.stop();
    }

    @Override
    public Object down(Event evt) {
        switch(evt.getType()) {
            case Event.VIEW_CHANGE:
                View old_view=view;
                boolean previous_coord=is_coord;
            	Object retval=super.down(evt);
                View new_view = (View)evt.getArg();
                handleView(new_view, old_view, previous_coord != is_coord);
                for(Address logical_addr: members) {
                    PhysicalAddress physical_addr=(PhysicalAddress)down_prot.down(new Event(Event.GET_PHYSICAL_ADDRESS, logical_addr));
                    if(physical_addr != null && !initial_hosts.contains(physical_addr)) {
                        dynamic_hosts.addIfAbsent(physical_addr);
                    }
                }
                return retval;
            case Event.SET_PHYSICAL_ADDRESS:
            	retval=super.down(evt);
                Tuple<Address,PhysicalAddress> tuple=(Tuple<Address,PhysicalAddress>)evt.getArg();
                PhysicalAddress physical_addr=tuple.getVal2();
                if(physical_addr != null && !initial_hosts.contains(physical_addr))
                	dynamic_hosts.addIfAbsent(physical_addr);
                return retval;
        }
        return super.down(evt);
    }
    
    @Override
    public void discoveryRequestReceived(Address sender, String logical_name, PhysicalAddress physical_addr) {
        super.discoveryRequestReceived(sender, logical_name, physical_addr);
        if(physical_addr != null) {
            if(!initial_hosts.contains(physical_addr))
                dynamic_hosts.addIfAbsent(physical_addr);
        }
    }

    @Override
    public void findMembers(final List<Address> members, final boolean initial_discovery, Responses responses) {
    	PhysicalAddress physical_addr=(PhysicalAddress)down(new Event(Event.GET_PHYSICAL_ADDRESS, local_addr));

        // https://issues.jboss.org/browse/JGRP-1670
        PingData data=new PingData(local_addr, false, org.jgroups.util.UUID.get(local_addr), physical_addr);
        PingHeader hdr=new PingHeader(PingHeader.GET_MBRS_REQ).clusterName(cluster_name);

        List<PhysicalAddress> cluster_members=new ArrayList<PhysicalAddress>(initial_hosts.size() + (dynamic_hosts != null? dynamic_hosts.size() : 0) + 5);
        for(PhysicalAddress phys_addr: initial_hosts)
            if(!cluster_members.contains(phys_addr))
                cluster_members.add(phys_addr);
        if(dynamic_hosts != null) {
            for(PhysicalAddress phys_addr : dynamic_hosts)
                if(!cluster_members.contains(phys_addr))
                    cluster_members.add(phys_addr);
        }

        if(use_disk_cache) {
            // this only makes sense if we have PDC below us
            Collection<PhysicalAddress> list=(Collection<PhysicalAddress>)down_prot.down(new Event(Event.GET_PHYSICAL_ADDRESSES));
            if(list != null)
                for(PhysicalAddress phys_addr: list)
                    if(!cluster_members.contains(phys_addr))
                        cluster_members.add(phys_addr);
        }

        for(final PhysicalAddress addr: cluster_members) {
            if(physical_addr != null && addr.equals(physical_addr)) // no need to send the request to myself
                continue;

            // the message needs to be DONT_BUNDLE, see explanation above
            final Message msg=new Message(addr).setFlag(Message.Flag.INTERNAL, Message.Flag.DONT_BUNDLE, Message.Flag.OOB)
              .putHeader(this.id,hdr).setBuffer(marshal(data));

            if(async_discovery_use_separate_thread_per_request) {
                timer.execute(new Runnable() {
                    public void run() {
                        log.trace("%s: sending discovery request to %s", local_addr, msg.getDest());
                        down_prot.down(new Event(Event.MSG, msg));
                    }
                });
            }
            else {
                log.trace("%s: sending discovery request to %s", local_addr, msg.getDest());
                down_prot.down(new Event(Event.MSG, msg));
            }
        }
        
        readAll(members, responses);
        update(new PingData(local_addr, is_server, UUID.get(local_addr), physical_addr).coord(is_coord));
    }

    // remove all files which are not from the current members
    protected void handleView(View new_view, View old_view, boolean coord_changed) {
        if(is_coord) {
            if(clear_table_on_view_change)
                clearTable();
            else if(old_view != null && new_view != null) {
                Address[][] diff=View.diff(old_view, new_view);
                Address[] left_mbrs=diff[1];
                for(Address left_mbr : left_mbrs)
                    if(left_mbr != null && !new_view.containsMember(left_mbr))
                        remove(left_mbr);
            }
        }
        if(coord_changed || clear_table_on_view_change)
            writeOwnInformation(); // write immediately
        if(info_writer_max_writes_after_view > 0)
            startInfoWriter(); // and / or write in the background
    }

    protected void writeOwnInformation() {
        PhysicalAddress physical_addr=(PhysicalAddress)down(new Event(Event.GET_PHYSICAL_ADDRESS, local_addr));
        update(new PingData(local_addr, is_server, UUID.get(local_addr), physical_addr).coord(is_coord));
    }

    protected synchronized void startInfoWriter() {
        if(info_writer == null || info_writer.isDone())
            info_writer=timer.scheduleWithDynamicInterval(new InfoWriter(info_writer_max_writes_after_view, info_writer_sleep_time));
    }

    protected synchronized void stopInfoWriter() {
        if(info_writer != null)
            info_writer.cancel(false);
    }

    /** Class which calls writeOwnInformation a few times. Started after each view change */
    protected class InfoWriter implements TimeScheduler.Task {
        protected final int  max_writes;
        protected int        num_writes;
        protected final long sleep_interval;

        public InfoWriter(int max_writes, long sleep_interval) {
            this.max_writes=max_writes;
            this.sleep_interval=sleep_interval;
        }

        @Override
        public long nextInterval() {
            if(++num_writes > max_writes)
                return 0; // discontinues this task
            return Math.max(1000, Util.random(sleep_interval));
        }

        @Override
        public void run() {
            if(!contains(local_addr))
                writeOwnInformation();
        }
    }
    
    protected void deleteSelf() {
        remove(local_addr);
    }
    
    protected void readAll(List<Address> members, Responses responses) {
    	if (!ClusterDiscoveryDAO.isConfigured()) return;
		org.hibernate.Session hibSession = ClusterDiscoveryDAO.getInstance().createNewSession();
        Transaction tx = null;
        try {
        	tx = hibSession.beginTransaction();
			for (ClusterDiscovery cluster: (List<ClusterDiscovery>)hibSession.createQuery("from ClusterDiscovery where clusterName = :clusterName").setString("clusterName", cluster_name).list()) {
				try {
					PingData data = deserialize(cluster.getPingData());
					if(data == null || (members != null && !members.contains(data.getAddress())))
                        continue;
					responses.addResponse(data, false);
                    if(local_addr != null && !local_addr.equals(data.getAddress()))
                        addDiscoveryResponseToCaches(data.getAddress(), data.getLogicalName(), data.getPhysicalAddr());
				} catch (Exception e) {
					hibSession.delete(cluster);
                }
			}
            if (tx != null) tx.commit();
		} catch (IllegalStateException e) {
			log.info("Failed to read all members of cluster " + cluster_name + ": " + e.getMessage());
			if (tx!=null) tx.rollback();
		} catch (Exception e) {
			if (tx!=null) tx.rollback();
		} finally {
			hibSession.close();
		}
    }
    
    protected boolean contains(Address addr) {
    	if (!ClusterDiscoveryDAO.isConfigured()) return false;
    	org.hibernate.Session hibSession = ClusterDiscoveryDAO.getInstance().createNewSession();
		String own_address = addressAsString(addr);
        try {
        	ClusterDiscovery cluster = ClusterDiscoveryDAO.getInstance().get(new ClusterDiscovery(own_address, cluster_name), hibSession);
        	return cluster != null;
        } catch (Exception e) {
			log.info("Failed to read data for cluster " + cluster_name + ": " + e.getMessage());
			return false;
		} finally {
			hibSession.close();
		}
    }
    
    protected void remove(Address addr) {
    	if (!ClusterDiscoveryDAO.isConfigured()) return;
    	org.hibernate.Session hibSession = ClusterDiscoveryDAO.getInstance().createNewSession();
		String own_address = addressAsString(addr);
		Transaction tx = null;
        try {
        	tx = hibSession.beginTransaction();
        	ClusterDiscovery cluster = ClusterDiscoveryDAO.getInstance().get(new ClusterDiscovery(own_address, cluster_name), hibSession);
        	if (cluster != null)
        		hibSession.delete(cluster);
        	hibSession.flush();
            if (tx != null) tx.commit();
        } catch (Exception e) {
			if (tx != null) tx.rollback();
			log.info("Failed to delete data for cluster " + cluster_name + ": " + e.getMessage());
		} finally {
			hibSession.close();
		}
    }
    
	protected void update(PingData data) {
		if (!ClusterDiscoveryDAO.isConfigured()) return;
		org.hibernate.Session hibSession = ClusterDiscoveryDAO.getInstance().createNewSession();
		String own_address = addressAsString(data.getAddress());
        Transaction tx = null;
        try {
        	tx = hibSession.beginTransaction();
			ClusterDiscovery cluster = ClusterDiscoveryDAO.getInstance().get(new ClusterDiscovery(own_address, cluster_name), hibSession);
			if (cluster == null)
				cluster = new ClusterDiscovery(own_address, cluster_name);
			cluster.setPingData(serializeWithoutView(data));
			cluster.setTimeStamp(new Date());
			hibSession.saveOrUpdate(cluster);
			hibSession.flush();
            if (tx != null) tx.commit();
		} catch (Exception e) {
			if (tx!=null) tx.rollback();
			log.info("Failed to update my data for cluster " + cluster_name + ": " + e.getMessage());
		} finally {
			hibSession.close();
		}
	}
	
	protected void clearTable() {
		if (!ClusterDiscoveryDAO.isConfigured()) return;
		org.hibernate.Session hibSession = ClusterDiscoveryDAO.getInstance().createNewSession();
		Transaction tx = null;
        try {
        	tx = hibSession.beginTransaction();
        	hibSession.createQuery("delete ClusterDiscovery where clusterName = :clusterName").setString("clusterName", cluster_name).executeUpdate();
        	if (tx != null) tx.commit();
        } catch (Exception e) {
			if (tx!=null) tx.rollback();
			log.info("Failed to clear data for cluster " + cluster_name + ": " + e.getMessage());
        } finally {
        	hibSession.close();
        }
	}
}
