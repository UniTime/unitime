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
package org.unitime.timetable.gwt.client.admin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.client.widgets.ServerDateTimeFormat;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader.HasColumnName;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader.Operation;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.TaskInterface.TaskExecutionInterface;
import org.unitime.timetable.gwt.shared.TaskInterface.TaskOptionsInterface;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant;

/**
 * @author Tomas Muller
 */
public class TaskExecutionsTable extends UniTimeTable<TaskExecutionInterface> implements TakesValue<Collection<TaskExecutionInterface>>{
	protected static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	private static final GwtConstants CONSTANTS = GWT.create(GwtConstants.class);
	private TaskExecutionsTableColumn iSortBy = null;
	private boolean iAsc = true;
	private TaskOptionsInterface iOptions;
	
	private static DateTimeFormat sDateFormatMeeting = ServerDateTimeFormat.getFormat(CONSTANTS.meetingDateFormat());
	private static DateTimeFormat sDateFormatTS = ServerDateTimeFormat.getFormat(CONSTANTS.timeStampFormatShort());
	
	public TaskExecutionsTable() {
		setStyleName("unitime-PeriodicTaskTable");
		
		List<UniTimeTableHeader> header = new ArrayList<UniTimeTableHeader>();
		for (TaskExecutionsTableColumn column: TaskExecutionsTableColumn.values()) {
			int nrCells = getNbrCells(column);
			for (int idx = 0; idx < nrCells; idx++) {
				UniTimeTableHeader h = new UniTimeTableHeader(getColumnName(column, idx), getColumnAlignment(column, idx));
				header.add(h);
			}
		}
		
		for (final TaskExecutionsTableColumn column: TaskExecutionsTableColumn.values()) {
			if (TaskExecutionsTableComparator.isApplicable(column) && getNbrCells(column) > 0) {
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
		
		setSortBy(AdminCookie.getInstance().getSortTaskExecutionsBy());
	}
	
	public void setOptions(TaskOptionsInterface options) { iOptions = options; }
	public TaskOptionsInterface getOptions() { return iOptions; }
	
	protected void doSort(TaskExecutionsTableColumn column) {
		if (column == iSortBy) {
			iAsc = !iAsc;
		} else {
			iSortBy = column;
			iAsc = true;
		}
		AdminCookie.getInstance().setSortTaskExecutionsBy(getSortBy());
		sort();
	}
	
	public boolean hasSortBy() { return iSortBy != null; }
	public int getSortBy() { return iSortBy == null ? 0 : iAsc ? 1 + iSortBy.ordinal() : -1 - iSortBy.ordinal(); }
	public void setSortBy(int sortBy) {
		if (sortBy == 0) {
			iSortBy = null;
			iAsc = true;
		} else if (sortBy > 0) {
			iSortBy = TaskExecutionsTableColumn.values()[sortBy - 1];
			iAsc = true;
		} else {
			iSortBy = TaskExecutionsTableColumn.values()[-1 - sortBy];
			iAsc = false;
		}
		sort();
	}
	
	public void sort() {
		if (iSortBy == null) return;
		if (getNbrCells(iSortBy) == 0) iSortBy = TaskExecutionsTableColumn.DATE;
		UniTimeTableHeader header = getHeader(getCellIndex(iSortBy));
		sort(header, new TaskExecutionsTableComparator(iSortBy, true), iAsc);
	}
	
	protected int getNbrCells(TaskExecutionsTableColumn column) {
		switch (column) {
		default:
			return 1;
		}
	}
	
	public String getColumnName(TaskExecutionsTableColumn column, int idx) {
		switch (column) {
		case DATE: return MESSAGES.colTaskScheduleDate();
		case TIME: return MESSAGES.colTaskScheduleTime();
		case QUEUED: return MESSAGES.colTaskQueued();
		case STARTED: return MESSAGES.colTaskStarted();
		case FINISHED: return MESSAGES.colTaskFinished();
		case STATUS: return MESSAGES.colTaskStatus();
		case MESSAGE: return MESSAGES.colTaskStatusMessage();
		case OUTPUT: return MESSAGES.colTaskOutput();
		default: return column.name();
		}
	}
	
	public int getColumnIndex(TaskExecutionsTableColumn column) {
		int before = 0;
		for (TaskExecutionsTableColumn c: TaskExecutionsTableColumn.values()) {
			if (c.equals(column)) return before;
			before += getNbrCells(c);
		}
		return -1;
	}
	
	protected HorizontalAlignmentConstant getColumnAlignment(TaskExecutionsTableColumn column, int idx) {
		switch (column) {
		default:
			return HasHorizontalAlignment.ALIGN_LEFT;
		}
	}
	
	protected int getCellIndex(TaskExecutionsTableColumn column) {
		int ret = 0;
		for (TaskExecutionsTableColumn c: TaskExecutionsTableColumn.values())
			if (c.ordinal() < column.ordinal()) ret += getNbrCells(c);
		return ret;
	}
	
	protected Widget getCell(final TaskExecutionInterface e, final TaskExecutionsTableColumn column, final int idx) {
		switch (column) {
		case DATE: return new Label(sDateFormatMeeting.format(e.getExecutionDate()));
		case TIME: return new Label(e.getExecutionTime(CONSTANTS));
		case QUEUED: return new Label(e.getQueued() == null ? "" : sDateFormatTS.format(e.getQueued()));
		case STARTED: return new Label(e.getStarted() == null ? "" : sDateFormatTS.format(e.getStarted()));
		case FINISHED: return new Label(e.getFinished() == null ? "" : sDateFormatTS.format(e.getFinished()));
		case STATUS: return new Label(CONSTANTS.taskStatus()[e.getStatus().ordinal()]);
		case MESSAGE:
			Label message = new Label(e.getStatusMessage() == null ? "" : e.getStatusMessage()); message.addStyleName("status-message");
			if (e.getStatusMessage() != null)
				message.setTitle(e.getStatusMessage());
			return message;
		case OUTPUT:
			if (e.getOutput() != null) return new Anchor(e.getOutput(), GWT.getHostPageBaseURL() + "/taskfile?e=" + e.getId());
			return new Label("");
		default:
			return null;
		}
	}
	
	public int getRow(Long execId) {
		if (execId == null) return -1;
		for (int row = 1; row < getRowCount(); row ++) {
			TaskExecutionInterface task = getData(row);
			if (task != null && execId.equals(task.getId())) return row;
		}
		return -1;
	}
	
	public void scrollToTaskExecution(Long execId) {
		if (execId == null) return;
		for (int row = 1; row < getRowCount(); row ++) {
			TaskExecutionInterface task = getData(row);
			if (task != null && execId.equals(task.getId())) {
				getRowFormatter().getElement(row).scrollIntoView();
				return;
			}
		}
	}
	
	public static class TaskExecutionsTableComparator implements Comparator<TaskExecutionInterface> {
		private TaskExecutionsTableColumn iColumn;
		private boolean iAsc;
		
		public TaskExecutionsTableComparator(TaskExecutionsTableColumn column, boolean asc) {
			iColumn = column;
			iAsc = asc;
		}
		
		protected int compareByTime(TaskExecutionInterface e1, TaskExecutionInterface e2) {
			int cmp = new Integer(e1.getSlot()).compareTo(e2.getSlot());
			if (cmp != 0) return cmp;
			return e1.getDayOfYear().compareTo(e2.getDayOfYear());
		}

		protected int compareByDate(TaskExecutionInterface e1, TaskExecutionInterface e2) {
			int cmp = e1.getDayOfYear().compareTo(e2.getDayOfYear());
			if (cmp != 0) return cmp;
			return new Integer(e1.getSlot()).compareTo(e2.getSlot());
		}

		protected int compareByStatus(TaskExecutionInterface e1, TaskExecutionInterface e2) {
			return (e1 == null ? e2 == null ? 0 : -1 : e2 == null ? 1 : e1.compareTo(e2));
		}
		
		protected int compareByColumn(TaskExecutionInterface e1, TaskExecutionInterface e2) {
			switch (iColumn) {
			case DATE: return compareByDate(e1, e2);
			case TIME: return compareByTime(e1, e2);
			case STATUS: return compareByStatus(e1, e2);
			case QUEUED: return compare(e1.getQueued(), e2.getQueued());
			case FINISHED: return compare(e1.getFinished(), e1.getFinished());
			case STARTED: return compare(e1.getStarted(), e2.getStarted());
			case MESSAGE: return compare(e1.getStatusMessage(), e2.getStatusMessage());
			case OUTPUT: return compare(e1.getOutput(), e2.getOutput());
			default:
				return e1.compareTo(e2);
			}
		}
		
		public static boolean isApplicable(TaskExecutionsTableColumn column) {
			switch (column) {
			case DATE:
			case TIME:
			case STATUS:
			case QUEUED:
			case FINISHED:
			case STARTED:
			case MESSAGE:
			case OUTPUT:
				return true;
			default:
				return false;
			}
		}

		@Override
		public int compare(TaskExecutionInterface e1, TaskExecutionInterface e2) {
			int cmp = compareByColumn(e1, e2);
			if (cmp != 0) return (iAsc ? cmp : -cmp);
			return (iAsc ? e1.compareTo(e2) : e2.compareTo(e1));
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

    public static enum TaskExecutionsTableColumn {
		DATE,
		TIME,
		QUEUED,
		STARTED,
		FINISHED,
		STATUS,
		MESSAGE,
		OUTPUT,
		;
	}
    
    public int addExecution(final TaskExecutionInterface exec) {
		List<Widget> widgets = new ArrayList<Widget>();
		for (TaskExecutionsTableColumn column: TaskExecutionsTableColumn.values()) {
			int nbrCells = getNbrCells(column);
			for (int idx = 0; idx < nbrCells; idx ++) {
				Widget cell = getCell(exec, column, idx);
				if (cell == null)
					cell = new P();
				widgets.add(cell);
			}
		}
		for (Widget w: widgets)
			if (w != null)
				w.addStyleName("status-" + exec.getStatus().name().toLowerCase());
		int row = addRow(exec, widgets);
		getRowFormatter().setStyleName(row, "row");
		for (int col = 0; col < getCellCount(row); col++)
			getCellFormatter().setStyleName(row, col, "cell");
		return row;
	}

	@Override
	public void setValue(Collection<TaskExecutionInterface> value) {
		clearTable(1);
    	boolean hasOutput = false;
    	if (value != null) {
    		for (TaskExecutionInterface exec: value) {
    			addExecution(exec);
    			if (exec.getOutput() != null) hasOutput = true;
    		}
    	}
    	setColumnVisible(getColumnIndex(TaskExecutionsTableColumn.OUTPUT), hasOutput);
    	sort();
	}

	@Override
	public Collection<TaskExecutionInterface> getValue() {
		return getData();
	}
	
	public static interface SortOperation extends Operation, HasColumnName {}
}
