package io.payfunds.core.security

import io.payfunds.core.IEncryptionManager
import io.payfunds.core.IKeyProvider

class EncryptionManager(private val keyProvider: IKeyProvider) : IEncryptionManager {

    @Synchronized
    override fun encrypt(data: String): String {
        return CipherWrapper().encrypt(data, keyProvider.getKey())
    }

    @Synchronized
    override fun decrypt(data: String): String {
        return CipherWrapper().decrypt(data, keyProvider.getKey())
    }
}
