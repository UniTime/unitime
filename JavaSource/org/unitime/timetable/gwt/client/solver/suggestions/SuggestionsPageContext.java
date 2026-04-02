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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.unitime.timetable.gwt.client.TimeHint;
import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.rooms.RoomHint;
import org.unitime.timetable.gwt.client.rooms.RoomSharingWidget.FP;
import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.SuggestionsInterface.BtbInstructorInfo;
import org.unitime.timetable.gwt.shared.SuggestionsInterface.ClassAssignmentDetails;
import org.unitime.timetable.gwt.shared.SuggestionsInterface.ClassInfo;
import org.unitime.timetable.gwt.shared.SuggestionsInterface.CurriculumInfo;
import org.unitime.timetable.gwt.shared.SuggestionsInterface.DateInfo;
import org.unitime.timetable.gwt.shared.SuggestionsInterface.DistributionInfo;
import org.unitime.timetable.gwt.shared.SuggestionsInterface.InstructorInfo;
import org.unitime.timetable.gwt.shared.SuggestionsInterface.PreferenceInterface;
import org.unitime.timetable.gwt.shared.SuggestionsInterface.RoomInfo;
import org.unitime.timetable.gwt.shared.SuggestionsInterface.SelectedAssignment;
import org.unitime.timetable.gwt.shared.SuggestionsInterface.StudentConflictInfo;
import org.unitime.timetable.gwt.shared.SuggestionsInterface.SuggestionProperties;
import org.unitime.timetable.gwt.shared.SuggestionsInterface.TimeInfo;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.TextDecoration;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.FlexTable;

/**
 * @author Tomas Muller
 */
public abstract class SuggestionsPageContext {
	protected static final GwtConstants CONSTANTS = GWT.create(GwtConstants.class);
	protected static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	protected static NumberFormat sDF = NumberFormat.getFormat("0.###");
	private SuggestionProperties iProperties;
	
	public SuggestionProperties getProperties() { return iProperties; }
	public void setSuggestionProperties(SuggestionProperties properties) { iProperties = properties; }

	public abstract void select(ClassInfo clazz);
	
	public abstract void remove(ClassInfo clazz);
	
	public abstract void onSelection(Command undo);
	
	public abstract void assign(List<SelectedAssignment> assignment, UniTimeHeaderPanel panel);
	
	public DateLabel createDateLabel(DateInfo date, boolean showStyle) {
		return new DateLabel(date, showStyle);
	}
	
	protected class DateLabel extends P {
		public DateLabel(DateInfo date, boolean showStyle) {
			super("date");
			setText(date.getDatePatternName());
			PreferenceInterface pref = iProperties.getPreference(date.getDatePatternPreference());
			if (pref.hasStyle() && showStyle)
				addStyleName(pref.getStyle());
			else if (date.getDatePatternPreference() != 0)
				getElement().getStyle().setColor(pref.getColor());
		}
	}
	
	public TimeLabel createTimeLabel(TimeInfo time, Long classId, boolean endTime, boolean showStyle) {
		return new TimeLabel(time, classId, endTime, showStyle);
	}
	
	protected class TimeLabel extends P {
		public TimeLabel(TimeInfo time, Long classId, boolean endTime, boolean showStyle) {
			super("time");
			setText(time.getName(iProperties.getFirstDay(), endTime, CONSTANTS));
			PreferenceInterface pref = iProperties.getPreference(time.getPref());
			if (pref.hasStyle() && showStyle)
				addStyleName(pref.getStyle());
			else if (time.getPref() != 0)
				getElement().getStyle().setColor(pref.getColor());
			if (time.isStriked())
				getElement().getStyle().setTextDecoration(TextDecoration.LINE_THROUGH);
			final String timeHint = classId + "," + time.getDays() + "," + time.getStartSlot();
			addMouseOverHandler(new MouseOverHandler() {
				@Override
				public void onMouseOver(MouseOverEvent e) {
					TimeHint.showHint(TimeLabel.this.getElement(), timeHint);
				}
			});
			addMouseOutHandler(new MouseOutHandler() {
				@Override
				public void onMouseOut(MouseOutEvent e) {
					TimeHint.hideHint();
				}
			});
		}
	}
	
