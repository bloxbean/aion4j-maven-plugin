package org.aion4j.maven.avm.it;

import java.io.File;
import org.apache.maven.it.Verifier;
import org.apache.maven.it.util.ResourceExtractor;

public class AVMInitializeMojoIT extends BaseTestHelper {

    public void testDeployPlugin() throws Exception {
        File testDir = ResourceExtractor
            .simpleExtractResources( getClass(), "/projects/deploy-test" );

        Verifier verifier;

        verifier = new Verifier( testDir.getAbsolutePath() );

        verifier.deleteArtifacts("org.aion4j.maven.avm.testing", "deploy-test", "1.0");

        verifier.addCliOption("-Daion4jPluginVersion=" + getPluginVersion());

        verifier.executeGoal("aion4j:init");

        String targetLibFolder = testDir.getAbsolutePath() + File.separator + "libtest";

        assertTrue(new File(targetLibFolder, "avm.jar").exists());
        assertTrue(new File(targetLibFolder, "org-aion-avm-api.jar").exists());
        assertTrue(new File(targetLibFolder, "org-aion-avm-userlib.jar").exists() );

    }
}
