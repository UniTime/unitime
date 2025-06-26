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

import java.util.List;

import org.hibernate.Transaction;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.client.offerings.PrefGroupEditInterface.Operation;
import org.unitime.timetable.gwt.client.offerings.PrefGroupEditInterface.Selection;
import org.unitime.timetable.gwt.client.offerings.PrefGroupEditInterface.SubpartEditRequest;
import org.unitime.timetable.gwt.client.offerings.PrefGroupEditInterface.SubpartEditResponse;
import org.unitime.timetable.gwt.client.offerings.PrefGroupEditInterface.TimePatternModel;
import org.unitime.timetable.gwt.client.offerings.PrefGroupEditInterface.TimeSelection;
import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.interfaces.ExternalSchedulingSubpartEditAction;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.CourseCreditFormat;
import org.unitime.timetable.model.CourseCreditType;
import org.unitime.timetable.model.CourseCreditUnitConfig;
import org.unitime.timetable.model.CourseCreditUnitType;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.FixedCreditUnitConfig;
import org.unitime.timetable.model.ItypeDesc;
import org.unitime.timetable.model.Preference;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.TimePattern;
import org.unitime.timetable.model.VariableFixedCreditUnitConfig;
import org.unitime.timetable.model.VariableRangeCreditUnitConfig;
import org.unitime.timetable.model.DatePattern.DatePatternType;
import org.unitime.timetable.model.dao.DatePatternDAO;
import org.unitime.timetable.model.dao.InstrOfferingConfigDAO;
import org.unitime.timetable.model.dao.ItypeDescDAO;
import org.unitime.timetable.model.dao.SchedulingSubpartDAO;
import org.unitime.timetable.model.dao.TimePatternDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.webutil.BackTracker;

