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
import net.ccbluex.liquidbounce.features.module.modules.misc.cheatdetector.ModuleCheatDetector.flagFormat
import net.ccbluex.liquidbounce.utils.client.chat
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Items
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket
import net.minecraft.network.packet.s2c.play.EntityAnimationS2CPacket
import net.minecraft.util.Hand
import javax.annotation.Nullable

//今天的编码就到这里吧
data class PlayerState(val name: String, var blocked : Boolean, var attacked : Boolean)

object CheckAutoBlock : Check("AutoBlock") {

    private var playerStateList = mutableListOf<PlayerState>()
    override var enabled: Boolean = super.enabled && ModuleCheatDetector.enabled

    val Swords = arrayOf(Items.WOODEN_SWORD, Items.STONE_SWORD, Items.IRON_SWORD, Items.GOLDEN_SWORD, Items.DIAMOND_SWORD)

    @Suppress("unused")
    val worldChangeHandler = handler<WorldChangeEvent> {
        playerStateList.clear()
        return@handler
    }

    @Suppress("unused")
    val attackAnimationPacketHandler = handler<PacketEvent> { event->
        if(enabled){
            val packet = event.packet
            //TMD狗屎判断代码写这么长
            //还有kotlin你他妈管那个null不null的干什么啊cnm
            //希望qwen3能帮一下我
            if(packet is EntityAnimationS2CPacket
                && packet.entityId != player.id
                && world.getEntityById(packet.entityId) is PlayerEntity
                && world.getEntityById(packet.entityId).let{it as PlayerEntity}.mainHandStack.item in Swords
                ){
                val animationPacket : EntityAnimationS2CPacket = packet
                val player = world.getEntityById(packet.entityId)
                if(player != null)
                {
                    if (animationPacket.animationId == EntityAnimationS2CPacket.SWING_MAIN_HAND) {
                        addPlayerState(player.name.string, null, true)
                    } else if (animationPacket.animationId == EntityAnimationS2CPacket.SWING_OFF_HAND) {
                        addPlayerState(player.name.string, true, null)
                    }
                }
            }
        }
        return@handler
    }

    var tick = 0
    @Suppress("unused")
    val tickHandler = handler<GameTickEvent> { event->
        if(enabled){
            if(tick==2){
                playerStateList.forEach {
                    if(it.blocked && it.attacked){
                        addVl(it.name,1)
                        flag(it.name,"AutoBlock")
                    }
                }
                tick = 0
                playerStateList.clear()
            }
            tick++
        }
        return@handler
    }




    //@Nullable@Nullable@Nullable@Nullable@Nullable
    //破防了Boolean?Boolean?Boolean?Boolean?Boolean?
    //Null cannot be a value of a non-null type 'Boolean'.Null cannot be a value of a non-null type 'Boolean'.
    //Fuck you kotlin Ij
    private fun addPlayerState(player: String, blocked: Boolean?, attacked: Boolean?) {
        val state = playerStateList.find{it.name == player}
        if (state != null){
            state.blocked = blocked ?: state.blocked
            state.attacked = attacked ?: state.attacked
        } else {
            playerStateList.add(PlayerState(player,blocked?:false,attacked?:false))
        }
    }

    override fun onEnabled() {
        playerStateList.clear()
    }
    override fun onDisabled() {
        playerStateList.clear()
    }
}
