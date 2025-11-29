package net.countercraft.movecraft.listener;

import net.countercraft.movecraft.Movecraft;
import net.countercraft.movecraft.MovecraftLocation;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.PlayerCraft;
import net.countercraft.movecraft.events.CraftRotateEvent;
import net.countercraft.movecraft.util.MathUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

public final class RotateListener implements Listener {
    @EventHandler(priority = EventPriority.LOWEST)
    public void onCraftRotate(@NotNull CraftRotateEvent e)
    {
        Craft craft = e.getCraft();
        Movecraft.getInstance().getDirectControlManager().addOrSetCooldown(craft, System.currentTimeMillis() + 500);
        MovecraftLocation originPoint = e.getOriginPoint();
        Location tOP = new Location(craft.getWorld(), originPoint.getX(), originPoint.getY(), originPoint.getZ());
        tOP.setX(tOP.getBlockX() + 0.5);
        tOP.setZ(tOP.getBlockZ() + 0.5);
        PlayerCraft pCraft = (PlayerCraft)e.getCraft();
        Location pilotLockedLoc = new Location(
                craft.getWorld(),
                pCraft.getPilotLockedX(),
                pCraft.getPilotLockedY(),
                pCraft.getPilotLockedZ()
        );


        pilotLockedLoc.subtract(tOP);
        double[] rotatedPilotLoc = MathUtils.rotateVecNoRound(e.getRotation(), pilotLockedLoc.getX(), pilotLockedLoc.getZ());
        pCraft.setPilotLockedX(rotatedPilotLoc[0] + tOP.getX());
        pCraft.setPilotLockedZ(rotatedPilotLoc[1] + tOP.getZ());
    }
}