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
package org.unitime.timetable.gwt.client.tables;

import java.util.ArrayList;
import java.util.List;

import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.aria.AriaButton;
import org.unitime.timetable.gwt.client.sectioning.CourseDetailsWidget;
import org.unitime.timetable.gwt.client.tables.TableInterface.CellInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.LineInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.PropertyInterface;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.client.widgets.UniTimeConfirmationDialog;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.resources.GwtResources;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.Style.VerticalAlign;
import com.google.gwt.dom.client.Style.WhiteSpace;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant;
import com.google.gwt.user.client.ui.HasVerticalAlignment.VerticalAlignmentConstant;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class TableWidget extends UniTimeTable<LineInterface> {
	private static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	private static final GwtResources RESOURCES = GWT.create(GwtResources.class);

	public TableWidget() {
		super();
		addMouseClickListener(new MouseClickListener<LineInterface>() {
			@Override
			public void onMouseClick(TableEvent<LineInterface> event) {
				if (event.getData() != null && event.getData().hasURL()) {
					LoadingWidget.showLoading(MESSAGES.waitLoadingPage());
					ToolBox.open(GWT.getHostPageBaseURL() + event.getData().getURL());
				}
			}
		});
	}
	
	public TableWidget(TableInterface table) {
		this();
		setData(table);
	}
	
	@Override
	protected boolean showHower(int row, LineInterface data) {
		return data != null && data.hasURL();
	}
	
	protected static void applyStyle(Style style, String text) {
		if (text == null || text.isEmpty())
			return;
		for (String param: text.split(";")) {
			if (param.indexOf(':') < 0) continue;
			String key = param.substring(0, param.indexOf(':')).trim();
			String value = param.substring(param.indexOf(':') + 1).trim();
			if (value.isEmpty())
				style.clearProperty(key);
			else
				style.setProperty(key, value);
		}
	}
	
	public void setData(TableInterface table) {
		clearTable();
		if (table.getHeader() != null)
			for (LineInterface line: table.getHeader()) {
				if (line.hasCells()) {
					List<CellWidget> cells = new ArrayList<CellWidget>();
					for (CellInterface cell: line.getCells())
						cells.add(new CellWidget(cell, false));
					int row = addRow(line, cells);
					if (line.hasBgColor())
						getRowFormatter().getElement(row).getStyle().setBackgroundColor(line.getBgColor());
					if (line.hasTitle())
						getRowFormatter().getElement(row).setTitle(line.getTitle());
					if (line.hasStyle())
						applyStyle(getRowFormatter().getElement(row).getStyle(), line.getStyle());
				}
			}
		if (table.hasErrorMessage()) {
			List<Widget> cells = new ArrayList<Widget>();
			cells.add(new ErrorWidget(table));
			addRow(null, cells);
		}	
		if (table.getLines() != null)
			for (final LineInterface line: table.getLines()) {
				if (line.hasCells()) {
					if (line.hasWarning()) line.getCells().get(0).setWarning(line.getWarning());
					List<CellWidget> cells = new ArrayList<CellWidget>();
					for (CellInterface cell: line.getCells()) {
						cells.add(new CellWidget(cell, false));
					}
					int row = addRow(line, cells);
					if (line.hasBgColor())
						getRowFormatter().getElement(row).getStyle().setBackgroundColor(line.getBgColor());
					if (line.hasTitle())
						getRowFormatter().getElement(row).setTitle(line.getTitle());
					if (line.hasStyle())
						applyStyle(getRowFormatter().getElement(row).getStyle(), line.getStyle());
				}
			}
		if (table.hasProperties()) {
			for (PropertyInterface property: table.getProperties()) {
				List<Widget> cells = new ArrayList<Widget>();
				if (property.getName() == null) {
					property.getCell().setColSpan(2);
				} else {
					Label label = new Label(property.getName(), false);
					cells.add(label);
				}
				CellWidget cell = new CellWidget(property.getCell());
				cells.add(cell);
				LineInterface line = new LineInterface();
				line.addCell(property.getName());
				line.getCells().add(property.getCell());
				int row = addRow(line, cells);
				if (property.getName() != null) {
					getCellFormatter().getElement(row, 0).addClassName("label-td");
					getCellFormatter().getElement(row, 1).addClassName("widget-td");
					if (property.hasStyle())
						applyStyle(getCellFormatter().getElement(row, 1).getStyle(), property.getStyle());
				} else {
					getCellFormatter().getElement(row, 0).addClassName("widget-td");
					if (property.hasStyle())
						applyStyle(getCellFormatter().getElement(row, 0).getStyle(), property.getStyle());
				}
			}
		}
	}
	
	public static class ErrorWidget extends P implements HasColSpan {
		private TableInterface iTable;
		
		ErrorWidget(TableInterface table) {
			super("unitime-ErrorMessage");
			setText(table.getErrorMessage());
			iTable = table;
		}

		@Override
		public int getColSpan() {
			int cols = iTable.getMaxColumns();
			return (cols <= 0 ? 1 : cols);
		}
	}
	
	
	public static class CellWidget extends P implements HasColSpan, HasRowSpan, HasCellAlignment, HasVerticalCellAlignment, HasStyleName {
		private CellInterface iCell;
		
		public CellWidget(final CellInterface cell) {
			this(cell, false);
		}
		
		public CellWidget(final CellInterface cell, boolean applyClass) {
			super(cell.isInline() ? DOM.createSpan() : DOM.createDiv());
			iCell = cell;
			if (cell.hasWarning()) {
				Image warning = new Image(RESOURCES.warning());
				warning.setTitle(cell.getWarning());
				warning.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent e) {
						UniTimeConfirmationDialog.alert(cell.getWarning());
						e.preventDefault(); e.getNativeEvent().stopPropagation();
					}
				});
				warning.getElement().getStyle().setPaddingRight(2, Unit.PX);
				warning.getElement().getStyle().setVerticalAlign(VerticalAlign.TOP);
				add(warning);
			}
			if (cell.hasText()) {
				if (cell.isHtml()) {
					if (cell.hasWarning()) {
						P p = new P(DOM.createSpan()); p.setHTML(cell.getText()); add(p);
					} else {
						setHTML(cell.getText());
					}
				} else {
					if (cell.hasWarning()) {
						P p = new P(DOM.createSpan()); p.setText(cell.getText()); add(p);
					} else {
						setText(cell.getText());
					}
				}
			} else if (!cell.hasItems() && !cell.hasWarning() && !cell.hasCourseLink() && cell.getTable() == null && !cell.hasScript())
				setText("\u202F");
			if (cell.hasTitle())
				setTitle(cell.getTitle());
			if (cell.hasAnchors())
				for (String anchor: cell.getAnchors()) {
					Anchor a = new Anchor();
					a.setName(anchor);
					a.getElement().setId(anchor);
					add(a);
				}
			if (cell.hasStyle())
				applyStyle(getElement().getStyle(), cell.getStyle());
			if (applyClass && cell.hasClassName())
				addStyleName(cell.getClassName());
			if (cell.hasImage()) {
				Image img = new Image(cell.getImage().getSource());
				if (cell.getImage().hasAlt())
					img.setAltText(cell.getImage().getAlt());
				if (cell.getImage().hasTitle())
					img.setTitle(cell.getImage().getTitle());
				if (cell.getImage().hasStyle()) {
					applyStyle(img.getElement().getStyle(), cell.getImage().getStyle());
				} else {
					img.getElement().getStyle().setPaddingRight(2, Unit.PX);
					img.getElement().getStyle().setVerticalAlign(VerticalAlign.TOP);
				}
				add(img);
			}
			if (cell.hasColor())
				getElement().getStyle().setColor(cell.getColor());
			if (cell.isNoWrap())
				getElement().getStyle().setWhiteSpace(WhiteSpace.NOWRAP);
			else if (cell.isWrap())
				getElement().getStyle().setWhiteSpace(WhiteSpace.PRE_WRAP);
			if (cell.hasMouseOver())
				addMouseOverHandler(new MouseOverHandler() {
					@Override
					public void onMouseOver(MouseOverEvent e) {
						setLastMouseOverElement(getElement());
						ToolBox.eval(iCell.getMouseOver());
					}
				});
			if (cell.hasMouseOut())
				addMouseOutHandler(new MouseOutHandler() {
					@Override
					public void onMouseOut(MouseOutEvent e) {
						ToolBox.eval(iCell.getMouseOut());
					}
				});
			if (cell.hasMouseClick()) {
				addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent e) {
						setLastMouseOverElement(getElement());
						ToolBox.eval(iCell.getMouseClick());
					}
				});
			}
			if (cell.hasIndent())
				getElement().getStyle().setPaddingLeft(cell.getIndent() * 10.0, Unit.PX);
			if (cell.hasItems())
				for (CellInterface item: cell.getItems())
					add(new CellWidget(item, true));
			if (cell.getTable() != null) {
				TableWidget tw = new TableWidget(cell.getTable());
				tw.setStyleName("unitime-InnerTable");
				add(tw);
			}
			if (cell.hasUrl()) {
				addStyleName("clickable-cell");
				addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent evt) {
						ToolBox.open(GWT.getHostPageBaseURL() + cell.getUrl());
					}
				});
			}
			if (cell.hasButton()) {
				AriaButton button = new AriaButton(cell.getButton().getText());
				if (cell.getButton().hasTitle()) button.setTitle(cell.getButton().getTitle());
				add(button);
				button.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent evt) {
						ToolBox.open(GWT.getHostPageBaseURL() + cell.getButton().getUrl());
					}
				});
			}
			if (cell.hasCourseLink()) {
				add(new CourseDetailsWidget(cell.getCourseLink().isAnchor()).forCourseId(cell.getCourseLink().getCourseId()));
			}
			if (cell.hasScript()) {
				final Element element = getElement();
				Scheduler.get().scheduleDeferred(new ScheduledCommand() {
					@Override
					public void execute() {
						executeScript(element, cell.getScript());
					}
				});
			}
		}
		
		public static native void populate()/*-{
			
		$wnd.showGwtHint = function(source, content) {
			@org.unitime.timetable.gwt.client.GwtHint::_showHint(Lcom/google/gwt/core/client/JavaScriptObject;Ljava/lang/String;)(source, content);
		};
		$wnd.hideGwtHint = function() {
			@org.unitime.timetable.gwt.client.GwtHint::hideHint()();
		};
		}-*/;

		@Override
		public int getColSpan() {
			return iCell.getColSpan();
		}
		
		@Override
		public int getRowSpan() {
			return iCell.getRowSpan();
		}

		@Override
		public VerticalAlignmentConstant getVerticalCellAlignment() {
			switch (iCell.getHorizontalAlignment()) {
			case TOP: return HasVerticalAlignment.ALIGN_TOP;
			case MIDLE: return HasVerticalAlignment.ALIGN_MIDDLE;
			case BOTTOM: return HasVerticalAlignment.ALIGN_BOTTOM;
			}
			return null;
		}

		@Override
		public HorizontalAlignmentConstant getCellAlignment() {
			switch (iCell.getTextAlignment()) {
			case LEFT: return HasHorizontalAlignment.ALIGN_LEFT;
			case CENTER: return HasHorizontalAlignment.ALIGN_CENTER;
			case RIGHT: return HasHorizontalAlignment.ALIGN_RIGHT;
			}
			return null;
		}
		
		@Override
		public String getStyleName() {
			return iCell.getClassName();
		}
	}
	
	public native static void setLastMouseOverElement(Element element)/*-{
		$wnd.lastMouseOverElement = element;
	}-*/;

	public native static void executeScript(Element element, String script)/*-{
		element.innerHTML = eval(script);
	}-*/;

}
