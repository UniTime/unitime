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
package org.unitime.timetable.onlinesectioning.model;

import java.io.IOException;
import java.io.ObjectInput;

/**
 * @author Tomas Muller
 */
public class XDummyReservation extends XReservation {
	private static final long serialVersionUID = 1L;

	public XDummyReservation() {
		super();
	}
	
	public XDummyReservation(ObjectInput in) throws IOException, ClassNotFoundException {
		super();
		readExternal(in);
	}
	
	public XDummyReservation(XOffering offering) {
		super(XReservationType.Dummy, offering, null);
	}

    /**
     * Dummy reservation is unlimited
     */
    @Override
    public int getReservationLimit() {
        return -1;
    }

    /**
     * Dummy reservation is not applicable to any students
     */
    @Override
    public boolean isApplicable(XStudent student, XCourseId course) {
        return false;
    }
}