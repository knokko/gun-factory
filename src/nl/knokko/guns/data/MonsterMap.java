package nl.knokko.guns.data;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

public class MonsterMap extends HashMap<String,WaveMonster> {

	private static final long serialVersionUID = 5425677400827806636L;
	
	public void save04(DataOutputStream output) throws IOException {
		output.writeInt(size());
		System.out.println("MonsterMap: saving " + size() + " monsters");
		Set<Entry<String,WaveMonster>> entrySet = entrySet();
		for(Entry<String,WaveMonster> entry : entrySet){
			output.writeUTF(entry.getKey());
			entry.getValue().save04(output);
		}
	}
	
	public void load04(DataInputStream input) throws IOException {
		clear();
		int size = input.readInt();
		System.out.println("MonsterMap: loading " + size + " monsters");
		for(int index = 0; index < size; index++){
			String name = input.readUTF();
			put(name, WaveMonster.load04(input, name));
		}
	}
}