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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Options {

    private static final String MEMJC_PREFIX = "-memjc";

    public enum Type {
        JAVAFILE,
        ARGFILE,
        MEMJCOPTION,
        OPTION
    }

    public final List<String> opts = new ArrayList<>();
    public final List<String> files = new ArrayList<>();
    public boolean memJcOut;

    public void setOptions(String[] args) throws IOException {

        for (String arg : args) {
            switch (isType(arg)) {
            case MEMJCOPTION:
                switch (arg) {
                case MEMJC_PREFIX + "-out":
                    this.memJcOut = true; break;
                default:
                    showUsage(); throw new RuntimeException("");
                }
                break;
            case ARGFILE:
                setOptions(readArgfile(arg));
                break;
            case JAVAFILE:
                files.add(arg);
                break;
            case OPTION:
            default:
                opts.add(arg);
                break;
            }
        }

        // default option
        if (!opts.contains("-cp") && !opts.contains("-classpath")) {
            opts.add("-cp");
            opts.add(System.getProperty("user.dir"));
        }
    }

    private Type isType(String arg) {

        if (arg.startsWith("@")) {
            return Type.ARGFILE;
        } else if (arg.endsWith(".java")) {
            return Type.JAVAFILE;
        } else if (arg.startsWith(MEMJC_PREFIX)) {
            return Type.MEMJCOPTION;
        } else {
            return Type.OPTION;
        }
    }

    private String[] readArgfile(String argfile) throws IOException {

        try (BufferedReader read = new BufferedReader(new FileReader(new File(argfile)))) {

            List<String> lines = new ArrayList<>();

            String line;
            while ((line = read.readLine()) != null) {
                lines.add(line);
            }

            return lines.toArray(new String[0]);
        }
    }

    public static void showUsage() {
        System.out.println("Usage: memjc <memjc options> <javac options> <source files>");
        System.out.println("memjc Options:");
        System.out.println("  -memjc-out Output class file");
    }

    public void debug() {

        System.out.println("debug start");

        System.out.println("memJcOut:" + memJcOut);
        for (String opt : opts) {
            System.out.println("opt:" + opt);
        }
        for (String file : files) {
            System.out.println("file:" + file);
        }

        System.out.println("debug end");
    }
}
