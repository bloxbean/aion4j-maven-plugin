package org.aion4j.maven.avm.it;

import org.apache.maven.it.Verifier;
import org.apache.maven.it.util.ResourceExtractor;
import org.junit.Ignore;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AVMCallMojoIT extends BaseTestHelper {

    public void testCallPlugin() throws Exception {
        File testDir = ResourceExtractor
            .simpleExtractResources( getClass(), "/projects/call-test" );

        Verifier verifier;

        verifier = new Verifier( testDir.getAbsolutePath());

        verifier.deleteArtifacts("org.aion4j.maven.avm.testing", "call-test", "1.0");

        verifier.addCliOption("-Daion4jPluginVersion=" + getPluginVersion());
        verifier.executeGoal("aion4j:init");
        List<String> goals = new ArrayList<>();
        //goals.add("aion4j:init");
        goals.add("package");
        goals.add("aion4j:deploy");
        goals.add("aion4j:call");

        verifier.setEnvironmentVariable("method", "getOwner");
        verifier.setEnvironmentVariable("args", "-T Alice");
        verifier.executeGoals(goals);

        verifier.verifyTextInLog("was deployed successfully to the embedded AVM");
        verifier.verifyTextInLog("Data       : Alice");
    }
}