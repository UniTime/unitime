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
package org.unitime.timetable.model;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;

import org.unitime.timetable.model.base.BaseConstraintInfo;




/**
 * @author Tomas Muller
 */
public class ConstraintInfo extends BaseConstraintInfo {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public ConstraintInfo () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public ConstraintInfo (java.lang.Long uniqueId) {
		super(uniqueId);
	}

/*[CONSTRUCTOR MARKER END]*/

	public String generateId() {
		Vector ids = new Vector();
		for (Iterator i=getAssignments().iterator();i.hasNext();)
			ids.add(((Assignment)i.next()).getUniqueId());
		Collections.sort(ids);
		StringBuffer sb = new StringBuffer("C");
		for (Enumeration e=ids.elements();e.hasMoreElements();) {
			Integer id = (Integer)e.nextElement();
			sb.append(id.toString());
			if (e.hasMoreElements())
				sb.append("-");
		}
		return sb.toString();
	}

}
