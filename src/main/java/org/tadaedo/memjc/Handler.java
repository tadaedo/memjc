package org.tadaedo.memjc;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

public final class Handler extends URLStreamHandler {

    // regist Handler 'memjc' protocol
    public static void regist() {

        String HANDLER_KEY = "java.protocol.handler.pkgs";
        String pkg = Handler.class.getPackage().getName();
        pkg = pkg.substring(0, pkg.lastIndexOf("."));

        String packages = System.getProperty(HANDLER_KEY);
        if (packages == null) {
            packages = pkg;
        } else {
            packages += "|" + pkg;
        }
        System.setProperty(HANDLER_KEY, packages);
    }

    @Override
    protected URLConnection openConnection(URL url) throws IOException {

        return new MemjcURLConnection(url);
    }

    private static class MemjcURLConnection extends URLConnection {

        public MemjcURLConnection(URL url) {
            super(url);
        }

        @Override
        public void connect() {}

        @Override
        public InputStream getInputStream() throws IOException {

            return ClassManager.getInputStream(url.getPath());
        }
    }
}

