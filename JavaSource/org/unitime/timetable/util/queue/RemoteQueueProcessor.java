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
package org.unitime.timetable.util.queue;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.activation.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.blocks.RpcDispatcher;
import org.jgroups.blocks.mux.MuxRpcDispatcher;
import org.jgroups.util.Rsp;
import org.jgroups.util.RspList;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.solver.jgroups.SolverServerImplementation;

/**
 * @author Tomas Muller
 */
public class RemoteQueueProcessor extends LocalQueueProcessor {
	private static Log sLog = LogFactory.getLog(RemoteQueueProcessor.class);
	
	private RpcDispatcher iDispatcher;
		
	public RemoteQueueProcessor(JChannel channel, short scope) {
		super();
		iDispatcher = new MuxRpcDispatcher(scope, channel, null, null, this);
		sInstance = this;
		start();
	}
	
	public RpcDispatcher getDispatcher() {
		return iDispatcher;
	}
	
	public String generateId() {
		loop: while (true) {
			String id = UUID.randomUUID().toString();
			if (get(id) != null) continue;
			try {
				RspList<QueueItem> ret = iDispatcher.callRemoteMethods(null, "invoke",  new Object[] { "get",  new Class[] { String.class } , new Object[] { id } }, new Class[] { String.class, Class[].class, Object[].class }, SolverServerImplementation.sAllResponses);
				for (Rsp<QueueItem> rsp : ret) {
					if (rsp != null && rsp.getValue() != null)
						continue loop;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return id;
		}
	}
	
	@Override
	public QueueItem add(QueueItem item) {
		item.setAddress(iDispatcher.getChannel().getAddress());
		item.setId(generateId());
		return super.add(item);
	}
	
	@Override
	public DataSource getFile(String id) {
		try {
			DataSource ds = super.getFile(id);
			return ds == null ? null : new ByteArrayDataSource(ds);
		} catch (IOException e) {
			sLog.error("Failed to create data source: " + e.getMessage(), e);
			return null;
		}
	}
	
	public Object invoke(String method, Class[] types, Object[] args) throws Exception {
		try {
			return getClass().getMethod(method, types).invoke(this, args);
		} finally {
			_RootDAO.closeCurrentThreadSessions();
		}
	}
	
	public Object dispatch(Address address, Method method, Object[] args) throws Exception {
		try {
			if ("add".equals(method.getName())) {
				return method.invoke(this, args);
				// return iDispatcher.callRemoteMethod(address, "invoke",  new Object[] { method.getName(), method.getParameterTypes(), args }, new Class[] { String.class, Class[].class, Object[].class }, SolverServerImplementation.sFirstResponse);
			} else if ("getItems".equals(method.getName())) {
				List<QueueItem> items = (List<QueueItem>)method.invoke(this, args);
				if (iDispatcher.getChannel().getView().getMembers().size() > 1) {
					List<Address> other = new ArrayList<Address>(iDispatcher.getChannel().getView().getMembers());
					other.remove(iDispatcher.getChannel().getAddress());
					RspList<List<QueueItem>> ret = iDispatcher.callRemoteMethods(other, "invoke",  new Object[] { method.getName(), method.getParameterTypes(), args }, new Class[] { String.class, Class[].class, Object[].class }, SolverServerImplementation.sAllResponses);
					for (Rsp<List<QueueItem>> rsp : ret) {
						if (rsp != null && rsp.getValue() != null)
							items.addAll(rsp.getValue());
						if (rsp != null && rsp.hasException())
							sLog.error("Excution of queue processor method " + method + " failed: " + rsp.getException().getMessage(), rsp.getException());
					}
					Collections.sort(items);
				}
				return items;
			} else {
				RspList<Object> ret = iDispatcher.callRemoteMethods(null, "invoke",  new Object[] { method.getName(), method.getParameterTypes(), args }, new Class[] { String.class, Class[].class, Object[].class }, SolverServerImplementation.sAllResponses);
				for (Rsp<Object> rsp : ret) {
					if (rsp != null && rsp.getValue() != null)
						return rsp.getValue();
					if (rsp != null && rsp.hasException())
						sLog.error("Excution of queue processor method " + method + " failed: " + rsp.getException().getMessage(), rsp.getException());
				}
				return null;
			}
		} catch (Exception e) {
			sLog.error("Excution of queue processor method " + method + " failed: " + e.getMessage(), e);
			return null;
		} finally {
			_RootDAO.closeCurrentThreadSessions();
		}
	}
}
