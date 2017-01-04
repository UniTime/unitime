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
import java.util.Set;
import java.util.TreeSet;

import org.unitime.commons.Debug;
import org.unitime.timetable.model.base.BaseClassDurationType;
import org.unitime.timetable.model.dao.ClassDurationTypeDAO;
import org.unitime.timetable.util.duration.DurationModel;
import org.unitime.timetable.util.duration.MinutesPerWeek;

public class ClassDurationType extends BaseClassDurationType implements Comparable<ClassDurationType> {
	private static final long serialVersionUID = 1L;

	public ClassDurationType() {
		super();
	}
	
	public DurationModel getModel() {
		try {
			return (DurationModel)Class.forName(getImplementation()).getConstructor(String.class).newInstance(getParameter());
		} catch (Exception e) {
			Debug.error("Failed to create duration model: " + e.getMessage(), e);
			return new MinutesPerWeek(null);
		}
	}
	
	@SuppressWarnings("unchecked")
	public static List<ClassDurationType> findAll() {
		return (List<ClassDurationType>)ClassDurationTypeDAO.getInstance().getSession().createQuery("from ClassDurationType order by label").setCacheable(true).list();
	}
	
	public static ClassDurationType findByReference(String reference, org.hibernate.Session hibSession) {
		return (ClassDurationType) (hibSession == null ? ClassDurationTypeDAO.getInstance().getSession() : hibSession).createQuery(
				"from ClassDurationType where reference = :reference")
				.setString("reference", reference)
				.setCacheable(true).uniqueResult();
	}
	
	public static ClassDurationType findDefaultType(Long sessionId, org.hibernate.Session hibSession) {
		return (ClassDurationType) (hibSession == null ? ClassDurationTypeDAO.getInstance().getSession() : hibSession).createQuery(
				"select t from Session s inner join s.defaultClassDurationType t where s.uniqueId = :sessionId")
				.setLong("sessionId", sessionId)
				.setCacheable(true).uniqueResult();
	}
	
	public static Set<ClassDurationType> findAllVisible(ClassDurationType include) {
		@SuppressWarnings("unchecked")
		Set<ClassDurationType> ret = new TreeSet<ClassDurationType>(
				ClassDurationTypeDAO.getInstance().getSession().createQuery("from ClassDurationType where visible = true order by label").setCacheable(true).list());
		if (include != null && !ret.contains(include))
			ret.add(include);
		return ret;
	}

	@Override
	public int compareTo(ClassDurationType t) {
		int cmp = getLabel().compareTo(t.getLabel());
		if (cmp != 0) return cmp;
		return (getUniqueId() == null ? new Long(0) : getUniqueId()).compareTo(t.getUniqueId() == null ? 0 : t.getUniqueId());
	}
}
