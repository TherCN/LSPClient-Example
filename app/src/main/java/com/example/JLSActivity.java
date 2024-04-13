package com.example;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.system.Os;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.view.MenuItemCompat;
import com.example.R;
import com.google.gson.Gson;
import io.github.rosemoe.sora.lang.Language;
import io.github.rosemoe.sora.lsp.client.connection.StreamConnectionProvider;
import io.github.rosemoe.sora.lsp.client.languageserver.serverdefinition.CustomLanguageServerDefinition;
import io.github.rosemoe.sora.lsp.client.languageserver.serverdefinition.LanguageServerDefinition;
import io.github.rosemoe.sora.lsp.client.languageserver.wrapper.EventHandler;
import io.github.rosemoe.sora.lsp.editor.LspEditor;
import io.github.rosemoe.sora.lsp.editor.LspProject;
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme;
import io.github.rosemoe.sora.widget.schemes.SchemeDarcula;
import io.github.rosemoe.sora.widget.schemes.SchemeEclipse;
import io.github.rosemoe.sora.widget.schemes.SchemeGitHub;
import io.github.rosemoe.sora.widget.schemes.SchemeNotepadXX;
import io.github.rosemoe.sora.widget.schemes.SchemeVS2019;
import java.io.File;
import java.io.IOException;
import json.JSONArray;
import json.JSONObject;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.services.LanguageServer;

public class JLSActivity extends Activity {

    private static final String TAG = "JDTLSActivity";
    TextView tv;
	ICodeEditor editor;
    AsyncProcess process;
    String projectPath;
	String filePath;
    LspEditor lspEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv = findViewById(R.id.message);
        editor = findViewById(R.id.ce);
        String JAVA_HOME = getFilesDir().getAbsolutePath() + "/jdk";

        if (!new File(getFilesDir(), "jdtls.sh").exists()) {
            startActivity(new Intent(this, SetupActivity.class));
            finish();
        }

