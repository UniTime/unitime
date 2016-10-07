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
package org.unitime.timetable.gwt.client.curricula;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.curricula.CurriculaClassifications.NameChangedEvent;
import org.unitime.timetable.gwt.client.widgets.CourseSelectionEvent;
import org.unitime.timetable.gwt.client.widgets.CourseSelectionHandler;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeDialogBox;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader;
import org.unitime.timetable.gwt.client.widgets.UniTimeTextBox;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.HasCellAlignment;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.HintProvider;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.TableEvent;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader.Operation;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.CurriculumInterface;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.RequestedCourse;
import org.unitime.timetable.gwt.shared.CurriculumInterface.AcademicClassificationInterface;
import org.unitime.timetable.gwt.shared.CurriculumInterface.CourseInterface;
import org.unitime.timetable.gwt.shared.CurriculumInterface.CurriculumCourseGroupInterface;
import org.unitime.timetable.gwt.shared.CurriculumInterface.CurriculumCourseInterface;
import org.unitime.timetable.gwt.shared.CurriculumInterface.CurriculumStudentsInterface;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.dom.client.Style.FontStyle;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.HasOpenHandlers;
import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.ValueBoxBase;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Tomas Muller
 */
public class CurriculaCourses extends Composite implements SimpleForm.HasMobileScroll {
	protected static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	private UniTimeTable<String> iTable = null;
	
	private static NumberFormat NF = NumberFormat.getFormat("##0.0");
	
	public static enum Mode {
		LAST (MESSAGES.abbvLastLikeEnrollment(), MESSAGES.fieldLastLikeEnrollment()),
		PROJ (MESSAGES.abbvProjectedByRule(), MESSAGES.fieldProjectedByRule()),
		ENRL (MESSAGES.abbvCurrentEnrollment(), MESSAGES.fieldCurrentEnrollment()),
		REQ (MESSAGES.abbvCourseRequests(), MESSAGES.fieldCourseRequests()),
		NONE ("&nbsp;", "NONE");

		private String iAbbv, iName;
		
		Mode(String abbv, String name) { iAbbv = abbv; iName = name; }
		
		public String getAbbv() { return iAbbv; }
		public String getName() { return iName; }
	}
	
	private List<Group> iGroups = new ArrayList<Group>();
	
	private CurriculaClassifications iClassifications;
	
	private CourseSelectionHandler iCourseChangedHandler = null;
	
	private GroupDialogBox iNewGroupDialog;
	private boolean iEditable = true;
	private Map<String, String> iBgColors = new HashMap<String, String>();
	
	private static String[] sColors = new String[] {
		"red", "blue", "green", "orange", "yellow", "pink",
		"purple", "teal", "darkpurple", "steelblue", "lightblue",
		"lightgreen", "yellowgreen", "redorange", "lightbrown", "lightpurple",
		"grey", "bluegrey", "lightteal", "yellowgrey", "brown"
	};
	
	private static String[] sBgColors = new String[] {
		"#f0fff0", //Honeydew
		"#f0f8ff", //AliceBlue
		"#faebd7", //AntiqueWhite
		"#f0ffff", //Azure
		"#f5f5dc", //Beige
		"#fff0f5", //LavenderBlush
		"#f5fffa", //MintCream
		"#faf0e6", //Linen
		"#ffe4e1", //MistyRose
		"#fffafa", //Snow
		"#fdf5e6", //OldLace
		"#f8f8ff", //GhostWhite
		"#f5f5f5", //WhiteSmoke
		"#fff5ee", //SeeShell
		"#fffaf0", //FloralWhite
		"#fffff0", //Ivory
	};
	
	private TreeSet<String> iVisibleCourses = null;
	private HashMap<String, CurriculumStudentsInterface[]> iLastCourses = null;
	
	public String getBackgroundColor(String template) {
		String color = iBgColors.get(template);
		if (color == null) {
			color = sBgColors[iBgColors.size() % sBgColors.length];
			iBgColors.put(template, color);
		}
		return color;
	}
	
	public CurriculaCourses() {
		iTable = new UniTimeTable<String>();
		iTable.addStyleName("unitime-CurriculaCourseProjections");
		initWidget(iTable);
		iCourseChangedHandler = new CourseSelectionHandler() {
			
			@Override
			public void onCourseSelection(CourseSelectionEvent event) {
				CurriculumStudentsInterface[] c = (iLastCourses == null ? null : iLastCourses.get(event.getCourse()));
				for (int col = 0; col < iClassifications.getClassifications().size(); col ++) {
					setEnrollmentAndLastLike(event.getCourse(), col,
							c == null || c[col] == null ? null : c[col].getEnrollment(), 
							c == null || c[col] == null ? null : c[col].getLastLike(),
							c == null || c[col] == null ? null : c[col].getProjection(),
							c == null || c[col] == null ? null : c[col].getRequested());
				}
				Element td = ((Widget)event.getSource()).getElement();
				while (td != null && !td.getPropertyString("tagName").equalsIgnoreCase("td")) {
					td = DOM.getParent(td);
				}
				Element tr = DOM.getParent(td);
			    Element body = DOM.getParent(tr);
			    int row = DOM.getChildIndex(body, tr);
			    if (event.getCourse().isEmpty()) {
					iTable.getRowFormatter().addStyleName(row, "unitime-NoPrint");
			    } else {
					iTable.getRowFormatter().removeStyleName(row, "unitime-NoPrint");
			    }
			    if (row + 1 == iTable.getRowCount() && !event.getCourse().isEmpty())
					addBlankLine();
			}
		};
		
		iNewGroupDialog = new GroupDialogBox();
		
		iTable.setHintProvider(new HintProvider<String>() {
			@Override
			public Widget getHint(TableEvent<String> event) {
				if (!canShowStudentsTable(event.getRow())) return null;
				StudentsTable studentsTable = new StudentsTable(event.getRow());
				if (studentsTable.canShow()) return studentsTable;
				return null;
			}
		});
	}
	
	public void link(CurriculaClassifications cx) {
		iClassifications = cx;
		iClassifications.addExpectedChangedHandler(new CurriculaClassifications.ExpectedChangedHandler() {
			@Override
			public void expectedChanged(CurriculaClassifications.ExpectedChangedEvent e) {
				setVisible(e.getColumn(), e.getExpected() != null);
				if (e.getExpected() != null)
					CurriculaCourses.this.expectedChanged(e.getColumn(), e.getExpected());
			}
		});
		iClassifications.addNameChangedHandler(new CurriculaClassifications.NameChangedHandler() {
			@Override
			public void nameChanged(NameChangedEvent e) {
				((Label)iTable.getWidget(0, 2 + 2 * e.getColumn())).setText(e.getName());
			}
		});
	}
	
