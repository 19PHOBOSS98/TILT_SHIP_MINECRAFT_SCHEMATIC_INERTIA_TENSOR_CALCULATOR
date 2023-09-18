package main;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.commons.math3.geometry.euclidean.threed.RotationConvention;
import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import net.querz.nbt.io.NBTUtil;
import net.querz.nbt.io.NamedTag;
import net.querz.nbt.tag.CompoundTag;
import net.querz.nbt.tag.IntTag;
import net.querz.nbt.tag.ListTag;
import net.querz.nbt.tag.NumberTag;


public class buildInertiaTensor {
	public static ArrayList<schematicBlock> buildBlockPositionMap(String filepath) {
		ArrayList<schematicBlock> block_position_map = new ArrayList<schematicBlock>();
		try {
			NamedTag named_tag = NBTUtil.read(filepath);
			CompoundTag ct = (CompoundTag) named_tag.getTag();
			ListTag blocks = ct.getListTag("blocks");
			
			for (int i=0 ; i<blocks.size() ; i++) {
				CompoundTag blocks_entry = (CompoundTag) blocks.get(i);
				
				Integer state = blocks_entry.getIntTag("state").asInt();
				
				ListTag<IntTag> coords = blocks_entry.getListTag("pos").asIntTagList();
				long x = Integer.parseInt(coords.get(0).valueToString());
				long y = Integer.parseInt(coords.get(1).valueToString());
				long z = Integer.parseInt(coords.get(2).valueToString());

				Vector3D pos = new Vector3D(x,y,z);
				String block_id = palette_map.get(state);
				Long mass = mass_dictionary.get(block_id);
				block_position_map.add(new schematicBlock(block_id,mass,pos));
			}
			
		} catch (FileNotFoundException e) {
	        e.printStackTrace();
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
		return block_position_map;
	}
	
	public static HashMap<Integer, String> buildPaletteMap(String filepath) {
		HashMap<Integer, String> palette_map = new HashMap<Integer, String>();
		try {
			NamedTag named_tag = NBTUtil.read(filepath);
			CompoundTag ct = (CompoundTag) named_tag.getTag();
			ListTag palette = ct.getListTag("palette");
			
			for (int i=0 ; i<palette.size() ; i++) {
				CompoundTag palette_entry = (CompoundTag) palette.get(i);
				String block_id = palette_entry.getStringTag("Name").getValue();
				palette_map.put(i, block_id);
			}
			
		} catch (FileNotFoundException e) {
	        e.printStackTrace();
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
		return palette_map;
	}


	public static HashMap<String, Long> buildMassDictionary(String filepath) {
		HashMap<String, Long> mass_dictionary = new HashMap<String, Long>();
		JSONParser parser = new JSONParser();
		try {

			JSONArray json_array = (JSONArray) parser.parse(new FileReader(filepath));
	        
	        Iterator<JSONObject> iterator = json_array.iterator();
	        while(iterator.hasNext()) {
	        	 JSONObject entry = iterator.next();
	        	 Long mass = (Long) entry.get("mass");
	        	 String id = (String) entry.get("id");
	        	 mass_dictionary.put(id,  mass);
	        }
	        
		} catch (FileNotFoundException e) {
	        e.printStackTrace();
	    } catch (IOException e) {
	        e.printStackTrace();
	    } catch (ParseException e) {
	    	e.printStackTrace();
	    }
		
		return mass_dictionary;
	
	}
	
	public static String schematic_file_address = "G:\\PROJECTS\\INERTIA_TENSOR_BUILDER\\Inertia_Tensor_Builder\\input_files\\create_schematic_files\\";
	public static String mass_dictionary_address = "G:\\PROJECTS\\INERTIA_TENSOR_BUILDER\\Inertia_Tensor_Builder\\input_files\\mass_dictionary\\mass_dictionary.json";
	public static HashMap<Integer, String> palette_map = new HashMap<Integer, String>();
	public static ArrayList<schematicBlock> block_map = new ArrayList<schematicBlock>();
	public static HashMap<String, Long> mass_dictionary = new HashMap<String, Long>();
	
	public static Vector3D getCenterOfMass() {
		
		double net_mass = 0;
		double mx = 0;
		double my = 0;
		double mz = 0;
		for (int i = 0; i < block_map.size(); i++) {
			
			long mass = block_map.get(i).mass;
			Vector3D pos = block_map.get(i).pos;
			
			//String blockID = block_map.get(i).blockID;
			
			mx += mass*pos.getX();
			my += mass*pos.getY();
			mz += mass*pos.getZ();
			
			net_mass += mass;
		}
		
		//System.out.println("net_mass: "+net_mass);
		
		Vector3D com = new Vector3D(mx,my,mz).scalarMultiply(1/net_mass);
		
		return com;
	}
	
	public static void recenterBlockMap(Vector3D c) {
		for (int i = 0; i < block_map.size(); i++) {

			Vector3D pos = block_map.get(i).pos;
			block_map.get(i).pos = pos.subtract(c);
		}
	}
	
	
	
	public static void reorientBlockMap(){
		Rotation q1 = new Rotation(new Vector3D(1,0,0), (-90*Math.PI)/180, RotationConvention.VECTOR_OPERATOR);
		Vector3D new_z_axis = q1.applyTo(new Vector3D(0,0,1));
		Rotation q2 = new Rotation(new_z_axis, (-45*Math.PI)/180, RotationConvention.VECTOR_OPERATOR).applyTo(q1);
		
		Rotation q3 = new Rotation(new Vector3D(0,0,1), (-45*Math.PI)/180, RotationConvention.VECTOR_OPERATOR);
		
		for (int i = 0; i < block_map.size(); i++) {

			Vector3D pos = block_map.get(i).pos;
			block_map.get(i).pos = q3.applyInverseTo(pos);
		}
	}
	
	
	public static ArrayList<schematicBlock> buildThrusterListFromBlockMap(ArrayList<schematicBlock> map){
		ArrayList<schematicBlock> rockets = new ArrayList<schematicBlock>();
		for (int i = 0; i < map.size(); i++) {
			if(map.get(i).blockID.contains("vs_tournament:thruster")) {
				rockets.add(map.get(i));
			}
		}
		return rockets;
	}
	
	public static ArrayList<schematicBlock> thrusters = new ArrayList<schematicBlock>();
	public static void addToThrusterList(schematicBlock block){
		if(block.blockID.contains("vs_tournament:thruster")) {
			thrusters.add(block);
		}
	}
	
	public static Vector3D recenterPosition(Vector3D p, Vector3D c) {
		return p.subtract(c);
	}
	
	public static Vector3D reorientPosition(Vector3D p, Rotation q){
			return q.applyInverseTo(p);
	}
	
	public static RealMatrix inertia_tensor = MatrixUtils.createRealMatrix(new double[3][3]);
	public static RealMatrix inv_inertia_tensor = MatrixUtils.createRealMatrix(new double[3][3]);
	
	public static void buildInertiaTensor(){
		String schematic_file = schematic_file_address+"geofish_segment_7_it.nbt";

		palette_map = buildPaletteMap(schematic_file);
		mass_dictionary = buildMassDictionary(mass_dictionary_address);
		
		block_map = buildBlockPositionMap(schematic_file);
		Vector3D com = getCenterOfMass();
		
		recenterBlockMap(com);
		
		//reorientBlockMap();
		
		double Ixx=0;
		double Iyy=0;
		double Izz=0;
		
		double Ixy=0;
		
		double Iyz=0;
		
		double Izx=0;
		/*//don't need to worry about pre-rotating the inertia tensors anymore
		Rotation q1 = new Rotation(new Vector3D(0,0,1), (-90*Math.PI)/180, RotationConvention.VECTOR_OPERATOR);
		Vector3D new_y_axis = q1.applyTo(new Vector3D(0,1,0));
		Rotation q2 = new Rotation(new_y_axis, (-45*Math.PI)/180, RotationConvention.VECTOR_OPERATOR).applyTo(q1);
		
		Rotation q3 = new Rotation(new Vector3D(0,1,0), (0*Math.PI)/180, RotationConvention.VECTOR_OPERATOR);
		*/
		for (int i = 0; i < block_map.size(); i++) {

			long mass = block_map.get(i).mass;
			
			Vector3D pos = block_map.get(i).pos;

			//pos = recenterPosition(pos,com);
			
			//pos = reorientPosition(pos, q3);
			
			double x = pos.getX();
			double y = pos.getY();
			double z = pos.getZ();
			
			Ixx += mass*((y*y)+(z*z));
			Iyy += mass*((x*x)+(z*z));
			Izz += mass*((x*x)+(y*y));
			Ixy += -mass*x*y;
			Iyz += -mass*y*z;
			Izx += -mass*z*x;
		}
		
		double tensor[][] = new double[3][3];
		tensor[0][0] = Ixx;
		tensor[0][1] = Ixy;
		tensor[0][2] = Izx;

		tensor[1][0] = Ixy;
		tensor[1][1] = Iyy;
		tensor[1][2] = Iyz;
        
		tensor[2][0] = Izx;
		tensor[2][1] = Iyz;
		tensor[2][2] = Izz;
		//System.out.println("not rotated:");
		inertia_tensor = MatrixUtils.createRealMatrix(tensor);
		System.out.println("LOCAL_INERTIA_TENSOR = \r\n"
				+ "{\r\n"
				+ "x=vector.new("+inertia_tensor.getData()[0][0]+","+inertia_tensor.getData()[0][1]+","+inertia_tensor.getData()[0][2]
				+"),\ny=vector.new("+inertia_tensor.getData()[1][0]+","+inertia_tensor.getData()[1][1]+","+inertia_tensor.getData()[1][2]
				+"),\nz=vector.new("+inertia_tensor.getData()[2][0]+","+inertia_tensor.getData()[2][1]+","+inertia_tensor.getData()[2][2]+")\n},");
		inv_inertia_tensor = MatrixUtils.inverse(inertia_tensor);
		
		System.out.println("LOCAL_INV_INERTIA_TENSOR = \n{\nx=vector.new("+inv_inertia_tensor.getData()[0][0]+","+inv_inertia_tensor.getData()[0][1]+","+inv_inertia_tensor.getData()[0][2]
				+"),\ny=vector.new("+inv_inertia_tensor.getData()[1][0]+","+inv_inertia_tensor.getData()[1][1]+","+inv_inertia_tensor.getData()[1][2]
				+"),\nz=vector.new("+inv_inertia_tensor.getData()[2][0]+","+inv_inertia_tensor.getData()[2][1]+","+inv_inertia_tensor.getData()[2][2]+")\n},");
		
		
		Rotation q_rotation = new Rotation(new Vector3D(0,1,0), (45*Math.PI)/180, RotationConvention.VECTOR_OPERATOR);
		
		Vector3D c1 = new Vector3D(inertia_tensor.getData()[0][0],inertia_tensor.getData()[1][0],inertia_tensor.getData()[2][0]);
		Vector3D c2 = new Vector3D(inertia_tensor.getData()[0][1],inertia_tensor.getData()[1][1],inertia_tensor.getData()[2][1]);
		Vector3D c3 = new Vector3D(inertia_tensor.getData()[0][2],inertia_tensor.getData()[1][2],inertia_tensor.getData()[2][2]);
		
		Vector3D cc1 = q_rotation.applyInverseTo(c1);
		Vector3D cc2 = q_rotation.applyInverseTo(c2);
		Vector3D cc3 = q_rotation.applyInverseTo(c3);
		
		Vector3D rr1 = new Vector3D(cc1.getX(),cc2.getX(),cc3.getX());
		Vector3D rr2 = new Vector3D(cc1.getY(),cc2.getY(),cc3.getY());
		Vector3D rr3 = new Vector3D(cc1.getZ(),cc2.getZ(),cc3.getZ());
		
		Vector3D rrr1 = q_rotation.applyInverseTo(rr1);
		Vector3D rrr2 = q_rotation.applyInverseTo(rr2);
		Vector3D rrr3 = q_rotation.applyInverseTo(rr3);
		
		Vector3D ccc1 = new Vector3D(rrr1.getX(),rrr2.getX(),rrr3.getX());
		Vector3D ccc2 = new Vector3D(rrr1.getY(),rrr2.getY(),rrr3.getY());
		Vector3D ccc3 = new Vector3D(rrr1.getZ(),rrr2.getZ(),rrr3.getZ());
		
		/*
		System.out.println("\n\nrotated:");
		double tensor2[][] = new double[3][3];
		tensor2[0][0] = rrr1.getX();
		tensor2[0][1] = rrr1.getY();
		tensor2[0][2] = rrr1.getZ();

		tensor2[1][0] = rrr2.getX();
		tensor2[1][1] = rrr2.getY();
		tensor2[1][2] = rrr2.getZ();
        
		tensor2[2][0] = rrr3.getX();
		tensor2[2][1] = rrr3.getY();
		tensor2[2][2] = rrr3.getZ();
		inertia_tensor = MatrixUtils.createRealMatrix(tensor2);
		System.out.println("x=vector.new("+inertia_tensor.getData()[0][0]+","+inertia_tensor.getData()[0][1]+","+inertia_tensor.getData()[0][2]
				+"),\ny=vector.new("+inertia_tensor.getData()[1][0]+","+inertia_tensor.getData()[1][1]+","+inertia_tensor.getData()[1][2]
				+"),\nz=vector.new("+inertia_tensor.getData()[2][0]+","+inertia_tensor.getData()[2][1]+","+inertia_tensor.getData()[2][2]+")");
		inv_inertia_tensor = MatrixUtils.inverse(inertia_tensor);
		
		System.out.print("\nINV: \nx=vector.new("+inv_inertia_tensor.getData()[0][0]+","+inv_inertia_tensor.getData()[0][1]+","+inv_inertia_tensor.getData()[0][2]
				+"),\ny=vector.new("+inv_inertia_tensor.getData()[1][0]+","+inv_inertia_tensor.getData()[1][1]+","+inv_inertia_tensor.getData()[1][2]
				+"),\nz=vector.new("+inv_inertia_tensor.getData()[2][0]+","+inv_inertia_tensor.getData()[2][1]+","+inv_inertia_tensor.getData()[2][2]+")");

		*/
		
		//ArrayList<schematicBlock> thrusterList = buildThrusterListFromBlockMap(block_map);
	}
	
	
	
	
	
	public static void main(String[] args) {

		
		
		buildInertiaTensor();
		
		
		
	}

}
