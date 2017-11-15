# speakingDuck
A maven translation (i18n) plugin for java properties files.

Configures whatever translation SW you have (eg translation shell[https://github.com/soimort/translate-shell]) 
to translate text phrases in Java property files. Translations are saved to other property files as per the 
pattern setup.

To install translation shell and use it with this plugin:  

    sudo apt-get install gawk  
    wget git.io/trans  
    chmod +x ./trans  
    sudo mv ./trans /usr/bin/  

Add the following plugin toe pom.xml and confiure accoringly.

     <plugin>
                <groupId>com.busyducks</groupId>
                <artifactId>speakingDuck</artifactId>
                <version>0.9.0 BETA</version>
                <configuration>
                    <call>trans -b :$CALL_LANG $TEXT_ESCAPED_QUOTED</call>
                    <translations>
                        <translation>
                            <bundlePattern>./src/main/resources/non_xml_text_$FILE_LANG.properties</bundlePattern>
                            <!-- $FILE_LANG:$CALL_LANG -->
                            <sourceLang>en:en</sourceLang>
                            <destLangs>de:de, zh:zh, es:es</destLangs>
                            <replaceExistingKeys>false</replaceExistingKeys>
                        </translation>
                    </translations>
                </configuration>

                <executions>
                    <execution><goals><goal>translate</goal></goals></execution>
                </executions>
    </plugin>
