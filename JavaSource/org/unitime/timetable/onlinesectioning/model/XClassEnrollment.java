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
import java.util.Date;

import org.unitime.timetable.model.StudentClassEnrollment;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;

/**
 * @author Tomas Muller
 */
public class XClassEnrollment implements Externalizable {
	private static final long serialVersionUID = 1L;
	private XCourseId iCourseId;
	private XSection iSection;
	private int iShiftDays = 0;
	private int iEnrollment = 0;
	private String iParentSectionName;
	private String iCredit;
	private Date iTimeStamp;
	
	public XClassEnrollment() {
    }
    
    public XClassEnrollment(ObjectInput in) throws IOException, ClassNotFoundException {
    	readExternal(in);
    }
	
	public XClassEnrollment(StudentClassEnrollment enrollment, OnlineSectioningHelper helper) {
		iCourseId = new XCourseId(enrollment.getCourseOffering());
		iSection = new XSection(enrollment.getClazz(), helper);
		iTimeStamp = enrollment.getTimestamp();
	}
	
	public XClassEnrollment(XEnrollment courseId, XSection section) {
		iCourseId = new XCourseId(courseId);
		iSection = section;
		iTimeStamp = courseId.getTimeStamp();
	}
	
	public XCourseId getCourseId() { return iCourseId; }
	public XSection getSection() { return iSection; }
	public Date getTimeStamp() { return iTimeStamp; }
	public XSection getShiftedSection() {
		if (iShiftDays == 0 || iSection.getTime() == null) return iSection;
		XSection section = new XSection(iSection); section.getTime().datePatternShiftDays(iShiftDays);
		return section;
	}
	public int getShiftDays() { return iShiftDays; }
	public void setShiftDays(int shiftDays) { iShiftDays = shiftDays; }
	
	public int getEnrollment() { return iEnrollment; }
	public void setEnrollment(int enrollment) { iEnrollment = enrollment; }
	public String getParentSectionName() { return iParentSectionName; }
	public void setParentSectionName(String name) { iParentSectionName = name; }
	public String getCredit() { return iCredit; }
	public void setCredit(String credit) { iCredit = credit; }

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		iCourseId.writeExternal(out);
		iSection.writeExternal(out);
		out.writeInt(iShiftDays);
		out.writeInt(iEnrollment);
		out.writeObject(iParentSectionName);
		out.writeObject(iCredit);
		out.writeObject(iTimeStamp);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		iCourseId = new XCourseId(in);
		iSection = new XSection(in);
		iShiftDays = in.readInt();
		iEnrollment = in.readInt();
		iParentSectionName = (String)in.readObject();
		iCredit = (String)in.readObject();
		iTimeStamp = (Date)in.readObject();
	}
}
