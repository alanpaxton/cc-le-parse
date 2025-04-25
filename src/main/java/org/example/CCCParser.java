package org.example;

import org.eclipse.cdt.core.dom.ast.IASTComment;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;

import java.io.File;
import java.util.List;

/**
 * {@link <a href="https://stackoverflow.com/questions/42066626/using-cdt-parser-out-of-eclipsehow-to-make-project">Using CDT parser out of Eclipse(How to make project?)</a>}
 */
public class CCCParser {
    public static void main(String[] args) throws Exception {

        portal();
    }

    private static void portal() throws Exception {
        File file = RepositoryConfiguration.portal.toFile();
        IASTTranslationUnit translationUnit = TranslationUnitBuilder.getIASTTranslationUnit(file);
        Transformer.DeclarationTransformer declarationTransformer = new Transformer.DeclarationTransformer(translationUnit);
        final List<DecoratedNode<List<IASTComment>>> declarationsWithComments = declarationTransformer.transform();
        declarationsWithComments.stream().forEach(node -> System.err.println(node));
    }

}