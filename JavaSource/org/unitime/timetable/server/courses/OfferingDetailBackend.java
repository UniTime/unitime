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
package org.unitime.timetable.server.courses;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.unitime.commons.Debug;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.defaults.CommonValues;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.gwt.client.offerings.OfferingsInterface.OfferingDetailRequest;
import org.unitime.timetable.gwt.client.offerings.OfferingsInterface.OfferingDetailResponse;
import org.unitime.timetable.gwt.client.tables.TableInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.LineInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.CellInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.CellInterface.Alignment;
import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.interfaces.ExternalInstructionalOfferingDeleteAction;
import org.unitime.timetable.interfaces.ExternalInstructionalOfferingNotOfferedAction;
import org.unitime.timetable.interfaces.ExternalInstructionalOfferingOfferedAction;
import org.unitime.timetable.interfaces.ExternalLinkLookup;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.Curriculum;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.Event;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.InstructorCourseRequirement;
import org.unitime.timetable.model.OfferingCoordinator;
import org.unitime.timetable.model.OverrideType;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.Reservation;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.StudentAccomodation;
import org.unitime.timetable.model.TeachingRequest;
import org.unitime.timetable.model.StudentAccomodation.AccommodationCounter;
import org.unitime.timetable.model.comparators.ClassComparator;
import org.unitime.timetable.model.comparators.CourseOfferingComparator;
import org.unitime.timetable.model.comparators.InstrOfferingConfigComparator;
import org.unitime.timetable.model.comparators.OfferingCoordinatorComparator;
import org.unitime.timetable.model.comparators.SchedulingSubpartComparator;
import org.unitime.timetable.model.dao.InstructionalOfferingDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.ClassAssignmentProxy;
import org.unitime.timetable.solver.exam.ExamSolverProxy;
import org.unitime.timetable.solver.service.AssignmentService;
import org.unitime.timetable.solver.service.SolverService;
import org.unitime.timetable.webutil.BackTracker;
import org.unitime.timetable.webutil.BackTracker.BackItem;
import org.unitime.timetable.webutil.JavascriptFunctions;

