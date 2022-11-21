/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.karaf.minho.tooling.common.maven;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.building.DefaultModelBuilder;
import org.apache.maven.model.building.DefaultModelBuildingRequest;
import org.apache.maven.model.building.DefaultModelProcessor;
import org.apache.maven.model.building.ModelBuildingRequest;
import org.apache.maven.model.composition.DefaultDependencyManagementImporter;
import org.apache.maven.model.inheritance.DefaultInheritanceAssembler;
import org.apache.maven.model.interpolation.DefaultModelVersionProcessor;
import org.apache.maven.model.interpolation.StringVisitorModelInterpolator;
import org.apache.maven.model.io.DefaultModelReader;
import org.apache.maven.model.management.DefaultDependencyManagementInjector;
import org.apache.maven.model.management.DefaultPluginManagementInjector;
import org.apache.maven.model.normalization.DefaultModelNormalizer;
import org.apache.maven.model.path.DefaultModelPathTranslator;
import org.apache.maven.model.path.DefaultModelUrlNormalizer;
import org.apache.maven.model.path.DefaultPathTranslator;
import org.apache.maven.model.path.DefaultUrlNormalizer;
import org.apache.maven.model.path.ProfileActivationFilePathInterpolator;
import org.apache.maven.model.profile.DefaultProfileInjector;
import org.apache.maven.model.profile.DefaultProfileSelector;
import org.apache.maven.model.resolution.ModelResolver;
import org.apache.maven.model.superpom.DefaultSuperPomProvider;
import org.apache.maven.model.validation.DefaultModelValidator;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.project.ProjectModelResolver;
import org.apache.maven.repository.internal.DefaultVersionRangeResolver;
import org.apache.maven.repository.internal.DefaultVersionResolver;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.RequestTrace;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.impl.LocalRepositoryProvider;
import org.eclipse.aether.internal.impl.DefaultArtifactResolver;
import org.eclipse.aether.internal.impl.DefaultChecksumPolicyProvider;
import org.eclipse.aether.internal.impl.DefaultFileProcessor;
import org.eclipse.aether.internal.impl.DefaultLocalRepositoryProvider;
import org.eclipse.aether.internal.impl.DefaultMetadataResolver;
import org.eclipse.aether.internal.impl.DefaultRemoteRepositoryManager;
import org.eclipse.aether.internal.impl.DefaultRepositoryConnectorProvider;
import org.eclipse.aether.internal.impl.DefaultRepositoryEventDispatcher;
import org.eclipse.aether.internal.impl.DefaultRepositoryLayoutProvider;
import org.eclipse.aether.internal.impl.DefaultRepositorySystem;
import org.eclipse.aether.internal.impl.DefaultSyncContextFactory;
import org.eclipse.aether.internal.impl.DefaultTransporterProvider;
import org.eclipse.aether.internal.impl.DefaultUpdateCheckManager;
import org.eclipse.aether.internal.impl.DefaultUpdatePolicyAnalyzer;
import org.eclipse.aether.internal.impl.Maven2RepositoryLayoutFactory;
import org.eclipse.aether.internal.impl.SimpleLocalRepositoryManagerFactory;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.LocalRepositoryManager;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.spi.localrepo.LocalRepositoryManagerFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// todo: do not use aether as a lib, either impl it from scratch (see tomee for ex) or use maven injections
public class Parser
{

    private static final Pattern VERSION_FILE_PATTERN = Pattern.compile( "^(.*)-([0-9]{8}\\.[0-9]{6})-([0-9]+)$" );
    /**
     * Default version if none present in the url.
     */
    public static final String VERSION_LATEST = "LATEST";

    /**
     * Syntax for the url; to be shown on exception messages.
     */
    private static final String SYNTAX = "mvn:[repository_url!]groupId/artifactId[/[version]/[type]]";

    /**
     * Separator between repository and artifact definition.
     */
    private static final String REPOSITORY_SEPARATOR = "!";
    /**
     * Artifact definition segments separator.
     */
    private static final String ARTIFACT_SEPARATOR = "/";

    /**
     * Snapshot version
     */
    private static final String VERSION_SNAPSHOT = "SNAPSHOT";
    /**
     * Default type if not present in the url.
     */
    private static final String TYPE_JAR = "jar";

