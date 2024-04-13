package com.example;
import android.os.Bundle;
import android.util.Log;
import com.google.gson.Gson;
import io.github.rosemoe.sora.lang.completion.CompletionHelper;
import io.github.rosemoe.sora.lang.completion.CompletionItemKind;
import io.github.rosemoe.sora.lang.completion.CompletionPublisher;
import io.github.rosemoe.sora.lang.completion.IdentifierAutoComplete;
import io.github.rosemoe.sora.lang.completion.SimpleSnippetCompletionItem;
import io.github.rosemoe.sora.lang.completion.SnippetDescription;
import io.github.rosemoe.sora.lang.completion.snippet.CodeSnippet;
import io.github.rosemoe.sora.lang.completion.snippet.parser.CodeSnippetParser;
import io.github.rosemoe.sora.langs.java.JavaIncrementalAnalyzeManager;
import io.github.rosemoe.sora.langs.java.JavaLanguage;
import io.github.rosemoe.sora.text.CharPosition;
import io.github.rosemoe.sora.text.ContentReference;
import io.github.rosemoe.sora.util.MyCharacter;
import java.lang.reflect.Field;
import json.JSONArray;
import json.JSONObject;
import org.eclipse.lsp4j.CompletionItem;
import io.github.rosemoe.sora.lang.completion.CompletionCancelledException;

public class JDTLSLanguage extends JavaLanguage {

    MainActivity activity;
	JSONObject jsonObject;
    Object lock = new Object();
    public JDTLSLanguage() {
		super();
	}

    public JDTLSLanguage(MainActivity activity) {
        super();
        this.activity = activity;
    }
	public void setJSONData(JSONObject jsonObject) {
		this.jsonObject = jsonObject;

	}

	

    public IdentifierAutoComplete.SyncIdentifiers getIdentifiers() throws Exception {
        Class<?> clazz = getClass().getSuperclass();
        Field manager = clazz.getDeclaredField("manager");
        manager.setAccessible(true);
        JavaIncrementalAnalyzeManager jiam = ((JavaIncrementalAnalyzeManager)manager.get(this));
        Field identifiers = jiam.getClass().getDeclaredField("identifiers");
        identifiers.setAccessible(true);
        return (IdentifierAutoComplete.SyncIdentifiers)identifiers.get(jiam);
    }

    public IdentifierAutoComplete getAutoComplete() throws Exception {
        Class<?> clazz = getClass().getSuperclass();
        Field autoComplete = clazz.getDeclaredField("autoComplete");
        autoComplete.setAccessible(true); 
        return (IdentifierAutoComplete)autoComplete.get(this);
    }

	@Override
	public void requireAutoComplete(ContentReference content, CharPosition position, CompletionPublisher publisher, Bundle extraArguments) {
        String prefix = "";
        try {
            //Log.e("", getIdentifiers().toString());
            prefix = CompletionHelper.computePrefix(content, position, new CompletionHelper.PrefixChecker() {
                    public boolean check(char c) {
                        return MyCharacter.isJavaIdentifierPart(c);
                    }
                });
            activity.requestCompletion();

            if (jsonObject == null) {
                return;
            }
            //List<CompletionItem> completionItems = new ArrayList<>();
            // 获取title
            JSONArray items = jsonObject.getJSONObject("result")
                .getJSONArray("items");
            for (int i = 0; i < items.length(); i++) {
                /*
                JSONObject item = items.getJSONObject(i);
                String insertText = item.getJSONObject("textEdit").getString("newText");
                String desc = "";
                if (item.has("detail")) {
                    desc = item.getString("detail");
                } else {
                    desc = "";
                }
                Log.e("添加补全item", String.format("当前:%d/总共%d，文本:%s", i + 1, items.length(), insertText));
                CodeSnippet cs = CodeSnippetParser.parse(insertText);
                SimpleSnippetCompletionItem simpleItem =new SimpleSnippetCompletionItem(insertText, desc, new SnippetDescription(prefix.length(), cs, true));
                simpleItem.kind(Enum.valueOf(CompletionItemKind.class, org.eclipse.lsp4j.CompletionItemKind.forValue(item.getInt("kind")).toString()));
                publisher.addItem(simpleItem);
                Log.e("kind",Enum.valueOf(CompletionItemKind.class, org.eclipse.lsp4j.CompletionItemKind.forValue(item.getInt("kind")).toString()).toString());
                */
                CompletionItem item = new Gson().fromJson(items.getJSONObject(i).toString(),org.eclipse.lsp4j.CompletionItem.class);
                publisher.addItem(new LspCompletionItem(item, prefix.length()));
            
            }
        } catch (Exception e) {
            if (!e.getClass().equals(CompletionCancelledException.class)) Log.wtf("",e);
            super.requireAutoComplete(content, position, publisher, extraArguments);
        }
    }

}