	public void populate(CurriculumInterface curriculum, boolean editable) {
		iEditable = curriculum.isEditable() && editable;
		iTable.setAllowSelection(iEditable);
		iTable.clearTable();
		// iTable.clear(true);
		iGroups.clear();
		
		// header
		List<UniTimeTableHeader> header = new ArrayList<UniTimeTableHeader>();
		final UniTimeTableHeader hGroup = new UniTimeTableHeader(MESSAGES.colGroup()) {
			@Override
			public List<Operation> getOperations() {
				List<Operation> ret = new ArrayList<Operation>();
				for (CurriculaCourses.Group g: getGroups()) {
					ret.add(g.getOperation());
				}
				ret.addAll(super.getOperations());
				return ret;
			}
		};
		header.add(hGroup);
		hGroup.addOperation(new Operation() {
			@Override
			public String getName() {
				return MESSAGES.opNewGroup();
			}
			@Override
			public boolean hasSeparator() {
				return true;
			}
			@Override
			public boolean isApplicable() {
				return iVisibleCourses == null && iEditable && iTable.getSelectedCount() > 0;
			}
			@Override
			public void execute() {
				iTable.clearHover();
				iNewGroupDialog.openNew();
			}
		});
		hGroup.addOperation(new Operation() {
			@Override
			public String getName() {
				return MESSAGES.opSortBy(MESSAGES.colGroup());
			}
			@Override
			public boolean hasSeparator() {
				return true;
			}
			@Override
			public boolean isApplicable() {
				return iVisibleCourses == null;
			}
			@Override
			public void execute() {
				iTable.sortByRow(hGroup, new Comparator<Integer>() {
					public int compare(Integer a, Integer b) {
						return compareTwoRows(0, a, b);
					}
				});
			}
		});
		
		final UniTimeTableHeader hCourse = new UniTimeTableHeader(MESSAGES.colCourse());
		header.add(hCourse);
		hCourse.addOperation(new Operation() {
			@Override
			public void execute() {
				showAllCourses();
			}
			@Override
			public boolean isApplicable() {
				return iVisibleCourses != null;
			}
			@Override
			public boolean hasSeparator() {
				return false;
			}
			@Override
			public String getName() {
				return MESSAGES.opShowAllCourses();
			}
		});
		hCourse.addOperation(new Operation() {
			@Override
			public void execute() {
				for (int i = 1; i < iTable.getRowCount(); i++)
					iTable.setSelected(i, !((CurriculaCourseSelectionBox)iTable.getWidget(i, 1)).getValue().isEmpty());				
			}
			@Override
			public boolean isApplicable() {
				return iEditable;
			}
			@Override
			public boolean hasSeparator() {
				return false;
			}
			@Override
			public String getName() {
				return MESSAGES.opSelectAllCourses();
			}
		});
		hCourse.addOperation(new Operation() {
			@Override
			public void execute() {
				for (int row = iTable.getRowCount() - 1; row > 0; row --) {
					if (!iTable.isSelected(row)) continue;
					String course = ((CurriculaCourseSelectionBox)iTable.getWidget(row, 1)).getText();
					if (course.isEmpty() && row + 1 == iTable.getRowCount()) {
						iTable.setSelected(row, false);
						continue;
					}
					iTable.removeRow(row);
				}
			}
			@Override
			public boolean isApplicable() {
				if (!iEditable || iTable.getSelectedCount() == 0 || iVisibleCourses != null)
					return false;
				for (int row = iTable.getRowCount() - 1; row > 0; row --) {
					if (iTable.isSelected(row) && !((CurriculaCourseSelectionBox)iTable.getWidget(row, 1)).isEnabled())
						return false;
				}
				return true;
			}
			@Override
			public boolean hasSeparator() {
				return false;
			}
			@Override
			public String getName() {
				return MESSAGES.opRemoveSelectedCourses();
			}
		});
		hCourse.addOperation(new Operation() {
			@Override
			public void execute() {
				for (int i = 1; i < iTable.getRowCount(); i++)
					iTable.setSelected(i, false);
			}
			@Override
			public boolean isApplicable() {
				return iEditable && iTable.getSelectedCount() > 0;
			}
			@Override
			public boolean hasSeparator() {
				return false;
			}
			@Override
			public String getName() {
				return MESSAGES.opClearSelection();
			}
		});
		hCourse.addOperation(new Operation() {
			@Override
			public void execute() {
				setPercent(!CurriculumCookie.getInstance().getCurriculaCoursesPercent());
			}
			@Override
			public boolean isApplicable() {
				return true;
			}
			@Override
			public boolean hasSeparator() {
				return true;
			}
			@Override
			public String getName() {
				return (CurriculumCookie.getInstance().getCurriculaCoursesPercent() ? MESSAGES.opShowNumbers() : MESSAGES.opShowPercentages());
			}
		});
		List<Operation> modeOps = new ArrayList<Operation>();
		for (final Mode m: Mode.values()) {
			if (m == Mode.NONE) continue;
			modeOps.add(new Operation() {
				@Override
				public void execute() {
					setMode(m == CurriculumCookie.getInstance().getCurriculaCoursesMode() ? Mode.NONE : m);
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
					return (m == CurriculumCookie.getInstance().getCurriculaCoursesMode() ? MESSAGES.opHide(m.getName()) : MESSAGES.opShow(m.getName()));
				}
			});
		}
		for (Operation op: modeOps)
			hCourse.addOperation(op);
		hCourse.addOperation(new Operation() {
			@Override
			public void execute() {
				updateEnrollmentsAndLastLike(iLastCourses, true);
			}
			@Override
			public boolean isApplicable() {
				return iVisibleCourses == null && iLastCourses != null;
			}
			@Override
			public boolean hasSeparator() {
				return true;
			}
			@Override
			public String getName() {
				return MESSAGES.opShowEmptyCourses();
			}
		});
		hCourse.addOperation(new Operation() {
			@Override
			public void execute() {
				// boolean selectedOnly = (iTable.getSelectedCount() > 0);
				rows: for (int row = iTable.getRowCount() - 1; row > 0; row --) {
					String course = ((CurriculaCourseSelectionBox)iTable.getWidget(row, 1)).getText();
					if (course.isEmpty() && row + 1 == iTable.getRowCount()) continue;
					/*
					if (selectedOnly && !iTable.isSelected(row)) {
						iTable.setSelected(row, false);
						continue;
					}
					*/
					for (int c = 0; c < iClassifications.getClassifications().size(); c++) {
						int x = 2 + 2 * c;
						ShareTextBox text = (ShareTextBox)iTable.getWidget(row, x);
						if (text.getDisplayedShare() != null) continue rows;
					}
					iTable.removeRow(row);
				}
			}
			@Override
			public boolean isApplicable() {
				return iVisibleCourses == null;
			}
			@Override
			public boolean hasSeparator() {
				return iLastCourses == null;
			}
			@Override
			public String getName() {
				return MESSAGES.opHideEmptyCourses();
			}
		});
		hCourse.addOperation(new Operation() {
			@Override
			public void execute() {
				boolean selectedOnly = (iTable.getSelectedCount() > 0);
				for (int c = 0; c < iClassifications.getClassifications().size(); c++) {
					int x = 2 + 2 * c;
					for (int row = 1; row < iTable.getRowCount(); row ++) {
						if (selectedOnly && !iTable.isSelected(row)) continue;
						ShareTextBox text = (ShareTextBox)iTable.getWidget(row, x);
						text.setShare(null);
					}
				}
			}
			@Override
			public boolean isApplicable() {
				return iVisibleCourses == null && iEditable;
			}
			@Override
			public boolean hasSeparator() {
				return true;
			}
			@Override
			public String getName() {
				return iTable.getSelectedCount() > 0 ? MESSAGES.opClearRequestedEnrollmentAllClassificationsSelectedCoursesOnly() : MESSAGES.opClearRequestedEnrollmentAllClassifications();
			}
		});
		hCourse.addOperation(new Operation() {
			@Override
			public void execute() {
				boolean selectedOnly = (iTable.getSelectedCount() > 0);
				for (int c = 0; c < iClassifications.getClassifications().size(); c++) {
					int x = 2 + 2 * c;
					for (int row = 1; row < iTable.getRowCount(); row ++) {
						if (selectedOnly && !iTable.isSelected(row)) continue;
						ShareTextBox text = (ShareTextBox)iTable.getWidget(row, x);
						EnrollmentLabel label = (EnrollmentLabel)iTable.getWidget(row, x + 1);
						if (CurriculumCookie.getInstance().getCurriculaCoursesPercent())
							text.setShare(label.getLastLikePercent());
						else
							text.setExpected(label.getLastLike());
					}
				}
			}
			@Override
			public boolean isApplicable() {
				return iVisibleCourses == null && iEditable && CurriculumCookie.getInstance().getCurriculaCoursesMode() == Mode.LAST;
			}
			@Override
			public boolean hasSeparator() {
				return false;
			}
			@Override
			public String getName() {
				return iTable.getSelectedCount() > 0 ? MESSAGES.opCopyLastLikeToRequestedAllClassificationsSelectedCoursesOnly() : MESSAGES.opCopyLastLikeToRequestedAllClassifications();
			}
		});
		hCourse.addOperation(new Operation() {
			@Override
			public void execute() {
				boolean selectedOnly = (iTable.getSelectedCount() > 0);
				for (int c = 0; c < iClassifications.getClassifications().size(); c++) {
					int x = 2 + 2 * c;
					for (int row = 1; row < iTable.getRowCount(); row ++) {
						if (selectedOnly && !iTable.isSelected(row)) continue;
						ShareTextBox text = (ShareTextBox)iTable.getWidget(row, x);
						EnrollmentLabel label = (EnrollmentLabel)iTable.getWidget(row, x + 1);
						if (CurriculumCookie.getInstance().getCurriculaCoursesPercent())
							text.setShare(label.getEnrollmentPercent());
						else
							text.setExpected(label.getEnrollment());
					}
				}
			}
			@Override
			public boolean isApplicable() {
				return iVisibleCourses == null && iEditable && CurriculumCookie.getInstance().getCurriculaCoursesMode() == Mode.ENRL;
			}
			@Override
			public boolean hasSeparator() {
				return false;
			}
			@Override
			public String getName() {
				return iTable.getSelectedCount() > 0 ? MESSAGES.opCopyCurrentToRequestedAllClassificationsSelectedCoursesOnly() : MESSAGES.opCopyCurrentToRequestedAllClassifications();
			}
		});
		hCourse.addOperation(new Operation() {
			@Override
			public void execute() {
				boolean selectedOnly = (iTable.getSelectedCount() > 0);
				for (int c = 0; c < iClassifications.getClassifications().size(); c++) {
					int x = 2 + 2 * c;
					for (int row = 1; row < iTable.getRowCount(); row ++) {
						if (selectedOnly && !iTable.isSelected(row)) continue;
						ShareTextBox text = (ShareTextBox)iTable.getWidget(row, x);
						EnrollmentLabel label = (EnrollmentLabel)iTable.getWidget(row, x + 1);
						if (CurriculumCookie.getInstance().getCurriculaCoursesPercent())
							text.setShare(label.getRequestedPercent());
						else
							text.setExpected(label.getRequested());
					}
				}
			}
			@Override
			public boolean isApplicable() {
				return iVisibleCourses == null && iEditable && CurriculumCookie.getInstance().getCurriculaCoursesMode() == Mode.REQ;
			}
			@Override
			public boolean hasSeparator() {
				return false;
			}
			@Override
			public String getName() {
				return iTable.getSelectedCount() > 0 ? MESSAGES.opCopyCourseRequestsToRequestedAllClassificationsSelectedCoursesOnly() : MESSAGES.opCopyCourseRequestsToRequestedAllClassifications();
			}
		});
		hCourse.addOperation(new Operation() {
			@Override
			public void execute() {
				boolean selectedOnly = (iTable.getSelectedCount() > 0);
				for (int c = 0; c < iClassifications.getClassifications().size(); c++) {
					int x = 2 + 2 * c;
					for (int row = 1; row < iTable.getRowCount(); row ++) {
						if (selectedOnly && !iTable.isSelected(row)) continue;
						ShareTextBox text = (ShareTextBox)iTable.getWidget(row, x);
						EnrollmentLabel label = (EnrollmentLabel)iTable.getWidget(row, x + 1);
						if (CurriculumCookie.getInstance().getCurriculaCoursesPercent())
							text.setShare(label.getProjectionPercent());
						else
							text.setExpected(label.getProjection());
					}
				}
			}
			@Override
			public boolean isApplicable() {
				return iVisibleCourses == null && iEditable && CurriculumCookie.getInstance().getCurriculaCoursesMode() == Mode.PROJ;
			}
			@Override
			public boolean hasSeparator() {
				return false;
			}
			@Override
			public String getName() {
				return iTable.getSelectedCount() > 0 ? MESSAGES.opCopyProjectionToRequestedAllClassificationsSelectedCoursesOnly() : MESSAGES.opCopyProjectionToRequestedAllClassifications();
			}
		});
		hCourse.addOperation(new Operation() {
			@Override
			public String getName() {
				return MESSAGES.opSortBy(MESSAGES.colCourse());
			}
			@Override
			public boolean hasSeparator() {
				return true;
			}
			@Override
			public boolean isApplicable() {
				return iVisibleCourses == null;
			}
			@Override
			public void execute() {
				iTable.sortByRow(hCourse, new Comparator<Integer>() {
					public int compare(Integer a, Integer b) {
						return compareTwoRows(1, a, b);
					}
				});
			}
		});
		
		OpenHandler<PopupPanel> fx = new OpenHandler<PopupPanel>() {
			@Override
			public void onOpen(OpenEvent<PopupPanel> event) {
				iTable.clearHover();
			}
		};
	
		int col = 2;
		for (final AcademicClassificationInterface clasf: iClassifications.getClassifications()) {
			final UniTimeTableHeader hExp = new UniTimeTableHeader(clasf.getCode(), HasHorizontalAlignment.ALIGN_RIGHT);
			header.add(hExp);
			final int expCol = col++;
			hExp.addOperation(new Operation() {
				@Override
				public void execute() {
					for (int i = 1; i < iTable.getRowCount(); i++)
						iTable.setSelected(i, !((ShareTextBox)iTable.getWidget(i, expCol)).getText().isEmpty());
				}
				@Override
				public boolean isApplicable() {
					return iEditable;
				}
				@Override
				public boolean hasSeparator() {
					return false;
				}
				@Override
				public String getName() {
					return MESSAGES.opSelectAll();
				}
			});
			hExp.addOperation(new Operation() {
				@Override
				public void execute() {
					for (int i = 1; i < iTable.getRowCount(); i++)
						iTable.setSelected(i, false);
				}
				@Override
				public boolean isApplicable() {
					return iEditable && iTable.getSelectedCount() > 0;
				}
				@Override
				public boolean hasSeparator() {
					return false;
				}
				@Override
				public String getName() {
					return MESSAGES.opClearSelection();
				}
			});
			hExp.addOperation(new Operation() {
				@Override
				public void execute() {
					setPercent(!CurriculumCookie.getInstance().getCurriculaCoursesPercent());
				}
				@Override
				public boolean isApplicable() {
					return true;
				}
				@Override
				public boolean hasSeparator() {
					return true;
				}
				@Override
				public String getName() {
					return CurriculumCookie.getInstance().getCurriculaCoursesPercent() ? MESSAGES.opShowNumbers() : MESSAGES.opShowPercentages();
				}
			});
			for (Operation op: modeOps)
				hExp.addOperation(op);
			hExp.addOperation(new Operation() {
				@Override
				public void execute() {
					boolean selectedOnly = (iTable.getSelectedCount() > 0);
					for (int row = 1; row < iTable.getRowCount(); row ++) {
						if (selectedOnly && !iTable.isSelected(row)) continue;
						ShareTextBox text = (ShareTextBox)iTable.getWidget(row, expCol);
						text.setShare(null);
					}
				}
				@Override
				public boolean isApplicable() {
					return iVisibleCourses == null && iEditable;
				}
				@Override
				public boolean hasSeparator() {
					return true;
				}
				@Override
				public String getName() {
					return iTable.getSelectedCount() > 0 ? MESSAGES.opClearRequestedEnrollmentSelectedCoursesOnly() : MESSAGES.opClearRequestedEnrollment();
				}
			});
			hExp.addOperation(new Operation() {
				@Override
				public void execute() {
					boolean selectedOnly = (iTable.getSelectedCount() > 0);
					for (int row = 1; row < iTable.getRowCount(); row ++) {
						if (selectedOnly && !iTable.isSelected(row)) continue;
						ShareTextBox text = (ShareTextBox)iTable.getWidget(row, expCol);
						EnrollmentLabel label = (EnrollmentLabel)iTable.getWidget(row, expCol + 1);
						if (CurriculumCookie.getInstance().getCurriculaCoursesPercent())
							text.setShare(label.getLastLikePercent());
						else
							text.setExpected(label.getLastLike());
					}
				}
				@Override
				public boolean isApplicable() {
					return iVisibleCourses == null && iEditable && CurriculumCookie.getInstance().getCurriculaCoursesMode() == Mode.LAST;
				}
				@Override
				public boolean hasSeparator() {
					return false;
				}
				@Override
				public String getName() {
					return iTable.getSelectedCount() > 0 ? MESSAGES.opCopyLastLikeToRequestedSelectedCoursesOnly() : MESSAGES.opCopyLastLikeToRequested();
				}
			});
			hExp.addOperation(new Operation() {
				@Override
				public void execute() {
					boolean selectedOnly = (iTable.getSelectedCount() > 0);
					for (int row = 1; row < iTable.getRowCount(); row ++) {
						if (selectedOnly && !iTable.isSelected(row)) continue;
						ShareTextBox text = (ShareTextBox)iTable.getWidget(row, expCol);
						EnrollmentLabel label = (EnrollmentLabel)iTable.getWidget(row, expCol + 1);
						if (CurriculumCookie.getInstance().getCurriculaCoursesPercent())
							text.setShare(label.getEnrollmentPercent());
						else
							text.setExpected(label.getEnrollment());
					}
				}
				@Override
				public boolean isApplicable() {
					return iVisibleCourses == null && iEditable && CurriculumCookie.getInstance().getCurriculaCoursesMode() == Mode.ENRL;
				}
				@Override
				public boolean hasSeparator() {
					return false;
				}
				@Override
				public String getName() {
					return iTable.getSelectedCount() > 0 ? MESSAGES.opCopyCurrentToRequestedSelectedCoursesOnly() : MESSAGES.opCopyCurrentToRequested();
				}
			});
			hExp.addOperation(new Operation() {
				@Override
				public void execute() {
					boolean selectedOnly = (iTable.getSelectedCount() > 0);
					for (int row = 1; row < iTable.getRowCount(); row ++) {
						if (selectedOnly && !iTable.isSelected(row)) continue;
						ShareTextBox text = (ShareTextBox)iTable.getWidget(row, expCol);
						EnrollmentLabel label = (EnrollmentLabel)iTable.getWidget(row, expCol + 1);
						if (CurriculumCookie.getInstance().getCurriculaCoursesPercent())
							text.setShare(label.getRequestedPercent());
						else
							text.setExpected(label.getRequested());
					}
				}
				@Override
				public boolean isApplicable() {
					return iVisibleCourses == null && iEditable && CurriculumCookie.getInstance().getCurriculaCoursesMode() == Mode.REQ;
				}
				@Override
				public boolean hasSeparator() {
					return false;
				}
				@Override
				public String getName() {
					return iTable.getSelectedCount() > 0 ? MESSAGES.opCopyCourseRequestsToRequestedSelectedCoursesOnly() : MESSAGES.opCopyCourseRequestsToRequested();
				}
			});
			hExp.addOperation(new Operation() {
				@Override
				public void execute() {
					boolean selectedOnly = (iTable.getSelectedCount() > 0);
					for (int row = 1; row < iTable.getRowCount(); row ++) {
						if (selectedOnly && !iTable.isSelected(row)) continue;
						ShareTextBox text = (ShareTextBox)iTable.getWidget(row, expCol);
						EnrollmentLabel label = (EnrollmentLabel)iTable.getWidget(row, expCol + 1);
						if (CurriculumCookie.getInstance().getCurriculaCoursesPercent())
							text.setShare(label.getProjectionPercent());
						else
							text.setExpected(label.getProjection());
					}
				}
				@Override
				public boolean isApplicable() {
					return iVisibleCourses == null && iEditable && CurriculumCookie.getInstance().getCurriculaCoursesMode() == Mode.PROJ;
				}
				@Override
				public boolean hasSeparator() {
					return false;
				}
				@Override
				public String getName() {
					return iTable.getSelectedCount() > 0 ? MESSAGES.opCopyProjectionToRequestedSelectedCoursesOnly() : MESSAGES.opCopyProjectionToRequested();
				}
			});
			hExp.addOperation(new Operation() {
				@Override
				public String getName() {
					return MESSAGES.opSortBy(clasf.getCode());
				}
				@Override
				public boolean hasSeparator() {
					return true;
				}
				@Override
				public boolean isApplicable() {
					return iVisibleCourses == null;
				}
				@Override
				public void execute() {
					iTable.sortByRow(hExp, new Comparator<Integer>() {
						public int compare(Integer a, Integer b) {
							return compareTwoRows(expCol, a, b);
						}
					});
				}
			});
			
			final UniTimeTableHeader hCmp = new UniTimeTableHeader(CurriculumCookie.getInstance().getCurriculaCoursesMode().getAbbv(), HasHorizontalAlignment.ALIGN_CENTER);
			header.add(hCmp);
			final int cmpCol = col++;
			hCmp.addOperation(new Operation() {
				@Override
				public void execute() {
					for (int i = 1; i < iTable.getRowCount(); i++)
						iTable.setSelected(i, !((EnrollmentLabel)iTable.getWidget(i, cmpCol)).getText().isEmpty());
				}
				@Override
				public boolean isApplicable() {
					return iEditable;
				}
				@Override
				public boolean hasSeparator() {
					return false;
				}
				@Override
				public String getName() {
					return MESSAGES.opSelectAll();
				}
			});
			hCmp.addOperation(new Operation() {
				@Override
				public void execute() {
					for (int i = 1; i < iTable.getRowCount(); i++)
						iTable.setSelected(i, false);
				}
				@Override
				public boolean isApplicable() {
					return iEditable && iTable.getSelectedCount() > 0;
				}
				@Override
				public boolean hasSeparator() {
					return false;
				}
				@Override
				public String getName() {
					return MESSAGES.opClearSelection();
				}
			});
			hCmp.addOperation(new Operation() {
				@Override
				public void execute() {
					setPercent(!CurriculumCookie.getInstance().getCurriculaCoursesPercent());
				}
				@Override
				public boolean isApplicable() {
					return true;
				}
				@Override
				public boolean hasSeparator() {
					return true;
				}
				@Override
				public String getName() {
					return CurriculumCookie.getInstance().getCurriculaCoursesPercent() ? MESSAGES.opShowNumbers() : MESSAGES.opShowPercentages();
				}
			});
			for (Operation op: modeOps)
				hCmp.addOperation(op);
			hCmp.addOperation(new Operation() {
				@Override
				public String getName() {
					return MESSAGES.opSortBy(clasf.getCode() + " " + CurriculumCookie.getInstance().getCurriculaCoursesMode().getName());
				}
				@Override
				public boolean hasSeparator() {
					return true;
				}
				@Override
				public boolean isApplicable() {
					return iVisibleCourses == null;
				}
				@Override
				public void execute() {
					iTable.sortByRow(hCmp, new Comparator<Integer>() {
						public int compare(Integer a, Integer b) {
							return compareTwoRows(cmpCol, a, b);
						}
					});
				}
			});
		}
		iTable.addRow(null, header);
		
		// body
		iBgColors.clear();
		if (curriculum.hasCourses()) {
			for (CourseInterface course: curriculum.getCourses()) {
				List<Widget> line = new ArrayList<Widget>();
				HorizontalPanel hp = new HorizontalPanel();
				line.add(hp);
				
				if (course.hasGroups()) {
					for (CurriculumCourseGroupInterface g: course.getGroups()) {
						Group gr = null;
						for (Group x: iGroups) {
							if (x.getName().equals(g.getName())) { gr = x; break; }
						}
						if (gr == null) {
							gr = new Group(g.getName(), g.getType(), g.isEditable());
							if (g.getColor() != null) {
								gr.setColor(g.getColor());
							} else {
								colors: for (String c: sColors) {
									for (Group x: iGroups) {
										if (x.getColor().equals(c)) continue colors;
									}
									gr.setColor(c);
									break;
								}
								if (gr.getColor() == null) gr.setColor(sColors[0]);
							}
							iGroups.add(gr);
						}
						hp.add(gr.cloneGroup());
					}
				}
				
				CurriculaCourseSelectionBox cx = new CurriculaCourseSelectionBox();
				RequestedCourse rc = new RequestedCourse(); rc.setCourseId(course.getId()); rc.setCourseName(course.getCourseName());
				cx.setValue(course, false);
				cx.setWidth("130px");
				if (cx.getCourseFinder() instanceof HasOpenHandlers)
					((HasOpenHandlers<PopupPanel>)cx.getCourseFinder()).addOpenHandler(fx);
				cx.addCourseSelectionHandler(iCourseChangedHandler);
				if (!iEditable || course.hasDefaultShare()) cx.setEnabled(false);
				line.add(cx);
				
				for (col = 0; col < iClassifications.getClassifications().size(); col++) {
					CurriculumCourseInterface cci = course.getCurriculumCourse(col);
					ShareTextBox ex = new ShareTextBox(col, cci == null ? null : cci.hasShare() ? cci.getShare() : null, cci == null ? null : cci.getDefaultShare());
					if (!iEditable) ex.setReadOnly(true);
					if (cci != null && cci.hasTemplates() && cci.getDefaultShare() != null)
						ex.setTitle(MESSAGES.hintDefaultPercentShare(NF.format(100.0 * cci.getDefaultShare()) + "%", ToolBox.toString(cci.getTemplates())));
					line.add(ex);
					EnrollmentLabel note = new EnrollmentLabel(col, cci == null ? null : cci.getEnrollment(), cci == null ? null : cci.getLastLike(), cci == null ? null : cci.getProjection(), cci == null ? null : cci.getRequested());
					line.add(note);
				}
				int row = iTable.addRow(course.getCourseName(), line);
				if (course.hasTemplate()) {
					String color = getBackgroundColor(course.getTemplate());
					iTable.getRowFormatter().getElement(row).getStyle().setBackgroundColor(color);
					iTable.getRowFormatter().getElement(row).setTitle(MESSAGES.hintTakenFromTemplate(course.getTemplate()));
				}
			}
		}
		if (iEditable) addBlankLine();
	}
	
