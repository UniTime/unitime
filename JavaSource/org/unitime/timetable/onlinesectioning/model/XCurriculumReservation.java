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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.cpsolver.studentsct.reservation.CurriculumOverride;
import org.unitime.timetable.model.AcademicArea;
import org.unitime.timetable.model.AcademicClassification;
import org.unitime.timetable.model.CurriculumOverrideReservation;
import org.unitime.timetable.model.CurriculumReservation;
import org.unitime.timetable.model.PosMajor;
import org.unitime.timetable.model.PosMajorConcentration;
import org.unitime.timetable.model.PosMinor;

/**
 * @author Tomas Muller
 */
public class XCurriculumReservation extends XReservation {
	private static final long serialVersionUID = 1L;
	private int iLimit;
    private Set<String> iAcadAreas  = new HashSet<String>();
    private Set<String> iClassifications = new HashSet<String>();
    private Set<String> iMajors = new HashSet<String>();
    private Set<String> iMinors = new HashSet<String>();
    private Map<String, Set<String>> iConcentrations = new HashMap<String, Set<String>>();
    private Boolean iExpired;
    private boolean iOverride = false;
    
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
    	for (AcademicArea area: reservation.getAreas())
    		iAcadAreas.add(area.getAcademicAreaAbbreviation());
    	for (AcademicClassification clasf: reservation.getClassifications())
    		iClassifications.add(clasf.getCode());
    	for (PosMajor major: reservation.getMajors())
    		iMajors.add(major.getCode());
    	for (PosMinor minor: reservation.getMinors())
    		iMinors.add(minor.getCode());
    	for (PosMajorConcentration conc: reservation.getConcentrations()) {
			Set<String> concentrations = iConcentrations.get(conc.getMajor().getCode());
			if (concentrations == null) {
				concentrations = new HashSet<String>();
				iConcentrations.put(conc.getMajor().getCode(), concentrations);
			}
			concentrations.add(conc.getCode());
    	}
    }
    
    public XCurriculumReservation(XOffering offering, CurriculumOverrideReservation reservation) {
    	super(XReservationType.CurriculumOverride, offering, reservation);
        iLimit = (reservation.getLimit() == null ? -1 : reservation.getLimit());
        for (AcademicArea area: reservation.getAreas())
    		iAcadAreas.add(area.getAcademicAreaAbbreviation());
    	for (AcademicClassification clasf: reservation.getClassifications())
    		iClassifications.add(clasf.getCode());
    	for (PosMajor major: reservation.getMajors())
    		iMajors.add(major.getCode());
    	for (PosMinor minor: reservation.getMinors())
    		iMinors.add(minor.getCode());
    	for (PosMajorConcentration conc: reservation.getConcentrations()) {
			Set<String> concentrations = iConcentrations.get(conc.getMajor().getCode());
			if (concentrations == null) {
				concentrations = new HashSet<String>();
				iConcentrations.put(conc.getMajor().getCode(), concentrations);
			}
			concentrations.add(conc.getCode());
    	}
    	iOverride = reservation.isAlwaysExpired();
        setMustBeUsed(reservation.isMustBeUsed());
        setAllowOverlap(reservation.isAllowOverlap());
        setCanAssignOverLimit(reservation.isCanAssignOverLimit());
        if (reservation.isAlwaysExpired()) iExpired = true; else iType = XReservationType.Curriculum;
    }
    
    public XCurriculumReservation(org.cpsolver.studentsct.reservation.CurriculumReservation reservation) {
    	super(XReservationType.Curriculum, reservation);
    	iLimit = (int)Math.round(reservation.getReservationLimit());
    	if (reservation.getAcademicAreas() != null)
    		iAcadAreas.addAll(reservation.getAcademicAreas());
    	if (reservation.getClassifications() != null)
    		iClassifications.addAll(reservation.getClassifications());
    	if (reservation.getMajors() != null)
    		iMajors.addAll(reservation.getMajors());
    	if (reservation.getMinors() != null)
    		iMinors.addAll(reservation.getMinors());
    	for (String major: reservation.getMajors()) {
    		Set<String> concentrations = reservation.getConcentrations(major);
    		if (concentrations != null) {
    			iConcentrations.put(major, new HashSet<String>(concentrations));
    		}
    	}
    }
    
    public XCurriculumReservation(CurriculumOverride reservation) {
    	super(XReservationType.CurriculumOverride, reservation);
    	iLimit = (int)Math.round(reservation.getReservationLimit());
    	if (reservation.getAcademicAreas() != null)
    		iAcadAreas.addAll(reservation.getAcademicAreas());
    	if (reservation.getClassifications() != null)
    		iClassifications.addAll(reservation.getClassifications());
    	if (reservation.getMajors() != null)
    		iMajors.addAll(reservation.getMajors());
    	if (reservation.getMinors() != null)
    		iMinors.addAll(reservation.getMinors());
    	for (String major: reservation.getMajors()) {
    		Set<String> concentrations = reservation.getConcentrations(major);
    		if (concentrations != null) {
    			iConcentrations.put(major, new HashSet<String>(concentrations));
    		}
    	}
    	iOverride = true;
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
    public Set<String> getAcademicAreas() {
        return iAcadAreas;
    }
    
    /**
     * Majors
     */
    public Set<String> getMajors() {
        return iMajors;
    }
    
    /**
     * Minors
     */
    public Set<String> getMinors() {
        return iMinors;
    }
    
    /**
     * Academic classifications
     */
    public Set<String> getClassifications() {
        return iClassifications;
    }
    
    public Set<String> getConcentrations(String major) {
    	return iConcentrations.get(major);
    }
    
    @Override
    public boolean isOverride() { return iOverride; }
    
    @Override
    public boolean isExpired() {
    	return (getType() == XReservationType.CurriculumOverride && iExpired != null ? iExpired.booleanValue() : super.isExpired());
    }
    
    @Override
    public boolean isAlwaysExpired() {
    	return getType() == XReservationType.CurriculumOverride && iExpired != null && iExpired.booleanValue();
    }


    /**
     * Check the area, classifications and majors
     */
    @Override
    public boolean isApplicable(XStudent student, XCourseId course) {
    	if (!getMajors().isEmpty() || getMinors().isEmpty())
    		for (XAreaClassificationMajor acm: student.getMajors()) {
                if (getAcademicAreas().contains(acm.getArea()) &&
                	(getClassifications().isEmpty() || getClassifications().contains(acm.getClassification())) &&
                	(getMajors().isEmpty() || getMajors().contains(acm.getMajor()))) {
                	Set<String> conc = iConcentrations.get(acm.getMajor());
                    if (conc != null && !conc.isEmpty()) {
                        return acm.getConcentration() != null && conc.contains(acm.getConcentration());
                    } else {
                        return true;
                    }
                }
            }
    	if (!getMinors().isEmpty())
    		for (XAreaClassificationMajor acm: student.getMinors()) {
                if (getAcademicAreas().contains(acm.getArea()) &&
                	(getClassifications().isEmpty() || getClassifications().contains(acm.getClassification())) &&
                	(getMinors().contains(acm.getMajor()))) return true;
            }
        return false;
    }
    
    @Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    	super.readExternal(in);
    	int nrAcadAreas = in.readInt();
    	iAcadAreas.clear();
    	for (int i = 0; i < nrAcadAreas; i++)
    		iAcadAreas.add((String)in.readObject());
    	
    	int nrClassifications = in.readInt();
    	iClassifications.clear();
    	for (int i = 0; i < nrClassifications; i++)
    		iClassifications.add((String)in.readObject());
    	
    	int nrMajors = in.readInt();
    	iMajors.clear();
    	for (int i = 0; i < nrMajors; i++)
    		iMajors.add((String)in.readObject());
    	
    	int nrMinors = in.readInt();
    	iMinors.clear();
    	for (int i = 0; i < nrMinors; i++)
    		iMinors.add((String)in.readObject());

    	iLimit = in.readInt();
    	
    	if (getType() == XReservationType.CurriculumOverride) {
    		switch (in.readByte()) {
    		case 0:
    			iExpired = false; break;
    		case 1:
    			iExpired = true; break;
    		default:
    			iExpired = null; break;
    		}
    		iOverride = in.readBoolean();
    	} else {
    		iExpired = null;
    		iOverride = false;
    	}
    	
    	iConcentrations = (Map<String, Set<String>>)in.readObject();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeInt(iAcadAreas.size());
		for (String area: iAcadAreas)
			out.writeObject(area);
		
		out.writeInt(iClassifications.size());
		for (String clasf: iClassifications)
			out.writeObject(clasf);
		
		out.writeInt(iMajors.size());
		for (String major: iMajors)
			out.writeObject(major);
		
		out.writeInt(iMinors.size());
		for (String minor: iMinors)
			out.writeObject(minor);
		
		out.writeInt(iLimit);
		
		if (getType() == XReservationType.CurriculumOverride) {
			out.writeByte(iExpired == null ? 2 : iExpired.booleanValue() ? 1 : 0);
			out.writeBoolean(iOverride);
		}
		
		out.writeObject(iConcentrations);
	}
}
