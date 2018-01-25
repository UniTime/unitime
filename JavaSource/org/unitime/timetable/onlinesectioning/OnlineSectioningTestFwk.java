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
package org.unitime.timetable.onlinesectioning;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.cpsolver.ifs.util.CSVFile;
import org.cpsolver.ifs.util.CSVFile.CSVField;
import org.infinispan.manager.EmbeddedCacheManager;
import org.jgroups.JChannel;
import org.jgroups.blocks.locking.LockService;
import org.jgroups.blocks.mux.MuxUpHandler;
import org.unitime.commons.hibernate.util.HibernateUtil;
import org.unitime.commons.jgroups.JGroupsUtils;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.StudentClassEnrollment;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.reports.OnlineSectioningReport.Counter;
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
	protected OnlineSectioningServer iServer = null;
	private Pool iTasks;
	private List<Runner> iRunners;
	private SynchronizedCounter iFinished = new SynchronizedCounter(), iExec = new SynchronizedCounter(), iQuality = new SynchronizedCounter();
	private double iT0 = 0;
	private double iRunTime = 0.0;
	private JChannel iChannel = null;
	protected SolverServer iSolverServer = null;
	protected Long iSessionId = null;
	private Map<String, Counter> iCounters = new Hashtable<String, Counter>();
	private Map<String, Map<String, Map<String, Counter>>> iReports = new Hashtable<String, Map<String,Map<String,Counter>>>();
    
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
        props.setProperty("log4j.logger.org.cpsolver.ifs.util.JProf", "INFO");
        props.setProperty("log4j.logger.org.jgroups", "INFO");
        props.setProperty("log4j.logger.net.sf.ehcache.distribution.jgroups", "WARN");
        props.setProperty("log4j.logger.org.hibernate.cache.ehcache.AbstractEhcacheRegionFactory", "ERROR");
        props.setProperty("log4j.logger.net.sf.ehcache.distribution.jgroups.JGroupsCacheReceiver", "ERROR");
        props.setProperty("log4j.logger.org.unitime.timetable.solver.jgroups.DummySolverServer", "INFO");
        PropertyConfigurator.configure(props);
	}
	
	public <X extends OnlineSectioningAction> X createAction(Class<X> clazz) {
		return getServer().createAction(clazz);
	}
	
	protected void startServer() {
		final Session session = Session.getSessionUsingInitiativeYearTerm(
                ApplicationProperties.getProperty("initiative", "woebegon"),
                ApplicationProperties.getProperty("year","2010"),
                ApplicationProperties.getProperty("term","Fal")
                );
        
        boolean remote = "true".equalsIgnoreCase(ApplicationProperties.getProperty("remote", "false"));

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
            	iChannel = new JChannel(JGroupsUtils.getConfigurator(ApplicationProperty.SolverClusterConfiguration.value()));
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
    				return false;
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
				iServer.execute(iServer.createAction(PersistExpectedSpacesAction.class).forOfferings(offeringIds), user());
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
	
	public static class SynchronizedCounter {
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
		iCounters.clear();
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
			logCounters();
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
			
			while (!getServer().isReady()) {
				sLog.info("Waiting for the server to load...");
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					break;
				}
			}
			
			List<Operation> operations = operations();
			
			Collections.shuffle(operations);
			
			for (int c: nrConcurrent) {
				run(nrTasks <= 0 || operations.size() <= nrTasks ? operations : operations.subList(0, nrTasks), c);
			}
			
			logCounters();
			
			writeReports();
			
			stopServer();
		} catch (Exception e) {
			sLog.fatal("Test failed: " + e.getMessage(), e);
		} finally {
			close();
		}
	}
	
	public double inc(String counter, double value) {
		synchronized (iCounters) {
			Counter cnt = iCounters.get(counter);
			if (cnt == null) {
				cnt = new Counter();
				iCounters.put(counter, cnt);
			}
			cnt.inc(value);
			return cnt.sum();
		}
	}
	
	public void inc(String report, String record, String property, double value) {
		synchronized (iReports) {
			Map<String, Map<String, Counter>> table = iReports.get(report);
			if (table == null) {
				table = new Hashtable<String, Map<String,Counter>>();
				iReports.put(report, table);
			}
			Map<String, Counter> line = table.get(record);
			if (line == null) {
				line = new Hashtable<String, Counter>();
				table.put(record, line);
			}
			Counter counter = line.get(property);
			if (counter == null) {
				counter = new Counter();
				line.put(property, counter);
			}
			counter.inc(value);
		}
	}
	
	protected void logCounters() {
		synchronized (iCounters) {
			for (String name: new TreeSet<String>(iCounters.keySet())) {
				sLog.info("  " + name + ": " + iCounters.get(name));
			}
		}
	}
		
	protected void writeReports() {
		synchronized (iReports) {
			for (Map.Entry<String, Map<String, Map<String, Counter>>> report: iReports.entrySet()) {
				TreeSet<String> rows = new TreeSet<String>(report.getValue().keySet());
				TreeSet<String> cols = new TreeSet<String>();
				CSVFile csv = new CSVFile();
				for (Map.Entry<String, Map<String, Counter>> record: report.getValue().entrySet()) {
					cols.addAll(record.getValue().keySet());
				}
				List<CSVField> header = new ArrayList<CSVField>();
				header.add(new CSVField(report.getKey()));
				for (String col: cols)
					header.add(new CSVField(col));
				csv.setHeader(header);
				for (String row: rows) {
					List<CSVField> line = new ArrayList<CSVField>();
					line.add(new CSVField(row));
					Map<String, Counter> table = report.getValue().get(row);
					for (String col: cols) {
						Counter counter = table.get(col);
						line.add(new CSVField(counter == null ? "" : String.valueOf(counter.sum())));
					}
					csv.addLine(line);
				}
				try {
					File output = new File(report.getKey() + ".csv");
					sLog.info("Writing " + output + " ...");
					csv.save(output);
				} catch (IOException e) {
					sLog.error("Unable to write report " + report.getKey() + ": " + e.getMessage(), e);
				}
			}			
		}
	}
}