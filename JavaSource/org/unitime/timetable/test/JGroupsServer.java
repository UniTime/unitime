package org.unitime.timetable.test;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

import javax.transaction.TransactionManager;

import net.sf.cpsolver.ifs.util.ToolBox;

import org.infinispan.Cache;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfiguration;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.distexec.DefaultExecutorService;
import org.infinispan.distexec.DistributedCallable;
import org.infinispan.distexec.DistributedExecutorService;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.transaction.LockingMode;
import org.infinispan.transaction.TransactionMode;
import org.infinispan.transaction.lookup.JBossStandaloneJTAManagerLookup;
import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.Message.Flag;
import org.jgroups.blocks.RequestOptions;
import org.jgroups.blocks.ResponseMode;
import org.jgroups.blocks.RpcDispatcher;
import org.jgroups.util.Rsp;
import org.jgroups.util.RspList;
import org.jgroups.util.Util;

import com.google.gwt.dev.util.collect.HashMap;

public class JGroupsServer {
	
	public boolean hello(String name) {
		System.err.println("Hello " + name + "!");
		return true;
	}
	
	public static void test1(String[] args) {
		try {
			JChannel channel = new JChannel();
			final RpcDispatcher dispatcher = new RpcDispatcher(channel, new JGroupsServer());
			final RequestOptions first = new RequestOptions(ResponseMode.GET_FIRST, 0).setFlags(Flag.DONT_BUNDLE, Flag.OOB);
			final RequestOptions all = new RequestOptions(ResponseMode.GET_ALL, 0).setFlags(Flag.DONT_BUNDLE, Flag.OOB);
			channel.connect("UniTime-2X");
			
			for (;;) {
				final String line = Util.readStringFromStdin(": ");
				new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							RspList<Boolean> o = dispatcher.callRemoteMethods(null, "hello", new String[] { line }, new Class[] { String.class }, all);
							for (Rsp<Boolean> x: o.values()) {
								System.out.println("  -- returned[" + line + "]: " + x.getSender() + " [" + x.getValue() + "]");
							}
							Address random = ToolBox.random(o.values()).getSender();
							Boolean x = dispatcher.callRemoteMethod(random, "hello", new String[] { line }, new Class[] { String.class }, first);
							System.out.println("  -- returned[" + line + "-2]: " + random + " [" + x + "]");
						} catch (Exception e) {}
					}
				}).start();
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		try {
			ToolBox.configureLogging();
						
			GlobalConfiguration global = GlobalConfigurationBuilder.defaultClusteredBuilder()
					.transport().addProperty("configurationFile", "jgroups-udp.xml")
					.build();
			Configuration config = new ConfigurationBuilder()
					.clustering().cacheMode(CacheMode.DIST_SYNC)
					.hash().numOwners(2)
					.transaction().transactionManagerLookup(new JBossStandaloneJTAManagerLookup()).transactionMode(TransactionMode.TRANSACTIONAL).lockingMode(LockingMode.PESSIMISTIC)
					.build();
			EmbeddedCacheManager cm = new DefaultCacheManager(global, config);
			
			Cache<Long, String> cache = cm.getCache("test");
			Cache<Long, History> history = cm.getCache("history");
			cache.put(5l, "Test");
			history.put(5l, new History("Test"));
			TransactionManager tm = cache.getAdvancedCache().getTransactionManager();
			tm.setTransactionTimeout(300);
			
			DistributedExecutorService ex = new DefaultExecutorService(cache);
			
			JChannel channel = new JChannel();
			final RpcDispatcher dispatcher = new RpcDispatcher(channel, new JGroupsServer());
			final RequestOptions first = new RequestOptions(ResponseMode.GET_FIRST, 0).setFlags(Flag.DONT_BUNDLE, Flag.OOB);
			final RequestOptions all = new RequestOptions(ResponseMode.GET_ALL, 0).setFlags(Flag.DONT_BUNDLE, Flag.OOB);
			channel.connect("UniTime");
			
			for (;;) {
				try {
					final String line = Util.readStringFromStdin(": ").trim();
					if (line.isEmpty()) {
					} else if ("exit".equals(line)) {
						break;
					} else if ("begin".equals(line)) {
						tm.begin();
					} else if ("commit".equals(line)) {
						tm.commit();
					} else if ("rollback".equals(line)) {
						tm.rollback();
					} else if (line.indexOf(' ') >= 0) {
						Long key = Long.valueOf(line.substring(0, line.indexOf(' ')));
						String value = line.substring(line.indexOf(' ') + 1);
						// if (tm.getTransaction() != null) cache.getAdvancedCache().lock(key);
						if ("?".equals(value)) {
							System.out.println("Retrieved: " + key + ": " + cache.get(key));
						} else {
							cache.put(key, value);
							History h = history.get(key);
							if (h == null)
								history.put(key, new History(value));
							else {
								h.insert(value);
								history.replace(key, h);
							}
							System.out.println("Added: " + key + ": " + value);
						}
					} else {
						Long key = Long.valueOf(line);
						String value = cache.remove(key);
						if (value != null) {
							History h = history.get(key);
							h.insert("NULL");
							history.replace(key, h);
						}
						System.out.println("Removed: " + key);
					}
					System.out.println("Local Entries: " + ToolBox.dict2string(cache, 2));
					System.out.println("Local History: " + ToolBox.dict2string(history, 2));
					
					List<Future<Map<Long, String>>> futures = ex.submitEverywhere(new Test());
					Map<Long, String> map = new HashMap<Long, String>();
					for (Future<Map<Long, String>> future: futures) {
						map.putAll(future.get());
					}
					System.out.println("All Entries: " + ToolBox.dict2string(map, 2));
					
					RspList<Boolean> o = dispatcher.callRemoteMethods(null, "hello", new String[] { line }, new Class[] { String.class }, all);
					for (Rsp<Boolean> x: o.values()) {
						System.out.println("  -- returned[" + line + "]: " + x.getSender() + " [" + x.getValue() + "]");
					}
					Address random = ToolBox.random(channel.getView().getMembers());
					Boolean x = dispatcher.callRemoteMethod(random, "hello", new String[] { line }, new Class[] { String.class }, first);
					System.out.println("  -- returned[" + line + "-2]: " + random + " [" + x + "]");
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			cm.stop();
			channel.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static class Test implements DistributedCallable<Long, String, Map<Long, String>>, Serializable {
		private static final long serialVersionUID = 1L;
		
		transient Cache<Long, String> cache;
		transient Set<Long> keys;
		
		public Test() {}
		
		@Override
		public void setEnvironment(Cache<Long, String> cache, Set<Long> inputKeys) {
			this.cache = cache;
			this.keys = inputKeys;
		}

		@Override
		public Map<Long, String> call() throws Exception {
			System.out.println("Got keys: " + keys);
			System.out.println("Returning local entries: " + ToolBox.dict2string(cache, 2));
			return new HashMap<Long, String>(cache);
		}
	}
	
	public static class History implements Serializable {
		private static final long serialVersionUID = 1L;
		
		List<String> entries = new ArrayList<String>();
		
		public History(String value) { entries.add(value); }
		
		public void insert(String value) {
			entries.add(0, value);
		}
		
		@Override
		public String toString() {
			return entries.toString();
		}
	}
}
