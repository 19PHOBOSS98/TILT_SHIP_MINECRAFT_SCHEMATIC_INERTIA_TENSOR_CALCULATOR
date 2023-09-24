# TILT_SHIP_MINECRAFT_SCHEMATIC_INERTIA_TENSOR_CALCULATOR
Until the Valkyrien Skies 2 Computer addon releases an update to expose a ships inertia tensor, ya'll need to calculate it manually. Here's what I use to do it.

## SETUP JAVA PROJECT
1. Open `INERTIA_TENSOR_BUILDER/Inertia_Tensor_Builder/` with your prefered IDE (I used IntelliJ IDEA)

![image](https://github.com/19PHOBOSS98/TILT_SHIP_MINECRAFT_SCHEMATIC_INERTIA_TENSOR_CALCULATOR/assets/37253663/c0521a4b-5701-4839-b77c-4d55b9d0db66)

2. Open `buildInertiaTensor.java`

![image](https://github.com/19PHOBOSS98/TILT_SHIP_MINECRAFT_SCHEMATIC_INERTIA_TENSOR_CALCULATOR/assets/37253663/3cadaa63-d0d0-4a24-a3ba-e70a1cce6bcf)


## HOW TO CALCULATE YOUR BUILD'S INERTIA TENSOR
1. Use the included VS2 weighing scale to add new blocks to the `mass_dictionary`. Follow the instructions on how to setup and use it
2. Use CreateMod-Schematics to create a schematic file of your build. 
    + Remember, NOT to include Create-contraptions in the schematic files. This might mess with the Inertia Tensor calculation since contraptions don't contribute to the overall weight of a VS2-ship.
3. Copy the file over to `INERTIA_TENSOR_BUILDER\Inertia_Tensor_Builder\input_files\create_schematic_files\`
4. At the `buildInertiaTensor()` in the `buildInertiaTensor.class` type in the name of your build schematic:

![image](https://github.com/19PHOBOSS98/TILT_SHIP_MINECRAFT_SCHEMATIC_INERTIA_TENSOR_CALCULATOR/assets/37253663/ab60a345-515b-43a3-a38d-a78968ed232d)


5. Run the class:

![image](https://github.com/19PHOBOSS98/TILT_SHIP_MINECRAFT_SCHEMATIC_INERTIA_TENSOR_CALCULATOR/assets/37253663/2dfb6bfc-3459-49cd-a662-681c9a929ace)



6. It should print out the following in the console:

![image](https://github.com/19PHOBOSS98/TILT_SHIP_MINECRAFT_SCHEMATIC_INERTIA_TENSOR_CALCULATOR/assets/37253663/ede70937-2899-45e0-bc20-1d992c1fdff7)

7. Copy that over to your drone script as your inertia tensors:

![image](https://github.com/19PHOBOSS98/TILT_SHIP_MINECRAFT_SCHEMATIC_INERTIA_TENSOR_CALCULATOR/assets/37253663/9a617305-66a4-4974-a6ce-3950706810d5)


## IN-GAME VALKYRIEN SKIES 2 WEIGHING SCALE
![2023-07-25_19 10 29](https://github.com/19PHOBOSS98/TILT_SHIP_MINECRAFT_SCHEMATIC_INERTIA_TENSOR_CALCULATOR/assets/37253663/35df12e8-1dcb-4cf8-b100-23abf2f12136)
### Minimum Required Mods
+ Valkyrien Skies 2
  + Eureka
  + Computers
+ Computercraft

  
### Setup And How To Use
1. Build `Block Weighing Scale/0weighing_scale_1.nbt` and assemble it as a VS2 ship (use a VS2-Eureka Helm block). Take note of the Computercraft Turtle's Computer ID.
2. Drop `rename_to_computer_id` folder and its contents in your save world's `computercraft/computer` folder and rename it to the weighing scale Turtle's Computer ID
3. In-game run `scale.lua` on the weighing scale's Turtle
4. Place the block that you want to weigh infront of the Turtle
5. The scale will save the readings in the `mass_dictionary` file
6. Copy the `mass_dictionary` file over to `INERTIA_TENSOR_BUILDER\Inertia_Tensor_Builder\input_files\mass_dictionary\` to be used in calculating the inertia tensors



