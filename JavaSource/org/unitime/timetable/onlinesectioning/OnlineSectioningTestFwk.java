/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2011 - 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.onlinesectioning;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.infinispan.manager.EmbeddedCacheManager;
import org.jgroups.JChannel;
import org.jgroups.blocks.locking.LockService;
import org.jgroups.blocks.mux.MuxUpHandler;
import org.unitime.commons.hibernate.util.HibernateUtil;
import org.unitime.commons.jgroups.JGroupsUtils;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.StudentClassEnrollment;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.server.InMemoryServer;
import org.unitime.timetable.onlinesectioning.updates.PersistExpectedSpacesAction;
import org.unitime.timetable.solver.jgroups.DummySolverServer;
import org.unitime.timetable.solver.jgroups.SolverServer;

/**
 * @author Tomas Muller
 */
public abstract class OnlineSectioningTestFwk { 
	protected static Logger sLog = Logger.getLogger(OnlineSectioningTestFwk.class);
	protected static DecimalFormat sDF = new DecimalFormat("0.000");
	private OnlineSectioningServer iServer = null;
	private Pool iTasks;
	private List<Runner> iRunners;
	private Counter iFinished = new Counter(), iExec = new Counter(), iQuality = new Counter();
	private double iT0 = 0;
	private double iRunTime = 0.0;
	private JChannel iChannel = null;
	private SolverServer iSolverServer = null;
	private Long iSessionId = null;
    
	protected void configureLogging() {
        Properties props = new Properties();
        props.setProperty("log4j.rootLogger", "DEBUG, A1");
        props.setProperty("log4j.appender.A1", "org.apache.log4j.ConsoleAppender");
        props.setProperty("log4j.appender.A1.layout", "org.apache.log4j.PatternLayout");
        props.setProperty("log4j.appender.A1.layout.ConversionPattern","%-5p %c{2}: %m%n");
        props.setProperty("log4j.logger.org.hibernate","INFO");
        props.setProperty("log4j.logger.org.hibernate.cfg","WARN");
        props.setProperty("log4j.logger.org.hibernate.cache.EhCacheProvider","ERROR");
        props.setProperty("log4j.logger.org.unitime.commons.hibernate","INFO");
        props.setProperty("log4j.logger.net","INFO");
        props.setProperty("log4j.logger.org.unitime.timetable.onlinesectioning","WARN");
        props.setProperty("log4j.logger.org.unitime.timetable.onlinesectioning.test","INFO");
        props.setProperty("log4j.logger." + OnlineSectioningTestFwk.class.getName(), "INFO");
        props.setProperty("log4j.logger.net.sf.cpsolver.ifs.util.JProf", "INFO");
        props.setProperty("log4j.logger.org.jgroups", "INFO");
        props.setProperty("log4j.logger.net.sf.ehcache.distribution.jgroups", "WARN");
        props.setProperty("log4j.logger.org.hibernate.cache.ehcache.AbstractEhcacheRegionFactory", "ERROR");
        props.setProperty("log4j.logger.net.sf.ehcache.distribution.jgroups.JGroupsCacheReceiver", "ERROR");
        props.setProperty("log4j.logger.org.unitime.timetable.solver.jgroups.DummySolverServer", "INFO");
        PropertyConfigurator.configure(props);
	}
	
	protected void startServer() {
        final Session session = Session.getSessionUsingInitiativeYearTerm(
                ApplicationProperties.getProperty("initiative", "woebegon"),
                ApplicationProperties.getProperty("year","2010"),
                ApplicationProperties.getProperty("term","Fal")
                );
        
        boolean remote = "true".equalsIgnoreCase(ApplicationProperties.getProperty("remote", "true"));

        if (session==null) {
            sLog.error("Academic session not found, use properties initiative, year, and term to set academic session.");
            System.exit(0);
        } else {
            sLog.info("Session: "+session);
        }
        
        iSessionId = session.getUniqueId();
        
        OnlineSectioningLogger.getInstance().setEnabled(false);

        if (remote) {
            try {
            	iChannel = new JChannel(JGroupsUtils.getConfigurator(ApplicationProperties.getProperty("unitime.solver.jgroups.config", "solver-jgroups-tcp.xml")));
            	iChannel.setUpHandler(new MuxUpHandler());
        		
        		iSolverServer = new DummySolverServer(iChannel);
        		
        		iChannel.connect("UniTime:rpc");
        		iChannel.getState(null, 0);
        		
                if (getServer() == null)
                	throw new Exception(session.getLabel() + " is not available");
            } catch (Exception e) {
            	sLog.error("Failed to access the solver server: " + e.getMessage(), e);
            	if (iChannel != null && iChannel.isConnected()) iChannel.disconnect();
            	if (iChannel != null && iChannel.isOpen()) iChannel.close();
            	System.exit(0);
            }
        } else {
            iServer = new InMemoryServer(new OnlineSectioningServerContext() {
    			@Override
    			public boolean isWaitTillStarted() {
    				return true;
    			}
    			
    			@Override
    			public EmbeddedCacheManager getCacheManager() {
    				return null;
    			}
    			
    			@Override
    			public Long getAcademicSessionId() {
    				return session.getUniqueId();
    			}

    			@Override
    			public LockService getLockService() {
    				return null;
    			}
    		});
        }
	}
	
