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
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.export.ExportHelper;
import org.unitime.timetable.export.Exporter;
import org.unitime.timetable.gwt.client.teachingschedule.TeachingScheduleAPI.GetInstructorTeachingSchedule;
import org.unitime.timetable.gwt.client.teachingschedule.TeachingScheduleAPI.Instructor;
import org.unitime.timetable.gwt.client.teachingschedule.TeachingScheduleAPI.InstructorMeetingAssignment;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.TeachingScheduleMessages;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.dao.DepartmentDAO;

@Service("org.unitime.timetable.export.Exporter:teaching-instructors.xls")
public class TeachingScheduleInstructorExportXLS implements Exporter {
	static GwtConstants CONSTANTS = Localization.create(GwtConstants.class);
	static TeachingScheduleMessages MESSAGES = Localization.create(TeachingScheduleMessages.class);
	static CourseMessages CMSG = Localization.create(CourseMessages.class);
	
	@Override
	public String reference() {
		return "teaching-instructors.xls";
	}

	@Override
	public void export(ExportHelper helper) throws IOException {
		// Load offering
		String department = helper.getParameter("department");
		if (department == null)
			throw new IllegalArgumentException("Department parameter not provided.");
		org.hibernate.Session hibSession = DepartmentDAO.getInstance().getSession();
		Department d = null;
		try {
			d = DepartmentDAO.getInstance().get(Long.valueOf(department), hibSession);
		} catch (NumberFormatException e) {}
		if (d == null) {
			if (helper.getAcademicSessionId() == null)
				throw new IllegalArgumentException("Academic session not provided, please set the term parameter.");
			d = Department.findByDeptCode(department, helper.getAcademicSessionId());
		}
		
		if (d == null)
			throw new IllegalArgumentException("Department " + d + " not found.");

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

		for (DepartmentalInstructor di: (List<DepartmentalInstructor>)hibSession.createQuery(
				"from DepartmentalInstructor di where di.department = :departmentId order by di.lastName, di.firstName"
				).setLong("departmentId", d.getUniqueId()).list()) {
			Instructor instructor = new TeachingScheduleGetInstructorSchedule().execute(new GetInstructorTeachingSchedule(di.getUniqueId()), helper.getSessionContext());
			if (instructor.hasAssignments()) {
				exportInstructor(workbook, styles, instructor);
			}
		}
		
		helper.setup("application/vnd.ms-excel", d.getDeptCode().replace('/', '-').replace('\\', '-').replace(':', '-') + ".xls", true);

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
	
	protected void exportInstructor(HSSFWorkbook workbook, Map<String, HSSFCellStyle> styles, Instructor instructor) {
		HSSFSheet sheet = workbook.createSheet(instructor.getName());
		sheet.setDisplayGridlines(false);
		sheet.setPrintGridlines(false);
		sheet.setFitToPage(true);
		sheet.setHorizontallyCenter(true);
		HSSFPrintSetup printSetup = sheet.getPrintSetup();
		printSetup.setLandscape(true);
		sheet.setAutobreaks(true);
		printSetup.setFitHeight((short)1);
		printSetup.setFitWidth((short)1);

		int rowIdx = 0, colIdx;
		HSSFRow row;
		HSSFCell cell;
		
		row = sheet.createRow(rowIdx++);
		cell = row.createCell(0);
		sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 7));
		cell.setCellStyle(styles.get("title"));
		cell.setCellValue(instructor.getName());
		row.setHeightInPoints(18f);
		
		row = sheet.createRow(rowIdx++); colIdx = 0;
		cell = row.createCell(colIdx++);
		cell.setCellStyle(styles.get("header-first"));
		cell.setCellValue(MESSAGES.colOffering());
		cell = row.createCell(colIdx++);
		cell.setCellStyle(styles.get("header"));
		cell.setCellValue(MESSAGES.colMeetingGroup());
		cell = row.createCell(colIdx++);
		cell.setCellStyle(styles.get("header"));
		cell.setCellValue(MESSAGES.colMeetingDivision());
		cell = row.createCell(colIdx++);
		cell.setCellStyle(styles.get("header-right"));
		cell.setCellValue(MESSAGES.colMeetingHours());
		cell = row.createCell(colIdx++);
		cell.setCellStyle(styles.get("header"));
		cell.setCellValue(MESSAGES.colMeetingDate());
		cell = row.createCell(colIdx++);
		cell.setCellStyle(styles.get("header-center"));
		cell.setCellValue(MESSAGES.colMeetingTime());
		cell = row.createCell(colIdx++);
		cell.setCellStyle(styles.get("header"));
		cell.setCellValue(MESSAGES.colMeetingRoom());
		cell = row.createCell(colIdx++);
		cell.setCellStyle(styles.get("header-last"));
		cell.setCellValue(MESSAGES.colMeetingNote());
		
