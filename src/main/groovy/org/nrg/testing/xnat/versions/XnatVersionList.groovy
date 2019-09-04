package org.nrg.testing.xnat.versions

import com.google.common.graph.GraphBuilder
import com.google.common.graph.Graphs
import com.google.common.graph.MutableGraph
import groovy.util.logging.Log4j
import org.nrg.testing.annotations.Follows
import org.nrg.testing.util.GraphUtils
import org.nrg.testing.xnat.conf.Settings
import org.nrg.testing.xnat.rest.XnatRestDriver
import org.reflections.Reflections

@Log4j
class XnatVersionList {

    public static final List<String> KNOWN_VERSION_KEYS = []
    public static final Map<String, Class<? extends XnatVersion>> KNOWN_KEY_VERSION_MAP = [:]
    public static final Map<Class<? extends XnatVersion>, Class<? extends XnatRestDriver>> KNOWN_VERSION_CLASS_REST_DRIVER_MAP = [:]
    public static final MutableGraph<Class<? extends XnatVersion>> XNAT_VERSION_GRAPH = GraphBuilder.directed().allowsSelfLoops(false).build()

    static void readXnatVersions(Collection<Class<? extends XnatVersion>> xnatVersions, Collection<Class<? extends XnatRestDriver>> restDrivers) {
        if (KNOWN_KEY_VERSION_MAP.isEmpty()) {
            final MutableGraph<String> versionStringGraph = GraphBuilder.directed().allowsSelfLoops(false).build()
            final Map<Class<? extends XnatVersion>, List<String>> keysForClass = xnatVersions.collectEntries { xnatVersionClass ->
                XNAT_VERSION_GRAPH.addNode(xnatVersionClass)
                final List<String> keys = xnatVersionClass.newInstance().versionKeys
                keys.each { versionKey ->
                    versionStringGraph.addNode(versionKey)
                }
                [(xnatVersionClass) : keys]
            }

            xnatVersions.each { xnatVersion ->
                final List<String> versionKeys = keysForClass[xnatVersion]
                versionKeys.eachWithIndex { versionKey, index ->
                    KNOWN_KEY_VERSION_MAP.put(versionKey, xnatVersion)
                    if (index < versionKeys.size() - 1) {
                        versionStringGraph.putEdge(versionKey, versionKeys[index + 1]) // maintain internal order in the versionKeys() list
                    }
                }

                final Follows follows = xnatVersion.getAnnotation(Follows)
                if (follows != null) {
                    follows.value().each { precedingXnatVersion ->
                        XNAT_VERSION_GRAPH.putEdge(precedingXnatVersion, xnatVersion)
                        versionStringGraph.putEdge(keysForClass[precedingXnatVersion][0], versionKeys[0])

                        keysForClass[precedingXnatVersion].each { precedingKey ->
                            versionKeys.each { versionKey ->
                                versionStringGraph.putEdge(precedingKey, versionKey)
                            }
                        }
                    }
                }
            }

            restDrivers.each { restDriverClass ->
                restDriverClass.newInstance().handledVersions.each { xnatVersionClass ->
                    KNOWN_VERSION_CLASS_REST_DRIVER_MAP.put(xnatVersionClass, restDriverClass)
                }
            }

            KNOWN_VERSION_KEYS.addAll(GraphUtils.topologicalSort(versionStringGraph))
            log.info("Known XNAT versions at runtime: ${KNOWN_VERSION_KEYS.join(', ')}")
        }
    }

    static void readXnatVersions() {
        readXnatVersions(
                new Reflections('org.nrg.testing.xnat.versions').getSubTypesOf(XnatVersion),
                new Reflections('org.nrg.testing.xnat.rest').getSubTypesOf(XnatRestDriver)
        )
    }

    static boolean testedVersionPrecedes(Class<? extends XnatVersion> specifiedVersion) {
        firstFollowsSecond(specifiedVersion, Settings.XNAT_VERSION)
    }

    static boolean testedVersionFollows(Class<? extends XnatVersion> specifiedVersion) {
        firstFollowsSecond(Settings.XNAT_VERSION, specifiedVersion)
    }

    static boolean firstFollowsSecond(Class<? extends XnatVersion> first, Class<? extends XnatVersion> second) {
        (first != second) && first in Graphs.reachableNodes(XNAT_VERSION_GRAPH, second)
    }

    static Class<? extends XnatRestDriver> lookupDriverClass(Class<? extends XnatVersion> xnatVersionClass) {
        readXnatVersions()
        if (KNOWN_VERSION_CLASS_REST_DRIVER_MAP.containsKey(xnatVersionClass)) {
            KNOWN_VERSION_CLASS_REST_DRIVER_MAP[xnatVersionClass]
        } else {
            throw new RuntimeException("Could not find requested version of XNAT (${xnatVersionClass}) in list of available versions.") // Shouldn't happen [should already have been failed in XNATProperties]
        }
    }

}