    /**
     * Final artifact path separator.
     */
    public static final String FILE_SEPARATOR = "/";
    /**
     * Group id path separator.
     */
    private static final String GROUP_SEPARATOR = "\\.";
    /**
     * Separator used to constructs the artifact file name.
     */
    private static final String VERSION_SEPARATOR = "-";
    /**
     * Artifact extension(type) separator.
     */
    private static final String TYPE_SEPARATOR = ".";
    /**
     * Separator used to separate classifier in artifact name.
     */
    private static final String CLASSIFIER_SEPARATOR = "-";
    /**
     * Maven metadata file.
     */
    private static final String METADATA_FILE = "maven-metadata.xml";
    /**
     * Maven local metadata file.
     */
    private static final String METADATA_FILE_LOCAL = "maven-metadata-local.xml";

    /**
     * Repository URL. Null if not present.
     */
    private String m_repositoryURL;
    /**
     * Artifact group id.
     */
    private String m_group;
    /**
     * Artifact id.
     */
    private String m_artifact;
    /**
     * Artifact version.
     */
    private String m_version;
    /**
     * Artifact type.
     */
    private String m_type;
    /**
     * Artifact classifier.
     */
    private String m_classifier;
    /**
     * Artifact classifier to use to build artifact name.
     */
    private String m_fullClassifier;

    /**
     * Creates a new protocol parser.
     *
     * @param path the mvn URL
     * @throws MalformedURLException if provided path does not comply to expected syntax or an malformed repository URL
     */
    public Parser( String path )
            throws MalformedURLException
    {
        if( path == null )
        {
            throw new MalformedURLException( "Path cannot be null. Syntax " + SYNTAX );
        }
        if (path.startsWith("mvn:")) {
            path = path.substring("mvn:".length());
        }
        if( path.startsWith( REPOSITORY_SEPARATOR ) || path.endsWith( REPOSITORY_SEPARATOR ) )
        {
            throw new MalformedURLException(
                    "Path cannot start or end with " + REPOSITORY_SEPARATOR + ". Syntax " + SYNTAX
            );
        }
        if( path.contains( REPOSITORY_SEPARATOR ) )
        {
            int pos = path.lastIndexOf( REPOSITORY_SEPARATOR );
            parseArtifactPart( path.substring( pos + 1 ) );
            m_repositoryURL = path.substring( 0, pos );
        }
        else
        {
            parseArtifactPart( path );
        }
    }

    /**
     * Return the artifact path from the given maven uri.
     * @param uri the Maven URI.
     * @return the artifact actual path.
     * @throws MalformedURLException in case of "bad" provided URL/URI.
     */
    public static String pathFromMaven(String uri) throws MalformedURLException {
        return pathFromMaven(uri, null);
    }

    public static String pathFromMaven(String uri, String resolved) throws MalformedURLException {
        if (!uri.startsWith("mvn:")) {
            return uri;
        }
        Parser parser = new Parser(uri.substring("mvn:".length()));
        if (resolved != null) {
            String grp = FILE_SEPARATOR
                    + parser.getGroup().replaceAll(GROUP_SEPARATOR, FILE_SEPARATOR)
                    + FILE_SEPARATOR
                    + parser.getArtifact()
                    + FILE_SEPARATOR;
            int idx = resolved.indexOf(grp);
            if (idx >= 0) {
                String version = resolved.substring(idx + grp.length(), resolved.indexOf('/', idx + grp.length()));
                return parser.getArtifactPath(version);
            }

        }
        return parser.getArtifactPath();
    }

    public static String pathToMaven(String location, Map<String, String> parts) {
        String[] p = location.split("/");
        if (p.length >= 4 && p[p.length-1].startsWith(p[p.length-3] + "-" + p[p.length-2])) {
            String artifactId = p[p.length-3];
            String version = p[p.length-2];
            String classifier;
            String type;
            String artifactIdVersion = artifactId + "-" + version;
            StringBuilder sb = new StringBuilder();
            if (p[p.length-1].charAt(artifactIdVersion.length()) == '-') {
                classifier = p[p.length-1].substring(artifactIdVersion.length() + 1, p[p.length-1].lastIndexOf('.'));
            } else {
                classifier = null;
            }
            type = p[p.length-1].substring(p[p.length-1].lastIndexOf('.') + 1);
            sb.append("mvn:");
            if (parts != null) {
                parts.put("artifactId", artifactId);
                parts.put("version", version);
                parts.put("classifier", classifier);
                parts.put("type", type);
            }
            for (int j = 0; j < p.length - 3; j++) {
                if (j > 0) {
                    sb.append('.');
                }
                sb.append(p[j]);
            }
            sb.append('/').append(artifactId).append('/').append(version);
            if (!"jar".equals(type) || classifier != null) {
                sb.append('/');
                if (!"jar".equals(type)) {
                    sb.append(type);
                }
                if (classifier != null) {
                    sb.append('/').append(classifier);
                }
            }
            return sb.toString();
        }
        return location;
    }

