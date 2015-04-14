/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tadaedo.memjc;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ClassManager {

    public static final String MEMJC_SCHEME = "memjc";
    private static final String MFS_ROOT_PATH = "classes";

    private static final FileSystem mfs = Jimfs.newFileSystem(Configuration.unix());
    private static final Map<String, String> pathMap = new ConcurrentHashMap<>();

    private static boolean isWindows() {

        return File.separatorChar == '\\';
    }

    private static String convertMfsFilePath(String fsRoot, String path) {

        StringBuilder buf = new StringBuilder(mfs.getSeparator());
        buf.append(MFS_ROOT_PATH);
        buf.append(mfs.getSeparator());

        if (isWindows()) {
            path = path.replaceFirst(":", "").replace(File.separator, mfs.getSeparator());
        } else {
            path = path.substring(fsRoot.length());
        }
        buf.append(path);

        return buf.toString();
    }

    protected static OutputStream getOutputStream(URI uri, String classPath) throws IOException {

        // relative path
        Path classFilePath = Paths.get(uri);
        String classFileRoot = classFilePath.getRoot().toString();

        String[] classPaths = classPath.split("\\.");
        String className = classPaths[classPaths.length - 1] + ".class";
        String fullPath = classFilePath.resolve(className).toString();
        String mfsPath = convertMfsFilePath(classFileRoot, fullPath);
        //System.out.println("fullPath:" + fullPath);
        //System.out.println("mfsPath:" + mfsPath);

        // save map path
        pathMap.put(mfsPath, fullPath);

        // create path on memory
        Path path = mfs.getPath(mfsPath);
        if (path.getParent() != null && !Files.isDirectory(path.getParent())) {
            Files.createDirectories(path.getParent());
        }

        // create stream
        return Files.newOutputStream(Files.createFile(path));
    }

    protected static void outputAllFile() throws IOException {

        for (String mfsPath : pathMap.keySet()) {

            String fullPath = pathMap.get(mfsPath);
            Path inputPath = mfs.getPath(mfsPath); // from memory filesystem
            Path outputPath = Paths.get(fullPath); // to default filesystem

            Files.copy(inputPath, outputPath, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    protected static InputStream getInputStream(String searchPath) throws IOException {

        Path inputPath = mfs.getPath(searchPath);
        return mfs.provider().newInputStream(inputPath);
    }

    protected static ClassLoader getClassLoader(List<String> classPaths) throws IOException {

        final String uriSeparator = "/";
        List<URL> urls = new ArrayList<>();

        // add memory filesystem classpath
        for (String classPath : classPaths) {
            Path fsPath = Paths.get(classPath);
            if (!fsPath.isAbsolute()) {
                fsPath = Paths.get(System.getProperty("user.dir"));
                fsPath = fsPath.resolve(classPath).normalize();
            }
            Path fsRootPath = fsPath.getRoot();
            String mfsPath = convertMfsFilePath(fsRootPath.toString(), fsPath.toString());
            String path = mfs.getPath(mfsPath).toUri().toString();
            if (Files.isDirectory(fsPath) && !path.endsWith(uriSeparator)) {
                path += uriSeparator;
            }
            // convert scheme
            path = path.replaceFirst(mfs.provider().getScheme(), MEMJC_SCHEME);
            //System.out.println("classloader path:" + path);
            urls.add(new URL(path));
        }

        // add default filesystem classpath
        for (String classPath : classPaths) {
            Path fsRootPath = Paths.get(classPath);
            String path = fsRootPath.toUri().toString();
            if (Files.isDirectory(fsRootPath) && !path.endsWith(uriSeparator)) {
                path += uriSeparator;
            }
            // convert scheme
            path = path.replaceFirst(mfs.provider().getScheme(), MEMJC_SCHEME);
            //System.out.println("classloader path:" + path);
            urls.add(new URL(path));
        }

        ClassLoader loader = ClassLoader.getSystemClassLoader();
        while (loader.getParent() != null) {
            loader = loader.getParent();
        }
        return new URLClassLoader(urls.toArray(new URL[0]), loader);
    }
}
