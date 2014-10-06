/*******************************************************************************
 * Copyright (c) 2012-2014 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package com.codenvy.ide.maven.tools;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** @author andrew00x */
public class MavenUtilTest {

    @Test
    public void testGetArtifactId() throws IOException {
        File workDir = new File(System.getProperty("workDir"));
        File pom = new File(workDir, "testGetGroupId-pom.xml");
        Model model = new Model();
        model.setArtifactId("fake-artifact");
        model.setGroupId("fake-group");
        model.setVersion("fake-version");
        model.setPomFile(pom);
        MavenUtils.writeModel(model);
        Assert.assertEquals("fake-artifact", MavenUtils.getArtifactId(pom));
    }

    @Test
    public void testGetGroupId() throws IOException {
        File workDir = new File(System.getProperty("workDir"));
        File pom = new File(workDir, "testGetGroupId-pom.xml");
        Model model = new Model();
        model.setArtifactId("fake-artifact");
        model.setGroupId("fake-group");
        model.setVersion("fake-version");
        model.setPomFile(pom);
        MavenUtils.writeModel(model);
        Assert.assertEquals("fake-group", MavenUtils.getGroupId(pom));
    }

    @Test
    public void testGetVersion() throws IOException {
        File workDir = new File(System.getProperty("workDir"));
        File pom = new File(workDir, "testGetVersion-pom.xml");
        Model model = new Model();
        model.setArtifactId("fake-artifact");
        model.setGroupId("fake-group");
        model.setVersion("fake-version");
        model.setPomFile(pom);
        MavenUtils.writeModel(model);
        Assert.assertEquals("fake-version", MavenUtils.getVersion(pom));
    }

    @Test
    public void testChangeArtifactId() throws IOException {
        File workDir = new File(System.getProperty("workDir"));
        File pom = new File(workDir, "testChangeArtifactId-pom.xml");
        Model model = new Model();
        model.setArtifactId("fake-artifact");
        model.setGroupId("fake-group");
        model.setVersion("fake-version");
        model.setPomFile(pom);
        MavenUtils.writeModel(model);
        MavenUtils.setArtifactId(pom, "new-artifact-id");
        Assert.assertEquals("new-artifact-id", MavenUtils.readModel(pom).getArtifactId());
    }

    @Test
    public void testChangeGroupId() throws IOException {
        File workDir = new File(System.getProperty("workDir"));
        File pom = new File(workDir, "testChangeGroupId-pom.xml");
        Model model = new Model();
        model.setArtifactId("fake-artifact");
        model.setGroupId("fake-group");
        model.setVersion("fake-version");
        model.setPomFile(pom);
        MavenUtils.writeModel(model);
        MavenUtils.setGroupId(pom, "new-group-id");
        Assert.assertEquals("new-group-id", MavenUtils.readModel(pom).getGroupId());
    }

    @Test
    public void testChangeVersion() throws IOException {
        File workDir = new File(System.getProperty("workDir"));
        File pom = new File(workDir, "testChangeVersion-pom.xml");
        Model model = new Model();
        model.setArtifactId("fake-artifact");
        model.setGroupId("fake-group");
        model.setVersion("fake-version");
        model.setPomFile(pom);
        MavenUtils.writeModel(model);
        MavenUtils.setVersion(pom, "new-version");
        Assert.assertEquals("new-version", MavenUtils.readModel(pom).getVersion());
    }

    @Test
    public void testSetGroupId() throws IOException {
        File workDir = new File(System.getProperty("workDir"));
        File pom = new File(workDir, "testSetGroupId-pom.xml");
        Model model = new Model();
        model.setArtifactId("fake-artifact");
        model.setVersion("fake-version");
        model.setPomFile(pom);
        MavenUtils.writeModel(model);
        MavenUtils.setGroupId(pom, "new-group-id");
        Assert.assertEquals("new-group-id", MavenUtils.readModel(pom).getGroupId());
    }

