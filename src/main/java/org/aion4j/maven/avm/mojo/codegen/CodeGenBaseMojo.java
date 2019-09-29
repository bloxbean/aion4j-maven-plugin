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
import org.aion4j.maven.avm.util.IOUtils;
import org.aion4j.maven.avm.util.JarBuilder;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.aion4j.maven.avm.tools.AvmToolsUtil.abiCompile;

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

    /**
     * Actual generated abi file path in target folder
     * @return abi file path
     */
    protected String getAbiPath() {
        return project.getBuild().getDirectory() + File.separator + project.getBuild().getFinalName() + ".abi";
    }

    /**
     * Helper method to generate temp abi file. This can be used if the code generation is done before packaging phase.
     * @param classesFolder
     * @return
     * @throws MojoExecutionException
     */
    protected String generateTempAbi(String classesFolder) throws MojoExecutionException {
        String tempArchive = getTempArchivePath();
        String abiPath = getTempAbiPath();

        String contractMainClass = project.getProperties().getProperty("contract.main.class");

        Path tempArchivePath = Paths.get(tempArchive);
        if(!tempArchivePath.toFile().exists()) {
            try {
                Files.createDirectory(Paths.get(tempArchive));
            } catch (IOException e) {
                throw new MojoExecutionException("Unable to create folder : " + tempArchive);
            }
        }

        getLog().debug("Contract class: " + contractMainClass);
        getLog().debug("Temp Archive path: " + tempArchive);
        getLog().debug("Temp Abi path: " + abiPath);

        List<String> jarInputs = new ArrayList<>();
        jarInputs.add(classesFolder);

        String jarPath = getTempJarFile();

        //Build temp jar archive
        JarBuilder jarBuilder = new JarBuilder();
        try {
            jarBuilder.build(jarInputs, contractMainClass, jarPath);
        } catch (IOException e) {
            throw new MojoExecutionException("Jar creation failed : " + jarPath, e);
        }

        byte[] jarContent = new byte[0];
        try {
            jarContent = IOUtils.readJarContent(getTempJarFile());
        } catch (IOException e) {
            throw new MojoExecutionException("Unable to read jar file : " + jarPath);
        }

        //compile jar to get temp abi file
        abiCompile(getLocalAVMClass(), jarContent, abiPath);
        return abiPath;
    }
}
