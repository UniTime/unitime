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
package org.unitime.commons.hibernate.connection;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cpsolver.ifs.util.ToolBox;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;

/**
 * @author Tomas Muller
 */
public class LoggingConnectionProvider implements DisposableConnectionProvider {
	private static final long serialVersionUID = 1L;
	private List<Lease> iLeases = new ArrayList<Lease>();
	private LeasedConnectionsLogger iLogger = null;
	private ConnectionProvider iConnectionProvider;
	
	public LoggingConnectionProvider(ConnectionProvider provider) {
		iConnectionProvider = provider;
		iLogger = new LeasedConnectionsLogger();
		iLogger.start();
	}
	
	@Override
	public Connection getConnection() throws SQLException {
		Connection connection = iConnectionProvider.getConnection();
		synchronized (iLeases) {
			iLeases.add(new Lease(connection));
		}
		return connection;
	}
	
	@Override
	public void closeConnection(Connection connection) throws SQLException {
		synchronized (iLeases) {
			for (Iterator<Lease> i = iLeases.iterator(); i.hasNext(); ) {
				Lease lease = i.next();
				if (lease.getConnection().equals(connection))
					i.remove();
			}
		}
		iConnectionProvider.closeConnection(connection);
	}
	
	@Override
	public boolean supportsAggressiveRelease() {
		return iConnectionProvider.supportsAggressiveRelease();
	}
	
	@Override
	public void destroy() {
		iLogger.interrupt();
	}
	
	public static class Lease {
		private static final DecimalFormat sDF = new DecimalFormat("#,##0.00");
		private Connection iConnection;
		private Thread iThread;
		private StackTraceElement[] iTrace;
		private long iTimeStamp;
		
		public Lease(Connection connection) {
			iConnection = connection;
			iThread = Thread.currentThread();
			iTrace = Thread.currentThread().getStackTrace();
			iTimeStamp = System.currentTimeMillis();
		}
		
		public Connection getConnection() {
			return iConnection;
		}
		
		public boolean equals(Object o) {
			if (o == null) return false;
			if (o instanceof Connection)
				return getConnection().equals(o);
			if (o instanceof Lease)
				return getConnection().equals(((Lease)o).getConnection());
			return false;
		}
		
		public double getLeaseTime() {
			return (System.currentTimeMillis() - iTimeStamp) / 1000.0;
		}
		
		public Thread.State getState() {
			return iThread.getState();
		}
		
		public String getName() {
			return iThread.getName();
		}
		
		public String getStackTrace() {
			int first = 0;
			for (int i = 3; i < iTrace.length; i++)
				if (iTrace[i].getClassName().startsWith("org.unitime.") && !iTrace[i].getClassName().endsWith("._BaseRootDAO")) { first = i; break; }
			StringBuffer ret = new StringBuffer();
			for (int i = first; i < iTrace.length; i++)
				ret.append("\n  " + iTrace[i]);
			return ret.toString();
		}
		
		public String toString() {
			StackTraceElement trace = null;
			for (int i = 3; i < iTrace.length; i++)
				if (iTrace[i].getClassName().startsWith("org.unitime.") && !iTrace[i].getClassName().endsWith("._BaseRootDAO")) { trace = iTrace[i]; break; }
			return sDF.format(getLeaseTime()) + " " + getState() + " " + getName() + " " + trace;
		}
	}
	
	public class LeasedConnectionsLogger extends Thread {
		private Log iLog = LogFactory.getLog(LeasedConnectionsLogger.class);

		private boolean iActive = true;
		
		public LeasedConnectionsLogger() {
			super("LeasedConnectionsLogger");
			setDaemon(true);
		}
		
		@Override
		public void run() {
			iLog.info("Database connection pool logging is enabled.");
			while (iActive) {
				try {
					try {
						sleep(60000);
					} catch (InterruptedException e) {}
					synchronized (iLeases) {
						List<Lease> suspicious = new ArrayList<Lease>();
						for (Lease lease: iLeases)
							if (lease.getLeaseTime() > 60.0 || lease.getState() == State.TERMINATED)
								suspicious.add(lease);
						if (!suspicious.isEmpty())
							iLog.warn("Suspicious leases:" + ToolBox.col2string(iLeases, 2));
						for (Lease lease: suspicious)
							if (lease.getState() == State.TERMINATED) {
								iLog.fatal("Releasing connection of a terminated thread " + lease.getName() + "." + lease.getStackTrace());
								closeConnection(lease.getConnection());
							}
					}
				} catch (Exception e) {
					iLog.warn("Logging failed: " + e.getMessage(), e);
				}
			}
		}
		
		@Override
		public void interrupt() {
			iActive = false;
			super.interrupt();
			try { join(); } catch (InterruptedException e) {}
		}
	}

	@Override
	public boolean isUnwrappableAs(Class clazz) {
		return iConnectionProvider.isUnwrappableAs(clazz);
	}

	@Override
	public <T> T unwrap(Class<T> clazz) {
		return iConnectionProvider.unwrap(clazz);
	}
}
