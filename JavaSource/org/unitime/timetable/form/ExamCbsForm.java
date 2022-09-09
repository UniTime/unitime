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

import java.util.ArrayList;
import java.util.List;

import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.ExaminationMessages;
import org.unitime.timetable.action.UniTimeAction;
import org.unitime.timetable.util.ComboBoxLookup;


/** 
 * @author Tomas Muller
 */
public class ExamCbsForm implements UniTimeForm {
	private static final long serialVersionUID = -8811101882445836746L;
	protected static final ExaminationMessages MSG = Localization.create(ExaminationMessages.class);
	
	public static double sDefaultLimit = 25.0;
	public static Type sDefaultType = Type.VariableOriented;
	private String iOp = null;
	private String iType = null;
	public static enum Type {
		VariableOriented, ConstraintOriented,
	}
	private double iLimit = sDefaultLimit;
	
	public ExamCbsForm() { reset(); }

	@Override
	public void validate(UniTimeAction action) {
	}

	@Override
	public void reset() {
		iOp = null; iLimit = sDefaultLimit; iType = sDefaultType.name();
	}
	
	public String getOp() { return iOp; }
	public void setOp(String op) { iOp = op; }
	public String getType() { return iType; }
	public void setType(String type) { iType = type; }
	public int getTypeInt() {
		for (Type t: Type.values())
			if (t.name().equals(iType)) return t.ordinal();
		return 0;
	}
	public void setTypeInt(int type) { iType = Type.values()[type].name(); }
	public double getLimit() { return iLimit; }
	public void setLimit(double limit) { iLimit = limit; }
	public List<ComboBoxLookup> getTypes() {
		List<ComboBoxLookup> ret = new ArrayList<ComboBoxLookup>();
		for (Type t: Type.values()) {
			ret.add(new ComboBoxLookup(getTypeLabel(t), t.name()));
		}
		return ret;
	}
	public static String getTypeLabel(Type t) {
		switch(t) {
		case VariableOriented: return MSG.cbsVariableOriented();
		case ConstraintOriented: return MSG.cbsConstraintOriented();
		default: return t.name();
		}
	}
}

