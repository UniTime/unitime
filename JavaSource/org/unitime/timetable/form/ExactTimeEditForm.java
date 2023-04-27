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
import java.util.Collections;
import java.util.List;

import org.hibernate.Transaction;
import org.unitime.timetable.action.UniTimeAction;
import org.unitime.timetable.model.ExactTimeMins;
import org.unitime.timetable.model.dao.ExactTimeMinsDAO;


/** 
 * @author Tomas Muller
 */
public class ExactTimeEditForm implements UniTimeForm {
	private static final long serialVersionUID = -7288169370839578510L;
    private List<ExactTimeMins> iExactTimeMins;

	public ExactTimeEditForm() {
		reset();
	}

	@Override
	public void validate(UniTimeAction action) {
	}

	public void reset() {
		iExactTimeMins = new ArrayList<ExactTimeMins>(ExactTimeMinsDAO.getInstance().findAll());
		Collections.sort(iExactTimeMins);
	}
	
	public void save() throws Exception {
		Transaction tx = null;
        try {
            org.hibernate.Session hibSession = (ExactTimeMinsDAO.getInstance()).getSession();
            if (hibSession.getTransaction()==null || !hibSession.getTransaction().isActive())
                tx = hibSession.beginTransaction();
            for (ExactTimeMins ex: iExactTimeMins) {
                hibSession.merge(ex);
            }
            if (tx!=null) tx.commit();
        } catch (Exception e) {
            if (tx!=null) tx.rollback();
            throw e;
        }
	}

	public int getSize() { return iExactTimeMins.size(); }
	public List<ExactTimeMins> getExactTimeMins() {
		return iExactTimeMins;
	}
	public int getMinsPerMtgMin(int idx) {
		return iExactTimeMins.get(idx).getMinsPerMtgMin();
	}
	public int getMinsPerMtgMax(int idx) {
		return iExactTimeMins.get(idx).getMinsPerMtgMax();
	}
	public int getNrTimeSlots(int idx) {
		return iExactTimeMins.get(idx).getNrSlots();
	}
	public int getBreakTime(int idx) {
		return iExactTimeMins.get(idx).getBreakTime();
	}
	public void setMinsPerMtgMin(int idx, int minsPerMtgMin) {
		iExactTimeMins.get(idx).setMinsPerMtgMin(minsPerMtgMin);
	}
	public void setMinsPerMtgMax(int idx, int minsPerMtgMax) {
		iExactTimeMins.get(idx).setMinsPerMtgMax(minsPerMtgMax);
	}
	public void setNrTimeSlots(int idx, int nrTimeSlots) {
		iExactTimeMins.get(idx).setNrSlots(nrTimeSlots);
	}
	public void setBreakTime(int idx, int breakTime) {
		iExactTimeMins.get(idx).setBreakTime(breakTime);
	}
}