	public RoomsLabel createRoomsLabel(List<RoomInfo> rooms, boolean showStyle) {
		return new RoomsLabel(rooms, showStyle);
	}
	
	protected class RoomsLabel extends P {
		public RoomsLabel(List<RoomInfo> rooms, boolean showStyle) {
			super("rooms");
			for (Iterator<RoomInfo> i = rooms.iterator(); i.hasNext(); ) {
				add(withSeparator(new RoomLabel(i.next(), showStyle), i.hasNext()));
			}
		}
	}
	
	public AssignmentLabel createAssignmentLabel(TimeInfo time, List<RoomInfo> rooms, Long classId, boolean showDate, boolean showStyle) {
		return new AssignmentLabel(time, rooms, classId, showDate, showStyle);
	}
	
	protected class AssignmentLabel extends P {
		public AssignmentLabel(TimeInfo time, List<RoomInfo> rooms, Long classId, boolean showDate, boolean showStyle) {
			super("assignment");
			if (showDate && time.hasDatePattern())
				add(new DateLabel(time.getDatePattern(), showStyle));
			add(new TimeLabel(time, classId, true, showStyle));
			if (rooms != null)
				for (Iterator<RoomInfo> i = rooms.iterator(); i.hasNext(); ) {
					add(withSeparator(new RoomLabel(i.next(), showStyle), i.hasNext()));
				}
		}
	}
	
	protected class RoomLabel extends P {
		public RoomLabel(final RoomInfo room, boolean showStyle) {
			super("room");
			setText(room.getName());
			PreferenceInterface pref = iProperties.getPreference(room.getPref());
			if (pref.hasStyle() && showStyle)
				addStyleName(pref.getStyle());
			else if (room.getPref() != 0)
				getElement().getStyle().setColor(pref.getColor());
			if (room.isStriked())
				getElement().getStyle().setTextDecoration(TextDecoration.LINE_THROUGH);
			if (room.getId() != null) {
				addMouseOverHandler(new MouseOverHandler() {
					@Override
					public void onMouseOver(MouseOverEvent e) {
						RoomHint.showHint(RoomLabel.this.getElement(), room.getId(), null, null, true);
					}
				});
				addMouseOutHandler(new MouseOutHandler() {
					@Override
					public void onMouseOut(MouseOutEvent e) {
						RoomHint.hideHint();
					}
				});
			}
		}
	}
	
	public InstructorsLabel createInstructorsLabel(List<InstructorInfo> instructors) {
		return new InstructorsLabel(instructors);
	}
	
	protected class InstructorsLabel extends P {
		public InstructorsLabel(List<InstructorInfo> instructors) {
			super("instructors");
			for (Iterator<InstructorInfo> i = instructors.iterator(); i.hasNext(); ) {
				final InstructorInfo instructor = i.next();
				final P r = new FP("instructor");
				r.setText(instructor.getName() + (i.hasNext() ? CONSTANTS.itemSeparator() : ""));
				if (instructor.getId() != null)
					r.addClickHandler(new ClickHandler() {
						@Override
						public void onClick(ClickEvent e) {
							ToolBox.open("instructorDetail.action?instructorId=" + instructor.getId());
						}
					});
				add(r);
			}
		}
	}
	
	public StudentConflicts createStudentConflicts(List<StudentConflictInfo> conflicts) {
		return new StudentConflicts(conflicts);
	}
	
	protected class StudentConflicts extends P {
		public StudentConflicts(List<StudentConflictInfo> conflicts) {
			super("conflicts");
			Collections.sort(conflicts);
			for (StudentConflictInfo conflict: conflicts) {
				add(new StudentConflict(conflict));
			}
		}
	}
	
	protected static String getCurriculumText(Set<CurriculumInfo> curricula) {
		int top = 0;
		double total = 0.0;
		for (CurriculumInfo i: curricula) {
			total += i.getNrStudents();
		}
		String ret = "";
		for (CurriculumInfo i: curricula) {
			double fraction = i.getNrStudents() / total;
			if (top < 3) {
				top++;
				if (fraction < 0.01) break;
				if (!ret.isEmpty()) ret += CONSTANTS.itemSeparator();
				ret += ((int)Math.round(100.0 * fraction)) + "% " + i.getName();
				if (fraction == 1.0) return i.getName();
			} else {
				ret += CONSTANTS.itemMore();
				break;
			}
		}
		return ret;			
	}

