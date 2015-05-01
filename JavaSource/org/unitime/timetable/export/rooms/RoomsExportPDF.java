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
package org.unitime.timetable.export.rooms;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

import org.springframework.stereotype.Service;
import org.unitime.timetable.export.ExportHelper;
import org.unitime.timetable.export.PDFPrinter;
import org.unitime.timetable.export.PDFPrinter.A;
import org.unitime.timetable.gwt.shared.RoomInterface.DepartmentInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.ExamTypeInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.FeatureInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.GroupInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomDetailInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomFlag;
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.MidtermPeriodPreferenceModel;
import org.unitime.timetable.model.PeriodPreferenceModel;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.dao.LocationDAO;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.Formats;
import org.unitime.timetable.util.Formats.Format;
import org.unitime.timetable.webutil.RequiredTimeTable;

/**
 * @author Tomas Muller
 */
@Service("org.unitime.timetable.export.Exporter:rooms.pdf")
public class RoomsExportPDF extends RoomsExporter {

	@Override
	public String reference() { return "rooms.pdf"; }

	@Override
	protected void print(ExportHelper helper, List<RoomDetailInterface> rooms, String department, int roomCookieFlags, int deptMode, boolean gridAsText, boolean vertical, String mode) throws IOException {
		if (checkRights(helper))
			helper.getSessionContext().hasPermission(Right.RoomsExportPdf);
		
		Printer printer = new PDFPrinter(helper.getOutputStream(), false);
		helper.setup(printer.getContentType(), reference(), false);
		hideColumns(printer, rooms, roomCookieFlags);
		print(printer, rooms, department, deptMode, gridAsText, vertical, mode);
	}
	
	@Override
	protected void hideColumn(Printer out, List<RoomDetailInterface> rooms, RoomFlag flag) {
		switch (flag) {
		case SHOW_TYPE: out.hideColumn(1); break;
		case SHOW_CAPACITY: out.hideColumn(2); break;
		case SHOW_EXAM_CAPACITY: out.hideColumn(3); break;
		case SHOW_AREA: out.hideColumn(4); break;
		case SHOW_COORDINATES: out.hideColumn(5); break;
		case SHOW_IGNORE_DISTANCES: out.hideColumn(6); break;
		case SHOW_IGNORE_ROOM_CHECK: out.hideColumn(7); break;
		case SHOW_PREFERENCE: out.hideColumn(8); break;
		case SHOW_AVAILABILITY: out.hideColumn(9); break;
		case SHOW_DEPARTMENTS: out.hideColumn(10); break;
		case SHOW_CONTROLLING_DEPARTMENT: out.hideColumn(11); break;
		case SHOW_EXAM_TYPES: out.hideColumn(12); break;
		case SHOW_PERIOD_PREFERENCES: out.hideColumn(13); break;
		case SHOW_EVENT_DEPARTMENT: out.hideColumn(14); break;
		case SHOW_EVENT_STATUS: out.hideColumn(15); break;
		case SHOW_EVENT_AVAILABILITY: out.hideColumn(16); break;
		case SHOW_EVENT_MESSAGE: out.hideColumn(17); break;
		case SHOW_BREAK_TIME: out.hideColumn(18); break;
		case SHOW_GROUPS: out.hideColumn(19); break;
		case SHOW_FEATURES: out.hideColumn(20); break;
		}
	}
	
