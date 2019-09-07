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

package org.aion4j.maven.avm.adapter;

import org.aion4j.maven.avm.util.StringUtils;
import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.MojoExecutionException;

import java.lang.reflect.Method;
import java.math.BigInteger;

public class LocalAvmAdapter {

    private Object localAvmInstance;
    public LocalAvmAdapter(Object localAvmInstance) {
        this.localAvmInstance = localAvmInstance;
    }

    public BigInteger getBalance(String address) throws MojoExecutionException {
        if(StringUtils.isEmpty(address))
            return null;
        try {
            final Method getBalanceMethod = localAvmInstance.getClass().getMethod("getBalance", String.class);
            Object response = getBalanceMethod.invoke(localAvmInstance, address);

            if (response != null) {
                return (BigInteger) response;
            } else {
                return null;
            }
        } catch (Exception e) {
            throw new MojoExecutionException("Get balance failed", e);
        }
    }

    public boolean transfer(String toAddress, BigInteger amount) throws MojoExecutionException {
        if(StringUtils.isEmpty(toAddress))
            return false;

        if(amount == null)
            amount = BigInteger.ZERO;

        try {
            final Method transferMethod = localAvmInstance.getClass().getMethod("transfer", String.class, BigInteger.class);
            Object response = transferMethod.invoke(localAvmInstance, toAddress, amount);

            if (response != null) {
                return (boolean) response;
            } else {
                return false;
            }
        } catch (Exception e) {
            throw new MojoExecutionException("Transfer in embedded Avm failed", e);
        }
    }

    public boolean createAccountWithBalance(String address, BigInteger balance) throws MojoExecutionException {
        try {
            final Method createAccountMethod = localAvmInstance.getClass().getMethod("createAccountWithBalance", String.class, BigInteger.class);

            Object response = createAccountMethod.invoke(localAvmInstance, address, balance);
            return (boolean) response;
        } catch (Exception e) {
            throw new MojoExecutionException("Create account error in embedded Avm", e);
        }
    }
}
