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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.aria.AriaButton;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.client.widgets.UniTimeConfirmationDialog;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader.HasColumnName;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader.Operation;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseList;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.resources.GwtResources;
import org.unitime.timetable.gwt.shared.PublishedSectioningSolutionInterface;
import org.unitime.timetable.gwt.shared.PublishedSectioningSolutionInterface.PublishedSectioningSolutionsRequest;
import org.unitime.timetable.gwt.shared.PublishedSectioningSolutionInterface.TableColumn;
import org.unitime.timetable.gwt.shared.TableInterface.NaturalOrderComparator;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant;

/**
 * @author Tomas Muller
 */
public class PublishedSectioningSolutionsTable extends UniTimeTable<PublishedSectioningSolutionInterface> implements TakesValue<List<PublishedSectioningSolutionInterface>> {
	protected static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	protected static final GwtResources RESOURCES = GWT.create(GwtResources.class);
	protected static final GwtConstants CONSTANTS = GWT.create(GwtConstants.class);
	private static DateTimeFormat sDF = DateTimeFormat.getFormat(CONSTANTS.timeStampFormat());
	protected static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	
	private TableColumn iSortBy = null;
	private boolean iAsc = true;
	
	public PublishedSectioningSolutionsTable() {
		setStyleName("unitime-PublishedSectioningSolutions");
		
		List<UniTimeTableHeader> header = new ArrayList<UniTimeTableHeader>();
		for (TableColumn column: TableColumn.values()) {
			int nrCells = getNbrCells(column);
			for (int idx = 0; idx < nrCells; idx++) {
				UniTimeTableHeader h = new UniTimeTableHeader(getColumnName(column, idx), getColumnAlignment(column, idx));
				header.add(h);
			}
		}
		
		for (final TableColumn column: TableColumn.values()) {
			if (TableComparator.isApplicable(column) && getNbrCells(column) > 0) {
				final UniTimeTableHeader h = header.get(getCellIndex(column));
				Operation op = new SortOperation() {
					@Override
					public void execute() {
						doSort(column);
					}
					@Override
					public boolean isApplicable() { return getRowCount() > 1 && h.isVisible(); }
					@Override
					public boolean hasSeparator() { return false; }
					@Override
					public String getName() { return MESSAGES.opSortBy(getColumnName()); }
					@Override
					public String getColumnName() { return h.getHTML().replace("<br>", " "); }
				};
				h.addOperation(op);
			}
		}
		
		addRow(null, header);
		
		for (int i = 0; i < getCellCount(0); i++)
			getCellFormatter().setStyleName(0, i, "unitime-ClickableTableHeader");
		
		setSortBy(SectioningCookie.getInstance().getSolutionsSortBy());
	}
	
	protected void doSort(TableColumn column) {
		if (column == iSortBy) {
			iAsc = !iAsc;
		} else {
			iSortBy = column;
			iAsc = true;
		}
		SectioningCookie.getInstance().setSolutionsSortBy(getSortBy());
		sort();
	}
	
	public boolean hasSortBy() { return iSortBy != null; }
	public int getSortBy() { return iSortBy == null ? 0 : iAsc ? 1 + iSortBy.ordinal() : -1 - iSortBy.ordinal(); }
	public void setSortBy(int sortBy) {
		if (sortBy == 0) {
			iSortBy = null;
			iAsc = true;
		} else if (sortBy > 0) {
			iSortBy = TableColumn.values()[sortBy - 1];
			iAsc = true;
		} else {
			iSortBy = TableColumn.values()[-1 - sortBy];
			iAsc = false;
		}
		sort();
	}
	
	public void sort() {
		if (iSortBy == null) return;
		if (getNbrCells(iSortBy) == 0) iSortBy = TableColumn.DATE_TIME;
		UniTimeTableHeader header = getHeader(getCellIndex(iSortBy));
		sort(header, new TableComparator(iSortBy, true), iAsc);
	}
	
	protected int getNbrCells(TableColumn column) {
		return 1;
	}
	
