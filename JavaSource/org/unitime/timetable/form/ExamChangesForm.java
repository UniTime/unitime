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
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.util.ComboBoxLookup;

/**
 * @author Tomas Muller
 */
public class ExamChangesForm extends ExamReportForm {
	private static final long serialVersionUID = 4093360180461644275L;
	protected static final ExaminationMessages MSG = Localization.create(ExaminationMessages.class);
	
	private String iChage = ExamChange.Initial.name();
    private boolean iReverse = false;
    private boolean iNoSolver = false;
    
    public static enum ExamChange {
    	Initial, Best, Saved,
    }
    
    public String getChangeName() {
		try {
			return getChangeName(ExamChange.valueOf(iChage));
		} catch (Exception e) {
			return iChage;
		}
	}
    public String getChangeName(ExamChange ch) {
    	switch (ch) {
		case Best: return MSG.changeBest();
		case Initial: return MSG.changeInitial();
		case Saved: return MSG.changeSaved();
		default:
			return ch.name();
		}
    }
    
    public boolean getReverse() { return iReverse; }
    public void setReverse(boolean reverse) { iReverse = reverse; }
    public String getChangeType() { return iChage; }
    public void setChangeType(String changeType) { iChage = changeType; }
    public List<ComboBoxLookup> getChangeTypes() {
		List<ComboBoxLookup> ret = new ArrayList<ComboBoxLookup>();
		for (ExamChange ch: ExamChange.values())
			ret.add(new ComboBoxLookup(getChangeName(ch), ch.name()));
		return ret;
	}
    
    @Override
	public void reset() {
    	super.reset();
    	iChage = ExamChange.Initial.name(); iReverse = false;
    }
    
    public void load(SessionContext session) {
        super.load(session);
        setReverse("1".equals(session.getUser().getProperty("ExamChanges.reverse", "0")));
        setChangeType(session.getUser().getProperty("ExamChanges.changeType", ExamChange.Initial.name()));
    }
        
    public void save(SessionContext session) {
        super.save(session);
        session.getUser().setProperty("ExamChanges.reverse", getReverse() ? "1" : "0");
        session.getUser().setProperty("ExamChanges.changeType", getChangeType());
    }
    
    public boolean getNoSolver() { return iNoSolver;}
    public void setNoSolver(boolean noSolver) { iNoSolver = noSolver;}
}