    @Test
    public void testReadPom() throws IOException {
        URL pom = Thread.currentThread().getContextClassLoader().getResource("test-pom.xml");
        Assert.assertNotNull(pom);
        Model model = MavenUtils.readModel(new File(pom.getFile()));
        Assert.assertEquals("a", model.getArtifactId());
        Parent parent = model.getParent();
        Assert.assertEquals("parent", parent.getGroupId());
        Assert.assertEquals("parent", parent.getArtifactId());
        Assert.assertEquals("x.x.x", parent.getVersion());
        List<Dependency> dependencies = model.getDependencies();
        Assert.assertEquals(dependencies.size(), 1);
        Dependency dependency = dependencies.get(0);
        Assert.assertEquals("x", dependency.getGroupId());
        Assert.assertEquals("y", dependency.getArtifactId());
        Assert.assertEquals("z", dependency.getVersion());
    }

    @Test
    public void testWrite() throws Exception {
        File workDir = new File(System.getProperty("workDir"));
        File pom = new File(workDir, "testWrite-pom.xml");
        List<Dependency> deps = new ArrayList<>(1);
        Dependency dependency = new Dependency();
        dependency.setGroupId("x");
        dependency.setArtifactId("y");
        dependency.setVersion("z");
        dependency.setScope("test");
        deps.add(dependency);
        Model model = new Model();
        model.setGroupId("a");
        model.setArtifactId("b");
        model.setVersion("c");
        model.setDescription("test pom");
        model.setDependencies(deps);
        model.setPomFile(pom);
        MavenUtils.writeModel(model);
        DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document dom = documentBuilder.parse(pom);
        XPathFactory xpathFactory = XPathFactory.newInstance();
        XPath xpath = xpathFactory.newXPath();
        Assert.assertEquals("a", xpath.evaluate("/project/groupId", dom, XPathConstants.STRING));
        Assert.assertEquals("b", xpath.evaluate("/project/artifactId", dom, XPathConstants.STRING));
        Assert.assertEquals("c", xpath.evaluate("/project/version", dom, XPathConstants.STRING));
        Assert.assertEquals("test pom", xpath.evaluate("/project/description", dom, XPathConstants.STRING));
        NodeList depsNodeList = (NodeList)xpath.evaluate("/project/dependencies", dom, XPathConstants.NODESET);
        Assert.assertEquals(1, depsNodeList.getLength());
        Node node = depsNodeList.item(0);
        Assert.assertEquals("x", xpath.evaluate("dependency/groupId", node, XPathConstants.STRING));
        Assert.assertEquals("y", xpath.evaluate("dependency/artifactId", node, XPathConstants.STRING));
        Assert.assertEquals("z", xpath.evaluate("dependency/version", node, XPathConstants.STRING));
        Assert.assertEquals("test", xpath.evaluate("dependency/scope", node, XPathConstants.STRING));
    }

    @Test
    public void testWriteToFile() throws Exception {
        File workDir = new File(System.getProperty("workDir"));
        File pom = new File(workDir, "testWrite-pom.xml");
        File pom2 = new File(workDir, "testWriteToFile-pom.xml");
        List<Dependency> deps = new ArrayList<>(1);
        Dependency dependency = new Dependency();
        dependency.setGroupId("x");
        dependency.setArtifactId("y");
        dependency.setVersion("z");
        dependency.setScope("test");
        deps.add(dependency);
        Model model = new Model();
        model.setGroupId("a");
        model.setArtifactId("b");
        model.setVersion("c");
        model.setDescription("test pom");
        model.setDependencies(deps);
        model.setPomFile(pom);
        MavenUtils.writeModel(model, pom2);
        DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document dom = documentBuilder.parse(pom2);
        XPathFactory xpathFactory = XPathFactory.newInstance();
        XPath xpath = xpathFactory.newXPath();
        Assert.assertEquals("a", xpath.evaluate("/project/groupId", dom, XPathConstants.STRING));
        Assert.assertEquals("b", xpath.evaluate("/project/artifactId", dom, XPathConstants.STRING));
        Assert.assertEquals("c", xpath.evaluate("/project/version", dom, XPathConstants.STRING));
        Assert.assertEquals("test pom", xpath.evaluate("/project/description", dom, XPathConstants.STRING));
        NodeList depsNodeList = (NodeList)xpath.evaluate("/project/dependencies", dom, XPathConstants.NODESET);
        Assert.assertEquals(depsNodeList.getLength(), 1);
        Node node = depsNodeList.item(0);
        Assert.assertEquals("x", xpath.evaluate("dependency/groupId", node, XPathConstants.STRING));
        Assert.assertEquals("y", xpath.evaluate("dependency/artifactId", node, XPathConstants.STRING));
        Assert.assertEquals("z", xpath.evaluate("dependency/version", node, XPathConstants.STRING));
        Assert.assertEquals("test", xpath.evaluate("dependency/scope", node, XPathConstants.STRING));
    }

