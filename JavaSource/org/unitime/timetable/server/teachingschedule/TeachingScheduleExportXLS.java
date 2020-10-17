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
package org.unitime.timetable.server.teachingschedule;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFPrintSetup;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.stereotype.Service;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.export.ExportHelper;
import org.unitime.timetable.export.Exporter;
import org.unitime.timetable.gwt.client.teachingschedule.TeachingScheduleAPI.Clazz;
import org.unitime.timetable.gwt.client.teachingschedule.TeachingScheduleAPI.GetTeachingSchedule;
import org.unitime.timetable.gwt.client.teachingschedule.TeachingScheduleAPI.Instructor;
import org.unitime.timetable.gwt.client.teachingschedule.TeachingScheduleAPI.MeetingAssignment;
import org.unitime.timetable.gwt.client.teachingschedule.TeachingScheduleAPI.TeachingMeeting;
import org.unitime.timetable.gwt.client.teachingschedule.TeachingScheduleAPI.TeachingSchedule;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.TeachingScheduleMessages;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.dao.CourseOfferingDAO;
import org.unitime.timetable.model.dao.InstructionalOfferingDAO;

@Service("org.unitime.timetable.export.Exporter:teaching-schedule.xls")
public class TeachingScheduleExportXLS implements Exporter {
	static GwtConstants CONSTANTS = Localization.create(GwtConstants.class);
	static TeachingScheduleMessages MESSAGES = Localization.create(TeachingScheduleMessages.class);
	
	@Override
	public String reference() {
		return "teaching-schedule.xls";
	}

	@Override
	public void export(ExportHelper helper) throws IOException {
		// Load offering
		String course = helper.getParameter("course");
		if (course == null)
			throw new IllegalArgumentException("Course parameter not provided.");
		org.hibernate.Session hibSession = InstructionalOfferingDAO.getInstance().getSession();
		CourseOffering co = null;
		try {
			co = CourseOfferingDAO.getInstance().get(Long.valueOf(course), hibSession);
		} catch (NumberFormatException e) {}
		if (co == null) {
			if (helper.getAcademicSessionId() == null)
				throw new IllegalArgumentException("Academic session not provided, please set the term parameter.");
			co = CourseOffering.findByName(course, helper.getAcademicSessionId());
		}
		
		if (co == null)
			throw new IllegalArgumentException("Course " + course + " not found.");

		TeachingSchedule schedule = new TeachingScheduleGet().execute(new GetTeachingSchedule(co.getUniqueId(), null), helper.getSessionContext());
		if (schedule == null || !schedule.hasGroups())
			throw new IllegalArgumentException("Course " + co.getCourseName() + " has no teaching schedule.");
		
		HSSFWorkbook workbook = new HSSFWorkbook();
		
		Map<String, HSSFCellStyle> styles = new HashMap<>();
		
		styles.put("title", getStyle(workbook, getFont(workbook, 12, true, false, false), HorizontalAlignment.CENTER, null));
		styles.get("title").setBorderLeft(BorderStyle.NONE);
		styles.get("title").setBorderRight(BorderStyle.NONE);
		styles.get("title").setVerticalAlignment(VerticalAlignment.BOTTOM);
		
		styles.put("header", getStyle(workbook, getFont(workbook, 10, true, false, false), HorizontalAlignment.LEFT, IndexedColors.LIGHT_CORNFLOWER_BLUE));
		styles.get("header").setBorderTop(BorderStyle.THIN);
		styles.put("header-right", getStyle(workbook, getFont(workbook, 10, true, false, false), HorizontalAlignment.RIGHT, IndexedColors.LIGHT_CORNFLOWER_BLUE));
		styles.get("header-right").setBorderTop(BorderStyle.THIN);
		styles.put("header-center", getStyle(workbook, getFont(workbook, 10, true, false, false), HorizontalAlignment.CENTER, IndexedColors.LIGHT_CORNFLOWER_BLUE));
		styles.get("header-center").setBorderTop(BorderStyle.THIN);
		styles.put("header-first", getStyle(workbook, getFont(workbook, 10, true, false, false), HorizontalAlignment.LEFT, IndexedColors.LIGHT_CORNFLOWER_BLUE));
		styles.get("header-first").setBorderTop(BorderStyle.THIN);
		styles.get("header-first").setBorderLeft(BorderStyle.THIN);
		styles.put("header-last", getStyle(workbook, getFont(workbook, 10, true, false, false), HorizontalAlignment.LEFT, IndexedColors.LIGHT_CORNFLOWER_BLUE));
		styles.get("header-last").setBorderTop(BorderStyle.THIN);
		styles.get("header-last").setBorderRight(BorderStyle.THIN);
		
		styles.put("text", getStyle(workbook, getFont(workbook, 10, false, false, false), HorizontalAlignment.LEFT, null));
		styles.put("text-right", getStyle(workbook, getFont(workbook, 10, false, false, false), HorizontalAlignment.RIGHT, null));
		styles.put("text-center", getStyle(workbook, getFont(workbook, 10, false, false, false), HorizontalAlignment.CENTER, null));
		styles.put("text-first", getStyle(workbook, getFont(workbook, 10, false, false, false), HorizontalAlignment.LEFT, null));
		styles.get("text-first").setBorderLeft(BorderStyle.THIN);
		styles.put("text-last", getStyle(workbook, getFont(workbook, 10, false, false, false), HorizontalAlignment.LEFT, null));
		styles.get("text-last").setBorderRight(BorderStyle.THIN);
		
		styles.put("text-dash", getStyle(workbook, getFont(workbook, 10, false, false, false), HorizontalAlignment.LEFT, null));
		styles.get("text-dash").setBorderBottom(BorderStyle.NONE);
		styles.put("text-first-dash", getStyle(workbook, getFont(workbook, 10, false, false, false), HorizontalAlignment.LEFT, null));
		styles.get("text-first-dash").setBorderLeft(BorderStyle.THIN);
		styles.get("text-first-dash").setBorderBottom(BorderStyle.NONE);
		styles.put("text-last-dash", getStyle(workbook, getFont(workbook, 10, false, false, false), HorizontalAlignment.LEFT, null));
		styles.get("text-last-dash").setBorderRight(BorderStyle.THIN);
		styles.get("text-last-dash").setBorderBottom(BorderStyle.NONE);
		styles.put("text-right-dash", getStyle(workbook, getFont(workbook, 10, false, false, false), HorizontalAlignment.RIGHT, null));
		styles.get("text-right-dash").setBorderBottom(BorderStyle.NONE);
		styles.put("text-center-dash", getStyle(workbook, getFont(workbook, 10, false, false, false), HorizontalAlignment.CENTER, null));
		styles.get("text-center-dash").setBorderBottom(BorderStyle.NONE);
		
		for (Clazz clazz: schedule.getClasses()) {
			exportClazz(workbook, styles, schedule, clazz);
		}
		
		helper.setup("application/vnd.ms-excel", co.getCourseName().replace('/', '-').replace('\\', '-').replace(':', '-') + ".xls", true);

		workbook.write(helper.getOutputStream());
		workbook.close();
		helper.getOutputStream().close();
	}
	
