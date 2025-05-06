package com.evolvedbinary.ccc;

import org.eclipse.cdt.core.dom.ast.IASTNode;

import java.util.List;

public record DecoratedNode<T>(IASTNode declaration, T decoration) {
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        buildString(sb);
        return sb.toString();
    }

    /**
     * Append the stringification of this object to the supplied StringBuilder
     *
     * @param sb StringBuilder to append to
     * @return the original StringBuilder, with this object appended
     */
    public void buildString(final StringBuilder sb) {
        if (decoration instanceof List<?> decorationList) {
            for (Object item : decorationList) {
                sb.append(item).append('\n');
            }
        } else {
            sb.append(decoration);
        }
        sb.append(declaration.getRawSignature());
    }

    public String getDeclarationClassName() {
        return Naming.nameOfDeclaration(declaration);
    }
}
