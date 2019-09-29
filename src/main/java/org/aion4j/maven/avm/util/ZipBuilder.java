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

package org.aion4j.maven.avm.util;

import java.io.*;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipOutputStream;

public class ZipBuilder {

    public ZipBuilder() {
    }

    public void build(List srcNames, String finalZip, String baseFolder) throws IOException {
        String source = null;
        ZipOutputStream zipOut = null;

        zipOut = new ZipOutputStream(new FileOutputStream(finalZip));
        boolean throwing = false;
        try {
            if (srcNames != null) {
                Iterator iter = srcNames.iterator();
                while (iter.hasNext()) {
                    source = (String) iter.next();
                    File fsource = new File(source);
                    String base = baseFolder;
                    if (!fsource.exists()) {
                        throwing = true;
                        throw new FileNotFoundException(fsource.getAbsolutePath());
                    }
                    if (fsource.isDirectory()) {
                        addDirectory(zipOut, base, fsource, 0);
                    } else {
                        addFileStream(zipOut, base, fsource);
                    }
                }
            }
        } finally {
            try {
                zipOut.close();
            } catch (ZipException z) {
                if (!throwing) {
                    throw new IOException(z.toString());
                }
            }
        }
    }

    protected String fileExtension(String file) {
        int leafPos = file.lastIndexOf('/');
        if (leafPos == file.length() - 1) return "";
        String leafName = file.substring(leafPos + 1);
        int dotPos = leafName.lastIndexOf('.');
        if (dotPos == -1) return "";
        String ext = leafName.substring(dotPos + 1);
        return ext;
    }

//    /** @return empty or a jar base path. Must not start with '/' */
//    protected String getBasePathInZipOut(String sourceFile) {
//        // TaskRunner will unjar and append to classpath: .:classes/:lib/*
//        String ext = fileExtension(sourceFile);
//        if (ext.equals("class")) {
//            return "classes/"; // or ""
//        } else if (ext.equals("jar") || ext.equals("zip")) {
//            return "lib/";
//        } else {
//            return "";
//        }
//    }

    /** @param name path in zip for this zip element. Must not start with '/' */
    void addNamedStream(ZipOutputStream dst, String name, InputStream in) throws IOException {
        if (verbose) {
            System.err.println("ZipBuilder.addNamedStream " + name);
        }
        try {
            dst.putNextEntry(new ZipEntry(name));
            int bytesRead = 0;
            while ((bytesRead = in.read(buffer, 0, BUFF_SIZE)) != -1) {
                dst.write(buffer, 0, bytesRead);
            }
        } catch (ZipException ze) {
            if (ze.getMessage().indexOf("duplicate entry") >= 0) {
                if (verbose) {
                    System.err.println(ze + " Skip duplicate entry " + name);
                }
            } else {
                throw ze;
            }
        } finally {
            in.close();
            dst.flush();
            dst.closeEntry();
        }
    }

    void addFileStream(ZipOutputStream dst, String zipBaseName, File file) throws IOException {
        FileInputStream in = new FileInputStream(file);
        try {
            String name = zipBaseName + file.getName();
            addNamedStream(dst, name, in);
        } finally {
            in.close();
        }
    }

    void addDirectory(ZipOutputStream dst, String zipBaseName, File dir, int depth) throws IOException {
        File[] contents = dir.listFiles();
        if (contents != null) {
            for (int i = 0; i < contents.length; i++) {
                File f = contents[i];
                String fBaseName = (depth == 0) ? "" : dir.getName();
                if (zipBaseName.length() > 0) {
                    fBaseName = zipBaseName + "/" + fBaseName;
                }
                if (f.isDirectory()) {
                    addDirectory(dst, fBaseName, f, depth + 1);
                } else {
                    addFileStream(dst, fBaseName + "/", f);
                }
            }
        }
    }

    private static final int BUFF_SIZE = 32 * 1024;
    private byte buffer[] = new byte[BUFF_SIZE];
    protected boolean verbose = false;
}