	public boolean saveCurriculum(CurriculumInterface c) {
		boolean ret = true;
		HashSet<String> courses = new HashSet<String>();
		HashMap<String, CurriculumCourseGroupInterface> groups = new HashMap<String, CurriculumCourseGroupInterface>();
		if (c.hasCourses()) c.getCourses().clear();
		for (int row = 1; row < iTable.getRowCount(); row++) {
			String course = ((CurriculaCourseSelectionBox)iTable.getWidget(row, 1)).getText();
			if (course.isEmpty()) continue;
			if (!courses.add(course)) {
				((CurriculaCourseSelectionBox)iTable.getWidget(row, 1)).setError(MESSAGES.errorDuplicateCourse(course));
				ret = false;
				continue;
			}
			CourseInterface cr = new CourseInterface();
			cr.setCourseName(course);
			for (int i = 0; i < iClassifications.getClassifications().size(); i++) {
				Float share = ((ShareTextBox)iTable.getWidget(row, 2 + 2 * i)).getShare();
				if (share == null) continue;
				Integer lastLike = ((EnrollmentLabel)iTable.getWidget(row, 3 + 2 * i)).iLastLike;
				CurriculumCourseInterface cx = new CurriculumCourseInterface();
				cx.setShare(share);
				cx.setLastLike(lastLike);
				cx.setCurriculumClassificationId(iClassifications.getClassifications().get(i).getId());
				cr.setCurriculumCourse(i, cx);
			}
			if (!cr.hasCurriculumCourses()) continue;
			HorizontalPanel hp = (HorizontalPanel)iTable.getWidget(row, 0);
			for (int i = 0; i < hp.getWidgetCount(); i++) {
				Group g = (Group)hp.getWidget(i);
				CurriculumCourseGroupInterface gr = groups.get(g.getName());
				if (gr == null) {
					gr = new CurriculumCourseGroupInterface();
					gr.setName(g.getName());
					gr.setType(g.getType());
					gr.setEditable(g.isEditable());
					gr.setColor(g.getColor());
					groups.put(g.getName(), gr);
				}
				cr.addGroup(gr);
			}
			c.addCourse(cr);
		}
		return ret;
	}
	
