package org.aion4j.maven.avm.it;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.it.Verifier;
import org.apache.maven.it.util.ResourceExtractor;

public class AVMDeployMojoIT extends BaseTestHelper {

    public void testDeployPlugin() throws Exception {
        File testDir = ResourceExtractor
            .simpleExtractResources( getClass(), "/projects/deploy-test" );

        Verifier verifier;

        verifier = new Verifier( testDir.getAbsolutePath());

        verifier.deleteArtifacts("org.aion4j.maven.avm.testing", "deploy-test", "1.0");

        verifier.addCliOption("-Daion4jPluginVersion=" + getPluginVersion());
        verifier.addCliOption("-DskipTests");

        verifier.executeGoal("initialize");
        List<String> goals = new ArrayList<>();
        //goals.add("aion4j:init");
        goals.add("package");
        goals.add("aion4j:deploy");

        verifier.executeGoals(goals);

        verifier.verifyTextInLog("was deployed successfully to the embedded AVM");

    }
}