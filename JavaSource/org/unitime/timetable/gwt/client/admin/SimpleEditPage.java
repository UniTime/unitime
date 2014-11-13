/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2010 - 2013, UniTime LLC, and individual contributors
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.unitime.timetable.gwt.client.Client;
import org.unitime.timetable.gwt.client.Lookup;
import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.Client.GwtPageChangeEvent;
import org.unitime.timetable.gwt.client.events.SingleDateSelector;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.page.UniTimePageLabel;
import org.unitime.timetable.gwt.client.widgets.NumberBox;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeDialogBox;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable;
import org.unitime.timetable.gwt.client.widgets.UniTimeWidget;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.HasCellAlignment;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.MouseClickListener;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.TableEvent;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.DataChangedEvent;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.DataChangedListener;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.HasFocus;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseNull;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.resources.GwtResources;
import org.unitime.timetable.gwt.shared.AcademicSessionProvider;
import org.unitime.timetable.gwt.shared.PersonInterface;
import org.unitime.timetable.gwt.shared.SimpleEditInterface;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.Field;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.FieldType;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.ListItem;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.PageName;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.Record;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.RecordComparator;
import org.unitime.timetable.gwt.shared.UserDataInterface;
import org.unitime.timetable.gwt.shared.UserDataInterface.GetUserDataRpcRequest;
import org.unitime.timetable.gwt.shared.UserDataInterface.SetUserDataRpcRequest;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.dom.client.Style.TextAlign;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Tomas Muller
 */
