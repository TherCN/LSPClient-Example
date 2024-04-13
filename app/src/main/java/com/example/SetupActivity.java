package com.example;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import java.io.File;

public class SetupActivity extends Activity {
    
    private EditText projectPath;
    private EditText sourceFile;
    private Button extractRuntime;
    private Button entry;
    public static final String TAG = "SetupActivity";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        projectPath = findViewById(R.id.project_path);
        sourceFile = findViewById(R.id.source_file);
        extractRuntime = findViewById(R.id.extract_runtime);
        entry = findViewById(R.id.entry);
        init();
    }
    
    private void init() {

        entry.setEnabled(false);
        extractRuntime.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    ExtractAsset.unpackJDK(SetupActivity.this);
                    ExtractAsset.unpackJDTLS(SetupActivity.this);
                    ExtractAsset.unpackTestProject(SetupActivity.this);
                    extractRuntime.setText("已解压");
                    extractRuntime.setEnabled(false);
                    entry.setEnabled(true);
                }
            });
        entry.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    
                    String projectPath = SetupActivity.this.projectPath.getText().toString();
                    String sourceFilePath = SetupActivity.this.sourceFile.getText().toString();
                    
                    if (!new File(projectPath).exists()) {
                        Toast.makeText(getApplication(), projectPath + ":没有那个文件或目录", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    if (!new File(projectPath,sourceFilePath).exists()) {
                        Toast.makeText(getApplication(), projectPath + sourceFilePath + ":没有那个文件或目录", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    SharedPreferences.Editor editor = getSharedPreferences("application",MODE_PRIVATE).edit();
                    editor.putString("projectPath",projectPath);
                    editor.putString("sourceFilePath",projectPath + sourceFilePath);
                    editor.commit();
                    editor.apply();
                    
                    startActivity(new Intent(SetupActivity.this,JLSActivity.class));
                }
            });
    }
    
    
    
}
