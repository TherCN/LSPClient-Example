package com.example;

//
// Decompiled by Jadx - 835ms
//

import android.graphics.drawable.Drawable;
import io.github.rosemoe.sora.lang.completion.CompletionItem;
import io.github.rosemoe.sora.lang.completion.CompletionItemKind;
import io.github.rosemoe.sora.lang.completion.SimpleCompletionIconDrawer;
import io.github.rosemoe.sora.lang.completion.snippet.CodeSnippet;
import io.github.rosemoe.sora.lang.completion.snippet.parser.CodeSnippetParser;
import io.github.rosemoe.sora.lsp.utils.LspUtilsKt;
import io.github.rosemoe.sora.text.CharPosition;
import io.github.rosemoe.sora.text.Content;
import io.github.rosemoe.sora.widget.CodeEditor;
import io.github.rosemoe.sora.widget.snippet.SnippetController;
import java.util.Arrays;
import java.util.List;
import kotlin.jvm.internal.Intrinsics;
import org.eclipse.lsp4j.CompletionItemLabelDetails;
import org.eclipse.lsp4j.InsertTextFormat;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;

//@Metadata(d1 = {"\u00008\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\u0018\u00002\u00020\u0001B\u001d\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007¢\u0006\u0002\u0010\bJ \u0010\t\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\u0010H\u0016J(\u0010\t\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\u000e2\u0006\u0010\u0011\u001a\u00020\u00072\u0006\u0010\u0012\u001a\u00020\u0007H\u0016R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004¢\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004¢\u0006\u0002\n\u0000¨\u0006\u0013"}, d2 = {"Lio/github/rosemoe/sora/lsp/editor/completion/LspCompletionItem;", "Lio/github/rosemoe/sora/lang/completion/CompletionItem;", "completionItem", "Lorg/eclipse/lsp4j/CompletionItem;", "eventManager", "Lio/github/rosemoe/sora/lsp/editor/LspEventManager;", "prefixLength", "", "(Lorg/eclipse/lsp4j/CompletionItem;Lio/github/rosemoe/sora/lsp/editor/LspEventManager;I)V", "performCompletion", "", "editor", "Lio/github/rosemoe/sora/widget/CodeEditor;", "text", "Lio/github/rosemoe/sora/text/Content;", "position", "Lio/github/rosemoe/sora/text/CharPosition;", "line", "column", "editor-lsp_debug"}, k = 1, mv = {1, 9, 0}, xi = 48)
public final class LspCompletionItem extends CompletionItem {
    private final org.eclipse.lsp4j.CompletionItem completionItem;
    private Content content;
    //private final LspEventManager eventManager;

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public LspCompletionItem(org.eclipse.lsp4j.CompletionItem completionItem,int prefixLength) {
        super(completionItem.getLabel(), completionItem.getDetail());
        Intrinsics.checkNotNullParameter(completionItem, "completionItem");
        //Intrinsics.checkNotNullParameter(eventManager, "eventManager");
        this.completionItem = completionItem;
        this.content = MainActivity.editor.getText();
        this.prefixLength = prefixLength;
        this.kind = completionItem.getKind() == null ? CompletionItemKind.Text : CompletionItemKind.valueOf(completionItem.getKind().name());
        this.sortText = completionItem.getSortText();
        CompletionItemLabelDetails labelDetails = completionItem.getLabelDetails();
        if (labelDetails != null && labelDetails.getDescription() != null) {
            this.desc = labelDetails.getDescription();
        }
        CompletionItemKind completionItemKind = this.kind;
        completionItemKind = completionItemKind == null ? CompletionItemKind.Text : completionItemKind;
        Intrinsics.checkNotNull(completionItemKind);
        this.icon = draw(completionItemKind, false, 2, (Object) null);
    }

