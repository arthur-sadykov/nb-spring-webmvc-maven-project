/*
 * Copyright (c) 2020 Arthur Sadykov.
 */
package nb.spring.webmvc.maven.project;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author Arthur Sadykov
 */
@Messages("SpringMVCProject_displayName=Spring Web MVC Project")
public class SpringMVCProjectWizardIterator implements WizardDescriptor./*
         * Progress
         */InstantiatingIterator {

    public static final String PROP_PROJECT_NAME = "projectName";
    public static final String PROP_PACKAGE = "package";
    private static final String PROP_VERSION = "version";
    private static final String PROP_GROUP_ID = "groupId";
    private static final String PROP_ARTIFACT_ID = "artifactId";
    private static final String PROP_NAME = "name";
    private static final String PROP_PROJECT_DIR = "projdir";
    private int index;
    private WizardDescriptor.Panel<?>[] panels;
    private WizardDescriptor wizard;
    private FileObject projectDirectory;

    public SpringMVCProjectWizardIterator() {
    }

    public static SpringMVCProjectWizardIterator createIterator() {
        return new SpringMVCProjectWizardIterator();
    }

    private WizardDescriptor.Panel<?>[] createPanels() {
        return new WizardDescriptor.Panel<?>[]{new SpringMVCProjectWizardPanel(),};
    }

    private String[] createSteps() {
        return new String[]{
            NbBundle.getMessage(SpringMVCProjectWizardIterator.class, "LBL_CreateProjectStep")
        };
    }

    @Override
    public Set<FileObject> instantiate(/*
             * ProgressHandle handle
             */) throws IOException {
        Set<FileObject> resultSet = new LinkedHashSet<>();
        File projectDir = FileUtil.normalizeFile((File) wizard.getProperty("projdir"));
        projectDir.mkdirs();
        projectDirectory = FileUtil.toFileObject(projectDir);
        String packageName = (String) wizard.getProperty(PROP_PACKAGE);
        String artifactId = (String) wizard.getProperty(PROP_ARTIFACT_ID);
        String groupId = (String) wizard.getProperty(PROP_GROUP_ID);
        String version = (String) wizard.getProperty(PROP_VERSION);
        Map<String, String> parameters = new HashMap<>();
        parameters.put(PROP_PROJECT_NAME, (String) wizard.getProperty(PROP_NAME));
        parameters.put(PROP_PACKAGE, packageName + ".config");
        parameters.put(PROP_ARTIFACT_ID, artifactId);
        parameters.put(PROP_GROUP_ID, groupId);
        parameters.put(PROP_VERSION, version);
        createFileFromTemplate("Templates/Project/Maven2/DispatcherServletInitializer.java",
                "src/main/java/" + packageNameToPath(packageName) + "/config", null, parameters);
        createFileFromTemplate("Templates/Project/Maven2/WebApplicationContextConfig.java",
                "src/main/java/" + packageNameToPath(packageName) + "/config", null, parameters);
        parameters.put("package", packageName + ".controller");
        createFileFromTemplate("Templates/Project/Maven2/HomeController.java",
                "src/main/java/" + packageNameToPath(packageName) + "/controller", null, parameters);
        createFileFromTemplate("Templates/Project/Maven2/index.jsp", "src/main/webapp/WEB-INF/jsp", null, parameters);
        createFileFromTemplate("Templates/Project/Maven2/context.xml", "src/main/webapp/META-INF", null, parameters);
        createFileFromTemplate("Templates/Project/Maven2/nb-configuration.xml", null, null, parameters);
        createFileFromTemplate("Templates/Project/Maven2/pom.xml", null, null, parameters);
        resultSet.add(projectDirectory);
        return resultSet;
    }

    private String packageNameToPath(String packageName) {
        return packageName.replace('.', '/');
    }

    private void createFileFromTemplate(String templateLocation, String folder, String name,
            Map<String, String> parameters) throws IOException {
        FileObject templateFile = FileUtil.getConfigFile(templateLocation);
        if (templateFile != null) {
            DataObject dataObject = DataObject.find(templateFile);
            FileObject configFolder;
            if (folder != null) {
                configFolder = FileUtil.createFolder(projectDirectory, folder);
            } else {
                configFolder = projectDirectory;
            }
            DataFolder dataFolder = DataFolder.findFolder(configFolder);
            dataObject.createFromTemplate(dataFolder, name, parameters);
        }
    }

    @Override
    public void initialize(WizardDescriptor wizard) {
        this.wizard = wizard;
        index = 0;
        panels = createPanels();
        // Make sure list of steps is accurate.
        String[] steps = createSteps();
        for (int i = 0; i < panels.length; i++) {
            Component c = panels[i].getComponent();
            if (steps[i] == null) {
                // Default step name to component name of panel.
                // Mainly useful for getting the name of the target
                // chooser to appear in the list of steps.
                steps[i] = c.getName();
            }
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent) c;
                // Step #.
                // TODO if using org.openide.dialogs >= 7.8, can use WizardDescriptor.PROP_*:
                jc.putClientProperty("WizardPanel_contentSelectedIndex", i);
                // Step name (actually the whole list for reference).
                jc.putClientProperty("WizardPanel_contentData", steps);
            }
        }
    }

    @Override
    public void uninitialize(WizardDescriptor wiz) {
        this.wizard.putProperty(PROP_PROJECT_DIR, null);
        this.wizard.putProperty(PROP_NAME, null);
        this.wizard.putProperty(PROP_PACKAGE, null);
        this.wizard.putProperty(PROP_ARTIFACT_ID, null);
        this.wizard.putProperty(PROP_GROUP_ID, null);
        this.wizard.putProperty(PROP_VERSION, null);
        this.wizard = null;
        panels = null;
    }

    @Override
    public String name() {
        return MessageFormat.format("{0} of {1}", new Object[]{index + 1, panels.length});
    }

    @Override
    public boolean hasNext() {
        return index < panels.length - 1;
    }

    @Override
    public boolean hasPrevious() {
        return index > 0;
    }

    @Override
    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        index++;
    }

    @Override
    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }

    @Override
    public WizardDescriptor.Panel<?> current() {
        return panels[index];
    }

    // If nothing unusual changes in the middle of the wizard, simply:
    @Override
    public void addChangeListener(ChangeListener l) {
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
    }
}
