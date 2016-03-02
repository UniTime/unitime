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

import org.unitime.timetable.model.base.BaseAttachmentType;
import org.unitime.timetable.model.dao.AttachmentTypeDAO;

public class AttachmentType extends BaseAttachmentType implements Comparable<AttachmentType> {
	private static final long serialVersionUID = 1L;
	
	public static enum VisibilityFlag {
		IS_IMAGE,
		ROOM_PICTURE_TYPE,
		SHOW_ROOMS_TABLE,
		SHOW_ROOM_TOOLTIP,
		;
		public int flag() { return 1 << ordinal(); }
		public boolean in(int flags) {
			return (flags & flag()) != 0;
		}
		public int set(int flags) {
			return (in(flags) ? flags : flags + flag());
		}
		public int clear(int flags) {
			return (in(flags) ? flags - flag() : flags);
		}
	}

	public AttachmentType() {
		super();
	}

	@Override
	public int compareTo(AttachmentType a) {
		int cmp = getLabel().compareTo(a.getLabel());
		if (cmp != 0) return cmp;
		return getUniqueId().compareTo(a.getUniqueId());
	}
	
	public static List<AttachmentType> listTypes(int flag) {
		if (flag == 0)
			return AttachmentTypeDAO.getInstance().getSession().createQuery(
					"from AttachmentType order by label").setCacheable(true).list();
		else
			return AttachmentTypeDAO.getInstance().getSession().createQuery(
					"from AttachmentType where bit_and(visibility, :flag) = :flag order by label").setInteger("flag", flag).setCacheable(true).list();
	}
	
	public static List<AttachmentType> listTypes(VisibilityFlag... flags) {
		int flag = 0;
		for (VisibilityFlag f: flags) {
			flag = f.set(flag);
		}
		return listTypes(flag);
	}
	
	public static AttachmentType findByReference(org.hibernate.Session hibSession, String reference) {
		if (reference == null) return null;
		return (AttachmentType)hibSession.createQuery(
				"from AttachmentType t where t.reference = :reference")
				.setString("reference", reference)
				.setMaxResults(1).setCacheable(true).uniqueResult();
	}
}
