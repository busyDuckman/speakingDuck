package com.busyducks;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by duckman on 14/11/17.
 *
 * Setting for translating a file.
 */
public class TranslationSetting
{
    /**
     * A Pattern for identifying bundles. Use $FILE_LANG as the language macro.
     * @parameter
     * @required
     */
    @Parameter(property = "bundlePattern")
    String bundlePattern;

    /**
     * True if existing keys should be replaced, default false.
     * @parameter
     */
    @Parameter(property = "replaceExistingKeys")
    boolean replaceExistingKeys;

    /**
     * Source language (file which holds default text).
     * @parameter
     * @required
     */
    @Parameter(property = "sourceLang")
    String sourceLang;

    /**
     * Languages to translate into.
     * @parameter
     * @required
     */
    @Parameter(property = "destLangs")
    String destLangs;



    public TranslationSetting() {
    }

    /**
     * Does the translations.
     * @param translate Delegation of actual translation.
     * @throws MojoExecutionException
     */
    public void doTranslations(SpeakingDuckMojo.DO_TRANS translate) throws MojoExecutionException
    {
        // Sanitise input
        if(!isValidLanguageToken(sourceLang)) {
            throw new MojoExecutionException("sourceLang must look like: en:en");
        }

        String resolvedSourceBundle = bundlePattern.replace(SpeakingDuckMojo.fileToken, getFileToken(sourceLang));
        if(!fileExists(resolvedSourceBundle)) {
            throw new MojoExecutionException("Can't find the source bundle: " + resolvedSourceBundle);
        }

        // load source input.
        try(FileInputStream in  = new FileInputStream(resolvedSourceBundle)) {
            // get destination languages
            List<String> destTokens = Arrays.stream(destLangs.split(","))
                    .filter(S -> S.trim().length() > 0)
                    .map(String::trim)
                    .collect(Collectors.toList());

            if(!destTokens.stream().allMatch(TranslationSetting::isValidLanguageToken)) {
                throw new MojoExecutionException("destLangs must look like: de:de, zh:zh, en:en-uk");
            }
            System.out.println ("\t-> Found " + destTokens.size() + " destination languages.");

            // a list of destination file names
            List<String> otherFiles = destTokens.stream()
                    .map(TranslationSetting::getFileToken)
                    .map(S-> bundlePattern.replace(SpeakingDuckMojo.fileToken, S))
                    .collect(Collectors.toList());

            // a list of language tokens to give to the translator
            List<String> languageTokens = destTokens.stream()
                    .map(TranslationSetting::getCallToken)
                    .collect(Collectors.toList());

            if(otherFiles.size() != languageTokens.size()) {
                throw new MojoExecutionException("destLangs must look like: de:de, zh:zh, en:en-uk");
            }

            // load source properties
            Properties properties = new Properties();
            properties.load(in);

            // loop through other files
            for (int i = 0; i < otherFiles.size(); i++) {
                boolean anyChanges = false;
                String languageToken = languageTokens.get(i);
                String otherFile = otherFiles.get(i);
                System.out.println("\tTranslating to: " + languageToken);

                if(!fileExists(otherFile)) {
                    throw new MojoExecutionException("Can't find the destination bundle: " + otherFile);
                }

                // load existing translation
                try(FileInputStream in2  = new FileInputStream(otherFile)) {
                    Properties destinationProperties = new Properties();
                    destinationProperties.load(in2);
                    System.out.println("\t-> loaded: " + otherFile);

                    int skippedKeys = 0;
                    // go through every key in the source translation
                    for(Map.Entry<Object, Object> entry : properties.entrySet()) {
                        String key =  entry.getKey().toString();
                        String value =  entry.getValue().toString();
                        System.out.println("\t-> key: " + key);

                        // is there already a translation
                        String translatedValue = destinationProperties.getProperty(key, null);
                        if((translatedValue == null) || replaceExistingKeys) {

                            // do translation, if required.
                            String translation = translate.apply(value, getCallToken(sourceLang), languageToken);
                            anyChanges = true;

                            System.out.println("\t\t-> source: " + value);
                            System.out.println("\t\t->  trans: " + translation);

                            if(translation == null) {
                                throw new MojoExecutionException("Call to translate failed for text: " + value);
                            }

                            //save property
                            destinationProperties.setProperty(key, translation);
                        }
                        else {
                            skippedKeys++;
                        }
                    }
                    if(skippedKeys > 0) {
                        System.out.println("\t-> # skipped existing translations: " + skippedKeys);
                    }

                    // save the updated destination file
                    if(anyChanges) {
                        try (FileOutputStream out = new FileOutputStream(otherFile)) {
                            destinationProperties.store(out, "Generated by speakingDuck");
                            System.out.println("\t-> saved: " + otherFile);
                        } catch (Exception ex) {
                            throw new MojoExecutionException(ex.getMessage());
                        }
                    }

                    System.out.println();

                } catch (MojoExecutionException ex) {
                    throw ex;
                } catch (Exception ex) {
                    throw new MojoExecutionException(ex.getMessage());
                }
            }
        } catch (MojoExecutionException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new MojoExecutionException(ex.getMessage());
        }
    }

    private static boolean isValidLanguageToken(String token) {
        return (token.contains(":") && !token.startsWith(":") && !token.endsWith(":"));
    }

    @Override
    public String toString() {
        return "TranslationSetting{" +
                "bundlePattern='" + bundlePattern + '\'' +
                ", replaceExistingKeys=" + replaceExistingKeys +
                ", sourceLang='" + sourceLang + '\'' +
                ", destLangs='" + destLangs + '\'' +
                '}';
    }

    private static String getFileToken(String langToken) {
        return langToken.substring(0, langToken.indexOf(':')).trim();
    }

    private static String getCallToken(String langToken) {
        return langToken.substring(langToken.indexOf(':')+1).trim();
    }

    private boolean fileExists(String path)
    {
        File f = new File(path);
        return (f.exists() && !f.isDirectory());
    }


}
