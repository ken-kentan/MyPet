/*
 * This file is part of MyPet
 *
 * Copyright © 2011-2019 Keyle
 * MyPet is licensed under the GNU Lesser General Public License.
 *
 * MyPet is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MyPet is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package de.Keyle.MyPet.compat.v1_11_R1.entity.types;

import com.google.common.base.Optional;
import de.Keyle.MyPet.api.Configuration;
import de.Keyle.MyPet.api.entity.EntitySize;
import de.Keyle.MyPet.api.entity.MyPet;
import de.Keyle.MyPet.api.entity.types.MyZombieHorse;
import de.Keyle.MyPet.compat.v1_11_R1.entity.EntityMyPet;
import net.minecraft.server.v1_11_R1.*;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_11_R1.inventory.CraftItemStack;

import java.util.UUID;

@EntitySize(width = 1.4F, height = 1.6F)
public class EntityMyZombieHorse extends EntityMyPet implements IJumpable {
    protected static final DataWatcherObject<Boolean> ageWatcher = DataWatcher.a(EntityMyZombieHorse.class, DataWatcherRegistry.h);
    protected static final DataWatcherObject<Byte> saddleChestWatcher = DataWatcher.a(EntityMyZombieHorse.class, DataWatcherRegistry.a);
    protected static final DataWatcherObject<Optional<UUID>> ownerWatcher = DataWatcher.a(EntityMyZombieHorse.class, DataWatcherRegistry.m);

    int soundCounter = 0;
    int rearCounter = -1;

    public EntityMyZombieHorse(World world, MyPet myPet) {
        super(world, myPet);
    }

    /**
     * Possible visual horse effects:
     * 4 saddle
     * 8 chest
     * 32 head down
     * 64 rear
     * 128 mouth open
     */
    protected void applyVisual(int value, boolean flag) {
        int i = this.datawatcher.get(saddleChestWatcher);
        if (flag) {
            this.datawatcher.set(saddleChestWatcher, (byte) (i | value));
        } else {
            this.datawatcher.set(saddleChestWatcher, (byte) (i & (~value)));
        }
    }

    public boolean attack(Entity entity) {
        boolean flag = false;
        try {
            flag = super.attack(entity);
            if (flag) {
                applyVisual(64, true);
                rearCounter = 10;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }

    @Override
    protected String getDeathSound() {
        return "entity.zombie_horse.death";
    }

    @Override
    protected String getHurtSound() {
        return "entity.zombie_horse.hurt";
    }

    protected String getLivingSound() {
        return "entity.zombie_horse.ambient";
    }


    public boolean handlePlayerInteraction(final EntityHuman entityhuman, EnumHand enumhand, final ItemStack itemStack) {
        if (super.handlePlayerInteraction(entityhuman, enumhand, itemStack)) {
            return true;
        }

        if (itemStack != null && canUseItem()) {
            if (itemStack.getItem() == Items.SADDLE && !getMyPet().hasSaddle() && !getMyPet().isBaby() && getOwner().getPlayer().isSneaking() && canEquip()) {
                getMyPet().setSaddle(CraftItemStack.asBukkitCopy(itemStack));
                if (!entityhuman.abilities.canInstantlyBuild) {
                    itemStack.subtract(1);
                    if (itemStack.getCount() <= 0) {
                        entityhuman.inventory.setItem(entityhuman.inventory.itemInHandIndex, ItemStack.a);
                    }
                }
                return true;
            } else if (itemStack.getItem() == Items.SHEARS && getOwner().getPlayer().isSneaking() && canEquip()) {
                if (getMyPet().hasSaddle()) {
                    EntityItem entityitem = new EntityItem(this.world, this.locX, this.locY + 1, this.locZ, CraftItemStack.asNMSCopy(getMyPet().getSaddle()));
                    entityitem.pickupDelay = 10;
                    entityitem.motY += (double) (this.random.nextFloat() * 0.05F);
                    this.world.addEntity(entityitem);
                }

                makeSound("entity.sheep.shear", 1.0F, 1.0F);
                getMyPet().setSaddle(null);
                if (!entityhuman.abilities.canInstantlyBuild) {
                    itemStack.damage(1, entityhuman);
                }

                return true;
            } else if (Configuration.MyPet.ZombieHorse.GROW_UP_ITEM.compare(itemStack) && getMyPet().isBaby() && getOwner().getPlayer().isSneaking()) {
                if (!entityhuman.abilities.canInstantlyBuild) {
                    itemStack.subtract(1);
                    if (itemStack.getCount() <= 0) {
                        entityhuman.inventory.setItem(entityhuman.inventory.itemInHandIndex, ItemStack.a);
                    }
                }
                getMyPet().setBaby(false);
                return true;
            }
        }
        return false;
    }

    private int getHorseArmorId(org.bukkit.inventory.ItemStack itemstack) {
        if (itemstack == null) {
            return 0;
        }
        Material item = itemstack.getType();

        return item == Material.DIAMOND_BARDING ? 3 : item == Material.GOLD_BARDING ? 2 : item == Material.IRON_BARDING ? 1 : 0;
    }

    protected void initDatawatcher() {
        super.initDatawatcher();
        this.datawatcher.register(ageWatcher, false);
        this.datawatcher.register(saddleChestWatcher, (byte) 0);
        this.datawatcher.register(ownerWatcher, Optional.absent());
    }

    protected void initAttributes() {
        super.initAttributes();
        this.getAttributeMap().b(EntityHorseAbstract.attributeJumpStrength);
    }

    @Override
    public void updateVisuals() {
        this.datawatcher.set(ageWatcher, getMyPet().isBaby());
        applyVisual(4, getMyPet().hasSaddle());
    }

    public void onLivingUpdate() {
        boolean oldRiding = hasRider;
        super.onLivingUpdate();
        if (!hasRider)
            if (rearCounter > -1 && rearCounter-- == 0) {
                applyVisual(64, false);
                rearCounter = -1;
            }
        if (oldRiding != hasRider) {
            if (hasRider) {
                applyVisual(4, true);
            } else {
                applyVisual(4, getMyPet().hasSaddle());
            }
        }
    }

    @Override
    public void playStepSound(BlockPosition pos, Block block) {
        SoundEffectType soundeffecttype = block.getStepSound();
        if (this.world.getType(pos) == Blocks.SNOW) {
            soundeffecttype = Blocks.SNOW_LAYER.getStepSound();
        }
        if (isVehicle()) {
            this.soundCounter += 1;
            if (this.soundCounter % 3 != 0) {
                if (this.soundCounter <= 5) {
                    a(SoundEffects.cL, soundeffecttype.a() * 0.15F, soundeffecttype.b());
                }
            }
        } else if (soundeffecttype == SoundEffectType.a) {
            a(SoundEffects.cL, soundeffecttype.a() * 0.15F, soundeffecttype.b());
        } else {
            a(SoundEffects.cK, soundeffecttype.a() * 0.15F, soundeffecttype.b());
        }
    }

    public MyZombieHorse getMyPet() {
        return (MyZombieHorse) myPet;
    }

    /* Jump power methods */
    @Override
    public boolean a() {
        return true;
    }

    @Override
    public void b(int i) {
        this.jumpPower = i;
    }

    @Override
    public void q_() {
    }
}