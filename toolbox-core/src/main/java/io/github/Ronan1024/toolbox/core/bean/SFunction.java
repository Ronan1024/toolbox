package io.github.Ronan1024.toolbox.core.bean;

import java.io.Serializable;
import java.util.function.Function;

/**
 * @author L.J.Ran
 * @version 1.0
 */
@FunctionalInterface
public interface SFunction<T, R> extends Function<T, R>, Serializable {
}
