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
package org.unitime.timetable.gwt.client.solver.suggestions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import org.unitime.timetable.gwt.client.solver.SolverCookie;
import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader.HasColumnName;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader.Operation;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.resources.GwtResources;
import org.unitime.timetable.gwt.shared.SuggestionsInterface.ClassAssignmentDetails;
import org.unitime.timetable.gwt.shared.SuggestionsInterface.TimeInfo;
import org.unitime.timetable.gwt.shared.TableInterface.NaturalOrderComparator;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant;

/**
 * @author Tomas Muller
 */
public class ConflictTable extends UniTimeTable<ClassAssignmentDetails> implements TakesValue<Collection<ClassAssignmentDetails>>{
	protected static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	protected static final GwtConstants CONSTANTS = GWT.create(GwtConstants.class);
	protected static final GwtResources RESOURCES =  GWT.create(GwtResources.class);
	
	private SuggestionsPageContext iContext;

	private ConflictColum iSortBy = null;
	private boolean iAsc = true;
	
	public ConflictTable(SuggestionsPageContext context) {
		addStyleName("unitime-ClassAssignmentTable");
		addStyleName("unitime-ClassAssignmentTableConflicts");
		iContext = context;
		List<UniTimeTableHeader> header = new ArrayList<UniTimeTableHeader>();

		for (ConflictColum column: ConflictColum.values()) {
			int nrCells = getNbrCells(column);
			for (int idx = 0; idx < nrCells; idx++) {
				UniTimeTableHeader h = new UniTimeTableHeader(getColumnName(column, idx), getColumnAlignment(column, idx));
				header.add(h);
			}
		}
		
		for (final ConflictColum column: ConflictColum.values()) {
			if (ConflictsComparator.isApplicable(column) && getNbrCells(column) > 0) {
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
			if (column == ConflictColum.STUDENT_CONFLICTS || column == ConflictColum.DISTRIBUTION_CONFLICTS) {
				final int index = getCellIndex(column);
				final UniTimeTableHeader h = header.get(index);
				h.addOperation(new Operation() {
					@Override
					public void execute() {
						if (column == ConflictColum.STUDENT_CONFLICTS)
							SolverCookie.getInstance().setShowAllStudentConflicts(true);
						else
							SolverCookie.getInstance().setShowAllDistributionConflicts(true);
						for (int row = 1; row < getRowCount(); row++) {
							Widget w = getWidget(row, index);
							if (w instanceof ConflictCell)
								((ConflictCell)w).showList();
						}						
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
						return MESSAGES.opShowAllConflicts();
					}
				});
				h.addOperation(new Operation() {
					@Override
					public void execute() {
						if (column == ConflictColum.STUDENT_CONFLICTS)
							SolverCookie.getInstance().setShowAllStudentConflicts(false);
						else
							SolverCookie.getInstance().setShowAllDistributionConflicts(false);
						for (int row = 1; row < getRowCount(); row++) {
							Widget w = getWidget(row, index);
							if (w instanceof ConflictCell)
								((ConflictCell)w).showNumber();
						}						
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
						return MESSAGES.opHideAllConflicts();
					}
				});
			}
		}
		
		addRow(null, header);
		
		for (int i = 0; i < getCellCount(0); i++)
			getCellFormatter().setStyleName(0, i, "unitime-ClickableTableHeader");
		
		setSortBy(SolverCookie.getInstance().getConflictsSort());
	}
	
	protected void doSort(ConflictColum column) {
		if (column == iSortBy) {
			iAsc = !iAsc;
		} else {
			iSortBy = column;
			iAsc = true;
		}
		SolverCookie.getInstance().setConflictsSort(getSortBy());
		sort();
	}
	
	public boolean hasSortBy() { return iSortBy != null; }
	public int getSortBy() { return iSortBy == null ? 0 : iAsc ? 1 + iSortBy.ordinal() : -1 - iSortBy.ordinal(); }
	public void setSortBy(int sortBy) {
		if (sortBy == 0) {
			iSortBy = null;
			iAsc = true;
		} else if (sortBy > 0) {
			iSortBy = ConflictColum.values()[sortBy - 1];
			iAsc = true;
		} else {
			iSortBy = ConflictColum.values()[-1 - sortBy];
			iAsc = false;
		}
		sort();
	}
	
	public void sort() {
		if (iSortBy == null) return;
		if (getNbrCells(iSortBy) == 0) iSortBy = ConflictColum.TIME;
		UniTimeTableHeader header = getHeader(getCellIndex(iSortBy));
		sort(header, new ConflictsComparator(iSortBy, true), iAsc);
	}

	public static enum ConflictColum {
		DATE,
		TIME,
		STUDENT_CONFLICTS,
		DISTRIBUTION_CONFLICTS,
		;
	}
	
	protected int getNbrCells(ConflictColum column) {
		switch (column) {
		default:
			return 1;
		}
	}
	
	public String getColumnName(ConflictColum column, int idx) {
		switch (column) {
		case DATE: return MESSAGES.colDate();
		case TIME: return MESSAGES.colTime();
		case STUDENT_CONFLICTS: return MESSAGES.colStudentConflicts();
		case DISTRIBUTION_CONFLICTS: return MESSAGES.colDistributionConflicts();
		default: return column.name();
		}
	}
	
	protected HorizontalAlignmentConstant getColumnAlignment(ConflictColum column, int idx) {
		switch (column) {
		default:
			return HasHorizontalAlignment.ALIGN_LEFT;
		}
	}
	
	protected int getCellIndex(ConflictColum column) {
		int ret = 0;
		for (ConflictColum c: ConflictColum.values())
			if (c.ordinal() < column.ordinal()) ret += getNbrCells(c);
		return ret;
	}
	
	protected Widget getCell(final ClassAssignmentDetails conflict, final ConflictColum column, final int idx) {
		switch (column) {
		case DATE:
			return iContext.createDateLabel(conflict.getTime().getDatePattern());
		case TIME:
			return iContext.createTimeLabel(conflict.getTime(), conflict.getClazz().getClassId(), true);
		case STUDENT_CONFLICTS:
			if (conflict.hasStudentConflicts()) {
				return new ConflictCell(SuggestionsPageContext.dispNumber(conflict.countStudentConflicts()), iContext.createStudentConflicts(conflict.getStudentConflicts()), SolverCookie.getInstance().isShowAllStudentConflicts());
			} else {
				return null;
			}
		case DISTRIBUTION_CONFLICTS:
			if (conflict.hasDistributionConflicts()) {
				return new ConflictCell(SuggestionsPageContext.dispNumber(conflict.countDistributionConflicts()), iContext.createViolatedConstraints(conflict.getDistributionConflicts(), null), SolverCookie.getInstance().isShowAllDistributionConflicts());
			} else {
				return null;
			}
		default:
			return null;
		}
	}
	
	public int addRow(final ClassAssignmentDetails suggestion) {
		List<Widget> widgets = new ArrayList<Widget>();
		
		for (ConflictColum column: ConflictColum.values()) {
			int nbrCells = getNbrCells(column);
			for (int idx = 0; idx < nbrCells; idx ++) {
				Widget cell = getCell(suggestion, column, idx);
				if (cell == null)
					cell = new P();
				widgets.add(cell);
			}
		}
		
		int row = addRow(suggestion, widgets);
		getRowFormatter().setStyleName(row, "row");
		for (int col = 0; col < getCellCount(row); col++)
			getCellFormatter().setStyleName(row, col, "cell");
		
		return row;
	}
	
	public static class ConflictsComparator implements Comparator<ClassAssignmentDetails> {
		private ConflictColum iColumn;
		private boolean iAsc;
		
		public ConflictsComparator(ConflictColum column, boolean asc) {
			iColumn = column;
			iAsc = asc;
		}

		public int compareByDate(ClassAssignmentDetails s1, ClassAssignmentDetails s2) {
			TimeInfo t1 = s1.getTime();
			TimeInfo t2 = s2.getTime();
			return compare(t1 == null ? null : t1.getDatePatternName(), t2 == null ? null : t2.getDatePatternName());
		}
		
		public int compareByTime(ClassAssignmentDetails s1, ClassAssignmentDetails s2) {
			TimeInfo t1 = s1.getTime();
			TimeInfo t2 = s2.getTime();
			int cmp = compare(t1 == null ? null : t1.getDaysName(new String[] {"A", "B", "C", "D", "E", "F", "G"}), t2 == null ? null : t2.getDaysName(new String[] {"A", "B", "C", "D", "E", "F", "G"}));
			if (cmp != 0) return cmp;
			cmp = compare(t1 == null ? null : t1.getStartSlot(), t2 == null ? null : t2.getStartSlot());
			if (cmp != 0) return cmp;
			return compare(t1 == null ? null : t1.getName(false, CONSTANTS), t2 == null ? null : t2.getName(false, CONSTANTS));
		}
		
		public int compareByDateTime(ClassAssignmentDetails s1, ClassAssignmentDetails s2) {
			TimeInfo t1 = s1.getTime();
			TimeInfo t2 = s2.getTime();
			int cmp = compare(t1 == null ? null : t1.getDatePatternName(), t2 == null ? null : t2.getDatePatternName());
			if (cmp != 0) return cmp;
			cmp = compare(t1 == null ? null : t1.getDaysName(new String[] {"A", "B", "C", "D", "E", "F", "G"}), t2 == null ? null : t2.getDaysName(new String[] {"A", "B", "C", "D", "E", "F", "G"}));
			if (cmp != 0) return cmp;
			cmp = compare(t1 == null ? null : t1.getStartSlot(), t2 == null ? null : t2.getStartSlot());
			if (cmp != 0) return cmp;
			return compare(t1 == null ? null : t1.getName(false, CONSTANTS), t2 == null ? null : t2.getName(false, CONSTANTS));
		}
		
		public int compareByStudentConflicts(ClassAssignmentDetails s1, ClassAssignmentDetails s2) {
			return compare(s1.countStudentConflicts(), s2.countStudentConflicts());
		}
		
		public int compareByDistributionConflicts(ClassAssignmentDetails s1, ClassAssignmentDetails s2) {
			return compare(s1.countDistributionConflicts(), s2.countDistributionConflicts());
		}
		
		protected int compareByColumn(ClassAssignmentDetails c1, ClassAssignmentDetails c2) {
			switch (iColumn) {
			case DATE: return compareByDate(c1, c2);
			case TIME: return compareByTime(c1, c2);
			case STUDENT_CONFLICTS: return compareByStudentConflicts(c1, c2);
			case DISTRIBUTION_CONFLICTS: return compareByDistributionConflicts(c1, c2);
			default: return compareByTime(c1, c2);
			}
		}
		
		public static boolean isApplicable(ConflictColum column) {
			switch (column) {
			case TIME:
			case STUDENT_CONFLICTS:
			case DISTRIBUTION_CONFLICTS:
				return true;
			default:
				return false;
			}
		}
		
		@Override
		public int compare(ClassAssignmentDetails c1, ClassAssignmentDetails c2) {
			int cmp = compareByColumn(c1, c2);
			if (cmp != 0) return (iAsc ? cmp : -cmp);
			cmp = compareByDate(c1, c2);
			if (cmp == 0)
				cmp = compareByTime(c1, c2);
			return (iAsc ? cmp : -cmp);
		}
		
		protected int compare(String s1, String s2) {
			if (s1 == null || s1.isEmpty()) {
				return (s2 == null || s2.isEmpty() ? 0 : 1);
			} else {
				return (s2 == null || s2.isEmpty() ? -1 : NaturalOrderComparator.compare(s1, s2));
			}
		}
		
		protected int compare(Number n1, Number n2) {
			return (n1 == null ? n2 == null ? 0 : -1 : n2 == null ? 1 : Double.compare(n1.doubleValue(), n2.doubleValue())); 
		}
		
		protected int compare(Boolean b1, Boolean b2) {
			return (b1 == null ? b2 == null ? 0 : -1 : b2 == null ? 1 : (b1.booleanValue() == b2.booleanValue()) ? 0 : (b1.booleanValue() ? 1 : -1));
		}
	}
	
	public static interface SortOperation extends Operation, HasColumnName {}
	
	@Override
	public Collection<ClassAssignmentDetails> getValue() {
		return getData();
	}

	@Override
	public void setValue(Collection<ClassAssignmentDetails> value) {
		clearTable(1);
		boolean hasStudentConflicts = false, hasDistributionConflicts = false;
		if (value != null)
			for (ClassAssignmentDetails suggestion: value) {
				if (suggestion.hasStudentConflicts()) hasStudentConflicts = true;
				if (suggestion.hasDistributionConflicts()) hasDistributionConflicts = true;
				addRow(suggestion);
			}
		sort();
		setColumnVisible(getCellIndex(ConflictColum.STUDENT_CONFLICTS), hasStudentConflicts);
		setColumnVisible(getCellIndex(ConflictColum.DISTRIBUTION_CONFLICTS), hasDistributionConflicts);
	}
	
	public static class ConflictCell extends P {
		P iN, iD, iL;
		
		public ConflictCell(String number, P conflicts, boolean visible) {
			super("conflicts");
			iN = new P("number"); iN.setHTML(number);
			add(iN);
			iD = new P("dots"); iD.setHTML(CONSTANTS.selectionMore());
			add(iD);
			iL = new P("list");
			add(iL);
			iL.add(conflicts);
			if (visible)
				showList();
			else
				showNumber();
			iD.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					showList();
					event.preventDefault();
					event.stopPropagation();
				}
			});
			iN.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					if (iL.isVisible())
						showNumber();
					else
						showList();
					event.preventDefault();
					event.stopPropagation();
				}
			});
		}
		
		public void showNumber() {
			iD.setVisible(true); iL.setVisible(false);
		}
		
		public void showList() {
			iD.setVisible(false); iL.setVisible(true);
		}
	}
}
