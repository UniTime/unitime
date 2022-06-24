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
package org.unitime.timetable.onlinesectioning.custom.purdue;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.reflect.TypeToken;

public class EduNavInterface {

	public static class Result {
		public static final Type TYPE_LIST = new TypeToken<ArrayList<Result>>() {}.getType();
		public String id;
		public String name;
		public Long updateDate;
		public Plan plan;
		public String owner;
		public String type;
		public String status;
	}
	
	public static class Plan {
		public List<Term> terms;
		public Goal goal;
	}
	
	public static class Term {
		public String id;
		public String name;
		public List<Element> elements;
	}
	
	public static class Element {
		public String id;
		public String name;
		public Double hours;
		public String type;
		public Boolean registered;
		public List<Rule> rules;
	}
	
	public static class Rule {
		public String ruleId;
		public Label label;
	}
	
	public static class Label {
		public String text;
		public String target;
		public String originalTarget;
	}
	
	public static class Goal {
		public List<Program> programs;
	}
	
	public static class Program {
		public String id;
		public String level;
		public String campus;
		public String college;
		public String degree;
		public String catalogYear;
		public String collegeName;
		public List<Major> majors;
	}
	
	public static class Major {
		public String id;
		public String name;
		public String degree;
		public String infoUrl;
		public String description;
		public String catalogTerm;
	}
}
