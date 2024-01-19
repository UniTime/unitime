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
package org.unitime.timetable.gwt.client.access;

import org.unitime.timetable.gwt.command.client.GwtRpcRequest;
import org.unitime.timetable.gwt.command.client.GwtRpcResponse;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * @author Tomas Muller
 */
public class AccessControlInterface {
	
	public static enum Operation implements IsSerializable {
		PING,
		CHECK_ACCESS,
		LOGOUT,
	}
	
	public static class PingRequest implements GwtRpcRequest<PingResponse>{
		private String iPage = null;
		private boolean iActive = false;
		private Operation iOperation = Operation.PING;
		
		public PingRequest() {}
		public PingRequest(String page) { iPage = page; }
		
		public void setPage(String page) { iPage = page; }
		public String getPage() { return iPage; }
		public void setActive(boolean active) { iActive = active; }
		public boolean isActive() { return iActive; }
		public void setOperation(Operation operation) { iOperation = operation; }
		public Operation getOperation() { return iOperation; }
	}
	
	public static class PingResponse implements GwtRpcResponse {
		private boolean iAccess = true;
		private Integer iInactive = null;
		private Integer iQueue = null;
		
		public PingResponse() {}
		
		public boolean isAccess() { return iAccess; }
		public void setAccess(boolean access) { iAccess = access; }
		public Integer getInactive() { return iInactive; }
		public void setInactive(Integer inactive) { iInactive = inactive; }
		public Integer getQueue() { return iQueue; }
		public void setQueue(Integer queue) { iQueue = queue; }
	}
}
