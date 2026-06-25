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
package org.unitime.timetable.gwt.client.page;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.aria.ImageButton;
import org.unitime.timetable.gwt.client.tables.TableInterface;
import org.unitime.timetable.gwt.client.tables.TableWidget;
import org.unitime.timetable.gwt.client.tables.TableInterface.PropertyInterface;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.client.widgets.SearchableListBox;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeFileUpload;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.command.client.GwtRpcRequest;
import org.unitime.timetable.gwt.command.client.GwtRpcResponse;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.resources.GwtResources;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteHandler;
import com.google.gwt.user.client.ui.Label;

public class ContactUsPage extends Composite {
	protected static CourseMessages COURSE = GWT.create(CourseMessages.class);
	protected static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	protected static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	protected static final GwtResources RESOURCES = GWT.create(GwtResources.class);
	
	SimpleForm iPanel;
	UniTimeHeaderPanel iHeader, iFooter;
	ListBox iCategories;
	ListBox iManagers;
	TextBox iSubject;
	TextArea iMessage;
	ContactUsResponse iData;
	P iCCs, iAFs;
	UniTimeFileUpload iUpload;

	public ContactUsPage() {
		iPanel = new SimpleForm();
		iPanel.addStyleName("unitime-ContactUsPage");
		initWidget(iPanel);
		
		iHeader = new UniTimeHeaderPanel(COURSE.sectionInquiry());
		iPanel.addHeaderRow(iHeader);
		
		iHeader.addButton("another", COURSE.actionInquirySubmitAnother(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				init();
			}
		});
		iHeader.getButton("another").setTitle(COURSE.titleInquirySubmitAnother(COURSE.accessInquirySubmitAnother()));
		iHeader.getButton("another").setAccessKey(COURSE.accessInquirySubmitAnother().charAt(0));
		iHeader.setEnabled("another", false);
		
