package guess.domain.function;

@FunctionalInterface
public interface QuintFunction<T, U, V, W, X, R> {
    R apply(T t, U u, V v, W w, X x);
}
