package com.br.urlshortener.domain.model

@JvmInline
value class UrlShortener private constructor(val tinyUrl: String) {
    companion object {
        fun create(url: String): UrlShortener {
            val tinyUrl = tinyUrl(url)
            return if (isValidUrl(url) && isValidUrl(tinyUrl)) {
                UrlShortener(tinyUrl = tinyUrl)
            } else {
                throw IllegalArgumentException("Invalid URL format")
            }
        }

        private fun isValidUrl(url: String): Boolean {
            val urlRegex =
                "^(https?|ftp)://[\\w-]+(\\.[\\w-]+)+([\\w.,@?^=%&:/~+#-]*[\\w@?^=%&/~+#-])?$"
            return Regex(urlRegex).matches(url)
        }

        private fun tinyUrl(url: String): String {
            return url
        }
    }
}
