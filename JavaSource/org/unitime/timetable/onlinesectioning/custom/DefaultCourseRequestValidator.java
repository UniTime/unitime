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
package org.unitime.timetable.onlinesectioning.custom;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cpsolver.ifs.assignment.Assignment;
import org.cpsolver.ifs.assignment.AssignmentMap;
import org.cpsolver.studentsct.constraint.DependentCourses;
import org.cpsolver.studentsct.extension.StudentQuality;
import org.cpsolver.studentsct.model.Course;
import org.cpsolver.studentsct.model.CourseRequest;
import org.cpsolver.studentsct.model.Enrollment;
import org.cpsolver.studentsct.model.Request;
import org.cpsolver.studentsct.model.Section;
import org.cpsolver.studentsct.model.Subpart;
import org.cpsolver.studentsct.online.OnlineReservation;
import org.cpsolver.studentsct.online.OnlineSectioningModel;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.resources.StudentSectioningConstants;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.server.Query;
import org.unitime.timetable.gwt.shared.CourseRequestInterface;
import org.unitime.timetable.gwt.shared.PageAccessException;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.CheckCoursesResponse;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.FreeTime;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.Preference;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.RequestPriority;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.RequestedCourse;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.RequestedCourseStatus;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.AdvisingStudentDetails;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.EligibilityCheck;
import org.unitime.timetable.model.CourseDemand;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalMethod;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.StudentSchedulingRule;
import org.unitime.timetable.model.StudentSectioningStatus;
import org.unitime.timetable.model.CourseDemand.Critical;
import org.unitime.timetable.model.dao.CourseOfferingDAO;
import org.unitime.timetable.model.dao.InstructionalOfferingDAO;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog.Action.Builder;
import org.unitime.timetable.onlinesectioning.basic.GetInfo;
import org.unitime.timetable.onlinesectioning.model.XAdvisorRequest;
import org.unitime.timetable.onlinesectioning.model.XConfig;
import org.unitime.timetable.onlinesectioning.model.XCourse;
import org.unitime.timetable.onlinesectioning.model.XCourseId;
import org.unitime.timetable.onlinesectioning.model.XCourseRequest;
import org.unitime.timetable.onlinesectioning.model.XDistribution;
import org.unitime.timetable.onlinesectioning.model.XDistributionType;
import org.unitime.timetable.onlinesectioning.model.XEnrollment;
import org.unitime.timetable.onlinesectioning.model.XFreeTimeRequest;
import org.unitime.timetable.onlinesectioning.model.XOffering;
import org.unitime.timetable.onlinesectioning.model.XRequest;
import org.unitime.timetable.onlinesectioning.model.XReservation;
import org.unitime.timetable.onlinesectioning.model.XReservationType;
import org.unitime.timetable.onlinesectioning.model.XSchedulingRule;
import org.unitime.timetable.onlinesectioning.model.XSection;
import org.unitime.timetable.onlinesectioning.model.XStudent;
import org.unitime.timetable.onlinesectioning.model.XSubpart;
import org.unitime.timetable.onlinesectioning.server.DatabaseServer;
import org.unitime.timetable.onlinesectioning.solver.FindAssignmentAction;
import org.unitime.timetable.onlinesectioning.solver.SectioningRequest;
import org.unitime.timetable.onlinesectioning.status.StatusPageSuggestionsAction.StudentMatcher;
import org.unitime.timetable.util.Formats;
import org.unitime.timetable.util.Formats.Format;

/**
 * @author Tomas Muller
 */
public class DefaultCourseRequestValidator implements CourseRequestsValidationProvider, AdvisorCourseRequestsValidationProvider {
	protected static StudentSectioningMessages MESSAGES = Localization.create(StudentSectioningMessages.class);
	protected static final StudentSectioningConstants CONSTANTS = Localization.create(StudentSectioningConstants.class);
	protected static Format<Number> sCreditFormat = Formats.getNumberFormat("0.##");
	protected String iParameterPrefix = "unitime.validation.";

	public DefaultCourseRequestValidator() {
	}

	protected boolean isWaitListNoAlts() {
		return "true".equalsIgnoreCase(ApplicationProperties.getProperty(iParameterPrefix + "waitListNoAlts", "false"));
	}

	protected boolean isAdvisedNoAlts() {
		return "true".equalsIgnoreCase(ApplicationProperties.getProperty(iParameterPrefix + "advisedNoAlts", "true"));
	}

	@Override
	public void checkEligibility(OnlineSectioningServer server, OnlineSectioningHelper helper, EligibilityCheck check, Student student) throws SectioningException {
	}

	protected List<RequestedCourse> getOverCreditRequests(CourseRequestInterface request, float maxCredit) {
		List<RequestedCourse> ret = new ArrayList<RequestedCourse>();
		// Step 1, only check primary courses
		float primary = 0f;
		for (CourseRequestInterface.Request r: request.getCourses()) {
			if (r.hasRequestedCourse() && r.getRequestedCourse(0).hasCredit()) {
				primary += r.getRequestedCourse(0).getCreditMin();
				if (primary > maxCredit)
					ret.add(r.getRequestedCourse(0));
			}
		}
		if (!ret.isEmpty()) return ret;
		// Step 2, check alternatives
		for (CourseRequestInterface.Request r: request.getCourses()) {
			if (r.hasRequestedCourse() && r.getRequestedCourse().size() > 1) {
				float credit = (r.getRequestedCourse(0).hasCredit() ? r.getRequestedCourse(0).getCreditMin() : 0f);
				for (int i = 1; i < r.getRequestedCourse().size(); i++) {
					float alt = (r.getRequestedCourse(i).hasCredit() ? r.getRequestedCourse(i).getCreditMin() : 0f);
					if (primary - credit + alt > maxCredit)
						ret.add(r.getRequestedCourse(i));
				}
			}
		}
		if (!ret.isEmpty()) return ret;
		// Step 3, check alternatives
		List<Float> credits = new ArrayList<Float>();
		float total = 0f;
		RequestedCourse last = null;
		for (CourseRequestInterface.Request r: request.getCourses()) {
			if (r.hasRequestedCourse()) {
				Float credit = null;
				for (RequestedCourse rc: r.getRequestedCourse()) {
					if (rc.hasCredit()) {
						if (credit == null || credit < rc.getCreditMin()) {
							credit = rc.getCreditMin();
							if (total + credit > maxCredit) ret.add(rc);
						}
					}
				}
				if (credit != null) {
					credits.add(credit); total += credit;
				}
				last = r.getRequestedCourse(0);
			}
		}
		if (!ret.isEmpty()) return ret;
		// Step 4, check alternate courses
		Collections.sort(credits);
		float low = (credits.isEmpty() ? 0f : credits.get(0));
		RequestedCourse first = null;
		for (CourseRequestInterface.Request r: request.getAlternatives()) {
			if (r.hasRequestedCourse()) {
				for (RequestedCourse rc: r.getRequestedCourse()) {
					if (rc.hasCredit()) {
						if (total + rc.getCreditMin() - low > maxCredit) { ret.add(rc); break; }
					}
				}
				if (first == null)
					first = r.getRequestedCourse(0);
			}
		}
		if (!ret.isEmpty()) return ret;
		// Fall back: return first alternate course or the last requested course
		ret.add(first != null ? first : last);
		return ret;
	}

	protected boolean isValidationEnabled(OnlineSectioningServer server, OnlineSectioningHelper helper, XStudent student) {
		String status = student.getStatus();
		if (status == null) status = server.getAcademicSession().getDefaultSectioningStatus();
		if (status == null) return true;
		StudentSectioningStatus dbStatus = StudentSectioningStatus.getPresentStatus(status, server.getAcademicSession().getUniqueId(), helper.getHibSession());
		return dbStatus != null && dbStatus.hasOption(StudentSectioningStatus.Option.reqval);
	}

