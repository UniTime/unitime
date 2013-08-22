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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.cpsolver.studentsct.model.Config;
import net.sf.cpsolver.studentsct.model.Offering;
import net.sf.cpsolver.studentsct.model.Section;
import net.sf.cpsolver.studentsct.model.Subpart;

import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.SectioningInfo;
import org.unitime.timetable.onlinesectioning.OnlineSectioningAction;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer.Lock;

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
	
	public static void persistExpectedSpaces(Long offeringId, boolean needLock, OnlineSectioningServer server, OnlineSectioningHelper helper) {
		Map<Long, Section> sections = new HashMap<Long, Section>();
		Lock lock = (needLock ? server.lockOffering(offeringId, null, false) : null);
		try {
			Offering offering = server.getOffering(offeringId);
			if (offering == null) return;
			helper.info("Persisting expected spaces for " + offering.getName());
			for (Config config: offering.getConfigs())
				for (Subpart subpart: config.getSubparts())
					for (Section section: subpart.getSections())
						sections.put(section.getId(), section);
		} finally {
			if (lock != null) lock.release();
		}
		
    	for (SectioningInfo info: (List<SectioningInfo>)helper.getHibSession().createQuery(
    			"select i from SectioningInfo i where i.clazz.schedulingSubpart.instrOfferingConfig.instructionalOffering = :offeringId").
    			setLong("offeringId", offeringId).
    			setCacheable(true).list()) {
    		Section section = sections.remove(info.getClazz().getUniqueId());
    		if (section == null) continue;
    		if (info.getNbrExpectedStudents() == section.getSpaceExpected() && info.getNbrHoldingStudents() == section.getSpaceHeld()) continue;
    		
    		helper.debug(info.getClazz().getClassLabel(helper.getHibSession()) + ": expected " + sDF.format(section.getSpaceExpected() - info.getNbrExpectedStudents()) +
    				", held " + sDF.format(section.getSpaceHeld() - info.getNbrHoldingStudents()));
    		if (section.getLimit() >= 0 && section.getLimit() >= info.getNbrExpectedStudents() && section.getLimit() < section.getSpaceExpected())
    			helper.info(info.getClazz().getClassLabel(helper.getHibSession()) + ": become over-expected");
    		if (section.getLimit() >= 0 && section.getLimit() < info.getNbrExpectedStudents() && section.getLimit() >= section.getSpaceExpected())
    			helper.info(info.getClazz().getClassLabel(helper.getHibSession()) + ": no longer over-expected");
    		
    		info.setNbrExpectedStudents(section.getSpaceExpected());
    		info.setNbrHoldingStudents(section.getSpaceHeld());
    		helper.getHibSession().saveOrUpdate(info);
    	}
    	
    	if (!sections.isEmpty())
        	for (Class_ clazz: (List<Class_>)helper.getHibSession().createQuery(
        			"select c from Class_ c where c.schedulingSubpart.instrOfferingConfig.instructionalOffering = :offeringId").
        			setLong("offeringId", offeringId).
        			setCacheable(true).list()) {
        		Section section = sections.remove(clazz.getUniqueId());
        		if (section == null) continue;
                SectioningInfo info = new SectioningInfo();
                
        		helper.debug(clazz.getClassLabel(helper.getHibSession()) + ": expected " + sDF.format(section.getSpaceExpected()) +
        				", held " + sDF.format(section.getSpaceHeld()) + " (new)");
        		if (section.getLimit() >= 0 && section.getLimit() < section.getSpaceExpected())
        			helper.info(clazz.getClassLabel(helper.getHibSession()) + ": become over-expected");
        		
                info.setClazz(clazz);
                info.setNbrExpectedStudents(section.getSpaceExpected());
                info.setNbrHoldingStudents(section.getSpaceHeld());
                helper.getHibSession().saveOrUpdate(info);
    		}
	}
	
	@Override
	public String name() {
		return "persist-expectations";
	}

}
