/*
 * Copyright (C) 2011-2012 Keyle
 *
 * This file is part of MyPet
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
 * along with MyPet. If not, see <http://www.gnu.org/licenses/>.
 */

package de.Keyle.MyPet.entity.types.pig;

import de.Keyle.MyPet.entity.types.MyPet;
import de.Keyle.MyPet.entity.types.MyPetType;
import de.Keyle.MyPet.util.MyPetPlayer;
import net.minecraft.server.v1_4_5.NBTTagCompound;

public class MyPig extends MyPet
{
    protected boolean hasSaddle = false;
    protected boolean isBaby = false;

    public MyPig(MyPetPlayer petOwner)
    {
        super(petOwner);
        this.petName = "Pig";
    }

    public boolean hasSaddle()
    {
        if (status == PetState.Here)
        {
            return ((CraftMyPig) getCraftPet()).hasSaddle();
        }
        else
        {
            return hasSaddle;
        }
    }

    public void setSaddle(boolean saddle)
    {
        if (status == PetState.Here)
        {
            ((CraftMyPig) getCraftPet()).setSaddle(saddle);
        }
        this.hasSaddle = saddle;
    }

    public boolean isBaby()
    {
        if (status == PetState.Here)
        {
            return ((CraftMyPig) getCraftPet()).isBaby();
        }
        else
        {
            return isBaby;
        }
    }

    public void setBaby(boolean flag)
    {
        if (status == PetState.Here)
        {
            ((CraftMyPig) getCraftPet()).setBaby(flag);
        }
        this.isBaby = flag;
    }

    @Override
    public NBTTagCompound getExtendedInfo()
    {
        NBTTagCompound info = new NBTTagCompound("Info");
        info.setBoolean("Saddle", hasSaddle());
        info.setBoolean("Baby", isBaby());
        return info;
    }

    @Override
    public void setExtendedInfo(NBTTagCompound info)
    {
        setSaddle(info.getBoolean("Saddle"));
        setBaby(info.getBoolean("Baby"));
    }

    @Override
    public MyPetType getPetType()
    {
        return MyPetType.Pig;
    }

    @Override
    public String toString()
    {
        return "MyPig{owner=" + getOwner().getName() + ", name=" + petName + ", exp=" + experience.getExp() + "/" + experience.getRequiredExp() + ", lv=" + experience.getLevel() + ", status=" + status.name() + ", skilltree=" + (skillTree != null ? skillTree.getName() : "-") + ", saddle=" + hasSaddle() + ", baby=" + isBaby() + "}";
    }
}