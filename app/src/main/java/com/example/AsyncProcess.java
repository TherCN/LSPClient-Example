package com.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class AsyncProcess {

	String[] cmd;
	ProcessBuilder processBuilder;
	Process commandProcess;
	ProcessCallbak listener;

	private AsyncProcess() {};

    public AsyncProcess(String... cmd) {
		this.cmd = cmd;
		processBuilder =  new ProcessBuilder();
	}

	public AsyncProcess(List<String> cmd) {
		this.cmd = cmd.toArray(new String[0]);
	}

	public interface ProcessCallbak {
		void onProcessExit(int value);
		void onCommandOutputUpdate(String output);
	}

	
	public void redirectErrorStream(boolean yes) {
		processBuilder.redirectErrorStream(yes);
	}

	public void prepare() {
		processBuilder.command(cmd);
	}

	public void setProcessCallbak(ProcessCallbak listener) {
		this.listener = listener;
	}

	public void start() {
		prepare();
		try {
			commandProcess = processBuilder.start();
		} catch (IOException e) {}
		Thread thread = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						if (listener != null) {
							readOutput();
							listener.onProcessExit(commandProcess.exitValue());
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		thread.start();
	}

	public Process getProcess() {
		return commandProcess;
	}

	private void readOutput() {
		BufferedReader br = new BufferedReader(new InputStreamReader(commandProcess.getInputStream()));
		String temp;
		try {
			while ((temp = br.readLine()) != null) {
				// 拼接换行符
				if (listener != null) {
					listener.onCommandOutputUpdate(temp);
				}
			}
			br.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}


