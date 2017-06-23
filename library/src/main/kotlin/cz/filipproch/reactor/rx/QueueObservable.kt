package cz.filipproch.reactor.rx

import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.subjects.PublishSubject

/**
 * @author Filip Prochazka (@filipproch)
 */
class QueueObservable<T> private constructor(
        private val state: State<T>
) : Observable<T>() {

    fun emit(value: T) {
        if (state.subscriber.hasObservers()) {
            state.subscriber.onNext(value)
        } else {
            if (state.queue != null) {
                state.queue?.add(value)
            } else {
                state.queue = mutableListOf(value)
            }
        }
    }

    override fun subscribeActual(observer: Observer<in T>) {
        state.queue?.let {
            fromIterable(state.queue)
                    .doOnNext { observer.onNext(it) }
                    .doOnComplete { state.queue = null }
                    .subscribe()
        }

        state.subscriber.subscribe(observer)
    }

    private class State<T>(
            var queue: MutableList<T>? = null,
            val subscriber: PublishSubject<T> = PublishSubject.create()
    )

    companion object {
        fun <T> create(): QueueObservable<T> {
            return QueueObservable(
                    state = State()
            )
        }
    }

}