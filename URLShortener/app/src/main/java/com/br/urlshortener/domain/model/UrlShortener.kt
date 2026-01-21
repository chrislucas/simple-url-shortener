package com.br.urlshortener.domain.model

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
            return url
        }
    }
}
