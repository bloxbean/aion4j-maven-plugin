Aion4j maven plugin provides build and deployment support for AVM based smart contracts.

Following maven goals has been implemented in this plugin
1. aion4j:clean - To clean storage folder in case of embedded AVM deployment
2. aion4j:deploy - Deploy the contract / dapp jar to an embedded AVM
3. aion4j:init - Initialize the project with required AVM dependencies. Currently it copies all required avm jars
to a lib folder under project's folder. You can also manually create this lib folder or point to an 
existing avm lib folder through the plugin's configuration parameter.
4. aion4j:prepack - Copy org-aion-avm-userlib.jar's classes to target folder for packaging with dapp's jar.

To Do:

1. aion4j:verify - To verify JCL whiltelist classes in the contract
2. aion4j: deploy - Extend the deploy goal for remote deployment.


**Requirement**

Java 10

**Build**
```
$> git clone https://github.com/satran004/aion4j-maven-plugin.git
```
1. Compile the plugin
```
$> ./mvnw clean install -DskipITs
```

2. Run integration tests
```
$>  ./mvnw integration-test
```
