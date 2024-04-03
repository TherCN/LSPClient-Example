package com.example;
import io.github.rosemoe.sora.lsp.client.connection.StreamConnectionProvider;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ProcessStreamConnectionProvider implements StreamConnectionProvider {
    
    private static final String TAG = "ProcessStreamConnectionProvider";
    Process process;
    AsyncProcess builder;
    public ProcessStreamConnectionProvider(AsyncProcess builder) {
       this.builder = builder;
    }

    @Override
    public InputStream getInputStream() {
        return process.getInputStream();
    }

    @Override
    public OutputStream getOutputStream() {
        return process.getOutputStream();
    }

    @Override
    public void start() {
        builder.start();
        try {
            //休眠5秒以等待语言服务器初始化完成
            Thread.currentThread().sleep(5000);
        } catch (InterruptedException e) {}
        process = builder.getProcess();
    }

    @Override
    public void close() {
        process.destroy();
    }
    
}