		int nrRows = 0;
		List<InstructorMeetingAssignment> meetings = instructor.getAssignmetns();
		for (int i = 0; i < meetings.size(); i++) {
			InstructorMeetingAssignment meeting = meetings.get(i);
			row = sheet.createRow(rowIdx++); colIdx = 0;
			boolean sameDiv = true;
			boolean sameNote = true;
			boolean sameTime = true;
			boolean sameGroup = true;
			if (nrRows == 0) {
				nrRows = 1;
				for (int j = i + 1; j < meetings.size(); j++) {
					if (meetings.get(j).getClassMeetingId().equals(meeting.getClassMeetingId())) {
						nrRows++;
						if (meeting.getDivision() != null && !meeting.getDivision().equals(meetings.get(j).getDivision()))
							sameDiv = false;
						if (meeting.getNote() != null && !meeting.getNote().equals(meetings.get(j).getNote()))
							sameNote = false;
						if (meeting.getHours() != null && !meeting.getHours().equals(meetings.get(j).getHours()))
							sameTime = false;
						if (meeting.getGroup() != null && !meeting.getGroup().equals(meetings.get(j).getGroup()))
							sameGroup = false;
					}
				}
				if (nrRows > 1) {
					sheet.addMergedRegion(new CellRangeAddress(rowIdx - 1, rowIdx + nrRows - 2, 0, 0));
					if (sameGroup)
						sheet.addMergedRegion(new CellRangeAddress(rowIdx - 1, rowIdx + nrRows - 2, 1, 1));
					if (sameDiv)
						sheet.addMergedRegion(new CellRangeAddress(rowIdx - 1, rowIdx + nrRows - 2, 2, 2));
					if (sameTime)
						sheet.addMergedRegion(new CellRangeAddress(rowIdx - 1, rowIdx + nrRows - 2, 3, 3));
					sheet.addMergedRegion(new CellRangeAddress(rowIdx - 1, rowIdx + nrRows - 2, 4, 4));
					if (sameTime)
						sheet.addMergedRegion(new CellRangeAddress(rowIdx - 1, rowIdx + nrRows - 2, 5, 5));
					sheet.addMergedRegion(new CellRangeAddress(rowIdx - 1, rowIdx + nrRows - 2, 6, 6));
					if (sameNote)
						sheet.addMergedRegion(new CellRangeAddress(rowIdx - 1, rowIdx + nrRows - 2, 7, 7));
				}
			}
			cell = row.createCell(colIdx++);
			cell.setCellStyle(styles.get("text-first" + (nrRows > 1 ? "-dash" : "")));
			cell.setCellValue(meeting.getName());
			
			cell = row.createCell(colIdx++);
			cell.setCellStyle(styles.get("text" + (nrRows > 1 ? "-dash" : "")));
			cell.setCellValue(meeting.getGroup() == null ? "" : meeting.getGroup());

			cell = row.createCell(colIdx++);
			cell.setCellStyle(styles.get("text" + (nrRows > 1 ? "-dash" : "")));
			cell.setCellValue(meeting.getDivision() == null ? "" : meeting.getDivision());

			cell = row.createCell(colIdx++);
			cell.setCellStyle(styles.get("text-right" + (nrRows > 1 ? "-dash" : "")));
			cell.setCellValue(meeting.getLoad());
			
			cell = row.createCell(colIdx++);
			cell.setCellStyle(styles.get("text" + (nrRows > 1 ? "-dash" : "")));
			cell.setCellValue(meeting.getMeetingDate());
			
			cell = row.createCell(colIdx++);
			cell.setCellStyle(styles.get("text-center" + (nrRows > 1 ? "-dash" : "")));
			cell.setCellValue(slot2time(meeting.getHours().getStartSlot()) + " - " + slot2time(meeting.getHours().getEndSlot()));
			
			cell = row.createCell(colIdx++);
			cell.setCellStyle(styles.get("text" + (nrRows > 1 ? "-dash" : "")));
			cell.setCellValue(meeting.getLocation() == null ? "" : meeting.getLocation());

			cell = row.createCell(colIdx++);
			cell.setCellStyle(styles.get("text-last" + (nrRows > 1 ? "-dash" : "")));
			cell.setCellValue(meeting.getNote() == null ? "" : meeting.getNote());
			nrRows --;
		}
		
		for (int c = 0; c < sheet.getRow(1).getLastCellNum(); c++) {
			sheet.autoSizeColumn(c);
		}
	}
}
