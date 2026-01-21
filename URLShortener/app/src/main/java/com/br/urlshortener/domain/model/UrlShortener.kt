package com.br.urlshortener.domain.model

import java.net.URI
import java.security.MessageDigest

@JvmInline
value class UrlShortener private constructor(val url: String) {
    companion object {
        fun create(url: String): UrlShortener {
            val tinyUrl = shortenerUrl(url)
            return if (isValidUrl(url) && isValidUrl(tinyUrl)) {
                UrlShortener(url = tinyUrl)
            } else {
                throw IllegalArgumentException("Invalid URL format")
            }
        }

        fun createFromResult(url: String): UrlShortener = UrlShortener(url)

        private fun isValidUrl(url: String): Boolean {
            val urlRegex =
                "^(https?|ftp)://[\\w-]+(\\.[\\w-]+)+([\\w.,@?^=%&:/~+#-]*[\\w@?^=%&/~+#-])?$"
            return Regex(urlRegex).matches(url)
        }

        private fun shortenerUrl(url: String): String {
            val scheme = runCatching { URI(url).scheme }
                .getOrNull()
                ?.lowercase()
                ?: "https"
            val digest = MessageDigest.getInstance("SHA-256")
            val hashBytes = digest.digest(url.toByteArray(Charsets.UTF_8))
            val hashHex = hashBytes.joinToString("") { "%02x".format(it) }
            val domain = hashHex.substring(0, 8)
            val path = hashHex.substring(8, 16)
            return "$scheme://$domain.com/$path"
        }
    }
}
