package com.zilagent.app.data.model

import com.zilagent.app.data.entity.BellSchedule
import com.zilagent.app.data.entity.SyllabusEntry

data class TransferData(
    val profileName: String,
    val bellSchedules: List<BellSchedule>,
    val syllabusEntries: List<SyllabusEntry>
)
