package de.isuewo;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

public class Data {
    public String installerVersion;
    public String shaderpackName;
    public String shaderpackFileName;
    public String shaderpackVersion;
    public String shaderpackMD5;
    public String shaderpackURL;
    public String editedShaderpackName;
    public String editedShaderpackFileName;
    public String editedShaderpackVersion;
    public String editedShaderpackAuthor;
    public String dataURL;

    public String getInstaller() {
        return editedShaderpackFileName.replace("_", "-") + "Installer-" + installerVersion + ".jar";
    }

    public URL getInstallerURL() throws MalformedURLException {
        return new URL(dataURL + "Installer/releases/download/" + installerVersion + "/" + getInstaller());
    }

    public File getShaderpackFile(File dir) {
        return new File(dir + File.separator + shaderpackFileName + shaderpackVersion + ".zip");
    }

    public File getEditedShaderpackFile(File dir) {
        return new File(dir + File.separator + getEditedShaderpackPatch().replace(".patch", ""));
    }

    public String getEditedShaderpackNameVersion() {
        return editedShaderpackName + " " + editedShaderpackVersion;
    }

    public String getEditedShaderpackPatch() {
        return editedShaderpackFileName + shaderpackVersion + "-" + editedShaderpackVersion + ".patch";
    }

    public URL getEditedShaderpackPatchURL() throws MalformedURLException {
        return new URL(dataURL + "Patch/releases/download/" + editedShaderpackVersion + "/" + getEditedShaderpackPatch());
    }
}