	@Override
	public void check(OnlineSectioningServer server, OnlineSectioningHelper helper, CourseRequestInterface request) throws SectioningException {
		XStudent original = (request.getStudentId() == null ? null : server.getStudent(request.getStudentId()));
		if (original == null) return;
		// Do not check when validation is disabled
		if (!isValidationEnabled(server, helper, original)) return;

		Integer ORD_UNITIME = 0;

		Set<Long> advisorCoursesNoAlt = new HashSet<Long>();
		if (original.hasAdvisorRequests() && isAdvisedNoAlts())
			for (XAdvisorRequest ar: original.getAdvisorRequests()) {
				int count = 0;
				for (XAdvisorRequest x: original.getAdvisorRequests()) {
					if (x.getPriority() == ar.getPriority()) count ++;
				}
				if (count == 1 && ar.getCourseId() != null) advisorCoursesNoAlt.add(ar.getCourseId().getCourseId());
			}
		else if (original.hasAdvisorRequests() && isWaitListNoAlts())
			for (XAdvisorRequest ar: original.getAdvisorRequests()) {
				if (ar.isWaitListOrNoSub() && !ar.isSubstitute()) {
					int count = 0;
					for (XAdvisorRequest x: original.getAdvisorRequests()) {
						if (x.getPriority() == ar.getPriority() && !x.isSubstitute()) count ++;
					}
					if (count == 1 && ar.getCourseId() != null) advisorCoursesNoAlt.add(ar.getCourseId().getCourseId());
				}
			}
		for (CourseRequestInterface.Request r: request.getCourses()) {
			if (r.hasRequestedCourse() && r.getRequestedCourse().size() == 1) {
				RequestedCourse rc = r.getRequestedCourse(0);
				if (rc.getCourseId() != null && !rc.isReadOnly() && !advisorCoursesNoAlt.contains(rc.getCourseId()) && !request.hasCourse(rc.getParentCourseId())) {
					request.addConfirmationMessage(rc.getCourseId(), rc.getCourseName(), "NO_ALT",
							ApplicationProperties.getProperty(iParameterPrefix + "messages.courseHasNoAlt", "No alternative course provided.").replace("{course}", rc.getCourseName()), ORD_UNITIME);
				}
			}
		}
		for (CourseRequestInterface.Request r: request.getCourses()) {
			if (r.hasRequestedCourse() && r.getRequestedCourse().size() > 1) {
				for (RequestedCourse rc: r.getRequestedCourse()) {
					RequestedCourse parent = r.getCourse(rc.getParentCourseId());
					if (parent != null) {
						request.addConfirmationMessage(rc.getCourseId(), rc.getCourseName(), "PARENT_SAME_REQ",
								ApplicationProperties.getProperty(iParameterPrefix + "messages.parentSameRequest", "Associated course {parent} on the same request line.")
								.replace("{parent}", parent.getCourseName())
								.replace("{course}", rc.getCourseName())
								, ORD_UNITIME);
					}
				}
			}
			if (r.hasRequestedCourse()) {
				for (RequestedCourse rc: r.getRequestedCourse()) {
					CourseRequestInterface.Request a = request.getAlternativeForCourseId(rc.getParentCourseId());
					if (a != null) {
						RequestedCourse parent = a.getCourse(rc.getParentCourseId());
						request.addConfirmationMessage(rc.getCourseId(), rc.getCourseName(), "PARENT_SUBST",
								ApplicationProperties.getProperty(iParameterPrefix + "messages.parentSubstitute", "Associated course {parent} is requested as a substitute.")
								.replace("{parent}", parent.getCourseName())
								.replace("{course}", rc.getCourseName())
								, ORD_UNITIME);
					}
				}
			}
			if (r.hasRequestedCourse()) {
				for (RequestedCourse rc: r.getRequestedCourse()) {
					if (rc.hasParentCourseId() && !request.hasCourse(rc.getParentCourseId())) {
						XCourse parent = server.getCourse(rc.getParentCourseId());
						request.addConfirmationMessage(rc.getCourseId(), rc.getCourseName(), "NO_PARENT",
								ApplicationProperties.getProperty(iParameterPrefix + "messages.noParent", "Depends on {parent}.")
								.replace("{parent}", parent.getCourseName())
								.replace("{course}", rc.getCourseName())
								, ORD_UNITIME);
					}
				}
			}
		}
		for (CourseRequestInterface.Request r: request.getAlternatives()) {
			if (r.hasRequestedCourse() && r.getRequestedCourse().size() > 1) {
				for (RequestedCourse rc: r.getRequestedCourse()) {
					RequestedCourse parent = r.getCourse(rc.getParentCourseId());
					if (parent != null) {
						request.addConfirmationMessage(rc.getCourseId(), rc.getCourseName(), "PARENT_SAME_REQ",
								ApplicationProperties.getProperty(iParameterPrefix + "messages.parentSameRequest", "Associated course {parent} on the same request line.")
								.replace("{parent}", parent.getCourseName())
								.replace("{course}", rc.getCourseName())
								, ORD_UNITIME);
					}
				}
			}
			if (r.hasRequestedCourse()) {
				for (RequestedCourse rc: r.getRequestedCourse()) {
					if (rc.hasParentCourseId() && !request.hasCourse(rc.getParentCourseId())) {
						XCourse parent = server.getCourse(rc.getParentCourseId());
						request.addConfirmationMessage(rc.getCourseId(), rc.getCourseName(), "NO_PARENT",
								ApplicationProperties.getProperty(iParameterPrefix + "messages.noParent", "Depends on {parent}.")
								.replace("{parent}", parent.getCourseName())
								.replace("{course}", rc.getCourseName())
								, ORD_UNITIME);
					}
				}
			}
		}

		for (int i = 0; i < request.getCourses().size(); i++) {
			CourseRequestInterface.Request r = request.getCourse(i);
			if (r.hasRequestedCourse() && r.getRequestedCourse(0).isFreeTime()) {
				boolean hasCourse = false;
				for (int j = i + 1; j < request.getCourses().size(); j++) {
					CourseRequestInterface.Request q = request.getCourse(j);
					if (q.hasRequestedCourse() && q.getRequestedCourse(0).isCourse()) {
						hasCourse = true; break;
					}
				}
				if (hasCourse) {
					String free = "";
					for (FreeTime ft: r.getRequestedCourse(0).getFreeTime()) {
						if (!free.isEmpty()) free += ", ";
						free += ft.toString(CONSTANTS.shortDays(), CONSTANTS.useAmPm());
					}
					request.addConfirmationMessage(0l, CONSTANTS.freePrefix() + free, "FREE-TIME",
						ApplicationProperties.getProperty(iParameterPrefix + "messages.freeTimeHighPriority", "High priority free time"), ORD_UNITIME);
				}
			}
		}

		String minCreditLimit = ApplicationProperties.getProperty(iParameterPrefix + "minCreditCheck");
		float minCredit = 0;
		for (CourseRequestInterface.Request r: request.getCourses()) {
			if (r.hasRequestedCourse()) {
				for (RequestedCourse rc: r.getRequestedCourse())
					if (rc.hasCredit()) {
						minCredit += rc.getCreditMin(); break;
					}
			}
		}
		if (!request.isEmpty()) request.setMaxCreditOverrideStatus(RequestedCourseStatus.SAVED);
		if (minCreditLimit != null && minCredit > 0 && minCredit < Float.parseFloat(minCreditLimit) && (original.getMaxCredit() == null || original.getMaxCredit() > Float.parseFloat(minCreditLimit))) {
			String minCreditLimitFilter = ApplicationProperties.getProperty(iParameterPrefix + "minCreditCheck.studentFilter");
			if (minCreditLimitFilter == null || minCreditLimitFilter.isEmpty() ||
					new Query(minCreditLimitFilter).match(new StudentMatcher(original, server.getAcademicSession().getDefaultSectioningStatus(), server, false))) {
				request.setCreditWarning(
						ApplicationProperties.getProperty(iParameterPrefix + "messages.minCredit",
						"Less than {min} credit hours requested.").replace("{min}", minCreditLimit).replace("{credit}", sCreditFormat.format(minCredit))
						);
				request.setMaxCreditOverrideStatus(RequestedCourseStatus.CREDIT_LOW);
			}
		}

		Float maxCredit = original.getMaxCredit();
		if (maxCredit == null) maxCredit = Float.parseFloat(ApplicationProperties.getProperty(iParameterPrefix + "maxCreditDefault", "18"));

		Set<Long> advisorWaitListedCourseIds = original.getAdvisorWaitListedCourseIds(server);
		if (maxCredit < request.getCredit(advisorWaitListedCourseIds)) {
			for (RequestedCourse rc: getOverCreditRequests(request, maxCredit)) {
				request.addConfirmationMessage(rc.getCourseId(), rc.getCourseName(), "CREDIT",
						ApplicationProperties.getProperty(iParameterPrefix + "messages.maxCredit", "Maximum of {max} credit hours exceeded.").replace("{max}", sCreditFormat.format(maxCredit)).replace("{credit}", sCreditFormat.format(request.getCredit(advisorWaitListedCourseIds))), null,
						ORD_UNITIME);
			}
			request.setCreditWarning(ApplicationProperties.getProperty(iParameterPrefix + "messages.maxCredit", "Maximum of {max} credit hours exceeded.").replace("{max}", sCreditFormat.format(maxCredit)).replace("{credit}", sCreditFormat.format(request.getCredit(advisorWaitListedCourseIds))));
			request.setMaxCreditOverrideStatus(RequestedCourseStatus.CREDIT_HIGH);
		}

		XSchedulingRule rule = server.getSchedulingRule(original,
				StudentSchedulingRule.Mode.Online,
				helper.hasAvisorPermission(),
				helper.hasAdminPermission());
		if (rule != null) {
			for (XRequest r: original.getRequests()) {
				if (r instanceof XCourseRequest) {
					XCourseRequest cr = (XCourseRequest)r;
					for (XCourseId course: cr.getCourseIds()) {
						if (!rule.matchesCourse(course, helper.getHibSession())) {
							request.addConfirmationMessage(course.getCourseId(), course.getCourseName(), "NOT-RULE",
									ApplicationProperties.getProperty(iParameterPrefix + "messages.notMatchingRuleCourse", "No {rule} option.")
									.replace("{rule}", rule.getRuleName())
									.replace("{course}", course.getCourseName()),
									ORD_UNITIME);
							RequestPriority rp = request.getRequestPriority(new RequestedCourse(course.getCourseId(), course.getCourseName()));
							if (rp != null)
								rp.getRequest().getRequestedCourse(rp.getChoice()).setInactive(true);
						}
					}
				}
			}
		} else {
			String filter = server.getConfig().getProperty("Load.OnlineOnlyStudentFilter", null);
			if (filter != null && !filter.isEmpty()) {
				if (new Query(filter).match(new StudentMatcher(original, server.getAcademicSession().getDefaultSectioningStatus(), server, false))) {
					// online only
					String cn = server.getConfig().getProperty("Load.OnlineOnlyCourseNameRegExp");
					String im = server.getConfig().getProperty("Load.OnlineOnlyInstructionalModeRegExp");
					for (XRequest r: original.getRequests()) {
						if (r instanceof XCourseRequest) {
							XCourseRequest cr = (XCourseRequest)r;
							for (XCourseId course: cr.getCourseIds()) {
								if (cn != null && !cn.isEmpty() && !course.getCourseName().matches(cn)) {
									request.addConfirmationMessage(course.getCourseId(), course.getCourseName(), "NOT-ONLINE",
											ApplicationProperties.getProperty(iParameterPrefix + "messages.onlineStudentReqResidentialCourse", "No online-only option.").replace("{course}", course.getCourseName()),
											ORD_UNITIME);
									RequestPriority rp = request.getRequestPriority(new RequestedCourse(course.getCourseId(), course.getCourseName()));
									if (rp != null)
										rp.getRequest().getRequestedCourse(rp.getChoice()).setInactive(true);
								} else if (im != null) {
									boolean hasMatchingConfig = false;
									InstructionalOffering offering = InstructionalOfferingDAO.getInstance().get(course.getOfferingId(), helper.getHibSession());
									if (offering != null)
										for (InstrOfferingConfig config: offering.getInstrOfferingConfigs()) {
											InstructionalMethod configIm = config.getEffectiveInstructionalMethod();
											if (im.isEmpty()) {
						        				if (configIm == null || configIm.getReference() == null || configIm.getReference().isEmpty())
						        					hasMatchingConfig = true;
						        			} else {
						        				if (configIm != null && configIm.getReference() != null && configIm.getReference().matches(im)) {
						        					hasMatchingConfig = true;
						        				}
						        			}
										}
									if (!hasMatchingConfig) {
										request.addConfirmationMessage(course.getCourseId(), course.getCourseName(), "NOT-ONLINE",
												ApplicationProperties.getProperty(iParameterPrefix + "messages.onlineStudentReqResidentialCourse", "No online-only option.").replace("{course}", course.getCourseName()),
												ORD_UNITIME);
										RequestPriority rp = request.getRequestPriority(new RequestedCourse(course.getCourseId(), course.getCourseName()));
										if (rp != null)
											rp.getRequest().getRequestedCourse(rp.getChoice()).setInactive(true);
									}
								}
							}
						}
					}
				} else if (server.getConfig().getPropertyBoolean("Load.OnlineOnlyExclusiveCourses", false)) {
					// exclusive
					String cn = server.getConfig().getProperty("Load.OnlineOnlyCourseNameRegExp");
					String im = server.getConfig().getProperty("Load.ResidentialInstructionalModeRegExp");
					for (XRequest r: original.getRequests()) {
						if (r instanceof XCourseRequest) {
							XCourseRequest cr = (XCourseRequest)r;
							for (XCourseId course: cr.getCourseIds()) {
								if (cn != null && !cn.isEmpty() && course.getCourseName().matches(cn)) {
									request.addConfirmationMessage(course.getCourseId(), course.getCourseName(), "NOT-RESIDENTIAL",
											ApplicationProperties.getProperty(iParameterPrefix + "messages.residentialStudentReqOnlineCourse", "No residential option.").replace("{course}", course.getCourseName()),
											ORD_UNITIME);
									RequestPriority rp = request.getRequestPriority(new RequestedCourse(course.getCourseId(), course.getCourseName()));
									if (rp != null)
										rp.getRequest().getRequestedCourse(rp.getChoice()).setInactive(true);
								} else if (im != null) {
									boolean hasMatchingConfig = false;
									InstructionalOffering offering = InstructionalOfferingDAO.getInstance().get(course.getOfferingId(), helper.getHibSession());
									if (offering != null)
										for (InstrOfferingConfig config: offering.getInstrOfferingConfigs()) {
											InstructionalMethod configIm = config.getEffectiveInstructionalMethod();
											if (im.isEmpty()) {
						        				if (configIm == null || configIm.getReference() == null || configIm.getReference().isEmpty())
						        					hasMatchingConfig = true;
						        			} else {
						        				if (configIm != null && configIm.getReference() != null && configIm.getReference().matches(im)) {
						        					hasMatchingConfig = true;
						        				}
						        			}
										}
									if (!hasMatchingConfig) {
										request.addConfirmationMessage(course.getCourseId(), course.getCourseName(), "NOT-RESIDENTIAL",
												ApplicationProperties.getProperty(iParameterPrefix + "messages.residentialStudentReqOnlineCourse", "No residential option.").replace("{course}", course.getCourseName()),
												ORD_UNITIME);
										RequestPriority rp = request.getRequestPriority(new RequestedCourse(course.getCourseId(), course.getCourseName()));
										if (rp != null)
											rp.getRequest().getRequestedCourse(rp.getChoice()).setInactive(true);
									}
								}
							}
						}
					}
				}
			}
		}

		if (!(server instanceof DatabaseServer)) {
			Map<XSection, XCourseId> singleSections = new HashMap<XSection, XCourseId>();
			for (XRequest r: original.getRequests()) {
				if (r.isAlternative()) continue; // no alternate course requests
				if (r instanceof XCourseRequest) {
					XCourseRequest cr = (XCourseRequest)r;
					for (XCourseId course: cr.getCourseIds()) {
						XOffering offering = server.getOffering(course.getOfferingId());
						if (offering != null && offering.getConfigs().size() == 1) { // take only single config courses
							for (XSubpart subpart: offering.getConfigs().get(0).getSubparts()) {
								if (subpart.getSections().size() == 1) { // take only single section subparts
									XSection section = subpart.getSections().get(0);
									for (XSection other: singleSections.keySet()) {
										if (section.isOverlapping(offering.getDistributions(), other)) {
											request.addConfirmationMessage(course.getCourseId(), course.getCourseName(), "OVERLAP",
													ApplicationProperties.getProperty(iParameterPrefix + "messages.courseOverlaps", "Conflicts with {other}.").replace("{course}", course.getCourseName()).replace("{other}", singleSections.get(other).getCourseName()),
													ORD_UNITIME);
										}
									}
									if (cr.getCourseIds().size() == 1) {
										// remember section when there are no alternative courses provided
										singleSections.put(section, course);
									}
								}
							}
						}
						if (offering.hasInconsistentRequirements(original, cr, course, server.getAcademicSession()))
							request.addConfirmationMessage(course.getCourseId(), course.getCourseName(), "STUD_PREF",
									ApplicationProperties.getProperty(iParameterPrefix + "messages.inconsistentStudPref", "Not available due to preferences selected.").replace("{course}", course.getCourseName()),
									ORD_UNITIME);
					}
				}
			}
		}

		if (server instanceof DatabaseServer) {
			for (CourseRequestInterface.Request r: request.getCourses()) {
				if (r.hasRequestedCourse())
					for (RequestedCourse rc: r.getRequestedCourse()) {
						if (rc.getCourseId() != null && !rc.isReadOnly()) {
							CourseOffering co = CourseOfferingDAO.getInstance().get(rc.getCourseId(), helper.getHibSession());
							InstructionalOffering io = (co == null ? null : co.getInstructionalOffering());
							if (io == null || !io.hasSchedulingDisclaimer()) continue;
							if (io.hasMultipleSchedulingDisclaimers()) {
								for (InstrOfferingConfig config: io.getInstrOfferingConfigs()) {
									if (!config.hasSchedulingDisclaimer()) continue;
									if (rule != null && !rule.matchesInstructionalMethod(config.getInstructionalMethod())) continue;
									request.addConfirmationMessage(rc.getCourseId(),
											rc.getCourseName(),
											"DISCLAIMER",
											(config.getInstructionalMethod() == null ? "" : config.getInstructionalMethod().getLabel() + ": ") + config.getSchedulingDisclaimer(),
											ORD_UNITIME);
								}
							} else {
								request.addConfirmationMessage(rc.getCourseId(),
										rc.getCourseName(),
										"DISCLAIMER",
										io.getFirstSchedulingDisclaimer(),
										ORD_UNITIME);
							}
						}
					}
			}
			for (CourseRequestInterface.Request r: request.getAlternatives()) {
				if (r.hasRequestedCourse())
					for (RequestedCourse rc: r.getRequestedCourse()) {
						if (rc.getCourseId() != null && !rc.isReadOnly()) {
							CourseOffering co = CourseOfferingDAO.getInstance().get(rc.getCourseId(), helper.getHibSession());
							InstructionalOffering io = (co == null ? null : co.getInstructionalOffering());
							if (io == null || !io.hasSchedulingDisclaimer()) continue;
							if (io.hasMultipleSchedulingDisclaimers()) {
								for (InstrOfferingConfig config: io.getInstrOfferingConfigs()) {
									if (!config.hasSchedulingDisclaimer()) continue;
									if (rule != null && !rule.matchesInstructionalMethod(config.getInstructionalMethod())) continue;
									request.addConfirmationMessage(rc.getCourseId(),
											rc.getCourseName(),
											"DISCLAIMER",
											(config.getInstructionalMethod() == null ? "" : config.getInstructionalMethod().getLabel() + ": ") + config.getSchedulingDisclaimer(),
											ORD_UNITIME);
								}
							} else {
								request.addConfirmationMessage(rc.getCourseId(),
										rc.getCourseName(),
										"DISCLAIMER",
										io.getFirstSchedulingDisclaimer(),
										ORD_UNITIME);
							}
						}
					}
			}
		} else {
			for (CourseRequestInterface.Request r: request.getCourses()) {
				if (r.hasRequestedCourse())
					for (RequestedCourse rc: r.getRequestedCourse()) {
						if (rc.getCourseId() != null && !rc.isReadOnly()) {
							XCourse course = server.getCourse(rc.getCourseId());
							XOffering offering = (course == null ? null : server.getOffering(course.getOfferingId()));
							if (offering == null || !offering.hasSchedulingDisclaimer()) continue;
							if (offering.hasMultipleSchedulingDisclaimers()) {
								XCourseRequest cr = original.getRequestForCourse(course.getCourseId());
								XEnrollment enrollment = (cr == null ? null : cr.getEnrollment());
								for (XConfig config: offering.getConfigs()) {
									if (!config.hasSchedulingDisclaimer()) continue;
									if (rule != null && !rule.matchesInstructionalMethod(config.getInstructionalMethod())) continue;
									if (!isAvailable(original, offering, course, config, enrollment, rc)) continue;
									request.addConfirmationMessage(rc.getCourseId(),
											rc.getCourseName(),
											"DISCLAIMER",
											(config.getInstructionalMethod() == null ? "" : config.getInstructionalMethod().getLabel() + ": ") + config.getSchedulingDisclaimer(),
											ORD_UNITIME);
								}
							} else {
								request.addConfirmationMessage(rc.getCourseId(),
										rc.getCourseName(),
										"DISCLAIMER",
										offering.getFirstSchedulingDisclaimer(),
										ORD_UNITIME);
							}
						}
					}
			}
			for (CourseRequestInterface.Request r: request.getAlternatives()) {
				if (r.hasRequestedCourse())
					for (RequestedCourse rc: r.getRequestedCourse()) {
						if (rc.getCourseId() != null && !rc.isReadOnly()) {
							XCourse course = server.getCourse(rc.getCourseId());
							XOffering offering = (course == null ? null : server.getOffering(course.getOfferingId()));
							if (offering == null || !offering.hasSchedulingDisclaimer()) continue;
							if (offering.hasMultipleSchedulingDisclaimers()) {
								XCourseRequest cr = original.getRequestForCourse(course.getCourseId());
								XEnrollment enrollment = (cr == null ? null : cr.getEnrollment());
								for (XConfig config: offering.getConfigs()) {
									if (!config.hasSchedulingDisclaimer()) continue;
									if (rule != null && !rule.matchesInstructionalMethod(config.getInstructionalMethod())) continue;
									if (!isAvailable(original, offering, course, config, enrollment, rc)) continue;
									request.addConfirmationMessage(rc.getCourseId(),
											rc.getCourseName(),
											"DISCLAIMER",
											(config.getInstructionalMethod() == null ? "" : config.getInstructionalMethod().getLabel() + ": ") + config.getSchedulingDisclaimer(),
											ORD_UNITIME);
								}
							} else {
								request.addConfirmationMessage(rc.getCourseId(),
										rc.getCourseName(),
										"DISCLAIMER",
										offering.getFirstSchedulingDisclaimer(),
										ORD_UNITIME);
							}
						}
					}
			}
		}
	}

