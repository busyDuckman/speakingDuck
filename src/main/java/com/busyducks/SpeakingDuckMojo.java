package com.busyducks;

import org.apache.maven.plugin.AbstractMojo;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.*;


/**
 * Goal which translates java properties files to "fill out" the resource bundles.
 * Following the apache "MyMojo" template.
 *
 * @goal touch
 * 
 * @phase process-sources
 */
@Mojo(name = "translate", defaultPhase = LifecyclePhase.COMPILE)
public class SpeakingDuckMojo
    extends AbstractMojo
{
    // Tokens used in substituting text
    public static final String fileToken   = "$FILE_LANG";
    public static final String callToken   = "$CALL_LANG";
    public static final String textToken   = "$TEXT";
    public static final String textEQToken = "$TEXT_ESCAPED_QUOTED";
    public static final String textQToken  = "$TEXT_QUOTED";


    @FunctionalInterface
    interface DO_TRANS { String apply (String text, String sourceLanguage, String destLanguage) throws MojoExecutionException;}

    /**
     * Location of the file.
     * @parameter
     * @required
     */
    @Parameter(property = "translations", required = true)
    private TranslationSetting[] translations;

    /**
     * Call to translation API
     * @parameter
     * @required
     */
    @Parameter(property = "call", required = true)
    String call;


    public void execute() throws MojoExecutionException
    {
        System.out.println("---------------------------------------------------------------------");

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

        System.out.println("---------------------------------------------------------------------");
    }


    public String doTranslation(String text, String sourceLanguage, String destLanguage) throws MojoExecutionException
    {
        if((call == null) || (call.trim().length() == 0)) {
            // No call = default to google service
            throw new MojoExecutionException("No translation command specified.");
        }
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
