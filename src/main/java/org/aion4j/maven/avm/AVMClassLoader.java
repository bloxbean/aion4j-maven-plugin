package org.aion4j.maven.avm;

import java.net.URL;
import java.net.URLClassLoader;

public class AVMClassLoader extends URLClassLoader {

    public AVMClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

}
