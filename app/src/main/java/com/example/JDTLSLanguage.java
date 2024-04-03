package com.example;
import android.os.Bundle;
import android.util.Log;
import io.github.rosemoe.sora.lang.completion.CompletionHelper;
import io.github.rosemoe.sora.lang.completion.CompletionItem;
import io.github.rosemoe.sora.lang.completion.CompletionPublisher;
import io.github.rosemoe.sora.lang.completion.IdentifierAutoComplete;
import io.github.rosemoe.sora.lang.completion.SimpleSnippetCompletionItem;
import io.github.rosemoe.sora.lang.completion.SnippetDescription;
import io.github.rosemoe.sora.lang.completion.snippet.CodeSnippet;
import io.github.rosemoe.sora.lang.completion.snippet.parser.CodeSnippetParser;
import io.github.rosemoe.sora.langs.java.JavaLanguage;
import io.github.rosemoe.sora.text.CharPosition;
import io.github.rosemoe.sora.text.ContentReference;
import io.github.rosemoe.sora.util.MyCharacter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import json.JSONArray;
import json.JSONObject;
import io.github.rosemoe.sora.langs.java.JavaIncrementalAnalyzeManager;

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

	private void addFromJSON(String prefix, CompletionPublisher publisher) {
		if (jsonObject == null) {
			return;
		}
		List<CompletionItem> completionItems = new ArrayList<>();
        // 获取title
		JSONArray items = jsonObject.getJSONObject("result")
			.getJSONArray("items");
		for (int i = 0; i < items.length(); i++) {
			//System.out.println(items.getJSONObject(i).toString(2));
			JSONObject item = items.getJSONObject(i);
			String insertText = item.getJSONObject("textEdit").getString("newText");
			//String label = item.getString("label");
			String desc = "";
			if (item.has("detail")) {
				desc = item.getString("detail");
			} else {
				desc = "";
			}
			CodeSnippet cs = CodeSnippetParser.parse(insertText);
			completionItems.add(new SimpleSnippetCompletionItem(insertText, desc, new SnippetDescription(prefix.length(), cs, true)));
		}
		publisher.addItems(completionItems);
        //publisher.updateList(true);
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
        try {

            Log.e("", getIdentifiers().toString());
            String prefix = CompletionHelper.computePrefix(content, position, new CompletionHelper.PrefixChecker() {
                    public boolean check(char c) {
                        return MyCharacter.isJavaIdentifierPart(c);
                    }
                });
            /*
             IdentifierAutoComplete.SyncIdentifiers idt = getIdentifiers();
             addFromJSON(prefix, publisher);
             if (idt != null) {
             getAutoComplete().requireAutoComplete(content,position,prefix, publisher, idt);
             getAutoComplete().requireAutoComplete(content,position,prefix, publisher, idt);
             }*/            
            addFromJSON(prefix, publisher);
            
        } catch (Exception e) {
            Log.wtf("", e);
        }
	}
}
