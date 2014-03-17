MalisisDoors
============


Adds animations to doors.
Adds sensors which detect players passing under and send a redstone signal to the block they are attached to
Adds vanishing blocks. Frames can be crafted and placed in the world, when supplied with redstone current, the 
vanish into thin air make all neighboring vanishing blocks vanishing as well. When the redstone current stop, 
the frames go back to being solid blocks. A frame can be activated with normal blocks which are used to "paint"
the frame. Different types of frames implies different vanishing propagation behavior :
- wood frames propagate to all frames around them
- iron frames propagate to all frames around painted with the same block
- gold frames propagate to all frames around painted with the same block and the same metadata (ie red wool would not make a blue wool vanish)
Frames not painted automatically propagate their state.