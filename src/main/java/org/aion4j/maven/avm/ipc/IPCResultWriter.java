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

package org.aion4j.maven.avm.ipc;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.aion4j.maven.avm.util.StringUtils;
import org.apache.maven.plugin.logging.Log;

import java.io.File;
import java.util.List;

public class IPCResultWriter {

    public static void saveAccountList(List<IPCAccount> accounts, String outputFile, Log log) {
        if(accounts == null || StringUtils.isEmpty(outputFile)) {
            if(log.isDebugEnabled()) {
                log.debug("Don't write any ipc account list as account list is empty or output file is null");
            }
            return;
        }

        IPCAccountCache accountCache = new IPCAccountCache();
        accountCache.setAccounts(accounts);

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            objectMapper.writeValue(new File(outputFile), accountCache);
            System.out.println("Output writeennnnn>>>>>> " + outputFile);
        } catch (Exception e) {
            if(log.isDebugEnabled()) {
                log.error(e);
                log.warn("Could not write to ipc account cache", e);
            }
        }
    }
}
