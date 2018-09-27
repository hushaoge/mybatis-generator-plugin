package com.solar.action.merger;

import com.github.javaparser.JavaParser;
import com.github.javaparser.TokenRange;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.comments.Comment;
import org.mybatis.generator.config.MergeConstants;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

import static org.mybatis.generator.api.dom.OutputUtilities.javaIndent;
import static org.mybatis.generator.api.dom.OutputUtilities.newLine;

/**
 * @author hushaoge
 * @date 2018/8/22 13:40
 */
public class JavaFileMergerJaxp {
    public String getNewJavaFile(String newFileSource, String existingFileFullPath) throws FileNotFoundException {
        CompilationUnit newCompilationUnit = JavaParser.parse(newFileSource);
        CompilationUnit existingCompilationUnit = JavaParser.parse(new File(existingFileFullPath));
        return mergerFile(newCompilationUnit, existingCompilationUnit);
    }

    public String mergerFile(CompilationUnit newCompilationUnit, CompilationUnit existingCompilationUnit) {

        StringBuilder sb = new StringBuilder(newCompilationUnit.getPackageDeclaration().get().toString());
        newCompilationUnit.removePackageDeclaration();

        //合并imports
        NodeList<ImportDeclaration> imports = newCompilationUnit.getImports();
        imports.addAll(existingCompilationUnit.getImports());
        Set importSet = new HashSet<ImportDeclaration>();
        importSet.addAll(imports);

        NodeList<ImportDeclaration> newImports = new NodeList<>();
        newImports.addAll(importSet);
        newCompilationUnit.setImports(newImports);
        for (ImportDeclaration i : newCompilationUnit.getImports()) {
            sb.append(i.toString());
        }
        newLine(sb);
        NodeList<TypeDeclaration<?>> types = newCompilationUnit.getTypes();
        NodeList<TypeDeclaration<?>> oldTypes = existingCompilationUnit.getTypes();

        for (int i = 0; i < types.size(); i++) {
            TypeDeclaration type = types.get(i);
            //截取Class
            String classNameInfo = type.toString().substring(0, type.toString().indexOf("{") + 1);
            sb.append(classNameInfo);
            newLine(sb);
            newLine(sb);
            //合并fields
            List<FieldDeclaration> fields = type.getFields();
            List<FieldDeclaration> oldFields = oldTypes.get(i).getFields();
            Map<String, FieldDeclaration> newFieldsMap = new HashMap<>();

            for(FieldDeclaration fieldDeclaration : oldFields){
                VariableDeclarator variable = fieldDeclaration.getVariable(0);
                newFieldsMap.put(variable.getName().asString() ,fieldDeclaration);
            }

            for(FieldDeclaration fieldDeclaration : fields){
                VariableDeclarator variable = fieldDeclaration.getVariable(0);
                newFieldsMap.put(variable.getName().asString() ,fieldDeclaration);
            }

            for(Map.Entry<String, FieldDeclaration> entry : newFieldsMap.entrySet()){
                FieldDeclaration f = entry.getValue();
                javaIndent(sb, 1);
                sb.append(format(f.getComment(), f.getTokenRange()));
                newLine(sb);
                newLine(sb);
            }

            //合并methods
            List<MethodDeclaration> methods = types.get(i).getMethods();
            List<MethodDeclaration> existingMethods = oldTypes.get(i).getMethods();
            for (MethodDeclaration f : methods) {
                javaIndent(sb, 1);
                sb.append(format(f.getComment(), f.getTokenRange()));
                newLine(sb);
                newLine(sb);
            }
            for (MethodDeclaration m : existingMethods) {
                boolean flag = true;
                for (String tag : MergeConstants.OLD_ELEMENT_TAGS) {
                    if (m.toString().contains(tag)) {
                        flag = false;
                        break;
                    }
                }
                if (flag) {
                    javaIndent(sb, 1);
                    sb.append(format(m.getComment(), m.getTokenRange()));
                    newLine(sb);
                    newLine(sb);
                }
            }

            //判断是否有内部类
            types.get(i).getChildNodes();
            for (Node n : types.get(i).getChildNodes()) {
                if (n.toString().contains("static class")) {
                    newLine(sb);
                    javaIndent(sb, 1);
                    sb.append(format(n.getComment(), n.getTokenRange()));
                }
            }

        }

        return sb.append(System.getProperty("line.separator") + "}").toString();
    }

    /**
     * 缩进格式需要自己处理
     * @param comment
     * @param tokenRange
     * @return
     */
    private StringBuilder format( Optional<Comment> comment, Optional<TokenRange> tokenRange){
        StringBuilder tsb = new StringBuilder();
        if(comment.isPresent()){
            tsb.append(comment.get().toString());
            javaIndent(tsb, 1);
        }
        tsb.append(tokenRange.get().toString());
        return tsb;
    }
}