	protected void print(Printer out, List<RoomDetailInterface> rooms, String department, int deptMode, boolean gridAsText, boolean vertical, String mode) throws IOException {
		out.printHeader(
				/*  0 */ MESSAGES.colName().replace("<br>", "\n"),
				/*  1 */ MESSAGES.colType().replace("<br>", "\n"),
				/*  2 */ MESSAGES.colCapacity().replace("<br>", "\n"),
				/*  3 */ MESSAGES.colExaminationCapacity().replace("<br>", "\n"),
				/*  4 */ MESSAGES.colArea().replace("<br>", "\n").replace("&sup2;", "2"),
				/*  5 */ MESSAGES.colCoordinates().replace("<br>", "\n"),
				/*  6 */ MESSAGES.colDistances().replace("<br>", "\n"),
				/*  7 */ MESSAGES.colRoomCheck().replace("<br>", "\n"),
				/*  8 */ MESSAGES.colPreference().replace("<br>", "\n"),
				/*  9 */ MESSAGES.colAvailability().replace("<br>", "\n"),
				/* 10 */ MESSAGES.colDepartments().replace("<br>", "\n"),
				/* 11 */ MESSAGES.colControl().replace("<br>", "\n"),
				/* 12 */ MESSAGES.colExamTypes().replace("<br>", "\n"),
				/* 13 */ MESSAGES.colPeriodPreferences().replace("<br>", "\n"),
				/* 14 */ MESSAGES.colEventDepartment().replace("<br>", "\n"),
				/* 15 */ MESSAGES.colEventStatus().replace("<br>", "\n"),
				/* 16 */ MESSAGES.colEventAvailability().replace("<br>", "\n"),
				/* 17 */ MESSAGES.colEventMessage().replace("<br>", "\n"),
				/* 18 */ MESSAGES.colBreakTime().replace("<br>", "\n"),
				/* 19 */ MESSAGES.colGroups().replace("<br>", "\n"),
				/* 20 */ MESSAGES.colFeatures().replace("<br>", "\n")
				);
		
		Format<Number> af = Formats.getNumberFormat(CONSTANTS.roomAreaFormat());
		Format<Number> cf = Formats.getNumberFormat(CONSTANTS.roomCoordinateFormat());
		
		for (RoomDetailInterface room: rooms) {
			((PDFPrinter)out).printLine(
					name(room, department),
					new A(room.getRoomType().getLabel()),
					new A(room.getCapacity() == null ? "0" : room.getCapacity().toString()).right(),
					new A(room.getExamCapacity() == null ? "" : room.getExamCapacity().toString()).right(),
					new A(room.getArea() == null ? "" : af.format(room.getArea())).right(),
					new A(room.hasCoordinates() ? cf.format(room.getX()) + "," + cf.format(room.getY()) : ""),
					new A(room.isIgnoreRoomCheck() ? MESSAGES.exportFalse() : MESSAGES.exportTrue()),
					new A(room.isIgnoreTooFar() ? MESSAGES.exportFalse() : MESSAGES.exportTrue()),
					pref2a(room.getDepartments(), deptMode),
					availability(room, false, gridAsText, vertical, mode),
					dept2a(room.getDepartments(), room.getControlDepartment(), deptMode),
					new A(dept2string(room.getControlDepartment(), deptMode)).color(room.getControlDepartment() == null ? null : room.getControlDepartment().getColor()),
					examTypes2a(room.getExamTypes()),
					periodPreferences(room, department, gridAsText, vertical),
					new A(dept2string(room.getEventDepartment(), deptMode)).color(room.getEventDepartment() == null ? null : room.getEventDepartment().getColor()),
					room.getEventStatus() != null ? new A(CONSTANTS.eventStatusAbbv()[room.getEventStatus()]) : room.getDefaultEventStatus() != null ? new A(CONSTANTS.eventStatusAbbv()[room.getDefaultEventStatus()]).italic() : new A(),
					availability(room, true, gridAsText, vertical, mode),
					room.getEventNote() != null ? new A(room.getEventNote()) : new A(room.getDefaultEventNote()).italic(),
					room.getBreakTime() != null ? new A(room.getBreakTime().toString()) : room.getDefaultBreakTime() != null ? new A(room.getDefaultBreakTime().toString()).italic() : new A(),
					groups2a(room.getGroups(), deptMode),
					features2a(room.getFeatures(), deptMode)
					);
			out.flush();
		}
		out.close();
	}
	
	protected A availability(RoomDetailInterface room, boolean events, boolean gridAsText, boolean vertical, String mode) {
		A ret = null;
		if (gridAsText) {
			ret = new A(events ? room.getEventAvailability() : room.getAvailability());
		} else {
			Location location = LocationDAO.getInstance().get(room.getUniqueId());
			RequiredTimeTable rtt = (events ? location.getEventAvailabilityTable() : location.getRoomSharingTable());
			rtt.getModel().setDefaultSelection(mode);
			ret = new A(rtt.createBufferedImage(vertical, false));
		}
		if (!events && room.getRoomSharingNote() != null)
			ret.add(new A(room.getRoomSharingNote()));
		//else if (events && (room.hasEventNote() || room.hasDefaultEventNote()))
		//	ret.add(new A(room.hasEventNote() ? room.getEventNote() : room.getDefaultEventNote()));
		return ret;
	}
	
