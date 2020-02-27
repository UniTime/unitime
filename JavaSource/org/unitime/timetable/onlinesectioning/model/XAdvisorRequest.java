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
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import org.infinispan.commons.marshall.Externalizer;
import org.infinispan.commons.marshall.SerializeWith;
import org.unitime.timetable.model.AdvisorCourseRequest;
import org.unitime.timetable.model.AdvisorSectioningPref;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.model.XCourseRequest.XPreference;

/**
 * @author Tomas Muller
 */
@SerializeWith(XAdvisorRequest.XAdvisorRequestSerializer.class)
public class XAdvisorRequest implements Comparable<XAdvisorRequest>, Serializable, Externalizable {
	private static final long serialVersionUID = 1L;
	
	private int iPriority, iAlternative;
	private boolean iSubstitute;
	private String iCourseName;
	private XCourseId iCourseId;
	private String iCredit, iNote;
	private int iCritical;
	private XTime iFreeTime;
	private List<XPreference> iPreferences = null;
	
	public XAdvisorRequest(AdvisorCourseRequest acr, OnlineSectioningHelper helper, BitSet freeTimePattern) {
		iPriority = acr.getPriority();
		iAlternative = acr.getAlternative();
		iSubstitute = acr.isSubstitute();
		iCritical = (acr.getCritical() == null ? 0 : acr.getCritical().intValue());
		iCourseName = acr.getCourse();
		if (acr.getCourseOffering() != null)
			iCourseId = new XCourseId(acr.getCourseOffering());
		if (acr.getFreeTime() != null)
			iFreeTime = new XTime(acr.getFreeTime(), freeTimePattern);
		iCredit = acr.getCredit();
		iNote = acr.getNotes();
		if (acr.getPreferences() != null && !acr.getPreferences().isEmpty()) {
			iPreferences = new ArrayList<XPreference>();
			for (AdvisorSectioningPref p:acr.getPreferences())
				iPreferences.add(new XPreference(acr, p));
		}
	}
	
	public XAdvisorRequest(ObjectInput in) throws IOException, ClassNotFoundException {
		readExternal(in);
	}
	
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		iPriority = in.readInt();
		iAlternative = in.readInt();
		iSubstitute = in.readBoolean();
		iCourseName = (String)in.readObject();
		if (in.readBoolean())
			iCourseId = new XCourseId(in);
		else 
			iCourseId = null;
		iCredit = (String)in.readObject();;
		iNote = (String)in.readObject();
		iCritical = in.readInt();
		if (in.readBoolean())
			iFreeTime = new XTime(in);
		else
			iFreeTime = null;
		int prefs = in.readInt();
		if (prefs < 0) {
			iPreferences = null;
		} else {
			iPreferences = new ArrayList<XPreference>(prefs);
			for (int i = 0; i < prefs; i++)
				iPreferences.add(new XPreference(in));
		}
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeInt(iPriority);
		out.writeInt(iAlternative);
		out.writeBoolean(iSubstitute);
		out.writeObject(iCourseName);
		if (iCourseId == null) {
			out.writeBoolean(false);
		} else {
			out.writeBoolean(true);
			iCourseId.writeExternal(out);
		}
		out.writeObject(iCredit);
		out.writeObject(iNote);
		out.writeInt(iCritical);
		if (iFreeTime == null) {
			out.writeBoolean(false);
		} else {
			out.writeBoolean(true);
			iFreeTime.writeExternal(out);
		}
		if (iPreferences == null) {
			out.writeInt(-1);
		} else {
			out.writeInt(iPreferences.size());
			for (XPreference p: iPreferences)
				p.writeExternal(out);
		}
	}
	
	public int getPriority() { return iPriority; }
	public int getAlternative() { return iAlternative; }
	public boolean isSubstitute() { return iSubstitute; }
	public String getCourseName() { return iCourseName; }
	public boolean hasCourseName() { return iCourseName != null && !iCourseName.isEmpty(); }
	public XCourseId getCourseId() { return iCourseId; }
	public boolean hasCourseId() { return iCourseId != null; }
	public XTime getFreeTime() { return iFreeTime; }
	public boolean hasFreeTime() { return iFreeTime != null; }
	public String getCredit() { return iCredit; }
	public boolean hasCredit() { return iCredit != null && !iCredit.isEmpty(); }
	public String getNote() { return iNote; }
	public boolean hasNote() { return iNote != null && !iNote.isEmpty(); }
	public int getCritical() { return iCritical; }
	public boolean isCritical() { return iCritical > 0; }
	public boolean hasPreferences() { return iPreferences != null && !iPreferences.isEmpty(); }
	public List<XPreference> getPreferences() { return iPreferences; }
	
	public float getCreditMin() {
		if (iCredit == null || iCredit.isEmpty()) return 0f;
		try {
			return Float.parseFloat(iCredit.replaceAll("\\s",""));
		} catch (NumberFormatException e) {}
		if (iCredit.contains("-")) {
			try {
				return Float.parseFloat(iCredit.substring(0, iCredit.indexOf('-')).replaceAll("\\s",""));
			} catch (NumberFormatException e) {}	
		}
		return 0f;
	}
	
	public float getCreditMax() {
		if (iCredit == null || iCredit.isEmpty()) return 0f;
		try {
			return Float.parseFloat(iCredit.replaceAll("\\s",""));
		} catch (NumberFormatException e) {}
		if (iCredit.contains("-")) {
			try {
				return Float.parseFloat(iCredit.substring(1 + iCredit.indexOf('-')).replaceAll("\\s",""));
			} catch (NumberFormatException e) {}	
		}
		return 0f;
	}

	@Override
	public int compareTo(XAdvisorRequest r) {
		if (getPriority() != r.getPriority())
			return getPriority() < r.getPriority() ? -1 : 1;
		if (getAlternative() != r.getAlternative())
			return getAlternative() < r.getAlternative() ? -1 : 1;
		return 0;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof XAdvisorRequest)) return false;
		XAdvisorRequest ar = (XAdvisorRequest)o;
		return getPriority() == ar.getPriority() && getAlternative() == ar.getAlternative();
	}
	
	@Override
	public int hashCode() {
		return 100 * getPriority() + getAlternative();
	}

	public static class XAdvisorRequestSerializer implements Externalizer<XAdvisorRequest> {
		private static final long serialVersionUID = 1L;

		@Override
		public void writeObject(ObjectOutput output, XAdvisorRequest object) throws IOException {
			object.writeExternal(output);
		}

		@Override
		public XAdvisorRequest readObject(ObjectInput input) throws IOException, ClassNotFoundException {
			return new XAdvisorRequest(input);
		}
	}
}
