package com.caretdev.synthea_service

import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.GetMapping
import kotlin.Throws
import org.springframework.web.bind.annotation.RequestParam
import org.mitre.synthea.engine.Generator.GeneratorOptions
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import com.google.gson.Gson
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.apache.commons.io.filefilter.TrueFileFilter
import org.mitre.synthea.engine.Generator
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.lang.Exception
import java.nio.file.Files
import java.util.concurrent.Semaphore

@RestController
class APIController {
    companion object {
        private val SEMAPHORE = Semaphore(1)
    }

    @GetMapping("/")
    fun index(): String {
        return "Hello World!"
    }

    @GetMapping(value = ["/data"], produces = ["application/json"])
    @Throws(Exception::class)
    fun getData(@RequestParam(required = false) seed: Long?): String {
        SEMAPHORE.acquire()
        val options = GeneratorOptions()
        if (seed != null) options.seed = seed
        val data = generateData(options)
        SEMAPHORE.release()
        return data
    }

    @PostMapping(value = ["/data"], produces = ["application/json"])
    @Throws(Exception::class)
    fun postData(@RequestBody body: String?): String {
        SEMAPHORE.acquire()
        val gson = Gson()
        val options = gson.fromJson(body, GeneratorOptions::class.java)
        val data = generateData(options)
        SEMAPHORE.release()
        return data
    }

    @Throws(Exception::class)
    private fun generateData(options: GeneratorOptions): String {
        options.population = 1
        val generator = Generator(options)
        generator.run()
        val source = File("./output/fhir")
        val files = FileUtils.listFiles(source, TrueFileFilter.INSTANCE,
                TrueFileFilter.INSTANCE) as List<File>
        if (files.isEmpty()) {
            throw Exception("Nothing generated")
        }
        val `in`: InputStream = FileInputStream(files[0])
        val response = String(IOUtils.toByteArray(`in`))
        cleanUpFiles()
        return response
    }

    private fun cleanUpFiles() {
        val source = File("./output/fhir")
        for (file in FileUtils.listFiles(source, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE)) {
            try {
                Files.delete(file.toPath())
            } catch (ignore: IOException) { /* ignore */
            }
        }
    }

}