    /* JADX WARN: Multi-variable type inference failed */
    public void performCompletion(CodeEditor editor, Content text, CharPosition position) {
        Intrinsics.checkNotNullParameter(editor, "editor");
        Intrinsics.checkNotNullParameter(text, "text");
        Intrinsics.checkNotNullParameter(position, "position");
        TextEdit textEdit = new TextEdit();
        textEdit.setRange(LspUtilsKt.createRange(LspUtilsKt.createPosition(position.line, position.column - this.prefixLength), LspUtilsKt.asLspPosition(position)));
        if (this.completionItem.getInsertText() != null) {
            textEdit.setNewText(this.completionItem.getInsertText());
        }
        if (this.completionItem.getTextEdit() != null && this.completionItem.getTextEdit().isLeft()) {
            TextEdit left = this.completionItem.getTextEdit().getLeft();
            Intrinsics.checkNotNullExpressionValue(left, "getLeft(...)");
            textEdit = left;
        }
        if (textEdit.getNewText() == null && this.completionItem.getLabel() != null) {
            textEdit.setNewText(this.completionItem.getLabel());
        }
        Position start = textEdit.getRange().getStart();
        Position end = textEdit.getRange().getEnd();
        if (start.getLine() > end.getLine() || (start.getLine() == end.getLine() && start.getCharacter() > end.getCharacter())) {
            textEdit.getRange().setEnd(start);
            textEdit.getRange().setStart(end);
        }
        Position documentEnd = LspUtilsKt.createPosition(text.getLineCount() - 1, text.getColumnCount(Math.max(0, position.line - 1)));
        Position textEditEnd = textEdit.getRange().getEnd();
        if (documentEnd.getLine() < textEditEnd.getLine() || (documentEnd.getLine() == textEditEnd.getLine() && documentEnd.getCharacter() < textEditEnd.getCharacter())) {
            textEdit.getRange().setEnd(documentEnd);
        }
        
        if (this.completionItem.getInsertTextFormat() == InsertTextFormat.Snippet) {
            CodeSnippet codeSnippet = CodeSnippetParser.parse(textEdit.getNewText());
            int startIndex = text.getCharIndex(textEdit.getRange().getStart().getLine(), textEdit.getRange().getStart().getCharacter());
            int endIndex = text.getCharIndex(textEdit.getRange().getEnd().getLine(), textEdit.getRange().getEnd().getCharacter());
            String selectedText = text.subSequence(startIndex, endIndex).toString();
            text.delete(startIndex, endIndex);
            SnippetController snippetController = editor.getSnippetController();
            Intrinsics.checkNotNull(codeSnippet);
            snippetController.startSnippet(startIndex, codeSnippet, selectedText);
        } else {
            applyEdits(Arrays.asList(textEdit));
        }
        
        if (this.completionItem.getAdditionalTextEdits() != null) {
            applyEdits(this.completionItem.getAdditionalTextEdits());
        }
    }
    
    public void applyEdits(List<TextEdit> edits) {
        for (TextEdit textEdit : edits) {
            Range range = textEdit.getRange();
            String text = textEdit.getNewText();
            int startIndex = content.getCharIndex(range.getStart().getLine(), range.getStart().getCharacter());
            int endIndex = content.getCharIndex(range.getEnd().getLine(), range.getEnd().getCharacter());
            if (endIndex < startIndex) {
                int diff = startIndex - endIndex;
                endIndex = startIndex;
                startIndex = endIndex - diff;
            }
            content.replace(startIndex, endIndex, text);
        }
    }

    public void performCompletion(CodeEditor editor, Content text, int line, int column) {
        Intrinsics.checkNotNullParameter(editor, "editor");
        Intrinsics.checkNotNullParameter(text, "text");
    }
    
    public static int coerceAtLeast(int value, int atLeast) {
        return value >= atLeast ? value : atLeast;
    }
    public static Drawable draw(CompletionItemKind completionItemKind, boolean z, int i, Object obj) {
        if ((i & 2) != 0) {
            z = true;
        }
        return SimpleCompletionIconDrawer.draw(completionItemKind, z);
    }
}