	protected A periodPreferences(RoomDetailInterface room, String department, boolean gridAsText, boolean vertical) {
		Location location = LocationDAO.getInstance().get(room.getUniqueId());
		for (ExamType type: location.getExamTypes())
			if (type.getReference().equals(department)) {
				if (type.getType() == ExamType.sExamTypeMidterm) {
					return new PDFMidtermPeriodPreferenceModel(location, type).toA();
				} else {
					PDFPeriodPreferenceModel px = new PDFPeriodPreferenceModel(location, type);
					if (gridAsText)
						return px.toA();
					else
						return new A(new RequiredTimeTable(px).createBufferedImage(vertical, false));
				}
			}
		return new A();
	}
	
	protected A name(RoomDetailInterface room, String department) {
		A a = new A(room.hasDisplayName() ? MESSAGES.label(room.getLabel(), room.getDisplayName()) : room.getLabel());
		for (DepartmentInterface d: room.getDepartments())
			if (d.getDeptCode().equals(department) && d.getPreference() != null)
				a.setColor(d.getPreference().getColor());
		return a;
	}
	
	protected A pref2a(Collection<DepartmentInterface> departments, int deptMode) {
		if (departments == null || departments.isEmpty()) return new A();
		A a = new A();
		for (DepartmentInterface d: departments) {
			if (d.getPreference() == null) continue;
			a.add(new A(PreferenceLevel.prolog2abbv(d.getPreference().getCode()) + " " + dept2string(d, deptMode)).color(d.getPreference().getColor()));
		}
		return a;
	}
	
	protected A dept2a(Collection<DepartmentInterface> departments, DepartmentInterface control, int deptMode) {
		if (departments == null || departments.isEmpty()) return new A();
		A a = new A();
		for (DepartmentInterface d: departments) {
			A b = new A(dept2string(d, deptMode)).color(d.getColor());
			if (d.equals(control)) b.underline();
			a.add(b);
		}
		return a;
	}
	
	protected A examTypes2a(Collection<ExamTypeInterface> types) {
		if (types == null || types.isEmpty()) return new A();
		A a = new A();
		for (ExamTypeInterface t: types) {
			a.add(new A(t.getLabel()));
		}
		return a;
	}
	
	protected A features2a(Collection<FeatureInterface> features, int deptMode) {
		if (features == null || features.isEmpty()) return new A();
		A a = new A();
		for (FeatureInterface f: features) {
			if (f.getType() != null)
				a.add(new A(f.getLabel() + " (" + f.getType().getAbbreviation() + ")").color(f.getDepartment() == null ? null : f.getDepartment().getColor()));
			else if (f.getDepartment() != null)
				a.add(new A(f.getLabel() + " (" + dept2string(f.getDepartment(), deptMode) + ")").color(f.getDepartment().getColor()));
			else
				a.add(new A(f.getLabel()));
		}
		return a;
	}
	
	protected A groups2a(Collection<GroupInterface> groups, int deptMode) {
		if (groups == null || groups.isEmpty()) return new A();
		A a = new A();
		for (GroupInterface g: groups) {
			a.add(new A(g.getLabel() + (g.getDepartment() == null ? "" : " (" + dept2string(g.getDepartment(), deptMode) + ")")
					).color(g.getDepartment() == null ? null : g.getDepartment().getColor()));
		}
		return a;
	}
	
	static class PDFMidtermPeriodPreferenceModel extends MidtermPeriodPreferenceModel {
		
		PDFMidtermPeriodPreferenceModel(Location location, ExamType type) {
			super(location.getSession(), type);
			load(location);
		}
		
