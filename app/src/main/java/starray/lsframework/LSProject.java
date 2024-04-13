package starray.lsframework;

import java.io.File;
import java.util.Map;
import starray.lsframework.server.Server;
import java.util.Collection;
import java.util.Collections;

public class LSProject {
    
    private String projectPath;
    private Map<String,Server> servers = Collections.emptyMap();
    
    public static final String TAG = "LSProject";
    /**
     * @param path 项目路径(目录)
     */
    public LSProject(String path) {
        this.projectPath = path;
    }
    
    public LSProject(File dir) {
        this.projectPath = dir.getAbsolutePath();
    }

    public void addSourceFile(String path) {
        
    }
    
    public void addServer(Server server,String suffix) {
        servers.put(suffix,server);
    }
    
    
}
