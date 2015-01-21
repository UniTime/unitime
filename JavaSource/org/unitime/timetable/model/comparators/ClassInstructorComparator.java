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
package org.unitime.timetable.model.comparators;

import java.util.Comparator;

import org.unitime.timetable.model.ClassInstructor;

public class ClassInstructorComparator implements Comparator {
	private ClassComparator iCC = null;
	public ClassInstructorComparator(ClassComparator cc) {
		iCC = cc;
	}
	
	public int compare(Object o1, Object o2) {
		ClassInstructor ci1 = (ClassInstructor)o1;
		ClassInstructor ci2 = (ClassInstructor)o2;
		return iCC.compare(ci1.getClassInstructing(),ci2.getClassInstructing());
	}
}
