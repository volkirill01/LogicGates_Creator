package editor.utils;

import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileTypeFilter extends FileFilter {

    private String extension;
    private String extension2;
    private String description;

    public static FileTypeFilter gateFilter = new FileTypeFilter(".gate", "Gate File\"");
    public static FileTypeFilter graphFilter = new FileTypeFilter(".graph", "Graph File");

    public static FileTypeFilter gateAndGraphFilter = new FileTypeFilter(".gate", ".graph", "Gates And Graph Files");

    public FileTypeFilter(String extension, String description) {
        this.extension = extension;
        this.description = description;
    }

    public FileTypeFilter(String extension, String extension2, String description) {
        this.extension = extension;
        this.extension2 = extension2;
        this.description = description;
    }

    public boolean accept(File file) {
        if (file.isDirectory())
            return true;

        return file.getName().endsWith(extension) || file.getName().endsWith(extension2);
    }

    public String getDescription() { return description + String.format(" (*%s, *%s)", extension, extension2); }
}