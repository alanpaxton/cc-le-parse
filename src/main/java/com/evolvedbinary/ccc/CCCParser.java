package com.evolvedbinary.ccc;

import org.eclipse.cdt.core.dom.ast.IASTComment;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * {@link <a href="https://stackoverflow.com/questions/42066626/using-cdt-parser-out-of-eclipsehow-to-make-project">Using CDT parser out of Eclipse(How to make project?)</a>}
 */
public class CCCParser {
    public static void main(String[] args) throws Exception {

        portal();
    }

    /**
     * Process the <code>portal.h</code> file into a separate file for each declaration
     *
     * @throws Exception
     */
    private static void portal() throws Exception {
        File file = RepositoryConfiguration.portal.toFile();
        IASTTranslationUnit translationUnit = TranslationUnitBuilder.getIASTTranslationUnit(file);

        Transformer.DeclarationTransformer declarationTransformer = new Transformer.DeclarationTransformer(translationUnit);
        final List<DecoratedNode<List<IASTComment>>> declarationsWithComments = declarationTransformer.transform();

        final Boilerplate boilerplate = new Boilerplate();
        declarationsWithComments.forEach(node -> {
            StringBuilder sb = new StringBuilder();
            boilerplate.preamble(sb);
            node.buildString(sb);
            boilerplate.postamble(sb);

            Path path = Path.of("portal").resolve(Path.of(node.getDeclarationName() + ".h"));
            try {
                FileOutput fileOutput = new FileOutput(path, sb);
                fileOutput.write();
            } catch (IOException e) {
                throw new RuntimeException("Output file could not be written: " + path, e);
            }
        });
    }

}