/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package org.unitime.timetable.gwt.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Query {
	
	private Term iQuery = null;
	
	public Query(String query) {
		iQuery = parse(query.trim());
	}
	
	public boolean match(TermMatcher m) {
		return iQuery.match(m);
	}
	
	public String toString() {
		return iQuery.toString();
	}
	
	private static List<String> split(String query, String... splits) {
		List<String> ret = new ArrayList<String>();
		int bracket = 0;
		boolean quot = false;
		int last = 0;
		boolean white = false;
		loop: for (int i = 0; i < query.length(); i++) {
			if (query.charAt(i) == '"') {
				quot = !quot;
				white = !quot;
				continue;
			}
			if (!quot && query.charAt(i) == '(') { bracket ++; white = false; continue; }
			if (!quot && query.charAt(i) == ')') { bracket --; white = true; continue; }
			if (quot || bracket > 0 || (!white && query.charAt(i) != ' ')) {
				white = (query.charAt(i) == ' ');
				continue;
			}
			white = (query.charAt(i) == ' ');
			String q = query.substring(i).toLowerCase();
			for (String split: splits) {
				if (split.isEmpty() || q.startsWith(split + " ") || q.startsWith(split + "\"") || q.startsWith(split + "(")) {
					String x = query.substring(last, i).trim();
					if (split.isEmpty() && x.endsWith(":")) continue;
					if (!x.isEmpty()) ret.add(x);
					last = i + split.length();
					if (!split.isEmpty())
						i += split.length() - 1;
					continue loop;
				}
			}
		}
		String x = query.substring(last).trim();
		if (!x.isEmpty()) ret.add(x);
		return ret;
	}

	private static Term parse(String query) {
		List<String> splits;
		splits = split(query, "and", "&&", "&");
		if (splits.size() > 1) {
			CompositeTerm t = new AndTerm();
			for (String q: splits)
				t.add(parse(q));
			return t;
		}
		splits = split(query, "or", "||", "|");
		if (splits.size() > 1) {
			CompositeTerm t = new OrTerm();
			for (String q: splits)
				t.add(parse(q));
			return t;
		}
		splits = split(query, "");
		if (splits.size() > 1) {
			CompositeTerm t = new AndTerm();
			boolean not = false;
			for (String q: splits) {
				if (q.equalsIgnoreCase("not") || q.equals("!")) { not = true; continue; }
				if (q.startsWith("!(")) {
					q = q.substring(1); not = true;
				} else if (q.toLowerCase().startsWith("not(")) {
					q = q.substring(3); not = true;
				}
				if (not) {
					t.add(new NotTerm(parse(q)));
					not = false;
				} else {
					t.add(parse(q));
				}
			}
			return t;
		}
		if (query.startsWith("(") && query.endsWith(")")) return parse(query.substring(1, query.length() - 1).trim());
		if (query.startsWith("\"") && query.endsWith("\"")) return new AtomTerm(null, query.substring(1, query.length() - 1).trim());
		int idx = query.indexOf(':');
		if (idx >= 0) {
			return new AtomTerm(query.substring(0, idx).trim().toLowerCase(), query.substring(idx + 1).trim());
		} else {
			return new AtomTerm(null, query);
		}
	}
	
	public static interface Term {
		public boolean match(TermMatcher m);
	}

	public static abstract class CompositeTerm implements Term {
		private List<Term> iTerms = new ArrayList<Term>();

		public CompositeTerm() {}
		
		public CompositeTerm(Term... terms) {
			for (Term t: terms) add(t);
		}
		
		public CompositeTerm(Collection<Term> terms) {
			for (Term t: terms) add(t);
		}
		
		public void add(Term t) { iTerms.add(t); }
		
		protected List<Term> terms() { return iTerms; }
		
		public abstract String getOp();
		
		public String toString() {
			String ret = "";
			for (Term t: terms()) {
				if (!ret.isEmpty()) ret += " " + getOp() + " ";
				ret += t;
			}
			return (terms().size() > 1 ? "(" + ret + ")" : ret);
		}
	}
	
	public static class OrTerm extends CompositeTerm {
		public OrTerm() { super(); }
		public OrTerm(Term... terms) { super(terms); }
		public OrTerm(Collection<Term> terms) { super(terms); }
		
		public String getOp() { return "OR"; }
		
		public boolean match(TermMatcher m) {
			if (terms().isEmpty()) return true;
			for (Term t: terms())
				if (t.match(m)) return true;
			return false;
		}

	}
	
	public static class AndTerm extends CompositeTerm {
		public AndTerm() { super(); }
		public AndTerm(Term... terms) { super(terms); }
		public AndTerm(Collection<Term> terms) { super(terms); }
		
		public String getOp() { return "AND"; }
		
		public boolean match(TermMatcher m) {
			for (Term t: terms())
				if (!t.match(m)) return false;
			return true;
		}
	}
	
	public static class NotTerm implements Term {
		private Term iTerm;
		
		public NotTerm(Term t) {
			iTerm = t;
		}
		
		public boolean match(TermMatcher m) {
			return !iTerm.match(m);
		}
		
		public String toString() { return "NOT " + iTerm.toString(); }

	}

	public static class AtomTerm implements Term {
		private String iAttr, iBody;
		
		public AtomTerm(String attr, String body) {
			iAttr = attr; iBody = body;
		}
		
		public boolean match(TermMatcher m) {
			return m.match(iAttr, iBody);
		}

		public String toString() { return (iAttr == null ? "" : iAttr + ":") + iBody; }
	}
	
	public static interface TermMatcher {
		public boolean match(String attr, String term);
	}
	
	public static void main(String[] args) {
		System.out.println(parse("(dept:1124 or dept:1125) and area:bio"));
		System.out.println(parse("a \"b c\" or ddd f \"x:x\" x: s !(band or org) (a)or(b)"));
		System.out.println(parse("! f (a)or(b) d !d not x s"));
		System.out.println(parse(""));
		System.out.println(split("(a \"b c\")  ddd f", ""));
		System.out.println(split("a \"b c\" OR not ddd f", "or"));
		System.out.println(split("a or((\"b c\" or dddor) f) q", "or"));
	}
	
	
}