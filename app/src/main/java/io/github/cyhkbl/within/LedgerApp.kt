package io.github.cyhkbl.within

import android.app.Application
import io.github.cyhkbl.within.data.LedgerRepository
import io.github.cyhkbl.within.data.db.LedgerDatabase
import io.github.cyhkbl.within.ui.theme.ThemePrefs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class LedgerApp : Application() {

    lateinit var repo: LedgerRepository
        private set

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        val db = LedgerDatabase.get(this)
        repo = LedgerRepository(db)
        AutoRecordGate.attach(this)
        ThemePrefs.init(this)
        appScope.launch {
            repo.seedIfNeeded()
            // 早先写入的退款记录被当成了收入，这里一次性纠正
            repo.normalizeRefundFlags()
            // 账户类型从「钱包/银行卡」迁到「微信/支付宝/工商银行」这类渠道
            repo.migrateAccountChannels()
        }
    }
}
