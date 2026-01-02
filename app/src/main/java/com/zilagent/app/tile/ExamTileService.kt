package com.zilagent.app.tile

import android.content.Intent
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.zilagent.app.MainActivity

class ExamTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        updateTile()
    }

    private fun updateTile() {
        val tile = qsTile ?: return
        tile.state = Tile.STATE_ACTIVE
        tile.label = "ZilAgent Sınav"
        tile.updateTile()
    }

    override fun onClick() {
        super.onClick()
        // Open the app to Exam Mode
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("START_EXAM_MODE", true)
        }
        
        // Start activity and collapse notification panel
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            // Need to handle Android 14+ pending intent if required, but simple startActivity often works for TileService
             val pendingIntent = android.app.PendingIntent.getActivity(
                this, 0, intent, 
                android.app.PendingIntent.FLAG_IMMUTABLE or android.app.PendingIntent.FLAG_UPDATE_CURRENT
            )
            // TileService.startActivityAndCollapse(PendingIntent) is the new way
            // But for simplicity and backward compatibility:
            startActivityAndCollapse(intent)
        } else {
            @Suppress("DEPRECATION")
            startActivityAndCollapse(intent)
        }
    }
}
