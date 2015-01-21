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

