package example

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileType
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.work.ChangeType
import org.gradle.work.Incremental
import org.gradle.work.InputChanges
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import org.gradle.workers.WorkerExecutor
import java.io.File
import javax.inject.Inject


@CacheableTask
abstract class FileProcessingTask : DefaultTask() {

    @get:Input
    abstract val processing: Property<String>

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    @get:Incremental
    abstract val inputDirectory: DirectoryProperty

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @TaskAction
    fun action(inputChanges: InputChanges) {

        inputChanges.getFileChanges(inputDirectory).forEach { change ->
            if (change.fileType == FileType.DIRECTORY) return@forEach

            val targetFile = outputDirectory.file(change.normalizedPath).get().asFile
            if (change.changeType == ChangeType.REMOVED) {
                targetFile.delete()
            } else {
                processFile(processing.get(), change.file, targetFile)
            }

        }
    }

    @get:Inject
    protected abstract val workers: WorkerExecutor

    private
    fun processFile(processing: String, inputFile: File, outputFile: File) {
        workers.noIsolation().submit(FileProcessingWork::class.java) {
            this.processing.set(processing)
            this.inputFile.set(inputFile)
            this.outputFile.set(outputFile)
        }
    }
}


interface FileProcessingParam : WorkParameters {
    val processing: Property<String>
    val inputFile: Property<File>
    val outputFile: Property<File>
}

abstract class FileProcessingWork : WorkAction<FileProcessingParam> {
    override fun execute() {
        println("processing file '${parameters.inputFile.get()}'")
        parameters.outputFile.get().run {
            parentFile.mkdirs()
            writeText(parameters.inputFile.get().readText().reversed())
        }
    }
}
