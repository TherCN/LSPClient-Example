package com.example;
import android.os.Bundle;
import io.github.rosemoe.sora.lang.completion.CompletionHelper;
import io.github.rosemoe.sora.lang.completion.CompletionItem;
import io.github.rosemoe.sora.lang.completion.CompletionPublisher;
import io.github.rosemoe.sora.lang.completion.SimpleSnippetCompletionItem;
import io.github.rosemoe.sora.lang.completion.snippet.CodeSnippet;
import io.github.rosemoe.sora.lang.completion.snippet.parser.CodeSnippetParser;
import io.github.rosemoe.sora.langs.java.JavaLanguage;
import io.github.rosemoe.sora.text.CharPosition;
import io.github.rosemoe.sora.text.ContentReference;
import io.github.rosemoe.sora.util.MyCharacter;
import java.util.ArrayList;
import java.util.List;
import json.JSONArray;
import json.JSONObject;
import io.github.rosemoe.sora.lang.completion.SnippetDescription;

public class JDTLSLanguage extends JavaLanguage {


	JSONObject jsonObject;
    public JDTLSLanguage() {
		super();
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
				desc = item.getString("label");
			}
			CodeSnippet cs = CodeSnippetParser.parse(insertText);
			completionItems.add(new SimpleSnippetCompletionItem(insertText, desc, new SnippetDescription(prefix.length(), cs, true)));
		}
		publisher.addItems(completionItems);
	}
	@Override
	public void requireAutoComplete(ContentReference content, CharPosition position, CompletionPublisher publisher, Bundle extraArguments) {
		String prefix = CompletionHelper.computePrefix(content, position, new CompletionHelper.PrefixChecker() {
				public boolean check(char c) {
					return MyCharacter.isJavaIdentifierPart(c);
				}
			});
		addFromJSON(prefix, publisher);
		super.requireAutoComplete(content, position, publisher, extraArguments);

	}
}
