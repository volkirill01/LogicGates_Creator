package editor.utils;

import javax.swing.*;
import java.io.File;
import java.util.List;

public class FileUtil {

    private static String targetPath = "";
    private static JFileChooser chooser;

    public static File openFile(FileTypeFilter filter, boolean setFilterByDefault) {
        chooser = new JFileChooser();
        if (setFilterByDefault)
            chooser.setFileFilter(filter);
        chooser.addChoosableFileFilter(filter);

        if (!targetPath.equals(""))
            chooser.setSelectedFile(new File(targetPath));

        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            targetPath = chooser.getSelectedFile().getAbsolutePath();
            return chooser.getSelectedFile();
        }

        return null;
    }

    public static File openFile(List<FileTypeFilter> filters, FileTypeFilter defaultFilter) {
        chooser = new JFileChooser();
        chooser.setFileFilter(defaultFilter);
        for (FileTypeFilter filter : filters)
            chooser.addChoosableFileFilter(filter);

        if (!targetPath.equals(""))
            chooser.setSelectedFile(new File(targetPath));

        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            targetPath = chooser.getSelectedFile().getAbsolutePath();
            return chooser.getSelectedFile();
        }

        return null;
    }
}
