package com.ghostipedia.cosmiccore.common.compat.gtceu.ae2;

import java.util.AbstractList;
import java.util.List;
import java.util.function.Supplier;

public final class MEStorageDisplayView<E> extends AbstractList<E> {

    private final Supplier<List<E>> source;

    public MEStorageDisplayView(Supplier<List<E>> source) {
        this.source = source;
    }

    @Override
    public E get(int index) {
        return source.get().get(index);
    }

    @Override
    public int size() {
        return source.get().size();
    }
}
