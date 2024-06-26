package com.example;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.system.ErrnoException;
import android.system.Os;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.view.MenuItemCompat;
import com.example.R;
import com.google.gson.Gson;
import io.github.rosemoe.sora.event.ContentChangeEvent;
import io.github.rosemoe.sora.event.EventReceiver;
import io.github.rosemoe.sora.event.Unsubscribe;
import io.github.rosemoe.sora.lang.diagnostic.DiagnosticsContainer;
import io.github.rosemoe.sora.lsp.utils.LspUtilsKt;
import io.github.rosemoe.sora.widget.SymbolInputView;
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme;
import io.github.rosemoe.sora.widget.schemes.SchemeDarcula;
import io.github.rosemoe.sora.widget.schemes.SchemeEclipse;
import io.github.rosemoe.sora.widget.schemes.SchemeGitHub;
import io.github.rosemoe.sora.widget.schemes.SchemeNotepadXX;
import io.github.rosemoe.sora.widget.schemes.SchemeVS2019;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import json.JSONArray;
import json.JSONException;
import json.JSONObject;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.jsonrpc.messages.NotificationMessage;
import org.eclipse.lsp4j.jsonrpc.messages.RequestMessage;


//此为基于Process和JSON发送接受实现的客户端，问题较多
public class MainActivity extends Activity {

	AsyncProcess process;
	InputStream server;
	OutputStream client;
	TextView tv;
	public static ICodeEditor editor;
	boolean started;
	DiagnosticsContainer con;
	JDTLSLanguage language;
    JSONObject completionRequest;
    Object lock = new Object();

    String c = "";
	JSONArray currentDiagnostics;
	int lines = 0;
	String projectPath = Environment.getExternalStorageDirectory() + "/AppProjects/MyJavaConsoleApp";
	String filePath = projectPath + "/src/Main.java";
	String fileURI = "file://" + filePath;