    public static String pathToMaven(String location) {
        return pathToMaven(location, null);
    }

    /**
     * Parse the artifact part of the url (without the repository).
     *
     * @param part url part without protocol and repository.
     * @throws MalformedURLException if provided path does not comply to syntax.
     */
    private void parseArtifactPart( final String part )
            throws MalformedURLException
    {
        String[] segments = part.split( ARTIFACT_SEPARATOR );
        if( segments.length < 2 )
        {
            throw new MalformedURLException( "Invalid path. Syntax " + SYNTAX );
        }
        // we must have a valid group
        m_group = segments[ 0 ];
        if( m_group.trim().length() == 0 )
        {
            throw new MalformedURLException( "Invalid groupId. Syntax " + SYNTAX );
        }
        // valid artifact
        m_artifact = segments[ 1 ];
        if( m_artifact.trim().length() == 0 )
        {
            throw new MalformedURLException( "Invalid artifactId. Syntax " + SYNTAX );
        }
        // version is optional but we have a default value 
        m_version = VERSION_LATEST;
        if( segments.length >= 3 && segments[ 2 ].trim().length() > 0 )
        {
            m_version = segments[ 2 ];
        }
        // type is optional but we have a default value
        m_type = TYPE_JAR;
        if( segments.length >= 4 && segments[ 3 ].trim().length() > 0 )
        {
            m_type = segments[ 3 ];
        }
        // classifier is optional (if not present or empty we will have a null classifier
        m_fullClassifier = "";
        if( segments.length >= 5 && segments[ 4 ].trim().length() > 0 )
        {
            m_classifier = segments[ 4 ];
            m_fullClassifier = CLASSIFIER_SEPARATOR + m_classifier;
        }
    }

    /**
     * Return the repository URL if present, null otherwise.
     *
     * @return repository URL.
     */
    public String getRepositoryURL()
    {
        return m_repositoryURL;
    }

    /**
     * Prints parsed mvn: URI (after possible change of any component)
     * @return
     */
    public String toMvnURI()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(m_group).append(ARTIFACT_SEPARATOR).append(m_artifact).append(ARTIFACT_SEPARATOR).append(m_version);
        if (!TYPE_JAR.equals(m_type)) {
            sb.append(ARTIFACT_SEPARATOR).append(m_type);
        }
        if (m_classifier != null && !"".equals(m_classifier)) {
            if (TYPE_JAR.equals(m_type)) {
                sb.append(ARTIFACT_SEPARATOR).append(m_type);
            }
            sb.append(ARTIFACT_SEPARATOR).append(m_classifier);
        }

