# logging

Downloading

Resolving artifacts using Maven

To download "logging" artifact, add below profiles to settings.xml file, located in ~/.m2 folder:
<profiles>
        <profile>

            <repositories>
                <repository>
                    <snapshots>
                        <enabled>
                            false
                        </enabled>
                    </snapshots>
                    <id>
                        bintray-smart-home-oss-m2
                    </id>
                    <name>
                        bintray
                    </name>
                    <url>
                        https://smart-home-oss.bintray.com/m2
                    </url>
                </repository>
            </repositories>

            <pluginRepositories>
                <pluginRepository>
                    <snapshots>
                        <enabled>
                            false
                        </enabled>
                    </snapshots>
                    <id>
                        bintray-smart-home-oss-m2
                    </id>
                    <name>
                        bintray-plugins
                    </name>
                    <url>
                        https://smart-home-oss.bintray.com/m2
                    </url>
                </pluginRepository>
            </pluginRepositories>

            <id>
                bintray
            </id>

        </profile>
    </profiles>

    <activeProfiles>
        <activeProfile>
            bintray
        </activeProfile>
    </activeProfiles>


Uploading

Deploying with Maven

In Maven’s setting.xml file, add the following section to declare your Bintray credentials. Use your API key as your password (not your login password):
    
    <servers>
            <server>
                <id>bintray-smart-home-oss-m2</id>
                <username>"username"</username>
                <password>"password"</password>
            </server>
    </servers>
    
Add the the following Distribution Management section to your project’s pom.xml file to tell Maven to deploy into this package using the credentials you configured in the previous step:
    
    <distributionManagement>
            <repository>
                <id>bintray-smart-home-oss-m2</id>
                <name>smart-home-oss-m2</name>
                <url>https://api.bintray.com/maven/smart-home-oss/m2/logging/;publish=1</url>
            </repository>
    </distributionManagement>