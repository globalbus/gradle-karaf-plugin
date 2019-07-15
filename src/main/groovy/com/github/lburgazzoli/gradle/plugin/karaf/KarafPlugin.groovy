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
import org.gradle.api.tasks.ClasspathNormalizer
import org.gradle.api.tasks.bundling.War
import org.gradle.jvm.tasks.Jar

/**
 * @author lburgazzoli
 */
class KarafPlugin implements Plugin<Project> {
    static final String ARTIFACTS_CONFIGURATION_NAME = 'archives'
    static final String CONFIGURATION_NAME = 'karaf'

    @Override
    void apply(Project project) {
        def ext = KarafPluginExtension.create(project)

        project.configurations.create(CONFIGURATION_NAME)

        // Karaf Features
        def feat = project.tasks.register(KarafFeaturesTask.NAME, KarafFeaturesTask.class) { KarafFeaturesTask task ->
            group       = KarafFeaturesTask.GROUP
            description = KarafFeaturesTask.DESCRIPTION
            if (ext.hasFeatures()) {
                ext.features.featureDescriptors.each {
                    it.configurations.each { Configuration configuration ->
                        task.inputs.files(configuration).withPropertyName('classpath').withNormalizer(ClasspathNormalizer)
                        task.dependsOn(configuration)
                    }
                }

                // if there is an output file, add that as an output
                if (ext.features.outputFile != null) {
                    task.outputs.file(ext.features.outputFile)
                }
            }

            project.artifacts.add(ARTIFACTS_CONFIGURATION_NAME, ext.features.outputFile) {
                classifier = 'features'
            }
        }
        project.tasks.withType(War.class).configureEach { dependsOn feat}
        project.tasks.withType(Jar.class).configureEach { dependsOn feat}
    }
}
