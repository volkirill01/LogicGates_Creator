import editor.TestFieldsWindow;
import editor.node.Gates_NodeEditor;
import editor.utils.FileTypeFilter;
import editor.utils.FileUtil;
import editor.utils.ImFonts;
import imgui.*;
import imgui.app.Application;
import imgui.app.Configuration;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiConfigFlags;
import imgui.flag.ImGuiStyleVar;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;

import javax.swing.*;
import java.io.File;

public class Main extends Application {

    private static final Gates_NodeEditor editor = new Gates_NodeEditor();

    @Override
    protected void configure(final Configuration config) {
        getColorBg().set(0.0f, 0.0f, 0.0f, 1.0f);
        config.setFullScreen(true);
        config.setTitle("LogicGates Creator");
    }

    @Override
    protected void initImGui(final Configuration config) {
        super.initImGui(config);

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        final ImGuiIO io = ImGui.getIO();
        io.setIniFilename("imgui.ini");                         // We don't want to save .ini file
        io.addConfigFlags(ImGuiConfigFlags.NavEnableKeyboard);  // Enable Keyboard Controls
        io.addConfigFlags(ImGuiConfigFlags.DockingEnable);      // Enable Docking
        io.addConfigFlags(ImGuiConfigFlags.ViewportsEnable);    // Enable Multi-Viewport / Platform Windows
        io.setConfigViewportsNoTaskBarIcon(true);

        io.setFramerate(60);

        ImFonts.init(io);

        setTheme();
    }

