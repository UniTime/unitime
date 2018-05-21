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
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.unitime.timetable.gwt.client.GwtHint;
import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.client.widgets.ServerDateTimeFormat;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader;
import org.unitime.timetable.gwt.client.widgets.TimeSelector.TimeUtils;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader.HasColumnName;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader.Operation;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.TaskInterface;
import org.unitime.timetable.gwt.shared.ScriptInterface.ScriptParameterInterface;
import org.unitime.timetable.gwt.shared.TaskInterface.ExecutionStatus;
import org.unitime.timetable.gwt.shared.TaskInterface.MultiExecutionInterface;
import org.unitime.timetable.gwt.shared.TaskInterface.TaskExecutionInterface;
import org.unitime.timetable.gwt.shared.TaskInterface.TaskOptionsInterface;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant;

/**
 * @author Tomas Muller
 */
public class TasksTable extends UniTimeTable<TaskInterface> implements TakesValue<List<TaskInterface>>{
	protected static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	private static final GwtConstants CONSTANTS = GWT.create(GwtConstants.class);
	private TasksTableColumn iSortBy = null;
	private boolean iAsc = true;
	private TaskOptionsInterface iOptions;
	
	private static DateTimeFormat sDateFormatShort = ServerDateTimeFormat.getFormat(CONSTANTS.eventDateFormatShort());
	private static DateTimeFormat sDateFormatLong = ServerDateTimeFormat.getFormat(CONSTANTS.eventDateFormatLong());

