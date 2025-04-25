package org.example;

import org.eclipse.cdt.core.dom.ast.IASTNode;

public record DecoratedNode<T>(IASTNode declaration, T decoration) {
}