public class SimpleEditPage extends Composite {
	public static final GwtResources RESOURCES =  GWT.create(GwtResources.class);
	public static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	private static final GwtConstants CONSTANTS = GWT.create(GwtConstants.class);
	protected static final GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);

	private SimpleForm iPanel;
	private UniTimeHeaderPanel iHeader, iBottom;
	private String iType;
	private PageName iPageName = null;
	private UniTimeTable<Record> iTable;
	
	private SimpleEditInterface iData;
	private SimplePanel iSimple;
	
	private boolean iEditable = false;
	private TextArea iStudentsText = null;
	private Lookup iLookup;
	private boolean[] iVisible = null;
	
	private AcademicSessionProvider iAcademicSessionProvider = new AcademicSessionProvider() {
		@Override
		public void selectSession(Long sessionId, AsyncCallback<Boolean> callback) {}
		
		@Override
		public String getAcademicSessionName() {
			return iData.getSessionName();
		}
		
		@Override
		public Long getAcademicSessionId() {
			return iData.getSessionId();
		}
		
		@Override
		public void addAcademicSessionChangeHandler(AcademicSessionChangeHandler handler) {}

		@Override
		public AcademicSessionInfo getAcademicSessionInfo() {
			return null;
		}
	};
	
	public SimpleEditPage() {
		iType = Window.Location.getParameter("type");
		if (iType == null) throw new RuntimeException(MESSAGES.errorNoEditType());
		
		ClickHandler save = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				String valid = validate();
				if (valid != null) {
					iHeader.setErrorMessage(valid);
					return;
				}
				iData.getRecords().clear();
				iData.getRecords().addAll(iTable.getData());
				iHeader.setMessage(MESSAGES.waitSavingData());
				RPC.execute(SimpleEditInterface.SaveDataRpcRequest.saveData(iType, iData), new AsyncCallback<SimpleEditInterface>() {
					@Override
					public void onFailure(Throwable caught) {
						iHeader.setErrorMessage(MESSAGES.failedSave(caught.getMessage()));
					}
					@Override
					public void onSuccess(SimpleEditInterface result) {
						iData = result;
						iEditable = false;
						refreshTable();
						saveOrder();
					}
				});
			}
		};
		
		ClickHandler edit = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				iEditable = true;
				iHeader.setEnabled("edit", false);
				refreshTable();
			}
		};
		
		ClickHandler back = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				iEditable = false;
				load(null);
			}
		};
		
		ClickHandler add = new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				detail(iData.addRecord(null));
			}
		};

		iPanel = new SimpleForm();
		iHeader = new UniTimeHeaderPanel();
		iHeader.addButton("add", MESSAGES.buttonAdd(), 75, add);
		iHeader.addButton("edit", MESSAGES.buttonEdit(), 75, edit);
		iHeader.addButton("save", MESSAGES.buttonSave(), 75, save);
		iHeader.addButton("back", MESSAGES.buttonBack(), 75, back);
		iPanel.addHeaderRow(iHeader);
		
		iTable = new UniTimeTable<Record>();
		iTable.setAllowSelection(true);
		iPanel.addRow(iTable);
		
		iBottom = iHeader.clonePanel();
		iPanel.addNotPrintableBottomRow(iBottom);
		
		iSimple = new SimplePanel(iPanel);
		
		initWidget(iSimple);
		
		final Timer timer = new Timer() {
			@Override
			public void run() {
				saveOrder();
			}
		};
		iTable.addDataChangedListener(new DataChangedListener<Record>() {
			@Override
			public void onDataInserted(DataChangedEvent<Record> event) {
			}

			@Override
			public void onDataMoved(List<DataChangedEvent<Record>> event) {
				timer.schedule(5000);
			}

			@Override
			public void onDataRemoved(DataChangedEvent<Record> event) {
			}

			@Override
			public void onDataSorted(List<DataChangedEvent<Record>> event) {
			}
		});
		iTable.addMouseClickListener(new MouseClickListener<SimpleEditInterface.Record>() {
			@Override
			public void onMouseClick(TableEvent<Record> event) {
				if (event.getCol() == 0 && event.getData() != null && hasDetails()) {
					if ("+".equals(event.getData().getField(0))) {
						setDetailsVisible(event.getData().getUniqueId(), true);
						return;
					} else if ("-".equals(event.getData().getField(0))) {
						setDetailsVisible(event.getData().getUniqueId(), false);
						return;
					}
				}
				if (iEditable || !iData.isEditable() || event.getData() == null || !event.getData().isEditable()) return;
				detail(event.getData());
			}
		});
		
		iLookup = new Lookup();
		iLookup.setOptions("mustHaveExternalId,source=students");
		iLookup.addValueChangeHandler(new ValueChangeHandler<PersonInterface>() {
			@Override
			public void onValueChange(ValueChangeEvent<PersonInterface> event) {
				PersonInterface student = event.getValue();
				if (student != null) {
					iStudentsText.setValue(iStudentsText.getValue() + (iStudentsText.getValue().isEmpty() ? "" : "\n")
							+ student.getId() + " " + student.getLastName() + ", " + student.getFirstName() + (student.getMiddleName() == null ? "" : " " + student.getMiddleName()), true);
				}
			}
		});
		
		RPC.execute(SimpleEditInterface.GetPageNameRpcRequest.getPageName(iType), new AsyncCallback<PageName>() {
			@Override
			public void onFailure(Throwable caught) {
				iHeader.setErrorMessage(MESSAGES.failedWrongEditType(caught.getMessage()));
				UniTimeNotifications.error(caught.getMessage(), caught);
			}

			@Override
			public void onSuccess(PageName result) {
				iPageName = result;
				UniTimePageLabel.getInstance().setPageName(iPageName.plural());
				load(null);
			}
		});
	}
	
	private Record next(Record record) {
		if (record == null || record.getUniqueId() == null) return null;
		boolean next = false;
		for (int row = 0; row < iTable.getRowCount(); row++) {
			Record rec = iTable.getData(row);
			if (rec == null || rec.getUniqueId() == null) continue;
			if (!iTable.getRowFormatter().isVisible(row)) continue;
			if (next) return rec;
			if (record.getUniqueId().equals(rec.getUniqueId())) next = true;
		}
		return null;
	}
	
	private Record previous(Record record) {
		if (record == null || record.getUniqueId() == null) return null;
		Record previous = null;
		for (int row = 0; row < iTable.getRowCount(); row++) {
			Record rec = iTable.getData(row);
			if (rec == null || rec.getUniqueId() == null) continue;
			if (!iTable.getRowFormatter().isVisible(row)) continue;
			if (record.getUniqueId().equals(rec.getUniqueId())) return previous;
			previous = rec;
		}
		return null;
	}
	
	private void detail(final Record record) {
		SimpleForm detail = new SimpleFormWithMouseOver();
		final List<MyCell> cells = new ArrayList<SimpleEditPage.MyCell>();
		UniTimePageLabel.getInstance().setPageName(record.getUniqueId() == null ? MESSAGES.pageAdd(iPageName.singular()) : MESSAGES.pageEdit(iPageName.singular()));
		final UniTimeHeaderPanel header = new UniTimeHeaderPanel();
		Record prev = previous(record);
		Record next = next(record);
		final Record backup = record.cloneRecord();
		
		header.addButton("save", MESSAGES.buttonSave(), 75, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				String valid = validate(record, cells);
				if (valid != null) {
					header.setErrorMessage(valid);
					return;
				}
				header.setMessage(MESSAGES.waitSavingRecord());
				RPC.execute(SimpleEditInterface.SaveRecordRpcRequest.saveRecord(iType, record), new AsyncCallback<Record>() {
					@Override
					public void onFailure(Throwable caught) {
						header.setErrorMessage(MESSAGES.failedSave(caught.getMessage()));
					}
					@Override
					public void onSuccess(Record result) {
						record.copyFrom(result);
						iEditable = false;
						iSimple.setWidget(iPanel);
						refreshTable();
						saveOrder();
						for (int r = 0; r < iTable.getRowCount(); r++) {
							if (iTable.getData(r) == null) continue;
							if (record.getUniqueId().equals(iTable.getData(r).getUniqueId())) {
								iTable.setSelected(r, true);
								ToolBox.scrollToElement(iTable.getRowFormatter().getElement(r - 1));
								break;
							}
						}
						Client.fireGwtPageChanged(new GwtPageChangeEvent());
					}
				});
			}
		});
		
		if (record.getUniqueId() != null && record.isDeletable()) {
			header.addButton("delete", MESSAGES.buttonDelete(), 75, new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					header.setMessage(MESSAGES.waitDeletingRecord());
					RPC.execute(SimpleEditInterface.DeleteRecordRpcRequest.deleteRecord(iType, record), new AsyncCallback<Record>() {
						@Override
						public void onFailure(Throwable caught) {
							header.setErrorMessage(MESSAGES.failedDelete(iPageName.singular().toLowerCase(), caught.getMessage()));
						}
						@Override
						public void onSuccess(Record result) {
							iData.getRecords().remove(result);
							iEditable = false;
							iSimple.setWidget(iPanel);
							refreshTable();
							saveOrder();
							Client.fireGwtPageChanged(new GwtPageChangeEvent());
						}
					});
				}
			});
		}
		
		if (prev != null) {
			header.addButton("prev", MESSAGES.buttonPrevious(), 75, new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					String valid = validate(record, cells);
					if (valid != null) {
						header.setErrorMessage(valid);
						return;
					}
					header.setMessage(MESSAGES.waitSavingRecord());
					RPC.execute(SimpleEditInterface.SaveRecordRpcRequest.saveRecord(iType, record), new AsyncCallback<Record>() {
						@Override
						public void onFailure(Throwable caught) {
							header.setErrorMessage(MESSAGES.failedSave(caught.getMessage()));
						}
						@Override
						public void onSuccess(Record result) {
							record.copyFrom(result);
							refreshTable();
							Record prev = previous(record);
							if (prev != null) {
								detail(prev);
							} else {
								iSimple.setWidget(iPanel);
							}
							Client.fireGwtPageChanged(new GwtPageChangeEvent());
						}
					});
				}
			});
		}
		
		if (next != null) {
			header.addButton("next", MESSAGES.buttonNext(), 75, new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					String valid = validate(record, cells);
					if (valid != null) {
						header.setErrorMessage(valid);
						return;
					}
					header.setMessage(MESSAGES.waitSavingRecord());
					RPC.execute(SimpleEditInterface.SaveRecordRpcRequest.saveRecord(iType, record), new AsyncCallback<Record>() {
						@Override
						public void onFailure(Throwable caught) {
							header.setErrorMessage(MESSAGES.failedSave(caught.getMessage()));
						}
						@Override
						public void onSuccess(Record result) {
							record.copyFrom(result);
							refreshTable();
							Record next = next(record);
							if (next != null) {
								detail(next);
							} else {
								iSimple.setWidget(iPanel);
							}
							Client.fireGwtPageChanged(new GwtPageChangeEvent());
						}
					});
				}
			});
		}
		
		header.addButton("back", MESSAGES.buttonBack(), 75, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				iSimple.setWidget(iPanel);
				record.copyFrom(backup);
				if (record.isEmpty())
					iData.getRecords().remove(record);
				iEditable = false;
				iSimple.setWidget(iPanel);
				refreshTable();
				saveOrder();
				if (record.getUniqueId() != null) {
					for (int r = 0; r < iTable.getRowCount(); r++) {
						if (iTable.getData(r) == null) continue;
						if (record.getUniqueId().equals(iTable.getData(r).getUniqueId())) {
							iTable.setSelected(r, true);
							ToolBox.scrollToElement(iTable.getRowFormatter().getElement(r - 1));
							break;
						}
					}
				}
				Client.fireGwtPageChanged(new GwtPageChangeEvent());
			}
		});
		
		detail.addHeaderRow(header);
		int idx = 0;
		for (Field field: iData.getFields()) {
			MyCell cell = new MyCell(record.isEditable(idx), field, record, idx, true);
			cells.add(cell);
			if (field.isVisible() && field.getType() != FieldType.parent) {
				String name = field.getName();
				if (hasDetails() && name.contains("|"))
					name = isParent(record) ? name.split("\\|")[0] : name.split("\\|")[1];
				detail.addRow(name + ":", cell);
			}
			idx ++;
		}
		UniTimeHeaderPanel bottom = header.clonePanel();
		detail.addNotPrintableBottomRow(bottom);
		
		iSimple.setWidget(detail);
		ToolBox.scrollToElement(detail.getElement());
		Client.fireGwtPageChanged(new GwtPageChangeEvent());
	}
	
	private boolean hasDetails() {
		return iData != null && iData.getFields().length > 0 && iData.getFields()[0].getType() == FieldType.parent;
	}
	
	private boolean isParent(Record r) {
		return hasDetails() && ("+".equals(r.getField(0)) || "-".equals(r.getField(0)));
	}

	private boolean isChild(Record r) {
		return hasDetails() && !"+".equals(r.getField(0)) && !"-".equals(r.getField(0));
	}

	private void setDetailsVisible(Long recordId, boolean show) {
		if (!hasDetails()) return;
		for (int i = 0; i < iTable.getRowCount(); i++) {
			Record r = iTable.getData(i);
			if (r == null) continue;
			if (r.getUniqueId().equals(recordId)) {
				Image details = (Image)((MyCell)iTable.getWidget(i, 0)).getInnerWidget();
				details.setResource(show ? RESOURCES.treeOpen() : RESOURCES.treeClosed());
				r.setField(0, show ? "-" : "+");
			} else if (String.valueOf(recordId).equals(r.getField(0))) {
				iTable.getRowFormatter().setVisible(i, show);
			}
		}
		saveOrder();
	}
	
	public void load(final AsyncCallback<Boolean> callback) {
		iBottom.setVisible(false);
		iHeader.setEnabled("add", false);
		iHeader.setEnabled("save", false);
		iHeader.setEnabled("edit", false);
		iHeader.setEnabled("back", false);
		iHeader.setMessage(MESSAGES.waitLoadingData());
		iTable.clearTable();

		RPC.execute(SimpleEditInterface.LoadDataRpcRequest.loadData(iType), new AsyncCallback<SimpleEditInterface>() {

			@Override
			public void onSuccess(SimpleEditInterface result) {
				iData = result;
				final Comparator<Record> cmp = iData.getComparator();
				
				if (iData.isSaveOrder()) {
					GetUserDataRpcRequest ordRequest = new GetUserDataRpcRequest();
					ordRequest.add("SimpleEdit.Order[" + iType.toString() + "]");
					if (hasDetails())
						ordRequest.add("SimpleEdit.Open[" + iType.toString() + "]");
					ordRequest.add("SimpleEdit.Hidden[" + iType.toString() + "]");
					RPC.execute(ordRequest, new AsyncCallback<UserDataInterface>() {
						@Override
						public void onSuccess(UserDataInterface result) {
							final String order = "|" + result.get("SimpleEdit.Order[" + iType.toString() + "]") + "|";
							if (hasDetails()) {
								String open = "|" + result.get("SimpleEdit.Open[" + iType.toString() + "]") + "|";
								for (Record r: iData.getRecords()) {
									if (isParent(r))
										r.setField(0, open.indexOf("|" + r.getUniqueId() + "|") >= 0 ? "-" : "+");
								}
							}
							Collections.sort(iData.getRecords(), new Comparator<Record>() {
								public int compare(Record r1, Record r2) {
									if (iData.getFields()[0].getType() == FieldType.parent) {
										Record p1 = ("+".equals(r1.getField(0)) || "-".equals(r1.getField(0)) ? null : iData.getRecord(Long.valueOf(r1.getField(0))));
										Record p2 = ("+".equals(r2.getField(0)) || "-".equals(r2.getField(0)) ? null : iData.getRecord(Long.valueOf(r2.getField(0))));
										if ((p1 == null ? r1 : p1).equals(p2 == null ? r2 : p2)) { // same parents
											if (p1 != null && p2 == null) return 1; // r1 is already a parent
											if (p1 == null && p2 != null) return -1; // r2 is already a parent
											// same level
										} else if (p1 != null || p2 != null) { // different parents
											return compare(p1 == null ? r1 : p1, p2 == null ? r2 : p2); // compare parents
										}
									}
									int i1 = (r1.getUniqueId() == null ? -1 : order.indexOf("|" + r1.getUniqueId() + "|"));
									if (i1 >= 0) {
										int i2 = (r2.getUniqueId() == null ? -1 : order.indexOf("|" + r2.getUniqueId() + "|"));
										if (i2 >= 0) {
											return (i1 < i2 ? -1 : i1 > i2 ? 1 : cmp.compare(r1, r2));
										}
									}
									return cmp.compare(r1, r2);
								}
							});
							refreshTable("|" + result.get("SimpleEdit.Hidden[" + iType.toString() + "]") + "|");
							if (callback != null) callback.onSuccess(true);
						}
						@Override
						public void onFailure(Throwable caught) {
							Collections.sort(iData.getRecords(), cmp);
							refreshTable();
							if (callback != null) callback.onSuccess(false);
						}
					});
				} else {
					refreshTable();
					if (callback != null) callback.onSuccess(true);
				}
			}
			
			@Override
			public void onFailure(Throwable caught) {
				iHeader.setErrorMessage(MESSAGES.failedLoadData(caught.getMessage()));
				ToolBox.checkAccess(caught);
				if (callback != null) callback.onFailure(caught);
			}
		});
	}
	
	private List<UniTimeTableHeader> header(boolean top) {
		List<UniTimeTableHeader> header = new ArrayList<UniTimeTableHeader>();
		int col = 0;
		for (final Field field: iData.getFields()) {
			String name = field.getName();
			if (hasDetails() && name.contains("|"))
				name = name.replace("|", "<br>&nbsp;&nbsp;");
			UniTimeTableHeader cell = new UniTimeTableHeader(name);
			if (!top) { cell.addStyleName("unitime-TopLineDash"); cell.getElement().getStyle().setPaddingTop(2, Unit.PX); }
			header.add(cell);
			final int index = col;
			if (field.getType() == FieldType.number)
				cell.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
			if (field.getType() == FieldType.parent) {
				cell.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
				cell.addOperation(new UniTimeTableHeader.Operation() {
					@Override
					public void execute() {
						for (int row = 0; row < iTable.getRowCount(); row++) {
							Record r = iTable.getData(row);
							if (r == null) continue;
							if ("+".equals(r.getField(0))) {
								Image details = (Image)((MyCell)iTable.getWidget(row, 0)).getInnerWidget();
								details.setResource(RESOURCES.treeOpen());
								r.setField(0, "-");
							} else if (!"-".equals(r.getField(0))) {
								iTable.getRowFormatter().setVisible(row, true);
							}
						}
						saveOrder();
					}
					
					@Override
					public boolean isApplicable() {
						for (int row = 0; row < iTable.getRowCount(); row++) {
							Record r = iTable.getData(row);
							if (r != null && "+".equals(r.getField(0)))
								return true;
						}
						return false;
					}
					
					@Override
					public boolean hasSeparator() {
						return false;
					}
					
					@Override
					public String getName() {
						return MESSAGES.opExpandAll();
					}
				});
				cell.addOperation(new UniTimeTableHeader.Operation() {
					@Override
					public void execute() {
						for (int row = 0; row < iTable.getRowCount(); row++) {
							Record r = iTable.getData(row);
							if (r == null) continue;
							if ("-".equals(r.getField(0))) {
								Image details = (Image)((MyCell)iTable.getWidget(row, 0)).getInnerWidget();
								details.setResource(RESOURCES.treeClosed());
								r.setField(0, "+");
							} else if (!"+".equals(r.getField(0))) {
								iTable.getRowFormatter().setVisible(row, false);
							}
						}
						saveOrder();
					}
					
					@Override
					public boolean isApplicable() {
						for (int row = 0; row < iTable.getRowCount(); row++) {
							Record r = iTable.getData(row);
							if (r != null && "-".equals(r.getField(0)))
								return true;
						}
						return false;
					}
					
					@Override
					public boolean hasSeparator() {
						return false;
					}
					
					@Override
					public String getName() {
						return MESSAGES.opCollapseAll();
					}
				});
			} else {
				cell.addOperation(new UniTimeTableHeader.Operation() {
					@Override
					public void execute() {
						iTable.sort(index, new Comparator<Record>() {
							RecordComparator iComparator = iData.getComparator();
							public int compare(Record a, Record b) {
								int cmp = iComparator.compare(index, a, b);
								if (cmp != 0) return cmp;
								return iComparator.compare(a, b);
							}
						});
						saveOrder();
					}
					
					@Override
					public boolean isApplicable() {
						return true;
					}
					
					@Override
					public boolean hasSeparator() {
						return false;
					}
					
					@Override
					public String getName() {
						return MESSAGES.opSortBy(field.getName());
					}
				});			
				if (col == 0) {
					cell.addOperation(new UniTimeTableHeader.Operation() {
						@Override
						public void execute() {
							iTable.sort(index, iData.getComparator());
							saveOrder();
						}
						
						@Override
						public boolean isApplicable() {
							return true;
						}
						
						@Override
						public boolean hasSeparator() {
							return false;
						}
						
						@Override
						public String getName() {
							return MESSAGES.opSortDefault();
						}
					});
				}
			}
			if (col == 0) {
				cell.addOperation(new UniTimeTableHeader.Operation() {
					@Override
					public void execute() {
						for (int index = 1; index < iData.getFields().length; index++) {
							if (iData.getFields()[index].isEditable() && iTable.isColumnVisible(index)) {
								iTable.setColumnVisible(index, false);
								iVisible[index] = false;
							}
						}
						saveOrder();
					}
					
					@Override
					public boolean isApplicable() {
						for (int index = 1; index < iData.getFields().length; index++) {
							if (iData.getFields()[index].isEditable() && iTable.isColumnVisible(index)) return true;
						}
						return false;
					}
					
					@Override
					public boolean hasSeparator() {
						return true;
					}
					
					@Override
					public String getName() {
						return MESSAGES.opHideAll();
					}
				});
				cell.addOperation(new UniTimeTableHeader.Operation() {
					@Override
					public void execute() {
						for (int index = 1; index < iData.getFields().length; index++) {
							if (iData.getFields()[index].isEditable() && !iTable.isColumnVisible(index)) {
								iTable.setColumnVisible(index, true);
								iVisible[index] = true;
							}
						}
						saveOrder();
					}
					
					@Override
					public boolean isApplicable() {
						for (int index = 1; index < iData.getFields().length; index++) {
							if (iData.getFields()[index].isEditable() && !iTable.isColumnVisible(index)) return true;
						}
						return false;
					}
					
					@Override
					public boolean hasSeparator() {
						for (int index = 1; index < iData.getFields().length; index++) {
							if (iData.getFields()[index].isEditable() && iTable.isColumnVisible(index)) return false;
						}
						return true;
					}
					
					@Override
					public String getName() {
						return MESSAGES.opShowAll();
					}
				});
			}
			col++;
		}
		for (UniTimeTableHeader h: header) {
			col = 0;
			boolean first = true;
			for (final Field field: iData.getFields()) {
				final int index = col;
				if (field.isEditable()) {
					final boolean sep = first; first = false;
					h.addOperation(new UniTimeTableHeader.Operation() {
						@Override
						public void execute() {
							iTable.setColumnVisible(index, !iTable.isColumnVisible(index));
							iVisible[index] = iTable.isColumnVisible(index);
							saveOrder();
						}
						
						@Override
						public boolean isApplicable() {
							if (!iTable.isColumnVisible(index)) return true;
							int nrVisible = 0;
							for (boolean v: iVisible) if (v) nrVisible ++;
							return nrVisible > 1;
						}
						
						@Override
						public boolean hasSeparator() {
							return sep;
						}
						
						@Override
						public String getName() {
							return (iTable.isColumnVisible(index) ? MESSAGES.opHide(field.getName()): MESSAGES.opShow(field.getName()));
						}
					});
				}
				col ++;
			}
		}
		if (iData.isEditable() && iEditable) {
			header.add(new UniTimeTableHeader());
			header.add(new UniTimeTableHeader());
		}
		return header;
	}
	
	private void refreshTable() {
		refreshTable(null);
	}
	
	private void refreshTable(String hidden) {
		UniTimePageLabel.getInstance().setPageName(iEditable ? MESSAGES.pageEdit(iPageName.plural()) : iPageName.plural());
		iTable.clearTable();
		
		iTable.setAllowSelection(!hasDetails());

		iTable.addRow(null, header(true));
		
		if (iVisible == null) {
			iVisible = new boolean[iData.getFields().length];
			for (int i = 0; i < iVisible.length; i++) iVisible[i] = iData.getFields()[i].isVisible() && (hidden == null || !hidden.contains("|" + iData.getFields()[i].getName() + "|"));
		}
		
		boolean empty = false;
		int row = 1;
		for (Record r: iData.getRecords()) {
			fillRow(r, row++);
			empty = r.isEmpty();
			if ((row % 31) == 0 && !hasDetails()) { iTable.addRow(null, header(false)); row++; }
		}
		if (!empty && iEditable && iData.isEditable() && iData.isAddable())
			fillRow(iData.addRecord(null), row);
		
		iBottom.setVisible(true);
		if (iData.isEditable()) {
			iHeader.setEnabled("back", iEditable);
			iHeader.setEnabled("save", iEditable);
			iHeader.setEnabled("edit", !iEditable);
			iHeader.setEnabled("add", !iEditable && iData.isAddable());
		}
		
		for (int i = 0; i < iVisible.length; i++) 
			iTable.setColumnVisible(i, iVisible[i]);
		
		iHeader.clearMessage();
	}
	
	private void fillRow(Record record, int row) {
		List<Widget> line = new ArrayList<Widget>();
		int col = 0;
		for (Field field: iData.getFields()) {
			MyCell cell = new MyCell(iData.isEditable() && iEditable && record.isEditable(col), field, record, col, false);
			line.add(cell);
			col++;
		}
		if (iData.isAddable() && iEditable) {
			Image add = new Image(RESOURCES.add());
			add.getElement().getStyle().setCursor(Cursor.POINTER);
			add.setTitle(MESSAGES.titleInsertRowAbove());
			add.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					int row = iTable.getCellForEvent(event).getRowIndex();
					fillRow(iData.addRecord(null), iTable.insertRow(row));
				}
			});
			line.add(add);
		} else if (iEditable && iData.isEditable()) {
			line.add(new Label());
		}
		if (iData.isEditable() && iEditable && record.isDeletable()) {
			Image delete = new Image(RESOURCES.delete());
			delete.setTitle(MESSAGES.titleDeleteRow());
			delete.getElement().getStyle().setCursor(Cursor.POINTER);
			delete.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					int row = iTable.getCellForEvent(event).getRowIndex();
					iData.getRecords().remove(iTable.getData(row));
					iTable.removeRow(row);
				}
			});
			line.add(delete);
		} else if (iEditable && iData.isEditable()) {
			line.add(new Label());
		}
		iTable.setRow(row, record, line);
		if (hasDetails()) {
			if (!"+".equals(record.getField(0)) && !"-".equals(record.getField(0))) {
				Record p = iData.getRecord(Long.valueOf(record.getField(0)));
				if (p != null && "+".equals(p.getField(0)))
					iTable.getRowFormatter().setVisible(row, false);
			}
			if (isParent(record)) {
				iTable.getRowFormatter().getElement(row).getStyle().setBackgroundColor("#f3f3f3");
				/*
				for (int i = 1; i < iTable.getCellCount(row); i++)
					iTable.getCellFormatter().addStyleName(row, i, "top-border-dashed");
				*/
			}
		}
	}
	
	public class MyCell extends Composite implements HasFocus, HasCellAlignment {
		private Field iField;
		private Record iRecord;
		private int iIndex;
		private boolean iDetail;
		
		public MyCell(boolean editable, final Field field, final Record record, final int index, boolean detail) {
			iField = field; iRecord = record; iIndex = index; iDetail = detail;
			if (field.getType() == FieldType.parent) {
				if ("+".equals(record.getField(index))) {
					initWidget(new Image(RESOURCES.treeClosed()));
				} else if ("-".equals(record.getField(index))) {
					initWidget(new Image(RESOURCES.treeOpen()));
				} else {
					initWidget(new Label());
				}
				return;
			}
			if (editable) {
				switch (field.getType()) {
				case text:
					final TextBox text = new TextBox();
					text.setStyleName("unitime-TextBox");
					text.setMaxLength(field.getLength());
					text.setText(record.getField(index));
					text.setWidth(field.getWidth() + "px");
					text.addChangeHandler(new ChangeHandler() {
						@Override
						public void onChange(ChangeEvent event) {
							record.setField(index, text.getText());
							setError(null);
						}
					});
					initWidget(new UniTimeWidget<TextBox>(text));
					if (iEditable && iData.isAddable() && record.getUniqueId() == null) {
						text.addChangeHandler(new ChangeHandler() {
							@Override
							public void onChange(ChangeEvent event) {
								if (iData.getRecords().indexOf(iRecord) == iData.getRecords().size() - 1 && !record.isEmpty())
									fillRow(iData.addRecord(null), iTable.insertRow(iTable.getRowCount()));
							}
						});
					}
					break;
				case textarea:
					final TextArea textarea = new TextArea();
					textarea.setStyleName("unitime-TextArea");
					if (detail) {
						textarea.setVisibleLines(Math.max(5, field.getHeight()));
						textarea.setCharacterWidth(Math.max(80, field.getWidth()));
					} else {
						textarea.setVisibleLines(field.getHeight() <= 0 ? 2 : Math.min(3, field.getHeight()));
						textarea.setCharacterWidth(field.getWidth() <= 0 ? 40: Math.min(60, field.getWidth()));
					}
					textarea.setText(record.getField(index));
					textarea.addChangeHandler(new ChangeHandler() {
						@Override
						public void onChange(ChangeEvent event) {
							record.setField(index, textarea.getText());
							setError(null);
						}
					});
					initWidget(new UniTimeWidget<TextArea>(textarea));
					if (iEditable && iData.isAddable() && record.getUniqueId() == null) {
						textarea.addChangeHandler(new ChangeHandler() {
							@Override
							public void onChange(ChangeEvent event) {
								if (iData.getRecords().indexOf(iRecord) == iData.getRecords().size() - 1 && !record.isEmpty())
									fillRow(iData.addRecord(null), iTable.insertRow(iTable.getRowCount()));
							}
						});
					}
					break;
				case number:
					final NumberBox number = new NumberBox();
					number.getElement().getStyle().setTextAlign(TextAlign.RIGHT);
					number.setText(record.getField(index));
					number.setDecimal(field.isAllowFloatingPoint());
					number.setNegative(field.isAllowNegative());
					number.setWidth(field.getWidth() + "px");
					number.addChangeHandler(new ChangeHandler() {
						@Override
						public void onChange(ChangeEvent event) {
							record.setField(index, number.getText());
							setError(null);
						}
					});
					initWidget(new UniTimeWidget<TextBox>(number));
					if (iEditable && iData.isAddable() && record.getUniqueId() == null) {
						number.addChangeHandler(new ChangeHandler() {
							@Override
							public void onChange(ChangeEvent event) {
								if (iData.getRecords().indexOf(iRecord) == iData.getRecords().size() - 1 && !record.isEmpty())
									fillRow(iData.addRecord(null), iTable.insertRow(iTable.getRowCount()));
							}
						});
					}
					break;
				case list:
					final ListBox list = new ListBox(false);
					list.setStyleName("unitime-TextBox");
					if (((record.getField(index) == null || record.getField(index).isEmpty()) || (!field.isNotEmpty() && (isChild(record) || field.isParentNotEmpty())))
						&& (field.getValues().isEmpty() || !field.getValues().get(0).getValue().isEmpty())) {
						list.addItem("", "");
					}
					for (ListItem item: field.getValues())
						list.addItem(item.getText(), item.getValue());
					for (int i = 0; i < list.getItemCount(); i++)
						if (list.getValue(i).equals(record.getField(index)))
							list.setSelectedIndex(i);
					list.addChangeHandler(new ChangeHandler() {
						@Override
						public void onChange(ChangeEvent event) {
							record.setField(index, (list.getSelectedIndex() < 0 || list.getValue(list.getSelectedIndex()).isEmpty() ? null : list.getValue(list.getSelectedIndex())));
							setError(null);
						}
					});
					initWidget(new UniTimeWidget<ListBox>(list));
					if (iEditable && iData.isAddable() && record.getUniqueId() == null) {
						list.addChangeHandler(new ChangeHandler() {
							@Override
							public void onChange(ChangeEvent event) {
								if (iData.getRecords().indexOf(iRecord) == iData.getRecords().size() - 1 && !record.isEmpty())
									fillRow(iData.addRecord(null), iTable.insertRow(iTable.getRowCount()));
							}
						});
					}
					break;
				case multi:
					final ListBox multi = new ListBox(true);
					multi.setStyleName("unitime-TextBox");
					multi.setVisibleItemCount(3);
					for (ListItem item: field.getValues())
						multi.addItem(item.getText(), item.getValue());
					String[] vals = record.getValues(index);
					if (vals != null) {
						for (String val: vals) {
							for (int i = 0; i < multi.getItemCount(); i++)
								if (multi.getValue(i).equals(val))
									multi.setItemSelected(i, true);
						}
					}
					multi.addChangeHandler(new ChangeHandler() {
						@Override
						public void onChange(ChangeEvent event) {
							record.setField(index, null);
							for (int i = 0; i < multi.getItemCount(); i++ ) {
								if (multi.isItemSelected(i)) record.addToField(index, multi.getValue(i));
							}
							setError(null);
						}
					});
					initWidget(new UniTimeWidget<ListBox>(multi));
					if (iEditable && iData.isAddable() && record.getUniqueId() == null) {
						multi.addChangeHandler(new ChangeHandler() {
							@Override
							public void onChange(ChangeEvent event) {
								if (iData.getRecords().indexOf(iRecord) == iData.getRecords().size() - 1 && !record.isEmpty())
									fillRow(iData.addRecord(null), iTable.insertRow(iTable.getRowCount()));
							}
						});
					}
					break;
				case toggle:
					final CheckBox check = new CheckBox();
					check.setValue(record.getField(index) == null ? null : "true".equalsIgnoreCase(record.getField(index)));
					check.addClickHandler(new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							record.setField(index, check.getValue() == null ? null : check.getValue() ? "true" : "false");
							setError(null);
						}
					});
					initWidget(new UniTimeWidget<CheckBox>(check));
					if (iEditable && iData.isAddable() && record.getUniqueId() == null) {
						check.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
							@Override
							public void onValueChange(ValueChangeEvent<Boolean> event) {
								if (iData.getRecords().indexOf(iRecord) == iData.getRecords().size() - 1 && !record.isEmpty())
									fillRow(iData.addRecord(null), iTable.insertRow(iTable.getRowCount()));
							}
						});
					}
					break;
				case students:
					if (detail) {
						final TextArea area = new TextArea();
						area.setValue(getValue());
						area.setStyleName("unitime-TextArea");
						area.setVisibleLines(10);
						area.setCharacterWidth(80);
						area.addValueChangeHandler(new ValueChangeHandler<String>() {
							@Override
							public void onValueChange(ValueChangeEvent<String> event) {
								record.setField(index, area.getText());
								setError(null);
							}
						});
						VerticalPanel students = new VerticalPanel();
						students.add(area);
						Button lookup = new Button(MESSAGES.buttonLookup());
						lookup.setAccessKey(UniTimeHeaderPanel.guessAccessKey(MESSAGES.buttonLookup()));
						lookup.addClickHandler(new ClickHandler() {
							@Override
							public void onClick(ClickEvent event) {
								iStudentsText = area;
								iLookup.center();
							}
						});
						students.add(lookup);
						students.setCellHorizontalAlignment(lookup, HasHorizontalAlignment.ALIGN_RIGHT);
						initWidget(new UniTimeWidget<VerticalPanel>(students));
					} else {
						HorizontalPanel hp = new HorizontalPanel();
						final Label label = new Label(String.valueOf(getValue().isEmpty() ? 0 : getValue().split("\\n").length));
						hp.add(label);
						Image change = new Image(RESOURCES.edit());
						hp.add(change);
						hp.setCellVerticalAlignment(change, HasVerticalAlignment.ALIGN_MIDDLE);
						label.getElement().getStyle().setPaddingRight(5, Unit.PX);
						change.addClickHandler(new ClickHandler() {
							@Override
							public void onClick(ClickEvent event) {
								final UniTimeDialogBox dialog = new UniTimeDialogBox(true, true);
								SimpleForm form = new SimpleForm();
								final TextArea text = new TextArea();
								text.setValue(getValue());
								text.setStyleName("unitime-TextArea");
								text.setVisibleLines(10);
								text.setCharacterWidth(80);
								text.addValueChangeHandler(new ValueChangeHandler<String>() {
									@Override
									public void onValueChange(ValueChangeEvent<String> event) {
										record.setField(index, event.getValue());
										label.setText(String.valueOf(event.getValue().isEmpty() ? 0 : event.getValue().split("\\n").length));
										setError(null);
									}
								});
								form.addRow(text);
								UniTimeHeaderPanel header = new UniTimeHeaderPanel();
								header.addButton("lookup", MESSAGES.buttonLookup(), 75, new ClickHandler() {
									@Override
									public void onClick(ClickEvent event) {
										iStudentsText = text;
										iLookup.center();
									}
								});
								header.addButton("close", MESSAGES.buttonClose(), 75, new ClickHandler() {
									@Override
									public void onClick(ClickEvent event) {
										dialog.hide();
									}
								});
								form.addBottomRow(header);
								dialog.setText(field.getName());
								dialog.setWidget(form);
								dialog.setEscapeToHide(true);
								dialog.center();
							}
						});
						initWidget(new UniTimeWidget<HorizontalPanel>(hp));
					}
					break;
				case person:
					HorizontalPanel hp = new HorizontalPanel();
					String[] name = record.getValues(index);
					final HTML label = new HTML(name.length <= 2 ? "<i>" + MESSAGES.notSet() + "</i>" : name.length >= 6 && !name[6].isEmpty() ? name[6] : name[0] + ", " + name[1] + (name[2].isEmpty() ? "" : " " + name[2]));
					label.setWidth(field.getWidth() + "px");
					hp.add(label);
					Image change = new Image(RESOURCES.edit());
					hp.add(change);
					hp.setCellVerticalAlignment(change, HasVerticalAlignment.ALIGN_MIDDLE);
					hp.setWidth("100%");
					hp.setCellHorizontalAlignment(change, HasHorizontalAlignment.ALIGN_RIGHT);
					label.getElement().getStyle().setPaddingRight(5, Unit.PX);
					change.addClickHandler(new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							Lookup lookup = new Lookup();
							lookup.setOptions("mustHaveExternalId");
							String[] name = record.getValues(index);
							if (name != null && name.length > 2)
								lookup.setQuery(name[0] + ", " + name[1] + (name[2].isEmpty() ? "" : " " + name[2]));
							lookup.addValueChangeHandler(new ValueChangeHandler<PersonInterface>() {
								@Override
								public void onValueChange(ValueChangeEvent<PersonInterface> event) {
									PersonInterface person = event.getValue();
									if (person != null) {
										label.setText(person.hasFormattedName() ? person.getFormattedName() : person.getLastName() + ", " + person.getFirstName() + (person.getMiddleName() == null ? "" : " " + person.getMiddleName()));
										record.setField(index, null);
										record.addToField(index, person.getLastName() == null ? "" : person.getLastName());
										record.addToField(index, person.getFirstName() == null ? "" : person.getFirstName());
										record.addToField(index, person.getMiddleName() == null ? "" : person.getMiddleName());
										record.addToField(index, person.getId() == null ? "" : person.getId());
										record.addToField(index, person.getEmail() == null ? "" : person.getEmail());
										record.addToField(index, person.getAcademicTitle() == null ? "" : person.getAcademicTitle());
										record.addToField(index, person.getFormattedName() == null ? "" : person.getFormattedName());
										setError(null);
									}
								}
							});
							lookup.center();
						}
					});
					initWidget(new UniTimeWidget<HorizontalPanel>(hp));
					break;
				case date:
					final SingleDateSelector date = new SingleDateSelector(iData.getSessionId() == null ? null : iAcademicSessionProvider, false);
					date.setText(record.getField(index));
					date.addValueChangeHandler(new ValueChangeHandler<Date>() {
						@Override
						public void onValueChange(ValueChangeEvent<Date> event) {
							record.setField(index, date.getText());
							setError(null);
						}
					});
					initWidget(new UniTimeWidget<SingleDateSelector>(date));
					if (iEditable && iData.isAddable() && record.getUniqueId() == null) {
						date.addValueChangeHandler(new ValueChangeHandler<Date>() {
							@Override
							public void onValueChange(ValueChangeEvent<Date> event) {
								if (iData.getRecords().indexOf(iRecord) == iData.getRecords().size() - 1 && !record.isEmpty())
									fillRow(iData.addRecord(null), iTable.insertRow(iTable.getRowCount()));
							}
						});
					}
					break;
				}
			} else {
				switch (field.getType()) {
				case toggle:
					Image image = new Image(record.getField(index) != null && "true".equalsIgnoreCase(record.getField(index)) ? RESOURCES.on() : RESOURCES.off());
					initWidget(image);
					break;
				case students:
					if (detail) {
						HTML html = new HTML(getValue().replaceAll("\\n", "<br>"));
						initWidget(html);
					} else {
						Label label = new Label(String.valueOf(getValue().isEmpty() ? 0 : getValue().split("\\n").length));
						initWidget(label);
					}
					break;
				case person:
					String[] name = record.getValues(index);
					initWidget(new HTML(name.length <= 2 ? "<i>" + MESSAGES.notSet() + "</i>" : name.length >= 6 && !name[6].isEmpty() ? name[6] : name[0] + ", " + name[1] + (name[2].isEmpty() ? "" : " " + name[2])));
					break;
				case textarea:
					HTML html = new HTML(getValue());
					initWidget(html);
					break;
				default:
					Label label = new Label(getValue());
					initWidget(label);
					if (field.getType() == FieldType.number)
						label.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
					break;
				}
			}
			if (!detail && index > 0 && iData.getFields()[index].getName().contains("|") && isChild(record))
				getElement().getStyle().setPaddingLeft(20, Unit.PX);
			setError(null);
		}
		
		public void setError(String message) {
			if (getWidget() instanceof UniTimeWidget<?>) {
				UniTimeWidget<?> w = (UniTimeWidget<?>)getWidget();
				if (message == null || message.isEmpty()) {
					w.clearHint();
					if (iDetail && iField.isShowParentWhenEmpty()) {
						String parent = getParentValue();
						if (parent != null && !parent.isEmpty()) {
							w.setHint(MESSAGES.hintDefaultsToWhenEmpty(parent.length() > 80 ? parent.substring(0, 77) + "..." : parent));
						}
					}
				} else
					w.setErrorHint(message);
			}
		}
		
		public String getParentValue() {
			if (!isChild(iRecord)) return null;
			Record parent = iData.getRecord(Long.valueOf(iRecord.getField(0)));
			if (parent == null) return null;
			String value = parent.getField(iIndex);
			if (value == null) return "";
			if (iField.getType() == FieldType.list) {
				for (ListItem item: iField.getValues()) {
					if (item.getValue().equals(value)) return item.getText();
				}
			} else if (iField.getType() == FieldType.multi) {
				String text = "";
				for (String val: parent.getValues(iIndex)) {
					for (ListItem item: iField.getValues()) {
						if (item.getValue().equals(val)) {
							if (!text.isEmpty()) text += ", ";
							text += item.getText();
						}
					}
				}
				return text;
			}
			return value;
		}
				
		public String getValue() {
			String value = iRecord.getField(iIndex);
			if (value == null) return "";
			if (iField.getType() == FieldType.list) {
				for (ListItem item: iField.getValues()) {
					if (item.getValue().equals(value)) return item.getText();
				}
			} else if (iField.getType() == FieldType.multi) {
				String text = "";
				for (String val: iRecord.getValues(iIndex)) {
					for (ListItem item: iField.getValues()) {
						if (item.getValue().equals(val)) {
							if (!text.isEmpty()) text += ", ";
							text += item.getText();
						}
					}
				}
				return text;
			}
			return value;
		}
		
		public Record getRecord() { return iRecord; }
		
		public boolean focus() { 
			Widget w = getWidget();
			if (w instanceof UniTimeWidget<?>)
				w = ((UniTimeWidget<?>)w).getWidget();
			if (w instanceof Focusable) {
				((Focusable)w).setFocus(true);
				if (w instanceof TextBox)
					((TextBox)w).selectAll();
				return true;
			}
			return false;
		}
		
		public Widget getInnerWidget() { return getWidget(); }

		@Override
		public HorizontalAlignmentConstant getCellAlignment() {
			switch (iField.getType()) {
			case toggle:
				return HasHorizontalAlignment.ALIGN_CENTER;
			case students:
				return HasHorizontalAlignment.ALIGN_RIGHT;
			default:
				return HasHorizontalAlignment.ALIGN_LEFT;
			}
		}
	}

	public void saveOrder() {
		if (!iData.isSaveOrder()) return;
		iHeader.setMessage(MESSAGES.waitSavingOrder());
		String ord = "";
		for (int i = 0; i < iTable.getRowCount(); i++) {
			Record r = iTable.getData(i);
			if (r == null || r.getUniqueId() == null) continue;
			if (!ord.isEmpty()) ord += "|";
			ord += r.getUniqueId();
		}
		SetUserDataRpcRequest data = new SetUserDataRpcRequest();
		data.put("SimpleEdit.Order[" + iType.toString() + "]", ord);
		if (iData.getFields()[0].getType() == FieldType.parent) {
			String open = "";
			for (int i = 0; i < iTable.getRowCount(); i++) {
				Record r = iTable.getData(i);
				if (r == null || r.getUniqueId() == null) continue;
				if ("-".equals(r.getField(0))) {
					if (!open.isEmpty()) open += "|";
					open += r.getUniqueId();
				}
			}
			data.put("SimpleEdit.Open[" + iType.toString() + "]", open);
		}
		String hidden = "";
		for (int i = 0; i < iData.getFields().length; i++) {
			if (!iTable.isColumnVisible(i)) {
				if (!hidden.isEmpty()) hidden += "|";
				hidden += iData.getFields()[i].getName();
			}
		}
		data.put("SimpleEdit.Hidden[" + iType.toString() + "]", hidden);
		RPC.execute(data, new AsyncCallback<GwtRpcResponseNull>() {
			@Override
			public void onFailure(Throwable caught) {
				iHeader.clearMessage();
			}
			@Override
			public void onSuccess(GwtRpcResponseNull result) {
				iHeader.clearMessage();
			}
		});
	}
	
	public String validate() {
		return validate(null, null);
	}
	
	public String validate(Record detailRecord, List<MyCell> detailCells) {
		String valid = null;
		DateTimeFormat dateFormat = DateTimeFormat.getFormat(CONSTANTS.eventDateFormat());
		Map<Integer, Map<String, MyCell>> uniqueMap = new HashMap<Integer, Map<String, MyCell>>();
		for (int row = 0; row < iTable.getRowCount(); row++) {
			SimpleEditInterface.Record record = iTable.getData(row);
			if (record == null || record.isEmpty()) continue;
			if (detailRecord != null && detailRecord.getUniqueId() != null && detailRecord.getUniqueId().equals(record.getUniqueId())) continue;
			for (int col = 0; col < iData.getFields().length; col ++) {
				Field field = iData.getFields()[col];
				String value = record.getField(col);
				MyCell widget = (MyCell)iTable.getWidget(row, col);
				widget.setError(null);
				if (!field.isEditable()) continue;
				if (field.isUnique()) {
					Map<String, MyCell> values = uniqueMap.get(col);
					if (values == null) {
						values = new HashMap<String, MyCell>(); uniqueMap.put(col, values);
					}
					if (value == null || value.isEmpty()) {
						widget.setError(MESSAGES.errorMustBeSet(field.getName()));
						if (valid == null && detailRecord == null) {
							valid = MESSAGES.errorMustBeSet(field.getName());
						}
					} else {
						MyCell old = values.put(value, widget);
						if (old != null) {
							widget.setError(MESSAGES.errorMustBeUnique(field.getName()));
							old.setError(MESSAGES.errorMustBeUnique(field.getName()));
							if (valid == null && detailRecord == null) {
								valid = MESSAGES.errorMustBeUnique(field.getName());
							}
						}
					}
				} else if (field.isNotEmpty() || (isParent(record) && field.isParentNotEmpty())) {
					if (value == null || value.isEmpty()) {
						widget.setError(MESSAGES.errorMustBeSet(field.getName()));
						if (valid == null && detailRecord == null) {
							valid = MESSAGES.errorMustBeSet(field.getName());
						}
					}
				} else {
					switch (field.getType()) {
					case date:
						Date date = null;
						try {
							date = dateFormat.parse(value);
						} catch (Exception e) {
							widget.setError(MESSAGES.errorNotValidDate(value));
							if (valid == null && detailRecord == null) {
								valid = MESSAGES.errorNotValidDate(value);
							}
						}
						if (date == null && field.isNotEmpty()) {
							widget.setError(MESSAGES.errorMustBeSet(field.getName()));
							if (valid == null && detailRecord == null) {
								valid = MESSAGES.errorMustBeSet(field.getName());
							}
						}
						break;
					case textarea:
						if (value.length() > field.getLength()) {
							widget.setError(MESSAGES.errorTooLong(field.getName()));
							if (valid == null && detailRecord == null) {
								valid = MESSAGES.errorTooLong(field.getName());
							}
						}
						break;
					}
				}
			}
		}
		if (detailRecord != null && !detailCells.isEmpty()) {
			SimpleEditInterface.Record record = detailRecord;
			for (int col = 0; col < iData.getFields().length; col ++) {
				Field field = iData.getFields()[col];
				String value = record.getField(col);
				MyCell widget = detailCells.get(col);
				widget.setError(null);
				if (!field.isEditable()) continue;
				if (field.isUnique()) {
					Map<String, MyCell> values = uniqueMap.get(col);
					if (values == null) {
						values = new HashMap<String, MyCell>(); uniqueMap.put(col, values);
					}
					if (value == null || value.isEmpty()) {
						widget.setError(MESSAGES.errorMustBeSet(field.getName()));
						if (valid == null) {
							valid = MESSAGES.errorMustBeSet(field.getName());
						}
					} else {
						MyCell old = values.put(value, widget);
						if (old != null) {
							widget.setError(MESSAGES.errorMustBeUnique(field.getName()));
							old.setError(MESSAGES.errorMustBeUnique(field.getName()));
							if (valid == null) {
								valid = MESSAGES.errorMustBeUnique(field.getName());
							}
						}
					}
				} else if (field.isNotEmpty() || (isParent(record) && field.isParentNotEmpty())) {
					if (value == null || value.isEmpty()) {
						widget.setError(MESSAGES.errorMustBeSet(field.getName()));
						if (valid == null) {
							valid = MESSAGES.errorMustBeSet(field.getName());
						}
					}
				} else {
					switch (field.getType()) {
					case date:
						Date date = null;
						try {
							date = dateFormat.parse(value);
						} catch (Exception e) {
							widget.setError(MESSAGES.errorNotValidDate(value));
							if (valid == null) {
								valid = MESSAGES.errorNotValidDate(value);
							}
						}
						if (date == null && field.isNotEmpty()) {
							widget.setError(MESSAGES.errorMustBeSet(field.getName()));
							if (valid == null) {
								valid = MESSAGES.errorMustBeSet(field.getName());
							}
						}
						break;
					case textarea:
						if (value.length() > field.getLength()) {
							widget.setError(MESSAGES.errorTooLong(field.getName()));
							if (valid == null) {
								valid = MESSAGES.errorTooLong(field.getName());
							}
						}
						break;
					}
				}
			}
		}
		return valid;
	}
	
	public static class SimpleFormWithMouseOver extends SimpleForm {
		public SimpleFormWithMouseOver() {
			super();
			
			sinkEvents(Event.ONMOUSEOVER);
			sinkEvents(Event.ONMOUSEOUT);
		}
		
		public void onBrowserEvent(final Event event) {
			Element td = getEventTargetCell(event);
			if (td==null) return;
		    final Element tr = DOM.getParent(td);
			int col = DOM.getChildIndex(tr, td);
		    Element body = DOM.getParent(tr);
		    int row = DOM.getChildIndex(body, tr);
		    
		    Widget widget = getWidget(row, col);
		    if (widget != null && widget instanceof UniTimeHeaderPanel) {
		    	super.onBrowserEvent(event);
		    	return;
		    }
		    
			switch (DOM.eventGetType(event)) {
			case Event.ONMOUSEOVER:
				getRowFormatter().addStyleName(row, "hover");
				break;
			case Event.ONMOUSEOUT:
				getRowFormatter().removeStyleName(row, "hover");
				break;
			}
			
			super.onBrowserEvent(event);
		}
	}
}
