/*
 * UniTime 3.5 (University Timetabling Application)
 * Copyright (C) 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.onlinesectioning.model;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashSet;
import java.util.Set;

import org.infinispan.marshall.Externalizer;
import org.infinispan.marshall.SerializeWith;
import org.unitime.timetable.model.AcademicClassification;
import org.unitime.timetable.model.CurriculumReservation;
import org.unitime.timetable.model.PosMajor;

@SerializeWith(XCurriculumReservation.XCurriculumReservationSerializer.class)
public class XCurriculumReservation extends XReservation {
	private static final long serialVersionUID = 1L;
	private int iLimit;
    private String iAcadArea;
    private Set<String> iClassifications = new HashSet<String>();
    private Set<String> iMajors = new HashSet<String>();
    
    public XCurriculumReservation() {
    	super();
    }
    
    public XCurriculumReservation(ObjectInput in) throws IOException, ClassNotFoundException {
    	super();
    	readExternal(in);
    }
    
    public XCurriculumReservation(XOffering offering, CurriculumReservation reservation) {
    	super(XReservationType.Curriculum, offering, reservation);
    	iLimit = reservation.getLimit();
    	iAcadArea = reservation.getArea().getAcademicAreaAbbreviation();
    	for (AcademicClassification clasf: reservation.getClassifications())
    		iClassifications.add(clasf.getCode());
    	for (PosMajor major: reservation.getMajors())
    		iMajors.add(major.getCode());
    }
    
    public XCurriculumReservation(net.sf.cpsolver.studentsct.reservation.CurriculumReservation reservation) {
    	super(XReservationType.Curriculum, reservation);
    	iLimit = (int)Math.round(reservation.getReservationLimit());
    	iAcadArea = reservation.getAcademicArea();
    	if (reservation.getClassifications() != null)
    		iClassifications.addAll(reservation.getClassifications());
    	if (reservation.getMajors() != null)
    		iMajors.addAll(reservation.getMajors());
    }

    /**
     * Curriculum reservation cannot go over the limit
     */
    @Override
    public boolean canAssignOverLimit() {
        return false;
    }
    
    /**
     * Curriculum reservation do not need to be used
     */
    @Override
    public boolean mustBeUsed() {
        return false;
    }

    /**
     * Reservation limit (-1 for unlimited)
     */
    @Override
    public int getReservationLimit() {
        return iLimit;
    }

    /**
     * Reservation priority (lower than individual and group reservations)
     */
    @Override
    public int getPriority() {
        return 3;
    }
    
    /**
     * Academic area
     */
    public String getAcademicArea() {
        return iAcadArea;
    }
    
    /**
     * Majors
     */
    public Set<String> getMajors() {
        return iMajors;
    }
    
    /**
     * Academic classifications
     */
    public Set<String> getClassifications() {
        return iClassifications;
    }

    /**
     * Check the area, classifications and majors
     */
    @Override
    public boolean isApplicable(XStudent student) {
        boolean match = false;
        if (student.getAcademicAreaClasiffications() == null) return false;
        for (XAcademicAreaCode aac: student.getAcademicAreaClasiffications()) {
            if (getAcademicArea().equals(aac.getArea())) {
                if (getClassifications().isEmpty() || getClassifications().contains(aac.getCode())) {
                    match = true; break;
                }
            }
        }
        if (!match) return false;
        for (XAcademicAreaCode aac: student.getMajors()) {
            if (getAcademicArea().equals(aac.getArea())) {
                if (getMajors().isEmpty() || getMajors().contains(aac.getCode()))
                    return true;
            }
        }
        return getMajors().isEmpty();
    }
    
    @Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    	super.readExternal(in);
    	iAcadArea = (String)in.readObject();
    	
    	int nrClassifications = in.readInt();
    	iClassifications.clear();
    	for (int i = 0; i < nrClassifications; i++)
    		iClassifications.add((String)in.readObject());
    	
    	int nrMajors = in.readInt();
    	iMajors.clear();
    	for (int i = 0; i < nrMajors; i++)
    		iMajors.add((String)in.readObject());
    	
    	iLimit = in.readInt();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeObject(iAcadArea);
		
		out.writeInt(iClassifications.size());
		for (String clasf: iClassifications)
			out.writeObject(clasf);
		
		out.writeInt(iMajors.size());
		for (String major: iMajors)
			out.writeObject(major);
		
		out.writeInt(iLimit);
	}
	
	public static class XCurriculumReservationSerializer implements Externalizer<XCurriculumReservation> {
		private static final long serialVersionUID = 1L;

		@Override
		public void writeObject(ObjectOutput output, XCurriculumReservation object) throws IOException {
			object.writeExternal(output);
		}

		@Override
		public XCurriculumReservation readObject(ObjectInput input) throws IOException, ClassNotFoundException {
			return new XCurriculumReservation(input);
		}
	}
}
