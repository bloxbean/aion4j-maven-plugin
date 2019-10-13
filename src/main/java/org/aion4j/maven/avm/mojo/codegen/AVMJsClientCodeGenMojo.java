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

package org.aion4j.maven.avm.mojo.codegen;

import org.aion4j.avm.codegenerator.generators.clientjs.JsClientGenerator;
import org.aion4j.avm.codegenerator.generators.clientjs.VueJsClientGenerator;
import org.aion4j.avm.codegenerator.util.FileUtil;
import org.aion4j.maven.avm.util.ZipBuilder;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mojo(name = "generate-js-client", defaultPhase = LifecyclePhase.PACKAGE)
public class AVMJsClientCodeGenMojo extends CodeGenBaseMojo {

    @Override
    public void execute() throws MojoExecutionException {

        String classesFolder = project.getBuild().getOutputDirectory();
        String jsClientSourceDir = getGeneratedJsClientSourceDir();
        String abiPath = getAbiPath();
        File abiFile = new File(abiPath);

        if(!abiFile.exists()) {
            getLog().info("Abi file is not found. This goal may be running before postpack goal." +
                    " Let's create a temp abi for code genertion");
            abiPath = generateTempAbi(classesFolder);

            abiFile = new File(abiPath);
            if(!abiFile.exists()) {
                throw new MojoExecutionException("ABI file is not found : " + abiPath);
            }
        }

        String abiStr = null;
        try {
            abiStr = FileUtil.readFile(abiPath);
        } catch (IOException e) {
            throw new MojoExecutionException("Unable to read ABI file : " + abiPath, e);
        }

        File jsClientSourceDirObj = new File(jsClientSourceDir);
        if(!jsClientSourceDirObj.exists())
            jsClientSourceDirObj.mkdirs();

        boolean isVerbose = getLog().isDebugEnabled()? true: false;

        Map<String, Object> data = new HashMap<>();
        data.put("jar", new File(getDappJar()).getName());
        data.put("projectName", project.getName());
        data.put("version", "1.0.0");
        //Generate client js code
        JsClientGenerator jsClientGenerator = new JsClientGenerator(isVerbose);
        VueJsClientGenerator vueJsClientGenerator = new VueJsClientGenerator(isVerbose);

        File nodeClientFolder = new File(jsClientSourceDir, "node");
        nodeClientFolder.mkdirs();
        try {
            jsClientGenerator.generate(abiStr, nodeClientFolder.getAbsolutePath() , data);
        } catch (IOException e) {
            throw new MojoExecutionException("Could not generate Javascript client code", e);
        }

        File vueClientFolder = new File(jsClientSourceDir, "vue-app");
        vueClientFolder.mkdirs();
        try {
            vueJsClientGenerator.generate(abiStr, vueClientFolder.getAbsolutePath(), data);
        } catch (IOException e) {
            throw new MojoExecutionException("Could not generate Javascript client (vuejs) code", e);
        }

        //Create js client zip file
        List<String> sourceFolders = new ArrayList<>();
        sourceFolders.add(getGeneratedJsClientSourceDir());

        String clientJsZip = getJsClientZipName();

        ZipBuilder zipBuilder = new ZipBuilder();
        try {
            zipBuilder.build(sourceFolders, clientJsZip, "js-client");
        } catch (IOException e) {
            throw new MojoExecutionException("Zip archive could not be built for Javascript client code", e);
        }
    }

    private String getJsClientZipName() {
        return project.getBuild().getDirectory() + File.separator + project.getArtifactId() + "-js-client-" + project.getVersion() + ".zip";
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
