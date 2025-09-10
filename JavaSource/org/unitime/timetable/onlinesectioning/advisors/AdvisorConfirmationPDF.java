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
package org.unitime.timetable.onlinesectioning.advisors;

import java.awt.Color;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;

import org.unitime.localization.impl.Localization;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.resources.StudentSectioningConstants;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.Preference;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.Request;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.RequestedCourse;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.AdvisingStudentDetails;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.WaitListMode;
import org.unitime.timetable.model.CourseDemand;
import org.unitime.timetable.model.CourseDemand.Critical;
import org.unitime.timetable.util.Formats;
import org.unitime.timetable.util.PdfEventHandler;
import org.unitime.timetable.util.PdfFont;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

/**
 * @author Tomas Muller
 */
public class AdvisorConfirmationPDF {
	private AdvisingStudentDetails iDetails;
	protected static StudentSectioningMessages MSG = Localization.create(StudentSectioningMessages.class);
	protected static StudentSectioningConstants CONST = Localization.create(StudentSectioningConstants.class);
	
	public AdvisorConfirmationPDF(AdvisingStudentDetails details) {
		iDetails = details;
	}

	public AdvisingStudentDetails getDetails() { return iDetails; }
	
	public void generatePdfConfirmation(OutputStream out) throws IOException, DocumentException {
		Document document = new Document(PageSize.LETTER);
		PdfWriter writer = PdfWriter.getInstance(document, out);
		writer.setPageEvent(new PdfEventHandler());
		document.open();
		Critical critical = Critical.fromText(ApplicationProperty.AdvisorCourseRequestsAllowCritical.valueOfSession(iDetails.getSessionId()));
		
		Image image = null; 
		URL imageUrl = AdvisorCourseRequestsSubmit.class.getClassLoader().getResource(ApplicationProperty.AdvisorCourseRequestsPDFLogo.value());
		if (imageUrl != null) {
			image = Image.getInstance(imageUrl);
		} else {
			image = Image.getInstance(new URL(ApplicationProperty.AdvisorCourseRequestsPDFLogo.value()));
		}
		image.scaleToFit(80f, 80f);
		
		PdfPTable header = new PdfPTable(new float[] {80f, 200f, 40f, 200f});
		header.setHeaderRows(0);
		header.setWidthPercentage(100f);
		
		Font font = PdfFont.getBigFont(true);
		PdfPCell imageCell = new PdfPCell();
		imageCell.setBorder(0);
		imageCell.addElement(new Chunk(image, 0f, 0f));
		imageCell.setPaddingTop(Math.max(0f, font.getCalculatedLeading(1.5f) - image.getScaledHeight()));
		imageCell.setPaddingBottom(10f);
		header.addCell(imageCell);
		
		Paragraph ch = new Paragraph(MSG.pdfHeaderAdvisorCourseRequests(), font);
		ch.setAlignment(Element.ALIGN_CENTER);
		PdfPCell headerCell = new PdfPCell();
		headerCell.addElement(ch);
		headerCell.setBorder(0);
		headerCell.setColspan(3);
		headerCell.setPaddingTop(Math.max(0f, image.getScaledHeight() - font.getCalculatedLeading(1.5f)));
		headerCell.setPaddingRight(80f);
		headerCell.setPaddingBottom(10f);
		header.addCell(headerCell);

		header.completeRow();
		
		if (getDetails().getStudentExternalId() != null) {
			// row 1
			header.addCell(header(MSG.propStudentName()));
			header.addCell(cell(getDetails().getStudentName()));
			header.addCell(header(MSG.propStudentExternalId()));
			header.addCell(cell(getDetails().getStudentExternalId()));
			// row 2
			header.addCell(header(MSG.propStudentEmail()));
			header.addCell(cell(getDetails().getStudentEmail()));
			header.addCell(header(MSG.propAcademicSession()));
			header.addCell(cell(getDetails().getSessionName()));
			// row 3
			header.addCell(header(MSG.propAdvisorEmail()));
			header.addCell(cell(getDetails().getAdvisorEmail()));
			header.addCell(header(MSG.propStudentStatus()));
			header.addCell(cell(getDetails().getStatus() == null ? "" : getDetails().getStatus().getLabel()));
			// row 4
			if (getDetails().getRequest().hasReleasedPin()) {
				header.addCell(header(""));
				header.addCell(cell(""));
				header.addCell(header(MSG.propStudentPin()));
				header.addCell(cell(getDetails().getRequest().getPin()));
			}
		} else {
			// row 1
			header.addCell(header(MSG.propStudentName()));
			header.addCell(cell(getDetails().getStudentName()));
			header.addCell(header(MSG.propAcademicSession()));
			header.addCell(cell(getDetails().getSessionName()));
			// row 2
			header.addCell(header(MSG.propStudentEmail()));
			header.addCell(cell(getDetails().getStudentEmail()));
			if (getDetails().getRequest().hasReleasedPin()) {
				header.addCell(header(MSG.propStudentStatus()));
				header.addCell(cell(getDetails().getStatus() == null ? "" : getDetails().getStatus().getLabel()));
			} else {
				PdfPCell sh = header(getDetails().getStatus() == null ? "" : MSG.propStudentStatus());
				sh.setRowspan(2); header.addCell(sh);
				PdfPCell sc = cell(getDetails().getStatus() == null ? "" : getDetails().getStatus().getLabel());
				sc.setRowspan(2); header.addCell(sc);
			}
			// row 3
			header.addCell(header(MSG.propAdvisorEmail()));
			header.addCell(cell(getDetails().getAdvisorEmail()));
			if (getDetails().getRequest().hasReleasedPin()) {
				header.addCell(header(MSG.propStudentPin()));
				header.addCell(cell(getDetails().getRequest().getPin()));
			}
		}
		
		document.add(header);
		
		if (getDetails().getRequest().getCourses() != null && !getDetails().getRequest().getCourses().isEmpty()) {
			boolean hasWaitList = false;
			for (Request r: getDetails().getRequest().getCourses()) {
				if (getDetails().getWaitListMode() == WaitListMode.WaitList) {
					if (r.isWaitList()) { hasWaitList = true; break; }
				} else if (getDetails().getWaitListMode() == WaitListMode.NoSubs) {
					if (r.isNoSub()) { hasWaitList = true; break; }
				}
			}
			boolean hasCritical = false;
			for (Request r: getDetails().getRequest().getCourses()) {
				if (r.hasCritical() && r.getCritical() > 0)
					hasCritical = true;
			}
			document.add(courseTable(true, hasWaitList ? getDetails().getWaitListMode() : WaitListMode.None, hasCritical ? critical : Critical.NORMAL));
		}
		
		if (getDetails().getRequest().getAlternatives() != null && !getDetails().getRequest().getAlternatives().isEmpty())
			document.add(courseTable(false, WaitListMode.None, Critical.NORMAL));
		
		if (getDetails().getRequest().hasCreditNote()) {
			Paragraph p = new Paragraph(getDetails().getRequest().getCreditNote(), PdfFont.getSmallFont());
			p.setSpacingBefore(10f);
			document.add(p);
		}
		
		PdfPTable sign = new PdfPTable(new float[] {
				PdfFont.getSmallFont(true).getBaseFont().getWidth(MSG.pdfAdvisorSignature()), 
				PdfFont.getSmallFont(true).getBaseFont().getWidth(MSG.pdfSignatureDate())});
		sign.setSpacingBefore(20f);
		sign.setHeaderRows(0);
		sign.setKeepTogether(true);
		sign.setWidthPercentage(100f);
		sign.addCell(header(MSG.pdfAdvisorSignature()));
		sign.addCell(header(MSG.pdfSignatureDate()));
		PdfPCell h1 = header(MSG.pdfStudentSignature());
		h1.setPaddingTop(10f);
		sign.addCell(h1);
		PdfPCell h2 = header(MSG.pdfSignatureDate());
		h2.setPaddingTop(10f);
		sign.addCell(h2);
		String disclaimer = ApplicationProperty.AdvisorCourseRequestsPDFDisclaimer.value();
		if (disclaimer != null && !disclaimer.isEmpty()) {
			PdfPCell disc = cell(disclaimer);
			disc.setPaddingTop(30f); disc.setColspan(2);
			sign.addCell(disc);
		}
		
		sign.setTotalWidth(document.right() - document.left());
		if (writer.getVerticalPosition(true) < sign.getTotalHeight() + document.bottom()) document.newPage();
		sign.writeSelectedRows(0, -1, document.left(), sign.getTotalHeight() + document.bottom(), writer.getDirectContent());
		// document.add(sign);
		
		document.close();
		out.flush(); out.close();
	}
	
