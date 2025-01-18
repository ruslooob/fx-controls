package com.ruslooob.fxcontrols.model;

import javafx.collections.ObservableList;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * @param idFunction       id getter
 * @param nextPageFunction (pageSize, lastId) -> List<TableRow> triggers, when user moves to next page
 * @param prevPageFunction (pageSize, lastId) -> List<TableRow> triggers, when user moves to prev page
 */
public record PaginationInfo<T, S>(Function<T, S> idFunction,
                                   BiFunction<Integer, S, ObservableList<T>> nextPageFunction,
                                   BiFunction<Integer, S, ObservableList<T>> prevPageFunction) {
}