		iHeader.addButton("back", COURSE.actionInquiryBack(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				ToolBox.open(GWT.getHostPageBaseURL() + "main");
			}
		});
		iHeader.getButton("back").setTitle(COURSE.titleInquiryBack(COURSE.accessInquiryBack()));
		iHeader.getButton("back").setAccessKey(COURSE.accessInquiryBack().charAt(0));
		iHeader.setEnabled("back", false);
		
		iHeader.addButton("submit", COURSE.actionInquirySubmit(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				iHeader.clearMessage();
				if (iMessage.getText().isEmpty()) {
					iHeader.setErrorMessage(COURSE.errorInquiryMessageRequired());
					return;
				}
				ContactUsRequest request = new ContactUsRequest(ContactUsRequest.Operation.SUBMIT);
				request.setSubject(iSubject.getText());
				request.setMessage(iMessage.getText());
				if (iCategories != null)
					request.setCategoryId(Long.valueOf(iCategories.getSelectedValue()));
				if (iCCs != null) {
					for (int i = 0; i < iCCs.getWidgetCount(); i++)
						request.addManagerToCC(((Item)iCCs.getWidget(i)).getId());
				}
				for (int i = 0; i < iAFs.getWidgetCount(); i++)
					request.addAttachment(((Item)iAFs.getWidget(i)).getId());
				LoadingWidget.getInstance().show(MESSAGES.waitPlease());
				RPC.execute(request, new AsyncCallback<ContactUsResponse>() {

					@Override
					public void onFailure(Throwable caught) {
						LoadingWidget.getInstance().hide();
						UniTimeNotifications.error(caught.getMessage());
						iHeader.setErrorMessage(caught.getMessage());
					}

					@Override
					public void onSuccess(ContactUsResponse result) {
						LoadingWidget.getInstance().hide();
						iPanel.clear();
						iHeader.setEnabled("submit", false);
						iHeader.setEnabled("cancel", false);
						iHeader.setEnabled("another", true);
						iHeader.setEnabled("back", true);
						iPanel.addRow(new Label(COURSE.messageInquirySubmitted()));
						iPanel.addBottomRow(iFooter);
						
						if (iData.hasContactInformation()) {
							iPanel.addHeaderRow(iData.getContactInformation().getName());
							for (PropertyInterface prop: iData.getContactInformation().getProperties()) {
								iPanel.addRow(prop.getName(), new TableWidget.CellWidget(prop.getCell()));
							}
						}
					}
				});
			}
		});
		iHeader.getButton("submit").setTitle(COURSE.titleInquirySubmit(COURSE.accessInquirySubmit()));
		iHeader.getButton("submit").setAccessKey(COURSE.accessInquirySubmit().charAt(0));
		iHeader.setEnabled("submit", false);
		
		iHeader.addButton("cancel", COURSE.actionInquiryCancel(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				ToolBox.open(GWT.getHostPageBaseURL() + "main");
			}
		});
		iHeader.getButton("cancel").setTitle(COURSE.titleInquiryCancel(COURSE.actionInquiryCancel()));
		iHeader.getButton("cancel").setAccessKey(COURSE.actionInquiryCancel().charAt(0));
		iHeader.setEnabled("cancel", false);
		
		iFooter = iHeader.clonePanel("");
		
		init();
	}
	
	protected void init() {
		iHeader.setEnabled("another", false);
		iHeader.setEnabled("back", false);
		iHeader.setEnabled("submit", false);
		iHeader.setEnabled("cancel", false);
		iHeader.setHeaderTitle(COURSE.sectionInquiry());
		iHeader.showLoading();
		RPC.execute(new ContactUsRequest(ContactUsRequest.Operation.LOAD), new AsyncCallback<ContactUsResponse>() {

			@Override
			public void onFailure(Throwable caught) {
				iHeader.setErrorMessage(MESSAGES.failedToInitialize(caught.getMessage()));
				UniTimeNotifications.error(MESSAGES.failedToInitialize(caught.getMessage()), caught);
				ToolBox.checkAccess(caught);
			}

			@Override
			public void onSuccess(ContactUsResponse result) {
				iData = result;
				iHeader.clearMessage();
				iPanel.clear();
				iPanel.addHeaderRow(iHeader);
				if (result.hasCategories()) {
					iCategories = new ListBox();
					for (Category cat: result.getCategories())
						iCategories.addItem(cat.getSubject(), cat.getId().toString());
					iCategories.setSelectedIndex(0);
					iCategories.addChangeHandler(new ChangeHandler() {
						@Override
						public void onChange(ChangeEvent event) {
							if (iCategories.getSelectedIndex() >= 0) {
								Category cat = iData.getCategory(Long.valueOf(iCategories.getSelectedValue()));
								if (cat.hasMessage())
									iMessage.setText(cat.getMessage());
								else
									iMessage.setText("");
							}
						}
					});
					iPanel.addRow(COURSE.propCategory(), iCategories);
				} else {
					iCategories = null;
				}
				if (result.hasManagers()) {
					iManagers = new ListBox();
					iManagers.addItem("", "");
					for (Manager m: result.getManagers()) {
						iManagers.addItem(m.getName(), m.getId().toString());
					}
					Button button = new Button(COURSE.actionAddRecipient());
					button.setTitle(COURSE.titleAddRecipient(COURSE.actionAddRecipient()));
					button.setAccessKey(COURSE.actionAddRecipient().charAt(0));
					button.addClickHandler(new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							if (iManagers.getSelectedIndex() > 0) {
								Manager m = iData.getManager(Long.valueOf(iManagers.getSelectedValue()));
								for (int i = 0; i < iCCs.getWidgetCount(); i++)
									if (m.getId().equals(((Item)iCCs.getWidget(i)).getId())) return;
								iCCs.add(new Item(m));
							}
						}
					});
					P managersLine = new P("managers-line");
					managersLine.add(new SearchableListBox(iManagers));
					managersLine.add(button);
					iPanel.addRow(COURSE.propEmailCC(), managersLine);
					iCCs = new P("items");
					iPanel.addRow("", iCCs);
				} else {
					iManagers = null;
					iCCs = null;
				}
				
				iSubject = new TextBox();
				iSubject.setStyleName("unitime-TextBox");
				iSubject.setWidth("800px");
				iPanel.addRow(COURSE.propEmailSubject(), iSubject);
				
				iMessage = new TextArea();
				iMessage.setStyleName("unitime-TextArea");
				iMessage.setWidth("800px");
				iMessage.setHeight("300px");
				iPanel.addRow(COURSE.propEmailMessage(), iMessage);
				
				iUpload = new UniTimeFileUpload(); iUpload.reset();
				iUpload.addSubmitCompleteHandler(new SubmitCompleteHandler() {
					@Override
					public void onSubmitComplete(SubmitCompleteEvent event) {
						RPC.execute(new ContactUsRequest(ContactUsRequest.Operation.UPLOAD), new AsyncCallback<ContactUsResponse>() {

							@Override
							public void onFailure(Throwable caught) {
								UniTimeNotifications.error(caught.getMessage());
							}

							@Override
							public void onSuccess(ContactUsResponse response) {
								if (response != null && response.getAttachedFile() != null) {
									iAFs.add(new Item(response.getAttachedFile()));
								}
							}
						});
					}
				});
				iPanel.addRow(COURSE.propEmailAttachment(), iUpload);
				iAFs = new P("items");
				iPanel.addRow("", iAFs);
				iPanel.addBottomRow(iFooter);
				
				if (result.hasContactInformation()) {
					iPanel.addHeaderRow(result.getContactInformation().getName());
					for (PropertyInterface prop: result.getContactInformation().getProperties()) {
						iPanel.addRow(prop.getName(), new TableWidget.CellWidget(prop.getCell()));
					}
				}
				
				iHeader.setEnabled("submit", true);
				iHeader.setEnabled("cancel", true);
			}
		});
	}
	
	class Item extends P {
		private Long iId;
		public Item(Manager m) {
			iId = m.getId();
			addStyleName("item-line");
			P email = new P("item-text"); email.setText(m.getName() + " <" + m.getEmail() + ">");
			add(email);
			ImageButton button = new ImageButton(RESOURCES.delete());
			button.addStyleName("item-remove");
			button.setAriaLabel(COURSE.titleDeleteRecipient());
			button.setTitle(COURSE.titleDeleteRecipient());
			button.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					((P)getParent()).remove(Item.this);
				}
			});
			add(button);
		}
		public Item(AttachedFile m) {
			iId = m.getId();
			addStyleName("item-line");
			P email = new P("item-text"); email.setText(m.getName() + " " + COURSE.attachmentFileSize(String.valueOf(m.getSize())));
			add(email);
			ImageButton button = new ImageButton(RESOURCES.delete());
			button.addStyleName("item-remove");
			button.setAriaLabel(COURSE.titleDeleteAttachedFile(m.getName()));
			button.setTitle(COURSE.titleDeleteAttachedFile(m.getName()));
			button.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					((P)getParent()).remove(Item.this);
				}
			});
			add(button);
		}
		
		public Long getId() { return iId; }
	}
	
	public static class ContactUsRequest implements GwtRpcRequest<ContactUsResponse> {
		private Operation iOperation;
		private Long iCategoryId;
		private List<Long> iManagersToCC;
		private List<Long> iAttachments;
		private String iSubject;
		private String iMessage;
		
		public ContactUsRequest() {}
		
		public ContactUsRequest(Operation op) { iOperation = op; }
		
		public static enum Operation {
			LOAD, SUBMIT, UPLOAD,
		}
		
		public Operation getOperation() { return iOperation; }
		public void setOperation(Operation op) { iOperation = op; }
		public Long getCategoryId() { return iCategoryId; }
		public void setCategoryId(Long id) { iCategoryId = id; }
		public boolean hasManagersToCC() { return iManagersToCC != null && !iManagersToCC.isEmpty(); }
		public void addManagerToCC(Long id) {
			if (iManagersToCC == null) iManagersToCC = new ArrayList<Long>();
			iManagersToCC.add(id);
		}
		public List<Long> getManagersToCC() { return iManagersToCC; }
		
		public String getSubject() { return iSubject; }
		public void setSubject(String subject) { iSubject = subject; }
		public String getMessage() { return iMessage; }
		public void setMessage(String message) { iMessage = message; }
		
		public boolean hasAttachments() { return iAttachments != null && !iAttachments.isEmpty(); }
		public void addAttachment(Long id) {
			if (iAttachments == null) iAttachments = new ArrayList<Long>();
			iAttachments.add(id);
		}
		public List<Long> getAttachments() { return iAttachments; }
	}

	public static class ContactUsResponse implements GwtRpcResponse {
		private List<Category> iCategories = null;
		private Set<Manager> iManagers = null;
		private TableInterface iContactInformation;
		private AttachedFile iAttachedFile = null;
		
		public boolean hasCategories() { return iCategories != null && !iCategories.isEmpty(); }
		public void addCategory(Category cat) {
			if (iCategories == null) iCategories = new ArrayList<Category>();
			iCategories.add(cat);
		}
		public List<Category> getCategories() { return iCategories; }
		public Category getCategory(Long id) {
			if (iCategories == null) return null;
			for (Category cat: iCategories)
				if (id.equals(cat.getId())) return cat;
			return null;
		}
		
		public boolean hasManagers() { return iManagers != null && !iManagers.isEmpty(); }
		public void addManager(Manager manager) {
			if (iManagers == null) iManagers = new TreeSet<Manager>();
			iManagers.add(manager);
		}
		public Set<Manager> getManagers() { return iManagers; }
		public Manager getManager(Long id) {
			if (iManagers == null) return null;
			for (Manager m: iManagers)
				if (id.equals(m.getId())) return m;
			return null;
		}
		
		public boolean hasContactInformation() { return iContactInformation != null && iContactInformation.hasProperties(); }
		public void setContactInformation(TableInterface table) { iContactInformation = table; }
		public TableInterface getContactInformation() { return iContactInformation; }
		
		public AttachedFile getAttachedFile() { return iAttachedFile; }
		public void setAttachedFile(AttachedFile file) { iAttachedFile = file; }
	}
	
	public static class Manager implements IsSerializable, Comparable<Manager> {
		private Long iId;
		private String iName;
		private String iEmail;
		
		public Long getId() { return iId; }
		public void setId(Long id) { iId = id; }
		public String getName() { return iName; }
		public void setName(String name) { iName = name; }
		public String getEmail() { return iEmail; }
		public void setEmail(String email) { iEmail = email; }
		@Override
		public String toString() { return getName() + " <" + getEmail() + ">"; }
		@Override
		public int compareTo(Manager m) {
			int cmp = getName().compareTo(m.getName());
			if (cmp != 0) return cmp;
			return getEmail().compareTo(m.getEmail());
		}
	}
	
	public static class AttachedFile implements IsSerializable {
		private Long iId;
		private String iName;
		private long iSize;
		
		public Long getId() { return iId; }
		public void setId(Long id) { iId = id; }
		public String getName() { return iName; }
		public void setName(String name) { iName = name; }
		public long getSize() { return iSize; }
		public void setSize(long size) { iSize = size; }
	}
	
	public static class Category implements IsSerializable {
		private Long iId;
		private String iSubject;
		private String iMessage;
		
		public Long getId() { return iId; }
		public void setId(Long id) { iId = id; }
		public String getSubject() { return iSubject; }
		public void setSubject(String subject) { iSubject = subject; }
		public boolean hasMessage() { return iMessage != null && !iMessage.isEmpty(); }
		public void setMessage(String message) { iMessage = message; }
		public String getMessage() { return iMessage; }
	}
}
