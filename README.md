[![Build Status](https://travis-ci.org/bloxbean/aion4j-maven-plugin.svg?branch=master)](https://travis-ci.org/bloxbean/aion4j-maven-plugin)
[![Gitter](https://badges.gitter.im/aion4j/community.svg)](https://gitter.im/aion4j/community?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)

### Latest Release: 1.0.0
  * Avm 2.0
  * AvmArchetype: Since 0.30
  * [Release notes](https://github.com/bloxbean/aion4j-maven-plugin/wiki/Release-Notes)
  
### Release: 0.8.0
  * Avm 1.5
  * AvmArchetype: Since 0.30
  * [Release notes](https://github.com/bloxbean/aion4j-maven-plugin/wiki/Release-Notes)
  
### Release: 0.7.2
  * Avm 1.4+ [Latest Tooling Jars](https://github.com/aionnetwork/AVM/releases/tag/latest-tooling)
  * AvmArchetype: Since 0.30
  * [Release notes](https://github.com/bloxbean/aion4j-maven-plugin/wiki/Release-Notes)
  

**Aion4j maven plugin** provides build and deployment support for AVM based smart contracts.

Following maven goals has been implemented in this plugin :

1. aion4j:clean - Default phase for this goal is "clean". To clean storage folder in case of embedded AVM deployment. 
2. aion4j:deploy - Deploy the contract / dapp jar to an embedded AVM. This goal needs to invoked explicitely from the command line.
3. aion4j:call - Call contract method 
4. aion4j:contract-txn
5. aion4j:init - Default phase for this goal is "initialize". It initializes the project with required AVM dependencies. Currently it copies all required avm jars to a lib folder under project's folder. You can also manually create this lib folder or point to an existing avm lib folder through the plugin's configuration parameter.
6. aion4j:prepack - Default phase "prepare-package". Copy org-aion-avm-userlib.jar's classes to target folder for packaging with dapp's jar.
7. aion4j:class-verifier - To verify JCL whiltelist classes in the contract
8. aion4j:deploy - Extend the deploy goal for remote deployment.
9. aion4j:postpack - Post process the jar after build. Example: Abi Compile to process @Callable annotation. Default phase "package"
10. aion4j:get-receipt - Get the receipt for a transaction hash
11. aion4j:get-logs - Get logs
12. aion4j:transfer - Transfer aion coins from one account to another.
13. aion4j:get-balance - Get balance of an account.
14: aion4j:account - Create and manage account, topup account with test Aion tokens.
15. aion4j:create-invokable - Create invokable transaction (meta-transaction)
16. aion4j:generate-js-client - Generate javascript client code to invoke the smart contract.
17. aion4j:generate-test-support - Generate helper classes for unit test.

For all the supported goals, check this [page](https://github.com/bloxbean/aion4j-maven-plugin/wiki/Aion4j-Maven-Plugin---Goals-(In-Progress)).

[Aion4j Maven Plugin Guide @ The OAN Docs](https://developer.theoan.com/docs/custom-kits/tutorials/maven-cli#)

[Quick Start Guide](https://github.com/bloxbean/aion4j-maven-plugin/wiki/Quick-Start-with-embedded-AVM)

[User Guide](https://github.com/bloxbean/aion4j-maven-plugin/wiki/Aion4j-Maven-Plugin---User-Guide)

[Aion4j Maven Plugin vs Avm Version Matrix](https://github.com/bloxbean/aion4j-maven-plugin/wiki/Aion4j-Maven-Plugin--&--Avm--version-matrix)

**Requirement**

Java 10

**Build**
```
$> git clone https://github.com/bloxbean/aion4j-maven-plugin.git
```
1. Install avm.jar to local .m2 repo for compilation
```
$> ./mvnw initialize
```
2. Compile the plugin
```
$> ./mvnw clean install
```

3. Run integration tests
```
$>  ./mvnw integration-test -DskipITs=false
```

**How to configure this plugin in a Smart Contract maven project**
1. Define avm lib directory property in "&lt;properties&gt;" section of pom.xml
```
  <properties>
       ...
       <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
       <java.version>10</java.version>
       <maven.compiler.source>${java.version}</maven.compiler.source>
       <maven.compiler.target>${java.version}</maven.compiler.target>
        
       <aion4j.plugin.version>x.x.x</aion4j.plugin.version>
       <avm.lib.dir>${project.basedir}/lib</avm.lib.dir>
       
       <contract.main.class>[contract-class]</contract.main.class>
  </properties>
```
2. Add aion4j plugin to "&lt;plugins&gt;" section of  pom.xml
```
<plugins>
    ...
    
    <plugin>
        <groupId>org.aion4j</groupId>
        <artifactId>aion4j-maven-plugin</artifactId>
        <version>${aion4j.plugin.version}</version>
        <configuration>
            <mode>local</mode>
            <avmLibDir>${avm.lib.dir}</avmLibDir>
            <localDefaultAddress>0xa092de3423a1e77f4c5f8500564e3601759143b7c0e652a7012d35eb67b283ca
            </localDefaultAddress>
        </configuration>
        <executions>
            <execution>
                <goals>
                    <goal>clean</goal>
                    <goal>init</goal>
                    <goal>prepack</goal>
                    <goal>class-verifier</goal>
                    <goal>postpack</goal>
                </goals>
            </execution>
        </executions>
    </plugin>
    <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-install-plugin</artifactId>
        <version>2.5</version>
        <executions>
            <execution>
                <phase>initialize</phase>
                <goals>
                    <goal>install-file</goal>
                </goals>
                <configuration>
                    <groupId>${groupId}</groupId>
                    <artifactId>${artifactId}-avm</artifactId>
                    <version>${version}</version>
                    <packaging>jar</packaging>
                    <file>${avm.lib.dir}/avm.jar</file>
                </configuration>
            </execution>
        </executions>
    </plugin>
    <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-jar-plugin</artifactId>
        <version>3.1.2</version>
        <configuration>
            <archive>
                <manifest>
                    <mainClass>${contract.main.class}</mainClass>
                </manifest>
            </archive>
        </configuration>
    </plugin>
    <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-surefire-plugin</artifactId>
        <version>3.0.0-M3</version>
        <dependencies>
            <dependency>
                <groupId>org.apache.maven.surefire</groupId>
                <artifactId>surefire-junit47</artifactId>
                <version>3.0.0-M3</version>
            </dependency>
        </dependencies>
    </plugin>
</plugins>
```

"mode" can be local or remote.

3. Add avm compile and test time dependencies to "&lt;dependencies&gt;" section.
```
    <dependency>
       <groupId>org.aion</groupId>
       <artifactId>avm-api</artifactId>
       <version>0.0.0</version>
       <scope>system</scope>
       <systemPath>${avm.lib.dir}/org-aion-avm-api.jar</systemPath>
     </dependency>
     <dependency>
        <groupId>org.aion</groupId>
        <artifactId>avm-userlib</artifactId>
        <version>0.0.0</version>
        <scope>system</scope>
        <systemPath>${avm.lib.dir}/org-aion-avm-userlib.jar</systemPath>
     </dependency>
     <dependency>
         <groupId>org.aion</groupId>
         <artifactId>avm-tooling</artifactId>
         <version>0.0.0</version>
         <scope>system</scope>
         <systemPath>${avm.lib.dir}/org-aion-avm-tooling.jar</systemPath>
      </dependency>

      <!-- Test dependencies -->
      <dependency>
         <groupId>junit</groupId>
         <artifactId>junit</artifactId>
         <version>4.11</version>
         <scope>test</scope>
      </dependency>
      <!-- Don't change the following dependency. This is required to compile & run test cases. The avm.jar for the project will be
        installed into the local maven repository during mvn initialize phase. -->
      <dependency>
          <groupId>${groupId}</groupId>
          <artifactId>${artifactId}-avm</artifactId>
          <version>${version}</version>
          <scope>test</scope>
      </dependency>
```
  Note: The above jars will be copies to "avm.lib.dir" using maven initialize (or through aion4j:init goal) if not there. 
  
  **How to use in a Smart Contract maven project (Embedded Avm or local mode)**
  1. Copy required avm dependencies.
  ```
  $>mvn initialize
  ```
  2. Build the project
  ```
  $>mvn clean package
  ```
  3. To deploy to an embedded AVM
  ```
  $>mvn aion4j:deploy
  ```
**For Remote deployment (Remote mode or Aion Kernel)**

For deployment and testing on Aion Kernel, add a new profile for remote mode under profiles section in pom.xml.
```
<profiles>
       ...
        <profile>
            <id>remote</id>
            <build>
                <plugins>
                    <plugin>
                        <groupId>org.aion4j</groupId>
                        <artifactId>aion4j-maven-plugin</artifactId>
                        <version>${aion4j.plugin.version}</version>
                        <configuration>
                            <mode>remote</mode>
                            <avmLibDir>${avm.lib.dir}</avmLibDir>
                            <web3rpcUrl></web3rpcUrl>
                        </configuration>
                        <executions>
                            <execution>
                                <goals>
                                    <goal>clean</goal>
                                    <goal>init</goal>
                                    <goal>prepack</goal>
                                    <goal>class-verifier</goal>
                                    <goal>postpack</goal>
                                </goals>
                            </execution>
                        </executions>
                    </plugin>
                </plugins>
            </build>
        </profile>
    </profiles>
```
**How to use in a Smart Contract maven project (Remote Aion Kernel)**

  1. To deploy to a remote Aion Kernel
  ```
  $>mvn aion4j:deploy -Premote
  ```

**How to contribute ?**

Please check this [contribution guide](https://github.com/bloxbean/aion4j-maven-plugin/blob/master/CONTRIBUTING.md)