    @Test
    public void testAddDependency() throws Exception {
        File workDir = new File(System.getProperty("workDir"));
        File pom = new File(workDir, "testAddDependency-pom.xml");
        Model model = new Model();
        model.setGroupId("a");
        model.setArtifactId("b");
        model.setVersion("c");
        model.setDescription("test pom");
        model.setPomFile(pom);
        MavenUtils.writeModel(model);
        Dependency dependency = new Dependency();
        dependency.setGroupId("x");
        dependency.setArtifactId("y");
        dependency.setVersion("z");
        dependency.setScope("test");
        MavenUtils.addDependency(pom, dependency);
        DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document dom = documentBuilder.parse(pom);
        XPathFactory xpathFactory = XPathFactory.newInstance();
        XPath xpath = xpathFactory.newXPath();
        NodeList depsNodeList = (NodeList)xpath.evaluate("/project/dependencies", dom, XPathConstants.NODESET);
        Assert.assertEquals(depsNodeList.getLength(), 1);
        Node node = depsNodeList.item(0);
        Assert.assertEquals("x", xpath.evaluate("dependency/groupId", node, XPathConstants.STRING));
        Assert.assertEquals("y", xpath.evaluate("dependency/artifactId", node, XPathConstants.STRING));
        Assert.assertEquals("z", xpath.evaluate("dependency/version", node, XPathConstants.STRING));
        Assert.assertEquals("test", xpath.evaluate("dependency/scope", node, XPathConstants.STRING));
    }

    @Test
    public void testAddModule() throws Exception {
        File workDir = new File(System.getProperty("workDir"));
        File pom = new File(workDir, "testAddModule-pom.xml");
        Model model = new Model();
        model.setGroupId("a");
        model.setArtifactId("b");
        model.setVersion("c");
        model.setDescription("test pom");
        model.setPackaging("pom");
        model.setPomFile(pom);
        MavenUtils.writeModel(model);

        MavenUtils.addModule(pom, "test-module");
        DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document dom = documentBuilder.parse(pom);
        XPathFactory xpathFactory = XPathFactory.newInstance();
        XPath xpath = xpathFactory.newXPath();
        NodeList depsNodeList = (NodeList)xpath.evaluate("/project/modules", dom, XPathConstants.NODESET);
        Assert.assertEquals(depsNodeList.getLength(), 1);
        Node node = depsNodeList.item(0);
        Assert.assertEquals("test-module", xpath.evaluate("module", node, XPathConstants.STRING));
    }

