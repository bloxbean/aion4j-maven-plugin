package org.aion4j.maven.avm.it;

import org.apache.maven.it.Verifier;
import org.apache.maven.it.util.ResourceExtractor;
import org.junit.Ignore;

import java.io.File;

@Ignore
public class AVMCallMojoIT extends BaseTestHelper {

    public void testCallPlugin() throws Exception {
        File testDir = ResourceExtractor
            .simpleExtractResources( getClass(), "/projects/call-test" );

        Verifier verifier;

        verifier = new Verifier( testDir.getAbsolutePath());

        verifier.deleteArtifacts("org.aion4j.maven.avm.testing", "call-test", "1.0");

        verifier.addCliOption("-Daion4jPluginVersion=" + getPluginVersion());
        verifier.executeGoal("aion4j:init");
        verifier.executeGoal("clean");
        verifier.executeGoal("package");
        verifier.executeGoal("aion4j:deploy");
        verifier.setEnvironmentVariable("args", "-T test");


        verifier.verifyTextInLog("dapp.jar was deployed successfully to the embedded AVM");

    }
}