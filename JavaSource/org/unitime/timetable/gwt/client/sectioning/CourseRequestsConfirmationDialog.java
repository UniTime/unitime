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

import java.util.HashMap;
import java.util.Map;

import org.unitime.timetable.gwt.client.aria.AriaButton;
import org.unitime.timetable.gwt.client.aria.AriaStatus;
import org.unitime.timetable.gwt.client.aria.AriaSuggestArea;
import org.unitime.timetable.gwt.client.aria.AriaTabBar;
import org.unitime.timetable.gwt.client.aria.AriaTextArea;
import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.client.widgets.UniTimeDialogBox;
import org.unitime.timetable.gwt.resources.GwtAriaMessages;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.resources.StudentSectioningConstants;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.resources.StudentSectioningResources;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.CheckCoursesResponse;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.CourseMessage;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;

/**
 * @author Tomas Muller
 */
public class CourseRequestsConfirmationDialog extends UniTimeDialogBox {
	protected static StudentSectioningConstants CONSTANTS = GWT.create(StudentSectioningConstants.class);
	protected static GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	protected static StudentSectioningResources RESOURCES = GWT.create(StudentSectioningResources.class);
	protected static StudentSectioningMessages SCT_MESSAGES = GWT.create(StudentSectioningMessages.class);
	protected static GwtAriaMessages ARIA = GWT.create(GwtAriaMessages.class);
	private AriaButton iYes, iNo;
	private AsyncCallback<Boolean> iCommand;
	private String iMessage;
	private boolean iValue = false;
	private AriaTextArea iNote = null;
	private AriaSuggestArea iNoteSuggestions = null;
	private Image iImage;
	private CheckBox iCheckBox = null;
	private AriaTabBar iCoursesTab = null;
	private P iCoursesTabScroll = null;
	private Map<Integer, CourseMessage> iCourse2message = null;

