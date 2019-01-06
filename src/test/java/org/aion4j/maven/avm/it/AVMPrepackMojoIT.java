package org.aion4j.maven.avm.it;

import java.io.File;
import org.apache.maven.it.Verifier;
import org.apache.maven.it.util.ResourceExtractor;

public class AVMPrepackMojoIT extends BaseTestHelper {

    public void testDeployPlugin() throws Exception {
        File testDir = ResourceExtractor
            .simpleExtractResources( getClass(), "/projects/prepack-test" );

        Verifier verifier;

        verifier = new Verifier( testDir.getAbsolutePath() );

        verifier.deleteArtifacts("org.aion4j.maven.avm.testing", "prepack-test", "1.0");

        verifier.addCliOption("-Daion4jPluginVersion=" + getPluginVersion());

        verifier.executeGoal("aion4j:init");
        verifier.executeGoal("compile");
        verifier.executeGoal("aion4j:prepack");

        String classesFolder = testDir.getAbsolutePath() + File.separator + "target" + File.separator + "classes";

        assertTrue(new File(classesFolder, "org" + File.separator + "aion").exists());
    }
}