		List<A> toA(int fDate, int lDate, Hashtable<Integer,String> prefs) {
	    	Formats.Format<Date> df = Formats.getDateFormat(Formats.Pattern.DATE_EXAM_PERIOD);
	        String dates = df.format(getDate(fDate))+(fDate==lDate?"":" - "+df.format(getDate(lDate)));
	        String lastPref = null; int fStart = -1, lStart = -1;
	        List<A> ret = new ArrayList<A>();
	        for (int start: iStarts) {
	            String pref = prefs.get(start);
	            if (pref==null) continue;
	            if (lastPref==null) {
	                lastPref = pref; fStart = start;
	            } else if (!pref.equals(lastPref)) {
	                if (iLocation && PreferenceLevel.sNeutral.equals(lastPref)) {
	                    //
	                } else if (!iLocation && PreferenceLevel.sProhibited.equals(lastPref)) {
	                    //
	                } else {
	                    String startTime = Constants.toTime(Constants.SLOT_LENGTH_MIN*fStart+Constants.FIRST_SLOT_TIME_MIN);
	                    String endTime = Constants.toTime(Constants.SLOT_LENGTH_MIN*(lStart+iLength.get(lStart))+Constants.FIRST_SLOT_TIME_MIN);
	                    ret.add(new A(PreferenceLevel.prolog2abbv(lastPref)+" "+dates+" "+(iStarts.size()==2?fStart==iStarts.first()?"Early":"Late":startTime)+(fStart==lStart?"":" - "+endTime)).color(PreferenceLevel.prolog2color(lastPref)));
	                }
	                lastPref = pref; fStart = start;
	            }
	            lStart = start;
	        }
	        if (lastPref!=null) {
	            if (iLocation && PreferenceLevel.sNeutral.equals(lastPref)) {
	                //
	            } else if (!iLocation && PreferenceLevel.sProhibited.equals(lastPref)) {
	                //
	            } else {
	                String startTime = Constants.toTime(Constants.SLOT_LENGTH_MIN*fStart+Constants.FIRST_SLOT_TIME_MIN);
	                String endTime = Constants.toTime(Constants.SLOT_LENGTH_MIN*(lStart+iLength.get(lStart))+Constants.FIRST_SLOT_TIME_MIN);
	                if (fStart==iStarts.first()) {
	                	ret.add(new A(PreferenceLevel.prolog2abbv(lastPref)+" "+dates).color(PreferenceLevel.prolog2color(lastPref)));

	                } else {
	                	ret.add(new A(PreferenceLevel.prolog2abbv(lastPref)+" "+dates+" "+(iStarts.size()==2?fStart==iStarts.first()?"Early":"Late":startTime)+(fStart==lStart?"":" - "+endTime)).color(PreferenceLevel.prolog2color(lastPref)));
	                }
	            }
	        }
	        return ret;
	    }
		
		A toA() {
			if (iStarts.isEmpty()) return new A();
	        A ret = new A();
	        Hashtable<Integer,String> fPref = null; 
	        int fDate = -1, lDate = -1;
	        for (Integer date: iDates) {
	        	Hashtable<Integer,String> pref = iPreferences.get(date);
	        	if (fPref==null) {
	        	    fPref = pref; fDate = date;
	        	} else if (!fPref.equals(pref)) {
	        		for (A b: toA(fDate, lDate, fPref)) ret.add(b);
	        	    fPref = pref; fDate = date;
	        	}
	        	lDate = date;
	        }
	        if (fPref!=null) {
	        	for (A b: toA(fDate, lDate, fPref)) ret.add(b);
	        }
	        return ret;
		}
	}
	
	class PDFPeriodPreferenceModel extends PeriodPreferenceModel {
		PDFPeriodPreferenceModel(Location location, ExamType type) {
			super(location.getSession(), type.getUniqueId());
			load(location);
		}
		
		A toA() {
			A ret = new A();
	        for (int d=0;d<getNrDays();d++) {
	            String pref = null; int a = 0, b = 0;
	            for (int t=0;t<getNrTimes();t++) {
	                String p = getPreference(d,t);
	                if (pref==null || !pref.equals(p)) {
	                    if (pref!=null && !"@".equals(pref) && !PreferenceLevel.sNeutral.equals(pref)) {
	                    	ret.add(new A(PreferenceLevel.prolog2abbv(pref)+" "+getDayHeader(d).replace("<br>", " ")+" "+getStartTime(a)+" - "+getEndTime(b)).color(PreferenceLevel.prolog2color(pref)));
	                    }
	                    pref = p; a = b = t;
	                } else {
	                    b = t;
	                }
	            }
	            if (pref!=null && !"@".equals(pref) && !PreferenceLevel.sNeutral.equals(pref)) {
	            	ret.add(new A(PreferenceLevel.prolog2abbv(pref)+" "+getDayHeader(d).replace("<br>", " ")+" "+getStartTime(a)+" - "+getEndTime(b)).color(PreferenceLevel.prolog2color(pref)));
	            }
	        }
	        return ret;
		}
	}
}
