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
package org.unitime.timetable.onlinesectioning.updates;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.SectioningInfo;
import org.unitime.timetable.onlinesectioning.OnlineSectioningAction;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.server.CheckMaster;
import org.unitime.timetable.onlinesectioning.server.CheckMaster.Master;

/**
 * @author Tomas Muller
 */
@CheckMaster(Master.REQUIRED)
public class PersistExpectedSpacesAction implements OnlineSectioningAction<Boolean>{
	private static final long serialVersionUID = 1L;
	private static StudentSectioningMessages MSG = Localization.create(StudentSectioningMessages.class);
	private Collection<Long> iOfferingIds;
	private static DecimalFormat sDF = new DecimalFormat("+0.000;-0.000");

	public PersistExpectedSpacesAction forOfferings(Long... offeringIds) {
		iOfferingIds = new ArrayList<Long>();
		for (Long offeringId: offeringIds)
			iOfferingIds.add(offeringId);
		return this;
	}
	
	public PersistExpectedSpacesAction forOfferings(Collection<Long> offeringIds) {
		iOfferingIds = offeringIds;
		return this;
	}
	
	public Collection<Long> getOfferingIds() { return iOfferingIds; }

	@Override
	public Boolean execute(OnlineSectioningServer server, OnlineSectioningHelper helper) {
		for (Long offeringId: getOfferingIds()) {
			try {
				helper.beginTransaction();
				
				persistExpectedSpaces(offeringId, true, server, helper);
		    	
		    	helper.commitTransaction();
			} catch (Exception e) {
				helper.rollbackTransaction();
				helper.error(MSG.exceptionUnknown(e.getMessage()), e);
			}
		}
		
		return true;
	}
	
	private static int getLimit(Class_ clazz) {
		int limit = -1;
		if (!clazz.getSchedulingSubpart().getInstrOfferingConfig().isUnlimitedEnrollment()) {
			limit = clazz.getMaxExpectedCapacity();
        	if (clazz.getExpectedCapacity() < clazz.getMaxExpectedCapacity() && clazz.getCommittedAssignment() != null && clazz.getCommittedAssignment().getRooms().isEmpty()) {
        		int roomSize = Integer.MAX_VALUE;
        		for (Location room: clazz.getCommittedAssignment().getRooms())
        			roomSize = Math.min(roomSize, room.getCapacity() == null ? 0 : room.getCapacity());
        		int roomLimit = (int) Math.floor(roomSize / (clazz.getRoomRatio() == null ? 1.0f : clazz.getRoomRatio()));
        		limit = Math.min(Math.max(clazz.getExpectedCapacity(), roomLimit), clazz.getMaxExpectedCapacity());
        	}
            if (limit >= 9999) limit = -1;
        }
		return limit;
	}
	
	public static void persistExpectedSpaces(Long offeringId, boolean needLock, OnlineSectioningServer server, OnlineSectioningHelper helper) {
		Map<Long, Double> expectations = server.getExpectations(offeringId).toMap();
		if (expectations == null || expectations.isEmpty()) return;
		
    	for (SectioningInfo info: (List<SectioningInfo>)helper.getHibSession().createQuery(
    			"select i from SectioningInfo i " +
    			"left join fetch i.clazz as c " +
    			"where i.clazz.schedulingSubpart.instrOfferingConfig.instructionalOffering = :offeringId").
    			setLong("offeringId", offeringId).
    			setCacheable(true).list()) {
    		Double expectation = expectations.remove(info.getClazz().getUniqueId());
    		if (expectation == null) {
    			helper.getHibSession().delete(info);
    		} else if (!expectation.equals(info.getNbrExpectedStudents())) {
        		helper.debug(info.getClazz().getClassLabel(helper.getHibSession()) + ": expected " + sDF.format(expectation - info.getNbrExpectedStudents()));
    			
    			int limit = getLimit(info.getClazz());
    			if (limit >= 0 && limit >= info.getNbrExpectedStudents() && limit < expectation)
        			helper.debug(info.getClazz().getClassLabel(helper.getHibSession()) + ": become over-expected");
        		if (limit >= 0 && limit < info.getNbrExpectedStudents() && limit >= expectation)
        			helper.debug(info.getClazz().getClassLabel(helper.getHibSession()) + ": no longer over-expected");
        		
    			info.setNbrExpectedStudents(expectation);
        		helper.getHibSession().saveOrUpdate(info);
    		}
    	}
    	
    	if (!expectations.isEmpty())
        	for (Class_ clazz: (List<Class_>)helper.getHibSession().createQuery(
        			"select c from Class_ c where c.schedulingSubpart.instrOfferingConfig.instructionalOffering = :offeringId").
        			setLong("offeringId", offeringId).
        			setCacheable(true).list()) {
        		Double expectation = expectations.remove(clazz.getUniqueId());
        		if (expectation == null) continue;
                SectioningInfo info = new SectioningInfo();
                
        		helper.debug(clazz.getClassLabel(helper.getHibSession()) + ": expected " + sDF.format(expectation) + " (new)");
        		
        		int limit = getLimit(clazz);
        		if (limit >= 0 && limit < expectation)
        			helper.debug(clazz.getClassLabel(helper.getHibSession()) + ": become over-expected");
        		
                info.setClazz(clazz);
                info.setNbrExpectedStudents(expectation);
                info.setNbrHoldingStudents(0.0);
                helper.getHibSession().saveOrUpdate(info);
    		}
	}
	
	@Override
	public String name() {
		return "persist-expectations";
	}

}
