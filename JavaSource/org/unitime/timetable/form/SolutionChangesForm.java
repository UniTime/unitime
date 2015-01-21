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
package org.unitime.timetable.form;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.unitime.timetable.solver.interactive.SuggestionsModel;


/** 
 * @author Tomas Muller
 */
public class SolutionChangesForm extends ActionForm {
	private static final long serialVersionUID = 7768482825453181893L;
	private String iOp = null;
	private static String[] sReferences = {"Best Solution", "Initial Solution", "Selected Solution"};
	public static int sReferenceBest = 0;
	public static int sReferenceInitial = 1;
	public static int sReferenceSelected = 2;
	private String iReference = null;
	private boolean iSimpleMode = false;
	private boolean iReversedMode = false;

	public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();
        return errors;
	}

	public void reset(ActionMapping mapping, HttpServletRequest request) {
		iReference = sReferences[sReferenceInitial];
		iSimpleMode = false; iReversedMode = false;
	}
	
	public void load(SuggestionsModel model) {
		iSimpleMode = model.getSimpleMode();
		iReversedMode = model.getReversedMode();
	}
	
	public void save(SuggestionsModel model) {
		model.setSimpleMode(getSimpleMode());
		model.setReversedMode(getReversedMode());
	}	
	
	public boolean getSimpleMode() { return iSimpleMode; }
	public void setSimpleMode(boolean simpleMode) { iSimpleMode = simpleMode; }
	public boolean getReversedMode() { return iReversedMode; }
	public void setReversedMode(boolean reversedMode) { iReversedMode = reversedMode; }
	public String getReference() {
		return iReference;
	}
	public void setReference(String reference) {
		iReference = reference;
	}
	public int getReferenceInt() {
		for (int i=0;i<sReferences.length;i++)
			if (sReferences[i].equals(iReference)) return i;
		return sReferenceBest;
	}
	public void setReferenceInt(int reference) {
		iReference = sReferences[reference];
	}
	public String[] getReferences() { return sReferences; }
	public String getOp() { return iOp; }
	public void setOp(String op) { iOp = op; }
}
