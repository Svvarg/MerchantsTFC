package org.swarg.mcf.event;

import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.Cancelable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

/**
 * Для лавок в которых возможен обмен один-на-один
 * Для возможности вести статистику наторгованного игроками
 * 27-06-22
 * @author Swarg
 */
@Cancelable
public class TradeEvent extends Event {
    /*игрок который купил товар*/
    public final EntityPlayer buyer;
    /*имя продовца у которого был куплен товар*/
    public final String seller;
    /*товар который купил игрок у продовца*/
    public final ItemStack good;
    /*цена которую покупатель отдал за товар*/
    public final ItemStack pay;
    /*второй слот цены (для поддержки составной цены)*/
    public final ItemStack pay2;
    public final boolean npc;


    public TradeEvent(EntityPlayer buyer, String seller, ItemStack good, ItemStack pay) {
        this.buyer = buyer;
        this.seller = seller;
        this.good = good;
        this.pay = pay;
        this.pay2 = null;
        this.npc = false;
    }


    public TradeEvent(EntityPlayer buyer, String seller, ItemStack good, ItemStack pay, ItemStack pay2, boolean isNpc) {
        this.buyer = buyer;
        this.seller = seller;
        this.good = good;
        this.pay = pay;
        this.pay2 = pay2;
        this.npc = isNpc;
    }

    /**
     * Создать событие обмена и запустить его в шину для обработки
     * @param buyer
     * @param seller
     * @param pay
     * @param good
     * @return true - cancelled (отменить сделку)
     */
    public static boolean fireTradeEvent(EntityPlayer buyer, String seller, ItemStack good, ItemStack pay) {
        return fireTradeEvent(buyer, seller, good, pay, null, false);
    }

    public static boolean fireTradeEvent(EntityPlayer buyer, String seller, ItemStack good, ItemStack pay, ItemStack pay2, boolean isNpc) {
        if (buyer != null && pay != null && good != null) {
            TradeEvent event = new TradeEvent(buyer, seller, good, pay, pay2, isNpc);
            final boolean cancelled = net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(event);
            return cancelled;
        }
        return false;//NotCanceled!
    }

}
