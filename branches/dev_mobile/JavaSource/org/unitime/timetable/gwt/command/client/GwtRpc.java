/*
 * UniTime 3.4 - 3.5 (University Timetabling Application)
 * Copyright (C) 2012 - 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.gwt.command.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author Tomas Muller
 */
public class GwtRpc {
	private static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	
	public static <T extends GwtRpcResponse> void execute(GwtRpcRequest<T> request, AsyncCallback<T> callback) {
		RPC.execute(request, callback);
	}
	
	public static <T extends GwtRpcResponse> void execute(GwtRpcRequest<T> request, final CancellableCallback<T> callback) {
		RPC.executeAsync(request, new AsyncCallback<Long>() {
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}

			@Override
			public void onSuccess(Long executionId) {
				callback.onExecution(executionId);
				RPC.waitForResults(executionId, new AsyncCallback<T>() {
					@Override
					public void onFailure(Throwable caught) {
						callback.onFailure(caught);
					}

					@Override
					public void onSuccess(T result) {
						callback.onSuccess(result);
					}
				});
			}
		});
	}
	
	public static void cancel(Long executionId) {
		RPC.cancelExecution(executionId, new AsyncCallback<Boolean>() {
			@Override
			public void onFailure(Throwable caught) {
			}
			@Override
			public void onSuccess(Boolean result) {
			}
		});
	}
	
	public static interface CancellableCallback<T> extends AsyncCallback<T> {
		public void onExecution(Long executionId);
	}

}
