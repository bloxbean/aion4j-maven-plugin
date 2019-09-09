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

package org.aion4j.maven.avm.mojo;

import org.aion4j.avm.helper.util.ConfigUtil;
import org.aion4j.avm.helper.util.ResultCache;
import org.aion4j.maven.avm.util.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

@Mojo(name = "get-cache", aggregator = true)
public class AVMGetCache extends AVMBaseMojo {

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        String outputPath = ConfigUtil.getProperty("output");

        String lastDeployedAddress = getCache().getLastDeployedAddress();
        boolean debugEnabled = getCache().getDebugEnabledInLastDeploy();
        String lastDeployTxnReceipt = getCache().getLastDeployTxnReceipt();
        String lastTxnReceipt = getCache().getLastTxnReceipt();

        getLog().info("Last deploy Address       : " + lastDeployedAddress);
        getLog().info("Last deploy debug enabled : " + debugEnabled);
        getLog().info("Last deploy txn receipt   : " + lastDeployTxnReceipt);
        getLog().info("Last txn receipt          : " + lastTxnReceipt);

        if(!StringUtils.isEmpty(outputPath)) {
            Properties props = new Properties();
            if(lastDeployedAddress != null)
                props.put(ResultCache.DEPLOY_ADDRESS, lastDeployedAddress);
            props.put(ResultCache.LAST_DEPLOY_DEBUG_ENABLE, String.valueOf(debugEnabled));
            if(lastDeployTxnReceipt != null)
                props.put(ResultCache.DEPLOY_TX_RECEIPT, lastDeployTxnReceipt);
            if(lastTxnReceipt != null)
                props.put(ResultCache.TX_RECEIPT, lastTxnReceipt);

            try {
                try (OutputStream output = new FileOutputStream(outputPath)) {
                    props.store(output, null);
                    getLog().info("Output written to : " + outputPath);
                } catch (IOException io) {
                    getLog().debug("Error writing output to file", io);
                    getLog().warn("Unable to write output to file : " + outputPath);
                }
            } catch(Exception e) {
                //ignore
                getLog().debug(e);
            }
        }
    }
}
