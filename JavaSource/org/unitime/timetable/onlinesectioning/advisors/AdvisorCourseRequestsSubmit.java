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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.unitime.localization.impl.Localization;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.resources.StudentSectioningConstants;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.server.DayCode;
import org.unitime.timetable.gwt.shared.CourseRequestInterface;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.Preference;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.Request;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.RequestedCourse;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.AdvisingStudentDetails;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.AdvisorCourseRequestSubmission;
import org.unitime.timetable.model.AdvisorCourseRequest;
import org.unitime.timetable.model.FreeTime;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.StudentSectioningStatus;
import org.unitime.timetable.model.dao.CourseOfferingDAO;
import org.unitime.timetable.model.dao.StudentDAO;
import org.unitime.timetable.onlinesectioning.OnlineSectioningAction;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer.Lock;
import org.unitime.timetable.onlinesectioning.custom.CustomCriticalCoursesHolder;
import org.unitime.timetable.onlinesectioning.custom.CriticalCoursesProvider.CriticalCourses;
import org.unitime.timetable.onlinesectioning.model.XStudent;
import org.unitime.timetable.onlinesectioning.model.XStudentId;
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
public class AdvisorCourseRequestsSubmit implements OnlineSectioningAction<AdvisorCourseRequestSubmission> {
	private static final long serialVersionUID = 1L;
	protected static StudentSectioningMessages MSG = Localization.create(StudentSectioningMessages.class);
	protected static StudentSectioningConstants CONST = Localization.create(StudentSectioningConstants.class);
	private AdvisingStudentDetails iDetails;
	
	public AdvisorCourseRequestsSubmit withDetails(AdvisingStudentDetails details) {
		iDetails = details;
		return this;
	}
	
	public AdvisingStudentDetails getDetails() { return iDetails; }
	