	protected class StudentConflict extends FlexTable {
		public StudentConflict(StudentConflictInfo conflict) {
			addStyleName("conflict");
			int col = 0;
			setHTML(0, col++, conflict.getInfo().getJenrl() + "&times;");
			getFlexCellFormatter().setStyleName(0, col - 1, "counter");
			P clazz = new P();
	        if (conflict.getInfo().isCommited())
	        	conflict.getOther().getClazz().setPref("R");
	        clazz.add(new ClassLabel(conflict.getOther().getClazz()));
			if (conflict.getOther().getTime() != null)
				clazz.add(new TimeLabel(conflict.getOther().getTime(), conflict.getOther().getClazz().getClassId(), true, false));
			if (conflict.getOther().getRoom() != null)
				for (Iterator<RoomInfo> i = conflict.getOther().getRoom().iterator(); i.hasNext(); ) {
					clazz.add(withSeparator(new RoomLabel(i.next(), false), i.hasNext()));
				}
			setWidget(0, col++, clazz);
			getFlexCellFormatter().setStyleName(0, col - 1, "class-assignment");
			if (conflict.getAnother() != null) {
				P another = new P();
				another.add(new ClassLabel(conflict.getAnother().getClazz()));
				if (conflict.getAnother().getTime() != null)
					another.add(new TimeLabel(conflict.getAnother().getTime(), conflict.getAnother().getClazz().getClassId(), true, false));
				if (conflict.getAnother().getRoom() != null)
					for (Iterator<RoomInfo> i = conflict.getAnother().getRoom().iterator(); i.hasNext(); ) {
						another.add(withSeparator(new RoomLabel(i.next(), false), i.hasNext()));
					}
				setWidget(1, 0, another);
				getFlexCellFormatter().setStyleName(1, 0, "class-assignment");
			}
			List<String> props = new ArrayList<String>();
			if (conflict.getInfo().isCommited()) props.add(MESSAGES.studentConflictCommitted());
	        if (conflict.getInfo().isFixed()) props.add(MESSAGES.studentConflictFixed());
	        else if (conflict.getInfo().isHard()) props.add(MESSAGES.studentConflictHard());
	        if (conflict.getInfo().isDistance()) props.add(MESSAGES.studentConflictDistance());
	        if (conflict.getInfo().isImportant()) props.add(MESSAGES.studentConflictImportant());
	        if (conflict.getInfo().isWorkDay()) props.add(MESSAGES.studentConflictWorkday());
	        if (conflict.getInfo().isInstructor()) props.add(MESSAGES.studentConflictInstructor());
	        if (!props.isEmpty()) {
	        	String text = "";
	        	for (Iterator<String> i = props.iterator(); i.hasNext();)
	        		text += i.next() + (i.hasNext() ? CONSTANTS.itemSeparator() : "");
	        	setText(0, col++, "[" + text + "]");
	        	getFlexCellFormatter().setStyleName(0, col - 1, "properties");
	        }
	        if (conflict.getInfo().hasCurricula()) {
	        	setText(0, col++, getCurriculumText(conflict.getInfo().getCurricula()));
	        	getFlexCellFormatter().setStyleName(0, col - 1, "curricula");
	        }
	        if (conflict.getAnother() != null) {
	        	for (int i = 0; i < col; i++) {
	        		if (i != 1)
	        			getFlexCellFormatter().setRowSpan(0, i, 2);
	        	}
	        }
		}
	}
	