	public boolean hasDefaultShare(int row) {
		for (int i = 0; i < iClassifications.getClassifications().size(); i++) {
			Float share = ((ShareTextBox)iTable.getWidget(row, 2 + 2 * i)).getDefaultShare();
			if (share != null) return true;
		}
		return false;
	}
	
	public void populateTemplate(CurriculumInterface c) {
		HashSet<String> courses = new HashSet<String>();
		HashMap<String, CurriculumCourseGroupInterface> groups = new HashMap<String, CurriculumCourseGroupInterface>();
		if (c.hasCourses())
			for (CourseInterface cr: c.getCourses())
				if (cr.hasGroups())
					for (CurriculumCourseGroupInterface g: cr.getGroups())
						groups.put(g.getName(), g);
		for (int row = 1; row < iTable.getRowCount(); row++) {
			String course = ((CurriculaCourseSelectionBox)iTable.getWidget(row, 1)).getText();
			if (course.isEmpty()) continue;
			if (!courses.add(course)) continue;
			CourseInterface cr = c.getCourse(course);
			if (cr == null) {
				cr = new CourseInterface();
				cr.setCourseName(course);
				c.addCourse(cr);
			}
			boolean hasShare = false;
			for (int i = 0; i < iClassifications.getClassifications().size(); i++) {
				Float share = ((ShareTextBox)iTable.getWidget(row, 2 + 2 * i)).getShare();
				if (share == null) continue;
				CurriculumCourseInterface cx = cr.getCurriculumCourse(i);
				if (cx == null) {
					cx = new CurriculumCourseInterface();
					cx.setCurriculumClassificationId(iClassifications.getClassifications().get(i).getId());
					cr.setCurriculumCourse(i, cx);
				}					
				cx.setShare(share);
				hasShare = true;
			}
			if (!cr.hasCurriculumCourses() || !hasShare) continue;
			HorizontalPanel hp = (HorizontalPanel)iTable.getWidget(row, 0);
			for (int i = 0; i < hp.getWidgetCount(); i++) {
				Group g = (Group)hp.getWidget(i);
				CurriculumCourseGroupInterface gr = groups.get(g.getName());
				if (gr == null) {
					gr = new CurriculumCourseGroupInterface();
					gr.setName(g.getName());
					gr.setType(g.getType());
					gr.setColor(g.getColor());
					gr.setEditable(g.isEditable());
					groups.put(g.getName(), gr);
				}
				cr.addGroup(gr);
			}
		}
		populate(c, true);
		for (int i = 0; i < iClassifications.getClassifications().size(); i++) {
			if (iClassifications.getExpected(i) == null)
				setVisible(i, false);
		}
	}
	
	public void addBlankLine() {
		List<Widget> line = new ArrayList<Widget>();

		HorizontalPanel hp = new HorizontalPanel();
		line.add(hp);

		CurriculaCourseSelectionBox cx = new CurriculaCourseSelectionBox();
		cx.setWidth("130px");
		cx.addCourseSelectionHandler(iCourseChangedHandler);
		if (cx.getCourseFinder() instanceof HasOpenHandlers)
			((HasOpenHandlers<PopupPanel>)cx.getCourseFinder()).addOpenHandler(new OpenHandler<PopupPanel>() {
				@Override
				public void onOpen(OpenEvent<PopupPanel> event) {
					iTable.clearHover();
				}
			});
		if (!iEditable) cx.setEnabled(false);
		line.add(cx);
		
		for (int col = 0; col < iClassifications.getClassifications().size(); col++) {
			ShareTextBox ex = new ShareTextBox(col, null, null);
			if (!iEditable) ex.setReadOnly(true);
			line.add(ex);
			EnrollmentLabel note = new EnrollmentLabel(col, null, null, null, null);
			line.add(note);
		}
		
		int row = iTable.addRow("", line);
		iTable.getRowFormatter().addStyleName(row, "unitime-NoPrint");
		if (iVisibleCourses != null) iTable.getRowFormatter().setVisible(row, false);
		for (int col = 0; col < line.size(); col++)
			if (!iTable.getCellFormatter().isVisible(0, col))
				iTable.getCellFormatter().setVisible(row, col, false);
	}
	
