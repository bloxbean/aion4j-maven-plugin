/*
 * MIT License
 *
 * Copyright (c) 2019 Aion4j Project
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

package org.aion4j.maven.avm.mojo.codegen;

import org.aion4j.maven.avm.mojo.AVMAbstractBaseMojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;

public abstract class CodeGenBaseMojo extends AVMAbstractBaseMojo {

    @Parameter(property = "generated-test-support-source-dir", defaultValue = "${project.build.directory}/generated-test-sources/aion4j-test-support")
    protected String generatedTestSupportSourceDir;

    @Parameter(property = "generated-client-source-dir", defaultValue = "${project.build.directory}/generated-sources/aion4j-web3j-client")
    protected String generatedClientSourceDir;

    @Parameter(property = "generatedClientType", defaultValue = "web3")
    protected String generatedClientType;

    @Parameter(property = "generated-js-client-source-dir", defaultValue = "${project.build.directory}/generated-sources/aion4j-js-client")
    protected String generatedJsClientSourceDir;

    public String getGeneratedTestSupportSourceDir() {
        return generatedTestSupportSourceDir;
    }

    public void setGeneratedTestSupportSourceDir(String generatedTestSupportSourceDir) {
        this.generatedTestSupportSourceDir = generatedTestSupportSourceDir;
    }

    public String getGeneratedClientSourceDir() {
        return generatedClientSourceDir;
    }

    public void setGeneratedClientSourceDir(String generatedClientSourceDir) {
        this.generatedClientSourceDir = generatedClientSourceDir;
    }

    public String getGeneratedClientType() {
        return generatedClientType;
    }

    public void setGeneratedClientType(String generatedClientType) {
        this.generatedClientType = generatedClientType;
    }

    public String getGeneratedJsClientSourceDir() {
        return generatedJsClientSourceDir;
    }

    public void setGeneratedJsClientSourceDir(String generatedJsClientSourceDir) {
        this.generatedJsClientSourceDir = generatedJsClientSourceDir;
    }

    protected String getTempAbiPath() {
        return getTempArchivePath() + File.separator + project.getBuild().getFinalName() + ".abi";
    }

    protected String getTempArchivePath() {
        return new File(project.getBuild().getOutputDirectory()).getParentFile() + File.separator + "temp-archive"; //target/temp-archive
    }

    protected String getTempJarFile() {
        return getTempArchivePath() + File.separator + project.getBuild().getFinalName() + ".jar";
    }
}
