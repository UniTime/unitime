/* Taken from Hibernate 3.2 (org.hibertane.pretty.Formater) as it is no longer present in Hibernate 3.5 */
package org.unitime.commons.hibernate.util;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.StringTokenizer;

import org.hibernate.util.StringHelper;

public class PrettyFormatter {
	
	private static final Set BEGIN_CLAUSES = new HashSet();
	private static final Set END_CLAUSES = new HashSet();
	private static final Set LOGICAL = new HashSet();
	private static final Set QUANTIFIERS = new HashSet();
	private static final Set DML = new HashSet();
	private static final Set MISC = new HashSet();
	static {
		
		BEGIN_CLAUSES.add("left");
		BEGIN_CLAUSES.add("right");
		BEGIN_CLAUSES.add("inner");
		BEGIN_CLAUSES.add("outer");
		BEGIN_CLAUSES.add("group");
		BEGIN_CLAUSES.add("order");

		END_CLAUSES.add("where");
		END_CLAUSES.add("set");
		END_CLAUSES.add("having");
		END_CLAUSES.add("join");
		END_CLAUSES.add("from");
		END_CLAUSES.add("by");
		END_CLAUSES.add("join");
		END_CLAUSES.add("into");
		END_CLAUSES.add("union");
		
		LOGICAL.add("and");
		LOGICAL.add("or");
		LOGICAL.add("when");
		LOGICAL.add("else");
		LOGICAL.add("end");
		
		QUANTIFIERS.add("in");
		QUANTIFIERS.add("all");
		QUANTIFIERS.add("exists");
		QUANTIFIERS.add("some");
		QUANTIFIERS.add("any");
		
		DML.add("insert");
		DML.add("update");
		DML.add("delete");
		
		MISC.add("select");
		MISC.add("on");
		//MISC.add("values");

	}
	
	String indentString = "    ";
	String initial = "\n    ";

	boolean beginLine = true;
	boolean afterBeginBeforeEnd = false;
	boolean afterByOrSetOrFromOrSelect = false;
	boolean afterValues = false;
	boolean afterOn = false;
	boolean afterBetween = false;
	boolean afterInsert = false;
	int inFunction = 0;
	int parensSinceSelect = 0;
	private LinkedList parenCounts = new LinkedList();
	private LinkedList afterByOrFromOrSelects = new LinkedList();

	int indent = 1;

	StringBuffer result = new StringBuffer();
	StringTokenizer tokens;
	String lastToken;
	String token;
	String lcToken;
	
	public PrettyFormatter(String sql) {
		tokens = new StringTokenizer(
				sql, 
				"()+*/-=<>'`\"[]," + StringHelper.WHITESPACE, 
				true
			);
	}

	public PrettyFormatter setInitialString(String initial) {
		this.initial = initial;
		return this;
	}
	
	public PrettyFormatter setIndentString(String indent) {
		this.indentString = indent;
		return this;
	}
	
	public String format() {
		
		result.append(initial);
		
		while ( tokens.hasMoreTokens() ) {
			token = tokens.nextToken();
			lcToken = token.toLowerCase();
			
			if ( "'".equals(token) ) {
				String t;
				do {
					t = tokens.nextToken();
					token += t;
				} 
				while ( !"'".equals(t) );
			}		
			else if ( "\"".equals(token) ) {
				String t;
				do {
					t = tokens.nextToken();
					token += t;
				} 
				while ( !"\"".equals(t) );
			}
			
			if ( afterByOrSetOrFromOrSelect && ",".equals(token) ) {
				commaAfterByOrFromOrSelect();
			}
			else if ( afterOn && ",".equals(token) ) {
				commaAfterOn();
			}
			
			else if ( "(".equals(token) ) {
				openParen();
			}
			else if ( ")".equals(token) ) {
				closeParen();
			}

			else if ( BEGIN_CLAUSES.contains(lcToken) ) {
				beginNewClause();
			}

			else if ( END_CLAUSES.contains(lcToken) ) {
				endNewClause(); 
			}
			
			else if ( "select".equals(lcToken) ) {
				select();
			}
			
			else if ( DML.contains(lcToken) ) {
				updateOrInsertOrDelete();
			}
			
			else if ( "values".equals(lcToken) ) {
				values();
			}
			
			else if ( "on".equals(lcToken) ) {
				on();
			}
			
			else if ( afterBetween && lcToken.equals("and") ) {
				misc();
				afterBetween = false;
			}
			
			else if ( LOGICAL.contains(lcToken) ) {
				logical();
			}
			
			else if ( isWhitespace(token) ) {
				white();
			}
			
			else {
				misc();
			}
			
			if ( !isWhitespace( token ) ) lastToken = lcToken;
			
		}
		return result.toString();
	}

	private void commaAfterOn() {
		out();
		indent--;
		newline();
		afterOn = false;
		afterByOrSetOrFromOrSelect = true;
	}

	private void commaAfterByOrFromOrSelect() {
		out();
		newline();
	}

	private void logical() {
		if ( "end".equals(lcToken) ) indent--;
		newline();
		out();
		beginLine = false;
	}

	private void on() {
		indent++;
		afterOn = true;
		newline();
		out();
		beginLine = false;
	}

