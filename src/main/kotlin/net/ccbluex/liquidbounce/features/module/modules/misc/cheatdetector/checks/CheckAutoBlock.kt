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

package net.ccbluex.liquidbounce.features.module.modules.misc.cheatdetector.checks

import net.ccbluex.liquidbounce.event.events.GameTickEvent
import net.ccbluex.liquidbounce.event.events.PacketEvent
import net.ccbluex.liquidbounce.event.events.WorldChangeEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.modules.misc.cheatdetector.ModuleCheatDetector
import net.ccbluex.liquidbounce.features.module.modules.misc.cheatdetector.ModuleCheatDetector.addVl
import net.ccbluex.liquidbounce.features.module.modules.misc.cheatdetector.ModuleCheatDetector.flag
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Items
import net.minecraft.network.packet.s2c.play.EntityAnimationS2CPacket

data class PlayerState(var blocked : Boolean, var attacked : Boolean)

object CheckAutoBlock : Check("AutoBlock") {

    private var playerStateMap = mutableMapOf<String,PlayerState>()
    override var enabled: Boolean = super.enabled && ModuleCheatDetector.enabled

    val Swords = setOf(
        Items.WOODEN_SWORD,
        Items.STONE_SWORD,
        Items.IRON_SWORD,
        Items.GOLDEN_SWORD,
        Items.DIAMOND_SWORD
    )

    @Suppress("unused")
    val worldChangeHandler = handler<WorldChangeEvent> {
        playerStateMap.clear()
        return@handler
    }
    //TMD狗屎判断代码写这么长
    //还有kotlin你他妈管那个null不null的干什么啊cnm
    //希望qwen3能帮一下我
    //嘿嘿它帮我了
    @Suppress("unused")
    val attackAnimationPacketHandler = handler<PacketEvent> { event ->
        if (!enabled || event.packet !is EntityAnimationS2CPacket) return@handler

        val packet = event.packet
        val playerEntity = world.getEntityById(packet.entityId) as? PlayerEntity
            ?: return@handler

        // 提前过滤非剑类武器
        if (playerEntity.mainHandStack.item !in Swords) return@handler

        // 统一处理攻击动画
        when (packet.animationId) {
            EntityAnimationS2CPacket.SWING_MAIN_HAND ->
                updatePlayerState(playerEntity.name.string, attacked = true)
            EntityAnimationS2CPacket.SWING_OFF_HAND ->
                updatePlayerState(playerEntity.name.string, blocked = true)
        }
    }

    var tick = 0
    @Suppress("unused")
    val tickHandler = handler<GameTickEvent> { event->
        if(enabled){
            if(tick==2){
                playerStateMap.entries.removeIf { (name, state) ->
                    if (state.blocked && state.attacked) {
                        addVl(name, 1)
                        flag(name, "AutoBlock")
                        true // 移除已处理项
                    } else {
                        false // 保留未完成项
                    }
                }
            }
            tick++
        }
        return@handler
    }




    //@Nullable@Nullable@Nullable@Nullable@Nullable
    //破防了Boolean?Boolean?Boolean?Boolean?Boolean?
    //Null cannot be a value of a non-null type 'Boolean'.Null cannot be a value of a non-null type 'Boolean'.
    //Fuck you kotlin Ij
    private fun updatePlayerState(playerName: String, blocked: Boolean = false, attacked: Boolean = false) {
        val state = playerStateMap.getOrPut(playerName) {
            PlayerState(
                blocked = false,
                attacked = false
            )
        }

        if (blocked) state.blocked = true
        if (attacked) state.attacked = true
    }

    override fun onEnabled() {
        playerStateMap.clear()
    }
    override fun onDisabled() {
        playerStateMap.clear()
    }
}
