package starray.lsframework.server;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public abstract class Server {

    abstract InputStream getInputStream() throws IOException;
    abstract OutputStream getOutputStream() throws IOException;
    abstract void start() throws IOException;
    abstract void exit() throws IOException;
    
    public static Server fromProcessBuilder(final ProcessBuilder builder) {
        return new Server() {
            private Process process;
            @Override
            public InputStream getInputStream() {
                return process.getInputStream();
            }
            
            public OutputStream getOutputStream() {
                return process.getOutputStream();
            }
            
            public void start() throws IOException {
                process = builder.start();
            }
            
            public void exit() {
                process.destroy();
            }
        };
    }
    
    public static Server fromServerSocket(final ServerSocket server) {
        return new Server() {
            private Socket socket;
            @Override
            public InputStream getInputStream() throws IOException {
                return socket.getInputStream();
            }

            public OutputStream getOutputStream() throws IOException {
                return socket.getOutputStream();
            }

            public void start() throws IOException {
                socket = server.accept();
            }

            public void exit() throws IOException {
                socket.close();
            }
        };
    }
}
