package org.unitime.timetable.gwt.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.unitime.timetable.gwt.resources.GwtResources;
import org.unitime.timetable.gwt.services.MenuService;
import org.unitime.timetable.gwt.services.MenuServiceAsync;
import org.unitime.timetable.gwt.services.SimpleEditService;
import org.unitime.timetable.gwt.services.SimpleEditServiceAsync;
import org.unitime.timetable.gwt.shared.SimpleEditException;
import org.unitime.timetable.gwt.shared.SimpleEditInterface;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.Field;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.FieldType;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.ListItem;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.Record;
import org.unitime.timetable.gwt.widgets.PageLabel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class SimpleEdit extends Composite {
	public static final GwtResources RESOURCES =  GWT.create(GwtResources.class);
	private final SimpleEditServiceAsync iService = GWT.create(SimpleEditService.class);
	private final MenuServiceAsync iMenuService = GWT.create(MenuService.class);

	private VerticalPanel iPanel;
	private SimpleEditInterface.Type iType;
	private HorizontalPanel iTopButtons, iBottomButtons;
	private Label iTopMessage, iBottomMessage;
	private Button iTopSave, iBottomSave;
	private MyFlexTable iTable;
	
	private SimpleEditInterface iData;
	
	public SimpleEdit() throws SimpleEditException {
		String typeString = Window.Location.getParameter("type");
		if (typeString == null) throw new SimpleEditException("Edit type is not provided.");
		iType = SimpleEditInterface.Type.valueOf(typeString);
		if (iType == null) throw new SimpleEditException("Edit type not recognized.");
		setPageName(iType.getTitle());
		
		iPanel = new VerticalPanel();
		iPanel.setWidth("100%");
		
		ClickHandler save = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				setMessage("Saving data...");
				iService.save(iData, new AsyncCallback<SimpleEditInterface>() {
					@Override
					public void onFailure(Throwable caught) {
						setErrorMessage("Save failed (" + caught.getMessage() + ").");
					}
					@Override
					public void onSuccess(SimpleEditInterface result) {
						iData = result;
						refreshTable();
						iTable.saveOrder();
					}
				});
			}
		};
		
		iTopButtons = new HorizontalPanel();
		iTopButtons.setWidth("100%");
		iTopButtons.setSpacing(2);
		iTopMessage = new Label("Loading data...", false);
		iTopMessage.setStyleName("unitime-Message");
		iTopButtons.add(iTopMessage);
		iTopButtons.setCellHorizontalAlignment(iTopMessage, HasHorizontalAlignment.ALIGN_CENTER);
		iTopSave = new Button("<u>S</u>ave", save);
		iTopSave.setAccessKey('s');
		iTopSave.addStyleName("unitime-NoPrint");
		iTopButtons.add(iTopSave);
		iTopButtons.setCellHorizontalAlignment(iTopSave, HasHorizontalAlignment.ALIGN_RIGHT);
		iPanel.add(iTopButtons);
		
		iTable = new MyFlexTable();
		iTable.setStylePrimaryName("unitime-MainTable");
		iTable.addStyleName("unitime-NotPrintableBottomLine");
		iPanel.add(iTable);
		
		iBottomButtons = new HorizontalPanel();
		iBottomButtons.setWidth("100%");
		iBottomButtons.setSpacing(2);
		iBottomMessage = new Label("", false);
		iBottomMessage.setStyleName("unitime-ErrorMessage");
		iBottomButtons.add(iBottomMessage);
		iBottomButtons.setCellHorizontalAlignment(iBottomMessage, HasHorizontalAlignment.ALIGN_CENTER);
		iBottomButtons.setVisible(false);
		iBottomSave = new Button("<u>S</u>ave", save);
		iBottomSave.addStyleName("unitime-NoPrint");
		iBottomButtons.add(iBottomSave);
		iBottomButtons.setCellHorizontalAlignment(iBottomSave, HasHorizontalAlignment.ALIGN_RIGHT);
		iPanel.add(iBottomButtons);
		
		initWidget(iPanel);
		
		iService.load(iType, new AsyncCallback<SimpleEditInterface>() {
			
			@Override
			public void onSuccess(SimpleEditInterface result) {
				iData = result;
				final Comparator<Record> cmp = iData.getComparator();
				
				Set<String> ordRequest = new HashSet<String>();
				ordRequest.add("SimpleEdit.Order[" + iType.toString() + "]");
				iMenuService.getUserData(ordRequest, new AsyncCallback<HashMap<String,String>>() {
					@Override
					public void onSuccess(HashMap<String, String> result) {
						final String order = "|" + result.get("SimpleEdit.Order[" + iType.toString() + "]") + "|";
						Collections.sort(iData.getRecords(), new Comparator<Record>() {
							public int compare(Record r1, Record r2) {
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
						refreshTable();
					}
					@Override
					public void onFailure(Throwable caught) {
						Collections.sort(iData.getRecords(), cmp);
						refreshTable();
					}
				});
			}
			
			@Override
			public void onFailure(Throwable caught) {
				setErrorMessage("Unable to load data (" + caught.getMessage() + ")");
			}
		});
	}
	
	private void refreshTable() {
		for (int row = iTable.getRowCount() - 1; row >= 0; row--)
			iTable.removeRow(row);

		int col = 0;
		for (Field field: iData.getFields()) {
			HTML html = new HTML(field.getName(), false);
			// html.addClickHandler(menu);
			iTable.getFlexCellFormatter().setStyleName(0, col, "unitime-ClickableTableHeader");
			if (col + 1 == iData.getFields().length) iTable.getFlexCellFormatter().setColSpan(0, col, 3);
			iTable.setWidget(0, col++, html);
		}
		
		int row = 1;
		boolean empty = false;
		for (Record r: iData.getRecords()) {
			fillRow(row++, r);
			empty = r.isEmpty();
		}
		if (!empty)
			fillRow(row++, iData.addRecord(null));
		
		iBottomButtons.setVisible(true);
		setMessage(null);
	}
	
	private void fillRow(int row, Record r) {
		int col = 0;
		for (Field field: iData.getFields()) {
			MyCell cell = new MyCell(iData.isEditable(), field, r, col);
			iTable.setWidget(row, col++, cell);
		}
		Image add = new Image(RESOURCES.add());
		add.getElement().getStyle().setCursor(Cursor.POINTER);
		add.setTitle("Insert a new row above this row.");
		add.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				int row = iTable.getCellForEvent(event).getRowIndex();
				fillRow(iTable.insertRow(row), iData.insertEmptyRecord(row - 1));
			}
		});
		iTable.setWidget(row, col++, add);
		Image delete = new Image(RESOURCES.delete());
		delete.setTitle("Delete this row.");
		delete.getElement().getStyle().setCursor(Cursor.POINTER);
		delete.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				int row = iTable.getCellForEvent(event).getRowIndex();
				iData.getRecords().remove(((MyCell)iTable.getWidget(row, 0)).getRecord());
				iTable.removeRow(row);
			}
		});
		iTable.setWidget(row, col++, delete);
	}
	
	public class MyCell extends Composite {
		private Field iField;
		private Record iRecord;
		private int iIndex;
		
		public MyCell(boolean editable, Field field, final Record record, final int index) {
			iField = field; iRecord = record; iIndex = index;
			if (editable) {
				if (field.getType() == FieldType.text) {
					final TextBox text = new TextBox();
					text.setStyleName("unitime-TextBox");
					text.setMaxLength(field.getLength());
					text.setText(record.getField(index));
					text.setWidth(field.getWidth() + "px");
					text.addChangeHandler(new ChangeHandler() {
						@Override
						public void onChange(ChangeEvent event) {
							record.setField(index, text.getText());
						}
					});
					initWidget(text);
				} else if (field.getType() == FieldType.list) {
					final ListBox list = new ListBox(false);
					list.setStyleName("unitime-TextBox");
					if (record.getField(index) == null) {
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
						}
					});
					initWidget(list);
				} else if (field.getType() == FieldType.multi) {
					final ListBox list = new ListBox(true);
					list.setStyleName("unitime-TextBox");
					list.setVisibleItemCount(3);
					for (ListItem item: field.getValues())
						list.addItem(item.getText(), item.getValue());
					String[] vals = record.getValues(index);
					if (vals != null) {
						for (String val: vals) {
							for (int i = 0; i < list.getItemCount(); i++)
								if (list.getValue(i).equals(val))
									list.setItemSelected(i, true);
						}
					}
					list.addChangeHandler(new ChangeHandler() {
						@Override
						public void onChange(ChangeEvent event) {
							record.setField(index, null);
							for (int i = 0; i < list.getItemCount(); i++ ) {
								if (list.isItemSelected(i)) record.addToField(index, list.getValue(i));
							}
						}
					});
					initWidget(list);
				} else if (field.getType() == FieldType.toggle) {
					final CheckBox check = new CheckBox();
					check.setValue(record.getField(index) == null ? null : "true".equalsIgnoreCase(record.getField(index)));
					check.addClickHandler(new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							record.setField(index, check.getValue() == null ? null : check.getValue() ? "true" : "false");
						}
					});
					initWidget(check);
				}
			} else {
				Label label = new Label(getValue());
				initWidget(label);
			}
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
		
		public void focus() { 
			if (getWidget() instanceof Focusable)
				((Focusable)getWidget()).setFocus(true);
			if (getWidget() instanceof TextBox)
				((TextBox)getWidget()).selectAll();
		}
	}
	
	public void setErrorMessage(String message) {
		iTopMessage.setStyleName("unitime-ErrorMessage");
		iTopMessage.setText(message);
		iTopMessage.setVisible(message != null && !message.isEmpty());
		iBottomMessage.setStyleName("unitime-ErrorMessage");
		iBottomMessage.setText(message);
		iBottomMessage.setVisible(message != null && !message.isEmpty());
	}
	
	public void setMessage(String message) {
		iTopMessage.setStyleName("unitime-Message");
		iTopMessage.setText(message);
		iTopMessage.setVisible(message != null && !message.isEmpty());
		iBottomMessage.setStyleName("unitime-Message");
		iBottomMessage.setText(message);
		iBottomMessage.setVisible(message != null && !message.isEmpty());
	}

	public void setPageName(String pageName) {
		((PageLabel)RootPanel.get("UniTimeGWT:Title").getWidget(0)).setPageName(pageName);
	}

	private class MyFlexTable extends FlexTable {
		private Timer iTimer;
		
		public MyFlexTable() {
			super();
			setCellPadding(2);
			setCellSpacing(0);
			sinkEvents(Event.ONMOUSEOVER);
			sinkEvents(Event.ONMOUSEOUT);
			sinkEvents(Event.ONKEYDOWN);
			setStylePrimaryName("unitime-MainTable");
			addStyleName("unitime-NotPrintableBottomLine");
			iTimer = new Timer() {
				@Override
				public void run() {
					saveOrder();
				}
			};
		}
		
		private boolean focus(Event event, int oldRow, int oldCol, int row, int col) {
			if (!getRowFormatter().isVisible(row) || col >= getCellCount(row)) return false;
			final Widget w = getWidget(row, col);
			if (w == null || !w.isVisible()) return false;
			if (w instanceof MyCell) {
				((MyCell)w).focus();
				event.stopPropagation();
				return true;
			}
			return false;
		}
		
		private void moveRow(Element tr, Element before) {
			Element body = DOM.getParent(tr);
			iData.moveRecord(DOM.getChildIndex(body, tr) - 1, DOM.getChildIndex(body, before) - 1);
			DOM.removeChild(body, tr);
			if (before == null)
				DOM.appendChild(body, tr);
			else
				DOM.insertBefore(body, tr, before);
		}
		
		public void saveOrder() {
			setMessage("Saving order...");
			String ord = "";
			for (int i = 1; i < getRowCount() - 1; i++) {
				Record r = ((MyCell)getWidget(i, 0)).getRecord();
				if (r == null || r.getUniqueId() == null) continue;
				if (!ord.isEmpty()) ord += "|";
				ord += r.getUniqueId();
			}
			List<String[]> data = new ArrayList<String[]>();
			data.add(new String[] {"SimpleEdit.Order[" + iType.toString() + "]", ord});
			iMenuService.setUserData(data, new AsyncCallback<Boolean>() {
				@Override
				public void onFailure(Throwable caught) {
					setErrorMessage("Failed to save table order (" + caught.getMessage() + ")");
				}
				@Override
				public void onSuccess(Boolean result) {
					setMessage(null);
				}
			});
		}
		
		public void onBrowserEvent(Event event) {
			Element td = getEventTargetCell(event);
			if (td==null) return;
		    Element tr = DOM.getParent(td);
			int col = DOM.getChildIndex(tr, td);
		    Element body = DOM.getParent(tr);
		    int row = DOM.getChildIndex(body, tr);
		    if (row == 0) return;
		    
			switch (DOM.eventGetType(event)) {
			case Event.ONMOUSEOVER:
				getRowFormatter().setStyleName(row, "unitime-TableRowHover");
				break;
			case Event.ONMOUSEOUT:
				getRowFormatter().setStyleName(row, null);	
				break;
			case Event.ONKEYDOWN:
				int oldRow = row, oldCol = col;
				if (event.getKeyCode() == KeyCodes.KEY_RIGHT && (event.getAltKey() || event.getMetaKey())) {
					do {
						col++;
						if (col >= getCellCount(row)) break;
					} while (!focus(event, oldRow, oldCol, row, col));
				}
				if (event.getKeyCode() == KeyCodes.KEY_LEFT && (event.getAltKey() || event.getMetaKey())) {
					do {
						col--;
						if (col < 0) break;
					} while (!focus(event, oldRow, oldCol, row, col));
				}
				if (event.getKeyCode() == KeyCodes.KEY_UP && (event.getAltKey() || event.getMetaKey())) {
					do {
						row--;
						if (row <= 0) break;
					} while (!focus(event, oldRow, oldCol, row, col));
				}
				if (event.getKeyCode() == KeyCodes.KEY_DOWN && (event.getAltKey() || event.getMetaKey())) {
					do {
						row++;
						if (row >= getRowCount()) break;
					} while (!focus(event, oldRow, oldCol, row, col));
				}
				if (event.getKeyCode() == KeyCodes.KEY_UP && event.getCtrlKey()) {
					MyCell u = (MyCell)getWidget(row, col);
					if (row > 1)
						moveRow(tr, DOM.getChild(body, row - 1));
					iTimer.schedule(5000);
			    	u.focus();
			    	event.stopPropagation();
			    	event.preventDefault();
				}
				if (event.getKeyCode() == KeyCodes.KEY_DOWN && event.getCtrlKey()) {
					MyCell u = (MyCell)getWidget(row, col);
					if (row < getRowCount() - 1)
						moveRow(DOM.getChild(body, row + 1), tr);
					iTimer.schedule(5000);
			    	u.focus();
			    	event.stopPropagation();
			    	event.preventDefault();
				}
				break;
		    }
		}
	}
}
