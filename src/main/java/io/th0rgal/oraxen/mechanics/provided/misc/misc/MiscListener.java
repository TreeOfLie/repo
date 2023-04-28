package io.th0rgal.oraxen.mechanics.provided.misc.misc;

import io.th0rgal.oraxen.api.OraxenItems;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Piglin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

import static org.bukkit.event.entity.EntityDamageEvent.DamageCause.FIRE;
import static org.bukkit.event.entity.EntityDamageEvent.DamageCause.FIRE_TICK;

public class MiscListener implements Listener {

    private final MiscMechanicFactory factory;

    public MiscListener(MiscMechanicFactory factory) {
        this.factory = factory;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPiglinAggro(EntityTargetLivingEntityEvent event) {
        if (!(event.getEntity() instanceof Piglin)) return;
        if (!(event.getTarget() instanceof Player player)) return;

        EntityEquipment equipment = player.getEquipment();
        if (equipment == null) return;

        if (shouldPreventPiglinAggro(equipment.getItemInMainHand()))
            event.setCancelled(true);
        if (shouldPreventPiglinAggro(equipment.getItemInOffHand()))
            event.setCancelled(true);
        if (Arrays.stream(equipment.getArmorContents()).anyMatch(this::shouldPreventPiglinAggro))
            event.setCancelled(true);
    }

    // Since EntityDamageByBlockEvent apparently does not trigger for fire, use this aswell
    @EventHandler
    public void onItemBurnFire(EntityDamageEvent event) {
        if (event.getCause() != FIRE && event.getCause() != FIRE_TICK) return;
        MiscMechanic mechanic = getMiscMechanic(event.getEntity());
        if (mechanic == null || mechanic.burnsInFire()) return;
        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onItemBurn(EntityDamageByBlockEvent event) {
        MiscMechanic mechanic = getMiscMechanic(event.getEntity());
        if (mechanic == null) return;

        switch (event.getCause()) {
            case CONTACT: if (!mechanic.breaksFromCactus()) event.setCancelled(true);
            case LAVA: if (!mechanic.burnsInLava()) event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDisableVanillaInteraction(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        MiscMechanic mechanic = (MiscMechanic) factory.getMechanic(OraxenItems.getIdByItem(event.getItem()));
        if (mechanic == null || !mechanic.isVanillaInteractionDisabled()) return;
        event.setCancelled(true);
    }

    private MiscMechanic getMiscMechanic(Entity entity) {
        if (!(entity instanceof Item item)) return null;
        ItemStack itemStack = item.getItemStack();
        String itemID = OraxenItems.getIdByItem(itemStack);
        if (itemID == null) return null;

        return (MiscMechanic) factory.getMechanic(itemID);
    }

    private boolean shouldPreventPiglinAggro(ItemStack itemStack) {
        MiscMechanic mechanic = (MiscMechanic) factory.getMechanic(OraxenItems.getIdByItem(itemStack));
        return mechanic != null && mechanic.piglinIgnoreWhenEquipped();
    }
}