        SharedPreferences sp = getSharedPreferences("application", MODE_PRIVATE);
        if (!new File(sp.getString("projectPath", "/s")).exists()
            || !new File(sp.getString("sourceFilePath", "/s")).exists()) {
            SharedPreferences.Editor edit = sp.edit();
            edit.clear();
            edit.commit();
            edit.apply();
            modifyPath();
        } else {
            projectPath = sp.getString("projectPath", "/s");
            filePath = sp.getString("sourceFilePath", "/s");

            try {
                TLog.initLogFile("/sdcard/LSPClient.log");
                TLog.clean();
                Os.setenv("JDTLS_DIR", getFilesDir().getAbsolutePath() + "/jdtls", true);
                Os.setenv("JAVA_HOME", JAVA_HOME, true);
                Os.setenv("PATH", Os.getenv("PATH") + ":" + JAVA_HOME + "/bin", true);
                Os.setenv("LD_LIBRARY_PATH", JAVA_HOME + "/lib:" + JAVA_HOME + "/lib/server", true);
                Os.setenv("TMPDIR", getFilesDir().getAbsolutePath(), true);
                process = new AsyncProcess("sh", getFilesDir().getAbsolutePath() + "/jdtls.sh");
                process.redirectErrorStream(true);
                editor.setFile(new File(filePath));
                editor.setEditable(false);
                editor.setTypefaceText(Typeface.createFromFile("/system/fonts/DroidSansMono.ttf"));
            } catch (Exception e) {}
            new Thread(serverThread).start();
        }
    }

    private Runnable serverThread = new Runnable() {

        @Override
        public void run() {
            LanguageServerDefinition sd = new CustomLanguageServerDefinition(getExtension(filePath), new CustomLanguageServerDefinition.ServerConnectProvider() {
                    public StreamConnectionProvider createConnectionProvider(String ext) {
                        return new ProcessStreamConnectionProvider(process);
                    };}) {
                @Override
                public EventHandler.EventListener getEventListener() {
                    return listener;
                }
            };
            final LspProject project = new LspProject(projectPath);
            project.addServerDefinition(sd);
            final Object lock = new Object();
            runOnUiThread(new Runnable(){

                    @Override
                    public void run() {
                        lspEditor = project.createEditor(filePath);

                        Language wrapperLanguage = new JDTLSLanguage();
                        lspEditor.setWrapperLanguage(wrapperLanguage);
                        lspEditor.setEditor(editor);

                        synchronized (lock) {
                            lock.notify();
                        }
                    }
                });
            synchronized (lock) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {}
            }

            boolean connected;
            try {
                lspEditor.connectWithTimeoutBlocking();
                /*
                 CodeActionParams codeAction = new CodeActionParams();
                 codeAction.setRange(new Range(
                 new Position(0, 0),
                 new Position(editor.getLineCount() - 1,
                 editor.getText().getColumnCount(editor.getLineCount() - 1))));
                 codeAction.setTextDocument(new TextDocumentIdentifier("file://" + filePath));
                 codeAction.setContext(new CodeActionContext(new ArrayList<Diagnostic>()));
                 lspEditor.getRequestManager().codeAction(codeAction);*/
                connected = true;
            } catch (Exception e) {
                connected = false;
                Log.wtf("", e);
            }
            final boolean finalConnected = connected;
            runOnUiThread(new Runnable() {
                    public void run() {
                        if (finalConnected) {
                            tv.setText("Initialized Language server");
                        } else {
                            tv.setText("Unable to connect language server");
                        }
                        editor.setEditable(true);
                    }
                });
        }
    };

    private void modifyPath() {
        View v = getLayoutInflater().inflate(R.layout.view_setup_path, null);
        final EditText projectPathEt = v.findViewById(R.id.project_path);
        final EditText sourcePathEt = v.findViewById(R.id.source_file);

        final AlertDialog dialog = new AlertDialog.Builder(this)
            .setView(v)
            .setCancelable(false)
            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dia, int which) {}
            })
            .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dia, int which) { dia.dismiss(); }
            })
            .create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {  
                @Override  
				public void onClick(View v) {
                    String projectPath = projectPathEt.getText().toString();
                    String sourceFilePath = sourcePathEt.getText().toString();
                    if (!new File(projectPath).exists()) {
                        Toast.makeText(getApplication(), projectPath + ":没有那个文件或目录", Toast.LENGTH_LONG).show();
                        return;
                    }
                    if (!new File(projectPath, sourceFilePath).exists()) {
                        Toast.makeText(getApplication(), projectPath + sourceFilePath + ":没有那个文件或目录", Toast.LENGTH_LONG).show();
                        return;
                    }
                    SharedPreferences.Editor editor = getSharedPreferences("application", MODE_PRIVATE).edit();
                    editor.putString("projectPath", projectPath);
                    editor.putString("sourceFilePath", projectPath + sourceFilePath);
                    editor.commit();
                    editor.apply();
                    dialog.dismiss();
                    recreate();
                }
            });
    }

    private void setText(final CharSequence text, boolean isJson) {

        if (isJson) {
            String finalResult = "";
            JSONObject json = new JSONObject(text);
            if (json.has("bytes")) {
                JSONArray array = json.getJSONArray("bytes"); 
                StringBuilder sb = new StringBuilder();

                // 遍历字节数组，将每个字节转换为字符并添加到StringBuilder中
                for (int i = 0; i < array.length(); i++) {
                    int b = array.getInt(i);
                    sb.append((char) b);
                }
                finalResult = sb.toString();
            } else {
                finalResult = json.toString(2);
            }
            TLog.i("Client", new JSONObject(finalResult).toString(2));

        } else {
            TLog.i("Client", text);
        }
        runOnUiThread(new Runnable(){

                @Override
                public void run() {
                    tv.setText(text);
                }
            });
    }

    private EventHandler.EventListener listener = new EventHandler.EventListener() {

        @Override
        public void initialize(LanguageServer languageServer, InitializeResult initializeResult) {
            setText(new Gson().toJson(initializeResult), true);
        }

        @Override
        public void onShowMessage(MessageParams messageParams) {
            setText(new Gson().toJson(messageParams), true);
        }

        @Override
        public void onLogMessage(MessageParams messageParams) {
            setText(new Gson().toJson(messageParams), true);
            if (messageParams.getMessage() != null) {
                TLog.e("message", messageParams.getMessage());
            }
        }

        @Override
        public void onHandlerException(Exception exception) {
            setText(Log.getStackTraceString(exception), false);
        }

    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actionbar_menu, menu);
        MenuItemCompat.setShowAsAction(menu.findItem(R.id.undo), MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
        MenuItemCompat.setShowAsAction(menu.findItem(R.id.redo), MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
        MenuItemCompat.setShowAsAction(menu.findItem(R.id.save), MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
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
                break;
            case R.id.tomain:
                startActivity(new Intent(this, MainActivity.class));
                break;
            case R.id.setup_path:
                modifyPath();
        }*/
        //gradle构建报错，使用if else
        if (item.getItemId() == R.id.undo) {
            editor.undo();
        } else if (item.getItemId() == R.id.save) {
            try {
                editor.saveFile();
                Toast.makeText(getApplication(), "Saved！", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                // Handle the exception or ignore it as per the existing code
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
        } else if (item.getItemId() == R.id.tomain) {
            startActivity(new Intent(this, MainActivity.class));
        } else if (item.getItemId() == R.id.setup_path) {
            modifyPath();
        }
        return super.onOptionsItemSelected(item);
	}

    /**
     * 获取文件扩展名
     *
     * @param fileName 文件名
     * @return 文件扩展名，如果没有扩展名则返回空字符串
     */
    public static String getExtension(String fileName) {
        // 检查文件名是否为空
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }

        // 寻找最后一个点号的位置
        int dotIndex = fileName.lastIndexOf('.');

        // 如果点号不存在，或者点号是文件名的第一个字符，则返回空字符串
        if (dotIndex == -1 || dotIndex == 0) {
            return "";
        }

        // 返回点号之后的子字符串作为扩展名
        return fileName.substring(dotIndex + 1);
    }
}
