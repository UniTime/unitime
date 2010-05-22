/**
 * 
 */
package org.unitime.commons.hibernate.util;

import org.dom4j.Document;
import org.unitime.timetable.ApplicationProperties;

/**
 * @author says
 *
 */
public class UniTimeCoreDatabaseUpdate extends DatabaseUpdate {

	/**
	 * @param document
	 * @throws Exception
	 */
	public UniTimeCoreDatabaseUpdate(Document document) throws Exception {
		super(document);
	}
	public UniTimeCoreDatabaseUpdate() throws Exception {
		super();
	}

	@Override
	protected String findDbUpdateFileName() {
		return(ApplicationProperties.getProperty("tmtbl.db.update","dbupdate.xml"));
	}
	@Override
	protected String versionParameterName() {
		return("tmtbl.db.version");
	}
	@Override
	protected String updateName() {
		return("UniTime");
	}

}
