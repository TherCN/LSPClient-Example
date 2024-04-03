package com.example;
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme;

public class AIDEColorSchemes {
    
    public static final String TAG = "AIDEColorSchemes";
    public static class Light extends EditorColorScheme {

        @Override
        public void applyDefault() {
            super.applyDefault();
            setColor(ANNOTATION, 0xFF0096FF); // 注解颜色
            setColor(FUNCTION_NAME, 0xff000000); // 函数名颜色
            setColor(IDENTIFIER_NAME, 0xFF0096FF); // 标识符名称颜色
            setColor(IDENTIFIER_VAR, 0xff24292e); // 标识符变量颜色
            setColor(LITERAL, 0xFFE91E63); // 字面量颜色
            setColor(OPERATOR, 0xff005cc5); // 操作符颜色
            setColor(COMMENT, 0xff6a737d); // 注释颜色
            setColor(KEYWORD, 0xff0096ff); // 关键字颜色
            setColor(WHOLE_BACKGROUND, 0xffffffff); // 整体背景颜色
            setColor(TEXT_NORMAL, 0xff000000); // 正常文本颜色
            setColor(LINE_NUMBER_BACKGROUND, 0xffffffff); // 行号背景颜色
            setColor(LINE_NUMBER, 0xffbec0c1); // 行号颜色
            setColor(LINE_NUMBER_CURRENT, 0xffbec0c1); // 当前行号颜色
            setColor(SELECTION_INSERT, 0xffc7edcc); // 选择插入点颜色
            setColor(SELECTION_HANDLE, 0xffc7edcc); // 选择句柄颜色
        }
	}
   
    public static class Dark extends EditorColorScheme {

        @Override
        public void applyDefault() {
            super.applyDefault();
            setColor(ANNOTATION, 0xFF0096FF); // 注解颜色
            setColor(FUNCTION_NAME, 0xff4fc3f7); // 函数名颜色
            setColor(IDENTIFIER_NAME, 0xff4fc3f7); // 标识符名称颜色
            setColor(IDENTIFIER_VAR, 0xfff0be4b); // 标识符变量颜色
            setColor(LITERAL, 0xFF8bc34a); // 字面量颜色
            setColor(OPERATOR, 0xfff0be4b); // 操作符颜色
            setColor(COMMENT, 0xffbdbdbd); // 注释颜色
            setColor(KEYWORD, 0xffff6060); // 关键字颜色
            setColor(WHOLE_BACKGROUND, 0xff212121); // 整体背景颜色
            setColor(TEXT_NORMAL, 0xffffffff); // 正常文本颜色
            setColor(LINE_NUMBER_BACKGROUND, WHOLE_BACKGROUND); // 行号背景颜色
            setColor(LINE_NUMBER, 0xff0096ff); // 行号颜色
            setColor(LINE_NUMBER_CURRENT, 0xff0096ff); // 当前行号颜色
            setColor(SELECTION_INSERT, 0xffffffff); // 选择插入点颜色
            setColor(SELECTION_HANDLE, 0xff0096ff); // 选择句柄颜色
        }
	}
    
    public static class Green extends EditorColorScheme {
        int a = 0;
        String b = "你好";
        public void c(String b) {
           Green g =  new Green();
        }
        //你好
    }
}
