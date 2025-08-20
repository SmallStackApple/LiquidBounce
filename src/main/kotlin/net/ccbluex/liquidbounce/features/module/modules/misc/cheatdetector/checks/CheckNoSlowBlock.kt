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

import net.ccbluex.liquidbounce.event.events.PacketEvent
import net.ccbluex.liquidbounce.event.events.WorldChangeEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.event.sequenceHandler
import net.ccbluex.liquidbounce.features.module.modules.misc.cheatdetector.ModuleCheatDetector.flagFormat
import net.ccbluex.liquidbounce.utils.client.chat
import net.minecraft.entity.LivingEntity
import net.minecraft.item.Items
import net.minecraft.network.packet.s2c.play.EntityAnimationS2CPacket


data class PlayerState(val name: String, val blocked : Boolean, val attacked : Boolean)
data class PlayerVl(val name: String,val vl : Int,val time : Long)
object CheckNoSlowBlock : Check("NoSlowBlock") {

    override var resetVlTime by int("ResetVlTime",100,1..1000,"mins")
    private var playerStateList = mutableListOf<PlayerState>()
    private var playerVlList = mutableListOf<PlayerVl>()

    val Swords = arrayOf(Items.WOODEN_SWORD, Items.STONE_SWORD, Items.IRON_SWORD, Items.GOLDEN_SWORD, Items.DIAMOND_SWORD)

    val worldChangeHandler = handler<WorldChangeEvent> {
        playerStateList.clear()
        playerVlList.clear()
    }

    val attackHandler = sequenceHandler<PacketEvent> { event->
        if(enabled){
            val packet = event.packet
            if(packet is EntityAnimationS2CPacket
                && packet.animationId == 1
                && packet.entityId != player.id
                && world.getEntityById(packet.entityId).let{it as LivingEntity}.mainHandStack.item in Swords
                ){


            }
        }
    }

    override fun onEnabled() {
        playerVlList.clear()
        playerStateList.clear()
    }
    override fun onDisabled() {
        playerVlList.clear()
        playerStateList.clear()
    }


    fun flag(player:String,vl:Int) {
        //我操你妈怎么这么多行
        chat(flagFormat
            .replace("%p",player)
            .replace("%c","NoSlowBlock")
            .replace("%v",vl.toString())
        )
    }
}