	private void misc() {
		out();
		if ( "between".equals(lcToken) ) {
			afterBetween = true;
		}
		if (afterInsert) {
			newline();
			afterInsert = false;
		}
		else {
			beginLine = false;
			if ( "case".equals(lcToken) ) {
				indent++;
			}
		}
	}

	private void white() {
		if ( !beginLine ) {
			result.append(" ");
		}
	}
	
	private void updateOrInsertOrDelete() {
		out();
		indent++;
		beginLine = false;
		if ( "update".equals(lcToken) ) newline();
		if ( "insert".equals(lcToken) ) afterInsert = true;
	}

	private void select() {
		out();
		indent++;
		newline();
		parenCounts.addLast( new Integer(parensSinceSelect) );
		afterByOrFromOrSelects.addLast( new Boolean(afterByOrSetOrFromOrSelect) );
		parensSinceSelect = 0;
		afterByOrSetOrFromOrSelect = true;
	}

	private void out() {
		result.append(token);
	}

	private void endNewClause() {
		if (!afterBeginBeforeEnd) {
			indent--;
			if (afterOn) {
				indent--;
				afterOn=false;
			}
			newline();
		}
		out();
		if ( !"union".equals(lcToken) ) indent++;
		newline();
		afterBeginBeforeEnd = false;
		afterByOrSetOrFromOrSelect = "by".equals(lcToken) 
				|| "set".equals(lcToken)
				|| "from".equals(lcToken);
	}

	private void beginNewClause() {
		if (!afterBeginBeforeEnd) {
			if (afterOn) {
				indent--;
				afterOn=false;
			}
			indent--;
			newline();
		}
		out();
		beginLine = false;
		afterBeginBeforeEnd = true;
	}

	private void values() {
		indent--;
		newline();
		out();
		indent++;
		newline();
		afterValues = true;
	}

	private void closeParen() {
		parensSinceSelect--;
		if (parensSinceSelect<0) {
			indent--;
			parensSinceSelect = ( (Integer) parenCounts.removeLast() ).intValue();
			afterByOrSetOrFromOrSelect = ( (Boolean) afterByOrFromOrSelects.removeLast() ).booleanValue();
		}
		if ( inFunction>0 ) {
			inFunction--;
			out();
		}
		else {
			if (!afterByOrSetOrFromOrSelect) {
				indent--;
				newline();
			}
			out();
		}
		beginLine = false;
	}

	private void openParen() {
		if ( isFunctionName( lastToken ) || inFunction>0 ) {
			inFunction++;
		}
		beginLine = false;
		if ( inFunction>0 ) {
			out();
		}
		else {
			out();
			if (!afterByOrSetOrFromOrSelect) {
				indent++;
				newline();
				beginLine = true;
			}
		}
		parensSinceSelect++;
	}

	private static boolean isFunctionName(String tok) {
		final char begin = tok.charAt(0);
		final boolean isIdentifier = Character.isJavaIdentifierStart( begin ) || '"'==begin;
		return isIdentifier && 
				!LOGICAL.contains(tok) && 
				!END_CLAUSES.contains(tok) &&
				!QUANTIFIERS.contains(tok) &&
				!DML.contains(tok) &&
				!MISC.contains(tok);
	}

	private static boolean isWhitespace(String token) {
		return StringHelper.WHITESPACE.indexOf(token)>=0;
	}
	
	private void newline() {
		result.append("\n");
		for ( int i=0; i<indent; i++ ) {
			result.append(indentString);
		}
		beginLine = true;
	}

	public static void main(String[] args) {
		if ( args.length>0 ) System.out.println( 
			new PrettyFormatter( StringHelper.join(" ", args) ).format()
		);
		System.out.println( 
			new PrettyFormatter("insert into Address (city, state, zip, \"from\") values (?, ?, ?, 'insert value')").format()
		);
		System.out.println( 
			new PrettyFormatter("delete from Address where id = ? and version = ?").format()
		);
		System.out.println( 
			new PrettyFormatter("update Address set city = ?, state=?, zip=?, version = ? where id = ? and version = ?").format()
		);
		System.out.println( 
			new PrettyFormatter("update Address set city = ?, state=?, zip=?, version = ? where id in (select aid from Person)").format()
		);
		System.out.println( 
			new PrettyFormatter("select p.name, a.zipCode, count(*) from Person p left outer join Employee e on e.id = p.id and p.type = 'E' and (e.effective>? or e.effective<?) join Address a on a.pid = p.id where upper(p.name) like 'G%' and p.age > 100 and (p.sex = 'M' or p.sex = 'F') and coalesce( trim(a.street), a.city, (a.zip) ) is not null order by p.name asc, a.zipCode asc").format()
		);
		System.out.println( 
			new PrettyFormatter("select ( (m.age - p.age) * 12 ), trim(upper(p.name)) from Person p, Person m where p.mother = m.id and ( p.age = (select max(p0.age) from Person p0 where (p0.mother=m.id)) and p.name like ? )").format()
		);
		System.out.println( 
			new PrettyFormatter("select * from Address a join Person p on a.pid = p.id, Person m join Address b on b.pid = m.id where p.mother = m.id and p.name like ?").format()
		);
		System.out.println( 
			new PrettyFormatter("select case when p.age > 50 then 'old' when p.age > 18 then 'adult' else 'child' end from Person p where ( case when p.age > 50 then 'old' when p.age > 18 then 'adult' else 'child' end ) like ?").format()
		);
	}

}
