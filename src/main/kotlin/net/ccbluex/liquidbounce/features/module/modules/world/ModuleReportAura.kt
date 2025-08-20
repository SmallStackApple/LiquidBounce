/*
 * This file is part of LiquidBounce (https://github.com/CCBlueX/LiquidBounce)
 *
 * Copyright (c) 2015 - 2025 CCBlueX
 *
 * LiquidBounce is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LiquidBounce is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LiquidBounce. If not, see <https://www.gnu.org/licenses/>.
 */

package net.ccbluex.liquidbounce.features.module.modules.world

import net.ccbluex.liquidbounce.event.events.PacketEvent
import net.ccbluex.liquidbounce.event.sequenceHandler
import net.ccbluex.liquidbounce.features.module.ClientModule
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.utils.client.chat
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket

object ModuleReportAura : ClientModule("ReportAura", Category.WORLD) {

    val reportFormat by text("ReportFormat", "/report %p hacking AimBot")
    val reportDelay by int("ReportDelay", 60, 0..120,"s")
    //val ignoreTeammates by boolean("IgnoreTeammates", true)
    val thread = Thread(::reportThread)

    var playersToReport = mutableListOf<String>()

    @Suppress("unused")
    val packetHandler = sequenceHandler<PacketEvent> { event->
        val packet = event.packet

        if (packet !is PlayerListS2CPacket) return@sequenceHandler
        if (!packet.actions.contains(PlayerListS2CPacket.Action.ADD_PLAYER)) return@sequenceHandler

        playersToReport = network.playerList.filter {
            it.profile.name != mc.session.username
        }.map {
            it.profile.name
        }.toMutableList()
    }

    fun enable() {
        playersToReport.clear()
        thread.start()
    }

    fun disable() {
        playersToReport.clear()
        thread.interrupt()
    }

    private fun reportThread() {
        while (true) {
            reportPlayer(playersToReport.first())
            if (!playersToReport.isEmpty()) {
                playersToReport.removeFirst()
            }
            Thread.sleep(reportDelay * 1000L)
        }
    }

    private fun reportPlayer(playerName: String) {
        network.sendCommand(reportFormat.replace("%p", playerName))
        chat("Reported $playerName")
    }
}
