package cz.filipproch.reactor.extras.ui.views

import android.content.Intent
import android.view.View
import cz.filipproch.reactor.base.translator.ReactorTranslator
import cz.filipproch.reactor.base.view.ReactorUiAction
import cz.filipproch.reactor.base.view.ReactorUiModel
import cz.filipproch.reactor.extras.ui.views.actions.FinishActivityAction
import cz.filipproch.reactor.extras.ui.views.actions.FinishActivityWithResultAction
import cz.filipproch.reactor.extras.ui.views.actions.StartActivityAction
import cz.filipproch.reactor.extras.ui.views.actions.StartActivityForResultAction
import cz.filipproch.reactor.extras.ui.views.events.ActivityResultEvent
import cz.filipproch.reactor.extras.ui.views.model.ContentFragmentModel
import cz.filipproch.reactor.ui.ReactorActivity
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

/**
 * TODO: add description
 *
 * @author Filip Prochazka (@filipproch)
 */
@Deprecated("This class was renamed and moved to another package",
        ReplaceWith(
                "ExtendedReactorActivity<T>",
                "cz.filipproch.reactor.extras.ui.views.activity.ExtendedReactorActivity"
        ),
        DeprecationLevel.WARNING)
abstract class BaseReactorActivity<T : ReactorTranslator> : ReactorActivity<T>() {

    private val CONTENT_FRAGMENT_TAG = "content_fragment"

    val activityResultSubject: PublishSubject<ActivityResultEvent> = PublishSubject.create<ActivityResultEvent>()

    override fun onEmittersInit() {
        super.onEmittersInit()
        registerEmitter(activityResultSubject)
    }

    override fun onConnectActionChannel(actionStream: Observable<out ReactorUiAction>) {
        super.onConnectActionChannel(actionStream)
        actionStream.ofType(FinishActivityAction::class.java).consumeOnUi {
            finish()
        }

        actionStream.ofType(FinishActivityWithResultAction::class.java).consumeOnUi {
            setResult(it.resultCode)
            finish()
        }

        actionStream.ofType(StartActivityAction::class.java).consumeOnUi {
            startActivity(Intent(this, it.activity))
        }

        actionStream.ofType(StartActivityForResultAction::class.java).consumeOnUi {
            startActivityForResult(Intent(this, it.activity), it.requestCode)
        }
    }

    override fun onConnectModelChannel(modelStream: Observable<out ReactorUiModel>) {
        super.onConnectModelChannel(modelStream)
        modelStream.ofType(ContentFragmentModel::class.java).consumeOnUi {
            val contentView = getContentView()
            if (contentView != null) {
                val existing = supportFragmentManager.findFragmentByTag(CONTENT_FRAGMENT_TAG)
                if (existing != null && existing.javaClass == it.fragment?.javaClass) {
                    return@consumeOnUi
                }
                supportFragmentManager.beginTransaction()
                        .replace(contentView.id, it.fragment, CONTENT_FRAGMENT_TAG)
                        .commit()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        activityResultSubject.onNext(ActivityResultEvent(requestCode, resultCode, data))
    }

    open fun getContentView(): View? {
        return null
    }

}