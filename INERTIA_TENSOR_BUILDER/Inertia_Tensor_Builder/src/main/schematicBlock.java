package main;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

public class schematicBlock {
	public String blockID;
	public Long mass;
	public Vector3D pos;

	@Override
	public String toString() {
		return "[blockID=" + blockID + ", mass=" + mass + ", pos=" + pos + "]";
	}

	public schematicBlock(String blockID, Long mass, Vector3D pos) {
		super();
		this.blockID = blockID;
		this.mass = mass;
		this.pos = pos;
	}
	
}
