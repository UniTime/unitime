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
import org.unitime.timetable.export.PDFPrinter.F;
import org.unitime.timetable.gwt.shared.EventInterface.EventServiceProviderInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.AttachmentTypeInterface;
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
		helper.getSessionContext().checkPermission(Right.RoomsExportPdf);
		
		List<Column> columns = new ArrayList<Column>();
		for (RoomsColumn column: RoomsColumn.values()) {
			int nrCells = getNbrCells(column, context);
			for (int idx = 0; idx < nrCells; idx++) {
				Column c = new Column(column, idx);
				if (isColumnVisible(c, context))
					columns.add(c);
			}
		}
		
		PDFPrinter printer = new PDFPrinter(helper.getOutputStream(), false);
		helper.setup(printer.getContentType(), reference(), false);
		
		String[] header = new String[columns.size()];
		for (int i = 0; i < columns.size(); i++)
			header[i] = getColumnName(columns.get(i), context).replace("<br>", "\n");
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
	
	@Override
	protected boolean isColumnVisible(Column column, ExportContext context) {
		switch (column.getColumn()) {
		case PICTURES:
			if (column.getIndex() > 0 && !context.getPictureTypes().get(column.getIndex() - 1).isImage()) return false;
		}
		return super.isColumnVisible(column, context);
	}
	

	protected A getCell(RoomDetailInterface room, Column column, ExportContext context) {
		switch (column.getColumn()) {
		case  NAME:
			A a = new A(room.hasDisplayName() ? MESSAGES.label(room.getLabel(), room.getDisplayName()) : room.getLabel());
			for (DepartmentInterface d: room.getDepartments())
				if (d.getDeptCode().equals(context.getDepartment()) && d.getPreference() != null)
					a.setColor(d.getPreference().getColor());
			return a;
			
		case EXTERNAL_ID:
			return new A(room.hasExternalId() ? room.getExternalId() : "");
		
		case TYPE:
			return new A(room.getRoomType().getLabel());

		case CAPACITY:
			return new A(room.getCapacity() == null ? "0" : room.getCapacity().toString()).right();
		
		case EXAM_CAPACITY:
			return new A(room.getExamCapacity() == null ? "" : room.getExamCapacity().toString()).right();
			
		case AREA:
			return new A(room.getArea() == null ? "" : context.getAreaFormat().format(room.getArea())).right();
			
		case COORDINATES:
			return new A(room.hasCoordinates() ? context.getCoordinateFormat().format(room.getX()) + "," + context.getCoordinateFormat().format(room.getY()) : "");
			
		case ROOM_CHECK:
			return new A(room.isIgnoreRoomCheck() ? MESSAGES.exportFalse() : MESSAGES.exportTrue());
			
		case DISTANCE_CHECK:
			return new A(room.isIgnoreTooFar() ? MESSAGES.exportFalse() : MESSAGES.exportTrue());
			
		case PREFERENCE:
			if (!room.hasDepartments()) return new A();
			a = new A();
			for (DepartmentInterface d: room.getDepartments()) {
				if (d.getPreference() == null) continue;
				a.add(new A(d.getPreference().getAbbv() + " " + context.dept2string(d, true)).color(d.getPreference().getColor()));
			}
			return a;
			
		case MAP:
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
		
		case PICTURES:
			AttachmentTypeInterface type = (column.getIndex() == 0 ? null : context.getPictureTypes().get(column.getIndex() - 1));
			if (room.hasPictures(type)) {
				a = new A();
				for (RoomPictureInterface picture: room.getPictures(type)) {
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
			
		case AVAILABILITY:
			return availability(room, false, context);
			
		case DEPARTMENTS:
			if (!room.hasDepartments()) return new A();
			if (context.isAllDepartments(room.getDepartments())) return new A(MESSAGES.departmentsAllLabel()).set(F.ITALIC);
			a = new A();
			for (DepartmentInterface d: room.getDepartments()) {
				A b = new A(context.dept2string(d, true)).color(d.getColor());
				if (d.equals(room.getControlDepartment())) b.underline();
				a.add(b);
			}
			return a;
		
		case CONTROL_DEPT:
			return new A(context.dept2string(room.getControlDepartment(), true)).color(room.getControlDepartment() == null ? null : room.getControlDepartment().getColor());
			
		case EXAM_TYPES:
			if (!room.hasExamTypes()) return new A();
			a = new A();
			for (ExamTypeInterface t: room.getExamTypes()) {
				a.add(new A(t.getLabel()));
			}
			return a;
			
		case PERIOD_PREF:
			return periodPreferences(room, context);
			
		case EVENT_DEPARTMENT:
			return new A(context.dept2string(room.getEventDepartment(), false)).color(room.getEventDepartment() == null ? null : room.getEventDepartment().getColor());
			
		case EVENT_STATUS:
			return room.getEventStatus() != null ? new A(CONSTANTS.eventStatusAbbv()[room.getEventStatus()], F.FIX_BR) : room.getDefaultEventStatus() != null ? new A(CONSTANTS.eventStatusAbbv()[room.getDefaultEventStatus()], F.FIX_BR).italic() : new A();
			
		case EVENT_AVAILABILITY:
			return availability(room, true, context);
			
		case EVENT_MESSAGE:
			return room.getEventNote() != null ? new A(room.getEventNote()) : new A(room.getDefaultEventNote()).italic();
			
		case BREAK_TIME:
			return room.getBreakTime() != null ? new A(room.getBreakTime().toString()) : room.getDefaultBreakTime() != null ? new A(room.getDefaultBreakTime().toString()).italic() : new A();
		
		case GROUPS:
			if (!room.hasGroups()) return new A();
			a = new A();
			for (GroupInterface g: room.getGroups()) {
				a.add(new A(g.getLabel() + (g.getDepartment() == null ? "" : " (" + context.dept2string(g.getDepartment(), true) + ")")).color(g.getDepartment() == null ? null : g.getDepartment().getColor()));
			}
			return a;
			
		case FEATURES:
			if (column.getIndex() == 0)
				return features(room.getFeatures(), null, context);
			else
				return features(room.getFeatures(), context.getRoomFeatureTypes().get(column.getIndex() - 1), context);
			
		case SERVICES:
			if (!room.hasServices()) return new A();
			a = new A();
			for (EventServiceProviderInterface s: room.getServices()) {
				a.add(new A(s.getLabel() + (s.getDepartmentId() == null || room.getEventDepartment() == null ? "" : " (" + context.dept2string(room.getEventDepartment(), true) + ")")));
			}
			return a;
		
		default:
			return null;
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
					a.add(new A(f.getLabel() + " (" + context.dept2string(f.getDepartment(), true) + ")").color(f.getDepartment().getColor()));
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
