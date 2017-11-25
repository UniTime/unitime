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
package org.unitime.timetable.onlinesectioning.model;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashSet;
import java.util.Set;

import org.infinispan.commons.marshall.Externalizer;
import org.infinispan.commons.marshall.SerializeWith;
import org.unitime.timetable.model.AcademicClassification;
import org.unitime.timetable.model.CurriculumReservation;
import org.unitime.timetable.model.PosMajor;

/**
 * @author Tomas Muller
 */
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
    	iLimit = (reservation.getLimit() == null ? -1 : reservation.getLimit());
    	iAcadArea = reservation.getArea().getAcademicAreaAbbreviation();
    	for (AcademicClassification clasf: reservation.getClassifications())
    		iClassifications.add(clasf.getCode());
    	for (PosMajor major: reservation.getMajors())
    		iMajors.add(major.getCode());
    }
    
    public XCurriculumReservation(org.cpsolver.studentsct.reservation.CurriculumReservation reservation) {
    	super(XReservationType.Curriculum, reservation);
    	iLimit = (int)Math.round(reservation.getReservationLimit());
    	iAcadArea = reservation.getAcademicArea();
    	if (reservation.getClassifications() != null)
    		iClassifications.addAll(reservation.getClassifications());
    	if (reservation.getMajors() != null)
    		iMajors.addAll(reservation.getMajors());
    }


    /**
     * Reservation limit (-1 for unlimited)
     */
    @Override
    public int getReservationLimit() {
        return iLimit;
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
    public boolean isApplicable(XStudent student, XCourseId course) {
        for (XAreaClassificationMajor acm: student.getMajors()) {
            if (getAcademicArea().equals(acm.getArea()) &&
            	(getClassifications().isEmpty() || getClassifications().contains(acm.getClassification())) &&
            	(getMajors().isEmpty() || getMajors().contains(acm.getMajor()))) return true;
        }
        return false;
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