	public String getColumnName(TableColumn column, int idx) {
		switch (column) {
		case DATE_TIME: return MESSAGES.colTimeStamp();
		case OWNER: return MESSAGES.colOwner();
		case COURSE_REQUESTS: return MESSAGES.colAssignedCourseRequests();
		case PRIORITY_REQUESTS: return MESSAGES.colAssignedPriorityCourseRequests();
		case TIME: return MESSAGES.colTimeConflicts();
		case DISTANCE: return MESSAGES.colDistanceConflicts();
		case SELECTION: return MESSAGES.colSectioningSelection();
		case OPERATIONS: return MESSAGES.colOperations();
		case COMPLETE: return MESSAGES.colStudentsWithCompleteSchedule();
		case DISBALANCED: return MESSAGES.colDisbalancedSections();
		case NO_TIME: return MESSAGES.colClassesWithoutTime();
		default: return column.name();
		}
	}
	
	protected HorizontalAlignmentConstant getColumnAlignment(TableColumn column, int idx) {
		switch (column) {
		default:
			return HasHorizontalAlignment.ALIGN_LEFT;
		}
	}
	
	protected int getCellIndex(TableColumn column) {
		int ret = 0;
		for (TableColumn c: TableColumn.values())
			if (c.ordinal() < column.ordinal()) ret += getNbrCells(c);
		return ret;
	}
	
	protected Widget getCell(final PublishedSectioningSolutionInterface solution, final TableColumn column, final int idx) {
		switch (column) {
		case DATE_TIME:
			return new Label(sDF.format(solution.getTimeStamp()));
		case OWNER:
			return new Label(solution.getOwner());
		case OPERATIONS:
			return new OperationsCell(solution);
		default:
			return new Label(solution.getValue(column.getAttribute()));
		}
	}
	
	public int addSolution(final PublishedSectioningSolutionInterface solution) {
		List<Widget> widgets = new ArrayList<Widget>();
		
		for (TableColumn column: TableColumn.values()) {
			int nbrCells = getNbrCells(column);
			for (int idx = 0; idx < nbrCells; idx ++) {
				Widget cell = getCell(solution, column, idx);
				if (cell == null)
					cell = new P();
				widgets.add(cell);
			}
		}
		
		int row = addRow(solution, widgets);
		getRowFormatter().setStyleName(row, "row");
		for (int col = 0; col < getCellCount(row); col++)
			getCellFormatter().setStyleName(row, col, "cell");
		if (solution.isClonned())
			getRowFormatter().addStyleName(row, "clonned");
		if (solution.isSelected())
			getRowFormatter().addStyleName(row, "selected");
		if (solution.isLoaded())
			getRowFormatter().addStyleName(row, "loaded");
		
		return row;
	} 
	
	public PublishedSectioningSolutionInterface getFeature(Long solutionId) {
		if (solutionId == null) return null;
		for (int i = 1; i < getRowCount(); i++) {
			if (solutionId.equals(getData(i).getUniqueId())) return getData(i);
		}
		return null;
	}
	
	public void scrollTo(Long solutionId) {
		if (solutionId == null) return;
		for (int i = 1; i < getRowCount(); i++) {
			if (solutionId.equals(getData(i).getUniqueId())) {
				ToolBox.scrollToElement(getRowFormatter().getElement(i));
				return;
			}
		}
	}
	
