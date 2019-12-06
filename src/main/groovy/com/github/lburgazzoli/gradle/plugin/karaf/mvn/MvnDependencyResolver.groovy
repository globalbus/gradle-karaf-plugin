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
package com.github.lburgazzoli.gradle.plugin.karaf.mvn

import com.github.lburgazzoli.gradle.plugin.karaf.features.model.DependencyDescriptor
import com.github.lburgazzoli.gradle.plugin.karaf.features.model.DependencyResolver


/**
 * @author lburgazzoli
 */
class MvnDependencyResolver extends DependencyResolver {

    @Override
    protected String renderUrl(DependencyDescriptor dependency) {

        String url = baseMvnUrl( dependency )

        if (dependency.bundle) {
            if(dependency.bundle.wrap) {
                url = "wrap:${url}"
            }

            if (dependency.bundle.instructions) {
                def res = dependency.bundle.instructions.inject([]) {
                    result, entry -> result << "${entry.key}=${entry.value}"
                }.join('&')

                if (dependency.isWar()) {
                    url = "${url}?${res}"
                } else {
                    url = "${url}\$${res}"
                }
            }
        } else if (!dependency.isOSGi() && !dependency.isWar()) {
            // if the resolved file does not have "proper" OSGi headers we
            // implicitly do the wrap as a courtesy...
            url = "wrap:${url}"
        }

        return url
    }

    /**
     *
     * @param bundleCoordinates
     * @return
     */
    static String baseMvnUrl(DependencyDescriptor dependencyDescriptor) {
        def gnv = "${dependencyDescriptor.group}/${dependencyDescriptor.name}/${dependencyDescriptor.version}"

        if (dependencyDescriptor.type && dependencyDescriptor.classifier) {
            gnv = "${gnv}/${dependencyDescriptor.type}/${dependencyDescriptor.classifier}"
        } else if (!dependencyDescriptor.type && dependencyDescriptor.classifier) {
            gnv = "${gnv}//${dependencyDescriptor.classifier}"
        }

        return dependencyDescriptor.isWar() ? "mvn:${gnv}/war" : "mvn:${gnv}"
    }
}