        return sb.toString();
    }

    /**
     * Return the group id of the artifact.
     *
     * @return group ID.
     */
    public String getGroup()
    {
        return m_group;
    }

    /**
     * Return the artifact id.
     *
     * @return artifact id.
     */
    public String getArtifact()
    {
        return m_artifact;
    }

    /**
     * Return the artifact version.
     *
     * @return version.
     */
    public String getVersion()
    {
        return m_version;
    }

    /**
     * Return the artifact type.
     *
     * @return type.
     */
    public String getType()
    {
        return m_type;
    }

    /**
     * Return the artifact classifier.
     *
     * @return classifier.
     */
    public String getClassifier()
    {
        return m_classifier;
    }

    /**
     * Changes parsed group - to allow printing mvn: URI with changed groupId
     * @param m_group
     */
    public void setGroup(String m_group)
    {
        this.m_group = m_group;
    }

    /**
     * Changes parsed artifact - to allow printing mvn: URI with changed artifactId
     * @param m_artifact
     */
    public void setArtifact(String m_artifact)
    {
        this.m_artifact = m_artifact;
    }

    /**
     * Changes parsed version - to allow printing mvn: URI with changed version
     * @param m_version
     */
    public void setVersion(String m_version)
    {
        this.m_version = m_version;
    }

    /**
     * Changes parsed type - to allow printing mvn: URI with changed type
     * @param m_type
     */
    public void setType(String m_type)
    {
        this.m_type = m_type;
    }

    /**
     * Changes parsed classifier - to allow printing mvn: URI with changed classifier
     * @param m_classifier
     */
    public void setClassifier(String m_classifier)
    {
        this.m_classifier = m_classifier;
    }

    /**
     * Return the complete path to artifact as stated by Maven 2 repository layout.
     *
     * @return artifact path.
     */
    public String getArtifactPath()
    {
        return getArtifactPath( m_version );
    }

    /**
     * Return the complete path to artifact as stated by Maven 2 repository layout.
     *
     * @param version The version of the artifact.
     * @return artifact path.
     */
    public String getArtifactPath( final String version )
    {

        Matcher m = VERSION_FILE_PATTERN.matcher(version);

        if ( m.matches() )
        {
            this.m_version = m.group( 1 ) + "-" + "SNAPSHOT";
            String ret = m_group.replaceAll(GROUP_SEPARATOR, FILE_SEPARATOR)
                    + FILE_SEPARATOR
                    + m_artifact
                    + FILE_SEPARATOR
                    + m_version
                    + FILE_SEPARATOR
                    + m_artifact
                    + VERSION_SEPARATOR
                    + m_version
                    + m_fullClassifier
                    + TYPE_SEPARATOR
                    + m_type;
            if (m_repositoryURL != null) {
                ret = m_repositoryURL + FILE_SEPARATOR + ret;
            }
            return ret;
        } else {
            String ret = m_group.replaceAll(GROUP_SEPARATOR, FILE_SEPARATOR)
                    + FILE_SEPARATOR
                    + m_artifact
                    + FILE_SEPARATOR
                    + version
                    + FILE_SEPARATOR
                    + m_artifact
                    + VERSION_SEPARATOR
                    + version
                    + m_fullClassifier
                    + TYPE_SEPARATOR
                    + m_type;
            if (m_repositoryURL != null) {
                ret = m_repositoryURL + FILE_SEPARATOR + ret;
            }
            return ret;
        }
    }

    /**
     * Return the version for an artifact for a snapshot version.
     *
     * @param version The version of the snapshot.
     * @param timestamp The timestamp of the snapshot.
     * @param buildnumber The buildnumber of the snapshot.
     * @return artifact path.
     */
    public String getSnapshotVersion( final String version, final String timestamp, final String buildnumber )
    {
        return version.replace( VERSION_SNAPSHOT, timestamp ) + VERSION_SEPARATOR + buildnumber;
    }

    /**
     * Return the complete path to artifact for a snapshot file.
     *
     * @param version The version of the snapshot.
     * @param timestamp The timestamp of the snapshot.
     * @param buildnumber The buildnumber of the snapshot.
     * @return artifact path.
     */
    public String getSnapshotPath( final String version, final String timestamp, final String buildnumber )
    {
        return m_group.replaceAll(GROUP_SEPARATOR, FILE_SEPARATOR)
                + FILE_SEPARATOR
                + m_artifact
                + FILE_SEPARATOR
                + version
                + FILE_SEPARATOR
                + m_artifact
                + VERSION_SEPARATOR
                + getSnapshotVersion(version, timestamp, buildnumber)
                + m_fullClassifier
                + TYPE_SEPARATOR
                + m_type;
    }

    /**
     * Return the path to metadata file corresponding to this artifact version.
     *
     * @param version The version of the the metadata.
     * @return metadata file path.
     */
    public String getVersionMetadataPath( final String version )
    {
        return m_group.replaceAll(GROUP_SEPARATOR, FILE_SEPARATOR)
                + FILE_SEPARATOR
                + m_artifact
                + FILE_SEPARATOR
                + version
                + FILE_SEPARATOR
                + METADATA_FILE;
    }

    /**
     * Return the path to local metadata file corresponding to this artifact version.
     *
     * @param version The version of the the metadata.
     * @return metadata file path.
     */
    public String getVersionLocalMetadataPath( final String version )
    {
        return m_group.replaceAll(GROUP_SEPARATOR, FILE_SEPARATOR)
                + FILE_SEPARATOR
                + m_artifact
                + FILE_SEPARATOR
                + version
                + FILE_SEPARATOR
                + METADATA_FILE_LOCAL;
    }

    /**
     * Return the complete path to artifact local metadata file.
     *
     * @return artifact path.
     */
    public String getArtifactLocalMetdataPath()
    {
        return m_group.replaceAll(GROUP_SEPARATOR, FILE_SEPARATOR)
                + FILE_SEPARATOR
                + m_artifact
                + FILE_SEPARATOR
                + METADATA_FILE_LOCAL;
    }

    /**
     * Return the complete path to artifact metadata file.
     *
     * @return artifact path.
     */
    public String getArtifactMetdataPath()
    {
        return m_group.replaceAll(GROUP_SEPARATOR, FILE_SEPARATOR)
                + FILE_SEPARATOR
                + m_artifact
                + FILE_SEPARATOR
                + METADATA_FILE;
    }

    public static void getDependencies(File pom, List<String> dependencies, boolean includeTransitive) throws Exception {
        DefaultModelBuilder modelBuilder = new DefaultModelBuilder();
        modelBuilder.setProfileSelector(new DefaultProfileSelector());
        DefaultModelProcessor modelProcessor = new DefaultModelProcessor();
        modelProcessor.setModelReader(new DefaultModelReader());
        modelBuilder.setModelProcessor(modelProcessor);
        modelBuilder.setModelValidator(new DefaultModelValidator(new DefaultModelVersionProcessor()));
        DefaultSuperPomProvider superPomProvider = new DefaultSuperPomProvider();
        superPomProvider.setModelProcessor(modelProcessor);
        modelBuilder.setSuperPomProvider(superPomProvider);
        modelBuilder.setModelNormalizer(new DefaultModelNormalizer());
        modelBuilder.setProfileInjector(new DefaultProfileInjector());
        ProfileActivationFilePathInterpolator profileActivationFilePathInterpolator = new ProfileActivationFilePathInterpolator();
        profileActivationFilePathInterpolator.setPathTranslator(new DefaultPathTranslator());
        modelBuilder.setProfileActivationFilePathInterpolator(profileActivationFilePathInterpolator);
        modelBuilder.setInheritanceAssembler(new DefaultInheritanceAssembler());
        StringVisitorModelInterpolator visitorModelInterpolator = new StringVisitorModelInterpolator();
        visitorModelInterpolator.setVersionPropertiesProcessor(new DefaultModelVersionProcessor());
        visitorModelInterpolator.setPathTranslator(new DefaultPathTranslator());
        visitorModelInterpolator.setUrlNormalizer(new DefaultUrlNormalizer());
        visitorModelInterpolator.setVersionPropertiesProcessor(new DefaultModelVersionProcessor());
        modelBuilder.setModelInterpolator(visitorModelInterpolator);
        DefaultModelUrlNormalizer modelUrlNormalizer = new DefaultModelUrlNormalizer();
        modelUrlNormalizer.setUrlNormalizer(new DefaultUrlNormalizer());
        modelBuilder.setModelUrlNormalizer(modelUrlNormalizer);
        DefaultModelPathTranslator modelPathTranslator = new DefaultModelPathTranslator();
        modelPathTranslator.setPathTranslator(new DefaultPathTranslator());
        modelBuilder.setModelPathTranslator(modelPathTranslator);
        modelBuilder.setPluginManagementInjector(new DefaultPluginManagementInjector());
        modelBuilder.setDependencyManagementInjector(new DefaultDependencyManagementInjector());
        modelBuilder.setDependencyManagementImporter(new DefaultDependencyManagementImporter());

        DefaultRepositorySystemSession repositorySystemSession = getRepositorySystemSession();
        LocalRepositoryProvider localRepositoryProvider = getLocalRepositoryProvider(repositorySystemSession);

        DefaultArtifactResolver resolver = getArtifactResolver(repositorySystemSession,
                localRepositoryProvider,
                getRemoteRepositories());

        DefaultSyncContextFactory syncContextFactory = new DefaultSyncContextFactory();
        DefaultRepositoryEventDispatcher repositoryEventDispatcher = new DefaultRepositoryEventDispatcher();
        DefaultVersionResolver versionResolver = new DefaultVersionResolver();
        DefaultMetadataResolver metadataResolver = new DefaultMetadataResolver();
        metadataResolver.setSyncContextFactory(syncContextFactory);
        metadataResolver.setRepositoryEventDispatcher(repositoryEventDispatcher);
        versionResolver.setMetadataResolver(metadataResolver);
        versionResolver.setSyncContextFactory(syncContextFactory);
        versionResolver.setRepositoryEventDispatcher(repositoryEventDispatcher);

        DefaultRepositorySystem repositorySystem = new DefaultRepositorySystem();
        repositorySystem.setLocalRepositoryProvider(localRepositoryProvider);
        repositorySystem.setVersionResolver(versionResolver);

        DefaultVersionRangeResolver versionRangeResolver = new DefaultVersionRangeResolver();
        versionRangeResolver.setMetadataResolver(metadataResolver);
        versionRangeResolver.setRepositoryEventDispatcher(repositoryEventDispatcher);
        versionRangeResolver.setSyncContextFactory(syncContextFactory);

        repositorySystem.setVersionRangeResolver(versionRangeResolver);
        repositorySystem.setArtifactResolver(resolver);

        ModelResolver modelResolver = new ProjectModelResolver(
                repositorySystemSession,
                new RequestTrace(null),
                repositorySystem,
                new DefaultRemoteRepositoryManager(),
                new ArrayList<RemoteRepository>(),
                ProjectBuildingRequest.RepositoryMerging.POM_DOMINANT,
                null);


        ModelBuildingRequest request = new DefaultModelBuildingRequest();
        request.setModelResolver(modelResolver);
        request.setPomFile(pom);
        Model model = modelBuilder.build(request).getEffectiveModel();

        for (Dependency dependency : model.getDependencies()) {
            if (!dependency.getScope().equals("test")) {
                dependencies.add("mvn:" + dependency.getGroupId() + "/" + dependency.getArtifactId() + "/" + dependency.getVersion() + "/" + dependency.getType());
                if (includeTransitive) {
                    // recursion
                    File transitivePom = Parser.resolve("mvn:" + dependency.getGroupId() + "/" + dependency.getArtifactId() + "/" + dependency.getVersion() + "/pom");
                    Parser.getDependencies(transitivePom, dependencies, includeTransitive);
                }
            }
        }
    }

    public static File resolve(String uri) throws Exception {

        DefaultRepositorySystemSession repositorySystemSession = getRepositorySystemSession();
        LocalRepositoryProvider localRepositoryProvider = getLocalRepositoryProvider(repositorySystemSession);

        List<RemoteRepository> remoteRepositories = getRemoteRepositories();

        DefaultArtifactResolver resolver = getArtifactResolver(repositorySystemSession, localRepositoryProvider, remoteRepositories);

        Parser parser = new Parser(uri);
        Artifact artifact = new DefaultArtifact(parser.getGroup(), parser.getArtifact(), parser.getType(), parser.getVersion());
        ArtifactRequest request = new ArtifactRequest();
        request.setArtifact(artifact);
        request.setRepositories(remoteRepositories);

        ArtifactResult result = resolver.resolveArtifact(repositorySystemSession, request);
        return result.getArtifact().getFile();
    }

    private static LocalRepositoryProvider getLocalRepositoryProvider(DefaultRepositorySystemSession repositorySystemSession) throws Exception {
        LocalRepository localRepository = new LocalRepository(System.getProperty("user.home") + "/.m2/repository");
        DefaultLocalRepositoryProvider localRepositoryProvider = new DefaultLocalRepositoryProvider();
        LocalRepositoryManagerFactory localRepositoryManagerFactory = new SimpleLocalRepositoryManagerFactory();
        localRepositoryProvider.setLocalRepositoryManagerFactories(Collections.singletonList(localRepositoryManagerFactory));
        LocalRepositoryManager localRepositoryManager = localRepositoryProvider.newLocalRepositoryManager(repositorySystemSession, localRepository);
        repositorySystemSession.setLocalRepositoryManager(localRepositoryManager);
        return localRepositoryProvider;
    }

    private static DefaultRepositorySystemSession getRepositorySystemSession() throws Exception {
        return new DefaultRepositorySystemSession();
    }

    private static DefaultArtifactResolver getArtifactResolver(RepositorySystemSession repositorySystemSession,
                                                               LocalRepositoryProvider localRepositoryProvider,
                                                               List<RemoteRepository> remoteRepositories) throws Exception {
        DefaultArtifactResolver artifactResolver = new DefaultArtifactResolver();

        DefaultSyncContextFactory syncContextFactory = new DefaultSyncContextFactory();
        DefaultRepositoryEventDispatcher repositoryEventDispatcher = new DefaultRepositoryEventDispatcher();
        DefaultVersionResolver versionResolver = new DefaultVersionResolver();
        DefaultMetadataResolver metadataResolver = new DefaultMetadataResolver();
        metadataResolver.setSyncContextFactory(syncContextFactory);
        metadataResolver.setRepositoryEventDispatcher(repositoryEventDispatcher);
        versionResolver.setMetadataResolver(metadataResolver);
        versionResolver.setSyncContextFactory(syncContextFactory);
        versionResolver.setRepositoryEventDispatcher(repositoryEventDispatcher);

        DefaultRepositorySystem repositorySystem = new DefaultRepositorySystem();
        repositorySystem.setLocalRepositoryProvider(localRepositoryProvider);
        repositorySystem.setVersionResolver(versionResolver);

        DefaultRemoteRepositoryManager remoteRepositoryManager = new DefaultRemoteRepositoryManager();
        metadataResolver.setRemoteRepositoryManager(remoteRepositoryManager);

        DefaultUpdateCheckManager updateCheckManager = new DefaultUpdateCheckManager();
        updateCheckManager.setUpdatePolicyAnalyzer(new DefaultUpdatePolicyAnalyzer());
        metadataResolver.setUpdateCheckManager(updateCheckManager);

        remoteRepositoryManager.aggregateRepositories(repositorySystemSession, remoteRepositories, new ArrayList<>(), false);
        remoteRepositoryManager.setChecksumPolicyProvider(new DefaultChecksumPolicyProvider());
        remoteRepositoryManager.setUpdatePolicyAnalyzer(new DefaultUpdatePolicyAnalyzer());
        repositorySystem.setRemoteRepositoryManager(remoteRepositoryManager);

        DefaultVersionRangeResolver versionRangeResolver = new DefaultVersionRangeResolver();
        versionRangeResolver.setMetadataResolver(metadataResolver);
        versionRangeResolver.setSyncContextFactory(syncContextFactory);
        versionRangeResolver.setRepositoryEventDispatcher(repositoryEventDispatcher);

        artifactResolver.setVersionResolver(versionResolver);
        artifactResolver.setSyncContextFactory(syncContextFactory);
        artifactResolver.setRepositoryEventDispatcher(repositoryEventDispatcher);

        DefaultRepositoryConnectorProvider repositoryConnectorProvider = new DefaultRepositoryConnectorProvider();
        BasicRepositoryConnectorFactory repositoryConnectorFactory = new BasicRepositoryConnectorFactory();
        DefaultRepositoryLayoutProvider repositoryLayoutProvider = new DefaultRepositoryLayoutProvider();
        repositoryLayoutProvider.setRepositoryLayoutFactories(Collections.singletonList(new Maven2RepositoryLayoutFactory()));
        DefaultTransporterProvider transporterProvider = new DefaultTransporterProvider();
        transporterProvider.addTransporterFactory(new HttpTransporterFactory());
        repositoryConnectorFactory.setRepositoryLayoutProvider(repositoryLayoutProvider);
        repositoryConnectorFactory.setTransporterProvider(transporterProvider);
        repositoryConnectorFactory.setFileProcessor(new DefaultFileProcessor());
        repositoryConnectorFactory.setChecksumPolicyProvider(new DefaultChecksumPolicyProvider());
        repositoryConnectorProvider.addRepositoryConnectorFactory(repositoryConnectorFactory);
        artifactResolver.setRepositoryConnectorProvider(repositoryConnectorProvider);
        artifactResolver.setRemoteRepositoryManager(remoteRepositoryManager);

        metadataResolver.setRepositoryConnectorProvider(repositoryConnectorProvider);

        repositorySystem.setVersionRangeResolver(versionRangeResolver);
        repositorySystem.setArtifactResolver(artifactResolver);

        return artifactResolver;
    }

    // todo: this will not work very quickly
    private static List<RemoteRepository> getRemoteRepositories() {
        return List.of(new RemoteRepository.Builder("maven-central", "default", "https://repo.maven.apache.org/maven2").build());
    }
}