	public CourseRequestsConfirmationDialog(CheckCoursesResponse response, int confirm, AsyncCallback<Boolean> callback) {
		super(true, true);
		addStyleName("unitime-CourseRequestsConfirmationDialog");
		setText(response.getConfirmationTitle(confirm, MESSAGES.dialogConfirmation()));
		iMessage = response.getConfirmations(confirm, " \n");
		iCommand = callback;
		
		P panel = new P("unitime-ConfirmationPanel");
		// setEscapeToHide(true);
		
		P bd = new P("body-panel");
		panel.add(bd);

		P ic = new P("icon-panel");
		bd.add(ic);
		iImage = new Image(RESOURCES.statusWarning());
		ic.add(iImage);

		P cp = new P("content-panel");
		bd.add(cp);
		P mp = new P("message-panel");
		cp.add(mp);
		P ctab = null;
		String last = null;
		for (final CourseMessage cm: response.getMessages()) {
			if (confirm != cm.getConfirm()) continue;
			if (cm.hasCourse() && "REQUEST_NOTE".equals(cm.getCode())) {
				if (iCourse2message == null) {
					iCourse2message = new HashMap<Integer, CourseMessage>();
					iCoursesTab = new AriaTabBar(); iCoursesTab.addStyleName("notes-tab");
					iCoursesTabScroll = new P("notes-scroll"); iCoursesTabScroll.add(iCoursesTab);
					iCoursesTabScroll.getElement().getStyle().clearPosition();
					iCoursesTabScroll.getElement().getStyle().clearOverflow();
					iNote = new AriaTextArea();
					iNote.setStyleName("unitime-TextArea"); iNote.addStyleName("request-notes");
					iNote.setVisibleLines(5);
					iNote.setCharacterWidth(80);
					iNote.addValueChangeHandler(new ValueChangeHandler<String>() {
						@Override
						public void onValueChange(ValueChangeEvent<String> event) {
							CourseMessage message = iCourse2message.get(iCoursesTab.getSelectedTab());
							message.setMessage(event.getValue());
						}
					});
					iNote.addKeyDownHandler(new KeyDownHandler() {
						@Override
						public void onKeyDown(KeyDownEvent event) {
							if (iNoteSuggestions.isSuggestionListShowing()) return;
							if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_TAB && event.getNativeEvent().getShiftKey()) {
								if (iCoursesTab.getSelectedTab() > 0) {
									iCourse2message.get(iCoursesTab.getSelectedTab()).setMessage(iNote.getText());
									iCoursesTab.selectTab(iCoursesTab.getSelectedTab() - 1, true);
									event.preventDefault();
								}
							} else if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_TAB) {
								if (iCoursesTab.getSelectedTab() + 1 < iCoursesTab.getTabCount()) {
									iCourse2message.get(iCoursesTab.getSelectedTab()).setMessage(iNote.getText());
									iCoursesTab.selectTab(iCoursesTab.getSelectedTab() + 1, true);
									event.preventDefault();
								}
							}
						}
					});
					iNoteSuggestions = new AriaSuggestArea(iNote, cm.getSuggestions());
					iNoteSuggestions.addStyleName("request-note");
					iNoteSuggestions.setTabPreventDefault(true);
					iNoteSuggestions.addSelectionHandler(new SelectionHandler<Suggestion>() {
						@Override
						public void onSelection(SelectionEvent<Suggestion> event) {
							CourseMessage message = iCourse2message.get(iCoursesTab.getSelectedTab());
							message.setMessage(event.getSelectedItem().getReplacementString());
							String text = iNote.getText();
							if (text.indexOf('<') >= 0 && text.indexOf('>') > text.indexOf('<')) {
								iNote.setSelectionRange(text.indexOf('<'), text.indexOf('>') - text.indexOf('<') + 1);
							}
						}
					});
					iCoursesTab.addSelectionHandler(new SelectionHandler<Integer>() {
						@Override
						public void onSelection(SelectionEvent<Integer> event) {
							iCoursesTab.getTabElement(event.getSelectedItem()).scrollIntoView();
							CourseMessage message = iCourse2message.get(event.getSelectedItem());
							boolean show = iNoteSuggestions.isSuggestionListShowing();
							if (show) iNoteSuggestions.hideSuggestionList();
							iNoteSuggestions.setSuggestions(message.getSuggestions());
							iNote.setText(message.getMessage() == null ? "" : message.getMessage());
							if (show) iNoteSuggestions.showSuggestions(iNote.getText());
							iNote.setAriaLabel(ARIA.requestNoteFor(message.getCourse()));
							AriaStatus.getInstance().setHTML(ARIA.requestNoteFor(message.getCourse()));
						}
					});
					if (cm.getMessage() != null) iNote.setText(cm.getMessage());
					mp.add(iCoursesTabScroll);
					mp.add(iNoteSuggestions);
				}
				iCourse2message.put(iCourse2message.size(), cm);
				iCoursesTab.addTab(cm.getCourse());
				if (iCourse2message.size() == 1) iCoursesTab.selectTab(0);
			} else if (cm.hasCourse()) {
				if (ctab == null) { ctab = new P("course-table"); last = null; }
				P cn = new P("course-name");
				if (last == null || !last.equals(cm.getCourse())) cn.setText(cm.getCourse());
				P m = new P("course-message"); m.setText(SCT_MESSAGES.courseMessage(cm.getMessage()));
				P crow = new P("course-row");
				if (last == null || !last.equals(cm.getCourse())) crow.addStyleName("first-course-line");
				crow.add(cn); crow.add(m);
				ctab.add(crow);
				last = cm.getCourse();
			} else if ("REQUEST_NOTE".equals(cm.getCode())) {
				iNote = new AriaTextArea();
				iNote.setStyleName("unitime-TextArea"); iNote.addStyleName("request-note");
				iNote.setVisibleLines(5);
				iNote.setCharacterWidth(80);
				if (cm.getMessage() != null) iNote.setText(cm.getMessage());
				iNote.addValueChangeHandler(new ValueChangeHandler<String>() {
					@Override
					public void onValueChange(ValueChangeEvent<String> event) {
						cm.setMessage(event.getValue());
					}
				});
				if (cm.hasSuggestions()) {
					iNoteSuggestions = new AriaSuggestArea(iNote, cm.getSuggestions());
					iNoteSuggestions.addStyleName("request-note");
					iNoteSuggestions.addSelectionHandler(new SelectionHandler<Suggestion>() {
						@Override
						public void onSelection(SelectionEvent<Suggestion> event) {
							cm.setMessage(event.getSelectedItem().getReplacementString());
							String text = iNote.getText();
							if (text.indexOf('<') >= 0 && text.indexOf('>') > text.indexOf('<')) {
								iNote.setSelectionRange(text.indexOf('<'), text.indexOf('>') - text.indexOf('<') + 1);
							}
						}
					});
					mp.add(iNoteSuggestions);
				} else {
					mp.add(iNote);
				}
			} else if ("CHECK_BOX".equals(cm.getCode())) {
				if (ctab != null) { mp.add(ctab); ctab = null; }
				iCheckBox = new CheckBox(cm.getMessage());
				iCheckBox.addStyleName("message");
				mp.add(iCheckBox);
			} else {
				if (ctab != null) { mp.add(ctab); ctab = null; }
				P m = new P("message"); m.setHTML(cm.getMessage());
				mp.add(m);
				
			}
		}
		if (ctab != null) { mp.add(ctab); ctab = null; }
		
		P bp = new P("buttons-panel");
		panel.add(bp);
		iYes = new AriaButton(response.getConfirmationYesButton(confirm, MESSAGES.buttonConfirmYes()));
		iYes.addStyleName("yes");
		String yesTitle = response.getConfirmationYesButtonTitle(confirm, null);
		if (yesTitle != null && !yesTitle.isEmpty()) iYes.setTitle(yesTitle);
		bp.add(iYes);
		iYes.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				submit();
			}
		});
		
		if (iCheckBox != null) {
			iYes.setEnabled(false);
			iCheckBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
				@Override
				public void onValueChange(ValueChangeEvent<Boolean> event) {
					iYes.setEnabled(event.getValue());
				}
			});
		}
		
		iNo = new AriaButton(response.getConfirmationNoButton(confirm, MESSAGES.buttonConfirmNo()));
		iNo.addStyleName("no");
		String noTitle = response.getConfirmationNoButtonTitle(confirm, null);
		if (noTitle != null && !noTitle.isEmpty()) iNo.setTitle(noTitle);
		bp.add(iNo);
		iNo.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				hide();
			}
		});
		
		addCloseHandler(new CloseHandler<PopupPanel>() {
			@Override
			public void onClose(CloseEvent<PopupPanel> event) {
				if (iCommand != null) 
					Scheduler.get().scheduleDeferred(new ScheduledCommand() {
						@Override
						public void execute() {
							iCommand.onSuccess(iValue);
						}
					});
			}
		});
		
		setWidget(panel);
	}
	
	public CourseRequestsConfirmationDialog withImage(ImageResource image) {
		if (image != null) 
			iImage.setResource(image);
		return this;
	}
	
	@Override
	public void center() {
		super.center();
		if (iMessage != null && !iMessage.isEmpty())
			AriaStatus.getInstance().setText(ARIA.dialogOpened(getText()) + " " + iMessage + " " + ARIA.confirmationEnterToAcceptEscapeToReject());
		Scheduler.get().scheduleDeferred(new ScheduledCommand() {
			@Override
			public void execute() {
				if (iNote != null)
					iNote.setFocus(true);
				else
					iYes.setFocus(true);
			}
		});
	}
	
	protected void submit() {
		iValue = true;
		hide();
	}
	
	public String getNote() {
		return iNote == null ? null : iNote.getText();
	}
	
	public static void confirm(CheckCoursesResponse response, int confirm, AsyncCallback<Boolean> callback) {
		new CourseRequestsConfirmationDialog(response, confirm, callback).center();
	}
	
	public static void confirm(CheckCoursesResponse response, int confirm, ImageResource image, AsyncCallback<Boolean> callback) {
		new CourseRequestsConfirmationDialog(response, confirm, callback).withImage(image).center();
	}
}
