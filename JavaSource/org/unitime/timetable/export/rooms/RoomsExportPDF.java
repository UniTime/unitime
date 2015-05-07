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

import java.awt.Color;
import java.io.IOException;
import java.net.URL;
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
import org.unitime.timetable.gwt.shared.RoomInterface.FeatureTypeInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.GroupInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomDetailInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomPictureInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomsColumn;
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.MidtermPeriodPreferenceModel;
import org.unitime.timetable.model.PeriodPreferenceModel;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.RoomPicture;
import org.unitime.timetable.model.dao.LocationDAO;
import org.unitime.timetable.model.dao.RoomPictureDAO;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.Formats;
import org.unitime.timetable.webutil.RequiredTimeTable;

import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;

/**
 * @author Tomas Muller
 */
@Service("org.unitime.timetable.export.Exporter:rooms.pdf")
public class RoomsExportPDF extends RoomsExporter {

	@Override
	public String reference() { return "rooms.pdf"; }

	@Override
	protected void print(ExportHelper helper, List<RoomDetailInterface> rooms, ExportContext context) throws IOException {
		if (checkRights(helper))
			helper.getSessionContext().hasPermission(Right.RoomsExportPdf);
		
		List<Integer> columns = new ArrayList<Integer>();
		for (int i = 0; i < getNbrColumns(context); i ++)
			if (isColumnVisible(i, context)) columns.add(i);
		
		PDFPrinter printer = new PDFPrinter(helper.getOutputStream(), false);
		helper.setup(printer.getContentType(), reference(), false);
		
		String[] header = new String[columns.size()];
		for (int i = 0; i < columns.size(); i++)
			header[i] = getColumnName(columns.get(i), context);
		printer.printHeader(header);
		printer.flush();
		
		for (RoomDetailInterface room: rooms) {
			A[] row = new A[columns.size()];
			for (int i = 0; i < columns.size(); i++)
				row[i] = getCell(room, columns.get(i), context);
			printer.printLine(row);
			printer.flush();
		}
		printer.close();
	}
	
	protected int getNbrColumns(ExportContext context) {
		return 23 + context.getRoomFeatureTypes().size();
	}
	
	protected String getColumnName(int column, ExportContext context) {
		switch (column) {
		case  0: return MESSAGES.colName().replace("<br>", "\n");
		case  1: return MESSAGES.colType().replace("<br>", "\n");
		case  2: return MESSAGES.colCapacity().replace("<br>", "\n");
		case  3: return MESSAGES.colExaminationCapacity().replace("<br>", "\n");
		case  4: return MESSAGES.colArea(CONSTANTS.roomAreaUnitsShortPlainText()).replace("<br>", "\n");
		case  5: return MESSAGES.colCoordinates().replace("<br>", "\n");
		case  6: return MESSAGES.colDistances().replace("<br>", "\n");
		case  7: return MESSAGES.colRoomCheck().replace("<br>", "\n");
		case  8: return MESSAGES.colPreference().replace("<br>", "\n");
		case  9: return MESSAGES.colMap().replace("<br>", "\n");
		case 10: return MESSAGES.colPictures().replace("<br>", "\n");
		case 11: return MESSAGES.colAvailability().replace("<br>", "\n");
		case 12: return MESSAGES.colDepartments().replace("<br>", "\n");
		case 13: return MESSAGES.colControl().replace("<br>", "\n");
		case 14: return MESSAGES.colExamTypes().replace("<br>", "\n");
		case 15: return MESSAGES.colPeriodPreferences().replace("<br>", "\n");
		case 16: return MESSAGES.colEventDepartment().replace("<br>", "\n");
		case 17: return MESSAGES.colEventStatus().replace("<br>", "\n");
		case 18: return MESSAGES.colEventAvailability().replace("<br>", "\n");
		case 19: return MESSAGES.colEventMessage().replace("<br>", "\n");
		case 20: return MESSAGES.colBreakTime().replace("<br>", "\n");
		case 21: return MESSAGES.colGroups().replace("<br>", "\n");
		case 22: return MESSAGES.colFeatures().replace("<br>", "\n");
		default: return context.getRoomFeatureTypes().get(column - 23).getAbbreviation();
		}
	}
	
