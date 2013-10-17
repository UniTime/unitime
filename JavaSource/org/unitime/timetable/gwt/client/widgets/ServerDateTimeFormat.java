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
package org.unitime.timetable.gwt.client.widgets;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.unitime.timetable.gwt.command.client.GwtRpcRequest;
import org.unitime.timetable.gwt.command.client.GwtRpcResponse;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.TimeZone;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author Tomas Muller
 */
public class ServerDateTimeFormat extends DateTimeFormat {
	protected static final GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	private static TimeZone sServerTimeZone = null;
	private static Integer sTimeZoneOffsetInMinutes = null;
	private static final Map<String, ServerDateTimeFormat> sFormatCache;
	
	static {
		String cookie = Cookies.getCookie("UniTime:ServerTimeZoneOffsetInMinutes");
		if (cookie != null) {
			try {
				sTimeZoneOffsetInMinutes = Integer.parseInt(cookie);
				sServerTimeZone = TimeZone.createTimeZone(sTimeZoneOffsetInMinutes);
			} catch (NumberFormatException e) {}
		}
		if (sServerTimeZone == null) {
			RPC.execute(new ServerTimeZoneRequest(), new AsyncCallback<ServerTimeZoneResponse>() {
				@Override
				public void onFailure(Throwable caught) {}

				@Override
				public void onSuccess(ServerTimeZoneResponse result) {
					sTimeZoneOffsetInMinutes = result.getTimeZoneOffsetInMinutes();
					sServerTimeZone = TimeZone.createTimeZone(sTimeZoneOffsetInMinutes);
					Cookies.setCookie("UniTime:ServerTimeZoneOffsetInMinutes", String.valueOf(result.getTimeZoneOffsetInMinutes()));
				}
			});
		}
		sFormatCache = new HashMap<String, ServerDateTimeFormat>();
	}
	
	public ServerDateTimeFormat(String pattern) {
		super(pattern);
	}
	
	public static TimeZone getServerTimeZone() {
		return sServerTimeZone;
	}
	
	public static Integer getServerTimeZoneOffsetInMinutes() {
		return sTimeZoneOffsetInMinutes;
	}
	
	@SuppressWarnings("deprecation")
	public static Date toLocalDate(Date serverDate) {
		if (serverDate == null || sTimeZoneOffsetInMinutes == null) return serverDate;
		return new Date(serverDate.getTime() + 60000 * (serverDate.getTimezoneOffset() - sTimeZoneOffsetInMinutes));
	}
	
	@SuppressWarnings("deprecation")
	public static Date toServerDate(Date localDate) {
		if (localDate == null || sTimeZoneOffsetInMinutes == null) return localDate;
		return new Date(localDate.getTime() + 60000 * (sTimeZoneOffsetInMinutes - localDate.getTimezoneOffset()));
	}
	
	@Override
	public String format(Date date) {
		return super.format(date, sServerTimeZone);
	}
	
	public static DateTimeFormat getFormat(String pattern) {
		ServerDateTimeFormat format = sFormatCache.get(pattern);
		if (format == null) {
			format = new ServerDateTimeFormat(pattern);
			sFormatCache.put(pattern, format);
		}
		return format;
	}
	
	public static class ServerTimeZoneRequest implements GwtRpcRequest<ServerTimeZoneResponse> {}
	
	public static class ServerTimeZoneResponse implements GwtRpcResponse {
		private int iTimeZoneOffsetInMinutes;
		
		public ServerTimeZoneResponse() {}
		
		public ServerTimeZoneResponse(int timeZoneOffsetInMinutes) {
			iTimeZoneOffsetInMinutes = timeZoneOffsetInMinutes;
		}
		
		public int getTimeZoneOffsetInMinutes() { return iTimeZoneOffsetInMinutes; }
	}
	
}
