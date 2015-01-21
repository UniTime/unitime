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

import java.util.Collections;
import java.util.Enumeration;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.hibernate.Transaction;
import org.unitime.timetable.model.ExactTimeMins;
import org.unitime.timetable.model.dao.ExactTimeMinsDAO;


/** 
 * @author Tomas Muller
 */
public class ExactTimeEditForm extends ActionForm {
	private static final long serialVersionUID = -7288169370839578510L;
	private String iOp;
    private Vector iExactTimeMins = new Vector();

	public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
		ActionErrors errors = new ActionErrors();
        
		return errors;
	}

	public void reset(ActionMapping mapping, HttpServletRequest request) {
		iOp = null; 
		iExactTimeMins.clear();
		iExactTimeMins.addAll((new ExactTimeMinsDAO()).findAll());
		Collections.sort(iExactTimeMins);
	}
	
	public void save() throws Exception {
		Transaction tx = null;
        try {
            org.hibernate.Session hibSession = (new ExactTimeMinsDAO()).getSession();
            if (hibSession.getTransaction()==null || !hibSession.getTransaction().isActive())
                tx = hibSession.beginTransaction();
            for (Enumeration e=iExactTimeMins.elements();e.hasMoreElements();) {
                ExactTimeMins ex = (ExactTimeMins)e.nextElement();
                hibSession.saveOrUpdate(ex);
            }
            if (tx!=null) tx.commit();
        } catch (Exception e) {
            if (tx!=null) tx.rollback();
            throw e;
        }
	}

	public String getOp() { return iOp; }
	public void setOp(String op) { iOp = op; }
	public int getSize() { return iExactTimeMins.size(); }
	public Vector getExactTimeMins() {
		return iExactTimeMins;
	}
	public int getMinsPerMtgMin(int idx) {
		return ((ExactTimeMins)iExactTimeMins.elementAt(idx)).getMinsPerMtgMin().intValue();
	}
	public int getMinsPerMtgMax(int idx) {
		return ((ExactTimeMins)iExactTimeMins.elementAt(idx)).getMinsPerMtgMax().intValue();
	}
	public int getNrTimeSlots(int idx) {
		return ((ExactTimeMins)iExactTimeMins.elementAt(idx)).getNrSlots().intValue();
	}
	public int getBreakTime(int idx) {
		return ((ExactTimeMins)iExactTimeMins.elementAt(idx)).getBreakTime().intValue();
	}
	public void setMinsPerMtgMin(int idx, int minsPerMtgMin) {
		((ExactTimeMins)iExactTimeMins.elementAt(idx)).setMinsPerMtgMin(new Integer(minsPerMtgMin));
	}
	public void setMinsPerMtgMax(int idx, int minsPerMtgMax) {
		((ExactTimeMins)iExactTimeMins.elementAt(idx)).setMinsPerMtgMax(new Integer(minsPerMtgMax));
	}
	public void setNrTimeSlots(int idx, int nrTimeSlots) {
		((ExactTimeMins)iExactTimeMins.elementAt(idx)).setNrSlots(new Integer(nrTimeSlots));
	}
	public void setBreakTime(int idx, int breakTime) {
		((ExactTimeMins)iExactTimeMins.elementAt(idx)).setBreakTime(new Integer(breakTime));
	}
}

