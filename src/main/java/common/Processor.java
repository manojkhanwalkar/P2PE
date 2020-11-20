package common;

public interface Processor<T,R> {

    R process(T t);

}