@GwtRpcImplements(OfferingDetailRequest.class)
public class OfferingDetailBackend implements GwtRpcImplementation<OfferingDetailRequest, OfferingDetailResponse>{
	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);
	
	@Autowired AssignmentService<ClassAssignmentProxy> classAssignmentService;
	@Autowired SolverService<ExamSolverProxy> examinationSolverService;
	
	@Override
	public OfferingDetailResponse execute(OfferingDetailRequest request, SessionContext context) {
		try {
			context.checkPermission(request.getOfferingId(), "InstructionalOffering", Right.InstructionalOfferingDetail);
			InstructionalOffering io = InstructionalOfferingDAO.getInstance().get(request.getOfferingId());

	        OfferingDetailResponse response = new OfferingDetailResponse();
	        response.setOfferingId(io.getUniqueId());

			if (request.getAction() == null) {
				BackTracker.markForBack(
						context,
						"gwt.action?page=offering&io="+request.getOfferingId(),
						MSG.backInstructionalOffering(io.getCourseName()),
						true, false);
			    // Set Session Variables
		    	setLastInstructionalOffering(context, io);
			} else {
				switch (request.getAction()) {
				case Lock:
					context.checkPermission(io, Right.OfferingCanLock);
					io.getSession().lockOffering(io.getUniqueId());
					break;
				case Unlock:
					context.checkPermission(io, Right.OfferingCanUnlock);
					io.getSession().unlockOffering(io, context.getUser());
					break;
				case MakeOffered:
					context.checkPermission(io, Right.OfferingMakeOffered);
					makeOffered(io, context);
					response.setUrl("gwt.jsp?page=instrOfferingConfig&offering=" + io.getUniqueId() + "&op=" + URLEncoder.encode(MSG.actionMakeOffered(), "utf-8"));
					return response;
				case MakeNotOffered:
					context.checkPermission(io, Right.OfferingMakeNotOffered);
					makeNotOffered(io, context);
			    	if (ApplicationProperty.MakeNotOfferedStaysOnDetail.isFalse()) {
						response.setUrl("gwt.action?page=offerings#A" + io.getUniqueId());
						return response;
			    	}				
					break;
				case Delete:
					context.checkPermission(io, Right.OfferingDelete);
					deleteOffering(io, context);
					context.removeAttribute(SessionAttribute.OfferingsCourseNumber);
					response.setUrl("gwt.action?page=offerings");
					return response;
				}
			}
	        
			CourseOffering control = io.getControllingCourseOffering();
			response.setSubjectAreaId(control.getSubjectArea().getUniqueId());
			response.setCourseId(control.getUniqueId());
			response.setCourseNumber(control.getCourseNbr());
	        response.setName(io.getCourseNameWithTitle());
	        response.setOffered(!io.isNotOffered());
	        response.setCourses(createCoursesTable(context, io));
	        response.addProperty(MSG.propertyEnrollment()).setText(io.getEnrollment() == null ? "0" : io.getEnrollment().toString());
	        response.addProperty(MSG.propertyLastEnrollment()).setText(io.getDemand() == null || io.getDemand().intValue() == 0 ? "-" : io.getDemand().toString());
	        if (io.getProjectedDemand() != null && io.getProjectedDemand() > 0)
	        	response.addProperty(MSG.propertyProjectedDemand()).setText(io.getProjectedDemand().toString());
	        boolean unlimited = false;
	        int offeringLimit = 0;
	        for (InstrOfferingConfig config: io.getInstrOfferingConfigs()) {
	        	if (config.isUnlimitedEnrollment()) unlimited = true;
	        	else offeringLimit += config.getLimit();
	        }
	        if (unlimited)
	        	response.addProperty(MSG.titleUnlimitedEnrollment()).setHtml("&infin;").addStyle("font-size: +1");
	        else {
	    		CellInterface c = response.addProperty(MSG.titleUnlimitedEnrollment());
	    		c.setText(String.valueOf(offeringLimit));
	        	
	        	// Check limits on courses if cross-listed
	        	if (io.getCourseOfferings().size() > 1) {
	                int lim = 0;
	                boolean reservationSet = false;
	                for (CourseOffering course: io.getCourseOfferings()) {
	                	if (course.getReservation() != null) {
	                		lim += course.getReservation();
	                		reservationSet = true;
	                	}
	                }
	                if (reservationSet && io.getLimit()!=null && lim < io.getLimit().intValue()) {
	            		c.addImage().setSource("images/cancel.png").setAlt(MSG.altLimitsDoNotMatch()).setTitle(MSG.titleLimitsDoNotMatch());
	            		c.add(MSG.errorReservedSpacesForOfferingsTotal(String.valueOf(lim))).setColor("#FF0000");
	                }
	            }
	            // Check configuration limits
	            TreeSet<InstrOfferingConfig> configsWithTooHighLimit = new TreeSet<InstrOfferingConfig>(new InstrOfferingConfigComparator(null));
	            for (InstrOfferingConfig config: io.getInstrOfferingConfigs()) {
	            	if (config.isUnlimitedEnrollment()) continue;
	            	Integer subpartLimit = null;
	            	for (SchedulingSubpart subpart: config.getSchedulingSubparts()) {
	            		int limit = 0;
	            		for (Class_ clazz: subpart.getClasses()) {
	            			limit += (clazz.getMaxExpectedCapacity() == null ? clazz.getExpectedCapacity() : clazz.getMaxExpectedCapacity());
	            		}
	            		if (subpartLimit == null || subpartLimit > limit) subpartLimit = limit;
	            	}
	            	if (subpartLimit != null && subpartLimit < config.getLimit())
	            		configsWithTooHighLimit.add(config);
	            }
	            if (!configsWithTooHighLimit.isEmpty()) {
	            	if (configsWithTooHighLimit.size() == 1) {
	            		c.addImage().setSource("images/cancel.png").setAlt(MSG.altLimitsDoNotMatch()).setTitle(MSG.titleLimitsDoNotMatch());
	            		c.add(MSG.errorConfigWithTooHighLimit(configsWithTooHighLimit.first().getName())).setColor("#FF0000");
	            	} else {
	            		String names = "";
	            		for (InstrOfferingConfig config: configsWithTooHighLimit) {
	            			if (!names.isEmpty()) names += ", ";
	            			names += config.getName();
	            		}
	            		c.addImage().setSource("images/cancel.png").setAlt(MSG.altLimitsDoNotMatch()).setTitle(MSG.titleLimitsDoNotMatch());
	            		c.add(MSG.errorConfigsWithTooHighLimit(names)).setColor("#FF0000");
	            	}
	            }
	        	if (c.hasItems()) {
	        		c.getItems().get(0).addStyle("padding-left: 20px;");
	        		if (c.getItems().size() > 2)
	        			c.getItems().get(2).addStyle("padding-left: 20px;");
	        	}
	        }
	        if (!unlimited && io.getSnapshotLimit() != null)
	        	response.addProperty(MSG.propertySnapshotLimit()).setText(io.getSnapshotLimit().toString());
	        if (Boolean.TRUE.equals(io.isByReservationOnly())) {
	        	CellInterface c = response.addProperty(MSG.propertyByReservationOnly());
	        	c.addImage().setSource("images/accept.png").setTitle(MSG.descriptionByReservationOnly2()).setAlt(MSG.enabled());
	        	c.add(MSG.descriptionByReservationOnly2()).addStyle("font-style: italic;");
	        }
	        if (!io.getOfferingCoordinators().isEmpty()) {
	        	CellInterface c = response.addProperty(MSG.propertyCoordinators());
	        	c.setInline(false);
	        	String instructorNameFormat = context.getUser().getProperty(UserProperty.NameFormat);
	            List<OfferingCoordinator> coordinatorList = new ArrayList<OfferingCoordinator>(io.getOfferingCoordinators());
	            Collections.sort(coordinatorList, new OfferingCoordinatorComparator(context));
	            for (OfferingCoordinator coordinator: coordinatorList) {
	            	c.add(coordinator.getInstructor().getName(instructorNameFormat) +
	            			(coordinator.getResponsibility() == null ?  (coordinator.getPercentShare() != 0 ? " (" + coordinator.getPercentShare() + "%)" : "") :
	            				" (" + coordinator.getResponsibility().getLabel() + (coordinator.getPercentShare() > 0 ? ", " + coordinator.getPercentShare() + "%" : "") + ")")
	            			).setUrl("instructorDetail.action?instructorId=" + coordinator.getInstructor().getUniqueId()).setClassName("noFancyLinks");
	            }
	        }
	        if (io.getLastWeekToEnroll() != null)
	        	response.addProperty(MSG.propertyLastWeekEnrollment()).setText(MSG.textLastWeekEnrollment(io.getLastWeekToEnroll().toString()));
	        if (io.getLastWeekToChange() != null)
	        	response.addProperty(MSG.propertyLastWeekChange()).setText(MSG.textLastWeekChange(io.getLastWeekToChange().toString()));
	        if (io.getLastWeekToDrop() != null)
	        	response.addProperty(MSG.propertyLastWeekDrop()).setText(MSG.textLastWeekDrop(io.getLastWeekToDrop().toString()));
	        if (io.getLastWeekToEnroll() != null || io.getLastWeekToChange() != null || io.getLastWeekToDrop() != null)
	        	response.addProperty("").setText(MSG.descriptionEnrollmentDeadlines(Localization.getDateFormat("EEEE").format(io.getSession().getSessionBeginDateTime())));
	        if (io.getEffectiveWaitListMode() != null) {
	        	CellInterface c = response.addProperty(MSG.propertyWaitListing());
	        	switch (io.getEffectiveWaitListMode()) {
	        	case WaitList:
	        		c.addImage().setSource("images/accept.png").setTitle(MSG.descWaitListEnabled()).setAlt(MSG.waitListEnabled());
	        		c.add(MSG.descWaitListEnabled());
	        		break;
	        	case ReSchedule:
	        		c.addImage().setSource("images/accept_gold.png").setTitle(MSG.descWaitListReschedule()).setAlt(MSG.waitListReschedule());
	        		c.add(MSG.descWaitListReschedule());
	        		break;
	        	case Disabled:
	        		c.addImage().setSource("images/cancel.png").setTitle(MSG.descWaitListDisabled()).setAlt(MSG.waitListDisabled());
	        		c.add(MSG.descWaitListDisabled());
	        		break;
	        	}
	        	if (io.effectiveWaitList()) {
	    			OverrideType prohibitedOverride = OverrideType.findByReference(ApplicationProperty.OfferingWaitListProhibitedOverride.value());
	    			if (prohibitedOverride != null) {
	    				String message = null;
	    				for (CourseOffering co: io.getCourseOfferings()) {
	    					if (co.getDisabledOverrides() == null || !co.getDisabledOverrides().contains(prohibitedOverride)) {
	    						message = (message == null ? "" : message + "\n") + MSG.problemWaitListProhibitedOverride(co.getCourseName(), prohibitedOverride.getLabel());
	    					}
	    				}
	    				if (message != null) {
	    					CellInterface d = c.add(null).setInline(false);
	    					d.addImage().setSource("images/cancel.png").setTitle(message);
	    	        		d.add(message).setColor("#FF0000").addStyle("white-space: pre;");
	    				}
	    			}
	    		}
	        }
	        
	        // Catalog Link
	        @SuppressWarnings("deprecation")
			String linkLookupClass = ApplicationProperty.CourseCatalogLinkProvider.value(); 
	        if (linkLookupClass!=null && !linkLookupClass.isEmpty()) {
	        	try {
	        		ExternalLinkLookup lookup = (ExternalLinkLookup) (Class.forName(linkLookupClass).getDeclaredConstructor().newInstance());
	        		Map results = lookup.getLink(io);
	        		if (results != null)
	        			response.addProperty(MSG.propertyCourseCatalog())
	        				.setText((String)results.get(ExternalLinkLookup.LINK_LABEL))
	        				.setUrl((String)results.get(ExternalLinkLookup.LINK_LOCATION));
	        	} catch (Exception e) {
	        		Debug.error("Failed to get catalog link: " + e.getMessage(), e);
	        	}
	        }
	        
	        List<AccommodationCounter> acc = StudentAccomodation.getAccommodations(io);
	        if (acc != null && !acc.isEmpty()) {
	        	CellInterface c = response.addProperty(MSG.propertyAccommodations());
	        	TableInterface table = new TableInterface();
	        	for (AccommodationCounter ac: acc)
	        		table.addProperty(ac.getAccommodation().getName() + ":").setText(String.valueOf(ac.getCount()));
	        	c.setTable(table);
	        }

	        ClassAssignmentProxy proxy = classAssignmentService.getAssignment();
			try {
				if (proxy != null && proxy.hasConflicts(io.getUniqueId())) {
					CellInterface c = response.addProperty("");
					c.addImage().setSource("images/warning.png").setTitle(MSG.warnOfferingHasConflictingClasses());
	        		c.add(MSG.warnOfferingHasConflictingClasses()).setColor("#FF0000").addStyle("padding-left: 2px;");
				}
			} catch (Exception e) {}
			
	        if (ApplicationProperty.OfferingShowClassNotes.isTrue()) {
	        	TableInterface notes = new TableInterface();
	        	List<InstrOfferingConfig> configs = new ArrayList<InstrOfferingConfig>(io.getInstrOfferingConfigs());
	        	Collections.sort(configs, new InstrOfferingConfigComparator(io.getControllingCourseOffering().getSubjectArea().getUniqueId()));
	        	for (InstrOfferingConfig config: configs) {
	        		List<SchedulingSubpart> subparts = new ArrayList<SchedulingSubpart>(config.getSchedulingSubparts());
	        		Collections.sort(subparts, new SchedulingSubpartComparator());
	        		for (SchedulingSubpart subpart: subparts) {
	        			List<Class_> classes = new ArrayList<Class_>(subpart.getClasses());
	        			Collections.sort(classes, new ClassComparator(ClassComparator.COMPARE_BY_ITYPE));
	        			for (Class_ clazz: classes) {
	        				if (clazz.getNotes() != null && !clazz.getNotes().isEmpty()) {
	        					notes.addProperty(subpart.getItypeDesc().trim() + " " + clazz.getSectionNumberString())
	        						.setText(clazz.getNotes()).addStyle("white-space: pre-wrap;");
	        				}
	        			}
	        		}
	        	}
	        	if (notes.hasProperties()) {
	        		if (io.getNotes() != null && !io.getNotes().isEmpty())
	        			notes.addProperty(io.getCourseName())
	        			.setText(io.getNotes()).addStyle("white-space: pre-wrap;");
	        		response.addProperty(MSG.propertyRequestsNotes()).setTable(notes);
	        	} else {
	        		if (io.getNotes() != null && !io.getNotes().isEmpty())
	            		response.addProperty(MSG.propertyRequestsNotes()).setText(io.getNotes()).addStyle("white-space: pre-wrap;");
	        	}
	        } else {
	        	if (io.getNotes() != null && !io.getNotes().isEmpty())
	        		response.addProperty(MSG.propertyRequestsNotes()).setText(io.getNotes()).addStyle("white-space: pre-wrap;");
	        }

	        if (context.hasPermission(Right.InstructorScheduling)) {
	            for (DepartmentalInstructor di: io.getDepartment().getInstructors()) {
	            	if (di.getTeachingPreference() != null && !PreferenceLevel.sProhibited.equals(di.getTeachingPreference().getPrefProlog())) {
	            		response.addOperation("teachingRequests");
	            		break;
	            	}
	            }
	        }
	        
	        InstructionalOffering next = io.getNextInstructionalOffering(context);
	        response.setNextId(next==null ? null : next.getUniqueId());
	        InstructionalOffering previous = io.getPreviousInstructionalOffering(context);
	        response.setPreviousId(previous == null ? null : previous.getUniqueId());
	        
	    	InstructionalOfferingTableBuilder builder = new InstructionalOfferingTableBuilder(context, request.getBackType(), request.getBackId());
	    	builder.generateConfigTablesForInstructionalOffering(
	    			classAssignmentService.getAssignment(), examinationSolverService.getSolver(),
	    			io, response);
	    	
	    	ExaminationsTableBuilder examBuilder = new ExaminationsTableBuilder(context, request.getBackType(), request.getBackId());
	    	if (request.getExamId() != null && !request.getExamId().isEmpty()) {
	    		examBuilder.setBackId(request.getBackId());
	    		examBuilder.setBackType("Exam");
	    	}
	    	response.setExaminations(examBuilder.createExamsTable(
	    			"InstructionalOffering", io.getUniqueId(), examinationSolverService.getSolver()));
		    
	    	DistributionsTableBuilder distBuilder = new DistributionsTableBuilder(context, request.getBackType(), request.getBackId());
	    	response.setDistributions(distBuilder.getDistPrefsTableForInstructionalOffering(io));
	        
	        if (CommonValues.Yes.eq(context.getUser().getProperty(UserProperty.DisplayLastChanges)))
	        	response.setLastChanges(getLastChanges(io));
	        
	        if (InstructorCourseRequirement.hasRequirementsForOffering(io))
	        	response.addOperation("instructor-survey");
	        
	        if (context.hasPermission(Right.CurriculumView))
	        	response.addOperation("curricula");
	        
	        if (context.hasPermission(Right.Reservations)) {
	        	response.addOperation("reservations");
	        	if (context.hasPermission(io, Right.ReservationOffering) && context.hasPermission(Right.ReservationAdd))
	        		response.addOperation("reservations-editable");	
	        }
	        
	        if (!io.isNotOffered() && context.hasPermission(Right.InstructorScheduling) && context.hasPermission(Right.InstructorAssignmentPreferences))
	            for (DepartmentalInstructor di: io.getDepartment().getInstructors())
	            	if (di.getTeachingPreference() != null && !PreferenceLevel.sProhibited.equals(di.getTeachingPreference().getPrefProlog())) {
	            		response.addOperation("teaching-requests");
	            		break;
	            	}
	        
	    	if (context.hasPermission(Right.ExaminationAdd))
	    		response.addOperation("add-exam");
	    	
	    	BackItem back = BackTracker.getBackItem(context, 2);
	    	if (back != null) {
	    		response.addOperation("back");
	    		response.setBackTitle(back.getTitle());
	    		response.setBackUrl(back.getUrl());
	    	}
	    	if (response.getPreviousId() != null && context.hasPermission(response.getPreviousId(), "InstructionalOffering", Right.InstructionalOfferingDetail))
	    		response.addOperation("previous");
	    	if (response.getNextId() != null && context.hasPermission(response.getNextId(), "InstructionalOffering", Right.InstructionalOfferingDetail))
	    		response.addOperation("next");
	    	if (context.hasPermission(io, Right.OfferingCanLock))
	    		response.addOperation("lock");
	    	if (context.hasPermission(io, Right.OfferingCanUnlock))
	    		response.addOperation("unlock");
	    	if (context.hasPermission(io, Right.InstrOfferingConfigAdd))
	    		response.addOperation("add-config");
	    	if (context.hasPermission(io, Right.InstructionalOfferingCrossLists))
	    		response.addOperation("cross-list");
	    	if (context.hasPermission(io, Right.OfferingMakeOffered))
	    		response.addOperation("make-offered");
	    	if (context.hasPermission(io, Right.OfferingDelete))
	    		response.addOperation("delete");
	    	if (context.hasPermission(io, Right.OfferingMakeNotOffered))
	    		response.addOperation("make-not-offered");

	    	response.setConfirms(JavascriptFunctions.isJsConfirm(context));
			
			return response;
		} catch (GwtRpcException e) {
			throw e;
		} catch (Exception e) {
			throw new GwtRpcException(e.getMessage(), e);
		}
		
	}
	
	public static TableInterface createCoursesTable(SessionContext context, InstructionalOffering io) {
        ArrayList<CourseOffering> offerings = new ArrayList<CourseOffering>(io.getCourseOfferings());
        Collections.sort(
                offerings, 
                new CourseOfferingComparator(CourseOfferingComparator.COMPARE_BY_CTRL_CRS));
        
        boolean hasCourseTypes = false, hasExtId = false, hasRes = false, hasCred = false, hasNote = false, hasDemandsFrom = false,
        		hasAlt = false, hasDisOvrd = false;
        for (CourseOffering co: offerings) {
        	if (co.getCourseType() != null) hasCourseTypes = true;
        	if (ApplicationProperty.CourseOfferingShowExternalIds.isTrue() && co.getExternalUniqueId() != null && !co.getExternalUniqueId().isEmpty()) hasExtId = true;
        	if (co.getReservation() != null) hasRes = true;
        	if (co.getCredit() != null) hasCred = true;
        	if (co.getScheduleBookNote() != null && !co.getScheduleBookNote().isEmpty()) hasNote = true;
        	if (co.getDemandOffering() != null) hasDemandsFrom = true;
        	if (co.getAlternativeOffering() != null) hasAlt = true;
        	if (!co.getDisabledOverrides().isEmpty()) hasDisOvrd = true;
        }
        boolean hasUrl = ApplicationProperty.CustomizationCourseLink.value() != null &&  !ApplicationProperty.CustomizationCourseLink.value().isEmpty();
        
        TableInterface table = new TableInterface();
        table.setName(io.getCourseNameWithTitle());
        LineInterface header = table.addHeader();
        header.addCell();
        if (hasCourseTypes) header.addCell(MSG.columnCourseType());
        header.addCell(MSG.columnTitle());
        if (hasExtId) header.addCell(MSG.columnExternalId());
        if (hasRes) header.addCell(MSG.columnReserved()).setTextAlignment(Alignment.RIGHT);
        if (hasCred) header.addCell(MSG.columnCredit());
        if (hasNote) header.addCell(MSG.columnScheduleOfClassesNote());
        if (hasDemandsFrom) header.addCell(MSG.columnDemandsFrom());
        if (hasAlt) header.addCell(MSG.columnAlternativeCourse());
        header.addCell(MSG.columnConsent());
        if (hasDisOvrd) header.addCell(MSG.columnDisabledOverrides());
        if (hasUrl) header.addCell(MSG.columnCourseCatalog());
        header.addCell();
        for (CellInterface h: header.getCells())
        	h.setClassName("WebTableHeader");
        
        for (CourseOffering co: offerings) {
        	LineInterface line = table.addLine(); line.setClassName("BottomBorderGray");
        	if (co.isIsControl()) {
        		line.addCell().setImage().setSource("images/accept.png")
        			.setAlt(MSG.altControllingCourse())
        			.setTitle(MSG.titleControllingCourse());
        	} else {
        		line.addCell();
        	}
        	if (hasCourseTypes) {
        		if (co.getCourseType() != null)
        			line.addCell(co.getCourseType().getReference()).setTitle(co.getCourseType().getLabel());
        		else
        			line.addCell();
        	}
        	line.addCell(co.getCourseNameWithTitle());
        	if (hasExtId) line.addCell(co.getExternalUniqueId());
        	if (hasRes) line.addCell(co.getReservation() == null ? "" : co.getReservation().toString()).setTextAlignment(Alignment.RIGHT);
        	if (hasCred) {
        		if (co.getCredit() != null)
        			line.addCell(co.getCredit().creditAbbv()).setTitle(co.getCredit().creditText());
        		else
        			line.addCell();
        	}
        	if (hasNote)
        		line.addCell().setHtml(co.getScheduleBookNote()).addStyle("white-space: pre-wrap;");
        	if (hasDemandsFrom)
        		line.addCell(co.getDemandOffering() == null ? "" : co.getDemandOffering().getCourseName());
        	if (hasAlt)
        		line.addCell(co.getAlternativeOffering() == null ? "" : co.getAlternativeOffering().getCourseName());
        	if (co.getConsentType() == null)
        		line.addCell(MSG.noConsentRequired());
        	else
        		line.addCell(co.getConsentType().getAbbv()).setTitle(co.getConsentType().getLabel());
        	if (hasDisOvrd) {
        		CellInterface c = line.addCell();
        		for (OverrideType ot: co.getDisabledOverrides()) {
        			if (c.hasItems()) c.add(", ");
        			c.add(ot.getReference()).setTitle(ot.getLabel());
        		}
        	}
        	if (hasUrl)
        		line.addCell().addCourseLink().setCourseId(co.getUniqueId());
        	CellInterface buttons = line.addCell().setTextAlignment(Alignment.RIGHT);
        	if (context.hasPermission(co, Right.EditCourseOffering) || context.hasPermission(co, Right.EditCourseOfferingCoordinators) || context.hasPermission(co, Right.EditCourseOfferingNote))
        		buttons.addButton().setUrl("gwt.jsp?page=courseOffering&offering=" + co.getUniqueId() + "&op=editCourseOffering")
        			.setText(MSG.actionEditCourseOffering()).setTitle(MSG.titleEditCourseOffering());
        }
        return table;
	}
	
	protected int printLastChangeTableRow(TableInterface table, ChangeLog lastChange) {
		if (lastChange == null) return 0;
		LineInterface line = table.addLine();
		line.addCell(lastChange.getSourceTitle());
		line.addCell(lastChange.getObjectTitle());
		line.addCell(lastChange.getOperationTitle());
		line.addCell(lastChange.getManager().getShortName());
		line.addCell(ChangeLog.sDF.format(lastChange.getTimeStamp()));
		return 1;
    }
	
	protected ChangeLog combine(ChangeLog c1, ChangeLog c2) {
        if (c1==null) return c2;
        if (c2==null) return c1;
        return (c1.compareTo(c2)<0?c2:c1);
    }
	
	public TableInterface getLastChanges(InstructionalOffering io) {
        if (io==null) return null;
        
        TableInterface table = new TableInterface();
        int nrChanges = 0;
        
        table.setName(MSG.columnLastChanges());
        LineInterface header = table.addHeader();
        header.addCell(MSG.columnPage());
        header.addCell(MSG.columnObject());
        header.addCell(MSG.columnOperation());
        header.addCell(MSG.columnManager());
        header.addCell(MSG.columnDate());
    	for (CellInterface cell: header.getCells()) {
    		cell.setClassName("WebTableHeader");
    		cell.setText(cell.getText().replace("<br>", "\n"));
    	}

    	Set<Long> configIds = new HashSet<Long>();
    	Set<Long> subpartIds = new HashSet<Long>();
    	Set<Long> classIds = new HashSet<Long>();
    	Set<Long> offeringIds = new HashSet<Long>();
    	Set<Long> curriculumIds = new HashSet<Long>();
    	
    	for (InstrOfferingConfig ioc: io.getInstrOfferingConfigs()) {
    		configIds.add(ioc.getUniqueId());
    		for (SchedulingSubpart ss: ioc.getSchedulingSubparts()) {
    			subpartIds.add(ss.getUniqueId());
    			for (Class_ c: ss.getClasses()) {
    				classIds.add(c.getUniqueId());
    			}
    		}
    	}
    	for (CourseOffering co: io.getCourseOfferings()) {
    		offeringIds.add(co.getUniqueId());
    	}
    	curriculumIds.addAll(InstructionalOfferingDAO.getInstance().getSession().createQuery(
    			"select c.classification.curriculum.uniqueId from CurriculumCourse c where c.course.instructionalOffering.uniqueId = :offeringId", Long.class)
    			.setParameter("offeringId", io.getUniqueId()).setCacheable(true).list());
    	
    	nrChanges += printLastChangeTableRow(table, ChangeLog.findLastChange(io, ChangeLog.Source.CROSS_LIST));
    	nrChanges += printLastChangeTableRow(table, combine(
    			ChangeLog.findLastChange(io, ChangeLog.Source.MAKE_OFFERED),
    			ChangeLog.findLastChange(io, ChangeLog.Source.MAKE_NOT_OFFERED)));
    	nrChanges += printLastChangeTableRow(table,
    			ChangeLog.findLastChange(CourseOffering.class.getName(), offeringIds, ChangeLog.Source.COURSE_OFFERING_EDIT));
    	nrChanges += printLastChangeTableRow(table, combine(
    			ChangeLog.findLastChange(InstructionalOffering.class.getName(), io.getUniqueId(), ChangeLog.Source.RESERVATION),
    			ChangeLog.findLastChange(Class_.class.getName(), classIds, ChangeLog.Source.RESERVATION)));
    	nrChanges += printLastChangeTableRow(table, combine(
    			ChangeLog.findLastChange(io, ChangeLog.Source.INSTR_CFG_EDIT),
    			ChangeLog.findLastChange(InstrOfferingConfig.class.getName(), configIds, ChangeLog.Source.INSTR_CFG_EDIT)));
    	nrChanges += printLastChangeTableRow(table,
    			ChangeLog.findLastChange(InstrOfferingConfig.class.getName(), configIds, ChangeLog.Source.CLASS_SETUP));
    	nrChanges += printLastChangeTableRow(table, 
    			ChangeLog.findLastChange(InstrOfferingConfig.class.getName(), configIds, ChangeLog.Source.CLASS_INSTR_ASSIGN));
    	nrChanges += printLastChangeTableRow(table,
    			ChangeLog.findLastChange(SchedulingSubpart.class.getName(), subpartIds, ChangeLog.Source.SCHEDULING_SUBPART_EDIT));
    	nrChanges += printLastChangeTableRow(table,
    			ChangeLog.findLastChange(Class_.class.getName(), classIds, ChangeLog.Source.CLASS_EDIT));
    	nrChanges += printLastChangeTableRow(table, 
    			ChangeLog.findLastChange(io, ChangeLog.Source.DIST_PREF_EDIT));
    	nrChanges += printLastChangeTableRow(table, 
    			ChangeLog.findLastChange(CourseOffering.class.getName(), offeringIds, ChangeLog.Source.CURRICULA));
    	nrChanges += printLastChangeTableRow(table, combine(
    			ChangeLog.findLastChange(Curriculum.class.getName(), curriculumIds, ChangeLog.Source.CURRICULA),
    			ChangeLog.findLastChange(Curriculum.class.getName(), curriculumIds, ChangeLog.Source.CURRICULUM_EDIT)));
    	nrChanges += printLastChangeTableRow(table, ChangeLog.findLastChange(Class_.class.getName(), classIds, ChangeLog.Source.INSTRUCTOR_ASSIGNMENT));
    	
    	if (nrChanges > 0) return table;
    	return null;
	}
	
	
	public static void setLastInstructionalOffering(SessionContext sessionContext, InstructionalOffering offering) {
		if (offering == null) return;
		String subjectAreaIds = (String)sessionContext.getAttribute(SessionAttribute.OfferingsSubjectArea);
		String subjectAreaId = offering.getControllingCourseOffering().getSubjectArea().getUniqueId().toString();
		if (subjectAreaIds == null) {
			sessionContext.setAttribute(SessionAttribute.OfferingsSubjectArea, subjectAreaId);
		} else {
			boolean contain = false;
			for (String s: subjectAreaIds.split(","))
				if (s.equals(subjectAreaId)) { contain = true; break; }
			if (!contain && sessionContext.hasPermission(offering.getControllingCourseOffering().getDepartment(), Right.InstructionalOfferings)) {
				sessionContext.setAttribute(SessionAttribute.OfferingsSubjectArea, subjectAreaId);
			}
		}
		
		if (sessionContext.getAttribute(SessionAttribute.OfferingsCourseNumber) != null && !sessionContext.getAttribute(SessionAttribute.OfferingsCourseNumber).toString().isEmpty())
            sessionContext.setAttribute(SessionAttribute.OfferingsCourseNumber, offering.getControllingCourseOffering().getCourseNbr());

	}
	
	protected void makeOffered(InstructionalOffering io, SessionContext context) {
		try {
	    	org.hibernate.Session hibSession = InstructionalOfferingDAO.getInstance().getSession();
	    	
	    	// Set flag to offered
	    	io.setNotOffered(Boolean.valueOf(false));
	    	
	    	hibSession.merge(io);
	            
	        ChangeLog.addChange(
	                hibSession, 
	                context, 
	                io, 
	                ChangeLog.Source.MAKE_OFFERED, 
	                ChangeLog.Operation.UPDATE, 
	                io.getControllingCourseOffering().getSubjectArea(),
	                null);
	        
	        // Lock the offering, if needed
	        if (context.hasPermission(io, Right.OfferingCanLock))
	        	io.getSession().lockOffering(io.getUniqueId());

	        hibSession.flush();
	        hibSession.clear();

	    	String className = ApplicationProperty.ExternalActionInstructionalOfferingOffered.value();
	    	if (className != null && className.trim().length() > 0){
	        	ExternalInstructionalOfferingOfferedAction offeredAction = (ExternalInstructionalOfferingOfferedAction) (Class.forName(className).getDeclaredConstructor().newInstance());
	       		offeredAction.performExternalInstructionalOfferingOfferedAction(io, hibSession);
	    	}
		} catch (Exception e) {
			throw new GwtRpcException(e.getMessage(), e);
		}
    }
	
	protected void makeNotOffered(InstructionalOffering io, SessionContext context) {
		try {
	    	org.hibernate.Session hibSession = InstructionalOfferingDAO.getInstance().getSession();
	    	
	        io.deleteAllDistributionPreferences(hibSession);
            
	        // Delete all classes only - config stays
            io.deleteAllClasses(hibSession);
            
            for (Iterator i=io.getCourseOfferings().iterator();i.hasNext();) {
                CourseOffering co = (CourseOffering)i.next();
                Event.deleteFromEvents(hibSession, co);
                Exam.deleteFromExams(hibSession, co);
            }
            
            Event.deleteFromEvents(hibSession, io);
            Exam.deleteFromExams(hibSession, io);
            
            for (Iterator<Reservation> i = io.getReservations().iterator(); i.hasNext(); ) {
            	Reservation r = i.next();
            	hibSession.remove(r);
            	i.remove();
            }
            
            for (Iterator<TeachingRequest> i = io.getTeachingRequests().iterator(); i.hasNext(); ) {
            	TeachingRequest tr = i.next();
            	hibSession.remove(tr);
            	i.remove();
            }
            
            // Set flag to not offered
            io.setNotOffered(Boolean.valueOf(true));
            
            hibSession.merge(io);

            ChangeLog.addChange(
                    hibSession, 
                    context, 
                    io, 
                    ChangeLog.Source.MAKE_NOT_OFFERED, 
                    ChangeLog.Operation.UPDATE, 
                    io.getControllingCourseOffering().getSubjectArea(),
                    null);

            hibSession.flush();
            hibSession.clear();

            // Unlock the offering, if needed
            if (context.hasPermission(io, Right.OfferingCanUnlock))
            	io.getSession().unlockOffering(io, context.getUser());

        	String className = ApplicationProperty.ExternalActionInstructionalOfferingNotOffered.value();
        	if (className != null && className.trim().length() > 0){
	        	ExternalInstructionalOfferingNotOfferedAction notOfferedAction = (ExternalInstructionalOfferingNotOfferedAction) (Class.forName(className).getDeclaredConstructor().newInstance());
	       		notOfferedAction.performExternalInstructionalOfferingNotOfferedAction(io, hibSession);
        	}
		} catch (Exception e) {
			throw new GwtRpcException(e.getMessage(), e);
		}
    }
	
	protected void deleteOffering(InstructionalOffering io, SessionContext context) {
		Transaction tx = null;
		try {
	    	org.hibernate.Session hibSession = InstructionalOfferingDAO.getInstance().getSession();
	    	
	        tx = hibSession.beginTransaction();

			io.deleteAllDistributionPreferences(hibSession);
            Event.deleteFromEvents(hibSession, io);
	        Exam.deleteFromExams(hibSession, io);
        	String className = ApplicationProperty.ExternalActionInstructionalOfferingDelete.value();
        	if (className != null && className.trim().length() > 0){
	        	ExternalInstructionalOfferingDeleteAction deleteAction = (ExternalInstructionalOfferingDeleteAction) (Class.forName(className).getDeclaredConstructor().newInstance());
	       		deleteAction.performExternalInstructionalOfferingDeleteAction(io, hibSession);
        	}
	        
            ChangeLog.addChange(
                    hibSession, 
                    context, 
                    io, 
                    ChangeLog.Source.OFFERING_DETAIL, 
                    ChangeLog.Operation.DELETE, 
                    io.getControllingCourseOffering().getSubjectArea(),
                    null);

            for (CourseOffering co: io.getCourseOfferings()) {
            	co.getSubjectArea().getCourseOfferings().remove(co);
            	hibSession.remove(co);
            }
	        hibSession.remove(io);
	        
	        tx.commit();
            hibSession.flush();
            hibSession.clear();
		} catch (Exception e) {
        	if (tx!=null) tx.rollback();
			throw new GwtRpcException(e.getMessage(), e);
		}
    }
}