	@Override
	public boolean updateStudent(OnlineSectioningServer server, OnlineSectioningHelper helper, Student student, Builder action) throws SectioningException {
		return false;
	}

	@Override
	public boolean revalidateStudent(OnlineSectioningServer server, OnlineSectioningHelper helper, Student student, Builder action) throws SectioningException {
		return false;
	}
	
	protected static boolean isEnabledForStudentScheduling(XConfig config) {
		if (config.getSubparts().isEmpty()) return false;
		for (XSubpart subpart: config.getSubparts()) {
			if (!isEnabledForStudentScheduling(subpart)) return false;
		}
		return true;
	}
	
	protected static boolean isEnabledForStudentScheduling(XSubpart subpart) {
		for (XSection section: subpart.getSections()) {
			if (section.isEnabledForScheduling() && !section.isCancelled()) return true;
		}
		return false;
	}
	
	protected static boolean meetsRequiredPrefs(XConfig config, RequestedCourse rc) {
		if (rc == null || (!rc.hasRequiredIntructionalMethods() && !rc.hasRequiredClasses())) return true;
		if (rc.hasRequiredIntructionalMethods() && config.getInstructionalMethod() != null && 
				rc.isSelectedIntructionalMethod(config.getInstructionalMethod().getUniqueId(), true)) return true;
		if (rc.hasRequiredClasses()) {
			for (Preference p: rc.getSelectedClasses())
				if (p.isRequired() && config.getSection(p.getId()) != null) return true;
		}
		return false;
	}

	public static boolean isAvailable(XStudent student, XOffering offering, XCourse course, XConfig config, XEnrollment enrollment, RequestedCourse rc) {
		boolean hasMustBeUsed = false;
		boolean hasReservation = false;
		boolean allowDisabled = false;
		for (XReservation r: offering.getReservations()) {
			if (student != null && !r.isApplicable(student, course)) continue; // reservation does not apply to this student
			boolean mustBeUsed = (r.mustBeUsed() && (r.isAlwaysExpired() || !r.isExpired()));
			if (mustBeUsed && !hasMustBeUsed) {
				hasReservation = false; hasMustBeUsed = true; allowDisabled = false;
			}
			if (hasMustBeUsed && !mustBeUsed) continue; // student must use a reservation, but this one is not it
			if (r.isIncluded(offering, config.getConfigId(), null)) {
				hasReservation = true;
				if (r.isAllowDisabled()) allowDisabled = true;
			}
		}
		if (hasMustBeUsed && !hasReservation) return false;
		boolean hasConfig = (enrollment != null && config.getConfigId().equals(enrollment.getConfigId()));
		boolean hasCourse = (enrollment != null && course.getCourseId().equals(enrollment.getCourseId()));
		if (!hasCourse && !hasReservation && offering.getUnreservedSpace(null) <= 0) return false;
		if (!hasConfig && !hasReservation && offering.getUnreservedConfigSpace(config.getConfigId(), null) <= 0) return false;
		if (!hasConfig && !allowDisabled && !isEnabledForStudentScheduling(config)) return false;
		if (!hasConfig && !meetsRequiredPrefs(config, rc)) return false;
		return true;
	}

