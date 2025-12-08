package io.payfunds.core

import java.util.Date

class CurrentDateProvider : ICurrentDateProvider {

    override val currentDate: Date
        get() = Date()

}