	private PdfPTable courseTable(boolean primary, WaitListMode waitListMode, CourseDemand.Critical critical) {
		Font font = PdfFont.getSmallFont();
		float wP = font.getBaseFont().getWidth(primary ? MSG.courseRequestsPriority(100) : MSG.courseRequestsAlternate(100)) * 0.001f * font.getSize();
		float wA = 20f + font.getBaseFont().getWidth(MSG.courseRequestsAlternative(100)) * 0.001f * font.getSize() - wP;
		float wC = 5f + font.getBaseFont().getWidth("999 - 999") * 0.001f * font.getSize();
		String criticalHead = null;
		switch (critical) {
		case CRITICAL: criticalHead = MSG.opSetCritical(); break;
		case IMPORTANT: criticalHead = MSG.opSetImportant(); break;
		case VITAL: criticalHead = MSG.opSetVital(); break;
		}
		String waitListHead = null;
		switch (waitListMode) {
		case NoSubs: waitListHead = MSG.colNoSubs(); break;
		case WaitList: waitListHead = MSG.colWaitList(); break;
		}

		float wV = (criticalHead == null ? 0f : 5f + font.getBaseFont().getWidth(criticalHead) * 0.001f * font.getSize());
		float wW = (waitListHead == null ? 0f : 5f + font.getBaseFont().getWidth(waitListHead) * 0.001f * font.getSize());
		float pw = PageSize.LETTER.getWidth() - 72f;
		float wX = (pw - wP - wA - wC - wV - wW);
		PdfPTable cr = (
				waitListHead != null && criticalHead != null ? new PdfPTable(new float[] {wP, wA, wX * 0.4f, wC, wX * 0.6f, wV, wW}) :
				waitListHead != null && criticalHead == null ? new PdfPTable(new float[] {wP, wA, wX * 0.4f, wC, wX * 0.6f, wW}) :
				waitListHead == null && criticalHead != null ? new PdfPTable(new float[] {wP, wA, wX * 0.4f, wC, wX * 0.6f, wV}) :
				new PdfPTable(new float[] {wP, wA, wX * 0.4f, wC, wX * 0.6f})
				);
		cr.setHeaderRows(1);
		cr.setWidthPercentage(100f);
		cr.setKeepTogether(true);
		
		if (primary) {
			PdfPCell c = new PdfPCell();
			c.setBorder(0);
			font = PdfFont.getBigFont(true);
			font.setSize(font.getSize() * 0.8f);
			Paragraph ch = new Paragraph(MSG.advisorRequestsCourses(), font);
			ch.setAlignment(Element.ALIGN_LEFT);
			ch.setSpacingBefore(10f); ch.setSpacingAfter(2f); c.addElement(ch); c.setColspan(3);
			c.setBorder(PdfPCell.BOTTOM); c.setBorderWidth(0.1f);
			cr.addCell(c);
			c = italicNumber(MSG.colCredit());
			c.setVerticalAlignment(Element.ALIGN_BOTTOM);
			c.setBorder(PdfPCell.BOTTOM); c.setBorderWidth(0.1f);
			cr.addCell(c);
			c = italic(MSG.colNotes());
			c.setVerticalAlignment(Element.ALIGN_BOTTOM);
			c.setBorder(PdfPCell.BOTTOM); c.setBorderWidth(0.1f);
			cr.addCell(c);
			if (criticalHead != null) {
				c = italic(criticalHead);
				c.setVerticalAlignment(Element.ALIGN_BOTTOM);
				c.setBorder(PdfPCell.BOTTOM); c.setBorderWidth(0.1f);
				cr.addCell(c);
			}
			if (waitListMode != WaitListMode.None) {
				c = italic(waitListMode == WaitListMode.WaitList ? MSG.colWaitList() : MSG.colNoSubs());
				c.setVerticalAlignment(Element.ALIGN_BOTTOM);
				c.setBorder(PdfPCell.BOTTOM); c.setBorderWidth(0.1f);
				cr.addCell(c);
			}
		} else {
			PdfPCell c = new PdfPCell();
			c.setBorder(0);
			font = PdfFont.getBigFont(true);
			font.setSize(font.getSize() * 0.8f);
			Paragraph ch = new Paragraph(MSG.advisorRequestsAlternatives(), font);
			Chunk x = new Chunk("          " + MSG.courseRequestsAlternativesNote());
			x.setFont(PdfFont.getSmallFont(false, true));
			ch.add(x);
			ch.setAlignment(Element.ALIGN_LEFT);
			ch.setSpacingBefore(10f); ch.setSpacingAfter(2f); c.addElement(ch); c.setColspan(5 + (criticalHead == null ? 0 : 1) + (waitListHead == null ? 0 : 1));
			c.setBorder(PdfPCell.BOTTOM); c.setBorderWidth(0.1f);
			cr.addCell(c);
		}
		
		int priority = 1;
		for (Request r: (primary ? getDetails().getRequest().getCourses() : getDetails().getRequest().getAlternatives())) {
			int alt = 0;
			if (r.getRequestedCourse() == null) {
				PdfPCell h = cell(MSG.courseRequestsPriority(priority));
				h.setBorder(PdfPCell.BOTTOM); h.setBorderWidth(0.1f);
				cr.addCell(h);
				
				PdfPCell course = cell("");
				course.setColspan(2);
				course.setBorder(PdfPCell.BOTTOM); course.setBorderWidth(0.1f);
				cr.addCell(course);
				
				PdfPCell credit = number(r.getAdvisorCredit());
				credit.setBorder(PdfPCell.LEFT | PdfPCell.RIGHT | PdfPCell.BOTTOM); credit.setBorderWidth(0.1f);
				cr.addCell(credit);
				
				PdfPCell note = cell(r.getAdvisorNote());
				note.setBorder(PdfPCell.BOTTOM); note.setBorderWidth(0.1f);
				cr.addCell(note);
				
				if (criticalHead != null) {
					PdfPCell vit = cell(
							r.isCritical() ? MSG.pdfCourseCritical() :
							r.isVital() ? MSG.pdfCourseVital() :
							r.isImportant() ? MSG.pdfCourseImportant() :
							MSG.pdfCourseNotCritical());
					((Paragraph)vit.getCompositeElements().get(0)).setAlignment(Element.ALIGN_CENTER);
					vit.setBorder(PdfPCell.BOTTOM | PdfPCell.LEFT); vit.setBorderWidth(0.1f);
					cr.addCell(vit);
				}

				if (waitListMode == WaitListMode.WaitList) {
					PdfPCell wl = cell(r.isWaitList() ? MSG.pdfCourseWaitListed() : MSG.pdfCourseNotWaitListed());
					((Paragraph)wl.getCompositeElements().get(0)).setAlignment(Element.ALIGN_CENTER);
					wl.setBorder(PdfPCell.BOTTOM | PdfPCell.LEFT); wl.setBorderWidth(0.1f);
					cr.addCell(wl);
				} else if (waitListMode == WaitListMode.NoSubs) {
					PdfPCell wl = cell(r.isNoSub() ? MSG.pdfCourseWaitListed() : MSG.pdfCourseNotWaitListed());
					((Paragraph)wl.getCompositeElements().get(0)).setAlignment(Element.ALIGN_CENTER);
					wl.setBorder(PdfPCell.BOTTOM | PdfPCell.LEFT); wl.setBorderWidth(0.1f);
					cr.addCell(wl);
				}
				continue;
			} else {
				for (RequestedCourse rc: r.getRequestedCourse()) {
					boolean last = (alt + 1 == r.getRequestedCourse().size());
					if (alt == 0) {
						PdfPCell h = cell(MSG.courseRequestsPriority(priority));
						if (last) {
							h.setBorder(PdfPCell.BOTTOM); h.setBorderWidth(0.1f);
						}
						cr.addCell(h);
						
						PdfPCell course = course(rc);
						course.setColspan(2);
						if (last) {
							course.setBorder(PdfPCell.BOTTOM); course.setBorderWidth(0.1f);
						}
						cr.addCell(course);
						
						PdfPCell credit = number(r.getAdvisorCredit());
						credit.setBorder(PdfPCell.BOTTOM | PdfPCell.RIGHT | PdfPCell.LEFT); credit.setBorderWidth(0.1f);
						credit.setRowspan(r.getRequestedCourse().size());
						cr.addCell(credit);
						
						PdfPCell note = cell(r.getAdvisorNote());
						note.setBorder(PdfPCell.BOTTOM); note.setBorderWidth(0.1f);
						note.setRowspan(r.getRequestedCourse().size());
						cr.addCell(note);
						
						if (criticalHead != null) {
							PdfPCell vit = cell(
									r.isCritical() ? MSG.pdfCourseCritical() :
									r.isVital() ? MSG.pdfCourseVital() :
									r.isImportant() ? MSG.pdfCourseImportant() :
									MSG.pdfCourseNotCritical());
							((Paragraph)vit.getCompositeElements().get(0)).setAlignment(Element.ALIGN_CENTER);
							vit.setBorder(PdfPCell.BOTTOM | PdfPCell.LEFT); vit.setBorderWidth(0.1f);
							vit.setRowspan(r.getRequestedCourse().size());
							cr.addCell(vit);
						}
						
						if (waitListMode == WaitListMode.WaitList) {
							PdfPCell wl = cell(r.isWaitList() ? MSG.pdfCourseWaitListed() : MSG.pdfCourseNotWaitListed());
							((Paragraph)wl.getCompositeElements().get(0)).setAlignment(Element.ALIGN_CENTER);
							wl.setBorder(PdfPCell.BOTTOM | PdfPCell.LEFT); wl.setBorderWidth(0.1f);
							wl.setRowspan(r.getRequestedCourse().size());
							cr.addCell(wl);
						} else if (waitListMode == WaitListMode.NoSubs) {
							PdfPCell wl = cell(r.isNoSub() ? MSG.pdfCourseWaitListed() : MSG.pdfCourseNotWaitListed());
							((Paragraph)wl.getCompositeElements().get(0)).setAlignment(Element.ALIGN_CENTER);
							wl.setBorder(PdfPCell.BOTTOM | PdfPCell.LEFT); wl.setBorderWidth(0.1f);
							wl.setRowspan(r.getRequestedCourse().size());
							cr.addCell(wl);
						}
					} else {
						PdfPCell h = cell(MSG.courseRequestsAlternative(alt));
						h.setColspan(2);
						h.setPaddingLeft(20f);
						if (last) {
							h.setBorder(PdfPCell.BOTTOM); h.setBorderWidth(0.1f);
						}
						cr.addCell(h);
						
						PdfPCell course = course(rc);
						if (last) {
							course.setBorder(PdfPCell.BOTTOM); course.setBorderWidth(0.1f);
						}
						cr.addCell(course);
					}
					alt ++;
				}
			}
			priority ++;
		}
		
		if (primary) {
			float[] minMax = getDetails().getRequest().getAdvisorCreditRange();
			String credTx = "";
			Formats.Format<Number> nf = Formats.getNumberFormat("0.#"); 
			if (minMax[0] < minMax[1]) {
				credTx = nf.format(minMax[0]) + " - " + nf.format(minMax[1]);
			} else {
				credTx = nf.format(minMax[0]);
			}
			PdfPCell credit = number(MSG.labelTotalPriorityCreditHours());
			credit.setColspan(3);
			cr.addCell(credit);
			credit = number(credTx);
			cr.addCell(credit);
			credit = cell(""); credit.setColspan(1 + (criticalHead == null ? 0 : 1) + (waitListHead == null ? 0 : 1));
			cr.addCell(credit);
		}
		return cr;
	}
	
