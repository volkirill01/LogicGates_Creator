package editor;

import imgui.ImGui;
import imgui.ImVec2;
import imgui.ImVec4;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiStyleVar;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class TestFieldsWindow {

    public static float[] getFloats = { 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f };
    public static int[] getInts = { 0, 0, 0, 0 };
    public static String[] getStrings = { "", "", "" };
    public static boolean[] getBooleans = { false, false, false, false, false, false };
    public static Vector2f[] getVectors2f = { new Vector2f(0.0f), new Vector2f(0.0f), new Vector2f(0.0f) };
    public static Vector3f[] getVectors3f = { new Vector3f(0.0f), new Vector3f(0.0f), new Vector3f(0.0f) };
    public static Vector4f[] getColors = { new Vector4f(1.0f), new Vector4f(1.0f) };


    public static void imgui() {
        ImGui.begin(" Test Fields ");

        for (int i = 0; i < getFloats.length; i++) {
            ImGui.pushID(i + "Float");

            ImGui.columns(2, "", false);
            ImGui.setColumnWidth(0, 150.0f);
            ImGui.setCursorPosY(ImGui.getCursorPosY() + 4.0f);
            ImGui.text("\t" + "Float (" + i + ")");
            ImGui.nextColumn();

            float[] valArr = { getFloats[i] };

            ImGui.setNextItemWidth(ImGui.getContentRegionAvailX());
            ImGui.dragFloat("##dragFloat" + i, valArr);

            ImGui.columns(1);
            ImGui.popID();

            getFloats[i] = valArr[0];
        }
//
//        for (int i = 0; i < getInts.length; i++)
//            getInts[i] = EditorImGui.field_Int_WithButtons("Test Int (" + i + ")", getInts[i]);
////                        EditorImGui.field_Int("Tets int", 0);
//
//        for (int i = 0; i < getStrings.length; i++)
//            getStrings[i] = EditorImGui.field_Text("Test String (" + i + ")", getStrings[i], "Test");
//
//        for (int i = 0; i < getBooleans.length; i++)
//            getBooleans[i] = EditorImGui.field_Boolean("Test Boolean (" + i + ")", getBooleans[i]);
//
//        for (int i = 0; i < getVectors2f.length; i++)
//            getVectors2f[i] = EditorImGui.field_Vector2f("Test Vector2 (" + i + ")", getVectors2f[i]);
//
//        for (int i = 0; i < getVectors3f.length; i++)
//            getVectors3f[i] = EditorImGui.field_Vector3f("Test Vector3 (" + i + ")", getVectors3f[i]);
//
        for (int i = 0; i < getColors.length; i++) {
            ImGui.pushID("ColorPicker3-" + i);

            ImGui.columns(2, "", false);
            ImGui.setColumnWidth(0, 150.0f);
            ImGui.setCursorPosY(ImGui.getCursorPosY() + 4.0f);
            ImGui.text("\t" + "Color (" + i + ")");
            ImGui.nextColumn();

            ImGui.setNextItemWidth(ImGui.getContentRegionAvailX());
            float[] imColor = { getColors[i].x / 255, getColors[i].y / 255, getColors[i].z / 255, getColors[i].w / 255};
            if (ImGui.colorEdit4("##colorPicker" + i, imColor))
                getColors[i].set(imColor[0] * 255, imColor[1] * 255, imColor[2] * 255, imColor[3] * 255);

            ImGui.columns(1);
            ImGui.popID();
        }

        ImGui.end();
    }
}