	protected void stopServer() {
		if (iChannel == null && iServer != null) {
			List<Long> offeringIds = iServer.getOfferingsToPersistExpectedSpaces(0);
			if (!offeringIds.isEmpty())
				iServer.execute(new PersistExpectedSpacesAction(offeringIds), user());
			iServer.unload();
		}
		iServer = null;
	}
	
	protected void close() {
    	if (iChannel != null && iChannel.isConnected()) iChannel.disconnect();
    	if (iChannel != null && iChannel.isOpen()) iChannel.close();
		OnlineSectioningLogger.stopLogger();
		HibernateUtil.closeHibernate();
	}
	
	public OnlineSectioningServer getServer() {
		if (iServer != null) return iServer;
		return iSolverServer.getOnlineStudentSchedulingContainer().getSolver(iSessionId.toString());
	}
	
	public interface Operation {
		public double execute(OnlineSectioningServer s);
	}	
	
	public class Runner implements Runnable {
		public void run() {
			Operation op = null; 
			while ((op = iTasks.next()) != null) {
				long t0 = System.currentTimeMillis();
				try {
					double val = op.execute(getServer());
					iQuality.inc(val);
				} catch (Throwable t) {
					sLog.warn("Task failed: " + t.getMessage(), t);
				} finally {
					iFinished.inc(1);
				}
				long t1 = System.currentTimeMillis();
				iRunTime = (t1 - iT0) / 1000.0;
				iExec.inc(t1 - t0);
			}
		}
	}
	
	public static class Pool {
		private Iterator<Operation> iIterator;
		private int iCount;
		
		public Pool(List<Operation> operations) {
			iIterator = operations.iterator();
			iCount = 0;
		}
		
		public synchronized Operation next() {
			if (iIterator.hasNext()) {
				iCount++;
				return iIterator.next();
			}
			return null;
		}
		
		public synchronized int count() {
			return iCount;
		}
	}
	
	public static class Counter {
		private double iValue = 0;
		private int iCount = 0;
		
		public synchronized void inc(double val) {
			iValue += val; iCount ++;
		}
		
		public synchronized int count() {
			return iCount;
		}

		public synchronized double value() {
			return iValue;
		}
		
		public synchronized void clear() {
			iValue = 0.0; iCount = 0;
		}
	}
	
	public int nrFinished() {
		return iFinished.count();
	}
	
	public double testRunTimeInSeconds() {
		return iRunTime;
	}
	
	public int nrConcurrent() {
		return iRunners.size();
	}
	
	public double totalExecutionTimeInSeconds() {
		return iExec.value() / 1000.0;
	}
	
	public double averageQuality() {
		return iQuality.value() / iQuality.count();
	}
	
	public String toString() {
		return nrFinished() + " tasks finished (" + nrConcurrent() + " in parallel)." +
			" Running took " + sDF.format(testRunTimeInSeconds()) + " s," +
			" throughput " + sDF.format(nrFinished() / testRunTimeInSeconds()) + " tasks / s," +
			" wait " + sDF.format(totalExecutionTimeInSeconds() / nrFinished()) + " s / task," +
			" quality " + sDF.format(100.0 * averageQuality()) + "% on average";
	}

	public synchronized void run(List<Operation> operations, int nrConcurrent) {
		sLog.info("Running " + operations.size() + " tasks...");
		iRunners = new ArrayList<Runner>();
		iTasks = new Pool(operations);
		iFinished.clear(); iExec.clear(); iQuality.clear();
		iT0 = System.currentTimeMillis();
		for (int i = 0; i < nrConcurrent; i++) {
			Runner r = new Runner();
			Thread t = new Thread(r);
			t.setDaemon(true); t.setName("Runner #" + (1 + i));
			t.start();
			iRunners.add(r);
		}
		do {
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				break;
			}
			sLog.info(toString());
		} while (nrFinished() < operations.size());
		sLog.info("All " + toString());
	}
	
	public abstract List<Operation> operations();
	
	public OnlineSectioningLog.Entity user() {
		return OnlineSectioningLog.Entity.newBuilder()
			.setExternalId(StudentClassEnrollment.SystemChange.TEST.name())
			.setName(StudentClassEnrollment.SystemChange.TEST.getName())
			.setType(OnlineSectioningLog.Entity.EntityType.OTHER).build();
	}
	
	public void test(int nrTasks, int... nrConcurrent) {
		try {
			configureLogging();
			
	        HibernateUtil.configureHibernate(ApplicationProperties.getProperties());
	        
			startServer();
			
			List<Operation> operations = operations();
			
			Collections.shuffle(operations);
			
			for (int c: nrConcurrent) {
				run(nrTasks <= 0 || operations.size() <= nrTasks ? operations : operations.subList(0, nrTasks), c);
			}
			
			stopServer();
		} catch (Exception e) {
			sLog.fatal("Test failed: " + e.getMessage(), e);
		} finally {
			close();
		}
	}

}