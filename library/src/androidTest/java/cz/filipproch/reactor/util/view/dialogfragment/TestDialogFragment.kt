package cz.filipproch.reactor.util.view.dialogfragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cz.filipproch.reactor.base.translator.SimpleTranslatorFactory
import cz.filipproch.reactor.base.translator.TranslatorFactory
import cz.filipproch.reactor.base.view.ReactorUiAction
import cz.filipproch.reactor.base.view.UiModel
import cz.filipproch.reactor.ui.ReactorDialogFragment
import cz.filipproch.reactor.util.view.ReactorViewTestHelper
import cz.filipproch.reactor.util.view.TestTranslator
import io.reactivex.Observable

/**
 * TODO
 *
 * @author Filip Prochazka (@filipproch)
 */
class TestDialogFragment : ReactorDialogFragment<TestTranslator>() {

    val helper = ReactorViewTestHelper()

    override val translatorFactory: TranslatorFactory<TestTranslator>
        get() = SimpleTranslatorFactory(TestTranslator::class.java)

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return View(context)
    }

    override fun onUiCreated() {
        super.onUiCreated()
        methodCalled(METHOD_UI_CREATED)
    }

    override fun onUiRestored(savedInstanceState: Bundle) {
        super.onUiRestored(savedInstanceState)
        methodCalled(METHOD_UI_RESTORED)
    }

    override fun onUiReady() {
        super.onUiReady()
        methodCalled(METHOD_UI_READY)
    }

    override fun onEmittersInit() {
        super.onEmittersInit()
        methodCalled(ReactorViewTestHelper.METHOD_EMITTERS_INIT)
    }

    override fun onConnectModelStream(modelStream: Observable<out UiModel>) {
        super.onConnectModelStream(modelStream)
        methodCalled(ReactorViewTestHelper.METHOD_CONNECT_MODEL_STREAM)
        modelStream.consumeOnUi {
            helper.uiModelReceived(it)
        }
    }

    override fun onConnectActionStream(actionStream: Observable<out ReactorUiAction>) {
        super.onConnectActionStream(actionStream)
        methodCalled(ReactorViewTestHelper.METHOD_CONNECT_ACTION_STREAM)
        actionStream.consumeOnUi {
            helper.uiActionReceived(it)
        }
    }

    private fun methodCalled(methodName: String) {
        helper.methodCalled(methodName)
    }

    companion object {
        val METHOD_UI_CREATED = "onUiCreated"
        val METHOD_UI_RESTORED = "onUiRestored"
        val METHOD_UI_READY = "onUiReady"
    }

}