	@Override
	public AdvisorCourseRequestSubmission execute(OnlineSectioningServer server, OnlineSectioningHelper helper) {
		try {
			AdvisorCourseRequestSubmission ret = new AdvisorCourseRequestSubmission();
			
			OnlineSectioningLog.Action.Builder action = helper.addAction(this, server.getAcademicSession());
			action.setStudent(OnlineSectioningLog.Entity.newBuilder()
				.setUniqueId(getDetails().getStudentId())
				.setExternalId(getDetails().getStudentExternalId())
				.setName(getDetails().getStudentName()));
			if (getDetails().getStatus() != null && getDetails().getStatus().getUniqueId() != null) {
				action.addOther(OnlineSectioningLog.Entity.newBuilder()
						.setUniqueId(getDetails().getStatus().getUniqueId())
						.setName(getDetails().getStatus().getLabel())
						.setExternalId(getDetails().getStatus().getReference())
						.setType(OnlineSectioningLog.Entity.EntityType.OTHER));
			}
			if (getDetails().getRequest() != null)
				for (OnlineSectioningLog.Request r: OnlineSectioningHelper.toProto(getDetails().getRequest()))
					action.addRequest(r);

			if (getDetails().isCanUpdate()) {
				Date ts = new Date();
				Lock lock = server.lockStudent(getDetails().getStudentId(), null, name());
				try {
					helper.beginTransaction();
					
					Student dbStudent = StudentDAO.getInstance().get(getDetails().getStudentId(), helper.getHibSession());
					if (dbStudent != null) {
						
						CriticalCourses critical = null;
						try {
							if (CustomCriticalCoursesHolder.hasProvider())
								critical = CustomCriticalCoursesHolder.getProvider().getCriticalCourses(server, helper, new XStudentId(dbStudent, helper));
						} catch (Exception e) {
							helper.warn("Failed to lookup critical courses: " + e.getMessage(), e);
						}
					
						List<AdvisorCourseRequest> acrs = helper.getHibSession().createQuery(
								"from AdvisorCourseRequest where student = :studentId order by priority, alternative"
								).setLong("studentId", getDetails().getStudentId()).list();
						if (dbStudent.getAdvisorCourseRequests() == null)
							dbStudent.setAdvisorCourseRequests(new HashSet<AdvisorCourseRequest>());
						
						if (getDetails().getRequest() != null) {
							int priority = 0;
							for (Request request: getDetails().getRequest().getCourses()) {
								if (request.hasRequestedCourse()) {
									int alt = 0;
									for (RequestedCourse rc: request.getRequestedCourse()) {
										if (rc.isFreeTime()) {
											for (CourseRequestInterface.FreeTime ft: rc.getFreeTime()) {
												AdvisorCourseRequest acr = null;
												for (Iterator<AdvisorCourseRequest> i = acrs.iterator(); i.hasNext(); ) {
													AdvisorCourseRequest adept = i.next();
													if (adept.getFreeTime() != null) {
														acr = adept; i.remove(); break;
													}
												}
												if (acr == null) {
													acr = new AdvisorCourseRequest();
													acr.setStudent(dbStudent);
													acr.setChangedBy(helper.getUser().getExternalId());
													acr.setTimestamp(ts);
													dbStudent.getAdvisorCourseRequests().add(acr);
												} else if (acr.getPreferences() != null) {
													acr.getPreferences().clear();
												}
												FreeTime free = acr.getFreeTime();
												if (free == null) {
													free = new FreeTime();
													acr.setFreeTime(free);
												}
												free.setCategory(0);
												free.setDayCode(DayCode.toInt(DayCode.toDayCodes(ft.getDays())));
												free.setStartSlot(ft.getStart());
												free.setLength(ft.getLength());
												free.setSession(dbStudent.getSession());
												free.setName(ft.toString());
												acr.setCourseOffering(null);
												acr.setCourse(CONST.freePrefix() + ft.toString(CONST.shortDays(), CONST.useAmPm()));
												acr.setPriority(priority); acr.setAlternative(alt); acr.setSubstitute(false);
												if (alt == 0) {
													acr.setCredit(request.getAdvisorCredit());
													acr.setNotes(request.getAdvisorNote());
												} else {
													acr.setCredit(null); acr.setNotes(null); 
												}
												acr.setCritical(false);
												helper.getHibSession().saveOrUpdate(free);
												helper.getHibSession().saveOrUpdate(acr);
												alt++;
											}
											continue;
										}
										AdvisorCourseRequest acr = null;
										for (Iterator<AdvisorCourseRequest> i = acrs.iterator(); i.hasNext(); ) {
											AdvisorCourseRequest adept = i.next();
											if (rc.hasCourseId() && adept.getCourseOffering() != null && rc.getCourseId().equals(adept.getCourseOffering().getUniqueId())) {
												acr = adept; i.remove(); break;
											} else if (!rc.hasCourseId() && adept.getCourseOffering() == null && rc.getCourseName().equals(adept.getCourse())) {
												acr = adept; i.remove(); break;
											}
										}
										if (acr == null) {
											acr = new AdvisorCourseRequest();
											acr.setStudent(dbStudent);
											acr.setChangedBy(helper.getUser().getExternalId());
											acr.setTimestamp(ts);
											dbStudent.getAdvisorCourseRequests().add(acr);
										}
										acr.setCourseOffering(rc.hasCourseId() ? CourseOfferingDAO.getInstance().get(rc.getCourseId(), helper.getHibSession()) : null);
										acr.setCourse(rc.getCourseName());
										acr.setPriority(priority); acr.setAlternative(alt); acr.setSubstitute(false);
										if (acr.getFreeTime() != null) {
											helper.getHibSession().delete(acr.getFreeTime());
											acr.setFreeTime(null);
										}
										if (alt == 0) {
											acr.setCredit(request.getAdvisorCredit());
											acr.setNotes(request.getAdvisorNote());
										} else {
											acr.setCredit(null); acr.setNotes(null); 
										}
										acr.setCritical(acr.isCritical(critical));
										acr.updatePreferences(rc, helper.getHibSession());
										helper.getHibSession().saveOrUpdate(acr);
										alt++;
									}
								} else {
									AdvisorCourseRequest acr = null;
									for (Iterator<AdvisorCourseRequest> i = acrs.iterator(); i.hasNext(); ) {
										AdvisorCourseRequest adept = i.next();
										if (adept.getCourseOffering() == null && adept.getFreeTime() == null && adept.getCourse() == null) {
											acr = adept; i.remove(); break;
										}
									}
									if (acr == null) {
										acr = new AdvisorCourseRequest();
										acr.setStudent(dbStudent);
										dbStudent.getAdvisorCourseRequests().add(acr);
									} else if (acr.getPreferences() != null) {
										acr.getPreferences().clear();
									}
									if (acr.getFreeTime() != null) {
										helper.getHibSession().delete(acr.getFreeTime());
										acr.setFreeTime(null);
									}
									acr.setFreeTime(null);
									acr.setCourse(null); acr.setCourseOffering(null);
									acr.setPriority(priority); acr.setAlternative(0); acr.setSubstitute(false);
									acr.setCredit(request.getAdvisorCredit());
									acr.setNotes(request.getAdvisorNote());
									acr.setChangedBy(helper.getUser().getExternalId());
									acr.setTimestamp(ts);
									acr.setCritical(false);
									helper.getHibSession().saveOrUpdate(acr);
								}
								priority ++;
							}
							// substitutes
							for (Request request: getDetails().getRequest().getAlternatives()) {
								if (request.hasRequestedCourse()) {
									int alt = 0;
									for (RequestedCourse rc: request.getRequestedCourse()) {
										AdvisorCourseRequest acr = null;
										for (Iterator<AdvisorCourseRequest> i = acrs.iterator(); i.hasNext(); ) {
											AdvisorCourseRequest adept = i.next();
											if (rc.hasCourseId() && adept.getCourseOffering() != null && rc.getCourseId().equals(adept.getCourseOffering().getUniqueId())) {
												acr = adept; i.remove(); break;
											} else if (!rc.hasCourseId() && adept.getCourseOffering() == null && rc.getCourseName().equals(adept.getCourse())) {
												acr = adept; i.remove(); break;
											}
										}
										if (acr == null) {
											acr = new AdvisorCourseRequest();
											acr.setStudent(dbStudent);
											acr.setChangedBy(helper.getUser().getExternalId());
											acr.setTimestamp(ts);
											dbStudent.getAdvisorCourseRequests().add(acr);
										}
										acr.setCourseOffering(rc.hasCourseId() ? CourseOfferingDAO.getInstance().get(rc.getCourseId(), helper.getHibSession()) : null);
										acr.setCourse(rc.getCourseName());
										acr.setPriority(priority); acr.setAlternative(alt); acr.setSubstitute(true);
										if (alt == 0) {
											acr.setCredit(request.getAdvisorCredit());
											acr.setNotes(request.getAdvisorNote());
										} else {
											acr.setCredit(null); acr.setNotes(null); 
										}
										if (acr.getFreeTime() != null) {
											helper.getHibSession().delete(acr.getFreeTime());
											acr.setFreeTime(null);
										}
										acr.setCritical(acr.isCritical(critical));
										acr.updatePreferences(rc, helper.getHibSession());
										helper.getHibSession().saveOrUpdate(acr);
										alt++;
									}
								} else {
									AdvisorCourseRequest acr = null;
									for (Iterator<AdvisorCourseRequest> i = acrs.iterator(); i.hasNext(); ) {
										AdvisorCourseRequest adept = i.next();
										if (adept.getCourseOffering() == null && adept.getFreeTime() == null && adept.getCourse() == null) {
											acr = adept; i.remove(); break;
										}
									}
									if (acr == null) {
										acr = new AdvisorCourseRequest();
										acr.setStudent(dbStudent);
										dbStudent.getAdvisorCourseRequests().add(acr);
									} else if (acr.getPreferences() != null) {
										acr.getPreferences().clear();
									}
									if (acr.getFreeTime() != null) {
										helper.getHibSession().delete(acr.getFreeTime());
										acr.setFreeTime(null);
									}
									acr.setCourse(null); acr.setCourseOffering(null);
									acr.setPriority(priority); acr.setAlternative(0); acr.setSubstitute(true);
									acr.setCredit(request.getAdvisorCredit());
									acr.setNotes(request.getAdvisorNote());
									acr.setChangedBy(helper.getUser().getExternalId());
									acr.setTimestamp(ts);
									acr.setCritical(false);
									helper.getHibSession().saveOrUpdate(acr);
								}
								priority ++;
							}
							if (getDetails().getRequest().hasCreditNote()) {
								AdvisorCourseRequest acr = null;
								for (Iterator<AdvisorCourseRequest> i = acrs.iterator(); i.hasNext(); ) {
									AdvisorCourseRequest adept = i.next();
									if (adept.getPriority() == -1) {
										acr = adept; i.remove(); break;
									}
								}
								if (acr == null) {
									acr = new AdvisorCourseRequest();
									acr.setStudent(dbStudent);
									dbStudent.getAdvisorCourseRequests().add(acr);
								} else if (acr.getPreferences() != null) {
									acr.getPreferences().clear();
								}
								if (acr.getFreeTime() != null) {
									helper.getHibSession().delete(acr.getFreeTime());
									acr.setFreeTime(null);
								}
								acr.setCourse(null); acr.setCourseOffering(null);
								acr.setPriority(-1); acr.setAlternative(0); acr.setSubstitute(false);
								acr.setCredit(null);
								acr.setNotes(getDetails().getRequest().getCreditNote());
								acr.setChangedBy(helper.getUser().getExternalId());
								acr.setTimestamp(ts);
								acr.setCritical(false);
								helper.getHibSession().saveOrUpdate(acr);
							}
						}
						
						
						for (AdvisorCourseRequest acr: acrs) {
							if (acr.getFreeTime() != null)
								helper.getHibSession().delete(acr.getFreeTime());
							helper.getHibSession().delete(acr);
							dbStudent.getAdvisorCourseRequests().remove(acr);
						}
						
						// change status
						if (getDetails().getStatus() != null) {
							XStudent student = server.getStudent(getDetails().getStudentId());
							if (student != null) {
								String current = (student.getStatus() == null ? "" : student.getStatus());
								if (!getDetails().getStatus().getReference().equals(current)) {
									//status change
									StudentSectioningStatus status = (getDetails().getStatus().getReference().isEmpty() ? null :
										StudentSectioningStatus.getStatus(getDetails().getStatus().getReference(), server.getAcademicSession().getUniqueId(), helper.getHibSession()));

									String oldStatus = (dbStudent.getSectioningStatus() != null ? dbStudent.getSectioningStatus().getReference() :
										dbStudent.getSession().getDefaultSectioningStatus() != null ? MSG.studentStatusSessionDefault(dbStudent.getSession().getDefaultSectioningStatus().getReference())
										: MSG.studentStatusSystemDefault());
									
									if (dbStudent.getSectioningStatus() != null)
										action.addOptionBuilder().setKey("old-status").setValue(dbStudent.getSectioningStatus().getReference());

									student.setStatus(status == null ? null : status.getReference());
									dbStudent.setSectioningStatus(status);
									
									String newStatus = (dbStudent.getSectioningStatus() != null ? dbStudent.getSectioningStatus().getReference() :
										dbStudent.getSession().getDefaultSectioningStatus() != null ? MSG.studentStatusSessionDefault(dbStudent.getSession().getDefaultSectioningStatus().getReference())
										: MSG.studentStatusSystemDefault());
									if (dbStudent.getSectioningStatus() != null)
										action.addOptionBuilder().setKey("new-status").setValue(dbStudent.getSectioningStatus().getReference());
									if (oldStatus.equals(newStatus))
										action.addMessage(OnlineSectioningLog.Message.newBuilder().setText(oldStatus).setTimeStamp(ts.getTime()).setLevel(OnlineSectioningLog.Message.Level.INFO));
									else
										action.addMessage(OnlineSectioningLog.Message.newBuilder().setText(oldStatus + " &rarr; " + newStatus).setTimeStamp(ts.getTime()).setLevel(OnlineSectioningLog.Message.Level.INFO));
									
									helper.getHibSession().saveOrUpdate(dbStudent);
									server.update(student, false);
									action.setResult(OnlineSectioningLog.Action.ResultType.TRUE);
								} else {
									action.setResult(OnlineSectioningLog.Action.ResultType.FALSE);
								}
							}
						}
					}
					helper.commitTransaction();
					ret.setUpdated(true);
				} catch (Exception e) {
					helper.rollbackTransaction();
					if (e instanceof SectioningException) throw (SectioningException)e;
					throw new SectioningException(MSG.exceptionUnknown(e.getMessage()), e);
				} finally {
					lock.release();
				}
			}

			generatePdfConfirmation(ret, server, helper);
			return ret;
		} catch (Exception e) {
			helper.error("Failed to produce confirmation PDF: " + e.getMessage(), e);
			throw new SectioningException("Failed to produce confirmation PDF: " + e.getMessage(), e);
		}
	}
	
