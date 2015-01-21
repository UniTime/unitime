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

import java.util.List;

import org.unitime.timetable.model.base.BaseStandardEventNote;
import org.unitime.timetable.model.dao.StandardEventNoteDAO;



/**
 * @author Stephanie Schluttenhofer, Zuzana Mullerova, Tomas Muller
 */
public class StandardEventNote extends BaseStandardEventNote {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public StandardEventNote () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public StandardEventNote (java.lang.Long uniqueId) {
		super(uniqueId);
	}

/*[CONSTRUCTOR MARKER END]*/

   public static List findAll() {
        return new StandardEventNoteDAO().getSession().createQuery(
                "select sen from StandardEventNote sen order by sen.reference"
                ).setCacheable(true).list();
    }	
   
   public String getLabel() {
       return getReference()+": "+getNote();
   }

}
