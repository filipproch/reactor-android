package org.reactorx.view.events

import io.reactivex.Observable
import org.reactorx.view.model.UiEvent

/**
 * @author Filip Prochazka (@filipproch)
 */
object ViewStarted : UiEvent

fun Observable<UiEvent>.whenViewStarted(): Observable<ViewStarted> {
    return ofType(ViewStarted::class.java)
}

fun <T> Observable<UiEvent>.whenViewStartedFlatMap(
        mapper: (UiEvent) -> Observable<T>
): Observable<T> {
    return whenViewStarted()
            .flatMap { mapper.invoke(it) }
}