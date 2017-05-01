package cz.filipproch.reactor.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.View
import cz.filipproch.reactor.base.translator.ReactorTranslator
import cz.filipproch.reactor.base.view.*
import cz.filipproch.reactor.ui.events.*
import io.reactivex.Observable
import io.reactivex.functions.Consumer
import io.reactivex.subjects.PublishSubject

/**
 * [Fragment] implementation of [ReactorView]
 *
 * @author Filip Prochazka (@filipproch)
 */
abstract class ReactorFragment<T : ReactorTranslator> :
        Fragment(),
        ReactorView<T> {

    private val TRANSLATOR_LOADER_ID = 1

    private lateinit var reactorViewHelper: ReactorViewHelper<T>

    private val activityEventsSubject = PublishSubject.create<ReactorUiEvent>()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if (savedInstanceState == null) {
            dispatch(ViewCreatedEvent)
        } else {
            dispatch(ViewRestoredEvent(savedInstanceState))
        }
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        reactorViewHelper = ReactorViewHelper(this)

        reactorViewHelper.onViewCreated()

        initUi()

        if (savedInstanceState != null) {
            onUiRestored(savedInstanceState)
        } else {
            onUiCreated()
        }

        onPostUiCreated()
    }

    @SuppressLint("CommitTransaction")
    @Suppress("UNCHECKED_CAST")
    override fun onStart() {
        super.onStart()

        var translatorFragment = childFragmentManager.findFragmentByTag(ReactorTranslatorFragment.TAG)
                as ReactorTranslatorFragment<T>?
        if (translatorFragment == null) {
            translatorFragment = ReactorTranslatorFragment()
            translatorFragment.setTranslatorFactory(translatorFactory)
            childFragmentManager.beginTransaction()
                    .add(translatorFragment, ReactorTranslatorFragment.TAG)
                    .commitNow()
        }

        reactorViewHelper.onTranslatorAttached(translatorFragment.translator!!)

        dispatch(ViewAttachedEvent)
        dispatch(ViewStartedEvent)
    }

    override fun onResume() {
        super.onResume()
        dispatch(ViewResumedEvent)
    }

    override fun onPause() {
        super.onPause()
        dispatch(ViewPausedEvent)
    }

    override fun onStop() {
        super.onStop()
        dispatch(ViewDetachedEvent)
        dispatch(ViewStoppedEvent)
    }

    override fun onDestroy() {
        super.onDestroy()
        dispatch(ViewDestroyedEvent)
        reactorViewHelper.onViewDestroyed()
    }

    override fun onEmittersInit() {
        registerEmitter(activityEventsSubject)
    }

    override fun onConnectModelChannel(modelStream: Observable<out ReactorUiModel>) {
    }

    override fun onConnectActionChannel(actionStream: Observable<out ReactorUiAction>) {
    }

    override fun dispatch(event: ReactorUiEvent) {
        activityEventsSubject.onNext(event)
    }

    override fun registerEmitter(emitter: Observable<out ReactorUiEvent>) {
        reactorViewHelper.registerEmitter(emitter)
    }

    override fun <T> receiveUpdatesOnUi(observable: Observable<T>, receiverAction: Consumer<T>) {
        reactorViewHelper.receiveUpdatesOnUi(observable, receiverAction)
    }

    @Deprecated("Replaced with extension function consumeOnUi", ReplaceWith(
            "receiver.consumeOnUi(action)"
    ))
    fun <T : ReactorUiModel> receiveUpdatesOnUi(receiver: Observable<T>, action: (T) -> Unit) {
        receiveUpdatesOnUi(receiver, Consumer<T> {
            action.invoke(it)
        })
    }

    override fun <T> Observable<T>.consumeOnUi(receiverAction: Consumer<T>) {
        reactorViewHelper.receiveUpdatesOnUi(this, receiverAction)
    }

    fun <T> Observable<T>.consumeOnUi(action: (T) -> Unit) {
        consumeOnUi(Consumer<T> {
            action.invoke(it)
        })
    }

    override fun <M : ReactorUiModel, T> Observable<M>.mapToUi(consumer: Consumer<T>, mapper: ConsumerMapper<M, T>) {
        reactorViewHelper.receiveUpdatesOnUi(this.map { mapper.mapModelToUi(it) }, consumer)
    }

    fun <M : ReactorUiModel, T> Observable<M>.mapToUi(consumer: Consumer<T>, mapper: (M) -> T) {
        this.mapToUi(consumer, object : ConsumerMapper<M, T> {
            override fun mapModelToUi(model: M): T {
                return mapper.invoke(model)
            }
        })
    }

    /*
        ReactorFragment specific
     */

    /**
     * Called from [onCreate]
     */
    @Deprecated("Due to ambiguous name replaced", ReplaceWith(
            "onPostUiCreated"
    ))
    open fun initUi() {
    }

    /**
     * Called from [onCreate] is savedInstanceState is null
     */
    open fun onUiCreated() {
    }

    /**
     * Called from [onCreate] is savedInstanceState is not null
     */
    open fun onUiRestored(savedInstanceState: Bundle) {
    }

    /**
     * Called from [onCreate] after either [onUiCreated] or [onUiRestored] has been called
     *
     * This method is useful to set [android.view.View] listeners or other stuff that doesn't survive activity recreation
     */
    open fun onPostUiCreated() {
    }

    @Deprecated("This method is not part of the Reactor architecture and was moved to the 'extras' module")
    open fun getLayoutResId(): Int {
        return -1
    }

    @Deprecated("This method is not part of the Reactor architecture and was moved to the 'extras' module")
    open fun bindViews(view: View) {
    }

}