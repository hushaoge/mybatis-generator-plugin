package com.solar.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiUtilBase;
import com.solar.action.callback.CustomShellCallback;
import com.sun.deploy.association.utility.AppConstants;
import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.config.*;
import org.mybatis.generator.config.xml.ConfigurationParser;
import org.mybatis.generator.internal.DefaultShellCallback;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author hushaoge
 * @date 2018/8/21 16:33
 */
public class MybatisGenerator extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent event) {
        Project project = event.getData(PlatformDataKeys.PROJECT);
        String basePath = project.getBasePath();
        try {
            List<String> warnings = new ArrayList<String>();
            File configFile = new File(basePath + "/empi-business/generatorConfig.xml");
            ConfigurationParser cp = new ConfigurationParser(warnings);
            Configuration config = cp.parseConfiguration(configFile);
            Context context = config.getContexts().get(0);
            // 自定义callback，支持java合并
            CustomShellCallback callback = new CustomShellCallback( true);
            String javaMergeSupported = context.getProperty("javaMergeSupported");
            callback.setMergeSupported(Boolean.parseBoolean(javaMergeSupported));

            JavaModelGeneratorConfiguration modelGeneratorConfiguration = context.getJavaModelGeneratorConfiguration();
            modelGeneratorConfiguration.setTargetProject(basePath + "/" + modelGeneratorConfiguration.getTargetProject());

            SqlMapGeneratorConfiguration mapGeneratorConfiguration  = context.getSqlMapGeneratorConfiguration();
            mapGeneratorConfiguration.setTargetProject(basePath + "/" + mapGeneratorConfiguration.getTargetProject());

            JavaClientGeneratorConfiguration clientGeneratorConfiguration = context.getJavaClientGeneratorConfiguration();
            clientGeneratorConfiguration.setTargetProject(basePath + "/" + clientGeneratorConfiguration.getTargetProject());

            MyBatisGenerator myBatisGenerator = new MyBatisGenerator(config, callback, warnings);
            myBatisGenerator.generate(null);
            Messages.showMessageDialog(project, "生成成功,请刷新目录.", "Information", Messages.getInformationIcon());
        } catch (FileNotFoundException fnfe) {
            String message = basePath + "/empi-business/generatorConfig.xml 不存在!";
            Messages.showMessageDialog(project, message, "Information", Messages.getInformationIcon());
        }catch (Exception e) {
            String message = "发生异常:"+ e.getMessage();
            Messages.showMessageDialog(project, message, "Information", Messages.getInformationIcon());
        }

    }


}
