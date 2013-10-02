/*
 * Copyright (C) 2011, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
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

public class PersistExpectedSpacesAction implements OnlineSectioningAction<Boolean>{
	private static final long serialVersionUID = 1L;
	private static StudentSectioningMessages MSG = Localization.create(StudentSectioningMessages.class);
	private Collection<Long> iOfferingIds;
	private static DecimalFormat sDF = new DecimalFormat("+0.000;-0.000");

	public PersistExpectedSpacesAction(Long... offeringIds) {
		iOfferingIds = new ArrayList<Long>();
		for (Long offeringId: offeringIds)
			iOfferingIds.add(offeringId);
	}
	
	public PersistExpectedSpacesAction(Collection<Long> offeringIds) {
		iOfferingIds = offeringIds;
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
    			"select i from SectioningInfo i where i.clazz.schedulingSubpart.instrOfferingConfig.instructionalOffering = :offeringId").
    			setLong("offeringId", offeringId).
    			setCacheable(true).list()) {
    		Double expectation = expectations.remove(info.getClazz().getUniqueId());
    		if (expectation == null) {
    			helper.getHibSession().delete(info);
    		} else if (!expectation.equals(info.getNbrExpectedStudents())) {
        		helper.debug(info.getClazz().getClassLabel(helper.getHibSession()) + ": expected " + sDF.format(expectation - info.getNbrExpectedStudents()));
    			
    			int limit = getLimit(info.getClazz());
    			if (limit >= 0 && limit >= info.getNbrExpectedStudents() && limit < expectation)
        			helper.info(info.getClazz().getClassLabel(helper.getHibSession()) + ": become over-expected");
        		if (limit >= 0 && limit < info.getNbrExpectedStudents() && limit >= expectation)
        			helper.info(info.getClazz().getClassLabel(helper.getHibSession()) + ": no longer over-expected");
        		
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
        			helper.info(clazz.getClassLabel(helper.getHibSession()) + ": become over-expected");
        		
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
