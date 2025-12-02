package net.countercraft.movecraft.async;

import net.countercraft.movecraft.CruiseDirection;
import net.countercraft.movecraft.Movecraft;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.CraftManager;
import net.countercraft.movecraft.craft.PlayerCraft;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

public class DirectControlManager extends BukkitRunnable implements Listener {
    private final Map<Craft, Player> controlledCrafts = new HashMap<>();
    private final Map<Craft, Long> cooldowns = new HashMap<>();
    private final Map<Player, Long> sneakTimes = new HashMap<>();
    @Override
    public void run() {
        if (controlledCrafts.isEmpty()) return;
        Bukkit.getLogger().info(controlledCrafts.toString());
        for (Map.Entry<Craft, Player> controlledCraft : controlledCrafts.entrySet())
        {
            if(controlledCraft.getKey() == null || controlledCraft.getValue() == null)
                controlledCrafts.remove(controlledCraft.getKey());
            Player player = controlledCraft.getValue();
            PlayerCraft pCraft = (PlayerCraft)controlledCraft.getKey();

            World world = pCraft.getWorld();

            Location pilotLockLocation = new Location(world, pCraft.getPilotLockedX(), pCraft.getPilotLockedY(), pCraft.getPilotLockedZ(), player.getYaw(), player.getPitch());

            double movedX = player.getLocation().getX() - pCraft.getPilotLockedX();
            double movedY = player.getLocation().getY() - pCraft.getPilotLockedY();
            double movedZ = player.getLocation().getZ() - pCraft.getPilotLockedZ();

            Movecraft.getInstance().getSmoothTeleport().teleport(controlledCraft.getValue(), pilotLockLocation);

            if(cooldowns.containsKey(pCraft))
            {
                if(cooldowns.get(pCraft) > System.currentTimeMillis()) return;
                else cooldowns.remove(pCraft);
            }
            CruiseDirection xDir = CruiseDirection.NONE;
            CruiseDirection zDir = CruiseDirection.NONE;

            if (movedX > 0.15)
                xDir = CruiseDirection.EAST;
            else if (movedX < -0.15)
                xDir = CruiseDirection.WEST;
            if (movedZ > 0.15)
                zDir = CruiseDirection.SOUTH;
            else if (movedZ < -0.15)
                zDir = CruiseDirection.NORTH;

            if(Math.abs(movedX) > 0 && !pCraft.getCruising()|| Math.abs(movedZ) > 0 && !pCraft.getCruising() || movedY > 0 && !pCraft.getCruising())
                pCraft.setCruising(true);

            CruiseDirection cd = pCraft.getCruiseDirection();
            if(xDir != CruiseDirection.NONE && zDir != CruiseDirection.NONE)
            {
                if (xDir == CruiseDirection.EAST)
                {
                    if (zDir == CruiseDirection.NORTH) cd = CruiseDirection.NORTHEAST;
                    else cd = CruiseDirection.SOUTHEAST;
                }
                else
                {
                    if (zDir == CruiseDirection.NORTH) cd = CruiseDirection.NORTHWEST;
                    else cd = CruiseDirection.SOUTHWEST;
                }
            }
            else if (xDir != CruiseDirection.NONE) cd = xDir;
            else if (zDir != CruiseDirection.NONE) cd = zDir;

            if(movedY > 0.15) cd = CruiseDirection.UP;

            if(player.isSneaking()) {
                if(!sneakTimes.containsKey(player))
                    sneakTimes.put(player, System.currentTimeMillis() + 250);
                else if(sneakTimes.containsKey(player) && System.currentTimeMillis() > sneakTimes.get(player)){
                    cd = CruiseDirection.DOWN;
                    if(!pCraft.getCruising()) pCraft.setCruising(true);
                }
            }
            else {
                if(sneakTimes.containsKey(player)) {
                    if(System.currentTimeMillis() < sneakTimes.get(player)) {
                        pCraft.setCruising(false);
                    }
                    sneakTimes.remove(player);
                }
            }

            if (cd != pCraft.getCruiseDirection())
                pCraft.setCruiseDirection(cd);
        }
    }

    public void addControlledCraft(Craft c, Player p) { controlledCrafts.put(c, p); }

    public void removeControlledCraft(Craft c) { controlledCrafts.remove(c); }

    public void addOrSetCooldown(Craft c, Long endTime) { cooldowns.put(c, endTime); }
}
