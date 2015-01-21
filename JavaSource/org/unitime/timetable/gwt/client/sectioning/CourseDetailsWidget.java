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
package org.unitime.timetable.gwt.client.sectioning;

import org.unitime.timetable.gwt.client.GwtHint;
import org.unitime.timetable.gwt.command.client.GwtRpcRequest;
import org.unitime.timetable.gwt.command.client.GwtRpcResponse;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.resources.GwtResources;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;

/**
 * @author Tomas Muller
 */
public class CourseDetailsWidget extends Composite {
	protected static GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	protected static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	protected static final GwtResources RESOURCES =  GWT.create(GwtResources.class);
	private FlowPanel iPanel;
	private Image iLoadingImage;
	private Anchor iAnchor = null;
	private HTML iDetails = null;
	
	public CourseDetailsWidget(boolean anchor) {
		iPanel = new FlowPanel();
		
		iLoadingImage = new Image(RESOURCES.loading_small());
		iLoadingImage.setVisible(false);
		iPanel.add(iLoadingImage);
		
		iDetails = new HTML("");
		
		if (anchor) {
			iAnchor = new Anchor(MESSAGES.courseCatalogLink());
			iAnchor.setVisible(false);
			iAnchor.setTarget("_blank");
			iPanel.add(iAnchor);
			
			iAnchor.addMouseOverHandler(new MouseOverHandler() {
				@Override
				public void onMouseOver(MouseOverEvent event) {
					if (!iDetails.getText().isEmpty())
						GwtHint.showHint(iAnchor.getElement(), iDetails);
				}
			});

			iAnchor.addMouseOutHandler(new MouseOutHandler() {
				@Override
				public void onMouseOut(MouseOutEvent event) {
					GwtHint.hideHint();
				}
			});
			iDetails.setStyleName("unitime-CourseDetailsPopup");
		} else {
			iPanel.add(iDetails);
		}
		
		initWidget(iPanel);
	}
	
	public void insert(final RootPanel panel) {
		String command = panel.getElement().getInnerText().trim();
		panel.getElement().setInnerText(null);
		
		panel.add(this);
		panel.setVisible(true);
		
		try {
			reload(new CourseDetailsRpcRequest(Long.parseLong(command)));
		} catch (NumberFormatException e) {
			Element subjectElement = DOM.getElementById(command.split(",")[0]);
			Element courseElement = DOM.getElementById(command.split(",")[1]);
			if ("select".equalsIgnoreCase(subjectElement.getTagName())) {
				final ListBox subjectId = ListBox.wrap(subjectElement);
				final TextBox courseNumber = TextBox.wrap(courseElement);
				courseNumber.addValueChangeHandler(new ValueChangeHandler<String>() {
					@Override
					public void onValueChange(ValueChangeEvent<String> event) {
						if (subjectId.getSelectedIndex() >= 0 && !courseNumber.getValue().isEmpty()) {
							try {
								reload(new CourseDetailsRpcRequest(Long.valueOf(subjectId.getValue(subjectId.getSelectedIndex())), courseNumber.getValue()));
							} catch (NumberFormatException e) {}
						}
					}
				});
				if (subjectId.getSelectedIndex() >= 0 && !courseNumber.getValue().isEmpty()) {
					try {
						reload(new CourseDetailsRpcRequest(Long.valueOf(subjectId.getValue(subjectId.getSelectedIndex())), courseNumber.getValue()));
					} catch (NumberFormatException f) {}
				}
			} else if ("input".equalsIgnoreCase(courseElement.getTagName())) {
				final Hidden subjectId = Hidden.wrap(subjectElement);
				final TextBox courseNumber = TextBox.wrap(courseElement);
				courseNumber.addValueChangeHandler(new ValueChangeHandler<String>() {
					@Override
					public void onValueChange(ValueChangeEvent<String> event) {
						if (!courseNumber.getValue().isEmpty()) {
							try {
								reload(new CourseDetailsRpcRequest(Long.valueOf(subjectId.getValue()), courseNumber.getValue()));
							} catch (NumberFormatException e) {}
						}
					}
				});
				if (!courseNumber.getValue().isEmpty()) {
					try {
						reload(new CourseDetailsRpcRequest(Long.valueOf(subjectId.getValue()), courseNumber.getValue()));
					} catch (NumberFormatException f) {}
				}
			} else {
				final Hidden subjectId = Hidden.wrap(subjectElement);
				final Hidden courseNumber = Hidden.wrap(courseElement);
				reload(new CourseDetailsRpcRequest(Long.valueOf(subjectId.getValue()), courseNumber.getValue()));
			}
		}
	}
	
	private void reload(CourseDetailsRpcRequest request) {
		iLoadingImage.setVisible(true);
		if (iAnchor != null) {
			iAnchor.setVisible(false);
		} else {
			iDetails.setVisible(false);
		}
		RPC.execute(request, new AsyncCallback<CourseDetailsRpcResponse>() {
			@Override
			public void onFailure(Throwable caught) {
				iLoadingImage.setVisible(false);
			}

			@Override
			public void onSuccess(CourseDetailsRpcResponse result) {
				iLoadingImage.setVisible(false);
				if (result != null) {
					iDetails.setHTML(result.hasDetails() ? result.getDetails() : "");
					if (iAnchor != null) {
						iAnchor.setHref(result.getLink());
						iAnchor.setVisible(true);
					} else {
						iDetails.setVisible(result.hasDetails());
					}
				}
			}
		});
	}
	
	public static class CourseDetailsRpcRequest implements GwtRpcRequest<CourseDetailsRpcResponse> {
		private Long iCourseId = null;
		private Long iSubjectId = null;
		private String iCourseNumber = null;
		
		public CourseDetailsRpcRequest() {}
		public CourseDetailsRpcRequest(Long courseId) {
			setCourseId(courseId);
		}
		public CourseDetailsRpcRequest(Long subjectId, String courseNbr) {
			setSubjectId(subjectId);
			setCourseNumber(courseNbr);
		}
		
		public Long getCourseId() { return iCourseId; }
		public void setCourseId(Long courseId) { iCourseId = courseId; }
		public boolean hasCourseId() { return iCourseId != null; }
		
		public Long getSubjectId() { return iSubjectId; }
		public void setSubjectId(Long subjectId) { iSubjectId = subjectId; }
		public boolean hasSubjectId() { return iSubjectId != null; }
		
		public String getCourseNumber() { return iCourseNumber; }
		public void setCourseNumber(String courseNbr) { iCourseNumber = courseNbr; }
		public boolean hasCourseNumber() { return iCourseNumber != null; }
		
		@Override
		public String toString() {
			if (hasCourseId())
				return getCourseId().toString();
			else
				return getSubjectId() + " " + getCourseNumber();
		}
	}
	
	public static class CourseDetailsRpcResponse implements GwtRpcResponse {
		private String iLink = null;
		private String iDetails = null;
		
		public CourseDetailsRpcResponse() {}
		
		public String getLink() { return iLink; }
		public void setLink(String link) { iLink = link; }
		public boolean hasLink() { return iLink != null && !iLink.isEmpty(); }
		
		public String getDetails() { return iDetails; }
		public void setDetails(String details) { iDetails = details; }
		public boolean hasDetails() { return iDetails != null && !iDetails.isEmpty(); }
	}
	

}
