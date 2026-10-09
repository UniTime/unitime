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
package org.unitime.timetable.gwt.shared;

import java.io.Serializable;
import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;

public class GwtLocalDate implements Serializable, IsSerializable, Comparable<GwtLocalDate> {
	private static final long serialVersionUID = -7673835470789581628L;
	private int iDay, iMonth, iYear;
	
	public GwtLocalDate() {}
	
	public GwtLocalDate(int year, int month, int day) {
		iDay = day; iMonth = month; iYear = year;
	}
	
	@SuppressWarnings("deprecation")
	public GwtLocalDate(Date date) {
		iDay = date.getDate(); iMonth = date.getMonth() + 1; iYear = date.getYear() + 1900;
	}

	@SuppressWarnings("deprecation")
	public Date getDate() { return new Date(iYear - 1900, iMonth - 1, iDay); }
	
	public void setDay(int day) { iDay = day; }
	public int getDay() { return iDay; }
	
	public void setMonth(int month) { iMonth = month; }
	public int getMonth() { return iMonth; }
	
	public void setYear(int year) { iYear = year; }
	public int getYear() { return iYear; }
	
	@Override
	public String toString() { return getDay() + "." + getMonth() + "." + getYear(); }
	
	@Override
	public int hashCode() { return 372 * getYear() + 31 * getMonth() + getDay(); }
	
	@Override
	@SuppressWarnings("deprecation")
	public boolean equals(Object o) {
		if (o == null) return false;
		if (o instanceof GwtLocalDate) {
			GwtLocalDate d = (GwtLocalDate) o;
			return d.getYear() == getYear() && d.getMonth() == getMonth() && d.getDay() == getDay();
		}
		if (o instanceof Date) {
			Date d = (Date) o;
			return d.getDay() == getDay() && d.getMonth() + 1 == getMonth() && d.getYear() + 1900 == getYear();
		}
		return false;
	}

	@Override
	public int compareTo(GwtLocalDate o) {
		if (getYear() != o.getYear())
			return (getYear() < o.getYear() ? -1 : 1);
		if (getMonth() != o.getMonth())
			return (getMonth() < o.getMonth() ? -1 : 1);
		if (getDay() != o.getDay())
			return (getDay() < o.getDay() ? -1 : 1);
		return 0;
	}
	
	public boolean before(GwtLocalDate o) {
		return compareTo(o) < 0;
	}
	public boolean after(GwtLocalDate o) {
		return compareTo(o) > 0;
	}
}