	private int compareTwoRows(int column, int r0, int r1) {
		boolean e1 = ((CurriculaCourseSelectionBox)iTable.getWidget(r0, 1)).getValue().isEmpty();
		boolean e2 = ((CurriculaCourseSelectionBox)iTable.getWidget(r1, 1)).getValue().isEmpty();
		if (e1 && !e2) return 1;
		if (e2 && !e1) return -1;
		if (column == 0) {
			HorizontalPanel p0 = (HorizontalPanel)iTable.getWidget(r0, 0);
			HorizontalPanel p1 = (HorizontalPanel)iTable.getWidget(r1, 0);
			TreeSet<Group> g0 = new TreeSet<Group>();
			TreeSet<Group> g1 = new TreeSet<Group>();
			for (int i = 0; i < p0.getWidgetCount(); i++) g0.add((Group)p0.getWidget(i));
			for (int i = 0; i < p1.getWidgetCount(); i++) g1.add((Group)p1.getWidget(i));
			Iterator<Group> i0 = g0.iterator();
			Iterator<Group> i1 = g1.iterator();
			while (i0.hasNext() || i1.hasNext()) {
				if (!i0.hasNext()) return 1;
				if (!i1.hasNext()) return -1;
				int cmp = i0.next().compareTo(i1.next());
				if (cmp != 0) return cmp;
			}
			return compareTwoRows(2, r0, r1);
		}
		if (column == 1)
			return ((CurriculaCourseSelectionBox)iTable.getWidget(r0, 1)).getValue().compareTo(((CurriculaCourseSelectionBox)iTable.getWidget(r1, 1)).getValue());
		if (column % 2 == 0) {
			Float s0 = ((ShareTextBox)iTable.getWidget(r0, column)).getDisplayedShare();
			Float s1 = ((ShareTextBox)iTable.getWidget(r1, column)).getDisplayedShare();
			return - (s0 == null ? new Float(0) : s0).compareTo(s1 == null ? new Float(0) : s1);
		} else {
			EnrollmentLabel l0 = ((EnrollmentLabel)iTable.getWidget(r0, column));
			EnrollmentLabel l1 = ((EnrollmentLabel)iTable.getWidget(r1, column));
			Mode mode = CurriculumCookie.getInstance().getCurriculaCoursesMode();
			Integer i0 = (mode == Mode.ENRL ? l0.iEnrollment : mode == Mode.LAST ? l0.iLastLike : mode == Mode.REQ ? l0.iRequested : l0.iProjection);
			Integer i1 = (mode == Mode.ENRL ? l1.iEnrollment : mode == Mode.LAST ? l1.iLastLike : mode == Mode.REQ ? l0.iRequested : l1.iProjection);
			return - (i0 == null ? new Integer(0) : i0).compareTo(i1 == null ? new Integer(0) : i1);
		}
	}
	
	public int getCourseIndex(String course) {
		for (int row = 1; row < iTable.getRowCount(); row++) {
			String c = ((CurriculaCourseSelectionBox)iTable.getWidget(row, 1)).getText();
			if (course.equals(c)) return row - 1;
		}
		return -1;
	}
	
	public boolean setEnrollmentAndLastLike(String course, int clasf, Integer enrollment, Integer lastLike, Integer projection, Integer requested) {
		boolean changed = false;
		for (int row = 1; row < iTable.getRowCount(); row++) {
			String c = ((CurriculaCourseSelectionBox)iTable.getWidget(row, 1)).getText();
			if (!course.equals(c)) continue;
			EnrollmentLabel note = ((EnrollmentLabel)iTable.getWidget(row, 3 + 2 * clasf));
			note.iEnrollment = enrollment;
			note.iLastLike = lastLike;
			note.iProjection = projection;
			note.iRequested = requested;
			note.update();
			changed = true;
		}
		return changed;
	}
	
	public void updateEnrollmentsAndLastLike(HashMap<String, CurriculumStudentsInterface[]> courses, boolean showEmptyCourses) {
		iLastCourses = courses;
		rows: for (int row = 1; row < iTable.getRowCount() - 1; ) {
			for (int col = 0; col < iClassifications.getClassifications().size(); col ++) {
				ShareTextBox text = (ShareTextBox)iTable.getWidget(row, 2 + 2 * col);
				if (!text.getText().isEmpty()) {
					row ++;
					continue rows;
				}
			}
			iTable.removeRow(row);
		}
		HashSet<String> updated = new HashSet<String>();
		for (int row = 1; row < iTable.getRowCount(); row++) {
			String c = ((CurriculaCourseSelectionBox)iTable.getWidget(row, 1)).getText();
			if (c.isEmpty()) continue;
			updated.add(c);
			CurriculumStudentsInterface[] cc = courses.get(c);
			for (int col = 0; col < iClassifications.getClassifications().size(); col++) {
				EnrollmentLabel note = ((EnrollmentLabel)iTable.getWidget(row, 3 + 2 * col));
				note.iEnrollment = (cc == null || cc[col] == null ? null : cc[col].getEnrollment());
				note.iLastLike = (cc == null || cc[col] == null ? null : cc[col].getLastLike());
				note.iProjection = (cc == null || cc[col] == null ? null : cc[col].getProjection());
				note.iRequested = (cc == null || cc[col] == null ? null : cc[col].getRequested());
				note.update();
			}
		}
		CurriculumStudentsInterface[] total = courses.get("");
		if (total == null) return;
		int totalEnrollment = 0, totalLastLike = 0, totalRequested = 0;
		for (int i = 0; i < total.length; i++) {
			if (total[i] != null) totalEnrollment += total[i].getEnrollment();
			if (total[i] != null) totalLastLike += total[i].getLastLike();
			if (total[i] != null) totalRequested += total[i].getRequested();
		}
		TreeSet<Map.Entry<String, CurriculumStudentsInterface[]>> include = new TreeSet<Map.Entry<String,CurriculumStudentsInterface[]>>(new Comparator<Map.Entry<String,CurriculumStudentsInterface[]>>() {
			/*
			private int highestClassification(CurriculumStudentsInterface[] a) {
				int best = a.length;
				int bestVal = -1;
				for (int i = 0; i < a.length; i++) {
					if (a[i] == null) continue;
					if (a[i].getEnrollment() > bestVal) {
						bestVal = a[i].getEnrollment(); best = i;
					}
					if (a[i].getLastLike() > bestVal) {
						bestVal = a[i].getLastLike(); best = i;
					}
				}
				return best;
			}
			*/
			private int firstClassification(CurriculumStudentsInterface[] a) {
				for (int i = 0; i < a.length; i++) {
					if (a[i] == null) continue;
					if (a[i].getEnrollment() > 0) return i;
					if (a[i].getLastLike() > 0) return i;
					if (a[i].getProjection() > 0) return i;
					if (a[i].getRequested() > 0) return i;
				}
				return a.length;
			}
			public int compare(Map.Entry<String,CurriculumStudentsInterface[]> c0, Map.Entry<String,CurriculumStudentsInterface[]> c1) {
				/*
				int a0 = highestClassification(c0.getValue());
				int a1 = highestClassification(c1.getValue());
				if (a0 < a1) return -1;
				if (a0 > a1) return 1;
				if (a0 < c0.getValue().length) {
					int v0 = (c0.getValue()[a0][0] == null ? 0 : c0.getValue()[a0][0]);
					int v1 = (c1.getValue()[a0][0] == null ? 0 : c1.getValue()[a0][0]);
					int w0 = (c0.getValue()[a0][1] == null ? 0 : c0.getValue()[a0][1]);
					int w1 = (c1.getValue()[a0][1] == null ? 0 : c1.getValue()[a0][1]);
					if (v0 < v1 || w0 < w1) return -1;
					if (v0 > v1 || w0 > w1) return 1;
				}
				*/
				int b0 = firstClassification(c0.getValue());
				int b1 = firstClassification(c1.getValue());
				if (b0 < b1) return -1;
				if (b0 > b1) return 1;
				while (b0 < c0.getValue().length) {
					int v0 = (c0.getValue()[b0] == null ? 0 : c0.getValue()[b0].getEnrollment());
					int v1 = (c1.getValue()[b0] == null ? 0 : c1.getValue()[b0].getEnrollment());
					int w0 = (c0.getValue()[b0] == null ? 0 : c0.getValue()[b0].getLastLike());
					int w1 = (c1.getValue()[b0] == null ? 0 : c1.getValue()[b0].getLastLike());
					int x0 = (c0.getValue()[b0] == null ? 0 : c0.getValue()[b0].getRequested());
					int x1 = (c1.getValue()[b0] == null ? 0 : c1.getValue()[b0].getRequested());
					if (v0 > v1 || w0 > w1 || x0 > x1) return -1;
					if (v0 < v1 || w0 < w1 || x0 < x1) return 1;
					b0++;
				}
				return c0.getKey().compareTo(c1.getKey());
			}
		});
		for (Map.Entry<String, CurriculumStudentsInterface[]> course: courses.entrySet()) {
			if (updated.contains(course.getKey()) || course.getKey().isEmpty()) continue;
			CurriculumStudentsInterface[] cc = course.getValue();
			int enrollment = 0, lastLike = 0, requested = 0;
			for (int i = 0; i < cc.length; i++) {
				if (cc[i] != null) enrollment += cc[i].getEnrollment();
				if (cc[i] != null) lastLike += cc[i].getLastLike();
				if (cc[i] != null) requested += cc[i].getRequested();
			}
			if ((totalEnrollment > 0 && 100.0f * enrollment / totalEnrollment > 3.0f) ||
				(totalLastLike > 0 && 100.0f * lastLike / totalLastLike > 3.0f) ||
				(totalRequested > 0 && 100.0f * requested / totalRequested > 3.0f)) {
				include.add(course);
			}
		}
		if (showEmptyCourses)
			for (Map.Entry<String, CurriculumStudentsInterface[]> course: include) {
				CurriculumStudentsInterface[] cc = course.getValue();
				int row = iTable.getRowCount() - 1;
				if (!iEditable) row++;
				addBlankLine();
				CurriculaCourseSelectionBox c = (CurriculaCourseSelectionBox)iTable.getWidget(row, 1);
				c.setValue(course.getKey(), false);
				iTable.getRowFormatter().removeStyleName(row, "unitime-NoPrint");
				for (int col = 0; col < iClassifications.getClassifications().size(); col++) {
					EnrollmentLabel note = ((EnrollmentLabel)iTable.getWidget(row, 3 + 2 * col));
					note.iEnrollment = (cc == null || cc[col] == null ? null : cc[col].getEnrollment());
					note.iLastLike = (cc == null || cc[col] == null ? null : cc[col].getLastLike());
					note.iProjection = (cc == null || cc[col] == null ? null : cc[col].getProjection());
					note.iRequested = (cc == null || cc[col] == null ? null : cc[col].getRequested());
					note.update();
				}
				if (iVisibleCourses!=null) {
					if (iVisibleCourses.contains(course.getKey())) {
						((CurriculaCourseSelectionBox)iTable.getWidget(row, 1)).setEnabled(false);
						iTable.getRowFormatter().setVisible(row, true);
					} else {
						iTable.getRowFormatter().setVisible(row, false);
					}
				}
			}
	}
	