    @Test
    public void testAddSourceFolder() throws Exception {
        File workDir = new File(System.getProperty("workDir"));
        File pom = new File(workDir, "testAddSourceFolder-pom.xml");
        Model model = new Model();
        model.setGroupId("a");
        model.setArtifactId("b");
        model.setVersion("c");
        model.setDescription("test pom");
        model.setPackaging("pom");
        model.setPomFile(pom);
        MavenUtils.writeModel(model);

        MavenUtils.setSourceFolder(pom, "aaa/bbb/ccc");
        DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document dom = documentBuilder.parse(pom);
        XPathFactory xpathFactory = XPathFactory.newInstance();
        XPath xpath = xpathFactory.newXPath();
        NodeList depsNodeList = (NodeList)xpath.evaluate("/project/build", dom, XPathConstants.NODESET);
        Assert.assertEquals(depsNodeList.getLength(), 1);
        Node node = depsNodeList.item(0);
        Assert.assertEquals("aaa/bbb/ccc", xpath.evaluate("sourceDirectory", node, XPathConstants.STRING));
    }

    @Test
    public void testAddTestSourceFolder() throws Exception {
        File workDir = new File(System.getProperty("workDir"));
        File pom = new File(workDir, "testAddSourceFolder-pom.xml");
        Model model = new Model();
        model.setGroupId("a");
        model.setArtifactId("b");
        model.setVersion("c");
        model.setDescription("test pom");
        model.setPackaging("pom");
        model.setPomFile(pom);
        MavenUtils.writeModel(model);

        MavenUtils.setTestSourceFolder(pom, "zzz/xxx/ccc");
        DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document dom = documentBuilder.parse(pom);
        XPathFactory xpathFactory = XPathFactory.newInstance();
        XPath xpath = xpathFactory.newXPath();
        NodeList depsNodeList = (NodeList)xpath.evaluate("/project/build", dom, XPathConstants.NODESET);
        Assert.assertEquals(depsNodeList.getLength(), 1);
        Node node = depsNodeList.item(0);
        Assert.assertEquals("zzz/xxx/ccc", xpath.evaluate("testSourceDirectory", node, XPathConstants.STRING));
    }
    @Test
    public void testAddDependencies() throws IOException, ParserConfigurationException, SAXException, XPathExpressionException {
        File workDir = new File(System.getProperty("workDir"));
        File pom = new File(workDir, "testAddDependencies-pom.xml");
        Model model = new Model();
        model.setGroupId("a");
        model.setArtifactId("b");
        model.setVersion("c");
        model.setDescription("test pom");
        model.setPomFile(pom);
        MavenUtils.writeModel(model);
        Dependency dependency1 = new Dependency();
        dependency1.setArtifactId("a1");
        dependency1.setGroupId("g1");
        dependency1.setVersion("v1");
        Dependency dependency2 = new Dependency();
        dependency2.setArtifactId("a2");
        dependency2.setGroupId("g2");
        dependency2.setVersion("v2");
        MavenUtils.addDependencies(pom, dependency1, dependency2);
        DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document dom = documentBuilder.parse(pom);
        XPathFactory xpathFactory = XPathFactory.newInstance();
        XPath xpath = xpathFactory.newXPath();
        NodeList depsNodeList = (NodeList)xpath.evaluate("/project/dependencies/dependency", dom, XPathConstants.NODESET);
        Assert.assertEquals(2, depsNodeList.getLength());
        Node dep1Node = depsNodeList.item(0);
        Assert.assertEquals("a1", xpath.evaluate("artifactId", dep1Node, XPathConstants.STRING));
        Assert.assertEquals("g1", xpath.evaluate("groupId", dep1Node, XPathConstants.STRING));
        Assert.assertEquals("v1", xpath.evaluate("version", dep1Node, XPathConstants.STRING));
        Node dep2Node = depsNodeList.item(1);
        Assert.assertEquals("a2", xpath.evaluate("artifactId", dep2Node, XPathConstants.STRING));
        Assert.assertEquals("g2", xpath.evaluate("groupId", dep2Node, XPathConstants.STRING));
        Assert.assertEquals("v2", xpath.evaluate("version", dep2Node, XPathConstants.STRING));
    }

