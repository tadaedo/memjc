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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

public final class Compiler {

    private final boolean cacheClass;

    public Compiler() {

        this(false);
    }

    public Compiler(boolean cacheClass) {

        this.cacheClass = cacheClass;
    }

    public boolean compile(List<String> opts, List<String> filePaths) throws IOException {

        beforeCompiler();

        CompilationTask task = getTask(opts, filePaths);
        boolean ret = task.call();

        afterCompiler();

        return ret;
    }

    private void beforeCompiler() {

    }

    private void afterCompiler() throws IOException {

    }

    private CompilationTask getTask(final List<String> opts, final List<String> filePaths) throws IOException {

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new RuntimeException("use JDK_HOME/bin/java");
        }

        try (StandardJavaFileManager stdfm = compiler.getStandardFileManager(null, null, null)) {
            JavaFileManager fm = new ForwardingJavaFileManager(stdfm) {

                @Override
                public JavaFileObject getJavaFileForOutput(JavaFileManager.Location location,
                        final String className, JavaFileObject.Kind kind, final FileObject sibling) throws IOException {

                    return new JavaFileObject() {

                        @Override
                        public OutputStream openOutputStream() throws IOException {

                            Path path = Paths.get(sibling.toUri());
                            Path parentPath = path.getParent();

                            OutputStream stream = null;
                            if (cacheClass) {
                                stream = ClassManager.getOutputStream(parentPath.toUri(), className);
                            }

                            if (stream == null) {
                                stream = getEmptyOutputStream();
                            }
                            return stream;
                        }

                        @Override
                        public JavaFileObject.Kind getKind() {
                            throw new UnsupportedOperationException("Not supported yet.");
                        }

                        @Override
                        public boolean isNameCompatible(String simpleName, JavaFileObject.Kind kind) {
                            throw new UnsupportedOperationException("Not supported yet.");
                        }

                        @Override
                        public NestingKind getNestingKind() {
                            throw new UnsupportedOperationException("Not supported yet.");
                        }

                        @Override
                        public Modifier getAccessLevel() {
                            throw new UnsupportedOperationException("Not supported yet.");
                        }

                        @Override
                        public URI toUri() {
                            throw new UnsupportedOperationException("Not supported yet.");
                        }

                        @Override
                        public String getName() {
                            throw new UnsupportedOperationException("Not supported yet.");
                        }

                        @Override
                        public InputStream openInputStream() throws IOException {
                            throw new UnsupportedOperationException("Not supported yet.");
                        }

                        @Override
                        public Reader openReader(boolean ignoreEncodingErrors) throws IOException {
                            throw new UnsupportedOperationException("Not supported yet.");
                        }

                        @Override
                        public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
                            throw new UnsupportedOperationException("Not supported yet.");
                        }

                        @Override
                        public Writer openWriter() throws IOException {
                            throw new UnsupportedOperationException("Not supported yet.");
                        }

                        @Override
                        public long getLastModified() {
                            throw new UnsupportedOperationException("Not supported yet.");
                        }

                        @Override
                        public boolean delete() {
                            throw new UnsupportedOperationException("Not supported yet.");
                        }
                    };
                }
            };

            return compiler.getTask(null, fm, null, opts, null, getUnits(stdfm, filePaths));
        }
    }

    private OutputStream getEmptyOutputStream() {

        return new OutputStream() {
            @Override
            public void write(int b) throws IOException {
            }
        };
    }

    private Iterable<? extends JavaFileObject> getUnits(StandardJavaFileManager fs, List<String> filePaths) {

        List<File> files = new ArrayList<>();
        for (String path: filePaths) {
            files.add(new File(path));
        }
        return fs.getJavaFileObjectsFromFiles(files);
    }
}