@GwtRpcImplements(SubpartEditRequest.class)
public class SubpartEditBackend implements GwtRpcImplementation<SubpartEditRequest, SubpartEditResponse>{
	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);

	@Override
	public SubpartEditResponse execute(SubpartEditRequest request, SessionContext context) {
		org.hibernate.Session hibSession = SchedulingSubpartDAO.getInstance().getSession();
		SchedulingSubpart subpart = SchedulingSubpartDAO.getInstance().get(request.getId());
		context.checkPermission(subpart, Right.SchedulingSubpartEdit);
		
		if (subpart == null)
			throw new GwtRpcException(MSG.errorNoSubpartId());

		if (request.getOperation() != null) {
			SubpartEditResponse ret;
			switch (request.getOperation()) {
			case CLEAR_CLASS_PREFS:
				context.checkPermission(subpart, Right.SchedulingSubpartEditClearPreferences);

				ClassEditBackend.doClear(subpart.getPreferences(),
						Preference.Type.TIME, Preference.Type.ROOM, Preference.Type.ROOM_FEATURE, Preference.Type.ROOM_GROUP, Preference.Type.BUILDING, Preference.Type.DATE);
				hibSession.merge(subpart);
				hibSession.flush();

	            ChangeLog.addChange(
	                    null,
	                    context,
	                    subpart,
	                    ChangeLog.Source.SCHEDULING_SUBPART_EDIT,
	                    ChangeLog.Operation.CLEAR_PREF,
	                    subpart.getInstrOfferingConfig().getInstructionalOffering().getControllingCourseOffering().getSubjectArea(),
	                    subpart.getManagingDept());
				
				ret = new SubpartEditResponse();
				ret.setUrl("subpart?id=" + subpart.getUniqueId());
				return ret;
			case UPDATE:
			case NEXT:
			case PREVIOUS:
				
				Transaction tx = hibSession.beginTransaction();
				try {
					SubpartEditResponse data = request.getPayLoad();
					subpart.setAutoSpreadInTime(data.isAutoSpreadInTime());
					subpart.setStudentAllowOverlap(data.isStudentsCanOverlap());
					subpart.setDatePattern(data.getDatePatternId() == null ? null : DatePatternDAO.getInstance().get(data.getDatePatternId()));
					if (data.getInstructionalTypeId() != null)
						subpart.setItype(ItypeDescDAO.getInstance().get(data.getInstructionalTypeId().intValue()));
					
					ClassEditBackend.doUpdate(subpart, subpart.getPreferences(), data,
							Preference.Type.TIME, Preference.Type.ROOM, Preference.Type.ROOM_FEATURE, Preference.Type.ROOM_GROUP, Preference.Type.BUILDING, Preference.Type.DATE);

			        hibSession.merge(subpart);
			        
			        if (data.getCreditFormat() == null) {
			        	CourseCreditUnitConfig origConfig = subpart.getCredit();
			        	if (origConfig != null){
	        				subpart.setCredit(null);
	            			hibSession.remove(origConfig);
			        	}
			        } else {
			         	if (subpart.getCredit() != null){
			        		CourseCreditUnitConfig ccuc = subpart.getCredit();
			        		if (ccuc.getCreditFormat().equals(data.getCreditFormat())) {
			        			boolean changed = false;
			        			if (!ccuc.getCreditType().getUniqueId().equals(data.getCreditTypeId())){
			        				changed = true;
			        			}
			        			if (!ccuc.getCreditUnitType().getUniqueId().equals(data.getCreditUnitTypeId())){
			        				changed = true;
			        			}
			        			if (ccuc instanceof FixedCreditUnitConfig) {
									FixedCreditUnitConfig fcuc = (FixedCreditUnitConfig) ccuc;
									if (!fcuc.getFixedUnits().equals(data.getCreditUnits())) {
										changed = true;
									}
								} else if (ccuc instanceof VariableFixedCreditUnitConfig) {
									VariableFixedCreditUnitConfig vfcuc = (VariableFixedCreditUnitConfig) ccuc;
									if (!vfcuc.getMinUnits().equals(data.getCreditUnits())){
										changed = true;
									}
									if (!vfcuc.getMaxUnits().equals(data.getCreditMaxUnits())){
										changed = true;
									}
									if (vfcuc instanceof VariableRangeCreditUnitConfig) {
										VariableRangeCreditUnitConfig vrcuc = (VariableRangeCreditUnitConfig) vfcuc;
										if (!vrcuc.isFractionalIncrementsAllowed().equals(data.isCreditFractionsAllowed())){
											changed = true;
										}
									}
								}
			        			if (changed) {
			        				CourseCreditUnitConfig origConfig = subpart.getCredit();
			        				subpart.setCredit(null);
			            			hibSession.remove(origConfig);
			            			subpart.setCredit(CourseCreditUnitConfig.createCreditUnitConfigOfFormat(
			            					data.getCreditFormat(),
			            					data.getCreditTypeId(), data.getCreditUnitTypeId(),
			            					data.getCreditUnits(), data.getCreditMaxUnits(),
			            					data.isCreditFractionsAllowed(), false));
			            			subpart.getCredit().setOwner(subpart);
			        			}
			        		} else {
			        			CourseCreditUnitConfig origConfig = subpart.getCredit();
		        				subpart.setCredit(null);
		            			hibSession.remove(origConfig);
		            			subpart.setCredit(CourseCreditUnitConfig.createCreditUnitConfigOfFormat(
		            					data.getCreditFormat(),
		            					data.getCreditTypeId(), data.getCreditUnitTypeId(),
		            					data.getCreditUnits(), data.getCreditMaxUnits(),
		            					data.isCreditFractionsAllowed(), false));
		            			subpart.getCredit().setOwner(subpart);
			        		}
			        	} else {
			        		subpart.setCredit(CourseCreditUnitConfig.createCreditUnitConfigOfFormat(
			        				data.getCreditFormat(),
	            					data.getCreditTypeId(), data.getCreditUnitTypeId(),
	            					data.getCreditUnits(), data.getCreditMaxUnits(),
	            					data.isCreditFractionsAllowed(), false));
	            			subpart.getCredit().setOwner(subpart);
			        	}
			        }
			        if (subpart.getCredit() != null){
			        	if (subpart.getCredit().getUniqueId() == null)
			        		hibSession.persist(subpart.getCredit());
			        	else
			        		hibSession.merge(subpart.getCredit());
			        }
			        
		            ChangeLog.addChange(
		                    null,
		                    context,
		                    subpart,
		                    ChangeLog.Source.SCHEDULING_SUBPART_EDIT,
		                    ChangeLog.Operation.UPDATE,
		                    subpart.getInstrOfferingConfig().getInstructionalOffering().getControllingCourseOffering().getSubjectArea(),
		                    subpart.getManagingDept());
					
			        String className = ApplicationProperty.ExternalActionSchedulingSubpartEdit.value();
			    	if (className != null && className.trim().length() > 0){
			        	ExternalSchedulingSubpartEditAction editAction = (ExternalSchedulingSubpartEditAction) (Class.forName(className).getDeclaredConstructor().newInstance());
			       		editAction.performExternalSchedulingSubpartEditAction(subpart, hibSession);
			    	}

			    	tx.commit();
					tx = null;
				} catch (Exception e) {
					if (tx != null) { tx.rollback(); }
					throw new GwtRpcException(e.getMessage(), e);
				}
				
				ret = new SubpartEditResponse();
				if (request.getOperation() == Operation.PREVIOUS && request.getPayLoad().getPreviousId() != null)
					ret.setUrl("subpartEdit?id=" + request.getPayLoad().getPreviousId());
				else if (request.getOperation() == Operation.NEXT  && request.getPayLoad().getNextId() != null)
					ret.setUrl("subpartEdit?id=" + request.getPayLoad().getNextId());
				else
					ret.setUrl("subpart?id=" + subpart.getUniqueId());
				return ret;
			case DATE_PATTERN:
				// update date and time preferences using the provided date pattern id
				ret = request.getPayLoad();
				DatePattern datePattern = DatePatternDAO.getInstance().get(ret.getDatePatternId());
				if (datePattern == null)
					datePattern = subpart.getSession().getDefaultDatePattern();
				
				List<Selection> dateSelections = (ret.getDatePreferences() == null ? null : ret.getDatePreferences().getSelections());
				ClassEditBackend.fillInDatePreferences(ret, subpart, null, datePattern, context, false);
				if (ret.getDatePreferences() != null && dateSelections != null) {
					for (Selection selection: dateSelections)
						if (ret.getDatePreferences().getItem(selection.getItem()) != null)
							ret.getDatePreferences().addSelection(selection);
				}
				
				List<TimeSelection> timeSelections = (ret.getTimePreferences() == null ? null : ret.getTimePreferences().getSelections());
				ClassEditBackend.fillInTimePreferences(ret, subpart, null, datePattern, context, false);
				if (ret.getTimePreferences() != null && timeSelections != null) {
					for (TimeSelection selection: timeSelections) {
						TimePatternModel m = ret.getTimePreferences().getItem(selection.getItem());
						if (m == null) {
							TimePattern tp = TimePatternDAO.getInstance().get(selection.getItem());
							if (tp != null) {
								m = ClassEditBackend.createTimePatternModel(subpart, tp, context);
								m.setValid(false);
								ret.getTimePreferences().addItem(m);
							}
						}
						if (m != null) ret.getTimePreferences().addSelection(selection);
					}
				}
				return ret;
			}
		}
		
		SubpartEditResponse ret = new SubpartEditResponse();
		ret.setId(request.getId());
		ret.setName(subpart.getSchedulingSubpartLabel());
		ret.setNbrRooms(subpart.getMaxRooms());
		ret.setAutoSpreadInTime(subpart.getAutoSpreadInTime());
		ret.setStudentsCanOverlap(subpart.getStudentAllowOverlap());
		
		SchedulingSubpart next = subpart.getNextSchedulingSubpart(context, Right.SchedulingSubpartEdit); 
        ret.setNextId(next==null ? null : next.getUniqueId());
        SchedulingSubpart previous = subpart.getPreviousSchedulingSubpart(context, Right.SchedulingSubpartEdit); 
        ret.setPreviousId(previous == null ? null : previous.getUniqueId());
        ret.setCanClearPrefs(context.hasPermission(subpart, Right.SchedulingSubpartEditClearPreferences));
        
		ret.addProperty(MSG.filterManager()).add(subpart.getManagingDept().getManagingDeptLabel());
        if (subpart.getParentSubpart() != null)
        	ret.addProperty(MSG.propertyParentSchedulingSubpart()).setText(subpart.getParentSubpart().getSchedulingSubpartLabel());

        DatePattern dp = subpart.getDatePattern();
        DatePattern edp = subpart.getSession().getDefaultDatePattern();
        
        ret.setSearchableDatePattern(ApplicationProperty.ClassEditSearcheableDatePattern.isTrue());
        if (dp != null)
        	ret.setDatePatternId(dp.getUniqueId());
        else if (edp != null)
        	ret.setDatePatternId(-edp.getUniqueId());
        if (edp != null)
        	ret.addDatePattern(-edp.getUniqueId(), MSG.dropDefaultDatePattern() + " (" + edp.getName() + ")", (edp.getDatePatternType() == DatePatternType.PatternSet ? null : edp.getPatternText()));
        for (DatePattern p: DatePattern.findAll(subpart.getSessionId(),
        		context.getUser().getCurrentAuthority().hasRight(Right.ExtendedDatePatterns),
        		subpart.getManagingDept(), dp))
        	ret.addDatePattern(p.getUniqueId(), p.getName(), (p.getDatePatternType() == DatePatternType.PatternSet ? null : p.getPatternText()));
		
        ClassEditBackend.fillInPreferences(ret, subpart, context);
        
        SchedulingSubpart parent = subpart.getParentSubpart();
        if (parent == null || !subpart.getItype().equals(parent.getItype())) {
        	if (ApplicationProperty.SubpartCreditEditable.isTrue()) {
                for (CourseCreditType creditType: CourseCreditType.getCourseCreditTypeList())
                	ret.addCreditType(creditType.getUniqueId(), creditType.getLabel(), creditType.getReference());
                for (CourseCreditUnitType creditUnitType: CourseCreditUnitType.getCourseCreditUnitTypeList())
                	ret.addCreditUnitType(creditUnitType.getUniqueId(), creditUnitType.getLabel(), creditUnitType.getReference());
                for (CourseCreditFormat creditFormat: CourseCreditFormat.getCourseCreditFormatList())
                	ret.addCreditFormat(creditFormat.getUniqueId(), creditFormat.getLabel(), creditFormat.getReference());
                
                CourseCreditUnitConfig credit = subpart.getCredit();
                if (credit != null) {
                	ret.setCreditFormatId(credit.getCourseCreditFormat() == null ? null : credit.getCourseCreditFormat().getUniqueId());
                	ret.setCreditTypeId(credit.getCreditType() == null ? null : credit.getCreditType().getUniqueId());
                	ret.setCreditUnitTypeId(credit.getCreditUnitType() == null ? null : credit.getCreditUnitType().getUniqueId());
                	if (credit instanceof FixedCreditUnitConfig) {
                		ret.setCreditUnits(((FixedCreditUnitConfig)credit).getFixedUnits());
                	} else if (credit instanceof VariableFixedCreditUnitConfig) {
                		ret.setCreditUnits(((VariableFixedCreditUnitConfig)credit).getMinUnits());
                		ret.setCreditMaxUnits(((VariableFixedCreditUnitConfig)credit).getMaxUnits());
                		if (credit instanceof VariableRangeCreditUnitConfig)
                			ret.setCreditFractionsAllowed(((VariableRangeCreditUnitConfig)credit).isFractionalIncrementsAllowed());
                	}
                }
            } else {
            	CourseCreditUnitConfig credit = subpart.getCredit();
            	if (credit != null)
            		ret.addProperty(MSG.propertySubpartCredit()).add(credit.creditText());
            }
        }
        
		for (ItypeDesc itype: InstrOfferingConfigDAO.getInstance().getSession().createQuery(
				"from ItypeDesc order by itype", ItypeDesc.class).setCacheable(true).list()) {
			if (itype.getBasic())
				ret.addInstructionalType(itype.getItype().longValue(), itype.getDesc(), itype.getDesc().trim());
			else
				ret.addExtInstructionalType(itype.getItype().longValue(), itype.getDesc(), itype.getDesc().trim());
		}
		ret.setInstructionalTypeId((long)subpart.getItype().getItype());

        
        BackTracker.markForBack(
        		context,
        		"subpart?id="+subpart.getUniqueId(),
        		MSG.backSubpart(subpart.getSchedulingSubpartLabel()),
        		true, false);

		return ret;
	}

}
