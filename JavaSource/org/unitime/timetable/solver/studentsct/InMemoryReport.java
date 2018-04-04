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
package org.unitime.timetable.solver.studentsct;

import org.cpsolver.ifs.util.CSVFile;

/**
 * @author Tomas Muller
 */
public class InMemoryReport extends CSVFile implements Comparable<InMemoryReport> {
	private static final long serialVersionUID = 1L;
	private String iReference, iName;
	
	public InMemoryReport() {}
	public InMemoryReport(String reference, String name) {
		iReference = reference; iName = name;
	}
	
	public String getReference() { return iReference; }
	public void setReference(String reference) { iReference = reference; }
	public String getName() { return iName; }
	public void setName(String name) { iName = name; }
	@Override
	public int compareTo(InMemoryReport o) {
		return getName().compareTo(o.getName());
	}
}
