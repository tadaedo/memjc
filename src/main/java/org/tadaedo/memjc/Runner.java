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

import java.lang.reflect.Method;
import java.util.List;

public final class Runner {

    public void execute(String className, List<String> classArgs, List<String> classPaths)
            throws Exception {

        ClassLoader cl = ClassManager.getClassLoader(classPaths);

        Class<?> clazz = Class.forName(className, true, cl);
        Method method = clazz.getMethod("main", new Class[] {String[].class});

        String[] args = classArgs.toArray(new String[]{});
        method.invoke(null, new Object[] { args });
    }
}
