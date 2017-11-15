package com.busyducks;

import org.apache.maven.plugin.AbstractMojo;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.*;
import java.util.Arrays;

/**
 * Goal which touches a timestamp file.
 *
 * @goal touch
 * 
 * @phase process-sources
 */

@Mojo(name = "translate", defaultPhase = LifecyclePhase.COMPILE)
public class MyMojo
    extends AbstractMojo
{
    public static final String fileToken = "$FILE_LANG";
    public static final String callToken = "$CALL_LANG";
    public static final String textToken = "$TEXT";
    public static final String textEQToken = "$TEXT_ESCAPED_QUOTED";
    public static final String textQToken = "$TEXT_QUOTED";


    @FunctionalInterface
    interface DO_TRANS { String apply (String text, String sourceLanguage, String destLanguage) throws MojoExecutionException;}

    /**
     * Location of the file.
     * @parameter expression="${project.build.directory}"
     * @required
     */

    @Parameter(property = "translations")
    private TranslationSetting[] translations;

    //$CALL_LANG $TEXT_ESCAPED_QUOTED
    //$TEXT $TEXT_QUOTED
    @Parameter(property = "call")
    String call;


    public void execute() throws MojoExecutionException
    {
        System.out.println("-------------------------------------------- Foobar!!!!!!!!!!!!!!!!!!! ");

        if(translations == null) {
            printSampleUsage();
            throw new MojoExecutionException("No translations specified.");
        }
        for (TranslationSetting translation : translations)
        {
            System.out.println("__Processing translation__");
            System.out.println("        sourceLang: " + translation.sourceLang);
            System.out.println("     bundlePattern: " + translation.bundlePattern);
            System.out.println("         destLangs: " + translation.destLangs);
            System.out.println("   replaceExisting: " + translation.replaceExistingKeys);

            translation.doTranslations((T,S,D) -> doTranslation(T,S,D));
        }
    }


    public String doTranslation(String text, String sourceLanguage, String destLanguage) throws MojoExecutionException
    {
        String textEQ = "\"" + text.replace("\"", "\\\"") + "\"";
        String textQ = "\"" + text + "\"";
        String theCall = call
                .replace(textEQToken, textEQ)
                .replace(textQToken, textQ)
                .replace(textToken, text)
                .replace(callToken, destLanguage);

        Process proc = null;
        try {
            proc = Runtime.getRuntime().exec(theCall);

            proc.waitFor();

            BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));

            String all = "";
            String line;
            while ((line = reader.readLine()) != null) {
                all += " " + line;
            }
            return all.trim();
        } catch (Exception ex) {
            throw new MojoExecutionException(ex.getMessage());
        }
    }

    public void printSampleUsage()
    {
        System.out.println("Speakingduck Sample Usage");
        String example = "<plugin>\n" +
                "                <groupId>com.busyducks</groupId>\n" +
                "                <artifactId>speakingDuck</artifactId>\n" +
                "                <version>0.9.0 BETA</version>\n" +
                "                <configuration>\n" +
                "                    <call>trans -b :$CALL_LANG $TEXT_ESCAPED_QUOTED</call>\n" +
                "                    <translations>\n" +
                "                        <translation>\n" +
                "                            <bundlePattern>./src/main/resources/non_xml_text_$FILE_LANG.properties</bundlePattern>\n" +
                "                            <!-- $FILE_LANG:$CALL_LANG -->\n" +
                "                            <sourceLang>en:en</sourceLang>\n" +
                "                            <destLangs>de:de, zh:zh, es:es</destLangs>\n" +
                "                            <replaceExistingKeys>false</replaceExistingKeys>\n" +
                "                        </translation>\n" +
                "                    </translations>\n" +
                "                </configuration>\n" +
                "\n" +
                "                <executions>\n" +
                "                    <execution><goals><goal>translate</goal></goals></execution>\n" +
                "                </executions>\n" +
                "            </plugin>";
        System.out.println(example);

    }
}
