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

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.HasRpcToken;

public class XsrfWrapper extends XsrfBaseWrapper implements GwtRpcServiceAsync {
	GwtRpcServiceAsync iRPC;
	
	public XsrfWrapper(GwtRpcServiceAsync rpc) {
		super((HasRpcToken) rpc);
		iRPC = rpc;
	}

	@Override
	public <T extends GwtRpcResponse> void execute(final GwtRpcRequest<T> request, final AsyncCallback<T> callback) throws GwtRpcException {
		checkToken(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				iRPC.execute(request, callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	@Override
	public <T extends GwtRpcResponse> void executeAsync(final GwtRpcRequest<T> request, final AsyncCallback<Long> callback) throws GwtRpcException {
		checkToken(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				iRPC.executeAsync(request, callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	@Override
	public <T extends GwtRpcResponse> void waitForResults(final Long executionId, final AsyncCallback<T> callback) throws GwtRpcException {
		checkToken(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				iRPC.waitForResults(executionId, callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	@Override
	public void cancelExecution(final Long executionId, final  AsyncCallback<Boolean> callback) throws GwtRpcException {
		checkToken(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
					iRPC.cancelExecution(executionId, callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}
}
