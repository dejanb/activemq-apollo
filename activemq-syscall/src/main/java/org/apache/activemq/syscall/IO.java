/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.activemq.syscall;

import org.fusesource.hawtjni.runtime.JniClass;
import org.fusesource.hawtjni.runtime.JniField;
import org.fusesource.hawtjni.runtime.JniMethod;

import static org.fusesource.hawtjni.runtime.MethodFlag.*;

import static org.fusesource.hawtjni.runtime.FieldFlag.CONSTANT;

/**
 * 
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
@JniClass
public class IO {

    @JniMethod(flags={CONSTANT_INITIALIZER})
    private static final native void init();
    static {
        CLibrary.LIBRARY.load();
        init();
    }

    @JniField(flags={CONSTANT})
    public static int O_RDONLY;
    @JniField(flags={CONSTANT})
    public static int O_WRONLY;
    @JniField(flags={CONSTANT})
    public static int O_RDWR;
    @JniField(flags={CONSTANT})
    public static int O_NONBLOCK;
    @JniField(flags={CONSTANT})
    public static int O_APPEND;
    @JniField(flags={CONSTANT})
    public static int O_CREAT;
    @JniField(flags={CONSTANT})
    public static int O_TRUNC;
    @JniField(flags={CONSTANT})
    public static int O_EXCL;
    @JniField(flags={CONSTANT})
    public static int O_ASYNC;

    @JniField(flags={CONSTANT})
    public static int S_IRWXU;
    @JniField(flags={CONSTANT})
    public static int S_IRUSR;
    @JniField(flags={CONSTANT})
    public static int S_IWUSR;
    @JniField(flags={CONSTANT})
    public static int S_IXUSR;
            
    @JniField(flags={CONSTANT})
    public static int S_IRWXG;
    @JniField(flags={CONSTANT})
    public static int S_IRGRP;
    @JniField(flags={CONSTANT})
    public static int S_IWGRP;
    @JniField(flags={CONSTANT})
    public static int S_IXGRP;
            
    @JniField(flags={CONSTANT})
    public static int S_IRWXO;
    @JniField(flags={CONSTANT})
    public static int S_IROTH;
    @JniField(flags={CONSTANT})
    public static int S_IWOTH;
    @JniField(flags={CONSTANT})
    public static int S_IXOTH;
            
    @JniField(flags={CONSTANT})
    public static int S_ISUID;
    @JniField(flags={CONSTANT})
    public static int S_ISGID;
    @JniField(flags={CONSTANT})
    public static int S_ISVTX;
    
    @JniField(flags={CONSTANT})
    public static int F_DUPFD;
    @JniField(flags={CONSTANT})
    public static int F_GETFD;
    @JniField(flags={CONSTANT})
    public static int F_SETFD;
    @JniField(flags={CONSTANT})
    public static int F_GETFL;
    @JniField(flags={CONSTANT})
    public static int F_SETFL;
    @JniField(flags={CONSTANT})
    public static int F_GETOWN;
    @JniField(flags={CONSTANT})
    public static int F_SETOWN;
    @JniField(flags={CONSTANT})
    public static int F_GETLK;
    @JniField(flags={CONSTANT})
    public static int F_SETLK;
    @JniField(flags={CONSTANT})
    public static int F_SETLKW;
    
    ///////////////////////////////////////////////////////////////////
    //
    // IO related methods 
    //
    ///////////////////////////////////////////////////////////////////
    /**
     * <code><pre>
     * int open(const char *path, int oflags, ...);
     * </pre></code>
     */
    public static final native int open(String path, int oflags);
    
    /**
     * <code><pre>
     * int open(const char *path, int oflags, ...);
     * </pre></code>
     */
    public static final native int open(String path, int oflags, int mode);

    /**
     * <code><pre>
     * int close(int fd);
     * </pre></code>
     */
    public static final native int close(int fd);

    /**
     * <code><pre>
     * int fcntl(int fd, int cmd, ...);
     * </pre></code>
     */
    public static final native int fcntl(int fd, int cmd);
        
    

}