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
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class Options {

    private static final String MEMJC_VERSION = "1.1";

    private static final String MEMJC_PREFIX = "-M";
    private static final String MEMJC_OPTION_HELP = MEMJC_PREFIX + "help";
    private static final String MEMJC_OPTION_OUT = MEMJC_PREFIX + "out";
    private static final String MEMJC_OPTION_RUN = MEMJC_PREFIX + "main";
    private static final String MEMJC_OPTION_CLASSPATH = MEMJC_PREFIX + "cp";

    public enum Type {
        JAVAFILE,
        ARGFILE,
        MEMJCOPTION,
        OPTION
    }

    // javac options
    public final List<String> opts = new ArrayList<>();
    public final List<String> files = new ArrayList<>();

    // memjc options
    public boolean memJcOut = false;
    public boolean memJcRun = false;
    public String memJcRunClassName = "";
    public final List<String> memJcClassArgs = new ArrayList<>();
    public final List<String> memJcClassPaths = new ArrayList<>();

    public boolean setOptions(String[] args) throws IOException {

        for (String arg : args) {
            switch (isType(arg)) {
            case MEMJCOPTION:
                String[] memArgs = arg.split(":", 2);
                switch (memArgs[0]) {
                case MEMJC_OPTION_HELP:
                    showUsage();
                    return false;
                case MEMJC_OPTION_OUT:
                    memJcOut = true;
                    break;
                case MEMJC_OPTION_RUN:
                    memJcRun = true;
                    if (memArgs.length >= 2) {
                        String[] memClass = memArgs[1].split(":", 2);
                        memJcRunClassName = memClass[0];
                        // class args
                        if (memClass.length >= 2) {
                            memJcClassArgs.addAll(Arrays.asList(memClass[1].split(":")));
                        }
                    }
                    break;
                case MEMJC_OPTION_CLASSPATH:
                    if (memArgs.length >= 2) {
                        String sep = getOptionSeparator();
                        memJcClassPaths.addAll(Arrays.asList(memArgs[1].split(sep)));
                    }
                    break;
                default:
                    throw new RuntimeException("Unrecognized option: " + arg);
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

        // default java option
        if (!opts.contains("-cp") && !opts.contains("-classpath")) {
            opts.add("-cp");
            String classPath = System.getenv("CLASSPATH");
            if (classPath == null) {
                opts.add(System.getProperty("user.dir"));
            } else {
                opts.add(classPath);
            }
        }

        // default memjc option
        String[] cps = new String[] {"-cp", "-classpath" };
        for (String cp : cps) {
            int cpIndex = opts.indexOf(cp);
            if (cpIndex != -1 && (cpIndex + 1) < opts.size()) {
                String javacCp = opts.get(cpIndex + 1);
                String sep = getOptionSeparator();
                memJcClassPaths.addAll(Arrays.asList(javacCp.split(sep)));
            }
        }

        return true;
    }

    private static String getOptionSeparator() {

        if (File.separatorChar == '\\') {
            return ";"; // windows
        } else {
            return ":"; // unix
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

        String sep = getOptionSeparator();
        System.out.println("Usage: memjc <memjc options> <javac options> <source files>");
        System.out.println("Version: " + MEMJC_VERSION);
        System.out.println("memjc Options:");
        System.out.println("  " + MEMJC_OPTION_HELP + " Show usage");
        System.out.println("  " + MEMJC_OPTION_OUT + " Output class file");
        System.out.println("  " + MEMJC_OPTION_RUN + ":<classname>[:<arg1>:<arg2>...] main class");
        System.out.println("  " + MEMJC_OPTION_CLASSPATH + ":<classpath>[" + sep + "<classpath1>" + sep + "<classpath2>...] java class path");
    }
}
