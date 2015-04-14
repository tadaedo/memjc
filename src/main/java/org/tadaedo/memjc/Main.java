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

public final class Main {

    static {
        // regist memjc protocol
        Handler.regist();
    }

    public static void main(String[] args) {

        run(args);
    }

    public static void run(String[] args) {

        Options options = new Options();

        try {
            if (!options.setOptions(args)) {
                return;
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            Options.showUsage();
            return;
        }

        try {

            Compiler compiler = new Compiler(options.memJcOut || options.memJcRun);
            compiler.compile(options.opts, options.files);

            if (options.memJcOut) {
                ClassManager.outputAllFile();
            }

            if (options.memJcRun) {

                Runner runner = new Runner();
                runner.execute(options.memJcRunClassName, options.memJcClassArgs,
                        options.memJcClassPaths);
            }

        } catch (Exception ex) {
            System.out.println(ex.toString());
        }
    }
}
