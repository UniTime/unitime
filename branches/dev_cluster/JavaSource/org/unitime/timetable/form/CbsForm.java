/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2008 - 2010, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
*/
package org.unitime.timetable.form;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;


/** 
 * @author Tomas Muller
 */
public class CbsForm extends ActionForm {
	private static final long serialVersionUID = -3460105920222103981L;
	public static double sDefaultLimit = 25.0;
	public static int sDefaultType = 0;
	private String iOp = null;
	private String iType = null;
	private static String[] sTypes = new String[] {"Variable - oriented","Constraint - oriented"};
	private double iLimit = sDefaultLimit;

	public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();
        
        return errors;
	}

	public void reset(ActionMapping mapping, HttpServletRequest request) {
		iOp = null; iLimit = sDefaultLimit; iType = sTypes[sDefaultType];
	}
	
	public String getOp() { return iOp; }
	public void setOp(String op) { iOp = op; }
	public String getType() { return iType; }
	public void setType(String type) { iType = type; }
	public int getTypeInt() {
		for (int i=0;i<sTypes.length;i++)
			if (sTypes[i].equals(iType)) return i;
		return 0;
	}
	public void setTypeInt(int type) {
		if (type < 0 || type >= sTypes.length) type = 0;
		iType = sTypes[type];
	}
	public double getLimit() { return iLimit; }
	public void setLimit(double limit) { iLimit = limit; }
	public String[] getTypes() { return sTypes; }
}

