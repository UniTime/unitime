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
package org.unitime.timetable.gwt.client.offerings;

import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.command.client.GwtRpcRequest;
import org.unitime.timetable.gwt.command.client.GwtRpcResponse;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtMessages;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;

public class CourseCatalogPage extends HTML {
	private static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	protected static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	
	public CourseCatalogPage() {
		addStyleName("unitime-CourseCatalogPage");
		removeStyleName("unitime-NotPrintableBottomLine");
		
		LoadingWidget.getInstance().show(MESSAGES.waitLoadingData());
		RPC.execute(new CatalogRequest(Window.Location.getParameter("term"), Window.Location.getParameter("subjectCode"), Window.Location.getParameter("courseNumber")),
				new AsyncCallback<CatalogResponse>() {
					@Override
					public void onSuccess(CatalogResponse response) {
						LoadingWidget.getInstance().hide();
						setHTML(response.getContent());
						NodeList<Element> scripts = getElement().getElementsByTagName("script");
						if (scripts != null)
							for (int i = 0; i < scripts.getLength(); i++)
								ToolBox.eval(scripts.getItem(i).getInnerHTML());
					}
					
					@Override
					public void onFailure(Throwable caught) {
						LoadingWidget.getInstance().hide();
						setHTML(MESSAGES.failedLoadData(caught.getMessage()));
						addStyleName("unitime-ErrorMessage");
						UniTimeNotifications.error(MESSAGES.failedLoadData(caught.getMessage()), caught);
					}
				});
	}
	
	public static class CatalogRequest implements GwtRpcRequest<CatalogResponse>{
		private String iTerm;
		private String iSubject;
		private String iCourseNbr;
		
		public CatalogRequest() {}
		public CatalogRequest(String term, String subject, String courseNbr) {
			iTerm = term;
			iSubject = subject;
			iCourseNbr = courseNbr;
		}
		
		public String getTerm() { return iTerm; }
		public void setTerm(String term) { iTerm = term; }
		public String getSubject() { return iSubject; }
		public void setSubject(String subject) { iSubject = subject; }
		public String getCourseNbr() { return iCourseNbr; }
		public void setCourseNbr(String courseNbr) { iCourseNbr = courseNbr; }
		
		@Override
		public String toString() {
			return getSubject() + " " + getCourseNbr() + " (" + getTerm() + ")";
		}
	}
	
	public static class CatalogResponse implements GwtRpcResponse{
		private String iContent;
		
		public CatalogResponse() {}
		
		public boolean hasContent() { return iContent != null && !iContent.isEmpty(); }
		public String getContent() { return iContent; }
		public void setContent(String pageLabel) { iContent = pageLabel; }
	}

}