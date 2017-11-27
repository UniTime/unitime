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
package org.unitime.timetable.gwt.shared;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.ErrorMessage;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.EligibilityCheck;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * @author Tomas Muller
 */
public class SectioningException extends GwtRpcException implements IsSerializable {
	private static final long serialVersionUID = 1L;
	private EligibilityCheck iCheck = null;
	public static enum Type { INFO, WARNING, ERROR };
	private Type iType = null;
	private Map<Long, String> iSectionMessages = null;
	private ArrayList<ErrorMessage> iErrors = null;
	
	public SectioningException() {
		super();
	}
	
	public SectioningException(String message) {
		super(message);
	}

	public SectioningException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public SectioningException withEligibilityCheck(EligibilityCheck check) { iCheck = check; return this; }
	public boolean hasEligibilityCheck() { return iCheck != null; }
	public EligibilityCheck getEligibilityCheck() { return iCheck; }
	
	public SectioningException withTypeInfo() { iType = Type.INFO; return this; }
	public SectioningException withTypeWarning() { iType = Type.WARNING; return this; }
	public SectioningException withTypeError() { iType = Type.ERROR; return this; }
	public boolean hasType() { return iType != null; }
	public boolean isInfo() { return iType != null && iType == Type.INFO; }
	public boolean isWarning() { return iType != null && iType == Type.WARNING; }
	public boolean isError() { return iType != null && iType == Type.ERROR; }
	
	public boolean hasSectionMessages() { return iSectionMessages != null && !iSectionMessages.isEmpty(); }
	public void setSectionMessage(Long classId, String message) {
		if (iSectionMessages == null) iSectionMessages = new HashMap<Long, String>();
		if (classId != null)
			iSectionMessages.put(classId, message);
	}
	public boolean hasSectionMessage(Long classId) {
		if (classId == null || iSectionMessages == null) return false;
		String message = iSectionMessages.get(classId);
		return message != null && !message.isEmpty();
	}
	public String getSectionMessage(Long classId) {
		if (classId == null || iSectionMessages == null) return null;
		return iSectionMessages.get(classId);
	}
	
	public void addError(ErrorMessage error) {
		if (iErrors == null) iErrors = new ArrayList<ErrorMessage>();
		iErrors.add(error);
	}
	public boolean hasErrors() {
		return iErrors != null && !iErrors.isEmpty();
	}
	public ArrayList<ErrorMessage> getErrors() { return iErrors; }
	
	@Override
	public String toString() {
		if (iType != null)
			return iType.name() + ": " + getMessage();
		else
			return super.toString();
	}
}
