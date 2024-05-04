package com.oopsmash.yasi;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.TextEdit;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Yasi {
    private final ASTParser parser;

    public Yasi() {
        this.parser = ASTParser.newParser(AST.JLS21);
    }

    private static Map<String, List<ImportDeclaration>> findAndRemoveImports(CompilationUnit unit, ASTRewrite rewrite) {
        Map<String, List<ImportDeclaration>> imports = new HashMap<>();
        for (Object object : unit.imports()) {
            if (object instanceof ImportDeclaration) {
                ImportDeclaration declaration = (ImportDeclaration) object;
                String name = getName(declaration);
                String key = String.format("%s%s", declaration.isStatic() ? "2" : "1", name.trim()); // first put
                                                                                                     // non-static ones
                imports.computeIfAbsent(key, k -> new ArrayList<>()).add(declaration);

                rewrite.remove(declaration, null);
            }
        }
        return imports;
    }

    private static String getName(ImportDeclaration declaration) {
        String name = declaration.getName().getFullyQualifiedName();
        String[] names = name.split("\\.");
        if (names.length > 0) {
            name = names[0];
        }
        return name.trim();
    }

    private static void rewriteImports(List<ImportDeclaration> imports, ASTRewrite rewrite, CompilationUnit unit) {
        imports.sort(Comparator.comparing(declaration -> declaration.getName().getFullyQualifiedName()));
        imports.forEach(declaration -> rewriteImport(rewrite, unit, declaration));
    }

    private static void rewriteImport(ASTRewrite rewrite, CompilationUnit unit, ASTNode node) {
        rewrite.getListRewrite(unit, CompilationUnit.IMPORTS_PROPERTY).insertLast(node, null);
    }

    private static void rewriteImportsMap(Map<String, List<ImportDeclaration>> imports, AST ast, ASTRewrite rewrite,
            CompilationUnit unit) {
        imports.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry -> {
            rewriteImports(entry.getValue(), rewrite, unit);
            rewriteImport(rewrite, unit, ast.newTextElement());
        });
    }

    private void setConfigs() {
        Map<String, String> options = JavaCore.getOptions();
        JavaCore.setComplianceOptions(JavaCore.VERSION_1_5, options);
        this.parser.setCompilerOptions(options);
        this.parser.setKind(ASTParser.K_COMPILATION_UNIT);
        this.parser.setResolveBindings(true);
        this.parser.setBindingsRecovery(true);
        this.parser.setStatementsRecovery(true);
        this.parser.setEnvironment(new String[] { /* classpath entries */ }, new String[] { /* source folders */ },
                null, true);
    }

    public String sort(String source) throws BadLocationException {
        if (source == null)
            return null;

        setConfigs();
        parser.setSource(source.toCharArray());

        CompilationUnit compilationUnit = (CompilationUnit) parser.createAST(null);
        compilationUnit.recordModifications();

        AST ast = compilationUnit.getAST();
        ASTRewrite rewrite = ASTRewrite.create(ast);

        Map<String, List<ImportDeclaration>> imports = findAndRemoveImports(compilationUnit, rewrite);
        rewriteImportsMap(imports, ast, rewrite, compilationUnit);

        Document document = new Document(source);
        TextEdit textEdit = rewrite.rewriteAST(document, null);
        textEdit.apply(document);

        return document.get();
    }
}