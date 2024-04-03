package com.example;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import android.util.Log;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ExtractAsset {

    public static final String TAG = "ExtractAsset";
	
    public static void unpackJDTLS(Context context) {
		final File jdtlsDir = new File(context.getFilesDir(), "jdtls");
		if (!jdtlsDir.exists()) {
			jdtlsDir.mkdir();
			exportAssets(context, "jdt-language-server-1.33.0-202402151717.tar", jdtlsDir.getAbsolutePath());
			exportAssets(context, "jdtls.sh", context.getFilesDir().getAbsolutePath());
			final String jdtlsPack = jdtlsDir.getAbsolutePath() + "/jdt-language-server-1.33.0-202402151717.tar";
			AsyncProcess p = new AsyncProcess("tar", "xvf", jdtlsPack, "-C", jdtlsDir.getAbsolutePath());
			p.setProcessCallbak(new AsyncProcess.ProcessCallbak() {
					public void onProcessExit(final int p) {
						try {
							Runtime.getRuntime().exec(new String[]{"rm",jdtlsPack});
						} catch (IOException e) {}
					}
					public void onCommandOutputUpdate(String output) {
					}
				});
			p.start();
            try {
                p.getProcess().waitFor();
            } catch (InterruptedException e) {}
		}
	}

	public static void unpackJDK(final Context context) {
		File jdkDir = new File(context.getFilesDir(), "jdk");
		if (!jdkDir.exists()) {
			jdkDir.mkdirs();
			exportAssets(context, "OpenJDK17-AJIDE.tar", jdkDir.getAbsolutePath());
			final String jdkPack = jdkDir.getAbsolutePath() + "/OpenJDK17-AJIDE.tar";
			String str[] = {
				"tar",
				"xvf",
				jdkPack,
				"-C",
				jdkDir.getAbsolutePath()
			};

			AsyncProcess p = new AsyncProcess(str);
			p.setProcessCallbak(new AsyncProcess.ProcessCallbak() {
					public void onProcessExit(final int p) {
						try {
							Runtime.getRuntime().exec(new String[]{"rm",jdkPack});
						} catch (IOException e) {}
					}
					public void onCommandOutputUpdate(String output) {
					}
				});
			p.start();
            try {
                p.getProcess().waitFor();
            } catch (InterruptedException e) {}
		}
	}
	
	public static void unpackTestProject(Context context) {
		File testProjectDir =new File(Environment.getExternalStorageDirectory(),"TestProject");
		if (testProjectDir.exists()) return;
		try {
			unzipFromAssets(context, "TestProject.zip", testProjectDir.getParent());
		} catch (IOException e) {}
	}

	public static void unzipFromAssets(Context context, String zipFileName, String outputDir) throws IOException {
		AssetManager assetManager = context.getAssets();
		InputStream is = assetManager.open(zipFileName);
		ZipInputStream zis = new ZipInputStream(is);
		ZipEntry entry;
		while ((entry = zis.getNextEntry()) != null) {
			String fileName = entry.getName();
			File newFile = new File(outputDir + File.separator + fileName);
			if (entry.isDirectory()) {
				newFile.mkdirs();
			} else {
				File parentFile = newFile.getParentFile();
				if (!parentFile.exists()) {
					parentFile.mkdirs();
				}
				OutputStream os = new FileOutputStream(newFile);
				byte[] buffer = new byte[1024];
				int len;
				while ((len = zis.read(buffer)) > 0) {
					os.write(buffer, 0, len);
				}
				os.close();
			}
		}
		zis.closeEntry();
		zis.close();
	}
	public static void exportAssets(Context context, String fileName,
									String outPath) {
		File outdir = new File(outPath);
		if (!outdir.exists()) {
			outdir.mkdirs();
		}
		try {
			InputStream inputStream = context.getAssets().open(fileName);
			File outFile = new File(outdir, fileName);
			if (outFile.exists()) {
				return;
			}
			FileOutputStream fileOutputStream = new FileOutputStream(outFile);
			byte[] buffer = new byte[1024];
			int byteRead;
			while (-1 != (byteRead = inputStream.read(buffer))) {
				fileOutputStream.write(buffer, 0, byteRead);
			}
			inputStream.close();
			fileOutputStream.flush();
			fileOutputStream.close();
		} catch (IOException e) {
			Log.e(TAG, Log.getStackTraceString(e));
		}
	}

}
