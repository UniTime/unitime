package org.unitime.localization.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.restlet.Client;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.CharacterSet;
import org.restlet.data.Form;
import org.restlet.data.Protocol;
import org.restlet.resource.ClientResource;
import org.unitime.localization.impl.ExportTranslations.Locale;
import org.unitime.localization.impl.POHelper.Block;
import org.unitime.localization.impl.POHelper.Bundle;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;

public class DeepLTranslator {
	private List<Locale> iLocales = new ArrayList<Locale>();
	private Project iProject;
	private File iBaseDir;
	private String iTranslations = "Documentation/Translations";
	private String iToken = null;

	public DeepLTranslator() {}
	
	public void setProject(Project project) {
		iProject = project;
		iBaseDir = project.getBaseDir();
	}
	
	public void setBaseDir(String baseDir) {
		iBaseDir = new File(baseDir);
	}
	
	public Locale createLocale() {
		Locale locale = new Locale();
		iLocales.add(locale);
		return locale;
	}
	
	public void addLocale(Locale locale) {
		iLocales.add(locale);
	}
	
	public void setLocales(String locales) {
		for (String value: locales.split(",")) {
			addLocale(new Locale(value));
		}
	}
	
	public void setToken(String token) {
		iToken = token;
	}
	
    public void info(String message) {
    	if (iProject != null)
    		iProject.log(message);
    	else
    		System.out.println("     [info] " + message);
    }
    
    public void warn(String message) {
    	if (iProject != null)
    		iProject.log(message, Project.MSG_WARN);
    	else
    		System.out.println("  [warning] " +message);
    }
    
    public void debug(String message) {
    	if (iProject != null)
    		iProject.log(message, Project.MSG_DEBUG);
    	else
    		System.out.println("    [debug] " +message);
    }

    public void error(String message) {
    	if (iProject != null)
    		iProject.log(message, Project.MSG_ERR);
    	else
    		System.out.println("    [error] " +message);
    }
    
    public void execute() throws BuildException {
    	try {
			File translations = new File(iBaseDir, iTranslations);
			for (Locale locale: iLocales) {
				info("locale:     " + locale);
				POHelper helper = new POHelper(locale.getValue(), null);
				helper.readPOFile(null, new InputStreamReader(new FileInputStream(new File(translations, "UniTime" + org.unitime.timetable.util.Constants.VERSION + "_" + locale.getValue() + ".po")), StandardCharsets.UTF_8));
				
				List<Protocol> protocols = new ArrayList<Protocol>();
				protocols.add(Protocol.HTTP);
				protocols.add(Protocol.HTTPS);
				Client client = new Client(protocols);
				
				for (Bundle bundle: Bundle.values()) {
					info("bundle:     " + bundle.name());
					for (Block block: helper.getBlocks(bundle)) {
						if (block.getTranslation() == null && !block.getDefaultText().isEmpty()) {
							String source = block.getDefaultText();
							String access = guessAccessKey(source);
				        	if (access != null)
				        		source = stripAccessKey(source);
							if (source.length() <= 1) {
								block.setTranslation(block.getDefaultText());
								block.fuzzy = true;
								continue;
							}
							info("original:   " + source);
							
							ClientResource resource = new ClientResource("https://api-free.deepl.com/v2/translate");
							resource.setNext(client);
							Form form = new Form();
							form.add("text", source);
							form.add("source_lang", "EN");
							form.add("target_lang", locale.getValue().toUpperCase());
							form.add("formality", "prefer_more");
							form.add("tag_handling", "html");
							ChallengeResponse challenge = new ChallengeResponse(new ChallengeScheme("", ""));
							challenge.setRawValue("DeepL-Auth-Key " + iToken);
							resource.setChallengeResponse(challenge);
							
							String response = resource.post(form.getWebRepresentation(CharacterSet.UTF_8)).getText();
							JsonObject json = JsonParser.parseString(response).getAsJsonObject();
							info("Response: " + json);
							String translated = json.get("translations").getAsJsonArray().get(0).getAsJsonObject().get("text").getAsString();
							if (translated != null) {
								info("translated: " + translated);
								block.setTranslation(translated);
								block.fuzzy = true;
							}
							resource.release();
						}
					}
				}
				client.stop();
				
				helper.writePOFile(new File(translations, "UniTime" + org.unitime.timetable.util.Constants.VERSION + "_" + locale + ".po"));
				
			}
			
    	} catch (Exception e) {
    		e.printStackTrace();
			throw new BuildException("Translation failed: " + e.getMessage(), e);
		}
    }
	
	private static RegExp sAcessKeyRegExp = RegExp.compile("<u>(\\w)</u>", "i");
    public static String guessAccessKey(String name) {
		if (name == null || name.isEmpty()) return null;
		MatchResult result = sAcessKeyRegExp.exec(name);
		return (result == null ? "" : result.getGroup(1).toLowerCase());
	}
    
    private static RegExp sStripAcessKeyRegExp = RegExp.compile("(.*)<u>(\\w)</u>(.*)", "i");
    public static String stripAccessKey(String name) {
		if (name == null || name.isEmpty()) return "";
		MatchResult result = sStripAcessKeyRegExp.exec(name);
		return (result == null ? name : (result.getGroup(1) + result.getGroup(2) + result.getGroup(3))).replace("&nbsp;", " ");
	}
	
	public static void main(String[] args) {
		try {
			DeepLTranslator task = new DeepLTranslator();
			task.setBaseDir(System.getProperty("source", "/Users/muller/git/unitime"));
			task.setLocales(System.getProperty("locale", "cs"));
			task.setToken(System.getProperty("token", "FIXME"));
			task.execute();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
