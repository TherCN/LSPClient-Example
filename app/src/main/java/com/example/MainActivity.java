package com.example;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.system.ErrnoException;
import android.system.Os;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;
import com.example.R;
import io.github.rosemoe.sora.event.ContentChangeEvent;
import io.github.rosemoe.sora.event.EventReceiver;
import io.github.rosemoe.sora.event.Unsubscribe;
import io.github.rosemoe.sora.lang.diagnostic.DiagnosticDetail;
import io.github.rosemoe.sora.lang.diagnostic.DiagnosticRegion;
import io.github.rosemoe.sora.lang.diagnostic.DiagnosticsContainer;
import io.github.rosemoe.sora.widget.CodeEditor;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.file.Paths;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.nio.file.Files;

public class MainActivity extends Activity {

	AsyncProcess process;
	InputStream server;
	OutputStream client;
	TextView tv;
	TextView request;
	CodeEditor editor;
	boolean started;
	DiagnosticsContainer con;
	
	String projectPath = Environment.getExternalStorageDirectory() + "/TestProject";
	String filePath = projectPath + "/src/main/java/Level.java";
	String fileURI = "file://" + filePath;

	private Runnable runnable = new Runnable() {
		@Override
		public void run() {
			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(server));
				String temp;
				while ((temp = br.readLine()) != null) {
					Log.e("", temp);

					if (temp.contains("}Content-Length")) {
						temp = temp.substring(0, temp.lastIndexOf("}") + 1);
					} else {
						continue;
					}
					final String str = temp;	
					Log.e("parse", str);
					writeLog(new JSONObject(str).toString(2));
					if (str.contains("result") && editor.isEditable()) {
						parseResult(new JSONObject(str));
					} else {
						runOnUiThread(new Runnable(){

								@Override
								public void run() {
									tv.setText(str);
									if (str.contains("Ready")) {
										editor.setEditable(true);
									}
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
		tv = findViewById(R.id.message);
		request = findViewById(R.id.request);
		String JAVA_HOME = getFilesDir().getAbsolutePath() + "/jdk";
		try {
			Os.setenv("JDTLS_DIR", getFilesDir().getAbsolutePath() + "/jdtls", true);
			Os.setenv("JAVA_HOME", JAVA_HOME, true);
			Os.setenv("PATH", Os.getenv("PATH") + ":" + JAVA_HOME + "/bin", true);
			Os.setenv("LD_LIBRARY_PATH", JAVA_HOME + "/lib:" + JAVA_HOME + "/lib/server", true);
		} catch (ErrnoException e) {}
		process = new AsyncProcess("sh", getFilesDir().getAbsolutePath() + "/jdtls.sh");
		process.redirectErrorStream(true);
		process.start();
		con = new DiagnosticsContainer();
		editor = findViewById(R.id.ce);
		try {
			editor.setText(fileToString(filePath));
			editor.setEditable(false);
			editor.setEditorLanguage(new JavaLanguage());
			editor.setDiagnostics(con);
			editor.subscribeEvent(
				ContentChangeEvent.class,
				new EventReceiver<ContentChangeEvent>() {
					long lastInvoke = System.currentTimeMillis();
					public void onReceive(ContentChangeEvent event, Unsubscribe unsubscribe) {
						try {
							sendUpdate(event);
						} catch (Exception e) {
							Log.e("", Log.getStackTraceString(e));
						}
					}
				});
		} catch (IOException e) {
			Log.e(e.getClass().getSimpleName(),Log.getStackTraceString(e));
		}
		server = process.getProcess().getInputStream();
		client = process.getProcess().getOutputStream();
		new Thread(runnable).start();
		customSleep(5000);
		try {
			sendInit();
		} catch (Exception e) {}
	}

	public static void customSleep(long milliseconds) {
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < milliseconds) {}
    }

	public void sendUpdate(final ContentChangeEvent codeText) throws Exception {

		JSONObject message = new JSONObject();

		// 设置jsonrpc版本
		message.put("jsonrpc", "2.0");

		// 设置方法名
		message.put("method", "textDocument/didChange");

		// 创建params对象
		JSONObject params = new JSONObject();

		// 创建textDocument对象
		JSONObject textDocument = new JSONObject();
		textDocument.put("version", 3);
		textDocument.put("uri", fileURI);
		// 将textDocument对象放入params对象中
		params.put("textDocument", textDocument);

		// 创建contentChanges数组
		JSONArray contentChanges = new JSONArray();

		// 创建第一个变化的对象
		JSONObject change = new JSONObject();
		JSONObject range = new JSONObject();
		JSONObject start = new JSONObject();
		JSONObject end = new JSONObject();

		// 设置range的start和end
		start.put("line", codeText.getChangeStart().getLine());
		start.put("character", codeText.getChangeStart().getColumn());
		end.put("line", codeText.getChangeEnd().getLine());
		end.put("character", codeText.getChangeEnd().getColumn());
		range.put("start", start);
		range.put("end", end);

		// 将range放入变化对象中
		change.put("range", range);
		change.put("rangeLength", codeText.getChangedText().length());
		change.put("text", codeText.getChangedText());

		// 将变化对象放入contentChanges数组中
		contentChanges.put(change);

		// 将contentChanges数组放入params对象中
		params.put("contentChanges", contentChanges);

		// 将params对象放入整个消息中
		message.put("params", params);

		String content = message + "\n";
		String headers = "Content-Length: " + content.length() + "\nContent-Type: application/json;charset=utf8\n\n";
		final String request = headers + content;


		if (codeText.getChangedText().toString().isEmpty()) {return;}
		JSONObject completionRequest = new JSONObject();
		completionRequest.put("jsonrpc", "2.0");
		completionRequest.put("method", "textDocument/completion");

		// 构建参数部分
		params = new JSONObject();
		textDocument = new JSONObject();
		textDocument.put("uri", fileURI);
		params.put("textDocument", textDocument);
		JSONObject position = new JSONObject();
		position.put("line", codeText.getChangeEnd().getLine());
		position.put("character", codeText.getChangeEnd().getColumn());
		params.put("position", position);
		// 将参数添加到请求中
		completionRequest.put("params", params);

		content = completionRequest + "\n";
		headers = "Content-Length: " + content.length() + "\nContent-Type: application/json;charset=utf8\n\n";
		final String compRequest = headers + content;
		this.request.setText("request:" + request + "\n" +
							 (codeText.toString().isEmpty() ? "" : compRequest));

		client.write(request.getBytes());
		client.flush();
		client.write(compRequest.getBytes());
		client.flush();

	}

	public void parseResult(final JSONObject rawObj) {

		runOnUiThread(new Runnable(){

				@Override
				public void run() {
					try {
						tv.setText(rawObj.toString(2));
					} catch (JSONException e) {}
				}
			});

	}

	public static String fileToString(String fileName) throws IOException{
		return new String(Files.readAllBytes(Paths.get(fileName)));
	}
	/*
	private void addDiagnose(JSONObject jsonObject)throws JSONException {
		con.reset();
        // 解析参数部分
        JSONObject params = jsonObject.getJSONObject("params");
        // 解析诊断信息
        JSONArray diagnostics = params.getJSONArray("diagnostics");
        for (int i = 0; i < diagnostics.length(); i++) {
            JSONObject diagnostic = diagnostics.getJSONObject(i);
            JSONObject range = diagnostic.getJSONObject("range");
            JSONObject start = range.getJSONObject("start");
			JSONObject end = range.getJSONObject("end");
            int startLine = start.getInt("line");
            int startCharacter = start.getInt("character");
			int endLine = end.getInt("line");
			int endCharacter = end.getInt("character");
            String message = diagnostic.getString("message");
			int startIndex = editor.getText().getCharIndex(startLine, startCharacter);
			int endIndex = 0;
			endIndex = editor.getText().getCharIndex(endLine, endCharacter);
			String diagnoseMessage[] = message.split(", ");
			String severity = DiagnosticSeverity.forValue(diagnostic.getInt("severity")).toString();

			if (diagnoseMessage[0].endsWith("is a non-project file")) {
				continue;
			}
			short serverityValue = 0;
			switch (severity) {
				case "Error":
					serverityValue = DiagnosticRegion.SEVERITY_ERROR;
					break;
				case "Warning":
					serverityValue = DiagnosticRegion.SEVERITY_WARNING;
					break;
				case "Information":
					serverityValue = DiagnosticRegion.SEVERITY_TYPO;
					break;
				case "Hint":
					serverityValue = DiagnosticRegion.SEVERITY_TYPO;

			}
			DiagnosticRegion diag = new DiagnosticRegion(startIndex, endIndex,
														 serverityValue,
														 0,
														 new DiagnosticDetail(diagnoseMessage[0], diagnoseMessage[1], null, null));
			con.addDiagnostic(diag);
        }
	}*/
	private void sendInit() throws Exception {
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
		textCapabilities.put("definition", def);
		capabilities.put("textDocument", textCapabilities);
		JSONObject workspaceCapabilities = new JSONObject();
		workspaceCapabilities.put("workspaceFolders", true);
		capabilities.put("workspace", workspaceCapabilities);
		params.put("capabilities", capabilities);
		json.put("params", params);

		sendRequest(json);

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

		JSONObject json2 = new JSONObject();
		params = new JSONObject();
		json2.put("jsonrpc", "2.0");
		json2.put("method", "textDocument/didOpen");
		JSONObject textDoc = new JSONObject();
		textDoc.put("uri", fileURI);
		textDoc.put("languageId", "java");
		textDoc.put("text", fileToString(filePath));
		textDoc.put("version", 3);
		params.put("textDocument", textDoc);
		json2.put("params", params);

		sendRequest(json2);

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
		end.put("character", editor.getText().getLineString(editor.getLineCount() - 1).length());
		end.put("line", editor.getText().getLineCount() - 1);
		range.put("start", start);
		range.put("end", end);
		params.put("range", range);
		JSONObject textDocument = new JSONObject();
		textDocument.put("uri", textDoc.getString("uri"));
		params.put("textDocument", textDocument);
		jsonObject2.put("params", params);

		sendRequest(jsonObject2);

		jsonObject = new JSONObject();

		// 向JSONObject中添加属性
		jsonObject.put("jsonrpc", "2.0");
		jsonObject.put("method", "initialized");
		jsonObject.put("params", new JSONObject()); // params作为一个空的JSONObject
		sendRequest(jsonObject);
	}

	public void sendRequest(final JSONObject json) throws IOException {
		String content = json.toString() + "\n";
		String headers = "Content-Length: " + content.length() + "\r\nContent-Type: application/vscode-jsonrpc;charset=utf8\n\n";
		final String request = headers + content;
		Log.e("发送", json.toString());
		Log.e("", "被调用");
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
