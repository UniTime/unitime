/*
 * UniTime 3.4 - 3.5 (University Timetabling Application)
 * Copyright (C) 2013, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
*/
package org.unitime.timetable.util;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;

import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.StudentSectioningConstants;


/**
 * To prevent concurrency issues (see bug JDK-6609686, http://bugs.sun.com/view_bug.do?bug_id=4264153) and to
 * promote localization, all date and number formating needs should be routed through this class. 
 *  
 * @author Tomas Muller
 */
public class Formats {
	private static StudentSectioningConstants SCT_CONSTANTS = Localization.create(StudentSectioningConstants.class);
	private static GwtConstants GWT_CONSTANTS = Localization.create(GwtConstants.class);
	
	private static final ThreadLocal<FormatBundle> sBundle = new ThreadLocal<FormatBundle>() {
		 @Override
		 protected FormatBundle initialValue() {
            return new FormatBundle();
		 }
	};
	
	public enum Pattern implements Serializable {
		DATE_EXAM_PERIOD(new PatternHolder() {
			public String getPattern() { return GWT_CONSTANTS.examPeriodDateFormat(); }
		}),
		DATE_EVENT(new PatternHolder() {
			public String getPattern() { return GWT_CONSTANTS.eventDateFormat(); }
		}),
		DATE_EVENT_SHORT(new PatternHolder() {
			public String getPattern() { return GWT_CONSTANTS.eventDateFormatShort(); }
		}),
		DATE_EVENT_LONG(new PatternHolder() {
			public String getPattern() { return GWT_CONSTANTS.eventDateFormatLong(); }
		}),
		DATE_TIME_STAMP(new PatternHolder() {
			public String getPattern() { return GWT_CONSTANTS.timeStampFormat(); }
		}),
		DATE_TIME_STAMP_SHORT(new PatternHolder() {
			public String getPattern() { return GWT_CONSTANTS.timeStampFormatShort(); }
		}),
		DATE_REQUEST(new PatternHolder() {
			public String getPattern() { return SCT_CONSTANTS.requestDateFormat(); }
		}),
		DATE_PATTERN(new PatternHolder() {
			public String getPattern() { return SCT_CONSTANTS.patternDateFormat(); }
		}),
		DATE_DAY_OF_WEEK(new PatternHolder() {
			public String getPattern() { return "EEE"; }
		}),
		DATE_MEETING(new PatternHolder() {
			public String getPattern() { return GWT_CONSTANTS.meetingDateFormat(); }
		}),
		DATE_SHORT(new PatternHolder() {
			public String getPattern() { return GWT_CONSTANTS.dateFormatShort(); }
		}),
		TIME_SHORT(new PatternHolder() {
			public String getPattern() { return GWT_CONSTANTS.timeFormatShort(); }
		}),
		;
		
		
		private PatternHolder iHolder;
		Pattern(PatternHolder holder) { iHolder = holder; }
		public String toPattern() { return iHolder.getPattern(); }
		protected PatternHolder holder() { return iHolder; }
	}
		
	public static void removeFormats() {
		sBundle.remove();
	}
	
	public static FormatBundle getFormats() {
		return sBundle.get();
	}
	
	/**
	 * Use this to create a new instance that can be accessed through multiple threads.
	 * For static reference use {@link Formats#getDateFormat(Pattern)}.
	 */
	public static Format<Date> getDateFormat(final String pattern) {
		return new Format<Date>() {
			private static final long serialVersionUID = 1L;
			
			@Override
			public String format(Date t) {
				return getFormats().getDateFormat(pattern).format(t);
			}

			@Override
			public Date parse(String source) throws ParseException {
				return getFormats().getDateFormat(pattern).parse(source);
			}

			@Override
			public String toPattern() {
				return pattern;
			}
		};
	}
	
	/**
	 * Use this to create a new instance that can be accessed through multiple threads.
	 * For static reference use {@link Formats#getDateFormat(Pattern)}.
	 */
	public static Format<Number> getNumberFormat(final String pattern) {
		return new Format<Number>() {
			private static final long serialVersionUID = 1L;

			@Override
			public String format(Number t) {
				return getFormats().getNumberFormat(pattern).format(t);
			}

			@Override
			public Number parse(String source) throws ParseException {
				return getFormats().getNumberFormat(pattern).parse(source);
			}

			@Override
			public String toPattern() {
				return pattern;
			}
		};
	}
	
	/**
	 * Use this to create a new instance that can be accessed through multiple threads.
	 * For static reference use {@link Formats#getDateFormat(Pattern)}.
	 */
	public static Format<Date> getDateFormat(final Pattern pattern) {
		return new Format<Date>() {
			private static final long serialVersionUID = 1L;
			
			@Override
			public String format(Date t) {
				return getFormats().getDateFormat(pattern).format(t);
			}

			@Override
			public Date parse(String source) throws ParseException {
				return getFormats().getDateFormat(pattern).parse(source);
			}

			@Override
			public String toPattern() {
				return pattern.toPattern();
			}
		};
	}
	
	/**
	 * Use this to create a new instance that can be accessed through multiple threads.
	 * For static reference use {@link Formats#getDateFormat(Pattern)}.
	 */
	public static Format<Number> getConcurrentNumberFormat(final Pattern pattern) {
		return new Format<Number>() {
			private static final long serialVersionUID = 1L;
			
			@Override
			public String format(Number t) {
				return getFormats().getNumberFormat(pattern).format(t);
			}

			@Override
			public Number parse(String source) throws ParseException {
				return getFormats().getNumberFormat(pattern).parse(source);
			}

			@Override
			public String toPattern() {
				return pattern.toPattern();
			}
		};
	}
	
	public static class FormatBundle {
		private Map<String, DateFormat> iDateFormats = new Hashtable<String, DateFormat>();
		private Map<String, NumberFormat> iNumberFormats = new Hashtable<String, NumberFormat>();
		
		private FormatBundle() {
		}
		
		public DateFormat getDateFormat(String pattern) {
			DateFormat df = iDateFormats.get(pattern);
			if (df == null) {
				df = new SimpleDateFormat(pattern, Localization.getJavaLocale());
				iDateFormats.put(pattern, df);
			}
			return df;
		}
		
		public DateFormat getDateFormat(Pattern pattern) {
			return getDateFormat(pattern.toPattern());
		}
		
		public NumberFormat getNumberFormat(String pattern) {
			NumberFormat nf = iNumberFormats.get(pattern);
			if (nf == null) {
				nf = new DecimalFormat(pattern, new DecimalFormatSymbols(Localization.getJavaLocale()));
				iNumberFormats.put(pattern, nf);
			}
			return nf;
		}
		
		public NumberFormat getNumberFormat(Pattern pattern) {
			return getNumberFormat(pattern.toPattern());
		}
	}
	
	public static interface Format<T> extends Serializable {
		public String format(T t);
		public T parse(String source) throws ParseException;
		public String toPattern();
	}
		
	protected interface PatternHolder {
		String getPattern();
	}
}
