package lol.ventura.features.combat;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lol.ventura.features.events.TickEvent;
import lol.ventura.features.modules.combat.KillAura;
import lol.ventura.foundation.GameAccessor;
import lol.ventura.foundation.Service;
import lol.ventura.foundation.event.EventBus;
import lol.ventura.foundation.event.IEventListener;
import lol.ventura.foundation.module.ModuleRepository;
import lol.ventura.misc.storage.StorageService;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public class CombatService extends Service implements GameAccessor {

    @Getter @Setter
    private static CombatService instance;
    private final KillAura aura;

    @Getter
    private Collection<LivingEntity> targets = new ArrayList<>();

    public CombatService() {
        EventBus.getInstance().register(this);

        this.aura = ModuleRepository.getInstance().getModule(KillAura.class);
    }

    private Collection<LivingEntity> findTargets() {
        List<LivingEntity> targets = mc.world.getEntitiesByClass(LivingEntity.class, mc.player.getBoundingBox().expand(aura.getRange().getValue()), entity -> entity != mc.player);
        return targets.stream()
                .filter(this::isValid)
                .sorted(Comparator.comparingDouble(entity -> entity.squaredDistanceTo(mc.player))).toList();
    }

    private boolean isValid(LivingEntity targetEntity) {
        if (targetEntity == null || !targetEntity.isAlive() || getDistance(targetEntity) >= Math.pow(aura.getRange().getValue(), 2)) {
            return false;
        }

        if (targetEntity instanceof PlayerEntity player) {
            List<String> friends = getFriends();

            if (friends.contains(player.getGameProfile().getName())) {
                return false;
            }

            return aura.getTargets().getValue().contains(KillAura.Targets.PLAYERS);
        }

        if (targetEntity instanceof MobEntity) {
            return aura.getTargets().getValue().contains(KillAura.Targets.MOBS);
        }

        if (targetEntity instanceof PassiveEntity) {
            return aura.getTargets().getValue().contains(KillAura.Targets.ANIMALS);
        }

        return false;
    }

    private float getDistance(Entity targetEntity) {
        return (float) mc.player.getPos().squaredDistanceTo(targetEntity.getPos());
    }

    private List<String> getFriends() {
        List<String> friends = new ArrayList<>();
        try {
            JsonObject friendData = StorageService.getInstance()
                    .getForClass(lol.ventura.features.command.FriendCommand.class);
            JsonArray friendArray = friendData.getAsJsonArray("friends");
            if (friendArray != null) {
                for (JsonElement element : friendArray) {
                    friends.add(element.getAsString());
                }
            }
        } catch (Exception e) {

        }
        return friends;
    }

    private final IEventListener<TickEvent> tickUpdate = event -> {
        this.targets = findTargets();
    };
}