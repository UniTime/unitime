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
package org.unitime.commons.ant;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Hashtable;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * @author Tomas Muller
 */
public class WikiGet extends Task {
    private URL iWikiUrl = null;
    private File iOutDir = null;
    private File iImgDir = null;
    private Hashtable iPages = new Hashtable();
    private Hashtable iImages = new Hashtable();
    private HashSet iRemainingPages = new HashSet();
    private HashSet iDonePages = new HashSet();
    
    private static String sFirstPage = "Main_Page";
    private static String sWikiPrefix = "/";
    private static String sWikiIndex = "/index.php";
    private static String sWikiImagesPrefix = "/images/";
    private static String sWikiSkinsPrefix = "/skins/";
    
    public void setOutput(String outDir) {
        iOutDir = new File(outDir);
        iOutDir.mkdirs();
        iImgDir = new File(iOutDir,"img");
        iImgDir.mkdirs();
    }
    
    public void setUrl(String wikiUrl) {
        try {
            iWikiUrl = new URL(wikiUrl);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
    
    public boolean copy(URL url, File file) {
        try {
            System.out.print("  Get: "+url+" ");
            InputStream is = url.openStream();
            file.getParentFile().mkdirs();
            OutputStream os = new FileOutputStream(file);
            byte[] buffer = new byte[16*1024];
            int read = 0;
            long total = 0;
            while ((read=is.read(buffer))>0) {
                os.write(buffer,0,read);
                total += read;
                System.out.print(".");
            }
            os.flush();os.close();is.close();
            System.out.println(" "+total+" bytes read.");
            return true;
        } catch (IOException ex) {
            System.out.println();
            System.err.println("Error: Unable to get "+url+": "+ex.getMessage());
        }
        return false;
    }
    
    public boolean copyAndParse(URL url, File file, Parser parser) {
        try {
            System.out.println("  Get: "+url);
            BufferedReader is = new BufferedReader(new InputStreamReader(url.openStream()));
            file.getParentFile().mkdirs();
            if (file.getName().equalsIgnoreCase(".html")) file = new File(file.getParentFile(), "index.html");
            PrintWriter pw = new PrintWriter(new FileWriter(file));
            String line = null;
            while ((line=is.readLine())!=null) {
                line = parser.parse(line);
                if (line!=null) pw.println(line);
            }
            pw.flush();pw.close();is.close();
            return true;
        } catch (Exception ex) {
            System.out.println();
            System.err.println("Error: Unable to get "+url+": "+ex.getMessage());
        }
        return false;
    }
    
    public void execute() throws BuildException {
        try {
            if (iWikiUrl==null)
                setUrl("http://wiki.unitime.org");
            if (iOutDir==null)
                setOutput("."+File.separator+"wiki");
            iRemainingPages.add(sFirstPage);
            copy(new URL(iWikiUrl.toString()+"/skins/monobook/headbg.jpg"), new File(iImgDir,"headbg.jpg"));
            while (!iRemainingPages.isEmpty()) {
                String page = (String)iRemainingPages.iterator().next();
                iRemainingPages.remove(page);
                iDonePages.add(page);
                System.out.println("Page: "+page);
                URL pageUrl = new URL(iWikiUrl.toString()+"/"+page);
                copyAndParse(pageUrl, new File(iOutDir, getPageFileName(page)), new PageParser(pageUrl));
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new BuildException(e);
        }
    }
    
    private static DecimalFormat sDF = new DecimalFormat("000");
    
    public String getPageFileName(String pageName) {
        String pageFile = (String)iPages.get(pageName);
        if (pageFile!=null) return pageFile+".html";
        pageFile = pageName.replaceAll("\\%[0-9A-F][0-9A-F]","").replaceAll(":","").replaceAll("/","").replaceAll("\\\\","");
        if (pageFile.length()>32) pageFile = pageFile.substring(0,32);
        int idx = 0;
        while (iPages.values().contains(pageFile + (idx==0?"":sDF.format(idx)))) idx++;
        iPages.put(pageName, pageFile + (idx==0?"":sDF.format(idx)));
        return pageFile + (idx==0?"":sDF.format(idx)) + ".html";
    }
    
    public void addPage(String page) {
        if (!iDonePages.contains(page)) {
            iRemainingPages.add(page);
        }
    }
    
    public String getImageFileName(URL image) {
        String imageFile = (String)iImages.get(image);
        if (imageFile!=null) return imageFile;
        String imageName = new File(image.getFile()).getName();
        String ext = (imageName.lastIndexOf('.')>0?imageName.substring(imageName.lastIndexOf('.')+1):null);
        if (ext!=null) {
            if (ext.indexOf('?')>0) ext = ext.substring(0, ext.indexOf('?'));
            if (ext.length()>10 || ext.indexOf(':')>=0 || ext.indexOf('%')>=0 || ext.indexOf('\\')>=0 || ext.indexOf('/')>=0) {
                imageFile = imageName;
                ext = null;
            } else {
                imageFile = imageName.substring(0, imageName.lastIndexOf('.'));
            }
        } else {
            imageFile = imageName;
        }
        imageFile = imageFile.replaceAll("\\%[0-9A-F][0-9A-F]","").replaceAll(":","").replaceAll("/","").replaceAll("\\\\","");
        if (imageFile.length()>32) imageFile = imageFile.substring(0,32);
        int idx = 0;
        while (iImages.values().contains(imageFile+(idx==0?"":sDF.format(idx))+(ext==null?"":"."+ext))) idx++;
        imageFile = imageFile+(idx==0?"":sDF.format(idx))+(ext==null?"":"."+ext);
        iImages.put(image,imageFile);
        return imageFile;
    }
    
    public static String replace(String source, String substring, String newsubstring) {
        int idx=-1;
        int len1=substring.length();
        int len2=newsubstring.length();
        StringBuffer sb = new StringBuffer(source);
        while ((idx=(sb.toString().indexOf(substring,idx)))>=0) {
            sb.replace(idx,idx+len1,newsubstring);idx+=len2;
        }
        return sb.toString();
    }
    
    public interface Parser {
        public String parse(String line) throws Exception ;
    }
    
    private class PageParser implements Parser {
        private URL iURL = null;
        
        public PageParser(URL url) {
            iURL = url;
        }
        
        private String get(String imageName) {
            try {
                URL imageUrl = null;
                try {
                    imageUrl = new URL(iURL,imageName);
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                };
                if (imageUrl.getHost().equals(iWikiUrl.getHost()) && imageUrl.getPath().startsWith(sWikiPrefix) && !imageUrl.getPath().startsWith(sWikiImagesPrefix) && !imageUrl.getPath().startsWith(sWikiSkinsPrefix)) {
                    if (imageUrl.getPath().startsWith(sWikiIndex)) {
                        if (imageUrl.getFile().indexOf("gen=js")>0 && imageUrl.getFile().indexOf("title=")>0) {
                            int titleIdx = imageUrl.getFile().indexOf("title=")+"title=".length();
                            String title = imageUrl.getFile().substring(titleIdx,imageUrl.getFile().indexOf('&',titleIdx));
                            if ("-".equals(title)) title="site";
                            if (!title.endsWith(".js")) title+=".js";
                            if (title.startsWith("MediaWiki:")) title=title.substring("MediaWiki:".length());
                            File imageFile = new File(iImgDir, title);
                            if (imageFile.exists() || copy(new URL(imageUrl.toString().replaceAll("&amp;","&")), imageFile)) return iImgDir.getName()+"/"+imageFile.getName();
                        }
                        if ((imageUrl.getFile().indexOf("gen=css")>0 || imageUrl.getFile().indexOf("action=raw&ctype=text/css")>0) && imageUrl.getFile().indexOf("title=")>0) {
                            int titleIdx = imageUrl.getFile().indexOf("title=")+"title=".length();
                            String title = imageUrl.getFile().substring(titleIdx,imageUrl.getFile().indexOf('&',titleIdx));
                            if ("-".equals(title)) title="site";
                            if (!title.endsWith(".css")) title+=".css";
                            if (title.startsWith("MediaWiki:")) title=title.substring("MediaWiki:".length());
                            File imageFile = new File(iImgDir, title);
                            if (imageFile.exists() || copy(new URL(imageUrl.toString().replaceAll("&amp;","&")), imageFile)) return iImgDir.getName()+"/"+imageFile.getName();
                        }
                        //System.out.println("Skip: "+imageUrl);
                        return imageUrl.toString();
                    }
                    String pageName = imageUrl.getPath().substring(sWikiPrefix.length());
                    if (pageName.startsWith("Special:") || pageName.startsWith("User:") || pageName.startsWith("UniTime:")) {
                        //System.out.println("Skip: "+imageUrl);
                        return imageUrl.toString();
                    }
                    addPage(pageName);
                    return getPageFileName(pageName);
                }
                if ("mailto".equalsIgnoreCase(imageUrl.getProtocol())) {
                    //System.out.println("Skip: "+imageName);
                    return imageName;
                }
                if (!iWikiUrl.getHost().equals(imageUrl.getHost())) {
                    //System.out.println("Skip: "+imageUrl);
                    return imageUrl.toString();
                }
                if (!imageUrl.getFile().startsWith(sWikiPrefix)) {
                    //System.out.println("Skip: "+imageUrl);
                    return imageUrl.toString();
                }
                File imageFile = new File(iImgDir, getImageFileName(imageUrl));
                if (imageFile.exists()) return iImgDir.getName()+"/"+imageFile.getName();
                if (imageName.toUpperCase().endsWith(".HTML") || imageName.toUpperCase().endsWith(".HTM")) {
                    if (!copyAndParse(imageUrl, imageFile, new PageParser(imageUrl))) return imageUrl.toString();
                } else {
                    if (!copy(imageUrl, imageFile)) return imageUrl.toString();
                }
                return iImgDir.getName()+"/"+imageFile.getName();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
        
        public String check(String line, String prefix, String sufix) {
            int pos = -1;
            while ((pos=line.indexOf(prefix, pos+1))>=0) {
                int begIdx = pos+prefix.length();
                int endIdx = line.indexOf(sufix, begIdx);
                String imageName = line.substring(begIdx,endIdx);
                if (imageName.startsWith("#")) continue;
                if (imageName.startsWith("&amp;")) continue;
                String imageNameTr = get(imageName);
                if (imageNameTr!=null) {
                    line = line.substring(0, begIdx) + imageNameTr + line.substring(endIdx); 
                }
            }
            return line;
        }
        
        public String parse(String line) throws Exception {
            line = check(line, "src=\"", "\"");
            line = check(line, "src=\'", "\'");
            line = check(line, "href=\"", "\"");
            line = check(line, "href=\'", "\'");
            line = check(line, "url(\"", "\"");
            line = check(line, "url(\'", "\'");
            line = check(line, "url(",")");
            line = check(line, "@import \"", "\"");
            line = check(line, "@import \'", "\'");
            return line;
        }
        
    }
    

    public static void main(String[] args) {
        try {
            WikiGet wg = new WikiGet();
            if (args.length>=1) {
                wg.setUrl(args[0]);
            } 
            if (args.length>=2) {
                wg.setOutput(args[1]);
            }
            wg.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
    
}
