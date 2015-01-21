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
