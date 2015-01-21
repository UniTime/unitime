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
package org.unitime.timetable.gwt.client.admin;

import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.command.client.GwtRpcRequest;
import org.unitime.timetable.gwt.command.client.GwtRpcResponse;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtMessages;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author Tomas Muller
 */
public class ClearHibernateCache {
	protected static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	protected static final GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);

	public static void clearHibernateCache() {
		LoadingWidget.getInstance().show(MESSAGES.waitClearHiberanteCache());
		RPC.execute(new ClearHibernateCacheRequest(), new AsyncCallback<ClearHibernateCacheResponse>() {

			@Override
			public void onFailure(Throwable caught) {
				LoadingWidget.getInstance().hide();
				UniTimeNotifications.error(MESSAGES.failedToClearHiberanteCache(caught.getMessage()), caught);
			}

			@Override
			public void onSuccess(ClearHibernateCacheResponse result) {
				LoadingWidget.getInstance().hide();
				UniTimeNotifications.info(MESSAGES.hibernateCacheCleared());
			}
		});
	}
	
	public static native void createTriggers()/*-{
	$wnd.gwtClearHibernateCache = function() {
		@org.unitime.timetable.gwt.client.admin.ClearHibernateCache::clearHibernateCache()();
	};
	}-*/;
	
	public static class ClearHibernateCacheRequest implements GwtRpcRequest<ClearHibernateCacheResponse> {}
	
	public static class ClearHibernateCacheResponse implements GwtRpcResponse {}
}
