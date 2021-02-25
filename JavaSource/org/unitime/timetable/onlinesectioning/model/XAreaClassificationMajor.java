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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

import org.cpsolver.ifs.util.ToolBox;
import org.cpsolver.studentsct.model.AreaClassificationMajor;
import org.infinispan.commons.marshall.Externalizer;
import org.infinispan.commons.marshall.SerializeWith;
import org.unitime.timetable.model.StudentAreaClassificationMajor;
import org.unitime.timetable.model.StudentAreaClassificationMinor;


/**
 * @author Tomas Muller
 */
@SerializeWith(XAreaClassificationMajor.XAreaClassificationMajorSerializer.class)
public class XAreaClassificationMajor implements Serializable, Externalizable, Comparable<XAreaClassificationMajor> {
    private static final long serialVersionUID = 1L;
	private String iAreaCode, iClassificationCode, iMajorCode, iConcentrationCode;
	private String iAreaLabel, iClassificationLabel, iMajorLabel, iConcentrationLabel;
	private double iWeight = 1.0;
	
	public XAreaClassificationMajor() {}
	
	public XAreaClassificationMajor(ObjectInput in) throws IOException, ClassNotFoundException {
		readExternal(in);
	}

    public XAreaClassificationMajor(String area, String classification, String major) {
        iAreaCode = area;
        iClassificationCode = classification;
        iMajorCode = major;
    }
    
    public XAreaClassificationMajor(StudentAreaClassificationMinor acm) {
        iAreaCode = acm.getAcademicArea().getAcademicAreaAbbreviation();
        iAreaLabel = acm.getAcademicArea().getTitle();
        iClassificationCode = acm.getAcademicClassification().getCode();
        iClassificationLabel = acm.getAcademicClassification().getName();
        iMajorCode = acm.getMinor().getCode();
        iMajorLabel = acm.getMinor().getName();
    }
    
    public XAreaClassificationMajor(StudentAreaClassificationMajor acm) {
        iAreaCode = acm.getAcademicArea().getAcademicAreaAbbreviation();
        iAreaLabel = acm.getAcademicArea().getTitle();
        iClassificationCode = acm.getAcademicClassification().getCode();
        iClassificationLabel = acm.getAcademicClassification().getName();
        iMajorCode = acm.getMajor().getCode();
        iMajorLabel = acm.getMajor().getName();
        if (acm.getConcentration() != null) {
        	iConcentrationCode = acm.getConcentration().getCode();
        	iConcentrationLabel = acm.getConcentration().getName();
        }
        if (acm.getWeight() != null)
        	iWeight = acm.getWeight();
    }
    
    public XAreaClassificationMajor(AreaClassificationMajor acm) {
    	iAreaCode = acm.getArea();
    	iAreaLabel = acm.getAreaName();
        iClassificationCode = acm.getClassification();
        iClassificationLabel = acm.getClassificationName();
        iMajorCode = acm.getMajor();
        iMajorLabel = acm.getMajorName();
        iConcentrationCode = acm.getConcentration();
        iConcentrationLabel = acm.getConcentrationName();
        iWeight = acm.getWeight();
    }

    /** Academic area */
    public String getArea() { return iAreaCode; }
    public String getAreaLabel() { return iAreaLabel; }

    /** Classification */
    public String getClassification() { return iClassificationCode; }
    public String getClassificationLabel() { return iClassificationLabel; }
    
    /** Major */
    public String getMajor() { return iMajorCode; }
    public String getMajorLabel() { return iMajorLabel; }
    
    /** Concentration */
    public String getConcentration() { return iConcentrationCode; }
    public String getConcentrationNotNull() { return iConcentrationCode == null ? "" : iConcentrationCode; }
    public String getConcentrationLabel() { return iConcentrationLabel; }
    
    /** Weight */
    public double getWeight() { return iWeight; }; 
    
    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof XAreaClassificationMajor))
            return false;
        XAreaClassificationMajor acm = (XAreaClassificationMajor) o;
        return ToolBox.equals(acm.getArea(), getArea()) && ToolBox.equals(acm.getClassification(), getClassification()) && ToolBox.equals(acm.getMajor(), getMajor()) &&
        		ToolBox.equals(acm.getConcentration(), getConcentration()) ;
    }

    @Override
    public String toString() {
        return getArea() + "/" + getMajor() + (getConcentration() == null ? "" : "-" + getConcentration()) + " " + getClassification();
    }

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		iAreaCode = (String)in.readObject();
		iAreaLabel = (String)in.readObject();
		iClassificationCode = (String)in.readObject();
		iClassificationLabel = (String)in.readObject();
		iMajorCode = (String)in.readObject();
		iMajorLabel = (String)in.readObject();
		iConcentrationCode = (String)in.readObject();
		iConcentrationLabel = (String)in.readObject();
		iWeight = in.readDouble();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(iAreaCode);
		out.writeObject(iAreaLabel);
		out.writeObject(iClassificationCode);
		out.writeObject(iClassificationLabel);
		out.writeObject(iMajorCode);
		out.writeObject(iMajorLabel);
		out.writeObject(iConcentrationCode);
		out.writeObject(iConcentrationLabel);
		out.writeDouble(iWeight);
	}
	
	public static class XAreaClassificationMajorSerializer implements Externalizer<XAreaClassificationMajor> {
		private static final long serialVersionUID = 1L;

		@Override
		public void writeObject(ObjectOutput output, XAreaClassificationMajor object) throws IOException {
			object.writeExternal(output);
		}

		@Override
		public XAreaClassificationMajor readObject(ObjectInput input) throws IOException, ClassNotFoundException {
			return new XAreaClassificationMajor(input);
		}
	}

	@Override
	public int compareTo(XAreaClassificationMajor acm) {
		if (getWeight() != acm.getWeight())
			return getWeight() > acm.getWeight() ? -1 : 1;
		if (!getArea().equals(acm.getArea()))
			return getArea().compareTo(acm.getArea());
		if (!getClassification().equals(acm.getClassification()))
			return getClassification().compareTo(acm.getClassification());
		if (!getMajor().equals(acm.getMajor()))
			return getMajor().compareTo(acm.getMajor());
		return getConcentrationNotNull().compareTo(acm.getConcentrationNotNull());
	}
}