	public class OperationsCell extends P {
		public OperationsCell(final PublishedSectioningSolutionInterface solution) {
			super("operations");
			final AsyncCallback<GwtRpcResponseList<PublishedSectioningSolutionInterface>> callback = new AsyncCallback<GwtRpcResponseList<PublishedSectioningSolutionInterface>>() {
				@Override
				public void onSuccess(GwtRpcResponseList<PublishedSectioningSolutionInterface> result) {
					LoadingWidget.getInstance().hide();
					setValue(result);
				}
				@Override
				public void onFailure(Throwable caught) {
					LoadingWidget.getInstance().hide();
					UniTimeNotifications.error(caught);
				}
			};
			final AsyncCallback<GwtRpcResponseList<PublishedSectioningSolutionInterface>> callbackOpenSolver = new AsyncCallback<GwtRpcResponseList<PublishedSectioningSolutionInterface>>() {
				@Override
				public void onSuccess(GwtRpcResponseList<PublishedSectioningSolutionInterface> result) {
					LoadingWidget.getInstance().hide();
					ToolBox.open(GWT.getHostPageBaseURL() + "gwt.jsp?page=solver&type=student");
				}
				@Override
				public void onFailure(Throwable caught) {
					LoadingWidget.getInstance().hide();
					UniTimeNotifications.error(caught);
				}
			};
			if (solution.isLoaded()) {
				addButton(MESSAGES.opSectioningSolutionUnpublish(), new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						LoadingWidget.getInstance().show(MESSAGES.waitPlease());
						RPC.execute(new PublishedSectioningSolutionsRequest(PublishedSectioningSolutionInterface.Operation.UNPUBLISH, solution.getUniqueId()), callback);
						event.preventDefault(); event.stopPropagation();
					}
				});
				if (solution.isSelected()) {
					addButton(MESSAGES.opSectioningSolutionDeselect(), new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							LoadingWidget.getInstance().show(MESSAGES.waitPlease());
							RPC.execute(new PublishedSectioningSolutionsRequest(PublishedSectioningSolutionInterface.Operation.DESELECT, solution.getUniqueId()), callback);
							event.preventDefault(); event.stopPropagation();
						}
					});
				} else if (solution.isCanSelect()) {
					addButton(MESSAGES.opSectioningSolutionSelect(), new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							LoadingWidget.getInstance().show(MESSAGES.waitPlease());
							RPC.execute(new PublishedSectioningSolutionsRequest(PublishedSectioningSolutionInterface.Operation.SELECT, solution.getUniqueId()), callbackOpenSolver);
							event.preventDefault(); event.stopPropagation();
						}
					});
				}
			} else if (solution.isCanLoad()) {
				addButton(MESSAGES.opSectioningSolutionPublish(), new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						LoadingWidget.getInstance().show(MESSAGES.waitPlease());
						RPC.execute(new PublishedSectioningSolutionsRequest(PublishedSectioningSolutionInterface.Operation.PUBLISH, solution.getUniqueId()), callback);
						event.preventDefault(); event.stopPropagation();
					}
				});
			}
			if (solution.isClonned()) {
				addButton(MESSAGES.opSectioningSolutionUnload(), new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						RPC.execute(new PublishedSectioningSolutionsRequest(PublishedSectioningSolutionInterface.Operation.UNLOAD, solution.getUniqueId()), callback);
						event.preventDefault(); event.stopPropagation();
					}
				});
			} else if (solution.isCanClone()) {
				addButton(MESSAGES.opSectioningSolutionLoad(), new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						LoadingWidget.getInstance().show(MESSAGES.waitPlease());
						RPC.execute(new PublishedSectioningSolutionsRequest(PublishedSectioningSolutionInterface.Operation.LOAD, solution.getUniqueId()), callbackOpenSolver);
						event.preventDefault(); event.stopPropagation();
					}
				});
			}
			if (!solution.isLoaded()) {
				addButton(MESSAGES.opSectioningSolutionRemove(), new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						UniTimeConfirmationDialog.confirm(MESSAGES.confirmDeletePublishedSectioningSolution(), new Command() {
							@Override
							public void execute() {
								LoadingWidget.getInstance().show(MESSAGES.waitPlease());
								RPC.execute(new PublishedSectioningSolutionsRequest(PublishedSectioningSolutionInterface.Operation.REMOVE, solution.getUniqueId()), callback);
							}
						});
						event.preventDefault(); event.stopPropagation();
					}
				});
			}

			addButton(MESSAGES.opExportXML(), new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					ToolBox.open(GWT.getHostPageBaseURL() + "export?output=published-solution.xml.gz&id=" + solution.getUniqueId());
					event.preventDefault(); event.stopPropagation();
				}
			});
		}
		
		private void addButton(String html, ClickHandler handler) {
			AriaButton button = new AriaButton(html);
			button.addClickHandler(handler);
			add(button);
		}
	}
	
	public static class TableComparator implements Comparator<PublishedSectioningSolutionInterface> {
		private TableColumn iColumn;
		private boolean iAsc;
		
		public TableComparator(TableColumn column, boolean asc) {
			iColumn = column;
			iAsc = asc;
		}

		public int compareById(PublishedSectioningSolutionInterface r1, PublishedSectioningSolutionInterface r2) {
			return compare(r1.getUniqueId(), r2.getUniqueId());
		}
		
		public int compareByTimeStamp(PublishedSectioningSolutionInterface r1, PublishedSectioningSolutionInterface r2) {
			return compare(r1.getTimeStamp(), r2.getTimeStamp());
		}

		public int compareByOwner(PublishedSectioningSolutionInterface r1, PublishedSectioningSolutionInterface r2) {
			return compare(r1.getOwner(), r2.getOwner());
		}

		
		public int compareByAttribute(String attribute, PublishedSectioningSolutionInterface r1, PublishedSectioningSolutionInterface r2) {
			String v1 = r1.getValue(attribute), v2 = r2.getValue(attribute);
			return NaturalOrderComparator.compare(v1, v2);
		}
		
		protected int compareByColumn(PublishedSectioningSolutionInterface r1, PublishedSectioningSolutionInterface r2) {
			switch (iColumn) {
			case DATE_TIME: return compareByTimeStamp(r1, r2);
			case OWNER: return compareByOwner(r1, r2);
			default:
				String att = iColumn.getAttribute();
				if (att != null)
					return compareByAttribute(att, r1, r2);
				else
					return compareById(r1, r2);
			}
		}
		
		public static boolean isApplicable(TableColumn column) {
			switch (column) {
			case DATE_TIME:
			case OWNER:
				return true;
			case OPERATIONS:
				return false;
			default:
				return column.getAttribute() != null;
			}
		}
		
		@Override
		public int compare(PublishedSectioningSolutionInterface r1, PublishedSectioningSolutionInterface r2) {
			int cmp = compareByColumn(r1, r2);
			if (cmp == 0) cmp = compareByTimeStamp(r1, r2);
			if (cmp == 0) cmp = compareById(r1, r2);
			return (iAsc ? cmp : -cmp);
		}
		
		protected int compare(String s1, String s2) {
			if (s1 == null || s1.isEmpty()) {
				return (s2 == null || s2.isEmpty() ? 0 : 1);
			} else {
				return (s2 == null || s2.isEmpty() ? -1 : s1.compareToIgnoreCase(s2));
			}
		}
		
		protected int compare(Number n1, Number n2) {
			return (n1 == null ? n2 == null ? 0 : -1 : n2 == null ? 1 : Double.compare(n1.doubleValue(), n2.doubleValue())); 
		}
		
		protected int compare(Date n1, Date n2) {
			return (n1 == null ? n2 == null ? 0 : -1 : n2 == null ? 1 : n1.compareTo(n2)); 
		}
		
		protected int compare(Boolean b1, Boolean b2) {
			return (b1 == null ? b2 == null ? 0 : -1 : b2 == null ? 1 : (b1.booleanValue() == b2.booleanValue()) ? 0 : (b1.booleanValue() ? 1 : -1));
		}
	}
	
	public static interface SortOperation extends Operation, HasColumnName {}

	@Override
	public void setValue(List<PublishedSectioningSolutionInterface> value) {
		clearTable(1);
		if (value != null)
			for (PublishedSectioningSolutionInterface solution: value)
				addSolution(solution);
		sort();
	}

	@Override
	public List<PublishedSectioningSolutionInterface> getValue() {
		return getData();
	}
}
