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
package org.unitime.timetable.gwt.client.solver;

import org.unitime.timetable.gwt.command.client.GwtRpcRequest;
import org.unitime.timetable.gwt.command.client.GwtRpcResponse;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtResources;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;

/**
 * @author Tomas Muller
 */
public class SolverAllocatedMemory extends SimplePanel {
	private static final GwtResources RESOURCES =  GWT.create(GwtResources.class);
	private static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	
	public SolverAllocatedMemory() {
		setWidget(new Image(RESOURCES.loading_small()));
	}
	
	
	public void insert(final RootPanel panel) {
		String id = panel.getElement().getInnerText();
		panel.getElement().setInnerText(null);
		panel.add(this);
		panel.setVisible(true);
		RPC.execute(new SolverAllocatedMemoryRpcRequest(id), new AsyncCallback<SolverAllocatedMemoryRpcResponse>() {
			@Override
			public void onFailure(Throwable caught) {
			}

			@Override
			public void onSuccess(SolverAllocatedMemoryRpcResponse result) {
				setWidget(new Label(result.getValue()));
			}
		});
	}
	
	public static class SolverAllocatedMemoryRpcRequest implements GwtRpcRequest<SolverAllocatedMemoryRpcResponse> {
		public String iSolverId;
		
		public SolverAllocatedMemoryRpcRequest() {}
		public SolverAllocatedMemoryRpcRequest(String solverId) {
			setSolverId(solverId);
		}
		
		public String getSolverId() { return iSolverId; }
		public void setSolverId(String solverId) { iSolverId = solverId; }
		
		@Override
		public String toString() { return iSolverId; }
	}
	
	public static class SolverAllocatedMemoryRpcResponse implements GwtRpcResponse {
		private String iValue;
		
		public SolverAllocatedMemoryRpcResponse() {}
		public SolverAllocatedMemoryRpcResponse(String value) {
			setValue(value);
		}
		
		public String getValue() { return iValue; }
		public void setValue(String value) { iValue = value; }

		@Override
		public String toString() { return iValue; }
	}
}
