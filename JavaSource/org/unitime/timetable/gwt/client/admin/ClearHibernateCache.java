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