	public void expectedChanged(int col, int expected) {
		if (!CurriculumCookie.getInstance().getCurriculaCoursesPercent()) {
			for (int row = 1; row < iTable.getRowCount(); row++) {
				((ShareTextBox)iTable.getWidget(row, 2 + 2 * col)).update();
			}
		}
	}
	
	private void setPercent(boolean percent) {
		if (CurriculumCookie.getInstance().getCurriculaCoursesPercent() == percent) return;
		CurriculumCookie.getInstance().setCurriculaCoursesPercent(percent);
		for (int row = 1; row < iTable.getRowCount(); row++) {
			for (int col = 0; col < iClassifications.getClassifications().size(); col++) {
				((ShareTextBox)iTable.getWidget(row, 2 + 2 * col)).update();
				((EnrollmentLabel)iTable.getWidget(row, 3 + 2 * col)).update();
			}
		}
	}
	
	private void setMode(Mode mode) {
		CurriculumCookie.getInstance().setCurriculaCoursesMode(mode);
		for (int col = 0; col < iClassifications.getClassifications().size(); col++) {
			((HTML)iTable.getWidget(0, 3 + 2 * col)).setHTML(mode.getAbbv());
		}
		for (int row = 1; row < iTable.getRowCount(); row++) {
			for (int col = 0; col < iClassifications.getClassifications().size(); col++) {
				((EnrollmentLabel)iTable.getWidget(row, 3 + 2 * col)).update();
			}
		}
	}
	
	public void setVisible(int col, boolean visible) {
		for (int row = 0; row < iTable.getRowCount(); row++) {
			iTable.getFlexCellFormatter().setVisible(row, 2 + 2 * col, visible);
			iTable.getFlexCellFormatter().setVisible(row, 3 + 2 * col, visible);
		}
	}
	
	public class EnrollmentLabel extends Label implements HasCellAlignment {
		private int iColumn;
		private Integer iEnrollment, iLastLike, iProjection, iRequested;
		
		public EnrollmentLabel(int column, Integer enrollment, Integer lastLike, Integer projection, Integer requested) {
			super();
			setStyleName("unitime-Label");
			iColumn = column;
			iEnrollment = enrollment;
			iLastLike = lastLike;
			iProjection = projection;
			iRequested = requested;
			update();
		}
		
		public void update() {
			switch (CurriculumCookie.getInstance().getCurriculaCoursesMode()) {
			case NONE: // None
				setText("");
				break;
			case ENRL: // Enrollment
				if (iEnrollment == null || iEnrollment == 0) {
					setText("");
				} else if (CurriculumCookie.getInstance().getCurriculaCoursesPercent()) {
					Integer total = iClassifications.getEnrollment(iColumn);
					setText(total == null ? MESSAGES.notApplicable() : NF.format(100.0 * iEnrollment / total) + "%");
				} else {
					setText(iEnrollment.toString());
				}
				break;
			case LAST: // Last-like
				if (iLastLike == null || iLastLike == 0) {
					setText("");
				} else if (CurriculumCookie.getInstance().getCurriculaCoursesPercent()) {
					Integer total = iClassifications.getLastLike(iColumn);
					setText(total == null ? MESSAGES.notApplicable() : NF.format(100.0 * iLastLike / total) + "%");
				} else {
					setText(iLastLike.toString());
				}
				break;
			case PROJ: // Projection
				if (iProjection == null || iProjection == 0) {
					setText("");
				} else if (CurriculumCookie.getInstance().getCurriculaCoursesPercent()) {
					Integer total = iClassifications.getProjection(iColumn);
					setText(total == null ? MESSAGES.notApplicable() : NF.format(100.0 * iProjection / total) + "%");
				} else {
					setText(iProjection.toString());
				}
				break;
			case REQ: // Course Requests
				if (iRequested == null || iRequested == 0) {
					setText("");
				} else if (CurriculumCookie.getInstance().getCurriculaCoursesPercent()) {
					Integer total = iClassifications.getRequested(iColumn);
					setText(total == null ? MESSAGES.notApplicable() : NF.format(100.0 * iRequested / total) + "%");
				} else {
					setText(iRequested.toString());
				}
				break;
			}
		}
		
		public Integer getLastLike() { return (iLastLike == null || iLastLike == 0 ? null : iLastLike); }
		
		public Integer getEnrollment() { return (iEnrollment == null || iEnrollment == 0 ? null : iEnrollment); }

		public Integer getProjection() { return (iProjection == null || iProjection == 0 ? null : iProjection); }
		
		public Integer getRequested() { return (iRequested == null || iRequested == 0 ? null : iRequested); }

		public Float getLastLikePercent() { 
			if (iLastLike == null || iLastLike == 0) return null;
			Integer total = iClassifications.getLastLike(iColumn);
			if (total == null) return null;
			return ((float)iLastLike) / total;
		}
		
		public Float getEnrollmentPercent() { 
			if (iEnrollment == null || iEnrollment == 0) return null;
			Integer total = iClassifications.getEnrollment(iColumn);
			if (total == null) return null;
			return ((float)iEnrollment) / total;
		}

		public Float getProjectionPercent() { 
			if (iProjection == null || iProjection == 0) return null;
			Integer total = iClassifications.getProjection(iColumn);
			if (total == null) return null;
			return ((float)iProjection) / total;
		}
		
		public Float getRequestedPercent() { 
			if (iRequested == null || iRequested == 0) return null;
			Integer total = iClassifications.getRequested(iColumn);
			if (total == null) return null;
			return ((float)iRequested) / total;
		}

		@Override
		public HorizontalAlignmentConstant getCellAlignment() {
			return HasHorizontalAlignment.ALIGN_RIGHT;
		}
	}

	public class ShareTextBox extends UniTimeTextBox {
		private int iColumn;
		private Float iShare = null, iDefaultShare = null;
		
		public ShareTextBox(int column, Float share, Float defaultShare) {
			super(6, ValueBoxBase.TextAlignment.RIGHT);
			iColumn = column;
			iShare = share;
			iDefaultShare = defaultShare;
			addChangeHandler(new ChangeHandler() {
				@Override
				public void onChange(ChangeEvent event) {
					try {
						if (getText().isEmpty()) {
							iShare = null;
						} else if (getText().endsWith("%")) {
							iShare = (float)NF.parse(getText().substring(0, getText().length() - 1)) / 100.0f;
							if (iShare > 1.0f) iShare = 1.0f;
							if (iShare <= 0.0f) iShare = 0.0f;
						} else {
							Integer exp = iClassifications.getExpected(iColumn);
							if (exp == null || exp == 0)
								iShare = (float)NF.parse(getText()) / 100.0f;
							else
								iShare = (float)NF.parse(getText()) / iClassifications.getExpected(iColumn);
							if (iShare > 1.0f) iShare = 1.0f;
							if (iShare < 0.0f) iShare = 0.0f;
						}
					} catch (Exception e) {
						iShare = null;
					}
					update();
				}
			});
			update();
		}
		
		public void setShare(Float share) {
			iShare = share;
			update();
		}
		
		public void setExpected(Integer expected) {
			if (expected == null) {
				iShare = null;
			} else {
				Integer total = iClassifications.getExpected(iColumn);
				if (total == null) {
					iShare = null;
				} else {
					iShare = ((float)expected) / total;
				}
			}
			update();
		}
		
		public Float getShare() {
			return iShare;
		}
		
		public Float getDisplayedShare() {
			return iShare == null ? iDefaultShare : iShare;
		}
		
		public Float getDefaultShare() {
			return iDefaultShare;
		}
		
		public void update() {
			if (iShare == null)  {
				if (iDefaultShare != null) {
					if (CurriculumCookie.getInstance().getCurriculaCoursesPercent() || new Integer(0).equals(iClassifications.getExpected(iColumn)))
						setText(NF.format(100.0 * iDefaultShare) + "%");
					else {
						Integer exp = iClassifications.getExpected(iColumn);
						setText(exp == null ? MESSAGES.notApplicable() : String.valueOf(Math.round(exp * iDefaultShare)));	
					}
					getElement().getStyle().setColor("gray");
				} else {
					setText("");
					getElement().getStyle().clearColor();
				}
			} else if (CurriculumCookie.getInstance().getCurriculaCoursesPercent() || new Integer(0).equals(iClassifications.getExpected(iColumn))) {
				setText(NF.format(100.0 * iShare) + "%");
				getElement().getStyle().clearColor();
			} else {
				Integer exp = iClassifications.getExpected(iColumn);
				setText(exp == null ? MESSAGES.notApplicable() : String.valueOf(Math.round(exp * iShare)));
				getElement().getStyle().clearColor();
			}
		}
	}
	
	public static class CourseChangedEvent {
		private String iCourseName = null;
		public CourseChangedEvent(String courseName) {
			iCourseName = courseName;
		}
		public String getCourseName() { return iCourseName; }
	}
	
