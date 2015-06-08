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

import org.unitime.timetable.model.base.BaseDistributionObject;

/**
 * @author Tomas Muller
 */
public class DistributionObject extends BaseDistributionObject implements Comparable {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public DistributionObject () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public DistributionObject (java.lang.Long uniqueId) {
		super(uniqueId);
	}

/*[CONSTRUCTOR MARKER END]*/
		
	public String preferenceText(boolean includeSuffix){
		PreferenceGroup prefGroup = this.getPrefGroup();
		if (prefGroup instanceof SchedulingSubpart){
			SchedulingSubpart ss = (SchedulingSubpart)prefGroup;
			return ss.getSchedulingSubpartLabel();
		} else if (prefGroup instanceof Class_) {
			Class_ c = (Class_)prefGroup;
			return c.getClassLabel(includeSuffix);
		} else if (prefGroup instanceof Exam) {
		    Exam x = (Exam)prefGroup;
		    return x.getLabel();
		} else {
			return " unknown "+prefGroup.getClass().getName();
		}
	}
	
	public String preferenceText() {
		return preferenceText(false);
	}
	
	/** Ordering based on sequence numbers or preference groups */
	public int compareTo(Object o) {
		if (o==null || !(o instanceof DistributionObject)) return -1;
		DistributionObject d = (DistributionObject)o;
		if (getSequenceNumber()!=null && d.getSequenceNumber()!=null)
			return getSequenceNumber().compareTo(d.getSequenceNumber());
		if (getPrefGroup() instanceof Comparable && d.getPrefGroup() instanceof Comparable)
			return ((Comparable)getPrefGroup()).compareTo(d.getPrefGroup());
		return getPrefGroup().toString().compareTo(d.getPrefGroup().toString());
	}
}
