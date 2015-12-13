package com.example.jwbauer3.mapsourcing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Keeps a sorted list of elements
 */
public class SortedArrayList<T> extends ArrayList<T> {

    private Comparator<? super T> comparator;

    public SortedArrayList(Comparator<T> comparator) {
        super();
        this.comparator = comparator;
    }

    public SortedArrayList(int capacity, Comparator<T> comparator) {
        super(capacity);
        this.comparator = comparator;
    }

    public void addSorted(T value) {
        super.add(value);
        for (int i = (size() - 1); i > 0; i--) {
            if(comparator.compare(value, this.get(i - 1)) > 0){
                break;
            }
            Collections.swap(this, i, i - 1);
        }
    }

    public void addAllSorted(List<? extends T> collection) {
        super.addAll(collection);
        Collections.sort(this, this.comparator);
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    //NOTE: The below functions don't make sense for a SortedList, so they are unsupported.
    //////////////////////////////////////////////////////////////////////////////////////////

    @Deprecated
    public boolean add(T object) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    public void add(int index, T object) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    public boolean addAll(Collection<? extends T> collection) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    public boolean addAll(int index, Collection<? extends T> collection) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    public T set(int index, T object) {
        //NOTE: Cannot throw UnsupportedOperationException because Collections.sort uses this
        return super.set(index, object);
    }
}
