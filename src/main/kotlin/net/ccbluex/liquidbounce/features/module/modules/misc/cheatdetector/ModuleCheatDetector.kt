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

package net.ccbluex.liquidbounce.features.module.modules.misc.cheatdetector

import net.ccbluex.liquidbounce.event.events.WorldChangeEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.ClientModule
import net.ccbluex.liquidbounce.features.module.modules.misc.cheatdetector.checks.CheckAutoBlock
import net.ccbluex.liquidbounce.utils.client.chat

data class PlayerVl(var vl : Int, var time : Long)

object ModuleCheatDetector : ClientModule("CheatDetector", Category.MISC) {

    private val resetVlTime by int("ResetVlTime",500,1..2000,"s")
    val flagFormat by text("FlagFormat", "%p flagged %c (vl:%v)")

    private var playerVlMap = mutableMapOf<String,PlayerVl>()

    @Suppress("unused")
    val worldChangeHandler = handler<WorldChangeEvent>{
        playerVlMap.clear()
    }

    override fun onEnabled(){
        playerVlMap.clear()
    }

    override fun onDisabled() {
        playerVlMap.clear()
    }

    fun addVl(name: String, vl: Int){
        val playerVl = playerVlMap[name]
        if (playerVl != null){
            playerVl.vl += vl
            playerVl.time = System.currentTimeMillis()
        }else{
            playerVlMap.put(name,PlayerVl(vl, System.currentTimeMillis()))
        }
    }

    fun flag(player:String,check: String) {
        //我操你妈怎么这么多行
        playerVlMap[player]?.let {
            chat(flagFormat.replace("%p",player).replace("%c",check).replace("%v",it.vl.toString()))
        }
    }

    init{

        val checks = arrayOf(
            CheckAutoBlock,
        )

        checks.forEach {
            tree(it)
        }
    }
}
