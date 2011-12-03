package jp.sourceforge.stigmata.utils;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * 
 * @author Haruaki Tamada
 */
public class ArrayIterator<T> implements Iterator<T>{
    private T[] values;
    private int length;
    private int currentIndex = 0;

    public ArrayIterator(T[] values){
        if(values != null){
            this.values = Arrays.copyOf(values, values.length);
            length = values.length;
        }
        else{
            length = 0;
        }
    }

    @Override
    public boolean hasNext(){
        return currentIndex < length;
    }

    @Override
    public T next(){
        if(currentIndex < 0 || currentIndex >= values.length){
            throw new NoSuchElementException();
        }
        T value = values[currentIndex];
        currentIndex++;
        return value;
    }

    @Override
    public void remove(){
    }
}
