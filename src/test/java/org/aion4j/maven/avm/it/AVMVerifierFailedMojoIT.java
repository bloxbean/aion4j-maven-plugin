package org.aion4j.maven.avm.it;

import org.apache.maven.it.Verifier;
import org.apache.maven.it.util.ResourceExtractor;

import java.io.File;

public class AVMVerifierFailedMojoIT extends BaseTestHelper {

    public void testDeployPlugin() throws Exception {
        File testDir = ResourceExtractor
            .simpleExtractResources( getClass(), "/projects/verifierFailed-test" );

        Verifier verifier;

        verifier = new Verifier( testDir.getAbsolutePath() );

        verifier.deleteArtifacts("org.aion4j.maven.avm.testing", "verifierFailed-test", "1.0");

        verifier.addCliOption("-Daion4jPluginVersion=" + getPluginVersion());

        verifier.executeGoal("initialize");
        verifier.executeGoal("clean");
        try {
            verifier.executeGoal("package");
        } catch (Exception e) {

        }

        verifier.verifyTextInLog("Class is not on white-list: java/io/File");

    }
}
