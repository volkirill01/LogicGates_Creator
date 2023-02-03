import editor.Gates_NodeEditor;
import editor.Graph;
import imgui.*;
import imgui.app.Application;
import imgui.app.Configuration;
import imgui.flag.ImGuiConfigFlags;
import imgui.flag.ImGuiStyleVar;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;

public class Main extends Application {

    private static final Gates_NodeEditor editor = new Gates_NodeEditor();

    @Override
    protected void configure(final Configuration config) {
        getColorBg().set(0.0f, 0.0f, 0.0f, 1.0f);
        config.setTitle("LogicGates Creator");
    }

    @Override
    protected void initImGui(final Configuration config) {
        super.initImGui(config);

        final ImGuiIO io = ImGui.getIO();
        io.setIniFilename("imgui.ini");                         // We don't want to save .ini file
        io.addConfigFlags(ImGuiConfigFlags.NavEnableKeyboard);  // Enable Keyboard Controls
        io.addConfigFlags(ImGuiConfigFlags.DockingEnable);      // Enable Docking
        io.addConfigFlags(ImGuiConfigFlags.ViewportsEnable);    // Enable Multi-Viewport / Platform Windows
        io.setConfigViewportsNoTaskBarIcon(true);

        initFonts(io);
    }

    private void initFonts(final ImGuiIO io) {
        io.getFonts().addFontDefault(); // Add default font for latin glyphs

        // You can use the ImFontGlyphRangesBuilder helper to create glyph ranges based on text input.
        // For example: for a game where your script is known, if you can feed your entire script to it (using addText) and only build the characters the game needs.
        // Here we are using it just to combine all required glyphs in one place
        final ImFontGlyphRangesBuilder rangesBuilder = new ImFontGlyphRangesBuilder(); // Glyphs ranges provide
        rangesBuilder.addRanges(io.getFonts().getGlyphRangesDefault());
        rangesBuilder.addRanges(io.getFonts().getGlyphRangesCyrillic());
        rangesBuilder.addRanges(io.getFonts().getGlyphRangesJapanese());

        // Font config for additional fonts
        // This is a natively allocated struct so don't forget to call destroy after atlas is built
        final ImFontConfig fontConfig = new ImFontConfig();
        fontConfig.setMergeMode(true);  // Enable merge mode to merge cyrillic, japanese and icons with default font

        final short[] glyphRanges = rangesBuilder.buildRanges();
        io.getFonts().addFontFromFileTTF("engineFiles/fonts/openSans/OpenSans-Medium.ttf", 14, fontConfig, glyphRanges);
        io.getFonts().build();

        fontConfig.destroy();
    }

    @Override
    public void process() {
        setupDockspace();

        if (ImGui.begin("Editor"))
            editor.imgui();
//            editor.ExampleImNodes.imgui(GRAPH);

        ImGui.end();
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
            if (ImGui.menuItem("Test1")) {
            }

            if (ImGui.menuItem("Test2")) {
            }

            ImGui.endMenu();
        }

        if (ImGui.beginMenu("Edit")) {
            if (ImGui.menuItem("Test1")) {
            }

            if (ImGui.menuItem("Test2")) {
            }

            ImGui.endMenu();
        }

        if (ImGui.beginMenu("Assets")) {
            if (ImGui.menuItem("Test1")) {
            }

            if (ImGui.menuItem("Test2")) {
            }

            ImGui.endMenu();
        }

        if (ImGui.beginMenu("Window")) {
            if (ImGui.menuItem("Test1")) {
            }

            if (ImGui.menuItem("Test2")) {
            }

            ImGui.endMenu();
        }

        if (ImGui.beginMenu("Help")) {
            if (ImGui.menuItem("Test1")) {
            }

            if (ImGui.menuItem("Test2")) {
            }

            ImGui.endMenu();
        }

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
