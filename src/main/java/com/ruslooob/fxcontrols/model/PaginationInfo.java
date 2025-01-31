package com.ruslooob.fxcontrols.model;

import javafx.collections.ObservableList;

import java.util.function.BiFunction;
import java.util.function.Function;

public class PaginationInfo<T, S> {
    //id getter
    private final Function<T, S> idFunction;
    //(pageSize, lastId) -> List<TableRow> triggers, when user moves to next page
    private final BiFunction<Integer, S, ObservableList<T>> nextPageFunction;
    //(pageSize, lastId) -> List<TableRow> triggers, when user moves to prev page
    private final BiFunction<Integer, S, ObservableList<T>> prevPageFunction;

    public PaginationInfo(Function<T, S> idFunction, BiFunction<Integer, S, ObservableList<T>> nextPageFunction, BiFunction<Integer, S, ObservableList<T>> prevPageFunction) {
        this.idFunction = idFunction;
        this.nextPageFunction = nextPageFunction;
        this.prevPageFunction = prevPageFunction;
    }

    public Function<T, S> getIdFunction() {
        return idFunction;
    }

    public BiFunction<Integer, S, ObservableList<T>> getNextPageFunction() {
        return nextPageFunction;
    }

    public BiFunction<Integer, S, ObservableList<T>> getPrevPageFunction() {
        return prevPageFunction;
    }
}