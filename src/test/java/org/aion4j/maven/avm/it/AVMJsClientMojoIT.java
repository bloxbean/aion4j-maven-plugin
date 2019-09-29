/*
 * MIT License
 *
 * Copyright (c) 2019 BloxBean Project
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.aion4j.maven.avm.it;

import org.apache.maven.it.Verifier;
import org.apache.maven.it.util.ResourceExtractor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AVMJsClientMojoIT extends BaseTestHelper {

    public void testJsClientZipExists() throws Exception {
        File testDir = ResourceExtractor
            .simpleExtractResources( getClass(), "/projects/js-client" );

        Verifier verifier;

        verifier = new Verifier( testDir.getAbsolutePath());

        verifier.deleteArtifacts("org.aion4j.maven.avm.testing", "js-client", "1.0");

        verifier.addCliOption("-Daion4jPluginVersion=" + getPluginVersion());
        verifier.executeGoal("initialize");

        List<String> goals = new ArrayList<>();
        //goals.add("aion4j:init");
        goals.add("clean");
        goals.add("package");

        verifier.executeGoals(goals);

        verifier.assertFilePresent("target/sample-contract-client-js-client-1.0.zip");
        verifier.assertFilePresent("target/generated-sources/aion4j-js-client/contract.js");
        verifier.assertFilePresent("target/generated-sources/aion4j-js-client/contract-abi.js");
        verifier.assertFilePresent("target/generated-sources/aion4j-js-client/contract-deploy.js");
        verifier.assertFilePresent("target/generated-sources/aion4j-js-client/contract-deploy-abi.js");
    }
}