package com.br.urlshortener.data.remote

import android.content.Context
import com.google.android.gms.safetynet.SafetyNet

/*
    https://developer.android.com/privacy-and-security/safetynet/safebrowsing
    https://developer.android.com/privacy-and-security/safetynet?hl=pt-br
    https://developers.google.com/safe-browsing/v4/get-started
    https://developers.google.com/safe-browsing/v4/lookup-api
 */
fun Context.checkUrlSafety(
    url: String,
    apiKey: String
) = SafetyNet.getClient(this)
    .lookupUri(url, apiKey)
    .addOnSuccessListener { response ->
        if (response.detectedThreats.isEmpty()) {
            println("URL is safe: $url")
        } else {
            println("URL is unsafe: $url, Threats: ${response.detectedThreats}")
        }
    }
    .addOnFailureListener { e ->
        println("Error checking URL safety: ${e.message}")
    }
