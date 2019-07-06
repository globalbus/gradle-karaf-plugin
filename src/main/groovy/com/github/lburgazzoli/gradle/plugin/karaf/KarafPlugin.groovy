/**
 * Copyright 2016, Luca Burgazzoli and contributors as indicated by the @author tags
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.lburgazzoli.gradle.plugin.karaf

import com.github.lburgazzoli.gradle.plugin.karaf.features.KarafFeaturesTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.WarPlugin
import org.gradle.api.tasks.ClasspathNormalizer

/**
 * @author lburgazzoli
 */
class KarafPlugin implements Plugin<Project> {
    static final String ARTIFACTS_CONFIGURATION_NAME = 'archives'
    static final String CONFIGURATION_NAME = 'karaf'
    static final List<String> ARTIFACT_TASKS = [JavaPlugin.JAR_TASK_NAME, WarPlugin.WAR_TASK_NAME ]
    static final List<String> ARCHIVE_TASKS = [ BasePlugin.ASSEMBLE_TASK_NAME ]

    @Override
    void apply(Project project) {
        def ext = KarafPluginExtension.create(project)

        project.configurations.create(CONFIGURATION_NAME)

        // Karaf Features
        def feat = project.task( KarafFeaturesTask.NAME , type: KarafFeaturesTask) {
            group       = KarafFeaturesTask.GROUP
            description = KarafFeaturesTask.DESCRIPTION
        }

        def war = project.tasks.find { it.name == WarPlugin.WAR_TASK_NAME }
        def jar = project.tasks.find { it.name == JavaPlugin.JAR_TASK_NAME }

        if (war) {
            war.dependsOn feat
        }  else if (jar) {
            jar.dependsOn feat
        }

        project.afterEvaluate {
            if (ext.hasFeatures()) {
                ext.features.featureDescriptors.each {
                    it.configurations.each { Configuration configuration ->
                        feat.inputs.files(configuration).withPropertyName('classpath').withNormalizer(ClasspathNormalizer)
                        feat.dependsOn(configuration)
                    }
                }

                // if there is an output file, add that as an output
                if (ext.features.outputFile != null) {
                    feat.outputs.file(ext.features.outputFile)
                }
            }

            project.artifacts.add(ARTIFACTS_CONFIGURATION_NAME, ext.features.outputFile) {
                classifier = 'features'
            }
        }
    }
}
