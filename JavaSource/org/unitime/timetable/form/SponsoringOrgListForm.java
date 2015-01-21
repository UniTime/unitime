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
 * @author Zuzana Mullerova, Tomas Muller
 */
public class SponsoringOrgListForm extends ActionForm {

	private static final long serialVersionUID = 2371430972689067425L;

	private String iOp;
	
	public ActionErrors validate(
			ActionMapping mapping,
			HttpServletRequest request) {

			return null;
		}
	
	public void reset(ActionMapping mapping, HttpServletRequest request) {
		
	}
    
	public String getOp() {return iOp;}
	public void setOp(String op) {iOp = op;}
	
}
