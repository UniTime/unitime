/*
< * Licensed to The Apereo Foundation under one or more contributor license
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
package org.unitime.timetable.onlinesectioning.custom.purdue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.unitime.timetable.onlinesectioning.custom.purdue.SpecialRegistrationInterface.Change;
import org.unitime.timetable.onlinesectioning.custom.purdue.SpecialRegistrationInterface.ChangeNote;
import org.unitime.timetable.onlinesectioning.custom.purdue.SpecialRegistrationInterface.ChangeStatus;
import org.unitime.timetable.onlinesectioning.custom.purdue.SpecialRegistrationInterface.CheckRestrictionsRequest;
import org.unitime.timetable.onlinesectioning.custom.purdue.SpecialRegistrationInterface.Crn;
import org.unitime.timetable.onlinesectioning.custom.purdue.SpecialRegistrationInterface.IncludeReg;
import org.unitime.timetable.onlinesectioning.custom.purdue.SpecialRegistrationInterface.RestrictionsCheckRequest;
import org.unitime.timetable.onlinesectioning.custom.purdue.SpecialRegistrationInterface.SpecialRegistration;
import org.unitime.timetable.onlinesectioning.custom.purdue.SpecialRegistrationInterface.ValidationMode;
import org.unitime.timetable.onlinesectioning.custom.purdue.SpecialRegistrationInterface.ValidationOperation;

/**
 * @author Tomas Muller
 */
public class SpecialRegistrationHelper {
	
	public static boolean hasLastNote(Change change) {
		if (change.notes == null || change.notes.isEmpty()) return false;
		for (ChangeNote n: change.notes)
			if (n.notes != null && !n.notes.isEmpty()) return true;
		return false;
	}
	
	public static String getLastNote(Change change) {
		if (change.notes == null || change.notes.isEmpty()) return null;
		ChangeNote note = null;
		for (ChangeNote n: change.notes)
			if (n.notes != null && !n.notes.isEmpty() && (note == null || note.dateCreated.isBefore(n.dateCreated)))
				note = n;
		return (note == null ? null : note.notes);
	}
	
	public static String note(SpecialRegistration reg, boolean credit) {
		String note = null;
		if (reg.changes != null)
			for (Change ch: reg.changes) {
				if (credit && ch.subject == null && ch.courseNbr == null && hasLastNote(ch))
					note = (note == null ? "" : note + "\n") + getLastNote(ch);
				if (!credit && ch.subject != null && ch.courseNbr != null && hasLastNote(ch) && ch.status != ChangeStatus.approved) {
					String n = getLastNote(ch);
					if (note == null)
						note = n;
					else if (!note.contains(n)) {
						note += "\n" + n;
					}
				}
			}
		return note;
	}
	
	public static RestrictionsCheckRequest createValidationRequest(CheckRestrictionsRequest req, ValidationMode mode, boolean includeRegistration) {
		RestrictionsCheckRequest ret = new RestrictionsCheckRequest();
		ret.sisId = req.studentId;
		ret.term = req.term;
		ret.campus = req.campus;
		ret.mode = mode;
		ret.includeReg = (includeRegistration ? IncludeReg.Y : IncludeReg.N);
		ret.actions = new HashMap<ValidationOperation, List<Crn>>();
		ret.actions.put(ValidationOperation.ADD, new ArrayList<Crn>());
		if (includeRegistration)
			ret.actions.put(ValidationOperation.DROP, new ArrayList<Crn>());
		return ret;
	}
	
	
	public static void addOperation(RestrictionsCheckRequest request, ValidationOperation op, String crn) {
		if (request.actions == null) request.actions = new HashMap<ValidationOperation, List<Crn>>();
		List<Crn> crns = request.actions.get(op);
		if (crns == null) {
			crns = new ArrayList<Crn>();
			request.actions.put(op, crns);
		} else {
			for (Crn c: crns) {
				if (crn.equals(c.crn)) return;
			}
		}
		Crn c = new Crn(); c.crn = crn;
		crns.add(c);
	}
	public static void addCrn(RestrictionsCheckRequest request, String crn) { addOperation(request, ValidationOperation.ADD, crn); }
	public static void dropCrn(RestrictionsCheckRequest request, String crn) { addOperation(request, ValidationOperation.DROP, crn); }
	public static boolean isEmpty(RestrictionsCheckRequest request) { return request.actions == null || request.actions.isEmpty(); }

	public static void addCrn(CheckRestrictionsRequest req, String crn) {
		if (req.changes == null)
			req.changes = createValidationRequest(req, ValidationMode.REG, false);
		addCrn(req.changes, crn);
	}
	
	public static void addAltCrn(CheckRestrictionsRequest req, String crn) {
		if (req.alternatives == null)
			req.alternatives = createValidationRequest(req, ValidationMode.ALT, false);
		addCrn(req.alternatives, crn);
	}
	
	public static boolean isEmpty(CheckRestrictionsRequest req) { 
		return (req.changes == null || isEmpty(req.changes)) && (req.alternatives == null || isEmpty(req.alternatives)); 
	}

}