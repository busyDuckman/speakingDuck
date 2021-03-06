# speakingDuck
A maven translation (i18n) plugin for java properties files.

Configures whatever translation SW you have 
(eg [translation shell](https://github.com/soimort/translate-shell)) 
to translate text phrases in Java property files. Translations are 
saved to other property files, as per the java resource bundles convention.

Speaking duck is in the [Maven Repository](https://search.maven.org/#search%7Cga%7C1%7Ca%3A%22speakingDuck-maven-plugin%22) 
so all that is required to install it, is a reference in your pom.xml. See below.

## Getting started in 2 minutes.
 
### 1) To use 'translation shell' with this plugin, install it first:  

EG. for ubuntu:  

    sudo apt-get install gawk  
    wget git.io/trans  
    chmod +x ./trans  
    sudo mv ./trans /usr/bin/  

This will make the command __trans__ available, which we will now reference in our pom.xml. 

### 2) Add the following plugin to pom.xml and configure accordingly.

    <build>
        <plugins>
            <plugin>
                <groupId>com.busyducks</groupId>
                <artifactId>speakingDuck-maven-plugin</artifactId>
                <version>0.9.7</version>
                <configuration>
                    <call>trans -b :$CALL_LANG $TEXT_ESCAPED_QUOTED</call>
                    <translations>
                        <translation>
                            <bundlePattern>./src/main/resources/generalText_$FILE_LANG.properties</bundlePattern>
                            <sourceLang>en:en</sourceLang>
                            <destLangs>de:de, zh:zh, es:es, ar:ar, bn:bn, fr:fr, hi:hi, ja:ja, jv:jv, ko:ko, pt:pt, ru:ru, zh:zh</destLangs>
                            <replaceExistingKeys>false</replaceExistingKeys>
                        </translation>
                    </translations>
                </configuration>

                <executions>
                <execution><goals><goal>translate</goal></goals></execution>
                </executions>
            </plugin>
        </plugins>
    </build>

__2.1) More detail about \<call\>__   
The \<call\> section is where we configure the call to the translation software (Translation 
shell, in this example).   
   
The following macros are applicable to \<call\>  
  -  __$CALL_LANG__ = The left side of the de:de pair(s) used in \<sourceLang\> and \<destLangs\>  
  -  __$TEXT__ = eg: Text to be "translated"
  -  __$TEXT_ESCAPED_QUOTED__ = "Text to be \\"translated\\"""
  -  __$TEXT_QUOTED__ = "Text to be "translated""
  
__NB:__ Quoting the text ($TEXT_QUOTED) works for some translators, but produces quoted output in others.

__2.2) More detail about \<translation\>__   
You will need one \<translation\>  section per resource bundle.

The following macros are applicable to \<bundlePattern\>
   - __$FILE_LANG__  = The left side of the de:de pair(s) used in \<sourceLang\> and \<destLangs\>  


### 3) Create the source language file 
We will use english, so create:  
  - /src/main/resources/generalText_en.properties
  
Add the following:

    HELLO=Hello world  
    MSG_PAKTOC=Press any key to continue.  
    
### 4) Create the following files
    /src/main/resources/generalText_de.properties  
    /src/main/resources/generalText_hi.properties  
    /src/main/resources/generalText_ja.properties  
    /src/main/resources/generalText_pt.properties
    /src/main/resources/generalText_ar.properties  
    /src/main/resources/generalText_es.properties  
    /src/main/resources/generalText_jv.properties  
    /src/main/resources/generalText_ru.properties
    /src/main/resources/generalText_bn.properties  
    /src/main/resources/generalText_fr.properties  
    /src/main/resources/generalText_ko.properties  
    /src/main/resources/generalText_zh.properties

## 5) Compile

mvn compile


You will see the translations going by and it will take some time to 
complete. However once the translation is done, it is not re-translated 
next time you compile. In this way ou are only translating the new 
text you created since the last compile.

Example output:  

    __Processing translation__
            sourceLang: en:en
         bundlePattern: ./src/main/resources/generalText_$FILE_LANG.properties
             destLangs: de:de, zh:zh, es:es, ar:ar, bn:bn, fr:fr, hi:hi, ja:ja, jv:jv, ko:ko, pt:pt, ru:ru, zh:zh
       replaceExisting: false
        -> Found 13 destination languages.
        
        Translating to: de
        -> loaded: ./src/main/resources/generalText_de.properties
        -> key: MSG_PAKTOC
            -> source: Press any key to continue.  
            ->  trans: Drücken Sie irgendein Schlüssel zu fortsetzen.
        -> key: HELLO
            -> source: Hello world  
            ->  trans: Hallo Welt
        -> saved: ./src/main/resources/generalText_de.properties


## 6) Enjoy
Previously, if you worked in english your software had an audience of 335 million people. 
Now you can reach 3.4+ billion people. Not bad for 2 minutes work. If you have not used 
resource bundles before, read [this guide](https://docs.oracle.com/javase/tutorial/i18n/intro/steps.html).