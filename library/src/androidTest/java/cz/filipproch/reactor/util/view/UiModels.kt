package cz.filipproch.reactor.util.view

import cz.filipproch.reactor.base.view.UiModel

object TestUiModel : UiModel {
    override fun getType(): Class<*> {
        return TestUiModel::class.java
    }
}