	protected void generatePdfConfirmation(AdvisorCourseRequestSubmission ret, OnlineSectioningServer server, OnlineSectioningHelper helper) throws IOException, DocumentException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Document document = new Document(PageSize.LETTER);
		PdfWriter writer = PdfWriter.getInstance(document, out);
		writer.setPageEvent(new PdfEventHandler());
		document.open();
		
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
		
		header.addCell(header(MSG.propStudentName()));
		header.addCell(cell(getDetails().getStudentName()));
		header.addCell(header(MSG.propStudentExternalId()));
		header.addCell(cell(getDetails().getStudentExternalId()));
		header.addCell(header(MSG.propAdvisorEmail()));
		header.addCell(cell(getDetails().getAdvisorEmail()));
		header.addCell(header(MSG.propAcademicSession()));
		header.addCell(cell(getDetails().getSessionName()));
		if (getDetails().getStatus() != null) {
			header.addCell(header(""));
			header.addCell(cell(""));
			header.addCell(header(MSG.propStudentStatus()));
			header.addCell(cell(getDetails().getStatus().getLabel()));
		}
		document.add(header);
		
		if (getDetails().getRequest().getCourses() != null && !getDetails().getRequest().getCourses().isEmpty())
			document.add(courseTable(true));
		
		if (getDetails().getRequest().getAlternatives() != null && !getDetails().getRequest().getAlternatives().isEmpty())
			document.add(courseTable(false));
		
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
		PdfPCell disc = cell(MSG.pdfStudentDisclaimer());
		disc.setPaddingTop(30f); disc.setColspan(2);
		sign.addCell(disc);
		
