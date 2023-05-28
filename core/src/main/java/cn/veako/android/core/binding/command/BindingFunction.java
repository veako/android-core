package cn.veako.android.core.binding.command;

public interface BindingFunction<T, R> {
    R apply(T t);
}