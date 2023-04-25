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
package org.unitime.timetable.model.base;

import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.CurriculumClassification;
import org.unitime.timetable.model.CurriculumCourse;
import org.unitime.timetable.model.CurriculumCourseGroup;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BaseCurriculumCourse implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private Float iPercShare;
	private Integer iOrd;
	private Float iSnapshotPercShare;
	private Date iSnapshotPercShareDate;

	private CurriculumClassification iClassification;
	private CourseOffering iCourse;
	private Set<CurriculumCourseGroup> iGroups;

	public BaseCurriculumCourse() {
	}

	public BaseCurriculumCourse(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Id
	@GenericGenerator(name = "curriculum_course_id", strategy = "org.unitime.commons.hibernate.id.UniqueIdGenerator", parameters = {
		@Parameter(name = "sequence", value = "pref_group_seq")
	})
	@GeneratedValue(generator = "curriculum_course_id")
	@Column(name="uniqueid")
	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	@Column(name = "pr_share", nullable = false)
	public Float getPercShare() { return iPercShare; }
	public void setPercShare(Float percShare) { iPercShare = percShare; }

	@Column(name = "ord", nullable = false)
	public Integer getOrd() { return iOrd; }
	public void setOrd(Integer ord) { iOrd = ord; }

	@Column(name = "snapshot_pr_share", nullable = true)
	public Float getSnapshotPercShare() { return iSnapshotPercShare; }
	public void setSnapshotPercShare(Float snapshotPercShare) { iSnapshotPercShare = snapshotPercShare; }

	@Column(name = "snapshot_pr_shr_date", nullable = true)
	public Date getSnapshotPercShareDate() { return iSnapshotPercShareDate; }
	public void setSnapshotPercShareDate(Date snapshotPercShareDate) { iSnapshotPercShareDate = snapshotPercShareDate; }

	@ManyToOne(optional = false)
	@JoinColumn(name = "cur_clasf_id", nullable = false)
	public CurriculumClassification getClassification() { return iClassification; }
	public void setClassification(CurriculumClassification classification) { iClassification = classification; }

	@ManyToOne(optional = false)
	@JoinColumn(name = "course_id", nullable = false)
	public CourseOffering getCourse() { return iCourse; }
	public void setCourse(CourseOffering course) { iCourse = course; }

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "curriculum_course_group",
		joinColumns = { @JoinColumn(name = "cur_course_id") },
		inverseJoinColumns = { @JoinColumn(name = "group_id") })
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, includeLazy = false)
	public Set<CurriculumCourseGroup> getGroups() { return iGroups; }
	public void setGroups(Set<CurriculumCourseGroup> groups) { iGroups = groups; }
	public void addTogroups(CurriculumCourseGroup curriculumCourseGroup) {
		if (iGroups == null) iGroups = new HashSet<CurriculumCourseGroup>();
		iGroups.add(curriculumCourseGroup);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof CurriculumCourse)) return false;
		if (getUniqueId() == null || ((CurriculumCourse)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((CurriculumCourse)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
	public String toString() {
		return "CurriculumCourse["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "CurriculumCourse[" +
			"\n	Classification: " + getClassification() +
			"\n	Course: " + getCourse() +
			"\n	Ord: " + getOrd() +
			"\n	PercShare: " + getPercShare() +
			"\n	SnapshotPercShare: " + getSnapshotPercShare() +
			"\n	SnapshotPercShareDate: " + getSnapshotPercShareDate() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