    private void setTheme() {
        ImGui.pushStyleColor(ImGuiCol.TextDisabled, 0.650f, 0.650f, 0.650f, 1.0f);
        ImGui.pushStyleColor(ImGuiCol.WindowBg, 0.120f, 0.126f, 0.136f, 1.0f);
        ImGui.pushStyleColor(ImGuiCol.PopupBg, 0.120f, 0.126f, 0.136f, 1.0f);
        ImGui.pushStyleColor(ImGuiCol.Border, 1.0f, 1.0f, 1.0f, 0.125f);
        ImGui.pushStyleColor(ImGuiCol.FrameBg, 0.046f, 0.050f, 0.063f, 0.552f);
        ImGui.pushStyleColor(ImGuiCol.FrameBgHovered, 0.058f, 0.066f, 0.079f, 0.552f);
        ImGui.pushStyleColor(ImGuiCol.FrameBgActive, 0.077f, 0.085f, 0.099f, 0.552f);
        ImGui.pushStyleColor(ImGuiCol.TitleBg, 0.0f, 0.0f, 0.0f, 1.0f);
        ImGui.pushStyleColor(ImGuiCol.TitleBgActive, 0.0f, 0.0f, 0.0f, 1.0f);
        ImGui.pushStyleColor(ImGuiCol.TitleBgCollapsed, 0.0f, 0.0f, 0.0f, 1.0f);
        ImGui.pushStyleColor(ImGuiCol.MenuBarBg, 0.0f, 0.0f, 0.0f, 1.0f);
        ImGui.pushStyleColor(ImGuiCol.CheckMark, 0.681f, 0.681f, 0.681f, 1.0f);
        ImGui.pushStyleColor(ImGuiCol.SliderGrab, 0.311f, 0.330f, 0.335f, 1.0f);
        ImGui.pushStyleColor(ImGuiCol.SliderGrabActive, 0.388f, 0.413f, 0.419f, 1.0f);
        ImGui.pushStyleColor(ImGuiCol.Button, 0.311f, 0.330f, 0.335f, 1.0f);
        ImGui.pushStyleColor(ImGuiCol.ButtonHovered, 0.354f, 0.376f, 0.382f, 1.0f);
        ImGui.pushStyleColor(ImGuiCol.ButtonActive, 0.388f, 0.413f, 0.419f, 1.0f);
        ImGui.pushStyleColor(ImGuiCol.Header, 0.311f, 0.330f, 0.335f, 1.0f);
        ImGui.pushStyleColor(ImGuiCol.HeaderHovered, 0.354f, 0.376f, 0.382f, 1.0f);
        ImGui.pushStyleColor(ImGuiCol.HeaderActive, 0.388f, 0.413f, 0.419f, 1.0f);
        ImGui.pushStyleColor(ImGuiCol.Separator, 0.0f, 0.0f, 0.0f, 0.4f);
        ImGui.pushStyleColor(ImGuiCol.SeparatorHovered, 0.455f, 0.455f, 0.455f, 0.780f);
        ImGui.pushStyleColor(ImGuiCol.SeparatorActive, 0.455f, 0.455f, 0.455f, 0.780f);
        ImGui.pushStyleColor(ImGuiCol.ResizeGrip, 0.424f, 0.424f, 0.424f, 0.200f);
        ImGui.pushStyleColor(ImGuiCol.ResizeGripHovered, 0.518f, 0.518f, 0.518f, 0.300f);
        ImGui.pushStyleColor(ImGuiCol.ResizeGripActive, 0.576f, 0.576f, 0.576f, 0.360f);
        ImGui.pushStyleColor(ImGuiCol.Tab, 0.311f, 0.330f, 0.335f, 1.0f);
        ImGui.pushStyleColor(ImGuiCol.TabUnfocused, 0.211f, 0.230f, 0.235f, 1.0f);
        ImGui.pushStyleColor(ImGuiCol.TabUnfocusedActive, 0.211f, 0.230f, 0.235f, 1.0f);
        ImGui.pushStyleColor(ImGuiCol.TabHovered, 0.354f, 0.376f, 0.382f, 1.0f);
        ImGui.pushStyleColor(ImGuiCol.TabActive, 0.388f, 0.413f, 0.419f, 1.0f);
        ImGui.pushStyleColor(ImGuiCol.DockingPreview, 0.476f, 0.476f, 0.476f, 0.702f);

        ImGui.pushStyleVar(ImGuiStyleVar.FramePadding, 8.0f, 4.0f);
        ImGui.pushStyleVar(ImGuiStyleVar.ItemSpacing, 4.0f, 4.0f);
        ImGui.pushStyleVar(ImGuiStyleVar.ScrollbarSize, 12.0f);
        ImGui.pushStyleVar(ImGuiStyleVar.GrabMinSize, 10.0f);
        ImGui.pushStyleVar(ImGuiStyleVar.GrabRounding, 4.0f);
        ImGui.pushStyleVar(ImGuiStyleVar.ChildRounding, 4.0f);
        ImGui.pushStyleVar(ImGuiStyleVar.FrameRounding, 4.0f);
        ImGui.pushStyleVar(ImGuiStyleVar.PopupRounding, 4.0f);
        ImGui.getStyle().setWindowMenuButtonPosition(-1);
        ImGui.getStyle().setCircleTessellationMaxError(0.1f);
        ImGui.getStyle().setCurveTessellationTol(0.8f);
    }

    @Override
    public void process() {
        setupDockspace();

        TestFieldsWindow.imgui();
        ImGui.showDemoWindow();

        editor.imgui();
    }

    private void setupDockspace() {
        ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, 0.0f, 0.0f);
        int windowFlags = ImGuiWindowFlags.MenuBar | ImGuiWindowFlags.NoDocking;

