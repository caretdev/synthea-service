package com.caretdev.synthea_service;

import java.nio.file.Files;
import java.util.List;
import java.io.*;
import java.util.concurrent.Semaphore;

import com.google.gson.Gson;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.mitre.synthea.engine.Generator;
import org.springframework.web.bind.annotation.*;

@RestController
public class APIController {

    private static final Semaphore SEMAPHORE = new Semaphore(1);

    @GetMapping("/")
    public String index()
    {
        return "Hello World!";
    }

    @GetMapping(value = "/data", produces = "application/json")
    public String getData(@RequestParam(required = false) Long seed) throws Exception
    {
        SEMAPHORE.acquire();

        Generator.GeneratorOptions options = new Generator.GeneratorOptions();
        if (seed != null) options.seed = seed;

        String data = generateData(options);

        SEMAPHORE.release();
        return data;
    }

    @PostMapping(value = "/data", produces = "application/json")
    public String postData(@RequestBody String body) throws Exception
    {
        SEMAPHORE.acquire();

        Gson gson = new Gson();
        Generator.GeneratorOptions options = gson.fromJson(body, Generator.GeneratorOptions.class);

        String data = generateData(options);

        SEMAPHORE.release();
        return data;
    }

    private String generateData(Generator.GeneratorOptions options) throws Exception
    {
        options.population = 1;
        Generator generator = new Generator(options);
        generator.run();

        File source = new File("./output/fhir");
        List<File> files = (List<File>) FileUtils.listFiles(source, TrueFileFilter.INSTANCE,
                TrueFileFilter.INSTANCE);
        if (files.isEmpty()) {
            throw new Exception("Nothing generated");
        }
        InputStream in = new FileInputStream(files.get(0));
        String response = new String(IOUtils.toByteArray(in));
        cleanUpFiles();

        return response;
    }

    private void cleanUpFiles()
    {
        File source = new File("./output/fhir");
        for (File file : FileUtils.listFiles(source, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE)) {
            try {
                Files.delete(file.toPath());
            } catch (IOException ignore) { /* ignore */ }
        }
    }
}
