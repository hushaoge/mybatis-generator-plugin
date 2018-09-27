package com.solar.action.callback;

import com.solar.action.merger.JavaFileMergerJaxp;
import org.mybatis.generator.exception.ShellException;
import org.mybatis.generator.internal.DefaultShellCallback;

import java.io.FileNotFoundException;

/**
 * @author hushaoge
 * @date 2018/8/22 17:04
 */
public class CustomShellCallback extends DefaultShellCallback {

    private boolean isMergeSupported;

    public CustomShellCallback(boolean overwrite) {
        super(overwrite);
    }

    @Override
    public boolean isMergeSupported() {
        return this.isMergeSupported;
    }

    public void setMergeSupported(boolean mergeSupported) {
        this.isMergeSupported = mergeSupported;
    }

    @Override
    public String mergeJavaFile(String newFileSource, String existingFileFullPath, String[] javadocTags, String fileEncoding) throws ShellException {
        JavaFileMergerJaxp javaFileMergerJaxp = new JavaFileMergerJaxp();
        try {
            return javaFileMergerJaxp.getNewJavaFile(newFileSource, existingFileFullPath);
        } catch (FileNotFoundException e) {
            throw new ShellException(e.getMessage(), e.getCause());
        }
    }

}
