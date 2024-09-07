package com.ghostipedia.cosmiccore.api.machine.multiblock;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import lombok.Getter;
@Getter
public class IrisMultiblockMachine extends WorkableElectricMultiblockMachine {

        public IrisMultiblockMachine(IMachineBlockEntity holder) {
            super(holder);
        }

        @Override
        public void onStructureFormed() {
            super.onStructureFormed();

            }

 }

