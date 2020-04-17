package org.unbrokendome.gradle.plugins.helm.command.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.unbrokendome.gradle.plugins.helm.util.property
import java.io.File
import java.net.URL

open class DownloadHelmClientTask : DefaultTask() {

    @get:[Input]
    val os: Property<String> = project.objects.property()
    @get:[Input]
    val arch: Property<String> = project.objects.property()
    @get:[Input]
    val helmVersion: Property<String> = project.objects.property()

    @get:[OutputDirectory]
    val helmClientDirectory: DirectoryProperty = project.objects.directoryProperty()

    @get:[OutputFile]
    val helmExecutable: RegularFileProperty = project.objects.fileProperty()

    init {
        helmClientDirectory.convention(project.layout.buildDirectory.dir("helm/client/"))
        helmExecutable.convention(helmClientDirectory.file("helm"))

        val osProperty: String = System.getProperty("os.name").toLowerCase()
        val archProperty: String = System.getProperty("os.arch")
        if (osProperty == "mac os x") {
            os.convention("darwin")
        } else {
            os.convention(osProperty)
        }
        if (archProperty == "x86_64") {
            arch.convention("amd64")
        } else {
            arch.convention(archProperty)
        }
    }

    @TaskAction
    fun download() {
        val helmClientDir = helmClientDirectory.get()
        project.delete(helmClientDir)
        helmClientDir.asFile.mkdirs()
        val helmPackage  = File(helmClientDir.asFile, "helm-v${helmVersion.get()}-${os.get()}-${arch.get()}.tar.gz")
        if (!helmPackage.exists()) {
            val helmArchiveInputStream = URL("https://get.helm.sh/helm-v${helmVersion.get()}-${os.get()}-${arch.get()}.tar.gz")
                    .openStream()
            helmArchiveInputStream.use { inputStream ->
                helmPackage.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            helmArchiveInputStream.close()
        }
        project.copy {
            it.from(project.tarTree(helmPackage))
            it.into(helmClientDirectory.get())
        }

        project.copy {
            it.from(File("${helmClientDirectory.get()}/${os.get()}-${arch.get()}/helm"))
            it.into(helmClientDirectory.get())
        }
    }
}