    @Test
    public void testAddDependencyWithModel() throws Exception {
        File workDir = new File(System.getProperty("workDir"));
        File pom = new File(workDir, "testAddDependencyWithModel-pom.xml");
        Model model = new Model();
        model.setGroupId("a");
        model.setArtifactId("b");
        model.setVersion("c");
        model.setDescription("test pom");
        model.setPomFile(pom);
        MavenUtils.writeModel(model);
        Model dependency = new Model();
        dependency.setGroupId("x");
        dependency.setArtifactId("y");
        dependency.setVersion("z");
        dependency.setDescription("test dependency pom");
        dependency.setPomFile(pom);
        MavenUtils.addDependency(pom, dependency);
        DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document dom = documentBuilder.parse(pom);
        XPathFactory xpathFactory = XPathFactory.newInstance();
        XPath xpath = xpathFactory.newXPath();
        NodeList depsNodeList = (NodeList)xpath.evaluate("/project/dependencies", dom, XPathConstants.NODESET);
        Assert.assertEquals(depsNodeList.getLength(), 1);
        Node node = depsNodeList.item(0);
        Assert.assertEquals("x", xpath.evaluate("dependency/groupId", node, XPathConstants.STRING));
        Assert.assertEquals("y", xpath.evaluate("dependency/artifactId", node, XPathConstants.STRING));
        Assert.assertEquals("z", xpath.evaluate("dependency/version", node, XPathConstants.STRING));
        // there is no 'scope' in this case
        Assert.assertEquals("", xpath.evaluate("dependency/scope", node, XPathConstants.STRING));
    }

    @Test
    public void testGetModules() throws Exception {
        URL project = Thread.currentThread().getContextClassLoader().getResource("multi-module");
        Assert.assertNotNull(project);
        List<Model> modules = MavenUtils.getModules(new File(project.getFile()));
        List<String> expected = Arrays.asList("parent:module1:jar:x.x.x",
                                              "parent:module2:jar:x.x.x",
                                              "project:project-modules-x:pom:x.x.x",
                                              "project:module3:jar:x.x.x",
                                              "project:module4:jar:x.x.x");
        Assert.assertEquals(expected.size(), modules.size());
        List<String> modulesStr = new ArrayList<>(modules.size());
        for (Model model : modules) {
            modulesStr.add(toString(model));
        }
        modulesStr.removeAll(expected);
        Assert.assertTrue("Unexpected modules " + modules, modulesStr.isEmpty());
    }

    @Test
    public void testSetPackaging() throws IOException {
        File workDir = new File(System.getProperty("workDir"));
        File pom = new File(workDir, "testSetGroupId-pom.xml");
        Model model = new Model();
        model.setArtifactId("fake-artifact");
        model.setVersion("fake-version");
        model.setPomFile(pom);
        MavenUtils.writeModel(model);
        MavenUtils.setPackaging(pom, "zip");
        Assert.assertEquals("zip", MavenUtils.readModel(pom).getPackaging());
    }

    @Test
    public void testSetParentVersion() throws IOException {
        File workDir = new File(System.getProperty("workDir"));
        File pom = new File(workDir, "testWithParent-pom.xml");
        Model model = new Model();
        model.setArtifactId("fake-artifact");
        model.setGroupId("fake-group");
        model.setVersion("fake-version");
        model.setPomFile(pom);
        MavenUtils.writeModel(model);
        MavenUtils.setParentVersion(pom, "no-fake");

        Model readModel = MavenUtils.readModel(pom);
        Assert.assertNotNull(readModel.getParent());
        Assert.assertEquals("no-fake", readModel.getParent().getVersion());
    }


    private String toString(Model model) {
        String groupId = model.getGroupId();
        if (groupId == null) {
            Parent parent = model.getParent();
            if (parent != null) {
                groupId = parent.getGroupId();
            }
        }
        String version = model.getVersion();
        if (version == null) {
            Parent parent = model.getParent();
            if (parent != null) {
                version = parent.getVersion();
            }
        }
        return groupId + ":" + model.getArtifactId() + ":" + model.getPackaging() + ":" + version;
    }
}
