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

import org.infinispan.commons.marshall.Externalizer;
import org.infinispan.commons.marshall.SerializeWith;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.TeachingClassRequest;
import org.unitime.timetable.model.TeachingResponsibility;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.util.NameInterface;

/**
 * @author Tomas Muller
 */
@SerializeWith(XInstructor.XInstructorSerializer.class)
public class XInstructor implements Serializable, Externalizable, NameInterface {
	private static final long serialVersionUID = 1L;
	private Long iUniqueId;
	private String iExternalId;
	private String iName;
	private String iEmail;
	private boolean iAllowOverlap;
	private boolean iDisplay = true;
	private boolean iInstructing = false;
	private String iFName, iMName, iLName, iTitle;
	
	public XInstructor() {}
	
	public XInstructor(ObjectInput in) throws IOException, ClassNotFoundException {
		readExternal(in);
	}
	
	public XInstructor(ClassInstructor instructor, OnlineSectioningHelper helper) {
		iUniqueId = instructor.getInstructor().getUniqueId();
		iExternalId = instructor.getInstructor().getExternalUniqueId();
		iName = helper.getInstructorNameFormat().format(instructor.getInstructor());
		iEmail = instructor.getInstructor().getEmail();
		iDisplay = instructor.getClassInstructing().isDisplayInstructor() &&
				(instructor.getResponsibility() == null || !instructor.getResponsibility().hasOption(TeachingResponsibility.Option.auxiliary));
		iAllowOverlap = !instructor.isLead();
		iInstructing = true;
		if (instructor.getTeachingRequest() != null)
			for (TeachingClassRequest tcr: instructor.getTeachingRequest().getClassRequests())
				if (tcr.getTeachingClass().equals(instructor.getClassInstructing())) {
					iAllowOverlap = tcr.isCanOverlap(); break;
				}
		iFName = instructor.getInstructor().getFirstName();
		iMName = instructor.getInstructor().getMiddleName();
		iLName = instructor.getInstructor().getLastName();
		iTitle = instructor.getInstructor().getAcademicTitle();
	}
	
	public XInstructor(DepartmentalInstructor instructor, TeachingClassRequest tcr, OnlineSectioningHelper helper) {
		iUniqueId = instructor.getUniqueId();
		iExternalId = instructor.getExternalUniqueId();
		iName = helper.getInstructorNameFormat().format(instructor);
		iEmail = instructor.getEmail();
		iDisplay = false;
		iAllowOverlap = tcr.isCanOverlap();
		iInstructing = false;
		iFName = instructor.getFirstName();
		iMName = instructor.getMiddleName();
		iLName = instructor.getLastName();
		iTitle = instructor.getAcademicTitle();
	}
	
	public XInstructor(Long uniqueId, String externalId, String name, String email, boolean display, boolean allowOverlap, boolean instructing) {
		iUniqueId = uniqueId;
		iExternalId = externalId;
		iName = name;
		iEmail = email;
		iDisplay = display;
		iAllowOverlap = allowOverlap;
		iInstructing = instructing;
	}
	
	public Long getIntructorId() {
		return iUniqueId;
	}
	
	public String getExternalId() {
		return iExternalId;
	}
	
	public boolean hasExternalId() {
		return iExternalId != null && !iExternalId.isEmpty();
	}
	
	public String getName() {
		return iName;
	}
	
	public String getEmail() {
		return iEmail;
	}
	
    @Override
    public String toString() {
        return getName();
    }
    
    public boolean isAllowOverlap() { return iAllowOverlap; }
    
    public boolean isAllowDisplay() { return iDisplay; }
    
    public boolean isInstructing() { return iInstructing; }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof XInstructor)) return false;
        return getIntructorId().equals(((XInstructor)o).getIntructorId());
    }
    
    @Override
    public int hashCode() {
        return Long.valueOf(getIntructorId()).hashCode();
    }

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		iUniqueId = in.readLong();
		iExternalId = (String)in.readObject();
		iName = (String)in.readObject();
		iEmail = (String)in.readObject();
		iAllowOverlap = in.readBoolean();
		iDisplay = in.readBoolean();
		iInstructing = in.readBoolean();
		iFName = (String)in.readObject();
		iMName = (String)in.readObject();
		iLName = (String)in.readObject();
		iTitle = (String)in.readObject();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeLong(iUniqueId);
		out.writeObject(iExternalId);
		out.writeObject(iName);
		out.writeObject(iEmail);
		out.writeBoolean(iAllowOverlap);
		out.writeBoolean(iDisplay);
		out.writeBoolean(iInstructing);
		out.writeObject(iFName);
		out.writeObject(iMName);
		out.writeObject(iLName);
		out.writeObject(iTitle);
	}

	@Override
	public String getFirstName() {
		return iFName;
	}

	@Override
	public String getMiddleName() {
		return iMName;
	}

	@Override
	public String getLastName() {
		return iLName;
	}

	@Override
	public String getAcademicTitle() {
		return iTitle;
	}

	@Override
	public String getExternalUniqueId() {
		return iExternalId;
	}
	
	public static class XInstructorSerializer implements Externalizer<XInstructor> {
		private static final long serialVersionUID = 1L;

		@Override
		public void writeObject(ObjectOutput output, XInstructor object) throws IOException {
			object.writeExternal(output);
		}

		@Override
		public XInstructor readObject(ObjectInput input) throws IOException, ClassNotFoundException {
			return new XInstructor(input);
		}
	}
}
