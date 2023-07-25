package com.example.example

import com.google.gson.annotations.SerializedName


data class SmsMarketingConsentmo (

  @SerializedName("state"                  ) var state                : String? = null,
  @SerializedName("opt_in_level"           ) var optInLevel           : String? = null,
  @SerializedName("consent_updated_at"     ) var consentUpdatedAt     : String? = null,
  @SerializedName("consent_collected_from" ) var consentCollectedFrom : String? = null

)