	public class Group extends Label implements Comparable<Group> {
		private String iName;
		private int iType;
		private String iColor;
		private Operation iOperation;
		private boolean iGroupEditable;
		
		public Group(String name, int type, boolean editable) {
			super(name, false);
			iName = name;
			iType = type;
			iGroupEditable = editable;
			setStylePrimaryName("unitime-TinyLabel" + (iType == 1 ? "White" : ""));
			if (iEditable && !iGroupEditable)
				getElement().getStyle().setFontStyle(FontStyle.ITALIC);
			if (iEditable && iGroupEditable) {
				addClickHandler(iNewGroupDialog.getClickHandler());
				getElement().getStyle().setCursor(Cursor.POINTER);
			}
			iOperation = new Operation() {
				@Override
				public String getName() {
					return getElement().getString();
				}
				@Override
				public boolean hasSeparator() {
					return false;
				}

				@Override
				public boolean isApplicable() {
					return iEditable && iGroupEditable && iVisibleCourses == null;
				}

				@Override
				public void execute() {
					assignGroup(null, iName, iType);
				}
			};
		}
		
		public Group(String name, int type) {
			this(name, type, true);
		}
		
		public String getName() { return iName; }
		public int getType() { return iType; }
		public void setType(int type) {
			iType = type;
			setStylePrimaryName("unitime-TinyLabel" + (iType == 1 ? "White" : ""));
		}
		public void setName(String name) {
			iName = name;
			setText(name);
		}
		public boolean equals(Object o) {
			if (o == null || !(o instanceof Group)) return false;
			return getName().equals(((Group)o).getName());
		}
		public Group cloneGroup() {
			Group g = new Group(iName, iType, iGroupEditable);
			g.setColor(getColor());
			return g;
		}
		public String getColor() {
			return iColor;
		}
		public void setColor(String color) {
			iColor = color;
			addStyleName(color);
		}
		public int compareTo(Group g) {
			return getName().compareTo(g.getName());
		}
		
		public Operation getOperation() { return iOperation; }
		public boolean isEditable() { return iGroupEditable; }
	}
	
	public void assignGroup(String oldName, String name, int type) {
		Group g = null;
		for (Group x: iGroups) {
			if (x.getName().equals(oldName == null ? name : oldName)) { g = x; break; }
		}
		if (g == null) {
			if (name == null || name.isEmpty()) return;
			g = new Group(name, type);
			colors: for (String c: sColors) {
				for (Group x: iGroups) {
					if (x.getColor().equals(c)) continue colors;
				}
				g.setColor(c);
				break;
			}
			iGroups.add(g);
		} else {
			rows: for (int row = 1; row < iTable.getRowCount(); row++ ) {
				HorizontalPanel p = (HorizontalPanel)iTable.getWidget(row, 0);
				for (int i = 0; i < p.getWidgetCount(); i++) {
					Group x = (Group)p.getWidget(i);
					if (x.equals(g)) {
						if (name == null || name.isEmpty()) {
							p.remove(i);
							continue rows;
						} else {
							x.setName(name); x.setType(type);
						}
					}
				}
			}
			if (name == null || name.isEmpty()) {
				iGroups.remove(g);
				return;
			} else {
				g.setName(name);
				g.setType(type);
			}
		}
		if (oldName != null) return;
		boolean nothing = true;
		boolean hasNoGroup = false;
		rows: for (int row = 1; row < iTable.getRowCount(); row++ ) {
			if (!iTable.isSelected(row)) continue;
			nothing = false;
			HorizontalPanel p = (HorizontalPanel)iTable.getWidget(row, 0);
			for (int i = 0; i < p.getWidgetCount(); i++) {
				Group x = (Group)p.getWidget(i);
				if (x.equals(g)) continue rows;
			}
			hasNoGroup = true;
			break;
		}
		if (nothing) {
			boolean select = false;
			for (int row = 1; row < iTable.getRowCount(); row++ ) {
				HorizontalPanel p = (HorizontalPanel)iTable.getWidget(row, 0);
				for (int i = 0; i < p.getWidgetCount(); i++) {
					Group x = (Group)p.getWidget(i);
					if (x.equals(g)) {
						iTable.setSelected(row, true);
						select = true;
					}
				}
			}
			if (select) return;
		}
		rows: for (int row = 1; row < iTable.getRowCount(); row++ ) {
			if (!iTable.isSelected(row)) continue;
			iTable.setSelected(row, false);
			HorizontalPanel p = (HorizontalPanel)iTable.getWidget(row, 0);
			for (int i = 0; i < p.getWidgetCount(); i++) {
				Group x = (Group)p.getWidget(i);
				if (x.equals(g)) {
					if (!hasNoGroup) p.remove(i);
					continue rows;
				}
			}
			p.add(g.cloneGroup());
		}
		boolean found = false;
		rows: for (int row = 1; row < iTable.getRowCount(); row++ ) {
			HorizontalPanel p = (HorizontalPanel)iTable.getWidget(row, 0);
			for (int i = 0; i < p.getWidgetCount(); i++) {
				Group x = (Group)p.getWidget(i);
				if (x.equals(g)) {
					found = true; break rows;
				}
			}
		}
		if (!found) iGroups.remove(g);
	}
	
	public List<Group> getGroups() { return iGroups; }
	
	public void showOnlyCourses(TreeSet<CourseInterface> courses) {
		iVisibleCourses = new TreeSet<String>();
		for (CourseInterface c: courses) iVisibleCourses.add(c.getCourseName());
		for (int row = 1; row < iTable.getRowCount(); row++) {
			String courseName = ((CurriculaCourseSelectionBox)iTable.getWidget(row, 1)).getText();
			if (iVisibleCourses.contains(courseName)) {
				((CurriculaCourseSelectionBox)iTable.getWidget(row, 1)).setEnabled(false);
				iTable.getRowFormatter().setVisible(row, true);
			} else {
				iTable.getRowFormatter().setVisible(row, false);
			}
		}
	}
	
	public void showAllCourses() {
		if (iVisibleCourses != null) {
			for (int i = 1; i < iTable.getRowCount(); i++) {
				String courseName = ((CurriculaCourseSelectionBox)iTable.getWidget(i, 1)).getText();
				iTable.setSelected(i, iVisibleCourses.contains(courseName));
			}
		}
		iVisibleCourses = null;
		for (int row = 1; row < iTable.getRowCount(); row++) {
			((CurriculaCourseSelectionBox)iTable.getWidget(row, 1)).setEnabled(!hasDefaultShare(row));
			iTable.getRowFormatter().setVisible(row, true);
		}
	}
	
	public boolean canShowStudentsTable(int row) {
		if (CurriculumCookie.getInstance().getCurriculaCoursesMode() == Mode.NONE) return false;
		if (row < 1 || row >= iTable.getRowCount()) return false;
		String course = ((CurriculaCourseSelectionBox)iTable.getWidget(row, 1)).getText();
		if (iLastCourses == null || !iLastCourses.containsKey(course)) return false;
		int nrOther = 0;
		for (int r = 1; r < iTable.getRowCount(); r ++) {
			if (r == row || !iTable.isSelected(r)) continue;
			nrOther ++;
		}
		return (nrOther > 0);
	}
	
	public class StudentsTable extends Composite {
		private FlexTable iT = new FlexTable();
		private VerticalPanel iP = new VerticalPanel();
		private boolean iCanShow = false;
		
		private int count(CurriculumStudentsInterface c, Set<Long> students) {
			if (CurriculumCookie.getInstance().getCurriculaCoursesMode() != Mode.PROJ || c == null) return students.size();
			return c.countProjectedStudents(students);
		}
		
