package com.brageast.java_soga;

import com.google.gson.Gson;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;

public class ModuleAMain {

    public static void main(String[] args) throws Exception {
        File file = new File("./plugins");
        file.mkdirs();

        List<File> files = Arrays.asList(file.listFiles());

        PluginClassLoader pluginClassLoader = new PluginClassLoader();

        Set<JarFile> collect = files.stream()
                .filter(File::isFile)
                .filter(f -> f.getName().endsWith(".jar"))
                .map(f -> {
                    try {
                        pluginClassLoader.addURL(f.toURI().toURL());
                        return new JarFile(f);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                })
                .collect(Collectors.toSet());

        Gson gson = new Gson();

        for (JarFile jarFile : collect) {
            ZipEntry entry = jarFile.getEntry("app.json");

            InputStream inputStream = jarFile.getInputStream(entry);
            APP app = gson.fromJson(new InputStreamReader(inputStream), APP.class);

            Class<?> aClass = Class.forName(app.getMain(), true, pluginClassLoader);

            Object o = aClass.newInstance();

            Method init = aClass.getDeclaredMethod("init");
            init.invoke(o);
        }


    }

}


class APP {
    private String main;

    public String getMain() {
        return main;
    }

    public void setMain(String main) {
        this.main = main;
    }
}



class PluginClassLoader extends URLClassLoader {

    public PluginClassLoader() {
        super(new URL[0]);
    }

    public PluginClassLoader(URL[] urls) {
        super(urls);
    }

    @Override
    public void addURL(URL url) {
        super.addURL(url);
    }
}