	@Override
	public void validate(OnlineSectioningServer server, OnlineSectioningHelper helper, CourseRequestInterface request, CheckCoursesResponse response) throws SectioningException {
		XStudent original = (request.getStudentId() == null ? null : server.getStudent(request.getStudentId()));
		if (original == null) throw new PageAccessException(MESSAGES.exceptionEnrollNotStudent(server.getAcademicSession().toString()));
		// Do not validate when validation is disabled
		if (!isValidationEnabled(server, helper, original)) return;

		Integer CONF_NONE = null;
		Integer CONF_UNITIME = 0;

		Set<Long> coursesWithNotAlt = new HashSet<Long>();
		for (XRequest r: original.getRequests()) {
			if (r instanceof XCourseRequest) {
				XCourseRequest cr = (XCourseRequest)r;
				if (cr.getCourseIds().size() == 1 && !cr.isAlternative()) coursesWithNotAlt.add(cr.getCourseIds().get(0).getCourseId());
			}
		}
		Set<Long> advisorCoursesNoAlt = new HashSet<Long>();
		if (original.hasAdvisorRequests() && isAdvisedNoAlts())
			for (XAdvisorRequest ar: original.getAdvisorRequests()) {
				int count = 0;
				for (XAdvisorRequest x: original.getAdvisorRequests()) {
					if (x.getPriority() == ar.getPriority()) count ++;
				}
				if (count == 1 && ar.getCourseId() != null) advisorCoursesNoAlt.add(ar.getCourseId().getCourseId());
			}
		else if (original.hasAdvisorRequests() && isWaitListNoAlts())
			for (XAdvisorRequest ar: original.getAdvisorRequests()) {
				if (ar.isWaitListOrNoSub() && !ar.isSubstitute()) {
					int count = 0;
					for (XAdvisorRequest x: original.getAdvisorRequests()) {
						if (x.getPriority() == ar.getPriority() && !x.isSubstitute()) count ++;
					}
					if (count == 1 && ar.getCourseId() != null) advisorCoursesNoAlt.add(ar.getCourseId().getCourseId());
				}
			}
		boolean questionNoAlt = false;
		for (CourseRequestInterface.Request r: request.getCourses()) {
			if (r.hasRequestedCourse() && r.getRequestedCourse().size() == 1) {
				RequestedCourse rc = r.getRequestedCourse(0);
				if (rc.getCourseId() != null && !rc.isReadOnly() && !advisorCoursesNoAlt.contains(rc.getCourseId()) && !request.hasCourse(rc.getParentCourseId())) {
					response.addMessage(rc.getCourseId(), rc.getCourseName(), "NO_ALT",
							ApplicationProperties.getProperty(iParameterPrefix + "messages.courseHasNoAlt", "No alternative course provided.").replace("{course}", rc.getCourseName()),
							!coursesWithNotAlt.contains(rc.getCourseId()) ? CONF_UNITIME : CONF_NONE);
					if (!coursesWithNotAlt.contains(rc.getCourseId())) {
						questionNoAlt = true;
					}
				}
			}
		}
		boolean questionParentSameReq = false;
		boolean questionParentSubstitute = false;
		boolean questionNoParent = false;
		for (CourseRequestInterface.Request r: request.getCourses()) {
			if (r.hasRequestedCourse() && r.getRequestedCourse().size() > 1) {
				for (RequestedCourse rc: r.getRequestedCourse()) {
					RequestedCourse parent = r.getCourse(rc.getParentCourseId());
					if (parent != null) {
						XCourseRequest cr = original.getRequestForCourse(rc.getCourseId());
						boolean alreadyExist = (cr != null && cr.hasCourse(rc.getParentCourseId()));
						response.addMessage(rc.getCourseId(), rc.getCourseName(), "PARENT_SAME_REQ",
								ApplicationProperties.getProperty(iParameterPrefix + "messages.parentSameRequest", "Associated course {parent} on the same request line.")
								.replace("{parent}", parent.getCourseName())
								.replace("{course}", rc.getCourseName()),
								!alreadyExist ? CONF_UNITIME : CONF_NONE);
						if (!alreadyExist) questionParentSameReq = true;
					}
				}
			}
			if (r.hasRequestedCourse()) {
				for (RequestedCourse rc: r.getRequestedCourse()) {
					CourseRequestInterface.Request a = request.getAlternativeForCourseId(rc.getParentCourseId());
					if (a != null) {
						RequestedCourse parent = a.getCourse(rc.getParentCourseId());
						XCourseRequest cr = original.getRequestForCourse(rc.getCourseId());
						XCourseRequest acr = original.getRequestForCourse(rc.getParentCourseId());
						boolean alreadyExist = (cr != null && acr != null && !cr.isAlternative() && acr.isAlternative());
						response.addMessage(rc.getCourseId(), rc.getCourseName(), "PARENT_SUBST",
								ApplicationProperties.getProperty(iParameterPrefix + "messages.parentSubstitute", "Associated course {parent} is requested as a substitute.")
								.replace("{parent}", parent.getCourseName())
								.replace("{course}", rc.getCourseName()),
								!alreadyExist ? CONF_UNITIME : CONF_NONE);
						if (!alreadyExist) questionParentSubstitute = true;
					}
				}
			}
			if (r.hasRequestedCourse()) {
				for (RequestedCourse rc: r.getRequestedCourse()) {
					if (rc.hasParentCourseId() && !request.hasCourse(rc.getParentCourseId())) {
						XCourseRequest cr = original.getRequestForCourse(rc.getCourseId());
						XCourseRequest acr = original.getRequestForCourse(rc.getParentCourseId());
						boolean alreadyExist = (cr != null && acr == null);
						XCourse parent = server.getCourse(rc.getParentCourseId());
						response.addMessage(rc.getCourseId(), rc.getCourseName(), "NO_PARENT",
								ApplicationProperties.getProperty(iParameterPrefix + "messages.noParent", "Depends on {parent}.")
								.replace("{parent}", parent.getCourseName())
								.replace("{course}", rc.getCourseName()),
								!alreadyExist ? CONF_UNITIME : CONF_NONE);
						if (!alreadyExist) questionNoParent = true;
					}
				}
			}
		}
		for (CourseRequestInterface.Request r: request.getAlternatives()) {
			if (r.hasRequestedCourse() && r.getRequestedCourse().size() > 1) {
				for (RequestedCourse rc: r.getRequestedCourse()) {
					RequestedCourse parent = r.getCourse(rc.getParentCourseId());
					if (parent != null) {
						XCourseRequest cr = original.getRequestForCourse(rc.getCourseId());
						boolean alreadyExist = (cr != null && cr.hasCourse(rc.getParentCourseId()));
						response.addMessage(rc.getCourseId(), rc.getCourseName(), "PARENT_SAME_REQ",
								ApplicationProperties.getProperty(iParameterPrefix + "messages.parentSameRequest", "Associated course {parent} on the same request line.")
								.replace("{parent}", parent.getCourseName())
								.replace("{course}", rc.getCourseName()),
								!alreadyExist ? CONF_UNITIME : CONF_NONE);
						if (!alreadyExist) questionParentSameReq = true;
					}
				}
			}
			if (r.hasRequestedCourse()) {
				for (RequestedCourse rc: r.getRequestedCourse()) {
					if (rc.hasParentCourseId() && !request.hasCourse(rc.getParentCourseId())) {
						XCourseRequest cr = original.getRequestForCourse(rc.getCourseId());
						XCourseRequest acr = original.getRequestForCourse(rc.getParentCourseId());
						boolean alreadyExist = (cr != null && acr == null);
						XCourse parent = server.getCourse(rc.getParentCourseId());
						response.addMessage(rc.getCourseId(), rc.getCourseName(), "NO_PARENT",
								ApplicationProperties.getProperty(iParameterPrefix + "messages.noParent", "Depends on {parent}.")
								.replace("{parent}", parent.getCourseName())
								.replace("{course}", rc.getCourseName()),
								!alreadyExist ? CONF_UNITIME : CONF_NONE);
						if (!alreadyExist) questionNoParent = true;
					}
				}
			}
		}

		boolean questionTimeConflict = false;
		boolean questionInconStuPref = false;
		if (!(server instanceof DatabaseServer)) {
			OnlineSectioningModel model = new OnlineSectioningModel(server.getConfig(), server.getOverExpectedCriterion());
			model.setDayOfWeekOffset(server.getAcademicSession().getDayOfWeekOffset());
			boolean linkedClassesMustBeUsed = server.getConfig().getPropertyBoolean("LinkedClasses.mustBeUsed", false);
			Assignment<Request, Enrollment> assignment = new AssignmentMap<Request, Enrollment>();

			org.cpsolver.studentsct.model.Student student = new org.cpsolver.studentsct.model.Student(request.getStudentId());
			student.setExternalId(original.getExternalId());
			student.setName(original.getName());
			student.setNeedShortDistances(original.hasAccomodation(server.getDistanceMetric().getShortDistanceAccommodationReference()));
			student.setAllowDisabled(original.isAllowDisabled());
			student.setClassFirstDate(original.getClassStartDate());
			student.setClassLastDate(original.getClassEndDate());
			student.setBackToBackPreference(original.getBackToBackPreference());
			student.setModalityPreference(original.getModalityPreference());
			Map<Long, Section> classTable = new HashMap<Long, Section>();
			Set<XDistribution> distributions = new HashSet<XDistribution>();
			boolean hasAssignment = false;
			for (XRequest reqest: original.getRequests()) {
				if (reqest instanceof XCourseRequest && ((XCourseRequest)reqest).getEnrollment() != null) {
					hasAssignment = true; break;
				}
			}
			Map<Long, Course> courseTable = new HashMap<Long, Course>();
			Map<Long, Long> parentCourses = new HashMap<Long, Long>();
			for (CourseRequestInterface.Request c: request.getCourses())
				FindAssignmentAction.addRequest(server, model, assignment, student, original, c, false, false, classTable, courseTable, parentCourses, distributions, hasAssignment, true, helper);
			for (CourseRequestInterface.Request c: request.getAlternatives())
				FindAssignmentAction.addRequest(server, model, assignment, student, original, c, true, false, classTable, courseTable, parentCourses, distributions, hasAssignment, true, helper);
			boolean hasParent = false;
			for (Map.Entry<Long, Long> e: parentCourses.entrySet()) {
				Course course = courseTable.get(e.getKey());
				Course parent = courseTable.get(e.getValue());
				if (course != null && parent != null) {
					course.setParent(parent);
					hasParent = true;
				}
			}
			if (hasParent && "true".equalsIgnoreCase(ApplicationProperties.getProperty(iParameterPrefix + "checkDependentCourses", "true")))
				model.addGlobalConstraint(new DependentCourses());
			if ("true".equalsIgnoreCase(ApplicationProperties.getProperty(iParameterPrefix + "checkUnavailabilitiesFromOtherSessions", "false"))) {
				if (server.getConfig().getPropertyBoolean("General.CheckUnavailabilitiesFromOtherSessions", false))
					GetInfo.fillInUnavailabilitiesFromOtherSessions(student, server, helper);
				else if (server.getConfig().getPropertyBoolean("General.CheckUnavailabilitiesFromOtherSessionsUsingDatabase", false))
					GetInfo.fillInUnavailabilitiesFromOtherSessionsUsingDatabase(student, server, helper);
			}
			model.addStudent(student);
			model.setStudentQuality(new StudentQuality(server.getDistanceMetric(), model.getProperties()));
			for (XDistribution link: distributions) {
				if (link.getDistributionType() == XDistributionType.LinkedSections) {
					List<Section> sections = new ArrayList<Section>();
					for (Long sectionId: link.getSectionIds()) {
						Section x = classTable.get(sectionId);
						if (x != null) sections.add(x);
					}
					if (sections.size() >= 2)
						model.addLinkedSections(linkedClassesMustBeUsed, sections);
				}
			}
			if ("true".equalsIgnoreCase(ApplicationProperties.getProperty(iParameterPrefix + "dummyReservation", "false"))) {
				for (Iterator<Request> e = student.getRequests().iterator(); e.hasNext();) {
					Request r = (Request)e.next();
					if (r instanceof CourseRequest) {
						CourseRequest cr = (CourseRequest)r;
						for (Course course: cr.getCourses()) {
							new OnlineReservation(XReservationType.Dummy.ordinal(), -3l, course.getOffering(), 5000, true, 1, true, true, true, true, true);
							continue;
						}
					}
				}
			}

			// Single section time conflict check
			Map<Section, Course> singleSections = new HashMap<Section, Course>();
			for (Iterator<Request> e = student.getRequests().iterator(); e.hasNext();) {
				Request r = (Request)e.next();
				if (r.isAlternative()) continue; // no alternate course requests
				if (r instanceof CourseRequest) {
					CourseRequest cr = (CourseRequest)r;
					for (Course course: cr.getCourses()) {
						if (course.getOffering().getConfigs().size() == 1) { // take only single config courses
							for (Subpart subpart: course.getOffering().getConfigs().get(0).getSubparts()) {
								if (subpart.getSections().size() == 1) { // take only single section subparts
									Section section = subpart.getSections().get(0);
									for (Section other: singleSections.keySet()) {
										if (section.isOverlapping(other)) {
											boolean confirm = (original.getRequestForCourse(course.getId()) == null || original.getRequestForCourse(singleSections.get(other).getId()) == null) && (cr.getCourses().size() == 1);
											response.addMessage(course.getId(), course.getName(), "OVERLAP",
													ApplicationProperties.getProperty(iParameterPrefix + "messages.courseOverlaps", "Conflicts with {other}.").replace("{course}", course.getName()).replace("{other}", singleSections.get(other).getName()),
													confirm ? CONF_UNITIME : CONF_NONE);
											if (confirm) questionTimeConflict = true;
										}
									}
									if (cr.getCourses().size() == 1) {
										// remember section when there are no alternative courses provided
										singleSections.put(section, course);
									}
								}
							}
						}
					}
				}
			}

			// Inconsistent requirements
			for (Iterator<Request> e = student.getRequests().iterator(); e.hasNext();) {
				Request r = (Request)e.next();
				if (r instanceof CourseRequest) {
					CourseRequest cr = (CourseRequest)r;
					for (Course course: cr.getCourses()) {
						if (SectioningRequest.hasInconsistentRequirements(cr, course.getId())) {
							boolean confirm = (original.getRequestForCourse(course.getId()) == null);
							response.addMessage(course.getId(), course.getName(), "STUD_PREF",
									ApplicationProperties.getProperty(iParameterPrefix + "messages.inconsistentStudPref", "Not available due to preferences selected.").replace("{course}", course.getName()),
									confirm ? CONF_UNITIME : CONF_NONE);
							if (confirm) questionInconStuPref = true;
						}
					}
				}
			}
		}

		// Check for critical course removals
		boolean questionDropCritical = false;
		boolean dropImportant = false, dropVital = false, dropCritical = false;
		for (XRequest r: original.getRequests()) {
			if (r instanceof XCourseRequest) {
				XCourseRequest cr = (XCourseRequest)r;
				if (cr.isCritical() && !cr.isAlternative() && !cr.getCourseIds().isEmpty()) {
					boolean hasCourse = false;
					for (XCourseId course: cr.getCourseIds()) {
						if (request.getRequestPriority(new RequestedCourse(course.getCourseId(), course.getCourseName())) != null) {
							hasCourse = true; break;
						}
					}
					if (!hasCourse) {
						XCourseId course = cr.getCourseIds().get(0);
						if (cr.getCritical() == 2) {
							response.addMessage(course.getCourseId(), course.getCourseName(), "DROP_CRIT",
									ApplicationProperties.getProperty(iParameterPrefix + "messages.courseDropCrit", "Important course has been removed.").replace("{course}", course.getCourseName()),
									CONF_UNITIME);
							dropImportant = true;
						} else if (cr.getCritical() == 3) {
							response.addMessage(course.getCourseId(), course.getCourseName(), "DROP_CRIT",
									ApplicationProperties.getProperty(iParameterPrefix + "messages.courseDropCrit", "Vital course has been removed.").replace("{course}", course.getCourseName()),
									CONF_UNITIME);
							dropVital = true;
						} else {
							response.addMessage(course.getCourseId(), course.getCourseName(), "DROP_CRIT",
									ApplicationProperties.getProperty(iParameterPrefix + "messages.courseDropCrit", "Critical course has been removed.").replace("{course}", course.getCourseName()),
									CONF_UNITIME);
							dropCritical = true;
						}
						questionDropCritical = true;
					}
				}
			}
		}

		// Check for missing critical courses that have been recommended by the advisor
		boolean questionMissingAdvisorCritical = false;
		boolean missCritical = false, missImportant = false, missVital = false;
		CourseDemand.Critical advCritical = CourseDemand.Critical.fromText(ApplicationProperty.AdvisorCourseRequestsAllowCritical.valueOfSession(server.getAcademicSession().getUniqueId()));
		if (original.hasAdvisorRequests()) {
			for (XAdvisorRequest ar: original.getAdvisorRequests()) {
				if (ar.getAlternative() == 0 && !ar.isSubstitute() && ar.isCritical() && ar.hasCourseId()) {
					RequestPriority arp = request.getRequestPriority(new RequestedCourse(ar.getCourseId().getCourseId(), ar.getCourseId().getCourseName()));
					if (arp == null || arp.isAlternative()) {
						boolean hasAlt = false;
						for (XAdvisorRequest alt: original.getAdvisorRequests()) {
							if (alt.getPriority() != ar.getPriority() || !alt.hasCourseId() || alt.isSubstitute() || !alt.isCritical() || ar.getAlternative() == 0) continue;
							RequestPriority altrp = request.getRequestPriority(new RequestedCourse(alt.getCourseId().getCourseId(), alt.getCourseId().getCourseName()));
							if (altrp != null && !altrp.isAlternative()) {
								hasAlt = true; break;
							}
						}
						if (!hasAlt) {
							if (advCritical == Critical.IMPORTANT || ar.getCritical() == 2) {
								response.addMessage(ar.getCourseId().getCourseId(), ar.getCourseId().getCourseName(), "DROP_CRIT",
										ApplicationProperties.getProperty(iParameterPrefix + "messages.courseMissingAdvisedCritical", "Missing important course that has been recommended by the advisor.").replace("{course}", ar.getCourseId().getCourseName()),
										CONF_UNITIME);
								missImportant = true;
							} else if (advCritical == Critical.VITAL || ar.getCritical() == 3) {
								response.addMessage(ar.getCourseId().getCourseId(), ar.getCourseId().getCourseName(), "DROP_CRIT",
										ApplicationProperties.getProperty(iParameterPrefix + "messages.courseMissingAdvisedCritical", "Missing vital course that has been recommended by the advisor.").replace("{course}", ar.getCourseId().getCourseName()),
										CONF_UNITIME);
								missVital = true;
							} else {
								response.addMessage(ar.getCourseId().getCourseId(), ar.getCourseId().getCourseName(), "DROP_CRIT",
										ApplicationProperties.getProperty(iParameterPrefix + "messages.courseMissingAdvisedCritical", "Missing critical course that has been recommended by the advisor.").replace("{course}", ar.getCourseId().getCourseName()),
										CONF_UNITIME);
								missCritical = true;
							}
							questionMissingAdvisorCritical = true;
						}
					}
				}
			}
		}

		boolean questionRestrictionsNotMet = false;
		XSchedulingRule rule = server.getSchedulingRule(original,
				StudentSchedulingRule.Mode.Online,
				helper.hasAvisorPermission(),
				helper.hasAdminPermission());
		boolean onlineOnly = false;
		if (rule != null) {
			for (CourseRequestInterface.Request r: request.getCourses()) {
				if (r.hasRequestedCourse())
					for (RequestedCourse course: r.getRequestedCourse()) {
						if (course.getCourseId() == null) continue;
						CourseOffering co = CourseOfferingDAO.getInstance().get(course.getCourseId(), helper.getHibSession());
						if (co != null && !rule.matchesCourse(co)) {
							boolean confirm = (original.getRequestForCourse(course.getCourseId()) == null);
							response.addMessage(course.getCourseId(), course.getCourseName(), "NOT-RULE",
									ApplicationProperties.getProperty(iParameterPrefix + "messages.notMatchingRuleCourse", "No {rule} option.")
									.replace("{rule}", rule.getRuleName())
									.replace("{course}", course.getCourseName()),
									confirm ? CONF_UNITIME : CONF_NONE);
							if (confirm) questionRestrictionsNotMet = true;
						}
					}
			}
		} else {
			String filter = server.getConfig().getProperty("Load.OnlineOnlyStudentFilter", null);
			if (filter != null && !filter.isEmpty()) {
				if (new Query(filter).match(new StudentMatcher(original, server.getAcademicSession().getDefaultSectioningStatus(), server, false))) {
					// online only
					onlineOnly = true;
					String cn = server.getConfig().getProperty("Load.OnlineOnlyCourseNameRegExp");
					String im = server.getConfig().getProperty("Load.OnlineOnlyInstructionalModeRegExp");
					for (CourseRequestInterface.Request r: request.getCourses()) {
						if (r.hasRequestedCourse())
							for (RequestedCourse course: r.getRequestedCourse()) {
								if (course.getCourseId() == null) continue;
								if (cn != null && !cn.isEmpty() && !course.getCourseName().matches(cn)) {
									boolean confirm = (original.getRequestForCourse(course.getCourseId()) == null);
									response.addMessage(course.getCourseId(), course.getCourseName(), "NOT-ONLINE",
											ApplicationProperties.getProperty(iParameterPrefix + "messages.onlineStudentReqResidentialCourse", "No online-only option.").replace("{course}", course.getCourseName()),
											confirm ? CONF_UNITIME : CONF_NONE);
									if (confirm) questionRestrictionsNotMet = true;
								} else if (im != null) {
									boolean hasMatchingConfig = false;
									CourseOffering co = CourseOfferingDAO.getInstance().get(course.getCourseId(), helper.getHibSession());
									if (co != null)
										for (InstrOfferingConfig config: co.getInstructionalOffering().getInstrOfferingConfigs()) {
											InstructionalMethod configIm = config.getEffectiveInstructionalMethod();
											if (im.isEmpty()) {
						        				if (config.getInstructionalMethod() == null || configIm.getReference() == null || configIm.getReference().isEmpty())
						        					hasMatchingConfig = true;
						        			} else {
						        				if (configIm != null && configIm.getReference() != null && configIm.getReference().matches(im)) {
						        					hasMatchingConfig = true;
						        				}
						        			}
										}
									if (!hasMatchingConfig) {
										boolean confirm = (original.getRequestForCourse(course.getCourseId()) == null);
										response.addMessage(course.getCourseId(), course.getCourseName(), "NOT-ONLINE",
												ApplicationProperties.getProperty(iParameterPrefix + "messages.onlineStudentReqResidentialCourse", "No online-only option.").replace("{course}", course.getCourseName()),
												confirm ? CONF_UNITIME : CONF_NONE);
										if (confirm) questionRestrictionsNotMet = true;
									}
								}
							}
					}
					for (CourseRequestInterface.Request r: request.getAlternatives()) {
						if (r.hasRequestedCourse())
							for (RequestedCourse course: r.getRequestedCourse()) {
								if (course.getCourseId() == null) continue;
								if (cn != null && !cn.isEmpty() && !course.getCourseName().matches(cn)) {
									boolean confirm = (original.getRequestForCourse(course.getCourseId()) == null);
									response.addMessage(course.getCourseId(), course.getCourseName(), "NOT-ONLINE",
											ApplicationProperties.getProperty(iParameterPrefix + "messages.onlineStudentReqResidentialCourse", "No online-only option.").replace("{course}", course.getCourseName()),
											confirm ? CONF_UNITIME : CONF_NONE);
									if (confirm) questionRestrictionsNotMet = true;
								} else if (im != null) {
									boolean hasMatchingConfig = false;
									CourseOffering co = CourseOfferingDAO.getInstance().get(course.getCourseId(), helper.getHibSession());
									if (co != null)
										for (InstrOfferingConfig config: co.getInstructionalOffering().getInstrOfferingConfigs()) {
											InstructionalMethod configIm = config.getEffectiveInstructionalMethod();
											if (im.isEmpty()) {
						        				if (configIm == null || configIm.getReference() == null || configIm.getReference().isEmpty())
						        					hasMatchingConfig = true;
						        			} else {
						        				if (configIm != null && configIm.getReference() != null && configIm.getReference().matches(im)) {
						        					hasMatchingConfig = true;
						        				}
						        			}
										}
									if (!hasMatchingConfig) {
										boolean confirm = (original.getRequestForCourse(course.getCourseId()) == null);
										response.addMessage(course.getCourseId(), course.getCourseName(), "NOT-ONLINE",
												ApplicationProperties.getProperty(iParameterPrefix + "messages.onlineStudentReqResidentialCourse", "No online-only option.").replace("{course}", course.getCourseName()),
												confirm ? CONF_UNITIME : CONF_NONE);
										if (confirm) questionRestrictionsNotMet = true;
									}
								}
							}
					}
				} else if (server.getConfig().getPropertyBoolean("Load.OnlineOnlyExclusiveCourses", false)) {
					// exclusive
					String cn = server.getConfig().getProperty("Load.OnlineOnlyCourseNameRegExp");
					String im = server.getConfig().getProperty("Load.ResidentialInstructionalModeRegExp");
					for (CourseRequestInterface.Request r: request.getCourses()) {
						if (r.hasRequestedCourse())
							for (RequestedCourse course: r.getRequestedCourse()) {
								if (course.getCourseId() == null) continue;
								if (cn != null && !cn.isEmpty() && course.getCourseName().matches(cn)) {
									boolean confirm = (original.getRequestForCourse(course.getCourseId()) == null);
									response.addMessage(course.getCourseId(), course.getCourseName(), "NOT-RESIDENTIAL",
											ApplicationProperties.getProperty(iParameterPrefix + "messages.residentialStudentReqOnlineCourse", "No residential option.").replace("{course}", course.getCourseName()),
											confirm ? CONF_UNITIME : CONF_NONE);
									if (confirm) questionRestrictionsNotMet = true;
								} else if (im != null) {
									boolean hasMatchingConfig = false;
									CourseOffering co = CourseOfferingDAO.getInstance().get(course.getCourseId(), helper.getHibSession());
									if (co != null)
										for (InstrOfferingConfig config: co.getInstructionalOffering().getInstrOfferingConfigs()) {
											InstructionalMethod configIm = config.getEffectiveInstructionalMethod();
											if (im.isEmpty()) {
						        				if (configIm == null || configIm.getReference() == null || configIm.getReference().isEmpty())
						        					hasMatchingConfig = true;
						        			} else {
						        				if (configIm != null && configIm.getReference() != null && configIm.getReference().matches(im)) {
						        					hasMatchingConfig = true;
						        				}
						        			}
										}
									if (!hasMatchingConfig) {
										boolean confirm = (original.getRequestForCourse(course.getCourseId()) == null);
										response.addMessage(course.getCourseId(), course.getCourseName(), "NOT-RESIDENTIAL",
												ApplicationProperties.getProperty(iParameterPrefix + "messages.residentialStudentReqOnlineCourse", "No residential option.").replace("{course}", course.getCourseName()),
												confirm ? CONF_UNITIME : CONF_NONE);
										if (confirm) questionRestrictionsNotMet = true;
									}
								}
							}
					}
					for (CourseRequestInterface.Request r: request.getAlternatives()) {
						if (r.hasRequestedCourse())
							for (RequestedCourse course: r.getRequestedCourse()) {
								if (course.getCourseId() == null) continue;
								if (cn != null && !cn.isEmpty() && course.getCourseName().matches(cn)) {
									boolean confirm = (original.getRequestForCourse(course.getCourseId()) == null);
									response.addMessage(course.getCourseId(), course.getCourseName(), "NOT-RESIDENTIAL",
											ApplicationProperties.getProperty(iParameterPrefix + "messages.residentialStudentReqOnlineCourse", "No residential option.").replace("{course}", course.getCourseName()),
											confirm ? CONF_UNITIME : CONF_NONE);
									if (confirm) questionRestrictionsNotMet = true;
								} else if (im != null) {
									boolean hasMatchingConfig = false;
									CourseOffering co = CourseOfferingDAO.getInstance().get(course.getCourseId(), helper.getHibSession());
									if (co != null)
										for (InstrOfferingConfig config: co.getInstructionalOffering().getInstrOfferingConfigs()) {
											InstructionalMethod configIm = config.getEffectiveInstructionalMethod();
											if (im.isEmpty()) {
						        				if (configIm == null || configIm.getReference() == null || configIm.getReference().isEmpty())
						        					hasMatchingConfig = true;
						        			} else {
						        				if (configIm != null && configIm.getReference() != null && configIm.getReference().matches(im)) {
						        					hasMatchingConfig = true;
						        				}
						        			}
										}
									if (!hasMatchingConfig) {
										boolean confirm = (original.getRequestForCourse(course.getCourseId()) == null);
										response.addMessage(course.getCourseId(), course.getCourseName(), "NOT-RESIDENTIAL",
												ApplicationProperties.getProperty(iParameterPrefix + "messages.residentialStudentReqOnlineCourse", "No residential option.").replace("{course}", course.getCourseName()),
												confirm ? CONF_UNITIME : CONF_NONE);
										if (confirm) questionRestrictionsNotMet = true;
									}
								}
							}
					}
				}
			}
		}

		boolean questionFreeTime = false;
		for (int i = 0; i < request.getCourses().size(); i++) {
			CourseRequestInterface.Request r = request.getCourse(i);
			if (r.hasRequestedCourse() && r.getRequestedCourse(0).isFreeTime()) {
				boolean hasCourse = false;
				for (int j = i + 1; j < request.getCourses().size(); j++) {
					CourseRequestInterface.Request q = request.getCourse(j);
					if (q.hasRequestedCourse() && q.getRequestedCourse(0).isCourse()) {
						hasCourse = true;
					}
				}
				String free = "";
				boolean confirm = false;
				for (FreeTime ft: r.getRequestedCourse(0).getFreeTime()) {
					if (!free.isEmpty()) free += ", ";
					free += ft.toString(CONSTANTS.shortDays(), CONSTANTS.useAmPm());
					if (!confirm) {
						XFreeTimeRequest ftr = original.getRequestForFreeTime(ft);
						if (ftr == null) {
							confirm = true;
						} else if (hasCourse) {
							for (int j = i + 1; j < request.getCourses().size(); j++) {
								CourseRequestInterface.Request q = request.getCourse(j);
								if (q.hasRequestedCourse() && q.getRequestedCourse(0).isCourse()) {
									XCourseRequest cr = original.getRequestForCourse(q.getRequestedCourse(0).getCourseId());
									if (cr == null || cr.getPriority() < ftr.getPriority()) {
										confirm = true;
										break;
									}
								}
							}
						}
					}
				}
				if (hasCourse)
					response.addMessage(0l, CONSTANTS.freePrefix() + free, "FREE-TIME",
						ApplicationProperties.getProperty(iParameterPrefix + "messages.freeTimeHighPriority", "High priority free time"),
						confirm ? CONF_UNITIME : CONF_NONE);
				if (confirm) questionFreeTime = true;
			}
		}

		Set<Long> originalCourses = new HashSet<Long>();
		for (XRequest r: original.getRequests()) {
			if (r instanceof XCourseRequest) {
				for (XCourseId c: ((XCourseRequest)r).getCourseIds())
					originalCourses.add(c.getCourseId());
			}
		}
		boolean questionDisclaimer = false;
		if (server instanceof DatabaseServer) {
			for (CourseRequestInterface.Request r: request.getCourses()) {
				if (r.hasRequestedCourse())
					for (RequestedCourse rc: r.getRequestedCourse()) {
						if (rc.getCourseId() != null && !rc.isReadOnly()) {
							CourseOffering co = CourseOfferingDAO.getInstance().get(rc.getCourseId(), helper.getHibSession());
							InstructionalOffering io = (co == null ? null : co.getInstructionalOffering());
							if (io == null || !io.hasSchedulingDisclaimer()) continue;
							if (io.hasMultipleSchedulingDisclaimers()) {
								for (InstrOfferingConfig config: io.getInstrOfferingConfigs()) {
									if (!config.hasSchedulingDisclaimer()) continue;
									if (rule != null && !rule.matchesInstructionalMethod(config.getInstructionalMethod())) continue;
									response.addMessage(rc.getCourseId(),
											rc.getCourseName(),
											"DISCLAIMER",
											(config.getInstructionalMethod() == null ? "" : config.getInstructionalMethod().getLabel() + ": ") + config.getSchedulingDisclaimer(),
											originalCourses.contains(rc.getCourseId()) ? CONF_NONE : CONF_UNITIME);
									if (!originalCourses.contains(rc.getCourseId()))
										questionDisclaimer = true;
								}
							} else {
								response.addMessage(rc.getCourseId(),
										rc.getCourseName(),
										"DISCLAIMER",
										io.getFirstSchedulingDisclaimer(),
										originalCourses.contains(rc.getCourseId()) ? CONF_NONE : CONF_UNITIME);
								if (!originalCourses.contains(rc.getCourseId()))
									questionDisclaimer = true;
							}
						}
					}
			}
			for (CourseRequestInterface.Request r: request.getAlternatives()) {
				if (r.hasRequestedCourse())
					for (RequestedCourse rc: r.getRequestedCourse()) {
						if (rc.getCourseId() != null && !rc.isReadOnly()) {
							CourseOffering co = CourseOfferingDAO.getInstance().get(rc.getCourseId(), helper.getHibSession());
							InstructionalOffering io = (co == null ? null : co.getInstructionalOffering());
							if (io == null || !io.hasSchedulingDisclaimer()) continue;
							if (io.hasMultipleSchedulingDisclaimers()) {
								for (InstrOfferingConfig config: io.getInstrOfferingConfigs()) {
									if (!config.hasSchedulingDisclaimer()) continue;
									if (rule != null && !rule.matchesInstructionalMethod(config.getInstructionalMethod())) continue;
									response.addMessage(rc.getCourseId(),
											rc.getCourseName(),
											"DISCLAIMER",
											(config.getInstructionalMethod() == null ? "" : config.getInstructionalMethod().getLabel() + ": ") + config.getSchedulingDisclaimer(),
											originalCourses.contains(rc.getCourseId()) ? CONF_NONE : CONF_UNITIME);
									if (!originalCourses.contains(rc.getCourseId()))
										questionDisclaimer = true;
								}
							} else {
								response.addMessage(rc.getCourseId(),
										rc.getCourseName(),
										"DISCLAIMER",
										io.getFirstSchedulingDisclaimer(),
										originalCourses.contains(rc.getCourseId()) ? CONF_NONE : CONF_UNITIME);
								if (!originalCourses.contains(rc.getCourseId()))
									questionDisclaimer = true;
							}
						}
					}
			}
		} else {
			for (CourseRequestInterface.Request r: request.getCourses()) {
				if (r.hasRequestedCourse())
					for (RequestedCourse rc: r.getRequestedCourse()) {
						if (rc.getCourseId() != null && !rc.isReadOnly()) {
							XCourse course = server.getCourse(rc.getCourseId());
							XOffering offering = (course == null ? null : server.getOffering(course.getOfferingId()));
							if (offering == null || !offering.hasSchedulingDisclaimer()) continue;
							if (offering.hasMultipleSchedulingDisclaimers()) {
								XCourseRequest cr = original.getRequestForCourse(course.getCourseId());
								XEnrollment enrollment = (cr == null ? null : cr.getEnrollment());
								for (XConfig config: offering.getConfigs()) {
									if (!config.hasSchedulingDisclaimer()) continue;
									if (rule != null && !rule.matchesInstructionalMethod(config.getInstructionalMethod())) continue;
									if (!isAvailable(original, offering, course, config, enrollment, rc)) continue;
									response.addMessage(rc.getCourseId(),
											rc.getCourseName(),
											"DISCLAIMER",
											(config.getInstructionalMethod() == null ? "" : config.getInstructionalMethod().getLabel() + ": ") + config.getSchedulingDisclaimer(),
											originalCourses.contains(rc.getCourseId()) ? CONF_NONE : CONF_UNITIME);
									if (!originalCourses.contains(rc.getCourseId()))
										questionDisclaimer = true;
								}
							} else {
								response.addMessage(rc.getCourseId(),
										rc.getCourseName(),
										"DISCLAIMER",
										offering.getFirstSchedulingDisclaimer(),
										originalCourses.contains(rc.getCourseId()) ? CONF_NONE : CONF_UNITIME);
								if (!originalCourses.contains(rc.getCourseId()))
									questionDisclaimer = true;
							}
						}
					}
			}
			for (CourseRequestInterface.Request r: request.getAlternatives()) {
				if (r.hasRequestedCourse())
					for (RequestedCourse rc: r.getRequestedCourse()) {
						if (rc.getCourseId() != null && !rc.isReadOnly()) {
							XCourse course = server.getCourse(rc.getCourseId());
							XOffering offering = (course == null ? null : server.getOffering(course.getOfferingId()));
							if (offering == null || !offering.hasSchedulingDisclaimer()) continue;
							if (offering.hasMultipleSchedulingDisclaimers()) {
								XCourseRequest cr = original.getRequestForCourse(course.getCourseId());
								XEnrollment enrollment = (cr == null ? null : cr.getEnrollment());
								for (XConfig config: offering.getConfigs()) {
									if (!config.hasSchedulingDisclaimer()) continue;
									if (rule != null && !rule.matchesInstructionalMethod(config.getInstructionalMethod())) continue;
									if (!isAvailable(original, offering, course, config, enrollment, rc)) continue;
									response.addMessage(rc.getCourseId(),
											rc.getCourseName(),
											"DISCLAIMER",
											(config.getInstructionalMethod() == null ? "" : config.getInstructionalMethod().getLabel() + ": ") + config.getSchedulingDisclaimer(),
											originalCourses.contains(rc.getCourseId()) ? CONF_NONE : CONF_UNITIME);
									if (!originalCourses.contains(rc.getCourseId()))
										questionDisclaimer = true;
								}
							} else {
								response.addMessage(rc.getCourseId(),
										rc.getCourseName(),
										"DISCLAIMER",
										offering.getFirstSchedulingDisclaimer(),
										originalCourses.contains(rc.getCourseId()) ? CONF_NONE : CONF_UNITIME);
								if (!originalCourses.contains(rc.getCourseId()))
									questionDisclaimer = true;
							}
						}
					}
			}
		}



		String creditError = null;
		Float maxCredit = original.getMaxCredit();
		if (maxCredit == null) maxCredit = Float.parseFloat(ApplicationProperties.getProperty(iParameterPrefix + "maxCreditDefault", "18"));

		Set<Long> advisorWaitListedCourseIds = original.getAdvisorWaitListedCourseIds(server);
		if (maxCredit != null && request.getCredit(advisorWaitListedCourseIds) > maxCredit) {
			for (RequestedCourse rc: getOverCreditRequests(request, maxCredit)) {
				response.addMessage(rc.getCourseId(), rc.getCourseName(), "CREDIT",
						ApplicationProperties.getProperty(iParameterPrefix + "messages.maxCredit", "Maximum of {max} credit hours exceeded.").replace("{max}", sCreditFormat.format(maxCredit)).replace("{credit}", sCreditFormat.format(request.getCredit(advisorWaitListedCourseIds)))
						, CONF_NONE);
			}
			response.setCreditWarning(ApplicationProperties.getProperty(iParameterPrefix + "messages.maxCredit",
					"Maximum of {max} credit hours exceeded.")
					.replace("{max}", sCreditFormat.format(maxCredit)).replace("{credit}", sCreditFormat.format(request.getCredit(advisorWaitListedCourseIds))));
			response.setMaxCreditOverrideStatus(RequestedCourseStatus.CREDIT_HIGH);
			creditError = ApplicationProperties.getProperty(iParameterPrefix + "messages.maxCreditError",
					"Maximum of {max} credit hours exceeded.\nYou may not be able to get a full schedule.")
					.replace("{max}", sCreditFormat.format(maxCredit)).replace("{credit}", sCreditFormat.format(request.getCredit(advisorWaitListedCourseIds)));
		}

		String minCreditLimit = ApplicationProperties.getProperty(iParameterPrefix + "minCreditCheck");
		float minCredit = 0;
		for (CourseRequestInterface.Request r: request.getCourses()) {
			if (r.hasRequestedCourse()) {
				for (RequestedCourse rc: r.getRequestedCourse())
					if (rc.hasCredit()) {
						minCredit += rc.getCreditMin(); break;
					}
			}
		}
		if (creditError == null && minCreditLimit != null && minCredit < Float.parseFloat(minCreditLimit) && (maxCredit == null || maxCredit > Float.parseFloat(minCreditLimit))) {
			String minCreditLimitFilter = ApplicationProperties.getProperty(iParameterPrefix + "minCreditCheck.studentFilter");
			if (minCreditLimitFilter == null || minCreditLimitFilter.isEmpty() ||
					new Query(minCreditLimitFilter).match(new StudentMatcher(original, server.getAcademicSession().getDefaultSectioningStatus(), server, false))) {
				creditError = ApplicationProperties.getProperty(iParameterPrefix + "messages.minCredit",
						"Less than {min} credit hours requested.").replace("{min}", minCreditLimit).replace("{credit}", sCreditFormat.format(minCredit));
				response.setCreditWarning(
						ApplicationProperties.getProperty(iParameterPrefix + "messages.minCredit",
						"Less than {min} credit hours requested.").replace("{min}", minCreditLimit).replace("{credit}", sCreditFormat.format(minCredit))
						);
				response.setMaxCreditOverrideStatus(RequestedCourseStatus.CREDIT_LOW);
			}
		}


		if (response.getConfirms().contains(CONF_UNITIME)) {
			response.addConfirmation(ApplicationProperties.getProperty(iParameterPrefix + "messages.unitimeProblemsFound", "The following issues have been detected:"), CONF_UNITIME, -1);
			response.addConfirmation("", CONF_UNITIME, 1);
		}
		int line = 2;
		if (creditError != null) {
			response.addConfirmation(creditError, CONF_UNITIME, line++);
		}
		if (questionNoAlt)
			response.addConfirmation(ApplicationProperties.getProperty(iParameterPrefix + "messages.noAlternatives", (line > 2 ? "\n" : "") +
					"One or more of the newly requested courses have no alternatives provided. You may not be able to get a full schedule because you did not provide an alternative course."),
					CONF_UNITIME, line ++);

		if (questionParentSameReq)
			response.addConfirmation(ApplicationProperties.getProperty(iParameterPrefix + "messages.question.parentSameRequest", (line > 2 ? "\n" : "") +
					"A course and its associated course are on the same request line. You will not be able to get both courses."),
					CONF_UNITIME, line ++);
		if (questionParentSubstitute)
			response.addConfirmation(ApplicationProperties.getProperty(iParameterPrefix + "messages.question.parentSubstitute", (line > 2 ? "\n" : "") +
					"An associated course is requested as a substitute. You may not be able to get both courses."),
					CONF_UNITIME, line ++);
		if (questionNoParent)
			response.addConfirmation(ApplicationProperties.getProperty(iParameterPrefix + "messages.question.noParent", (line > 2 ? "\n" : "") +
					"A course depends on another course. You will not be able to get the course if you do not request the associated course unless you have already taken the associated course in the past."),
					CONF_UNITIME, line ++);

		if (questionTimeConflict)
			response.addConfirmation(ApplicationProperties.getProperty(iParameterPrefix + "messages.timeConflicts", (line > 2 ? "\n" : "") +
					"Two or more single section courses are conflicting with each other. You will likely not be able to get the conflicting course, so please provide an alternative course if possible."),
					CONF_UNITIME, line ++);

		if (questionInconStuPref)
			response.addConfirmation(ApplicationProperties.getProperty(iParameterPrefix + "messages.inconsistentStudPref", (line > 2 ? "\n" : "") +
					"One or more courses are not available due to the selected preferences."),
					CONF_UNITIME, line ++);

		if (questionDropCritical) {
			if (dropVital && !dropCritical && !dropImportant)
				response.addConfirmation(ApplicationProperties.getProperty(iParameterPrefix + "messages.dropCritical", (line > 2 ? "\n" : "") +
						"One or more vital courses have been removed. This may prohibit progress towards degree. Please consult with your academic advisor."),
						CONF_UNITIME, line ++);
			else if (dropImportant && !dropVital && !dropCritical)
				response.addConfirmation(ApplicationProperties.getProperty(iParameterPrefix + "messages.dropCritical", (line > 2 ? "\n" : "") +
						"One or more important courses have been removed. This may prohibit progress towards degree. Please consult with your academic advisor."),
						CONF_UNITIME, line ++);
			else if (advCritical != Critical.NORMAL)
				response.addConfirmation(ApplicationProperties.getProperty(iParameterPrefix + "messages.dropCritical", (line > 2 ? "\n" : "") +
						"One or more critical courses have been removed. This may prohibit progress towards degree. Please consult with your academic advisor."),
						CONF_UNITIME, line ++);
			else
				response.addConfirmation(ApplicationProperties.getProperty(iParameterPrefix + "messages.dropCritical", (line > 2 ? "\n" : "") +
						"One or more courses that are marked as critical in your degree plan have been removed. This may prohibit progress towards degree. Please consult with your academic advisor."),
						CONF_UNITIME, line ++);
		}

		if (questionMissingAdvisorCritical)
			if (advCritical == Critical.IMPORTANT || (missImportant && !missCritical && !missVital))
				response.addConfirmation(ApplicationProperties.getProperty(iParameterPrefix + "messages.missingAdvisedCritical", (line > 2 ? "\n" : "") +
						"One or more courses that are marked by your advisor as important have not been requested. This may prohibit progress towards degree. Please see you advisor course requests and/or consult with your academic advisor."),
						CONF_UNITIME, line ++);
			else if (advCritical == Critical.VITAL || (missVital && !missCritical && !missImportant))
				response.addConfirmation(ApplicationProperties.getProperty(iParameterPrefix + "messages.missingAdvisedCritical", (line > 2 ? "\n" : "") +
						"One or more courses that are marked by your advisor as vital have not been requested. This may prohibit progress towards degree. Please see you advisor course requests and/or consult with your academic advisor."),
						CONF_UNITIME, line ++);
			else if (advCritical == Critical.CRITICAL)
				response.addConfirmation(ApplicationProperties.getProperty(iParameterPrefix + "messages.missingAdvisedCritical", (line > 2 ? "\n" : "") +
						"One or more courses that are marked by your advisor as critical have not been requested. This may prohibit progress towards degree. Please see you advisor course requests and/or consult with your academic advisor."),
						CONF_UNITIME, line ++);
			else
				response.addConfirmation(ApplicationProperties.getProperty(iParameterPrefix + "messages.missingAdvisedCritical", (line > 2 ? "\n" : "") +
						"One or more courses that are marked as critical in your degree plan and that have been listed by your advisor have not been requested. This may prohibit progress towards degree. Please see you advisor course requests and/or consult with your academic advisor."),
						CONF_UNITIME, line ++);

		if (questionRestrictionsNotMet) {
			if (rule != null)
				response.addConfirmation(ApplicationProperties.getProperty(iParameterPrefix + "messages.ruleNotMet", (line > 2 ? "\n" : "") +
						"One or more of the newly requested courses have no {rule} option at the moment. You may not be able to get a full schedule because becasue you are not allowed to take these courses."
						.replace("{rule}", rule.getRuleName())),
						CONF_UNITIME, line ++);
			else if (onlineOnly)
				response.addConfirmation(ApplicationProperties.getProperty(iParameterPrefix + "messages.onlineOnlyNotMet", (line > 2 ? "\n" : "") +
					"One or more of the newly requested courses have no online-only option at the moment. You may not be able to get a full schedule because becasue you are not allowed to take these courses."),
					CONF_UNITIME, line ++);
			else
				response.addConfirmation(ApplicationProperties.getProperty(iParameterPrefix + "messages.residentialNotMet", (line > 2 ? "\n" : "") +
					"One or more of the newly requested courses have no residential option at the moment. You may not be able to get a full schedule because becasue you are not allowed to take these courses."),
					CONF_UNITIME, line ++);
		}
		if (questionFreeTime) {
			response.addConfirmation(ApplicationProperties.getProperty(iParameterPrefix + "messages.freeTimeRequested", (line > 2 ? "\n" : "") +
					"Free time requests will be considered as time blocks during the pre-registration process. When possible, classes should be avoided during free time. However, if a free time request is placed higher than a course, the course cannot be attended during free time and you may not receive a full schedule."),
					CONF_UNITIME, line ++);
		}

		if (line > 2 || questionDisclaimer)
			response.addConfirmation(ApplicationProperties.getProperty(iParameterPrefix + "messages.confirmation", "\nDo you want to proceed?"), CONF_UNITIME, line ++);

		Set<Integer> conf = response.getConfirms();
		if (conf.contains(CONF_UNITIME)) {
		response.setConfirmation(CONF_UNITIME, ApplicationProperties.getProperty(iParameterPrefix + "confirm.unitimeDialogName","Warning Confirmations"),
				(ApplicationProperties.getProperty(iParameterPrefix + "confirm.unitimeYesButton", "Accept & Submit")),
				ApplicationProperties.getProperty(iParameterPrefix + "confirm.unitimeNoButton", "Cancel Submit"),
				(ApplicationProperties.getProperty(iParameterPrefix + "confirm.unitimeYesButtonTitle", "Accept the above warning(s) and submit the Course Requests")),
				ApplicationProperties.getProperty(iParameterPrefix + "confirm.unitimeNoButtonTitle", "Go back to editing your Course Requests"));
		}
	}