	protected HSSFFont getFont(HSSFWorkbook workbook, int size, boolean bold, boolean italic, boolean underline) {
		HSSFFont font = workbook.createFont();
		font.setColor(IndexedColors.BLACK.getIndex());
		font.setBold(bold);
		font.setItalic(italic);
		if (underline) font.setUnderline(Font.U_SINGLE);
		else font.setUnderline(Font.U_NONE);
		font.setFontHeightInPoints((short)size);
		font.setFontName("Arial");
		return font;
	}
	
	protected HSSFCellStyle getStyle(HSSFWorkbook workbook, HSSFFont font, HorizontalAlignment align, IndexedColors bg) {
		HSSFCellStyle style = workbook.createCellStyle();
		style.setBorderBottom(BorderStyle.THIN);
		style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
		style.setBorderRight(BorderStyle.DASHED);
		style.setBorderLeft(BorderStyle.DASHED);
		style.setAlignment(align);
		style.setVerticalAlignment(VerticalAlignment.TOP);
		style.setFont(font);
		if (bg != null) {
			style.setFillForegroundColor(bg.getIndex());
			style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		}
		style.setWrapText(true);
		return style;
	}
	
	protected String slot2time(int slot) {
		int timeSinceMidnight = 5 * slot;
		int hour = timeSinceMidnight / 60;
	    int min = timeSinceMidnight % 60;
	    if (CONSTANTS.useAmPm())
	    	return (hour==0?12:hour>12?hour-12:hour)+":"+(min<10?"0":"")+min+(hour<24 && hour>=12?"p":"a");
	    else
	    	return hour + ":" + (min < 10 ? "0" : "") + min;
	}
	
