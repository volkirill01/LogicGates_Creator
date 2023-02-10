package editor.utils;

import imgui.*;

public class ImFonts {

    private static float fontSize = 14.0f;

    public static ImFont regular100;
    public static ImFont regular150;

    public static ImFont bold100;
    public static ImFont bold150;
    public static ImFont bold200;

    public static ImFont semiBold100;

    public static ImFont light100;

    public static void init(final ImGuiIO io) {
        final ImFontAtlas fontAtlas = io.getFonts();
        final ImFontConfig fontConfig = new ImFontConfig(); // Natively allocated object, should be explicitly destroyed

        // Glyphs could be added per-font as well as per config used globally like here
        fontConfig.setGlyphRanges(fontAtlas.getGlyphRangesDefault());

        ImFontConfig defaultFontConfig = new ImFontConfig();
        defaultFontConfig.setPixelSnapH(true);
        defaultFontConfig.setGlyphRanges(fontAtlas.getGlyphRangesDefault());

        // Fonts merge example
        fontConfig.setPixelSnapH(true);
        regular100 = fontAtlas.addFontFromFileTTF("engineFiles/fonts/cascadia/Cascadia Mono Regular 400.otf", fontSize, defaultFontConfig);
        fontConfig.setMergeMode(true);

        regular150 = fontAtlas.addFontFromFileTTF("engineFiles/fonts/cascadia/Cascadia Mono Regular 400.otf", fontSize * 1.5f, defaultFontConfig);

        bold100 = fontAtlas.addFontFromFileTTF("engineFiles/fonts/cascadia/Cascadia Mono Bold 700.otf", fontSize, defaultFontConfig);
        bold150 = fontAtlas.addFontFromFileTTF("engineFiles/fonts/cascadia/Cascadia Mono Bold 700.otf", fontSize * 1.5f, defaultFontConfig);
        bold200 = fontAtlas.addFontFromFileTTF("engineFiles/fonts/cascadia/Cascadia Mono Bold 700.otf", fontSize * 2.0f, defaultFontConfig);

        semiBold100 = fontAtlas.addFontFromFileTTF("engineFiles/fonts/cascadia/Cascadia Mono SemiBold 600.otf", fontSize, defaultFontConfig);

        light100 = fontAtlas.addFontFromFileTTF("engineFiles/fonts/cascadia/Cascadia Mono Light 300.otf", fontSize, defaultFontConfig);

        fontAtlas.build();
        fontConfig.destroy(); // After all fonts were added we don't need this config more


//        io.getFonts().addFontDefault(); // Add default font for latin glyphs
//
//        // You can use the ImFontGlyphRangesBuilder helper to create glyph ranges based on text input.
//        // For example: for a game where your script is known, if you can feed your entire script to it (using addText) and only build the characters the game needs.
//        // Here we are using it just to combine all required glyphs in one place
//        final ImFontGlyphRangesBuilder rangesBuilder = new ImFontGlyphRangesBuilder(); // Glyphs ranges provide
//        rangesBuilder.addRanges(io.getFonts().getGlyphRangesDefault());
//        rangesBuilder.addRanges(io.getFonts().getGlyphRangesCyrillic());
//        rangesBuilder.addRanges(io.getFonts().getGlyphRangesJapanese());
//
//        // Font config for additional fonts
//        // This is a natively allocated struct so don't forget to call destroy after atlas is built
//        final ImFontConfig fontConfig = new ImFontConfig();
//        fontConfig.setMergeMode(true);  // Enable merge mode to merge cyrillic, japanese and icons with default font
//
//        final short[] glyphRanges = rangesBuilder.buildRanges();
//
//        regular100 = io.getFonts().addFontFromFileTTF("engineFiles/fonts/openSans/OpenSans-Medium.ttf", fontSize, fontConfig, glyphRanges);
//        regular200 = io.getFonts().addFontFromFileTTF("engineFiles/fonts/openSans/OpenSans-Medium.ttf", fontSize * 2.0f, fontConfig, glyphRanges);
//        regular500 = io.getFonts().addFontFromFileTTF("engineFiles/fonts/openSans/OpenSans-Medium.ttf", fontSize * 5.0f, fontConfig, glyphRanges);
//        io.getFonts().build();
//
//        fontConfig.destroy();
    }
}
