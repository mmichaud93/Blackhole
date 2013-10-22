import com.esotericsoftware.kryo.Kryo;


public class Register {
	// a global class to register objects with the kryo system
	public static void RegisterObjects(Kryo kryo) {
		kryo.register(Request.class);
		kryo.register(byte[].class);
	}
}