	protected boolean isColumnVisible(int column, ExportContext context) {
		int flags = context.getRoomCookieFlags();
		switch(column) {
		case 1: return RoomsColumn.TYPE.in(flags);
		case 2: return RoomsColumn.CAPACITY.in(flags);
		case 3: return RoomsColumn.EXAM_CAPACITY.in(flags);
		case 4: return RoomsColumn.AREA.in(flags);
		case 5: return RoomsColumn.COORDINATES.in(flags);
		case 6: return RoomsColumn.DISTANCE_CHECK.in(flags);
		case 7: return RoomsColumn.ROOM_CHECK.in(flags);
		case 8: return RoomsColumn.PREFERENCE.in(flags);
		case 9: return RoomsColumn.MAP.in(flags);
		case 10: return RoomsColumn.PICTURES.in(flags);
		case 11: return RoomsColumn.AVAILABILITY.in(flags);
		case 12: return RoomsColumn.DEPARTMENTS.in(flags);
		case 13: return RoomsColumn.CONTROL_DEPT.in(flags);
		case 14: return RoomsColumn.EXAM_TYPES.in(flags);
		case 15: return RoomsColumn.PERIOD_PREF.in(flags);
		case 16: return RoomsColumn.EVENT_DEPARTMENT.in(flags);
		case 17: return RoomsColumn.EVENT_STATUS.in(flags);
		case 18: return RoomsColumn.EVENT_AVAILABILITY.in(flags);
		case 19: return RoomsColumn.EVENT_MESSAGE.in(flags);
		case 20: return RoomsColumn.BREAK_TIME.in(flags);
		case 21: return RoomsColumn.GROUPS.in(flags);
		case 22: return RoomsColumn.FEATURES.in(flags);
		default:
			if (column > 22) {
				int flag = (1 << (column - 23 + RoomsColumn.values().length));
				return (flags & flag) == 0;
			} else {
				return true;
			}
		}
	}
	
	protected A getCell(RoomDetailInterface room, int column, ExportContext context) {
		switch (column) {
		case  0:
			A a = new A(room.hasDisplayName() ? MESSAGES.label(room.getLabel(), room.getDisplayName()) : room.getLabel());
			for (DepartmentInterface d: room.getDepartments())
				if (d.getDeptCode().equals(context.getDepartment()) && d.getPreference() != null)
					a.setColor(d.getPreference().getColor());
			return a;
		
		case 1:
			return new A(room.getRoomType().getLabel());

		case 2:
			return new A(room.getCapacity() == null ? "0" : room.getCapacity().toString()).right();
		
		case 3:
			return new A(room.getExamCapacity() == null ? "" : room.getExamCapacity().toString()).right();
			
		case 4:
			return new A(room.getArea() == null ? "" : context.getAreaFormat().format(room.getArea())).right();
			
		case 5:
			return new A(room.hasCoordinates() ? context.getCoordinateFormat().format(room.getX()) + "," + context.getCoordinateFormat().format(room.getY()) : "");
			
		case 6:
			return new A(room.isIgnoreRoomCheck() ? MESSAGES.exportFalse() : MESSAGES.exportTrue());
			
		case 7:
			return new A(room.isIgnoreTooFar() ? MESSAGES.exportFalse() : MESSAGES.exportTrue());
			
		case 8:
			if (!room.hasDepartments()) return new A();
			a = new A();
			for (DepartmentInterface d: room.getDepartments()) {
				if (d.getPreference() == null) continue;
				a.add(new A(PreferenceLevel.prolog2abbv(d.getPreference().getCode()) + " " + context.dept2string(d)).color(d.getPreference().getColor()));
			}
			return a;
			
		case 9:
			if (room.hasMiniMapUrl()) {
				try {
					Image image = Image.getInstance(new URL(room.getMiniMapUrl()));
					image.scaleToFit(150f, 100f);
					image.setBorder(Rectangle.BOX);
					image.setBorderWidth(1f);
					image.setBorderColor(new Color(Integer.parseInt("9CB0CE", 16)));
					return new A(image);
				} catch (Exception e) {
					return new A();
				}
			} else {
				return new A();
			}
		
		case 10:
			if (room.hasPictures()) {
				a = new A();
				for (RoomPictureInterface picture: room.getPictures()) {
					RoomPicture rp = RoomPictureDAO.getInstance().get(picture.getUniqueId());
					if (rp != null) {
						try {
							Image image = Image.getInstance(rp.getDataFile());
							image.scaleToFit(150f, 100f);
							image.setBorder(Rectangle.BOX);
							image.setBorderWidth(1f);
							image.setBorderColor(new Color(Integer.parseInt("9CB0CE", 16)));
							a.add(new A(image));
						} catch (Exception e) {
						}
					}
				}
				return a;
			} else{
				return new A();
			}
			
		case 11:
			return availability(room, false, context);
			
		case 12:
			if (!room.hasDepartments()) return new A();
			a = new A();
			for (DepartmentInterface d: room.getDepartments()) {
				A b = new A(context.dept2string(d)).color(d.getColor());
				if (d.equals(room.getControlDepartment())) b.underline();
				a.add(b);
			}
			return a;
		
		case 13:
			new A(context.dept2string(room.getControlDepartment())).color(room.getControlDepartment() == null ? null : room.getControlDepartment().getColor());
			
		case 14:
			if (!room.hasExamTypes()) return new A();
			a = new A();
			for (ExamTypeInterface t: room.getExamTypes()) {
				a.add(new A(t.getLabel()));
			}
			return a;
			
		case 15:
			return periodPreferences(room, context);
			
		case 16:
			return new A(context.dept2string(room.getEventDepartment())).color(room.getEventDepartment() == null ? null : room.getEventDepartment().getColor());
			
		case 17:
			return room.getEventStatus() != null ? new A(CONSTANTS.eventStatusAbbv()[room.getEventStatus()]) : room.getDefaultEventStatus() != null ? new A(CONSTANTS.eventStatusAbbv()[room.getDefaultEventStatus()]).italic() : new A();
			
		case 18:
			return availability(room, true, context);
			
		case 19:
			return room.getEventNote() != null ? new A(room.getEventNote()) : new A(room.getDefaultEventNote()).italic();
			
		case 20:
			return room.getBreakTime() != null ? new A(room.getBreakTime().toString()) : room.getDefaultBreakTime() != null ? new A(room.getDefaultBreakTime().toString()).italic() : new A();
		
		case 21:
			if (!room.hasGroups()) return new A();
			a = new A();
			for (GroupInterface g: room.getGroups()) {
				a.add(new A(g.getLabel() + (g.getDepartment() == null ? "" : " (" + context.dept2string(g.getDepartment()) + ")")).color(g.getDepartment() == null ? null : g.getDepartment().getColor()));
			}
			return a;
			
		case 22:
			return features(room.getFeatures(), null, context);
			
		default:
			return features(room.getFeatures(), context.getRoomFeatureTypes().get(column - 23), context);
		}
	}
	
