package org.reactorx.view.events

import io.reactivex.Observable
import org.reactorx.view.model.UiEvent

/**
 * @author Filip Prochazka (@filipproch)
 */
object ViewStopped : UiEvent

fun <T> Observable<T>.takeUntilViewStopped(events: Observable<UiEvent>): Observable<T> {
    return takeUntil(events.ofType(ViewStopped::class.java))
}