	protected class ClassLabel extends FP {
		public ClassLabel(final ClassInfo clazz) {
			super("class");
			setText(clazz.getName());
			if (clazz.getPref() != null)
				getElement().getStyle().setColor(iProperties.getPreference(clazz.getPref()).getColor());
			addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent e) {
					select(clazz);
				}
			});
		}
	}
	
	public ViolatedConstraints createViolatedConstraints(List<DistributionInfo> conflicts, List<BtbInstructorInfo> btbConflicts, boolean showStyle) {
		return new ViolatedConstraints(conflicts, btbConflicts, showStyle);
	}
	
	protected class ViolatedConstraints extends P {
		public ViolatedConstraints(List<DistributionInfo> conflicts, List<BtbInstructorInfo> btbConflicts, boolean showStyle) {
			super("constraints");
			if (conflicts != null)
				for (DistributionInfo conflict: conflicts) {
					if (!conflict.getInfo().isSatisfied())
						add(new ViolatedConstraint(conflict, showStyle));
				}
			if (btbConflicts != null)
				for (BtbInstructorInfo conflict: btbConflicts) 
					add(new ViolatedConstraint(conflict, showStyle));
		}
	}
	
	protected class ViolatedConstraint extends P {
		public ViolatedConstraint(DistributionInfo conflict, boolean showStyle) {
			super("constraint");
			P h = new P("header");
			P pref = new P("preference");
			PreferenceInterface preference = iProperties.getPreference(conflict.getInfo().getPreference());
			pref.setText(preference.getName());
			if (preference.hasStyle() && showStyle)
				pref.addStyleName(preference.getStyle());
			else
				pref.getElement().getStyle().setColor(preference.getColor());
			h.add(pref);
			P name = new P("name");
			name.setText(conflict.getInfo().getName());
			h.add(name);
			add(h);
			for (ClassAssignmentDetails other: conflict.getOtherClasses()) {
				P p = new P("other");
				p.add(new ClassLabel(other.getClazz()));
				if (other.getTime() != null)
					p.add(new TimeLabel(other.getTime(), other.getClazz().getClassId(), true, showStyle));
				if (other.getRoom() != null)
					for (Iterator<RoomInfo> i = other.getRoom().iterator(); i.hasNext(); ) {
						p.add(withSeparator(new RoomLabel(i.next(), showStyle), i.hasNext()));
					}
				add(p);
			}
		}
		
		public ViolatedConstraint(BtbInstructorInfo conflict, boolean showStyle) {
			super("constraint");
			P h = new P("header");
			P pref = new P("preference");
			PreferenceInterface preference = iProperties.getPreference(conflict.getPreference());
			pref.setText(preference.getName());
			if (preference.hasStyle() && showStyle)
				pref.addStyleName(preference.getStyle());
			else
				pref.getElement().getStyle().setColor(preference.getColor());
			h.add(pref);
			P name = new P("name");
			name.setText(MESSAGES.btbInstructorConflictConstraint());
			h.add(name);
			add(h);
			ClassAssignmentDetails other = conflict.getOther();
			P p = new P("other");
			p.add(new ClassLabel(other.getClazz()));
			if (other.getTime() != null)
				p.add(new TimeLabel(other.getTime(), other.getClazz().getClassId(), true, showStyle));
			if (other.getRoom() != null)
				for (Iterator<RoomInfo> i = other.getRoom().iterator(); i.hasNext(); ) {
					p.add(withSeparator(new RoomLabel(i.next(), showStyle), i.hasNext()));
				}
			add(p);
		}
	}
	
	public DateLocations createDateLocations(List<TimeInfo> times) {
		return new DateLocations(times);
	}
	
	protected class DateLocations extends P {
		Map<Long, DateLocation> iDates = new HashMap<Long, DateLocation>();
		DateLocation iSelectedDate = null;
		
		public DateLocations(List<TimeInfo> times) {
			super("dates", "selection");
			Set<DateInfo> dates = new TreeSet<DateInfo>();
			for (TimeInfo time: times) {
				if (time.hasDatePattern()) dates.add(time.getDatePattern());
			}
			DateLocation prev = null;
			for (Iterator<DateInfo> i = dates.iterator(); i.hasNext(); ) {
				DateInfo date = i.next();
				DateLocation location = new DateLocation(date);
				if (prev != null) { prev.setNext(location); location.setPrevious(prev); }
				prev = location;
				iDates.put(date.getDatePatternId(), location);
				add(withSeparator(location, i.hasNext()));
			}
		}
	
		protected class DateLocation extends FP {
			private DateInfo iDate;
			public DateLocation(DateInfo date) {
				super("date", "item");
				iDate = date;
				setText(date.getDatePatternName());
				PreferenceInterface pref = iProperties.getPreference(date.getDatePatternPreference());
				if (pref.hasStyle())
					addStyleName(pref.getStyle());
				else if (date.getDatePatternPreference() != 0)
					getElement().getStyle().setColor(pref.getColor());
				addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent e) {
						invert(DateLocation.this, true);
					}
				});
			}
		}
		
		protected void invert(final DateLocation date, boolean fireUpdate) {
			if (iSelectedDate != null) {
				iSelectedDate.removeStyleName("selected");
				if (iSelectedDate.equals(date)) {
					iSelectedDate = null;
					return;
				}
			}
			iSelectedDate = date;
			if (iSelectedDate != null) iSelectedDate.addStyleName("selected");
			if (fireUpdate) onSelection(new Command() {
				@Override
				public void execute() {
					invert(date, false);
				}
			});
		}
		
		protected void select(final DateLocation date, boolean fireUpdate) {
			if (iSelectedDate != null)
				iSelectedDate.removeStyleName("selected");
			iSelectedDate = date;
			if (iSelectedDate != null) iSelectedDate.addStyleName("selected");
			if (fireUpdate) onSelection(new Command() {
				@Override
				public void execute() {
					invert(date, false);
				}
			});
		}
		
		protected void select(DateInfo date, boolean fireUpdate) {
			select(date == null ? (DateLocation) null : iDates.get(date.getDatePatternId()), fireUpdate);
		}
		
		protected DateInfo getSelectedDate() { return (iSelectedDate == null ? null : iSelectedDate.iDate); }
	}
	
	protected TimeLocations createTimeLocations(Long classId, List<TimeInfo> times) {
		return new TimeLocations(classId, times);
	}
	
	protected class TimeLocations extends P {
		Map<String, TimeLocation> iTimes = new HashMap<String, TimeLocation>();
		TimeLocation iSelectedTime = null;
		FP iMore = null;
		
		public TimeLocations(Long classId, List<TimeInfo> times) {
			super("times", "selection");
			Set<TimeInfo> t = new TreeSet<TimeInfo>(new Comparator<TimeInfo>() {
				@Override
				public int compare(TimeInfo t1, TimeInfo t2) {
					if (t1.isStriked() && !t2.isStriked()) return 1;
					if (!t1.isStriked() && t2.isStriked()) return -1;
					int cmp = Double.compare(t1.getDaysOrder(iProperties.getFirstDay()),t2.getDaysOrder(iProperties.getFirstDay()));
					if (cmp!=0) return cmp;
					cmp = Double.compare(t1.getStartSlot(),t2.getStartSlot());
					if (cmp!=0) return cmp;
					cmp = Double.compare(t1.getMin(),t2.getMin());
					return cmp;
				}
			});
			t.addAll(times);
			FP prev = null;
			for (Iterator<TimeInfo> i = t.iterator(); i.hasNext(); ) {
				TimeInfo time = i.next();
				if (time.isStriked() && iMore == null) {
					iMore = new FP("more");
					iMore.setText(CONSTANTS.selectionMore());
					iMore.addClickHandler(new ClickHandler() {
						@Override
						public void onClick(ClickEvent e) {
							P panel = TimeLocations.this;
							for (int i = panel.getWidgetIndex(iMore) + 1; i < panel.getWidgetCount(); i++)
								panel.getWidget(i).setVisible(true);
							iMore.setVisible(false);
						}
					});
					add(iMore);
					if (prev != null) { prev.setNext(iMore); iMore.setPrevious(prev); }
					prev = iMore;
				}
				TimeLocation location = new TimeLocation(classId, time);
				if (prev != null) { prev.setNext(location); location.setPrevious(prev); }
				prev = location;
				iTimes.put(code(time), location);
				P p = withSeparator(location, i.hasNext());
				if (time.isStriked()) p.setVisible(false);
				add(p);
			}
		}
		
		protected class TimeLocation extends FP {
			private TimeInfo iTime;
			public TimeLocation(Long classId, TimeInfo time) {
				super("time", "item");
				iTime = time;
				setText(time.getName(iProperties.getFirstDay(), false, CONSTANTS));
				PreferenceInterface pref = iProperties.getPreference(time.getPref());
				if (pref.hasStyle())
					addStyleName(pref.getStyle());
				else if (time.getPref() != 0)
					getElement().getStyle().setColor(pref.getColor());
				if (time.isStriked())
					getElement().getStyle().setTextDecoration(TextDecoration.LINE_THROUGH);
				final String timeHint = classId + "," + time.getDays() + "," + time.getStartSlot();
				if (!time.isStriked()) {
					addMouseOverHandler(new MouseOverHandler() {
						@Override
						public void onMouseOver(MouseOverEvent e) {
							TimeHint.showHint(TimeLocation.this.getElement(), timeHint);
						}
					});
					addMouseOutHandler(new MouseOutHandler() {
						@Override
						public void onMouseOut(MouseOutEvent e) {
							TimeHint.hideHint();
						}
					});	
				}
				addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent e) {
						invert(TimeLocation.this, true);
					}
				});
			}
		}
		
		protected String code(TimeInfo time) {
			return time.getPatternId() + "," + time.getDays() + "," + time.getStartSlot();
		}
		
		protected void invert(final TimeLocation time, boolean fireUpdate) {
			if (iSelectedTime != null) {
				iSelectedTime.removeStyleName("selected");
				if (iSelectedTime.equals(time)) {
					iSelectedTime = null;
					return;
				}
			}
			iSelectedTime = time;
			if (iSelectedTime != null) iSelectedTime.addStyleName("selected");
			if (fireUpdate) onSelection(new Command() {
				@Override
				public void execute() {
					invert(time, false);
				}
			});
		}
		
		protected void select(final TimeLocation time, boolean fireUpdate) {
			if (iSelectedTime != null)
				iSelectedTime.removeStyleName("selected");
			iSelectedTime = time;
			if (iSelectedTime != null) iSelectedTime.addStyleName("selected");
			if (fireUpdate) onSelection(new Command() {
				@Override
				public void execute() {
					invert(time, false);
				}
			});
		}
		
		protected void select(TimeInfo time, boolean fireUpdate) {
			select(time == null ? (TimeLocation) null : iTimes.get(code(time)), fireUpdate);
		}
		
		public TimeInfo getSelectedTime() {
			return iSelectedTime == null ? null : iSelectedTime.iTime;
		}
	}
	
	public RoomLocations createRoomLocations(int nrRooms, List<RoomInfo> rooms) {
		return new RoomLocations(nrRooms, rooms);
	}
	
	protected class RoomLocations extends P {
		Map<Long, RoomLocation> iRooms = new HashMap<Long, RoomLocation>();
		int iNrRooms = 0;
		List<RoomLocation> iSelectedRooms = new ArrayList<RoomLocation>();
		FP iMore = null;
		
		public RoomLocations(int nrRooms, List<RoomInfo> rooms) {
			super("rooms", "selection");
			iNrRooms = nrRooms;
			Collections.sort(rooms);
			FP prev = null;
			for (Iterator<RoomInfo> i = rooms.iterator(); i.hasNext(); ) {
				RoomInfo room = i.next();
				if (room.isStriked() && iMore == null) {
					iMore = new FP("more");
					iMore.setText(CONSTANTS.selectionMore());
					iMore.addClickHandler(new ClickHandler() {
						@Override
						public void onClick(ClickEvent e) {
							P panel = RoomLocations.this;
							for (int i = panel.getWidgetIndex(iMore) + 1; i < panel.getWidgetCount(); i++)
								panel.getWidget(i).setVisible(true);
							iMore.setVisible(false);
						}
					});
					add(iMore);
					if (prev != null) { prev.setNext(iMore); iMore.setPrevious(prev); }
					prev = iMore;
				}
				RoomLocation location = new RoomLocation(room);
				if (prev != null) { prev.setNext(location); location.setPrevious(prev); }
				prev = location;
				iRooms.put(room.getId(), location);
				P p = withSeparator(location, i.hasNext());
				if (room.isStriked()) p.setVisible(false);
				add(p);
			}
		}
		
		protected class RoomLocation extends FP {
			private RoomInfo iRoom;
			public RoomLocation(final RoomInfo room) {
				super("room", "item");
				iRoom = room;
				setText(room.getName());
				PreferenceInterface pref = iProperties.getPreference(room.getPref());
				if (pref.hasStyle())
					addStyleName(pref.getStyle());
				else if (room.getPref() != 0)
					getElement().getStyle().setColor(pref.getColor());
				if (room.isStriked())
					getElement().getStyle().setTextDecoration(TextDecoration.LINE_THROUGH);
				addMouseOverHandler(new MouseOverHandler() {
					@Override
					public void onMouseOver(MouseOverEvent e) {
						RoomHint.showHint(RoomLocation.this.getElement(), room.getId(), null, null, true);
					}
				});
				addMouseOutHandler(new MouseOutHandler() {
					@Override
					public void onMouseOut(MouseOutEvent e) {
						RoomHint.hideHint();
					}
				});
				addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent e) {
						invert(RoomLocation.this, true);
					}
				});
			}
		}
		
		protected void invert(final RoomLocation room, boolean fireUpdate) {
			for (int i = 0; i < iSelectedRooms.size(); i++)
				if (iSelectedRooms.get(i).equals(room)) {
					iSelectedRooms.get(i).removeStyleName("selected");
					iSelectedRooms.remove(i);
					return;
				}
			if (iSelectedRooms.size() >= iNrRooms) {
				iSelectedRooms.get(iNrRooms - 1).removeStyleName("selected");
				iSelectedRooms.remove(iNrRooms - 1);
			}
			if (room != null) {
				iSelectedRooms.add(0, room);
				room.addStyleName("selected");
			}
			if (fireUpdate) onSelection(new Command() {
				@Override
				public void execute() {
					invert(room, false);
				}
			});
		}
		
		protected void select(final RoomLocation room, boolean fireUpdate) {
			for (int i = 0; i < iSelectedRooms.size(); i++)
				if (iSelectedRooms.get(i).equals(room)) {
					iSelectedRooms.get(i).removeStyleName("selected");
					iSelectedRooms.remove(i);
				}
			if (iSelectedRooms.size() >= iNrRooms) {
				iSelectedRooms.get(iNrRooms - 1).removeStyleName("selected");
				iSelectedRooms.remove(iNrRooms - 1);
			}
			if (room != null) {
				iSelectedRooms.add(0, room);
				room.addStyleName("selected");
			}
			if (fireUpdate) onSelection(new Command() {
				@Override
				public void execute() {
					invert(room, false);
				}
			});
		}
		
		protected void select(RoomInfo room, boolean fireUpdate) {
			select(room == null ? null : iRooms.get(room.getId()), fireUpdate);
		}
		
		public int getNrSelectedRooms() {
			return iSelectedRooms.size();
		}
		public List<RoomInfo> getSelectedRooms() {
			List<RoomInfo> rooms = new ArrayList<RoomInfo>();
			for (RoomLocation loc: iSelectedRooms)
				rooms.add(loc.iRoom);
			return rooms;
		}
	}
	
	protected class Separator extends P {
		public Separator() {
			super("separator");
			setText(CONSTANTS.itemSeparator());
		}
	}
	
	protected class Composite extends P {
		public Composite(P... items) {
			super("composite");
			for (P item: items)
				add(item);
		}
	}
	
	protected P withSeparator(P item, boolean hasNext) {
		if (hasNext)
			return new Composite(item, new Separator());
		else
			return new Composite(item);
	}
	
	public String dispNumber(int number) {
		return dispNumber("",number);
	}
	
	public String dispNumber(String prefix, int number) {
		if (number>0) return "<span style='color:#b80000;'>"+prefix+"+"+number+"</span>";
		if (number<0) return "<span style='color:#1d6600;'>"+prefix+number+"</span>";
	    return prefix+"0";
	}
	
	public String dispNumber(int n1, int n2) {
		return dispNumber(n1-n2)+" ("+n2+(n1==n2?"":" &rarr; "+n1)+")";
	}
	
	public String dispNumber(double n1, double n2) {
		return dispNumber(n1-n2)+" ("+sDF.format(n2)+(n1==n2?"":" &rarr; "+sDF.format(n1))+")";
	}
	
	public String dispNumber(double number) {
		return dispNumber("",number);
	}
	
	public String dispNumber(String prefix, double number) {
		if (number>0) return "<span style='color:#b80000;'>"+prefix+"+"+sDF.format(number)+"</span>";
		if (number<0) return "<span style='color:#1d6600;'>"+prefix+sDF.format(number)+"</span>";
	    return prefix+"0";
	}
}
