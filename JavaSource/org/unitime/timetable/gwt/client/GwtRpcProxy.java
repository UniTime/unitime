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
package org.unitime.timetable.gwt.client;

import org.unitime.timetable.gwt.client.page.UniTimeNotifications;

import com.google.gwt.http.client.Request;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.impl.RemoteServiceProxy;
import com.google.gwt.user.client.rpc.impl.RpcStatsContext;
import com.google.gwt.user.client.rpc.impl.Serializer;
import com.google.gwt.user.client.rpc.impl.RequestCallbackAdapter.ResponseReader;

/**
 * @author Tomas Muller
 */
public class GwtRpcProxy extends RemoteServiceProxy {
	
	public GwtRpcProxy(String moduleBaseURL, String remoteServiceRelativePath, String serializationPolicyName, Serializer serializer) {
		super(moduleBaseURL, remoteServiceRelativePath, serializationPolicyName, serializer);
	}
	
	@Override
    protected <T> Request doInvoke(ResponseReader responseReader, final String methodName, RpcStatsContext statsContext, String requestData, final AsyncCallback<T> callback) {
		return super.doInvoke(responseReader, methodName, statsContext, requestData, new AsyncCallback<T>() {
			@Override
			public void onFailure(Throwable caught) {
				UniTimeNotifications.error("Request " + methodName.replace("_Proxy", "") + " failed: " + caught.getMessage(), caught);
				callback.onFailure(caught);
			}
			@Override
			public void onSuccess(T result) {
				callback.onSuccess(result);
			}
		});
	}
}
