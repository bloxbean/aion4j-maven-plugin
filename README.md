### Current release version: 0.4.4

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

For all the supported goals, check this [page](https://github.com/satran004/aion4j-maven-plugin/wiki/Aion4j-Maven-Plugin---Goals).

[Quick Start Guide](https://github.com/satran004/aion4j-maven-plugin/wiki/Quick-Start-with-embedded-AVM)

[User Guide](https://github.com/satran004/aion4j-maven-plugin/wiki/Aion4j-Maven-Plugin---User-Guide)

**Requirement**

Java 10

**Build**
```
$> git clone https://github.com/satran004/aion4j-maven-plugin.git
```
1. Install avm.jar to local .m2 repo for compilation
```
$> ./mvnw initialize
```
2. Compile the plugin
```
$> ./mvnw clean install -DskipITs
```

3. Run integration tests
```
$>  ./mvnw integration-test
```

**How to configure this plugin in a dapp maven project**
1. Define avm lib directory property in "&lt;properties&gt;" section of pom.xml
```
  <properties>
       ...
       <aion4j.plugin.version>x.x.x</aion4j.plugin.version>
       <avm.lib.dir>${project.basedir}/lib</avm.lib.dir>
  </properties>
```
2. Add aion4j plugin to "&lt;plugins&gt;" section of  pom.xml
```
<plugin>
      <groupId>org.aion4j</groupId>
      <artifactId>aion4j-maven-plugin</artifactId>
      <version>${aion4j.plugin.version}</version>
      <configuration>
         <mode>local</mode>
         <avmLibDir>${avm.lib.dir}</avmLibDir>
         <localDefaultAddress>0xa092de3423a1e77f4c5f8500564e3601759143b7c0e652a7012d35eb67b283ca</localDefaultAddress>  
      </configuration>
      <executions>
          <execution>
           <goals>
             <goal>clean</goal>
             <goal>init</goal>
             <goal>class-verifier</goal>
             <goal>prepack</goal>
           </goals>
          </execution>
      </executions>
 </plugin>
```

"mode" can be local or remote.

3. Add avm compile time dependencies to "&lt;dependencies&gt;" section.
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
```
  Note: The above jars will be copies to "avm.lib.dir" using maven initialize (or through aion4j:init goal) if not there. 
  
  **How to use in a dapp maven project**
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

