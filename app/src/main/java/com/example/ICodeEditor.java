package com.example;
import android.content.Context;
import io.github.rosemoe.sora.widget.CodeEditor;
import java.io.File;
import java.nio.file.Files;
import java.io.IOException;
import android.util.AttributeSet;

public class ICodeEditor extends CodeEditor{
    
    public static final String TAG = "ICodeEditor";
    private File file;
    public ICodeEditor(Context context) {
		super(context);
	}
    
	public ICodeEditor(Context context,AttributeSet attr) {
		super(context,attr);
	}
	
	public void setFile(File file) throws IOException {
		setText(new String(Files.readAllBytes(file.toPath())));
		this.file = file;
	}
	
	public File getFile() {
		return this.file;
	}
	
	public void saveFile() throws IOException{
		Files.write(file.toPath(),getText().toString().getBytes());
	}
}
