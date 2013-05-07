package redis.clients.jedis.tests;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import redis.clients.jedis.tests.commands.JedisCommandTestBase;

public class LuaScriptTest extends JedisCommandTestBase {
	@SuppressWarnings("unchecked")
	@Test
	public void testJedisListReturns() throws Exception {
		Object result = jedis.eval("return {ARGV[1], tonumber(ARGV[2])}", 0,
				"string1", "1234");
		List<String> expected = Arrays.asList("string1", "1234");
		assertEquals(result, expected);
	}
}
