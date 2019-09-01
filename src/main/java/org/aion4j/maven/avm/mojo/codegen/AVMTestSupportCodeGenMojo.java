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

import org.aion4j.avm.codegenerator.api.testsupport.TestSupportGenerator;
import org.aion4j.avm.codegenerator.util.FileUtil;
import org.aion4j.maven.avm.util.IOUtils;
import org.aion4j.maven.avm.util.JarBuilder;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.List;

import static org.aion4j.maven.avm.tools.AvmToolsUtil.abiCompile;

@Mojo(name = "generate-test-support", defaultPhase = LifecyclePhase.GENERATE_TEST_SOURCES)
public class AVMTestSupportCodeGenMojo extends CodeGenBaseMojo {

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        String classesFolder = project.getBuild().getOutputDirectory();
        String generatedTestSourcesDir = getGeneratedTestSupportSourceDir();
        String tempArchive = getTempArchivePath();
        String abiPath = getTempAbiPath();

        //Add grneratedTestSource directory to test compile path.
        project.addTestCompileSourceRoot(generatedTestSourcesDir);

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
        getLog().debug("Generated Test Source Dir:" + generatedTestSourcesDir);

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

        File abiFile = new File(abiPath);
        if(!abiFile.exists()) {
            throw new MojoExecutionException("ABI file is not found : " + abiPath);
        }

        String abiStr = null;
        try {
            abiStr = FileUtil.readFile(abiPath);
        } catch (IOException e) {
            throw new MojoExecutionException("Unable to read ABI file : " + abiPath, e);
        }

        File generatedTestSourceFile = new File(generatedTestSourcesDir);
        if(!generatedTestSourceFile.exists())
            generatedTestSourceFile.mkdirs();

        boolean isVerbose = getLog().isDebugEnabled()? true: false;
        //Generate code
        TestSupportGenerator testSupportGenerator = new TestSupportGenerator(isVerbose);
        try {
            testSupportGenerator.generate(abiStr, generatedTestSourcesDir);
        } catch (IOException e) {
            throw new MojoExecutionException("Could not generate Avm Test Support code", e);
        }
    }

    //Dummy implementation. Not required
    @Override
    protected void preexecuteLocalAvm() throws MojoExecutionException {

    }

    @Override
    protected void executeLocalAvm(ClassLoader avmClassloader, Object localAvmInstance) throws MojoExecutionException {

    }

    @Override
    protected void postExecuteLocalAvm(Object localAvmInstance) throws MojoExecutionException {

    }

    @Override
    protected Object getLocalAvmImplInstance(ClassLoader avmClassloader) {
        return null;
    }


}