	@Override
	public void submit(OnlineSectioningServer server, OnlineSectioningHelper helper, CourseRequestInterface request) throws SectioningException {
	}

	@Override
	public Collection<Long> updateStudents(OnlineSectioningServer server, OnlineSectioningHelper helper, List<Student> students) throws SectioningException {
		return null;
	}

	@Override
	public void dispose() {
	}

	protected boolean isAdvisorValidationEnabled(OnlineSectioningServer server, OnlineSectioningHelper helper, XStudent student, String status) {
		if (status == null) status = student.getStatus();
		else if (status.isEmpty()) status = server.getAcademicSession().getDefaultSectioningStatus();
		if (status == null) status = server.getAcademicSession().getDefaultSectioningStatus();
		if (status == null) return true;
		StudentSectioningStatus dbStatus = StudentSectioningStatus.getPresentStatus(status, server.getAcademicSession().getUniqueId(), helper.getHibSession());
		return dbStatus != null && dbStatus.hasOption(StudentSectioningStatus.Option.reqval) || dbStatus.hasOption(StudentSectioningStatus.Option.specreg);
	}

	@Override
	public void validateAdvisorRecommendations(OnlineSectioningServer server, OnlineSectioningHelper helper, AdvisingStudentDetails details, CheckCoursesResponse response) throws SectioningException {
		XStudent original = (details.getStudentId() == null ? null : server.getStudent(details.getStudentId()));
		if (original == null) throw new PageAccessException(MESSAGES.exceptionEnrollNotStudent(server.getAcademicSession().toString()));
		// Do not validate when validation is disabled
		if (!isAdvisorValidationEnabled(server, helper, original, details.getStatus() == null ? null : details.getStatus().getReference())) return;
		CourseRequestInterface request = details.getRequest();

		Integer CONF_UNITIME = 0;

		Set<Long> courseIds = new HashSet<Long>();
		for (CourseRequestInterface.Request r: request.getCourses()) {
			if (r.hasRequestedCourse())
				for (RequestedCourse rc: r.getRequestedCourse()) {
					if (rc.hasCourseId() && !courseIds.add(rc.getCourseId())) {
						response.addError(rc.getCourseId(), rc.getCourseName(), "DUPL",
								ApplicationProperties.getProperty(iParameterPrefix + "messages.duplicateCourse", "Course {course} used multiple times.").replace("{course}", rc.getCourseName())
								);
						if (!response.hasErrorMessage())
							response.setErrorMessage(ApplicationProperties.getProperty(iParameterPrefix + "messages.duplicateCourse", "Course {course} used multiple times.").replace("{course}", rc.getCourseName()));
					}
				}
		}

		boolean questionNoAlt = false;
		if (!isAdvisedNoAlts())
			for (CourseRequestInterface.Request r: request.getCourses()) {
				if (r.hasRequestedCourse() && r.getRequestedCourse().size() == 1) {
					if ((r.isWaitList() || r.isNoSub()) && isWaitListNoAlts()) continue;
					RequestedCourse rc = r.getRequestedCourse(0);
					if (rc.getCourseId() != null && !rc.isReadOnly() && !request.hasCourse(rc.getParentCourseId())) {
						response.addMessage(rc.getCourseId(), rc.getCourseName(), "NO_ALT",
								ApplicationProperties.getProperty(iParameterPrefix + "messages.courseHasNoAlt", "No alternative course provided.").replace("{course}", rc.getCourseName()),
								CONF_UNITIME);
						questionNoAlt = true;
					}
				}
			}

		boolean questionParentSameReq = false;
		boolean questionParentSubstitute = false;
		boolean questionNoParent = false;
		for (CourseRequestInterface.Request r: request.getCourses()) {
			if (r.hasRequestedCourse() && r.getRequestedCourse().size() > 1) {
				for (RequestedCourse rc: r.getRequestedCourse()) {
					RequestedCourse parent = r.getCourse(rc.getParentCourseId());
					if (parent != null) {
						response.addMessage(rc.getCourseId(), rc.getCourseName(), "PARENT_SAME_REQ",
								ApplicationProperties.getProperty(iParameterPrefix + "messages.parentSameRequest", "Associated course {parent} on the same request line.")
								.replace("{parent}", parent.getCourseName())
								.replace("{course}", rc.getCourseName()),
								CONF_UNITIME);
						questionParentSameReq = true;
					}
				}
			}
			if (r.hasRequestedCourse()) {
				for (RequestedCourse rc: r.getRequestedCourse()) {
					CourseRequestInterface.Request a = request.getAlternativeForCourseId(rc.getParentCourseId());
					if (a != null) {
						RequestedCourse parent = a.getCourse(rc.getParentCourseId());
						response.addMessage(rc.getCourseId(), rc.getCourseName(), "PARENT_SUBST",
								ApplicationProperties.getProperty(iParameterPrefix + "messages.parentSubstitute", "Associated course {parent} is requested as a substitute.")
								.replace("{parent}", parent.getCourseName())
								.replace("{course}", rc.getCourseName()),
								CONF_UNITIME);
						questionParentSubstitute = true;
					}
				}
			}
			if (r.hasRequestedCourse()) {
				for (RequestedCourse rc: r.getRequestedCourse()) {
					if (rc.hasParentCourseId() && !request.hasCourse(rc.getParentCourseId())) {
						XCourse parent = server.getCourse(rc.getParentCourseId());
						response.addMessage(rc.getCourseId(), rc.getCourseName(), "NO_PARENT",
								ApplicationProperties.getProperty(iParameterPrefix + "messages.noParent", "Depends on {parent}.")
								.replace("{parent}", parent.getCourseName())
								.replace("{course}", rc.getCourseName()),
								CONF_UNITIME);
						questionNoParent = true;
					}
				}
			}
		}
		for (CourseRequestInterface.Request r: request.getAlternatives()) {
			if (r.hasRequestedCourse() && r.getRequestedCourse().size() > 1) {
				for (RequestedCourse rc: r.getRequestedCourse()) {
					RequestedCourse parent = r.getCourse(rc.getParentCourseId());
					if (parent != null) {
						response.addMessage(rc.getCourseId(), rc.getCourseName(), "PARENT_SAME_REQ",
								ApplicationProperties.getProperty(iParameterPrefix + "messages.parentSameRequest", "Associated course {parent} on the same request line.")
								.replace("{parent}", parent.getCourseName())
								.replace("{course}", rc.getCourseName()),
								CONF_UNITIME);
						questionParentSameReq = true;
					}
				}
			}
			if (r.hasRequestedCourse()) {
				for (RequestedCourse rc: r.getRequestedCourse()) {
					if (rc.hasParentCourseId() && !request.hasCourse(rc.getParentCourseId())) {
						XCourse parent = server.getCourse(rc.getParentCourseId());
						response.addMessage(rc.getCourseId(), rc.getCourseName(), "NO_PARENT",
								ApplicationProperties.getProperty(iParameterPrefix + "messages.noParent", "Depends on {parent}.")
								.replace("{parent}", parent.getCourseName())
								.replace("{course}", rc.getCourseName()),
								CONF_UNITIME);
						questionNoParent = true;
					}
				}
			}
		}

		boolean questionTimeConflict = false;
		boolean questionInconStuPref = false;
		if (!(server instanceof DatabaseServer)) {
			OnlineSectioningModel model = new OnlineSectioningModel(server.getConfig(), server.getOverExpectedCriterion());
			model.setDayOfWeekOffset(server.getAcademicSession().getDayOfWeekOffset());
			boolean linkedClassesMustBeUsed = server.getConfig().getPropertyBoolean("LinkedClasses.mustBeUsed", false);
			Assignment<Request, Enrollment> assignment = new AssignmentMap<Request, Enrollment>();

			org.cpsolver.studentsct.model.Student student = new org.cpsolver.studentsct.model.Student(request.getStudentId());
			student.setExternalId(original.getExternalId());
			student.setName(original.getName());
			student.setNeedShortDistances(original.hasAccomodation(server.getDistanceMetric().getShortDistanceAccommodationReference()));
			student.setAllowDisabled(original.isAllowDisabled());
			student.setClassFirstDate(original.getClassStartDate());
			student.setClassLastDate(original.getClassEndDate());
			student.setBackToBackPreference(original.getBackToBackPreference());
			student.setModalityPreference(original.getModalityPreference());
			Map<Long, Section> classTable = new HashMap<Long, Section>();
			Set<XDistribution> distributions = new HashSet<XDistribution>();
			boolean hasAssignment = false;
			for (XRequest reqest: original.getRequests()) {
				if (reqest instanceof XCourseRequest && ((XCourseRequest)reqest).getEnrollment() != null) {
					hasAssignment = true; break;
				}
			}
			Map<Long, Course> courseTable = new HashMap<Long, Course>();
			Map<Long, Long> parentCourses = new HashMap<Long, Long>();
			for (CourseRequestInterface.Request c: request.getCourses())
				FindAssignmentAction.addRequest(server, model, assignment, student, original, c, false, false, classTable, courseTable, parentCourses, distributions, hasAssignment, true, helper);
			for (CourseRequestInterface.Request c: request.getAlternatives())
				FindAssignmentAction.addRequest(server, model, assignment, student, original, c, true, false, classTable, courseTable, parentCourses, distributions, hasAssignment, true, helper);
			boolean hasParent = false;
			for (Map.Entry<Long, Long> e: parentCourses.entrySet()) {
				Course course = courseTable.get(e.getKey());
				Course parent = courseTable.get(e.getValue());
				if (course != null && parent != null) {
					course.setParent(parent);
					hasParent = true;
				}
			}
			if (hasParent && "true".equalsIgnoreCase(ApplicationProperties.getProperty(iParameterPrefix + "checkDependentCourses", "true")))
				model.addGlobalConstraint(new DependentCourses());
			if ("true".equalsIgnoreCase(ApplicationProperties.getProperty(iParameterPrefix + "checkUnavailabilitiesFromOtherSessions", "false"))) {
				if (server.getConfig().getPropertyBoolean("General.CheckUnavailabilitiesFromOtherSessions", false))
					GetInfo.fillInUnavailabilitiesFromOtherSessions(student, server, helper);
				else if (server.getConfig().getPropertyBoolean("General.CheckUnavailabilitiesFromOtherSessionsUsingDatabase", false))
					GetInfo.fillInUnavailabilitiesFromOtherSessionsUsingDatabase(student, server, helper);
			}
			model.addStudent(student);
			model.setStudentQuality(new StudentQuality(server.getDistanceMetric(), model.getProperties()));
			for (XDistribution link: distributions) {
				if (link.getDistributionType() == XDistributionType.LinkedSections) {
					List<Section> sections = new ArrayList<Section>();
					for (Long sectionId: link.getSectionIds()) {
						Section x = classTable.get(sectionId);
						if (x != null) sections.add(x);
					}
					if (sections.size() >= 2)
						model.addLinkedSections(linkedClassesMustBeUsed, sections);
				}
			}
			if ("true".equalsIgnoreCase(ApplicationProperties.getProperty(iParameterPrefix + "dummyReservation", "false"))) {
				for (Iterator<Request> e = student.getRequests().iterator(); e.hasNext();) {
					Request r = (Request)e.next();
					if (r instanceof CourseRequest) {
						CourseRequest cr = (CourseRequest)r;
						for (Course course: cr.getCourses()) {
							new OnlineReservation(XReservationType.Dummy.ordinal(), -3l, course.getOffering(), 5000, true, 1, true, true, true, true, true);
							continue;
						}
					}
				}
			}

			// Single section time conflict check
			Map<Section, Course> singleSections = new HashMap<Section, Course>();
			for (Iterator<Request> e = student.getRequests().iterator(); e.hasNext();) {
				Request r = (Request)e.next();
				if (r.isAlternative()) continue; // no alternate course requests
				if (r instanceof CourseRequest) {
					CourseRequest cr = (CourseRequest)r;
					for (Course course: cr.getCourses()) {
						if (course.getOffering().getConfigs().size() == 1) { // take only single config courses
							for (Subpart subpart: course.getOffering().getConfigs().get(0).getSubparts()) {
								if (subpart.getSections().size() == 1) { // take only single section subparts
									Section section = subpart.getSections().get(0);
									for (Section other: singleSections.keySet()) {
										if (section.isOverlapping(other)) {
											response.addMessage(course.getId(), course.getName(), "OVERLAP",
													ApplicationProperties.getProperty(iParameterPrefix + "messages.courseOverlaps", "Conflicts with {other}.").replace("{course}", course.getName()).replace("{other}", singleSections.get(other).getName()),
													CONF_UNITIME);
											questionTimeConflict = true;
										}
									}
									if (cr.getCourses().size() == 1) {
										// remember section when there are no alternative courses provided
										singleSections.put(section, course);
									}
								}
							}
						}
					}
				}
			}

			// Inconsistent requirements
			for (Iterator<Request> e = student.getRequests().iterator(); e.hasNext();) {
				Request r = (Request)e.next();
				if (r instanceof CourseRequest) {
					CourseRequest cr = (CourseRequest)r;
					for (Course course: cr.getCourses()) {
						if (SectioningRequest.hasInconsistentRequirements(cr, course.getId())) {
							response.addMessage(course.getId(), course.getName(), "STUD_PREF",
									ApplicationProperties.getProperty(iParameterPrefix + "messages.inconsistentStudPref", "Not available due to preferences selected.").replace("{course}", course.getName()),
									CONF_UNITIME);
							questionInconStuPref = true;
						}
					}
				}
			}
		}

		boolean questionRestrictionsNotMet = false;
		XSchedulingRule rule = server.getSchedulingRule(original,
				StudentSchedulingRule.Mode.Online,
				helper.hasAvisorPermission(),
				helper.hasAdminPermission());
		boolean onlineOnly = false;
		if (rule != null) {
			for (CourseRequestInterface.Request r: request.getCourses()) {
				if (r.hasRequestedCourse())
					for (RequestedCourse course: r.getRequestedCourse()) {
						if (course.getCourseId() == null) continue;
						CourseOffering co = CourseOfferingDAO.getInstance().get(course.getCourseId(), helper.getHibSession());
						if (co != null && !rule.matchesCourse(co)) {
							response.addMessage(course.getCourseId(), course.getCourseName(), "NOT-RULE",
									ApplicationProperties.getProperty(iParameterPrefix + "messages.notMatchingRuleCourse", "No {rule} option.")
									.replace("{rule}", rule.getRuleName())
									.replace("{course}", course.getCourseName()),
									CONF_UNITIME);
							questionRestrictionsNotMet = true;
						}
					}
			}
		} else {
			String filter = server.getConfig().getProperty("Load.OnlineOnlyStudentFilter", null);
			if (filter != null && !filter.isEmpty()) {
				if (new Query(filter).match(new StudentMatcher(original, server.getAcademicSession().getDefaultSectioningStatus(), server, false))) {
					// online only
					onlineOnly = true;
					String cn = server.getConfig().getProperty("Load.OnlineOnlyCourseNameRegExp");
					String im = server.getConfig().getProperty("Load.OnlineOnlyInstructionalModeRegExp");
					for (CourseRequestInterface.Request r: request.getCourses()) {
						if (r.hasRequestedCourse())
							for (RequestedCourse course: r.getRequestedCourse()) {
								if (course.getCourseId() == null) continue;
								if (cn != null && !cn.isEmpty() && !course.getCourseName().matches(cn)) {
									response.addMessage(course.getCourseId(), course.getCourseName(), "NOT-ONLINE",
											ApplicationProperties.getProperty(iParameterPrefix + "messages.onlineStudentReqResidentialCourse", "No online-only option.").replace("{course}", course.getCourseName()),
											CONF_UNITIME);
									questionRestrictionsNotMet = true;
								} else if (im != null) {
									boolean hasMatchingConfig = false;
									CourseOffering co = CourseOfferingDAO.getInstance().get(course.getCourseId(), helper.getHibSession());
									if (co != null)
										for (InstrOfferingConfig config: co.getInstructionalOffering().getInstrOfferingConfigs()) {
											InstructionalMethod configIm = config.getEffectiveInstructionalMethod();
											if (im.isEmpty()) {
						        				if (config.getInstructionalMethod() == null || configIm.getReference() == null || configIm.getReference().isEmpty())
						        					hasMatchingConfig = true;
						        			} else {
						        				if (configIm != null && configIm.getReference() != null && configIm.getReference().matches(im)) {
						        					hasMatchingConfig = true;
						        				}
						        			}
										}
									if (!hasMatchingConfig) {
										response.addMessage(course.getCourseId(), course.getCourseName(), "NOT-ONLINE",
												ApplicationProperties.getProperty(iParameterPrefix + "messages.onlineStudentReqResidentialCourse", "No online-only option.").replace("{course}", course.getCourseName()),
												CONF_UNITIME);
										questionRestrictionsNotMet = true;
									}
								}
							}
					}
					for (CourseRequestInterface.Request r: request.getAlternatives()) {
						if (r.hasRequestedCourse())
							for (RequestedCourse course: r.getRequestedCourse()) {
								if (course.getCourseId() == null) continue;
								if (cn != null && !cn.isEmpty() && !course.getCourseName().matches(cn)) {
									response.addMessage(course.getCourseId(), course.getCourseName(), "NOT-ONLINE",
											ApplicationProperties.getProperty(iParameterPrefix + "messages.onlineStudentReqResidentialCourse", "No online-only option.").replace("{course}", course.getCourseName()),
											CONF_UNITIME);
									questionRestrictionsNotMet = true;
								} else if (im != null) {
									boolean hasMatchingConfig = false;
									CourseOffering co = CourseOfferingDAO.getInstance().get(course.getCourseId(), helper.getHibSession());
									if (co != null)
										for (InstrOfferingConfig config: co.getInstructionalOffering().getInstrOfferingConfigs()) {
											InstructionalMethod configIm = config.getEffectiveInstructionalMethod();
											if (im.isEmpty()) {
						        				if (configIm == null || configIm.getReference() == null || configIm.getReference().isEmpty())
						        					hasMatchingConfig = true;
						        			} else {
						        				if (configIm != null && configIm.getReference() != null && configIm.getReference().matches(im)) {
						        					hasMatchingConfig = true;
						        				}
						        			}
										}
									if (!hasMatchingConfig) {
										response.addMessage(course.getCourseId(), course.getCourseName(), "NOT-ONLINE",
												ApplicationProperties.getProperty(iParameterPrefix + "messages.onlineStudentReqResidentialCourse", "No online-only option.").replace("{course}", course.getCourseName()),
												CONF_UNITIME);
										questionRestrictionsNotMet = true;
									}
								}
							}
					}
				} else if (server.getConfig().getPropertyBoolean("Load.OnlineOnlyExclusiveCourses", false)) {
					// exclusive
					String cn = server.getConfig().getProperty("Load.OnlineOnlyCourseNameRegExp");
					String im = server.getConfig().getProperty("Load.ResidentialInstructionalModeRegExp");
					for (CourseRequestInterface.Request r: request.getCourses()) {
						if (r.hasRequestedCourse())
							for (RequestedCourse course: r.getRequestedCourse()) {
								if (course.getCourseId() == null) continue;
								if (cn != null && !cn.isEmpty() && course.getCourseName().matches(cn)) {
									response.addMessage(course.getCourseId(), course.getCourseName(), "NOT-RESIDENTIAL",
											ApplicationProperties.getProperty(iParameterPrefix + "messages.residentialStudentReqOnlineCourse", "No residential option.").replace("{course}", course.getCourseName()),
											CONF_UNITIME);
									questionRestrictionsNotMet = true;
								} else if (im != null) {
									boolean hasMatchingConfig = false;
									CourseOffering co = CourseOfferingDAO.getInstance().get(course.getCourseId(), helper.getHibSession());
									if (co != null)
										for (InstrOfferingConfig config: co.getInstructionalOffering().getInstrOfferingConfigs()) {
											InstructionalMethod configIm = config.getEffectiveInstructionalMethod();
											if (im.isEmpty()) {
						        				if (configIm == null || configIm.getReference() == null || configIm.getReference().isEmpty())
						        					hasMatchingConfig = true;
						        			} else {
						        				if (configIm != null && configIm.getReference() != null && configIm.getReference().matches(im)) {
						        					hasMatchingConfig = true;
						        				}
						        			}
										}
									if (!hasMatchingConfig) {
										response.addMessage(course.getCourseId(), course.getCourseName(), "NOT-RESIDENTIAL",
												ApplicationProperties.getProperty(iParameterPrefix + "messages.residentialStudentReqOnlineCourse", "No residential option.").replace("{course}", course.getCourseName()),
												CONF_UNITIME);
										questionRestrictionsNotMet = true;
									}
								}
							}
					}
					for (CourseRequestInterface.Request r: request.getAlternatives()) {
						if (r.hasRequestedCourse())
							for (RequestedCourse course: r.getRequestedCourse()) {
								if (course.getCourseId() == null) continue;
								if (cn != null && !cn.isEmpty() && course.getCourseName().matches(cn)) {
									response.addMessage(course.getCourseId(), course.getCourseName(), "NOT-RESIDENTIAL",
											ApplicationProperties.getProperty(iParameterPrefix + "messages.residentialStudentReqOnlineCourse", "No residential option.").replace("{course}", course.getCourseName()),
											CONF_UNITIME);
									questionRestrictionsNotMet = true;
								} else if (im != null) {
									boolean hasMatchingConfig = false;
									CourseOffering co = CourseOfferingDAO.getInstance().get(course.getCourseId(), helper.getHibSession());
									if (co != null)
										for (InstrOfferingConfig config: co.getInstructionalOffering().getInstrOfferingConfigs()) {
											InstructionalMethod configIm = config.getEffectiveInstructionalMethod();
											if (im.isEmpty()) {
						        				if (configIm == null || configIm.getReference() == null || configIm.getReference().isEmpty())
						        					hasMatchingConfig = true;
						        			} else {
						        				if (configIm != null && configIm.getReference() != null && configIm.getReference().matches(im)) {
						        					hasMatchingConfig = true;
						        				}
						        			}
										}
									if (!hasMatchingConfig) {
										response.addMessage(course.getCourseId(), course.getCourseName(), "NOT-RESIDENTIAL",
												ApplicationProperties.getProperty(iParameterPrefix + "messages.residentialStudentReqOnlineCourse", "No residential option.").replace("{course}", course.getCourseName()),
												CONF_UNITIME);
										questionRestrictionsNotMet = true;
									}
								}
							}
					}
				}
			}
		}


		boolean questionFreeTime = false;
		for (int i = 0; i < request.getCourses().size(); i++) {
			CourseRequestInterface.Request r = request.getCourse(i);
			if (r.hasRequestedCourse() && r.getRequestedCourse(0).isFreeTime()) {
				boolean hasCourse = false;
				for (int j = i + 1; j < request.getCourses().size(); j++) {
					CourseRequestInterface.Request q = request.getCourse(j);
					if (q.hasRequestedCourse() && q.getRequestedCourse(0).hasCourseId()) {
						hasCourse = true;
					}
				}
				String free = "";
				for (FreeTime ft: r.getRequestedCourse(0).getFreeTime()) {
					if (!free.isEmpty()) free += ", ";
					free += ft.toString(CONSTANTS.shortDays(), CONSTANTS.useAmPm());
				}
				if (hasCourse)
					response.addMessage(0l, CONSTANTS.freePrefix() + free, "FREE-TIME",
						ApplicationProperties.getProperty(iParameterPrefix + "messages.freeTimeHighPriority", "High priority free time"),
						CONF_UNITIME);
				questionFreeTime = true;
			}
		}

		boolean questionDisclaimer = false;
		if (server instanceof DatabaseServer) {
			for (CourseRequestInterface.Request r: request.getCourses()) {
				if (r.hasRequestedCourse())
					for (RequestedCourse rc: r.getRequestedCourse()) {
						if (rc.getCourseId() != null && !rc.isReadOnly()) {
							CourseOffering co = CourseOfferingDAO.getInstance().get(rc.getCourseId(), helper.getHibSession());
							InstructionalOffering io = (co == null ? null : co.getInstructionalOffering());
							if (io == null || !io.hasSchedulingDisclaimer()) continue;
							if (io.hasMultipleSchedulingDisclaimers()) {
								for (InstrOfferingConfig config: io.getInstrOfferingConfigs()) {
									if (!config.hasSchedulingDisclaimer()) continue;
									if (rule != null && !rule.matchesInstructionalMethod(config.getInstructionalMethod())) continue;
									response.addMessage(rc.getCourseId(),
											rc.getCourseName(),
											"DISCLAIMER",
											(config.getInstructionalMethod() == null ? "" : config.getInstructionalMethod().getLabel() + ": ") + config.getSchedulingDisclaimer(),
											CONF_UNITIME);
									questionDisclaimer = true;
								}
							} else {
								response.addMessage(rc.getCourseId(),
										rc.getCourseName(),
										"DISCLAIMER",
										io.getFirstSchedulingDisclaimer(),
										CONF_UNITIME);
								questionDisclaimer = true;
							}
						}
					}
			}
			for (CourseRequestInterface.Request r: request.getAlternatives()) {
				if (r.hasRequestedCourse())
					for (RequestedCourse rc: r.getRequestedCourse()) {
						if (rc.getCourseId() != null && !rc.isReadOnly()) {
							CourseOffering co = CourseOfferingDAO.getInstance().get(rc.getCourseId(), helper.getHibSession());
							InstructionalOffering io = (co == null ? null : co.getInstructionalOffering());
							if (io == null || !io.hasSchedulingDisclaimer()) continue;
							if (io.hasMultipleSchedulingDisclaimers()) {
								for (InstrOfferingConfig config: io.getInstrOfferingConfigs()) {
									if (!config.hasSchedulingDisclaimer()) continue;
									if (rule != null && !rule.matchesInstructionalMethod(config.getInstructionalMethod())) continue;
									response.addMessage(rc.getCourseId(),
											rc.getCourseName(),
											"DISCLAIMER",
											(config.getInstructionalMethod() == null ? "" : config.getInstructionalMethod().getLabel() + ": ") + config.getSchedulingDisclaimer(),
											CONF_UNITIME);
									questionDisclaimer = true;
								}
							} else {
								response.addMessage(rc.getCourseId(),
										rc.getCourseName(),
										"DISCLAIMER",
										io.getFirstSchedulingDisclaimer(),
										CONF_UNITIME);
								questionDisclaimer = true;
							}
						}
					}
			}
		} else {
			for (CourseRequestInterface.Request r: request.getCourses()) {
				if (r.hasRequestedCourse())
					for (RequestedCourse rc: r.getRequestedCourse()) {
						if (rc.getCourseId() != null && !rc.isReadOnly()) {
							XCourse course = server.getCourse(rc.getCourseId());
							XOffering offering = (course == null ? null : server.getOffering(course.getOfferingId()));
							if (offering == null || !offering.hasSchedulingDisclaimer()) continue;
							if (offering.hasMultipleSchedulingDisclaimers()) {
								XCourseRequest cr = original.getRequestForCourse(course.getCourseId());
								XEnrollment enrollment = (cr == null ? null : cr.getEnrollment());
								for (XConfig config: offering.getConfigs()) {
									if (!config.hasSchedulingDisclaimer()) continue;
									if (rule != null && !rule.matchesInstructionalMethod(config.getInstructionalMethod())) continue;
									if (!isAvailable(original, offering, course, config, enrollment, rc)) continue;
									response.addMessage(rc.getCourseId(),
											rc.getCourseName(),
											"DISCLAIMER",
											(config.getInstructionalMethod() == null ? "" : config.getInstructionalMethod().getLabel() + ": ") + config.getSchedulingDisclaimer(),
											CONF_UNITIME);
									questionDisclaimer = true;
								}
							} else {
								response.addMessage(rc.getCourseId(),
										rc.getCourseName(),
										"DISCLAIMER",
										offering.getFirstSchedulingDisclaimer(),
										CONF_UNITIME);
								questionDisclaimer = true;
							}
						}
					}
			}
			for (CourseRequestInterface.Request r: request.getAlternatives()) {
				if (r.hasRequestedCourse())
					for (RequestedCourse rc: r.getRequestedCourse()) {
						if (rc.getCourseId() != null && !rc.isReadOnly()) {
							XCourse course = server.getCourse(rc.getCourseId());
							XOffering offering = (course == null ? null : server.getOffering(course.getOfferingId()));
							if (offering == null || !offering.hasSchedulingDisclaimer()) continue;
							if (offering.hasMultipleSchedulingDisclaimers()) {
								XCourseRequest cr = original.getRequestForCourse(course.getCourseId());
								XEnrollment enrollment = (cr == null ? null : cr.getEnrollment());
								for (XConfig config: offering.getConfigs()) {
									if (!config.hasSchedulingDisclaimer()) continue;
									if (rule != null && !rule.matchesInstructionalMethod(config.getInstructionalMethod())) continue;
									if (!isAvailable(original, offering, course, config, enrollment, rc)) continue;
									response.addMessage(rc.getCourseId(),
											rc.getCourseName(),
											"DISCLAIMER",
											(config.getInstructionalMethod() == null ? "" : config.getInstructionalMethod().getLabel() + ": ") + config.getSchedulingDisclaimer(),
											CONF_UNITIME);
									questionDisclaimer = true;
								}
							} else {
								response.addMessage(rc.getCourseId(),
										rc.getCourseName(),
										"DISCLAIMER",
										offering.getFirstSchedulingDisclaimer(),
										CONF_UNITIME);
								questionDisclaimer = true;
							}
						}
					}
			}
		}

		String creditError = null;
		Float maxCredit = original.getMaxCredit();
		if (maxCredit == null) maxCredit = Float.parseFloat(ApplicationProperties.getProperty(iParameterPrefix + "maxCreditDefault", "18"));

		request.setWaitListMode(details.getWaitListMode());
		if (maxCredit != null && request.getCredit(null) > maxCredit) {
			for (RequestedCourse rc: getOverCreditRequests(request, maxCredit)) {
				response.addMessage(rc.getCourseId(), rc.getCourseName(), "CREDIT",
						ApplicationProperties.getProperty(iParameterPrefix + "messages.maxCredit", "Maximum of {max} credit hours exceeded.").replace("{max}", sCreditFormat.format(maxCredit)).replace("{credit}", sCreditFormat.format(request.getCredit(null)))
						, CONF_UNITIME);
			}
			response.setCreditWarning(ApplicationProperties.getProperty(iParameterPrefix + "messages.maxCredit",
					"Maximum of {max} credit hours exceeded.")
					.replace("{max}", sCreditFormat.format(maxCredit)).replace("{credit}", sCreditFormat.format(request.getCredit(null))));
			response.setMaxCreditOverrideStatus(RequestedCourseStatus.CREDIT_HIGH);
			creditError = ApplicationProperties.getProperty(iParameterPrefix + "messages.acr.maxCreditError",
					"Maximum of {max} credit hours exceeded.\nThe student may not be able to get a full schedule.")
					.replace("{max}", sCreditFormat.format(maxCredit)).replace("{credit}", sCreditFormat.format(request.getCredit(null)));
		}

		String minCreditLimit = ApplicationProperties.getProperty(iParameterPrefix + "minCreditCheck");
		float minCredit = 0;
		for (CourseRequestInterface.Request r: request.getCourses()) {
			if (r.hasAdvisorCredit()) {
				minCredit += r.getAdvisorCreditMin();
			} else if (r.hasRequestedCourse()) {
				for (RequestedCourse rc: r.getRequestedCourse())
					if (rc.hasCredit()) {
						minCredit += rc.getCreditMin(); break;
					}
			}
		}
		if (creditError == null && minCreditLimit != null && minCredit < Float.parseFloat(minCreditLimit) && (maxCredit == null || maxCredit > Float.parseFloat(minCreditLimit))) {
			String minCreditLimitFilter = ApplicationProperties.getProperty(iParameterPrefix + "minCreditCheck.studentFilter");
			if (minCreditLimitFilter == null || minCreditLimitFilter.isEmpty() ||
					new Query(minCreditLimitFilter).match(new StudentMatcher(original, server.getAcademicSession().getDefaultSectioningStatus(), server, false))) {
				creditError = ApplicationProperties.getProperty(iParameterPrefix + "messages.minCredit",
						"Less than {min} credit hours requested.").replace("{min}", minCreditLimit).replace("{credit}", sCreditFormat.format(minCredit));
				response.setCreditWarning(
						ApplicationProperties.getProperty(iParameterPrefix + "messages.minCredit",
						"Less than {min} credit hours requested.").replace("{min}", minCreditLimit).replace("{credit}", sCreditFormat.format(minCredit))
						);
				response.setMaxCreditOverrideStatus(RequestedCourseStatus.CREDIT_LOW);
			}
		}


		if (response.getConfirms().contains(CONF_UNITIME)) {
			response.addConfirmation(ApplicationProperties.getProperty(iParameterPrefix + "messages.unitimeProblemsFound", "The following issues have been detected:"), CONF_UNITIME, -1);
			response.addConfirmation("", CONF_UNITIME, 1);
		}
		int line = 2;
		if (creditError != null) {
			response.addConfirmation(creditError, CONF_UNITIME, line ++);
		}
		if (questionNoAlt)
			response.addConfirmation(ApplicationProperties.getProperty(iParameterPrefix + "messages.acr.noAlternatives", (line > 2 ? "\n" : "") +
					"One or more of the recommended courses have no alternatives provided. The student may not be able to get a full schedule."),
					CONF_UNITIME, line ++);
		if (questionParentSameReq)
			response.addConfirmation(ApplicationProperties.getProperty(iParameterPrefix + "messages.acr.parentSameRequest", (line > 2 ? "\n" : "") +
					"A course and its associated course are on the same request line. The student will not be able to get both courses."),
					CONF_UNITIME, line ++);
		if (questionParentSubstitute)
			response.addConfirmation(ApplicationProperties.getProperty(iParameterPrefix + "messages.acr.parentSubstitute", (line > 2 ? "\n" : "") +
					"An associated course is requested as a substitute. The student may not be able to get both courses."),
					CONF_UNITIME, line ++);
		if (questionNoParent)
			response.addConfirmation(ApplicationProperties.getProperty(iParameterPrefix + "messages.acr.noParent", (line > 2 ? "\n" : "") +
					"A course depends on another course. The student will not be able to get the course if they do not request the associated course, unless they have already taken the associated course in the past."),
					CONF_UNITIME, line ++);
		if (questionTimeConflict)
			response.addConfirmation(ApplicationProperties.getProperty(iParameterPrefix + "messages.acr.timeConflicts", (line > 2 ? "\n" : "") +
					"Two or more single section courses are conflicting with each other. The student will likely not be able to get the conflicting course, so please provide an alternative course if possible."),
					CONF_UNITIME, line ++);
		if (questionInconStuPref)
			response.addConfirmation(ApplicationProperties.getProperty(iParameterPrefix + "messages.acr.inconsistentStudPref", (line > 2 ? "\n" : "") +
					"One or more courses are not available due to the selected preferences."),
					CONF_UNITIME, line ++);
		if (questionRestrictionsNotMet) {
			if (rule != null)
				response.addConfirmation(ApplicationProperties.getProperty(iParameterPrefix + "messages.acr.ruleNotMet", (line > 2 ? "\n" : "") +
						"One or more of the recommended courses have no {rule} option at the moment. The student may not be able to get a full schedule."
						.replace("{rule}", rule.getRuleName())),
						CONF_UNITIME, line ++);
			else if (onlineOnly)
				response.addConfirmation(ApplicationProperties.getProperty(iParameterPrefix + "messages.acr.onlineOnlyNotMet", (line > 2 ? "\n" : "") +
					"One or more of the recommended courses have no online-only option at the moment. The student may not be able to get a full schedule."),
					CONF_UNITIME, line ++);
			else
				response.addConfirmation(ApplicationProperties.getProperty(iParameterPrefix + "messages.acr.residentialNotMet", (line > 2 ? "\n" : "") +
					"One or more of the recommended courses have no residential option at the moment. The student may not be able to get a full schedule."),
					CONF_UNITIME, line ++);
		}
		if (questionFreeTime) {
			response.addConfirmation(ApplicationProperties.getProperty(iParameterPrefix + "messages.acr.freeTimeRequested", (line > 2 ? "\n" : "") +
					"Free time requests will be considered as time blocks during the pre-registration process. When possible, classes should be avoided during free time. However, if a free time request is placed higher than a course, the course cannot be attended during free time and the student may not receive a full schedule."),
					CONF_UNITIME, line ++);
		}

		if (line > 2 || questionDisclaimer)
			response.addConfirmation(ApplicationProperties.getProperty(iParameterPrefix + "messages.confirmation", "\nDo you want to proceed?"), CONF_UNITIME, line ++);

		Set<Integer> conf = response.getConfirms();
		if (conf.contains(CONF_UNITIME)) {
		response.setConfirmation(CONF_UNITIME, ApplicationProperties.getProperty(iParameterPrefix + "confirm.acr.unitimeDialogName","Warning Confirmations"),
				(ApplicationProperties.getProperty(iParameterPrefix + "confirm.acr.unitimeYesButton", "Accept & Submit")),
				ApplicationProperties.getProperty(iParameterPrefix + "confirm.acr.unitimeNoButton", "Cancel Submit"),
				(ApplicationProperties.getProperty(iParameterPrefix + "confirm.acr.unitimeYesButtonTitle", "Accept the above warning(s) and submit the Advisor Course Recommendations")),
				ApplicationProperties.getProperty(iParameterPrefix + "confirm.acr.unitimeNoButtonTitle", "Go back to editing your Advisor Course Recommendations"));
		}
	}
}