	private PdfPCell cell(String text, boolean bold, boolean italic, boolean right) {
		PdfPCell cell = new PdfPCell();
		cell.setVerticalAlignment(Element.ALIGN_TOP);
		Font font = PdfFont.getSmallFont(bold, italic);
		Paragraph ch = new Paragraph(text, font);
		ch.setAlignment(right ? Element.ALIGN_RIGHT : Element.ALIGN_LEFT);
		if (right) 
			cell.setPaddingRight(5f);
		ch.setLeading(0, 1);
		ch.setSpacingAfter(2f);
		cell.addElement(ch);
		cell.setBorder(0);
		return cell;
	}
	
	private PdfPCell cell(String text) {
		return cell(text, false, false, false);
	}
	
	private PdfPCell header(String text) {
		return cell(text, true, false, false);
	}
	
	private PdfPCell number(String text) {
		return cell(text, false, false, true);
	}

	private PdfPCell italic(String text) {
		return cell(text, false, true, false);
	}
	
	private PdfPCell italicNumber(String text) {
		return cell(text, false, true, true);
	}
	
	private PdfPCell course(RequestedCourse rc) {
		PdfPCell course = new PdfPCell();
		Paragraph coursePg = new Paragraph(rc.toString(CONST), PdfFont.getSmallFont(true));
		coursePg.setLeading(0, 1);
		coursePg.setSpacingAfter(2f);
		coursePg.setAlignment(Element.ALIGN_LEFT);
		course.addElement(coursePg);
		course.setBorder(0);
		boolean first = true;
		if (rc.hasSelectedClasses())
			for (Preference p: rc.getSelectedClasses()) {
				Chunk x = new Chunk((first ? "  " : ", ") + p.getText() + (p.isRequired() ? "!" : ""));
				x.setFont(PdfFont.getSmallFont());
				x.getFont().setSize(x.getFont().getSize() * 0.8f);
				x.getFont().setColor(Color.DARK_GRAY);
				coursePg.add(x);
				first = false;
			}
		if (rc.hasSelectedIntructionalMethods())
			for (Preference p: rc.getSelectedIntructionalMethods()) {
				Chunk x = new Chunk((first ? "  " : ", ") + p.getText() + (p.isRequired() ? "!" : ""));
				x.setFont(PdfFont.getSmallFont());
				x.getFont().setSize(x.getFont().getSize() * 0.8f);
				x.getFont().setColor(Color.DARK_GRAY);
				coursePg.add(x);
				first = false;
			}
		if (rc.hasCourseTitle()) {
			Chunk x = new Chunk((first ? "  " : " - ") + rc.getCourseTitle());
			x.setFont(PdfFont.getSmallFont(false, true));
			x.getFont().setSize(x.getFont().getSize() * 0.8f);
			x.getFont().setColor(Color.DARK_GRAY);
			coursePg.add(x);
		}
		return course;
	}
}