	private Runnable runnable = new Runnable() {
		@Override
		public void run() {
			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(server));
				String temp;
				while ((temp = br.readLine()) != null) {
					lines++;
                    if (lines < 5) {
                        final String text = temp;
                        runOnUiThread(new Runnable(){

                                @Override
                                public void run() {
                                    tv.append("\n" + text);
                                }
                            });
                    } else if (lines == 5) {
                        editor.setEditable(true);
                        sendInit();}
					if (temp.contains("}Content-Length")) {
						temp = temp.substring(0, temp.lastIndexOf("}") + 1);
					} else {
						continue;
					}
					final String str = temp;	
					TLog.i("Server", new JSONObject(str).toString(2) + "\n" + str);
					if (editor.isEditable()) {
						parseResult(new JSONObject(str));
					} else {
						runOnUiThread(new Runnable(){

								@Override
								public void run() {

									tv.setText(str);
								}
							});
					}}
				br.close();

			} catch (Exception e) {
				Log.e("错误", Log.getStackTraceString(e));
			}
		}};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

		ExtractAsset.unpackJDTLS(this);
		ExtractAsset.unpackJDK(this);
		ExtractAsset.unpackTestProject(this);
		TLog.initLogFile("/sdcard/LSPClient.log");
		TLog.clean();
		tv = findViewById(R.id.message);
		tv.setText("Starting JVM...");
		String JAVA_HOME = getFilesDir().getAbsolutePath() + "/jdk";
		try {
			Os.setenv("JDTLS_DIR", getFilesDir().getAbsolutePath() + "/jdtls", true);
			Os.setenv("JAVA_HOME", JAVA_HOME, true);
			Os.setenv("PATH", Os.getenv("PATH") + ":" + JAVA_HOME + "/bin", true);
			Os.setenv("LD_LIBRARY_PATH", JAVA_HOME + "/lib:" + JAVA_HOME + "/lib/server", true);
            Os.setenv("TMPDIR", getFilesDir().getAbsolutePath(), true);
			process = new AsyncProcess("sh", getFilesDir().getAbsolutePath() + "/jdtls.sh");
			process.redirectErrorStream(true);
			process.start();
            Typeface t = Typeface.createFromFile(new File("/system/fonts/DroidSansMono.ttf"));
			con = new DiagnosticsContainer();
			editor = findViewById(R.id.ce);
			editor.setFile(new File(filePath));
			editor.setTypefaceText(t);
			editor.setEditable(false);
			editor.setColorScheme(new AIDEColorSchemes.Dark());
			language = new JDTLSLanguage(this);
			editor.setEditorLanguage(language);
			SymbolInputView siv = findViewById(R.id.siv);
			String[] charArray = new String[] {
				"→",
				"{",
				"}",
				"(",
				")",
				";",
				",",
				".",
				"=",
				"\"",
				"\\",
				"|",
				"&",
				"!",
				"[",
				"]",
				"<",
				"%",
				">",
				"+",
				"-",
				"/",
				"*",
				"?",
				":",
				"_"};
			String[] insertCharArray = new String[] {
				"\t",
				"{",
				"}",
				"(",
				")",
				";",
				",",
				".",
				"=",
				"\"",
				"\\",
				"|",
				"&",
				"!",
				"[",
				"]",
				"<",
				"%",
				">",
				"+",
				"-",
				"/",
				"*",
				"?",
				":",
				"_"};
			siv.addSymbols(charArray, insertCharArray);
			siv.bindEditor(editor);
			editor.setDiagnostics(con);
			editor.subscribeEvent(ContentChangeEvent.class, new EventReceiver<ContentChangeEvent>() {
					public void onReceive(final ContentChangeEvent event, Unsubscribe unsubscribe) {

                        try {
                            setupCompeltion(event);
                            sendUpdate(event);
                        } catch (Exception e) {}

					}
				});
		} catch (IOException|ErrnoException e) {
			Log.e(e.getClass().getSimpleName(), Log.getStackTraceString(e));
		}
		server = process.getProcess().getInputStream();
		client = process.getProcess().getOutputStream();

        try {

        } catch (Exception e) {}
		new Thread(runnable).start();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.actionbar_menu, menu);
		MenuItemCompat.setShowAsAction(menu.findItem(R.id.undo), MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
		MenuItemCompat.setShowAsAction(menu.findItem(R.id.redo), MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
		MenuItemCompat.setShowAsAction(menu.findItem(R.id.save), MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
        menu.findItem(R.id.tomain).setVisible(false);
		return super.onCreateOptionsMenu(menu);
	}


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /*
        switch (item.getItemId()) {
            case R.id.undo: editor.undo(); break;
            case R.id.save:
                try {
                    editor.saveFile();
                    Toast.makeText(getApplication(), "Saved！", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {} 
                break;
            case R.id.redo: editor.redo(); break;
            case R.id.scheme_eclipse:
                editor.setColorScheme(new SchemeEclipse());
                break;
            case R.id.scheme_vs2019:
                editor.setColorScheme(new SchemeVS2019());
                break;
            case R.id.scheme_notepadpp:
                editor.setColorScheme(new SchemeNotepadXX());
                break;
            case R.id.scheme_github:
                editor.setColorScheme(new SchemeGitHub());
                break;
            case R.id.scheme_durcula:
                editor.setColorScheme(new SchemeDarcula());
                break;
            case R.id.scheme_default:
                editor.setColorScheme(new EditorColorScheme());
                break;
            case R.id.aide_dark:
                editor.setColorScheme(new AIDEColorSchemes.Dark());
                break;
            case R.id.aide_light:
                editor.setColorScheme(new AIDEColorSchemes.Light());
        }*/
        
        if (item.getItemId() == R.id.undo) {
            editor.undo();
        } else if (item.getItemId() == R.id.save) {
            try {
                editor.saveFile();
                Toast.makeText(getApplication(), "Saved！", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                // Handle the exception, possibly with a Toast or log message
            }
        } else if (item.getItemId() == R.id.redo) {
            editor.redo();
        } else if (item.getItemId() == R.id.scheme_eclipse) {
            editor.setColorScheme(new SchemeEclipse());
        } else if (item.getItemId() == R.id.scheme_vs2019) {
            editor.setColorScheme(new SchemeVS2019());
        } else if (item.getItemId() == R.id.scheme_notepadpp) {
            editor.setColorScheme(new SchemeNotepadXX());
        } else if (item.getItemId() == R.id.scheme_github) {
            editor.setColorScheme(new SchemeGitHub());
        } else if (item.getItemId() == R.id.scheme_durcula) {
            editor.setColorScheme(new SchemeDarcula());
        } else if (item.getItemId() == R.id.scheme_default) {
            editor.setColorScheme(new EditorColorScheme());
        } else if (item.getItemId() == R.id.aide_dark) {
            editor.setColorScheme(new AIDEColorSchemes.Dark());
        } else if (item.getItemId() == R.id.aide_light) {
            editor.setColorScheme(new AIDEColorSchemes.Light());
        }

        return super.onOptionsItemSelected(item);
    }

	@Override
	protected void onPause() {
		super.onPause();
		try {
			editor.saveFile();
		} catch (IOException e) {}
	}

	@Override
	public void onBackPressed() {
		onDestroy();
        super.onBackPressed();
	}

    @Override
    protected void onDestroy() {
        editor.release();
        process.getProcess().destroy();
        super.onDestroy();
    }

	public static void customSleep(long milliseconds) {
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < milliseconds) {}
    }

	public void sendUpdate(final ContentChangeEvent codeText) throws Exception {
        FileUri file = new FileUri(filePath);
		String changedText = codeText.getChangedText().toString();
		JSONObject message = new JSONObject();
		message.put("jsonrpc", "2.0");
		message.put("method", "textDocument/didChange");
	    JSONObject params = new JSONObject();
		JSONObject textDocument = new JSONObject();
		textDocument.put("version", file.getVersion());
		textDocument.put("uri", file.toUri().toString());
		params.put("textDocument", textDocument);
		JSONArray contentChanges = new JSONArray();
		JSONObject text = new JSONObject();
		text.put("text", editor.getText().toString());
		contentChanges.put(text);
		params.put("contentChanges", contentChanges);
		message.put("params", params);
        sendRequest(message, "change");
        /*
         JSONObject jsonObject2 = new JSONObject();
         jsonObject2.put("jsonrpc", "2.0");
         jsonObject2.put("method", "textDocument/codeAction");
         params = new JSONObject();
         JSONObject context = new JSONObject();
         if (currentDiagnostics == null) {
         currentDiagnostics = new JSONArray();
         }
         context.put("diagnostics", currentDiagnostics);
         params.put("context", context);
         JSONObject range = new JSONObject();
         JSONObject start = new JSONObject();
         JSONObject end = new JSONObject();
         start.put("character", 0);
         start.put("line", 0);
         end.put("character", editor.getText().getLineString(editor.getLineCount() - 2).length());
         end.put("line", editor.getText().getLineCount() - 2);
         range.put("start", start);
         range.put("end", end);
         params.put("range", range);
         JSONObject textDocument2 = new JSONObject();
         textDocument2.put("uri", fileURI);
         params.put("textDocument", textDocument2);
         jsonObject2.put("params", params);

         sendRequest(jsonObject2, "action");
         */


	}
    private void setupCompeltion(ContentChangeEvent codeText) {
        completionRequest = new JSONObject();
        completionRequest.put("jsonrpc", "2.0");
        completionRequest.put("method", "textDocument/completion");
        JSONObject params = new JSONObject();
        JSONObject textDocument = new JSONObject();
        textDocument.put("uri", fileURI);
        params.put("textDocument", textDocument);
        JSONObject position = new JSONObject();
        position.put("line", codeText.getChangeEnd().getLine());
        position.put("character", codeText.getChangeEnd().getColumn());
        params.put("position", position);
		completionRequest.put("params", params);
    }


    public void requestCompletion() {
        try {
            c = "completion" + lines;
            sendRequest(completionRequest, c);
            Log.e("", Thread.currentThread().getState().toString());
            synchronized (lock) {
                Log.e("", "等待中");
                cancelRequest(c);
                lock.notify();
                lock.wait();
            }
        } catch (Exception e) {
            Log.e(getClass().getSimpleName(), Log.getStackTraceString(e));
        }
    }

	public void parseResult(final JSONObject rawObj) {
        if (!rawObj.has("id")) return;
		if (rawObj.getString("id").equals(c)) {
            synchronized (lock) {
                Log.e("", "解除等待");
                lock.notifyAll();
            }
			language.setJSONData(rawObj);
			//editor.getComponent(EditorAutoCompletion.class).requireCompletion();
		}
		runOnUiThread(new Runnable(){

				@Override
				public void run() {
					try {
						tv.setText(rawObj.toString(2));
					} catch (JSONException e) {}
				}
			});

	}

	public static String fileToString(String fileName) throws IOException {
		return new String(Files.readAllBytes(Paths.get(fileName)));
	}

	private void addDiagnose(JSONObject jsonObject)throws JSONException {
        JSONObject params = jsonObject.getJSONObject("params");
        JSONArray diagnosticsArray = params.getJSONArray("diagnostics");
        currentDiagnostics = diagnosticsArray;
        // 创建Diagnostic列表
        List<Diagnostic> diagnostics = new ArrayList<>();

        // 遍历diagnostics数组并创建Diagnostic对象
        for (int i = 0; i < diagnosticsArray.length(); i++) {
            JSONObject diagnosticJson = diagnosticsArray.getJSONObject(i);
            int severity = diagnosticJson.getInt("severity");
            String code = diagnosticJson.getString("code");
            JSONObject rangeJson = diagnosticJson.getJSONObject("range");
            JSONObject startJson = rangeJson.getJSONObject("start");
            JSONObject endJson = rangeJson.getJSONObject("end");
            String source = diagnosticJson.getString("source");
            String message = diagnosticJson.getString("message");

            // 创建Position和Range对象
            Position start = new Position(startJson.getInt("line"), startJson.getInt("character"));
            Position end = new Position(endJson.getInt("line"), endJson.getInt("character"));
            Range range = new Range(start, end);

            // 创建Diagnostic对象
            Diagnostic diagnostic = new Diagnostic(range, message);
            diagnostic.setSeverity(DiagnosticSeverity.forValue(severity));
            diagnostic.setCode(code);
            diagnostic.setSource(source);

            // 将Diagnostic对象添加到列表中
            diagnostics.add(diagnostic);
        }
		con.reset();
		con.addDiagnostics(LspUtilsKt.transformToEditorDiagnostics(diagnostics, editor));
	}

	private void sendInit() throws Exception {
        FileUri file = new FileUri(filePath);
		JSONObject json = new JSONObject();
		JSONObject params = new JSONObject();
		json.put("jsonrpc", "2.0");
		json.put("method", "initialize");

		params.put("rootPath", projectPath);
		params.put("rootUri", "file://" + projectPath);
		JSONObject capabilities = new JSONObject();
		JSONObject textCapabilities = new JSONObject();
		JSONObject ref = new JSONObject();
		ref.put("dynamicRegistration", true);
		textCapabilities.put("references", ref);
		JSONObject def = new JSONObject();
		def.put("dynamicRegistration", true);
		JSONObject completion = new JSONObject();

        // 创建completionItem子节点
        JSONObject completionItem = new JSONObject();
        // 添加documentationFormat数组
        JSONArray documentationFormat = new JSONArray();
        documentationFormat.put("plaintext");
        documentationFormat.put("markdown");
        completionItem.put("documentationFormat", documentationFormat);

        // 创建resolveSupport子节点
        JSONObject resolveSupport = new JSONObject();
        JSONArray properties = new JSONArray();
        properties.put("documentation");
        properties.put("detail");
        resolveSupport.put("properties", properties);

        // 将resolveSupport添加到completionItem
        completionItem.put("resolveSupport", resolveSupport);

        // 将completionItem添加到completion
        completion.put("completionItem", completionItem);

        // 创建completionItemKind子节点
        JSONObject completionItemKind = new JSONObject();
        JSONArray valueSet = new JSONArray();
        for (int i = 1; i <= 25; i++) {
            valueSet.put(i);
        }
        completionItemKind.put("valueSet", valueSet);

        // 将completionItemKind添加到completion
        completion.put("completionItemKind", completionItemKind);
		textCapabilities.put("completion", completion);
		textCapabilities.put("definition", def);
		capabilities.put("textDocument", textCapabilities);
		JSONObject workspaceCapabilities = new JSONObject();
		workspaceCapabilities.put("workspaceFolders", true);
		capabilities.put("workspace", workspaceCapabilities);
		params.put("capabilities", capabilities);
		params.put("locale", "zh_CN.UTF-8");
		json.put("params", params);

		sendRequest(json, "init");
		JSONObject jsonObject = new JSONObject();

		// 向JSONObject中添加属性
		jsonObject.put("jsonrpc", "2.0");
		jsonObject.put("method", "initialized");
		jsonObject.put("params", new JSONObject()); // params作为一个空的JSONObject
		sendRequest(jsonObject, "initialized");
		/*
		 JSONObject jsonObject = new JSONObject();
		 jsonObject.put("jsonrpc", "2.0");
		 jsonObject.put("method", "workspace/didChangeWorkspaceFolders");
		 params = new JSONObject();
		 JSONObject event = new JSONObject();
		 JSONArray added = new JSONArray();
		 JSONObject addedFolder = new JSONObject();
		 addedFolder.put("name", fileURI);
		 addedFolder.put("uri", fileURI);
		 added.put(addedFolder);
		 event.put("added", added);
		 JSONArray removed = new JSONArray();
		 event.put("removed", removed);
		 params.put("event", event);
		 jsonObject.put("params", params);

		 sendRequest(jsonObject);
		 */
        DidOpenTextDocumentParams didopen = new DidOpenTextDocumentParams();
        TextDocumentItem textDocumentItem = new TextDocumentItem();
		//JSONObject json2 = new JSONObject();
        didopen.setTextDocument(textDocumentItem);
        textDocumentItem.setUri( file.toUri().toString());
        textDocumentItem.setLanguageId("java");
        textDocumentItem.setText(editor.getText().toString());
		textDocumentItem.setVersion(file.getVersion());
        RequestMessage message = new RequestMessage();
        message.setMethod("textDocument/didOpen");
        message.setParams(didopen);
        message.setId("openfile");
		sendRequest(new JSONObject(new Gson().toJson(message)), "openfile");


		JSONObject jsonObject2 = new JSONObject();
		jsonObject2.put("jsonrpc", "2.0");
		jsonObject2.put("method", "textDocument/codeAction");
		params = new JSONObject();
		JSONObject context = new JSONObject();
		JSONArray diagnostics = new JSONArray();
		context.put("diagnostics", diagnostics);
		params.put("context", context);
		JSONObject range = new JSONObject();
		JSONObject start = new JSONObject();
		JSONObject end = new JSONObject();
		start.put("character", 0);
		start.put("line", 0);
        String text = editor.getText().toString();
		end.put("character", text.isEmpty() ? 0 : editor.getText().getLineString(editor.getLineCount() - 1).length());
		end.put("line", text.isEmpty() ? 1 : editor.getText().getLineCount() - 1);
		range.put("start", start);
		range.put("end", end);
		params.put("range", range);
		JSONObject textDocument = new JSONObject();
		textDocument.put("uri", file.toUri().toString());
		params.put("textDocument", textDocument);
		jsonObject2.put("params", params);

		sendRequest(jsonObject2, "action");

	}

    private void cancelRequest(String id) {
        JSONObject jsonObject = new JSONObject();

        // 向该JSONObject中添加元素
        jsonObject.put("jsonrpc", "2.0");
        jsonObject.put("method", "$/cancelRequest");

        // 创建一个嵌套的JSONObject作为参数
        JSONObject paramsObject = new JSONObject();
        paramsObject.put("id", id);

        // 将参数JSONObject添加到外层的JSONObject中
        jsonObject.put("params", paramsObject);
    }
	public void sendRequest(final JSONObject json, String id) throws Exception {
        if (json.has("method")) json.put("id", id);
		String content = json.toString() + "\n";
		String headers = "Content-Length: " + content.length() + "\r\nContent-Type: application/vscode-jsonrpc;charset=utf8\n\n";
		final String request = headers + content;
		TLog.i("Client", json.toString(2) + "\n" + json.toString());
		client.write(request.getBytes());
		client.flush();
	}

	public void writeLog(CharSequence thro) {

		try {
			FileWriter fw = new FileWriter(new File("/sdcard/log"));
			fw.write(thro.toString());
			fw.close();
		} catch (IOException e) {}

	}
}
