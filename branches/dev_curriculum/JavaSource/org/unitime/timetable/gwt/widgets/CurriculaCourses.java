/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package org.unitime.timetable.gwt.widgets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.unitime.timetable.gwt.shared.CurriculumInterface;
import org.unitime.timetable.gwt.shared.CurriculumInterface.AcademicClassificationInterface;
import org.unitime.timetable.gwt.shared.CurriculumInterface.CourseInterface;
import org.unitime.timetable.gwt.shared.CurriculumInterface.CurriculumCourseGroupInterface;
import org.unitime.timetable.gwt.shared.CurriculumInterface.CurriculumCourseInterface;
import org.unitime.timetable.gwt.widgets.CurriculaClassifications.NameChangedEvent;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class CurriculaCourses extends Composite {
	private MyFlexTable iTable = null;
	
	private boolean iPercent = true;
	private static NumberFormat NF = NumberFormat.getFormat("##0.0");
	
	public static String[] MODES = new String[] { "None", "Enrollment", "Last-Like" };
	private static String[] MODE_SHORT = new String[] {"&nbsp;", "Enrl", "Last"};
	private int iMode = 0;
	
	private List<CourseChangedHandler> iCourseChangedHandlers = new ArrayList<CourseChangedHandler>();
	private List<Group> iGroups = new ArrayList<Group>();
	
	private CurriculaClassifications iClassifications;
	
	private CurriculaCourseSelectionBox.CourseSelectionChangeHandler iCourseChangedHandler = null;
	
	private DialogBox iNewGroupDialog;
	private TextBox iGrName;
	private ListBox iGrType;
	private Button iGrAssign, iGrDelete, iGrUpdate;
	private String iGrOldName = null;
	private ClickHandler iGrHandler;
	
	private static String[] sColors = new String[] {
		"red", "blue", "green", "orange", "yellow", "pink",
		"purple", "teal", "darkpurple", "steelblue", "lightblue",
		"lightgreen", "yellowgreen", "redorange", "lightbrown", "lightpurple",
		"grey", "bluegrey", "lightteal", "yellowgrey", "brown"
	};
	
	public CurriculaCourses() {
		iTable = new MyFlexTable();
		iTable.setCellPadding(2);
		iTable.setCellSpacing(0);
		initWidget(iTable);
		iCourseChangedHandler = new CurriculaCourseSelectionBox.CourseSelectionChangeHandler() {
			@Override
			public void onChange(String course, boolean valid) {
				if (valid) {
					CourseChangedEvent e = new CourseChangedEvent(course);
					for (CourseChangedHandler h: iCourseChangedHandlers)
						h.courseChanged(e);
				} else if (course.isEmpty()) {
					for (int col = 0; col < iClassifications.getClassifications().size(); col++) {
						setEnrollmentAndLastLike(course, col, null, null);
					}
				}
			}
		};
		
		iNewGroupDialog = new DialogBox();
		iNewGroupDialog.setAnimationEnabled(true);
		iNewGroupDialog.setAutoHideEnabled(true);
		iNewGroupDialog.setGlassEnabled(true);
		iNewGroupDialog.setModal(true);
		FlexTable groupTable = new FlexTable();
		groupTable.setCellSpacing(2);
		groupTable.setText(0, 0, "Name:");
		iGrName = new TextBox();
		groupTable.setWidget(0, 1, iGrName);
		groupTable.setText(1, 0, "Type:");
		iGrType = new ListBox();
		iGrType.addItem("No conflict (different students)");
		iGrType.addItem("Conflict (same students)");
		iGrType.setSelectedIndex(0);
		groupTable.setWidget(1, 1, iGrType);
		HorizontalPanel grButtons = new HorizontalPanel();
		grButtons.setSpacing(2);
		iGrAssign = new Button("Assign");
		grButtons.add(iGrAssign);
		iGrUpdate = new Button("Update");
		grButtons.add(iGrUpdate);
		iGrDelete = new Button("Delete");
		grButtons.add(iGrDelete);
		groupTable.setWidget(2, 1, grButtons);
		groupTable.getFlexCellFormatter().setHorizontalAlignment(2, 1, HasHorizontalAlignment.ALIGN_RIGHT);
		iNewGroupDialog.add(groupTable);
		
		iGrAssign.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				iNewGroupDialog.hide();
				assignGroup(iGrOldName, iGrName.getText(), iGrType.getSelectedIndex());
			}
		});
		
		iGrName.addKeyUpHandler(new KeyUpHandler() {
			@Override
			public void onKeyUp(KeyUpEvent event) {
				if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
					iNewGroupDialog.hide();
					assignGroup(iGrOldName, iGrName.getText(), iGrType.getSelectedIndex());
				}
				if (event.getNativeKeyCode() == KeyCodes.KEY_ESCAPE) {
					iNewGroupDialog.hide();
				}
			}
		});
		
		iGrUpdate.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				iNewGroupDialog.hide();
				assignGroup(iGrOldName, iGrName.getText(), iGrType.getSelectedIndex());
			}
		});
		
		iGrDelete.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				iNewGroupDialog.hide();
				assignGroup(iGrOldName, null, iGrType.getSelectedIndex());
			}
		});
		
		iGrHandler = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				iNewGroupDialog.setText("Edit group");
				iGrOldName = ((Group)event.getSource()).getName();
				iGrName.setText(((Group)event.getSource()).getText());
				iGrType.setSelectedIndex(((Group)event.getSource()).getType());
				iGrAssign.setVisible(false);
				iGrDelete.setVisible(true);
				iGrUpdate.setVisible(true);
				DeferredCommand.addCommand(new Command() {
					@Override
					public void execute() {
						iGrName.setFocus(true);
						iGrName.selectAll();
					}
				});
				iNewGroupDialog.center();
			}
		};
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
	
	public void populate(final CurriculumInterface curriculum) {
		for (int row = iTable.getRowCount() - 1; row >= 0; row--) {
			iTable.removeRow(row);
		}
		iTable.clear(true);
		iGroups.clear();
		iTable.setEnabled(curriculum.isEditable());
		
		// header
		final Label groupsLabel = new Label("Group");
		iTable.setWidget(0, 0, groupsLabel);
		iTable.getFlexCellFormatter().setStyleName(0, 0, "unitime-TableHeader");
		iTable.getFlexCellFormatter().setWidth(0, 0, "20px");
		groupsLabel.addMouseOverHandler(new MouseOverHandler() {
			@Override
			public void onMouseOver(MouseOverEvent event) {
				((Widget)event.getSource()).getElement().getStyle().setCursor(Cursor.POINTER);
			}
		});
		groupsLabel.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				final PopupPanel popup = new PopupPanel(true);
				MenuBar menu = new MenuBar(true);
				if (curriculum.isEditable()) {
					for (final CurriculaCourses.Group g: getGroups()) {
						menu.addItem(
								new MenuItem(
										DOM.toString(g.getElement()),
										true,
										new Command() {
											@Override
											public void execute() {
												popup.hide();
												assignGroup(null, g.getName(), g.getType());
											}
										}));
					}
					if (!getGroups().isEmpty()) {
						menu.addSeparator();
					}
					if (getSelectedCount() > 0) {
						menu.addItem(new MenuItem("New group...", true, new Command() {
							@Override
							public void execute() {
								popup.hide();
								iNewGroupDialog.setText("New group");
								iGrOldName = null;
								iGrName.setText(String.valueOf((char)('A' + getGroups().size())));
								iGrType.setSelectedIndex(0);
								iGrAssign.setVisible(true);
								iGrDelete.setVisible(false);
								iGrUpdate.setVisible(false);
								DeferredCommand.addCommand(new Command() {
									@Override
									public void execute() {
										iGrName.setFocus(true);
										iGrName.selectAll();
									}
								});
								iNewGroupDialog.center();
							}
						}));
						menu.addSeparator();
					}
				}
				menu.addItem(new MenuItem("Sort by Group", true, new Command() {
					@Override
					public void execute() {
						popup.hide();
						sort(0);
					}
				}));
				menu.setVisible(true);
				popup.add(menu);
				//popup.setPopupPosition(event.getNativeEvent().getScreenX(), event.getNativeEvent().getScreenY());
				popup.setPopupPosition(groupsLabel.getAbsoluteLeft(), groupsLabel.getAbsoluteTop() + 20);
				popup.show();
			}
		});
		
		final Label courseLabel = new Label("Course");
		iTable.setWidget(0, 1, courseLabel);
		iTable.getFlexCellFormatter().setStyleName(0, 1, "unitime-TableHeader");
		iTable.getFlexCellFormatter().setWidth(0, 1, "100px");
		courseLabel.addMouseOverHandler(new MouseOverHandler() {
			@Override
			public void onMouseOver(MouseOverEvent event) {
				((Widget)event.getSource()).getElement().getStyle().setCursor(Cursor.POINTER);
			}
		});
		courseLabel.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				final PopupPanel popup = new PopupPanel(true);
				MenuBar menu = new MenuBar(true);
				if (curriculum.isEditable()) {
					menu.addItem(new MenuItem("Select All", true, new Command() {
						@Override
						public void execute() {
							popup.hide();
							for (int i = 1; i < iTable.getRowCount(); i++)
								setSelected(i, !((CurriculaCourseSelectionBox)iTable.getWidget(i, 1)).getCourse().isEmpty());
						}
					}));
					if (getSelectedCount() > 0) {
						menu.addItem(new MenuItem("Clear Selection", true, new Command() {
							@Override
							public void execute() {
								popup.hide();
								for (int i = 1; i < iTable.getRowCount(); i++)
									setSelected(i, false);
							}
						}));
					}
					menu.addSeparator();
				}
				if (iPercent)
					menu.addItem(new MenuItem("Show Numbers", true, new Command() {
						@Override
						public void execute() {
							popup.hide();
							setPercent(false);
						}
					}));
				else
					menu.addItem(new MenuItem("Show Percentages", true, new Command() {
						@Override
						public void execute() {
							popup.hide();
							setPercent(true);
						}
					}));
				if (iMode != 1)
					menu.addItem(new MenuItem("Show " + MODES[1], true, new Command() {
						@Override
						public void execute() {
							popup.hide();
							setMode(1);
						}
					}));
				if (iMode != 2)
					menu.addItem(new MenuItem("Show " + MODES[2], true, new Command() {
						@Override
						public void execute() {
							popup.hide();
							setMode(2);
						}
					}));
				if (iMode > 0)
					menu.addItem(new MenuItem("Hide " + MODES[iMode], true, new Command() {
						@Override
						public void execute() {
							popup.hide();
							setMode(0);
						}
					}));
				menu.addSeparator();
				menu.addItem(new MenuItem("Sort by Course", true, new Command() {
					@Override
					public void execute() {
						popup.hide();
						sort(1);
					}
				}));
				menu.setVisible(true);
				popup.add(menu);
				popup.setPopupPosition(courseLabel.getAbsoluteLeft(), courseLabel.getAbsoluteTop() + 20);
				popup.show();
			}
		});
		
		int col = 1;
		for (AcademicClassificationInterface clasf: iClassifications.getClassifications()) {
			col++;
			final Label cl = new Label(clasf.getCode());
			iTable.setWidget(0, col, cl);
			iTable.getFlexCellFormatter().setStyleName(0, col, "unitime-TableHeader");
			iTable.getFlexCellFormatter().setWidth(0, col, "60px");
			iTable.getFlexCellFormatter().setHorizontalAlignment(0, col, HasHorizontalAlignment.ALIGN_RIGHT);
			final int x = col;
			cl.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					final PopupPanel popup = new PopupPanel(true);
					MenuBar menu = new MenuBar(true);
					if (curriculum.isEditable()) {
						menu.addItem(new MenuItem("Select All", true, new Command() {
							@Override
							public void execute() {
								popup.hide();
								for (int i = 1; i < iTable.getRowCount(); i++)
									setSelected(i, !((MyTextBox)iTable.getWidget(i, x)).getText().isEmpty());
							}
						}));
						if (getSelectedCount() > 0) {
							menu.addItem(new MenuItem("Clear Selection", true, new Command() {
								@Override
								public void execute() {
									popup.hide();
									for (int i = 1; i < iTable.getRowCount(); i++)
										setSelected(i, false);
								}
							}));
						}
						menu.addSeparator();
					}
					if (iPercent)
						menu.addItem(new MenuItem("Show Numbers", true, new Command() {
							@Override
							public void execute() {
								popup.hide();
								setPercent(false);
							}
						}));
					else
						menu.addItem(new MenuItem("Show Percentages", true, new Command() {
							@Override
							public void execute() {
								popup.hide();
								setPercent(true);
							}
						}));
					if (iMode != 1)
						menu.addItem(new MenuItem("Show " + MODES[1], true, new Command() {
							@Override
							public void execute() {
								popup.hide();
								setMode(1);
							}
						}));
					if (iMode != 2)
						menu.addItem(new MenuItem("Show " + MODES[2], true, new Command() {
							@Override
							public void execute() {
								popup.hide();
								setMode(2);
							}
						}));
					if (iMode > 0)
						menu.addItem(new MenuItem("Hide " + MODES[iMode], true, new Command() {
							@Override
							public void execute() {
								popup.hide();
								setMode(0);
							}
						}));
					menu.addSeparator();
					menu.addItem(new MenuItem("Sort by " + cl.getText(), true, new Command() {
						@Override
						public void execute() {
							popup.hide();
							sort(x);
						}
					}));
					menu.setVisible(true);
					popup.add(menu);
					popup.setPopupPosition(cl.getAbsoluteLeft(), cl.getAbsoluteTop() + 20);
					popup.show();
				}
			});
			cl.addMouseOverHandler(new MouseOverHandler() {
				@Override
				public void onMouseOver(MouseOverEvent event) {
					((Widget)event.getSource()).getElement().getStyle().setCursor(Cursor.POINTER);
				}
			});
			col++;
			final HTML m = new HTML(MODE_SHORT[iMode]);
			iTable.setWidget(0, col, m);
			iTable.getFlexCellFormatter().setStyleName(0, col, "unitime-TableHeader");
			iTable.getFlexCellFormatter().setWidth(0, col, "5px");
			iTable.getFlexCellFormatter().setHorizontalAlignment(0, col, HasHorizontalAlignment.ALIGN_CENTER);
			final int y = col;
			m.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					final PopupPanel popup = new PopupPanel(true);
					MenuBar menu = new MenuBar(true);
					menu.addItem(new MenuItem("Select All", true, new Command() {
						@Override
						public void execute() {
							popup.hide();
							for (int i = 1; i < iTable.getRowCount(); i++)
								setSelected(i, !((MyLabel)iTable.getWidget(i, y)).getText().isEmpty());
						}
					}));
					if (getSelectedCount() > 0) {
						menu.addItem(new MenuItem("Clear Selection", true, new Command() {
							@Override
							public void execute() {
								popup.hide();
								for (int i = 1; i < iTable.getRowCount(); i++)
									setSelected(i, false);
							}
						}));
					}
					menu.addSeparator();
					if (iPercent)
						menu.addItem(new MenuItem("Show Numbers", true, new Command() {
							@Override
							public void execute() {
								popup.hide();
								setPercent(false);
							}
						}));
					else
						menu.addItem(new MenuItem("Show Percentages", true, new Command() {
							@Override
							public void execute() {
								popup.hide();
								setPercent(true);
							}
						}));
					if (iMode != 1)
						menu.addItem(new MenuItem("Show " + MODES[1], true, new Command() {
							@Override
							public void execute() {
								popup.hide();
								setMode(1);
							}
						}));
					if (iMode != 2)
						menu.addItem(new MenuItem("Show " + MODES[2], true, new Command() {
							@Override
							public void execute() {
								popup.hide();
								setMode(2);
							}
						}));
					if (iMode > 0)
						menu.addItem(new MenuItem("Hide " + MODES[iMode], true, new Command() {
							@Override
							public void execute() {
								popup.hide();
								setMode(0);
							}
						}));
					menu.addSeparator();
					menu.addItem(new MenuItem("Sort by " + cl.getText() + " " + MODES[iMode], true, new Command() {
						@Override
						public void execute() {
							popup.hide();
							sort(y);
						}
					}));
					menu.setVisible(true);
					popup.add(menu);
					popup.setPopupPosition(m.getAbsoluteLeft(), m.getAbsoluteTop() + 20);
					popup.show();
				}
			});
			m.addMouseOverHandler(new MouseOverHandler() {
				@Override
				public void onMouseOver(MouseOverEvent event) {
					((Widget)event.getSource()).getElement().getStyle().setCursor(Cursor.POINTER);
				}
			});

		}
		
		// body
		int row = 0;
		if (curriculum.hasCourses()) {
			for (CourseInterface course: curriculum.getCourses()) {
				row ++;
				HorizontalPanel hp = new HorizontalPanel();
				iTable.setWidget(row, 0, hp);
				
				if (course.hasGroups()) {
					for (CurriculumCourseGroupInterface g: course.getGroups()) {
						Group gr = null;
						for (Group x: iGroups) {
							if (x.getName().equals(g.getName())) { gr = x; break; }
						}
						if (gr == null) {
							gr = new Group(g.getName(), g.getType(), curriculum.isEditable());
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
				
				CurriculaCourseSelectionBox cx = new CurriculaCourseSelectionBox(course.getId().toString(), iClassifications.getClassifications());
				cx.setCourse(course.getCourseName(), false);
				cx.setWidth("100px");
				cx.addCourseSelectionChangeHandler(iCourseChangedHandler);
				if (!curriculum.isEditable()) cx.setEnabled(false);
				iTable.setWidget(row, 1, cx);
				
				col = 0;
				for (final AcademicClassificationInterface clasf: iClassifications.getClassifications()) {
					CurriculumCourseInterface cci = course.getCurriculumCourse(col);
					MyTextBox ex = new MyTextBox(col, cci == null ? null : cci.getShare());
					if (!curriculum.isEditable()) ex.setEnabled(false);
					iTable.setWidget(row, 2 + 2 * col, ex);
					MyLabel note = new MyLabel(col, cci == null ? null : cci.getEnrollment(), cci == null ? null : cci.getLastLike());
					iTable.setWidget(row, 3 + 2 * col, note);
					iTable.getFlexCellFormatter().setHorizontalAlignment(row, 3 + 2 * col, HasHorizontalAlignment.ALIGN_RIGHT);
					col++;
				}
			}
		}
		if (curriculum.isEditable()) addBlankLine();
	}
	
	public boolean saveCurriculum(CurriculumInterface c) {
		boolean ret = true;
		HashSet<String> courses = new HashSet<String>();
		HashMap<String, CurriculumCourseGroupInterface> groups = new HashMap<String, CurriculumCourseGroupInterface>();
		if (c.hasCourses()) c.getCourses().clear();
		for (int row = 1; row < iTable.getRowCount(); row++) {
			String course = ((CurriculaCourseSelectionBox)iTable.getWidget(row, 1)).getCourse();
			if (course.isEmpty()) continue;
			if (!courses.add(course)) {
				((CurriculaCourseSelectionBox)iTable.getWidget(row, 1)).setError("Duplicate course " + course);
				ret = false;
				continue;
			}
			CourseInterface cr = new CourseInterface();
			cr.setCourseName(course);
			for (int i = 0; i < iClassifications.getClassifications().size(); i++) {
				Float share = ((MyTextBox)iTable.getWidget(row, 2 + 2 * i)).getShare();
				if (share == null) continue;
				Integer lastLike = ((MyLabel)iTable.getWidget(row, 3 + 2 * i)).iLastLike;
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
					gr.setColor(g.getColor());
					groups.put(g.getName(), gr);
				}
				cr.addGroup(gr);
			}
			c.addCourse(cr);
		}
		return ret;
	}
	
	public void addBlankLine() {
		final int row = iTable.getRowCount();
		HorizontalPanel hp = new HorizontalPanel();
		iTable.setWidget(row, 0, hp);

		CurriculaCourseSelectionBox cx = new CurriculaCourseSelectionBox(null, iClassifications.getClassifications());
		cx.setWidth("100px");
		cx.addCourseSelectionChangeHandler(new CurriculaCourseSelectionBox.CourseSelectionChangeHandler() {
			@Override
			public void onChange(String course, boolean valid) {
				if (row + 1 == iTable.getRowCount() && valid && !course.isEmpty())
					addBlankLine();
				if (valid) {
					CourseChangedEvent e = new CourseChangedEvent(course);
					for (CourseChangedHandler h: iCourseChangedHandlers)
						h.courseChanged(e);
				} else if (course.isEmpty()) {
					for (int col = 0; col < iClassifications.getClassifications().size(); col++) {
						setEnrollmentAndLastLike(course, col, null, null);
					}
				}
			}
		});
		cx.addCourseSelectionChangeHandler(iCourseChangedHandler);
		iTable.setWidget(row, 1, cx);
		
		int col = 0;
		for (final AcademicClassificationInterface clasf: iClassifications.getClassifications()) {
			MyTextBox ex = new MyTextBox(col, null);
			iTable.setWidget(row, 2 + 2 * col, ex);
			MyLabel note = new MyLabel(col, null, null);
			iTable.setWidget(row, 3 + 2 * col, note);
			iTable.getFlexCellFormatter().setHorizontalAlignment(row, 3 + 2 * col, HasHorizontalAlignment.ALIGN_RIGHT);
			if (iClassifications.getExpected(col) == null) {
				iTable.getFlexCellFormatter().setVisible(row, 2 + 2 * col, false);
				iTable.getFlexCellFormatter().setVisible(row, 3 + 2 * col, false);
			}
			col++;
		}
	}
	
	public void addCourse(String course) {
		GWT.log("Adding " + course);
		addBlankLine();
		for (int row = 1; row < iTable.getRowCount(); row++) {
			CurriculaCourseSelectionBox c = (CurriculaCourseSelectionBox)iTable.getWidget(row, 1);
			if (c.getCourse().isEmpty()) {
				c.setCourse(course, true);
				return;
			}
		}
	}
	
	public void sort(final int column) {
		Integer[] x = new Integer[iTable.getRowCount() - 1];
		for (int i = 0; i < x.length; i ++) x[i] = i;
		Arrays.sort(x, new Comparator<Integer>() {
			public int compare(Integer a, Integer b) {
				int cmp = compareTwoRows(column, a, b);
				if (cmp != 0) return cmp;
				if (column > 1) {
					int c = column + 2;
					while (c < 2 + 2 * iClassifications.getClassifications().size()) {
						cmp = compareTwoRows(c, a, b);
						if (cmp != 0) return cmp;
						c += 2;
					}
				}
				return compareTwoRows(1, a, b);
			}
		});
		for (int i = 0; i < x.length; i ++) {
			int j = x[i];
			while (j < i) j = x[j];
			swap(i, j);
		}
	}
	
	private int compareTwoRows(int column, int r0, int r1) {
		boolean e1 = ((CurriculaCourseSelectionBox)iTable.getWidget(r0 + 1, 1)).getCourse().isEmpty();
		boolean e2 = ((CurriculaCourseSelectionBox)iTable.getWidget(r1 + 1, 1)).getCourse().isEmpty();
		if (e1 && !e2) return 1;
		if (e2 && !e1) return -1;
		if (column == 0) {
			HorizontalPanel p0 = (HorizontalPanel)iTable.getWidget(r0 + 1, 0);
			HorizontalPanel p1 = (HorizontalPanel)iTable.getWidget(r1 + 1, 0);
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
			return ((CurriculaCourseSelectionBox)iTable.getWidget(r0 + 1, 1)).getCourse().compareTo(((CurriculaCourseSelectionBox)iTable.getWidget(r1 + 1, 1)).getCourse());
		int col = (column - 2) / 2;
		if (column % 2 == 0) {
			Float s0 = ((MyTextBox)iTable.getWidget(r0 + 1, column)).getShare();
			Float s1 = ((MyTextBox)iTable.getWidget(r1 + 1, column)).getShare();
			return - (s0 == null ? new Float(0) : s0).compareTo(s1 == null ? new Float(0) : s1);
		} else {
			MyLabel l0 = ((MyLabel)iTable.getWidget(r0 + 1, column));
			MyLabel l1 = ((MyLabel)iTable.getWidget(r1 + 1, column));
			Integer i0 = (iMode == 1 ? l0.iEnrollment : l0.iLastLike);
			Integer i1 = (iMode == 1 ? l1.iEnrollment : l1.iLastLike);
			return - (i0 == null ? new Integer(0) : i0).compareTo(i1 == null ? new Integer(0) : i1);
		}
	}
	
	private void swap(int r0, int r1) {
		if (r0 == r1) return;
		String s = iTable.getRowFormatter().getStyleName(1 + r0);
		iTable.getRowFormatter().setStyleName(1 + r0, iTable.getRowFormatter().getStyleName(1 + r1));
		iTable.getRowFormatter().setStyleName(1 + r1, s);
		Widget w = iTable.getWidget(1 + r0, 0);
		iTable.setWidget(1 + r0, 0, iTable.getWidget(1 + r1, 0));
		iTable.setWidget(1 + r1, 0, w);
		CurriculaCourseSelectionBox c0 = (CurriculaCourseSelectionBox)iTable.getWidget(1 + r0, 1);
		CurriculaCourseSelectionBox c1 = (CurriculaCourseSelectionBox)iTable.getWidget(1 + r1, 1);
		String course = c0.getCourse();
		c0.setCourse(c1.getCourse(), false);
		c1.setCourse(course, false);
		for (int col = 0; col < iClassifications.getClassifications().size(); col ++) {
			MyTextBox t0 = (MyTextBox)iTable.getWidget(1 + r0, 2 + 2 * col);
			MyTextBox t1 = (MyTextBox)iTable.getWidget(1 + r1, 2 + 2 * col);
			Float share = t0.getShare();
			t0.setShare(t1.getShare());
			t1.setShare(share);
			MyLabel l0 = (MyLabel)iTable.getWidget(1 + r0, 3 + 2 * col);
			MyLabel l1 = (MyLabel)iTable.getWidget(1 + r1, 3 + 2 * col);
			Integer enrl = l0.iEnrollment;
			l0.iEnrollment = l1.iEnrollment;
			l1.iEnrollment = enrl;
			Integer last = l0.iLastLike;
			l0.iLastLike = l1.iLastLike;
			l1.iLastLike = last;
			l0.update(); l1.update();
		}
	}

	public int getCourseIndex(String course) {
		for (int row = 1; row < iTable.getRowCount(); row++) {
			String c = ((CurriculaCourseSelectionBox)iTable.getWidget(row, 1)).getCourse();
			if (course.equals(c)) return row - 1;
		}
		return -1;
	}
	
	public boolean setEnrollmentAndLastLike(String course, int clasf, Integer enrollment, Integer lastLike) {
		boolean changed = false;
		for (int row = 1; row < iTable.getRowCount(); row++) {
			String c = ((CurriculaCourseSelectionBox)iTable.getWidget(row, 1)).getCourse();
			if (!course.equals(c)) continue;
			MyLabel note = ((MyLabel)iTable.getWidget(row, 3 + 2 * clasf));
			note.iEnrollment = enrollment;
			note.iLastLike = lastLike;
			note.update();
			changed = true;
		}
		return changed;
	}
	
	public void updateEnrollmentsAndLastLike(HashMap<String, Integer[][]> courses) {
		rows: for (int row = 1; row < iTable.getRowCount() - 1; ) {
			for (int col = 0; col < iClassifications.getClassifications().size(); col ++) {
				MyTextBox text = (MyTextBox)iTable.getWidget(row, 2 + 2 * col);
				if (!text.getText().isEmpty()) {
					row ++;
					continue rows;
				}
			}
			GWT.log("Remove " + ((CurriculaCourseSelectionBox)iTable.getWidget(row, 1)).getCourse() + " @" + row);
			iTable.removeRow(row);
		}
		HashSet<String> updated = new HashSet<String>();
		for (int row = 1; row < iTable.getRowCount(); row++) {
			String c = ((CurriculaCourseSelectionBox)iTable.getWidget(row, 1)).getCourse();
			if (c.isEmpty()) continue;
			updated.add(c);
			Integer cc[][] = courses.get(c);
			for (int col = 0; col < iClassifications.getClassifications().size(); col++) {
				MyLabel note = ((MyLabel)iTable.getWidget(row, 3 + 2 * col));
				note.iEnrollment = (cc == null || cc[col] == null ? null : cc[col][0]);
				note.iLastLike = (cc == null || cc[col] == null ? null : cc[col][1]);
				note.update();
			}
		}
		Integer[][] total = courses.get("");
		if (total == null) return;
		int totalEnrollment = 0, totalLastLike = 0;
		for (int i = 0; i < total.length; i++) {
			if (total[i] != null && total[i][0] != null) totalEnrollment += total[i][0];
			if (total[i] != null && total[i][1] != null) totalLastLike += total[i][1];
		}
		TreeSet<Map.Entry<String, Integer[][]>> include = new TreeSet<Map.Entry<String,Integer[][]>>(new Comparator<Map.Entry<String,Integer[][]>>() {
			private int highestClassification(Integer[][] a) {
				int best = a.length;
				int bestVal = -1;
				for (int i = 0; i < a.length; i++) {
					if (a[i] == null) continue;
					if (a[i][0] != null && a[i][0] > bestVal) {
						bestVal = a[i][0]; best = i;
					}
					if (a[i][1] != null && a[i][1] > bestVal) {
						bestVal = a[i][1]; best = i;
					}
				}
				return best;
			}
			private int firstClassification(Integer[][] a) {
				for (int i = 0; i < a.length; i++) {
					if (a[i] == null) continue;
					if (a[i][0] != null && a[i][0] > 0) return i;
					if (a[i][1] != null && a[i][1] > 0) return i;
				}
				return a.length;
			}
			public int compare(Map.Entry<String,Integer[][]> c0, Map.Entry<String,Integer[][]> c1) {
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
					int v0 = (c0.getValue()[b0][0] == null ? 0 : c0.getValue()[b0][0]);
					int v1 = (c1.getValue()[b0][0] == null ? 0 : c1.getValue()[b0][0]);
					int w0 = (c0.getValue()[b0][1] == null ? 0 : c0.getValue()[b0][1]);
					int w1 = (c1.getValue()[b0][1] == null ? 0 : c1.getValue()[b0][1]);
					if (v0 > v1 || w0 > w1) return -1;
					if (v0 < v1 || w0 < w1) return 1;
					b0++;
				}
				return c0.getKey().compareTo(c1.getKey());
			}
		});
		for (Map.Entry<String, Integer[][]> course: courses.entrySet()) {
			if (updated.contains(course.getKey()) || course.getKey().isEmpty()) continue;
			Integer cc[][] = course.getValue();
			int enrollment = 0, lastLike = 0;
			for (int i = 0; i < cc.length; i++) {
				if (cc[i] != null && cc[i][0] != null) enrollment += cc[i][0];
				if (cc[i] != null && cc[i][1] != null) lastLike += cc[i][1];
			}
			if ((totalEnrollment > 0 && 100.0f * enrollment / totalEnrollment > 3.0f) ||
				(totalLastLike > 0 && 100.0f * lastLike / totalLastLike > 3.0f)) {
				include.add(course);
			}
		}
		for (Map.Entry<String, Integer[][]> course: include) {
			Integer cc[][] = course.getValue();
			int row = iTable.getRowCount() - 1;
			GWT.log("Adding " + course.getKey() + " @" + row);
			addBlankLine();
			CurriculaCourseSelectionBox c = (CurriculaCourseSelectionBox)iTable.getWidget(row, 1);
			c.setCourse(course.getKey(), false);
			for (int col = 0; col < iClassifications.getClassifications().size(); col++) {
				MyLabel note = ((MyLabel)iTable.getWidget(row, 3 + 2 * col));
				note.iEnrollment = (cc == null || cc[col] == null ? null : cc[col][0]);
				note.iLastLike = (cc == null || cc[col] == null ? null : cc[col][1]);
				note.update();
			}
		}
	}
	
	public void expectedChanged(int col, int expected) {
		if (!iPercent) {
			for (int row = 1; row < iTable.getRowCount(); row++) {
				((MyTextBox)iTable.getWidget(row, 2 + 2 * col)).update();
			}
		}
	}
	
	public void setPercent(boolean percent) {
		if (iPercent == percent) return;
		iPercent = percent;
		for (int row = 1; row < iTable.getRowCount(); row++) {
			for (int col = 0; col < iClassifications.getClassifications().size(); col++) {
				((MyTextBox)iTable.getWidget(row, 2 + 2 * col)).update();
				((MyLabel)iTable.getWidget(row, 3 + 2 * col)).update();
			}
		}
	}
	
	public void setMode(int mode) {
		iMode = mode;
		for (int col = 0; col < iClassifications.getClassifications().size(); col++) {
			((HTML)iTable.getWidget(0, 3 + 2 * col)).setHTML(MODE_SHORT[iMode]);
		}
		for (int row = 1; row < iTable.getRowCount(); row++) {
			for (int col = 0; col < iClassifications.getClassifications().size(); col++) {
				((MyLabel)iTable.getWidget(row, 3 + 2 * col)).update();
			}
		}
	}

	public void setVisible(int col, boolean visible) {
		for (int row = 0; row < iTable.getRowCount(); row++) {
			iTable.getFlexCellFormatter().setVisible(row, 2 + 2 * col, visible);
			iTable.getFlexCellFormatter().setVisible(row, 3 + 2 * col, visible);
		}
	}
	
	public class MyLabel extends Label {
		private int iColumn;
		private Integer iEnrollment, iLastLike;
		
		public MyLabel(int column, Integer enrollment, Integer lastLike) {
			super();
			setStyleName("unitime-Label");
			iColumn = column;
			iEnrollment = enrollment;
			iLastLike = lastLike;
			update();
		}
		
		public void update() {
			switch (iMode) {
			case 0: // None
				setText("");
				break;
			case 1: // Enrollment
				if (iEnrollment == null) {
					setText("");
				} else if (iPercent) {
					Integer total = iClassifications.getEnrollment(iColumn);
					setText(total == null ? "N/A" : NF.format(100.0 * iEnrollment / total) + "%");
				} else {
					setText(iEnrollment.toString());
				}
				break;
			case 2: // Last-like
				if (iLastLike == null) {
					setText("");
				} else if (iPercent) {
					Integer total = iClassifications.getLastLike(iColumn);
					setText(total == null ? "N/A" : NF.format(100.0 * iLastLike / total) + "%");
				} else {
					setText(iLastLike.toString());
				}
				break;
			}
		}
	}

	public class MyTextBox extends TextBox {
		private int iColumn;
		private Float iShare = null;
		
		public MyTextBox(int column, Float share) {
			super();
			iColumn = column;
			iShare = share;
			setWidth("60px");
			setStyleName("gwt-SuggestBox");
			setMaxLength(6);
			setTextAlignment(TextBox.ALIGN_RIGHT);
			addChangeHandler(new ChangeHandler() {
				@Override
				public void onChange(ChangeEvent event) {
					try {
						if (getText().isEmpty()) {
							iShare = null;
						} else if (getText().endsWith("%")) {
							iShare = Float.valueOf(getText().substring(0, getText().length() - 1)) / 100.0f;
						} else {
							iShare = Float.valueOf(getText()) / iClassifications.getExpected(iColumn);
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
		
		public Float getShare() {
			return iShare;
		}
		
		public void update() {
			if (iShare == null) 
				setText("");
			else if (iPercent)
				setText(NF.format(100.0 * iShare) + "%");
			else {
				Integer exp = iClassifications.getExpected(iColumn);
				setText(exp == null ? "N/A" : String.valueOf(Math.round(exp * iShare)));	
			}
		}
		
		public void setEnabled(boolean enabled) {
			super.setEnabled(enabled);
			if (enabled) {
				getElement().getStyle().setBorderColor(null);
				getElement().getStyle().setBackgroundColor(null);
			} else {
				getElement().getStyle().setBorderColor("transparent");
				getElement().getStyle().setBackgroundColor("transparent");
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
	
	public static interface CourseChangedHandler {
		public void courseChanged(CourseChangedEvent e);
	}
	
	public void addCourseChangedHandler(CourseChangedHandler h) {
		iCourseChangedHandlers.add(h);
	}
	
	public class Group extends Label implements Comparable<Group> {
		private String iName;
		private int iType;
		private String iColor;
		private boolean iEditable;
		
		public Group(String name, int type, boolean editable) {
			super(name, false);
			iName = name;
			iType = type;
			setStylePrimaryName("unitime-TinyLabel" + (iType == 1 ? "White" : ""));
			iEditable = editable;
			if (iEditable) {
				addClickHandler(iGrHandler);
				addMouseOverHandler(new MouseOverHandler() {
					@Override
					public void onMouseOver(MouseOverEvent event) {
						((Widget)event.getSource()).getElement().getStyle().setCursor(Cursor.POINTER);
					}
				});
			}
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
			Group g = new Group(iName, iType, iEditable);
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
	}
	
	public void assignGroup(String oldName, String name, int type) {
		Group g = null;
		for (Group x: iGroups) {
			if (x.getName().equals(oldName == null ? name : oldName)) { g = x; break; }
		}
		if (g == null) {
			if (name == null || name.isEmpty()) return;
			g = new Group(name, type, true);
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
			if (!isSelected(row)) continue;
			nothing = false;
			HorizontalPanel p = (HorizontalPanel)iTable.getWidget(row, 0);
			Group found = null;
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
						setSelected(row, true);
						select = true;
					}
				}
			}
			if (select) return;
		}
		rows: for (int row = 1; row < iTable.getRowCount(); row++ ) {
			if (!isSelected(row)) continue;
			setSelected(row, false);
			HorizontalPanel p = (HorizontalPanel)iTable.getWidget(row, 0);
			Group found = null;
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
	
	public boolean isSelected(int row) {
		String style = iTable.getRowFormatter().getStyleName(row);
		return "unitime-TableRowSelected".equals(style) || "unitime-TableRowSelectedHover".equals(style);
	}
	
	public void setSelected(int row, boolean selected) {
		String style = iTable.getRowFormatter().getStyleName(row);
		boolean hover = ("unitime-TableRowHover".equals(style) || "unitime-TableRowSelectedHover".equals(style));
		iTable.getRowFormatter().setStyleName(row, "unitime-TableRow" + (selected ? "Selected" : "") + (hover ? "Hover" : ""));
	}
	
	public int getSelectedCount() {
		int selected = 0;
		for (int row = 1; row < iTable.getRowCount(); row ++)
			if (isSelected(row)) selected ++;
		return selected;
	}
	
	public class MyFlexTable extends FlexTable {
		private boolean iEnabled = true;
		
		public MyFlexTable() {
			super();
			sinkEvents(Event.ONMOUSEOVER);
			sinkEvents(Event.ONMOUSEOUT);
			sinkEvents(Event.ONCLICK);
		}
		
		public void setEnabled(boolean enabled) { iEnabled = enabled; }
		public boolean isEnabled() { return iEnabled; }
		
		public void onBrowserEvent(Event event) {
			if (!iEnabled) return;
			Element td = getEventTargetCell(event);
			if (td==null) return;
		    Element tr = DOM.getParent(td);
		    Element body = DOM.getParent(tr);
		    int row = DOM.getChildIndex(body, tr);
		    
		    if (row == 0) return;
			String style = getRowFormatter().getStyleName(row);

		    switch (DOM.eventGetType(event)) {
			case Event.ONMOUSEOVER:
				if ("unitime-TableRowSelected".equals(style))
					getRowFormatter().setStyleName(row, "unitime-TableRowSelectedHover");	
				else
					getRowFormatter().setStyleName(row, "unitime-TableRowHover");	
				break;
			case Event.ONMOUSEOUT:
				if ("unitime-TableRowHover".equals(style))
					getRowFormatter().setStyleName(row, null);	
				else if ("unitime-TableRowSelectedHover".equals(style))
					getRowFormatter().setStyleName(row, "unitime-TableRowSelected");	
				break;
			case Event.ONCLICK:
				Element element = DOM.eventGetTarget(event);
				while (DOM.getElementProperty(element, "tagName").equalsIgnoreCase("div"))
					element = DOM.getParent(element);
				if (DOM.getElementProperty(element, "tagName").equalsIgnoreCase("td")) {
					boolean hover = ("unitime-TableRowHover".equals(style) || "unitime-TableRowSelectedHover".equals(style));
					boolean selected = !("unitime-TableRowSelected".equals(style) || "unitime-TableRowSelectedHover".equals(style));
					getRowFormatter().setStyleName(row, "unitime-TableRow" + (selected ? "Selected" : "") + (hover ? "Hover" : ""));
				}
				break;
			}
		}
	}
}
