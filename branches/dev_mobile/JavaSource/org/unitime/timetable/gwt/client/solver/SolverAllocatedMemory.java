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
