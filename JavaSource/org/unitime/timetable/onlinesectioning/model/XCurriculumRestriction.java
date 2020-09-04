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

/**
 * @author Tomas Muller
 */
@SerializeWith(XCurriculumRestriction.XCurriculumRestrictionSerializer.class)
public class XCurriculumRestriction extends XRestriction {
	private static final long serialVersionUID = 1L;
    private String iAcadArea;
    private Set<String> iClassifications = new HashSet<String>();
    private Set<String> iMajors = new HashSet<String>();
    
    public XCurriculumRestriction() {
    	super();
    }
    
    public XCurriculumRestriction(ObjectInput in) throws IOException, ClassNotFoundException {
    	super();
    	readExternal(in);
    }
    
    public XCurriculumRestriction(org.cpsolver.studentsct.reservation.CurriculumRestriction reservation) {
    	super(XRestrictionType.Curriculum, reservation);
    	iAcadArea = reservation.getAcademicArea();
    	if (reservation.getClassifications() != null)
    		iClassifications.addAll(reservation.getClassifications());
    	if (reservation.getMajors() != null)
    		iMajors.addAll(reservation.getMajors());
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
	}
	
	public static class XCurriculumRestrictionSerializer implements Externalizer<XCurriculumRestriction> {
		private static final long serialVersionUID = 1L;

		@Override
		public void writeObject(ObjectOutput output, XCurriculumRestriction object) throws IOException {
			object.writeExternal(output);
		}

		@Override
		public XCurriculumRestriction readObject(ObjectInput input) throws IOException, ClassNotFoundException {
			return new XCurriculumRestriction(input);
		}
	}
}