		private StudentsTable(int currentRow) {
			super();
			
			String course = ((CurriculaCourseSelectionBox)iTable.getWidget(currentRow, 1)).getText();
			
			iP.add(new Label(MESSAGES.hintComparingStudentsWithOtherCourses(course + " " + CurriculumCookie.getInstance().getCurriculaCoursesMode().getName().toLowerCase().replace(" enrollment", ""))));
			iP.add(iT);
			initWidget(iP);
			
			if (iLastCourses == null) return;
			CurriculumStudentsInterface[] thisCourse = iLastCourses.get(course);
			CurriculumStudentsInterface[] totals = iLastCourses.get("");
			if (thisCourse == null) return;
			
			int column = 0;
			for (int c = 0; c < iClassifications.getClassifications().size(); c++) {
				if (iClassifications.getExpected(c) == null) continue;
				iT.setText(0, 1 + column, iClassifications.getName(c));
				iT.getCellFormatter().setWidth(0, 1 + column, "50px");
				iT.getCellFormatter().setStyleName(0, 1 + column, "unitime-DashedBottom");
				column++;
			}
			
			iT.setText(1, 0, MESSAGES.hintStudentsInOneOtherCourse());
			iT.setText(2, 0, MESSAGES.hintStudentsInTwoOtherCourses());
			iT.setText(3, 0, MESSAGES.hintStudentsInThreeOtherCourses());
			iT.setText(4, 0, MESSAGES.hintStudentsInAllOtherCourses());
			iT.setText(5, 0, MESSAGES.hintStudentsNotInAnyOtherCourse());
			int row = 0;
			List<CurriculumStudentsInterface[]> other = new ArrayList<CurriculumStudentsInterface[]>();
			for (int r = 1; r < iTable.getRowCount(); r ++) {
				if (r == currentRow || !iTable.isSelected(r)) continue;
				String c = ((CurriculaCourseSelectionBox)iTable.getWidget(r, 1)).getText();
				if (c.isEmpty()) continue;
				other.add(iLastCourses.get(c));
				iT.setText(6 + row, 0, MESSAGES.hinStudentsSharedWith(c));
				row++;
			}

			column = 0;
			int totalC[] = new int [other.size()];
			for (int i = 0; i < totalC.length; i++)
				totalC[i] = 0;
			boolean has1 = false, has2 = false, has3 = false, hasAll = false, hasNone = false;
			for (int c = 0; c < iClassifications.getClassifications().size(); c++) {
				CurriculumStudentsInterface tc = totals[c];
				if (iClassifications.getExpected(c) == null) continue;
				Set<Long> thisEnrollment = (thisCourse[c] == null ? null : (CurriculumCookie.getInstance().getCurriculaCoursesMode() == Mode.ENRL ? thisCourse[c].getEnrolledStudents() :
					CurriculumCookie.getInstance().getCurriculaCoursesMode() == Mode.REQ ? thisCourse[c].getRequestedStudents() : thisCourse[c].getLastLikeStudents()));
				if (thisEnrollment != null && count(tc,thisEnrollment) != 0) {
					Set<Long> sharedWithOneOther = new HashSet<Long>();
					Set<Long> sharedWithTwoOther = new HashSet<Long>();
					Set<Long> sharedWithThreeOther = new HashSet<Long>();
					Set<Long> sharedWithAll = new HashSet<Long>(thisEnrollment);
					Set<Long> notShared = new HashSet<Long>(thisEnrollment);
					row = 0;
					for (CurriculumStudentsInterface[] o: other) {
						Set<Long> enrl = (o == null || o[c] == null ? null : CurriculumCookie.getInstance().getCurriculaCoursesMode() == Mode.ENRL ? o[c].getEnrolledStudents() :
								CurriculumCookie.getInstance().getCurriculaCoursesMode() == Mode.REQ ? o[c].getRequestedStudents() : o[c].getLastLikeStudents());
						if (enrl == null) {
							sharedWithAll.clear();
							row++;
							continue;
						}
						Set<Long> share = new HashSet<Long>();
						for (Long s: thisEnrollment) {
							if (enrl.contains(s)) {
								if (!sharedWithOneOther.add(s))
									if (!sharedWithTwoOther.add(s))
										sharedWithThreeOther.add(s);
								share.add(s);
							}
						}
						for (Iterator<Long> i = sharedWithAll.iterator(); i.hasNext(); )
							if (!enrl.contains(i.next())) i.remove();
						for (Iterator<Long> i = notShared.iterator(); i.hasNext(); )
							if (enrl.contains(i.next())) i.remove();
						if (!share.isEmpty() && count(tc, share) != 0) {
							totalC[row] += share.size();
							iT.setText(6 + row, 1 + column, (CurriculumCookie.getInstance().getCurriculaCoursesPercent() ? NF.format(100.0 * count(tc, share) / count(tc,thisEnrollment)) + "%" : "" + count(tc,share)));
						}
						row++;
					}
					boolean percent = CurriculumCookie.getInstance().getCurriculaCoursesPercent();
					if (!sharedWithOneOther.isEmpty() && count(tc,sharedWithOneOther) != 0) {
						iT.setText(1, 1 + column, (percent ? NF.format(100.0 * count(tc,sharedWithOneOther) / count(tc,thisEnrollment)) + "%" : "" + count(tc,sharedWithOneOther)));
						has1 = true;
					}
					if (!sharedWithTwoOther.isEmpty() && count(tc,sharedWithTwoOther) != 0) {
						iT.setText(2, 1 + column, (percent ? NF.format(100.0 * count(tc,sharedWithTwoOther) / count(tc,thisEnrollment)) + "%" : "" + count(tc,sharedWithTwoOther)));
						has2 = true;
					}
					if (!sharedWithThreeOther.isEmpty() && count(tc,sharedWithThreeOther) != 0) {
						iT.setText(3, 1 + column, (percent ? NF.format(100.0 * count(tc,sharedWithThreeOther) / count(tc,thisEnrollment)) + "%" : "" + count(tc,sharedWithThreeOther)));
						has3 = true;
					}
					if (!sharedWithAll.isEmpty() && count(tc,sharedWithAll) != 0) {
						iT.setText(4, 1 + column, (percent ? NF.format(100.0 * count(tc,sharedWithAll) / count(tc,thisEnrollment)) + "%" : "" + count(tc,sharedWithAll)));
						hasAll = true;
					}
					if (!notShared.isEmpty() && count(tc,notShared) != 0) {
						iT.setText(5, 1 + column, (percent ? NF.format(100.0 * count(tc,notShared) / count(tc,thisEnrollment)) + "%" : "" + count(tc,notShared)));
						hasNone = true;
					}
				}
				column ++;
			}
			if (!has1 || other.size() == 1) iT.getRowFormatter().setVisible(1, false);
			if (!has2 || other.size() == 1) iT.getRowFormatter().setVisible(2, false);
			if (!has3 || other.size() == 1) iT.getRowFormatter().setVisible(3, false);
			if (!hasAll || other.size() <= 3) iT.getRowFormatter().setVisible(4, false);
			if (!hasNone || other.size() == 1) iT.getRowFormatter().setVisible(5, false);
			if (other.size() > 1) {
				int minTotal = -1;
				List<Integer> visible = new ArrayList<Integer>();
				for (row = other.size() - 1; row >= 0; row--) {
					if (totalC[row] < 1)
						iT.getRowFormatter().setVisible(6 + row, false);
					else {
						visible.add(row);
						if (minTotal < 0 || minTotal < totalC[row])
							minTotal = totalC[row];
					}
				}
				while (visible.size() > 10) {
					int limit = minTotal; minTotal = -1;
					for (Iterator<Integer> i = visible.iterator(); i.hasNext() && visible.size() > 10; ) {
						row = i.next();
						if (totalC[row] <= limit) {
							iT.getRowFormatter().setVisible(6 + row, false);
							i.remove();
						} else {
							if (minTotal < 0 || minTotal < totalC[row])
								minTotal = totalC[row];
						}
					}
				}
				if (!visible.isEmpty()) {
					int r = 6 + visible.get(visible.size() - 1);
					int col = 1;
					for (int c = 0; c < iClassifications.getClassifications().size(); c++) {
						if (iClassifications.getExpected(c) == null) continue;
						if (iT.getCellCount(r) <= col || iT.getText(r, col) == null || iT.getText(r, col).isEmpty()) iT.setHTML(r, col, "&nbsp;");
						iT.getCellFormatter().setStyleName(r, col, "unitime-DashedTop");
						col++;
					}
				}

			}
						
			iCanShow = has1 || has2 || hasAll || hasNone;
		}
		
		public boolean canShow() { return iCanShow; }
		
	}
	
	private class GroupDialogBox extends UniTimeDialogBox {
		private TextBox iGrName;
		private ListBox iGrType;
		private Button iGrAssign, iGrDelete, iGrUpdate;
		private String iGrOldName = null;
		private ClickHandler iGrHandler;

		private GroupDialogBox() {
			super(true, true);
			FlexTable groupTable = new FlexTable();
			groupTable.setCellSpacing(2);
			groupTable.setText(0, 0, MESSAGES.propName());
			iGrName = new UniTimeTextBox();
			groupTable.setWidget(0, 1, iGrName);
			groupTable.setText(1, 0, MESSAGES.propType());
			iGrType = new ListBox();
			iGrType.addItem(MESSAGES.groupDifferentStudents());
			iGrType.addItem(MESSAGES.groupSameStudents());
			iGrType.setSelectedIndex(0);
			groupTable.setWidget(1, 1, iGrType);
			HorizontalPanel grButtons = new HorizontalPanel();
			grButtons.setSpacing(2);
			iGrAssign = new Button(MESSAGES.opGroupAssign());
			grButtons.add(iGrAssign);
			iGrUpdate = new Button(MESSAGES.opGroupUpdate());
			grButtons.add(iGrUpdate);
			iGrDelete = new Button(MESSAGES.opGroupDelete());
			grButtons.add(iGrDelete);
			groupTable.setWidget(2, 1, grButtons);
			groupTable.getFlexCellFormatter().setHorizontalAlignment(2, 1, HasHorizontalAlignment.ALIGN_RIGHT);
			setWidget(groupTable);
			
			iGrAssign.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					hide();
					assignGroup(iGrOldName, iGrName.getText(), iGrType.getSelectedIndex());
				}
			});
			
			setEscapeToHide(true);
			setEnterToSubmit(new Command() {
				@Override
				public void execute() {
					hide();
					assignGroup(iGrOldName, iGrName.getText(), iGrType.getSelectedIndex());
				}
			});
			/*
			iGrName.addKeyUpHandler(new KeyUpHandler() {
				@Override
				public void onKeyUp(KeyUpEvent event) {
					if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
						hide();
						assignGroup(iGrOldName, iGrName.getText(), iGrType.getSelectedIndex());
					}
					if (event.getNativeKeyCode() == KeyCodes.KEY_ESCAPE) {
						hide();
					}
				}
			});
			*/
			
			iGrUpdate.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					hide();
					assignGroup(iGrOldName, iGrName.getText(), iGrType.getSelectedIndex());
				}
			});
			
			iGrDelete.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					hide();
					assignGroup(iGrOldName, null, iGrType.getSelectedIndex());
				}
			});
			
			iGrHandler = new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					setText(MESSAGES.dialogEditGroup());
					iGrOldName = ((Group)event.getSource()).getName();
					iGrName.setText(((Group)event.getSource()).getText());
					iGrType.setSelectedIndex(((Group)event.getSource()).getType());
					iGrAssign.setVisible(false);
					iGrDelete.setVisible(true);
					iGrUpdate.setVisible(true);
					Scheduler.get().scheduleDeferred(new ScheduledCommand() {
						@Override
						public void execute() {
							iGrName.setFocus(true);
							iGrName.selectAll();
						}
					});
					iTable.clearHover();
					event.stopPropagation();
					center();
				}
			};
		}
		
		public ClickHandler getClickHandler() {
			return iGrHandler;
		}
		
		public void openNew() {
			setText(MESSAGES.dialogNewGroup());
			iGrOldName = null;
			iGrName.setText(String.valueOf((char)('A' + getGroups().size())));
			iGrType.setSelectedIndex(0);
			iGrAssign.setVisible(true);
			iGrDelete.setVisible(false);
			iGrUpdate.setVisible(false);
			Scheduler.get().scheduleDeferred(new ScheduledCommand() {
				@Override
				public void execute() {
					iGrName.setFocus(true);
					iGrName.selectAll();
				}
			});
			center();
		}
	}
}