		sign.setTotalWidth(document.right() - document.left());
		sign.writeSelectedRows(0, -1, document.left(), sign.getTotalHeight() + document.bottom(), writer.getDirectContent());
		// document.add(sign);
		
		document.close();
		out.flush(); out.close();
		ret.setPdf(out.toByteArray());
	}
	
	private PdfPTable courseTable(boolean primary) {
		Font font = PdfFont.getSmallFont();
		float wP = font.getBaseFont().getWidth(primary ? MSG.courseRequestsPriority(100) : MSG.courseRequestsAlternate(100)) * 0.001f * font.getSize();
		float wA = 20f + font.getBaseFont().getWidth(MSG.courseRequestsAlternative(100)) * 0.001f * font.getSize() - wP;
		float wC = 5f + font.getBaseFont().getWidth("999 - 999") * 0.001f * font.getSize();
		float pw = PageSize.LETTER.getWidth() - 72f;
		float wX = (pw - wP - wA - wC);
		PdfPTable cr = new PdfPTable(new float[] {wP, wA, wX * 0.4f, wC, wX * 0.6f});
		cr.setHeaderRows(1);
		cr.setWidthPercentage(100f);
		cr.setKeepTogether(true);
		
		if (primary) {
			PdfPCell c = new PdfPCell();
			c.setBorder(0);
			font = PdfFont.getBigFont(true);
			font.setSize(font.getSize() * 0.8f);
			Paragraph ch = new Paragraph(MSG.courseRequestsCourses(), font);
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
		} else {
			PdfPCell c = new PdfPCell();
			c.setBorder(0);
			font = PdfFont.getBigFont(true);
			font.setSize(font.getSize() * 0.8f);
			Paragraph ch = new Paragraph(MSG.courseRequestsAlternatives(), font);
			Chunk x = new Chunk("          " + MSG.courseRequestsAlternativesNote());
			x.setFont(PdfFont.getSmallFont(false, true));
			ch.add(x);
			ch.setAlignment(Element.ALIGN_LEFT);
			ch.setSpacingBefore(10f); ch.setSpacingAfter(2f); c.addElement(ch); c.setColspan(5);
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
			float min = 0, max = 0;
			for (Request r: getDetails().getRequest().getCourses()) {
				if (!r.hasAdvisorCredit()) continue;
				String cred = r.getAdvisorCredit().replaceAll("\\s","");
				try {
					min += Float.parseFloat(cred);
					max += Float.parseFloat(cred);
					continue;
				} catch (NumberFormatException e) {}
				int idx = cred.indexOf('-');
				if (idx >= 0) {
					try {
						min += Float.parseFloat(cred.substring(0, idx));
						max += Float.parseFloat(cred.substring(idx + 1));
						continue;
					} catch (NumberFormatException e) {}	
				}
			}
			
			String credTx = "";
			Formats.Format<Number> nf = Formats.getNumberFormat("0.#"); 
			if (min < max) {
				credTx = nf.format(min) + " - " + nf.format(max);
			} else {
				credTx = nf.format(min);
			}
			PdfPCell credit = number(MSG.labelTotalPriorityCreditHours());
			credit.setColspan(3);
			cr.addCell(credit);
			credit = number(credTx);
			cr.addCell(credit);
			credit = cell("");
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
	
	@Override
	public String name() {
		return "advisor-submit";
	}
}