	protected A availability(RoomDetailInterface room, boolean events, ExportContext context) {
		A ret = null;
		if (context.isGridAsText()) {
			ret = new A(events ? room.getEventAvailability() : room.getAvailability());
		} else {
			Location location = LocationDAO.getInstance().get(room.getUniqueId());
			RequiredTimeTable rtt = (events ? location.getEventAvailabilityTable() : location.getRoomSharingTable());
			rtt.getModel().setDefaultSelection(context.getMode());
			ret = new A(rtt.createBufferedImage(context.isVertical(), false));
		}
		if (!events && room.getRoomSharingNote() != null)
			ret.add(new A(room.getRoomSharingNote()));
		//else if (events && (room.hasEventNote() || room.hasDefaultEventNote()))
		//	ret.add(new A(room.hasEventNote() ? room.getEventNote() : room.getDefaultEventNote()));
		return ret;
	}
	
	protected A periodPreferences(RoomDetailInterface room, ExportContext context) {
		Location location = LocationDAO.getInstance().get(room.getUniqueId());
		for (ExamType type: location.getExamTypes())
			if (type.getReference().equals(context.getDepartment())) {
				if (type.getType() == ExamType.sExamTypeMidterm) {
					return new PDFMidtermPeriodPreferenceModel(location, type).toA();
				} else {
					PDFPeriodPreferenceModel px = new PDFPeriodPreferenceModel(location, type);
					if (context.isGridAsText())
						return px.toA();
					else
						return new A(new RequiredTimeTable(px).createBufferedImage(true, false));
				}
			}
		return new A();
	}
	
	protected A features(Collection<FeatureInterface> features, FeatureTypeInterface type, ExportContext context) {
		if (features == null || features.isEmpty()) return new A();
		A a = new A();
		for (FeatureInterface f: features) {
			if (type == null && f.getType() == null)
				a.add(new A(f.getLabel()));
			if (type != null && type.equals(f.getType())) {
				if (f.getDepartment() != null)
					a.add(new A(f.getLabel() + " (" + context.dept2string(f.getDepartment()) + ")").color(f.getDepartment().getColor()));
				else
					a.add(new A(f.getLabel()));					
			}
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