	protected void exportClazz(HSSFWorkbook workbook, Map<String, HSSFCellStyle> styles, TeachingSchedule schedule, Clazz clazz) {
		HSSFSheet sheet = workbook.createSheet(clazz.getName());
		sheet.setDisplayGridlines(false);
		sheet.setPrintGridlines(false);
		sheet.setFitToPage(true);
		sheet.setHorizontallyCenter(true);
		HSSFPrintSetup printSetup = sheet.getPrintSetup();
		printSetup.setLandscape(false);
		sheet.setAutobreaks(true);
		printSetup.setFitHeight((short)1);
		printSetup.setFitWidth((short)1);

		int rowIdx = 0, colIdx;
		HSSFRow row;
		HSSFCell cell;
		
		row = sheet.createRow(rowIdx++);
		cell = row.createCell(0);
		sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 4));
		cell.setCellStyle(styles.get("title"));
		cell.setCellValue(schedule.getCourseName() + ": " + clazz.getName());
		row.setHeightInPoints(18f);
		
		row = sheet.createRow(rowIdx++); colIdx = 0;
		cell = row.createCell(colIdx++);
		cell.setCellStyle(styles.get("header-first"));
		cell.setCellValue(MESSAGES.colMeetingDate());
		cell = row.createCell(colIdx++);
		cell.setCellStyle(styles.get("header-center"));
		cell.setCellValue(MESSAGES.colMeetingTime());
		cell = row.createCell(colIdx++);
		cell.setCellStyle(styles.get("header"));
		cell.setCellValue(MESSAGES.colMeetingRoom());
		cell = row.createCell(colIdx++);
		cell.setCellStyle(styles.get("header"));
		cell.setCellValue(MESSAGES.colMeetingInstructor());
		cell = row.createCell(colIdx++);
		cell.setCellStyle(styles.get("header-last"));
		cell.setCellValue(MESSAGES.colMeetingNote());
		
		int nrRows = 0;
		List<MeetingAssignment> mas = clazz.getMeetingAssignments();
		for (int i = 0; i < mas.size(); i++) {
			MeetingAssignment ma = mas.get(i);
			TeachingMeeting meeting = schedule.getMeeting(ma);
			row = sheet.createRow(rowIdx++); colIdx = 0;

			if (nrRows == 0) {
				nrRows = 1;
				for (int j = i + 1; j < mas.size(); j++) {
					if (mas.get(j).getClassMeetingId().equals(ma.getClassMeetingId())) {
						nrRows++;
					}
				}
				if (nrRows > 1) {
					sheet.addMergedRegion(new CellRangeAddress(rowIdx - 1, rowIdx + nrRows - 2, 0, 0));
					sheet.addMergedRegion(new CellRangeAddress(rowIdx - 1, rowIdx + nrRows - 2, 2, 2));
				}
			}
			cell = row.createCell(colIdx++);
			cell.setCellStyle(styles.get("text-first" + (nrRows > 1 ? "-dash" : "")));
			cell.setCellValue(meeting.getMeetingDate());
			
			cell = row.createCell(colIdx++);
			cell.setCellStyle(styles.get("text-center" + (nrRows > 1 ? "-dash" : "")));
			cell.setCellValue(slot2time(meeting.getHour(ma.getFirstHour()).getStartSlot()) + " - " + slot2time(meeting.getHour(ma.getLastHour()).getEndSlot()));

			cell = row.createCell(colIdx++);
			cell.setCellStyle(styles.get("text" + (nrRows > 1 ? "-dash" : "")));
			cell.setCellValue(meeting.getLocation());

			String instructors = "";
			if (ma.hasInstructors()) {
				for (Long id: ma.getInstructor()) {
					Instructor instructor = schedule.getInstructor(id);
					if (instructor != null) 
						instructors += (instructors.isEmpty() ? "" : "\n") + instructor.getName();
				}
			}
				
			cell = row.createCell(colIdx++);
			cell.setCellStyle(styles.get("text" + (nrRows > 1 ? "-dash" : "")));
			cell.setCellValue(instructors);
			
			cell = row.createCell(colIdx++);
			cell.setCellStyle(styles.get("text-last" + (nrRows > 1 ? "-dash" : "")));
			cell.setCellValue(ma.hasNote() ? ma.getNote() : "");
			nrRows --;
		}
		
		for (int c = 0; c < sheet.getRow(1).getLastCellNum(); c++) {
			sheet.autoSizeColumn(c);
		}
	}
}
