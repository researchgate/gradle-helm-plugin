package org.unbrokendome.gradle.plugins.helm.command.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.file.Directory
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Console
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.process.ExecResult
import org.unbrokendome.gradle.plugins.helm.HELM_GROUP
import org.unbrokendome.gradle.plugins.helm.command.GlobalHelmOptions
import org.unbrokendome.gradle.plugins.helm.command.GlobalHelmOptionsApplier
import org.unbrokendome.gradle.plugins.helm.command.HelmExecProviderSupport
import org.unbrokendome.gradle.plugins.helm.command.HelmExecSpec
import org.unbrokendome.gradle.plugins.helm.command.execHelm
import org.unbrokendome.gradle.plugins.helm.util.property


/**
 * Base class for tasks that invoke a Helm CLI command.
 */
abstract class AbstractHelmCommandTask
    : DefaultTask(), GlobalHelmOptions {

    init {
        group = HELM_GROUP
    }


    @get:Internal("represented by other properties")
    internal val globalOptions: Property<GlobalHelmOptions> =
        project.objects.property()

    @get:[Input Optional]
    final override val helmVersion: Provider<String>
        get() = globalOptions.flatMap { it.helmVersion }

    @get:Input
    final override val executable: Provider<String>
        get() = globalOptions.flatMap { it.executable }


    @get:Console
    final override val debug: Provider<Boolean>
        get() = globalOptions.flatMap { it.debug }


    @get:Input
    final override val extraArgs: Provider<List<String>>
        get() = globalOptions.flatMap { it.extraArgs }


    @get:Internal
    final override val xdgDataHome: Provider<Directory>
        get() = globalOptions.flatMap { it.xdgDataHome }


    @get:Internal
    final override val xdgConfigHome: Provider<Directory>
        get() = globalOptions.flatMap { it.xdgConfigHome }


    @get:Internal
    final override val xdgCacheHome: Provider<Directory>
        get() = globalOptions.flatMap { it.xdgCacheHome }


    @get:Internal
    protected val registryConfigFile: Provider<RegularFile>
        get() = xdgConfigHome.map { it.file("helm/registry.json") }


    @get:Internal
    protected val repositoryCacheDir: Provider<Directory>
        get() = xdgCacheHome.map { it.dir("helm/repository") }


    @get:Internal
    protected val repositoryConfigFile: Provider<RegularFile>
        get() = xdgConfigHome.map { it.file("helm/repositories.yaml") }


    protected fun execHelm(
        command: String, subcommand: String? = null, action: (HelmExecSpec.() -> Unit)? = null
    ): ExecResult =
        execProviderSupport.execHelm(command, subcommand, action)


    @get:Internal
    internal open val execProviderSupport: HelmExecProviderSupport
        get() = HelmExecProviderSupport(project, this, GlobalHelmOptionsApplier)
}