        ImGuiViewport mainViewport = ImGui.getMainViewport();
        ImGui.setNextWindowPos(mainViewport.getWorkPosX(), mainViewport.getWorkPosY());
        ImGui.setNextWindowSize(mainViewport.getWorkSizeX(), mainViewport.getWorkSizeY());
        ImGui.setNextWindowViewport(mainViewport.getID());
        ImGui.setNextWindowSize(mainViewport.getWorkSizeX(), mainViewport.getWorkSizeY());
        ImGui.setNextWindowSize(getWidth(), getHeight());
        ImGui.pushStyleVar(ImGuiStyleVar.WindowRounding, 0.0f);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowBorderSize, 0.0f);
        windowFlags |= ImGuiWindowFlags.NoTitleBar | ImGuiWindowFlags.NoCollapse |
                ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoMove |
                ImGuiWindowFlags.NoBringToFrontOnFocus | ImGuiWindowFlags.NoNavFocus;

        ImGui.begin("Dockspace Demo", new ImBoolean(true), windowFlags);
        ImGui.popStyleVar(3);

        ImGui.dockSpace(ImGui.getID("Dockspace"));
        menuBar();
        ImGui.end();
    }

    public void menuBar() {
        ImGui.beginMenuBar();
        ImGui.pushStyleVar(ImGuiStyleVar.ItemSpacing, 15f, ImGui.getStyle().getItemSpacingY());

        if (ImGui.beginMenu("File")) {
            if (ImGui.menuItem("Save")) {
                Gates_NodeEditor.getCurrentGraph().save();
            }

            if (ImGui.menuItem("Open")) {
                File graph = FileUtil.openFile(FileTypeFilter.gateAndGraphFilter, true);
                if (graph != null)
                    Gates_NodeEditor.setCurrentGraph(Gates_NodeEditor.getCurrentGraph().load(graph.getPath()));
            }

            if (ImGui.menuItem("Reload")) {
                Gates_NodeEditor.setCurrentGraph(Gates_NodeEditor.getCurrentGraph().load(Gates_NodeEditor.getCurrentGraph().getFilepath()));
            }

            ImGui.endMenu();
        }

        if (ImGui.beginMenu("Help")) {
            ImGui.pushStyleColor(ImGuiCol.PopupBg, 0.22f, 0.22f, 0.22f, 1.0f);
            if (ImGui.beginMenu("Basics")) {
                ImGui.text("On/1/true");
                ImGui.getForegroundDrawList().addCircleFilled(
                        ImGui.getCursorScreenPosX() + 90.0f, // X Pos
                        ImGui.getCursorScreenPosY() - 10.0f, // Y Pos
                        9.0f, // Circle size
                        ImGui.getColorU32(0.936f, 0.401f, 0.069f, 1.0f), // Color
                        12); // Circle segments

                ImVec2 start = ImGui.getCursorPos();
                ImGui.sameLine();
                ImGui.setCursorPosX(ImGui.getCursorPosX() + 20.0f);

                ImGui.setCursorPos(start.x, start.y + ImGui.getStyle().getItemSpacingY());
                ImGui.text("Off/0/false");
                ImGui.getForegroundDrawList().addCircleFilled(
                        ImGui.getCursorScreenPosX() + 90.0f, // X Pos
                        ImGui.getCursorScreenPosY() - 10.0f, // Y Pos
                        9.0f, // Circle size
                        ImGui.getColorU32(0.080f, 0.083f, 0.103f, 1.0f), // Color
                        12); // Circle segments

                ImGui.endMenu();
            }
            ImGui.popStyleColor();

//            if (ImGui.menuItem("Creating Gates")) {
//            }

            ImGui.endMenu();
        }

//        if (ImGui.beginMenu("Edit")) {
//            if (ImGui.menuItem("Test1")) {
//            }
//
//            if (ImGui.menuItem("Test2")) {
//            }
//
//            ImGui.endMenu();
//        }

//        if (ImGui.beginMenu("Assets")) {
//            if (ImGui.menuItem("Test1")) {
//            }
//
//            if (ImGui.menuItem("Test2")) {
//            }
//
//            ImGui.endMenu();
//        }

//        if (ImGui.beginMenu("Window")) {
//            if (ImGui.menuItem("Test1")) {
//            }
//
//            if (ImGui.menuItem("Test2")) {
//            }
//
//            ImGui.endMenu();
//        }

        ImGui.popStyleVar();
        ImGui.endMenuBar();
    }

    private int getWidth() { return (int) ImGui.getIO().getDisplaySizeX(); }

    private int getHeight() { return (int) ImGui.getIO().getDisplaySizeY(); }

    public static void main(final String[] args) {
        launch(new Main());
        System.exit(0);
    }
}
