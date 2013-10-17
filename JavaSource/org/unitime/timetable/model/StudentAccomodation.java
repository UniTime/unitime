/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2008 - 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.unitime.timetable.model.base.BaseStudentAccomodation;
import org.unitime.timetable.model.dao.StudentAccomodationDAO;



/**
 * @author Tomas Muller
 */
public class StudentAccomodation extends BaseStudentAccomodation {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public StudentAccomodation () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public StudentAccomodation (java.lang.Long uniqueId) {
		super(uniqueId);
	}

/*[CONSTRUCTOR MARKER END]*/

    public static StudentAccomodation findByAbbv(Long sessionId, String abbv) {
        return (StudentAccomodation)new StudentAccomodationDAO().
            getSession().
            createQuery(
                    "select a from StudentAccomodation a where "+
                    "a.session.uniqueId=:sessionId and "+
                    "a.abbreviation=:abbv").
             setLong("sessionId", sessionId.longValue()).
             setString("abbv", abbv).
             setCacheable(true).
             uniqueResult(); 
    }
    
    public static List<AccommodationCounter> getAccommodations(InstructionalOffering offering) {
    	List<AccommodationCounter> ret = new ArrayList<AccommodationCounter>();
    	for (Object[] line: (List<Object[]>)StudentAccomodationDAO.getInstance().getSession().createQuery(
    			"select a, count(distinct e.student) from StudentClassEnrollment e inner join e.student.accomodations a " +
    			"where e.courseOffering.instructionalOffering.uniqueId = :offeringId " +
    			"group by a.uniqueId, a.session.uniqueId, a.abbreviation, a.name, a.externalUniqueId " +
    			"order by count(a) desc, a.name")
    			.setLong("offeringId", offering.getUniqueId())
    			.setCacheable(true).list()) {
    		ret.add(new AccommodationCounter((StudentAccomodation)line[0], ((Number)line[1]).intValue()));
    	}
    	return ret;
    }
    
    public static List<AccommodationCounter> getAccommodations(Class_ clazz) {
    	List<AccommodationCounter> ret = new ArrayList<AccommodationCounter>();
    	for (Object[] line: (List<Object[]>)StudentAccomodationDAO.getInstance().getSession().createQuery(
    			"select a, count(distinct e.student) from StudentClassEnrollment e inner join e.student.accomodations a " +
    			"where e.clazz.uniqueId = :classId " +
    			"group by a.uniqueId, a.session.uniqueId, a.abbreviation, a.name, a.externalUniqueId " +
    			"order by count(a) desc, a.name")
    			.setLong("classId", clazz.getUniqueId())
    			.setCacheable(true).list()) {
    		ret.add(new AccommodationCounter((StudentAccomodation)line[0], ((Number)line[1]).intValue()));
    	}
    	return ret;
    }
    
    public static List<AccommodationCounter> getAccommodations(Exam exam) {
    	Map<StudentAccomodation, Integer> counter = new Hashtable<StudentAccomodation, Integer>();
    	for (ExamOwner owner: exam.getOwners()) {
    		String query = null;
            switch (owner.getOwnerType()) {
            case ExamOwner.sOwnerTypeClass : 
            	query = "e.clazz.uniqueId = :examOwnerId";
            	break;
            case ExamOwner.sOwnerTypeConfig :
            	query = "e.clazz.schedulingSubpart.instrOfferingConfig.uniqueId = :examOwnerId";
            	break;
            case ExamOwner.sOwnerTypeCourse :
            	query = "e.courseOffering.uniqueId = :examOwnerId";
            	break;
            case ExamOwner.sOwnerTypeOffering :
            	query = "e.courseOffering.instructionalOffering.uniqueId = :examOwnerId";
            	break;
            }
            if (query == null) continue;
            for (Object[] line: (List<Object[]>)StudentAccomodationDAO.getInstance().getSession().createQuery(
        			"select a, count(distinct e.student) from StudentClassEnrollment e inner join e.student.accomodations a " +
        			"where " + query + " " +
        			"group by a.uniqueId, a.session.uniqueId, a.abbreviation, a.name, a.externalUniqueId " +
        			"order by count(a) desc, a.name")
        			.setLong("examOwnerId", owner.getOwnerId())
        			.setCacheable(true).list()) {
            	StudentAccomodation a = (StudentAccomodation)line[0];
            	int count = ((Number)line[1]).intValue();
            	Integer prev = counter.get(a);
            	counter.put(a, count + (prev == null ? 0 : prev.intValue()));
            }
    	}
    	List<AccommodationCounter> ret = new ArrayList<AccommodationCounter>();
    	for (Map.Entry<StudentAccomodation, Integer> entry: counter.entrySet()) {
    		ret.add(new AccommodationCounter(entry.getKey(), entry.getValue()));
    	}
    	Collections.sort(ret);
    	return ret;
    }

    public static String toHtml(List<AccommodationCounter> table) {
    	if (table == null || table.isEmpty()) return null;
    	StringBuffer ret = new StringBuffer("<table>");
    	for (AccommodationCounter ac: table)
    		ret.append(ac.toHtmlRow());
    	ret.append("</table>");
    	return ret.toString();
    }
    
    public static class AccommodationCounter implements Comparable<AccommodationCounter> {
    	StudentAccomodation iAccommodation;
    	private int iCount;
    	
    	public AccommodationCounter(StudentAccomodation accommodation, int count) {
    		iAccommodation = accommodation;
    		iCount = count;
    	}
    	
    	public int getCount() { return iCount; }
    	public StudentAccomodation getAccommodation() { return iAccommodation; }
    	
    	public void increment(int count) { iCount += count; }
    	
    	public String toHtmlRow() { return "<tr><td>" + getAccommodation().getName() + ":</td><td>" + getCount() + "</td></tr>"; }
    	
    	public int compareTo(AccommodationCounter ac) {
    		if (getCount() > ac.getCount()) return -1;
    		if (getCount() < ac.getCount()) return 1;
    		int cmp = getAccommodation().getName().compareTo(ac.getAccommodation().getName());
    		if (cmp != 0) return cmp;
    		return getAccommodation().getUniqueId().compareTo(ac.getAccommodation().getUniqueId());
    	}
    }

}