	public TasksTable() {
		setStyleName("unitime-PeriodicTaskTable");
		
		List<UniTimeTableHeader> header = new ArrayList<UniTimeTableHeader>();
		for (TasksTableColumn column: TasksTableColumn.values()) {
			int nrCells = getNbrCells(column);
			for (int idx = 0; idx < nrCells; idx++) {
				UniTimeTableHeader h = new UniTimeTableHeader(getColumnName(column, idx), getColumnAlignment(column, idx));
				header.add(h);
			}
		}
		
		for (final TasksTableColumn column: TasksTableColumn.values()) {
			if (TasksTableComparator.isApplicable(column) && getNbrCells(column) > 0) {
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
		
		setSortBy(AdminCookie.getInstance().getSortTasksBy());
	}
	
	public void setOptions(TaskOptionsInterface options) { iOptions = options; }
	public TaskOptionsInterface getOptions() { return iOptions; }
	
	protected void doSort(TasksTableColumn column) {
		if (column == iSortBy) {
			iAsc = !iAsc;
		} else {
			iSortBy = column;
			iAsc = true;
		}
		AdminCookie.getInstance().setSortTasksBy(getSortBy());
		sort();
	}
	
	public boolean hasSortBy() { return iSortBy != null; }
	public int getSortBy() { return iSortBy == null ? 0 : iAsc ? 1 + iSortBy.ordinal() : -1 - iSortBy.ordinal(); }
	public void setSortBy(int sortBy) {
		if (sortBy == 0) {
			iSortBy = null;
			iAsc = true;
		} else if (sortBy > 0) {
			iSortBy = TasksTableColumn.values()[sortBy - 1];
			iAsc = true;
		} else {
			iSortBy = TasksTableColumn.values()[-1 - sortBy];
			iAsc = false;
		}
		sort();
	}
	
	public void sort() {
		if (iSortBy == null) return;
		if (getNbrCells(iSortBy) == 0) iSortBy = TasksTableColumn.NAME;
		UniTimeTableHeader header = getHeader(getCellIndex(iSortBy));
		sort(header, new TasksTableComparator(iSortBy, true), iAsc);
	}
	
	protected int getNbrCells(TasksTableColumn column) {
		switch (column) {
		default:
			return 1;
		}
	}
	
	public String getColumnName(TasksTableColumn column, int idx) {
		switch (column) {
		case NAME: return MESSAGES.colTaskName();
		case SCRIPT: return MESSAGES.colTaskScript();
		case OWNER: return MESSAGES.colTaskOwner();
		case PARAMETERS: return MESSAGES.colTaskParameters();
		case DATE: return MESSAGES.colTaskScheduleDate();
		case TIME: return MESSAGES.colTaskScheduleTime();
		case STATUS: return MESSAGES.colTaskStatus();
		default: return column.name();
		}
	}
	
	protected HorizontalAlignmentConstant getColumnAlignment(TasksTableColumn column, int idx) {
		switch (column) {
		default:
			return HasHorizontalAlignment.ALIGN_LEFT;
		}
	}
	
	protected int getCellIndex(TasksTableColumn column) {
		int ret = 0;
		for (TasksTableColumn c: TasksTableColumn.values())
			if (c.ordinal() < column.ordinal()) ret += getNbrCells(c);
		return ret;
	}
	
	protected Widget getCell(final TaskInterface task, final TasksTableColumn column, final int idx) {
		switch (column) {
		case NAME:
			return new Label(task.getName());
		case SCRIPT:
			return new Label(task.getScript().getName());
		case OWNER:
			return new Label(task.getOwner().getFormattedName(false));
		case PARAMETERS:
			String parameters = "";
			if (task.getScript().hasParameters())
				for (ScriptParameterInterface parameter: task.getScript().getParameters()) {
					String value = task.getParameter(parameter.getName());
					if (parameter.hasOptions() && value != null && !value.isEmpty()) {
						if (parameter.isMultiSelect()) {
							String ids = value;
							value = "";
							for (String id: ids.split(","))
								value += (value.isEmpty() ? "" : ", ") + parameter.getOption(id);
						} else {
							value = parameter.getOption(value);
						}
					}
					if ("slot".equalsIgnoreCase(parameter.getType()) || "time".equalsIgnoreCase(parameter.getType()) && value != null && !value.isEmpty()) {
						try {
							value = TimeUtils.slot2time(Integer.parseInt(value));
						} catch (Exception e) {}
					}
					if (value != null)
						parameters += (parameters.isEmpty() ? "" : "<br>") + parameter.getLabel() + ": " + value;
				}
			final P p = new P("parameters"); p.setHTML(parameters);
			p.addMouseOverHandler(new MouseOverHandler() {
				@Override
				public void onMouseOver(MouseOverEvent event) {
					GwtHint.showHint(event.getRelativeElement(), new HTML(p.getHTML(), true));
				}
			});
			p.addMouseOutHandler(new MouseOutHandler() {
				@Override
				public void onMouseOut(MouseOutEvent event) {
					GwtHint.hideHint();
				}
			});
			return p;
		case DATE:
			String dates = "";
			for (MultiExecutionInterface exec: TaskInterface.getMultiExecutions(task.getExecutions(), false)) {
				if (exec.getNrMeetings() == 1) {
					dates += (dates.isEmpty() ? "" : "<br>") + "<span class='status-" + exec.getStatus().name().toLowerCase() + "'>" +
							exec.getDays(iOptions.getFirstDayOfWeek(), CONSTANTS) + " " + sDateFormatLong.format(exec.getFirstExecutionDate()) +
							"</span>";
				} else {
					dates += (dates.isEmpty() ? "" : "<br>") + "<span class='status-" + exec.getStatus().name().toLowerCase() + "'>" +
							exec.getDays(iOptions.getFirstDayOfWeek(), CONSTANTS) + " " + 
							sDateFormatShort.format(exec.getFirstExecutionDate()) + " - " + sDateFormatLong.format(exec.getLastExecutionDate())+
							"</span>";
				}
			}
			return new HTML(dates, false);
		case TIME:
			String times = "";
			for (MultiExecutionInterface exec: TaskInterface.getMultiExecutions(task.getExecutions(), false)) {
				if (exec.getNrMeetings() == 1) {
					times += (times.isEmpty() ? "" : "<br>") + "<span class='status-" + exec.getStatus().name().toLowerCase() + "'>" +
							exec.getExecutionTime(CONSTANTS) + "</span>";
				} else {
					times += (times.isEmpty() ? "" : "<br>") + "<span class='status-" + exec.getStatus().name().toLowerCase() + "'>" +
							exec.getExecutionTime(CONSTANTS) + "</span>";
				}
			}
			return new HTML(times, false);
		case STATUS:
			String statuses = "";
			for (MultiExecutionInterface exec: TaskInterface.getMultiExecutions(task.getExecutions(), false)) {
				statuses += (statuses.isEmpty() ? "" : "<br>") + "<span class='status-" + exec.getStatus().name().toLowerCase() + "'>" +
						CONSTANTS.taskStatus()[exec.getStatus().ordinal()] + "</span>";
			}
			return new HTML(statuses, false);
		default:
			return null;
		}
	}
	
	public int getRow(Long taskId) {
		if (taskId == null) return -1;
		for (int row = 1; row < getRowCount(); row ++) {
			TaskInterface task = getData(row);
			if (task != null && taskId.equals(task.getId())) return row;
		}
		return -1;
	}
	
	public void scrollToTask(Long taskId) {
		if (taskId == null) return;
		for (int row = 1; row < getRowCount(); row ++) {
			TaskInterface task = getData(row);
			if (task != null && taskId.equals(task.getId())) {
				getRowFormatter().getElement(row).scrollIntoView();
				return;
			}
		}
	}
	
	public static class TasksTableComparator implements Comparator<TaskInterface> {
		private TasksTableColumn iColumn;
		private boolean iAsc;
		
		public TasksTableComparator(TasksTableColumn column, boolean asc) {
			iColumn = column;
			iAsc = asc;
		}
		
		protected int compareByName(TaskInterface t1, TaskInterface t2) {
			return compare(t1.getName(), t2.getName());
		}
		
		protected int compareByOwner(TaskInterface t1, TaskInterface t2) {
			return compare(
					t1.getOwner() == null ? "" : t1.getOwner().getLastName() + ", " + t1.getOwner().getFirstName() + " " + t1.getOwner().getMiddleName(),
					t2.getOwner() == null ? "" : t2.getOwner().getLastName() + ", " + t2.getOwner().getFirstName() + " " + t2.getOwner().getMiddleName()); 
		}
		
		protected int compareByScript(TaskInterface t1, TaskInterface t2) {
			return compare(t1.getScript() == null ? "" : t1.getScript().getName(), t2.getScript() == null ? "" : t2.getScript().getName()); 
		}
		
		protected int compareByDate(TaskInterface t1, TaskInterface t2) {
			int cmp = compare(t1.getLastExecuted(), t2.getLastExecuted());
			if (cmp != 0) return cmp;
			TaskExecutionInterface e1 = null, e2 = null;
			for (TaskExecutionInterface e: t1.getExecutions())
				if (e.getStatus() == ExecutionStatus.CREATED) { e1 = e; break; }
			for (TaskExecutionInterface e: t2.getExecutions())
				if (e.getStatus() == ExecutionStatus.CREATED) { e2 = e; break; }
			return (e1 == null ? e2 == null ? 0 : -1 : e2 == null ? 1 : e1.compareTo(e2));
		}
		
		protected int compareByTime(TaskInterface t1, TaskInterface t2) {
			int cmp = compare(t1.getLastExecuted(), t2.getLastExecuted());
			if (cmp != 0) return cmp;
			TaskExecutionInterface e1 = null, e2 = null;
			for (TaskExecutionInterface e: t1.getExecutions())
				if (e.getStatus() == ExecutionStatus.CREATED) { e1 = e; break; }
			for (TaskExecutionInterface e: t2.getExecutions())
				if (e.getStatus() == ExecutionStatus.CREATED) { e2 = e; break; }
			return (e1 == null ? e2 == null ? 0 : -1 : e2 == null ? 1 : e1.compareTo(e2));
		}
		
		protected int compareByTime(TaskExecutionInterface t1, TaskExecutionInterface t2) {
			int cmp = new Integer(t1.getSlot()).compareTo(t2.getSlot());
			if (cmp != 0) return cmp;
			return t1.getDayOfYear().compareTo(t2.getDayOfYear());
		}
		
		protected int compareByStatus(TaskInterface t1, TaskInterface t2) {
			int cmp = (t1.getLastStatus() == null ? t1.getLastStatus() == null ? 0 : -1 : t2.getLastStatus() == null ? 1 : t1.getLastStatus().compareTo(t2.getLastStatus()));
			if (cmp != 0) return cmp;
			TaskExecutionInterface e1 = null, e2 = null;
			for (TaskExecutionInterface e: t1.getExecutions())
				if (e.getStatus() == ExecutionStatus.CREATED) { e1 = e; break; }
			for (TaskExecutionInterface e: t2.getExecutions())
				if (e.getStatus() == ExecutionStatus.CREATED) { e2 = e; break; }
			return (e1 == null ? e2 == null ? 0 : -1 : e2 == null ? 1 : e1.compareTo(e2));
		}
		
		protected int compareByColumn(TaskInterface t1, TaskInterface t2) {
			switch (iColumn) {
			case NAME: return compareByName(t1, t2);
			case SCRIPT: return compareByScript(t1, t2);
			case OWNER: return compareByOwner(t1, t2);
			case DATE: return compareByDate(t1, t2);
			case TIME: return compareByTime(t1, t2);
			case STATUS: return compareByStatus(t1, t2);
			default:
				return t1.compareTo(t2);
			}
		}
		
		public static boolean isApplicable(TasksTableColumn column) {
			switch (column) {
			case NAME:
			case SCRIPT:
			case OWNER:
			case DATE:
			case TIME:
			case STATUS:
				return true;
			default:
				return false;
			}
		}

		@Override
		public int compare(TaskInterface t1, TaskInterface t2) {
			int cmp = compareByColumn(t1, t2);
			if (cmp != 0) return (iAsc ? cmp : -cmp);
			return (iAsc ? t1.compareTo(t2) : t2.compareTo(t1));
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

    public static enum TasksTableColumn {
		NAME,
		OWNER,
		SCRIPT,
		PARAMETERS,
		DATE,
		TIME,
		STATUS,
		;
	}
    
    public int addTask(final TaskInterface task) {
		List<Widget> widgets = new ArrayList<Widget>();
		for (TasksTableColumn column: TasksTableColumn.values()) {
			int nbrCells = getNbrCells(column);
			for (int idx = 0; idx < nbrCells; idx ++) {
				Widget cell = getCell(task, column, idx);
				if (cell == null)
					cell = new P();
				widgets.add(cell);
			}
		}
		int row = addRow(task, widgets);
		getRowFormatter().setStyleName(row, "row");
		for (int col = 0; col < getCellCount(row); col++)
			getCellFormatter().setStyleName(row, col, "cell");
		
		return row;
	}

	@Override
	public void setValue(List<TaskInterface> value) {
		clearTable(1);
    	if (value != null)
    		for (TaskInterface task: value)
    			addTask(task);
    	sort();
	}

	@Override
	public List<TaskInterface> getValue() {
		return getData();
	}
	
	public static interface SortOperation extends Operation, HasColumnName {}
}
