package com.caretdev.synthea_service

import org.mitre.synthea.engine.Generator
import org.mitre.synthea.helpers.Config
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.SpringApplication

@SpringBootApplication
open class Application

fun main(args: Array<String>) {
    Config.set("exporter.fhir.export", "true")
    Config.set("exporter.fhir.transaction_bundle", "true")
    Config.set("exporter.practitioner.fhir.export", "false")
    Config.set("exporter.hospital.fhir.export", "false")
    Config.set("generate.only_alive_patients", "true")
    Config.set("exporter.use_uuid_filenames", "true")
    Generator(0).run()
    SpringApplication.run(Application::class.java, *args)
}
