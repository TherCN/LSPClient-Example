package com.example;
import android.os.Bundle;
import androidx.annotation.NonNull;
import io.github.rosemoe.sora.lang.EmptyLanguage;
import io.github.rosemoe.sora.lang.analysis.AsyncIncrementalAnalyzeManager;
import io.github.rosemoe.sora.lang.completion.CompletionHelper;
import io.github.rosemoe.sora.lang.completion.CompletionPublisher;
import io.github.rosemoe.sora.lang.completion.IdentifierAutoComplete;
import io.github.rosemoe.sora.lang.completion.SimpleSnippetCompletionItem;
import io.github.rosemoe.sora.lang.completion.SnippetDescription;
import io.github.rosemoe.sora.lang.completion.snippet.CodeSnippet;
import io.github.rosemoe.sora.lang.completion.snippet.parser.CodeSnippetParser;
import io.github.rosemoe.sora.langs.java.State;
import io.github.rosemoe.sora.text.CharPosition;
import io.github.rosemoe.sora.text.ContentReference;
import io.github.rosemoe.sora.util.MyCharacter;
import io.github.rosemoe.sora.langs.java.JavaIncrementalAnalyzeManager;
import io.github.rosemoe.sora.lang.QuickQuoteHandler;

public class JavaLanguage extends EmptyLanguage {

	private final static CodeSnippet FOR_SNIPPET = CodeSnippetParser.parse("for(int ${1:i} = 0;$1 < ${2:count};$1++) {\n    $0\n}");
    private final static CodeSnippet STATIC_CONST_SNIPPET = CodeSnippetParser.parse("private final static ${1:type} ${2/(.*)/${1:/upcase}/} = ${3:value};");
    private final static CodeSnippet CLIPBOARD_SNIPPET = CodeSnippetParser.parse("${1:${CLIPBOARD}}");
    public static final String TAG = "JavaLanguage";
    public IdentifierAutoComplete IAC;
	JavaAnalyzeManager manager;
    public JavaLanguage() {
		super();
		IAC = new IdentifierAutoComplete();
		manager = new JavaAnalyzeManager();
	}

	

	
	public void setKeyWords(String[] keywords) {
		IAC.setKeywords(keywords,true);
		
	}
	
	@Override
    public void requireAutoComplete(@NonNull ContentReference content, @NonNull CharPosition position,
                                    @NonNull CompletionPublisher publisher, @NonNull Bundle extraArguments) {
        String prefix = CompletionHelper.computePrefix(content, position, new CompletionHelper.PrefixChecker() {
			public boolean check(char p) {
				return MyCharacter.isJavaIdentifierPart(p);
			}
		});
        final IdentifierAutoComplete.SyncIdentifiers idt = manager.identifiers;
        if (idt != null) {
            IAC.requireAutoComplete(content,position,prefix, publisher, idt);
        }
        if ("fori".startsWith(prefix) && prefix.length() > 0) {
            publisher.addItem(new SimpleSnippetCompletionItem("fori", "Snippet - For loop on index", new SnippetDescription(prefix.length(), FOR_SNIPPET, true)));
        }
        if ("sconst".startsWith(prefix) && prefix.length() > 0) {
            publisher.addItem(new SimpleSnippetCompletionItem("sconst", "Snippet - Static Constant", new SnippetDescription(prefix.length(), STATIC_CONST_SNIPPET, true)));
        }
        if ("clip".startsWith(prefix) && prefix.length() > 0) {
            publisher.addItem(new SimpleSnippetCompletionItem("clip", "Snippet - Clipboard contents", new SnippetDescription(prefix.length(), CLIPBOARD_SNIPPET, true)));
        }
    }
	
	class JavaAnalyzeManager extends JavaIncrementalAnalyzeManager {
		protected IdentifierAutoComplete.SyncIdentifiers identifiers = new IdentifierAutoComplete.SyncIdentifiers();
		@Override
		public void onAddState(State state) {
			if (state.identifiers != null) {
				for (String identifier : state.identifiers) {
					identifiers.identifierIncrease(identifier);
				}
			}
		}

		@Override
		public void onAbandonState(State state) {
			if (state.identifiers != null) {
				for (String identifier : state.identifiers) {
					identifiers.identifierDecrease(identifier);
				}
			}
		}

		@Override
		public void reset(@NonNull ContentReference content, @NonNull Bundle extraArguments) {
			super.reset(content, extraArguments);
			identifiers.clear();
		}
		@Override
		@NonNull
		public State getInitialState() {
			return new State();
		}

		@Override
		public boolean stateEquals(@NonNull State state, @NonNull State another) {
			return state.equals(another);
		}
		
	}
    
	
}
