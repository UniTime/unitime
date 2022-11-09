package org.unitime.localization.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.unitime.localization.impl.ExportTranslations.Locale;
import org.unitime.localization.impl.POHelper.Block;
import org.unitime.localization.impl.POHelper.Bundle;
import org.unitime.timetable.action.UniTimeAction;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

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
				
				CloseableHttpClient client = HttpClients.createDefault();
				
				for (Bundle bundle: Bundle.values()) {
					info("bundle:     " + bundle.name());
					for (Block block: helper.getBlocks(bundle)) {
						if (block.getTranslation() == null && !block.getDefaultText().isEmpty()) {
							String source = block.getDefaultText();
							String access = UniTimeAction.guessAccessKey(source);
				        	if (access != null)
				        		source = UniTimeAction.stripAccessKey(source);
							if (source.length() <= 1) {
								block.setTranslation(block.getDefaultText());
								block.fuzzy = true;
								continue;
							}
							info("original:   " + source);
							
							HttpPost post = new HttpPost("https://api-free.deepl.com/v2/translate");
							List<NameValuePair> params = new ArrayList<NameValuePair>(2);
							params.add(new BasicNameValuePair("text", source));
							params.add(new BasicNameValuePair("source_lang", "EN"));
							params.add(new BasicNameValuePair("target_lang", locale.getValue().toUpperCase()));
							params.add(new BasicNameValuePair("formality", "prefer_more"));
							params.add(new BasicNameValuePair("tag_handling", "html"));
							
							post.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
							post.addHeader("Authorization", "DeepL-Auth-Key " + iToken);
							CloseableHttpResponse response = client.execute(post);
							HttpEntity entity = response.getEntity();
							String translated = null;
							if (entity != null) {
								try {
									JsonObject json = JsonParser.parseReader(new InputStreamReader(entity.getContent())).getAsJsonObject();
									debug("response:   " + json);
									translated = json.get("translations").getAsJsonArray().get(0).getAsJsonObject().get("text").getAsString();
								} catch (IllegalStateException e) {
									error(response.toString());
									throw e;
									
								}
								if (translated != null) {
									info("translated: " + translated);
									block.setTranslation(translated);
									block.fuzzy = true;
								}
							}
							response.close();
						}
					}
				}
				client.close();
				
				helper.writePOFile(new File(translations, "UniTime" + org.unitime.timetable.util.Constants.VERSION + "_" + locale + ".po"));
				
			}
			
    	} catch (Exception e) {
			throw new BuildException("Translation failed: " + e.getMessage(), e);
		}
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
