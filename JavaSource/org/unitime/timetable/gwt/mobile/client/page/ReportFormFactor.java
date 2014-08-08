/*
 * UniTime 3.5 (University Timetabling Application)
 * Copyright (C) 2014, UniTime LLC, and individual contributors
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
package org.unitime.timetable.gwt.mobile.client.page;

import org.unitime.timetable.gwt.command.client.GwtRpcRequest;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseNull;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.googlecode.mgwt.ui.client.MGWT;

/**
 * @author Tomas Muller
 */
public class ReportFormFactor {
	private static final GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	
	public static void report() {
		ReportFormFactorRequest req = new ReportFormFactorRequest();
		req.setFormFactor(MGWT.getFormFactor().isDesktop() ? "desktop" : MGWT.getFormFactor().isPhone() ? "phone" : "tablet");
		RPC.execute(req, new AsyncCallback<GwtRpcResponseNull>() {
			@Override
			public void onFailure(Throwable caught) { }
			@Override
			public void onSuccess(GwtRpcResponseNull result) { }
		});
	}

	public static class ReportFormFactorRequest implements GwtRpcRequest<GwtRpcResponseNull> {
		private String iFormFactor = null;
		
		public ReportFormFactorRequest() {}
		
		public void setFormFactor(String formFactor) { iFormFactor = formFactor; }
		public String getFormFactor() { return iFormFactor; }
		
		@Override
		public String toString() {
			return "form-factor: " + getFormFactor();
		}
	}
}
