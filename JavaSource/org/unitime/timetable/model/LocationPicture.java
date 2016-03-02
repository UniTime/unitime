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

import org.unitime.timetable.model.base.BaseLocationPicture;

/**
 * @author Tomas Muller
 */
public abstract class LocationPicture extends BaseLocationPicture implements Comparable<LocationPicture> {
	private static final long serialVersionUID = 1L;

	public LocationPicture() {
		super();
	}
	
	public void setLocation(Location location) {
		if (this instanceof RoomPicture)
			((RoomPicture)this).setLocation((Room)location);
		else
			((NonUniversityLocationPicture)this).setLocation((NonUniversityLocation)location);
	}
	
	public abstract Location getLocation();
	
	public int compareTo(LocationPicture other) {
		if (isImage() != other.isImage()) return isImage() ? -1 : 1;
		if (getType() == null) {
			if (other.getType() != null) return -1;
		} else {
			if (other.getType() == null) return 1;
			else {
				int cmp = getType().compareTo(other.getType());
				if (cmp != 0) return cmp;
			}
		}
		return getTimeStamp().compareTo(other.getTimeStamp());
	}
	
	public boolean isImage() {
		return getType() == null || AttachmentType.VisibilityFlag.IS_IMAGE.in(getType().getVisibility());
	}
}
